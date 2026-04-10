public class DefendBuff extends TemporaryEffects {
    private final int defenseBoost;

    public DefendBuff() {
        super(2);
        this.defenseBoost = 10;
    }

    @Override
    public void applyEffect(Combatant target) {
        target.setDefense(target.getDefense() + defenseBoost);
    }

    @Override
    public void removeEffect(Combatant target) {
        target.setDefense(target.getDefense() - defenseBoost);
    }
}
