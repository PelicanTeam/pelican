package fr.unistra.pelican.algorithms.applied.video.shot;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.histogram.Histogram;


/**
 * This class computes histogram-based interframe differences in image sequences for
 * shot change detection
 * 
 * S. Lefèvre, N. Vincent, Efficient and Robust Shot Change Detection, Journal
 * of Real Time Image Processing, Springer, Vol. 2, No. 1, october 2007, pages
 * 23-34, doi:10.1007/s11554-007-0033-1.
 * 
 * @author Sébatien Lefèvre
 */
public class HistogramBasedInterframeDifference extends Algorithm {
	
	/**
	 * The input image sequence
	 */
	public Image input;

	/**
	 * The array of difference values
	 */
	public Double[] output;

	/**
	 * Default constructor
	 */
	public HistogramBasedInterframeDifference() {
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/**
	 * Computes histogram-based interframe differences in image sequences for shot
	 * change detection
	 * 
	 * @param input
	 *            The input image sequence
	 * @return The array of difference values
	 */
	public static Double[] exec(Image input) {
		return (Double[]) new HistogramBasedInterframeDifference().process(input);
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		// check if the image is a video sequence
		if (input.tdim < 2)
	    	throw new AlgorithmException("The input image is not a video sequence");
		int duration=input.getTDim();
		output=new Double[duration];
		double hist1[];
		double hist2[];
		Image img1;
		Image img2;
		double diff;
			// Sequence scanning
			for(int t=1;t<duration;t++) {
				img1=input.getImage4D(t-1,Image.T);
				img2=input.getImage4D(t,Image.T);
				if (input.isColor()) {
					img1=(Image) new RGBToGray().process(img1);
					img2=(Image) new RGBToGray().process(img2);
				}
				hist1=Histogram.exec(img1,true);
				hist2=Histogram.exec(img2,true);
				diff=0;
				for(int v=0;v<hist1.length;v++)
					diff+=Math.abs(hist1[v]-hist2[v]);
				// diff is in [0,2] so output is in [0,100]
				output[t-1]=diff*100/2;
			}
			output[duration-1]=0.0;
	}


	
}
