package fr.unistra.pelican.algorithms.descriptors.texture;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.GrayClosingByReconstruction;
import fr.unistra.pelican.util.data.DoubleArrayData;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;



/**
 * Performs a gray granulometry with a square shaped flat structuring element.
 * 
 * @author Erchan Aptoula
 * @author Régis Witz (mask support and framework adaptation)
 */
public class ConnectedGrayCrossGranulometry extends Descriptor {

	public int length = 25;

	/** First input parameter. */
	public Image input;

	/** Output parameter. */
	public DoubleArrayData output;

	/** Constructor */
	public ConnectedGrayCrossGranulometry() {

		super();
		super.inputs = "input";
		super.options = "length";
		super.outputs = "output";
	}

	public static DoubleArrayData exec(Image input) {
		return (DoubleArrayData) new ConnectedGrayCrossGranulometry()
				.process(input);
	}

	public static DoubleArrayData exec(Image input, int length) {
		return (DoubleArrayData) new ConnectedGrayCrossGranulometry().process(
				input, length);
	}

	/** @see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException {

		int size = length * input.getBDim(); // size of SEs increases in steps of 2
		Double[] values = new Double[size];
		for (int i = 0; i < size; i++)
			values[i] = new Double(0);

		int MOMENTX = 0;
		int MOMENTY = 0;

		// every size
		for ( int i = 0 ; i < length ; i++ ) {

			int side = i * 2 + 1;
			BooleanImage seHor = FlatStructuringElement2D
					.createHorizontalLineFlatStructuringElement(side);
			BooleanImage seVer = FlatStructuringElement2D
					.createVerticalLineFlatStructuringElement(side);

			// schnell Hans, schnell!!!
			Image tmp = GrayClosingByReconstruction.exec( this.input, seHor );
			tmp = GrayClosingByReconstruction.exec(tmp, seVer);

			for (int b = 0; b < input.getBDim(); b++)
				values[ b * this.length + i ] = moment2( tmp, 		 b, MOMENTX,MOMENTY, side/2 )
											  / moment2( this.input, b, MOMENTX,MOMENTY, side/2 );

		}

		this.output = new DoubleArrayData();
		this.output.setDescriptor( (Class) this.getClass() );
		this.output.setValues(values);
	}

	@SuppressWarnings("unused")
	private double moment(Image img, int channel, int i, int j, int radius) {

		double d = 0.0;
		for (int x = radius; x < img.getXDim() - radius; x++)
			for (int y = radius; y < img.getYDim() - radius; y++)
				if (img.isPresentXYB(x, y, channel))
					d += img.getPixelXYBByte(x, y, channel);
		return d;
	}

	private double moment2(Image img, int channel, int i, int j, int radius) {

		double d = 0.0;
		for (int x = 0; x < img.getXDim(); x++)
			for (int y = 0; y < img.getYDim(); y++)
				if (img.isPresentXYB(x, y, channel))
					d += img.getPixelXYBByte(x, y, channel);
		return d;
	}

	// public static double distance( Data d1, Data d2 ) {
	//
	// Double[] v1 = (Double[]) d1.getValues();
	// Double[] v2 = (Double[]) d2.getValues();
	// return Tools.euclideanDistance( v1,v2 );
	// }

}
