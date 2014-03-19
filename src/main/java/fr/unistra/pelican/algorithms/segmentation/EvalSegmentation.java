
package fr.unistra.pelican.algorithms.segmentation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeSet;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.applied.remotesensing.RegionBuilderWatershedClassical;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.RegionsLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Evaluate a segmentation given a expert segmentation. The expert segmentation
 * can be multi-band.
 *
 * @author Sebastien Derivaux
 */
public class EvalSegmentation extends Algorithm {

    /*
     * Input Image
     */
	public Image input;

    /*
     * Expert Image
     */
	public Image expert;

    /*
     * Result evaluation
     */
	public String result;

	// BIG HACK
	public static double eval;

	public static double maxTheoricalAccuracy;

	public static double[] overSegmentation;

	/**
	 * Constructor
	 * 
	 */
	public EvalSegmentation() {

		super();
		super.inputs = "input,expert";
		super.outputs = "result";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int nbClasses = expert.getBDim();

		double overSegmentationGlobal = 0.0;
		double overSegmentationPond = 0.0;
		int nbLabelsTotal = 0;
		overSegmentation = new double[nbClasses];

		// Calculate oversegmentation.
		for (int c = 0; c < nbClasses; c++) {
			int nbLabels = 0;
			// Count the number of labels in the expert band.
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					nbLabels = Math.max(expert.getPixelXYBInt(x, y, c),
							nbLabels);

			// Don't forget the label 0 is background not an effective label.
			LinkedList<Integer> segments = new LinkedList<Integer>();

			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++) {
					int expertLabel = expert.getPixelXYBInt(x, y, c);
					int segment = input.getPixelXYInt(x, y);
					if (expertLabel != 0)
						if (!segments.contains(segment))
							segments.add(segment);
				}

			int nbSegments = segments.size();

			overSegmentation[c] = (double) nbSegments / (double) nbLabels;
			overSegmentationGlobal += overSegmentation[c];
			overSegmentationPond += overSegmentation[c] * nbLabels;
			nbLabelsTotal += nbLabels;
		}

		overSegmentationGlobal /= nbClasses;
		overSegmentationPond /= (double) nbLabelsTotal;

		result = "oversegmentation per class = "
				+ Arrays.toString(overSegmentation) + " \n"
				+ "medium oversegmentation (per class) = "
				+ overSegmentationGlobal + " \n"
				+ "medium oversegmentation (per label) = "
				+ overSegmentationPond + "\n";

		// Calculate undersegmentation and minimum classification error per
		// regions.
		// There may be void segments.
		int nbSegments = 0;
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				nbSegments = Math.max(input.getPixelXYInt(x, y), nbSegments);
		nbSegments++; // Don't forget segment 0

		TreeSet<Integer>[] set = new TreeSet[nbSegments];
		int[][] distribution = new int[nbSegments][];
		for (int i = 0; i < nbSegments; i++) {
			set[i] = new TreeSet<Integer>();
			distribution[i] = new int[nbClasses];
		}

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) {
				for (int c = 0; c < nbClasses; c++)
					if (expert.getPixelXYBInt(x, y, c) != 0) {
						set[input.getPixelXYInt(x, y)].add(c);
						distribution[input.getPixelXYInt(x, y)][c]++;
					}
			}

		int nbSegmentWithLabel = 0;
		int nbLabels = 0;
		int nbPixelExpert = 0;
		int nbPixelClassed = 0;
		for (int i = 0; i < nbSegments; i++) {
			int count = set[i].size();
			if (count > 0) {
				nbSegmentWithLabel++;
				nbLabels += count;
			}
			int max = 0;
			for (int c = 0; c < nbClasses; c++) {
				nbPixelExpert += distribution[i][c];
				max = Math.max(max, distribution[i][c]);
			}
			nbPixelClassed += max;
		}

		result += "Average number of labels per segments = "
				+ (double) nbLabels / (double) nbSegmentWithLabel + " \n";
		result += "Maximum precision for pixel classification = "
				+ (double) nbPixelClassed / (double) nbPixelExpert + " \n";
		double mppc = ((double) nbPixelClassed / (double) nbPixelExpert);
		maxTheoricalAccuracy = mppc;
		eval = 1.0
				/ (overSegmentationPond >= 1.0 ? overSegmentationPond : 1.0)
				* (mppc > 0.95 ? mppc
						: (mppc > 0.90 ? mppc * 0.5 : mppc * 0.01)) * 20.0;
	}

    /*
     * Evaluate a segmentation.
     */
    public String exec(Image input, Image expert) {
	return (String)new EvalSegmentation().process(input, expert);
    }

    /*
	public static void main(String[] args) {
		String file = "./samples/remotesensing1";
		if (args.length > 0)
			file = args[0];
		BooleanImage se3 = FlatStructuringElement2D
				.createSquareFlatStructuringElement(3);

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file + ".png");
			Image regions = (Image) new RegionsLoader().process(file);

			// Create regions
			Image result = (Image) new RegionBuilderWatershedClassical()
					.process(source, 0.20);

			// View it
			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
					new FrontiersFromSegmentation().process(result)),
					"Segmentation of " + file);
			System.out.println(new EvalSegmentation().process(result, regions));

		} catch (InvalidTypeOfParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

}
