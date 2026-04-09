import java.util.List;

public class UseItemAction implements Action {
    private Item item;
    private Combatant target;
    private int hpBefore;
    private Action delegatedAction;

    public UseItemAction() {
    }

    public UseItemAction(Item item) {
        this.item = item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public boolean prepare(Combatant user, BattleEngineInterface ui, List<Enemy> livingEnemies) {
        Item chosenItem = ui.getItemChoice(user);
        if (chosenItem == null) {
            ui.showItemCancel(user);
            return false;
        }

        this.item = chosenItem;

        if (chosenItem.triggersSpecialSkill()) {
            // Power Stone: consume item, then delegate to special skill
            user.removeFromInventory(chosenItem);
            Action specialSkill = user.getSpecialSkillAction();
            specialSkill.setFromPowerStone(true);
            if (!specialSkill.prepare(user, ui, livingEnemies)) {
                return false;
            }
            this.delegatedAction = specialSkill;
            return true;
        }

        // Normal item (Potion, Smoke Bomb, etc.)
        target = user;
        hpBefore = user.getHp();
        return true;
    }

    @Override
    public Combatant getTarget() {
        if (delegatedAction != null) {
            return delegatedAction.getTarget();
        }
        return target;
    }

    @Override
    public void execute(Combatant user, Combatant target) {
        if (delegatedAction != null) {
            delegatedAction.execute(user, delegatedAction.getTarget());
            return;
        }

        if (item == null || !user.hasInventory()) {
            return;
        }

        item.useItem(user, target);
        user.removeFromInventory(item);
    }

    @Override
    public void showResult(Combatant user, BattleEngineInterface ui) {
        if (delegatedAction != null) {
            delegatedAction.showResult(user, ui);
            return;
        }
        ui.showItemUse(user, item, hpBefore);
    }

    @Override
    public String getActionName() {
        return "Use Item";
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public boolean isSelfTargeting() {
        return true;
    }
}
