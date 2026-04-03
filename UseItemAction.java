public class UseItemAction implements Action {
    
        private Item item;
        public UseItemAction(Item item) {
            this.item = item;
        }
        public void execute(Combatant user, Combatant target) {
            if (item != null) {
                item.useItem(user, target);
                user.getInventory().remove(itemToUse);
            } 
        }
    }
