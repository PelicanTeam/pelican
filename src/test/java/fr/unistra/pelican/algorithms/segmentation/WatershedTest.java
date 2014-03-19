package fr.unistra.pelican.algorithms.segmentation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.MultispectralEuclideanGradient;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class WatershedTest {

	@Test
	public void testSimpleWatersed() {
		Image input = ImageLoader.exec("src/test/resources/watershed.png");

		Image output = ContrastStretch.exec(Watershed.exec(MultispectralEuclideanGradient.exec(input,FlatStructuringElement2D.createCircleFlatStructuringElement(2))));
//		Viewer2D.exec(output, "samples/watershed-truth.png");
//		ImageSave.exec(output, "src/test/resources/watershed-truth.png");
		
		Image truth = ImageLoader.exec("src/test/resources/watershed-truth.tiff");
 
		assertEquals(truth.size(), output.size());

		for(int i = 0; i < truth.size(); i++)
			assertEquals(output.getPixelByte(i), truth.getPixelByte(i));
	}

	public static void main(String[] args) {
		Image input = ImageLoader.exec("src/test/resources/watershed.png");

		Image output = new ByteImage(ContrastStretch.exec(Watershed.exec(MultispectralEuclideanGradient.exec(input,FlatStructuringElement2D.createCircleFlatStructuringElement(2)))));
//		Viewer2D.exec(output, "samples/watershed-truth.png");
		ImageSave.exec(output, "src/test/resources/watershed-truth.tiff");
		
		Image truth = ImageLoader.exec("src/test/resources/watershed-truth.tiff");

		assertEquals(truth.size(), output.size());

		for(int i = 0; i < truth.size(); i++)
			assertEquals(output.getPixelByte(i), truth.getPixelByte(i));
	}

}
