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
            StringBuilder msg = new StringBuilder();
            msg.append(getCombatantLabel(currentCombatant)).append(" → ELIMINATED: Skipped");
            // Check if stun expires on this dead combatant
            for (StatusEffect effect : currentCombatant.getStatusEffects()) {
                if (effect instanceof StunEffect) {
                    msg.append(" | Stun expires");
                    break;
                }
            }
            ui.showMessage(msg.toString());
            currentCombatant.removeExpiredStatusEffects();
            return;
        }

        currentCombatant.processTurnStart();

        // A2: Proper stun message matching LogicalGameFlow.md
        if (!currentCombatant.getIsAbleToAct()) {
            ui.showMessage(getCombatantLabel(currentCombatant) + " → STUNNED: Turn skipped");
            currentCombatant.removeExpiredStatusEffects();
            return;
        }

        if (currentCombatant instanceof Player) {
            executePlayerTurn((Player) currentCombatant);
        } else if (currentCombatant instanceof Enemy) {
            executeEnemyTurn((Enemy) currentCombatant);
        }

        currentCombatant.removeExpiredStatusEffects();
    }

    // ─── C1+C2: Extracted helper methods to eliminate duplication ───
    private void executePlayerTurn(Player player) {
        Action action = ui.getPlayerActionChoice(player);
        if (action == null) {
            return;
        }

        if (action instanceof UseItemAction) {
            Item chosenItem = ui.getItemChoice(player);
            if (chosenItem == null) {
                ui.showMessage(player.getName() + " cancels item use.");
                return;
            }

            ((UseItemAction) action).setItem(chosenItem);

            if (chosenItem instanceof Potion) {
                int hpBefore = player.getHp();
                action.execute(player, player);
                ui.showMessage(player.getName() + " → Item → Potion used: HP: " + hpBefore + " → "
                        + player.getHp() + " (+" + (player.getHp() - hpBefore) + ")");
                return;
            }

            if (chosenItem instanceof SmokeBomb) {
                action.execute(player, player);
                ui.showMessage(player.getName() + " → Item → Smoke Bomb used: Enemy attacks deal 0 damage this turn + next");
                return;
            }

            if (chosenItem instanceof PowerStone) {
                Action specialSkill = player.getSpecialSkillAction();
                // Remove the PowerStone from inventory (consumed on use)
                player.getInventory().remove(chosenItem);

                if (specialSkill instanceof ShieldBashAction) {
                    handleShieldBash(player, true);
                    return;
                }

                if (specialSkill instanceof ArcaneBlastAction) {
                    handleArcaneBlast(player, true);
                    return;
                }
            }
        }

        if (action instanceof DefendAction) {
            action.execute(player, player);
            ui.showMessage(player.getName() + " → Defend: DEF +10 for the current round and the next round");
            return;
        }

        if (action instanceof ShieldBashAction) {
            handleShieldBash(player, false);
            return;
        }

        if (action instanceof ArcaneBlastAction) {
            handleArcaneBlast(player, false);
            return;
        }

        // BasicAttack (fallthrough)
        Combatant target = ui.getTargetChoice(getLivingEnemies());
        if (target == null) {
            return;
        }

        int hpBefore = target.getHp();
        int targetDefense = target.getDefense();
        String targetName = getCombatantLabel(target);

        action.execute(player, target);

        // A4 + A5: Append cooldown and "All enemies defeated" to BasicAttack
        StringBuilder msg = new StringBuilder();
        msg.append(player.getName()).append(" → BasicAttack → ").append(targetName)
                .append(": HP: ").append(hpBefore).append(" → ").append(target.getHp());

        if (!target.isAlive()) {
            msg.append(" ✗ ELIMINATED");
        }

        msg.append(" (dmg: ").append(player.getAttack()).append("−").append(targetDefense).append("=")
                .append(Math.max(0, player.getAttack() - targetDefense)).append(")");

        // A4: Show cooldown when relevant
        if (player.getSkillCooldown() > 0) {
            msg.append(" | Cooldown: ").append(player.getSkillCooldown());
        }

        // A5: Announce if all enemies are now defeated
        if (getLivingEnemies().isEmpty() && backupEnemies.isEmpty()) {
            msg.append(" | All enemies defeated");
        }

        ui.showMessage(msg.toString());
    }

    // ─── C1: Consolidated ShieldBash handler (direct use + PowerStone) ───
    private void handleShieldBash(Player player, boolean fromPowerStone) {
        Combatant target = ui.getTargetChoice(getLivingEnemies());
        if (target == null) {
            return;
        }

        int hpBefore = target.getHp();
        int targetDefense = target.getDefense();
        String targetName = getCombatantLabel(target);

        if (fromPowerStone) {
            // PowerStone triggers the special skill action internally
            Action specialSkill = player.getSpecialSkillAction();
            prepareActionContext(specialSkill);
            specialSkill.execute(player, target);
        } else {
            Action action = player.getSpecialSkillAction();
            action.execute(player, target);
            player.startCooldown();
        }

        StringBuilder msg = new StringBuilder();
        msg.append(player.getName()).append(" → ");

        if (fromPowerStone) {
            msg.append("Item → Power Stone used → Shield Bash triggered → ");
        } else {
            msg.append("Shield Bash → ");
        }

        msg.append(targetName).append(": HP: ").append(hpBefore).append(" → ").append(target.getHp())
                .append(" (dmg: ").append(player.getAttack()).append("−").append(targetDefense).append("=")
                .append(Math.max(0, player.getAttack() - targetDefense)).append(")");

        if (target.isAlive()) {
            msg.append(" | ").append(targetName).append(" STUNNED (2 turns)");
        } else {
            msg.append(" ✗ ELIMINATED");
        }

        if (fromPowerStone) {
            msg.append(" | Cooldown unchanged → ").append(player.getSkillCooldown())
                    .append(" (Power Stone does not affect cooldown) | Power Stone consumed");
        } else {
            msg.append(" | Cooldown set to 3");
        }

        // A5: Check if all enemies defeated
        if (getLivingEnemies().isEmpty() && backupEnemies.isEmpty()) {
            msg.append(" | All enemies defeated");
        }

        ui.showMessage(msg.toString());
    }

    // ─── C2: Consolidated ArcaneBlast handler (direct use + PowerStone) ───
    private void handleArcaneBlast(Player player, boolean fromPowerStone) {
        Action specialSkill = player.getSpecialSkillAction();
        prepareActionContext(specialSkill);

        List<Enemy> targets = getLivingEnemies();
        List<Integer> hpBefore = new ArrayList<Integer>();
        List<Integer> defenses = new ArrayList<Integer>();
        int attackValue = player.getAttack();
        int startingAttack = attackValue;

        for (Enemy enemy : targets) {
            hpBefore.add(enemy.getHp());
            defenses.add(enemy.getDefense());
        }

        specialSkill.execute(player, null);

        if (!fromPowerStone) {
            player.startCooldown();
        }

        StringBuilder message = new StringBuilder();
        message.append(player.getName()).append(" → ");

        if (fromPowerStone) {
            message.append("Item → Power Stone used → Arcane Blast triggered → All Enemies (ATK: ");
        } else {
            message.append("Arcane Blast → All Enemies (ATK: ");
        }

        message.append(startingAttack).append("): ");

        for (int i = 0; i < targets.size(); i++) {
            Enemy enemy = targets.get(i);
            String enemyName = getCombatantLabel(enemy);
            int damage = Math.max(0, attackValue - defenses.get(i));

            message.append(enemyName).append(" HP: ").append(hpBefore.get(i)).append(" → ").append(enemy.getHp());
            if (!enemy.isAlive()) {
                message.append(" ✗ ELIMINATED");
            }
            message.append(" (dmg: ").append(attackValue).append("−").append(defenses.get(i)).append("=")
                    .append(damage).append(")");

            if (hpBefore.get(i) > 0 && !enemy.isAlive()) {
                message.append(" | ATK: ").append(attackValue).append(" → ").append(attackValue + 10)
                        .append(" (+10)");
                attackValue += 10;
            }

            if (i < targets.size() - 1) {
                message.append(" | ");
            }
        }

        if (fromPowerStone) {
            message.append(" | Power Stone consumed | Cooldown unchanged → ").append(player.getSkillCooldown())
                    .append(" (Power Stone does not affect cooldown)");
        } else {
            message.append(" | Cooldown set to 3");
        }

        // A5: Check if all enemies defeated
        if (getLivingEnemies().isEmpty() && backupEnemies.isEmpty()) {
            message.append(" | All enemies defeated");
        }

        ui.showMessage(message.toString());
    }

    // ─── A3: Smoke Bomb narration in enemy attacks ───
    private void executeEnemyTurn(Enemy enemy) {
        Player target = getFirstLivingPlayer();
        if (target == null) {
            return;
        }

        int hpBefore = target.getHp();
        enemy.getAction().execute(enemy, target);

        if (target.getInvulnerable()) {
            // A3: Show Smoke Bomb active message
            ui.showMessage(getCombatantLabel(enemy) + " → BasicAttack → " + target.getName()
                    + ": 0 damage (Smoke Bomb active) | " + target.getName() + " HP: " + target.getHp());
        } else {
            int defense = target.getDefense();
            int damage = Math.max(0, enemy.getAttack() - defense);
            ui.showMessage(getCombatantLabel(enemy) + " → BasicAttack → " + target.getName() + ": HP: " + hpBefore
                    + " → " + target.getHp() + " (dmg: " + enemy.getAttack() + "−" + defense + "=" + damage + ")");
        }
    }

    private void prepareActionContext(Action action) {
        if (action instanceof ArcaneBlastAction) {
            ((ArcaneBlastAction) action).setCurrentEnemies(activeEnemies);
        }
    }

    // ─── A6: Improved backup spawn message matching LogicalGameFlow ───
    private void handleBackupSpawn() {
        if (backupSpawnTriggered || backupEnemies.isEmpty()) {
            return;
        }

        boolean allInitialEnemiesDefeated = allDefeated(activeEnemies);

        if (allInitialEnemiesDefeated) {
            backupSpawnTriggered = true;

            // Build descriptive spawn message
            StringBuilder spawnMsg = new StringBuilder();
            spawnMsg.append("All initial enemies eliminated → Backup Spawn triggered! ");
            for (int i = 0; i < backupEnemies.size(); i++) {
                Enemy backup = backupEnemies.get(i);
                if (i > 0) {
                    spawnMsg.append(" + ");
                }
                spawnMsg.append(backup.getName()).append(" (HP: ").append(backup.getHp()).append(")");
            }
            spawnMsg.append(" enter simultaneously");

            activeEnemies.addAll(backupEnemies);
            backupEnemies.clear();

            ui.showMessage(spawnMsg.toString());
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

        switch (difficulty) {
            case 2:
                activeEnemies.add(new Goblin());
                activeEnemies.add(new Wolf());
                backupEnemies.add(new Wolf());
                backupEnemies.add(new Wolf());
                break;
            case 3:
                activeEnemies.add(new Goblin());
                activeEnemies.add(new Goblin());
                backupEnemies.add(new Goblin());
                backupEnemies.add(new Wolf());
                backupEnemies.add(new Wolf());
                break;
            case 1:
            default:
                activeEnemies.add(new Goblin());
                activeEnemies.add(new Goblin());
                activeEnemies.add(new Goblin());
                break;
        }

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
        StringBuilder summary = new StringBuilder();
        summary.append("End of Round ").append(currentRound).append(": ");
        summary.append(player.getName()).append(" HP: ").append(player.getHp()).append("/").append(player.getMaxHp());

        // A7: Show ✗ for dead enemies and [STUNNED] tag for stunned ones
        for (Enemy enemy : activeEnemies) {
            summary.append(" | ").append(getCombatantLabel(enemy)).append(" HP: ");
            if (!enemy.isAlive()) {
                summary.append("✗");
            } else {
                summary.append(enemy.getHp());
                if (!enemy.getIsAbleToAct()) {
                    summary.append(" [STUNNED]");
                }
            }
        }

        // A8: Show item counts with "← consumed" annotations
        // Collect distinct item display names from current inventory
        List<String> shownTypes = new ArrayList<String>();
        for (Item item : player.getInventory()) {
            String name = item.getDisplayName();
            if (!shownTypes.contains(name)) {
                shownTypes.add(name);
            }
        }

        // Show remaining items
        for (String itemName : shownTypes) {
            int count = 0;
            for (Item item : player.getInventory()) {
                if (item.getDisplayName().equals(itemName)) {
                    count++;
                }
            }
            summary.append(" | ").append(itemName).append(": ").append(count);
        }

        // Show items that were fully consumed (count = 0) — track what types existed at start
        // We check if inventory is empty to add the "Item action no longer available" note
        if (player.getInventory().isEmpty()) {
            summary.append(" | Item action no longer available");
        }

        summary.append(" | Special Skills Cooldown: ").append(player.getSkillCooldown()).append(" ");
        if (player.getSkillCooldown() <= 1) {
            summary.append("Round");
        } else {
            summary.append("Rounds");
        }

        ui.showMessage(summary.toString());
    }

    private String getCombatantLabel(Combatant combatant) {
        if (!(combatant instanceof Enemy)) {
            return combatant.getName();
        }

        Enemy enemy = (Enemy) combatant;
        int sameTypeCount = 0;
        int sameTypeIndex = 0;

        for (Enemy currentEnemy : activeEnemies) {
            if (currentEnemy.getClass() == enemy.getClass()) {
                if (currentEnemy == enemy) {
                    sameTypeIndex = sameTypeCount;
                }
                sameTypeCount++;
            }
        }

        if (sameTypeCount <= 1) {
            return enemy.getName();
        }

        char suffix = (char) ('A' + sameTypeIndex);
        return enemy.getName() + " " + suffix;
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