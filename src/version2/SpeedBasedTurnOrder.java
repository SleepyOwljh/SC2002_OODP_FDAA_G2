import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpeedBasedTurnOrder implements TurnOrderStrategy {

    @Override
    public List<Combatant> determineOrder(List<Combatant> combatants) {
        List<Combatant> turnOrder = new ArrayList<>(combatants);
        turnOrder.sort(Comparator.comparingInt(Combatant::getSpeed).reversed());
        return turnOrder;
    }
}
