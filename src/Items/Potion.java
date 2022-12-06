package Items;

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
}
