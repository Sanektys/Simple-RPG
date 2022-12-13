package Inhabitants;

public class Skeleton extends Inhabitant {
    private final int WEAPON_POWER;

    public Skeleton(int level) {
        super(level < 20 ? "Skeleton"
                        : level < 40 ? "Skeleton Defender"
                        : level < 60 ? "Skeleton Archer"
                        : level < 80 ? "Goblin Sinister" : "Skeleton Superior",
                50, level, RANDOM.nextInt(170) + 90);
        WEAPON_POWER = level < 20 ? 80
                : level < 40 ? 140
                : level < 60 ? 200
                : level < 80 ? 260 : 360;
    }

    @Override
    public int doStrike(Inhabitant foe) {
        return super.doStrike(WEAPON_POWER, foe);
    }

    @Override
    protected void arrangeNewSkillPoints() {
        super.arrangeNewSkillPoints();
        health = maxHealth += 15;
    }
}
