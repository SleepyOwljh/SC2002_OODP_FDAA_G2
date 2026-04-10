public class Potion implements Item {
    private final int healAmount;

    public Potion() {
        this.healAmount = 100;
    }

    @Override
    public void useItem(Combatant user, Combatant target) {
        if (user != null) {
            user.heal(healAmount);
        }
    }
}
