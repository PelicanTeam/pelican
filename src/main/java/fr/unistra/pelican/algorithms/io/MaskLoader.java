package fr.unistra.pelican.algorithms.io;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Loading of samples images Each image contains samples for one class Samples
 * filnames are stanardiszed If given filename is toto/toto The samples for
 * region 1 are in toto/toto-region0.png The samples for region 2 are in
 * toto/toto-region1.png ...
 * *
 * @author
 */

public class MaskLoader extends Algorithm {
	
	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public MaskLoader() {

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
		Image mask = null;

		// Load samples images until there is no images left (exception)
		try {
			int i = 0;
				mask = (Image) new ImageLoader().process(filename + ".png");
		} catch (InvalidTypeOfParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.lang.IllegalArgumentException e) {
		}

		if (mask == null)
			throw new AlgorithmException("No regions images found!!");

		outputImage = new BooleanImage(mask.getXDim(), mask.getYDim(), 1, 1, 1);

		try {
			for (int x = 0; x < outputImage.getXDim(); x++)
				for (int y = 0; y < outputImage.getYDim(); y++)
					outputImage.setPixelXYBoolean(x, y, mask.getPixelXYBBoolean(
							x, y, 0));
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

//	public static void main(String[] args) {
//		String path = "samples/remotesensing1";
//		try {
//			new Viewer2D().process(new ContrastStretchEachBands().process(new MaskLoader()
//					.process(path)), "regions of " + path);
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
	 * Loading of samples images Each image contains samples for one class Samples
	 * 
	 * @param filename Filename of the INR image.
	 * @return The samples images.
	 */
	public static Image exec(String filename) {
		return (Image) new MaskLoader().process(filename);
	}
}
