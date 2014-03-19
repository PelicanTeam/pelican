package fr.unistra.pelican.algorithms.segmentation;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.arithmetic.LinearCombination;
import fr.unistra.pelican.algorithms.arithmetic.Minimum;
import fr.unistra.pelican.algorithms.arithmetic.Multiplication;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.conversion.RGBToHSY;
import fr.unistra.pelican.algorithms.geometric.SwapDimensions;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.ColourHSYWeightedGradient;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.MultispectralEuclideanGradient;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSoftAndCoarseClassificationKNN;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement3D;

public class MarkerBasedMultiProbashed3D extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Image samples;

	public double weight = -1.0;

	public boolean color = false;

	public boolean subsampling = true;

	// Outputs parameters
	public IntegerImage outputImage;

	private boolean cpu = true;

	/**
	 * Constructor
	 * 
	 */
	public MarkerBasedMultiProbashed3D() {
		super.inputs = "inputImage,samples";
		super.options = "weight,color,subsampling";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		Image relief, relief1, relief2, data;
		Image samples2=samples;
		// Traitement particulier si on passe une image de labels plutôt qu'une pile d'images binaires
		if(samples.getBDim()==1)
			samples2=LabelsToBinaryMasks.exec(samples);
		// Traitement de l'image en HSL si demandï¿œ
		if (color) {
			inputImage = RGBToHSY.exec(inputImage);
			data = scalarize(inputImage);
			relief2 = ColourHSYWeightedGradient.exec(inputImage,
				FlatStructuringElement2D.createSquareFlatStructuringElement(3),
				ColourHSYWeightedGradient.COMBINED, 0.5, 10.0);

		}
		// Traitement de l'image en RGB / Multispectral sinon
		else {
			data = inputImage;
			relief2 = MultispectralEuclideanGradient.exec(inputImage,
				FlatStructuringElement3D.createSquareFlatStructuringElement(3));
		}
		// Calcul de la carte de probabilitï¿œs
		if (weight != 0) {
			long t1 = 0, t2 = 0;
			if (cpu)
				t1 = System.currentTimeMillis();
			if (subsampling)
				relief1 = WekaSoftAndCoarseClassificationKNN.exec(data, samples2);
			else
				relief1 = WekaSoftAndCoarseClassificationKNN.exec(data, samples2, 1);
			// relief1 = WekaSoftClassification5NN.exec(data, samples);
			if (cpu) {
				t2 = System.currentTimeMillis();
				System.err.println("Classification step: " + ((t2 - t1)) + " ms");
				t1 = System.currentTimeMillis();
			}
			relief1 = ContrastStretch.exec(relief1);
			relief1 = Inversion.exec(relief1);
		} else
			relief1 = samples2.copyImage(false);
//Viewer2D.exec(GrayToPseudoColors.exec(SwapDimensions.exec(relief1,Image.B,Image.Z)),"relief1");
//Viewer2D.exec(GrayToPseudoColors.exec(SwapDimensions.exec(relief2,Image.B,Image.Z)),"relief2");
		// Fusion des deux reliefs
		relief2 = duplicate(relief2, relief1.getBDim());
		System.out.println(relief1+"///"+relief2);
		if (weight >= 0)
			relief = LinearCombination.exec(new Image[] { relief1, relief2 },
				new Double[] { weight, 1 - weight });
		else
			relief = Multiplication.exec(ContrastStretch.exec(relief1),
				ContrastStretch.exec(relief2));
		relief = AdditionConstantChecked.exec(relief, 1.0 / 255);
//Viewer2D.exec(GrayToPseudoColors.exec(SwapDimensions.exec(relief,Image.B,Image.Z)),"relief");
		// Imposition des marqueurs
		Image marker = Inversion.exec(samples2);
		relief = Minimum.exec(relief, marker);
//Viewer2D.exec(GrayToPseudoColors.exec(SwapDimensions.exec(relief,Image.B,Image.Z)),"relief with markers");
		
		// Viewer2D.exec(marker);
		// Viewer2D.exec(relief);
		// Application du watershed
		outputImage = (IntegerImage) MarkerBasedMultiWatershedND.exec(relief);

	}

	public static Image scalarize(Image i1) {
		Image i2 = i1.newInstance(i1.getXDim(), i1.getYDim(), i1.getZDim(), i1
			.getTDim(), 4);
		for (int x = 0; x < i1.getXDim(); x++)
			for (int y = 0; y < i1.getYDim(); y++)
				for (int z = 0; z < i1.getZDim(); z++)
					for (int t = 0; t < i1.getTDim(); t++) {
						i2.setPixelDouble(x, y, z, t, 0, Math.sin(i1.getPixelDouble(x, y,
							z, t, 0)
							* 2 * Math.PI));
						i2.setPixelDouble(x, y, z, t, 1, Math.cos(i1.getPixelDouble(x, y,
							z, t, 0)
							* 2 * Math.PI));
						i2.setPixelDouble(x, y, z, t, 2, i1.getPixelDouble(x, y, z, t, 1));
						i2.setPixelDouble(x, y, z, t, 3, i1.getPixelDouble(x, y, z, t, 2));
					}
		return i2;
	}

	public static Image duplicate(Image i1, int nb) {
		Image i2 = i1.newInstance(i1.getXDim(), i1.getYDim(), i1.getZDim(), i1
			.getTDim(), nb);
		for (int b = 0; b < nb; b++)
			for (int x = 0; x < i1.getXDim(); x++)
				for (int y = 0; y < i1.getYDim(); y++)
					for (int z = 0; z < i1.getZDim(); z++)
						for (int t = 0; t < i1.getTDim(); t++)
							i2
								.setPixelDouble(x, y, z, t, b, i1.getPixelDouble(x, y, z, t, 0));
		return i2;
	}

	public static ByteImage convertToByteImage(Image img) {
		ByteImage res = new ByteImage(img.getXDim(), img.getYDim(), img.getZDim(),
			img.getTDim(), img.getBDim());
		for (int p = 0; p < img.size(); p++)
			res.setPixelByte(p, img.getPixelInt(p));
		return res;
	}

	public static IntegerImage exec(Image image, Image samples, double weight,
		boolean color, boolean subsampling) {
		return (IntegerImage) new MarkerBasedMultiProbashed3D().process(image,
			samples, weight, color, subsampling);
	}

	public static IntegerImage exec(Image image, Image samples, double weight,
		boolean color) {
		return (IntegerImage) new MarkerBasedMultiProbashed3D().process(image,
			samples, weight, color);
	}

	public static IntegerImage exec(Image image, Image samples, double weight) {
		return (IntegerImage) new MarkerBasedMultiProbashed3D().process(image,
			samples, weight);
	}

	public static IntegerImage exec(Image image, Image samples, boolean color,
		boolean subsampling) {
		return (IntegerImage) new MarkerBasedMultiProbashed3D().process(image,
			samples, null, color, subsampling);
	}

	public static IntegerImage exec(Image image, Image samples, boolean color) {
		return (IntegerImage) new MarkerBasedMultiProbashed3D().process(image,
			samples, null, color);
	}

	public static IntegerImage exec(Image image, Image samples) {
		return (IntegerImage) new MarkerBasedMultiProbashed3D().process(image,
			samples);
	}

}
