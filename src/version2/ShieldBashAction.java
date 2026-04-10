public class ShieldBashAction implements Action {
    public ShieldBashAction() {
    }

    @Override
    public void execute(Combatant user, Combatant target) {
        if (user == null || target == null || !user.isAlive() || !target.isAlive()) {
            return;
        }

        target.takeDamage(user.getAttack());
        if (target.isAlive()) {
            target.applyStatusEffect(new StunEffect());
        }
    }
}
