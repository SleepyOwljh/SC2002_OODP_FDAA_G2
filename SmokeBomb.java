public class SmokeBomb implements Item {
    public SmokeBomb() {
    }

    @Override
    public void useItem(Combatant user, Combatant target) {
        if (user != null) {
            user.applyStatusEffect(new SmokeBombEffect());
        }
    }

    @Override
    public String getDisplayName() {
        return "Smoke Bomb";
    }
}
