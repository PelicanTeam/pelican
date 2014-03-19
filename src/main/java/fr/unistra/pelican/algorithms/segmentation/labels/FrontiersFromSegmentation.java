package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.LinkedList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;


/**
 * This class create a binary image with frontier from a label image. A frontier
 * is a pixel where the 4-connexity neighborhood where all differents labels
 * come from smaller regions. Dimensions: X, Y
 * 
 * @author Sebastien Derivaux, Jonathan Weber
 */
public class FrontiersFromSegmentation extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 */
	public FrontiersFromSegmentation() {
		super.inputs = "inputImage";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), inputImage.getBDim());

		LinkedList<Integer> list = new LinkedList<Integer>();
		int[] regionSize = null;
		try {
			regionSize = (int[]) new RegionSize().process(inputImage);
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

		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int zDim = outputImage.getZDim();
		int tDim = outputImage.getTDim();
		
		for(int t = 0; t< tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++)
					{
						int curentLabel = inputImage.getPixelXYZTInt(x, y, z, t);
						list.clear();

						if (x > 0)
							list.add(inputImage.getPixelXYZTInt(x - 1, y, z, t));
						if (y > 0)
							list.add(inputImage.getPixelXYZTInt(x, y - 1, z, t));
						if (z > 0)
							list.add(inputImage.getPixelXYZTInt(x, y, z - 1, t));
						if (t > 0)
							list.add(inputImage.getPixelXYZTInt(x, y, z, t - 1));
					
						if (x < xDim - 1)
							list.add(inputImage.getPixelXYZTInt(x + 1, y, z, t));
						if (y < yDim - 1)
							list.add(inputImage.getPixelXYZTInt(x, y + 1, z, t));
						if (z < zDim - 1)
							list.add(inputImage.getPixelXYZTInt(x, y, z + 1, t));
						if (t < tDim - 1)
							list.add(inputImage.getPixelXYZTInt(x, y, z, t + 1));

						int maxSize = -1;

						for (int i : list)
							if (i != curentLabel)
								maxSize = Math.max(maxSize, regionSize[i]);

						if (maxSize != -1 && maxSize < regionSize[curentLabel])
							outputImage.setPixelXYZTBoolean(x, y, z, t, true);
						else
							outputImage.setPixelXYZTBoolean(x, y, z, t, false);
				}
	}

	/*public static void main(String[] args) {
		String file = "samples/detection_test2.png";
		if (args.length > 0)
			file = args[0];

		try {
			// Load the image
			Image source = ((Image) new ImageLoader().process(file))
				.getByteChannelZTB(0, 0, 0);
			new Viewer2D().process(source, "Image " + file);

			// Create regions
			Image result = (Image) new DeleteFrontiers().process(new Watershed()
				.process(source));

			// View it
			new Viewer2D().process(new FrontiersFromSegmentation().process(result),
				"Frontiers of " + file);

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

	/**
	 * Return an BooleanImag of the frontiers of a segmentation
	 */
	public static BooleanImage exec(Image segmentation) {
		return (BooleanImage) new FrontiersFromSegmentation().process(segmentation);
	}
}
