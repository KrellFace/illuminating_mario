package illumsearch.mapElites;

import java.util.ArrayList;
import java.util.List;

import illumsearch.genericFunc.*;

public class MapElitesRun extends AlgoRunManager{

	public MapElitesRun(List<LevelWrap> initPop, IllumConfig config) {
		super(initPop, config);
	}

	@Override
	public void run() throws Exception {
        
		long runStartTime = System.nanoTime();
		
        try {
            levelsToFiles(initPop, "Initial Population" );
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        int iterCount = 0;
        
        ArrayList<String> runHistory = new ArrayList<String>();
        
        ElitesMap map = new ElitesMap(config.mapSize,  config.getParam1Min(), config.getParam1Max(), config.getParam2Min(), config.getParam2Max());

        for (int i = 0; i< initPop.size(); i++) {
            map.addLevel(initPop.get(i));          
        }
        
        //Run until we reach max iterations
        do {

            //System.out.println("Beginning iteration " + iterCount);
                
            ArrayList<LevelWrap> levelPair = new ArrayList<LevelWrap>();
            
            //Select parent pair for this generation
            levelPair.add(map.getRandomLevel().clone());
            levelPair.add(map.getRandomLevel().clone());
            
            //Generate offspring from parents
            levelPair = xOverAndMutate(levelPair);
            
            //Add the level pair back into the map
            for (int i = 0; i < levelPair.size(); i++) {
                levelPair.get(i).runAgent();
                map.addLevel(levelPair.get(i));
            }

            
            
            //Add to history 
            if (iterCount%10==0) {
                System.out.println("ITERATION " + iterCount);

                System.out.println(map.toString());         
                runHistory.add(iterCount +", " + map.getCoverage() + ", " + map.getReliability() + ", " + map.getAvgFitness());
                
                if (iterCount%10==0) {
                    mapOutput(map, runHistory, config.getRunName()+" - Snapshot"+iterCount, runStartTime, true);
                }
            }
            System.out.println("ITERATION " + iterCount);
            iterCount+=1;
    
        }
        while (iterCount<(config.getNumOffspring()/2));
        
        //Create the output from the map
        mapOutput(map, runHistory, config.getRunName()+" - Snapshot"+iterCount, runStartTime, true);
	}

}
