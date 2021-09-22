package illuminating_mario.genericFunc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/* Generates png and jpeg representations of a mario level from its associated .txt file */
public class ImageGen {

    public String fileName;
    public char[][] levelRep;
    public Path outputDir;

    public ImageGen(String fileName, Path outputDir, char[][] levelRep) {
    	//System.out.println("ImageGen initiated");
        this.fileName = fileName;
        this.levelRep = levelRep;
        this.outputDir = outputDir;

        try {
			genImage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void genImage() throws IOException {

        //Default directory to create level representations set here:
        //String outputDir = "C:\\Users\\Ollie\\Documents\\MSc Studying\\Project\\Level Representations\\";

        int height = levelRep.length;
        int width = levelRep[0].length;

        //Set block size in pixels
        int blockSize = 10;
        
        //Debug
        //System.out.println("Level height: " + height);

        //Debug
        //System.out.println("Level width: " + width);

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage((width * blockSize), (height * blockSize), BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        //Paint the full image light blue
        g2d.setColor(new Color(191, 252, 252));
        g2d.fillRect(0, 0, (width * blockSize), (height * blockSize));
  
        //Instantiate block characters to check
        String block = "X";
        char blockChar = block.charAt(0);
        String lPipe = "[";
        char lPipeChar = lPipe.charAt(0);
        String rPipe = "]";
        char rPipeChar = rPipe.charAt(0);
        String lTopPipe = "<";
        char lTopPipeChar = lTopPipe.charAt(0);
        String rTopPipe = ">";
        char rTopPipeChar = rTopPipe.charAt(0);
        String qBlock = "Q";
        char qBlockChar = qBlock.charAt(0);
        String smashBlock = "S";
        char smashBlockChar = smashBlock.charAt(0);
        


        //Paint image based on file contents
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                if ((levelRep[i][j] == blockChar) || (levelRep[i][j] == qBlockChar) || (levelRep[i][j] == smashBlockChar)) {
                    //Paint all solid blocks brown
                    g2d.setColor(new Color(112, 71, 25));
                    g2d.fillRect((j * blockSize), (i * blockSize), blockSize, blockSize);

                    //If it is a question block, write a ?
                    //NB: It writes the ? above the block unless +1 added to y axis
                    //I am also adding 2 in the x direction to better centre the ?. This is a complete hack
                    if (levelRep[i][j] == qBlockChar) {
                        g2d.setColor(Color.yellow);
                        g2d.drawString("?", (j * blockSize) + 2, ((i + 1) * blockSize));
                    }
                } else if ((levelRep[i][j] == lPipeChar) || (levelRep[i][j] == rPipeChar)) {

                    g2d.setColor(new Color(10, 173, 1));
                    g2d.fillRect((j * blockSize), (i * blockSize), blockSize, blockSize);
                } else if ((levelRep[i][j] == lTopPipeChar) || (levelRep[i][j] == rTopPipeChar)) {

                    g2d.setColor(new Color(6, 114, 0));
                    g2d.fillRect((j * blockSize), (i * blockSize), blockSize, blockSize);
                }
            }
        }

        // Release used system resources 
        g2d.dispose();

        //Remove file extension from file name
        fileName = fileName.replace(".txt", "");

        // Save as PNG
        //File imageFile = new File(defaultOutputDir+fileName+"PNG.png");
        //ImageIO.write(bufferedImage, "png", imageFile);

        // Save as JPEG
        //Horrible hack here to get it to actually enter the folder
        File imageFileJPEG = new File(outputDir + "/" + fileName + "JPG.jpg");
        ImageIO.write(bufferedImage, "jpg", imageFileJPEG);

    }
}