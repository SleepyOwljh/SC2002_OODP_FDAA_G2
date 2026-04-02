public class SmokeBombEffect extends TemporaryEffects{
    public SmokeBombEffect() {
        super(2);
    };

    public void applyEffect(Combatant target) {
        target.setAttack(0);
    };

    public void removeEffect(Combatant target) {
        target.setAttack(target.getAttack());
    };
}
