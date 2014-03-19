package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayASF;
import fr.unistra.pelican.algorithms.morphology.gray.GrayLeveling;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * 
 * Normalised histograms in a leveling based scale-space
 * 
 * HSV 7-3-3
 * 
 * @author Erchan Aptoula
 * 
 */
public class LevelingBasedMultiscaleHistogram extends Descriptor
{
	public Image input;

	public double[] output;

	public int scales = 3;
	
	public int[] size = {7,3,3};

	/**
	 * Constructor
	 * 
	 */
	public LevelingBasedMultiscaleHistogram() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int totalHistoSizePerScale = 7 * 3 * 3;

		output = new double[scales * totalHistoSizePerScale];

		int nbpxhere = 0;
		for (int s = 0; s < scales; s++) {

			// prepare this scale
			Image tmp = null;

			BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
			Image marker = (Image) new GrayASF().process( 
					input, se, GrayASF.OPENING_FIRST, new Integer(s * 2 + 1));
			tmp = (Image) new GrayLeveling().process(input, marker, new Integer(0));

			// extract the histogram
			nbpxhere = 0;	// yes, this number of present pixels is recomputed scales times..
			for (int x = 0; x < input.getXDim(); x++) {
				for (int y = 0; y < input.getYDim(); y++) { 

					if ( input.isPresentXYZT( x,y,0,0 ) ) { 

						int[] p = tmp.getVectorPixelXYZTByte( x,y,0,0 );
						output[ s*totalHistoSizePerScale 
						        + p[0]*size[1]*size[2] 
						        + p[1]*size[2] 
						        + p[2]					]++;
						nbpxhere++;
					}
				}
			}
		}

		// normalize
		for ( int i = 0 ; i < output.length ; i++ ) output[i] = output[i] / nbpxhere;
	}
	
	/**
	 * 
	 * @param inputImage Image to be converted in a histogram.
	 * @param size The number of bins of each band.
	 * @param scales The number of scales.
	 * @return The normalized histogram.
	 */
	public static Image exec(Image input)
	{
		return (Image) new LevelingBasedMultiscaleHistogram().process(input);
	}

	public double distance(double[] d1, double[] d2)
	{
		return Tools.pyramidMatchDistance(d1,d2,3,7*3*3);
	}
}
