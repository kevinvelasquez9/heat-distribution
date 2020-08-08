import java.util.Scanner;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Color;

/**
 * JHED: kvelasq1
 * Due Date: 31 October 2019
 * This program will simulate and visualize the heat distribution
 * across a metal plate. The plate will have restricted cells
 * that are either the coldest or hottest points on the plate.
 * The unrestricted cells are free to change, by taking the average of
 * its surrounding cells. The simulation will continue until two
 * iterations have data within epsilon of each other.
 */
 
public class HeatDistrSim {
   
   /**
    * The main method will call various helper methods
    * to complete the task. Initially, it will prompt the user
    * for the coldest and hottest temperature, the value of epsilon, and 
    * the name of the text file with the plate's structure.
    * Execution starts here.
    * @param args command-line arguments
    * @throws IOException if file cannot be opened    
    */

   public static void main(String[] args) throws IOException {
      Scanner kb = new Scanner(System.in);
      
      System.out.print("Enter a value for the cold temperatures: ");
      double cold = kb.nextDouble();
      
      System.out.print("Enter a value for the hot temperatures: ");
      double hot = kb.nextDouble();
      
      System.out.print("Enter a value for epsilon (0 < epsilon <= 1): ");
      double epsilon = kb.nextDouble();
      
      System.out.print("Enter the input filename: ");
      String file = kb.next();
      
      String fileSub = file.substring(0, file.lastIndexOf('.'));
      
      // arrays that determine where coldest or hottest values lie
      boolean[][] coldMask = inputFile2Mask(file, 'C'); 
      boolean[][] hotMask = inputFile2Mask(file, 'H');
      
      // the original plate before averaging of cells
      double[][] tempBefore = masks2Temps(coldMask, hotMask, cold, hot); 
      
      // the new plate that is stable in terms of epsilon
      double[][] tempAfter = simulate(tempBefore, epsilon, coldMask, hotMask);
      
      String outputFile = fileSub + "_" + cold + "_" + hot + ".txt"; 
      saveText(tempAfter, outputFile);
      
      String outputPic = outputFile + ".x10.jpg";
      
      // visualizing the plate
      Picture heatMap = createScaledPic(tempAfter, cold, hot);
      displayAndSavePic(heatMap, outputPic);
      
   }
   
   /**
    * This method will return a boolean array that correlates
    * to the input file. If the input file has the prompted
    * character, the boolean array will have a true in that
    * same cell. Otherwise, a false will be assigned.
    * @param inFilename the name of the file being streamed
    * @param hotOrCold the character the method is searching for
    * @return boolean array
    * @throws IOException if file cannot be opened
    */
    
   public static boolean[][] inputFile2Mask(String inFilename, char hotOrCold) 
      throws IOException {
      FileInputStream fileByteStream = null;
      Scanner inFS = null;  
      fileByteStream = new FileInputStream(inFilename);
      inFS = new Scanner(fileByteStream);
      
      int row = inFS.nextInt();
      int col = inFS.nextInt();
      char[][] input = new char[row][col];
      
      for (int r = 0; r < row; r++) {
         String dummy = inFS.next();
         for (int c = 0; c < col; c++) {
            char dummyChar = dummy.charAt(c);
            input[r][c] = dummyChar;
         }
      }
            
      boolean[][] output = new boolean[row][col];
      
      for (int r = 0; r < row; r++) {
         for (int c = 0; c < col; c++) {
            if (input[r][c] == hotOrCold) {
               output[r][c] = true;
            }
            else {
               output[r][c] = false;
            }
         }                  
      }
      fileByteStream.close();
      return output;
   }
   
   /** 
    * This method will utilize the two boolean arrays formed by 
    * inputFile2Mask, one for the cold condition and one for the 
    * hot condition. If either array is true, the new double array
    * will receive the hottest or coldest temperature 
    * (depending on which array is true). If neither is true, the cell
    * receives the average of the two extremes.
    * @param coldMask the boolean array for cold condition
    * @param hotMask the boolean array for hot condition
    * @param cold the coldest temperature
    * @param hot the hottest temperature
    * @return the initial state of the plate
    */
    
   public static double[][] masks2Temps(boolean[][] coldMask, 
      boolean[][] hotMask, double cold, double hot) {
      double[][] temp = new double[coldMask.length][coldMask[0].length];
      
      for (int r = 0; r < coldMask.length; r++) {
         for (int c = 0; c < coldMask[0].length; c++) {
            if (coldMask[r][c]) {
               temp[r][c] = cold;
            }
            else if (hotMask[r][c]) {
               temp[r][c] = hot;
            }
            else {
               temp[r][c] = (cold + hot) / 2.0;
            }
         }
      }
      return temp;
   }
   
   /**
    * This method will assign a new value to each cell
    * if it was not assigned to either the coldest or
    * hottest. The average of the nearest cells
    * will become the cell's new value if its not
    * restricted.
    * @param currDistr the iteration that will be averaged
    * @param coldMask whether or not cell is coldest
    * @param hotMask whether or not cell is hottest
    * @return the new iteration
    */
    
   public static double[][] average(double[][] currDistr, 
      boolean[][] coldMask, boolean[][] hotMask) {
      double[][] distribution = new double[coldMask.length][coldMask[0].length];
      
      for (int r = 0; r < coldMask.length; r++) {
         for (int c = 0; c < coldMask[0].length; c++) {
            if (coldMask[r][c] || hotMask[r][c]) {
               distribution[r][c] = currDistr[r][c];
            }
            else {
               distribution[r][c] = avg(currDistr, c, r);
            }
         }
      }
      return distribution; 
   }
   
   /**
    * This method will average the neighbor cells to the inputted
    * x,y coordinate. If a neighbor cell is out of bounds, it will
    * not be counted.
    * @param distr the array that has the cell that needs to be average
    * @param xCoord the column value
    * @param yCoord the row value
    * @return the new averaged value
    */
       
   public static double avg(double[][] distr, int xCoord, int yCoord) {
      double sum = 0;
      int i = 0;
      
      if (xCoord > 0) {
         sum = sum + distr[yCoord][xCoord - 1];
         i++;
      }
      if (xCoord < distr[0].length - 1) {
         sum = sum + distr[yCoord][xCoord + 1];
         i++;
      }
      if (yCoord > 0) {
         sum = sum + distr[yCoord - 1][xCoord];
         i++;
      }
      if (yCoord < distr.length - 1) {
         sum = sum + distr[yCoord + 1][xCoord];
         i++;
      }
      return sum / i;
   }
   
   /**
    * This method will check if the previous and new plate values
    * at the same coordinates are within epsilon of each other. 
    * @param newDistr the new averaged array
    * @param oldDistr the previous array
    * @param epsilon value between 0 and 1, inclusive
    * @return stable or not stable
    */
    
   public static boolean equiv(double[][] newDistr, double[][] oldDistr, 
      double epsilon) {
   
      for (int r = 0; r < oldDistr.length; r++) {
         for (int c = 0; c < oldDistr[0].length; c++) {
            if (Math.abs(newDistr[r][c] - oldDistr[r][c]) > epsilon) {
               return false;
            }
         }
      }
      return true;
   
   }
   
   /**
    * This method will continue until it creates a new distribution
    * that is within epsilon value of the previous one. Or in other
    * words, the plate is stable. It will reassign cells that were
    * unrestricted, by averaging its neighbors.
    * @param initialDistr the previous iteration of the plate
    * @param epsilon a value between 0 and 1, inclusive
    * @param coldMask shows whether or not the cell is coldest
    * @param hotMask whether or not that cell is hottest
    * @return the stable heat plate
    */
    
   public static double[][] simulate(double[][] initialDistr, double epsilon, 
      boolean[][] coldMask, boolean[][] hotMask) {
   
      double[][] newDistr = average(initialDistr, coldMask, hotMask);
      boolean isReady = equiv(newDistr, initialDistr, epsilon);
      
      // if the new array is not within epsilon, repeat this method
      if (!isReady) {
         newDistr = simulate(newDistr, epsilon, coldMask, hotMask);
      } 
      return newDistr;
   }
   
   /**
    * This method will output the values of each cell onto a text
    * file.
    * @param table the stable plate
    * @param filename name of the output file
    * @throws IOException if file cannot be opened
    */
    
   public static void saveText(double[][] table, String filename) 
      throws IOException {
      FileOutputStream fileByteStream = null;
      PrintWriter outFS = null; 
      
      System.out.println();
      System.out.println("Text output is going to file: " + filename);
      fileByteStream = new FileOutputStream(filename);
      outFS = new PrintWriter(fileByteStream);
      
      write2Text(table, outFS); 
      outFS.flush();
      fileByteStream.close();      
   }
   
   /**
    * This method is used by saveText to format the
    * values in each cell.
    * @param table the stable plate
    * @param writer the print writer created in saveText
    */
    
   public static void write2Text(double[][] table, PrintWriter writer) {
   
      for (int r = 0; r < table.length; r++) {
         for (int c = 0; c < table[0].length; c++) {
            writer.printf("%5.5f ", table[r][c]);
         }
         writer.println();
      }
      
   }
   
   /**
    * This method will create a visualization of the plate
    * by creating new colors for each cell, depending on its
    * value.
    * @param temps the temperature in each cell
    * @param cold the coldest value
    * @param hot the hottest value
    * @return picture the plate visualized
    */
    
   public static Picture createScaledPic(double[][] temps, 
      double cold, double hot) {
      Picture heatMap = new Picture(temps[0].length, temps.length);
      
      double range = Math.abs(hot - cold);
      for (int r = 0; r < temps.length; r++) {
         for (int c = 0; c < temps[0].length; c++) {
            double ratio = normalize(temps[r][c], range, cold);
            int red = (int) (Math.max(0, 255 * (ratio - 1)));
            int blue = (int) (Math.max(0, 255 * (1 - ratio)));
            int green = (int) (255 - red - blue);
            
            heatMap.set(c, r, new Color(red, green, blue));
         
         }
      }
      heatMap = heatMap.scale(10);
      return heatMap;
   }
   
   /**
    * Creates a ratio for the cell's color depending on
    * the plate's range of temperatures and the actual
    * temperature of the cell.
    * @param value the cell's temperature
    * @param rangeSize the max minus min temperature
    * @param lowEnd the coldest value
    * @return ratio
    */
   public static double normalize(double value, double rangeSize, 
      double lowEnd) {
      return 2 * ((value - lowEnd) / rangeSize);
   }
   
   /**
    * This method will display the visualization and save it to a file.
    * @param pic the plate
    * @param outputFilename where the picture will be saved
    */
    
   public static void displayAndSavePic(Picture pic, String outputFilename) {
      
      System.out.print("Scaled picture is going to file: " + outputFilename);
      System.out.println();
      pic.save(outputFilename);
      pic.show();    
   }
}
