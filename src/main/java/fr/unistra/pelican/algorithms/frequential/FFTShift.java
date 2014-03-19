/**
 * 
 */
package fr.unistra.pelican.algorithms.frequential;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;

/**
 * FFTShift  rearranges the outputs of fft, and fft2 by moving the zero-frequency component to the center of the array. 
 * It is useful for visualizing a Fourier transform with the zero-frequency component in the middle of the spectrum.
 * <p>
 * Take care to set inverse option to true if you want to be sure that the inverse transform is correctly done 
 * (should not be needed when dimensions are even as inverse transform reduces to the original one in this particular case).
 * 
 * @author Benjamin Perret
 *
 */
public class FFTShift extends Algorithm {

	/**
	 * Input Image
	 */
	public Image inputImage;
	
	/**
	 * Do the inverse transform, inverse transform is the same as original one if image dimensions are even
	 */
	public boolean inverse=false;
	
	/**
	 * Result
	 */
	public Image outputImage;
	
	private int xdim;
	private int ydim;
	private int bdim;
	
	/**
	 * 
	 */
	public FFTShift() {
		super.inputs="inputImage";
		super.options="inverse";
		super.outputs="outputImage";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		outputImage=inputImage.copyImage(false);
		xdim=inputImage.xdim;
		ydim=inputImage.ydim;
		bdim=inputImage.bdim;
		
		int mx=xdim/2;
		int rx=xdim-mx;
		int my=ydim/2;
		int ry=ydim-my;
		if(inverse)
			doIt(rx, mx, ry, my);
		else
			doIt(mx, rx, my, ry);
		

	}

	private void doIt(int mx, int rx, int my, int ry)
	{
		for (int b = 0; b < bdim; b++) {
			// down right corner move to up left corner
			for (int y = 0; y < ry; y++)
				for (int x = 0; x < rx; x++)
					outputImage.setPixelXYBDouble(x, y, b, inputImage
							.getPixelXYBDouble(x + mx, y + my, b));
			
			// down left corner move to up right corner
			for (int y = 0; y < ry; y++)
				for (int x = rx; x < xdim; x++)
					outputImage.setPixelXYBDouble(x, y, b, inputImage
							.getPixelXYBDouble(x - rx, y + my, b));
			
			// up right corner move to down left corner
			for (int y = ry; y < ydim; y++)
				for (int x = 0; x < rx; x++)
					outputImage.setPixelXYBDouble(x, y, b, inputImage
							.getPixelXYBDouble(x + mx, y -ry, b));
			
			// up left corner move to down right corner
			for (int y = ry; y < ydim; y++)
				for (int x = rx; x < xdim; x++)
					outputImage.setPixelXYBDouble(x, y, b, inputImage
							.getPixelXYBDouble(x - rx, y -ry, b));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage)	
	{
		return (T)(new FFTShift()).process(inputImage);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T inputImage, boolean inverse)	
	{
		return (T)(new FFTShift()).process(inputImage, inverse);
	}
	
	
	public static void main(String[] args) {
		DoubleImage im = new DoubleImage(ImageLoader.exec("samples/lenna.png"),true);
		MultiView mv=MViewer.exec(im);
		//im=Crop2D.exec(im,0,0,254,254);
		DoubleImage [] op = FFT2.exec(im, null, false);
		
		
		
		mv.add(Magnitude.exec(FFT2.exec(op[0], op[1], true)));
		
		DoubleImage shift1=FFTShift.exec(op[0]);
		DoubleImage shift2=FFTShift.exec(shift1, true);
		Image diff=Difference.exec(op[0], shift2, false);
		
		
		mv.add(shift1);
		mv.add(shift2);
		mv.add(diff);
		mv.add(Magnitude.exec(FFT2.exec(shift2, op[1], true)));
	}

}
