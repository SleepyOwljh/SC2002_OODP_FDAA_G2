import java.util.List;

public class ShieldBashAction implements Action {
    private Combatant target;
    private int hpBefore;
    private int targetDefense;
    private boolean fromPowerStone;

    public ShieldBashAction() {
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
        if (target.isAlive()) {
            target.applyStatusEffect(new StunEffect());
        }

        if (!fromPowerStone) {
            user.startCooldown();
        }
    }

    @Override
    public void showResult(Combatant user, BattleEngineInterface ui) {
        ui.showTargetedSkill(user, this, target, hpBefore, targetDefense, fromPowerStone);
    }

    @Override
    public void setFromPowerStone(boolean fromPowerStone) {
        this.fromPowerStone = fromPowerStone;
    }

    @Override
    public String getActionName() {
        return "Shield Bash";
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean appliesStun() {
        return true;
    }
}
