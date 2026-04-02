public class ArcaneBlastBuff extends PermanentEffects {
    private int attackBonus;

    public ArcaneBlastBuff() {
        this.attackBonus = 10;
    }

    public void applyEffect(Combatant target) {
        target.setAttack(target.getAttack() + attackBonus);
    }

    public void removeEffect(Combatant target) {
        target.setAttack(target.getAttack() - attackBonus);
    }
}
