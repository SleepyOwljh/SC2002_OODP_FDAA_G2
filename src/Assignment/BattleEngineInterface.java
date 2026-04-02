package Assignment;

import java.util.List;
import java.util.Scanner;

public class BattleEngineInterface {
    private final Scanner scanner;

    public BattleEngineInterface() {
        this.scanner = new Scanner(System.in);
    }

    public void showBattleIntro(List<String> introLines) {
        for (String line : introLines) {
            System.out.println(line);
        }
        System.out.println();
    }

    public void showRoundHeader(int round) {
        System.out.println("Round " + round);
    }

    public void showTurnOrder(String turnOrderLine) {
        System.out.println(turnOrderLine);
    }

    public void showActionLine(String line) {
        System.out.println(line);
    }

    public void showRoundSummary(String summary) {
        System.out.println(summary);
    }

    public void displayBattleState(List<Player> players, List<Enemy> enemies, int round) {
        System.out.println("Round " + round);
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            System.out.println((i + 1) + ". " + getCombatantLabel(p) + " HP: " + p.getHp() + "/" + p.getMaxHp());
        }

        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            String status = e.isAlive() ? String.valueOf(e.getHp()) : "0";
            System.out.println((i + 1) + ". " + getCombatantLabel(e) + " HP: " + status);
        }
    }

    public Action getPlayerActionChoice(Player player) {
        System.out.println(getCombatantLabel(player) + " - choose an action:");
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
                return new UseItemAction();
            default:
                return new BasicAttackAction();
        }
    }

    public Item getItemChoice(Player player) {
        if (player.getInventory().isEmpty()) {
            return null;
        }

        System.out.println("Choose an item:");
        for (int i = 0; i < player.getInventory().size(); i++) {
            System.out.println((i + 1) + ". " + formatItemLabel(player.getInventory().get(i)));
        }

        int choice = getValidInput(1, player.getInventory().size());
        return player.getInventory().get(choice - 1);
    }

    public Combatant getTargetChoice(List<Enemy> enemies) {
        System.out.println("Select a target:");
        int displayIndex = 1;
        Enemy[] displayedEnemies = new Enemy[enemies.size()];

        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).isAlive()) {
                displayedEnemies[displayIndex - 1] = enemies.get(i);
                System.out.println(displayIndex + ". " + getCombatantLabel(enemies.get(i)) + " (HP: "
                        + enemies.get(i).getHp() + ")");
                displayIndex++;
            }
        }

        int choice = getValidInput(1, displayIndex - 1);
        return displayedEnemies[choice - 1];
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showVictoryScreen(String resultLine) {
        System.out.println("Victory");
        System.out.println(resultLine);
    }

    public void showDefeatScreen(int roundsSurviving, int enemiesRemaining) {
        System.out.println("Defeat");
        System.out.println("Result: Player Defeat | Enemies remaining: " + enemiesRemaining + " | Total Rounds: "
                + roundsSurviving);
    }

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
                scanner.next();
            }
        }
        return choice;
    }

    private String getCombatantLabel(Combatant combatant) {
        return combatant.getClass().getSimpleName();
    }

    private String formatItemLabel(Item item) {
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
