import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BattleEngineInterface ui = new BattleEngineInterface();
        boolean running = true;

        int playerChoice = 0;
        List<Integer> itemChoices = new ArrayList<>();
        int difficulty = 1;

        while (running) {
            if (playerChoice == 0 || itemChoices.isEmpty()) {
                ui.showLoadingScreen();

                Player selectedPlayer = ui.choosePlayer();
                if (selectedPlayer instanceof Warrior) {
                    playerChoice = 1;
                } else {
                    playerChoice = 2;
                }

                List<Item> selectedItems = ui.chooseStartingItems();
                itemChoices.clear();
                for (Item item : selectedItems) {
                    if (item instanceof Potion) {
                        itemChoices.add(1);
                    } else if (item instanceof PowerStone) {
                        itemChoices.add(2);
                    } else {
                        itemChoices.add(3);
                    }
                }

                difficulty = ui.chooseDifficulty();
            }

            Player chosenPlayer;
            if (playerChoice == 1) {
                chosenPlayer = new Warrior();
            } else {
                chosenPlayer = new Wizard();
            }

            List<Item> freshItems = new ArrayList<>();
            for (Integer choice : itemChoices) {
                if (choice == 1) {
                    freshItems.add(new Potion());
                } else if (choice == 2) {
                    freshItems.add(new PowerStone());
                } else {
                    freshItems.add(new SmokeBomb());
                }
            }
            chosenPlayer.setInventory(freshItems);

            List<Player> players = new ArrayList<>();
            players.add(chosenPlayer);

            ui.showMessage("Starting battle with " + chosenPlayer.getName() + " on difficulty " + difficulty + "...");

            BattleEngine engine = new BattleEngine(players, null, null, new SpeedBasedTurnOrder(), ui);
            int nextAction = engine.startBattle(difficulty);

            switch (nextAction) {
                case 1:
                    ui.showMessage("Replaying with the same settings...");
                    break;
                case 2:
                    ui.showMessage("Returning to the home screen...");
                    playerChoice = 0;
                    itemChoices.clear();
                    break;
                case 3:
                default:
                    ui.showMessage("Exiting game. Thanks for playing!");
                    running = false;
                    break;
            }
        }
    }
}
