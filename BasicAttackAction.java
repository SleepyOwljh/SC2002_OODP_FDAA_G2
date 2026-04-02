package Actions;
import java.util.Scanner;

public class BasicAttackAction implements Action {
    public BasicAttackAction() {}
    public void execute (Combatant user, Combatant target) {
        int damage = user.getAttack();
        target.takeDamage(damage);
            
        }
    }
    

