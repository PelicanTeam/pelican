package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.TreeSet;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * Convert a label image to a set of binary mask using the multiple image bands
 * 
 * @author lefevre
 */
public class LabelsToBinaryMasks extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Output image
	 */
	public Image outputImage;

	/**
	 * Optional flag to also display the background
	 */
	public boolean background = false;

	/**
	 * Constructor
	 * 
	 */
	public LabelsToBinaryMasks() {

		super.inputs = "inputImage";
		super.outputs = "outputImage";
		super.options = "background";

	}

	public static Image exec(Image inputImage) {
		return (Image) new LabelsToBinaryMasks().process(inputImage);
	}

	public static Image exec(Image inputImage, boolean background) {
		return (Image) new LabelsToBinaryMasks().process(inputImage, background);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */

	public void launch() throws AlgorithmException {

		// Count the number of labels
		// int max=0;
		TreeSet<Object> set = new TreeSet<Object>();
		if (inputImage instanceof ByteImage) {
			for (int p = 0; p < inputImage.size(); p++)
				if (background || inputImage.getPixelByte(p) != 0)
					set.add(inputImage.getPixelByte(p));
			// if (inputImage.getPixelByte(p)>max)
			// max=inputImage.getPixelByte(p);

		} else {
			for (int p = 0; p < inputImage.size(); p++)
				if (background || inputImage.getPixelInt(p) != 0)
					set.add(inputImage.getPixelInt(p));
			// if (inputImage.getPixelInt(p)>max)
			// max=inputImage.getPixelInt(p);
		}

		// Build the output image
		// outputImage=new
		// BooleanImage(inputImage.getXDim(),inputImage.getYDim(),inputImage.getZDim(),inputImage.getTDim(),max);
		outputImage = new BooleanImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), set.size());
		int val;
		if (inputImage instanceof ByteImage) {
			for (int x = 0; x < inputImage.getXDim(); x++)
				for (int y = 0; y < inputImage.getYDim(); y++)
					for (int z = 0; z < inputImage.getZDim(); z++)
						for (int t = 0; t < inputImage.getTDim(); t++) {
							val = inputImage.getPixelXYZTByte(x, y, z, t);
							if (background || val != 0)
								// outputImage.setPixelXYZTBBoolean(x,y,z,t,val-1,true);
								outputImage.setPixelXYZTBBoolean(x, y, z, t, set.headSet(val)
									.size(), true);
						}
		} else {
			for (int x = 0; x < inputImage.getXDim(); x++)
				for (int y = 0; y < inputImage.getYDim(); y++)
					for (int z = 0; z < inputImage.getZDim(); z++)
						for (int t = 0; t < inputImage.getTDim(); t++) {
							val = inputImage.getPixelXYZTInt(x, y, z, t);
							if (background || val != 0)
								// outputImage.setPixelXYZTBBoolean(x,y,z,t,val-1,true);
								outputImage.setPixelXYZTBBoolean(x, y, z, t, set.headSet(val)
									.size(), true);
						}
		}

	}

}
