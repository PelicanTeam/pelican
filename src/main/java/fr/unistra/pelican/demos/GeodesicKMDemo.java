package fr.unistra.pelican.demos;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.geometric.ResamplingByRatio;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.noise.Uniform;
import fr.unistra.pelican.algorithms.segmentation.GeodesicKMeans;
import fr.unistra.pelican.algorithms.segmentation.WatershedKMeans;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class GeodesicKMDemo {

	public static void main(String[] args) throws Exception {
		// Load image and markers
		String path = "./samples/";
		String file = "horse2.png";
		file = "macaws.png";
		file = "smallcat.png";
		file = "/home/lefevre/egc.png";
		file = "samples/berkeley/plane1.png";
		// file="/home/lefevre/noisy2.png";
		file ="samples/berkeley/bear1.png";
		file="samples/simple.png";
		int k = 10;
		int iterations = 50;
		double ratio = 1;
		boolean watershed = false;
		boolean gray=false;
		if (args.length > 0)
			file = args[0];
		if (args.length > 3) {
			k = Integer.parseInt(args[1]);
			iterations = Integer.parseInt(args[2]);
			ratio = Double.parseDouble(args[3]);
		}
		Image source = (Image) new ImageLoader().process(file);

		if(gray)
			source=AverageChannels.exec(source);
		if (ratio < 1)
			source = ResamplingByRatio.exec(source, ratio, ratio, 1, 1, 1,
				ResamplingByRatio.BILINEAR);
		Viewer2D.exec(source);
		long t1 = System.currentTimeMillis();
		Image labels = null;
		if (watershed)
			labels = WatershedKMeans.exec(source, k, iterations);
		else
			labels = GeodesicKMeans.exec(source, k, iterations);
		long t2 = System.currentTimeMillis();
		System.out.println(((t2 - t1) / 60000) + "m " + ((t2 - t1) / 1000) + "s ");
		Image res = DrawFrontiersOnImage.exec(source, FrontiersFromSegmentation
			.exec(labels));
		Viewer2D.exec(res);
		Viewer2D.exec(LabelsToRandomColors.exec(labels));
		ImageSave.exec(res, file.substring(0, file.lastIndexOf('.')) + "-"+k+".png");
	}

}