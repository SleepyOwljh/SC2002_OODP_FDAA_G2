public class DefendBuff extends TemporaryEffects {
    private int defenseBoost;

    public DefendBuff() {
        super(2);
        this.defenseBoost = 10;
    };

    public void applyEffect(Combatant target) {
        target.setDefense(target.getDefense() + defenseBoost);
    };

    public void removeEffect(Combatant target) {
        target.setDefense(target.getDefense() - defenseBoost);
    };
}
