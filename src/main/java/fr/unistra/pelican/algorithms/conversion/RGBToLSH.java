package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.*;



/**
 *	RGB to polar representation using the L1 norm for both brightness and saturation.
 *	Using the "simplified" hue transformation.
 * 
 *	@author Erchan Abdullah, Régis Witz, Jonathan Weber
 */
public class RGBToLSH extends Algorithm {

	/**	Input RGB image. */
	public Image input;

	/**	Output LSH image. */
	public Image output;

	public boolean scaleToByte=false;
	
	/**	Constructor. */
	public RGBToLSH() {

		super();
		super.inputs = "input";
		super.options = "scaleToByte";
		super.outputs = "output";
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	public void launch() throws AlgorithmException { 
		int size = this.input.size();

		if ( this.input.getBDim() != 3 ) 
			throw new AlgorithmException( "The input must be a tristumulus RGB image" );

		this.output = this.input.newDoubleImage();
		this.output.setMask( this.input.getMask() );
		this.output.setColor(true);
		
		for(int i=0;i<size;i=i+3)
		{
			double R = this.input.getPixelDouble( i );
			double G = this.input.getPixelDouble( i+1 );
			double B = this.input.getPixelDouble( i+2 );
			double[] lsh = convert( R,G,B );
			this.output.setPixelDouble( i, lsh[0] );
			this.output.setPixelDouble( i+1, lsh[1] );
			this.output.setPixelDouble( i+2, lsh[2] );
		}
		if (scaleToByte)
			output=scaleToByte(output);
	}

	/**	Converts a triplet of RGB in [0,255] into LSH
	 *	@param r Red channel value.
	 *	@param g Green channel value.
	 *	@param b Blue channel value.
	 *	@return Array of { L,S,H } values.
	 */
	private static double[] convert( double r, double g, double b ) { 

		double[] lsh = new double[3];
		lsh[0] = lsh[1] = lsh[2] = 0.0;
		double max = 0.0, med = 0.0, min = 0.0;
		
		if( r >= g && r >= b ) { 

			max = r;
			if ( g >= b ) { 

				med = g;
				min = b;
			} else { 

				med = b;
				min = g;
			}
		} else 
		if( g >= r && g >= b ) { 

			max = g;
			if( r >= b ) { 

				med = r;
				min = b;
			} else { 

				med = b;
				min = r;
			}
		} else 
		if( b >= r && b >= g ) { 

			max = b;
			if( r >= g ) { 

				med = r;
				min = g;
			} else { 

				med = g;
				min = r;
			}
		} // fi

		// luminance
		lsh[0] = ( max + med + min ) / 3.0;

		// saturation
		if( lsh[0] >= med ) 
			 lsh[1] = 1.5 * ( max - lsh[0] );
		else lsh[1] = 1.5 * ( lsh[0] - min );

		// hue
		double k = 1.0/6.0;
		int lambda = 0;
			 if( r > g  && g >= b ) lambda = 0;
		else if( g >= r && r > b  ) lambda = 1;
		else if( g > b  && b >= r ) lambda = 2;
		else if( b >= g && g > r  ) lambda = 3;
		else if( b > r  && r >= g ) lambda = 4;
		else if( r >= b && b > g  ) lambda = 5;

		if ( lsh[1] > 0.0 ) 
			 lsh[2] = k * ( lambda + 0.5 - Math.pow(-1,lambda) * ( max+min-2*med ) / ( 2*lsh[1] ) );
		else lsh[2] = 0;

		if( lsh[2] < 0 ) lsh[2] = 0;

		return lsh;
	}
	
	/**
	 * Scales each band of the resulting LSH image according to the value
	 * intervals L,S,H in [0,1] and returns a valid byteImage
	 * 
	 * @return resulting ByteImage
	 */
	private static Image scaleToByte(Image lsh) {
		ByteImage bimg = lsh.newByteImage();
		int size = bimg.size();

		for(int i=0;i<size;i=i+3)
		{
			double d = lsh.getPixelDouble(i);
			// L
			bimg.setPixelByte(i, (int) Math.round(d * 255));

			// S
			d = lsh.getPixelDouble(i+1);
			bimg.setPixelByte(i+1, (int) Math.round(d * 255));

			// H
			d = lsh.getPixelDouble(i+2);
			bimg.setPixelByte(i+2, (int) Math.round(d * 255));
		}
		return bimg;
	}

	/**	Realizes the transformation of a tristumulus RGB image into a double valued LSH image 
	 *	with pixels in the interval [0,1]. Thus it is adequate also for visualisation.
	 *
	 *	@param input Tristumulus RGB image.
	 *	@return Double valued LSH image.
	 */
	public static Image exec( Image input ) { 
		return ( Image ) new RGBToLSH().process(input);
	}
	
	/**	Realizes the transformation of a tristumulus RGB image into a double valued LSH image 
	 *	with pixels in the interval [0,1]. Thus it is adequate also for visualisation.
	 *
	 *	@param input Tristumulus RGB image.
	 *	@param scaleToByte Scale result to byteImage
	 *	@return Double valued LSH image.
	 */
	public static Image exec( Image input, boolean scaleToByte ) { 
		return ( Image ) new RGBToLSH().process(input,scaleToByte);
	}
	
	public static void main( String[] args ) { 

		Image img = fr.unistra.pelican.algorithms.io.ImageLoader.exec( "samples/macaws.png" );
		img = RGBToLSH.exec(img);
		img.color = false;
		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec(img);
	}



}
