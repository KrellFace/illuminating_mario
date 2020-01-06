package illumsearch;

import java.nio.file.Path;

public class IllumConfig {
	
    public final int Algo_MapElites = 1;
    public final int Algo_ShineCD = 2;
    public final int Algo_ShineFit = 3;
    public final int Algo_ShineHybrid = 4;
	
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
    public final float config_map_minJE = 0.00f;
    public final float config_map_maxJE = 0.08f;
    public final float config_map_minSpeed = 0.07f;
    public final float config_map_maxSpeed = 0.18f;
    public final float config_map_minRTime = 0.0f;
    public final float config_map_maxRTime = 30.0f;
    public final float config_map_minContig = 0f;
    public final float config_map_maxContig = 1000f;
    public final float config_map_minClearRows = -0.1f;
    public final float config_map_maxClearRows = 16;
    public final float config_map_minAgrSmooth = 0;
    public final float config_map_maxAgrSmooth = 300;
    
    public final float config_map_minWidth = 50f;
    public final float config_map_maxWidth = 150f;
        
    public final float config_map_fitnessThresshold = 1.00f;

    //Parameters for each level run
    public final int ticksPerRun = 10;
    
    //Mutation chances - Shared between both ME and SHINE
    public final float Dupe_Remove_Chance = 0.02F;
    public final float Tile_Mutation_Chance = 0.005F;
    public final float Crossover_Chance = .2F;
    
    //Parameters for generating random noise levels  
    public final float max_tile_chance = 0.3f;
    public final int fixed_width = 100;
    
    //SHINE Variables
    public final int Generation_Size = 20;
    public final int Max_Tree_Depth = 6;
    public final int Max_Vertex_Reps = 5;
    
    //Size of Map Elites map (Maps always square)
    public final int mapSize = 20;
    
    //Seed used for generating initial population
    public final int initialSeed = 100;
    
    //Parameters for current run
    private int algoType;
    private int numOffspring;
    private int runParam1;
    private int runParam2;
    private float param1Min;
    private float param1Max;
    private float param2Min;
    private float param2Max;
    private Path runPath;
    private String runName;
    
    public IllumConfig(int algoType, int numOffspring, int runParam1, int runParam2, Path runPath, String runName) {
    	this.algoType = algoType;
    	this.numOffspring = numOffspring;
    	this.runParam1 = runParam1;
    	this.runParam2 = runParam2;
    	this.runPath = runPath;
    	this.runName = runName;
    	this.setParamMinMax();
    }
    
    public float getParam1(LevelWrap inputLevel) {

        switch(runParam1) {
    	case config_paramBC:
    		return inputLevel.getBlockCount();
    	case config_paramWidth:
    		return inputLevel.getWidth();
    	case config_paramSpeed:
    		return inputLevel.getSpeed();
    	case config_paramContig:
    		return inputLevel.getContigScore();
    	case config_paramJE:
    		return (Float) inputLevel.getJumpEntropy();
    	case config_paramClearRows:
    		return inputLevel.getClearRows();
    	case config_paramAgrSmooth:
    		return inputLevel.getAggrSmooth();
    	default:
    		return (Float) null;
        }

    }
    
    public float getParam2(LevelWrap inputLevel) {

        switch(runParam2) {
        	case config_paramBC:
        		return inputLevel.getBlockCount();
        	case config_paramWidth:
        		return inputLevel.getWidth();
        	case config_paramSpeed:
        		return inputLevel.getSpeed();
        	case config_paramContig:
        		return inputLevel.getContigScore();
        	case config_paramJE:
        		return (Float) inputLevel.getJumpEntropy();
        	case config_paramClearRows:
        		return inputLevel.getClearRows();
        	case config_paramAgrSmooth:
        		return inputLevel.getAggrSmooth();
        	default:
        		return (Float) null;
        }
    }
    
    private void setParamMinMax() {
    	
        switch(this.runParam1) {
    	case config_paramBC:
    		this.param1Min = this.config_map_minBC;
    		this.param1Max = this.config_map_maxBC;
    		break;
    	case config_paramWidth:
    		this.param1Min = this.config_map_minWidth;
    		this.param1Max = this.config_map_maxWidth;
    		break;
    	case config_paramSpeed:
    		this.param1Min = this.config_map_minSpeed;
    		this.param1Max = this.config_map_maxSpeed;
    		break;
    	case config_paramContig:
    		this.param1Min = this.config_map_minContig;
    		this.param1Max = this.config_map_maxContig;
    		break;
    	case config_paramJE:
    		this.param1Min = this.config_map_minJE;
    		this.param1Max = this.config_map_maxJE;
    		break;
    	case config_paramClearRows:
    		this.param1Min = this.config_map_minClearRows;
    		this.param1Max = this.config_map_maxClearRows;
    		break;
    	case config_paramAgrSmooth:
    		this.param1Min = this.config_map_minAgrSmooth;
    		this.param1Max = this.config_map_maxAgrSmooth;
    		break;
    	default:
        }
        switch(this.runParam2) {
    	case config_paramBC:
    		this.param2Min = this.config_map_minBC;
    		this.param2Max = this.config_map_maxBC;
    		break;
    	case config_paramWidth:
    		this.param2Min = this.config_map_minWidth;
    		this.param2Max = this.config_map_maxWidth;
    		break;
    	case config_paramSpeed:
    		this.param2Min = this.config_map_minSpeed;
    		this.param2Max = this.config_map_maxSpeed;
    		break;
    	case config_paramContig:
    		this.param2Min = this.config_map_minContig;
    		this.param2Max = this.config_map_maxContig;
    		break;
    	case config_paramJE:
    		this.param2Min = this.config_map_minJE;
    		this.param2Max = this.config_map_maxJE;
    		break;
    	case config_paramClearRows:
    		this.param2Min = this.config_map_minClearRows;
    		this.param2Max = this.config_map_maxClearRows;
    		break;
    	case config_paramAgrSmooth:
    		this.param2Min = this.config_map_minAgrSmooth;
    		this.param2Max = this.config_map_maxAgrSmooth;
    		break;
    	default:
        }
        
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
    public float getParam1Min() {
    	return this.param1Min;
    }
    public float getParam1Max() {
    	return this.param1Max;
    }
    public float getParam2Min() {
    	return this.param2Min;
    }
    public float getParam2Max() {
    	return this.param2Max;
    }
    public Path getRunPath() {
    	return this.runPath;
    }
    public String getRunName() {
    	return this.runName;
    }
    
    public String toString() {
    	return ("IllumConfig information: AlgoType:  " + this.algoType + "; Param1: " + getParam1() + "; Param2 " + getParam2() + 
    			" Param1 Min/Max: " + this.getParam1Min() + "/" + this.getParam1Max() + " Param2 Min/Max: " + this.getParam2Min() + "/" + this.getParam2Max());
    }

}
