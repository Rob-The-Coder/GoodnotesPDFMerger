package it.rob.main;
import it.rob.Exceptions.ArgumentException;
import it.rob.PDFMerger.PDFMerger;
import org.apache.commons.cli.*;

import javax.swing.*;

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

    Option helpOption = new Option("h", "help", false, "Print help message");
    Option slidesPerPageOption = new Option("n", "number",true, "Specify how many slides per page it needs to use. (Default is 3)");
    Option fileNameOption = new Option("f", "filename",true, "Specify which file is used as background. An example is \"Goodnotes.pdf\" or \"Goodnotes.png\".\n" +
            "Beware that when choosing -s the default file is \"Goodnotes.pdf\" whereas for -m is \"Goodnotes.png\".");
    Option superimposingOption = new Option("s", "superimposing",false, "Uses pdfs superimposing technique to merge slides onto background.");
    Option mergingOption = new Option("m","merging", false, "Uses images merging technique to merge slides onto background.");

    options.addOption(helpOption).addOption(slidesPerPageOption).addOption(fileNameOption).addOption(mergingOption).addOption(superimposingOption);

    try {
      //Create GNU like options
      CommandLineParser gnuParser = new GnuParser();
      CommandLine cmd = gnuParser.parse(options, args);

      if(cmd.hasOption(helpOption)){
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = "java -jar GoodnotesPDFMerger -args";
        final String usageHeader = "List of options for GoodnotesPDFMerger.jar";
        final String footer = "Either one of -m or -s is mandatory, if you use both at the same time by default superimpose will be used.";
        formatter.printHelp(syntax, usageHeader, options, footer);
      }else{
        if (cmd.hasOption(slidesPerPageOption)) {
          SLIDES_PER_PAGE = Integer.parseInt(cmd.getOptionValue("n"));
        }//end-if

        if(cmd.hasOption(superimposingOption)){
          PDFMerger.getInstance().superImpose(SLIDES_PER_PAGE, cmd.hasOption(fileNameOption) ? cmd.getOptionValue(fileNameOption) : DEFAULT_SUPERIMPOSING_FILENAME );
        }else if(cmd.hasOption(mergingOption)){
          PDFMerger.getInstance().merge(SLIDES_PER_PAGE, cmd.hasOption(fileNameOption) ? cmd.getOptionValue(fileNameOption) : DEFAULT_MERGING_FILENAME );
        }else{
          throw new ArgumentException();
        }//end-if
      }//end-if

    }catch (Exception e) {
      JOptionPane.showMessageDialog(null, "An error occurred.\n"+e.getMessage());
    }//end-try
  }
}