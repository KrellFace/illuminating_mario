package illumsearch;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class AlgorithmRun
{   
	IllumConfig config = new IllumConfig();
	
	//SHINE Variables
    private int Num_Generations;
    //MAP Elites Variables (Each iteration = new new offspring due to crossover operation)
    private int Num_Iterations;
    
    //Storage for the parameters used in current run
    private static float param1Min;
    private static float param1Max;
    private static float param2Min;
    private static float param2Max;
    
    //Seed used for generating initial population
    private int algoType;
    private int runConfigType;
    private Path outputFolder;
    private String runName;
    
    
    public AlgorithmRun(int algoType, int numOffspring, int runConfig, Path outputFolder, String runName) {
        
        this.algoType = algoType;
        this.runConfigType = runConfig;
        this.Num_Iterations = (numOffspring/2);
        this.Num_Generations = (numOffspring/20);
        this.outputFolder = outputFolder;
        this.runName = runName;     
        
    }
    
    public void run() {
        if (algoType == config.Algo_MapElites) {
            System.out.println("Starting map elites run of config type: " + runConfigType + " running for " + Num_Iterations + " iterations");

            init_mapelites();
            
        }
        else if (algoType == config.Algo_Shine) {
            
            init_shine();
        }
        else {
            System.out.println("Unknown algorithm inputted");
        }
    }

    
    public void init_shine() {
        
        long runStartTime = System.nanoTime();
            
        setParams();
        
       // System.out.println("Params set");
        
        //List<LevelWrap> init_pop = initPopFromFolder(Input_Files);
        List<LevelWrap> init_pop = initRandomPop(runConfigType, config.initialSeed);
        System.out.println("Population fully initialised");
        //Store initial levels used
        try {
            levelsToFiles(init_pop, "Initial Population" );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("Population stored");
        
        ShineTree tree = new ShineTree(config.Max_Tree_Depth, config.Max_Vertex_Reps, param1Min, param1Max, param2Min, param2Max);
        int Gen_Count = 0;
        
        ArrayList<String> runHistory = new ArrayList<String>();
                
        //Add levels to our tree
        for (int i = 0; i< init_pop.size(); i++) {
            tree.root.addLevel(init_pop.get(i));
            
        }
        
        
        //Run until we reach max generations
        do {
            System.out.println("STARTING GENERATION " + Gen_Count);
            
            //tree.root.printLeaves(true);  
            System.out.println("Total leaf nodes: " + tree.root.countLeaves());
            
                        
            //CREATING ARCHIVE FROM TREE (i.e limit reps at each vertex)
            ArrayList<LevelWrap> archive = tree.createArchive();
            //printArchive(archive, "Depth weight selected", false);
            
            //SELECTING PARENTS FOR NEXT GENERATION
            ArrayList<LevelWrap> selectedArchive =  tournamentSelect(archive);
            //printArchive(selectedArchive, "Selected Parents before mutation:", true);
            
            //MUTATING SELECTED PARENTS
            selectedArchive = archiveOffspring(selectedArchive);
            //printArchive(selectedArchive, "Selected Parents after mutation:", true);
        
            //ADDING CHILDREN TO TREE
            for (int i = 0; i<selectedArchive.size();i++) {
                
                LevelWrap currChild = selectedArchive.get(i);
                currChild.runAgent();
                tree.root.addLevel(currChild.clone());
                currChild = null;
            }
            Gen_Count+=1;
            
            ElitesMap currMap = eval_CreateMap(tree);
            System.out.println(currMap.toString());
            runHistory.add(Gen_Count +", " + currMap.getCoverage() + ", " + currMap.getReliability() + ", " + currMap.getAvgFitness());
            mapOutput(currMap, runHistory, runName+" - Snapshot"+Gen_Count, runStartTime, true);

            
        }
        while (Gen_Count<Num_Generations);
        
        
        //Create the output from the map
        mapOutput(eval_CreateMap(tree), runHistory, runName+" - Final Data", runStartTime, false);
                        
        
    }
    
    public void init_mapelites() {
        
        long runStartTime = System.nanoTime();
        
        setParams();
        
        //List<LevelWrap> init_pop = initPopFromFolder(Input_Files);
        List<LevelWrap> init_pop = initRandomPop(runConfigType, config.initialSeed);
        
        
        try {
            levelsToFiles(init_pop, "Initial Population" );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        int iterCount = 0;
        
        ArrayList<String> runHistory = new ArrayList<String>();
        
        ElitesMap map = new ElitesMap(config.mapSize, param1Min, param1Max, param2Min, param2Max);

        for (int i = 0; i< init_pop.size(); i++) {
            map.addLevel(init_pop.get(i));          
        }
        
        //Run until we reach max iterations
        do {

            //System.out.println("Beginning iteration " + iterCount);
            Random random = new Random();
                
            ArrayList<LevelWrap> levelPair = new ArrayList<LevelWrap>();
            
            //Select parent pair for this generation
            levelPair.add(map.getRandomLevel().clone());
            levelPair.add(map.getRandomLevel().clone());
            
            //Generate offspring from parents
            levelPair = archiveOffspring(levelPair);
            
            //Add the level pair back into the map
            for (int i = 0; i < levelPair.size(); i++) {
                levelPair.get(i).runAgent();
                map.addLevel(levelPair.get(i));
            }

            
            
            //Add to history 
            if (iterCount%20==0) {
                System.out.println("ITERATION " + iterCount);

                System.out.println(map.toString());         
                runHistory.add(iterCount +", " + map.getCoverage() + ", " + map.getReliability() + ", " + map.getAvgFitness());
                
                if (iterCount%100==0) {
                    mapOutput(map, runHistory, runName+" - Snapshot"+iterCount, runStartTime, true);
                }
            }
            System.out.println("ITERATION " + iterCount);
            iterCount+=1;
    
        }
        while (iterCount<Num_Iterations);
            
        //Create the output from the map
        mapOutput(map, runHistory, runName, runStartTime, true);
                        
        
    }
    
    //Setting the parameters of current run based on config type
    public void setParams() {
        if (runConfigType == config.config_runType_JEvsBC) {
            param1Min = config.config_map_minBC;
            param1Max = config.config_map_maxBC;
            param2Min = config.config_map_minJE;
            param2Max = config.config_map_maxJE;
        }
        else if (runConfigType == config.config_runType_JEvsWidth) {
            param1Min = config.config_map_minLW;
            param1Max = config.config_map_maxLW;
            param2Min = config.config_map_minJE;
            param2Max = config.config_map_maxJE;
        }
        else if (runConfigType == config.config_runType_JEvsSpeed) {
            param1Min = config.config_map_minSpeed;
            param1Max = config.config_map_maxSpeed;
            param2Min = config.config_map_minJE;
            param2Max = config.config_map_maxJE;
        }
        else if (runConfigType == config.config_runType_JEvsContig) {
            param1Min = config.config_map_minContig;
            param1Max = config.config_map_maxContig;
            param2Min = config.config_map_minJE;
            param2Max = config.config_map_maxJE;
        }
        else if (runConfigType == config.config_runType_SpeedvsContig) {
            param1Min = config.config_map_minContig;
            param1Max = config.config_map_maxContig;
            param2Min = config.config_map_minSpeed;
            param2Max = config.config_map_maxSpeed;
        }
    }
    
    

    
    public ArrayList<LevelWrap> archiveOffspring(ArrayList<LevelWrap> inputArchive){
        
        ArrayList<LevelWrap> outputArchive = new ArrayList<LevelWrap>();
        
        Random random = new Random();
        
        //Loop through archive selecting random pairs
        while (inputArchive.size() > 0) {
            LevelWrap[] selectedPair = new LevelWrap[2];
            int first = random.nextInt(inputArchive.size());
            selectedPair[0] = inputArchive.get(first);
            inputArchive.remove(first);
            int second = random.nextInt(inputArchive.size());
            selectedPair[1] = inputArchive.get(second);
            inputArchive.remove(second);
            
            if (random.nextFloat()<config.Crossover_Chance) {
                selectedPair = selectedPair[0].crossover(selectedPair[1]);
                //System.out.println("Crossover fired");
            }
            
            mutate_dupeRemoveLevel(selectedPair[0]);
            mutate_dupeRemoveLevel(selectedPair[1]);
            mutate_tileflipLevel(selectedPair[0]);
            mutate_tileflipLevel(selectedPair[1]);
            
            outputArchive.add(selectedPair[0]);
            outputArchive.add(selectedPair[1]);
        }
        
        
        return outputArchive;
    }
    
    //Runs the Duplicate + Remove a column method for every column if odds achieved
    public void mutate_dupeRemoveLevel(LevelWrap inputLevel) {
        Random random = new Random();
        for (int y = 5; y < inputLevel.getWidth()-1; y++) {
        
            if (random.nextFloat()<config.Dupe_Remove_Chance) {
                inputLevel.mutate_removeColumn();
                inputLevel.mutate_dupeColInPlace(y);
            }
        }
    }
    
    //Runs the Tile Flip mutation method for every tile in a level if odds achieved
    public void mutate_tileflipLevel(LevelWrap inputLevel) {
        Random random = new Random();
        for (int y = 1; y < inputLevel.getHeight(); y++) {
            for (int x = 5; x < inputLevel.getWidth(); x++) {
                if (random.nextFloat()<config.Tile_Mutation_Chance){
                    inputLevel.mutate_blockUnblockCell(x, y);
                }
            }
        }
    }
    
    
    public ArrayList<LevelWrap> initRandomPop(int configType, int seed){
    	
    	System.out.println("Init random pop started");
        
        ArrayList<LevelWrap> outputlevels = new ArrayList<>();
        
        //Initialise the random number generator from seed that will be used to create all levels
        Random fixedRandom = new Random(seed);
                
        for (int i = 0; i<config.Generation_Size; i++) {
            
            //IllumMarioLevel levelToAdd = genRandLevel(fixed_width, fixedRandom);
            LevelWrap sLevelToAdd = new LevelWrap(("Level " + i),configType, fixedRandom);
            //System.out.println("Run agent about to be run in AR");
            sLevelToAdd.runAgent();
            
            //System.out.println("Param 2 now : " + sLevelToAdd.getParam2());
            
            //Skipping run agent for testing
            /*
            Random rand = new Random();
            sLevelToAdd.setFitness(rand.nextFloat());
            sLevelToAdd.setJumpEntropy(rand.nextFloat()/10);
            */
            
            System.out.println("Algo Run - Level Added to init pop: " + sLevelToAdd.toString());
            //System.out.println("ShinemAN - Level generated with widith " + sLevelToAdd.getWidth());
            outputlevels.add(sLevelToAdd.clone());      
        }
        
        return outputlevels;
        
    }
    
    //Select a population equal to generation size from an inputted archive
    public ArrayList<LevelWrap> tournamentSelect(ArrayList<LevelWrap> inputArchive){
        
        Random random = new Random(10);
        int iSize = inputArchive.size();
        
        ArrayList<LevelWrap> outputArchive = new ArrayList<>();
        
        while (outputArchive.size()<config.Generation_Size) {
            
            LevelWrap r1 = inputArchive.get(random.nextInt(iSize));
            LevelWrap r2 = inputArchive.get(random.nextInt(iSize));
            if (r1.getSelectionChance()>r2.getSelectionChance()) {
                outputArchive.add(r1);
            }
            else {
                outputArchive.add(r2);
            }
        }                           
        return outputArchive;   
    }

    
    public File[] getFiles(String filePath) {

        File[] outputFiles = null;
        File rootFolder = new File(filePath);

        if (rootFolder.isDirectory()) {

            outputFiles = rootFolder.listFiles();

        } else
            System.err.println("Given file path not valid");

        return outputFiles;
    }
    
    public char[][] readLevel(File fileLevel) {
        char[][] a = null;
        try {
            Scanner scanner = new Scanner(new FileInputStream(fileLevel));
            String line;
            ArrayList < String > lines = new ArrayList < > ();
            int width = 0;
            while (scanner.hasNext()) {
                line = scanner.nextLine();
                width = line.length();
                lines.add(line);
                //System.out.println(line);
            }
            a = new char[lines.size()][width];
            //System.out.println("Arrays length (i.e level height): " + a.length);
            for (int y = 0; y < lines.size(); y++) {
                //System.out.println("Processing line: " + lines.get(y));
                for (int x = 0; x < width; x++) {
                    a[y][x] = lines.get(y).charAt(x);
                }

            }
            //This is just for debugging, check that we have retried the same level
            /*
            for (int i = 0; i<a.length;i++) {
                System.out.println(Arrays.toString(a[i]));
            }
            */
            //System.out.println("Level length:  " + a[0].length + "Level height: " + a.length);
            
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }
    
    public void printArchive(ArrayList<LevelWrap> archive, String aName, boolean noisy) {
        System.out.println("Total " + aName + " archive size:" + archive.size());
        float totalfitness = 0;
        float totalselectionchance = 0;
        for (int i = 0; i < archive.size(); i++) {
            totalfitness+=archive.get(i).getFitness();
            totalselectionchance+=archive.get(i).getSelectionChance();

            if (noisy) {
                System.out.println("ShineTesting- In archive - Level with " + archive.get(i).toString());
            }
        }
        System.out.println( aName + " archive average fitness: " + (totalfitness/archive.size()) + ". Archive average selection chance: " + (totalselectionchance/archive.size())); 
    }
    
    public ElitesMap eval_CreateMap(ShineTree tree){
        
        ArrayList<LevelWrap> allLevels = tree.root.getAllChildLevels();
        
        return new ElitesMap(allLevels, config.mapSize, param1Min, param1Max, param2Min, param2Max);
        
        
    }
    
    public void mapOutput(ElitesMap sMap, ArrayList<String> runHistory, String dataFolder, long runStartT, boolean onlyFit) {
    	
    	//System.out.println("MapOutput running with imperfect levels flag: " + impefectLevels);
        
        HashMap<List<Integer>, LevelWrap> map = sMap.getMap();
        
        Path dataFolderPath = Paths.get(outputFolder+"\\"+dataFolder);
        
        //In case we havent updated the run name, we dont want to lose the data
        try {
            if (!Files.exists(dataFolderPath)) {
                //Random rand = new Random();
                //rootPath = Paths.get(Output_Location+runName+rand.nextInt(1000));
                //Files.createDirectory(rootPath);
                
                //System.out.println("Output folder does not exist when it should");
                Files.createDirectory(dataFolderPath);
                
            }
            /*else {
                Files.createDirectory(rootPath);
            }
            */
            
            //Create overall output 
            PrintWriter mapwriter = new PrintWriter((dataFolderPath + "/" +dataFolder+"-Data.txt"), "UTF-8");
            mapwriter.println("Data for run: " + dataFolder);
            mapwriter.println("Map Size " + config.mapSize);
            mapwriter.println("Map Coverage: " + sMap.getCoverage());
            mapwriter.println("Map Reliability: " + sMap.getReliability());
            mapwriter.println("Map Avg Fitness: " + sMap.getAvgFitness());
            mapwriter.println("Run Time (hrs): " + ((System.nanoTime()- runStartT)/(1000000000f*60f*60f)));
            mapwriter.println("");
            if (algoType == config.Algo_Shine) {
                mapwriter.println("SHINE tree parameters-");
                mapwriter.println("Max Vertex Reps: " + config.Max_Vertex_Reps);
                mapwriter.println("Max Tree Depth: " + config.Max_Tree_Depth);
                mapwriter.println("");
            }
            mapwriter.println("Parameter 1 min: " + param1Min);
            mapwriter.println("Parameter 1 max: " + param1Max);
            mapwriter.println("Parameter 2 min: " + param2Min);
            mapwriter.println("Parameter 2 max: " + param2Max);
            mapwriter.println("");
            mapwriter.println("Algorithm parameters-");
            mapwriter.println("Generation size: " + config.Generation_Size);
            mapwriter.println("Number of generations: " + Num_Generations);
            mapwriter.println("Chance of grow/shrink mutations: " + config.Dupe_Remove_Chance);
            mapwriter.println("Chance of tile flip mutations: " + config.Tile_Mutation_Chance);
            mapwriter.println("Chance of crossover: " + config.Crossover_Chance);
            mapwriter.println("");
            mapwriter.println("Algorithm ID: " + algoType);
            mapwriter.println("Configuration: " + runConfigType);
            mapwriter.close();
    
            //Create output files from each level in map
            sMap.createOutputFiles(dataFolderPath, dataFolder, onlyFit);
            
            //Create CSV output
            //PrintWriter historywriter = new PrintWriter((rootPath + "/" +runName+"-Full History.txt"), "UTF-8");
            FileWriter historywriter = new FileWriter((dataFolderPath + "/" +dataFolder+"-Full History.csv"));
            historywriter.append("Generation"); historywriter.append(","); historywriter.append("Coverage"); historywriter.append(",");historywriter.append("Reliability");historywriter.append(",");historywriter.append("Avg Fitness"); historywriter.append("\n");

            for (int i = 0; i < runHistory.size(); i++) {
                historywriter.append(runHistory.get(i));
                historywriter.append("\n");
            }
            
            historywriter.flush();
            historywriter.close();
        
        }
        catch (Exception e) {
            System.out.println("Failed to create output folder struct");
            System.out.println(e.getCause());
            
        }
    }
    
    public void levelsToFiles(List<LevelWrap> init_pop, String folder) throws IOException {
        
        Path rootPath = Paths.get(outputFolder+"/"+folder); 
        Files.createDirectory(rootPath);
        
        for (LevelWrap level : init_pop) {
            
            Path levelPath = Paths.get(rootPath + "/"+level.getName() +"/");
            Files.createDirectory(levelPath);   
            new ImageGen((level.getName()),levelPath,level.getCharRep());
            
            //Create level stats file
            PrintWriter lvlwriter = new PrintWriter((levelPath + "/" +level.getName()+"-Data.txt"), "UTF-8");
            lvlwriter.println("Level Name: " + level.getName());
            lvlwriter.println("Level Fitness: " + level.getFitness());
            lvlwriter.println("Level Width: " + level.getWidth());
            lvlwriter.println("Level Jump Entropy: " + level.getJumpEntropy());
            lvlwriter.println("Level Block Count: " + level.getBlockCount());
            lvlwriter.println("Level Contiguity Score: " + level.getContigScore());
            lvlwriter.println("Level Speed: " + level.getSpeed());
            lvlwriter.println("Level Time Taken: " + level.getTimeTaken());
            lvlwriter.close();
            
        }
        
    }
    
}
