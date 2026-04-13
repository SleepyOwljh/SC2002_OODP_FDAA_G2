import java.util.List;

public class ArcaneBlastAction implements Action {

    public ArcaneBlastAction() {
    }

    @Override
    public void execute(Combatant user, Combatant target) {
    }

    public void execute(Combatant user, List<Enemy> targets) {
        for (Combatant enemy : targets) {
            if (enemy == null || !enemy.isAlive()) {
                continue;
            }

            boolean wasAlive = enemy.isAlive();
            enemy.takeDamage(user.getAttack());
            if (wasAlive && !enemy.isAlive()) {
                user.applyStatusEffect(new ArcaneBlastBuff());
            }
        }
    }
}
