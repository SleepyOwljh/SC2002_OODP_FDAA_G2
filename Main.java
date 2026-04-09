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

                playerChoice = ui.choosePlayerChoice();
                itemChoices = ui.chooseStartingItemChoices();
                difficulty = ui.chooseDifficulty();
            }

            Player chosenPlayer = ui.createPlayer(playerChoice);

            List<Item> freshItems = ui.createItemsFromChoices(itemChoices);
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
