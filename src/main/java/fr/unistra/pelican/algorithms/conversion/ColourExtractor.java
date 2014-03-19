package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.BooleanMask;


/**
 * Given a colour image, it extracts a given colour with integer precision,
 * resulting into a binary monochannel image.
 *
 *	MASK MANAGEMENT (by Régis) : 
 *	- pixels absent in input are considered of a different color than (c1,c2,c3).
 *	- output image mask is 1 band large and is the input image bands AND.
 *
 * @author Erchan Aptoula
 * 
 */
public class ColourExtractor extends Algorithm {

	/**
	 * First Input parameter.
	 */
	public Image input;

	/**
	 * Second Input parameter.
	 */
	public int c1;

	/**
	 * Third Input parameter.
	 */
	public int c2;

	/**
	 * Fourth Input parameter.
	 */
	public int c3;

	/**
	 * Output parameter.
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public ColourExtractor() {

		super();
		super.inputs = "input,c1,c2,c3";
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

		if (bdim != 3)
			throw new AlgorithmException(
					"The input must be a tristumulus RGB image");

		output = new BooleanImage(xdim, ydim, zdim, tdim, 1);
		output.setColor(true);

		boolean flag;

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						int p1 = input.getPixelXYZTBByte(x, y, z, t, 0);
						int p2 = input.getPixelXYZTBByte(x, y, z, t, 1);
						int p3 = input.getPixelXYZTBByte(x, y, z, t, 2);

						if ( p1 == c1 && p2 == c2 && p3 == c3 ) flag = true;
						else flag = false;

						flag = flag	&& input.isPresentXYZTB( x,y,z,t, 0 )
									&& input.isPresentXYZTB( x,y,z,t, 1 )
									&& input.isPresentXYZTB( x,y,z,t, 2 );

						output.setPixelXYZTBoolean(x, y, z, t, flag);

					}
				}
			}
		}

		BooleanImage mask = new BooleanImage( xdim, ydim, zdim, tdim, 1 );
		for ( int x = 0 ; x < xdim ; x++ ) 
			for ( int y = 0 ; y < ydim ; y++ ) 
				for ( int z = 0 ; z < zdim ; z++ ) 
					for ( int t = 0 ; t < tdim ; t++ ) 
						mask.setPixelXYZTBoolean( x,y,z,t, 
											   input.isPresentXYZTB( x,y,z,t, 0 )
											&& input.isPresentXYZTB( x,y,z,t, 1 )
											&& input.isPresentXYZTB( x,y,z,t, 2 ) );
		output.pushMask( new BooleanMask( mask ) );

	}

	/**
	 * Given a colour image, it extracts a given colour with integer precision,
	 * resulting into a binary monochannel image.
	 * 
	 * @param input
	 *            Colour image
	 * @param c1
	 *            Color 1
	 * @param c2
	 *            Color 2
	 * @param c3
	 *            Color 3
	 * @return The resulting RGB image
	 */
	public static Image exec(Image input, int c1, int c2, int c3) {
		return (Image) new ColourExtractor().process(input, c1, c2, c3);
	}

//	public static void main(String[] args) {
//		Image img = (Image) new ImageLoader()
//				.process("samples/Corel1000/building/200.jpg");
//		new Viewer2D().process(img, "giris");
//		img = (Image) new ColourExtractor().process(img, 73, 106, 125);
//		new Viewer2D().process(img, "sonuc");
//
//	}
}