import java.util.List;

public class PowerStone implements Item {
    public PowerStone() {
    }

    @Override
    public void useItem(Combatant user, Combatant target) {
        if (!(user instanceof Player)) {
            return;
        }

        Player player = (Player) user;
        player.getSpecialSkillAction().execute(user, target);
    }

    public void useItem(Combatant user, List<Enemy> targets) {
        if (!(user instanceof Player)) {
            return;
        }

        Player player = (Player) user;
        Action specialSkill = player.getSpecialSkillAction();
        if (specialSkill == null) {
            return;
        }

        if (specialSkill instanceof ArcaneBlastAction) {
            ((ArcaneBlastAction) specialSkill).execute(user, targets);
            return;
        }

        if (targets != null && !targets.isEmpty()) {
            specialSkill.execute(user, targets.get(0));
        }
    }

    @Override
    public String getDisplayName() {
        return "Power Stone";
    }
}
