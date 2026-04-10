public abstract class Enemy extends Combatant {

	private static final Action DEFAULT_ACTION = new BasicAttackAction();

	public Enemy(int hp, int attack, int defense, int speed) {
		super(hp, attack, defense, speed);
	}

	public Action getAction() {
		return DEFAULT_ACTION;
	}
}