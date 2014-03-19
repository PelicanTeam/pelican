package fr.unistra.pelican.demos;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.spatial.DistanceTransform;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class CCDemo {
	public static void main(String args[]) {
		String path="samples/binary.png";
		if (args.length==1)
			path=args[0];
		Image image = (Image) new ImageLoader().process(path);
		new Viewer2D().process(image, "input");
		Image result = (Image) new BooleanConnectedComponentsLabeling().process(image,
				BooleanConnectedComponentsLabeling.CONNEXITY4);
		result = (Image) new LabelsToRandomColors().process(result, true);
		new Viewer2D().process(result, "output 4");
		result = (Image) new BooleanConnectedComponentsLabeling().process(image,
				BooleanConnectedComponentsLabeling.CONNEXITY8);
		result = (Image) new LabelsToRandomColors().process(result, true);
		new Viewer2D().process(result, "output 8");
		result = (Image) new BooleanConnectedComponentsLabeling().process(image,
				BooleanConnectedComponentsLabeling.CONNEXITY4,true);
		result = (Image) new LabelsToRandomColors().process(result, true);
		new Viewer2D().process(result, "output 4 with background");
		result = (Image) new BooleanConnectedComponentsLabeling().process(image,
				BooleanConnectedComponentsLabeling.CONNEXITY8,true);
		result = (Image) new LabelsToRandomColors().process(result, true);
		new Viewer2D().process(result, "output 8 with background");
		result=GrayToPseudoColors.exec(DistanceTransform.exec(image));
		Viewer2D.exec(result,"distance transform in pseudo colors");

	}

}