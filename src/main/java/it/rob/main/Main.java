package it.rob.main;
import it.rob.PDFMerger.PDFMerger;

import javax.swing.*;
import java.io.IOException;

public class Main {
  /*********************************************************************************/
  //CONSTANTS
  /*********************************************************************************/
  private static int SLIDES_PER_PAGE;
  private static String BACKGROUND_IMAGE_FILENAME;
  /*********************************************************************************/
  //METHODS
  /*********************************************************************************/
  public static void main(String[] args){
    //Checking passed arguments
    switch (args.length) {
      case 0:
        SLIDES_PER_PAGE = 3;
        BACKGROUND_IMAGE_FILENAME = "background.png";
        break;
      case 1:
        if (args[0].equals("-h")) {
          System.out.println("GoodnotesPDFMerger accepts upto 2 command line arguments:");
          System.out.println("SLIDES_PER_PAGE: tells the PDF converter how many slides per page it needs to consider.");
          System.out.println("BACKGROUND_IMAGE_FILENAME: specify which background image to choose");
          System.out.println("Please note that in case no argument is passed the default values will be used, that is to say 3 for SLIDES_PER_IMAGE and \"background.png\" for BACKGROUND_IMAGE_FILENAME.");
          System.exit(0);
        } else {
          SLIDES_PER_PAGE = Integer.parseInt(args[0]);
          BACKGROUND_IMAGE_FILENAME = "background.png";
        }//end-if
        break;
      case 2:
        SLIDES_PER_PAGE = Integer.parseInt(args[0]);
        BACKGROUND_IMAGE_FILENAME = args[1];
        break;
      default:
        System.out.println("AN ERROR OCCURRED!\nInvalid number of arguments passed, please check how to use GoodnotesPDFMerger using -h parameter...");
        System.exit(0);
    }//end-switch
    
    try {
      PDFMerger.getInstance().merge(SLIDES_PER_PAGE, BACKGROUND_IMAGE_FILENAME);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, "An unknown error occurred.\n"+e.getMessage());
    }//end-try
  }
}