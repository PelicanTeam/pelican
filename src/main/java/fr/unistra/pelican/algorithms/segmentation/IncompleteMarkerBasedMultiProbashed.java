package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.BinaryMasksToLabels;
import fr.unistra.pelican.algorithms.io.PelicanImageLoad;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryErosion;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryOpening;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.FastBinaryReconstruction;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionSize;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class IncompleteMarkerBasedMultiProbashed extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Image samples;

	public double weight = -1.0;

	public boolean color = false;

	public boolean subsampling = true;

	// Outputs parameters
	public IntegerImage outputImage;

	public Image outputSamples;

	private boolean cpu = true;

	/**
	 * Constructor
	 * 
	 */
	public IncompleteMarkerBasedMultiProbashed() {
		super.inputs = "inputImage,samples";
		super.options = "weight,color,subsampling";
		super.outputs = "outputImage,outputSamples";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Classification Ã  partir des marqueurs
		long t1 = 0, t2 = 0;
		if (cpu)
			t1 = System.currentTimeMillis();

		/*
		if (subsampling)
			outputSamples = WekaCoarseClassificationKNN.exec(
				color ? MarkerBasedMultiProbashed.scalarize(RGBToHSY.exec(inputImage))
					: inputImage, samples, 5);
		else
			outputSamples = WekaCoarseClassificationKNN.exec(
				color ? MarkerBasedMultiProbashed.scalarize(RGBToHSY.exec(inputImage))
					: inputImage, samples, 5,1);

		outputSamples = LabelsToBinaryMasks.exec(outputSamples, true);
		*/
		
		//PelicanImageSave.exec(outputSamples,"debug.pel");
		//if(true)return;
		
		
		outputSamples=PelicanImageLoad.exec("debug.pel");
		
		
		if (cpu) {
			t2 = System.currentTimeMillis();
			System.err.println("Preclassification step: " + ((t2 - t1)) + " ms");
			t1 = System.currentTimeMillis();
		}
		// Image recons = classif.copyImage(false);
		// Viewer2D.exec(classif);

		// Recherche des nouveaux marqueurs dans chaque classe
		if (cpu)
			t1 = System.currentTimeMillis();
		for (int b = 0; b < outputSamples.getBDim(); b++) {
			Image samp = samples.getImage4D(b, Image.B);
			Image clas = outputSamples.getImage4D(b, Image.B);
			Image rec = FastBinaryReconstruction.exec(samp, clas);
			// recons.setImage4D(rec, b, Image.B);
			Image cc = BooleanConnectedComponentsLabeling.exec(rec);
			int tab[] = RegionSize.exec(cc);
			int area = Integer.MAX_VALUE;
			for (int r = 1; r < tab.length; r++)
				if (area > tab[r])
					area = tab[r];
			area *= 0.5;
			area=50;
			//area=1;
			System.out.println("Class " + b + " => " + area);
			// CritÃšre : composante de taille > 50% du marqueur le plus petit
			//rec = BinaryAreaOpening.exec(clas, area);
			rec=clas;
			int se=5;
			//se=5;
			// TolÃ©rance d'un pixel d'erreur dans le voisinage
			rec = GrayMedian.exec(rec, FlatStructuringElement2D
				.createSquareFlatStructuringElement(3));
			// On ne conserve que les zones d'au moins 11x11, soit 4m*4m
			rec = BinaryOpening.exec(rec, FlatStructuringElement2D
				.createSquareFlatStructuringElement(11));
//			rec = BinaryAreaOpening.exec(rec, area);
			// Erosion pour Ã©viter les marqueurs qui se touchent
			rec = BinaryErosion.exec(rec, FlatStructuringElement2D
				.createSquareFlatStructuringElement(se));
			outputSamples.setImage4D(rec, b, Image.B);
		}
		if (cpu) {
			t2 = System.currentTimeMillis();
			System.err.println("Marker identification step: " + ((t2 - t1)) + " ms");
			t1 = System.currentTimeMillis();
		}

		// classif=BinaryErosion.exec(classif,FlatStructuringElement2D.createSquareFlatStructuringElement(21));
		// classif=BinaryAreaOpening.exec(classif,100);
		samples = outputSamples;
		outputSamples = BinaryMasksToLabels.exec((BooleanImage) outputSamples);
		 Viewer2D.exec(LabelsToRandomColors.exec(BinaryMasksToLabels.exec((BooleanImage)samples),true));
		// Viewer2D.exec(samples);
		// Application du MarkerBasedMultiProbashed
		outputImage = MarkerBasedMultiProbashed.exec(inputImage, samples, weight,
			color, subsampling);
	}

	public static IntegerImage exec(Image image, Image samples, double weight,
		boolean color, boolean subsampling) {
		return (IntegerImage) new IncompleteMarkerBasedMultiProbashed().process(
			image, samples, weight, color, subsampling);
	}

	public static IntegerImage exec(Image image, Image samples) {
		return (IntegerImage) new IncompleteMarkerBasedMultiProbashed().process(
			image, samples);
	}

}
