package Inhabitants;

public class Goblin extends Inhabitant {

    private final int WEAPON_POWER;

    public Goblin(int level) {
        super(level < 20 ? "Goblin"
                        : level < 40 ? "Goblin Lumberjack"
                        : level < 60 ? "Goblin Butcher"
                        : level < 80 ? "Goblin Master" : "Goblin Behemoth",
                200 + level * 25, level, RANDOM.nextInt(251) + 50);
        WEAPON_POWER = level < 20 ? 100
                : level < 40 ? 150
                : level < 60 ? 200
                : level < 80 ? 250 : 300;
    }

    @Override
    public void doStrike(Inhabitant foe) {
        super.doStrike(WEAPON_POWER, foe);
    }
}
