public class ArcaneBlastAction implements Action {
    public ArcaneBlastAction() {}
    public void execute(Combatant user, Combatant target) {
    int damage = user.getAttack();
    target.takeDamage(damage);
    ArcaneBlastBuff effect = new ArcaneBlastBuff();
    user.applyStatusEffect(effect)
        }
    }
