package fr.unistra.pelican.algorithms.io;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Loading of samples images Each image contains samples for one class Samples
 * filnames are stanardiszed If given filename is toto/toto The samples for
 * region 1 are in toto/toto-region0.png The samples for region 2 are in
 * toto/toto-region1.png ...
 * 
 * @author
 */

public class RegionsLoader extends Algorithm {

	/**
	 * Input parameter.
	 */
	public String filename;

	/**
	 * Output parameter.
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public RegionsLoader() {

		super();
		super.inputs = "filename";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		ArrayList<Image> regions = new ArrayList<Image>(30);

		// Load samples images until there is no images left (exception)
		try {
			int i = 0;
			while (true)
				regions.add((Image) new ImageLoader().process(filename
						+ "-region" + (i++) + ".png"));
		} catch (InvalidParameterException e) {
		}

		if (regions.isEmpty())
			throw new AlgorithmException("No regions images found!!");

		// Combine them to the samples image
		Image first = regions.get(0);
		outputImage = new IntegerImage(first.getXDim(), first.getYDim(), 1, 1,
				regions.size());

		try {
			for (int c = 0; c < regions.size(); c++) {
				Image region = regions.get(c);

				region = (Image) new BooleanConnectedComponentsLabeling()
						.process(region, BooleanConnectedComponentsLabeling.CONNEXITY8, true);
				// Viewer2D.exec(ContrastStretchEachBands.process(region),
				// "regions ");
				for (int x = 0; x < outputImage.getXDim(); x++)
					for (int y = 0; y < outputImage.getYDim(); y++)
						outputImage.setPixelXYBInt(x, y, c, region
								.getPixelXYBInt(x, y, 0));
			}
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

		// collect and recycle the garbage
		System.gc();
	}

//	public static void main(String[] args) {
//		String path = "samples/remotesensing1";
//		try {
//			new Viewer2D().process(new ContrastStretchEachBands()
//					.process(new RegionsLoader().process(path)), "regions of "
//					+ path);
//		} catch (InvalidTypeOfParameterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (AlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidNumberOfParametersException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * Loading of samples images Each image contains samples for one class
	 * Samples.
	 * 
	 * @param filename
	 *            Directrory of the image to be loaded.
	 * @return The samples images.
	 */
	public static Image exec(String filename) {
		return (Image) new RegionsLoader().process(filename);
	}
}
