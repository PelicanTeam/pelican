package fr.unistra.pelican.demos;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.geometric.Crop2D;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaClassification;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaClassificationKNN;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationKmeans;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * This class applies the KMeans algorithm with optionnally various number of
 * clusters to perform a pixel-based clustering on the input image.
 * 
 * @author lefevre
 * 
 */
public class KNNDemo {

	public static void main(String[] args) {
		// Check number of parameters
		if (args.length != 3 && args.length != 4) {
			System.out
				.println("KNNDemo usage : filename ngb [ngb2]\n"
					+ "filename: the image to be clustered\n"
					+"samples: the label image used as learning set"
					+ "ngb: the number of neighbours\n"
					+ "ngb2: if specified, apply a k-NN with k varying from ngb to ngb2");
			return;
		}
		// Determine the parameters
		String path = args[0];
		String path2 = null;
		if (path.indexOf('.') == -1)
			path2 = new String(path);
		else
			path2 = path.substring(0, path.lastIndexOf('.'));
		Image samples=LabelsToBinaryMasks.exec(ImageLoader.exec(args[1]));
		int kmin = Integer.parseInt(args[2]);
		int kmax = kmin;
		if (args.length > 3)
			kmax = Integer.parseInt(args[3]);
		// Load and process
		Image input = ImageLoader.exec(path);
		for (int i = kmin; i <= kmax; i++) {
			input=ContrastStretch.exec(input);
			long t1=System.currentTimeMillis();
			System.out.print("Perform KNN " + i + " / " + kmax+" ... ");
			ImageSave.exec((WekaClassificationKNN.exec(input, samples,i))
				, path2 + "-knn-" + i + ".tif");
			long t2=System.currentTimeMillis();
			System.out.println((t2-t1)/1000+" seconds");
		}
	}
}
