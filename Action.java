import java.util.List;

public interface Action {
    void execute(Combatant user, Combatant target);

    String getActionName();

    boolean requiresTarget();

    default boolean isSelfTargeting() {
        return false;
    }

    default boolean isAoE() {
        return false;
    }

    default void setTargets(List<? extends Combatant> targets) {
        // Default no-op; AoE actions override this
    }

    default boolean appliesStun() {
        return false;
    }

    // ─── Lifecycle methods for polymorphic turn execution ───

    /**
     * Prepares the action for execution (target selection, item selection, etc.).
     * Returns true if ready to execute, false if cancelled.
     */
    default boolean prepare(Combatant user, BattleEngineInterface ui, List<Enemy> livingEnemies) {
        return true;
    }

    /**
     * Returns the resolved target after prepare(), or null for AoE/self-targeting.
     */
    default Combatant getTarget() {
        return null;
    }

    /**
     * Displays the result of executing this action via the UI.
     */
    default void showResult(Combatant user, BattleEngineInterface ui) {
    }

    /**
     * Marks this action as triggered from a Power Stone (affects cooldown and
     * display).
     */
    default void setFromPowerStone(boolean fromPowerStone) {
    }
}