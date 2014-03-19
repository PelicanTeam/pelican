package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.io.HdfImageLoad;
import fr.unistra.pelican.algorithms.visualisation.Viewer3x2D;

public class DisplayHdfDemo {
	public static void main(String[] args) throws PelicanException {
		if (args.length < 2)
			System.out
				.println("Usage: DisplayHDFDemo image view1 view2 ... viewN \n where viewX are the image bands to be displayed");
		else
			for (int i = 1; i < args.length; i++) {
				Image im = HdfImageLoad.exec(args[0], Integer.parseInt(args[i]));
				if (!(im instanceof ByteImage))
				im=GrayToPseudoColors.exec(im);
				Viewer3x2D.exec(im, args[i]);
			}
	}
}