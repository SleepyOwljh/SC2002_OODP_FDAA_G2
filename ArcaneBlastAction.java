public class ArcaneBlastAction implements Action {
    public ArcaneBlastAction() {}
    public void execute(Combatant user, Combatant target) {
    ArcaneBlastBuff effect = new ArcaneBlastBuff();
    int damage = user.getAttack();
    target.takeDamage(damage);
    if (!target.isAlive) {
        user.addEffect(effect);
        effect.applyEffect(user);
    }
}
}
