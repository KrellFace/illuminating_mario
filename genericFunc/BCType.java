package illumsearch.genericFunc;

public enum BCType {
    
    JE(1),
    Width(2),
    Speed(3),
    Contig(4),
    BlockCount(5),
    ClearRows(6),
    AgrSmooth(7),
    ContigOverBlockCount(8),
    TotalJumps(9);

    private int value;

    BCType(int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }

}
