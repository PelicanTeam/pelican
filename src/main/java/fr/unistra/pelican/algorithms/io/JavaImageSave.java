package fr.unistra.pelican.algorithms.io;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.InvalidParameterException;

/**
 * This class saves the given Buffered Image into the desired format. All JAI supported
 * formats may be used. Including bmp,jpeg, png and tiff. <br>
 * In case of an unsupported format the input is saved as a tiff file (the
 * default format of JAI). <br>
 * The input can be either a monochannel grayscale image, a tristumulus color
 * image, or even a multispectral image (TIFF is then the only possible output
 * format). In case of depth and time dimensions, the produced file is a
 * multipage TIFF file. Independently from the initial precision, the result is
 * always saved as an 8 bit image.
 * 
 * @author Perret
 */
public class JavaImageSave extends Algorithm {

	/**
	 * First input parameter
	 */
	public File file;

	/**
	 * Second input parameter
	 */
	public Image  input;

	/**
	 * Constructor
	 * 
	 */
	public JavaImageSave() {
		super.inputs = "input,file";
		super.outputs = "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		String extension = null;
		
		int indice = file.getAbsolutePath().lastIndexOf('.');
		if (indice != -1)
			extension = file.getAbsolutePath().substring(indice + 1).toLowerCase();
		else
			extension = "tiff";
		try {
			
			BufferedImage buf = toBufferedImage(input);
			
			if (!ImageIO.write(buf, extension, file)) {
				System.err
				.println("Error encountered with the desired file format, switching to \'tiff\'");
				ImageIO.write(buf, "tiff", file);
			}
		} catch (Exception e1) {
			throw new InvalidParameterException("PELICAN cannot write :" + file +"\n" +e1);
		}
	}

	
	  // This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(java.awt.Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
    
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
    
        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);
    
        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }
    
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
    
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
    
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
    
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
    
        return bimage;
    }

    // This method returns true if the specified image has transparent pixels
    public static boolean hasAlpha(java.awt.Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage)image;
            return bimage.getColorModel().hasAlpha();
        }
    
        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
         PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }
    
        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }
	
	/**
	 * Saves the given images into the desired format.
	 * 
	 * @param image
	 *          Image to be saved.
	 * @param file
	 *          File where the image will be saved.
	 */
	public static void exec(Image input, File file) {
		new JavaImageSave().process(input, file);
	}

	/**
	 * Saves the given images into the desired format.
	 * 
	 * @param image
	 *          Image to be saved.
	 * @param file
	 *          Path where the image will be saved.
	 */
	public static void exec(Image input, String filename) {
		exec(input, new File(filename));
	}
	
}
