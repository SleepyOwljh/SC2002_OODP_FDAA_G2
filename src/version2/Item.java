public interface Item {
    void useItem(Combatant user, Combatant target);

    default String getDisplayName() {
        return getClass().getSimpleName();
    }
}