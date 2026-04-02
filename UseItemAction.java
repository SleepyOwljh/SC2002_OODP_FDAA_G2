package Actions;

public class UseItemAction implements Action {
    
        private Item item;
        public UseItemAction(Item item) {
            this.item = item;
        }

        public UseItemAction(){}
        public void execute(Combatant user, Combatant target) {
            item.user(user, target);
        }
    }

