package illuminating_mario.shine;

import java.util.ArrayList;
import java.util.List;

import illuminating_mario.genericFunc.*;
import illuminating_mario.mapElites.ElitesMap;

public class ShineRun extends AlgoRunManager {

    public ShineRun(List < LevelWrap > initPop, IllumConfig config) {
        super(initPop,config);

    }

    public void run()  {
    	
        long runStartTime = System.nanoTime();
        
        try {
            levelsToFiles(initPop, "Initial Population" );
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Config:" +config);
        ShineTree tree = new ShineTree(config.Max_Tree_Depth, config.Max_Vertex_Reps, config.getParam1Min(), config.getParam1Max(), config.getParam2Min(), config.getParam2Max());
        int Gen_Count = 0;

        ArrayList < String > runHistory = new ArrayList < String > ();

        //Add levels to our tree
        for (int i = 0; i < initPop.size(); i++) {
            tree.root.addLevel(initPop.get(i));

        }

        //Run until we reach max generations
        do {
            System.out.println("STARTING GENERATION " + Gen_Count);

            //tree.root.printLeaves(true);  
            System.out.println("Total leaf nodes: " + tree.root.countLeaves());

            //CREATING ARCHIVE FROM TREE (i.e limit reps at each vertex)
            ArrayList < LevelWrap > archive = tree.createArchive();
            //printArchive(archive, "Depth weight selected", false);

            //SELECTING PARENTS FOR NEXT GENERATION
            ArrayList < LevelWrap > selectedArchive = tournamentSelect(archive);
            
            //printArchive(selectedArchive, "Selected Parents before mutation:", true);

            //MUTATING SELECTED PARENTS
            selectedArchive = xOverAndMutate(selectedArchive);
            //printArchive(selectedArchive, "Selected Parents after mutation:", true);

            //ADDING CHILDREN TO TREE
            for (int i = 0; i < selectedArchive.size(); i++) {

                LevelWrap currChild = selectedArchive.get(i);
                currChild.runAgent();
                tree.root.addLevel(currChild.clone());
            }
            Gen_Count += 1;

            //ElitesMap currMap = eval_CreateMap(tree);
            ElitesMap currMap = new ElitesMap(tree.root.getAllChildLevels(), config.mapSize,config.getParam1Min(), config.getParam1Max(), config.getParam2Min(), config.getParam2Max());
            System.out.println(currMap.toString());
            runHistory.add(Gen_Count + ", " + currMap.getCoverage() + ", " + currMap.getReliability() + ", " + currMap.getAvgFitness());
            if (Gen_Count % 5 == 0) {
                mapOutput(currMap, runHistory, config.getRunName()+" - Generation "+Gen_Count, runStartTime, true);
            }


        }
        while (Gen_Count < Num_Generations);


        //Create the output from the map
        ElitesMap endMap = new ElitesMap(tree.root.getAllChildLevels(), config.mapSize, config.getParam1Min(), config.getParam1Max(), config.getParam2Min(), config.getParam2Max());
        mapOutput(endMap, runHistory, config.getRunName()+" - Final Data", runStartTime, true);

    }

}