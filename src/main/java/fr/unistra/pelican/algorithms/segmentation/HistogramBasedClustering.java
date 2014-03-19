package fr.unistra.pelican.algorithms.segmentation;

import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.histogram.Histogram;

/**
 * HistogramBasedClustering
 * 
 * @author weber,lefevre
 * 
 */
public class HistogramBasedClustering extends Algorithm {

	// Inputs
	
	/**
	 * Image to cluster
	 */
	public Image inputImage;

	/**
	 * Threshold
	 */
	public double thresh;

	/**
	 * Flag to ignore black pixels
	 */
	public boolean ignore=false;

	// Outputs
	
	/**
	 * Resulting Clusters
	 */
	public Image outputImage;

	/**
	 * Resulting number of clusters
	 */
	public int nbClusters;

	/**
	 * Constructor
	 * 
	 */
	public HistogramBasedClustering() {
		super.inputs = "inputImage,thresh";
		super.options="ignore";
		super.outputs = "outputImage,nbClusters";
	}

	public static Image exec(Image inputImage, double thr) {
		return (Image) new HistogramBasedClustering().process(inputImage,thr); 
	}
	
	public void launch() throws AlgorithmException {

		// Histogram computing
		double[] histo = (double[]) new Histogram().process(inputImage);
		if (ignore)
			histo[0] = 0;
		// Histogram smoothing
		// histo=smoothMax(histo);
		histo = average(histo);
		// equivalent to opening 3
		histo = smoothMin(histo);
		histo = smoothMax(histo);
		// equivalent to closing 5
		histo = smoothMax(histo);
		histo = smoothMax(histo);
		histo = smoothMin(histo);
		histo = smoothMin(histo);

		// Clustering 1st step
		Image clusterImage = new IntegerImage(inputImage, false);
		Vector<Object> cluster = new Vector<Object>();
		double max = 0;
		double total = 0;
		double pointsAffectes = 0, pointsCluster;
		for (int i = 0; i < histo.length; i++)
			total = total + histo[i];
		System.out.println("total:" + total+ "\t thresh: "+thresh+" => "+(thresh*total));
		int centroide = -1, borneInf, borneSup;
		while (pointsAffectes < thresh * total) {
			for (int i = 0; i < histo.length; i++)
				if (histo[i] > max) {
					max = histo[i];
					centroide = i;
				}
			cluster.add(centroide);
			borneInf = centroide;
			borneSup = centroide;
			for (int i = centroide + 1; i < 255 && histo[i] <= histo[i - 1]
					&& histo[i] > 0; i++)
				borneSup = i;
			for (int i = centroide - 1; i > 0 && histo[i] <= histo[i + 1]
					&& histo[i] > 0; i--)
				borneInf = i;
			pointsCluster = 0;
			for (int i = borneInf; i <= borneSup; i++) {
				pointsCluster = pointsCluster + histo[i];
				histo[i] = 0.;
			}
			pointsAffectes = pointsAffectes + pointsCluster;
			System.out.println("indice = " + cluster.size() + " inf = "
					+ borneInf + " sup = " + borneSup + " taille = "
					+ pointsCluster + " total = " + pointsAffectes);
			for (int i = 0; i < inputImage.size(); i++)
				if (inputImage.getPixelByte(i) >= borneInf
						&& inputImage.getPixelByte(i) <= borneSup)
					clusterImage.setPixelInt(i, cluster.size());
			max = 0;
		}
		outputImage = clusterImage;
		nbClusters = cluster.size();
	}

	double[] average(double[] histo) {
		double histo2[] = new double[histo.length];
		for (int h = 1; h < histo2.length - 1; h++)
			histo2[h] = Math
					.ceil((histo[h - 1] + histo[h] + histo[h + 1]) / 3.0);
		histo2[0] = Math.ceil((histo[0] + histo[1]) / 2.0);
		histo2[histo2.length - 1] = Math
				.ceil((histo[histo.length - 2] + histo[histo.length - 1]) / 2.0);
		return histo2;
	}

	double[] smoothMax(double[] histo) {
		double histo2[] = new double[histo.length];
		for (int h = 1; h < histo2.length - 1; h++)
			histo2[h] = Math
					.max(histo[h - 1], Math.max(histo[h], histo[h + 1]));
		histo2[0] = Math.max(histo[0], histo[1]);
		histo2[histo2.length - 1] = Math.max(histo[histo.length - 2],
				histo[histo.length - 1]);
		return histo2;
	}

	double[] smoothMin(double[] histo) {
		double histo2[] = new double[histo.length];
		for (int h = 1; h < histo2.length - 1; h++)
			histo2[h] = Math
					.min(histo[h - 1], Math.min(histo[h], histo[h + 1]));
		histo2[0] = Math.min(histo[0], histo[1]);
		histo2[histo2.length - 1] = Math.min(histo[histo.length - 2],
				histo[histo.length - 1]);
		return histo2;
	}

}
