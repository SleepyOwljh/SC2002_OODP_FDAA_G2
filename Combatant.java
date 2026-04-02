import java.util.ArrayList;
import java.util.List;

public abstract class Combatant {
	private int hp;
	private int maxHp;
	private int attack;
	private int defense;
	private int speed;
	protected List<StatusEffect> statusEffects;
	private boolean isInvulnerable;
	private boolean isAbleToAct;

	public Combatant(int hp, int attack, int defense, int speed) {
		this.maxHp = hp;
		this.hp = this.maxHp;
		this.attack = attack;
		this.defense = defense;
		this.speed = speed;
		this.statusEffects = new ArrayList<>();
		this.isInvulnerable = false;
		this.isAbleToAct = true;
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
		// To be changed
		return defense;
	}

	public int getSpeed() {
		return speed;
	}

	public void takeDamage(int amount) {
		
		int damage = Math.max(0, amount - defense);

		if (damage <= 0) {
			return;
		}
		if (isInvulnerable) {
			return;
		}
		setHp(hp - damage);
	}

	public boolean getInvulnerable() {
		return isInvulnerable;
	}

	public void setInvulnerable(boolean invulnerable) {
		this.isInvulnerable = invulnerable;
	}

	public boolean getIsAbleToAct() {
		return isAbleToAct;
	}

	public void setIsAbleToAct(boolean ableToAct) {
		this.isAbleToAct = ableToAct;
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
			effect.applyEffect(this);
		}
	}

	public void removeStatusEffect(StatusEffect effect) {
		if (effect != null) {
			statusEffects.remove(effect);

			boolean sameTypeStillExists = false;
			for (StatusEffect currentEffect : statusEffects) {
				if (currentEffect.getClass() == effect.getClass()) {
					sameTypeStillExists = true;
					break;
				}
			}

			if (!sameTypeStillExists) {
				effect.removeEffect(this);
			}
		}
	}

	public void processTurnStart() {
		for (StatusEffect effect : statusEffects) {
			effect.countDuration();
		}
	}
}
