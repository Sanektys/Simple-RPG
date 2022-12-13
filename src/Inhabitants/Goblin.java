package Inhabitants;

public class Goblin extends Inhabitant {

    private final int WEAPON_POWER;

    public Goblin(int level) {
        super(level < 20 ? "Goblin"
                        : level < 40 ? "Goblin Lumberjack"
                        : level < 60 ? "Goblin Butcher"
                        : level < 80 ? "Goblin Master" : "Goblin Behemoth",
                50, level, RANDOM.nextInt(121) + 40);
        WEAPON_POWER = level < 20 ? 50
                : level < 40 ? 100
                : level < 60 ? 150
                : level < 80 ? 200 : 300;
    }

    @Override
    public int doStrike(Inhabitant foe) {
        return super.doStrike(WEAPON_POWER, foe);
    }

    @Override
    protected void arrangeNewSkillPoints() {
        super.arrangeNewSkillPoints();
        health = maxHealth += 25;
    }
}
