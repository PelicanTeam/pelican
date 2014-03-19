package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * This classe merge frontier of two frontiers map It proceed that way: For all
 * pixel where the structuring element contains one true pixel on frontier map 1
 * and one on frontier map 2 (not necessary the same) the pixel is mark as
 * frontier.
 * @author Sébastien Derivaux
 */
public class IntersectFrontiers extends Algorithm {
	// Inputs parameters
	public Image frontier1;

	public Image frontier2;

	public BooleanImage se;

	// Outputs parameters
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public IntersectFrontiers() {

		super();
		super.inputs = "frontier1,frontier2,se";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int xDim = frontier1.getXDim();
		int yDim = frontier1.getYDim();
		output = frontier1.copyImage(false);
		output.fill(0.0);

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				if (hasFrontier(frontier1, x, y)
						&& hasFrontier(frontier2, x, y))
					output.setPixelXYBoolean(x, y, true);
	}

	public boolean hasFrontier(Image frontier, int x, int y) {
		for (int i = 0; i < se.getXDim(); i++)
			for (int j = 0; j < se.getYDim(); j++) {
				int valX = x - se.getCenter().x + i;
				int valY = y - se.getCenter().y + j;

				if (se.getPixelXYBoolean(i, j) && valX >= 0 && valX < frontier.getXDim()
						&& valY >= 0 && valY < frontier.getYDim())
					if (frontier.getPixelXYBoolean(valX, valY) == true)
						return true;
			}
		return false;
	}
}
