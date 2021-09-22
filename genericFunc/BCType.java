package illuminating_mario.genericFunc;

public enum BCType {


    //This Enum stores all currently available Behavioral Characteristics (BCs) that we can use to describe and sort levels
    //Each BC is stored with its hard coded min and max values
    //In future It would be good for this to be configurable on start - good for if we only want to design levels with a narrow parameter range
    JE("Jump Entropy", 0.00f, 0.08f),
    Width("Level Width", 50f, 150f),
    Speed("Agent Speed", 0.1f, 0.2f),
    Contig("Contiguity", 0f, 1000f),
    BlockCount("Block Count", 200f, 550f),
    ClearRows("Clear Rows", -0.1f, 16),
    AgrSmooth("Aggregate Smoothness", 0f, 300f),
    ContigOverBlockCount("Contig over BC", 0.0f, 2f),
    TotalJumps("Total Jumps", 0f, 50f);

    private String bcName;
    private float min;
    private float max;

    BCType(String newName, float newMin, float newMax) {
        bcName = newName;
        min = newMin;
        max = newMax;
    }

    public String getBCName(){
        return bcName;
    }
    
    public float getMinValue() {
        return min;
    }

    public float getMaxValue(){
        return max;
    }

}
