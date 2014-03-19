package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class DisplayLabelsBackgroundDemo
{
	public static void main(String[] args) throws PelicanException
	{
		if (args.length==0)
			System.out.println("Usage: DisplayDemo image1 image2 ... imageN \n where imageX are the images to be displayed");
		else for (int i=0;i<args.length;i++){
			Image im=ImageLoader.exec(args[i]);
			if(im instanceof ByteImage)
				im=((ByteImage)im).copyToIntegerImage();
			Viewer2D.exec(LabelsToRandomColors.exec(im,true),args[i]+" : colored labels");
			//Viewer2D.exec(LabelsToBinaryMasks.exec(im,true),args[i]+" : binary masks");
		}
		
	}
	
	
	
} 