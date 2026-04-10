import java.util.List;

public class BasicAttackAction implements Action {
    private Combatant target;
    private int hpBefore;
    private int targetDefense;

    public BasicAttackAction() {
    }

    @Override
    public boolean prepare(Combatant user, BattleEngineInterface ui, List<Enemy> livingEnemies) {
        target = ui.getTargetChoice(livingEnemies);
        if (target == null) {
            return false;
        }
        hpBefore = target.getHp();
        targetDefense = target.getDefense();
        return true;
    }

    @Override
    public Combatant getTarget() {
        return target;
    }

    @Override
    public void execute(Combatant user, Combatant target) {
        if (user == null || target == null || !user.isAlive() || !target.isAlive()) {
            return;
        }

        target.takeDamage(user.getAttack());
    }

    @Override
    public void showResult(Combatant user, BattleEngineInterface ui) {
        ui.showSingleTargetAttack(user, this, target, hpBefore, targetDefense);
    }

    @Override
    public String getActionName() {
        return "BasicAttack";
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}