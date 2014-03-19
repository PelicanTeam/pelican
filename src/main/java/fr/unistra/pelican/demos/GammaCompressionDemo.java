/**
 * 
 */
package fr.unistra.pelican.demos;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.GammaCompression;
import fr.unistra.pelican.algorithms.histogram.HistogramCorrection;
import fr.unistra.pelican.algorithms.histogram.HistogramCorrection.MultiBandPolicy;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.gui.MultiViews.View;
import fr.unistra.pelican.util.colour.REC709GammaCompressionModel;
import fr.unistra.pelican.util.colour.SRGBGammaCompressionModel;
import fr.unistra.pelican.util.colour.SimpleGammaCompressionModel;

/**
 * @author Benjamin Perret
 *
 */
public class GammaCompressionDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Image im1=ImageLoader.exec("samples/AstronomicalImagesFITS/PGC0035538_i.fits");
		Image im2=ImageLoader.exec("samples/AstronomicalImagesFITS/PGC0035538_r.fits");
		Image im3=ImageLoader.exec("samples/AstronomicalImagesFITS/PGC0035538_g.fits");
		Image im=im1.newInstance(im1.xdim, im1.ydim, 1, 1, 3);
		im.setImage2D(im1, 0, 0, 0);
		im.setImage2D(im2, 0, 0, 1);
		im.setImage2D(im3, 0, 0, 2);
		
		im=HistogramCorrection.exec(im, 0.999,HistogramCorrection.STRETCH_NOT_USE,MultiBandPolicy.Median);
		Image correct1=GammaCompression.exec(im, new SimpleGammaCompressionModel());
		Image correct2=GammaCompression.exec(im, new SRGBGammaCompressionModel());
		Image correct3=GammaCompression.exec(im, new REC709GammaCompressionModel());
		MultiView mv=MViewer.exec();
		View v=mv.add(im,"No correction");
		v.setAutoCorrect(false);
		v.setScaleResult(false);
		v=mv.add(correct1,"Simple Compression");
		v.setAutoCorrect(false);
		v.setScaleResult(false);
		v=mv.add(correct2,"sRGB Compression");
		v.setAutoCorrect(false);
		v.setScaleResult(false);
		v=mv.add(correct3,"REC709 Compression");
		v.setAutoCorrect(false);
		v.setScaleResult(false);
		mv.lockAllViews();
	}

}
