public class UseItemAction implements Action {
        
    private Item item;
    public UseItemAction() {}
    public void setItem(Item item) {
        this.item = item;
    }
    @Override
    public void execute(Combatant user, Combatant target) {
        if (item != null) {
            item.useItem(user, target);
            user.getInventory().remove(item);
            } 
        }
    }
