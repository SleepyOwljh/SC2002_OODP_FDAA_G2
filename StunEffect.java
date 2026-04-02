public class StunEffect extends TemporaryEffects{
    public StunEffect() {
        super(2);
    };

    public void applyEffect(Combatant target) {
        target.ableToAct = false;
    };

    public void removeEffect(Combatant target) {
        target.ableToAct = true;
    };
}
