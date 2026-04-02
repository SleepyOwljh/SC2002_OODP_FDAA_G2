interface StatusEffect {
    public void applyEffect(Combatant target) {};
    public void removeEffect(Combatant target) {};
}

abstract class TemporaryEffects implements StatusEffect {
    private int duration;

    public TemporaryEffects(int startingDuration) {
        this.duration = startingDuration;
    }

    public void countDuration() {};

    public boolean isExpired() {
        return duration <= 0;
    }
}

abstract class PermanentEffects implements StatusEffect {
    public PermanentEffects() {};
}
