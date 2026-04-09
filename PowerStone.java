public class PowerStone implements Item {
    public PowerStone() {
    }

    @Override
    public void useItem(Combatant user, Combatant target) {
        Action specialSkill = user.getSpecialSkillAction();
        if (specialSkill != null) {
            specialSkill.execute(user, target);
        }
    }

    @Override
    public String getDisplayName() {
        return "Power Stone";
    }

    @Override
    public boolean triggersSpecialSkill() {
        return true;
    }
}
