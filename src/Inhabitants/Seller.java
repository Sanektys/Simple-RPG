package Inhabitants;

import Items.Equipment;
import Items.Potion;
import Items.Weapon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Seller {

    private final Map<Equipment, Integer> catalog = new LinkedHashMap<>();
    private final Map<Potion.Type, Integer> maxPotionCount;

    private final ScheduledExecutorService assortmentUpdater = Executors.newSingleThreadScheduledExecutor();

    private final Potion STRENGTH_POTION = new Potion(Potion.Type.STRENGTH_POTION, 20, 120);
    private final Potion AGILITY_POTION = new Potion(Potion.Type.AGILITY_POTION, 5, 180);
    private final Potion HEALTH_POTION = new Potion(Potion.Type.HEALTH_POTION, 500, 90);


    public Seller() {
        maxPotionCount = Map.of(Potion.Type.STRENGTH_POTION, 2,
                                Potion.Type.AGILITY_POTION, 3,
                                Potion.Type.HEALTH_POTION, 6);

        catalog.put(new Weapon("Sword", 250, 10_000, 16), 1);
        catalog.put(new Weapon("Sledgehammer", 200, 6_000, 8), 1);
        catalog.put(new Weapon("Tomahawk", 150, 2_000, 4), 1);

        updateAssortment();

        assortmentUpdater.scheduleAtFixedRate(this::updateAssortment, 10, 10, TimeUnit.MINUTES);
    }


    synchronized public void trade(Player player) {
        while (true) {
            int itemsCount = catalogOutput();
            System.out.print("= What would you like to buy? Enter a number in catalog(or type exit): ");
            int number;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
                String answer = input.readLine();
                while (true) {
                    try {
                        number = Integer.parseInt(answer);
                    } catch (NumberFormatException e) {
                        if (answer.equalsIgnoreCase("exit")) {
                            tradeExitDisplay();
                            return;
                        }
                        System.out.print("=   Incorrect entered number, try again: ");
                        continue;
                    }
                    if (number <= 0 || number > itemsCount) {
                        System.out.print("=   Wrong number in the catalog, try again: ");
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
                    break;
                }
            } catch (IOException e) {
                System.out.println("=  Not quite sure what you need, take another look at the catalog... =");
            }
        }
    }

    private int catalogOutput() {
        System.out.println("""
        ======================================================================
        ======            "Sunny & Berry Rag" store catalog             ======
        ======================================================================
        =   Available goods:                                                 =
        """);
        int count = 0;
        for (var item : catalog.entrySet()) {
            ++count;
            System.out.printf("= %d -> ", count);
            if (item.getKey() instanceof Weapon) {
                System.out.print("Weapon - ");
            } else if (item.getKey() instanceof Potion) {
                System.out.print("Potion - ");
            }
            System.out.print(item.getKey());
            System.out.printf("(available %d pieces)\t=", item.getValue());
        }
        System.out.println("""
        ======================================================================
        """);
        return count;
    }

    private void tradeExitDisplay() {
        System.out.println("""
        ======================================================================
        ======                Goodbye, come back again!                 ======
        ========      The assortment is updated every 10 minutes      ========
        ======================================================================
        """);
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
                if (equip instanceof Weapon) {
                    if (player.getLevel() < ((Weapon) equip).requiredLevel) {
                        System.out.println("=   Sorry, but you don't have enough experience for this weapon.     =");
                        return;
                    }
                    if (player.pay(((Weapon) equip).cost)) {
                        isSold = player.getNewEquipment(equip);
                    } else {
                        System.out.println("=   You don't have enough gold to buy this weapon.                   =");
                    }
                } else if (equip instanceof Potion) {
                    if (player.pay(((Potion) equip).cost)) {
                        isSold = player.getNewEquipment(equip);
                    } else {
                        System.out.println("=   You don't have enough gold to buy this potion.                   =");
                    }
                }
                if (isSold) {
                    System.out.println("=   You have acquired follow equipment:                              =");
                    System.out.printf("=   -> %-62s=", item.getKey());
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
        catalog.put(STRENGTH_POTION, maxPotionCount.get(Potion.Type.STRENGTH_POTION));
        catalog.put(AGILITY_POTION, maxPotionCount.get(Potion.Type.AGILITY_POTION));
        catalog.put(HEALTH_POTION, maxPotionCount.get(Potion.Type.HEALTH_POTION));
    }
}