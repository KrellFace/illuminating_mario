package illuminating_mario.genericFunc;

import java.nio.file.Path;

public class IllumConfig {
        
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
    public final int Max_Tree_Depth = 5;
    public final int Max_Vertex_Reps = 3;
    
    //Size of Map Elites map (Maps always square)
    public final int mapSize = 32;
    
    //Seed used for generating initial population
    public final int initialSeed = 100;
    
    //Parameters for current run
    private AlgoType algoType;
    private int numOffspring;
    private BCType runParam1;
    private BCType runParam2;


    private Path runPath;
    private String runName;
    
    public IllumConfig(AlgoType algoType, int numOffspring, BCType runParam1, BCType runParam2, Path runPath, String runName) {
    	this.algoType = algoType;
    	this.numOffspring = numOffspring;
    	this.runParam1 = runParam1;
    	this.runParam2 = runParam2;
    	this.runPath = runPath;
    	this.runName = runName;
    }
    
    public float getParam1(LevelWrap inputLevel) {

        switch(runParam1) {
    	case BlockCount:
    		return inputLevel.getBlockCount();
    	case Width:
    		return inputLevel.getWidth();
    	case Speed:
    		return inputLevel.getSpeed();
    	case Contig:
    		return inputLevel.getContigScore();
    	case JE:
    		return (Float) inputLevel.getJumpEntropy();
    	case ClearRows:
    		return inputLevel.getClearRows();
    	case AgrSmooth:
    		return inputLevel.getAggrSmooth();
    	case ContigOverBlockCount:
    		return inputLevel.getContigScore()/inputLevel.getBlockCount();
		case TotalJumps:
			return inputLevel.getTotalJumps();
    	default:
    		return -1f;
        }

    }
    
    public float getParam2(LevelWrap inputLevel) {

        switch(runParam2) {
		case BlockCount:
			return inputLevel.getBlockCount();
		case Width:
			return inputLevel.getWidth();
		case Speed:
			return inputLevel.getSpeed();
		case Contig:
			return inputLevel.getContigScore();
		case JE:
			return (Float) inputLevel.getJumpEntropy();
		case ClearRows:
			return inputLevel.getClearRows();
		case AgrSmooth:
			return inputLevel.getAggrSmooth();
		case ContigOverBlockCount:
			return inputLevel.getContigScore()/inputLevel.getBlockCount();
		case TotalJumps:
			return inputLevel.getTotalJumps();
		default:
			return -1f;
        }
    }
    
    
    public AlgoType getAlgoType() {
    	return this.algoType;
    }
    public int getNumOffspring() {
    	return this.numOffspring;
    }
	
    public String getParam1() {
    	return this.runParam1.name();
    }
    public String getParam2() {
    	return this.runParam2.name();
    }
	
    public float getParam1Min() {
    	return this.runParam1.getMinValue();
    }
    public float getParam1Max() {
    	return this.runParam1.getMaxValue();
    }
    public float getParam2Min() {
    	return this.runParam2.getMinValue();
    }
    public float getParam2Max() {
    	return this.runParam2.getMaxValue();
    }
    public Path getRunPath() {
    	return this.runPath;
    }
    public String getRunName() {
    	return this.runName;
    }
    
    public String toString() {
    	return ("IllumConfig information: AlgoType:  " + this.algoType + "; Param1: " + runParam1.name() + "; Param2 " + runParam2.name() + 
    			" Param1 Min/Max: " + this.getParam1Min() + "/" + this.getParam1Max() + " Param2 Min/Max: " + this.getParam2Min() + "/" + this.getParam2Max());
    }

}
