package Inhabitants;

public class Skeleton extends Inhabitant {

    private final int WEAPON_POWER;
    private final Type type;

    public Skeleton(int level) {
        super(level < 20 ? "Skeleton"
                        : level < 40 ? "Skeleton " + Type.Defender
                        : level < 60 ? "Skeleton " + Type.Archer
                        : level < 80 ? "Skeleton " + Type.Sinister
                        : "Skeleton " + Type.Superior,
                50, level, 0);
        type = level < 20 ? Type.Common
                : level < 40 ? Type.Defender
                : level < 60 ? Type.Archer
                : level < 80 ? Type.Sinister
                : Type.Superior;
        switch (type) {
            case Common -> {
                WEAPON_POWER = 80;
                gold = RANDOM.nextInt(121) + 100;
            }
            case Defender -> {
                WEAPON_POWER = 140;
                gold = RANDOM.nextInt(151) + 120;
            }
            case Archer -> {
                WEAPON_POWER = 200;
                gold = RANDOM.nextInt(181) + 140;
            }
            case Sinister -> {
                WEAPON_POWER = 260;
                gold = RANDOM.nextInt(211) + 160;
            }
            case Superior -> {
                WEAPON_POWER = 360;
                gold = RANDOM.nextInt(271) + 200;
            }
            default -> WEAPON_POWER = -1;
        }
    }


    public Type getType() { return type; }

    @Override
    public int doStrike(Inhabitant foe) {
        return super.doStrike(WEAPON_POWER, foe);
    }

    @Override
    protected void arrangeNewSkillPoints() {
        super.arrangeNewSkillPoints();
        health = maxHealth += 20;
    }

    enum Type {
        Common,
        Defender,
        Archer,
        Sinister,
        Superior,
    }
}
