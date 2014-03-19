/**
 * 
 */
package fr.unistra.pelican.algorithms.io;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStream;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * Read an image contained in a pdf file.
 * <p>Support is only ensured for flat encoded images in RGB format, 
 * other types may or may not work depending on JAI implemented capabilities
 * (in other words, don't expect to much!). 
 * <p>All the way, dct encoded images (like jpeg) should work fine and 
 * images produced using PDFImageSave class too.
 * <p>Support for other color spaces in flat encoded mode should be easy to implement!
 * <p>If the pdf file contains several images the first one is loaded.
 * 
 * @TODO support all type of images managed by pdf specification
 * @TODO support pdf with several images
 * 
 * @author Benjamin Perret
 *
 */
public class PDFImageLoad extends Algorithm {

	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image output;
	
	/**
	 * Constructor
	 * 
	 */
	public PDFImageLoad() {

		super();
		super.inputs = "filename";
		super.outputs = "output";
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		PdfReader reader;
		try {
			reader = new PdfReader(
					(new File(filename)).getAbsolutePath());

			for (int i = 0; i < reader.getXrefSize(); i++) {
				PdfObject pdfobj = reader.getPdfObject(i);
				if (pdfobj != null) {
					if (pdfobj.isStream()) {
						PdfStream pdfdict = (PdfStream) pdfobj;
						PdfObject pdfsubtype = pdfdict
						.get(PdfName.SUBTYPE);
						if (pdfsubtype == null) {
							continue;
						}
						if (!pdfsubtype.toString().equals(
								PdfName.IMAGE.toString())) {
							continue;
						}
						//	System.out.println("total_number_of_pictures: "
						//			+ total_number_of_pictures);
						//System.out.println("height:"+ pdfdict.get(PdfName.HEIGHT));
						//System.out.println("width:"+ pdfdict.get(PdfName.WIDTH));
						//System.out.println("bitspercomponent:"+ pdfdict.get(PdfName.BITSPERCOMPONENT));
						for(Object o:pdfdict.getKeys())
						{
							System.out.println(o +  " " + pdfdict.get((PdfName)o));
						}
						byte[] barr = PdfReader.getStreamBytesRaw((PRStream) pdfdict);
						if(pdfdict.get(PdfName.FILTER).equals(PdfName.FLATEDECODE))
						{ // yep this is the only mode managed
							if(pdfdict.get(PdfName.COLORSPACE).equals(PdfName.DEVICERGB))
							{ // yes and same for this point
								//System.out.println("flat rgb");
								barr=PdfReader.FlateDecode(barr);
								int h=Integer.parseInt(""+pdfdict.get(PdfName.HEIGHT));
								int w=Integer.parseInt(""+pdfdict.get(PdfName.WIDTH));
								ByteImage res=new ByteImage(w, h, 1, 1, 3);
								for(int j=0;j<res.size();j++)
								{
									// convert from unsigned to signed
									int a=0;
									a=a | barr[j];
									res.setPixelByte(j, a);
								}
								output=res;
								return;
							}
							
						}
						java.awt.Image im = Toolkit.getDefaultToolkit().createImage(barr);
						//System.out.println(im.getWidth(null) + " " + im.getHeight(null));
						javax.swing.ImageIcon ii = new javax.swing.ImageIcon(im);
						//System.out.println(ii.getIconHeight() + " " + ii.getIconWidth());
						BufferedImage buff=toBufferedImage(im);
						output=ImageLoader.convertFromJAI(buff, false);
						return;
						//JLabel label = new JLabel();
						//label.setIcon(ii);
						//image_panel.add(label, String.valueOf(total_number_of_pictures++));
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	private BufferedImage toBufferedImage(java.awt.Image image) {
        /** On test si l'image n'est pas déja une instance de BufferedImage */
        if( image instanceof BufferedImage ) {
                return( (BufferedImage)image );
        } else {
                /** On s'assure que l'image est complètement chargée */
                image = new ImageIcon(image).getImage();
                
                /** On crée la nouvelle image */
                BufferedImage bufferedImage = new BufferedImage(
                            image.getWidth(null),
                            image.getHeight(null),
                            BufferedImage.TYPE_INT_RGB );
                System.out.println(image.getWidth(null) + " " + image.getHeight(null));
                Graphics g = bufferedImage.createGraphics();
                g.drawImage(image,0,0,null);
                g.dispose();
                
                return( bufferedImage );
        } 
}
	
	public static Image exec(String filename)
	{
		return (Image)new PDFImageLoad().process(filename);
	}

}
