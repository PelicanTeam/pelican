package fr.unistra.pelican.demos;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.HdfImageLoad;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.PelicanImageSave;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.algorithms.visualisation.Viewer3x2D;

public class ConvertHdfToPelicanDemo {
	public static void main(String[] args) throws PelicanException {
		Image input, output;
		if (args.length < 3)
			System.out.println("Usage: ConvertHdfToPelicanDemo input view type [stretch]");
		input = HdfImageLoad.exec(args[0], Integer.parseInt(args[1]));
		if (args.length>=4 && args[3].equalsIgnoreCase("stretch")) {
			input=ContrastStretch.exec(input);
			System.out.println("Contrast stretched");
		}
		if (args[2].equalsIgnoreCase("boolean"))
			output = new BooleanImage(input);
		else if (args[2].equalsIgnoreCase("byte"))
			output = new ByteImage(input);
		else if (args[2].equalsIgnoreCase("int"))
			output = new IntegerImage(input);
		else if (args[2].equalsIgnoreCase("double"))
			output = new DoubleImage(input);
		else
			throw new PelicanException("Problem with data type");

		PelicanImageSave.exec(output, args[0]
			.substring(0, args[0].lastIndexOf('.'))
			+ ".pel");
		System.out.println("Output file: "
			+	args[0].substring(0, args[0].lastIndexOf('.')) + ".pel");

	}
}