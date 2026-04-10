public class StunEffect extends TemporaryEffects{
    public StunEffect() {
        super(2);
    }

    @Override
    public void applyEffect(Combatant target) {
        target.setIsAbleToAct(false);
    }

    @Override
    public void removeEffect(Combatant target) {
        target.setIsAbleToAct(true);
    }
}
