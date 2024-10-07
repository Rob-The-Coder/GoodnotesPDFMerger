package it.rob.PDFMerger;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PDFMerger {
  /*********************************************************************************/
  //ATTRIBUTES
  /*********************************************************************************/
  private static PDFMerger instance=null;
  private static final Dimension PDF_A4_DIMENSIONS=new Dimension(595, 842);
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

  public void merge(int SLIDES_PER_PAGE, String backgroundImageFilename) throws IOException {
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
        File destinationDirectory = new File(pdfs_target_folder.getPath());
        File file = new File(destinationDirectory, selectedPDF.getName().replaceAll("[-+^:,]", ""));

        PdfWriter pdfWriter=new PdfWriter(file.getPath());
        PdfDocument destPdf = new PdfDocument(pdfWriter);
        Document targetPDF = new Document(destPdf);
        targetPDF.setMargins(0f, 0f, 0f, 0f);

        System.out.println("CONVERSION OF "+ selectedPDF.getName() +" STARTED.....");

        //Extracting pngs from selectedPDF, after a SLIDES_PER_PAGES number of pngs an overlay image is created
        //and is instantly added to the target PDF
        PDDocument pdf = PDDocument.load(new File(selectedPDF.getPath()));
        PDFRenderer pdfRenderer = new PDFRenderer(pdf);
        for (int i = 0; i < pdf.getNumberOfPages(); i += 1) {
          //Extracting pngs
          BufferedImage bgImage = readImage(backgroundImageFilename);
          BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 600, ImageType.RGB);

          slides.add(bim);
          System.out.println("Slide-" + i + " created successfully");

          counter += 1;

          //If we reach SLIDES_PER_PAGE, or we reach the end of the PDF we create the overlay and write in the target PDF
          if (counter == SLIDES_PER_PAGE || i == pdf.getNumberOfPages() - 1) {
            BufferedImage overlayedImage = overlayImages(SLIDES_PER_PAGE, bgImage, slides);
            slides.clear();

            //writing image to target pdf
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(overlayedImage, "png", baos);
            ImageData imageData= ImageDataFactory.create(baos.toByteArray());
            Image image=new Image(imageData);
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

  public void superImpose(int SLIDES_PER_PAGE, String backgroundPagePDF) throws IOException {

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

    if(selectedPDFs!=null){
      for(File selectedPDF : selectedPDFs){
        System.out.println("SUPERIMPOSING OF "+ selectedPDF.getName() +" STARTED.....");
        //Creating target PDF
        File destinationDirectory = new File(pdfs_target_folder.getPath());
        File file = new File(destinationDirectory, selectedPDF.getName().replaceAll("[-+^:,]", ""));

        // Creating a PdfDocument objects
        PdfDocument targetPDF = new PdfDocument(new PdfWriter(file.getPath()));
        PdfDocument backgroundPDF = new PdfDocument(new PdfReader(Objects.requireNonNull(PDFMerger.getInstance().getClass().getClassLoader().getResourceAsStream(backgroundPagePDF))));
        PdfDocument slidePDF = new PdfDocument(new PdfReader(selectedPDF.getPath()));

        //Looping each slide
        int counter=0;
        ArrayList<PdfPage> pages=new ArrayList<>();
        for (int i = 0; i < slidePDF.getNumberOfPages(); i += 1) {
          // Opening a page from the existing PDF
          pages.add(slidePDF.getPage(i+1));

          System.out.println("Slide-" + i + " extracted successfully");

          counter += 1;

          //If we reach SLIDES_PER_PAGE, or we reach the end of the PDF we create the overlay and write in the target PDF
          if (counter == SLIDES_PER_PAGE || i == slidePDF.getNumberOfPages() - 1) {
            PdfPage page = targetPDF.addNewPage(PageSize.A4);
            PdfCanvas canvas = new PdfCanvas(page);
            canvas.addXObject(backgroundPDF.getPage(1).copyAsFormXObject(targetPDF), 0, 0);
            for (int j=0; j<pages.size(); j+=1){
              // Getting the page size
              com.itextpdf.kernel.geom.Rectangle origSize = pages.get(j).getPageSize();

              // Getting the size of the page
              PdfFormXObject pageCopy = pages.get(j).copyAsFormXObject(targetPDF);

              Dimension scaledDimension=getScaledDimension(new Dimension((int) origSize.getWidth(), (int) origSize.getHeight()), new Dimension((int) backgroundPDF.getPage(1).getPageSize().getWidth(), (int) (backgroundPDF.getPage(1).getPageSize().getHeight()/SLIDES_PER_PAGE)));
              AffineTransform transformationMatrix = AffineTransform.getScaleInstance(scaledDimension.getWidth()/origSize.getWidth() , page.getPageSize().getHeight()/ origSize.getHeight()/3);

              // j-th tile
              if(j==0)
                canvas.concatMatrix(transformationMatrix);
              canvas.addXObject(pageCopy, 0, origSize.getHeight()*(SLIDES_PER_PAGE-1-j));
            }//end-for

            System.out.println("Page-" + (i/SLIDES_PER_PAGE) + " created successfully\n");

            pages.clear();
            counter = 0;
          }//end-if
        }//end-for
        targetPDF.close();
        slidePDF.close();
        System.out.println("SUPERIMPOSING OF "+ selectedPDF.getName() +" STOPPED.....\n");
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

  private BufferedImage readImage(String fileLocation) {
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
    Graphics2D g=outputImage.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(img.getScaledInstance(dimension.width, dimension.height, java.awt.Image.SCALE_AREA_AVERAGING), 0, 0, null);
    g.dispose();

    return outputImage;
    //return Scalr.resize(img, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, dimension.width, dimension.height);
  }
  /*********************************************************************************/
}
