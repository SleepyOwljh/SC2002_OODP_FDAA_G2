public class DefendAction implements Action {
    public DefendAction() {
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
}
