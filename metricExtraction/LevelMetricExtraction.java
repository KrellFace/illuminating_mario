package illuminating_mario.metricExtraction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.*;
import java.io.FileInputStream;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.*;

import illuminating_mario.mainFunc.AlgoType;
import illuminating_mario.mainFunc.BCType;
import illuminating_mario.mainFunc.IllumConfig;
import illuminating_mario.mainFunc.IllumMarioLevel;
import illuminating_mario.mainFunc.HelperMethods;
        
public class LevelMetricExtraction {
    
    
    public static void main(String[] args)  throws IOException{
        Path testOut = Paths.get("C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\output\\MetricExtraction\\");
        IllumConfig cnfig =  new IllumConfig(AlgoType.MapElites, 1000, BCType.AgrSmooth, BCType.BlockCount, testOut, "MetricExtraction1");
        String testFolder = "C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\levels\\ge\\";


        List<String[]> outputData = new ArrayList<String[]>();
        outputData.add(new String[] {"LevelName", "Generator", "Contiguity","AdjustedContiguity","EnemyCount","Linearity","Density","EmptySpace","BlockCount","ClearRows","Playability","JumpCount","JumpEntropy","Speed","TimeTaken"});
        
        File[] files = HelperMethods.getFiles(testFolder);

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

            outputData.add(new String[] {lvlwrp.getName(), "GenName",String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.Contiguity)),String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.AdjustedContiguity)),
                String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.EnemyCount)),String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.Linearity)),
                String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.Density)),String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.EmptySpace)),
                String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.BlockCount)),String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.ClearRows)),
                String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.Playability)),String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.JumpCount)),
                String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.JumpEntropy)),
                String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.Speed)),String.valueOf(lvlwrp.GetMetricValue(enum_MarioMetrics.TimeTaken))});

        }

        //createCSV("C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\output\\MetricExtraction\\testout.csv", outputData);

        File outCSV = new File("C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\output\\MetricExtraction\\testout.csv");
        FileWriter fileWriter = new FileWriter(outCSV);
        for (int i = 0; i<outputData.size(); i++){
            StringBuilder line = new StringBuilder();
            String[] row = outputData.get(i);
            for (int j = 0; j<row.length;j++){
                line.append("\"");
                line.append(row[j].replaceAll("\"","\"\""));
                line.append("\"");
                if (i != row.length - 1) {
                    line.append(',');
                }
            }
            line.append("\n");
            fileWriter.write(line.toString());

        }
        fileWriter.close();


    }
}
        