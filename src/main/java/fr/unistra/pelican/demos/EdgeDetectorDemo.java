package fr.unistra.pelican.demos;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.edge.Sobel;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class EdgeDetectorDemo {

	/**
	 * @param args
	 * @throws InvalidNumberOfParametersException
	 * @throws AlgorithmException
	 * @throws InvalidTypeOfParameterException
	 */
	public static void main(String[] args)
		throws InvalidTypeOfParameterException, AlgorithmException,
		InvalidNumberOfParametersException {
		Image src = ImageLoader.exec("samples/monsters.png");
		src.setColor(false);
		Viewer2D.exec(ContrastStretch.exec(Sobel.exec(src)), "Sobel");

		Viewer2D.exec(GrayGradient.exec(src, FlatStructuringElement2D
			.createSquareFlatStructuringElement(3)), "Morpho");
	}

}
