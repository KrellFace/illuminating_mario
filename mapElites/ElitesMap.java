package illuminating_mario.mapElites;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import illuminating_mario.mainFunc.*;

public class ElitesMap {
    
    public static final float fitnessThresshold = 1.00f;
    
    private HashMap<List<Integer>, IllumLevelWrap> map;
    private float[] param1Intervals;
    private float[] param2Intervals;
    private int mapSize;
    float countPopulated = 0;
    float fitCellCount = 0;
    float averageFitness = 0;
    
    private float param1Min;
    private float param1Max;
    private float param2Min;
    private float param2Max;
    
    //Constructor to initialise with levels
    public ElitesMap (ArrayList<IllumLevelWrap> allLevels, int mapSize, float param1Min, float param1Max, float param2Min, float param2Max){
        
    	this(mapSize, param1Min, param1Max, param2Min, param2Max);
        this.addLevels(allLevels);
        updateFitnessStats();

    }
    
    //Constructor to initialise empty map
    public ElitesMap (int mapSize, float param1Min, float param1Max, float param2Min, float param2Max){
        
        this.param1Min = param1Min;
        this.param1Max = param1Max;
        this.param2Min = param2Min;
        this.param2Max = param2Max;
        this.map  =  new HashMap<List<Integer>, IllumLevelWrap>();
        this.mapSize = mapSize;
        this.param1Intervals = new float[mapSize];
        float param1Iter = param1Min;
        //Create BC map intervals
        for (int i = 0; i<mapSize; i++) {
            param1Intervals[i]=param1Iter;
            param1Iter += (param1Max-param1Min)/mapSize;
        }
        
        //System.out.println("ShineTree- Param1 Intervals: " + Arrays.toString(param1Intervals));
        
        this.param2Intervals = new float[mapSize];
        float param2Iter = param2Min;
        //Create JE map intervals
        for (int i = 0; i<mapSize; i++) {
            param2Intervals[i]=param2Iter;
            param2Iter += (param2Max-param2Min)/mapSize;
        }
        
        //System.out.println("ShineTree- Param2 Intervals: " + Arrays.toString(param2Intervals));
        
        //Populate map with empty cells
        for (int x = 0; x<mapSize; x++) {
            for (int y = 0; y<mapSize; y++) {
                //System.out.println("Adding " + x +"/" + y);
                map.put(Arrays.asList(x,y), null);
            }
        }
         
        //System.out.println("Map size: " + map.size());                
    }
     
    private void addLevels(ArrayList<IllumLevelWrap> levels) {
        //System.out.println("Param 1 & 2 max: " + this.param1Max + "/" + this.param2Max);
        for (int curLev = 0; curLev<levels.size();curLev++) {
            
            addLevel(levels.get(curLev));
                                    
        }
    }
    
    public void addLevel(IllumLevelWrap level) {
        //Locating level within JumpEnt and BlockCount axis
        boolean param1Set = false;
        boolean param2Set = false;
        int param1MapLoc = -1;
        int param2MapLoc = -1;
        
        //System.out.println("ElitesMap- Level param1 = " + level.getParam1());
        //System.out.println("ElitesMap- Level param2 = " + level.getParam2());
        
        //System.out.println("Param1 min/max: "+ param1Min + "/" + param1Max);
        
        //Find levels location within map by looping from final param
        for (int i = mapSize-1; i>=0; i--) {
            
            //System.out.println("ElitesMap - Checking if level with " + level.getParam1() +"/"+level.getParam2()+ " belongs in " + param1Intervals[i]+"/"+param2Intervals[i]);
            
            //Find param1 location
            if (!param1Set && level.getParam1() >= param1Intervals[i] && level.getParam1()<param1Max && level.getParam1()>param1Min) {
                param1MapLoc = i;
                param1Set=true;
                //System.out.println("Param 1 location found as " + level.getParam1() + " is bigger than interval " + param1Intervals[i] + " and smaller than " + param1Intervals[i+1] );
            }
            //Find param2 location
            if (!param2Set && level.getParam2() >= param2Intervals[i] && level.getParam2()<param2Max && level.getParam2()>param2Min) {
                param2MapLoc = i;
                param2Set=true;
                //System.out.println("Param 2 location found as " + level.getParam2() + " is bigger than interval " + param2Intervals[i] + " and smaller than " + param2Intervals[i+1] );

            }
        }
        
        List<Integer> cell = Arrays.asList(param1MapLoc, param2MapLoc);
        //Only run if cells were found for both
        if (param1MapLoc>=0&&param2MapLoc>=0) {
            
            //System.out.println("ElitesMap - Location found, adding level");
        
            //Evaluate if cell is currently null
            if (getMapValue(cell) ==  null) {
                //System.out.println("Blank cell populated" );
                map.put(cell, level);
                //System.out.println(map.get(cell) == null);
                
                //Add to our count of populated cells
                countPopulated += 1;
            }
            //Evaluate if it is the highest fitness so far for cell
            else if(getMapValue(cell).getFitness()<level.getFitness()){
                //System.out.println("Fitter level found for cell " );
                map.put(cell, level);
            }
        }
        else {
            //System.out.println(level.getName()+ " does not fit. Param1Loc:" + param1MapLoc+". Param2Loc "+ param2MapLoc);
            //System.out.println("Param1: " + level.getParam1() + ". Param2: " + level.getParam2());
            //System.out.println("Param1 intervals : " + Arrays.toString(param1Intervals));
            //System.out.println("Param2 intervals : " + Arrays.toString(param2Intervals));
        }
        
    }
    
    public void createOutputFiles(Path rootPath, String runName, boolean onlyFit) throws Exception {
        
        if (!Files.exists(rootPath)) {
            //System.out.println("Output folder does not exist when it should");
            Files.createDirectory(rootPath);
            
        }
        FileWriter heatMap = new FileWriter((rootPath + "/" +runName+"- HeatMap.csv"));

        System.out.println("Create output files in ElitesMap being run");
        
        //Create header row
        heatMap.append(" ,");
        for (int i = 0; i < mapSize; i++) {
            heatMap.append( i + ",");
        }
        heatMap.append("\n");
        
        //Create a folder for each map tile (if it contains a level)
        for (int i = 0; i<mapSize; ++i) {
            //Add index in first column
            heatMap.append( i + ",");
            for (int j = 0; j<mapSize; ++j) {
                List<Integer> cell = Arrays.asList(i, j);
                //If there is a level at the cell, create a level rep in the folder 
                if (map.get(cell) != null) {
                    IllumLevelWrap level = map.get(cell);
                    //Add fitness to heatmap
                    heatMap.append( level.getFitness().toString());
                    
                    //Only create if the level is fully fit, or if we are printing all levels
                    if (!onlyFit||map.get(cell).getFitness()>=1.0f) {
                    	
                    	//System.out.println("Creating output files for level with fitness " + map.get(cell).getFitness() + " and with onlyFit flag: " + onlyFit);
                    	
                    	Path levelFolder = Paths.get(rootPath + "/" + cell);

                    	level.createLevelFiles(levelFolder);
                    	
                    }
                }
                else {
                    heatMap.append( "0");
                }
                heatMap.append( ", ");    
            }     
            heatMap.append("\n");            
        } 
        heatMap.flush();
        heatMap.close();
    }
    
    
    
    public void updateFitnessStats() {
        fitCellCount = 0;
        averageFitness = 0;
        
        float totalFit = 0;
        
        //System.out.println("Updating fit count");
        for (IllumLevelWrap value : map.values()) {
            
            if (value!=null) {
                    //System.out.println("Level " + value.getName() + " with fitness " + value.getFitness());
                totalFit+=value.getFitness();
            }
            
            if (value!=null && value.getFitness()>=fitnessThresshold) {
                //System.out.println("Fit level found");
                fitCellCount+=1;
            }
            
        }
        
        averageFitness = totalFit/(mapSize*mapSize);
    }
    
    public IllumLevelWrap getRandomLevel() {
        
        boolean retrieved = false;
        IllumLevelWrap level = null;
        
        //System.out.println("Starting get random level");
        
        do {
            int x = ThreadLocalRandom.current().nextInt(0, mapSize + 1);
            int y = ThreadLocalRandom.current().nextInt(0, mapSize + 1);
            List<Integer> cell = Arrays.asList(x, y);
            if (getMapValue(cell) !=  null) {
                //System.out.println("Blank cell populated" );
                level = map.get(cell);
                retrieved = true;
            }
        }
        while (!retrieved);
        
        //System.out.println("Ending get random level");

        
        return level;
        
    }
    
    private IllumLevelWrap getMapValue(List<Integer> key) {
        IllumLevelWrap value = map.get(key);
        return value == null ? null : value;
    }
    
    public HashMap<List<Integer>, IllumLevelWrap> getMap(){
        return map;
    }

    public float getCoverage() {
        return countPopulated/(float) (mapSize*mapSize);
    }
    
    public float getReliability() {
        return fitCellCount/(float) (mapSize*mapSize);
    }
    
    public float getAvgFitness() {
        return averageFitness;
    }
    
    public String toString() {
        updateFitnessStats();
        return("Coverage of tree = " + getCoverage()  + ". Reliability of tree = " + getReliability()+". Average Fitness = " + getAvgFitness());
    }
}
