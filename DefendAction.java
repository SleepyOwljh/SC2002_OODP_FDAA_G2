package Actions;

public class DefendAction implements Action {
    public DefendAction() {}

    public void execute(Combatant user, Combatant target) {
    Defendbuff effect = new Defendbuff();
    user.addEffect(effect);
    effect.applyEffect(user);

}
}
