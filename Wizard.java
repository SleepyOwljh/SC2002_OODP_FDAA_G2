public class Wizard extends Player {

	private final Action specialSkill;

	public Wizard() {
		super(200, 50, 10, 20);
		this.specialSkill = new ArcaneBlastAction();
	}

	@Override
	public Action getSpecialSkillAction() {
		return specialSkill;
	}
}
