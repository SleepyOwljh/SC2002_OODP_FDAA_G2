import java.util.ArrayList;
import java.util.List;

public abstract class Combatant {
	private int hp;
	private int maxHp;
	private int attack;
	private int defense;
	private int speed;
	private List<StatusEffect> statusEffects;

	public Combatant(int hp, int attack, int defense, int speed) {
		this.maxHp = hp;
		this.hp = this.maxHp;
		this.attack = attack;
		this.defense = defense;
		this.speed = speed;
		this.statusEffects = new ArrayList<>();
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int newHp) {
		if (newHp > maxHp) {
			this.hp = maxHp;
		} else if (newHp < 0) {
			this.hp = 0;
		} else {
			this.hp = newHp;
		}
	}

	public int getMaxHp() {
		return maxHp;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int newAtk) {
		this.attack = newAtk;
	}

	public int getDefense() {
		return defense;
	}

	public void setDefense(int newDef) {
		this.defense = newDef;
	}

	public int getEffectiveDefense() {
		// Future enhancement: include status effect modifiers.
		return defense;
	}

	public int getSpeed() {
		return speed;
	}

	public void takeDamage(int amount) {
		if (amount <= 0) {
			return;
		}
		setHp(hp - amount);
	}

	public void heal(int amount) {
		if (amount <= 0) {
			return;
		}
		setHp(hp + amount);
	}

	public boolean isAlive() {
		return hp > 0;
	}

	public void applyStatusEffect(StatusEffect effect) {
		if (effect != null) {
			statusEffects.add(effect);
		}
	}

	public void removeStatusEffect(StatusEffect effect) {
		if (effect != null) {
			statusEffects.remove(effect);
		}
	}

	public boolean ableToAct() {
		for (StatusEffect effect : statusEffects) {
			if (effect != null && "StunEffect".equalsIgnoreCase(effect.getClass().getSimpleName())) {
				return false;
			}
		}
		return true;
	}

	public void processTurnStart() {
		// Placeholder for future status effect processing.
	}
}
