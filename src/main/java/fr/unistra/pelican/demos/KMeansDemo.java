package fr.unistra.pelican.demos;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.geometric.Crop2D;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationKmeans;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * This class applies the KMeans algorithm with optionnally various number of
 * clusters to perform a pixel-based clustering on the input image.
 * 
 * @author lefevre
 * 
 */
public class KMeansDemo {

	public static void main(String[] args) {
		// Check number of parameters
		if (args.length != 2 && args.length != 3)
			System.out
				.println("KMeansDemo usage : filename clusters [clusters2]\n"
					+ "filename: the image to be clustered\n"
					+ "clusters: the number of clusters\n"
					+ "clusters2: if specified, apply a kmeans with k varying from clusters to clusters2");
		// Determine the parameters
		String path = args[0];
		String path2 = null;
		if (path.indexOf('.') == -1)
			path2 = new String(path);
		else
			path2 = path.substring(0, path.lastIndexOf('.'));
		int kmin = Integer.parseInt(args[1]);
		int kmax = kmin;
		if (args.length > 2)
			kmax = Integer.parseInt(args[2]);
		// Load and process
		Image input = ImageLoader.exec(path);
		for (int i = kmin; i <= kmax; i++) {
			input=ContrastStretch.exec(input);
			long t1=System.currentTimeMillis();
			System.out.print("Perform Kmeans " + i + " / " + kmax+" ... ");
			ImageSave.exec(((IntegerImage) WekaSegmentationKmeans.exec(input, i,1.0))
				.copyToByteImage(), path2 + "-kmeans-" + i + ".png");
			long t2=System.currentTimeMillis();
			System.out.println((t2-t1)/1000+" seconds");
		}
	}
}
