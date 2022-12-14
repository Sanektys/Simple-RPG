import Inhabitants.Goblin;
import Inhabitants.Inhabitant;
import Inhabitants.Player;
import Inhabitants.Skeleton;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Battle {

    private final Random RANDOM = new Random();
    private final int FIGHT_START_DELAY = 10_000;
    private final int BATTLE_REPORT_DELAY = 3_000;
    private final int STRIKES_DELAY = 200;

    private volatile boolean fightActive;


    public void battle(Player player) {
        System.out.println("\n---==You are in a sinister forest inhabited by dangerous goblins and skeletons==---");
        InputStreamReader input = new InputStreamReader(System.in);
        try {
            char command = '\0';
            while (command != '0' && player.isAlive()) {
                Inhabitant enemy = newEnemy(player.getLevel());

                while (true) {  // Вход в инвентарь до начала боя, откладывает его начало
                    System.out.printf("%nYour next enemy: %s%n%n", enemy);
                    System.err.printf("After %d second the fight will start, to escape, type \"0\"%n",
                            FIGHT_START_DELAY / 1000);
                    System.out.println("To use inventory during fight, type \"1\"");

                    Thread battleThread = new Thread(() -> fight(player, enemy));
                    battleThread.start();

                    while (fightActive || battleThread.isAlive()) {
                        if (input.ready()) {
                            command = (char) input.read();
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                        if (command == '\n' || command == '\r') {
                            continue;
                        }
                        if (command != '0' && command != '1') {
                            System.out.println("Enter \"0\" for escape or \"1\" to look inventory");
                        } else if (command == '1') {
                            battleThread.interrupt();
                            if (!fightActive) {
                                player.useEquipment();  // Битва отсрочена, переход обратно к представлению противника
                            }
                        } else if (!fightActive) {
                            battleThread.interrupt();
                            break;
                        } else {
                            System.out.println("Too late for escape...");
                        }
                    }
                    if (command == '0') {
                        System.out.println("You leaving the sinister forest...");
                        break;
                    } else if (!player.isAlive()) {
                        System.out.print("You have fallen by the death of brave... ");
                        break;
                    } else if (!enemy.isAlive()) {
                        System.out.println("The next enemy is approaching!");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void fight(Player player, Inhabitant enemy) {
        try {
            Thread.sleep(FIGHT_START_DELAY);
        } catch (InterruptedException e) {
            return;
        }

        int playerMisses = 0, playerHits = 0, enemyMisses = 0, enemyHits = 0;
        long startReportTime = 0;
        fightActive = true;
        System.out.println("\tThe fight started!\n");
        while (player.isAlive() && enemy.isAlive()) {
            try {
                if (System.currentTimeMillis() - startReportTime >= BATTLE_REPORT_DELAY) {
                    startReportTime = 0;
                    System.err.printf("%s[misses %d, hits %d]  vs  %s[misses %d, hits %d]%n",  // err для красоты
                            player.getShortStats(), playerMisses, playerHits,
                            enemy.getShortStats(), enemyMisses, enemyHits);
                }
                if (startReportTime == 0) {
                    startReportTime = System.currentTimeMillis();
                }
                if (Thread.interrupted()) {
                    player.useEquipment();
                }

                int temp = enemy.doStrike(player);
                if (temp > 0) {
                    enemyHits++;
                } else if (temp == 0) {
                    enemyMisses++;
                }
                Thread.sleep(STRIKES_DELAY);
                temp = player.doStrike(enemy);
                if (temp > 0) {
                    playerHits++;
                } else if (temp == 0) {
                    playerMisses++;
                }
                Thread.sleep(STRIKES_DELAY);
            } catch (InterruptedException e) {
                player.useEquipment();
            }
        }
        fightActive = false;
    }

    private Inhabitant newEnemy(int playerLevel) {
        int enemyLevel = playerLevel - 2 + RANDOM.nextInt(5);  // Уровень противника +-2 от игрока
        if (enemyLevel > 100) {
            enemyLevel = 100;
        } else if (enemyLevel <= 0) {
            enemyLevel = 1;
        }
        return switch (RANDOM.nextInt(2)) {
            case 0 -> new Goblin(enemyLevel);
            case 1 -> new Skeleton(enemyLevel);
            default -> throw new IllegalStateException("The enemy generator threw an error");
        };
    }
}
