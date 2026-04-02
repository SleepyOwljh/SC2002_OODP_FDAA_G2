public class PowerStone implements Item{
    public void useItem(Combatant user, Combatant target){
        user.getSpecialSkillAction();
        System.out.println("You used the PowerStone! You can use your special skill now!");
    }

}

