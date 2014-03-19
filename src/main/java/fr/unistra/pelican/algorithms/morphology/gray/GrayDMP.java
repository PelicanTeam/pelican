package fr.unistra.pelican.algorithms.morphology.gray;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.FastGrayReconstruction;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This class represents the grayscale version of the differential morphological
 * profile introduced by Pesaresi & Benediktsson
 * 
 * @author Lefevre
 * 
 */
public class GrayDMP extends Algorithm {
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the size of the DMP, i.e. the number of SE considered
	 */
	public int size;

	/**
	 * the structuring element
	 */
	BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(3);
	/**
	 * a flag to determine if differential MP or the non differential MP is
	 * computed
	 */
	public boolean difference = true;

	/**
	 * a flag to determine if the DMP include openings
	 */
	public boolean openings = true;

	/**
	 * a flag to determine if the DMP include closings
	 */
	public boolean closings = true;

	/**
	 * a flag to determine if geodesic operations (opening/closing by
	 * reconstruction) are used instead of standard ones
	 */
	public boolean geodesic = true;

	/**
	 * the output image
	 */
	public Image output;

	/**
	 * This method computes the grayscale version of the differential
	 * morphological profile
	 * 
	 * @param image
	 *            the input image
	 * @param size
	 *            the size of the DMP, i.e. the number of SE considered
	 * @return the output image
	 */
	public static Image exec(Image input, int size) {
		return (Image) new GrayDMP().process(input, size);
	}

	/**
	 * This method computes the grayscale version of the differential
	 * morphological profile
	 * 
	 * @param image
	 *            the input image
	 * @param size
	 *            the size of the DMP, i.e. the number of SE considered
	 * @return the output image
	 */
	public static Image exec(Image input, int size, BooleanImage se) {
		return (Image) new GrayDMP().process(input, size,se);
	}

	/**
	 * This method computes grayscale version of the differential morphological
	 * profile
	 * 
	 * @param image
	 *            the input image
	 * @param size
	 *            the size of the DMP, i.e. the number of SE considered
	 * @return the output image
	 */
	public static Image exec(Image input, int size, BooleanImage se,
			boolean difference, boolean openings,
			boolean closings, boolean geodesic) {
		return (Image) new GrayDMP().process(input, size, se, difference,
				openings, closings, geodesic);
	}

	/**
	 * Constructor
	 * 
	 */
	public GrayDMP() {
		super.inputs = "input,size";
		super.options = "se,difference,openings,closings,geodesic";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int size2 = 0;
		if (openings)
			size2 += size;
		if (closings)
			size2 += size;
		int length = input.getBDim() * size2;
		output = input.newInstance(input.getXDim(), input.getYDim(), 1, 1,
				length);
		Image marker=input.copyImage(true);
		Image previous, current, diff;
		int i, j;
		// ouvertures
		System.out.print(" ");
		for (previous = input, i = 0; i < size && openings; i++) {
			System.out.print(".");
			// Erosion
			marker=GrayErosion.exec(marker,se);
			// Calcul des reconstructions
			current=marker;
			if (!geodesic)
				for (int ii=0;ii<=i;ii++)
					current = GrayDilation.exec(current, se);
			else 
				current = FastGrayReconstruction.exec(marker,input);
			// Calcul des différences
			if (difference)
				diff = Difference.exec(previous, current);
			else
				diff = current;
			// Mise à jour des buffers
			previous = current;
			// Enregistrement de l'image
			for (int b = 0; b < input.getBDim(); b++)
				output.setImage4D((diff.getImage4D(b, Image.B)), i
						* input.getBDim() + b, Image.B);
		}
		// fermetures
		marker=input.copyImage(true);
		System.out.print(" ");
		for (previous = input, j = i, i = 0; i < size && closings; i++,j++) {
			System.out.print(".");
			// Dilatation
			marker=GrayDilation.exec(marker,se);
			// Calcul des reconstructions
			current=marker;
			if (!geodesic)
				for (int ii=0;ii<=i;ii++)
					current = GrayErosion.exec(current, se);
			else 
				current = FastGrayReconstruction.exec(marker,input,true);
			// Calcul des différences
			if (difference)
				diff = Difference.exec(current, previous);
			else
				diff = current;
			// Mise à jour des buffers
			previous = current;
			// Enregistrement de l'image
			for (int b = 0; b < input.getBDim(); b++)
				output.setImage4D((diff.getImage4D(b, Image.B)), j
						* input.getBDim() + b, Image.B);
		}
		System.out.println();
	}
}
