import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BattleEngineInterface {
    private final Scanner scanner;
    private List<Enemy> activeEnemies;
    private List<Enemy> backupEnemies;

    public BattleEngineInterface() {
        scanner = new Scanner(System.in);
    }

    public BattleEngineInterface(Scanner scanner) {
        if (scanner == null) {
            this.scanner = new Scanner(System.in);
        } else {
            this.scanner = scanner;
        }
    }

    public void setEnemyContext(List<Enemy> activeEnemies, List<Enemy> backupEnemies) {
        this.activeEnemies = activeEnemies;
        this.backupEnemies = backupEnemies;
    }

    public void showLoadingScreen() {
        System.out.println("====================================");
        System.out.println("  SC2002 Turn-Based Combat Game");
        System.out.println("====================================");
        System.out.println("Choose your player, pick 2 items, and select a difficulty level.");
        System.out.println();

        System.out.println("Players:");
        System.out.println("1. Warrior | HP: 260 | ATK: 40 | DEF: 20 | SPD: 30 | Skill: Shield Bash");
        System.out.println("2. Wizard  | HP: 200 | ATK: 50 | DEF: 10 | SPD: 20 | Skill: Arcane Blast");
        System.out.println();

        System.out.println("Enemies:");
        System.out.println("- Goblin | HP: 55 | ATK: 35 | DEF: 15 | SPD: 25");
        System.out.println("- Wolf   | HP: 40 | ATK: 45 | DEF: 5  | SPD: 35");
        System.out.println();

        System.out.println("Difficulties:");
        System.out.println("1. Easy   - 3 Goblins");
        System.out.println("2. Medium - 1 Goblin, 1 Wolf, backup: 2 Wolves");
        System.out.println("3. Hard   - 2 Goblins, backup: 1 Goblin and 2 Wolves");
        System.out.println();
    }

    public Player choosePlayer() {
        int choice = choosePlayerChoice();
        return createPlayer(choice);
    }

    public Player createPlayer(int choice) {
        switch (choice) {
            case 1:
                return new Warrior();
            case 2:
            default:
                return new Wizard();
        }
    }

    public int choosePlayerChoice() {
        System.out.println("Select your player:");
        System.out.println("1. Warrior");
        System.out.println("2. Wizard");

        return getValidInput(1, 2);
    }

    public List<Item> chooseStartingItems() {
        return createItemsFromChoices(chooseStartingItemChoices());
    }

    public List<Item> createItemsFromChoices(List<Integer> choices) {
        List<Item> items = new ArrayList<>();
        if (choices == null) {
            return items;
        }

        for (Integer choice : choices) {
            items.add(createItem(choice));
        }
        return items;
    }

    public List<Integer> chooseStartingItemChoices() {
        List<Integer> choices = new ArrayList<>();
        System.out.println();
        System.out.println("Choose 2 starting items (duplicates allowed):");

        for (int i = 1; i <= 2; i++) {
            System.out.println("Item slot " + i + ":");
            System.out.println("1. Potion");
            System.out.println("2. Power Stone");
            System.out.println("3. Smoke Bomb");

            choices.add(getValidInput(1, 3));
        }

        return choices;
    }

    public int chooseDifficulty() {
        System.out.println();
        System.out.println("Choose a difficulty:");
        System.out.println("1. Easy");
        System.out.println("2. Medium");
        System.out.println("3. Hard");
        return getValidInput(1, 3);
    }

    public void showBattleOverview(Player player, List<Enemy> enemies, int difficulty, TurnOrderStrategy turnOrder) {
        System.out.println();
        System.out.println("Player: " + player.getName() + ", " + player.getName() + " Stats: HP: " + player.getMaxHp()
                + ", ATK: " + player.getAttack() + ", DEF: " + player.getDefense() + ", SPD: " + player.getSpeed());

        StringBuilder itemLine = new StringBuilder("Items: ");
        for (int i = 0; i < player.getInventory().size(); i++) {
            Item item = player.getInventory().get(i);
            if (i > 0) {
                itemLine.append(" + ");
            }
            itemLine.append(item.getDisplayName());
        }
        System.out.println(itemLine.toString());

        if (difficulty == 1) {
            System.out.println("Level: Easy (3 Goblins) – A, B, C, Goblin Stats: HP: 55, ATK: 35, DEF: 15, SPD: 25");
        } else if (difficulty == 2) {
            System.out.println(
                    "Level: Medium (1 Goblin, 1 Wolf), Goblin Stats: HP: 55, ATK: 35, DEF: 15, SPD: 25, Wolf Stats: HP: 40, ATK: 45, DEF: 5, SPD: 35");
        } else {
            System.out.println(
                    "Level: Hard (2 Goblins, backup: 1 Goblin + 2 Wolves), Goblin Stats: HP: 55, ATK: 35, DEF: 15, SPD: 25, Wolf Stats: HP: 40, ATK: 45, DEF: 5, SPD: 35");
        }

        List<Combatant> orderPreview = new ArrayList<>();
        orderPreview.add(player);
        orderPreview.addAll(enemies);
        List<Combatant> orderedCombatants = turnOrder.determineOrder(orderPreview);

        StringBuilder orderLine = new StringBuilder("Turn Order: ");
        for (int i = 0; i < orderedCombatants.size(); i++) {
            Combatant combatant = orderedCombatants.get(i);
            if (i > 0) {
                orderLine.append(" → ");
            }

            if (combatant instanceof Enemy && enemies.size() > 1) {
                String typeName = combatant.getClass().getSimpleName();
                int sameTypeCount = 0;
                for (Enemy enemy : enemies) {
                    if (enemy.getClass() == combatant.getClass()) {
                        sameTypeCount++;
                    }
                }

                if (sameTypeCount > 1) {
                    orderLine.append(typeName).append("s (SPD ").append(combatant.getSpeed()).append(")");
                    while (i + 1 < orderedCombatants.size()
                            && orderedCombatants.get(i + 1) instanceof Enemy
                            && orderedCombatants.get(i + 1).getClass() == combatant.getClass()
                            && orderedCombatants.get(i + 1).getSpeed() == combatant.getSpeed()) {
                        i++;
                    }
                } else {
                    orderLine.append(typeName).append(" (SPD ").append(combatant.getSpeed()).append(")");
                }
            } else {
                orderLine.append(combatant.getName()).append(" (SPD ").append(combatant.getSpeed()).append(")");
            }
        }

        System.out.println(orderLine.toString());
        System.out.println();
    }

    public void displayBattleState(List<Player> players, List<Enemy> enemies, int round) {
        System.out.println();
        System.out.println("=== Round " + round + " ===");
        System.out.println("Players:");

        if (players == null || players.isEmpty()) {
            System.out.println("- None");
        } else {
            for (Player player : players) {
                if (player.isAlive()) {
                    System.out.println("- " + player.getName() + " | HP: " + player.getHp() + "/" + player.getMaxHp()
                            + " | Cooldown: " + player.getSkillCooldown() + " | Items: "
                            + formatItems(player.getInventory()));
                } else {
                    System.out.println("- " + player.getName() + " | DEFEATED");
                }
            }
        }

        System.out.println("Enemies:");
        if (enemies == null || enemies.isEmpty()) {
            System.out.println("- None");
        } else {
            for (Enemy enemy : enemies) {
                String enemyName = formatEnemyName(enemy, enemies);
                if (enemy.isAlive()) {
                    System.out.println("- " + enemyName + " | HP: " + enemy.getHp() + "/" + enemy.getMaxHp());
                } else {
                    System.out.println("- " + enemyName + " | DEFEATED");
                }
            }
        }
    }

    public Action getPlayerActionChoice(Combatant player) {
        String skillName = player.getSpecialSkillAction().getActionName();

        while (true) {

            System.out.println();
            System.out.println("Choose an action for " + player.getName() + ":");
            System.out.println("1. Basic Attack");
            System.out.println("2. Defend");
            System.out.println("3. Use Item");
            System.out.println("4. " + skillName + " (Cooldown: " + player.getSkillCooldown() + ")");

            int choice = getValidInput(1, 4);
            switch (choice) {
                case 1:
                    return new BasicAttackAction();
                case 2:
                    return new DefendAction();
                case 3:
                    if (player.getInventory().isEmpty()) {
                        showMessage("No items available.");
                        break;
                    }
                    return new UseItemAction();
                case 4:
                    if (!player.canUseSpecialSkill()) {
                        showMessage("Special skill is on cooldown for " + player.getSkillCooldown() + " more turn(s).");
                        break;
                    }
                    return player.getSpecialSkillAction();
                default:
                    break;
            }
        }
    }

    public Combatant getTargetChoice(List<Enemy> enemies) {
        List<Enemy> livingEnemies = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                livingEnemies.add(enemy);
            }
        }

        if (livingEnemies.isEmpty()) {
            return null;
        }

        System.out.println("Choose a target:");
        for (int i = 0; i < livingEnemies.size(); i++) {
            Enemy enemy = livingEnemies.get(i);
            System.out.println((i + 1) + ". " + formatEnemyName(enemy, livingEnemies) + " (HP: " + enemy.getHp()
                    + "/" + enemy.getMaxHp() + ")");
        }

        int choice = getValidInput(1, livingEnemies.size());
        return livingEnemies.get(choice - 1);
    }

    public Item getItemChoice(Combatant player) {
        List<Item> inventory = player.getInventory();
        if (inventory.isEmpty()) {
            return null;
        }

        System.out.println("Choose an item:");
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            System.out.println((i + 1) + ". " + item.getDisplayName());
        }
        System.out.println("0. Cancel");

        int choice = getValidInput(0, inventory.size());
        if (choice == 0) {
            return null;
        }

        return inventory.get(choice - 1);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public int showVictoryScreen(int rounds, int remainingHp, int maxHp) {
        System.out.println();
        System.out.println("Victory!");
        System.out.println("Remaining HP: " + remainingHp + "/" + maxHp + " | Total Rounds: " + rounds);
        return showPostBattleMenu();
    }

    public int showDefeatScreen(int roundsSurvived, int enemiesRemaining) {
        System.out.println();
        System.out.println("Defeated. Don't give up, try again!");
        System.out.println("Rounds Survived: " + roundsSurvived + " | Enemies Remaining: " + enemiesRemaining);
        return showPostBattleMenu();
    }

    public int showPostBattleMenu() {
        System.out.println();
        System.out.println("What would you like to do next?");
        System.out.println("1. Replay with the same settings");
        System.out.println("2. Start a new game (return to home screen)");
        System.out.println("3. Exit");
        return getValidInput(1, 3);
    }

    public int getValidInput(int min, int max) {
        while (true) {
            System.out.print("> ");
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                if (value >= min && value <= max) {
                    return value;
                }
            } else {
                scanner.next();
            }
            System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
        }
    }

    // ─── Battle display methods ───

    public void showEliminatedSkip(Combatant combatant) {
        StringBuilder msg = new StringBuilder();
        msg.append(getCombatantLabel(combatant)).append(" → ELIMINATED: Skipped");
        for (StatusEffect effect : combatant.getStatusEffects()) {
            if (effect instanceof StunEffect) {
                msg.append(" | Stun expires");
                break;
            }
        }
        showMessage(msg.toString());
    }

    public void showStunnedSkip(Combatant combatant) {
        showMessage(getCombatantLabel(combatant) + " → STUNNED: Turn skipped");
    }

    public void showItemCancel(Combatant user) {
        showMessage(user.getName() + " cancels item use.");
    }

    public void showItemUse(Combatant user, Item item, int hpBefore) {
        showMessage(user.getName() + " → Item → " + item.getResultMessage(user, hpBefore));
    }

    public void showDefend(Combatant user) {
        showMessage(user.getName() + " → Defend: DEF +10 for the current round and the next round");
    }

    public void showSingleTargetAttack(Combatant user, Action action, Combatant target,
            int hpBefore, int targetDefense) {
        StringBuilder msg = new StringBuilder();
        msg.append(user.getName()).append(" → ").append(action.getActionName()).append(" → ")
                .append(getCombatantLabel(target))
                .append(": HP: ").append(hpBefore).append(" → ").append(target.getHp());

        if (!target.isAlive()) {
            msg.append(" ✗ ELIMINATED");
        }

        msg.append(" (dmg: ").append(user.getAttack()).append("−").append(targetDefense).append("=")
                .append(Math.max(0, user.getAttack() - targetDefense)).append(")");

        if (user.getSkillCooldown() > 0) {
            msg.append(" | Cooldown: ").append(user.getSkillCooldown());
        }

        appendAllEnemiesDefeated(msg);
        showMessage(msg.toString());
    }

    public void showTargetedSkill(Combatant user, Action specialSkill, Combatant target,
            int hpBefore, int targetDefense, boolean fromPowerStone) {
        StringBuilder msg = new StringBuilder();
        msg.append(user.getName()).append(" → ");

        if (fromPowerStone) {
            msg.append("Item → Power Stone used → ").append(specialSkill.getActionName()).append(" triggered → ");
        } else {
            msg.append(specialSkill.getActionName()).append(" → ");
        }

        String targetName = getCombatantLabel(target);
        msg.append(targetName).append(": HP: ").append(hpBefore).append(" → ").append(target.getHp())
                .append(" (dmg: ").append(user.getAttack()).append("−").append(targetDefense).append("=")
                .append(Math.max(0, user.getAttack() - targetDefense)).append(")");

        if (target.isAlive() && specialSkill.appliesStun()) {
            msg.append(" | ").append(targetName).append(" STUNNED (2 turns)");
        } else if (!target.isAlive()) {
            msg.append(" ✗ ELIMINATED");
        }

        appendCooldownInfo(msg, user, fromPowerStone);
        appendAllEnemiesDefeated(msg);
        showMessage(msg.toString());
    }

    public void showAoESkill(Combatant user, Action specialSkill, List<Enemy> targets,
            List<Integer> hpBefore, List<Integer> defenses, int startingAttack, boolean fromPowerStone) {
        int attackValue = startingAttack;

        StringBuilder message = new StringBuilder();
        message.append(user.getName()).append(" → ");

        if (fromPowerStone) {
            message.append("Item → Power Stone used → ").append(specialSkill.getActionName())
                    .append(" triggered → All Enemies (ATK: ");
        } else {
            message.append(specialSkill.getActionName()).append(" → All Enemies (ATK: ");
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

        appendCooldownInfo(message, user, fromPowerStone);
        appendAllEnemiesDefeated(message);
        showMessage(message.toString());
    }

    public void showEnemyAttack(Combatant enemy, Action enemyAction, Combatant target, int hpBefore) {
        if (target.getInvulnerable()) {
            showMessage(getCombatantLabel(enemy) + " → " + enemyAction.getActionName() + " → " + target.getName()
                    + ": 0 damage (Smoke Bomb active) | " + target.getName() + " HP: " + target.getHp());
        } else {
            int defense = target.getDefense();
            int damage = Math.max(0, enemy.getAttack() - defense);
            showMessage(getCombatantLabel(enemy) + " → " + enemyAction.getActionName() + " → " + target.getName()
                    + ": HP: " + hpBefore + " → " + target.getHp() + " (dmg: " + enemy.getAttack() + "−" + defense
                    + "=" + damage + ")");
        }
    }

    public void showBackupSpawn(List<Enemy> backups) {
        StringBuilder spawnMsg = new StringBuilder();
        spawnMsg.append("All initial enemies eliminated → Backup Spawn triggered! ");
        for (int i = 0; i < backups.size(); i++) {
            Enemy backup = backups.get(i);
            if (i > 0) {
                spawnMsg.append(" + ");
            }
            spawnMsg.append(backup.getName()).append(" (HP: ").append(backup.getHp()).append(")");
        }
        spawnMsg.append(" enter simultaneously");
        showMessage(spawnMsg.toString());
    }

    public void showRoundSummary(int currentRound, Player player) {
        StringBuilder summary = new StringBuilder();
        summary.append("End of Round ").append(currentRound).append(": ");
        summary.append(player.getName()).append(" HP: ").append(player.getHp()).append("/").append(player.getMaxHp());

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

        List<String> shownTypes = new ArrayList<String>();
        for (Item item : player.getInventory()) {
            String name = item.getDisplayName();
            if (!shownTypes.contains(name)) {
                shownTypes.add(name);
            }
        }

        for (String itemName : shownTypes) {
            int count = 0;
            for (Item item : player.getInventory()) {
                if (item.getDisplayName().equals(itemName)) {
                    count++;
                }
            }
            summary.append(" | ").append(itemName).append(": ").append(count);
        }

        if (player.getInventory().isEmpty()) {
            summary.append(" | Item action no longer available");
        }

        summary.append(" | Special Skills Cooldown: ").append(player.getSkillCooldown()).append(" ");
        if (player.getSkillCooldown() <= 1) {
            summary.append("Round");
        } else {
            summary.append("Rounds");
        }

        showMessage(summary.toString());
    }

    // ─── Private display helpers ───

    private String getCombatantLabel(Combatant combatant) {
        if (!(combatant instanceof Enemy) || activeEnemies == null) {
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

    private void appendAllEnemiesDefeated(StringBuilder msg) {
        boolean allDefeated = true;
        if (activeEnemies != null) {
            for (Enemy enemy : activeEnemies) {
                if (enemy.isAlive()) {
                    allDefeated = false;
                    break;
                }
            }
        }
        if (allDefeated && (backupEnemies == null || backupEnemies.isEmpty())) {
            msg.append(" | All enemies defeated");
        }
    }

    private void appendCooldownInfo(StringBuilder msg, Combatant user, boolean fromPowerStone) {
        if (fromPowerStone) {
            msg.append(" | Power Stone consumed | Cooldown unchanged → ").append(user.getSkillCooldown())
                    .append(" (Power Stone does not affect cooldown)");
        } else {
            msg.append(" | Cooldown set to 3");
        }
    }

    private Item createItem(int choice) {
        switch (choice) {
            case 1:
                return new Potion();
            case 2:
                return new PowerStone();
            case 3:
            default:
                return new SmokeBomb();
        }
    }

    private String formatItems(List<Item> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            return "none";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < inventory.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(inventory.get(i).getDisplayName());
        }
        return builder.toString();
    }

    private String formatEnemyName(Enemy enemy, List<Enemy> enemies) {
        String baseName = enemy.getName();
        int sameTypeCount = 0;
        int sameTypeIndex = 0;

        for (Enemy currentEnemy : enemies) {
            if (currentEnemy.getClass() == enemy.getClass()) {
                if (currentEnemy == enemy) {
                    sameTypeIndex = sameTypeCount;
                }
                sameTypeCount++;
            }
        }

        if (sameTypeCount <= 1) {
            return baseName;
        }

        char suffix = (char) ('A' + sameTypeIndex);
        return baseName + " " + suffix;
    }
}