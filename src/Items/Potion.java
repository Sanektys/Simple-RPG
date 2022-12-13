package Items;

import java.util.Objects;

public class Potion implements Equipment {

    public final Type type;
    public final int statePointsImprovement;
    public final int cost;

    public Potion(Type type, int statePointsImprovement, int cost) {
        this.type = type;
        this.statePointsImprovement = statePointsImprovement;
        this.cost = cost;
    }

    public enum Type {
        AGILITY_POTION,
        HEALTH_POTION,
        STRENGTH_POTION,
    }

    @Override
    public String toString() {
        return switch (type) {
            case STRENGTH_POTION -> String.format("%16s:  %-27s Costs %3d gold.",
                    "Strength potion", String.format("increases strength by %2d.", statePointsImprovement), cost);
            case AGILITY_POTION -> String.format("%16s:  %-27s Costs %3d gold.",
                    "Agility potion", String.format("increases agility by %2d.", statePointsImprovement), cost);
            case HEALTH_POTION -> String.format("%16s:  %-27s Costs %3d gold.",
                    "Health potion", String.format("restores %3d health points.", statePointsImprovement), cost);
        };
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || object.getClass() != this.getClass()) {
            return false;
        }
        Potion another = (Potion) object;
        return Objects.equals(type, another.type)
                && Objects.equals(statePointsImprovement, another.statePointsImprovement)
                && Objects.equals(cost, another.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, statePointsImprovement);
    }
}