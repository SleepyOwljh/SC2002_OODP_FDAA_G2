package Assignment;

import java.util.List;
import java.util.Scanner;

import javax.swing.Action;

public class BattleEngineInterface {
    private final Scanner scanner;

    public BattleEngineInterface() {
        this.scanner = new Scanner(System.in);
    }

    // Demonstrates SRP: Formatting the battle state is entirely handled by the UI.
    public void displayBattleState(List<Player> players, List<Enemy> enemies, int round) {
        System.out.println("\n================ ROUND " + round + " ================");
        System.out.println("--- PLAYERS ---");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String status = p.isDefeated() ? "[DEFEATED]" : "HP: " + p.getHp() + "/" + p.getMaxHp();
            System.out.println((i + 1) + ". " + p.getName() + " | " + status + " | Cooldown: " + p.getSkillCooldown());
        }

        System.out.println("\n--- ENEMIES ---");
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            String status = e.isDefeated() ? "[DEFEATED]" : "HP: " + e.getHp() + "/" + e.getMaxHp();
            System.out.println((i + 1) + ". " + e.getName() + " | " + status);
        }
        System.out.println("==========================================\n");
    }

    // Returns an Action object, keeping the engine decoupled from the menu text.
    public Action getPlayerActionChoice(Player player) {
        System.out.println(player.getName() + "'s Turn! Choose an action:");
        System.out.println("1. Basic Attack");
        System.out.println("2. Defend");
        System.out.println("3. Special Skill");
        System.out.println("4. Use Item");

        int choice = getValidInput(1, 4);

        switch (choice) {
            case 1:
                return new BasicAttackAction();
            case 2:
                return new DefendAction();
            case 3:
                return new SpecialSkillAction();
            case 4:
                // Note: You would normally prompt the user to select WHICH item here
                // and return new UseItemAction(selectedItem).
                // For simplicity in this skeleton, we assume a placeholder.
                return new UseItemAction(player.getInventory().get(0));
            default:
                return new BasicAttackAction();
        }
    }

    public Combatant getTargetChoice(List<Enemy> enemies) {
        System.out.println("Select a target:");
        for (int i = 0; i < enemies.size(); i++) {
            if (!enemies.get(i).isDefeated()) {
                System.out.println((i + 1) + ". " + enemies.get(i).getName() + " (HP: " + enemies.get(i).getHp() + ")");
            }
        }

        int choice = getValidInput(1, enemies.size());
        return enemies.get(choice - 1);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showVictoryScreen(int rounds, int remainingHp, int maxHp) {
        System.out.println("\n*** CONGRATULATIONS! ***");
        System.out.println("You have defeated all enemies.");
        System.out.println("Statistics: Remaining HP: " + remainingHp + "/" + maxHp + " | Total Rounds: " + rounds);
    }

    public void showDefeatScreen(int roundsSurviving, int enemiesRemaining) {
        System.out.println("\n*** DEFEATED ***");
        System.out.println("Don't give up, try again!");
        System.out.println(
                "Statistics: Enemies remaining: " + enemiesRemaining + " | Total Rounds Survived: " + roundsSurviving);
    }

    // Helper method to prevent the program from crashing if the user types a letter
    private int getValidInput(int min, int max) {
        int choice = -1;
        while (choice < min || choice > max) {
            System.out.print("Enter choice (" + min + "-" + max + "): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice < min || choice > max) {
                    System.out.println("Invalid number. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // Clear the bad input
            }
        }
        return choice;
    }
}
