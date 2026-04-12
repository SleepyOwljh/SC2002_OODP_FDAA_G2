public class SmokeBombEffect extends TemporaryEffects{
    public SmokeBombEffect() {
        super(2);
    }

    @Override
    public void applyEffect(Combatant target) {
        target.setInvulnerable(true);
    }

    @Override
    public void removeEffect(Combatant target) {
        target.setInvulnerable(false);
    }
}
