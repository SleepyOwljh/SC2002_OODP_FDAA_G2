public class Wizard extends Player {

	private Action specialSkill;

	public Wizard() {
		super("Wizard",200, 50, 10, 20);
		this.specialSkill = new ArcaneBlastAction();
	}

	@Override
	public Action getSpecialSkillAction() {
		
		return this.specialSkill;
	}
}
