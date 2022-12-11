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
    private int nextLevelThreshold = 10;

    protected int agility = 1;  // [1 - 30]
    protected final int MAX_AGILITY = 30;
    protected int strength = 1;  // [1 - 100]
    protected final int MAX_STRENGTH = 100;
    protected int luck;  // [1 - 100]
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

        luck = RANDOM.nextInt(MAX_LUCK / 2) + 1;
    }

    public Inhabitant(String name, int maxHealth, int level, int gold) {
        this(name, maxHealth);

        if (level <= 0 || level > MAX_LEVEL) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
                                                + " incorrect level of entity");
        }
        this.level = level;
        for (int i = 1; i <= level; ++i) {
            arrangeNewSkillPoints();
        }
//        if (agility < 0 || agility > MAX_AGILITY) {
//            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
//                                               + " incorrect agility points");
//        }
//        this.agility = agility;
//        if (strength < 0 || strength > MAX_STRENGTH) {
//            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
//                                               + " incorrect strength points");
//        }
//        this.strength = strength;
        if (gold < 0) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
                                               + " gold amount below zero");
        }
        this.gold = gold;
    }


    public boolean isAlive() { return isAlive; }

    public int getLevel() { return level; }

    public void doStrike(Inhabitant foe) {
        doStrike(0, foe);
    }

    public void doStrike(int weaponPower, Inhabitant foe) {
        if (foe == this || !isAlive) {
            return;
        }
        int strikeStrength = strikeStrength();
        if (strikeStrength == 0) {
            System.out.printf("%s missed to %s%n", this.name, foe.name);
            return;
        }
        if (weaponPower != 0) {  // Перерасчёт урона от оружия от % навыка "сила"
            strikeStrength = (int) (weaponPower * (float) strikeStrength / 100.0f);
        }
        strikeStrength = isCriticalStrike() ? strikeStrength * 2 : strikeStrength;
        applyDamage(strikeStrength, foe);
    }

    private int strikeStrength() {
        int probabilityOfFail = RANDOM.nextInt(150) + 1 - luck;
        return (agility * 3 > probabilityOfFail) ? strength : 0;
    }

    private boolean isCriticalStrike() {
        int probabilityOfCriticalStrike = RANDOM.nextInt(200) + 1;
        return probabilityOfCriticalStrike <= luck;  // При 100% навыке "удача" крит. удар проходит 50 на 50
    }

    private void applyDamage(int damage, Inhabitant foe) {
        if (foe.health > damage) {
            foe.health -= damage;
            System.out.printf("%s has dealt %d damage to %s(remain %dHP)%n", this.name, damage, foe.name, foe.health);
        } else {
            foe.health = 0;
            foe.isAlive = false;
            System.out.printf("%s killed the %s%n", this.name, foe.name);
            rewardFor(foe);
        }
    }

    private void checkNextLevel() {
        while (experience >= nextLevelThreshold && level < MAX_LEVEL) {
            ++level;
            nextLevelThreshold += nextLevelThreshold * 1.1f;
            arrangeNewSkillPoints();
        }
    }

    protected void arrangeNewSkillPoints() {
        health = maxHealth += 50;
        if (level % 3 == 0) {
            switch (RANDOM.nextInt(5)) {
                case 0, 1, 2 -> {
                    if (agility < MAX_AGILITY) {
                        ++agility;
                    } else if (strength < MAX_STRENGTH) {
                        ++strength;
                    } else if (luck < MAX_LUCK) {
                        ++luck;
                    }}
                case 3 -> {
                    if (strength < MAX_STRENGTH - 1) {
                        strength += 2;
                    } else if (luck < MAX_LUCK - 1) {
                        luck += 2;
                    }}
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
                    }}
                case 1 -> {
                    if (luck < MAX_LUCK) {
                        ++luck;
                    } else {
                        strength = strength < MAX_STRENGTH ? strength + 1 : strength;
                    }}
                default -> throw new IllegalStateException("Error during auto arrange skills");
            }
        }
    }

    private void enemyLooting(Inhabitant foe) {
        if (!foe.isAlive) {
            this.gold += foe.gold;
            System.out.printf("%s takes %d gold from the body of %s%n", this.name, foe.gold, foe.name);
            foe.gold = 0;
        }
    }

    private void rewardFor(Inhabitant foe) {
        if (this != foe) {
            int earnedExperience = switch (foe.getClass().getSimpleName()) {
                case "Skeleton" -> 5 * foe.level;
                case "Goblin" -> 10 * foe.level;
                default -> throw new IllegalStateException("Reward for wrong enemy");
            };
            this.experience += earnedExperience;
            System.out.printf("You earned %d experience%n", earnedExperience);
            enemyLooting(foe);
            checkNextLevel();
        }
    }

    @Override
    public String toString() {
        return String.format("%s: level - %d (strength:%d, luck:%d, agility:%d", name, level, strength, luck, agility);
    }
}