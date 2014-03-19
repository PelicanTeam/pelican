package fr.unistra.pelican.demos;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.spatial.DistanceTransform;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class CCLabelingDemo {

	public static void main(String args[]) {

		if (args.length == 0)
			System.out
				.println("CCLabelingDemo input [output]: performs CC Labeling on input and save output if required (otherwise display the result)");
		else {
			Image input = ImageLoader.exec(args[0]);
			Image output = BooleanConnectedComponentsLabeling.exec(input);
			if (args.length == 1)
				Viewer2D.exec(LabelsToRandomColors.exec(output, true));
			else
				ImageSave.exec(output, args[1]);
		}
	}

}