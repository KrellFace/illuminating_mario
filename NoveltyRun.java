package illumsearch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


import illumsearch.genericFunc.*;

public class NoveltyRun extends AlgoRun{

	public NoveltyRun(List<LevelWrap> initPop, IllumConfig config) {
		super(initPop, config);
	}

	public void run() throws Exception {
        long runStartTime = System.nanoTime();
        
        List<LevelWrap> novelArchive = new ArrayList<LevelWrap>();
        int Gen_Count = 0;
        ArrayList<LevelWrap> currentPopulation = new ArrayList<LevelWrap>();

        ArrayList < String > runHistory = new ArrayList < String > ();

        //Set initial population as current pop
        currentPopulation.addAll(initPop);

        /*
        //Run until we reach max generations
        do {
            System.out.println("STARTING GENERATION " + Gen_Count);
            
            //SET NOVELTY SCORES

            //SELECTING PARENTS FOR NEXT GENERATION
            ArrayList < LevelWrap > selectedArchive = tournamentSelect(currentPopulation);
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
                	Path snapsshotPath = Paths.get(config.getRunPath()+"\\Generation "+ Gen_Count);
					currMap.createOutputFiles(snapsshotPath, config.getRunName(), true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }


        }
        while (Gen_Count < Num_Generations);


        //Create the output from the map
        ElitesMap endMap = new ElitesMap(tree.root.getAllChildLevels(), config.mapSize, param1Min, param1Max, param2Min, param2Max);
        mapOutput(endMap, runHistory, config.getRunName()+" - Final Data", runStartTime, false);
        */
	}
	
	public void setNovelty(ArrayList<LevelWrap> currentPop, ArrayList<LevelWrap> novelArchive) {
		
		
		
	}
	
	public void getNormalisedDistance(LevelWrap l1, LevelWrap l2) {
		//float normFactor = ((config.get))
	}

}
