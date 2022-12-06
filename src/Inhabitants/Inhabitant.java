package Inhabitants;

import java.util.Random;

public abstract class Inhabitant {

    private static final Random RANDOM = new Random();

    private String name;
    private int health;
    private boolean isAlive;
    private int experience;
    private int agility = 1;  // [1 - 30]
    private int strength;  // [1 - 100]
    private int luck;  // [1 - 100]
    private int gold;


    public Inhabitant(String name, int health) {
        this.name = name;
        if (health < 1) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName() + " health below zero.");
        } else if (health > 2000) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName() + " health above 2000");
        }
        this.health = health;
        isAlive = true;

        luck = RANDOM.nextInt(50) + 1;
    }

    public Inhabitant(String name, int health, int experience, int agility, int strength, int gold) {
        this(name, health);

        if (experience < 0 || experience > 10_000) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
                                                + " incorrect experience points");
        }
        this.experience = experience;
        if (agility < 0 || agility > 30) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
                                               + " incorrect agility points");
        }
        this.agility = agility;
        if (strength < 0 || strength > 100) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
                                               + " incorrect strength points");
        }
        this.strength = strength;
        if (gold < 0) {
            throw new IllegalArgumentException("In new " + this.getClass().getSimpleName()
                                               + " gold amount below zero");
        }
        this.gold = gold;
    }


    public void enemyLooting(Inhabitant enemy) {
        if (!enemy.isAlive) {
            this.gold += enemy.gold;
        }
    }

    public void doStrike(Inhabitant foe) {
        if (foe == this) {
            return;
        }
        int strikeStrength = strikeStrength();
        if (strikeStrength == 0) {
            System.out.printf("%s missed to %s%n", this.name, foe.name);
            return;
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
        return probabilityOfCriticalStrike <= luck;
    }

    private void applyDamage(int damage, Inhabitant foe) {
        if (foe.health > damage) {
            foe.health -= damage;
            System.out.printf("%s has dealt %d damage to %s", this.name, damage, foe.name);
        } else {
            foe.health = 0;
            foe.isAlive = false;
            System.out.printf("%s killed the %s", this.name, foe.name);
        }
    }
}