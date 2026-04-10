import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArcaneBlastAction implements Action {
    private List<Combatant> currentEnemies;

    public ArcaneBlastAction() {
        this.currentEnemies = new ArrayList<>();
    }

    public void setCurrentEnemies(List<? extends Combatant> enemies) {
        this.currentEnemies = new ArrayList<>();
        if (enemies != null) {
            this.currentEnemies.addAll(enemies);
        }
    }

    @Override
    public void execute(Combatant user, Combatant target) {
        List<Combatant> targets = currentEnemies;
        if ((targets == null || targets.isEmpty()) && target != null) {
            targets = Collections.singletonList(target);
        }

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
