package Assignment;

import java.util.ArrayList;
import java.util.List;

public class BattleEngine {
    private List<Player> players;
    private List<Enemy> activeEnemies;
    private List<Enemy> backupEnemies;
    private TurnOrderStrategy turnStrategy;
    private BattleEngineInterface ui;
    private int currentRound;

    // Constructor utilizes Dependency Injection for the UI and Turn Strategy
    public BattleEngine(List<Player> players, List<Enemy> initialEnemies, List<Enemy> backupEnemies,
            TurnOrderStrategy turnStrategy, BattleEngineInterface ui) {
        this.players = players;
        this.activeEnemies = initialEnemies;
        this.backupEnemies = backupEnemies;
        this.turnStrategy = turnStrategy;
        this.ui = ui;
        this.currentRound = 1;
    }

    public void startBattle() {
        ui.showMessage("Battle Initiated!");

        while (!isGameOver()) {
            processRound();
            currentRound++;
        }

        resolveBattleEnd();
    }

    private void processRound() {
        ui.displayBattleState(players, activeEnemies, currentRound);

        // 1. Combine all alive entities to determine turn order
        List<Combatant> allCombatants = new ArrayList<>();
        allCombatants.addAll(players);
        allCombatants.addAll(activeEnemies);

        // Polymorphism: Engine relies on the interface abstraction to sort
        List<Combatant> turnOrder = turnStrategy.determineOrder(allCombatants);

        // 2. Execute turns
        for (Combatant currentCombatant : turnOrder) {
            // Check if the battle ended mid-round or if combatant died before their turn
            if (isGameOver() || currentCombatant.isDefeated()) {
                continue;
            }

            executeTurn(currentCombatant);

            // Handle backup spawn immediately if an attack wiped out the initial wave
            handleBackupSpawn();
        }
    }

    private void executeTurn(Combatant combatant) {
        ui.showMessage("\n--- " + combatant.getName() + "'s turn ---");

        // Process pre-turn status effects (e.g., ticking down stun duration)
        // Assume ableToAct() returns false if the character is stunned and
        // must skip
        boolean canAct = combatant.ableToAct();

        if (!canAct) {
            ui.showMessage(combatant.getName() + " skips their turn due to a status effect!");
            return;
        }

        // Liskov Substitution Principle / Polymorphism: Differentiate between Player
        // and Enemy AI
        if (combatant instanceof Player) {
            Player player = (Player) combatant;

            // UI handles the choice, Engine just receives the Action interface
            Action chosenAction = ui.getPlayerActionChoice(player);
            Combatant target = ui.getTargetChoice(activeEnemies); // Realistically, actions like 'Defend' target self

            chosenAction.execute(player, target);
            player.reduceCooldown(); // Tick down skill cooldown if a turn was taken

        } else if (combatant instanceof Enemy) {
            Enemy enemy = (Enemy) combatant;

            // Simple enemy AI: Always Basic Attack the first alive player
            Action enemyAction = new BasicAttackAction();
            Combatant target = players.get(0);

            ui.showMessage(enemy.getName() + " attacks " + target.getName() + "!");
            enemyAction.execute(enemy, target);
        }
    }

    private void handleBackupSpawn() {
        // If all active enemies are dead, but backups exist, bring them in
        // simultaneously
        boolean allActiveDead = activeEnemies.stream().allMatch(Combatant::isDefeated);

        if (allActiveDead && !backupEnemies.isEmpty()) {
            ui.showMessage("\n⚠️ BACKUP SPAWN TRIGGERED! New enemies enter the arena! ⚠️");
            activeEnemies.addAll(backupEnemies);
            backupEnemies.clear(); // Empty the backup list so they don't spawn again
        }
    }

    private boolean isGameOver() {
        boolean allPlayersDead = players.stream().allMatch(Combatant::isDefeated);
        boolean allEnemiesDead = activeEnemies.stream().allMatch(Combatant::isDefeated) && backupEnemies.isEmpty();

        return allPlayersDead || allEnemiesDead;
    }

    private void resolveBattleEnd() {
        boolean playerWon = !players.get(0).isDefeated(); // Assuming single player character

        if (playerWon) {
            Player p = players.get(0);
            ui.showVictoryScreen(currentRound, p.getHp(), p.getMaxHp());
        } else {
            int remainingEnemies = (int) activeEnemies.stream().filter(e -> !e.isDefeated()).count();
            remainingEnemies += backupEnemies.size();
            ui.showDefeatScreen(currentRound, remainingEnemies);
        }
    }
}