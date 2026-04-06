public class ArcaneBlastAction implements Action {
    private List<Combatant> allEnemies;
    
    public ArcaneBlastAction(List <Combatant> allEnemies) {
        this.allEnemies = allEnemies;
    }
    public void execute(Combatant user, Combatant target) {
        for (Combatant enemy : allEnemies) {
            if (enemy.isAlive()) {
                enemy.takeDamage(user.getAttack());
            }
    ArcaneBlastBuff effect = new ArcaneBlastBuff();
    user.applyStatusEffect(effect)
        
    }
