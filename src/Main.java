public class Main {
    public static void main(String[] args) {
        World world = new World();
        world.start();
        while (world.playerIsHere()) {
            world.townMenu();
        }
    }
}
