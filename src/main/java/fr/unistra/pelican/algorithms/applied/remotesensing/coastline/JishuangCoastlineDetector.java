package fr.unistra.pelican.algorithms.applied.remotesensing.coastline;


import java.awt.Point;
import java.util.Stack;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryClosing;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryOpening;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.OtsuThresholding;


/** Application of Jishuang's method for detection of coastlines. More details
 * in the paper : "A multi-threshold based morphological approach for extracting
 * coastal line feature in remote sensed images"
* 
* @author Dany DAMAJ, Jonathan WEBER
*/
public class JishuangCoastlineDetector extends Algorithm{
	
	/**
	 * Image to be processed
	 */
	public Image input;
	/**
	 * Binarisation mode
	 */
	public Integer binarisationmode;
	/**
	 * Threshold for binarisation (if needed)
	 */
	public Integer thresh; //if binarisationmode == other
	/**
	 * Band to be computed
	 */
	public Integer band;


	
	/**
	 * Constant for mean binarisation mode
	 */
	public static final int MEAN= 0;
	/**
	 * Constant for otsu binarisation mode
	 */
	public static final int OTSU = 1;
	/**
	 * Constant for manual binarisation mode
	 */
	public static final int OTHER = 2;
	
	//delta = "tuning" parameter of the mean-thresholding
	private double delta = -0.04;
	
	/**
	 * resulting picture
	 */
	public Image output;
	private Image bInput;
	
	//for the algorithm
	private IntegerImage labelledImage;

	
	/**
  	 * Constructor
  	 *
  	 */
	public JishuangCoastlineDetector() {		
		
		super();		
		super.inputs = "input,binarisationmode,thresh,band";		
		super.outputs = "output";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException{
		int bdim = input.getBDim(); //bandes
		int tdim = input.getTDim(); //temps
		int zdim = input.getZDim();
		int ydim = input.getYDim();
		int xdim = input.getXDim();
		
		bInput = input.getImage4D(band, Image.B);
		output = new BooleanImage(xdim,ydim,zdim,tdim,1);
		
		
		
			
			
			
			
			if(binarisationmode == MEAN)
			{
				int compteur = 0;
				double valeur_moyenne = 0;

				for(int t = 0; t < tdim; t++){
					for(int z = 0; z < zdim; z++){
						for(int y = 1; y < ydim-1 ; y++){
							for(int x = 1; x < xdim-1 ; x++)
							{
								valeur_moyenne +=  bInput.getPixelXYZTBDouble(x,y,z,t,0);
								compteur++;
							}
						}
					}
				}
				
				valeur_moyenne /= (double) compteur;
				
				bInput = (Image) new ManualThresholding().process(bInput,valeur_moyenne + delta);
			}
			else if (binarisationmode == OTSU)
				bInput = (Image) new OtsuThresholding().process(bInput);
			else
				bInput = (Image) new ManualThresholding().process(bInput, thresh);
			
			System.out.println("estimation : 10%");
			
			// Jishuang suppose that after this thresholding, there's a main Land area and a main Sea area.
			// now we have to label the image, using 4-connexity
			labelledImage = new IntegerImage(bInput.getXDim(),bInput.getYDim(), 1, 1, 1);
			labelledImage.fill(-1);
			 
			int label = 0;
			 
			for(int x = 0; x < xdim; x++)
				for(int y = 0; y < ydim; y++) 
					if(labelledImage.getPixelXYInt(x, y) == -1)
						newSegment(x, y, label++);
	
			System.out.println("estimation : 20%");
			
			// labelledImage is now the complete 4-connexity labelled image of the initial image "input"
			// now we must adapt the "RegionSize" algorithm to know the main white area (sea) and the main black area (cont)
			 final int[][] regionSize;
			 
			// Find the number of regions to allocate an array
			int max = 0;
			for (int i = 0; i < labelledImage.size(); i++)
				max = Math.max(labelledImage.getPixelInt(i), max);

			regionSize = new int[max + 1][2];
	
			 int []minimumDistance; //minimum Distance between current area and main sea area
			minimumDistance = new int[max + 1]; 
	
			// Fill this array with the size of each region.
			for (int i = 0; i <= max; i++)
			{
				regionSize[i][0] = 0;
				regionSize[i][1] = 0;
				minimumDistance[i] = Integer.MAX_VALUE;
			}
			
			for(int x = 0; x < xdim; x++)
				for(int y = 0; y < ydim; y++) 
				{
					int labelxy = labelledImage.getPixelXYBInt(x, y, 0);
					regionSize[labelxy][0]++;
					if(bInput.getPixelXYZTBDouble(x, y,0,0, 0) > 0) //blanc
						regionSize[labelxy][1] = 1;
					else
						regionSize[labelxy][1] = 0; //noir
				}
			
			System.out.println("estimation : 30%");
			
			// computation the 2 maximums
			int maxWHITE = 0, maxWHITEvalue = 0 ;
			int maxBLACK = 0 , maxBLACKvalue = 0 ;
			
			for(int i = 0 ; i <= max ; i++)
			{
				if(regionSize[i][1] == 1) //white
					if(regionSize[i][0] >= maxWHITEvalue)
					{
						maxWHITEvalue = regionSize[i][0];
						maxWHITE = i;
					}
				if(regionSize[i][1] == 0) //black
					if(regionSize[i][0] >= maxBLACKvalue)
					{
						maxBLACKvalue = regionSize[i][0];
						maxBLACK = i;
					}
			}
			
			System.out.println("estimation : 40%");
			
			// now, maxWHITE is the label of the main sea area, and maxBLACK the label of the main continental area
			// for each which doesn't belong to maxWHITE or maxBLACK, we must decide if the label of this pixel will
			// finally be maxWHITE or maxBLACK  or undefined
			// WHITE == continental , and BLACK == Sea
			
			boolean belongWHITE[] = new boolean[max + 1];
			boolean belongBLACK[] = new boolean[max + 1];
			for(int i = 0 ; i <= max ; i++)
			{
				belongWHITE[i] = true; // default supposal
				belongBLACK[i] = true;
			}
			
			int thres1 = 2; // minimal distance between pixel and sea (BLACK) to know if pixel belongs to continental (WHITE)
			int thres2 = 2;
			
			//for each pixel
			for(int x = 0; x < xdim; x++)
			{
				for(int y = 0; y < ydim; y++) 
				{
					// which doesn't belong to maxBLACK or maxWHITE
					if( labelledImage.getPixelXYBInt(x, y, 0) != maxBLACK &&  labelledImage.getPixelXYBInt(x, y, 0) != maxWHITE)
					{
						int iinf = Math.max(x - thres1, 0),isup = Math.min(x + thres1 , xdim -1);
						int jinf = Math.max(y - thres1, 0),jsup = Math.min(y + thres1 , ydim -1);
						// consider the area of distances <= thres1 in 4 connexity
						for(int i = iinf ; i <= isup ; i++)
						{
							for(int j = jinf ; j <= jsup ; j++)
							{
								if(Math.abs(i-x)+Math.abs(j-y) <= thres1)
								{
									// if the pixel is mainWHITE, the minimal distance between the label of (x,y) and maxWHITE
									// is NOT > thres1, though the label of (x,y) does NOT belong to the main contiental area
									if( labelledImage.getPixelXYBInt(i, j, 0)  == maxBLACK)
									{
										belongWHITE[ labelledImage.getPixelXYBInt(x, y, 0)] = false;
										
										// update the current minimal distance
										if(Math.abs(i-x)+Math.abs(j-y)  < minimumDistance[ labelledImage.getPixelXYBInt(x, y, 0)] )
											minimumDistance[ labelledImage.getPixelXYBInt(x, y, 0)] = Math.abs(i-x)+Math.abs(j-y);
									}
								}
							}
						}
						
						iinf = Math.max(x - thres2, 0); isup = Math.min(x + thres2 , xdim -1);
						jinf = Math.max(y - thres2, 0); jsup = Math.min(y + thres2 , ydim -1);
						
						//same thing for thres2
						for(int i = iinf ; i <= isup ; i++)
						{
							for(int j = jinf ; j <= jsup ; j++)
							{
								if(Math.abs(i-x)+Math.abs(j-y) <= thres2)
								{
									// if the pixel is mainWHITE, the minimal distance between the label of (x,y) and maxWHITE
									// is NOT > thres1, though the label of (x,y) does NOT belong to the main contiental area
									if( labelledImage.getPixelXYBInt(i, j, 0)  == maxWHITE)
									{
										belongBLACK[ labelledImage.getPixelXYBInt(x, y, 0)] = false;
									}
								}
							}
						}
					}
				}
			}
			
			System.out.println("estimation : 60%");
			
			// now, the last isolated regions are the regions which don't belong to BLACK and don't belong to WHITE
			// Jishuang says : if a such area is "water" (BLACK) then we can connect it to the sea via the minimal path
			
			
			
			// for main sea, main continental, isolated continental regions and isolated sea regions
			for(int y = 0; y < ydim ; y++)
			{
				for(int x = 0; x < xdim ; x++)
				{
					int labelcourant =  labelledImage.getPixelXYBInt(x, y, 0);
					
					if(labelcourant == maxWHITE)
						output.setPixelBoolean(x, y, 0, 0, 0,true); //WHITE
					else if( labelcourant == maxBLACK)
						output.setPixelBoolean(x, y, 0, 0, 0,false);
					else if (belongWHITE[labelcourant])
						output.setPixelBoolean(x, y, 0, 0, 0,true);
					else if(belongBLACK[labelcourant])
						output.setPixelBoolean(x, y, 0, 0, 0,false);
				}
			}
			
			System.out.println("estimation : 70%");
			
			// for along-coastal sea-areas
			for(int y = 0; y < ydim ; y++)
			{
				for(int x = 0; x < xdim ; x++)
				{
					int labelcourant =  labelledImage.getPixelXYBInt(x, y, 0);
	
					 if (!belongBLACK[labelcourant] &&  !belongWHITE[labelcourant])
					{ //then x,y belong to an along-coastal area
	
						if(bInput.getPixelXYZTBDouble(x, y,0,0, 0) == 0) //sea = BLACK
						{ // minimal-path technique
							
							int tmp = minimumDistance[labelcourant];
							int iinf = Math.max(x - tmp, 0),isup = Math.min(x + tmp , xdim -1);
							int jinf = Math.max(y - tmp, 0),jsup = Math.min(y + tmp , ydim -1);
							
							for(int i = iinf ; i <= isup ; i++)
							{
								for(int j = jinf ; j <= jsup ; j++)
								{
									if(Math.abs(i-x)+Math.abs(j-y) == tmp)
									{
										if( labelledImage.getPixelXYBInt(i, j, 0) == maxBLACK )
										{
											int minx = Math.min(i, x), miny = Math.min(j, y);
											int maxx = Math.max(i, x), maxy = Math.max(j, y);

											for(int k = minx ; k <= maxx ; k++)
												for(int l = miny ; l <= maxy ; l++)
												{
													output.setPixelBoolean(k, l, 0, 0, 0,false); //BLACK : main sea
												}
										}
									}
								}
							}
						}
					}			
				}
			}
			
			System.out.println("estimation : 80%");
			
			// end : for the along-coastal continental areas
			Vector <Point> areas[];
			areas = new Vector [max+1];
			int minxareas[],maxxareas[],minyareas[],maxyareas[];
			minxareas = new int[max + 1];
			maxxareas = new int[max + 1];
			minyareas = new int[max + 1];
			maxyareas = new int[max + 1];
			
			for(int i = 0 ; i <= max ; i++)
			{
				areas[i] = new Vector<Point>();
				minxareas[i] = Integer.MAX_VALUE;
				maxxareas[i] = Integer.MIN_VALUE;
				minyareas[i] = Integer.MAX_VALUE;
				maxyareas[i] =  Integer.MIN_VALUE;
			}
			
			for(int y = 0; y < ydim ; y++)
			{
				for(int x = 0; x < xdim ; x++)
				{
					int labelcourant =  labelledImage.getPixelXYBInt(x, y, 0);
	
					 if (!belongBLACK[labelcourant] &&  !belongWHITE[labelcourant])
					{ 	
						areas[labelcourant].add(new Point(x,y));
						
						if( x < minxareas[ labelcourant])
							minxareas[ labelcourant] = x;
						if( y < minyareas[ labelcourant])
							minyareas[ labelcourant] = y;
						if( x > maxxareas[ labelcourant])
							maxxareas[ labelcourant] = x;
						if( y > maxyareas[ labelcourant])
							maxyareas[ labelcourant] = y;
					}
				}
			}
			
			System.out.println("estimation : 90%");
			
			int thresO = 16;
			int thresC = 50000;
			
			BooleanImage se = new BooleanImage(3,3 ,1,1,1);
			se.fill(true);
			
			//boolean affiche = true;
			for(int i = 0 ; i <= max ; i++)
			{
				if(areas[i].size() < thresO && areas[i].size() > 0 && output.getPixelXYZTBDouble(areas[i].firstElement().x,areas[i].firstElement().y,0,0,0)== 0 )
				{ //> 0 means that there is an area, and thresO is the reference to the paper
					Image ii = new BooleanImage(maxxareas[i]-minxareas[i]+9 , maxyareas[i]-minyareas[i]+9 , 1 ,1 ,1);
					ii.fill(0);
					
					for(int j = 0 ; j < areas[i].size() ; j++)
					{
						Point pj = (Point) areas[i].elementAt(j);
						ii.setPixelBoolean(pj.x - minxareas[i] + 4  ,pj.y - minyareas[i] + 4 , 0, 0, 0,true);
					}
				
					ii = (Image) new BinaryOpening().process(ii, se);
					
					for(int k = 0 ; k < ii.getXDim() ; k++)
						for(int l = 0 ; l < ii.getYDim() ; l++)
							if(ii.getPixelBoolean(k, l, 0, 0, 0))
							{
								output.setPixelBoolean( minxareas[i] + k , minyareas[i] + l  , 0, 0, 0, true);
							}
				}
				if(areas[i].size() < thresC && areas[i].size() > 0 && output.getPixelXYZTBDouble(areas[i].firstElement().x,areas[i].firstElement().y,0,0,0)!= 0 )
				{ //> 0 means that there is an area, and thresC is the reference to the paper, and != 0 means WHITE = continental
					Image ii = new BooleanImage(maxxareas[i]-minxareas[i]+9 , maxyareas[i]-minyareas[i]+9 , 1 ,1 ,1);
					ii.fill(0);
					
					for(int j = 0 ; j < areas[i].size() ; j++)
					{
						Point pj = (Point) areas[i].elementAt(j);
						//System.out.println("pj : "+pj.x+" , "+pj.y+" min : "+minxareas[i]+" , "+minyareas[i]);
						ii.setPixelBoolean(pj.x - minxareas[i] + 4  ,pj.y - minyareas[i] + 4 , 0, 0, 0,true);
					}
					
					ii = BinaryClosing.exec(ii, se);
					
					for(int k = 0 ; k < ii.getXDim() ; k++)
						for(int l = 0 ; l < ii.getYDim() ; l++)
							if(ii.getPixelBoolean(k, l, 0, 0, 0))
							{
								output.setPixelBoolean( minxareas[i] + k , minyareas[i] + l  , 0, 0, 0,false);
							}
				}
			}
		
	}
	
	/**
	 * Creates a new segment, as done in the class "SegmentByConnexity". Here, we search only neightbors in 4-connexity
	 * @param x
	 * @param y
	 * @param label
	 */
	private void newSegment(int x, int y, int label) {
		labelledImage.setPixelXYInt(x, y, label);
		
		Stack<Point> fifo = new Stack<Point>();
		fifo.push(new Point(x, y));
		
		while(!fifo.empty()) {
			Point p = fifo.pop();
			labelledImage.setPixelXYInt(p.x, p.y, label);
			
			// for every pixel in 4-neighbourhood of the pixel
			int k = p.x - 1,l = p.y;
	
			if(k >= 0 && k < labelledImage.getXDim() && l >= 0 &&  l <labelledImage.getYDim() && labelledImage.getPixelXYInt(k, l) == -1 && areEquals(p.x, p.y, k, l))
				fifo.push(new Point(k, l));
			k = p.x + 1; l = p.y;
			if(k >= 0 && k < labelledImage.getXDim() && l >= 0 &&  l <labelledImage.getYDim() && labelledImage.getPixelXYInt(k, l) == -1 && areEquals(p.x, p.y, k, l))
				fifo.push(new Point(k, l));
			k = p.x; l = p.y - 1;
			if(k >= 0 && k < labelledImage.getXDim() && l >= 0 &&  l <labelledImage.getYDim() && labelledImage.getPixelXYInt(k, l) == -1 && areEquals(p.x, p.y, k, l))
				fifo.push(new Point(k, l));
			k = p.x; l = p.y+1;
			if(k >= 0 && k < labelledImage.getXDim() && l >= 0 &&  l <labelledImage.getYDim() && labelledImage.getPixelXYInt(k, l) == -1 && areEquals(p.x, p.y, k, l))
				fifo.push(new Point(k, l));	
		}
	}
	
	/**
	 * Returns true if (x1,y1) == (x2,y2) in the selected band
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private boolean areEquals(int x1, int y1, int x2, int y2) {
		for(int b = 0; b < bInput.getBDim(); b++)
			if(bInput.getPixelXYZTBDouble(x1, y1,0,0, 0) !=bInput.getPixelXYZTBDouble(x2, y2,0,0, 0))
				return false;
		return true;
	}
	
	/**
	 * Method which applies jishuang's algorithm for coastline detection
	 * @param input Satellite picture
	 * @param binarisationmode binarisation mode
	 * @param thresh threshold (if needed)
	 * @param band Band to be computed
	 * @return image with detected coastline
	 */
	public static Image exec(Image input,Integer binarisationmode,Integer thresh,Integer band)
	{
		return (Image) new JishuangCoastlineDetector().process(input,binarisationmode,thresh,band);
	}
	
}
