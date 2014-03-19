package fr.unistra.pelican.demos;

import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;

public class ConvertStretchDemo
{
	public static void main(String[] args) throws PelicanException
	{
		if (args.length<2)
			System.out.println("Usage: ConvertStretchDemo marginal input1 output1 input2 output2 ... inputN outputN \n where inputN and outputN are the input and output files, respectively \n and marginal is a boolean flag to determine the kind of stretching to be applied (marginal/on each band, or global on the whole image)");
		else
			for (int i=1;i<args.length;i+=2)
				if (Boolean.parseBoolean(args[0]))
					ImageSave.exec(ContrastStretchEachBands.exec(ImageLoader.exec(args[i])),args[i+1]);
				else
					ImageSave.exec(ContrastStretch.exec(ImageLoader.exec(args[i])),args[i+1]);
	}
} 