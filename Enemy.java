public abstract class Enemy extends Combatant {

    public Enemy(String name, int hp, int attack, int defense, int speed) {
		super(name, hp, attack, defense, speed);
	}

	public Action getAction() {
		// Function to simulate enemy AI, currently only returns basic attack as per project specifications
		return new BasicAttackAction();
	}
}