public interface Item {
    void useItem(Combatant user, Combatant target);
}

public class Potion implements Item{
    private int healAmount;

    public Potion(){
        this.healAmount = 100;
    }
    public void useItem(Combatant user, Combatant target){
        int hp = target.getHp();
        int maxHp = target.getMaxHp();
        int newHp = Math.min(hp + healAmount, maxHp);
        target.setHp(newHp);
        System.out.println("You used the potion and received healing!");
        System.out.println("Current HP: " + newHp);

    }
}

