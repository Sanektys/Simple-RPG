package Inhabitants;

import Items.Equipment;
import Items.Potion;
import Items.Weapon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class Player extends Inhabitant {

    private final Inventory inventory = new Inventory();
    private static final int INITIAL_HEALTH = 100;
    private static final int INITIAL_GOLD = 300;

    private int currentWeaponPower = 0;
    private volatile int prevAgilityLevel = agility;
    private volatile int prevStrengthLevel = strength;


    public Player(String name) {
        super(name, INITIAL_HEALTH, 1, INITIAL_GOLD);
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

    @Override
    public int doStrike(Inhabitant foe) {
        return super.doStrike(currentWeaponPower, foe);
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
        health = maxHealth += 30;

        final String strength = "strength";
        final String luck = "luck";
        final String agility = "agility";

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try {
            final int level = getLevel();
            final boolean itsAgilityLevel = level % 3 == 0;  // Agility можно увеличивать только каждый третий уровень.
            System.out.printf("%n---==You have reached a new level - %d!==---%n", level);
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
            synchronized (skillsMonitor) {
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

        private Thread agilityPotionCanceller;
        private Thread strengthPotionCanceller;


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
            System.out.println("=".repeat(120));
            System.out.printf("%1$s%2$s%3$s%2$s%1$s%n", "=".repeat(12), " ".repeat(42),
                    "My inventory");
            System.out.println("=".repeat(120));
            if (inventory.size() == 0) {
                System.out.printf("=%s=%n", " ".repeat(118));
                System.out.printf("=%1$s%2$s%1$s=%n", " ".repeat(48), "There is nothing here!");
                System.out.printf("=%s=%n", " ".repeat(118));
            } else {
                var iterator = inventory.iterator();
                for (int i = 1; i <= inventory.size(); ++i) {
                    System.out.printf("= (%d)  %-112s=%n", i, iterator.next());
                }
            }
            System.out.println("=".repeat(120));
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
            System.out.printf("=   %1$s%2$s=%n", "You picked follow equipment:", " ".repeat(87));
            System.out.printf("=   -> %-112s=%n", equip);
            if (equip instanceof Weapon weapon) {
                currentWeaponPower = weapon.power;
                System.out.printf("=   %1$s%2$s=%n",
                        String.format("Now you can deal up to %3d damage!", currentWeaponPower), " ".repeat(81));
                inventory.add(weapon);  // Взяли и положили обратно
            } else if (equip instanceof Potion potion) {
                switch (potion.type) {
                    case STRENGTH_POTION -> {
                        if (strengthPotionCanceller != null && strengthPotionCanceller.isAlive()) {
                            System.out.printf("=   %1$s%2$s=%n",
                                    "The strength potion is still working, you can't drink it any more!",
                                    " ".repeat(49));
                            inventory.add(potion);
                        } else {
                            strengthPotionCanceller = new Thread(() -> {
                                try {
                                    Thread.sleep(3 * 60 * 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                synchronized (skillsMonitor) {
                                    strength = prevStrengthLevel;
                                }
                                System.err.println("The potion of strength has ceased to work!");
                            });
                            strengthPotionCanceller.setDaemon(true);
                            strengthPotionCanceller.start();
                            synchronized (skillsMonitor) {
                                prevStrengthLevel = strength;
                                strength += potion.statePointsImprovement;
                            }
                            System.out.printf("=   %1$s%2$s=%n",
                                    String.format("Now your strength increased to %3d for three minutes!", strength),
                                    " ".repeat(62));
                        }
                    }
                    case AGILITY_POTION -> {
                        if (agilityPotionCanceller != null && agilityPotionCanceller.isAlive()) {
                            System.out.printf("=   %1$s%2$s=%n",
                                    "The agility potion is still working, you can't drink it any more!",
                                    " ".repeat(50));
                            inventory.add(potion);
                        } else {
                            agilityPotionCanceller = new Thread(() -> {
                                try {
                                    Thread.sleep(5 * 60 * 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                synchronized (skillsMonitor) {
                                    agility = prevAgilityLevel;
                                }
                                System.err.println("The potion of agility has ceased to work!");
                            });
                            agilityPotionCanceller.setDaemon(true);
                            agilityPotionCanceller.start();
                            synchronized (skillsMonitor) {
                                prevAgilityLevel = agility;
                                agility += potion.statePointsImprovement;
                            }
                            System.out.printf("=   %1$s%2$s=%n",
                                    String.format("Now your agility increased to %2d for five minutes!", agility),
                                    " ".repeat(65));
                        }
                    }
                    case HEALTH_POTION -> {
                        if (health == maxHealth) {
                            System.out.printf("=   %1$s%2$s=%n", "You don't need a potion, you have full health.",
                                    " ".repeat(69));
                            inventory.add(potion);
                        } else {
                            int healing = potion.statePointsImprovement;
                            if (health + healing >= maxHealth) {
                                healing = maxHealth - health;
                                health = maxHealth;
                            } else {
                                health += healing;
                            }
                            System.out.printf("=   %1$s%2$s=%n",
                                    String.format("You healed %3d HP, now you have %4d HP.", healing, health),
                                    " ".repeat(75));
                        }
                    }
                    default -> throw new IllegalStateException("Wrong used potion");
                }
            }
            System.out.println("=".repeat(120));
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
            System.out.printf("=   %1$s%2$s=%n", "You abandon follow equipment:", " ".repeat(86));
            System.out.printf("=   -> %-112s=%n", equip);
            System.out.println("=".repeat(120));
            return true;
        }

        private int pickItem() {
            int number = -1;
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            try {
                if (inventory.size() == 0) {
                    System.out.print("\n(Inventory is empty, press \"enter\" to exit): ");
                    input.readLine();
                    return -1;
                }
                while (true) {
                    String answer = input.readLine();
                    try {
                        number = Integer.parseInt(answer);
                    } catch (NumberFormatException e) {
                        if (answer.equalsIgnoreCase("cancel")) {
                            System.out.printf("=   %1$s%2$s=%n",
                                    "All items remain in inventory...", " ".repeat(83));
                            System.out.println("=".repeat(120));
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
