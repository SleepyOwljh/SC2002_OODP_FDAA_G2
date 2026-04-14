public interface StatusEffect {
    void applyEffect(Combatant target);

    void removeEffect(Combatant target);

    default void countDuration() {
    }

    default boolean isExpired() {
        return false;
    }

    default void resetDuration() {
    }
}
