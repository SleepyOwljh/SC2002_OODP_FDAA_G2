public class Warrior extends Player {

	private Action specialSkill;
	
	public Warrior() {
		super("Warrior", 260, 40, 20, 30);
		this.specialSkill = new ShieldBashAction();
	}

	@Override
	public Action getSpecialSkillAction() {
		return this.specialSkill;
	};
}
