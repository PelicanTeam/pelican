package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AbsoluteDifference;
import fr.unistra.pelican.algorithms.morphology.gray.GrayClosing;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOpening;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.GrayClosingByReconstruction;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.GrayOpeningByReconstruction;
import fr.unistra.pelican.algorithms.morphology.vectorial.geodesic.VectorialClosingByReconstruction;
import fr.unistra.pelican.algorithms.morphology.vectorial.geodesic.VectorialOpeningByReconstruction;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class represents the vectorial version of the differential morphological
 * profile introduced by Pesaresi & Benediktsson
 * 
 * @author Lefevre
 * 
 */
public class VectorialDMP extends Algorithm {
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the vectorial ordering, null for marginal approach
	 */
	public VectorialOrdering vo;

	/**
	 * the size of the DMP, i.e. the number of SE considered
	 */
	public int size;

	/**
	 * a flag to determine if differential MP or the non differential MP is
	 * computed
	 */
	public boolean difference = true;

	/**
	 * a flag to determine if geodesic operations (opening/closing by
	 * reconstruction) are used instead of standard ones
	 */
	public boolean geodesic = true;

	/**
	 * the structuring element
	 */
	BooleanImage se = null;

	/**
	 * a flag to determine if the DMP include openings
	 */
	public boolean openings = true;

	/**
	 * a flag to determine if the DMP include closings
	 */
	public boolean closings = true;

	/**
	 * a flag to determine if the DMP is symmetric (large SE are at the edges of
	 * the series)
	 */
	public boolean reverse = true;

	public int dimension = Image.B;

	/**
	 * the output image
	 */
	public Image output;

	/**
	 * This method computes the vectorial version of the differential
	 * morphological profile
	 * 
	 * @param image
	 *          the input image
	 * @param vo
	 *          the vectorial ordering, null for marginal approach
	 * @param size
	 *          the size of the DMP, i.e. the number of SE considered
	 * @return the output image
	 */
	public static Image exec(Image input, VectorialOrdering vo, int size) {
		return (Image) new VectorialDMP().process(input, vo, size);
	}

	/**
	 * This method computes the vectorial version of the differential
	 * morphological profile
	 * 
	 * @param image
	 *          the input image
	 * @param vo
	 *          the vectorial ordering, null for marginal approach
	 * @param size
	 *          the size of the DMP, i.e. the number of SE considered
	 * @param dimension
	 *          the dimension used to store the DMP : Image.Z, Image.T, or Image.B
	 * @return the output image
	 */
	public static Image exec(Image input, VectorialOrdering vo, int size,
		int dimension) {
		return (Image) new VectorialDMP().process(input, vo, size, dimension);
	}

	/**
	 * This method computes vectorial version of the differential morphological
	 * profile
	 * 
	 * @param image
	 *          the input image
	 * @param vo
	 *          the vectorial ordering, null for marginal approach
	 * @param size
	 *          the size of the DMP, i.e. the number of SE considered
	 * @param difference
	 *          a flag to determine if differential MP or the non differential MP
	 *          is computed
	 * @param geodesic
	 *          a flag to determine if geodesic operations (opening/closing by
	 *          reconstruction) are used instead of standard ones
	 * @param openings
	 *          a flag to determine if the DMP include openings
	 * @param closings
	 *          a flag to determine if the DMP include closings
	 * @param reverse
	 *          a flag to determine if the DMP is symmetric (large SE are at the
	 *          edges of the series)
	 * @return the output image
	 */
	public static Image exec(Image input, VectorialOrdering vo, int size,
		boolean difference, boolean geodesic, boolean openings, boolean closings,
		boolean reverse) {
		return (Image) new VectorialDMP().process(input, vo, size, null,
			difference, geodesic, openings, closings, reverse);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialDMP() {
		super.inputs = "input,vo,size";
		super.options = "dimension,difference,geodesic,openings,closings,reverse";
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
		int length;
		switch (dimension) {
		case Image.Z:
			length = input.getZDim() * size2;
			output = input.newInstance(input.getXDim(), input.getYDim(), length,
				input.getTDim(), input.getBDim());
			break;
		case Image.T:
			length = input.getTDim() * size2;
			output = input.newInstance(input.getXDim(), input.getYDim(), input
				.getZDim(), length, input.getBDim());
			break;
		case Image.B:
			length = input.getBDim() * size2;
			output = input.newInstance(input.getXDim(), input.getYDim(), input
				.getZDim(), input.getTDim(), length);
			break;
		}
		Image previous, current, diff;
		int i, j;
		// ouvertures
		System.out.print(" ");
		for (previous = input, i = 0; i < size && openings; i++) {
			System.out.print(".");
			// se = FlatStructuringElement
			// .createCircleFlatStructuringElement(i + 1);
			se = FlatStructuringElement2D
				.createSquareFlatStructuringElement(2 * (i + 1) + 1);
			// Calcul des reconstructions
			if (!geodesic && vo == null)
				current = GrayOpening.exec(input, se);
			else if (!geodesic && vo != null)
				current = VectorialOpening.exec(input, se, vo);
			else if (geodesic && vo == null)
				current = GrayOpeningByReconstruction.exec(input, se);
			else
				// (geodesic && vo!=null)
				current = VectorialOpeningByReconstruction.exec(input, se, vo, 1);
			// Calcul des différences
			if (difference)
				diff = AbsoluteDifference.exec(previous, current);
			else
				diff = current;
			// Mise à jour des buffers
			previous = current;
			// Enregistrement de l'image
			int ii = i;
			if (reverse && openings && closings)
				ii = size - 1 - i;
			switch (dimension) {
			case Image.Z:
				for (int z = 0; z < input.getZDim(); z++)
					output.setImage4D((diff.getImage4D(z, Image.Z)), ii * input.getZDim()
						+ z, Image.Z);
				break;
			case Image.T:
				for (int t = 0; t < input.getTDim(); t++)
					output.setImage4D((diff.getImage4D(t, Image.T)), ii * input.getTDim()
						+ t, Image.T);
				break;
			case Image.B:
				for (int b = 0; b < input.getBDim(); b++)
					output.setImage4D((diff.getImage4D(b, Image.B)), ii * input.getBDim()
						+ b, Image.B);
				break;
			}
		}
		// fermetures
		System.out.print(" ");
		for (previous = input, j = i, i = 0; i < size && closings; i++, j++) {
			System.out.print(".");
			// se = FlatStructuringElement
			// .createCircleFlatStructuringElement(i + 1);
			se = FlatStructuringElement2D
				.createSquareFlatStructuringElement(2 * (i + 1) + 1);
			// Calcul des reconstructions
			if (!geodesic && vo == null)
				current = GrayClosing.exec(input, se);
			else if (!geodesic && vo != null)
				current = VectorialClosing.exec(input, se, vo);
			else if (geodesic && vo == null)
				current = GrayClosingByReconstruction.exec(input, se);
			else
				// (geodesic && vo!=null)
				current = VectorialClosingByReconstruction.exec(input, se, vo, 1);
			// Calcul des différences
			if (difference)
				diff = AbsoluteDifference.exec(current, previous);
			else
				diff = current;
			// Mise à jour des buffers
			previous = current;
			// Enregistrement de l'image
			switch (dimension) {
			case Image.Z:
				for (int z = 0; z < input.getZDim(); z++)
					output.setImage4D((diff.getImage4D(z, Image.Z)), j * input.getZDim()
						+ z, Image.Z);
				break;
			case Image.T:
				for (int t = 0; t < input.getTDim(); t++)
					output.setImage4D((diff.getImage4D(t, Image.T)), j * input.getTDim()
						+ t, Image.T);
				break;
			case Image.B:
				for (int b = 0; b < input.getBDim(); b++)
					output.setImage4D((diff.getImage4D(b, Image.B)), j * input.getBDim()
						+ b, Image.B);
				break;
			}
		}
		System.out.println();
	}
}
