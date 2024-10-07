package it.rob.main;
import it.rob.PDFMerger.PDFMerger;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.IOException;

public class Main {
  /*********************************************************************************/
  //CONSTANTS
  /*********************************************************************************/
  private static int SLIDES_PER_PAGE=3;
  private static final String DEFAULT_MERGING_FILENAME="Goodnotes.png";
  private static final String DEFAULT_SUPERIMPOSING_FILENAME="Goodnotes.pdf";
  /*********************************************************************************/
  //METHODS
  /*********************************************************************************/
  public static void main(String[] args){
    //Checking passed arguments
    Options options = new Options();

    Option slidesPerPageOption = new Option("n", true, "Number of slides per page");
    Option fileNameOption = new Option("f", true, "File name");
    Option superimposingOption = new Option("s", false, "Superimposing");
    Option mergingOption = new Option("m", false, "Merging");

    options.addOption(slidesPerPageOption).addOption(superimposingOption).addOption(mergingOption).addOption(fileNameOption);

    try {
      //Create GNU like options
      CommandLineParser gnuParser = new GnuParser();
      CommandLine cmd = gnuParser.parse(options, args);
      if (cmd.hasOption("n")) {
        SLIDES_PER_PAGE = Integer.parseInt(cmd.getOptionValue("n"));
      }//end-if
      if (cmd.hasOption("f")) {
        if (cmd.hasOption("s")) {
          PDFMerger.getInstance().superImpose(SLIDES_PER_PAGE, cmd.getOptionValue("f"));
        }else if (cmd.hasOption("m")) {
          PDFMerger.getInstance().merge(SLIDES_PER_PAGE, cmd.getOptionValue("f"));
        }//end-if
      }else{
        if (cmd.hasOption("s")) {
          PDFMerger.getInstance().superImpose(SLIDES_PER_PAGE, DEFAULT_SUPERIMPOSING_FILENAME);
        }else if (cmd.hasOption("m")) {
          PDFMerger.getInstance().merge(SLIDES_PER_PAGE, DEFAULT_MERGING_FILENAME);
        }//end-if
      }//endif
    }catch (IOException | ParseException e) {
      JOptionPane.showMessageDialog(null, "An error occurred.\n"+e.getMessage());
    }//end-try
  }
}