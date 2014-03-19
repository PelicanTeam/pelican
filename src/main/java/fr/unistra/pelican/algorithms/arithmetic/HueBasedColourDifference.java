package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

/**
 * Compute the difference between two images containing a hue band
 * 
 * @author  ?, Benjamin Perret
 */
public class HueBasedColourDifference extends Algorithm {
	/**
	 * First input image.
	 */
	public Image input1;

	/**
	 * Second input image.
	 */
	public Image input2;

	/**
	 * Algorithm result: difference between input image one and two.
	 */
	public Image output;


	/**
	 * Constructor
	 * 
	 */
	public HueBasedColourDifference() {

		super();
		super.inputs = "input1,input2";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = input1.copyImage(false);

		int xdim = input1.getXDim();
		int ydim = input1.getYDim();

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				double[] p1;
				double[] p2;
				if ( input1.isPresentXY(x,y) ) p1 = input1.getVectorPixelXYZTDouble(x, y, 0, 0);
				else p1 = new double[ input1.getBDim() ];
				if ( input1.isPresentXY(x,y) ) p2 = input2.getVectorPixelXYZTDouble(x, y, 0, 0);
				else p2 = new double[ input2.getBDim() ];

				// why was this line here ?  Regis.
//				output.setPixelXYBDouble(x, y, 0, Tools.hueDifference(p1[0],p2[0]));

				if (p1[1] - p2[1] < 0.0)
					output.setPixelXYBDouble(x, y, 1, 0.0);
				else
					output.setPixelXYBDouble(x, y, 1, p1[1] - p2[1]);

				if (p1[2] - p2[2] < 0.0)
					output.setPixelXYBDouble(x, y, 2, 0.0);
				else
					output.setPixelXYBDouble(x, y, 2, p1[2] - p2[0]);
			}
		}
	}
	
	/**
	 * Compute the difference between two images containing a hue band.
	 * 
	 * @param input1 First input image.
	 * @param input2 Second input image.
	 * @return  difference between two images;
	 */
	public static Image exec(Image input1, Image input2) {
		return (Image) new HueBasedColourDifference().process(input1,
				input2);
	}
}
