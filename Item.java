public interface Item {
    void useItem(Combatant user, Combatant target);

    default String getDisplayName() {
        return getClass().getSimpleName();
    }

    default String getResultMessage(Combatant user, int hpBefore) {
        return getDisplayName() + " used";
    }

    default boolean triggersSpecialSkill() {
        return false;
    }
}