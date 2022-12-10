package Inhabitants;

public class Skeleton extends Inhabitant {
    private final int WEAPON_POWER;

    public Skeleton(int level) {
        super(level < 20 ? "Goblin"
                        : level < 40 ? "Goblin Lumberjack"
                        : level < 60 ? "Goblin Butcher"
                        : level < 80 ? "Goblin Master" : "Goblin Behemoth",
                100 + level * 15, level, RANDOM.nextInt(381) + 120);
        WEAPON_POWER = level < 20 ? 200
                : level < 40 ? 260
                : level < 60 ? 320
                : level < 80 ? 380 : 440;
    }

    @Override
    public void doStrike(Inhabitant foe) {
        super.doStrike(WEAPON_POWER, foe);
    }
}
