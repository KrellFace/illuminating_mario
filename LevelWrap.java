package illumsearch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import agents.robinBaumgarten.Agent;
import engine.core.MarioGame;
import engine.core.MarioResult;

public class LevelWrap implements Comparable < LevelWrap > {

    private IllumConfig config;

    //Storage for all level parameters for the level stored in the Level Wrap
    private int config_param1;
    private int config_param2;
    private String name;
    private IllumMarioLevel level;
    private float fitness;
    private Integer blockCount;
    private float contigScore;
    private float agrSmooth;
    private Integer clearRows;
    private float jumpEntropy;
    private float selectionChance;
    private int widthCells;
    private int heightCells;
    private float timeTaken;
    private float marioSpeed;
    private MarioResult result;

    private int platformSize = 5;

    //Used for storing the novelty within SHINECell
    private double novelty;

    //Constructor for creating with a specified unevaluated level
    public LevelWrap(String name, IllumConfig config, int param1, int param2, IllumMarioLevel level) {
        this.config = config;
        this.name = name;
        this.level = level;
        this.fitness = 0f;
        this.widthCells = level.tileWidth;
        this.heightCells = level.tileHeight;
        this.config_param1 = param1;
        this.config_param2 = param2;
        this.updateLevelFeatures();


    }

    //Constructor for random noise level
    public LevelWrap(String name, IllumConfig config, int param1, int param2, Random random) {
        this.config = config;
        this.level = new IllumMarioLevel(genNoiseLevel(config.fixed_width, random), true);
        this.name = name;
        this.fitness = 0f;
        this.widthCells = level.tileWidth;
        this.heightCells = level.tileHeight;
        this.config_param1 = param1;
        this.config_param2 = param2;
        this.updateLevelFeatures();
    }
  
    



	//Constructor used for creating with all parameters, facilitating cloning
    public LevelWrap(String name, IllumConfig config, int param1, int param2, IllumMarioLevel level, Float fitness, Integer blockCount, Integer clearRows, Float jumpEntropy, Float selectionChoice, int width, float timeTaken, float marioSpeed) {
        this.config = config;
        this.name = name;
        this.selectionChance = 0;
        this.level = level;
        this.fitness = fitness;
        this.jumpEntropy = jumpEntropy;
        this.widthCells = level.tileWidth;
        this.heightCells = level.tileHeight;
        this.blockCount = blockCount;
        this.clearRows = clearRows;
        this.config_param1 = param1;
        this.config_param2 = param2;
        this.marioSpeed = marioSpeed;
        this.updateLevelFeatures();
    }


    public char[][] charRep(String stringRep) {

        String[] lines = stringRep.split("\n");

        char[][] charRep = new char[lines.length][lines[0].length()];

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
                if (levelRep[y][x] == nulls) {
                    output += "-";
                } else {
                    output += String.valueOf(levelRep[y][x]);
                }
                if (x == levelRep[y].length - 1) {
                    output += "\n";
                }
            }

        }
        return output;

    }

    public String genNoiseLevel(int size, Random random) {


        char[][] charRep = new char[14][size];

        String space = "-";
        String block = "X";

        //Build ceiling
        for (int i = platformSize; i < charRep[0].length; i++) {
            charRep[0][i] = block.charAt(0);
        }

        //Build initial platform
        for (int i = 0; i < charRep.length; i++) {
            for (int j = 0; j < platformSize; j++) {

                if (i > 6) {
                    charRep[i][j] = block.charAt(0);
                } else {
                    charRep[i][j] = space.charAt(0);
                }

            }
        }

        //Randomly tile
        float tile_chance = random.nextFloat() * config.max_tile_chance;

        for (int i = 1; i < charRep.length; i++) {
            for (int j = platformSize; j < charRep[0].length; j++) {

                if (random.nextFloat() < tile_chance) {
                    charRep[i][j] = block.charAt(0);
                } else {
                    charRep[i][j] = space.charAt(0);
                }
            }
        }

        return stringRep(charRep);

    }


    public LevelWrap[] crossover(LevelWrap inputLevel) {

        char[][] thisLevelRep = charRep(this.level.getStringRep());
        char[][] inputLevelRep = charRep(inputLevel.getLevel().getStringRep());

        //Initialise a copy of input level 1 (caller)
        char[][] output1 = new char[thisLevelRep.length][thisLevelRep[0].length];
        for (int i = 0; i < output1.length; i++) {
            output1[i] = thisLevelRep[i].clone();
        }

        //Initialise a copy of input level 2 (input)
        char[][] output2 = new char[thisLevelRep.length][thisLevelRep[0].length];
        for (int i = 0; i < output2.length; i++) {
            output2[i] = inputLevelRep[i].clone();
        }

        Random random = new Random();

        //Generate our two crossover points
        int point1 = random.nextInt(widthCells / 2);
        int point2 = random.nextInt(widthCells / 2) + widthCells / 2;

        //System.out.println("Crossing over between point: " + point1 +"/"+point2 + " on levels with width" + output1[0].length + " and " + output1[1].length);

        for (int y = 0; y < thisLevelRep.length; y++) {

            for (int x = 0; x < thisLevelRep[0].length; x++) {

                //Only replace cells if we are between the two crossover points
                if (point1 < x && x < point2) {

                    //System.out.println("This level y,x: " + thisLevelRep[y][x]);

                    output1[y][x] = inputLevelRep[y][x];
                    output2[y][x] = thisLevelRep[y][x];

                    //System.out.println("This level y,x: " + thisLevelRep[y][x]);

                }
            }
        }

        LevelWrap[] output = new LevelWrap[2];

        //System.out.println("Creating new IllumLevel post crossover:");

        output[0] = new LevelWrap("Output1", this.config, this.config_param1, this.config_param2, new IllumMarioLevel(stringRep(output1), true));
        output[1] = new LevelWrap("Output2", this.config, this.config_param1, this.config_param2, new IllumMarioLevel(stringRep(output2), true));

        return output;
    }

    //Remove random column
    public void mutate_removeColumn() {

        Random random = new Random();
        int colRemove = (random.nextInt(widthCells - 5) + 5);
        mutate_removeColumn(colRemove);
    }

    //Remove specified column
    public void mutate_removeColumn(int toRemove) {

        //Only run if we are not below our min level size of 146
        int colRemove = toRemove;

        char[][] levelRep = charRep(level.getStringRep());
        char[][] newLevel = new char[levelRep.length][levelRep[0].length - 1];
        for (int y = 0; y < levelRep.length; y++) {

            int col = 0;
            for (int x = 0; x < levelRep[0].length; x++) {

                //Skip the column we are removing
                if (x != colRemove) {
                    newLevel[y][col] = levelRep[y][x];
                    col += 1;
                }

            }
        }
        //Create new level based on updated map
        level = new IllumMarioLevel(stringRep(newLevel), true);
        updateLevelFeatures();
    }

    public void mutate_addColumn() {
        float oldBC = this.blockCount;
        System.out.println("Mutate add column being run");
        System.out.println("Input level: " + level.getStringRep());
        Random random = new Random();
        int colDupe = random.nextInt(widthCells);
        //System.out.println("Duplicating column" + colDupe + ". Values to duplicate:");

        char[][] levelRep = charRep(level.getStringRep());
        char[][] newLevel = new char[levelRep.length][levelRep[0].length + 1];

        for (int y = 0; y < levelRep.length; y++) {

            for (int x = 0; x < levelRep[0].length; x++) {
                newLevel[y][x] = levelRep[y][x];

            }
        }
        //Adding last column
        for (int y = 0; y < levelRep.length; y++) {
            newLevel[y][newLevel[0].length - 1] = levelRep[y][colDupe];

        }
        //Create new level based on updated map
        level = new IllumMarioLevel(stringRep(newLevel), true);
        updateLevelFeatures();
        //System.out.println("BC before column add: " + oldBC + ". After: " + level.getBlockCount());
    }


    public void mutate_addColumn(int toAdd) {
        int colDupe = toAdd;

        char[][] levelRep = charRep(level.getStringRep());
        char[][] newLevel = new char[levelRep.length][levelRep[0].length + 1];
        for (int y = 0; y < levelRep.length; y++) {

            for (int x = 0; x < levelRep[0].length; x++) {
                newLevel[y][x] = levelRep[y][x];
            }
        }
        //Adding last column

        for (int y = 0; y < levelRep.length; y++) {
            newLevel[y][newLevel[0].length - 1] = levelRep[y][colDupe];
        }
        //Create new level based on updated map
        //System.out.println("Creating new IllumLevel post mutation:");
        level = new IllumMarioLevel(stringRep(newLevel), true);
        updateLevelFeatures();
    }


    public void mutate_dupeColInPlace() {
        Random random = new Random();
        mutate_dupeColInPlace(random.nextInt(level.width));
    }

    public void mutate_dupeColInPlace(int colDupe) {
        float oldBC = this.blockCount;
        //System.out.println("Dupe in place on column " + colDupe + ". On " + toString());

        char[][] levelRep = charRep(level.getStringRep());
        char[][] newLevel = new char[levelRep.length][levelRep[0].length + 1];


        for (int y = 0; y < newLevel.length; y++) {

            boolean dupeSet = false;

            for (int x = 0; x < newLevel[0].length; x++) {

                //System.out.println("Duplicating cell " + x+ "," + y);

                if (x == colDupe + 1) {
                    newLevel[y][x] = levelRep[y][x - 1];
                    dupeSet = true;
                } else if (!dupeSet) {
                    newLevel[y][x] = levelRep[y][x];
                } else {
                    newLevel[y][x] = levelRep[y][x - 1];
                }

            }
        }
        //Adding last column
        for (int y = 0; y < levelRep.length; y++) {
            newLevel[y][newLevel[0].length - 1] = levelRep[y][colDupe];

        }
        //Create new level based on updated map
        level = new IllumMarioLevel(stringRep(newLevel), true);
        //System.out.println("BC before column add: " + oldBC + ". After: " + level.getBlockCount());
        updateLevelFeatures();
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
            if (newVal != levelRep[y][x]) {
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
            if (newVal != levelRep[y][x]) {
                //levelRep[y][x]=newVal;
                set = true;
            }
        }
        level = new IllumMarioLevel(stringRep(levelRep), true);
        updateLevelFeatures();

    }

    public void mutate_blockUnblockCell(int x, int y) {

        //System.out.println("Mutate blockUnblock being run");

        char[][] levelRep = charRep(level.getStringRep());

        String space = "-";
        String block = "X";

        //Set value to random int between 0 and 10, so long as it is not the current value
        if (levelRep[y][x] == space.charAt(0)) {
            levelRep[y][x] = block.charAt(0);
        } else {
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
    public void updateLevelFeatures() {
        //this.blockCount = level.getBlockCount();
        char[][] charRep = charRep(level.getStringRep());
        
        //Instantiate local feature scores
        int bc = 0;
        int contig = 0;
        int clearRows = 0;
        int smoothness = 0;
        
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
        
        int prevBC = 0;
        int currBC = 0;
        
        //Looping through every block, column by column
        for (int x = platformSize; x< charRep[0].length; x++) {
        	
	
        	for (int y = 0; y<charRep.length; y++) {
        		
        		if (charRep[y][x] == floor.charAt(0)) {
        			currBC+=1;
        		}
        	}
        	smoothness+=(Math.abs(currBC-prevBC));
        	prevBC=currBC;
        	currBC=0;
        }
        
        this.blockCount = bc;
        this.contigScore = contig;
        this.clearRows = clearRows;
        this.agrSmooth = smoothness;
    }

    public void updateResultFeatures(MarioResult result) {

        //System.out.println("Number of jumps: " + result.getNumJumps() + " Number of actions: " + result.getAgentEvents().size());

        jumpEntropy = (float) result.getNumJumps() / (float) result.getAgentEvents().size();
        //System.out.println("Time spent (in ticks): " + (ticksPerRun-(result.getRemainingTime()/1000)) + " Completion percentage: " + result.getCompletionPercentage() + " Mario speed: " + result.getCompletionPercentage()/(ticksPerRun-(result.getRemainingTime()/1000)));
        marioSpeed = (result.getCompletionPercentage() / (config.ticksPerRun - (result.getRemainingTime() / 1000)));
        timeTaken = (config.ticksPerRun - (result.getRemainingTime() / 1000));
        fitness = result.getCompletionPercentage();

    }

    public Float getFitness() {
        return fitness;
    }

    public void setFitness(float fit) {
        this.fitness = fit;
    }
    //Run the a* agent to update our attributes
    public void runAgent() {
        Agent agent = new Agent();
        runAgent(agent);
    }

    //Run the a* agent to update our attributes
    public void runAgent(Agent agent) {

        //System.out.println("Running runGame " + System.currentTimeMillis());
        long startTime = System.currentTimeMillis();
        //System.out.println(level.getStringRep());
        result = new MarioGame().runGame(agent, level.getStringRep(), config.ticksPerRun);

        updateResultFeatures(result);
        //System.out.println(this.toString());
        //System.out.println("Agent run took " + ((System.currentTimeMillis()-startTime)/1000f)  + " seconds to complete");
    }

    public LevelWrap clone() {

        return new LevelWrap(this.name, this.config, this.config_param1, this.config_param2, this.level, this.fitness, this.blockCount, this.clearRows, this.jumpEntropy, this.selectionChance, this.widthCells, this.timeTaken, this.marioSpeed);

    }

    public void createLevelFiles(Path levelPath) throws IOException,
    Exception {

        if (Files.notExists(levelPath)) {
        	Files.createDirectory(levelPath);
        }
        
        //Create visual rep of the level
        new ImageGen((getName()), levelPath, getCharRep());

        //Create level stats file
        PrintWriter lvlwriter = new PrintWriter((levelPath + "/" + getName() + "-Data.txt"), "UTF-8");
        lvlwriter.println("Level Name: " + getName());
        lvlwriter.println("Level Fitness: " + getFitness());
        lvlwriter.println("Level Width: " + getWidth());
        lvlwriter.println("Level Jump Entropy: " + getJumpEntropy());
        lvlwriter.println("Level Block Count: " + getBlockCount());
        lvlwriter.println("Level Contiguity Score: " + getContigScore());
        lvlwriter.println("Level Speed: " + getSpeed());
        lvlwriter.println("Level Time Taken: " + getTimeTaken());
        lvlwriter.println("Level Clear Rows: " + getClearRows());
        lvlwriter.println("Level Aggregate Smoothness: " + getAggrSmooth());
        lvlwriter.close();
        
        //Print level wrap
        PrintWriter levelrep = new PrintWriter((levelPath + "/" + getName() + "-LevelRep.txt"), "UTF-8");
        levelrep.println(level.getStringRep());
        levelrep.close();

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // root element
        Element levelroot = document.createElement("Level");
        document.appendChild(levelroot);

        // set an attribute to staff element
        Attr df = document.createAttribute("derived-from");
        df.setValue(getName());
        levelroot.setAttributeNode(df);

        Element fitness = document.createElement("fitness");
        fitness.appendChild(document.createTextNode(getFitness().toString()));
        levelroot.appendChild(fitness);

        Element width = document.createElement("width");
        width.appendChild(document.createTextNode(String.valueOf(getWidth())));
        levelroot.appendChild(width);

        Element je = document.createElement("jump-entropy");
        je.appendChild(document.createTextNode(String.valueOf(getJumpEntropy())));
        levelroot.appendChild(je);

        Element bc = document.createElement("block-count");
        bc.appendChild(document.createTextNode(String.valueOf(getBlockCount())));
        levelroot.appendChild(bc);

        Element speed = document.createElement("speed");
        speed.appendChild(document.createTextNode(String.valueOf(getSpeed())));
        levelroot.appendChild(speed);

        Element tt = document.createElement("time-taken");
        tt.appendChild(document.createTextNode(String.valueOf(getTimeTaken())));
        levelroot.appendChild(tt);

        Element contig = document.createElement("contiguity");
        contig.appendChild(document.createTextNode(String.valueOf(getContigScore())));
        levelroot.appendChild(contig);

        Element cr = document.createElement("clear-rows");
        cr.appendChild(document.createTextNode(String.valueOf(getClearRows())));
        levelroot.appendChild(cr);

        // create the xml file
        //transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(levelPath + "/" + getName() + "-xml.txt"));

        transformer.transform(domSource, streamResult);

    }

    public float getParam1() {

        if (config_param1 == config.config_paramBC) {
            return blockCount;
        } else if (config_param1 == config.config_paramWidth) {
            return (float) widthCells;
        } else if (config_param1 == config.config_paramSpeed) {
            return (Float) marioSpeed;
        } else if (config_param1 == config.config_paramContig) {
            return (Float) contigScore;
        } else if (config_param1 == config.config_paramJE) {
            return (Float) jumpEntropy;
        } else if (config_param1 == config.config_paramClearRows) {
            return clearRows;
        }else if (config_param1 == config.config_paramAgrSmooth) {
            return agrSmooth;
        } else {
            return (Float) null;
        }

    }

    public float getParam2() {
        //System.out.println("Get param2 ran with config type: " + this.configType);
        if (config_param2 == config.config_paramBC) {
            return blockCount;
        } else if (config_param2 == config.config_paramWidth) {
            return (float) widthCells;
        } else if (config_param2 == config.config_paramSpeed) {
            return (Float) marioSpeed;
        } else if (config_param2 == config.config_paramContig) {
            return (Float) contigScore;
        } else if (config_param2 == config.config_paramJE) {
            return (Float) jumpEntropy;
        } else if (config_param2 == config.config_paramClearRows) {
            return clearRows;
        } else if (config_param2 == config.config_paramAgrSmooth) {
            return agrSmooth;
        }  else {
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

    public float getClearRows() {
        return clearRows;
    }

    public char[][] getCharRep() {
        return charRep(this.level.getStringRep());
    }
    
    public float getAggrSmooth() {
    	return agrSmooth;
    }

    public void setNovelty(double lvlNovelty) {
        this.novelty = lvlNovelty;
    }

    public String toString() {
        return ("LevelWrap- LevelName: " + name + ". LevelFitness: " + fitness + "Level Block count: " + blockCount + ". LevelContig: " + contigScore + ". JE: " + jumpEntropy + " LevelSpeed: " + marioSpeed + " Clear rows: " + this.clearRows + " AggrSmoothness: " + this.agrSmooth);
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
        if (this.novelty - o.novelty > 0) {
            return 1;
        } else if (this.novelty == o.novelty) {
            return 0;
        } else {
            return -1;
        }
    }


}