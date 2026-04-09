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

    @Override
    public String getResultMessage(Combatant user, int hpBefore) {
        return "Potion used: HP: " + hpBefore + " → " + user.getHp() + " (+" + (user.getHp() - hpBefore) + ")";
    }
}
