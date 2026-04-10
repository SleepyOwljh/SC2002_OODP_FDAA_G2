import java.util.ArrayList;
import java.util.List;

public class BattleEngine {
    private final List<Player> players;
    private final List<Enemy> activeEnemies;
    private final List<Enemy> backupEnemies;
    private final TurnOrderStrategy turnOrder;
    private final BattleEngineInterface ui;
    private int currentRound;
    private boolean backupSpawnTriggered;

    public BattleEngine(List<Player> players, List<Enemy> initialEnemies, List<Enemy> backupEnemies,
            TurnOrderStrategy turnOrder, BattleEngineInterface ui) {
        this.players = players == null ? new ArrayList<Player>() : new ArrayList<Player>(players);
        this.activeEnemies = initialEnemies == null ? new ArrayList<Enemy>() : new ArrayList<Enemy>(initialEnemies);
        this.backupEnemies = backupEnemies == null ? new ArrayList<Enemy>() : new ArrayList<Enemy>(backupEnemies);
        this.turnOrder = turnOrder == null ? new SpeedBasedTurnOrder() : turnOrder;
        this.ui = ui == null ? new BattleEngineInterface() : ui;
        this.ui.setEnemyContext(this.activeEnemies, this.backupEnemies);
        this.currentRound = 1;
        this.backupSpawnTriggered = this.backupEnemies.isEmpty();
    }

    public int startBattle(int difficulty) {
        if (players.isEmpty()) {
            ui.showMessage("No players are available to enter battle.");
            return 3;
        }

        if (activeEnemies.isEmpty()) {
            initializeEncounter(difficulty);
        }

        ui.showBattleOverview(players.get(0), activeEnemies, difficulty, turnOrder);

        currentRound = 1;
        while (!isGameOver()) {
            ui.displayBattleState(players, activeEnemies, currentRound);
            processRound();
            showRoundSummary();
            if (!isGameOver()) {
                currentRound++;
            }
        }

        return showEndScreen();
    }

    public void processRound() {
        List<Combatant> combatants = new ArrayList<Combatant>();
        for (Player player : players) {
            if (player.isAlive()) {
                combatants.add(player);
            }
        }

        for (Enemy enemy : activeEnemies) {
            if (enemy.isAlive()) {
                combatants.add(enemy);
            }
        }

        List<Combatant> roundOrder = turnOrder.determineOrder(combatants);
        for (Combatant currentCombatant : roundOrder) {
            if (isGameOver()) {
                return;
            }

            executeTurn(currentCombatant);
            handleBackupSpawn();
        }
    }

    // ─── A1 + A2: Proper output for eliminated and stunned combatants ───
    private void executeTurn(Combatant currentCombatant) {
        if (currentCombatant == null) {
            return;
        }

        // A1: Print skip line for eliminated combatants
        if (!currentCombatant.isAlive()) {
            ui.showEliminatedSkip(currentCombatant);
            currentCombatant.removeExpiredStatusEffects();
            return;
        }

        currentCombatant.processTurnStart();

        // A2: Proper stun message matching LogicalGameFlow.md
        if (!currentCombatant.getIsAbleToAct()) {
            ui.showStunnedSkip(currentCombatant);
            currentCombatant.removeExpiredStatusEffects();
            return;
        }

        if (currentCombatant.isPlayerControlled()) {
            executePlayerTurn(currentCombatant);
        } else {
            executeEnemyTurn(currentCombatant);
        }

        currentCombatant.removeExpiredStatusEffects();
    }

    // ─── C1+C2: Polymorphic player turn — BattleEngine never checks action type
    // ───
    private void executePlayerTurn(Combatant player) {
        Action action = ui.getPlayerActionChoice(player);
        if (action == null) {
            return;
        }

        if (!action.prepare(player, ui, getLivingEnemies())) {
            return;
        }

        action.execute(player, action.getTarget());
        action.showResult(player, ui);
    }

    // ─── A3: Smoke Bomb narration in enemy attacks ───
    private void executeEnemyTurn(Combatant enemy) {
        Player target = getFirstLivingPlayer();
        if (target == null) {
            return;
        }

        int hpBefore = target.getHp();
        Action enemyAction = enemy.getAction();
        enemyAction.execute(enemy, target);

        ui.showEnemyAttack(enemy, enemyAction, target, hpBefore);
    }

    // ─── A6: Improved backup spawn message matching LogicalGameFlow ───
    private void handleBackupSpawn() {
        if (backupSpawnTriggered || backupEnemies.isEmpty()) {
            return;
        }

        boolean allInitialEnemiesDefeated = allDefeated(activeEnemies);

        if (allInitialEnemiesDefeated) {
            backupSpawnTriggered = true;

            ui.showBackupSpawn(backupEnemies);

            activeEnemies.addAll(backupEnemies);
            backupEnemies.clear();
        }
    }

    public boolean isGameOver() {
        return allDefeated(players) || (backupEnemies.isEmpty() && allDefeated(activeEnemies));
    }

    // ─── C3: Unified "all defeated" helper ───
    private boolean allDefeated(List<? extends Combatant> combatants) {
        for (Combatant c : combatants) {
            if (c.isAlive()) {
                return false;
            }
        }
        return true;
    }

    private List<Enemy> getLivingEnemies() {
        List<Enemy> livingEnemies = new ArrayList<Enemy>();
        for (Enemy enemy : activeEnemies) {
            if (enemy.isAlive()) {
                livingEnemies.add(enemy);
            }
        }
        return livingEnemies;
    }

    private Player getFirstLivingPlayer() {
        for (Player player : players) {
            if (player.isAlive()) {
                return player;
            }
        }
        return null;
    }

    private void initializeEncounter(int difficulty) {
        activeEnemies.clear();
        backupEnemies.clear();
        backupSpawnTriggered = false;

        activeEnemies.addAll(EncounterFactory.createInitialEnemies(difficulty));
        backupEnemies.addAll(EncounterFactory.createBackupEnemies(difficulty));

        if (backupEnemies.isEmpty()) {
            backupSpawnTriggered = true;
        }
    }

    // ─── A7 + A8: Round summary with [STUNNED], ✗, and ← consumed ───
    private void showRoundSummary() {
        if (players.isEmpty()) {
            return;
        }

        Player player = players.get(0);
        ui.showRoundSummary(currentRound, player);
    }

    private int showEndScreen() {
        if (allDefeated(players)) {
            return ui.showDefeatScreen(currentRound, getLivingEnemies().size());
        }

        Player survivingPlayer = getFirstLivingPlayer();
        if (survivingPlayer != null) {
            return ui.showVictoryScreen(currentRound, survivingPlayer.getHp(), survivingPlayer.getMaxHp());
        }

        return 3;
    }
}