package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.mask.BooleanMask;

/**
 * This class transform a multiband binary image into a single band label image.
 * 
 * The outputImage format is IntegerImage
 *
 *	MASK MANAGEMENT (by Régis) : output image mask is 1 band large and is the input image bands OR.
 * @author Lefevre
 */
public class BinaryMasksToLabels extends Algorithm {

	/**
	 * The input array of binary images.
	 */
	public BooleanImage inputImage;

	/**
	 * The output label image
	 */
	public Image outputImage;

	/**
	 * Constructor
	 */
	public BinaryMasksToLabels() {
		super.inputs = "inputImage";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

		BooleanImage mask = new BooleanImage(	this.inputImage.getXDim(), 
												this.inputImage.getYDim(), 
												this.inputImage.getZDim(), 
												this.inputImage.getTDim(), 
												1
											  );
		for ( int x = 0 ; x < this.inputImage.getXDim() ; x++ )
			for ( int y = 0 ; y < this.inputImage.getYDim() ; y++ )
				for ( int z = 0 ; z < this.inputImage.getZDim() ; z++ )
					for ( int t = 0 ; t < this.inputImage.getTDim() ; t++ ) { 
						mask.setPixelXYZTBBoolean( x,y,z,t,0, false );
						for (	int b = 0 ; 
								b < this.inputImage.getBDim() &&
								!mask.getPixelXYZTBBoolean( x,y,z,t,0 ); 
								b++ )
							mask.setPixelXYZTBBoolean( x,y,z,t,0,
									this.inputImage.isPresentXYZTB( x,y,z,t,b ) );
					}

		// generate output
		outputImage = new IntegerImage(	inputImage.getXDim(), 
										inputImage.getYDim(), 
										inputImage.getZDim(), 
										inputImage.getTDim(), 
										1 );
		this.outputImage.pushMask( new BooleanMask( mask ) );
		for (int b = 0; b < inputImage.getBDim(); b++)
			for (int t = 0; t < inputImage.getTDim(); t++)
				for (int z = 0; z < inputImage.getZDim(); z++)
					for (int y = 0; y < inputImage.getYDim(); y++)
						for (int x = 0; x < inputImage.getXDim(); x++)
							if (inputImage.getPixelBoolean(x, y, z, t, b))
								outputImage.setPixelInt(x, y, z, t, 0, b + 1);
	}

	/**
	 * Combines several binary images into a single label image
	 * 
	 * @param inputImage
	 *          The input array of binary images
	 * @return The output label image
	 */
	public static IntegerImage exec(BooleanImage inputImage) {
		return (IntegerImage) new BinaryMasksToLabels()
			.process((Object) inputImage);
	}
}
