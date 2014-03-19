package fr.unistra.pelican.demos;

import java.io.File;

import javax.swing.JFileChooser;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.algorithms.visualisation.ViewerVideo;
/**
 * This class allow to load and view image/video
 * @author Jonathan Weber
 *
 */
public class PelicanImageLoaderViewerDemo {
	
	public static void main(String[] args)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choose the picture to load");
		
		int returnVal = chooser.showOpenDialog(chooser);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       Image tmp = ImageLoader.exec(chooser.getCurrentDirectory()+File.separator+chooser.getSelectedFile().getName());
	       if(tmp.getTDim()!=1)
	    	   ViewerVideo.exec(tmp);
	       else
	    	   Viewer2D.exec(tmp);
	    }
	}

}
