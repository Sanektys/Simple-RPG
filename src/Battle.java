import Inhabitants.Goblin;
import Inhabitants.Inhabitant;
import Inhabitants.Player;
import Inhabitants.Skeleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Battle {

    private final Random RANDOM = new Random();
    private final ScheduledExecutorService battleThread = Executors.newSingleThreadScheduledExecutor();

    private final int FIGHT_START_DELAY = 10;

    public void battle(Player player) {
        System.out.println("---==You are in a sinister forest inhabited by dangerous goblins and skeletons==---");
        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
            String command = "null";
            while (!command.equalsIgnoreCase("escape") && player.isAlive()) {
                Inhabitant enemy = newEnemy(player.getLevel());
                System.out.printf("Your next enemy: %s%n", enemy);
                System.out.printf("After %d second the fight will start, to escape, type \"escape\"%n",
                        FIGHT_START_DELAY);
                System.out.println("To use inventory during fight, type \"aid\"");
                var fight = battleThread.schedule(() -> fight(player, enemy),
                        10, TimeUnit.SECONDS);
                while (!fight.isDone()) {
                    command = input.readLine();
                    if (command.equalsIgnoreCase("aid")) {
                        fight.cancel(true);
                    }
                    if (command.equalsIgnoreCase("escape") && !fight.isCancelled()) {
                        fight.cancel(false);
                    }
                    if (command.equalsIgnoreCase("escape") && fight.isCancelled() && !fight.isDone()) {
                        System.out.println("Too late for escape...");
                    }
                }
            }
            if (command.equalsIgnoreCase("escape")) {
                System.out.println("You leaving the sinister forest...");
            } else if (!player.isAlive()) {
                System.out.print("You have fallen by the death of brave...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fight(Player player, Inhabitant enemy) {
        while (player.isAlive() && enemy.isAlive()) {
            try {
                enemy.doStrike(player);
                Thread.sleep(500);
                player.doStrike(enemy);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                player.useEquipment();
            }
        }
    }

    private Inhabitant newEnemy(int playerLevel) {
        int enemyLevel = playerLevel - 5 + RANDOM.nextInt(11);  // Уровень противника +-5 от игрока
        return switch (RANDOM.nextInt(2)) {
            case 0 -> new Goblin(enemyLevel);
            case 1 -> new Skeleton(enemyLevel);
            default -> throw new IllegalStateException("The enemy generator threw an error");
        };
    }
}
