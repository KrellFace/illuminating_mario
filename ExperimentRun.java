package illumsearch;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ExperimentRun
{   
	//SHINE Variables
    private int Num_Generations;
    //MAP Elites Variables (Each iteration = new new offspring due to crossover operation)
    private int Num_Iterations;
    
    //Storage for the parameters used in current run
    private static float param1Min;
    private static float param1Max;
    private static float param2Min;
    private static float param2Max;
    
    private IllumConfig runConfig;
    
    public ExperimentRun(IllumConfig config) {
        
        this.runConfig = config;
        this.Num_Iterations = (config.getNumOffspring()/2);
        this.Num_Generations = (config.getNumOffspring()/config.Generation_Size);
        
    }
    
    public void run() {
        if (runConfig.getAlgoType() == runConfig.Algo_MapElites) {
            System.out.println("Starting map elites run with param1: " + runConfig.getParam1() + " and param2 " + runConfig.getParam2() + " running for " + Num_Iterations + " iterations");

            try {
				init_mapelites();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
        else if (runConfig.getAlgoType() == runConfig.Algo_Shine) {
            
            try {
				init_shine();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else {
            System.out.println("Unknown algorithm inputted");
        }
    }

    
    public void init_shine() throws Exception {
        
        long runStartTime = System.nanoTime();
                  
        //List<LevelWrap> init_pop = initPopFromFolder(Input_Files);
        List<LevelWrap> init_pop = initRandomPop(runConfig.getParam1(), runConfig.getParam2(), runConfig.initialSeed);
        System.out.println("Population fully initialised");
        //Store initial levels used
        try {
            levelsToFiles(init_pop, "Initial Population" );
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        new ShineRun(init_pop, runConfig).run();
  
        //Create the output from the map
        //mapOutput(outputTree, runHistory, runName+" - Final Data", runStartTime, false);                     
        
    }
    
    public void init_mapelites() throws Exception {
        
        long runStartTime = System.nanoTime();     
        //List<LevelWrap> init_pop = initPopFromFolder(Input_Files);
        
        List<LevelWrap> init_pop = initRandomPop(runConfig.getParam1(), runConfig.getParam2(), runConfig.initialSeed);
        
        new MapElitesRun(init_pop, runConfig).run();
        
        //Create the output from the map
        //mapOutput(map, runHistory, runConfig.getRunName(), runStartTime, true);          
        
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
            
            if (random.nextFloat()<runConfig.Crossover_Chance) {
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
        
            if (random.nextFloat()<runConfig.Dupe_Remove_Chance) {
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
                if (random.nextFloat()<runConfig.Tile_Mutation_Chance){
                    inputLevel.mutate_blockUnblockCell(x, y);
                }
            }
        }
    }
    
    
    public ArrayList<LevelWrap> initRandomPop(int param1, int param2, int seed){
    	
    	System.out.println("Init random pop started");
        
        ArrayList<LevelWrap> outputlevels = new ArrayList<>();
        
        //Initialise the random number generator from seed that will be used to create all levels
        Random fixedRandom = new Random(seed);
                
        for (int i = 0; i<runConfig.Generation_Size; i++) {
            
            //IllumMarioLevel levelToAdd = genRandLevel(fixed_width, fixedRandom);
            LevelWrap sLevelToAdd = new LevelWrap(("Level " + i),this.runConfig, fixedRandom);
            //System.out.println("Run agent about to be run in AR");
            sLevelToAdd.runAgent();
            
            //System.out.println("Param 2 now : " + sLevelToAdd.getParam2());
            
            
            System.out.println("Algo Run - Level Added to init pop: " + sLevelToAdd.toString());
            outputlevels.add(sLevelToAdd.clone());      
        }
        
        return outputlevels;
        
    }
    
    //Select a population equal to generation size from an inputted archive
    public ArrayList<LevelWrap> tournamentSelect(ArrayList<LevelWrap> inputArchive){
        
        Random random = new Random(10);
        int iSize = inputArchive.size();
        
        ArrayList<LevelWrap> outputArchive = new ArrayList<>();
        
        while (outputArchive.size()<runConfig.Generation_Size) {
            
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
        
        return new ElitesMap(allLevels, runConfig.mapSize, param1Min, param1Max, param2Min, param2Max);
        
        
    }
    
    public void mapOutput(ElitesMap sMap, ArrayList<String> runHistory, String dataFolder, long runStartT, boolean onlyFit) {
   
        Path dataFolderPath = Paths.get(runConfig.getRunPath()+"\\"+dataFolder);
        
        //In case we havent updated the run name, we dont want to lose the data
        try {
            if (!Files.exists(dataFolderPath)) {
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
            mapwriter.println("Map Size " + runConfig.mapSize);
            mapwriter.println("Map Coverage: " + sMap.getCoverage());
            mapwriter.println("Map Reliability: " + sMap.getReliability());
            mapwriter.println("Map Avg Fitness: " + sMap.getAvgFitness());
            mapwriter.println("Run Time (hrs): " + ((System.nanoTime()- runStartT)/(1000000000f*60f*60f)));
            mapwriter.println("");
            if (runConfig.getAlgoType() == runConfig.Algo_Shine) {
                mapwriter.println("SHINE tree parameters-");
                mapwriter.println("Max Vertex Reps: " + runConfig.Max_Vertex_Reps);
                mapwriter.println("Max Tree Depth: " + runConfig.Max_Tree_Depth);
                mapwriter.println("");
            }
            mapwriter.println("Parameter 1 min: " + param1Min);
            mapwriter.println("Parameter 1 max: " + param1Max);
            mapwriter.println("Parameter 2 min: " + param2Min);
            mapwriter.println("Parameter 2 max: " + param2Max);
            mapwriter.println("");
            mapwriter.println("Algorithm parameters-");
            mapwriter.println("Generation size: " + runConfig.Generation_Size);
            mapwriter.println("Number of generations: " + Num_Generations);
            mapwriter.println("Chance of grow/shrink mutations: " + runConfig.Dupe_Remove_Chance);
            mapwriter.println("Chance of tile flip mutations: " + runConfig.Tile_Mutation_Chance);
            mapwriter.println("Chance of crossover: " + runConfig.Crossover_Chance);
            mapwriter.println("");
            mapwriter.println("Algorithm ID: " + runConfig.getAlgoType());
            mapwriter.println("Configuration param1: " + runConfig.getParam1() + " param 2: " + runConfig.getParam2());
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
    
    public void levelsToFiles(List<LevelWrap> init_pop, String folder) throws Exception {
        
        Path rootPath = Paths.get(runConfig.getRunPath()+"/"+folder);  
        Files.createDirectory(rootPath);
        
        for (LevelWrap level : init_pop) {
        	Path levelFolder = Paths.get(rootPath + "/" + level.getName());
        	level.createLevelFiles(levelFolder);
        }
        
    }
    
}
