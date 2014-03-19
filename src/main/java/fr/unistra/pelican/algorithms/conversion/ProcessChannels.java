package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.*;

/**
 * This class realizes a processing on all bands of a multiband image and return
 * single band image
 *
 *	MASK MANAGEMENT (by Régis) : absent pixels don't count in the min/max/sum/mean calculations.
 * 
 * @author Lefevre
 * 
 */

public class ProcessChannels extends Algorithm {

	/**
	 * Input parameter
	 */
	public Image input;

	/**
	 * The operation to be computed
	 */
	public int op;

	public static final int MAXIMUM = 0;
	public static final int MINIMUM = 1;
	public static final int SUM = 2;
	public static final int AVERAGE = 3;
	public static final int NORM = 4;

	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Constructor
	 */
	public ProcessChannels() {
		super.inputs = "input,op";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();
		if (bdim < 1)
			throw new AlgorithmException("The input must be a multiband image");

		output = input.newInstance(xdim, ydim, zdim, tdim, 1);
		output.setColor(false);
		if (input instanceof DoubleImage) {
			double val;
			for (int t = 0; t < tdim; t++)
				for (int z = 0; z < zdim; z++)
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++) {
							switch (op) {
							case MAXIMUM:
								val = Double.NEGATIVE_INFINITY;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									if (input.getPixelDouble(x, y, z, t, b) > val)
										val = input.getPixelDouble(x, y, z, t, b);
								output.setPixelDouble(x, y, z, t, 0, val);
								break;
							case MINIMUM:
								val = Double.POSITIVE_INFINITY;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									if (input.getPixelDouble(x, y, z, t, b) < val)
										val = input.getPixelDouble(x, y, z, t, b);
								output.setPixelDouble(x, y, z, t, 0, val);
								break;
							case SUM:
								val = 0;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									val += input.getPixelDouble(x, y, z, t, b);
								output.setPixelDouble(x, y, z, t, 0, val);
								break;
							case AVERAGE:
								val = 0;
								int n = 0;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) ) { 
										val += input.getPixelDouble(x, y, z, t, b);
										n++;
									}
								if ( n == 0 ) output.setPixelDouble( x,y,z,t,0, val );
								else output.setPixelDouble( x, y, z, t, 0, 
										(double)((double)val /(double)n) );
								break;
							case NORM:
								val = 0;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) ) { 
										double v=input.getPixelDouble(x, y, z, t, b);
										val += v*v;
									}
								output.setPixelDouble( x, y, z, t, 0, 
										Math.sqrt(val) );
								break;
							}
						}
		}
		if (input instanceof IntegerImage) {
			int val;
			for (int t = 0; t < tdim; t++)
				for (int z = 0; z < zdim; z++)
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++) {
							switch (op) {
							case MAXIMUM:
								val = Integer.MIN_VALUE;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									if (input.getPixelInt(x, y, z, t, b) > val)
										val = input.getPixelInt(x, y, z, t, b);
								output.setPixelInt(x, y, z, t, 0, val);
								break;
							case MINIMUM:
								val = Integer.MAX_VALUE;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									if (input.getPixelInt(x, y, z, t, b) < val)
										val = input.getPixelInt(x, y, z, t, b);
								output.setPixelInt(x, y, z, t, 0, val);
								break;
							case SUM:
								val = 0;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									val += input.getPixelInt(x, y, z, t, b);
								output.setPixelInt(x, y, z, t, 0, val);
								break;
							case AVERAGE:
								val = 0;
								int n = 0;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) ) {

										val += input.getPixelInt(x, y, z, t, b);
										n++;
									}
								if ( n == 0 ) output.setPixelInt( x,y,z,t,0, val );
								else output.setPixelInt(x, y, z, t, 0, 
										(int)((double)val /(double)n) );
								break;
							}
						}
		}
		if (input instanceof ByteImage) {
			int val;
			for (int t = 0; t < tdim; t++)
				for (int z = 0; z < zdim; z++)
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++) {
							switch (op) {
							case MAXIMUM:
								val = 0;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									if (input.getPixelByte(x, y, z, t, b) > val)
										val = input.getPixelByte(x, y, z, t, b);
								output.setPixelByte(x, y, z, t, 0, val);
								break;
							case MINIMUM:
								val = 255;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									if (input.getPixelByte(x, y, z, t, b) < val)
										val = input.getPixelByte(x, y, z, t, b);
								output.setPixelByte(x, y, z, t, 0, val);
								break;
							case SUM:
								val = 0;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									val += input.getPixelByte(x, y, z, t, b);
								output.setPixelByte(x, y, z, t, 0, val);
								break;
							case AVERAGE:
								val = 0;
								int n = 0;
								for (int b = 0; b < bdim; b++)
									if ( input.isPresent( x,y,z,t,b ) ) { 

										val += input.getPixelByte(x, y, z, t, b);
										n++;
									}
//								output.setPixelByte( x,y,z,t,0, val/bdim );
								if ( n == 0 ) output.setPixelByte( x,y,z,t,0, val ); 
								else output.setPixelByte(x, y, z, t, 0, 
										(byte)((double)val /(double)n) );
								break;
							}
						}
		}
		if (input instanceof BooleanImage) {
			boolean val;
			for (int t = 0; t < tdim; t++)
				for (int z = 0; z < zdim; z++)
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++) {
							switch (op) {
							case MAXIMUM:
								val = false;
								for (int b = 0; b < bdim && !val ; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									if (input.getPixelBoolean(x, y, z, t, b))
										val = true;
								output.setPixelBoolean(x, y, z, t, 0, val);
								break;
							case MINIMUM:
								val = true;
								for (int b = 0; b < bdim && val ; b++)
									if ( input.isPresent( x,y,z,t,b ) )
									if (!input.getPixelBoolean(x, y, z, t, b)) 
										val = false;
								output.setPixelBoolean(x, y, z, t, 0, val);
								break;
							case SUM:
								System.err
									.println("ProcessChannels: SUM with BooleanImage not available");
								break;
							case AVERAGE:
								int sum = 0;
								int n = 0;
								for (int b = 0; b < bdim; b++)

									if ( input.isPresent( x,y,z,t,b ) )
									if (input.getPixelBoolean(x, y, z, t, b)) { 
										sum++;
										n++;
									}
//								output.setPixelBoolean( x,y,z,t,0, sum > (bdim/2.0) );
								output.setPixelBoolean( x,y,z,t,0, 
										(double)sum > (double)((double)n/2.0) );
								break;
							}
						}
		}
	}

	/**
	 * Apply a processing on all bands of a multiband image and return a single
	 * band image
	 * 
	 * @param image
	 *          The multiband image
	 * @param op
	 *          The operation to be applied
	 * @return The graylevel image.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T input, int op) {
		return (T) new ProcessChannels().process(input, op);
	}

}