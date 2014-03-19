package fr.unistra.pelican.algorithms.segmentation.labels;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.arithmetic.DeleteSmallValues;
import fr.unistra.pelican.algorithms.arithmetic.EuclideanNorm;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.segmentation.Watershed;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Draw a frontier (in white) over an image. The frontiers image is a one band
 * image and input image can have more than one bands.
 * 
 *	@author Derivaux, Lefevre, Régis Witz (management of colour option).
 */
public class DrawFrontiersOnImage extends Algorithm {

	/**	Input Image. */
	public Image inputImage;

	/**	Border image (BooleanImage). */
	public BooleanImage frontiers;

	/**	Resulting image. */
	public Image outputImage;

	/**	Optional contour coloring colour value. */
	public double[] colour = null;

	/**	Constructor. */
	public DrawFrontiersOnImage() { 

		super.inputs = "inputImage,frontiers";
		super.options = "colour";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException { 

		this.outputImage = this.inputImage.copyImage( false );
		if ( this.colour == null ) { 

			// we need to initialize the colour field
			int bdim = this.inputImage.getBDim();
			this.colour = new double[ bdim ];
			for ( int b = 0 ; b < bdim ; b++ ) this.colour[b] = new Double( 1.0 );
		}

		int xDim = outputImage.getXDim();
		int yDim = outputImage.getYDim();
		int zDim = outputImage.getZDim();
		int tDim = outputImage.getTDim();
		int bDim = outputImage.getBDim();
		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int y = 0; y < yDim; y++)
					for (int x = 0; x < xDim; x++)
						for ( int b = 0; b < bDim; b++ ) { 
							if ( frontiers.getPixelXYZTBoolean( x,y,z,t ) )
								outputImage.setPixelDouble( x,y,z,t,b, this.colour[b] );
							else
								outputImage.setPixelDouble( x,y,z,t,b, 
										inputImage.getPixelDouble( x,y,z,t,b) );
						}
	}

	public static void main(String[] args) {
		String file = "samples/remotesensing1.png";
		if (args.length > 0)
			file = args[0];

		BooleanImage se3 = FlatStructuringElement2D
			.createSquareFlatStructuringElement(3);

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);

			// Compute the gradient on each band
			Image work = (Image) new GrayGradient().process(source, se3);

			// Compute the euclidian distance of the gradient
			work = (Image) new EuclideanNorm().process(work);

			work = (Image) new DeleteSmallValues().process(work, 0.2);

			// Process a watershed transformation
			work = (Image) new Watershed().process(work);

			Image frontiers = (Image) new FrontiersFromSegmentation()
				.process(new DeleteFrontiers().process(work));

			// View it
			new Viewer2D().process(new DrawFrontiersOnImage().process(source,
				frontiers), "Frontiers of " + file);

		} catch (InvalidTypeOfParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*	Draw some frontiers on an image. */
	public static Image exec(Image image, BooleanImage frontiers) { 
		return (Image) new DrawFrontiersOnImage().process( image, frontiers );
	}

	/*	Draw some frontiers on an image with an specified colour. */
	public static Image exec( Image image, BooleanImage frontiers, double[] colour ) { 
		return (Image) new DrawFrontiersOnImage().process( image, frontiers, colour );
	}

}
