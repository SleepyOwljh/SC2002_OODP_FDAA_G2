public class Warrior extends Player {

	private final Action specialSkill;

	public Warrior() {
		// Health: 260, Attack: 40, Defense: 20, Speed: 30
		super(260, 40, 20, 30);
		this.specialSkill = new ShieldBashAction();
	}

	@Override
	public Action getSpecialSkillAction() {
		return specialSkill;
	}
}
