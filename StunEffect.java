public class StunEffect extends TemporaryEffects{
    public StunEffect() {
        super(2);
    }

    public void applyEffect(Combatant target) {
        target.setIsAbleToAct(false);
    }

    public void removeEffect(Combatant target) {
        target.setIsAbleToAct(true);
    }
}
