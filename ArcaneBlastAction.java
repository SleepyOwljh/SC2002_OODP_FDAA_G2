public class ArcaneBlastAction implements Action {
    public ArcaneBlastAction() {}
    public void execute(Combatant user, Combatant target) {
    ArcaneBlastBuff effect = new ArcaneBlastBuff();
    int damage = 50;
    target.takeDamage(damage);
    if (target.getHp() == 0) {
        user.addEffect(effect);
        effect.applyEffect(user);
    }
}
}