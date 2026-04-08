abstract class TemporaryEffects implements StatusEffect {
    private int duration;

    public TemporaryEffects(int startingDuration) {
        this.duration = startingDuration;
    }

    public void countDuration() {
        duration--;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public void extendDuration(int additionalDuration) {
        this.duration += additionalDuration;
    }
}
