public interface Item {
    void useItem(Combatant user, Combatant target);
}

public class SmokeBomb implements Item{
    public SmokeBomb(){}
    public void useItem(Combatant user, Combatant target){
        SmokeBombEffect effect = new SmokeBombEffect();
        user.addEffect(effect);
        effect.applyEffect(user);
        System.out.println("You used the SmokeBomb! The enemies cannot hurt you for this turn and the next!");
    }

}

