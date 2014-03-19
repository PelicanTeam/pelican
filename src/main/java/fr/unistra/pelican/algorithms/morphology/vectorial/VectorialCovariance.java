package fr.unistra.pelican.algorithms.morphology.vectorial;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;

/**
 * This class produces a normalized vectorial covariance curve on 4 directions (0,90,180,270)
 * The result is normalised in [0,1]
 * For instance a length of 10 would lead to a curve of 40.
 * 
 * @author Abdullah
 * 
 */
public class VectorialCovariance extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the length of the curve for each direction
	 */
	public int length;

	/**
	 * the vector ordering
	 */
	public VectorialOrdering vo;

	/**
	 * the resulting curve
	 */
	public double[] curve;

	/**
	 * whether to have scale invariance or not
	 */
	public boolean scaleInvariance = false;

	/**
	 * horizontal moment
	 */
	public int MOMENTX;
	
	/**
	 * vertical moment
	 */
	public int MOMENTY;
	
	/**
	 * This method produces a normalized vectorial covariance curve on 4 directions (0,90,180,270)
	 * @param input the input image
	 * @param length the length of the covariance curve on each direction and with each operator
	 * @param scale whether to have scale invariance or not
	 * @param momentX horizontal moment
	 * @param momentY vertical moment
	 * @param vo vector ordering
	 * @return the covariance curve
	 */
	public static double[] exec(Image input,Integer length,Boolean scaleInvariance,Integer momentX,Integer momentY,VectorialOrdering vo) {
		return (double[]) new VectorialCovariance().process(input,length,scaleInvariance,momentX,momentY,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialCovariance() {

		super();
		super.inputs = "input,length,scaleInvariance,MOMENTX,MOMENTY,vo";
		super.outputs = "curve";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {
			int size = length * 4 * input.getBDim();
			curve = new double[size];

			double[] originalVolumes = new double[input.getBDim()];

			for (int b = 0; b < input.getBDim(); b++)
				originalVolumes[b] = moment(input, b, MOMENTX, MOMENTY);

			// every size
			for (int i = 1; i <= length; i++) {

				int side = i * 2 + 1;

				// vertical line
				BooleanImage se = new BooleanImage(side,
						side,1,1,1);
				se.setCenter(new Point(i, i));
				se.setPixelXYBoolean(0, i, true);
				se.setPixelXYBoolean(side - 1, i, true);

				// erosion
				Image tmp = (Image) new VectorialErosion().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * length + i - 1] = moment(tmp,b,MOMENTX,MOMENTY) / originalVolumes[b];

				// left diagonal line
				se = new BooleanImage(side, side, 1,1,1);
				se.setCenter(new Point(i, i));
				se.setPixelXYBoolean(0, 0, true);
				se.setPixelXYBoolean(side - 1, side - 1, true);

				// erosion
				tmp = (Image) new VectorialErosion().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * length + length + i - 1] = moment(tmp, b,
							MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// horizontal line
				se = new BooleanImage(side, side, 1,1,1);
				se.setCenter(new Point(i, i));
				se.setPixelXYBoolean(i, 0, true);
				se.setPixelXYBoolean(i, side - 1, true);

				// erosion
				tmp = (Image) new VectorialErosion().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * length + 2 * length + i - 1] = moment(tmp, b,
							MOMENTX, MOMENTY)
							/ originalVolumes[b];

				// right diagonal line
				se = new BooleanImage(side, side, 1,1,1);
				se.setCenter(new Point(i, i));
				se.setPixelXYBoolean(side - 1, 0, true);
				se.setPixelXYBoolean(0, side - 1, true);

				// erosion
				tmp = (Image) new VectorialErosion().process(input, se, vo);

				for (int b = 0; b < input.getBDim(); b++)
					curve[b * 4 * length + 3 * length + i - 1] = moment(tmp, b,
							MOMENTX, MOMENTY)
							/ originalVolumes[b];
			}
		} catch (PelicanException e) {
			e.printStackTrace();
		}
	}

	private double moment(Image img, int channel, int i, int j) {
		double d = 0.0;

		double ort = 0.0;

		int gamma = (i + j) / 2 + 1;

		for (int x = 0; x < img.getXDim(); x++) {
			for (int y = 0; y < img.getYDim(); y++) {

				if ( input.isPresentXYB( x,y,channel ) ) { 

					double tmp = img.getPixelXYBDouble(x, y, channel);
					ort += tmp;
					d += Math.pow(x + 1, i) * Math.pow(y + 1, j) * tmp;
				}
			}
		}

		ort = Math.pow(ort, gamma);

		if ((i > 0 || j > 0) && scaleInvariance == true)
			d = d / ort;

		return d;
	}

}
