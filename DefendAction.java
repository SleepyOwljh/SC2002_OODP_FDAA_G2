public class DefendAction implements Action {
    public DefendAction() {}

    public void execute(Combatant user, Combatant target) {
    DefendBuff effect = new DefendBuff();
    user.applyStatusEffect(effect);
    }
}
