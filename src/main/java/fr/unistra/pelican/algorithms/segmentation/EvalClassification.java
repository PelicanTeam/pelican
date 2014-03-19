
package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;


/**
 * Evaluate a segmentation given a expert segmentation. The expert segmentation
 * can be multi-band.
 * @author Sebastien Derivaux
 */
public class EvalClassification extends Algorithm {
	public static boolean info = false;

    /*
     * Input Image
     */
	public Image input;

    /*
     * Epert Image
     */
	public Image expert;

    /*
     * Result
     */
	public double result;

	/**
	 * Constructor
	 * 
	 */
	public EvalClassification() {

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

		// Correlation matrix creation
		int correlation[][] = new int[nbClasses][];
		for (int i = 0; i < correlation.length; i++) {
			correlation[i] = new int[nbClasses + 1];
		}

		// Parse expert data
		int total = 0;
		int diag = 0;
		int reject = 0;
		int[] perClasses = new int[nbClasses];
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				for (int c = 0; c < nbClasses; c++)
					if (expert.getPixelXYBBoolean(x, y, c)) {
						perClasses[c]++;
						int label = input.getPixelInt(x, y, 0, 0, 0);
						if (label == -1) {
							correlation[c][nbClasses]++;
							reject++;
						} else
							correlation[c][label]++;
						if (c == label)
							diag++;
						total++;
					}

		if (info) {
			System.out.println("Correlation Matrix\n==================");
			for (int i = 0; i < correlation.length; i++) {
				int[] local = correlation[i];
				for (int j = 0; j < local.length; j++)
					System.out.print(intToString(local[j] * 100 / total, 5));
				System.out.println("");
			}
		}

		double accuracy = 0.0;
		for (int i = 0; i < nbClasses; i++)
			accuracy += (double) correlation[i][i] / (double) perClasses[i];
		accuracy = accuracy / (double) nbClasses * 100.0;
		// System.out.println("Correctly Classified Pixels " + accuracy + "%");

		result = accuracy;

	}

	public String intToString(int num, int width) {

		String numstr = Integer.toString(num);
		while (numstr.length() < width)
			numstr = " " + numstr;
		return numstr;
	}

    /**
     * Evaluate a segmentation
     */
    public double exec(Image input, Image expert){
	return (Double)new EvalClassification().process(input, expert);
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
			Image samples = (Image) new SamplesLoader().process(file);
			Image regions = (Image) new RegionsLoader().process(file);

			// Create regions
			Image result = (Image) new WekaClassification5NN().process(source, samples);

			// View it
			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
					new FrontiersFromSegmentation().process(result)),
					"Segmentation of " + file);
			System.out.println(new EvalClassification().process(result, regions));

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
