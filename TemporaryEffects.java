abstract class TemporaryEffects implements StatusEffect {
    private int duration;

    public TemporaryEffects(int startingDuration) {
        this.duration = startingDuration;
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
    public void extendDuration(int additionalDuration) {
        if (additionalDuration > 0) {
            this.duration += additionalDuration;
        }
    }
}
