package illumsearch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ShineRun extends AlgoRun {

    public ShineRun(List < LevelWrap > initPop, int param1, int param2, int numOffspring, IllumConfig config, Path outputFolder, String runName) {
        super(initPop,param1, param2, numOffspring, config,outputFolder, runName);

    }

    public ShineTree run()  {
        long runStartTime = System.nanoTime();
        //System.out.println("Config:" +config);
        ShineTree tree = new ShineTree(config.Max_Tree_Depth, config.Max_Vertex_Reps, param1Min, param1Max, param2Min, param2Max);
        int Gen_Count = 0;

        ArrayList < String > runHistory = new ArrayList < String > ();

        //Add levels to our tree
        for (int i = 0; i < initialPop.size(); i++) {
            tree.root.addLevel(initialPop.get(i));

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
            selectedArchive = archiveOffspring(selectedArchive);
            //printArchive(selectedArchive, "Selected Parents after mutation:", true);

            //ADDING CHILDREN TO TREE
            for (int i = 0; i < selectedArchive.size(); i++) {

                LevelWrap currChild = selectedArchive.get(i);
                currChild.runAgent();
                tree.root.addLevel(currChild.clone());
            }
            Gen_Count += 1;

            //ElitesMap currMap = eval_CreateMap(tree);
            ElitesMap currMap = new ElitesMap(tree.root.getAllChildLevels(), config.mapSize, param1Min, param1Max, param2Min, param2Max);
            System.out.println(currMap.toString());
            runHistory.add(Gen_Count + ", " + currMap.getCoverage() + ", " + currMap.getReliability() + ", " + currMap.getAvgFitness());
            if (Gen_Count % 10 == 0) {
                //mapOutput(currMap, runHistory, runName+" - Generation "+Gen_Count, runStartTime, true);
                try {
                	Path snapsshotPath = Paths.get(outputFolder+"\\Generation "+ Gen_Count);
					currMap.createOutputFiles(snapsshotPath, runName, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }


        }
        while (Gen_Count < Num_Generations);


        //Create the output from the map
        ElitesMap endMap = new ElitesMap(tree.root.getAllChildLevels(), config.mapSize, param1Min, param1Max, param2Min, param2Max);
        mapOutput(endMap, runHistory, runName+" - Final Data", runStartTime, false);


        return null;

    }

}