package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.BinaryFillHole;
import fr.unistra.pelican.algorithms.morphology.binary.hitormiss.BinaryHST;

/**
 * Draw frontiers from a label image without frontiers, and using an elevation image.
 * 
 * @author Lefevre
 */
public class DrawFrontiersFromElevation extends Algorithm {

	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * Elevation image
	 */
	public Image elevation;

	/**
	 * Resulting image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public DrawFrontiersFromElevation() {
		super.inputs = "inputImage,elevation";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */

	public void launch() throws AlgorithmException {
		outputImage = new BooleanImage(inputImage, false);
		int label, label2, val, val2, i, j;
		// Premiere etape, on determine les points de contour
		for (int z = 0; z < inputImage.getZDim(); z++)
			for (int t = 0; t < inputImage.getTDim(); t++)
				for (int b = 0; b < inputImage.getBDim(); b++) {
					for (int y = 0; y < inputImage.getYDim(); y++)
						for (int x = 0; x < inputImage.getXDim(); x++) 	{
							boolean border=false;
							label = inputImage.getPixelInt(x, y, z, t, b);
							if (label == -1)
								continue;
							for (int di = -1; di <= 1; di++)
								for (int dj = -1; dj <= 1; dj++)
									if (di != 0 || dj != 0) {
										i = x + di;
										j = y + dj;
										if (i >= 0 && j >= 0
												&& i < inputImage.getXDim()
												&& j < inputImage.getYDim()) {
											label2 = inputImage.getPixelInt(i,
													j, z, t, b);
											if (label2 != -1 && label2!=label) {
												border=true;
											}
										}
									}
							outputImage
							.setPixelBoolean(x, y, z, t, b, border);
						}
				}
		//Image mask=outputImage.copyImage(true);
		// Deuxieme etape, on selectionne les bons points de contour
		for (int z = 0; z < inputImage.getZDim(); z++)
			for (int t = 0; t < inputImage.getTDim(); t++)
				for (int b = 0; b < inputImage.getBDim(); b++) {
					for (int y = 0; y < inputImage.getYDim(); y++)
						for (int x = 0; x < inputImage.getXDim(); x++) {
							if (!outputImage.getPixelBoolean(x, y, z, t, b))
								continue;
							boolean remove=true;
							label = inputImage.getPixelInt(x, y, z, t, b);
							val = elevation.getPixelInt(x, y, z, t, b);
							for (int di = -1; di <= 1; di++)
								for (int dj = -1; dj <= 1; dj++)
									if (di != 0 || dj != 0) {
										i = x + di;
										j = y + dj;
										if (i >= 0 && j >= 0
												&& i < inputImage.getXDim()
												&& j < inputImage.getYDim()) {
											label2 = inputImage.getPixelInt(i,
													j, z, t, b);
											val2 = elevation.getPixelInt(i, j,
													z, t, b);
//											if (label2 != -1 && val2 > val)
//												remove=false;
											if (label2 != -1 && label2!=label && val2 <= val)
												remove=false;
										}
									}
							outputImage
							.setPixelBoolean(x, y, z, t, b, !remove);
//							.setPixelBoolean(x, y, z, t, b, remove);
						}
				}
		outputImage=BinaryFillHole.exec(outputImage,BinaryFillHole.CONNEXITY4);
		outputImage=BinaryHST.exec(outputImage,5);
		
		
	}

	/*
	 * Draw some frontiers on an image.
	 */
	public static Image exec(Image image, Image elevation) {
		return (Image) new DrawFrontiersFromElevation().process(image,
				elevation);
	}

}
