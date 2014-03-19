/**
 * 
 */
package fr.unistra.pelican.algorithms.io;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;


/**
 * Save a bufferedImage into a pdf file. 
 * If you want to save a PELICAN image into a pdf, 
 * use ImageSave and end your filename with extension ".pdf".
 * 
 * The pdf file will have same dimensions as input image.
 * 
 * @author Benjamin Perret
 *
 */
public class PDFImageSave extends Algorithm {

	/**
	 * Input image
	 */
	public BufferedImage inputImage;
	
	/**
	 * Location of saved file
	 */
	public String path;
	
	public PDFImageSave(){
		super.inputs="inputImage,path";
		super.outputs="";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
	    
			int xdim=inputImage.getWidth();
			int ydim=inputImage.getHeight();
		
	        Document document = new Document(new Rectangle(xdim,ydim),0,0,0,0);
	        document.addCreator("Provided by PELICAN via iText.");
	        try {
	        	FileOutputStream stream= new FileOutputStream(path);
				PdfWriter.getInstance(document,stream );
				 document.open();
			     com.lowagie.text.Image im=com.lowagie.text.Image.getInstance(inputImage,null);
			     document.add(im);
			     document.close();
	        
	        } catch (FileNotFoundException e) {
				throw new AlgorithmException("Can not create file : " +path + " . Error was : " +e );
			} catch (DocumentException e) {
				throw new AlgorithmException("Can not create pdf document. Error was : " +e );
			}catch (IOException e) {
				throw new AlgorithmException("Can not access file : " +path + " . Error was : " +e );
			}
	    
	       


	}

	/**
	 * 
	 * @param inputImage
	 */
	public static void exec(BufferedImage inputImage, String path)
	{
		new PDFImageSave().process(inputImage,path);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*Image op=ImageLoader.exec("samples/lennaGray256.png");
		ImageSave.exec(op,"c:\\lenna.pdf");*/
	}

}
