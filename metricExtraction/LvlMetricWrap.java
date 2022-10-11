package illuminating_mario.metricExtraction;

import java.util.Dictionary;
import java.util.Hashtable;

import agents.robinBaumgarten.Agent;
import engine.core.MarioGame;
import engine.core.MarioResult;

import illuminating_mario.mainFunc.*;

public class LvlMetricWrap {

	//Storage for config object for current run
    private IllumConfig config;

    //Storage for all level parameters for the level stored in the Level Wrap
    private String name;
    private IllumMarioLevel level;
    private Hashtable<enum_MarioMetrics, Float> metricVals = new Hashtable<enum_MarioMetrics, Float>();
    private int widthCells;
    private int heightCells;
    private MarioResult result;

    private int platformSize = 5;

    //Constructor for creating with a specified unevaluated level
    public LvlMetricWrap(String name, IllumConfig config,  IllumMarioLevel level) {
        this.config = config;
        this.name = name;
        this.level = level;
        //this.fitness = 0f;
        this.widthCells = level.tileWidth;
        this.heightCells = level.tileHeight;
        

        this.updateLevelFeatures();
    }
    public void updateLevelFeatures() {
        //this.blockCount = level.getBlockCount();
        char[][] charRep = HelperMethods.charRepFromString(level.getStringRep());
        
        //Instantiate local feature scores
        int bc = 0;
        int contig = 0;
        int clearRows = 0;
        //Store floor value
        String floor = "X";
        
        //Looping through every block, row by row
        for (int y = 0; y < charRep.length; y++) {
            Boolean rowClear = true;
            for (int x = platformSize; x < charRep[y].length; x++) {

                //Increment block count
                if (charRep[y][x] == floor.charAt(0)) {
                    bc += 1;
                    //Set the flag for this row being clear to false
                    rowClear = false;

                    //Increment contiguity score
                    char[] adjacentVals = new char[4];
                    if (y > 0 && x > 0) {
                        adjacentVals[0] = charRep[y - 1][x - 1];
                    }
                    if (y > 0 && x < charRep[y].length - 1) {
                        adjacentVals[1] = charRep[y - 1][x + 1];

                    }
                    if (y < charRep.length - 1 && x > 0) {
                        adjacentVals[2] = charRep[y + 1][x - 1];

                    }
                    if (y < charRep.length - 1 && x < charRep[y].length - 1) {
                        adjacentVals[3] = charRep[y + 1][x + 1];

                    }
                    int localContig = 0;
                    //Increase contig score for every adjacent block
                    for (int i = 0; i < adjacentVals.length; i++) {
                        if (adjacentVals[i] == floor.charAt(0)) {
                            localContig += 1;

                        }
                    }
                    contig += localContig;
                }

            }
            if (rowClear) {
                clearRows += 1;
            }

        }
        
        metricVals.put(enum_MarioMetrics.BlockCount,(float)bc);
        metricVals.put(enum_MarioMetrics.ClearRows,(float)clearRows);
        metricVals.put(enum_MarioMetrics.Contiguity,(float)contig);
    }

    private void updateResultFeatures(MarioResult result) {

        metricVals.put(enum_MarioMetrics.Playability,(float)result.getCompletionPercentage());
        metricVals.put(enum_MarioMetrics.JumpCount,(float)result.getNumJumps());
        metricVals.put(enum_MarioMetrics.JumpEntropy,(float) result.getNumJumps() / (float) result.getAgentEvents().size());metricVals.put(enum_MarioMetrics.TimeTaken,(float)(config.ticksPerRun - (result.getRemainingTime() / 1000)));
        metricVals.put(enum_MarioMetrics.Speed,(float)((result.getCompletionPercentage()*1000) / ((config.ticksPerRun*1000) - result.getRemainingTime())));
    }
    //Run the a* agent to update our attributes
    public void runAgent() {
        Agent agent = new Agent();
        runAgent(agent);
    }

    //Run the a* agent to update our attributes
    public void runAgent(Agent agent) {

        result = new MarioGame().runGame(agent, level.getStringRep(), config.ticksPerRun);
    	
    	//RUN VISIBLE
    	//result = new MarioGame().runGame(agent, level.getStringRep(), config.ticksPerRun, 0, true);

        updateResultFeatures(result);
    }

    public IllumMarioLevel getLevel() {
        return level;
    }

    public float GetMetricValue(enum_MarioMetrics metric){
        return metricVals.get(metric);
    }

    public int getWidth() {
        return widthCells;
    }

    public int getHeight() {
        return heightCells;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
    	this.name = newName;
    }

    public char[][] getCharRep() {
        return HelperMethods.charRepFromString(this.level.getStringRep());
    }

    public void printSummary(){
        System.out.println("Level name: " + this.name);

        for (enum_MarioMetrics metric : enum_MarioMetrics.values()) { 
            System.out.println(metric + " value: " + metricVals.get(metric)); 
        }

    }

}