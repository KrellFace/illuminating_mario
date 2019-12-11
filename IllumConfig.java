package illumsearch;

import java.nio.file.Path;

public class IllumConfig {
	
    public final int Algo_MapElites = 1;
    public final int Algo_Shine = 2;
	
    //Config options for each run
    public final int config_paramJE	= 0;
    public final int config_paramWidth	= 1;
    public final int config_paramSpeed	= 2;
    public final int config_paramContig	= 3;
    public final int config_paramBC	= 4;
    public final int config_paramClearRows = 5;
    public final int config_paramAgrSmooth = 6;
    
    //Fixed min and max level parameters for map generation (hard coded based around a level width of 100)
    public final float config_map_minBC = 200f;
    public final float config_map_maxBC = 550f; 
    public final float config_map_minLW = 50;
    public final float config_map_maxLW = 150;
    public final float config_map_minJE = 0.04f;
    public final float config_map_maxJE = 0.08f;
    public final float config_map_minSpeed = 0.07f;
    public final float config_map_maxSpeed = 0.18f;
    public final float config_map_minRTime = 0.0f;
    public final float config_map_maxRTime = 30.0f;
    public final float config_map_minContig = 0f;
    public final float config_map_maxContig = 2000f;
    public final float config_map_minClearRows = -0.1f;
    public final float config_map_maxClearRows = 16;
    public final float config_map_minAgrSmooth = 0;
    public final float config_map_maxAgrSmooth = 300;
        
    public final float config_map_fitnessThresshold = 1.00f;

    //Parameters for each level run
    public final int ticksPerRun = 10;
    
    //Mutation chances - Shared between both ME and SHINE
    public final float Dupe_Remove_Chance = 0.5F;
    public final float Tile_Mutation_Chance = 0.005F;
    public final float Crossover_Chance = .2F;
    
    //Parameters for generating random noise levels
    public final float max_tile_chance = 0.3f;
    public final int fixed_width = 100;
    
    //SHINE Variables
    public final int Generation_Size = 20;
    public final int Max_Tree_Depth = 6;
    public final int Max_Vertex_Reps = 10;
    
    //Size of Map Elites map (Maps always square)
    public final int mapSize = 20;
    
    //Seed used for generating initial population
    public final int initialSeed = 100;
    
    //Parameters for current run
    private int algoType;
    private int numOffspring;
    private int runParam1;
    private int runParam2;
    private Path runPath;
    private String runName;
    
    public IllumConfig(int algoType, int numOffspring, int runParam1, int runParam2, Path runPath, String runName) {
    	this.algoType = algoType;
    	this.numOffspring = numOffspring;
    	this.runParam1 = runParam1;
    	this.runParam2 = runParam2;
    	this.runPath = runPath;
    	this.runName = runName;
    }
    
    public int getAlgoType() {
    	return this.algoType;
    }
    public int getNumOffspring() {
    	return this.numOffspring;
    }
    public int getParam1() {
    	return this.runParam1;
    }
    public int getParam2() {
    	return this.runParam2;
    }
    public Path getRunPath() {
    	return this.runPath;
    }
    public String getRunName() {
    	return this.runName;
    }
    
    

}
