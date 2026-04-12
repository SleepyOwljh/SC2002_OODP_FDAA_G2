import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BattleEngineInterface {
    private final Scanner scanner;

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
        System.out.println("Select your player:");
        System.out.println("1. Warrior");
        System.out.println("2. Wizard");

        int choice = getValidInput(1, 2);
        if (choice == 1) {
            return new Warrior();
        }
        return new Wizard();
    }

    public List<Item> chooseStartingItems() {
        List<Item> chosenItems = new ArrayList<>();
        System.out.println();
        System.out.println("Choose 2 starting items (duplicates allowed):");

        for (int i = 1; i <= 2; i++) {
            System.out.println("Item slot " + i + ":");
            System.out.println("1. Potion");
            System.out.println("2. Power Stone");
            System.out.println("3. Smoke Bomb");

            int choice = getValidInput(1, 3);
            chosenItems.add(createItem(choice));
        }

        return chosenItems;
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
                orderLine.append(" -> ");
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

    public Action getPlayerActionChoice(Player player) {
        String skillName;

        while (true) {
            if (player.getSpecialSkillAction() instanceof ShieldBashAction) {
                skillName = "Shield Bash";
            } else {
                skillName = "Arcane Blast";
            }

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

    public Item getItemChoice(Player player) {
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