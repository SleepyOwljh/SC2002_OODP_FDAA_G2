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

    public void execute(Combatant user, java.util.List<Enemy> targets, Item item) {
        if (!(user instanceof Player) || item == null) {
            return;
        }

        Player player = (Player) user;

        if (item instanceof PowerStone) {
            ((PowerStone) item).useItem(user, targets);
        } else {
            Combatant fallbackTarget = user;
            if (targets != null && !targets.isEmpty()) {
                fallbackTarget = targets.get(0);
            }
            item.useItem(user, fallbackTarget);
        }

        player.getInventory().remove(item);
    }
}
