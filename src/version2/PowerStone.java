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

    @Override
    public String getDisplayName() {
        return "Power Stone";
    }
}
