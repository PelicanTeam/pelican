package fr.unistra.pelican.demos;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.spatial.MedianFilter;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class FilteringDemo {

	/**
	 * the main method of the filtering demo class. this is a very long javadoc
	 * comment just to see how eclipse is performing its layout
	 * 
	 * @param args
	 * @throws InvalidNumberOfParametersException
	 * @throws AlgorithmException
	 * @throws InvalidTypeOfParameterException
	 */
	public static void main(String[] args)
			throws InvalidTypeOfParameterException, AlgorithmException,
			InvalidNumberOfParametersException {
		Image src = ImageLoader
				.exec("/Users/slefevre/Work/workspace/multimedia/res/java.png");
		// src.setColor(false);
		Viewer2D.exec(MedianFilter.exec(src,
				FlatStructuringElement.createSquareFlatStructuringElement(7)),
				"Median");

		Viewer2D.exec(
				GrayGradient.exec(src, FlatStructuringElement2D
						.createSquareFlatStructuringElement(3)), "Morpho");
	}

}
