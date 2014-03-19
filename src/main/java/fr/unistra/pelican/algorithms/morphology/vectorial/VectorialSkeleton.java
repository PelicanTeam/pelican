package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Addition;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class computes the skeleton of a greyscale image using the iterative algorithm of
 * Lanteuejoul,1980
 * 
 * @author Abdullah..?
 * 
 */
public class VectorialSkeleton extends Algorithm
{
	/**
	 * The input image
	 */
	public Image inputImage;

	/**
	 * the structuring element
	 */
	public BooleanImage se;

	/**
	 * The output image
	 */
	public Image outputImage;

	/**
	 * the vector ordering
	 */
	public VectorialOrdering vo;
	
	/**
	 * This class computes the skeleton of a greyscale image using the iterative algorithm of Lanteuejoul,1980
	 * @param image the input image
	 * @param se the structuring element
	 * @param vo the vectorial ordering
	 * @return the vectorial skeleton
	 */
	public static Image exec(Image inputImage, BooleanImage se,VectorialOrdering vo) {
		return (Image) new VectorialSkeleton().process(inputImage,se,vo);
	}
	

	/**
	 * Constructor
	 * 
	 */
	public VectorialSkeleton() {

		super();
		super.inputs = "inputImage,se,vo";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {
			outputImage = inputImage.copyImage(false);
			outputImage.fill(0.0);

			int erosionNbr = 0;

			// find the number of erosions needed in order to fully eliminate
			// the input
			Image tmp = inputImage.copyImage(true);
			Image tmp2 = null;

			do {
				tmp2 = tmp;
				tmp = (Image) new VectorialErosion().process(tmp2, se, vo);
				erosionNbr++;
				System.err.println(erosionNbr);
			} while (isDifferent(tmp, tmp2) == true);

			System.err.println(erosionNbr);

			Image eroded = inputImage;
			for (int i = 0; i < erosionNbr; i++) {
				System.err.println(i);
				eroded = (Image) new VectorialErosion().process(eroded, se, vo);
				Image opened = (Image) new VectorialOpening().process(eroded, se, vo);
				Image diff = (Image) new Difference().process(eroded, opened);
				outputImage = (Image) new Addition().process(outputImage, diff);
			}

		} catch (PelicanException ex) {
			ex.printStackTrace();
		}
	}

	private boolean isDifferent(Image img, Image img2) {

		for (int i = 0; i < img.size(); i++) {

			boolean isHere1 = img.isPresent(i);
			boolean isHere2 = img2.isPresent(i);
			if ( isHere1 && !isHere2 ) return true;
			if ( !isHere1 && !isHere2 ) return true;
			if ( isHere1 && isHere2 ) { 

				int p1 = img.getPixelByte(i);
				int p2 = img2.getPixelByte(i);
				if (p1 != p2) return true;
			}
		}
		return false;
	}

}
