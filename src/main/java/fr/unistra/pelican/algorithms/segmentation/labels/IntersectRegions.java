package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;

/**
 * This classe merge frontier of two region map
 * @author Sébastien Derivaux
 */
public class IntersectRegions extends Algorithm {
	// Inputs parameters
	public Image frontier1;

	public Image frontier2;

	// Outputs parameters
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public IntersectRegions() {

		super();
		super.inputs = "frontier1,frontier2";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		Image tmp = new IntegerImage(frontier1.getXDim(), frontier1.getYDim(),
				1, 1, 2);
		int xDim = frontier1.getXDim();
		int yDim = frontier1.getYDim();

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				for (int b = 0; b < 2; b++) {
					if (b == 0)
						tmp.setPixelXYBInt(x, y, b, frontier1.getPixelXYBInt(x,
								y, 0));
					else
						tmp.setPixelXYBInt(x, y, b, frontier2.getPixelXYBInt(x,
								y, 0));
				}

		try {
			output = (Image) new BooleanConnectedComponentsLabeling().process(tmp);
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

	}
}
