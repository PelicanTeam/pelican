package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawLabelsOnImage;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class DisplayImageLabelsDemo
{
	public static void main(String[] args) {
	if (args.length < 0)
		System.out
			.println("Usage: DisplayImageLabelsDemo source image1 image2 ... imageN \n where imageX are the images to be displayed");
	else {
		Image source = ImageLoader.exec(args[0]);
		for (int i = 1; i < args.length; i++) {
			Image im = ImageLoader.exec(args[i]);
			// Viewer2D.exec(DrawFrontiersOnImage.exec(ContrastStretch.exec(source),
			// (BooleanImage) BinaryDilation.exec(
			// FrontiersFromSegmentation.exec(im), FlatStructuringElement2D
			// .createSquareFlatStructuringElement(3))));
			Viewer2D.exec(DrawLabelsOnImage.exec(ContrastStretch.exec(source),
				im),args[i]+" : labels");
		}
	}

}
}