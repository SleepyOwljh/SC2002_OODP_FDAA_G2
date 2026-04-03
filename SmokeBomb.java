public class SmokeBomb implements Item{
    public SmokeBomb(){}
    public void useItem(Combatant user, Combatant target){
        SmokeBombEffect effect = new SmokeBombEffect();
        user.applyStatusEffect(effect);
        effect.applyEffect(user);
    }

}

