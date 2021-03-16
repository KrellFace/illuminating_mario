package illumsearch.genericFunc;

import java.util.Random;

public class GeneticOperators{


    //Function for crossing over two levels 
    public LevelWrap[] crossover(LevelWrap inputLevel1, LevelWrap inputLevel2, IllumConfig config) {

        char[][] l1LevelRep = charRep(inputLevel1.getLevel().getStringRep());
        char[][] l2LevelRep = charRep(inputLevel2.getLevel().getStringRep());

        LevelWrap[] output = new LevelWrap[2];
   
        if (l1LevelRep[0].length == l2LevelRep[0].length && l1LevelRep.length == l2LevelRep.length){

            int width = l1LevelRep[0].length;
            int height = l1LevelRep.length; 

            //Initialise a copy of input level 1 (caller)
            char[][] output1 = new char[height][width];
            for (int i = 0; i < output1.length; i++) {
                output1[i] = l1LevelRep[i].clone();
            }

            //Initialise a copy of input level 2 (input)
            char[][] output2 = new char[height][width];
            for (int i = 0; i < output2.length; i++) {
                output2[i] = l2LevelRep[i].clone();
            }

            Random random = new Random();

            //Generate our two crossover points
            int point1 = random.nextInt(width / 2);
            int point2 = random.nextInt(width / 2) + width / 2;

            System.out.println("Crossing over between point: " + point1 +"/"+point2 + " on levels with width " + output1[0].length + " and " + output1[1].length);

            for (int y = 0; y < height; y++) {

                for (int x = 0; x < width; x++) {

                    //Only replace cells if we are between the two crossover points
                    if (point1 < x && x < point2) {
                        output1[y][x] = l2LevelRep[y][x];
                        output2[y][x] = l1LevelRep[y][x];
                    }
                }
            }

            output[0] = new LevelWrap("Output1", config, new IllumMarioLevel(stringRep(output1), true));
            output[1] = new LevelWrap("Output2", config, new IllumMarioLevel(stringRep(output2), true));
        }
        else{
            System.out.println("Level sizes for crossover did not match, returning original levels");

            output[0] = inputLevel1;
            output[1] = inputLevel2;
        }

        return output;
    }
    
    public char[][] charRep(String stringRep) {

        String[] lines = stringRep.split("\n");

        char[][] charRep = new char[lines.length][lines[0].length()];

        for (int y = 0; y < charRep.length; y++) {
            for (int x = 0; x < charRep[y].length; x++) {

                Character c = lines[y].charAt(x);
                charRep[y][x] = c;

            }
        }
        return charRep;
    }

    public String stringRep(char[][] levelRep) {

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
}