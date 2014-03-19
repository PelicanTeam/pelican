package fr.unistra.pelican.algorithms.segmentation.labels;

import java.awt.Color;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * Transform a label image into a color image using predefined colors
 * 
 * @author Sbastien Derivaux
 * 
 */
public class LabelsToPredefinedColor extends Algorithm {

	// Inputs parameters
	public Image input;

	public Color[] colors;

	// Outputs parameters
	public Image result;

	/**
	 * Constructor
	 * 
	 */
	public LabelsToPredefinedColor() {

		super();
		super.inputs = "input,colors";
		super.outputs = "result";
		
	}

	public static Image exec(Image input,Color[] colors) {
		return (Image) new LabelsToPredefinedColor().process(input,colors);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		// Initialisation
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int zDim = input.getZDim();
		int tDim = input.getTDim();
		result = new ByteImage(xDim, yDim, zDim, tDim, 3);
		result.setColor(true);

		// Colorization
		int label;
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				for (int z = 0; z < zDim; z++)
					for (int t = 0; t < tDim; t++) {
						label = input.getPixelXYZTInt(x, y, z, t);
						if (label < colors.length) {
							result.setPixelByte(x, y, z, t, 0, colors[label]
									.getRed());
							result.setPixelByte(x, y, z, t, 1, colors[label]
									.getGreen());
							result.setPixelByte(x, y, z, t, 2, colors[label]
									.getBlue());
						} else {
							System.err.println("problem with label" + label);
							result.setPixelByte(x, y, z, t, 0, label % 255);
							result.setPixelByte(x, y, z, t, 1, label % 255);
							result.setPixelByte(x, y, z, t, 2, label % 255);
						}
					}
	}

	/*
	 * public static void main(String[] args) { String file =
	 * "samples/watershed.png"; if(args.length > 0) file = args[0];
	 * 
	 * try { // View it
	 * Viewer2D.exec(LabelsToPredefinedColor.process(SegmentByConnexity.process(ImageLoader.exec(file))),
	 * "Segmentation of " + file);
	 *  } catch (InvalidTypeOfParameterException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } catch (AlgorithmException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch
	 * (InvalidNumberOfParametersException e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); } }
	 */
}
