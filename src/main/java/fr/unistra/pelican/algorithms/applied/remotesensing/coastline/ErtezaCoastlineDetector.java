package fr.unistra.pelican.algorithms.applied.remotesensing.coastline;


import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.Equalization;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.OtsuThresholding;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


/** This class is a possible implementation of Erteza's algorithm for detection of coastline in the paper "An automatic Coastline
 * Detector for Use with SAR Images".
 * 
 * @author Dany DAMAJ, Jonathan WEBER
 */
public class ErtezaCoastlineDetector extends Algorithm
{
	/**
	 * Image to be processed
	 */
	public Image inputImage;
	
	/**
	 * land position in the satellite picture
	 */
	public Integer orient;
	

	/**
	 * Binarisation mode
	 */
	public Integer binarisationmode;
	
	/**
	 * threshold for binarisation (if needed)
	 */
	public Integer thresh; //if binarisationmode == other
	
	/**
	 * band to be computed
	 */
	public Integer band;
	
	/**
	 * Resulting picture
	 */
	public Image outputImage;
	
	/**
	 * Constant for mean binarisation method
	 */
	public static final int MEAN= 0;
	/**
	 * Constant for otsu binarisation method
	 */
	public static final int OTSU = 1;
	/**
	 * Constant for manual binarisation method
	 */
	public static final int OTHER = 2;
	
	private int DIR;
	

	
	/**
	 * Constant for bottom land position
	 */
	public static final int LAND_IS_BOTTOM = 0;
	/**
	 * Constant for right land position
	 */
	public static final int LAND_IS_RIGHT = 1;
	/**
	 * Constant for left land position
	 */
	public static final int LAND_IS_LEFT = 2;
	/**
	 * Constant for top land position
	 */
	public static final int LAND_IS_TOP = 3;
	
	// "conventions" in the paper
	private static final int IL = 0;
	private static final int IR = 180;
	private static final int IT = 270;
	private static final int IB = 90;
	
	private static final int CW = 1;
	private static final int CCW = -1;
	
	
	/**
  	 * Constructor
  	 *
  	 */
	public ErtezaCoastlineDetector() {		
		
		super();		
		super.inputs = "inputImage,orient,binarisationmode,thresh,band";		
		super.outputs = "outputImage";		
		
	}
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException
	{ 
		long ti = System.currentTimeMillis();
		Image tmpImage;
		Image tmpOutputImage;
		boolean rotation = false;
		
		
		tmpImage = inputImage.getImage4D(band, Image.B);
		// 1st step : apply 2 3*3 median filters
		tmpImage = (Image) new GrayMedian().process(tmpImage,FlatStructuringElement2D
				.createSquareFlatStructuringElement(3));
		//tmpImage = MedianFilter.process(tmpImage,3);
		
		// 2nd step : histogram equalization
		tmpImage = (Image) new Equalization().process(tmpImage);
		
		// 3rd step : 200-thresholding with SAR images, but it's a value that have to be modified in our case
		//tmpImage = ThresholdBinarisation.process(tmpImage,80.0/256.0);
		if(binarisationmode == MEAN)
		{
			int compteur = 0;
			double valeur_moyenne = 0;

			for(int t = 0; t < tmpImage.getTDim(); t++){
				for(int z = 0; z < tmpImage.getZDim(); z++){
					for(int y = 1; y < tmpImage.getYDim() ; y++){
						for(int x = 1; x < tmpImage.getXDim() ; x++)
						{
							valeur_moyenne +=  tmpImage.getPixelXYZTBDouble(x,y,z,t,0);
							compteur++;
						}
					}
				}
			}
			
			valeur_moyenne /= (double) compteur;
			
			tmpImage = (Image) new ManualThresholding().process(tmpImage,valeur_moyenne);
		}
		else if (binarisationmode == OTSU)
			tmpImage = (Image) new OtsuThresholding().process(tmpImage);
		else
			tmpImage = (Image) new ManualThresholding().process(tmpImage, thresh);
		
		BooleanImage se = new BooleanImage(7,7,1,1,1);
		se.fill(true);
		
		// 4th step : apply 2 7*7 dilation
		tmpImage = (Image) new BinaryDilation().process(tmpImage, se);
		tmpImage = (Image) new BinaryDilation().process(tmpImage, se);
		
		long tf = System.currentTimeMillis();
		System.out.println("temps d'execution : "+(tf-ti));
		
		// in our case, LAND may be on the top or on the bottom of the image : we need to 
		// do a 90 rotation in these cases
		if( orient == LAND_IS_BOTTOM || orient == LAND_IS_TOP)
		{
			tmpOutputImage = new BooleanImage(tmpImage.getYDim() , tmpImage.getXDim() , tmpImage.getZDim() , tmpImage.getTDim() , tmpImage.getBDim());
			for(int b = 0; b <  tmpImage.getBDim(); b++)
			{
				for(int t = 0; t < tmpImage.getTDim() ; t++)
				{
					for(int z = 0; z < tmpImage.getZDim(); z++)
					{
						for(int y = 0; y < tmpImage.getYDim(); y++)
						{
							for(int x = 0; x < tmpImage.getXDim(); x++)
							{
								tmpOutputImage.setPixelXYZTBBoolean(y , tmpImage.getXDim()-1-x , z , t , b , tmpImage.getPixelXYZTBBoolean(x, y, z, t, b));
							}
						}
					}
				}
			}
			
			rotation = true;
			
			if(orient == LAND_IS_BOTTOM)
				orient = LAND_IS_RIGHT;
			else orient = LAND_IS_LEFT;
			
		}
		else
		{
			tmpOutputImage = tmpImage.copyImage(true);
		}
		
		// clockwise if the land is on the right, else counterclockwise
		if(orient == LAND_IS_RIGHT)
			DIR = CW;
		else DIR = CCW;
		
		// now we apply the contour following algorithm :
		// starting from the bottom of the image, we search the unique land (WHITE) pixel which touches the sea 
		// AND we have to do that for every band
		for(int bands = 0 ; bands < tmpOutputImage.getBDim() ; bands++)
		{
			int i,j;

			if( orient == LAND_IS_RIGHT)
			{
				// let's start at the bottom left
				j = 0;
				i = tmpOutputImage.getYDim() - 1;
				while(j < tmpOutputImage.getXDim() && tmpOutputImage.getPixelDouble(j, i, 0, 0,bands) < 0.5 ) //sea
				{
					j++;
				}
			}
			else
			{
				// let's start at the bottom right
				j = tmpOutputImage.getXDim() - 1;
				i = tmpOutputImage.getYDim() - 1;
				while( j >= 0&& tmpOutputImage.getPixelDouble(j, i, 0, 0, bands) < 0.5 ) // sea
					j --;
			}
			
			// initialisation
			Point next_pixel = new Point(i , j - DIR);
			Point p0 = new Point(next_pixel);
			int next_orientation = 90 + 90*DIR;
			
			// and for the result
			Vector <Point> resulting_points = new Vector<Point>();
			
			boolean bool;
			// "recursivity"
			int cpt = 0;
			while( (next_pixel.x >= 0 )  &&  (next_pixel.x <  tmpOutputImage.getYDim() ) &&  (next_pixel.y >= 0 ) &&  (next_pixel.y <  tmpOutputImage.getXDim() ))
			{
				// a small test to prevent infinite recursivity
				if(p0.equals(next_pixel) && cpt > 0)
					break;
				cpt++;
				
				if( (bool=tmpOutputImage.getPixelBoolean(next_pixel.y, next_pixel.x, 0, 0, bands)) )
				{
					resulting_points.add(new Point(next_pixel));
				}
				
				switch(next_orientation)
				{
					case 0 : 
						if( bool ) //LAND
						{
							next_pixel = new Point(next_pixel.x - DIR,next_pixel.y);
							next_orientation = 0 + 90 * DIR;
						}
						else
						{
							next_pixel = new Point(next_pixel.x + DIR,next_pixel.y);
							next_orientation = 0 - 90 * DIR;
						}
						break;
						
					case 90 : 
						if( bool ) //LAND
						{
							next_pixel = new Point(next_pixel.x,next_pixel.y - DIR);
							next_orientation = 90 + 90 * DIR;
						}
						else
						{
							next_pixel = new Point(next_pixel.x,next_pixel.y + DIR);
							next_orientation = 90 - 90 * DIR;
						}
						break;
						
					case 180 : 
						if( bool ) //LAND
						{
							next_pixel = new Point(next_pixel.x + DIR,next_pixel.y);
							next_orientation = 180 + 90 * DIR;
						}
						else
						{
							next_pixel = new Point(next_pixel.x - DIR,next_pixel.y);
							next_orientation = 180 - 90 * DIR;
						}
						break;
						
					case 270 : 
						if( bool ) //LAND
						{
							next_pixel = new Point(next_pixel.x,next_pixel.y + DIR);
							next_orientation = 270 + 90 * DIR;
						}
						else
						{
							next_pixel = new Point(next_pixel.x,next_pixel.y - DIR);
							next_orientation = 270 - 90 * DIR;
						}
						break;
				}
				next_orientation = (next_orientation + 360) % 360;
			}
			
			// printing the results
			for(int t = 0; t < tmpOutputImage.getTDim() ; t++)
			{
				for(int z = 0; z < tmpOutputImage.getZDim(); z++)
				{
					for(int y = 0; y < tmpOutputImage.getYDim(); y++)
					{
						for(int x = 0; x < tmpOutputImage.getXDim(); x++)
						{
							tmpOutputImage.setPixelXYZTBDouble(x , y , z , t , bands , 0.0);
						}
					}
				}
			}
			for(int k = 0 ; k < resulting_points.size(); k++)
				tmpOutputImage.setPixelDouble(resulting_points.elementAt(k).y , resulting_points.elementAt(k).x , 0 , 0 , bands , 1.0);
		}
		
		if(rotation)
		{
			outputImage = new BooleanImage(tmpOutputImage.getYDim() , tmpOutputImage.getXDim() , tmpOutputImage.getZDim() , tmpOutputImage.getTDim() , tmpOutputImage.getBDim());
			for(int b = 0; b <  tmpOutputImage.getBDim(); b++)
			{
				for(int t = 0; t < tmpOutputImage.getTDim() ; t++)
				{
					for(int z = 0; z < tmpOutputImage.getZDim(); z++)
					{
						for(int y = 0; y < tmpOutputImage.getYDim(); y++)
						{
							for(int x = 0; x < tmpOutputImage.getXDim(); x++)
							{
								outputImage.setPixelXYZTBBoolean(tmpOutputImage.getYDim()-1-y ,/*tmpOutputImage.getXDim()-1-*/x , z , t , b , tmpOutputImage.getPixelXYZTBBoolean(x, y, z, t, b));
							}
						}
					}
				}
			}
		}
		else
		{
			outputImage = tmpOutputImage.copyImage(true);
		}
	}

	/**
	 * Method which applies Erteza's algorithm for coastline detection in SAR images.
	 * @param inputImage Satellite picture
	 * @param orient Land position in the picture
	 * @param binarisationmode Binarisation mode
	 * @param thresh Threshold for binarisation (if needed)
	 * @param band Band to be computed
	 * @return Image with detected coastline
	 */
	public static Image exec(Image inputImage,Integer orient, Integer binarisationmode,Integer thresh,Integer band)
	{
		return (Image) new ErtezaCoastlineDetector().process(inputImage,orient,binarisationmode,thresh,band);
	}
}
