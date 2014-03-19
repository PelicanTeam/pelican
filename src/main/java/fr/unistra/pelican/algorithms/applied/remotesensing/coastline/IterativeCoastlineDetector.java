package fr.unistra.pelican.algorithms.applied.remotesensing.coastline;

import java.awt.Point;
import java.util.Arrays;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.BinaryClosingByReconstruction;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.BinaryFillHole;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.BinaryOpeningByReconstruction;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionSize;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.demos.applied.remotesensing.CoastlineDetectionDemo;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Perform coastline detection using an iterative spectral and spatial approach
 * 
 * @author Lefevre
 */
public class IterativeCoastlineDetector extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image input;

	/**
	 * Resulting image
	 */
	public Image output;

	private boolean debug=false;
	
	/**
	 * Constructor
	 */
	public IterativeCoastlineDetector() {
		super.inputs = "input";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		Image band;
		BooleanImage binary;
		IntegerImage labels;
		int thrBest, labelBest, bandBest;
		int arrayLabels[];
		labelBest = 0;
		thrBest = 0;
		bandBest = 0;
		// Calcul du meilleur seuillage
		for (int b = 0; b < input.getBDim(); b++) {
			band = input.getImage4D(b, Image.B);
			System.out.print("|");
			for (int thr = 0; thr < 255; thr++) {
				if (thr%25==0)
					System.out.print(".");
				binary = ManualThresholding.exec(band, thr / 255.0);
				binary=(BooleanImage) BinaryFillHole.exec(binary);
				binary=(BooleanImage) BinaryFillHole.exec(binary.getComplement());
				labels = BooleanConnectedComponentsLabeling.exec(binary,true);
				arrayLabels = RegionSize.exec(labels);
				Arrays.sort(arrayLabels);
				if (arrayLabels.length > 1
					&& arrayLabels[arrayLabels.length - 2] > labelBest) {
					labelBest = arrayLabels[arrayLabels.length - 2];
					thrBest = thr;
					bandBest = b;
				}
			}
		}
		System.out.println("\nbest threshold=" + thrBest + " (pixels=" + labelBest
			+ ")");
		if (debug)
		Viewer2D.exec(ManualThresholding.exec(input.getImage4D(bandBest, Image.B),
			thrBest / 255.0), "b=" + bandBest + " t=" + thrBest);
		// Calcul du meilleur filtrage
		binary = ManualThresholding.exec(input.getImage4D(bandBest, Image.B),
			thrBest / 255.0);
		int radius = 3;
		do {
			binary = (BooleanImage) BinaryClosingByReconstruction.exec(binary,
				FlatStructuringElement2D.createCircleFlatStructuringElement(radius));
			binary = (BooleanImage) BinaryOpeningByReconstruction.exec(binary,
				FlatStructuringElement2D.createCircleFlatStructuringElement(radius));
			labels = BooleanConnectedComponentsLabeling.exec(binary);
			arrayLabels = RegionSize.exec(labels);
			radius+=2;
			int sum=0;
			for (int s=0;s<arrayLabels.length-2;s++)
				sum+=arrayLabels[s];
			System.out.println(sum+" pixels remaining");
		} while (arrayLabels.length > 2);
		System.out.println("\nbest filter radius=" + radius);
		if (debug)
		Viewer2D.exec(binary, "best filter radius=" + radius);
	}

	/**
	 * Method that applies the double threshold coastline detection
	 * 
	 * @param marker
	 *          Marker image
	 * @param mask
	 *          Mask image
	 * @return image with coastline
	 */
	public static Image exec(Image input) {
		return (Image) new IterativeCoastlineDetector().process(input);
	}

	public static void main(String[] args) {
		String path = "/home/miv/lefevre/data/teledetection/ecosgil/isprs/";
		String res = "iterative/";
		String file = "extrait1_VillervilleQB2_marais.hdr";
		if (args.length!=0) {
			path=args[0];
			file=args[1];
			res=args[2];
		}
		Image input = ImageLoader.exec(path + file);
		input = CoastlineDetectionDemo.addBands(input);
		long t1=System.currentTimeMillis();
		Image result=IterativeCoastlineDetector.exec(input);
		long t2=System.currentTimeMillis();
		System.out.println("IterativeCoastlineDetector "+(t2-t1)/1000+" seconds");
		ImageSave.exec(result,path+res+file+".png");

	}
}
