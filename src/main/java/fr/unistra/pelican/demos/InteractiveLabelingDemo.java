package fr.unistra.pelican.demos;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageBuilder;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;

public class InteractiveLabelingDemo {

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out
				.println("Usage: InteractiveLabelingDemo file output [markers] [-stretch][-colorview]\n"
					+ "- file is the image to be segmented\n"
					+ "- output is the resulting image\n"
					+ "- markers is the predefined marker image\n"
					+ "- -stretch to perform a contrast stretch step\n"
					+ "- -colorview to have a color display of the image to be segmented");
			return;
		}
		new InteractiveLabelingDemo(args);
	}

	public InteractiveLabelingDemo(String[] args) {
		boolean color = false;
		String path = args[0];
		Image input = ImageLoader.exec(path);
		String outfile = args[1];
		Image marker = null;
		Image result = null;

		for (int i = 2; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-stretch"))
				input = ContrastStretch.exec(input);
			else if (args[i].equalsIgnoreCase("-colorview"))
				color = true;
			else
				marker = ImageLoader.exec(args[i]);
		}

		Image disp = input;
		if (input.getBDim() > 3 && color)
			disp = ColorImageFromMultiBandImage.exec(input, 2, 1, 0);
		if (input.getBDim() == 3)
			input.setColor(true);
		if (disp.getBDim() == 3)
			disp.setColor(true);

		if (marker == null)
			result = ImageBuilder.exec(disp, "InteractiveLabelingDemo");
		else {
			if(marker instanceof IntegerImage)
				marker=((IntegerImage)marker).copyToByteImage();
			result = ImageBuilder.exec(disp, "InteractiveLabelingDemo",
				(ByteImage) marker);
		}

		if(result!=null)
			ImageSave.exec(result, outfile);
	}
}
