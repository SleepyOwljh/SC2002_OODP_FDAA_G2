public class Warrior extends Player {

	private final Action specialSkill;

	public Warrior() {
		super(260, 40, 20, 30);
		this.specialSkill = new ShieldBashAction();
	}

	@Override
	public Action getSpecialSkillAction() {
		return specialSkill;
	}
}
