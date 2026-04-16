abstract class TemporaryEffects implements StatusEffect {
    private int duration;
    private final int originalDuration;

    public TemporaryEffects(int startingDuration) {
        this.duration = startingDuration;
        this.originalDuration = startingDuration;
    }

    @Override
    public void countDuration() {
        if (duration > 0) {
            duration--;
        }
    }

    @Override
    public boolean isExpired() {
        return duration <= 0;
    }

    @Override
    public void resetDuration() {
        this.duration = originalDuration;
    }
}
