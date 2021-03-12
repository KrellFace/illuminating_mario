package illumsearch.genericFunc;

public enum AlgoType {
    MapElites(1),
    ShineCD(2),
    ShineFit(3),
    ShineHybrid(4);

    private int value;

    AlgoType(int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
