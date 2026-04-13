public class UseItemAction implements Action {

    public UseItemAction() {
    }

    @Override
    public void execute(Combatant user, Combatant target) {
    }


    public void execute(Combatant user, Combatant target, Item item) {
        if (!(user instanceof Player) || item == null) {
            return;
        }

        Player player = (Player) user;
        item.useItem(user, target);
        player.getInventory().remove(item);
    }
}
