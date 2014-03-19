package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;

/**
 * A nice mean filter...where it all once started.
 * 
 * @author Lefevre
 */
public class MeanFilter extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Size of the filter
	 */
	public int size;
	
	/**
	 * Ouput image
	 */
	public Image output;

	/**
	 * Structuring element
	 */
	private GrayStructuringElement kernel;

	/**
	 * Constructor
	 * 
	 */
	public MeanFilter() {

		super();
		super.inputs = "input,size";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		kernel = GrayStructuringElement
				.createSquareFlatStructuringElement(size);
		kernel.fill(1.0 / (double) (size * size));

		output = (Image) new Convolution().process(input, kernel);
	}

	/**
	 * A nice mean filter...where it all once started
	 * @param input Input image
	 * @param size Size of the filter
	 * @return Output image
	 */
	public static <T extends Image> T exec(T input, int size) {
		return (T) new MeanFilter().process(input,size);
	}
}
