package fr.unistra.pelican.demos.applied.video;


import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.applied.video.caption.ColorDetector;
import fr.unistra.pelican.algorithms.applied.video.caption.EdgeDensityDetector;
import fr.unistra.pelican.algorithms.applied.video.caption.EdgeRegularityDetector;
import fr.unistra.pelican.algorithms.applied.video.caption.TextureDetector;
import fr.unistra.pelican.algorithms.arithmetic.LinearCombination;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.geometric.BlockResampling2D;
import fr.unistra.pelican.algorithms.io.MultipleImageLoad;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryClosing;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryOpening;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class CaptionDetectionDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws PelicanException {
		
		
		Image decisions[]=new Image[4];
		double visible=1.0/decisions.length;
		Double coefficients [] = new Double[decisions.length];
	    java.util.Arrays.fill(coefficients,visible);

		int w=12;
		int h=4;
		
		long t1=System.currentTimeMillis();
		String path="/home/lefevre/data/sample3";
		Image video = MultipleImageLoad.exec(path,new Integer(Image.T));
		Image gray=RGBToGray.exec(video);
		Viewer2D.exec(video,"Sequence");
		
		// Texture detector
		Image texture=TextureDetector.exec(gray,w,h,30,0.1);
		Viewer2D.exec(BlockResampling2D.exec(texture,w,h,true),"texture");
		decisions[0]=texture;
		
		// Edge density detector
		Image density=EdgeDensityDetector.exec(gray,w,h,200,0.25);
		Viewer2D.exec(BlockResampling2D.exec(density,w,h,true),"density");
		decisions[1]=density;

		// Edge regularity detector
		Image regularity=EdgeRegularityDetector.exec(gray,w,h,200,0.5,1,0.3);
		Viewer2D.exec(BlockResampling2D.exec(regularity,w,h,true),"regularity");
		decisions[2]=regularity;
		
		// Color detector
		Image color=ColorDetector.exec(video,w,h,20,0.15);
		Viewer2D.exec(BlockResampling2D.exec(color,w,h,true),"color");
		decisions[3]=color;
		
		// Fusion of detectors
		Image result=LinearCombination.exec(decisions,coefficients);
		Viewer2D.exec(BlockResampling2D.exec(((DoubleImage)result).scaleToZeroOne(),w,h,true),"scores");
		
		// Post-processing
		result=ManualThresholding.exec(result,2.0*visible);
		Viewer2D.exec(BlockResampling2D.exec(result,w,h,true),"threshold");
		result=BinaryClosing.exec(result,FlatStructuringElement2D.createSquareFlatStructuringElement(3));
	    result=BinaryOpening.exec(result,FlatStructuringElement2D.createRectangularFlatStructuringElement(3,5));

	    // Final result
		Viewer2D.exec(BlockResampling2D.exec(result,w,h,true),"final result");
		
		long t2=System.currentTimeMillis()-t1;
		System.out.println("Demo terminee : "+ (t2/1000) + " secondes");
		
	}
}