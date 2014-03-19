package fr.unistra.pelican.demos;

import java.awt.HeadlessException;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.AdditionChecked;
import fr.unistra.pelican.algorithms.io.ImageBuilder;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedMultiProbashed;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


public class MarkerBasedMultiProbashedDemo {

	public static void main(String[] args) throws HeadlessException {

		if (args.length == 0) {
			System.out.println("Usage: MarkerBasedMultiProbashedDemo file [output]\n"
					+ "- file is the image to be segmented\n"
					+ "- output is the resulting image\n");
			return;
		}

	
		String path = args[0];
		Image input = ImageLoader.exec(path);
		Image markers = (Image) new ImageBuilder().process(input,"SupervisedWatersedDemo: " + args[0]);
		
		Image samples = (Image) new LabelsToBinaryMasks().process(markers);
		long t1 = System.currentTimeMillis();
		Image result = (Image) new MarkerBasedMultiProbashed().process(input, samples);
		long t2 = System.currentTimeMillis();
		System.out.println("Supervised segmentation : " + (t2 - t1) / 1000.0
				+ " s");

		Image view1 = (Image) new LabelsToColorByMeanValue().process(result, input);
		Viewer2D.exec(view1, "SupervisedWatersedDemo: regions of " + args[0]);

		Image frontiers = (Image) new BinaryDilation().process(
				(Image) new FrontiersFromSegmentation().process(result), 
				FlatStructuringElement2D.createSquareFlatStructuringElement(3));

		Image frontiers2 = (Image) new FrontiersFromSegmentation()
				.process(convertToIntegerImage(markers));
		frontiers2 = (Image) new AdditionChecked().process(frontiers, frontiers2);
		Image view3 = (Image) new DrawFrontiersOnImage().process(input, frontiers2);
		Viewer2D.exec(view3,
				"SupervisedWatersedDemo: frontiers with markers of " + args[0]);

		if (args.length == 2) {
			result = convertToByteImage(result);
			ImageSave.exec(result, args[1]);
		}
	}

	private static IntegerImage convertToIntegerImage(Image img) {
		IntegerImage res = new IntegerImage(img.getXDim(), img.getYDim(), img
				.getZDim(), img.getTDim(), img.getBDim());
		for (int p = 0; p < img.size(); p++)
			res.setPixelInt(p, img.getPixelByte(p));
		return res;
	}

	private static ByteImage convertToByteImage(Image img) {
		ByteImage res = new ByteImage(img.getXDim(), img.getYDim(), img
				.getZDim(), img.getTDim(), img.getBDim());
		for (int p = 0; p < img.size(); p++)
			res.setPixelByte(p, img.getPixelInt(p));
		return res;
	}

}