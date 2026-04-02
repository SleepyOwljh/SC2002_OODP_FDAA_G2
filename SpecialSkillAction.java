package Actions;

public class SpecialSkillAction {
    public SpecialSkillAction() {}
    public void execute(Combatant user, Combatant target) {
        if (user.canUseSpecialSkill()) {
            useSpecialSkill();

            user.startCooldown();
        } else {
            System.out.println("Skill is still on cooldown");
        }
        }
    }

