package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * This class performs 2D image cropping (i.e. reducing the size of the image) automatically by removing null pixels
 * 
 * @deprecated This does not do the given job: do not use it or debug it.
 * 
 * @author Witz
 */
public class AutomaticCrop2D extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/** 
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 */
	public AutomaticCrop2D() {
		super.inputs = "input";
		super.outputs = "output";
	}

	/**
	 * Performs 2D image cropping (i.e. reducing the size of the image)
	 * @param input The input image
	 * @param p1 The top left point
	 * @param p2 The bottom right point
	 * @return The output image
	 */
	public static Image exec (Image input) {
		return (Image) new AutomaticCrop2D().process(input);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int top, down, left, right;
		top = 0;
		down = input.getYDim()-1;
		left = input.getXDim()-1;
		right = 0;
		for( int b = 0 ; b < 1 ; b++ )
			for( int t = 0 ; t < input.getTDim() ; t++ )
				for( int z = 0 ; z < input.getZDim() ; z++ )
					for( int x = 0 ; x < input.getXDim() ; x++ )
						for( int y = 0 ; y < input.getYDim() ; y++ )
							if ( input.getPixelXYZTBBoolean(x,y,z,t,b) )
							{
								if ( y > top )    top = y;
								if ( y < down )  down = y;
								if ( x < left )	 left = x;
								if ( x > right ) right = x;
							}
		if ( top > down && left < right )
		{
			output = ( Image ) new BooleanImage(	right-left+1,
														top-down+1,
														input.getZDim(),
														input.getTDim(),
														input.getBDim() );
			for( int b = 0 ; b < input.getBDim() ; b++ )
				for( int t = 0 ; t < input.getTDim() ; t++ )
					for( int z = 0 ; z < input.getZDim() ; z++ )
						for( int x = left ; x <= right ; x++ )
							for( int y = down ; y <= top ; y++ )
								output.setPixelXYZTBBoolean(x-left,y-down,z,t,b,
											input.getPixelXYZTBBoolean(x,y,z,t,b) );
		}
		else
			output = input.copyImage(true);
	}
}
