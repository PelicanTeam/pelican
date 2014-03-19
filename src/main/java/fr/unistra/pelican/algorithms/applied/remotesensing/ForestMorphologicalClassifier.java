package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.conversion.BinaryArrayToLabels;
import fr.unistra.pelican.algorithms.logical.BinaryDifference;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryErosion;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryOpening;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.BinaryGeodesicDilation;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Classify forest areas using morphological operators
 * 
 * Mapping spatial patterns with morphological image processing P. Vogt, K.
 * Ritters, C. Estreguil, J. Kozak, T. Wade, J.D. Wickham Landscape Ecology,
 * 22:171-177, 2007 http://forest.jrc.it/biodiversity/Product/4classweb0.html
 * 
 * @author Sebastien Lefevre, Jonathan Weber
 * 
 */
public class ForestMorphologicalClassifier extends Algorithm {

	/**
	 * Image to be processed
	 */
	public BooleanImage inputImage;

	/**
	 * Size of the structuring element
	 */
	public int size;

	/**
	 * Classified image
	 */
	public IntegerImage outputImage;

	
	private boolean debug = false;

	/**
	 * Constructor
	 * 
	 */
	public ForestMorphologicalClassifier() {
		super.inputs = "inputImage,size";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Initialisation
		BooleanImage forest = inputImage;
		if (debug)
			new  Viewer2D().process(forest, "forest");
		outputImage = new IntegerImage(inputImage);

		// Creation of the SE
		BooleanImage SE1 = FlatStructuringElement2D
				.createSquareFlatStructuringElement(size);
		BooleanImage SE2 = FlatStructuringElement2D
				.createCrossFlatStructuringElement((size - 1) / 2);

		// Step 1 : detect core forest
		BooleanImage core = (BooleanImage) new BinaryErosion().process(forest, SE1);
		if (debug)
			new Viewer2D().process(core, "core");

		// Step 2 : detect patch forest
		BooleanImage patch = (BooleanImage) new BinaryDilation().process(core, SE1);
		patch = (BooleanImage) new  FastBinaryReconstruction().process(patch, forest,
				new FastBinaryReconstruction().CONNEXITY4);
		if (debug)
			new Viewer2D().process(patch, "patch");
		patch = (BooleanImage)new  BinaryDifference().process(forest, patch);
		if (debug)
			new Viewer2D().process(patch, "patch");

		// Step3 : detect edge pixels
		BooleanImage nonforest = (BooleanImage)new  Inversion().process(forest);
		BooleanImage edge = (BooleanImage) new BinaryOpening().process(nonforest, SE1);
		edge = (BooleanImage) new  FastBinaryReconstruction().process(edge, nonforest,
				new FastBinaryReconstruction().CONNEXITY4);
		if (debug)
			new Viewer2D().process(edge, "edge");
		edge = (BooleanImage) new BinaryGeodesicDilation().process(edge, forest, SE1);
		edge = (BooleanImage) new BinaryDifference().process(edge, patch);
		if (debug)
			new Viewer2D().process(edge, "edge");

		// Step4 : detect perforated pixels
		BooleanImage perforated = (BooleanImage)new  BinaryDifference().process(
				forest, core);
		perforated = (BooleanImage)new  BinaryDifference().process(perforated, patch);
		perforated = (BooleanImage)new  BinaryDifference().process(perforated, edge);
		if (debug)
			new Viewer2D().process(perforated, "perforated");

		// Final step : combine binary images into a single one with labels
		BooleanImage[] cluster = new BooleanImage[5];
		cluster[0] = nonforest;
		cluster[1] = core;
		cluster[2] = patch;
		cluster[3] = edge;
		cluster[4] = perforated;
		outputImage = (IntegerImage) new BinaryArrayToLabels().process(cluster);
		
		if (debug)
			new Viewer2D().process(new LabelsToRandomColors().process(outputImage),
					"cluster");

	}
	
	/**
	 * This method Classifies forest areas using morphological operators
	 * @param inputImage  Image to be processed
	 * @param size Size of the structuring element
	 * @return Classified image
	 */
	public static IntegerImage exec(BooleanImage inputImage, Integer size)
	{
		return (IntegerImage) new ForestMorphologicalClassifier().process(inputImage,size);
	}

}
