package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.gui.MultiViews.MultiView;

public class MDisplayDemo {
	public static void main(String[] args) throws PelicanException {
		if (args.length == 0)
			System.out
					.println("Usage: MDisplayDemo image1 image2 ... imageN \n where imageX are the images to be displayed");
		else {
			MultiView view = MViewer.exec();
			for (int i = 0; i < args.length; i++) {
				Image im = ImageLoader.exec(args[i]);
				if (im.getBDim() == 3)
					im.setColor(true);
				if (!(im instanceof ByteImage))
					im = ContrastStretch.exec(im);
				view.add(im);
			}
		}
	}
}