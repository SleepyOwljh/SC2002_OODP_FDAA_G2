public class ArcaneBlastBuff extends PermanentEffects {
    private final int attackBonus;

    public ArcaneBlastBuff() {
        this.attackBonus = 10;
    }

    @Override
    public void applyEffect(Combatant target) {
        target.setAttack(target.getAttack() + attackBonus);
    }

    @Override
    public void removeEffect(Combatant target) {
        target.setAttack(target.getAttack() - attackBonus);
    }
}
