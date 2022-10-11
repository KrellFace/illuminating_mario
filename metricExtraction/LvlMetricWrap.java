package illuminating_mario.metricExtraction;

import java.util.Dictionary;
import java.util.Hashtable;

import agents.robinBaumgarten.Agent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import java.util.*;

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

    private int platformSize = 5;

    
    //Store floor value

    private static List<Character> solidChars = Arrays.asList('X','#','%','D','S');
    private static List<Character> enemyChars = Arrays.asList('y','Y','E','g','G','k','K','r');
    private static List<Character> standableChars = Arrays.asList('-', 'M', 'F', '|','*','B','b');

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
        float bc = 0;
        float contig = 0;
        float linearity = 0;
        float density = 0;
        float enemyCount = 0;
        float clearRows = 0;
        float emptySpace = 0;
        //String floor = "X";
        
        //Looping through every block, row by row
        for (int y = 0; y < charRep.length; y++) {
            Boolean rowClear = true;
            for (int x = 0; x < charRep[y].length; x++) {

                //Solid Tile Detected
                if(solidChars.contains(charRep[y][x])){
                    bc += 1;
                    //Set the flag for this row being clear to false
                    rowClear = false;

                    //Check adjacent tiles

                    //UP
                    if (y > 0) {
                        if(solidChars.contains(charRep[y - 1][x])){
                            contig +=1;
                        }
                        else if(standableChars.contains(charRep[y - 1][x])){
                            density+=1;
                        }
                    }
                    //DOWN
                    if (y < charRep.length - 1 ) {
                        if(solidChars.contains(charRep[y + 1][x])){
                            contig +=1;
                        }

                    }
                    //LEFT
                    if (x > 0) {
                        if(solidChars.contains(charRep[y][x-1])){
                            contig +=1;
                            linearity+=1;
                        }
                    }
                    //RIGHT
                    if (x < charRep[y].length - 1) {
                        if(solidChars.contains(charRep[y][x + 1])){
                            contig +=1;
                            linearity+=1;
                        }
                    }
                }
                //Enemy Detected
                if(enemyChars.contains(charRep[y][x])){
                    enemyCount+=1;
                }                
                //Empty Space Detected
                if(standableChars.contains(charRep[y][x])){
                    emptySpace+=1;
                }
            }
            if (rowClear) {
                clearRows += 1;
            }

        }
        
        metricVals.put(enum_MarioMetrics.BlockCount,bc);
        metricVals.put(enum_MarioMetrics.ClearRows,clearRows);
        metricVals.put(enum_MarioMetrics.Contiguity,contig);
        metricVals.put(enum_MarioMetrics.AdjustedContiguity,(contig/(float)(charRep.length*charRep[0].length)));
        metricVals.put(enum_MarioMetrics.Linearity,linearity);
        metricVals.put(enum_MarioMetrics.Density,(density/(float)charRep[0].length));
        metricVals.put(enum_MarioMetrics.EnemyCount, enemyCount);
        metricVals.put(enum_MarioMetrics.EmptySpace, emptySpace);

    }

    private void updateResultFeatures(MarioResult result) {

        metricVals.put(enum_MarioMetrics.Playability,(float)result.getCompletionPercentage());
        metricVals.put(enum_MarioMetrics.JumpCount,(float)result.getNumJumps());
        metricVals.put(enum_MarioMetrics.JumpEntropy,(float) result.getNumJumps() / (float) result.getAgentEvents().size());metricVals.put(enum_MarioMetrics.TimeTaken,(float)(config.ticksPerRun - (result.getRemainingTime() / 1000)));
        metricVals.put(enum_MarioMetrics.Speed,(((float)result.getCompletionPercentage()*1000) / (float)((config.ticksPerRun*1000) - result.getRemainingTime())));
    }
    //Run the a* agent to update our attributes
    public void runAgent() {
        Agent agent = new Agent();
        runAgent(agent);
    }

    //Run the a* agent to update our attributes
    public void runAgent(Agent agent) {

        MarioResult result = new MarioGame().runGame(agent, level.getStringRep(), config.ticksPerRun);
    	
    	//RUN VISIBLE
    	//result = new MarioGame().runGame(agent, level.getStringRep(), config.ticksPerRun, 0, true);

        updateResultFeatures(result);
    }

    public void runAgentNTimes(int n){
        float totPlayability = 0f;
        float totJumpCount = 0f;
        float totJumpEntropy = 0f;
        float totSpeed = 0f;
        float totTimeTaken = 0f;

        for (int i = 0; i <n; i++){
            Agent agent = new Agent();
            MarioResult result = new MarioGame().runGame(agent, level.getStringRep(), config.ticksPerRun);

            totPlayability+=(float)result.getCompletionPercentage();
            //System.out.println("Run Playability: " + (float)result.getCompletionPercentage());
            totJumpCount+=(float)result.getNumJumps();
            totJumpEntropy+=(float) (result.getNumJumps() / (float) result.getAgentEvents().size());
            totSpeed+=(float)((result.getCompletionPercentage()*1000) / ((config.ticksPerRun*1000) - result.getRemainingTime()));
            totTimeTaken+= (float) (config.ticksPerRun - (result.getRemainingTime() / 1000));
        }
        metricVals.put(enum_MarioMetrics.Playability,(float)totPlayability/n);
        //System.out.println("Avg playability from n runs: " + (float)totPlayability/n);
        metricVals.put(enum_MarioMetrics.JumpCount,(float)totJumpCount/n);
        metricVals.put(enum_MarioMetrics.JumpEntropy,(float) totJumpEntropy/n);
        metricVals.put(enum_MarioMetrics.Speed,(float)totSpeed/n);
        metricVals.put(enum_MarioMetrics.TimeTaken,(float)totTimeTaken/n);
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