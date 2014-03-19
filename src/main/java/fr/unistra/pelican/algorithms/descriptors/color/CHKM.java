package fr.unistra.pelican.algorithms.descriptors.color;

import java.util.Arrays;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationKmeans;
import fr.unistra.pelican.util.data.Data;
import fr.unistra.pelican.util.data.HistogramData;

/**
 * Color Histogram for K-Means (CHKM)
 * 
 * Chuen-Horng Lin, Rong-Tai Chen and Yung-Kuan Chan A smart content-based image
 * retrieval system based on color and texture feature Image and Vision
 * Computing Volume 27, Issue 6, 4 May 2009, Pages 658-665
 * doi:10.1016/j.imavis.2008.07.004
 * 
 * 
 * @author Lefevre
 * 
 *         TODO: add mask support
 */
public class CHKM extends Descriptor {

	/** First input parameter. */
	public Image input;

	/** Optional number of colour clusters (=k) */
	public int nbClusters = 16;

	/** Output parameter. */
	public HistogramData output;

	/** Constructor */
	public CHKM() {
		super.inputs = "input";
		super.options = "nbClusters";
		super.outputs = "output";
	}

	public static HistogramData exec(Image input) {
		return (HistogramData) new CHKM().process(input);
	}

	/** @see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException {

		// TODO resolve the choice of the color space
		// if (this.input.getBDim() == 1)
		// this.input = GrayToRGB.exec(this.input);
		// this.input = RGBToHSV.exec(this.input);
		// this.input = NonUniformHSVQuantization733.exec(this.input);

		// Apply kmeans algorithm TODO check if centers have to be randomly
		// initialized ?
		Image classif = WekaSegmentationKmeans.exec(input, nbClusters);
		// Compute histogram from the clusters
		double[] hist = new double[nbClusters];
		Arrays.fill(hist, 0);
		for (int i = 0; i < classif.size(); i++)
			hist[classif.getPixelInt(i)]++;

		// normalize
		int sum = classif.size();
		if ( sum > 0 )
		   for ( int bin = 0 ; bin < hist.length ; bin++ ) 
			   hist[bin] /= sum; 

		// Probably missing details in the original paper, sort the histogram to
		// ensure possible comparisons
		Arrays.sort(hist);
		Double[] values = new Double[nbClusters];
		for (int i = 0; i < nbClusters; i++)
			values[i] = hist[hist.length - 1 - i];

		// by Régis: maybe it'll works if normalize the final vector ?
		// answer : not really :/
//		values = fr.unistra.pelican.util.Tools.vectorNormalize( values );

		// Generate output
		this.output = new HistogramData();
		this.output.setDescriptor((Class) this.getClass());
		this.output.setValues(values);
	}

	/**
	 * Specific distance measure given in the original paper
	 * 
	 * @param d1
	 *            first histogram
	 * @param d2
	 *            second histogram
	 * @return computed distance
	 */
	public static double distance(Data d1, Data d2) {
		Double[] v1 = (Double[]) d1.getValues();
		Double[] v2 = (Double[]) d2.getValues();
		if (v1.length != v2.length)
			return -1;
		double sum = 0;
		for (int i = 0; i < v1.length; i++)
			if (v1[i] != 0 || v2[i] != 0)
				sum += Math.abs((v1[i] - v2[i]) / (v1[i] + v2[i]));
		// Specific step to get a similarity in [0,1]
		// sum = 1 - sum / 2;
		// Specific step to get a distance in [0,1]
		sum = sum / 2;
		assert sum>=0&&sum<=1:sum;
		return sum;
	}

	// public static void main(String[] args) {
	// Image input = ImageLoader.exec("samples/lenna.png");
	// Data d1 = CHKM.exec(input);
	// Double[] feature1 = (Double[]) d1.getValues();
	// Data d2 = CHKM.exec(input);
	// Double[] feature2 = (Double[]) d2.getValues();
	// for (int i = 0; i < feature1.length; i++)
	// System.out.println(feature1[i] + " " + feature2[i]);
	// System.out.println(CHKM.distance(d1, d2));
	// }

}
