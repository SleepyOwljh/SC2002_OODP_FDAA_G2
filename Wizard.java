import java.util.List;

public class Wizard extends Player {

	public Wizard() {
		super(200, 50, 10, 20);
		
	}

	public void useSpecialSkill(List<Combatant> targets) {
		
	}

	@Override
	public Action getSpecialSkillAction() {
		
		return null;
	}
}
