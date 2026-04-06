public class ShieldBashAction implements Action {
    public ShieldBashAction() {}

    public void execute(Combatant user, Combatant target) {
        int damage = user.getAttack();
        target.takeDamage(damage);
        StunEffect effect = new StunEffect();
        target.addEffect(effect);
    }
    
}
