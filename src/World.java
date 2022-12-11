import Inhabitants.Player;
import Inhabitants.Seller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class World {

    private Player player;
    private Seller seller;
    private Battle battle;

    private boolean playerIsHere;


    public boolean playerIsHere() { return playerIsHere; }

    public void start() {
        System.out.println("------=====       Welcome to RPG game \"No Name\"!       =====------");
        System.out.println("---==Before you enter the Town, introduce yourself by your name==---");
        System.out.print("     My name -> ");
        String playerName;
        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
            playerName = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        player = new Player(playerName);
        System.out.printf("Hello %s, good luck in the Town!%n%n", playerName);

        seller = new Seller();
        battle = new Battle();
    }

    public void townMenu() {
        displayTownMenu();
        int choice;
        while (true) {
            try (Scanner input = new Scanner(System.in)) {
                choice = input.nextInt();
            } catch (RuntimeException e) {
                System.out.print("Your input is incorrect, please try again: ");
                continue;
            }
            if (choice < 1 || choice > 3) {
                System.out.print("Your choice number is wrong, try again: ");
                continue;
            }
            break;
        }
        switch (choice) {
            case 1 -> seller.trade(player);
            case 2 -> battle.battle(player);
            case 3 -> playerIsHere = false;
            default -> throw new IllegalStateException("Incorrect choice in town.");
        }
        if (!player.isAlive()) {
            playerIsHere = false;
            System.out.println("Game is over.");
        }
    }

    private void displayTownMenu() {
        player.displayStats();
        System.out.println("=".repeat(70));
        System.out.printf("=%1$s%2$s%1$s=%n", " ".repeat(28), "--= Town =--");
        System.out.printf("=%1$s%2$s%1$s=%n", "    ", "What do you want to do? (type in the number of your choice):");
        System.out.println("=".repeat(70));
        System.out.printf("=  1. Go to the seller%s=%n", " ".repeat(47));
        System.out.printf("=  2. To go into the sinister forest to beat the foes%s=%n", " ".repeat(16));
        System.out.printf("=  3. Leave town (and game)%s=%n", " ".repeat(42));
        System.out.println("=".repeat(70));
        System.out.print("Your choice -> ");
    }
}
