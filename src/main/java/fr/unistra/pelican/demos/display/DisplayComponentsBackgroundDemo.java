package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class DisplayComponentsBackgroundDemo {
	public static void main(String[] args) throws PelicanException {
		if (args.length == 0)
			System.out
				.println("Usage: DisplayDemo image1 image2 ... imageN \n where imageX are the images to be displayed");
		else
			for (int i = 0; i < args.length; i++) {
				Image im = ImageLoader.exec(args[i]);
				im = BooleanConnectedComponentsLabeling.exec(im,true);
				Viewer2D.exec(LabelsToRandomColors.exec(im), args[i] + " : "
					+ im.getProperty("nbRegions") + " components");
			}

	}

}