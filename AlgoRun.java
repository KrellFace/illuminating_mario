package illumsearch;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AlgoRun {

    protected List < LevelWrap > initPop;
    protected IllumConfig config;

    protected int Num_Generations;

    /*
    //Storage for the parameters used in current run
    protected static float param1Min;
    protected static float param1Max;
    protected static float param2Min;
    protected static float param2Max;
	*/
	
    public AlgoRun(List < LevelWrap > initPop, IllumConfig config) {
        this.initPop = initPop;
        this.config = config;
        this.Num_Generations = (config.getNumOffspring() / config.Generation_Size);
        //this.setParams();

    }

    public abstract void run() throws Exception;

    public ArrayList < LevelWrap > tournamentSelect(ArrayList < LevelWrap > inputArchive) {

        Random random = new Random(10);
        int iSize = inputArchive.size();

        ArrayList < LevelWrap > outputArchive = new ArrayList < > ();

        while (outputArchive.size() < config.Generation_Size) {

            LevelWrap r1 = inputArchive.get(random.nextInt(iSize));
            LevelWrap r2 = inputArchive.get(random.nextInt(iSize));
            if (r1.getSelectionChance() > r2.getSelectionChance()) {
                outputArchive.add(r1);
            } else {
                outputArchive.add(r2);
            }
        }
        return outputArchive;
    }

    public void levelsToFiles(List < LevelWrap > init_pop, String folder) throws Exception {

        Path rootPath = Paths.get(config.getRunPath() + "/" + folder);
        Files.createDirectory(rootPath);

        for (LevelWrap level: init_pop) {
            Path levelFolder = Paths.get(rootPath + "/" + level.getName());
            level.createLevelFiles(levelFolder);
        }

    }

    public ArrayList < LevelWrap > archiveOffspring(ArrayList < LevelWrap > inputArchive) {

        ArrayList < LevelWrap > outputArchive = new ArrayList < LevelWrap > ();

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

            if (random.nextFloat() < config.Crossover_Chance) {
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
        for (int y = 5; y < inputLevel.getWidth() - 1; y++) {

            if (random.nextFloat() < config.Dupe_Remove_Chance) {
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
                if (random.nextFloat() < config.Tile_Mutation_Chance) {
                    inputLevel.mutate_blockUnblockCell(x, y);
                }
            }
        }
    }
    
public void mapOutput(ElitesMap sMap, ArrayList<String> runHistory, String dataFolder, long runStartT, boolean onlyFit) {
    	
    	//System.out.println("MapOutput running with imperfect levels flag: " + impefectLevels);
 
        Path dataFolderPath = Paths.get(config.getRunPath()+"\\"+dataFolder);
        
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
            mapwriter.println("Map Size " + config.mapSize);
            mapwriter.println("Map Coverage: " + sMap.getCoverage());
            mapwriter.println("Map Reliability: " + sMap.getReliability());
            mapwriter.println("Map Avg Fitness: " + sMap.getAvgFitness());
            mapwriter.println("Run Time (hrs): " + ((System.nanoTime()- runStartT)/(1000000000f*60f*60f)));
            mapwriter.println("");
            if (config.getAlgoType() == config.Algo_ShineCD) {
                mapwriter.println("SHINE tree parameters-");
                mapwriter.println("Max Vertex Reps: " + config.Max_Vertex_Reps);
                mapwriter.println("Max Tree Depth: " + config.Max_Tree_Depth);
                mapwriter.println("");
            }
            mapwriter.println("Parameter 1 min: " + config.getParam1Min());
            mapwriter.println("Parameter 1 max: " + config.getParam1Max());
            mapwriter.println("Parameter 2 min: " + config.getParam2Min());
            mapwriter.println("Parameter 2 max: " + config.getParam2Max());
            mapwriter.println("");
            mapwriter.println("Algorithm parameters-");
            mapwriter.println("Generation size: " + config.Generation_Size);
            mapwriter.println("Number of generations: " + Num_Generations);
            mapwriter.println("Chance of grow/shrink mutations: " + config.Dupe_Remove_Chance);
            mapwriter.println("Chance of tile flip mutations: " + config.Tile_Mutation_Chance);
            mapwriter.println("Chance of crossover: " + config.Crossover_Chance);
            mapwriter.println("");
            mapwriter.println("Algorithm ID: " + config.getAlgoType());
            mapwriter.println("Configuration param1: " + config.getParam1() + " param 2: " + config.getParam2());
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

	

}