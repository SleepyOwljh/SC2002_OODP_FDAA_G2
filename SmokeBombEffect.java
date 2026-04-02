public class SmokeBombEffect extends TemporaryEffects{
    public SmokeBombEffect() {
        super(2);
    }

    public void applyEffect(Combatant target) {
        target.setInvulnerable(true);
    }

    public void removeEffect(Combatant target) {
        target.setInvulnerable(false);
    }
}
