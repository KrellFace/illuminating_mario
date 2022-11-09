package illuminating_mario.metricExtraction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.*;
import java.util.List;
import java.util.EnumSet;

import illuminating_mario.mainFunc.AlgoType;
import illuminating_mario.mainFunc.BCType;
import illuminating_mario.mainFunc.IllumConfig;
import illuminating_mario.mainFunc.IllumMarioLevel;
import illuminating_mario.mainFunc.HelperMethods;
        
public class LevelMetricExtraction {

    static String outputFileName = "C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\output\\MetricExtraction\\TEST_10LevelsAllBCS_EnumSet3.csv";

    /* 
    static enum_MarioMetrics[] metricsToExtract = new enum_MarioMetrics[]{enum_MarioMetrics.Contiguity,enum_MarioMetrics.AdjustedContiguity,
        enum_MarioMetrics.EnemyCount,enum_MarioMetrics.Linearity,enum_MarioMetrics.Density,enum_MarioMetrics.EmptySpace,enum_MarioMetrics.BlockCount,enum_MarioMetrics.ClearRows,
        enum_MarioMetrics.Playability,enum_MarioMetrics.JumpCount,enum_MarioMetrics.JumpCountByPlayability,enum_MarioMetrics.JumpEntropy,enum_MarioMetrics.Speed,enum_MarioMetrics.TimeTaken,
        enum_MarioMetrics.EnemyCount,enum_MarioMetrics.RewardCount, enum_MarioMetrics.ClearColumns, enum_MarioMetrics.TotalKills, enum_MarioMetrics.KillsOverEnemies,
        enum_MarioMetrics.KillsByStomp, enum_MarioMetrics.MaxJumpAirTime};
    */

    
    public static void main(String[] args)  throws IOException{
        Path testOut = Paths.get("C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\output\\MetricExtraction\\");
        IllumConfig cnfig =  new IllumConfig(AlgoType.MapElites, 1000, BCType.AgrSmooth, BCType.BlockCount, testOut, "MetricExtraction1");
        //String testFolder = "C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\levels\\ge\\";

        File[] generatorFolders = new File("C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\levels\\").listFiles(File::isDirectory);

        List<String[]> outputData = new ArrayList<String[]>();

        EnumSet<enum_MarioMetrics> metricsToExtract = EnumSet.allOf(enum_MarioMetrics.class);

        System.out.println("Metrics2Extract: " + metricsToExtract);

        //Generate the header of the output csv
        //String[] firstLine = new String[metricsToExtract.length+2];
        String[] firstLine = new String[metricsToExtract.size()+2];
        firstLine[0] = "LevelName"; firstLine[1] = "Generator";  
        /* 
        for (int i = 0; i<metricsToExtract.size(); i++){
            firstLine[i+2] = metricsToExtract[i].name();
        } outputData.add(firstLine);
        */
        int iter = 0;
        for(enum_MarioMetrics metric: metricsToExtract){
            firstLine[iter+2] = metric.name();
            iter+=1;
        }
        outputData.add(firstLine);

        for (int q = 0; q<generatorFolders.length; q++){
            //File[] files = HelperMethods.getFilesFromString(generatorFolders[j]);
            File[] files = generatorFolders[q].listFiles();
            System.out.println("Processing generator: " + generatorFolders[q].getName());

            //System.out.print(files);
            //for (int i = 0; i<files.length; i++){
            for (int i = 0; i<10; i++){  
                char[][] cr = HelperMethods.fileToCharRep(files[i]);
                String s = HelperMethods.stringRepFromCharRep(cr);
                IllumMarioLevel lvl = new IllumMarioLevel(s, false);
                LvlMetricWrap lvlwrp = new LvlMetricWrap(files[i].getName(), cnfig, lvl);
                lvlwrp.runAgentNTimes((int)5);
                //System.out.println(lvlwrp.toString());
                //System.out.println(HelperMethods.stringRepFromCharRep(lvlwrp.getCharRep()));
                //lvlwrp.printSummary();

                //String[] levelLine = new String[metricsToExtract.length+2];
                String[] levelLine = new String[metricsToExtract.size()+2];
                levelLine[0] = lvlwrp.getName(); levelLine[1] = generatorFolders[q].getName();  
                
                /* 
                for (int j = 0; j<metricsToExtract.length; j++){    
                    levelLine[j+2] = String.valueOf(lvlwrp.GetMetricValue(metricsToExtract[j]));
                */
                int subIter = 0;
                for(enum_MarioMetrics metric: metricsToExtract){
                    levelLine[subIter+2] = String.valueOf(lvlwrp.GetMetricValue(metric));
                    subIter+=1;
                }

                outputData.add(levelLine);
            }
    
            //createCSV("C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\output\\MetricExtraction\\testout.csv", outputData);
    
            File outCSV = new File(outputFileName);
            FileWriter fileWriter = new FileWriter(outCSV);
            for (int i = 0; i<outputData.size(); i++){
                StringBuilder line = new StringBuilder();
                String[] row = outputData.get(i);
                //System.out.println("Next row: " + row);
                for (int j = 0; j<row.length;j++){
                    line.append("\"");
                    line.append(row[j].replaceAll("\"","\"\""));
                    line.append("\"");
                    
                    //if (i != row.length - 1) {
                    line.append(',');
                    //}
                    
                }
                line.append("\n");
                fileWriter.write(line.toString());
    
            }
            fileWriter.close();
        }



        



    }
}
        