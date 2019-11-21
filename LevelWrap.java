package illumsearch;

import java.util.Random;

import agents.robinBaumgarten.Agent;
import engine.core.MarioGame;
import engine.core.MarioResult;

public class LevelWrap implements Comparable<LevelWrap>{

    protected static final int Config_JEvsBC	= 1;
    protected static final int Config_JEvsWidth	= 2;
    protected static final int Config_JEvsSpeed	= 3;
    protected static final int Config_JEvsContig	= 4;
    protected static final int Config_SpeedvsContig = 5;
    
    //Parameters for generating random noise levels
    public final static float max_tile_chance = 0.3f;
    public final static int fixed_width = 100;
    
    private int ticksPerRun = 100;
    
    private int configType;
    
	private String name;
	private IllumMarioLevel level;
	private float fitness;
	private Integer blockCount;
	private float contigScore;
	private float jumpEntropy;
	private float selectionChance;
	private int widthCells;
	private int heightCells;
	private float timeTaken;
	private float marioSpeed;
	
	private MarioResult result;
	
	//Used for storing the novelty within SHINECell
	private double novelty;
	
	public static final int minWidth = 146;
	public static final int maxWidth = 444;
	
	//Constructor used for creating with all parameters, facilitating cloning
	public LevelWrap(String name, int configType, IllumMarioLevel level, Float fitness, Integer blockCount, Float jumpEntropy, Float selectionChoice, int width, float timeTaken, float marioSpeed) {
		
		this.name = name;
		this.selectionChance = 0;
		this.level = level;
		this.fitness = fitness;
		this.setLinearityScore();
		this.jumpEntropy = jumpEntropy;
		this.widthCells = level.tileWidth;
		this.heightCells = level.tileHeight;
		this.configType = configType;
		this.marioSpeed = marioSpeed;
		this.updateLevelFeatures();
	}
	
	public LevelWrap(String name, int configType, IllumMarioLevel level) {
		this.name = name;
		this.level = level;
		this.fitness = 0f;
		this.setLinearityScore();
		this.widthCells = level.tileWidth;
		this.heightCells = level.tileHeight;
		this.configType = configType;
		this.updateLevelFeatures();
		
	}
	
	//Constructor for random noise level
	public LevelWrap(String name, int configType, Random random){
		
		this.level = new IllumMarioLevel(genNoiseLevel(fixed_width, random), true);
		this.name = name;
		this.fitness = 0f;
		this.setLinearityScore();
		this.widthCells = level.tileWidth;
		this.heightCells = level.tileHeight;
		this.configType = configType;
		this.updateLevelFeatures();
	}
	
	
	public char[][] charRep(String stringRep){
		
		//System.out.println("Level heightcells and widthcells: " + heightCells + "/" + widthCells);
		String[] lines = stringRep.split("\n");
		
		char[][] charRep = new char[lines.length][lines[0].length()];
		
		
		//System.out.println("Widthcells: " + widthCells + " First string line length: " + lines[0].length());
		//System.out.println("Heightcells: " + heightCells + " Number of lines: " + lines.length);
      
        for (int y = 0; y < charRep.length; y++) {
            for (int x = 0; x < charRep[y].length; x++) {
            	
                Character c = lines[y].charAt(x);
                charRep[y][x] = c;
                
            }
        }
        return charRep;
	}
	
	public String stringRep(char[][] levelRep) {
		
		
		String output = "";
		
		char nulls = '\u0000';
		
        for (int y = 0; y < levelRep.length; y++) {
            for (int x = 0; x < levelRep[y].length; x++) {
            	
            	//Clunky handling for blank cells
            	if (levelRep[y][x] == nulls){
            		output+="-";
            	}
            	else {
            		output+=String.valueOf(levelRep[y][x]);
            	}
                if (x==levelRep[y].length-1) {
                	output+="\n";
                }
            }

        }
        
		
		return output;
		
	}
	
	public String genNoiseLevel(int size, Random random) {
		
		
        char[][] charRep = new char[14][size];
        int platformSize = 5;
        
        String space = "-";
        String block = "X";
        
        //Build ceiling
        for (int i = platformSize; i<charRep[0].length; i++) {
        	charRep[0][i] = block.charAt(0);
        }
        
        //Build initial platform
        for (int i = 0; i<charRep.length; i++) {
            for (int j = 0; j<platformSize; j++) {
                
                if (i>6) {
                	charRep[i][j] = block.charAt(0);
                }
                else {
                	charRep[i][j] = space.charAt(0);
                }
                
            }
        }
            
        //Randomly tile
        float tile_chance = random.nextFloat()*max_tile_chance;
        
        for (int i = 1; i<charRep.length; i++) {
            for (int j = platformSize; j<charRep[0].length; j++) {
                
                if (random.nextFloat()<tile_chance) {
                	charRep[i][j] = block.charAt(0);
                }
                else {
                	charRep[i][j] = space.charAt(0);
                }               
            }
        }
        
        return stringRep(charRep) ;
	
		
	}
	
	
	public LevelWrap[] crossover(LevelWrap inputLevel) {
		
		char[][] thisLevelRep = charRep(this.level.getStringRep());
		char[][] inputLevelRep = charRep(inputLevel.getLevel().getStringRep());
		
		char[][] output1 = new char[thisLevelRep.length][thisLevelRep[0].length];
		
		for (int i = 0; i<output1.length; i++) {
			output1[i] = thisLevelRep[i].clone();
		}
		
		char[][] output2 =  new char[thisLevelRep.length][thisLevelRep[0].length];
		
		for (int i = 0; i<output2.length; i++) {
			output2[i] = inputLevelRep[i].clone();
		}
		
		Random random = new Random();
		
		//Generate our two crossover points
		int point1 = random.nextInt(widthCells/2);
		int point2 = random.nextInt(widthCells/2)+widthCells/2;
		
		//System.out.println("Crossing over between point: " + point1 +"/"+point2 + " on levels with width" + output1[0].length + " and " + output1[1].length);
		
		for (int y = 0; y<thisLevelRep.length; y++) {
			
			for (int x = 0; x <thisLevelRep[0].length;x++) {
					
				//Only replace cells if we are between the two crossover points
				if (point1<x && x<point2) {
					
					//System.out.println("This level y,x: " + thisLevelRep[y][x]);
					
					output1[y][x] = inputLevelRep[y][x];
					output2[y][x] = thisLevelRep[y][x];
					
					//System.out.println("This level y,x: " + thisLevelRep[y][x]);
					
				}
					
			}
		}
		
		LevelWrap[] output = new LevelWrap[2];
		
		//System.out.println("Creating new IllumLevel post crossover:");
		
		output[0] = new LevelWrap("Output1", this.configType, new IllumMarioLevel(stringRep(output1), true));
		output[1] = new LevelWrap("Output2", this.configType, new IllumMarioLevel(stringRep(output2), true));
		
		return output;		
	}
	
	public void mutate_removeColumn() {
		
		Random random = new Random();
		int colRemove = (random.nextInt(widthCells-5)+5);

		//System.out.println("Mutate remove column being run " + colRemove);	
		//System.out.println("Input level: " + level.getStringRep());
			
		char[][] levelRep = charRep(level.getStringRep());
		char[][] newLevel = new char[levelRep.length][levelRep[0].length-1];
		for (int y = 0; y<levelRep.length; y++) {
				
			int col = 0;
			for (int x = 0; x <levelRep[0].length;x++) {
					
				//Skip the column we are removing
				if (x!=colRemove) {
					newLevel[y][col]=levelRep[y][x];
					col+=1;
				}
					
			}
		}
		
		//System.out.println("Creating new IllumLevel post mutation:");
		//System.out.println(stringRep(newLevel));
		//System.out.println("New level total characters" + stringRep(newLevel).length());
		//System.out.println("New level width: " + newLevel[0].length + " New level height: " + newLevel.length);
		//Create new level based on updated map
		level = new IllumMarioLevel(stringRep(newLevel), true);			
		//System.out.println("BC before column remove: " + oldBC + ". After: " + level.getBlockCount());
		updateLevelFeatures();
		//System.out.println("Level mutated and features updated");
		
	}
	
	public void mutate_removeColumn(int toRemove) {
		
		//Only run if we are not below our min level size of 146
		if (widthCells > minWidth) {
			int colRemove = toRemove;
			
			char[][] levelRep = charRep(level.getStringRep());
			char[][] newLevel = new char[levelRep.length][levelRep[0].length-1];
			for (int y = 0; y<levelRep.length; y++) {
				
				int col = 0;
				for (int x = 0; x <levelRep[0].length;x++) {
					
					//Skip the column we are removing
					if (x!=colRemove) {
						newLevel[y][col]=levelRep[y][x];
						col+=1;
					}
					
				}
			}
			//Create new level based on updated map
			level = new IllumMarioLevel(stringRep(newLevel), true);
			updateLevelFeatures();
		}
	}
	
	public void mutate_addColumn() {
		float oldBC = this.blockCount;
		//Only run if we are not above our max size
		if (widthCells < maxWidth) {
			System.out.println("Mutate add column being run");
			System.out.println("Input level: " + level.getStringRep());
			Random random = new Random();
			int colDupe = random.nextInt(widthCells);
			//System.out.println("Duplicating column" + colDupe + ". Values to duplicate:");
			
			char[][] levelRep = charRep(level.getStringRep());
			char[][] newLevel = new char[levelRep.length][levelRep[0].length+1];
			
			for (int y = 0; y<levelRep.length; y++) {
				
				for (int x = 0; x <levelRep[0].length;x++) {
					newLevel[y][x]=levelRep[y][x];
					
				}
			}
			//Adding last column
			for (int y = 0; y<levelRep.length; y++) {
				newLevel[y][newLevel[0].length-1]=levelRep[y][colDupe];

			}
			//Create new level based on updated map
			level = new IllumMarioLevel(stringRep(newLevel), true);
			updateLevelFeatures();
			//System.out.println("BC before column add: " + oldBC + ". After: " + level.getBlockCount());
			
		}
	}

	
	public void mutate_addColumn(int toAdd) {
		//Only run if we are not above our max size
		if (widthCells < maxWidth) {
			int colDupe = toAdd;
			
			char[][] levelRep = charRep(level.getStringRep());
			char[][] newLevel = new char[levelRep.length][levelRep[0].length+1];
			for (int y = 0; y<levelRep.length; y++) {
				
				for (int x = 0; x <levelRep[0].length;x++) {
					newLevel[y][x]=levelRep[y][x];
					
				}
			}
			//Adding last column
			
			for (int y = 0; y<levelRep.length; y++) {
				
				newLevel[y][newLevel[0].length-1]=levelRep[y][colDupe];

			}
			
			//Create new level based on updated map
			//System.out.println("Creating new IllumLevel post mutation:");
			level = new IllumMarioLevel(stringRep(newLevel), true);
			updateLevelFeatures();
		}
	}
	 
	
	public void mutate_dupeColInPlace() {
		Random random = new Random();
		mutate_dupeColInPlace(random.nextInt(level.width));
	}
	
	public void mutate_dupeColInPlace(int colDupe) {
		float oldBC = this.blockCount;
		//Only run if we are not above our max size
		if (widthCells < maxWidth) {
			//System.out.println("Dupe in place on column " + colDupe + ". On " + toString());
			//System.out.println("Duplicating column" + colDupe + ". Values to duplicate:");
			
			char[][] levelRep = charRep(level.getStringRep());
			char[][] newLevel = new char[levelRep.length][levelRep[0].length+1];
			
			
			for (int y = 0; y<newLevel.length; y++) {
				
				boolean dupeSet = false;
				
				for (int x = 0; x <newLevel[0].length;x++) {
					
					//System.out.println("Duplicating cell " + x+ "," + y);
					
					if (x == colDupe+1) {
						newLevel[y][x]=levelRep[y][x-1];
						dupeSet = true;
					}
					
					else if(!dupeSet){
							newLevel[y][x]=levelRep[y][x];
					}
					else {
						newLevel[y][x]=levelRep[y][x-1];
					}
					
				}
			}
			//Adding last column
			for (int y = 0; y<levelRep.length; y++) {
				newLevel[y][newLevel[0].length-1]=levelRep[y][colDupe];

			}
			//Create new level based on updated map
			level = new IllumMarioLevel(stringRep(newLevel), true);
			//System.out.println("BC before column add: " + oldBC + ". After: " + level.getBlockCount());
			updateLevelFeatures();
		}
	}
	
	public void mutate_flipCell() {
		
		System.out.println("Mutate flip cell being run");
		
		//Create random x and y
		Random random = new Random();
		int x = random.nextInt(level.width);
		int y = random.nextInt(level.height);		
		
		char[][] levelRep = charRep(level.getStringRep());
		
		boolean set = false;
		
		//Set value to random int between 0 and 10, so long as it is not the current value
		while (!set) {
			int newVal = random.nextInt(10);
			if (newVal!=levelRep[y][x]) {
				//levelRep[y][x]=newVal;
				set = true;
			}
		}	
		level = new IllumMarioLevel(stringRep(levelRep), true);
		updateLevelFeatures();
				
	}


	
	public void mutate_flipCell(int x, int y) {
		
		System.out.println("Mutate flip cell being run");
		
		Random random = new Random();
		
		char[][] levelRep = charRep(level.getStringRep());
		
		boolean set = false;
		
		//Set value to random int between 0 and 10, so long as it is not the current value
		while (!set) {
			int newVal = random.nextInt(10);
			if (newVal!=levelRep[y][x]) {
				//levelRep[y][x]=newVal;
				set = true;
			}
		}	
		level = new IllumMarioLevel(stringRep(levelRep),true);
		updateLevelFeatures();
				
	}
	
	public void mutate_blockUnblockCell(int x, int y) {
		
		//System.out.println("Mutate blockUnblock being run");
		
		char[][] levelRep = charRep(level.getStringRep());
		
		String space = "-";
        String block = "X";
		
		//Set value to random int between 0 and 10, so long as it is not the current value
		if (levelRep[y][x]==space.charAt(0)) {
			levelRep[y][x]=block.charAt(0);
		}
		else {
			space.charAt(0);
		}
	
		level = new IllumMarioLevel(stringRep(levelRep), true);
		updateLevelFeatures();
				
	}
	
	public int getBlockCount() {
		return blockCount;
	}
	
	public void setBlockCount(int blockCount) {
		this.blockCount = blockCount;
	}
	
	public void setLinearityScore() {
		//this.contigScore = level.getContigScore();
	}
	
	public void updateLevelFeatures() {
		//this.blockCount = level.getBlockCount();
		char[][] charRep = charRep(level.getStringRep());
		int bc = 0;
		int contig = 0;
		//Store floor value
		String floor = "X";
        for (int y = 0; y < charRep.length; y++) {
            for (int x = 0; x < charRep[y].length; x++) {
            	
            	//Increment block count
            	if(charRep[y][x]==floor.charAt(0)){
            		bc+=1;
            		
            		//Increment contiguity score
            		char [] adjacentVals = new char[4];
                    if (y>0&&x>0) {
                    	adjacentVals[0] = charRep[y-1][x-1];
                    }
                    if (y>0&&x<charRep[y].length-1) {
                        adjacentVals[1] = charRep[y-1][x+1];

                    }
                    if (y<charRep.length-1&&x>0) {
                        adjacentVals[2] = charRep[y+1][x-1];

                    }
                    if (y<charRep.length-1&&x<charRep[y].length-1) {
                        adjacentVals[3] = charRep[y+1][x+1];

                    }
                    int localContig = 0;
                    //Increase contig score for every adjacent block
                    for (int i = 0; i< adjacentVals.length; i++) {
                    	if (adjacentVals[i]==floor.charAt(0)) {
                    		localContig+=1;
                    		
                    	}
                    }
                    contig += localContig;
            	}
                
            }
        }
        this.blockCount = bc;
        this.contigScore = contig;
	}
	
	public void updateResultFeatures(MarioResult result) {
		
		//System.out.println("Number of jumps: " + result.getNumJumps() + " Number of actions: " + result.getAgentEvents().size());
		
		jumpEntropy = (float)result.getNumJumps()/(float)result.getAgentEvents().size();
		//System.out.println("Time spent (in ticks): " + (ticksPerRun-(result.getRemainingTime()/1000)) + " Completion percentage: " + result.getCompletionPercentage() + " Mario speed: " + result.getCompletionPercentage()/(ticksPerRun-(result.getRemainingTime()/1000)));
		marioSpeed = (result.getCompletionPercentage()/(ticksPerRun-(result.getRemainingTime()/1000)));
		fitness = result.getCompletionPercentage();
		
	}
	
	public Float getFitness() {
		return fitness;
	}
	
	public void setFitness(float fit) {
		this.fitness=fit;
	}
	//Run the a* agent to update our attributes
	public void runAgent() {
		Agent agent = new  Agent();
		runAgent(agent);
	}
	
	//Run the a* agent to update our attributes
	public void runAgent(Agent agent) {

		//System.out.println("Running runGame " + System.currentTimeMillis());
		result = new MarioGame().runGame(agent, level.getStringRep(), ticksPerRun);
		
		updateResultFeatures(result);
		//System.out.println("Result features updated");
	}
	
	public LevelWrap clone() {
		
		return new LevelWrap(this.name, this.configType, this.level, this.fitness, this.blockCount, this.jumpEntropy, this.selectionChance, this.widthCells, this.timeTaken, this.marioSpeed);

	}
	
	public float getParam1() {
		
		if (configType == Config_JEvsBC) {
			return blockCount;
		}
		else if (configType == Config_JEvsWidth) {
			return (float) widthCells;
		}
		else if (configType == Config_JEvsSpeed){
			return (Float) marioSpeed;
		}
		else if (configType == Config_JEvsContig){
			return (Float) contigScore;
		}
		else if (configType ==   Config_SpeedvsContig){
			return (Float) contigScore;
		}
		else {
			return (Float) null;
		}
		
	}
	
	public float getParam2() {
		//System.out.println("Get param2 ran with config type: " + this.configType);
		if (configType == Config_JEvsBC) {
			return jumpEntropy;
		}
		else if (configType == Config_JEvsWidth) {
			return jumpEntropy;
		}
		else if (configType == Config_JEvsSpeed){
			return jumpEntropy;
		}
		else if (configType == Config_JEvsContig){
			return jumpEntropy;
		}
		else if (configType ==   Config_SpeedvsContig){
			return marioSpeed;
		}
		else {
			return (Float) null;
		}
	}

	
	public void setSelectionChance(float f) {
		selectionChance = f;
	}
	
	public float getSelectionChance() {
		return selectionChance;
	}
	
	public IllumMarioLevel getLevel() {
		return level;
	}
	
	public float getJumpEntropy() {
		return jumpEntropy;
	}
	
	public void setJumpEntropy(float je) {
		this.jumpEntropy = je;
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
	
	public float getSpeed() {
		return marioSpeed;
	}
	
	public float getTimeTaken() {
		return timeTaken;
	}
	
	public float getContigScore() {
		return contigScore;
	}
	
	public char[][] getCharRep(){
		return charRep(this.level.getStringRep());
	}
	
	public void setNovelty(double lvlNovelty) {
		this.novelty = lvlNovelty;
	}
	
	public String toString() {
		return ("LevelWrap- LevelName: " + name + ". LevelFitness: " + fitness +"Level Block count: " + blockCount + ". LevelContig: " + contigScore + ". JE: " + jumpEntropy + " LevelSpeed: " + marioSpeed);
	}
	
	
	//Fitness CompareTo
	/*
	@Override
	
	public int compareTo(ShineLevel o) {
		if( this.fitness - o.fitness > 0) {
			return 1;
		}
		else if (this.fitness == o.fitness) {
			return 0;
		}
		else {
			return -1;
		}
	}
	*/
	
	
	//Novelty Compare to
	
	public int compareTo(LevelWrap o) {
		if( this.novelty - o.novelty > 0) {
			return 1;
		}
		else if (this.novelty == o.novelty) {
			return 0;
		}
		else {
			return -1;
		}
	}
	
	
}
