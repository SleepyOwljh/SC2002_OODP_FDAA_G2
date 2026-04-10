public class BasicAttackAction implements Action {
    public BasicAttackAction() {
    }

    @Override
    public void execute(Combatant user, Combatant target) {
        if (user == null || target == null || !user.isAlive() || !target.isAlive()) {
            return;
        }

        target.takeDamage(user.getAttack());
    }
}