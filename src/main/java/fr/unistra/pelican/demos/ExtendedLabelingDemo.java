package fr.unistra.pelican.demos;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.BinaryMasksToLabels;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.conversion.RGBToHSY;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageBuilder;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryErosion;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryOpening;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedMultiProbashed;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionSize;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaClassificationKNN;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class ExtendedLabelingDemo {

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out
				.println("Usage: InteractiveLabelingDemo file markers output [-stretch][-color]\n"
					+ "- file is the image to be segmented\n"
					+ "- output is the resulting image\n"
					+ "- markers is the predefined marker image\n"
					+ "- -stretch to perform a contrast stretch step\n"
					+ "- -color to perform the classification in a color space");
			return;
		}
		new ExtendedLabelingDemo(args);
	}

	public ExtendedLabelingDemo(String[] args) {
		boolean color = false;
		Image input = ImageLoader.exec(args[0]);
		Image samples= LabelsToBinaryMasks.exec(ImageLoader.exec(args[1]));
		String outfile = args[2];
		Image result = null;

		for (int i = 2; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-stretch"))
				input = ContrastStretch.exec(input);
			else if (args[i].equalsIgnoreCase("-color"))
				color = true;
		}

		
		Image outputSamples = WekaClassificationKNN
			.exec(color ? MarkerBasedMultiProbashed.scalarize(RGBToHSY.exec(input))
				: input, samples, 5);
		outputSamples=LabelsToBinaryMasks.exec(outputSamples,true);


		for (int b = 0; b < outputSamples.getBDim(); b++) {
			Image samp = samples.getImage4D(b, Image.B);
			Image clas = outputSamples.getImage4D(b, Image.B);
			Image rec = FastBinaryReconstruction.exec(samp, clas);
			int area = 50;
			// CritÃšre : composante de taille > 50% du marqueur le plus petit
			rec = clas;
			int se = 5;
			// TolÃ©rance d'un pixel d'erreur dans le voisinage
			rec = GrayMedian.exec(rec, FlatStructuringElement2D
				.createSquareFlatStructuringElement(3));
			// On ne conserve que les zones d'au moins 11x11, soit 4m*4m
			rec = BinaryOpening.exec(rec, FlatStructuringElement2D
				.createSquareFlatStructuringElement(11));
			// Erosion pour Ã©viter les marqueurs qui se touchent
			rec = BinaryErosion.exec(rec, FlatStructuringElement2D
				.createSquareFlatStructuringElement(se));
			outputSamples.setImage4D(rec, b, Image.B);
		}

		result = BinaryMasksToLabels.exec((BooleanImage) outputSamples);

		if (result != null)
			ImageSave.exec(result, outfile);
	}
}
