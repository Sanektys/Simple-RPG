package Items;

public class Weapon implements Equipment {

    public final String name;
    public final int power;
    public final int cost;
    public final int requiredLevel;

    public Weapon(String name, int power, int cost, int requiredLevel) {
        this.name = name;
        this.power = power;
        this.cost = cost;
        this.requiredLevel = requiredLevel;
    }
}
