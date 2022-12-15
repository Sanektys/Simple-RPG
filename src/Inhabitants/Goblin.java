package Inhabitants;

public class Goblin extends Inhabitant {

    private final int WEAPON_POWER;
    private final Type type;

    public Goblin(int level) {
        super(level < 20 ? "Goblin"
                        : level < 40 ? "Goblin " + Type.Butcher
                        : level < 60 ? "Goblin " + Type.Beast
                        : level < 80 ? "Goblin " + Type.Master
                        : "Goblin " + Type.Behemoth,
                80, level, 0);
        type = level < 20 ? Type.Common
                : level < 40 ? Type.Butcher
                : level < 60 ? Type.Beast
                : level < 80 ? Type.Master
                : Type.Behemoth;
        switch(type) {
            case Common -> {
                WEAPON_POWER = 50;
                gold = RANDOM.nextInt(81) + 50;
            }
            case Butcher -> {
                WEAPON_POWER = 100;
                gold = RANDOM.nextInt(111) + 70;
            }
            case Beast -> {
                WEAPON_POWER = 150;
                gold = RANDOM.nextInt(141) + 90;
            }
            case Master -> {
                WEAPON_POWER = 200;
                gold = RANDOM.nextInt(171) + 110;
            }
            case Behemoth -> {
                WEAPON_POWER = 300;
                gold = RANDOM.nextInt(231) + 150;
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
        health = maxHealth += 25;
    }

    enum Type {
        Common,
        Butcher,
        Beast,
        Master,
        Behemoth,
    }
}
