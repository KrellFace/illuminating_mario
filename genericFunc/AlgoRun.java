package illumsearch.genericFunc;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import illumsearch.mapElites.*;

public abstract class AlgoRun {

    protected List < LevelWrap > initPop;
    protected IllumConfig config;

    protected int Num_Generations;

    public AlgoRun(List < LevelWrap > initPop, IllumConfig config) {
        this.initPop = initPop;
        this.config = config;
        this.Num_Generations = (config.getNumOffspring() / config.Generation_Size);
    }

    public abstract void run() throws Exception;


    public void levelsToFiles(List < LevelWrap > init_pop, String folder) throws Exception {

        Path rootPath = Paths.get(config.getRunPath() + "/" + folder);
        Files.createDirectory(rootPath);

        for (LevelWrap level: init_pop) {
            Path levelFolder = Paths.get(rootPath + "/" + level.getName());
            level.createLevelFiles(levelFolder);
        }

    }

    public ArrayList < LevelWrap > xOverAndMutate(ArrayList < LevelWrap > inputArchive) {

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
            }

            mutate_dupeRemove(selectedPair[0]);
            mutate_dupeRemove(selectedPair[1]);
            mutate_tileflip(selectedPair[0]);
            mutate_tileflip(selectedPair[1]);

            outputArchive.add(selectedPair[0]);
            outputArchive.add(selectedPair[1]);
        }


        return outputArchive;
    }
    //Runs the Duplicate + Remove a column method for every column if odds achieved
    private void mutate_dupeRemove(LevelWrap inputLevel) {
        Random random = new Random();   
        for (int x = 5; x < inputLevel.getWidth() - 1; x++) {

            if (random.nextFloat() < config.Dupe_Remove_Chance) {
                inputLevel.mutate_removeColumn();
                inputLevel.mutate_dupeColInPlace(x);
            }
        }
    }

    //Runs the Tile Flip mutation method for every tile in a level if odds achieved
    private void mutate_tileflip(LevelWrap inputLevel) {
        Random random = new Random();       
        //y=1 to avoid tampering with level ceiling
        for (int y = 1; y < inputLevel.getHeight(); y++) {
            for (int x = 5; x < inputLevel.getWidth(); x++) {
                if (random.nextFloat() < config.Tile_Mutation_Chance) {
                    inputLevel.mutate_blockUnblockCell(x, y);                 
                }
            }
        }
    }

    protected void mapOutput(ElitesMap sMap, ArrayList < String > runHistory, String dataFolder, long runStartT, boolean onlyFit) {

        //System.out.println("MapOutput running with imperfect levels flag: " + impefectLevels);

        Path dataFolderPath = Paths.get(config.getRunPath() + "\\" + dataFolder);

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
            PrintWriter mapwriter = new PrintWriter((dataFolderPath + "/" + dataFolder + "-Data.txt"), "UTF-8");
            mapwriter.println("Data for run: " + dataFolder);
            mapwriter.println("Map Size " + config.mapSize);
            mapwriter.println("Map Coverage: " + sMap.getCoverage());
            mapwriter.println("Map Reliability: " + sMap.getReliability());
            mapwriter.println("Map Avg Fitness: " + sMap.getAvgFitness());
            mapwriter.println("Run Time (hrs): " + ((System.nanoTime() - runStartT) / (1000000000f * 60f * 60f)));
            mapwriter.println("");
            if (config.getAlgoType() != config.Algo_MapElites) {
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
            FileWriter historywriter = new FileWriter((dataFolderPath + "/" + dataFolder + "-Full History.csv"));
            historywriter.append("Generation");
            historywriter.append(",");
            historywriter.append("Coverage");
            historywriter.append(",");
            historywriter.append("Reliability");
            historywriter.append(",");
            historywriter.append("Avg Fitness");
            historywriter.append("\n");

            for (int i = 0; i < runHistory.size(); i++) {
                historywriter.append(runHistory.get(i));
                historywriter.append("\n");
            }

            historywriter.flush();
            historywriter.close();

        } catch (Exception e) {
            System.out.println("Failed to create output folder struct");
            System.out.println(e.getCause());

        }
    }

    protected void printArchive(ArrayList < LevelWrap > archive, String aName, boolean noisy) {
        System.out.println("Total " + aName + " archive size:" + archive.size());
        float totalfitness = 0;
        float totalselectionchance = 0;
        for (int i = 0; i < archive.size(); i++) {
            totalfitness += archive.get(i).getFitness();
            totalselectionchance += archive.get(i).getSelectionChance();

            if (noisy) {
                System.out.println("ShineTesting- In archive - Level with " + archive.get(i).toString());
            }
        }
        System.out.println(aName + " archive average fitness: " + (totalfitness / archive.size()) + ". Archive average selection chance: " + (totalselectionchance / archive.size()));
    }



}