package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.StructuringElement;

/**
 * This class performs a cyclic transform. WORKS ONLY WITH IMAGES OF 2^n PIXELS
 * 
 * @author
 */
public class CyclicTransform extends Algorithm {

	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * The size of the transform, default value -1 for transform until convergence
	 */
	public int size = -1;

	/**
	 * Flag to perform the dual transform
	 */
	public boolean dual = false;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * Default constructor
	 */
	public CyclicTransform() {
		super.inputs = "inputImage";
		super.options = "size,dual";
		super.outputs = "outputImage";

	}

	/**
	 * Performs a cyclic transform
	 * 
	 * @param inputImage
	 *          The input image
	 * @param size
	 *          The size of the transform
	 * @return The output image
	 */
	public static Image exec(Image inputImage, int size, boolean dual) {
		return (Image) new CyclicTransform().process(inputImage, size, dual);
	}

	public static Image exec(Image inputImage, boolean dual) {
		return (Image) new CyclicTransform().process(inputImage, null, dual);
	}

	public static Image exec(Image inputImage, int size) {
		return (Image) new CyclicTransform().process(inputImage, size);
	}

	public static Image exec(Image inputImage) {
		return (Image) new CyclicTransform().process(inputImage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		outputImage = inputImage.copyImage(true);
		Image tmp;
		double v1, v2, min, max;

		// Check the size parameter
		if (size == -1)
			size = inputImage.size();
		while (Math.pow(2, size) > inputImage.size())
			size--;

		// Check if the image size is equal to 2^n
		double log2 = Math.log(inputImage.size()) / Math.log(2);
		// System.out.println(log2);
		if (log2 != (int) log2)
			return;

		// System.out.println("size="+size);
		int comp = inputImage.size() / 2;
		int shift = inputImage.size();
		for (int i = 1; i < size + 1; i++) {
			tmp = outputImage.copyImage(true);
			shift /= 2;
			int j = 0;
			for (int k = 0; k < comp; k++) {
				// System.out.println(k+" "+j+" "+(j+shift));
				v1 = tmp.getPixelDouble(j);
				v2 = tmp.getPixelDouble(j + shift);
				if ((!dual && v1 < v2) || (dual && v1 > v2)) {
					min = v1;
					max = v2;
				} else {
					min = v2;
					max = v1;
				}
				outputImage.setPixelDouble(j, max);
				outputImage.setPixelDouble(j + shift, min);
				j++;
				if (j % shift == 0)
					j += shift;
			}
		}
	}

	public static void main(String args[]) {
		Image img = null;

		// // Translation invariance
		// img=ImageLoader.exec("samples/stample.png");
		// Viewer2D.exec(img);
		// Viewer2D.exec(CyclicTransform.exec(img));

		img = ImageLoader.exec("samples/stample2.png");
		Viewer2D.exec(img);
		Viewer2D.exec(CyclicTransform.exec(img));

		// // Idempotence
		// Viewer2D.exec(CyclicTransform.exec(img));
		// Viewer2D.exec(CyclicTransform.exec(CyclicTransform.exec(img)));

		// // Duality
		// Viewer2D.exec(Inversion.exec(CyclicTransform.exec(Inversion.exec(img))));
		// Viewer2D.exec(CyclicTransform.exec(img,true));

	}

}
