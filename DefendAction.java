public class DefendAction implements Action {
    public DefendAction() {}

    public void execute(Combatant user, Combatant target) {
        for (StatusEffect effect : statusEffects) {
            if(effect instanceof DefendBuff) {
                effect.extendDuration(1);
        } else {
    DefendBuff effect = new DefendBuff();
    user.applyStatusEffect(effect);
    }
    }
}
}
