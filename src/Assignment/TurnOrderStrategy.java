package Assignment;

import java.util.List;

public interface TurnOrderStrategy {

    List<Combatant> determineOrder(List<Combatant> combatants);
}