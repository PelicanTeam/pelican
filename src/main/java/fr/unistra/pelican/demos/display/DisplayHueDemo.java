package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.AngleToPseudoColors;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.conversion.RGBToHSY;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class DisplayHueDemo {
	public static void main(String[] args) throws PelicanException {
		if (args.length == 0)
			System.out
				.println("Usage: DisplayHueDemo image1 image2 ... imageN \n where imageX are the images to be displayed");
		else
			for (int i = 0; i < args.length; i++) {
				Image im = ImageLoader.exec(args[i]);
				if (im.getBDim() != 3) continue;
				im.setColor(true);
				im=RGBToHSY.exec(im);
				Viewer2D.exec(GrayToPseudoColors.exec(im.getImage4D(0,Image.B),true), args[i]);
				Viewer2D.exec(AngleToPseudoColors.exec(im.getImage4D(0,Image.B),true), args[i]);
				Viewer2D.exec(GrayToPseudoColors.exec(im.getImage4D(0,Image.B),false), args[i]);
				Viewer2D.exec(AngleToPseudoColors.exec(im.getImage4D(0,Image.B),false), args[i]);
				Viewer2D.exec(ContrastStretch.exec(im.getImage4D(0,Image.B)), args[i]);
				Viewer2D.exec(im.getImage4D(0,Image.B), args[i]);
				System.out.println(i + ": " + im.getClass().getName() + " "
					+ im.getXDim() + "x" + im.getYDim() + "x" + im.getZDim() + "x"
					+ im.getTDim() + "x" + im.getBDim() + " (" + im.size() + " pixels)");
			}
	}
}