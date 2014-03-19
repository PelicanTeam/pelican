/**
 * 
 */
package fr.unistra.pelican.demos;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.algorithms.histogram.HistogramCorrection;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryClosing;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.mask.RectangleMask;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * @author Benjamin Perret
 *
 */
public class MaskDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// load image
		DoubleImage im = (DoubleImage)ImageLoader.exec("samples/AstronomicalImagesFITS/img1-12.fits");
		
		// border size
		int dx=im.xdim/4;
		int dy=im.ydim/4;
		
		// define working area
		RectangleMask rm1 = new RectangleMask(0,0,dx,dy,RectangleMask.ALL_CHANNELS,RectangleMask.IS_NOT_PRESENT);
		RectangleMask rm2 = new RectangleMask(im.xdim-dx,0,im.xdim,dy,RectangleMask.ALL_CHANNELS,RectangleMask.IS_NOT_PRESENT);
		RectangleMask rm3 = new RectangleMask(im.xdim-dx,im.ydim-dy,im.xdim,im.ydim,RectangleMask.ALL_CHANNELS,RectangleMask.IS_NOT_PRESENT);
		RectangleMask rm4 = new RectangleMask(0,im.ydim-dy,dx,im.ydim,RectangleMask.ALL_CHANNELS,RectangleMask.IS_NOT_PRESENT);
		
		// set working area
		im.pushMask(rm1);
		im.pushMask(rm2);
		im.pushMask(rm3);
		im.pushMask(rm4);
		
		// define another mask for iterative thresholding
		BooleanImage bim = new BooleanImage(im.xdim,im.ydim,1,1,1);
		bim.fill(true);
		im.pushMask(bim);
		
		// iterative threshold
		double mean=Double.POSITIVE_INFINITY;
		
		double m=Double.NEGATIVE_INFINITY;
		
		double k=2.5;
		
		while (!Tools.relativeDoubleEquality(mean, m))
		{
			mean=m;
			// compute mean and deviation over pixels
			m=0.0;
			double m2=0.0;
			double n=0;
			double dev=0.0;
			for(int y=0;y<im.ydim;y++)
				for(int x=0;x<im.xdim;x++)
					if(im.isPresentXY(x, y))
					{
						double v=im.getPixelXYDouble(x, y);
						m+=v;
						m2+=v*v;
						n++;
					}
			
			m2/=n;
			m/=n;
			dev=Math.sqrt(m2-m*m);
			
			System.out.println("m " +m + " dev " + dev +"n " + n);
			// cut image
			double lim = m + k * dev;
			
			for(int y=0;y<im.ydim;y++)
				for(int x=0;x<im.xdim;x++)
					if(im.isPresentXY(x, y))
					{
						double v=im.getPixelXYDouble(x, y);
						if(v>lim)
							bim.setPixelXYBoolean(x, y, false);
					}
			
			
		}
		
		// enhancement
		//bim= BinaryClosing.exec(bim, FlatStructuringElement2D.createCircleFlatStructuringElement(2));
	
		Viewer2D.exec(HistogramCorrection.exec(im,HistogramCorrection.STRETCH_NOT_USE), "Image");
		Viewer2D.exec(bim, "Segmentation map computed without corners");
	}

}
