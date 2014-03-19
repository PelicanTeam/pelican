package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawLabelsOnImage;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class MultispectralDisplayImageLabelsDemo {
	public static void main(String[] args) throws PelicanException {
		if (args.length < 0)
			System.out
				.println("Usage: DisplayLabelsDemo source image1 image2 ... imageN \n where imageX are the images to be displayed");
		else {
			Image source =
			ColorImageFromMultiBandImage.exec(ImageLoader.exec(args[0]), 2,1,0);
			for (int i = 1; i < args.length; i++) {
				Image im = ImageLoader.exec(args[i]);
				Viewer2D.exec(DrawLabelsOnImage.exec(ContrastStretch.exec(source),
					im),args[i]+" : frontiers");
			}
		}

	}
}