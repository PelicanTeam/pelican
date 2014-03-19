package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.logical.AND;
import fr.unistra.pelican.algorithms.logical.BinaryDifference;
import fr.unistra.pelican.algorithms.logical.CompareImage;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;


/**
 * This class computes some quality measures (pixel-based, region-based, and
 * mixed) between a binary detection result and a reference image, the results
 * are gathered into an output string
 * 
 * @author Lefevre
 */

public class DetectionQualityEvaluation extends Algorithm {

	/**
	 * The first image
	 */
	public Image input;

	/**
	 * The second image
	 */
	public Image reference;

	/**
	 * A flag to include pixel-based evaluation
	 */
	public boolean pixel = true;

	/**
	 * A flag to include region-based evaluation
	 */
	public boolean region = true;

	/**
	 * A flag to include mixed evaluation
	 */
	public boolean mixed = true;

	/**
	 * The output string
	 */
	public String output;
	
	/**
	 * The temporary StringBuffer used to build the output String
	 */
	private StringBuffer out;

	/**
	 * This class computes some quality measures (pixel-based, region-based, and
	 * mixed) between a binary detection result and a reference image, the
	 * results are gathered into an output string
	 * 
	 * @param input
	 *            The detection result to be evaluated
	 * @param reference
	 *            The reference image
	 * @return The string containing evaluation results
	 */
	public static String exec(Image input, Image reference) {
		return (String) new DetectionQualityEvaluation().process(input,
				reference);
	}

	/**
	 * This class computes some quality measures (pixel-based, region-based, and
	 * mixed) between a binary detection result and a reference image, the
	 * results are gathered into an output string
	 * 
	 * @param input
	 *            The detection result to be evaluated
	 * @param reference
	 *            The reference image
	 * @param pixel
	 *            A flag to include pixel-based evaluation
	 * @param region
	 *            A flag to include region-based evaluation
	 * @param mixed
	 *            A flag to include mixed evaluation
	 * @return The string containing evaluation results
	 */
	public static String exec(Image input, Image reference, boolean pixel,
			boolean region, boolean mixed) {
		return (String) new DetectionQualityEvaluation().process(input,
				reference, pixel, region, mixed);
	}

	/**
	 * Constructor
	 * 
	 */
	public DetectionQualityEvaluation() {
		super.inputs = "input,reference";
		super.options = "pixel,region,mixed";
		super.outputs = "output";
		super.help = "computes some quality measures (pixel-based, region-based, and mixed)"
				+ " between a binary detection result and a reference image, "
				+ "the results are gathered into an output string\n"
				+ "fr.unistra.pelican.Image evaluated image\n"
				+ "fr.unistra.pelican.Image reference image\n"
				+ "\n"
				+ "java.lang.Boolean flag to include pixel-based evaluation\n"
				+ "java.lang.Boolean flag to include region-based evaluation\n"
				+ "java.lang.Boolean flag to include mixed evaluation\n"
				+ "\n"
				+ "java.lang.String evaluation results\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		out = new StringBuffer();
		if (pixel)
			evaluatePixel();
		if (region)
			evaluateRegion();
		if (mixed)
			evaluateMixte();
		output=out.toString();
	}

	private void evaluatePixel() {
		double total = input.size();
		// Classif
		double batiClassif = 0;
		for (int p = 0; p < input.size(); p++)
			if (input.getPixelBoolean(p))
				batiClassif++;
		double fondClassif = input.size() - batiClassif;
		// Terrain
		double batiTerrain = 0;
		for (int p = 0; p < reference.size(); p++)
			if (reference.getPixelBoolean(p))
				batiTerrain++;
		double fondTerrain = input.size() - batiTerrain;
		// Bati - Bati
		Image batibatiImg = AND.exec(input, reference); // correct
		int batibati = 0;
		for (int p = 0; p < batibatiImg.size(); p++)
			if (batibatiImg.getPixelBoolean(p))
				batibati++;
		// Bati - Fond
		Image batifondImg = CompareImage.exec(input, reference,
				CompareImage.SUP); // faux positifs
		int batifond = 0;
		for (int p = 0; p < batifondImg.size(); p++)
			if (batifondImg.getPixelBoolean(p))
				batifond++;
		// Fond - Bati
		Image fondbatiImg = CompareImage.exec(reference, input,
				CompareImage.SUP); // faux négatifs
		int fondbati = 0;
		for (int p = 0; p < fondbatiImg.size(); p++)
			if (fondbatiImg.getPixelBoolean(p))
				fondbati++;
		// Fond - Fond
		Image fondfondImg = OR.exec(input, reference); // fond
		int fondfond = 0;
		for (int p = 0; p < fondfondImg.size(); p++)
			if (!fondfondImg.getPixelBoolean(p))
				fondfond++;
		out.append("\nEvaluation par pixels");
		// Matrice de confusion
		out.append("\n**** Matrice de confusion ****");
		out.append("\n       bati   fond    total");
		out.append("\nbati   " + batibati + "    " + batifond + "    "
				+ (int) batiClassif);
		out.append("\nfond   " + fondbati + "    " + fondfond + "    "
				+ (int) fondClassif);
		out.append("\ntotal  " + (int) batiTerrain + "    " + (int) fondTerrain);
		// Exactitude producteur
		out.append("\n**** Qualité producteur ****");
		out.append("\n\t bati = " + batibati / batiTerrain);
		out.append("\n\t fond = " + fondfond / fondTerrain);
		// Exactitude utilisateur
		out.append("\n**** Qualité utilisateur ****");
		out.append("\n\t bati = " + batibati / batiClassif);
		out.append("\n\t fond = " + fondfond / fondClassif);
		// Precision globale
		double precision = (batibati + fondfond) / total;
		out.append("\n**** Mesures globales ****");
		out.append("\n\t précision = " + precision);
		// Indice Kappa
		double chance = (batiClassif * batiTerrain) / (total * total)
				+ (fondClassif * fondTerrain) / (total * total);
		double kappa = (precision - chance) / (1 - chance);
		out.append("\n\t indice kappa = " + kappa);
		out.append("\n");
	}

	private void evaluateRegion() {
		double total = input.size();
		// Classif
		int batiClassif = (Integer) new BooleanConnectedComponentsLabeling()
				.processOne(1, input, BooleanConnectedComponentsLabeling.CONNEXITY4);
		// Terrain
		int batiTerrain = (Integer) new BooleanConnectedComponentsLabeling()
				.processOne(1, reference,
						BooleanConnectedComponentsLabeling.CONNEXITY4);
		// Bati - Bati
		Image batibatiImg = AND.exec(input, reference); // correct
		int batibati = (Integer) new BooleanConnectedComponentsLabeling().processOne(
				1, batibatiImg, BooleanConnectedComponentsLabeling.CONNEXITY4);
		// Bati - Fond
		Image batifondImg = FastBinaryReconstruction.exec(batibatiImg, input,
				FastBinaryReconstruction.CONNEXITY8);
		int batifondImg2=(Integer) new BooleanConnectedComponentsLabeling().processOne(
				1, batifondImg, BooleanConnectedComponentsLabeling.CONNEXITY4);
		batifondImg = CompareImage.exec(input, batifondImg, CompareImage.SUP); // faux
		// négatifs
		int batifond = (Integer) new BooleanConnectedComponentsLabeling().processOne(
				1, batifondImg, BooleanConnectedComponentsLabeling.CONNEXITY4);
		// Fond - Bati
		Image fondbatiImg = FastBinaryReconstruction.exec(batibatiImg,
				reference, FastBinaryReconstruction.CONNEXITY8);
		int fondbatiImg2=(Integer) new BooleanConnectedComponentsLabeling().processOne(
		1, fondbatiImg, BooleanConnectedComponentsLabeling.CONNEXITY4);
		fondbatiImg = CompareImage.exec(reference, fondbatiImg,
				CompareImage.SUP); // faux négatifs
		int fondbati = (Integer) new BooleanConnectedComponentsLabeling().processOne(
				1, fondbatiImg, BooleanConnectedComponentsLabeling.CONNEXITY4);
		out.append("\nEvaluation par régions");
		// Matrice de confusion
		out.append("\n**** Matrice de confusion ****");
		out.append("\n       bati   fond    total");
		out.append("\nbati   " + batibati + "    " + batifond + "    "
				+ batiClassif);
		out.append("\nfond   " + fondbati + "    ");
		out.append("\ntotal  " + batiTerrain);
		// Exactitude producteur
		out.append("\n**** Qualité producteur ****");
		out.append("\n\t bati = " + fondbatiImg2 / (double) batiTerrain);
		// Exactitude utilisateur
		out.append("\n**** Qualité utilisateur ****");
		out.append("\n\t bati = " + batifondImg2 / (double) batiClassif);
		// Precision globale
		out.append("\n**** Mesures globales ****");
		double precision = (batibati)
				/ ((double) (batibati + batifond + fondbati));
		out.append("\n\tprécision = " + precision);
		// Indice Kappa
		double chance = (batiClassif * batiTerrain) / (total * total);
		double kappa = (precision - chance) / (1 - chance);
		out.append("\n\t indice kappa = " + kappa);
		out.append("\n");
	}

	private void evaluateMixte() {
		// Mise à jour du résultat
		Image intersection = AND.exec(input, reference);
		Image recIntRef = FastBinaryReconstruction.exec(intersection,
				reference, FastBinaryReconstruction.CONNEXITY8);
		Image recIntInp = FastBinaryReconstruction.exec(intersection, input,
				FastBinaryReconstruction.CONNEXITY8);
		Image ssIntInp = BinaryDifference.exec(input, recIntInp);
		//Image ssIntRef = BinaryDifference.exec(input, recIntRef);
		Image result = OR.exec(ssIntInp, recIntRef);
		// Calcul par pixel
		double total = input.size();
		// Classif
		double batiClassif = 0;
		for (int p = 0; p < result.size(); p++)
			if (result.getPixelBoolean(p))
				batiClassif++;
		double fondClassif = result.size() - batiClassif;
		// Terrain
		double batiTerrain = 0;
		for (int p = 0; p < reference.size(); p++)
			if (reference.getPixelBoolean(p))
				batiTerrain++;
		double fondTerrain = result.size() - batiTerrain;
		// Bati - Bati
		Image batibatiImg = AND.exec(result, reference); // correct
		int batibati = 0;
		for (int p = 0; p < batibatiImg.size(); p++)
			if (batibatiImg.getPixelBoolean(p))
				batibati++;
		// Bati - Fond
		Image batifondImg = CompareImage.exec(result, reference,
				CompareImage.SUP); // faux positifs
		int batifond = 0;
		for (int p = 0; p < batifondImg.size(); p++)
			if (batifondImg.getPixelBoolean(p))
				batifond++;
		// Fond - Bati
		Image fondbatiImg = CompareImage.exec(reference, result,
				CompareImage.SUP); // faux négatifs
		int fondbati = 0;
		for (int p = 0; p < fondbatiImg.size(); p++)
			if (fondbatiImg.getPixelBoolean(p))
				fondbati++;
		// Fond - Fond
		Image fondfondImg = OR.exec(result, reference); // fond
		int fondfond = 0;
		for (int p = 0; p < fondfondImg.size(); p++)
			if (!fondfondImg.getPixelBoolean(p))
				fondfond++;
		out.append("\nEvaluation mixte");
		// Matrice de confusion
		out.append("\n**** Matrice de confusion ****");
		out.append("\n       bati   fond    total");
		out.append("\nbati   " + batibati + "    " + batifond + "    "
				+ (int) batiClassif);
		out.append("\nfond   " + fondbati + "    " + fondfond + "    "
				+ (int) fondClassif);
		out.append("\ntotal  " + (int) batiTerrain + "    " + (int) fondTerrain);
		// Exactitude producteur
		out.append("\n**** Qualité producteur ****");
		out.append("\n\t bati = " + batibati / batiTerrain);
		out.append("\n\t fond = " + fondfond / fondTerrain);
		// Exactitude utilisateur
		out.append("\n**** Qualité utilisateur ****");
		out.append("\n\t bati = " + batibati / batiClassif);
		out.append("\n\t fond = " + fondfond / fondClassif);
		// Precision globale
		double precision = (batibati + fondfond) / total;
		out.append("\n**** Mesures globales ****");
		out.append("\n\t précision = " + precision);
		// Indice Kappa
		double chance = (batiClassif * batiTerrain) / (total * total)
				+ (fondClassif * fondTerrain) / (total * total);
		double kappa = (precision - chance) / (1 - chance);
		out.append("\n\t indice kappa = " + kappa);
		out.append("\n");
	}

}