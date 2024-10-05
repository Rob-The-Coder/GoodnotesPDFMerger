package it.rob.PDFMerger;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Objects;

public class PDFMerger {
  /*********************************************************************************/
  //ATTRIBUTES
  /*********************************************************************************/
  private static PDFMerger instance=null;
  /*********************************************************************************/
  //CONSTRUCTOR
  /*********************************************************************************/
  private PDFMerger(){}
  /*********************************************************************************/
  //STATIC METHODS
  public static PDFMerger getInstance(){
    if(instance==null){
      instance=new PDFMerger();
    }//end-if
    return instance;
  }
  /*********************************************************************************/
  //METHODS
  /*********************************************************************************/
  public void convert(int SLIDES_PER_PAGE, String backgroundImageFilename) throws IOException, DocumentException {
//    BufferedImage BGIMAGE= ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(backgroundImageFilename)));
    BufferedImage BGIMAGE=readImage(backgroundImageFilename);
    
    //Choosing PDFs to convert
    File[] selectedPDFs = null;
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Choose PDFs to convert");
    chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    chooser.setMultiSelectionEnabled(true);
    chooser.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      selectedPDFs = chooser.getSelectedFiles();
    }//end-if

    //Choosing target directory where to store PDFs
    File pdfs_target_folder;
    JFileChooser directoryChooser = new JFileChooser(new File(System.getProperty("user.home")));
    directoryChooser.setDialogTitle("Choose target directory");
    directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    directoryChooser.setAcceptAllFileFilterUsed(false);
    if (directoryChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      pdfs_target_folder=directoryChooser.getSelectedFile();
    }else {
      pdfs_target_folder=directoryChooser.getFileSystemView().getDefaultDirectory();
    }//end-if

    //Conversion process
    if (selectedPDFs != null) {
      //Foreach pdf selected
      for (File selectedPDF : selectedPDFs) {
        //Converting from PDF to PNGs
        int counter = 0;
        ArrayList<BufferedImage> slides = new ArrayList<>();

        //Creating target PDF
        Document targetPDF = new Document(new com.itextpdf.text.Rectangle(BGIMAGE.getWidth(), BGIMAGE.getHeight()));
        File destinationDirectory = new File(pdfs_target_folder.getPath());
        File file = new File(destinationDirectory, selectedPDF.getName().replaceAll("[-+^:,]", ""));
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        PdfWriter pdfWriter = PdfWriter.getInstance(targetPDF, fileOutputStream);
        targetPDF.open();
        System.out.println("CONVERSION OF "+ selectedPDF.getName() +" STARTED.....");

        //Extracting pngs from selectedPDF, after a SLIDES_PER_PAGES number of pngs an overlay image is created
        //and is instantly added to the target PDF
        PDDocument pdf = PDDocument.load(new File(selectedPDF.getPath()));
        PDFRenderer pdfRenderer = new PDFRenderer(pdf);
        for (int i = 0; i < pdf.getNumberOfPages(); i += 1) {
          //Extracting pngs
          BufferedImage bgImage = readImage("background.png");
          BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 600, ImageType.RGB);
          slides.add(bim);
          System.out.println("Slide-" + i + " created successfully");

          counter += 1;

          //If we reach SLIDES_PER_PAGE, or we reach the end of the PDF we create the overlay and write in the target PDF
          if (counter == SLIDES_PER_PAGE || i == pdf.getNumberOfPages() - 1) {
            BufferedImage overlayedImage = overlayImages(SLIDES_PER_PAGE, bgImage, slides);
            slides.clear();

            //writing to target pdf
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(overlayedImage, "png", baos);
            Image image = Image.getInstance(baos.toByteArray());
            targetPDF.setPageSize(image);
            targetPDF.newPage();
            image.setAbsolutePosition(0, 0);
            targetPDF.add(image);

            counter = 0;
            System.out.println("Overlay " + (i / 3) + " Completed...");
          }//end-if
        }//end-for
        pdf.close();
        targetPDF.close();
        System.out.println("CONVERSION OF "+ selectedPDF.getName() +" STOPPED.....");
      }//end-for
    }//end-if
  }

  private BufferedImage overlayImages(int SLIDES_PER_PAGE, BufferedImage bgImage, ArrayList<BufferedImage> slides) {
    /*
     * Doing some preliminary validations.
     * Foreground image height cannot be greater than background image height.
     * Foreground image width cannot be greater than background image width.
     *
     * returning a null value if such condition exists.
     */

    Dimension newDimension = getScaledDimension(new Dimension(slides.get(0).getWidth(), slides.get(0).getHeight()), new Dimension(bgImage.getWidth(), bgImage.getHeight() / SLIDES_PER_PAGE));

    for (int i = 0; i < slides.size(); i += 1) {
      BufferedImage tmp = slides.remove(i);
      slides.add(i, scalePicture(newDimension, tmp));
    }//end-for

    /* Create a Graphics  from the background image**/
    Graphics2D g = bgImage.createGraphics();

    /* Set Antialias Rendering**/
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    /*
     * Draw background image at location (0,0)
     * You can change the (x,y) value as required
     */
    g.drawImage(bgImage, 0, 0, null);

    /*
     * Draw foreground image at location (0,0)
     * Change (x,y) value as required.
     */
    for (int i = 0; i < slides.size(); i += 1) {
      g.drawImage(slides.get(i), 0, slides.get(i).getHeight() * i, null);
    }//end-for

    g.dispose();
    return bgImage;
  }

  private static BufferedImage readImage(String fileLocation) {
    BufferedImage img;
    BufferedImage convertedImg = null;
    try {
      img = ImageIO.read(Objects.requireNonNull(PDFMerger.getInstance().getClass().getClassLoader().getResourceAsStream(fileLocation)));

      convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
      convertedImg.getGraphics().drawImage(img, 0, 0, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return convertedImg;
  }

  private Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

    int original_width = imgSize.width;
    int original_height = imgSize.height;
    int bound_width = boundary.width;
    int bound_height = boundary.height;
    int new_width = original_width;
    int new_height = original_height;

    // first check if we need to scale width
    if (original_width > bound_width) {
      //scale width to fit
      new_width = bound_width;
      //scale height to maintain aspect ratio
      new_height = (new_width * original_height) / original_width;
    }

    // then check if we need to scale even with the new height
    if (new_height > bound_height) {
      //scale height to fit instead
      new_height = bound_height;
      //scale width to maintain aspect ratio
      new_width = (new_height * original_width) / original_height;
    }

    return new Dimension(new_width, new_height);
  }

  private BufferedImage scalePicture(Dimension dimension, BufferedImage img) {
    /*
    Scaling the image
    * */
    //Creating a buffered image from collection picture
    BufferedImage outputImage = new BufferedImage(dimension.width, dimension.height, img.getType());
    outputImage.getGraphics().drawImage(img.getScaledInstance(dimension.width, dimension.height, java.awt.Image.SCALE_AREA_AVERAGING), 0, 0, null);

    return outputImage;
  }
  /*********************************************************************************/
}
