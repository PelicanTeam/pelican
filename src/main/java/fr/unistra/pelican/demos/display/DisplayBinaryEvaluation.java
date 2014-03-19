package fr.unistra.pelican.demos.display;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.logical.XOR;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class DisplayBinaryEvaluation
{
	public static void main(String[] args) {
	if (args.length < 0)
		System.out
			.println("Usage: DisplayBinaryEvaluation reference result1 result2 ... resultN \n where resultX are the results to be evaluated");
	else {
		Image reference = ImageLoader.exec(args[0]);
		Image display = new ByteImage(reference.getXDim(), reference.getYDim(), reference
				.getZDim(), reference.getTDim(), 3);
		display.fill(0);
		display.setColor(true);
		for (int i = 1; i < args.length; i++) {
			Image im = ImageLoader.exec(args[i]);
			if(!Image.haveSameDimensions(reference, im)) {
				System.out.println(args[i]+" does not have the same dimensions than "+args[0]);
				continue;
			}
			display.setImage4D(XOR.exec(reference, im), 0, Image.B);
			display.setImage4D(im, 1, Image.B);
			Viewer2D.exec(display, args[i]+" compared with "+args[0]);
		}
	}

}
}