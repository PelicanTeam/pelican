package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBandWithPercentileEdgeCutting;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class MultispectralDisplayDemo {
	public static void main(String[] args) throws PelicanException {
		if (args.length != 4 && args.length != 5)
			System.out
					.println("Usage: DisplayDemo image red green blue [stretch|percentage]\n "
							+ "where image is the image to be displayed "
							+ "and red/green/blue the indices of relative bands (-1 to disable one image band)"
							+ "and stretch is an optional flag to stretch values or a percentage value for percentile cutting");
		else {
			Image im = ImageLoader.exec(args[0]);
			if (args.length == 5) {
				if (args[4].equalsIgnoreCase("stretch"))
					im = ContrastStretch.exec(im);
				else
					im = ContrastStretchEachBandWithPercentileEdgeCutting.exec(
							im, Integer.parseInt(args[4]) / 100.0);
			}
			im.setColor(true);
			im = ColorImageFromMultiBandImage.exec(im, Integer
					.parseInt(args[1]), Integer.parseInt(args[2]), Integer
					.parseInt(args[3]));
			Viewer2D.exec(im, args[0]);
			System.out.println(im.getClass().getName() + " " + im.getXDim()
					+ "x" + im.getYDim() + "x" + im.getZDim() + "x"
					+ im.getTDim() + "x" + im.getBDim() + " (" + im.size()
					+ " pixels)");
		}
	}
}