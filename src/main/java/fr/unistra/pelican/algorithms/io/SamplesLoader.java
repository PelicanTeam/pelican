package fr.unistra.pelican.algorithms.io;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Loading of samples images Each image contains samples for one class Samples
 * filenames are stanardiszed If given filename is toto/toto The samples for
 * class 1 are in toto/toto-class0.png The samples for class 2 are in
 * toto/toto-class1.png ...
 * 
 * @author
 */

public class SamplesLoader extends Algorithm {
	
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
	public SamplesLoader() {

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
		ArrayList<Image> samples = new ArrayList<Image>(30);

		// Load samples images until there is no images left (exception)
		try {
			int i = 0;
			while (true)
				samples.add((Image) new ImageLoader().process(filename + "-class" + (i++)
						+ ".png"));
		} catch (InvalidTypeOfParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			// do nothing, stop condition
		} catch (java.lang.IllegalArgumentException e) {
		}

		if (samples.isEmpty())
			throw new AlgorithmException("No samples images found!!");

		// Combine them to the samples image
		Image first = samples.get(0);
		outputImage = new BooleanImage(first.getXDim(), first.getYDim(), 1, 1,
				samples.size());

		for (int c = 0; c < samples.size(); c++) {
			Image sample = samples.get(c);
			for (int x = 0; x < outputImage.getXDim(); x++)
				for (int y = 0; y < outputImage.getYDim(); y++)
					outputImage.setPixelXYBBoolean(x, y, c, sample
							.getPixelXYBBoolean(x, y, 0));
		}

		// collect and recycle the garbage
		System.gc();
	}

	
//	public static void main(String[] args) {
//		String path = "samples/remotesensing1";
//		try {
//			new Viewer2D().process(new SamplesLoader().process(path), "samples of " + path);
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
	 * Loading of samples images Each image contains samples for one class Samples.
	 * 
	 * @param filename Directory of the image to be loaded.
	 * @return The sample image.
	 */
	public static BooleanImage exec(String filename) {
		return (BooleanImage) new SamplesLoader().process(filename);
	}
}
