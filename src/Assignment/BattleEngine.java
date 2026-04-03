package Assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleEngine {
    private List<Player> players;
    private List<Enemy> activeEnemies;
    private List<Enemy> backupEnemies;
    private TurnOrderStrategy turnStrategy;
    private BattleEngineInterface ui;
    private int currentRound;

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
        ui.showBattleIntro(buildBattleIntroLines());

        while (!isGameOver()) {
            processRound();
            currentRound++;
        }

        resolveBattleEnd();
    }

    private void processRound() {
        boolean specialSkillUsedThisRound = false;
        ui.showRoundHeader(currentRound);

        List<Combatant> turnOrder = buildCurrentTurnOrder();
        ui.showTurnOrder(buildTurnOrderLine(turnOrder));

        for (Combatant currentCombatant : turnOrder) {
            if (isGameOver()) {
                break;
            }

            if (executeTurn(currentCombatant)) {
                specialSkillUsedThisRound = true;
            }

            handleBackupSpawn();
        }

        if (!specialSkillUsedThisRound) {
            for (Player player : players) {
                if (player.isAlive()) {
                    player.reduceCooldown();
                }
            }
        }

        ui.showRoundSummary(buildRoundSummaryLine());

    }

    private boolean executeTurn(Combatant combatant) {
        String actorLabel = getCombatantLabel(combatant);

        if (!combatant.isAlive()) {
            String line = actorLabel + " → ELIMINATED: Skipped";
            ui.showActionLine(line);
            return false;
        }

        if (!combatant.getIsAbleToAct()) {
            ui.showActionLine(actorLabel + " → STUNNED: Turn skipped");
            return false;
        }

        if (combatant instanceof Player) {
            return executePlayerTurn((Player) combatant);
        }

        executeEnemyTurn((Enemy) combatant);
        return false;
    }

    private boolean executePlayerTurn(Player player) {
        Action chosenAction = ui.getPlayerActionChoice(player);
        
        // Special handling for UseItemAction to pick the item first
        if (chosenAction instanceof UseItemAction) {
            Item item = ui.getItemChoice(player);
            if (item == null) {
                ui.showMessage(player.getName() + " has no items or cancelled.");
                return false;
            }
            chosenAction = new UseItemAction(item);
        }

        Combatant target = resolveTarget(player, chosenAction);
        
        // Execute the action
        chosenAction.execute(player, target);
        
        // Return true if it was a SpecialSkillAction
        return (chosenAction instanceof SpecialSkillAction);
    }

    private void executeEnemyTurn(Enemy enemy) {
        Combatant target = getFirstAlivePlayer();
        if (target == null) {
            return;
        }

        Action action = enemy.performTurn(target);
        action.execute(enemy, target);
    }


    private void handleBackupSpawn() {
        boolean allActiveDead = true;
        for (Enemy enemy : activeEnemies) {
            if (enemy.isAlive()) {
                allActiveDead = false;
                break;
            }
        }

        if (allActiveDead && !backupEnemies.isEmpty()) {
            List<String> spawned = new ArrayList<>();
            for (Enemy enemy : backupEnemies) {
                spawned.add(getCombatantLabel(enemy) + " (HP: " + enemy.getHp() + ")");
            }
            ui.showActionLine("All initial enemies eliminated → Backup Spawn triggered! "
                    + String.join(" + ", spawned) + " enter simultaneously");
            activeEnemies.addAll(backupEnemies);
            backupEnemies.clear();
        }
    }

    private boolean isGameOver() {
        boolean allPlayersDead = true;
        for (Player player : players) {
            if (player.isAlive()) {
                allPlayersDead = false;
                break;
            }
        }

        boolean allEnemiesDead = backupEnemies.isEmpty();
        for (Enemy enemy : activeEnemies) {
            if (enemy.isAlive()) {
                allEnemiesDead = false;
                break;
            }
        }

        return allPlayersDead || allEnemiesDead;
    }

    private void resolveBattleEnd() {
        Player player = players.get(0);
        boolean playerWon = player.isAlive();

        if (playerWon) {
            ui.showVictoryScreen(buildVictoryLine(player));
        } else {
            int remainingEnemies = 0;
            for (Enemy enemy : activeEnemies) {
                if (enemy.isAlive()) {
                    remainingEnemies++;
                }
            }
            remainingEnemies += backupEnemies.size();
            ui.showDefeatScreen(currentRound, remainingEnemies);
        }
    }

    private List<Combatant> buildCurrentTurnOrder() {
        List<Combatant> allCombatants = new ArrayList<>();
        for (Player player : players) {
            if (player.isAlive()) {
                allCombatants.add(player);
            }
        }
        for (Enemy enemy : activeEnemies) {
            if (enemy.isAlive()) {
                allCombatants.add(enemy);
            }
        }
        return turnStrategy.determineOrder(allCombatants);
    }

    private Combatant resolveTarget(Player player, Action action) {
        if (action instanceof DefendAction) {
            return player;
        }

        List<Enemy> aliveEnemies = getAliveEnemies();
        if (aliveEnemies.isEmpty()) {
            return player;
        }

        return ui.getTargetChoice(aliveEnemies);
    }

    private List<Enemy> getAliveEnemies() {
        List<Enemy> aliveEnemies = new ArrayList<>();
        for (Enemy enemy : activeEnemies) {
            if (enemy.isAlive()) {
                aliveEnemies.add(enemy);
            }
        }
        return aliveEnemies;
    }

    private Player getFirstAlivePlayer() {
        for (Player player : players) {
            if (player.isAlive()) {
                return player;
            }
        }
        return null;
    }

    private int calculateDamage(Combatant attacker, Combatant defender) {
        return Math.max(0, attacker.getAttack() - defender.getDefense());
    }


    private String formatAttackLine(String actorLabel, String actionLabel, String targetLabel, int beforeHp,
            int afterHp,
            int attack, int defense, boolean eliminated, String extraNote) {
        int damage = Math.max(0, beforeHp - afterHp);
        StringBuilder builder = new StringBuilder();
        builder.append(actorLabel).append(" → ").append(actionLabel).append(" → ").append(targetLabel)
                .append(": HP: ").append(beforeHp).append(" → ").append(afterHp);
        if (eliminated) {
            builder.append(" ✗ ELIMINATED");
        }
        builder.append(" (dmg: ").append(attack).append("-").append(defense).append("=").append(damage).append(")");
        if (extraNote != null && !extraNote.isEmpty()) {
            builder.append(" | ").append(extraNote);
        }
        return builder.toString();
    }

    private List<String> buildBattleIntroLines() {
        List<String> lines = new ArrayList<>();
        if (!players.isEmpty()) {
            Player player = players.get(0);
            String playerLabel = getCombatantLabel(player);
            lines.add(
                    "Player: " + playerLabel + ", Player Stats: HP: " + player.getHp() + ", ATK: " + player.getAttack()
                            + ", DEF: " + player.getDefense() + ", SPD: " + player.getSpeed());
            lines.add("Items: " + formatItemLoadout(player));
        }
        lines.add("Level: " + inferDifficultyLabel() + " - " + buildEnemyRosterLine());
        lines.add(buildTurnOrderLine(buildCurrentTurnOrder()));
        return lines;
    }

    private String buildEnemyRosterLine() {
        List<String> activeLabels = new ArrayList<>();
        for (Enemy enemy : activeEnemies) {
            activeLabels.add(getCombatantLabel(enemy));
        }
        String activeLine = String.join(" + ", activeLabels);
        if (backupEnemies.isEmpty()) {
            return activeLine;
        }

        List<String> backupLabels = new ArrayList<>();
        for (Enemy enemy : backupEnemies) {
            backupLabels.add(getCombatantLabel(enemy));
        }
        return activeLine + " | Backup: " + String.join(" + ", backupLabels);
    }

    private String buildTurnOrderLine(List<Combatant> turnOrder) {
        List<String> parts = new ArrayList<>();
        for (Combatant combatant : turnOrder) {
            parts.add(getCombatantLabel(combatant) + " (SPD " + combatant.getSpeed() + ")");
        }
        return "Turn Order: " + String.join(" → ", parts);
    }

    private String buildRoundSummaryLine() {
        StringBuilder builder = new StringBuilder();
        builder.append("End of Round ").append(currentRound).append(": ");

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i > 0) {
                builder.append(" | ");
            }
            builder.append(getCombatantLabel(player)).append(" HP: ").append(player.getHp()).append("/")
                    .append(player.getMaxHp());
            if (player instanceof Wizard) {
                builder.append(" | Wizard ATK: ").append(player.getAttack());
            }
        }

        for (Enemy enemy : activeEnemies) {
            builder.append(" | ").append(getCombatantLabel(enemy)).append(" HP: ");
            if (enemy.isAlive()) {
                builder.append(enemy.getHp());
                if (!enemy.getIsAbleToAct()) {
                    builder.append(" [STUNNED]");
                }
            } else {
                builder.append("0");
            }
        }

        if (!players.isEmpty()) {
            Player player = players.get(0);
            builder.append(" | ").append(formatItemCounts(player));
            builder.append(" | Special Skills Cooldown: ").append(readCooldown(player)).append(" rounds");
        }

        return builder.toString();
    }

    private String buildVictoryLine(Player player) {
        return "Result: Player Victory Remaining HP: " + player.getHp() + "/" + player.getMaxHp() + " | Total Rounds: "
                + currentRound + " | Remaining " + formatItemCounts(player);
    }

    private String formatItemLoadout(Player player) {
        List<String> itemNames = new ArrayList<>();
        for (Item item : player.getInventory()) {
            itemNames.add(getItemLabel(item));
        }
        return itemNames.isEmpty() ? "None" : String.join(" + ", itemNames);
    }

    private String formatItemCounts(Player player) {
        List<String> counts = new ArrayList<>();
        String[] tracked = { "Potion", "Smoke Bomb", "Power Stone" };
        for (String trackedItem : tracked) {
            counts.add(trackedItem + ": " + getItemCount(player, trackedItem));
        }
        return String.join(" | ", counts);
    }

    private int getItemCount(Player player, String itemLabel) {
        int count = 0;
        for (Item item : player.getInventory()) {
            if (getItemLabel(item).equals(itemLabel)) {
                count++;
            }
        }
        return count;
    }

    private int readCooldown(Player player) {
        return player.getSkillCooldown();
    }

    private String inferDifficultyLabel() {
        return backupEnemies.isEmpty() ? "Easy" : "Medium";
    }

    private String getCombatantLabel(Combatant combatant) {
        return combatant.getName();
    }

    private String getItemLabel(Item item) {
        String simpleName = item.getClass().getSimpleName();
        if ("SmokeBomb".equals(simpleName)) {
            return "Smoke Bomb";
        }
        if ("PowerStone".equals(simpleName)) {
            return "Power Stone";
        }
        return simpleName;
    }
}