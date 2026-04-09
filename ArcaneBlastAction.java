import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArcaneBlastAction implements Action {
    private List<Combatant> currentEnemies;
    private List<Enemy> snapshotTargets;
    private List<Integer> hpBefore;
    private List<Integer> defenses;
    private int startingAttack;
    private boolean fromPowerStone;

    public ArcaneBlastAction() {
        this.currentEnemies = new ArrayList<>();
    }

    @Override
    public void setTargets(List<? extends Combatant> enemies) {
        this.currentEnemies = new ArrayList<>();
        if (enemies != null) {
            this.currentEnemies.addAll(enemies);
        }
    }

    @Override
    public boolean prepare(Combatant user, BattleEngineInterface ui, List<Enemy> livingEnemies) {
        // Set all enemies as targets for AoE
        currentEnemies = new ArrayList<>(livingEnemies);

        // Capture pre-execution state for display
        snapshotTargets = new ArrayList<>(livingEnemies);
        hpBefore = new ArrayList<>();
        defenses = new ArrayList<>();
        startingAttack = user.getAttack();

        for (Enemy enemy : livingEnemies) {
            hpBefore.add(enemy.getHp());
            defenses.add(enemy.getDefense());
        }
        return true;
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

        if (!fromPowerStone) {
            user.startCooldown();
        }
    }

    @Override
    public void showResult(Combatant user, BattleEngineInterface ui) {
        ui.showAoESkill(user, this, snapshotTargets, hpBefore, defenses, startingAttack, fromPowerStone);
    }

    @Override
    public void setFromPowerStone(boolean fromPowerStone) {
        this.fromPowerStone = fromPowerStone;
    }

    @Override
    public String getActionName() {
        return "Arcane Blast";
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public boolean isAoE() {
        return true;
    }
}
