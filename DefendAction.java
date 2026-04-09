import java.util.List;

public class DefendAction implements Action {
    private Combatant target;

    public DefendAction() {
    }

    @Override
    public boolean prepare(Combatant user, BattleEngineInterface ui, List<Enemy> livingEnemies) {
        target = user;
        return true;
    }

    @Override
    public Combatant getTarget() {
        return target;
    }

    @Override
    public void execute(Combatant user, Combatant target) {
        if (user == null || !user.isAlive()) {
            return;
        }

        for (StatusEffect effect : user.getStatusEffects()) {
            if (effect instanceof DefendBuff) {
                effect.extendDuration(1);
                return;
            }
        }

        user.applyStatusEffect(new DefendBuff());
    }

    @Override
    public void showResult(Combatant user, BattleEngineInterface ui) {
        ui.showDefend(user);
    }

    @Override
    public String getActionName() {
        return "Defend";
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public boolean isSelfTargeting() {
        return true;
    }
}
