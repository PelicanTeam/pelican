package fr.unistra.pelican.demos;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.GrayRegionalMaxima;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayCornerDetection;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.OtsuThresholding;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class GrayCornerDetectionDemo {

	public static void main(String[] args) {
		Image img = ImageLoader.exec("samples/lenna.png");
		Image input = (Image) new AverageChannels().process(img);
		img = GrayCornerDetection.exec(input);
		int threshold = (Integer) new OtsuThresholding().processOne(1,img);
		for (int i = 0; i < img.size(); i++) {
			int p = img.getPixelByte(i);
			if (p < threshold)
				img.setPixelByte(i, 0);
		}
		img = GrayRegionalMaxima.exec(img);
		img = ManualThresholding.exec(img, new Integer(1));
		Viewer2D.exec(img, "result");
	}

}
