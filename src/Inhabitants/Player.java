package Inhabitants;

import Items.Equipment;
import Items.Potion;
import Items.Weapon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Player extends Inhabitant {

    private final Inventory inventory = new Inventory();
    private static final int INITIAL_HEALTH = 500;

    private int currentWeaponPower;
    private int prevAgilityLevel;
    private int prevStrengthLevel;


    public Player(String name) {
        super(name, INITIAL_HEALTH);
    }


    public boolean pay(int gold) {
        if (this.gold >= gold) {
            this.gold -= gold;
            return true;
        } else {
            return false;
        }
    }

    public boolean getNewEquipment(Equipment equip) {
        return inventory.addEquipment(equip);
    }

    public void useEquipment() {
        if (!isAlive()) {
            return;
        }
        inventory.useItem();
    }

    public void leave() {
        inventory.potionsCoolDown.shutdown();
    }

    @Override
    public void doStrike(Inhabitant foe) {
        super.doStrike(currentWeaponPower, foe);
    }

    @Override
    public void displayStats() {
        System.out.println("=".repeat(70));
        System.out.printf("=%1$s%2$s%1$s=%n", " ".repeat(30), "My stats");
        super.displayStats();
        System.out.println("=".repeat(70));
    }

    @Override
    protected void arrangeNewSkillPoints() {
        health = maxHealth += 50;

        final String strength = "strength";
        final String luck = "luck";
        final String agility = "agility";

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try {
            final int level = getLevel();
            final boolean itsAgilityLevel = level % 3 == 0;  // Agility можно увеличивать только каждый третий уровень.
            System.out.printf("---==You have reached a new level - %d!==---%n", level);
            if (prevStrengthLevel >= MAX_STRENGTH && this.luck >= MAX_LUCK) {
                if (!itsAgilityLevel || prevAgilityLevel >= MAX_AGILITY) {
                    System.out.println("No skills that could be improved");
                    return;
                }
            }
            if (itsAgilityLevel) {
                System.out.println("You can add two points to either strength or luck, or one point to agility.");
            } else {
                System.out.println("You can add one point to either strength or luck.");
            }
            System.out.printf("My stats:  Strength - %d/%d,  Luck - %d/%d,  Agility - %d/%d%n",
                    prevStrengthLevel, MAX_STRENGTH, this.luck, MAX_LUCK, prevAgilityLevel, MAX_AGILITY);
            String answer = "null";
            while (true) {
                while (!answer.equalsIgnoreCase(strength) && !answer.equalsIgnoreCase((luck))
                        && !(itsAgilityLevel && answer.equalsIgnoreCase(agility))) {
                    if (itsAgilityLevel) {
                        System.out.print("Type \"strength\", \"luck\" or \"agility\": ");
                    } else {
                        System.out.print("Type \"strength\" or \"luck\": ");
                    }
                    answer = input.readLine();
                }
                if (answer.equalsIgnoreCase(strength) && prevStrengthLevel >= MAX_STRENGTH) {
                    System.out.println("Your strength already in maximum, try another skill");
                    continue;
                } else if (answer.equalsIgnoreCase(luck) && this.luck >= MAX_LUCK) {
                    System.out.println("Your luck already in maximum, try another skill");
                    continue;
                } else if (answer.equalsIgnoreCase(agility) && prevAgilityLevel >= MAX_AGILITY) {
                    System.out.println("Your agility already in maximum, try another skill");
                    continue;
                }
                break;
            }
            switch (answer) {
                case strength -> {
                    if (itsAgilityLevel) {
                        prevStrengthLevel += 2;
                        this.strength += 2;
                        System.out.println("Strength increased by 2 points");
                    } else {
                        ++prevStrengthLevel;
                        ++this.strength;
                        System.out.println("Strength increased by 1 point");
                    }
                }
                case luck -> {
                    if (itsAgilityLevel) {
                        this.luck += 2;
                        System.out.println("Luck increased by 2 points");
                    } else {
                        ++this.luck;
                        System.out.println("Luck increased by 1 point");
                    }
                }
                case agility -> {
                    ++prevAgilityLevel;
                    ++this.agility;
                    System.out.println("Agility increased by 1 point");
                }
            }
            System.out.printf("My stats:  Strength - %d/%d,  Luck - %d/%d,  Agility - %d/%d%n",
                    prevStrengthLevel, MAX_STRENGTH, this.luck, MAX_LUCK, prevAgilityLevel, MAX_AGILITY);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private class Inventory {

        private final List<Equipment> inventory = new LinkedList<>();
        private final int INVENTORY_SIZE = 10;

        private final ScheduledExecutorService potionsCoolDown = Executors.newScheduledThreadPool(2);
        private ScheduledFuture<?> agilityPotionCanceller;
        private ScheduledFuture<?> strengthPotionCanceller;


        private boolean addEquipment(Equipment equip) {
            if (inventory.size() < INVENTORY_SIZE) {
                inventory.add(equip);
                return true;
            } else {
                if (dropEquipment()) {
                    inventory.add(equip);
                    return true;
                }
            }
            return false;
        }

        private void display() {
            System.out.println("""
                    ======================================================================
                    ========                     My inventory                     ========
                    ======================================================================
                    """);
            var iterator = inventory.iterator();
            for (int i = 0; i < inventory.size(); ++i) {
                System.out.printf("= (%d)  %-62s=\n", i + 1, iterator.next());
            }
            System.out.println("""
                    ======================================================================
                    """);
        }

        private void useItem() {
            display();
            System.out.print("=   Which item do you want to use? Enter its number(or \"cancel\"): ");
            int number = pickItem();
            if (number == -1) {
                return;
            }
            Equipment equip = inventory.get(number - 1);
            inventory.remove(number - 1);
            System.out.println("=   You picked follow equipment:                                     =");
            System.out.printf("=   -> %-62s=%n", equip);
            if (equip instanceof Weapon weapon) {
                currentWeaponPower = weapon.power;
                System.out.printf("=   Now you can deal up to %3d damage!                               =%n",
                        currentWeaponPower);
            } else if (equip instanceof Potion potion) {
                switch (potion.type) {
                    case STRENGTH_POTION -> {
                        if (strengthPotionCanceller != null) {
                            strengthPotionCanceller.cancel(true);
                        }
                        strengthPotionCanceller = potionsCoolDown.schedule(() -> {
                            synchronized (skillsMonitor) {
                                if (!Thread.interrupted()) {
                                    strength = prevStrengthLevel;
                                }
                            }
                        }, 3, TimeUnit.MINUTES);
                        synchronized (skillsMonitor) {
                            prevStrengthLevel = strength;
                            strength += potion.statePointsImprovement;
                        }
                        System.out.printf("=   Now your strength increased to %3d for three minutes!            =%n",
                                strength);
                    }
                    case AGILITY_POTION -> {
                        if (agilityPotionCanceller != null) {
                            agilityPotionCanceller.cancel(true);
                        }
                        agilityPotionCanceller = potionsCoolDown.schedule(() -> {
                            synchronized (skillsMonitor) {
                                if (!Thread.interrupted()) {
                                    agility = prevAgilityLevel;
                                }
                            }
                        }, 5, TimeUnit.MINUTES);
                        synchronized (skillsMonitor) {
                            prevAgilityLevel = agility;
                            agility += potion.statePointsImprovement;
                        }
                        System.out.printf("=   Now your agility increased to %2d for five minutes!              =%n",
                                agility);
                    }
                    case HEALTH_POTION -> {
                        int actualHealing = potion.statePointsImprovement;
                        if (actualHealing >= maxHealth - health) {
                            health += actualHealing;
                        } else {
                            actualHealing = maxHealth - health;
                            health = maxHealth;
                        }
                        System.out.printf("=   You healed %4d HP, now you have %5d HP.                          =%n",
                                actualHealing, health);
                    }
                    default -> throw new IllegalStateException("Wrong used potion");
                }
            }
            System.out.println("======================================================================");
        }

        private boolean dropEquipment() {
            display();
            System.out.print("=   Which item do you want to abandon? Enter its number(or \"cancel\"): ");
            int number = pickItem();
            if (number == -1) {
                return false;
            }
            Equipment equip = inventory.get(number - 1);
            inventory.remove(number - 1);
            System.out.println("=   You abandon follow equipment:                                    =");
            System.out.printf("=   -> %-62s=%n", equip);
            System.out.println("======================================================================");
            return true;
        }

        private int pickItem() {
            int number = -1;
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (true) {
                    String answer = input.readLine();
                    try {
                        number = Integer.parseInt(answer);
                    } catch (NumberFormatException e) {
                        if (answer.equalsIgnoreCase("cancel")) {
                            System.out.println("=   All items remain in inventory...                                 =");
                            System.out.println("======================================================================");
                            return -1;
                        }
                        System.out.print("=   You entered an invalid answer, enter a number: ");
                        continue;
                    }
                    if (number <= 0 || number > inventory.size()) {
                        System.out.print("=   You entered a wrong number, take a look at inventory again: ");
                        continue;
                    }
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return number;
        }
    }
}
