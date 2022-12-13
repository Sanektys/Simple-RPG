package Items;

import java.util.Objects;

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

    @Override
    public String toString() {
        return String.format("%16s:  deals %3d damage. Costs %,4d gold. Required level - %2d.",
                name, power, cost, requiredLevel);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || object.getClass() != this.getClass()) {
            return false;
        }
        Weapon another = (Weapon) object;
        return Objects.equals(name, another.name)
                && Objects.equals(power, another.power)
                && Objects.equals(requiredLevel, another.requiredLevel)
                && Objects.equals(cost, another.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, power, requiredLevel);
    }
}
