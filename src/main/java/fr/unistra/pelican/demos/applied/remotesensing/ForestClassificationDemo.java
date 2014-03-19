package fr.unistra.pelican.demos.applied.remotesensing;

import java.awt.Color;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.applied.remotesensing.ForestMorphologicalClassifier;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToPredefinedColor;


/**
 * Demo class for ForestMorphologyClassifier : 
 * Classify forest areas using morphological operators
 * 
 * Mapping spatial patterns with morphological image processing
 * P. Vogt, K. Ritters, C. Estreguil, J. Kozak, T. Wade, J.D. Wickham
 * Landscape Ecology, 22:171-177, 2007 
 * http://forest.jrc.it/biodiversity/Product/4classweb0.html
 * 
 * @author lefevre
 *
 */
public class ForestClassificationDemo {

	public static void main(String [] args) {
		boolean view=false;
		String path="samples/teledetection/forest.tif";
		String output="_result.png";

		// Display help
		System.out.println("ForestClassificationDemo name [view]\n"
					+"name: File name to be processed\n"
					+"[view]: flag (true/false) to display the images\n");
		
		// Set the parameters
		if (args.length>=1)
			path=args[0];
		if (args.length>=2)
			view=Boolean.parseBoolean(args[1]);
		
		// Load the image
		Image input=ImageLoader.exec(path);
		input.setColor(false);
		
		// Binarize and process the image
		BooleanImage img=ManualThresholding.exec(input,2);
		IntegerImage res=(IntegerImage) ForestMorphologicalClassifier.exec(img,3);
		
		// Colorize the result
		Color colors[]={Color.DARK_GRAY,Color.LIGHT_GRAY,Color.GREEN,Color.MAGENTA,Color.BLACK,Color.BLUE};
		Image res2=LabelsToPredefinedColor.exec(res,colors);
		
		// Save the result
		ImageSave.exec(res2,path+output);
		
		// View the result if required
		if (view) {
			fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec(img,"input");
			fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec(res2,"output");
			}
		
	}
		
}
	