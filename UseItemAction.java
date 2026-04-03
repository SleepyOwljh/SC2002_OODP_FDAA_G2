public class UseItemAction implements Action {
    
        private Item item;
        public UseItemAction(Item item) {
            this.item = itemToUse;
        }
        public void execute(Combatant user, Combatant target) {
            if (itemToUse != null) {
                itemToUse.useItem(user, target);
                user.getInventory().remove(itemToUse);
            } 
        }
    }
