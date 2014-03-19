package fr.unistra.pelican.algorithms.descriptors.texture;


import java.awt.Point;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;
import fr.unistra.pelican.util.data.DoubleArrayData;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 *	Normalized covariance curve on 4 directions (0,45,90,135) and multiple sizes.
 * 
 *	@author Erchan Aptoula
 *	@author Régis Witz (mask support and framework adaptation)
 */
public class SizeDistanceCovariance extends Descriptor { 

	public int length = 15;
	public int size = 4;

	/**	Input image. */
	public Image input;

	/**	Output covariance curve. */
	public DoubleArrayData output;

	/**	Constructor */
	public SizeDistanceCovariance() { 

		super();
		super.inputs = "input";
		super.options = "length,size";
		super.outputs = "output";
	}

	public static DoubleArrayData exec( Image input ) { 
		return ( DoubleArrayData ) new SizeDistanceCovariance().process( input );
	}

	/**	Computes normalised covariance curve on 4 directions (0,45,90,135)
	 *	@param input The input image
	 *	@param length The length of the covariance curve for each orientation
	 *	@return The output covariance curve
	 */
	public static DoubleArrayData exec( Image input, int length ) { 
		return ( DoubleArrayData ) new SizeDistanceCovariance().process( input, length );
	}

	public static DoubleArrayData exec( Image input, int length, int size ) { 
		return ( DoubleArrayData ) new SizeDistanceCovariance().process( input,length,size );
	}


	/**	@see fr.unistra.pelican.Algorithm#launch() */
	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException
	{
		int featureLength = length * 4 * input.getBDim();
			
		double[][] curve = new double[featureLength][size+1];
		int sizeTotal = featureLength * (size+1);
		Double[] values = new Double[ sizeTotal ];
		for ( int i = 0 ; i < sizeTotal ; i++ ) values[i] = new Double(0);
			
		double[] originalVolumes = new double[input.getBDim()];
			
		for(int b = 0; b < input.getBDim(); b++)
			originalVolumes[b] = volume(input,b);

		BooleanImage magnifier = FlatStructuringElement2D.createCrossFlatStructuringElement(1);
		
		// every length
		for(int i = 1; i <= length; i++){
			int side = i * 2 + 1;

			// every size
			for(int j = 0; j <= size; j++){	
				
				// vertical line
				int middle = (side + 2 * j)/2;
				Image se = new BooleanImage( side + 2*j, side + 2*j, 1,1,1 );
				se.setCenter( new Point( middle,middle ) );
				se.setPixelBoolean(middle,j,0,0,0,true);
				se.setPixelBoolean(middle,side + 2 * j - 1 - j,0,0,0,true);
					
				for(int k = 0; k < j; k++) se = GrayDilation.exec(se,magnifier);
				
				Image tmp = GrayErosion.exec( input,(BooleanImage)se );
				
				for(int b = 0; b < input.getBDim(); b++)
					curve[ b*4*length + i-1 ][j] = volume(tmp,b) / originalVolumes[b];
				
				// left diagonal line
				se = new BooleanImage( side + 2*j, side + 2*j, 1,1,1 );
				se.setCenter( new Point( middle,middle ) );
				se.setPixelBoolean(j,j,0,0,0,true);
				se.setPixelBoolean(side + 2 * j - 1 - j,side + 2 * j - 1 - j,0,0,0,true);

				for(int k = 0; k < j; k++) se = GrayDilation.exec(se,magnifier);
					
				tmp = GrayErosion.exec(input,(BooleanImage)se);
				
				for(int b = 0; b < input.getBDim(); b++)
					curve[ b*4*length + length + i-1 ][j] = volume(tmp,b) / originalVolumes[b];
				
				// horizontal line
				se = new BooleanImage( side + 2*j, side + 2*j, 1,1,1 );
				se.setCenter( new Point( middle,middle ) );
				se.setPixelBoolean(j,middle,0,0,0,true);
				se.setPixelBoolean(side + 2 * j - 1 - j,middle,0,0,0,true);

				for(int k = 0; k < j; k++) se = GrayDilation.exec(se,magnifier);
					
				tmp = GrayErosion.exec(input,(BooleanImage)se);
				
				for(int b = 0; b < input.getBDim(); b++)
					curve[ b*4*length + 2*length + i-1 ][j] = volume(tmp,b) / originalVolumes[b];
				
				// right diagonal line
				se = new BooleanImage( side + 2*j, side + 2*j, 1,1,1 );
				se.setCenter( new Point( middle,middle ) );
				se.setPixelBoolean(j,side + 2 * j - 1 - j,0,0,0,true);
				se.setPixelBoolean(side + 2 * j - 1 - j,j,0,0,0,true);

				for(int k = 0; k < j; k++) se = GrayDilation.exec(se,magnifier);
					
				tmp = GrayErosion.exec(input,(BooleanImage)se);
				
				for(int b = 0; b < input.getBDim(); b++)
					curve[ b*4*length + 3*length + i-1 ][j] = volume(tmp,b) / originalVolumes[b];
			}
		}
		
		// transform into a monodimensional array
		int index = 0;
		
		for(int i = 0; i < curve.length; i++)
			for(int j = 0; j < curve[0].length; j++)
				values[index++] = curve[i][j];

		this.output = new DoubleArrayData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		this.output.setValues( values );
	}

	private double volume(Image img, int channel)
	{
		double d = 0.0;
		for ( int x = 0 ; x < img.getXDim() ; x++ )
			for ( int y = 0 ; y < img.getYDim() ; y++ )
				if ( img.isPresentXYB( x,y,channel ) ) d += img.getPixelXYBDouble( x,y,channel );
		return d;
	}

//	public static double distance( Data d1, Data d2 ) { 
//
//		Double[] v1 = (Double[]) d1.getValues();
//		Double[] v2 = (Double[]) d2.getValues();
//		return Tools.euclideanDistance( v1,v2 );
//	}


	
}
