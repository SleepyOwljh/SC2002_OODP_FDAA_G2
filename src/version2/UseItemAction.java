public class UseItemAction implements Action {
    private Item item;

    public UseItemAction() {
    }

    public UseItemAction(Item item) {
        this.item = item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public void execute(Combatant user, Combatant target) {
        if (!(user instanceof Player) || item == null) {
            return;
        }

        Player player = (Player) user;
        item.useItem(user, target);
        player.getInventory().remove(item);
        item = null;
    }
}
