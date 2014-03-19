package fr.unistra.pelican.demos;

import java.util.ArrayList;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.AdditionChecked;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;
import fr.unistra.pelican.algorithms.segmentation.IncompleteMarkerBasedMultiProbashed;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedMultiProbashed;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.labels.MergeLabelsFromClasses;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaClassificationKNN;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.gui.OldDraw2DThread;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class SupervisedWatershedDemo {

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out
				.println("Usage: MarkerBasedMultiProbashedDemo file [output][-stretch][-compare][-colorview]\n"
					+ "- file is the image to be segmented\n"
					+ "- output is the resulting image\n"
					+ "- -stretch to perform a contrast stretch step\n"
					+ "- -thick to display thick region edges\n"
					+ "- -merge to merge connected regions belong to the same class\n"
					+ "- -semi to apply a semi-supervised strategy\n"
					+ "- -compare to compare with classical watershed and supervised classification\n"
					+ "- -colorview to have a color display of the image to be segmented");
			return;
		}
		new SupervisedWatershedDemo(args);
	}

	public SupervisedWatershedDemo(String[] args) {
		boolean compare = false;
		boolean color = false;
		boolean merge = false;
		boolean semi = false;
		boolean thick = false;
		boolean subsampling=false;
		String path = args[0];
		Image input = ImageLoader.exec(path);
		String outfile = null;

		for (int i = 1; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-stretch"))
				input = ContrastStretch.exec(input);
			else if (args[i].equalsIgnoreCase("-subsampling"))
				subsampling = true;
			else if (args[i].equalsIgnoreCase("-semi"))
				semi = true;
			else if (args[i].equalsIgnoreCase("-merge"))
				merge = true;
			else if (args[i].equalsIgnoreCase("-thick"))
				thick = true;
			else if (args[i].equalsIgnoreCase("-compare"))
				compare = true;
			else if (args[i].equalsIgnoreCase("-colorview"))
				color = true;
			else
				outfile = args[i];
		}

		Image disp = input;
		if (input.getBDim() > 3 && color)
			disp = ColorImageFromMultiBandImage.exec(input, 2, 1, 0);
		if (input.getBDim() == 3)
			input.setColor(true);
		if (disp.getBDim() == 3)
			disp.setColor(true);

		OldDraw2DThread d2d;
		d2d = new OldDraw2DThread(disp, "SupervisedWatersedDemo: " + args[0], this);
		new Thread(d2d).start();

		while (d2d.isActive) {

			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}

			if (!d2d.isActive) {
				// TODO: do a cleaner exit
				System.exit(0);
			} else {

				BooleanImage se = FlatStructuringElement2D
					.createSquareFlatStructuringElement(thick ? 3 : 1);

				Image markers = (Image) d2d.output;

				Image samples = LabelsToBinaryMasks.exec(markers);
				long t1 = System.currentTimeMillis();
				Image result = null;
				Image newMarkers = null;
				boolean isColor = (input.getBDim() == 3);
//isColor=false;				
				if (semi) {
					ArrayList results = new IncompleteMarkerBasedMultiProbashed()
						.processAll(input, samples, null, isColor, subsampling);
					result = (Image) results.get(0);
					newMarkers = (Image) results.get(1);
					// Viewer2D.exec(LabelsToRandomColors.exec(newMarkers,true));
					// Viewer2D.exec(LabelsToRandomColors.exec(((ByteImage)markers).copyToIntegerImage(),true));
				} else {
					result = MarkerBasedMultiProbashed.exec(input,
						samples, isColor, subsampling);
					newMarkers = ((ByteImage) markers).copyToIntegerImage();
				}
				if (merge)
					result = MergeLabelsFromClasses.exec(result, newMarkers);

				long t2 = System.currentTimeMillis();
				System.out.println("Supervised segmentation : " + (t2 - t1) / 1000.0
					+ " s");

				Image view1 = LabelsToColorByMeanValue.exec((IntegerImage)result, input);
				Viewer2D.exec(view1, "SupervisedWatersedDemo: regions of " + args[0]);

				BooleanImage frontiers = (BooleanImage) BinaryDilation.exec(
					FrontiersFromSegmentation.exec(result), se);

				BooleanImage frontiers2 = (BooleanImage) FrontiersFromSegmentation
					.exec(convertToIntegerImage(markers));
				frontiers2 = (BooleanImage) OR.exec(frontiers, frontiers2);
				Image view3 = DrawFrontiersOnImage.exec(input, frontiers2);
				Viewer2D.exec(view3,
					"SupervisedWatersedDemo: frontiers with markers of " + args[0]);

				if (outfile != null) {
					result = convertToByteImage(result);
					ImageSave.exec(result, outfile);
				}

				if (compare) {
					result = MarkerBasedMultiProbashed.exec(input,
						samples, 0);
					if (!semi)
						if (merge)
							result = MergeLabelsFromClasses.exec(result, newMarkers);
					view1 = LabelsToColorByMeanValue.exec((IntegerImage)result, input);
					Viewer2D.exec(view1, "SupervisedWatersedDemo (standard): regions of " + args[0]);
					frontiers = (BooleanImage) BinaryDilation.exec(
						FrontiersFromSegmentation.exec(result), se);
					frontiers2 = (BooleanImage) FrontiersFromSegmentation
						.exec(convertToIntegerImage(markers));
					frontiers2 = (BooleanImage) OR.exec(frontiers, frontiers2);
					view3 = DrawFrontiersOnImage.exec(input, frontiers2);
					Viewer2D.exec(view3,
						"SupervisedWatersedDemo (standard): frontiers with markers of " + args[0]);

					result = WekaClassificationKNN.exec(input, samples,
						5);
					view1 = LabelsToColorByMeanValue.exec((IntegerImage)result, input);
					Viewer2D.exec(view1, "SupervisedWatersedDemo (classification): regions of " + args[0]);
					frontiers = (BooleanImage) BinaryDilation.exec(
						FrontiersFromSegmentation.exec(result), se);
					frontiers2 = (BooleanImage) FrontiersFromSegmentation
						.exec(convertToIntegerImage(markers));
					frontiers2 = (BooleanImage) OR.exec(frontiers, frontiers2);
					view3 = DrawFrontiersOnImage.exec(input, frontiers2);
					Viewer2D.exec(view3,
						"SupervisedWatersedDemo (classification): frontiers with markers of " + args[0]);

					Viewer2D.exec(LabelsToRandomColors.exec(result));

				}

			}
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
		ByteImage res = new ByteImage(img.getXDim(), img.getYDim(), img.getZDim(),
			img.getTDim(), img.getBDim());
		for (int p = 0; p < img.size(); p++)
			res.setPixelByte(p, img.getPixelInt(p));
		return res;
	}

}
