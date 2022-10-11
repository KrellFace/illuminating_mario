package illuminating_mario.metricExtraction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import illuminating_mario.mainFunc.AlgoType;
import illuminating_mario.mainFunc.BCType;
import illuminating_mario.mainFunc.ExperimentRun;
import illuminating_mario.mainFunc.IllumConfig;
import illuminating_mario.mainFunc.IllumLevelWrap;
import illuminating_mario.mainFunc.IllumMarioLevel;
import illuminating_mario.mainFunc.HelperMethods;
        
public class LevelMetricExtraction {
    
    
    public static void main(String[] args) {
        Path testOut = Paths.get("C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\output\\MetricExtraction\\");
        IllumConfig cnfig =  new IllumConfig(AlgoType.MapElites, 1000, BCType.AgrSmooth, BCType.BlockCount, testOut, "MetricExtraction1");
        String testFolder = "C:\\Users\\owith\\Documents\\PhD Work\\IllumMario Platform\\levels\\ge\\";
        
        File[] files = HelperMethods.getFiles(testFolder);

        //System.out.print(files);

        for (int i = 0; i<files.length; i++){
            char[][] cr = HelperMethods.fileToCharRep(files[i]);
            String s = HelperMethods.stringRepFromCharRep(cr);
            IllumMarioLevel lvl = new IllumMarioLevel(s, false);
            LvlMetricWrap lvlwrp = new LvlMetricWrap(s, cnfig, lvl);
            lvlwrp.runAgent();
            //System.out.println(lvlwrp.toString());
            lvlwrp.printSummary();
        }
    }

    /* 
    private static File[] getFiles(String filePath) {

        File[] outputFiles = null;
        File rootFolder = new File(filePath);
        System.out.println("RF:" + rootFolder);

        if (rootFolder.isDirectory()) {
            outputFiles = rootFolder.listFiles();
        } else
            System.err.println("Given file path not valid");
        
        return outputFiles;
    }

    private static char[][] fileToCharRep(File fileLevel) {
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
            }
            a = new char[lines.size()][width];
            for (int y = 0; y < lines.size(); y++) {
                for (int x = 0; x < width; x++) {
                    a[y][x] = lines.get(y).charAt(x);
                }
            }          
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }   

    public static String stringRepFromCharRep(char[][] levelRep) {

        String output = "";

        char nulls = '\u0000';

        for (int y = 0; y < levelRep.length; y++) {
            for (int x = 0; x < levelRep[y].length; x++) {

                //Clunky handling for blank cells
                if (levelRep[y][x] == nulls) {
                    output += "-";
                } else {
                    output += String.valueOf(levelRep[y][x]);
                }
                if (x == levelRep[y].length - 1) {
                    output += "\n";
                }
            }

        }
        return output;

    }

    public static char[][] charRepFromString(String stringRep) {

        String[] lines = stringRep.split("\n");

        char[][] charRep = new char[lines.length][lines[0].length()];

        for (int y = 0; y < charRep.length; y++) {
            for (int x = 0; x < charRep[y].length; x++) {

                Character c = lines[y].charAt(x);
                charRep[y][x] = c;

            }
        }
        return charRep;
    */

}
        