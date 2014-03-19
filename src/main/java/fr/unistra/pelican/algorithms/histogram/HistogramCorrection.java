package fr.unistra.pelican.algorithms.histogram;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Tools;


/**
 * <p>This class performs an histogram correction and dynamic pixel scaling on an
 * image. Histogram correction keeps 99%
 * of original pixels. 
 * <p>Histogram stretching is then performed with a function chosen among log, square, quare_root or none.
 * and pixels are
 * <p>Finally pixels are scaled in [0;1].
 * 
 * <p>Each band is corrected independently.
 * 
 * <p>Masked pixels are not considered to evaluate correction parameters but are scaled at the end. 
 * This imply that final corrected image pixel values may not be in range [0;1] 
 * but values of pixels not masked will be in [0;1]. Use MViewer to display such kind of image, 
 * it won't be fooled by masked pixels.
 * 
 * <p> T dim and Z dim are NOT considered
 * @author Benjamin Perret
 * 
 */
public class HistogramCorrection extends Algorithm {

	/**
	 * How do we process multiband images
	 * - Independent: marginal processing
	 * - Max (min, mean, median) : final dynamic range is max (min, mean, median) of corrected marginal  dynamic ranges
	 * @author Benjamin Perret
	 *
	 */
	public static enum MultiBandPolicy {Independent,Max,Min,Mean,Median};
	
	
	/**
	 * Histogram Stretching option: linear to 0,1.
	 */
	public static final int STRETCH_NOT_USE = 0;

	/**
	 * Histogram Stretching option: use Log law, finally mapped to [0,1].
	 */
	public static final int STRETCH_LOG = 1;

	/**
	 * Histogram Stretching option: use square root law, finally mapped to [0,1].
	 */
	public static final int STRETCH_SQUAREROOT = 2;

	/**
	 * Histogram Stretching option: use power two law, finally mapped to [0,1].
	 */
	public static final int STRETCH_POWER2 = 3;
	
	/**
	 * Histogram Stretching option: only the percentile cut is done.
	 */
	public static final int STRETCH_DO_NOTHING = 4;

	/**
	 * Ratio of kept pixels
	 */
	public double pixelsRatio = 0.999;

	/**
	 * Input Image
	 */
	public Image input = null;

	/**
	 * Input strecth param
	 */
	public int stretchParam = STRETCH_NOT_USE;

	/**
	 * What is the behavior to adopt with multiband images
	 */
	public MultiBandPolicy multiBandPolicy=MultiBandPolicy.Independent;
	
	/**
	 * Perform [0,1] scaling at the end of the process ?
	 */
	public boolean scaleToZeroOne=true;
	
	/**
	 * Output image
	 */
	public Image output = null;

	/**
	 * Number of bins for histogram decomposition
	 */
	private int bins = 1024;

	/**
	 * Total number of pixels in image
	 */
	private int nbPixels = 0;

	/**
	 * Min value of all pixels in image
	 */
	private double min = Double.POSITIVE_INFINITY;

	/**
	 * Max value of all pixels in image
	 */
	private double max = Double.NEGATIVE_INFINITY;

	/**
	 * Interval size of a bin
	 */
	private double binSize = Double.POSITIVE_INFINITY;

	/**
	 * Computed histogram
	 */
	private int[] histogram = null;

	/**
	 * index of first element of reduced histogram
	 */
	private int minHistogram = 0;

	/**
	 * index of last element of reduced histogram
	 */
	private int maxHistogram = 0;

	/**
	 * number of pixels in reduced histogram
	 */
	private int nbPixelsInHistogram = 0;

	/**
	 * The stretching function
	 */
	private StretchingFunction sF = null;

	
	private boolean debug=false;
	
	/**
	 * Constructor
	 * 
	 */
	public HistogramCorrection() {

		super();
		super.inputs = "input";
		super.options = "pixelsRatio,stretchParam,multiBandPolicy";
		super.outputs = "output";
		
	}

	/**
	 * Both performs stretching and scaling at the same time
	 */
	private void writeOutput(int b) {
		double tmpMin = sF.getMinBound();
		double tmpMax = sF.getMaxBound();
		double sMin = sF.stretch(tmpMin);
		double sMax = sF.stretch(tmpMax);
		for (int i = b; i < input.size(); i += input.bdim) {
			if (input.isPresent(i)) {
				double val = input.getPixelDouble(i);
				
				// performs cut
				val = Math.min(Math.max(val, min), max);
				if(scaleToZeroOne){
					// scales to optimal stretching function interval
					val = (tmpMax - tmpMin) * (val - min) / (max - min) + tmpMin;
					// stretch
					val = sF.stretch(val);
					// scale to 0-1
				
					//val = (val - tmpMin) / (tmpMax - tmpMin);
					val = (val - sMin) / (sMax - sMin);
				}
				output.setPixelDouble(i, val);

			}
			else{
				output.setPixelDouble(i, 0);
			}
		}
	}

	/**
	 * Create the stretching function
	 */
	private void chooseStretchParam() {
		switch (stretchParam) {
		case STRETCH_NOT_USE:
			sF = new IdStretch();
			scaleToZeroOne=true;
			break;
		case STRETCH_LOG:
			sF = new LogStretch();
			scaleToZeroOne=true;
			break;
		case STRETCH_SQUAREROOT:
			sF = new SqrtStretch();
			scaleToZeroOne=true;
			break;
		case STRETCH_POWER2:
			sF = new PowerStretch();
			scaleToZeroOne=true;
			break;
		case STRETCH_DO_NOTHING:
			sF = new IdStretch();
			scaleToZeroOne=false;
			break;
		}
	}

	/**
	 * Determine the upper and lower bound of new histogram with respect to the
	 * new pixel ratio
	 */
	private void cutHistogram() {
		minHistogram = 0;
		maxHistogram = bins - 1;
		//nbPixelsInHistogram = input.getXDim() * input.getYDim();
		while (((double) nbPixelsInHistogram / (double) nbPixels) > pixelsRatio) // until
		// pixel
		// ratio
		// is
		// reached
		{
			// System.out.println(((double)nbPixelsInHistogram/(double)nbPixels) + " " +histogram[maxHistogram] + " "			+histogram[minHistogram] );
			// delete the smaller bin on the extrema
			if (histogram[minHistogram] > histogram[maxHistogram])
				nbPixelsInHistogram -= histogram[maxHistogram--];
			else
				nbPixelsInHistogram -= histogram[minHistogram++];
		}
		if(maxHistogram <= minHistogram)
		{
			//System.out.println("Histogram Cut: abording 99% cut, dynamic range too small!");
		}
		else{
		// this is new min max value
			max = (min+binSize * maxHistogram);
			min = (min+binSize * minHistogram);
		}
		if (debug)System.out.println("Histogram Correction -> min/max after cut: "+ min+ "/" +max);
	}

	/**
	 * Search for min and max pixels of input
	 */
	private void getMinMax(int b) {
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		for (int i = b; i < input.size(); i+=input.bdim)
			if(input.isPresent(i)) {
				double val = input.getPixelDouble(i);
				
				if (val < min)
					min = val;
				if (val > max)
					max = val;

			}
		
	}

	/**
	 * Compute histogram, image statistic must be determined before
	 */
	private void computeHistogram(int b) {
		nbPixelsInHistogram=0;
		if (histogram==null)histogram = new int[bins];
		else for(int i=0;i<histogram.length;i++)
			histogram[i]=0;
		
		for (int i = b; i < input.size(); i+=input.bdim)
			if(input.isPresent(i))
			 {
			double val = input.getPixelDouble(i);
			
			/*
			 * Min max are performed to avoid approximation errors
			 */
			int bi=Math.max(0, Math.min((int) ((val-min) / binSize), bins - 1));
			//System.out.println("Val:" +(int) ((val-min) / binSize) + "  Bin: " +bi);
			histogram[bi]++;
			nbPixelsInHistogram++;
		}
		nbPixels=nbPixelsInHistogram;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		/*
		 * Compute image statistic
		 */
		if(pixelsRatio<0.0 || pixelsRatio>1.0)
		{
			pixelsRatio=0.995;
			System.err.println("Histogram Correction, ratio of kept pixels must be between 0.0 and 1.0. Ratio is set to 0.995");
		}
		//nbPixelsInHistogram = nbPixels = input.getXDim() * input.getYDim();
		bins = 10000;// (int)Math.floor(1.0+3.3*Math.log(nbPixels));
		chooseStretchParam();
		output = input.copyImage(false);
		
		switch(multiBandPolicy){
			case Independent:
				processIndepent();
				break;
			case Max:
			case Min:
			case Mean:
			case Median:
				processMulti();
				break;
		}
	
	}

	private void processIndepent(){
		for (int b=0;b<input.getBDim();b++)
		{
			getMinMax(b);
			if (debug)System.out.println("Histogram Correction -> min/max: "+ min+ "/" +max);
			binSize = (double) (max - min) / (double) bins;
			if (debug)System.out.println("Histogram Correction -> binsize: "+ binSize);
			maxHistogram = bins - 1;

			/*
			 * Compute histogram and performs cut
			 */
			computeHistogram(b);
			cutHistogram();


			/*
			 * Create Result
			 */
			writeOutput(b);
		}
	}
	
	private void processMulti(){
		double [] maxs=new double[input.bdim];
		double [] mins=new double[input.bdim];
		for (int b=0;b<input.bdim;b++)
		{
			getMinMax(b);
			if (debug)System.out.println("Histogram Correction -> min/max: "+ min+ "/" +max);
			binSize = (double) (max - min) / (double) bins;
			if (debug)System.out.println("Histogram Correction -> binsize: "+ binSize);
			maxHistogram = bins - 1;

			/*
			 * Compute histogram and performs cut
			 */
			computeHistogram(b);
			cutHistogram();
			
			maxs[b]=max;
			mins[b]=min;
		}
		Arrays.sort(maxs);
		Arrays.sort(mins);
		
		switch (multiBandPolicy){
			case Max:
				max=maxs[input.bdim-1];
				min=mins[0];
				break;
			case Min:
				max=maxs[0];
				min=mins[input.bdim-1];
				break;
			case Mean:
				max=Tools.mean(maxs);
				min=Tools.mean(mins);
				break;
			case Median:
				max=maxs[maxs.length/2];
				min=mins[mins.length/2];
				break;
		}
		for (int b=0;b<input.bdim;b++)
		{
			/*
		 	* Create Result
		 	*/
			writeOutput(b);
		}
	}
	
//	public static void main(String[] args) {
//		Image im = (Image) new ImageLoader()
//				.process("samples/AstronomicalImagesFITS/img1-12.fits");
//		new Viewer2D()
//				.process(((DoubleImage) im).scaleToZeroOne(), "originale");
//		Image im2 = (Image) new HistogramCorrection().process(im,
//				HistogramCorrection.STRETCH_NOT_USE);
//		new Viewer2D().process(im2, "corrigée ");
//		Image im3 = (Image) new HistogramCorrection().process(im,
//				HistogramCorrection.STRETCH_LOG);
//		new Viewer2D().process(im3, "corrigée - Log ");
//		Image im4 = (Image) new HistogramCorrection().process(im,
//				HistogramCorrection.STRETCH_POWER2);
//		new Viewer2D().process(im4, "corrigée - Power2 ");
//		Image im5 = (Image) new HistogramCorrection().process(im,
//				HistogramCorrection.STRETCH_SQUAREROOT);
//		new Viewer2D().process(im5, "corrigée - SquareRoot");
//	}

	/**
	 * This class performs an histogram correction and dynamic pixel scaling on
	 * an image (Each band is corrected independently.).
	 * 
	 * @param input Image to be corrected.
	 * @param stretchParam strecth parameter.
	 * @return The corrected image.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T input,double percentileCut, int stretchParam) {
		return (T) new HistogramCorrection().process(input,percentileCut, stretchParam);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T input,double percentileCut, int stretchParam, MultiBandPolicy multiBandPolicy) {
		return (T) new HistogramCorrection().process(input,percentileCut, stretchParam,multiBandPolicy);
	}
	
	/**
	 * This class performs an histogram correction and dynamic pixel scaling on
	 * an image (Each band is corrected independently.).
	 * 
	 * @param input Image to be corrected.
	 * @return The corrected image.
	 */
	@SuppressWarnings("unchecked")
	public static  <T extends Image> T exec(T input) {
		return (T) new HistogramCorrection().process(input);
	}
	
	/**
	 * This class performs an histogram correction and dynamic pixel scaling on
	 * an image (Each band is corrected independently.).
	 * 
	 * @param input Image to be corrected.
	 * @return The corrected image.
	 */
	@SuppressWarnings("unchecked")
	public static  <T extends Image> T exec(T input, double percentileCut) {
		return (T) new HistogramCorrection().process(input,percentileCut);
	}

}

/**
 * Small interface to implement stretching function
 * 
 * @author Benjamin Perret
 * 
 */
interface StretchingFunction {
	/**
	 * Returned the corrected value
	 * 
	 * @param a
	 *            original pixel value
	 * @return corrected value
	 */
	public double stretch(double a);

	/**
	 * Get min bound to perform scaling before strectching
	 * 
	 * @return
	 */
	public double getMinBound();

	/**
	 * Get max bound to perform scaling before strectching
	 * 
	 * @return
	 */
	public double getMaxBound();

}

/**
 * Identity function
 * 
 * @author Benjamin Perret
 * 
 */
class IdStretch implements StretchingFunction {
	public double stretch(double a) {
		return a;
	}

	public double getMaxBound() {
		return 1.0;
	}

	public double getMinBound() {
		return 0.0;
	}
}

/**
 * Logarithmic transform
 * 
 * @author Benjamin Perret
 * 
 */
class LogStretch implements StretchingFunction {
	public double stretch(double a) {
		return Math.log(a);
	}

	public double getMaxBound() {
		return 10000.0;
	}

	public double getMinBound() {
		return 1.0;
	}
}

/**
 * Square root transform
 * 
 * @author Benjamin Perret
 * 
 */
class SqrtStretch implements StretchingFunction {
	public double stretch(double a) {
		return Math.sqrt(a);
	}

	public double getMaxBound() {
		return 10.0;
	}

	public double getMinBound() {
		return 1.0;
	}
}

/**
 * Power two transform
 * 
 * @author Benjamin Perret
 * 
 */
class PowerStretch implements StretchingFunction {
	public double stretch(double a) {
		return a * a;
	}

	public double getMaxBound() {
		return 5.0;
	}

	public double getMinBound() {
		return 0.0;
	}
}