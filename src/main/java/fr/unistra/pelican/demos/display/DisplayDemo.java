package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class DisplayDemo {
	public static void main(String[] args) throws PelicanException {
		if (args.length == 0)
			System.out
				.println("Usage: DisplayDemo image1 image2 ... imageN \n where imageX are the images to be displayed");
		else
			for (int i = 0; i < args.length; i++) {
				Image im = ImageLoader.exec(args[i]);
				if (im.getBDim() == 3)
					im.setColor(true);
				if(!(im instanceof ByteImage))
					im=ContrastStretch.exec(im);
				Viewer2D.exec(im, args[i]);
				System.out.println(i + ": " + im.getClass().getName() + " "
					+ im.getXDim() + "x" + im.getYDim() + "x" + im.getZDim() + "x"
					+ im.getTDim() + "x" + im.getBDim() + " (" + im.size() + " pixels)");
			}
	}
}