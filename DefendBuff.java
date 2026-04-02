public class DefendBuff extends TemporaryEffects {
    private int defenseBoost;
    private boolean active;

    public DefendBuff() {
        super(2);
        this.defenseBoost = 10;
        this.active = false;
    }

    public void applyEffect(Combatant target) {
        if (this.active) {
            extendDuration(1);
        } else {
            target.setDefense(target.getDefense() + defenseBoost);
            this.active = true;
        }
    }

    public void removeEffect(Combatant target) {
        target.setDefense(target.getDefense() - defenseBoost);
        this.active = false;
    }
}
