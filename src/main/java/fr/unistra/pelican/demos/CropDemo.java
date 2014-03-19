package fr.unistra.pelican.demos;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.geometric.Crop2D;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;

public class CropDemo {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out
				.println("CropDemo usage: CropDemo input output a b c d\n=> Crop the input image to output image from (a,b) to (c,d)");
			return;
		}
		Image input = ImageLoader.exec(args[0]);
		if (args.length != 6) {
			System.out.println(input);
			return;
		}
		int a = Integer.parseInt(args[2]);
		int b = Integer.parseInt(args[3]);
		int c = Integer.parseInt(args[4]);
		int d = Integer.parseInt(args[5]);
		Image output = Crop2D.exec(input, a, b, c, d);
		ImageSave.exec(output, args[1]);
	}
}