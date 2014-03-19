package fr.unistra.pelican.demos;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.binary.*;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.spatial.DistanceTransform;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class BinaryMorphologyDemo {
	public static void main(String args[]) {
		String path="samples/binary.png";
		if (args.length==1)
			path=args[0];
		BooleanImage image = ManualThresholding.exec(ImageLoader.exec(path),0.5);
		Viewer2D.exec(image, "input");
		Viewer2D.exec(GrayToPseudoColors.exec(DistanceTransform.exec(image)));
		int size=20;
		BooleanImage erosion=new BooleanImage(image.getXDim(),image.getYDim(),1,size,1);
		BooleanImage dilation=erosion.copyImage(false);
		BooleanImage opening=erosion.copyImage(false);
		BooleanImage closing=erosion.copyImage(false);
		BooleanImage se=null;
		for (int i=0;i<size;i++) {
			//se=FlatStructuringElement2D.createHorizontalLineFlatStructuringElement(2*i+1);
			//se=FlatStructuringElement2D.createVerticalLineFlatStructuringElement(2*i+1);
			//se=FlatStructuringElement2D.createLeftDiagonalLineFlatStructuringElement(2*i+1);
			//se=FlatStructuringElement2D.createRightDiagonalLineFlatStructuringElement(2*i+1);
			se=FlatStructuringElement2D.createCircleFlatStructuringElement(i);
			//se=FlatStructuringElement2D.createSquareFlatStructuringElement(2*i+1);
			erosion.setImage2D(BinaryErosion.exec(image,se),0,i,0);
			dilation.setImage2D(BinaryDilation.exec(image,se),0,i,0);
			opening.setImage2D(BinaryOpening.exec(image,se),0,i,0);
			closing.setImage2D(BinaryClosing.exec(image,se),0,i,0);
		}
		Viewer2D.exec(erosion, "erosion");
		Viewer2D.exec(dilation, "dilation");
		Viewer2D.exec(opening, "opening");
		Viewer2D.exec(closing, "closing");
		
	}

}