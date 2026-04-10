public class Wizard extends Player {

	private final Action specialSkill;

	public Wizard() {
		// Health: 200, Attack: 50, Defense: 10, Speed: 20
		super(200, 50, 10, 20);
		this.specialSkill = new ArcaneBlastAction();
	}

	@Override
	public Action getSpecialSkillAction() {
		return specialSkill;
	}
}
