package fr.unistra.pelican.demos;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.EuclideanNorm;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.arithmetic.Maximum;
import fr.unistra.pelican.algorithms.arithmetic.Minimum;
import fr.unistra.pelican.algorithms.conversion.BinaryArrayToLabels;
import fr.unistra.pelican.algorithms.conversion.BinaryMasksToLabels;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedMultiProbashed;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershed;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.labels.MergeLabelsFromClasses;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSoftClassification5NN;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class MBWDemo {

	public static void main(String[] args) throws Exception {

		String path = "./samples/";
		String file = "bear1"; // oui
		// String file = "autruche"; // oui
		//String file = "horse2"; // oui
		// String file = "zebre"; // oui
		// String file = "monkey1"; // oui assez bon
		// String file = "horse1"; // assez bon
		// String file = "plane1"; // oui mais bon spatial pur
		// String file = "tiger2"; // oui mais assez bon pour tout
		// String file = "desert"; // oui mais bonne classif
		// String file = "tiger1"; // mauvais
		// String file = "pyramides"; // mauvais

		// String file = "guepard"; // ok
		// String file = "etoile"; // ok
		// String file = "aigle2"; //ok

		String ext = "png";
		file = path + file;
		if (args.length > 0)
			file = args[0];
		if (args.length > 1)
			ext = args[1];

		BooleanImage se1 = FlatStructuringElement2D
			.createSquareFlatStructuringElement(5);

		// Load the image and the samples
		Image source = ImageLoader.exec(file + "." + ext);
		BooleanImage samples = SamplesLoader.exec(file);
		// source = GrayMedian.process(source, se1);
		// Viewer2D.exec(source, "Source");

		// Comparison with classical MBW

		BooleanImage se2 = FlatStructuringElement2D
			.createSquareFlatStructuringElement(3);
		Image relief = GrayGradient.exec(source, se2);
		// relief=AverageChannels.process(relief);
		relief = EuclideanNorm.exec(relief);
		relief = AdditionConstantChecked.exec(relief, 1.0/255);
		Image marker=samples.getImage4D(0,Image.B);
		for(int i=1;i<samples.getBDim();i++)
			marker=Maximum.exec(marker,samples.getImage4D(i,Image.B));
		marker = Inversion.exec(marker);
		relief = Minimum.exec(relief, new ByteImage(marker));
		Image mbww = MarkerBasedWatershed.exec(relief);
		Viewer2D.exec(DrawFrontiersOnImage.exec(source, FrontiersFromSegmentation
			.exec(mbww)), "MBW " + file);

		// Apply Probashed
		boolean frontiers = true;
		int thickness = 3;
		boolean single = false;
		boolean compare = true;
		boolean merge = true;

		Image im;
		if (!single) {
			// Compare with classifier
			Image proba = (Image) new WekaSoftClassification5NN().process(source,
				samples);
			proba = ContrastStretch.exec(proba);
			Image classif = segmentFromProbabilities(proba);
			Viewer2D.exec(classif, "Classification");
			ImageSave.exec(classif, file + "-res-classif.png");

			if (frontiers)
				im = DrawFrontiersOnImage.exec(source, (BooleanImage) BinaryDilation
					.exec(FrontiersFromSegmentation.exec(MarkerBasedMultiProbashed.exec(
						source, samples, 0.25)), FlatStructuringElement2D
						.createSquareFlatStructuringElement(thickness)));
			else
				im = LabelsToColorByMeanValue.exec(MarkerBasedMultiProbashed.exec(
					source, samples, 0.25), source);
			Viewer2D.exec(im, "marker-based probashed more spatial");
			ImageSave.exec(im, file + "-res-more-spatial.png");

			if (frontiers)
				im = DrawFrontiersOnImage.exec(source, (BooleanImage) BinaryDilation
					.exec(FrontiersFromSegmentation.exec(MarkerBasedMultiProbashed.exec(
						source, samples, 0.5)), FlatStructuringElement2D
						.createSquareFlatStructuringElement(thickness)));
			else
				im = LabelsToColorByMeanValue.exec(MarkerBasedMultiProbashed.exec(
					source, samples, 0.5), source);

			Viewer2D.exec(im, "marker-based probashed");
			ImageSave.exec(im, file + "-res-mixed.png");

			if (frontiers)
				im = DrawFrontiersOnImage.exec(source, (BooleanImage) BinaryDilation
					.exec(FrontiersFromSegmentation.exec(MarkerBasedMultiProbashed.exec(
						source, samples, 0.75)), FlatStructuringElement2D
						.createSquareFlatStructuringElement(thickness)));
			else
				im = LabelsToColorByMeanValue.exec(MarkerBasedMultiProbashed.exec(
					source, samples, 0.75), source);
			Viewer2D.exec(im, "marker-based probashed more spectral");
			ImageSave.exec(im, file + "-res-more-spectral.png");

			if (frontiers)
				im = DrawFrontiersOnImage.exec(source, (BooleanImage) BinaryDilation
					.exec(FrontiersFromSegmentation.exec(MarkerBasedMultiProbashed.exec(
						source, samples, 1.0)), FlatStructuringElement2D
						.createSquareFlatStructuringElement(thickness)));
			else
				im = LabelsToColorByMeanValue.exec(MarkerBasedMultiProbashed.exec(
					source, samples, 1.0), source);
			Viewer2D.exec(im, "spectral only");
			ImageSave.exec(im, file + "-res-only-spectral.png");

		}
		if (compare) {
			if (frontiers)
				im = DrawFrontiersOnImage.exec(source, (BooleanImage) BinaryDilation
					.exec(FrontiersFromSegmentation.exec(MarkerBasedMultiProbashed.exec(
						source, samples, 0.0)), FlatStructuringElement2D
						.createSquareFlatStructuringElement(thickness)));
			else
				im = LabelsToColorByMeanValue.exec(MarkerBasedMultiProbashed.exec(
					source, samples, 0.0), source);
			Viewer2D.exec(im, "spatial only");
			ImageSave.exec(im,file + "-res-only-spatial.png");
		}
		long t1 = System.currentTimeMillis();
		IntegerImage mbw = MarkerBasedMultiProbashed.exec(source, samples);
		if (merge)
			mbw = MergeLabelsFromClasses.exec(mbw, BinaryMasksToLabels.exec(samples));
		if (frontiers)
			im = DrawFrontiersOnImage.exec(source, (BooleanImage) BinaryDilation
				.exec(FrontiersFromSegmentation.exec(mbw), FlatStructuringElement2D
					.createSquareFlatStructuringElement(thickness)));
		else
			im = LabelsToColorByMeanValue.exec(mbw, source);
		long t2 = System.currentTimeMillis();
		System.out
			.println("Supervised segmentation : " + (t2 - t1) / 1000.0 + " s");
		Viewer2D.exec(im, "experimental multiplied");
		ImageSave.exec(im,file + "-res-multiplied.png");

	}

	public static Image segmentFromProbabilities(Image i1) {
		int bdim = i1.getBDim();
		boolean color = false;
		if (bdim < 3) {
			bdim = 3;
			color = true;
		}
		Image i2 = new ByteImage(i1.getXDim(), i1.getYDim(), i1.getZDim(), i1
			.getTDim(), bdim);
		i2.setColor(color);
		boolean ok;
		for (int x = 0; x < i1.getXDim(); x++)
			for (int y = 0; y < i1.getYDim(); y++)
				for (int z = 0; z < i1.getZDim(); z++)
					for (int t = 0; t < i1.getTDim(); t++)
						for (int b = 0; b < i1.getBDim(); b++) {
							ok = true;
							for (int b2 = 0; b2 < i1.getBDim(); b2++)
								if (b2 != b
									&& i1.getPixelDouble(x, y, z, t, b2) > i1.getPixelDouble(x,
										y, z, t, b))
									ok = false;
							i2.setPixelBoolean(x, y, z, t, b, ok);
						}
		return i2;

	}

}