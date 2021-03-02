package illumsearch.genericFunc;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import illumsearch.shine.*;
import illumsearch.mapElites.*;

public class ExperimentRun
{   
    private IllumConfig runConfig;
    
    public ExperimentRun(IllumConfig config) {
        
        this.runConfig = config;
        
        System.out.println(this.runConfig.toString());
        
    }
    
    public void run() {
        if (runConfig.getAlgoType() == runConfig.Algo_MapElites) {
            try {
				init_mapelites();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
        else if (runConfig.getAlgoType() == runConfig.Algo_ShineCD||runConfig.getAlgoType() == runConfig.Algo_ShineFit||runConfig.getAlgoType() == runConfig.Algo_ShineHybrid) {
            
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
                 
        //List<LevelWrap> init_pop = initPopFromFolder(Input_Files);
        List<LevelWrap> init_pop = initRandomPop(runConfig.initialSeed);
        System.out.println("Population fully initialised");
        //Store initial levels used
        new ShineRun(init_pop, runConfig).run();            
        
    }
    
    public void init_mapelites() throws Exception {
           
        //List<LevelWrap> init_pop = initPopFromFolder(Input_Files);     
        List<LevelWrap> init_pop = initRandomPop(runConfig.initialSeed); 
        new MapElitesRun(init_pop, runConfig).run();  
        
    }
    
    
    public ArrayList<LevelWrap> initRandomPop(int seed){
    	
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
            
            
            //System.out.println("Experiment Run - Level Added to init pop: " + sLevelToAdd.toString());
            outputlevels.add(sLevelToAdd.clone());      
        }
        
        return outputlevels;
        
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
    
}
