/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * Transferable object to send Java AWT image to the clipboard.
 * 
 * Use static method copyImageToClipboard for easy use.
 * 
 * @author Benjamin Perret
 *
 */
public class ClipBoardImageTranserferable implements Transferable{
	
	
	    private Image image;
	   
	    public static void copyImageToClipboard(Image image) {
	    	ClipBoardImageTranserferable imageTrans = new ClipBoardImageTranserferable(image);
	        Toolkit toolkit = Toolkit.getDefaultToolkit();
	        try{
	        	toolkit.getSystemClipboard().setContents(imageTrans, null);
	        } catch (Exception e)
	        {
	        	System.err.println("Cannot access to clipboard : " +e);
	        }
	    }
	   
	    public ClipBoardImageTranserferable(Image image) {
	        this.image = image;
	    }
	   
	    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
	        if (flavor.equals(DataFlavor.imageFlavor) == false) {
	            throw new UnsupportedFlavorException(flavor);
	        }
	        return image;
	    }
	   
	    public boolean isDataFlavorSupported(DataFlavor flavor) {
	        return flavor.equals(DataFlavor.imageFlavor);
	    }
	   
	    public DataFlavor[] getTransferDataFlavors() {
	        return new DataFlavor[] {
	            DataFlavor.imageFlavor
	        };
	    }
	
}
