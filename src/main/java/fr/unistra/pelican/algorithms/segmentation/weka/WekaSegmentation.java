package fr.unistra.pelican.algorithms.segmentation.weka;

import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Perform a segmentation using a Weka algorithm. Each band represents a
 * attribute.
 * 
 * @author Sï¿œbastien Derivaux
 */
public class WekaSegmentation extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public Clusterer clusterer;

	// Outputs parameters
	public Image outputImage;

	/**
	 * an optional subsampling percentage
	 */
	public double subsampling = 100.0;

	/**
	 * Constructor
	 * 
	 */
	public WekaSegmentation() {

		super.inputs = "inputImage,clusterer";
		super.outputs = "outputImage";
		super.options = "subsampling";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		outputImage = new IntegerImage(inputImage.getXDim(), inputImage
				.getYDim(), 1, 1, 1);

		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int bDim = inputImage.getBDim();

		// Creation of the datas for Wek.
		// Create attributes.
		FastVector attributes = new FastVector(bDim);
		for (int i = 0; i < bDim; i++)
			attributes.addElement(new weka.core.Attribute("bande" + i));

		Instances dataset = new Instances("dataset", attributes, 0);

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) 
				if (this.inputImage.isPresentXY(x, y))
				{
				Instance instance = new Instance(dataset.numAttributes());
				for (int b = 0; b < bDim; b++)
						instance.setValue(b, inputImage.getPixelXYBDouble(x, y,
								b));
				instance.setDataset(dataset);
				dataset.add(instance);
			}

		// Learn the classification
		try {

			if (subsampling < 100) {
				Resample resample = new Resample();
				resample.setSampleSizePercent(subsampling);
				resample.setInputFormat(dataset);
				resample
						.setRandomSeed((int) (Integer.MAX_VALUE * Math.random()));
				dataset = Filter.useFilter(dataset, resample);
			}

			// TODO : Ã  laisser ?
			dataset.compactify();

			clusterer.buildClusterer(dataset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++) 
				if (this.inputImage.isPresentXY(x, y))
					{
				Instance instance = new Instance(dataset.numAttributes());
				for (int b = 0; b < bDim; b++)
			
					instance.setValue(b, inputImage.getPixelXYBDouble(x, y, b));
				instance.setDataset(dataset);
				int label = -1;
				try {
					label = clusterer.clusterInstance(instance);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outputImage.setPixelXYInt(x, y, label);
			}
				else
					outputImage.setPixelXYInt(x, y, -1);
	}

	public static void main(String[] args) {
		String file = "samples/remotesensing1.png";
		if (args.length > 0)
			file = args[0];

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);

			SimpleKMeans clusterer = new SimpleKMeans();
			try {
				clusterer.setNumClusters(3);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clusterer.setSeed((int) System.currentTimeMillis());
			Image work = (Image) new WekaSegmentation().process(source,
					clusterer);

			Image frontiers = (Image) new FrontiersFromSegmentation()
					.process(work);

			new Viewer2D().process(new LabelsToColorByMeanValue().process(work,
					source), "Mean of clusters from " + file);

			// View it
			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
					frontiers), "Frontiers of " + file);

		} catch (InvalidTypeOfParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
