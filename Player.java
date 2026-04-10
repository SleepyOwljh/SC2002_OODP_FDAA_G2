import java.util.ArrayList;
import java.util.List;

public abstract class Player extends Combatant {
	private List<Item> inventory;
	private int specialSkillCooldown;

	public Player(int hp, int attack, int defense, int speed) {
		super(hp, attack, defense, speed);
		this.inventory = new ArrayList<>();
		this.specialSkillCooldown = 0;
	}

	@Override
	public boolean hasInventory() {
		return true;
	}

	@Override
	public List<Item> getInventory() {
		return inventory;
	}

	public void setInventory(List<Item> inventory) {
		this.inventory = inventory;
	}

	@Override
	public void removeFromInventory(Item item) {
		inventory.remove(item);
	}

	@Override
	public int getSkillCooldown() {
		return specialSkillCooldown;
	}

	@Override
	public boolean canUseSpecialSkill() {
		return specialSkillCooldown == 0;
	}

	@Override
	public void startCooldown() {
		specialSkillCooldown = 3;
	}

	public void reduceCooldown() {
		if (specialSkillCooldown > 0) {
			specialSkillCooldown--;
		}
	}

	@Override
	public boolean isPlayerControlled() {
		return true;
	}

	@Override
	public abstract Action getSpecialSkillAction();

	@Override
	public void processTurnStart() {
		super.processTurnStart();
		reduceCooldown();
	}
}
