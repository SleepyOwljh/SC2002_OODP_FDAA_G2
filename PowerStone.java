public class PowerStone implements Item{
    public PowerStone(){}
    public void useItem(Combatant user, Combatant target){
        user.getSpecialSkillAction();
        user.execute();
    }

}

