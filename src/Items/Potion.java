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
            case STRENGTH_POTION -> String.format("Strength potion: increases strength by %d. Costs %d gold.",
                    statePointsImprovement, cost);
            case AGILITY_POTION -> String.format("Agility potion: increases agility by %d. Costs %d gold.",
                    statePointsImprovement, cost);
            case HEALTH_POTION -> String.format("Health potion: restores %d health points. Costs %d gold.",
                    statePointsImprovement, cost);
            default -> "undefined potion";
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