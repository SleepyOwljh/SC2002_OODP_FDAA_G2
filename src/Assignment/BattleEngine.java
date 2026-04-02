package Assignment;

import java.lang.reflect.Field;
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
    private Map<Combatant, Integer> stunDurations;
    private boolean smokeBombActive;
    private boolean smokeBombJustUsed;

    public BattleEngine(List<Player> players, List<Enemy> initialEnemies, List<Enemy> backupEnemies,
            TurnOrderStrategy turnStrategy, BattleEngineInterface ui) {
        this.players = players;
        this.activeEnemies = initialEnemies;
        this.backupEnemies = backupEnemies;
        this.turnStrategy = turnStrategy;
        this.ui = ui;
        this.currentRound = 1;
        this.stunDurations = new HashMap<>();
        this.smokeBombActive = false;
        this.smokeBombJustUsed = false;
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

        if (smokeBombActive) {
            if (smokeBombJustUsed) {
                smokeBombJustUsed = false;
            } else {
                smokeBombActive = false;
            }
        }
    }

    private boolean executeTurn(Combatant combatant) {
        String actorLabel = getCombatantLabel(combatant);

        if (!combatant.isAlive()) {
            String line = actorLabel + " → ELIMINATED: Skipped";
            if (stunDurations.containsKey(combatant)) {
                stunDurations.remove(combatant);
                combatant.setIsAbleToAct(true);
                line += " | Stun expires";
            }
            ui.showActionLine(line);
            return false;
        }

        if (isStunned(combatant)) {
            int remainingTurns = stunDurations.get(combatant);
            String line = actorLabel + " → STUNNED: Turn skipped";
            if (remainingTurns <= 1) {
                stunDurations.remove(combatant);
                combatant.setIsAbleToAct(true);
                line += " | Stun expires";
            } else {
                stunDurations.put(combatant, remainingTurns - 1);
            }
            ui.showActionLine(line);
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
        if (chosenAction instanceof UseItemAction) {
            return executeItemTurn(player);
        }

        Combatant target = resolveTarget(player, chosenAction);
        String actorLabel = getCombatantLabel(player);

        if (chosenAction instanceof DefendAction) {
            ui.showActionLine(actorLabel + " → Defend");
            return false;
        }

        if (chosenAction instanceof SpecialSkillAction) {
            return executeSpecialSkill(player, target, false);
        }

        int beforeHp = target.getHp();
        int damage = calculateDamage(player, target);
        target.takeDamage(damage);
        ui.showActionLine(
                formatAttackLine(actorLabel, "BasicAttack", getCombatantLabel(target), beforeHp, target.getHp(),
                        player.getAttack(), target.getDefense(), target.getHp() == 0, null));
        return false;
    }

    private void executeEnemyTurn(Enemy enemy) {
        Combatant target = getFirstAlivePlayer();
        if (target == null) {
            return;
        }

        String actorLabel = getCombatantLabel(enemy);
        String targetLabel = getCombatantLabel(target);
        int beforeHp = target.getHp();

        if (smokeBombActive) {
            ui.showActionLine(actorLabel + " → BasicAttack → " + targetLabel
                    + ": 0 damage (Smoke Bomb active) | " + targetLabel + " HP: " + target.getHp());
            return;
        }

        int damage = calculateDamage(enemy, target);
        target.takeDamage(damage);
        ui.showActionLine(
                formatAttackLine(actorLabel, "BasicAttack", targetLabel, beforeHp, target.getHp(), enemy.getAttack(),
                        target.getDefense(), target.getHp() == 0, null));
    }

    private boolean executeSpecialSkill(Player player, Combatant target, boolean triggeredByPowerStone) {
        String actorLabel = getCombatantLabel(player);
        if (!triggeredByPowerStone && !player.canUseSpecialSkill()) {
            ui.showActionLine(
                    actorLabel + " → Special Skill unavailable | Cooldown: " + readCooldown(player) + " rounds");
            return false;
        }

        if (player instanceof Warrior) {
            int beforeHp = target.getHp();
            int damage = calculateDamage(player, target);
            target.takeDamage(damage);
            applyStun(target, 2);
            if (!triggeredByPowerStone) {
                player.startCooldown();
            }
            String extra = getCombatantLabel(target) + " STUNNED (2 turns)";
            if (!triggeredByPowerStone) {
                extra += " | Cooldown set to " + readCooldown(player);
            } else {
                extra += " | Cooldown unchanged → " + readCooldown(player) + " (Power Stone does not affect cooldown)";
            }
            ui.showActionLine(
                    formatAttackLine(actorLabel, "Shield Bash", getCombatantLabel(target), beforeHp, target.getHp(),
                            player.getAttack(), target.getDefense(), target.getHp() == 0, extra));
            return !triggeredByPowerStone;
        }

        if (player instanceof Wizard) {
            List<Enemy> aliveEnemies = getAliveEnemies();
            List<String> segments = new ArrayList<>();
            for (Enemy enemy : aliveEnemies) {
                int beforeHp = enemy.getHp();
                int damage = calculateDamage(player, enemy);
                enemy.takeDamage(damage);
                String segment = getCombatantLabel(enemy) + " HP: " + beforeHp + " → " + enemy.getHp()
                        + (enemy.getHp() == 0 ? " ✗ ELIMINATED" : "") + " (dmg: " + player.getAttack() + "-"
                        + enemy.getDefense() + "=" + damage + ")";
                if (enemy.getHp() == 0) {
                    int beforeAtk = player.getAttack();
                    player.setAttack(beforeAtk + 10);
                    segment += " | ATK: " + beforeAtk + " → " + player.getAttack() + " (+10)";
                }
                segments.add(segment);
            }

            if (!triggeredByPowerStone) {
                player.startCooldown();
                segments.add("Cooldown set to " + readCooldown(player));
            } else {
                segments.add(
                        "Cooldown unchanged → " + readCooldown(player) + " (Power Stone does not affect cooldown)");
            }

            ui.showActionLine(actorLabel + " → Arcane Blast → All Enemies: " + String.join(" | ", segments));
            return !triggeredByPowerStone;
        }

        ui.showActionLine(actorLabel + " → Special Skill");
        return false;
    }

    private boolean executeItemTurn(Player player) {
        Item item = ui.getItemChoice(player);
        String actorLabel = getCombatantLabel(player);
        if (item == null) {
            ui.showActionLine(actorLabel + " → Item unavailable");
            return false;
        }

        String itemLabel = getItemLabel(item);
        int beforeHp = player.getHp();

        if (item instanceof Potion) {
            player.heal(100);
            player.getInventory().remove(item);
            ui.showActionLine(actorLabel + " → Item → " + itemLabel + " used: HP: " + beforeHp + " → "
                    + player.getHp() + " (+" + (player.getHp() - beforeHp) + ")");
            return false;
        }

        if (item instanceof SmokeBomb) {
            smokeBombActive = true;
            smokeBombJustUsed = true;
            player.getInventory().remove(item);
            ui.showActionLine(actorLabel + " → Item → Smoke Bomb used: Enemy attacks deal 0 damage this turn + next");
            return false;
        }

        if (item instanceof PowerStone) {
            Combatant target = ui.getTargetChoice(getAliveEnemies());
            player.getInventory().remove(item);
            ui.showActionLine(actorLabel + " → Item → Power Stone used → "
                    + (player instanceof Warrior ? "Shield Bash triggered" : "Arcane Blast triggered"));
            executeSpecialSkill(player, target, true);
            return false;
        }

        item.useItem(player, player);
        player.getInventory().remove(item);
        ui.showActionLine(actorLabel + " → Item → " + itemLabel + " used");
        return false;
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

    private void applyStun(Combatant target, int turns) {
        stunDurations.put(target, turns);
        target.setIsAbleToAct(false);
    }

    private boolean isStunned(Combatant combatant) {
        return stunDurations.containsKey(combatant) && stunDurations.get(combatant) > 0;
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
                if (isStunned(enemy)) {
                    builder.append(" [STUNNED]");
                }
            } else {
                builder.append("0");
            }
        }

        if (!players.isEmpty()) {
            Player player = players.get(0);
            builder.append(" | ").append(formatItemCounts(player));
            if (smokeBombActive) {
                builder.append(" | Effect: 1 turn remaining");
            }
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
        try {
            Field field = Player.class.getDeclaredField("specialSkillCooldown");
            field.setAccessible(true);
            return field.getInt(player);
        } catch (ReflectiveOperationException error) {
            return 0;
        }
    }

    private String inferDifficultyLabel() {
        return backupEnemies.isEmpty() ? "Easy" : "Medium";
    }

    private String getCombatantLabel(Combatant combatant) {
        if (combatant instanceof Player) {
            return combatant.getClass().getSimpleName();
        }

        String type = combatant.getClass().getSimpleName();
        List<Enemy> allEnemies = new ArrayList<>();
        allEnemies.addAll(activeEnemies);
        allEnemies.addAll(backupEnemies);

        int sameTypeCount = 0;
        int sameTypeIndex = 0;
        for (Enemy enemy : allEnemies) {
            if (enemy.getClass() == combatant.getClass()) {
                sameTypeCount++;
                if (enemy == combatant) {
                    sameTypeIndex = sameTypeCount;
                }
            }
        }

        if (sameTypeCount <= 1) {
            return type;
        }

        return type + " " + (char) ('A' + sameTypeIndex - 1);
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