package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class DisplayStretchDemo
{
	public static void main(String[] args) throws PelicanException
	{
		if (args.length<2)
			System.out.println("Usage: DisplayStretchDemo marginal image1 image2 ... imageN \n where imageX are the images to be displayed \n and marginal is a boolean flag to determine the kind of stretching to be applied (marginal/on each band, or global on the whole image)");
		else
			for (int i=1;i<args.length;i++)
				if (Boolean.parseBoolean(args[0]))
					Viewer2D.exec(ContrastStretchEachBands.exec(ImageLoader.exec(args[i])),args[i]);
				else
					Viewer2D.exec(ContrastStretch.exec(ImageLoader.exec(args[i])),args[i]);
	}
} 