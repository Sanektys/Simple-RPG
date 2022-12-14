package Inhabitants;

import Items.Equipment;
import Items.Potion;
import Items.Weapon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Seller {

    private final Map<Equipment, Integer> catalog = new ConcurrentHashMap<>();
    private final Map<Potion.Type, Integer> maxPotionCount;

    private final ScheduledExecutorService assortmentUpdater = Executors.newSingleThreadScheduledExecutor();
    private final int ASSORTMENT_UPDATE_TIME = 15;

    private final Potion WEAK_STRENGTH_POTION   = new Potion(Potion.Type.STRENGTH_POTION,
               8, 340);
    private final Potion STRENGTH_POTION        = new Potion(Potion.Type.STRENGTH_POTION,
              15, 600);
    private final Potion WEAK_AGILITY_POTION    = new Potion(Potion.Type.AGILITY_POTION,
               3, 410);
    private final Potion AGILITY_POTION         = new Potion(Potion.Type.AGILITY_POTION,
               5, 730);
    private final Potion HEALTH_POTION          = new Potion(Potion.Type.HEALTH_POTION,
             250, 170);
    private final Potion ENHANCED_HEALTH_POTION = new Potion(Potion.Type.HEALTH_POTION,
             500, 320);
    private final Potion SUPER_HEALTH_POTION    = new Potion(Potion.Type.HEALTH_POTION,
            1000, 560);


    public Seller() {
        maxPotionCount = Map.of(Potion.Type.STRENGTH_POTION, 2,
                                Potion.Type.AGILITY_POTION,  1,
                                Potion.Type.HEALTH_POTION,   5);

        catalog.put(new Weapon("Hammer-reaper",   450, 9_000, 90), 1);
        catalog.put(new Weapon("Enchanted Sword", 350, 6_000, 75), 1);
        catalog.put(new Weapon("Sword",           250, 4_000, 55), 1);
        catalog.put(new Weapon("Sledgehammer",    200, 2_000, 35), 1);
        catalog.put(new Weapon("Tomahawk",        150, 1_000, 15), 1);

        assortmentUpdater.scheduleAtFixedRate(this::updateAssortment,
                0, ASSORTMENT_UPDATE_TIME, TimeUnit.MINUTES);
    }


    public void leave() {
        assortmentUpdater.shutdown();
    }

    synchronized public void trade(Player player) {
        while (true) {
            int itemsCount = catalogOutput(player.gold);
            System.out.print("= What would you like to buy? Enter a number in catalog(or type exit): ");
            int number;
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String answer;
            try {
                while (true) {
                    answer = input.readLine();
                    try {
                        number = Integer.parseInt(answer);
                    } catch (NumberFormatException e) {
                        if (answer.equalsIgnoreCase("exit")) {
                            tradeExitDisplay();
                            return;
                        }
                        System.out.print("= Incorrect entered number, try again: ");
                        continue;
                    }
                    if (number <= 0 || number > itemsCount) {
                        System.out.print("= Wrong number in the catalog, try again: ");
                        continue;
                    }
                    break;
                }
                sellItem(player, number);
                answer = "";
                while (!answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no")) {
                    System.out.print("=   Do you want to see something else? (yes/no): ");
                    answer = input.readLine();
                }
                if (answer.equalsIgnoreCase("no")) {
                    tradeExitDisplay();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private int catalogOutput(int playerGold) {
        System.out.println("=".repeat(120));
        System.out.printf("%1$s%2$s%3$s%2$s%1$s%n", "=".repeat(12), " ".repeat(31),
                "\"Sunny & Berry Rag\" store catalog ");
        System.out.println("=".repeat(120));
        System.out.printf("=====       %1$s%2$s%3$s       =====%n", "Available goods:", " ".repeat(64),
                String.format("Your gold: %5d", playerGold));

        int count = 0;
        for (var item : catalog.entrySet()) {
            ++count;
            System.out.printf("= (%2d) ", count);
            if (item.getKey() instanceof Weapon) {
                System.out.print("Weapon - ");
            } else if (item.getKey() instanceof Potion) {
                System.out.print("Potion - ");
            }
            System.out.printf("%-103s", String.format("%s (available %d pieces)", item.getKey(), item.getValue()));
            System.out.println("=");
        }
        System.out.println("=".repeat(120));
        return count;
    }

    private void tradeExitDisplay() {
        System.out.println("=".repeat(120));
        System.out.printf("%1$s%2$s%3$s%2$s%1$s%n", "=".repeat(10), " ".repeat(37),
                "Goodbye, come back again! ");
        System.out.printf("%1$s%2$s%3$s%2$s%1$s%n", "=".repeat(16), " ".repeat(23),
                String.format("The assortment is updated every %2d minutes", ASSORTMENT_UPDATE_TIME));
        System.out.println("=".repeat(120));
    }

    private void sellItem(Player player, int catalogNumber) {
        var iterator = catalog.entrySet().iterator();
        int position = 0;
        while (iterator.hasNext()) {
            var item = iterator.next();
            ++position;
            if (position == catalogNumber) {
                var equip = item.getKey();
                boolean isSold = false;
                if (equip instanceof Weapon weapon) {
                    if (player.getLevel() < weapon.requiredLevel) {
                        System.out.printf("=   %1$s%2$s=%n",
                                "Sorry, but you don't have enough experience for this weapon.", " ".repeat(55));
                        return;
                    }
                    if (player.pay(weapon.cost)) {
                        isSold = player.getNewEquipment(equip);
                    } else {
                        System.out.printf("=   %1$s%2$s=%n",
                                "You don't have enough gold to buy this weapon.", " ".repeat(69));
                    }
                } else if (equip instanceof Potion potion) {
                    if (player.pay(potion.cost)) {
                        isSold = player.getNewEquipment(equip);
                    } else {
                        System.out.printf("=   %1$s%2$s=%n",
                                "You don't have enough gold to buy this potion.", " ".repeat(69));
                    }
                }
                if (isSold) {
                    System.out.printf("=   %1$s%2$s=%n",
                            "You have acquired follow equipment:", " ".repeat(80));
                    System.out.printf("=   -> %-112s=%n", item.getKey());
                    if (item.getValue() > 1) {
                        item.setValue(item.getValue() - 1);
                    } else {
                        iterator.remove();
                    }
                }
                return;
            }
        }
    }

    synchronized private void updateAssortment() {
        catalog.put(WEAK_STRENGTH_POTION,   maxPotionCount.get(Potion.Type.STRENGTH_POTION));
        catalog.put(STRENGTH_POTION,        maxPotionCount.get(Potion.Type.STRENGTH_POTION));
        catalog.put(WEAK_AGILITY_POTION,    maxPotionCount.get(Potion.Type.AGILITY_POTION));
        catalog.put(AGILITY_POTION,         maxPotionCount.get(Potion.Type.AGILITY_POTION));
        catalog.put(HEALTH_POTION,          maxPotionCount.get(Potion.Type.HEALTH_POTION));
        catalog.put(ENHANCED_HEALTH_POTION, maxPotionCount.get(Potion.Type.HEALTH_POTION));
        catalog.put(SUPER_HEALTH_POTION,    maxPotionCount.get(Potion.Type.HEALTH_POTION));
    }
}