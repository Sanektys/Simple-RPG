package Inhabitants;

import java.util.Random;

public abstract class Inhabitant {

    protected static final Random RANDOM = new Random();

    private final String name;
    protected int health;
    protected int maxHealth;
    private boolean isAlive;

    private int experience;
    private int level = 1;  // [1 - 100]
    private final int MAX_LEVEL = 100;
    private float nextLevelThreshold = 10f;

    protected volatile int agility = 1;  // [1 - 30]
    protected final int MAX_AGILITY = 30;
    protected volatile int strength = 1;  // [1 - 100]
    protected final int MAX_STRENGTH = 100;
    protected int luck = 1;  // [1 - 100]
    protected final int MAX_LUCK = 100;
    protected final Object skillsMonitor = new Object();

    protected int gold;


    public Inhabitant(String name, int maxHealth) {
        this.name = name;
        if (maxHealth < 1) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName() + " health below zero.");
        } else if (maxHealth > 3000) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName() + " health above 3000");
        }
        this.maxHealth = maxHealth;
        this.health =  maxHealth;
        isAlive = true;
    }

    public Inhabitant(String name, int maxHealth, int level, int gold) {
        this(name, maxHealth);

        if (level <= 0 || level > MAX_LEVEL) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
                                                + " incorrect level of entity");
        }
        for (int i = 1; i < level; ++i) {
            ++this.level;
            arrangeNewSkillPoints();
        }
        if (gold < 0) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
                                               + " gold amount below zero");
        }
        this.gold = gold;
    }


    public boolean isAlive() { return isAlive; }

    public int getLevel() { return level; }

    public String getShortStats() {
        return String.format("(%s) [%d/%dHP]", name, health, maxHealth);
    }

    public void displayStats() {
        System.out.printf("= Level %-3d  Experience %6d/%-6d  Health %4d/%-4d  Gold %-5d%s=%n",
                level, experience, (int) nextLevelThreshold, health, maxHealth, gold, "  ");
        System.out.printf("=      Strength %3d/%-3d%7$sLuck %3d/%-3d%7$sAgility %2d/%-2d       =%n",
                strength, MAX_STRENGTH, luck, MAX_LUCK, agility, MAX_AGILITY, " ".repeat(7));
    }

    public int doStrike(Inhabitant foe) {
        return doStrike(0, foe);  // Если сила оружия 0, то драка "кулаками", т.е. исключительно от strength
    }

    public int doStrike(int weaponPower, Inhabitant foe) {
        if (foe == this || !isAlive) {
            return -1;
        }
        if (weaponPower < 0) {
            throw new IllegalStateException("Power of weapon is below zero");
        }
        int strikeStrength = strikeStrength();
        if (strikeStrength == 0) {
            return 0;
        }
        if (weaponPower != 0) {  // Перерасчёт урона от оружия от % навыка "сила"
            strikeStrength = (int) (weaponPower * (float) strikeStrength / 100.0f);
            if (strikeStrength == 0) {
                strikeStrength = 1;
            }
        }
        strikeStrength = isCriticalStrike() ? strikeStrength * 2 : strikeStrength;
        applyDamage(strikeStrength, foe);
        return strikeStrength;
    }

    private int strikeStrength() {
        int probabilityOfFail = RANDOM.nextInt(level * 2 + 6) + 1 - luck;
        return (agility * 3 > probabilityOfFail) ? strength : 0;
    }

    private boolean isCriticalStrike() {
        int probabilityOfCriticalStrike = RANDOM.nextInt(200) + 1;
        return probabilityOfCriticalStrike <= luck;  // При 100% навыке "удача" крит. удар проходит 50 на 50
    }

    private void applyDamage(int damage, Inhabitant foe) {
        if (foe.health > damage) {
            foe.health -= damage;
        } else {
            foe.health = 0;
            foe.isAlive = false;
            System.out.printf("\t%s killed %s%n", this.name, foe.name);
            if (this instanceof Player) {
                rewardFor(foe);
            }
        }
    }

    private void checkNextLevel() {
        while (experience >= (int) nextLevelThreshold && level < MAX_LEVEL) {
            ++level;
            nextLevelThreshold += nextLevelThreshold * 0.4f;
            arrangeNewSkillPoints();
        }
    }

    protected void arrangeNewSkillPoints() {
        synchronized (skillsMonitor) {
            if (level % 3 == 0) {
                switch (RANDOM.nextInt(5)) {
                    case 0, 1, 2 -> {
                        if (agility < MAX_AGILITY) {
                            ++agility;
                        } else if (strength < MAX_STRENGTH) {
                            ++strength;
                        } else if (luck < MAX_LUCK) {
                            ++luck;
                        }
                    }
                    case 3 -> {
                        if (strength < MAX_STRENGTH - 1) {
                            strength += 2;
                        } else if (luck < MAX_LUCK - 1) {
                            luck += 2;
                        }
                    }
                    case 4 -> {
                        if (luck < MAX_LUCK - 1) {
                            luck += 2;
                        } else if (strength < MAX_STRENGTH - 1) {
                            strength += 2;
                        }
                    }
                    default -> throw new IllegalStateException("Error during auto arrange skills");
                }
            } else {
                switch (RANDOM.nextInt(2)) {
                    case 0 -> {
                        if (strength < MAX_STRENGTH) {
                            ++strength;
                        } else {
                            luck = luck < MAX_LUCK ? luck + 1 : luck;
                        }
                    }
                    case 1 -> {
                        if (luck < MAX_LUCK) {
                            ++luck;
                        } else {
                            strength = strength < MAX_STRENGTH ? strength + 1 : strength;
                        }
                    }
                    default -> throw new IllegalStateException("Error during auto arrange skills");
                }
            }
        }
    }

    private void enemyLooting(Inhabitant foe) {
        if (!foe.isAlive) {
            this.gold += foe.gold;
            System.out.printf("\t%s takes %d gold from the body of %s%n", this.name, foe.gold, foe.name);
            foe.gold = 0;
        }
    }

    private void rewardFor(Inhabitant foe) {
        if (this != foe) {
            int earnedExperience;
            if (foe instanceof Skeleton skeleton) {
                earnedExperience = switch (skeleton.getType()) {
                    case Common   -> 8 * foe.level;
                    case Defender -> 14 * foe.level;
                    case Archer   -> 22 * foe.level;
                    case Sinister -> 32 * foe.level;
                    case Superior -> 46 * foe.level;
                };
            } else if (foe instanceof Goblin goblin) {
                earnedExperience = switch (goblin.getType()) {
                    case Common   -> 12 * foe.level;
                    case Butcher  -> 20 * foe.level;
                    case Beast    -> 32 * foe.level;
                    case Master   -> 48 * foe.level;
                    case Behemoth -> 70 * foe.level;
                };
            } else {
                throw new IllegalStateException("Reward for wrong enemy");
            }
            this.experience += earnedExperience;
            System.out.printf("\tYou earned %d experience%n", earnedExperience);
            enemyLooting(foe);
            checkNextLevel();
        }
    }

    @Override
    public String toString() {
        return String.format("%s: level - %d (strength:%d, luck:%d, agility:%d)", name, level, strength, luck, agility);
    }
}