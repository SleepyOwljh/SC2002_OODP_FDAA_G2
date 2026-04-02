import java.util.ArrayList;
import java.util.List;

public abstract class Player extends Combatant {
	private List<Item> inventory;
	private int specialSkillCooldown;

	public Player(String name, int hp, int attack, int defense, int speed) {
		super(name, hp, attack, defense, speed);
		this.inventory = new ArrayList<>();
		this.specialSkillCooldown = 0;
	}

	public List<Item> getInventory() {
		return inventory;
	}

	public void setInventory(List<Item> inventory) {
		this.inventory = inventory;
	}

	public int getSkillCooldown() {
		return specialSkillCooldown;
	}

	public void useItem(Item item, Combatant target) {
        item.useItem(this, target);
        // TODO update removal of item
        inventory.remove(item);
	}

	public boolean canUseSpecialSkill() {
		return specialSkillCooldown == 0;
	}

	public void startCooldown() {
		specialSkillCooldown = 3;
	}

	public void defend() {
        applyStatusEffect(new DefendBuff());
	}

	public void reduceCooldown() {
		if (specialSkillCooldown > 0) {
			specialSkillCooldown--;
		}
	}

    //override in subclass warrior and wizard
	public abstract Action getSpecialSkillAction();

	@Override
	public void processTurnStart() {
		for (StatusEffect effect : statusEffects) {
			effect.countDuration();
		}
		this.reduceCooldown();
	}
}
