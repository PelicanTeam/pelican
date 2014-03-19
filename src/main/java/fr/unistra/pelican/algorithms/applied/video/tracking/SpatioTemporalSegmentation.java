package fr.unistra.pelican.algorithms.applied.video.tracking;



import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.Minimum;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.io.VideoLoader;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershedND;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * This class performs spatio-temporal segmentation of video sequence based on
 * the a marker-based watershed (markers are gradient pixels with a value lower
 * than a threshold
 * 
 * @author Sébatien Lefèvre
 */
public class SpatioTemporalSegmentation extends Algorithm {

	/**
	 * The input image sequence
	 */
	public Image inputSequence;

	/**
	 * The threshold used to determine the markers
	 */
	public double threshold=0.5;

	/**
	 * The output image sequence
	 */
	public Image outputSequence;

	/**
	 * (optional) The minimum size of the regions
	 */
	public int minSize;
	
	/**
	 * Default constructor
	 */
	public SpatioTemporalSegmentation() {
		super.inputs = "inputSequence,threshold";
		super.options="minSize";
		super.outputs = "outputSequence";
	}

	/**
	 * performs spatio-temporal segmentation of video sequence based on the a
	 * marker-based watershed (markers are gradient pixels with a value lower than
	 * a threshold
	 * 
	 * @param inputSequence
	 *          The input image sequence
	 * @param threshold
	 *          tHE threshold used to determine the markers
	 * @return The output image sequence
	 */
	public static Image exec(Image inputSequence, double threshold) {
		return (Image) new SpatioTemporalSegmentation().process(inputSequence,
			threshold);
	}

	public static Image exec(Image inputSequence, double threshold,int minSize) {
		return (Image) new SpatioTemporalSegmentation().process(inputSequence,
			threshold,minSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		// Create the SE used in the gradient
		BooleanImage se = new BooleanImage(3, 3, 1, 3, 1);
		se.setPixelXYZBoolean(1, 1, 1, true);
		se.setPixelXYZBoolean(0, 1, 1, true);
		se.setPixelXYZBoolean(1, 0, 1, true);
		se.setPixelXYZBoolean(1, 1, 0, true);
		se.setPixelXYZBoolean(2, 1, 1, true);
		se.setPixelXYZBoolean(1, 2, 1, true);
		se.setPixelXYZBoolean(1, 1, 2, true);
		se.resetCenter();
		Image grad = GrayGradient.exec(inputSequence, se);
		grad = AdditionConstantChecked.exec(grad, 1.0 / 255);
		// Compute the markers
		BooleanImage markers = ManualThresholding.exec(grad, threshold);
		//Viewer2D.exec(markers);
		grad = Minimum.exec(grad, markers);
		outputSequence= MarkerBasedWatershedND.exec(grad,minSize);

	}

	public static void main(String[] args) {
		// Load video
		String path = "/home/miv/lefevre/data/";
		String filename = "tennis.avi";
		// 0-23 => balle
		// 24-87 => premier joueur
		// 88-135 => second joueur
		Image input = VideoLoader.exec(path + filename);
		input = AverageChannels.exec(input);
		input = GrayMedian.exec(input, 3);
		Viewer2D.exec(input, "Video");
		double thr=5.0/255;
		int min=0;
		Image res=SpatioTemporalSegmentation.exec(input,thr,min);
		Viewer2D.exec(LabelsToRandomColors.exec(res));
	}

}
