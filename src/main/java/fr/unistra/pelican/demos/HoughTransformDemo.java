/**
 * 
 */
package fr.unistra.pelican.demos;

import java.util.Arrays;

import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Blending;
import fr.unistra.pelican.algorithms.detection.HoughTransform;
import fr.unistra.pelican.algorithms.edge.Sobel;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.util.IMath;
import fr.unistra.pelican.util.Line;

/**
 * Demo of the Hough Transform Algorithm for line detection
 * 
 * @author Benjamin Perret
 *
 */
public class HoughTransformDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// a simple image
		Image im=ImageLoader.exec("samples/bloc.png");
		//edge detector
		Image edge=Sobel.exec(im);
		
		// hough transform
		DoubleImage acc=HoughTransform.exec(edge,0.5,0.002);
		
		
		// drawing buffer for result
		Image result=im.copyImage(false);
		result.fill(0);
		
		// compute threshold 
		double [] pixels = acc.getPixels();
		Arrays.sort(pixels);
		double threshold=pixels[pixels.length-(int)(pixels.length*0.0002)];
		
		// draw detected line in result
		for(int y=0;y<acc.ydim;y++)
			for(int x=0;x<acc.xdim;x++)
			{
				if( acc.getPixelXYDouble(x, y)>=threshold)
				{				
					Line l=HoughTransform.getLineFromBuffer(acc,  x, y);
					//System.out.println(l.toString());
					l.drawGrayLine(result,0, acc.getPixelXYDouble(x, y));
						
				}
			
		}
		IMath.scaleToZeroOne(edge);
		
		MultiView mv=MViewer.exec();
		mv.add(im,"image");
		mv.add(edge,"edge detection");
		mv.add(acc,"hough transform");
		mv.add(Blending.exec(edge,result,0.5),"result");
		

		
		im=new DoubleImage(400,100,1,1,1);
		int inc=100;
		Line l=new Line(50,0,150,100);
		l.drawGrayLine(im, 0, 1.0);
		l.getX1().x+=inc;
		l.getX2().x+=inc;
		l.drawGrayLine(im, 0, 1.0);
		l.getX1().x+=inc;
		l.getX2().x+=inc;
		l.drawGrayLine(im, 0, 1.0);
		l.getX1().x+=inc;
		l.getX2().x+=inc;
		//l.drawGrayLine(im, 0, 1.0);
		Image reconst1=im.copyImage(false);
		DoubleImage hough1=HoughTransform.exec(im, 1.0, 0.003, false);
		for(int y=0;y<hough1.ydim;y++)
			for(int x=0;x<hough1.xdim;x++)
			{
				if(hough1.getPixelXYDouble(x, y)>90.0)
				{
					Line l1=HoughTransform.getLineFromBuffer(hough1, x, y);
					l1.drawGrayLine(reconst1, 0, 1.0);
					//System.out.println(l1.toString());
				}
			}
		
		//System.out.println("Wrapped space ...........");
		Image reconst2=im.copyImage(false);
		
		DoubleImage hough2=HoughTransform.exec(im, 1.0, 0.003, true);
		for(int y=0;y<hough2.ydim;y++)
			for(int x=0;x<hough2.xdim;x++)
			{
				if(hough2.getPixelXYDouble(x, y)>200.0)
				{
					Line l1=HoughTransform.getLineFromBuffer(hough2, x, y);
					//System.out.println(l1.toString());
					l1.drawGrayLine(reconst2, 0, 1.0);
					
				}
			}
		MultiView mv2 = MViewer.exec();
		mv2.add(im,"parllel lines");
		mv2.add(hough1,"usual Hough transform");
		mv2.add(reconst1,"result of usual Hough Transform");
		mv2.add(hough2,"cylinder Hough transform");
		mv2.add(reconst2,"result of cylinder Hough Transform");
	
		
		
	}

}
