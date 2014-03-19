package fr.unistra.pelican.algorithms.applied.remotesensing.coastline;


import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.OtsuThresholding;
import fr.unistra.pelican.algorithms.spatial.GaussianSmoothing;
import fr.unistra.pelican.util.Gradient;


/** These class is a possible application of Günther Heene and Sidharta Gautama in the paper "Optimisation of a Coastline
 * Extraction Algorithm for Object-Oriented Matching of Multisensor Satellite Imagery". It builds the coastline of an image,
 * using the canny edge detection and the edge focusing technique. M. R. Della Rocca presents, in the paper "Active contour
 * model to detect linear features in satellite images" a similar method based on the wavelet transform. S. Mallat and S. Zhong proved
 * in the paper "Characterization of Signals from multiscale Edges" that wavelet transform and canny edge detector are
 * equivalent, so these class is a possible application of each one of these methods.
 * 
 * @author Dany DAMAJ, Jonathan WEBER
 */
public class HeeneCoastlineDetector extends Algorithm
{
	/**
	 * Image to be processed
	 */
	public Image input;
	
	/**
	 * Band to be computed
	 */
	public Integer band;
	
	private Image bInput;
	/**
	 * resulting picture
	 */
	public Image output;
	
	
	private int xDim,yDim,bDim;
	private Gradient gradients[][];
	private boolean isConsidered[][];
	private boolean newConsidered[][];
	
	// Heene says : the size of the gaussian mask can be constant, the sigma of the mask decrement while it's greater than 0
	private int maskSize = 15;
	private int edging = 1;
	private float sigma = 8.8f;  //Heene's initial value
	private float decrementation = 1.0f;
	
	/**
  	 * Constructor
  	 *
  	 */
	public HeeneCoastlineDetector() {		
		
		super();		
		super.inputs = "input,band";		
		super.outputs = "output";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException
	{
		xDim = input.getXDim();
		yDim = input.getYDim();
		int zDim = input.getZDim();
		int tDim = input.getTDim();
		bDim = input.getBDim();
		
		output = new BooleanImage(input.getXDim(),input.getYDim(),1,1,1);
		
		bInput = input.getImage4D(band, Image.B);
		
		
		
			int nb_step_focus = (int)Math.ceil((double)(sigma/decrementation));
			int step_focus = 0;
			
			
		
			 ///// INITIALISATION
			// first, smooth the image with a gaussian mask
			Image gauss = (Image )new GaussianSmoothing().process(bInput, maskSize, sigma);
			
			//apply a simple thresholding to obtain 2 classes : land and water
			output = (Image) new OtsuThresholding().process(gauss);
			
			gradients = new Gradient[xDim][yDim];
			double max_gradient_magnitude = 0.0,current_gradient = 0.0;
			double gi,gj; //horizontal and vertical gradients
			
			for (int x = edging; x < xDim-edging; x++)
			{
				for (int y = edging; y < yDim-edging; y++)
				{
					gi = output.getPixelXYZTBByte(x+1,y-1,0,0,0) +2.0* output.getPixelXYZTBByte(x+1,y,0,0,0) + output.getPixelXYZTBByte(x+1,y+1,0,0,0)
						- (output.getPixelXYZTBByte(x-1,y-1,0,0,0) +2.0* output.getPixelXYZTBByte(x-1,y,0,0,0) + output.getPixelXYZTBByte(x-1,y+1,0,0,0));
					
					gj = output.getPixelXYZTBByte(x-1,y+1,0,0,0) +2.0* output.getPixelXYZTBByte(x,y+1,0,0,0) + output.getPixelXYZTBByte(x+1,y+1,0,0,0)
						- (output.getPixelXYZTBByte(x-1,y-1,0,0,0) +2.0* output.getPixelXYZTBByte(x,y-1,0,0,0) + output.getPixelXYZTBByte(x+1,y-1,0,0,0));		
					
					// |gi|+|gj| is the commom approximation of the magnitude
					if(gi == 0.0)
					{
						if(gj == 0.0)
						{
							gradients[x][y] = new Gradient(x,y,Math.abs(gi) + Math.abs(gj),0);
						}
						else
						{
							gradients[x][y] = new Gradient(x,y,Math.abs(gi) + Math.abs(gj),Math.PI/2.0);
						}
					}
					else
						gradients[x][y] = new Gradient(x,y,Math.abs(gi) + Math.abs(gj),Math.atan(gj/gi));
					
					if(gradients[x][y].magnitude > max_gradient_magnitude)
						max_gradient_magnitude = gradients[x][y].magnitude;
				}
			}
			
			// next step : threshold the result
			double main_thres = max_gradient_magnitude / 10.0;
			isConsidered = new boolean [xDim][yDim];
			for(int x = 0 ; x < xDim ; x++)
			{
				for(int y = 0 ; y < yDim ; y++)
				{
					if(gradients[x][y] != null && gradients[x][y].magnitude >= main_thres)
						isConsidered[x][y] = true;
					else isConsidered[x][y] = false;
				}
			}
			
			
			
			Gradient gxy;
			// suppress non-maxima pixels
			for(int x = 0 ; x < xDim ; x++)
			{
				for(int y = 0 ; y < yDim ; y++)
				{
					if(isConsidered[x][y])
					{
						// we suppress the pixel if the 2 connected pixel in the direction of the gradient are lesser
						gxy = gradients[x][y];
						if(gxy.direction == Gradient.PI04  && gradients[x-1][y] != null && gradients[x+1][y]!=null &&  !(gxy.magnitude >= gradients[x-1][y].magnitude && gxy.magnitude >= gradients[x+1][y].magnitude))
							isConsidered[x][y] = false;
						else if(gxy.direction == Gradient.PI14  && gradients[x-1][y-1] != null && gradients[x+1][y+1]!=null && !(gxy.magnitude >= gradients[x-1][y-1].magnitude && gxy.magnitude >= gradients[x+1][y+1].magnitude))
							isConsidered[x][y] = false;
						else if(gxy.direction == Gradient.PI24  && gradients[x][y-1] != null && gradients[x][y+1]!=null && !(gxy.magnitude >= gradients[x][y-1].magnitude && gxy.magnitude >= gradients[x][y+1].magnitude))
							isConsidered[x][y] = false;
						else if(gxy.direction == Gradient.PI34  && gradients[x-1][y+1] != null && gradients[x+1][y-1]!=null && !(gxy.magnitude >= gradients[x-1][y+1].magnitude && gxy.magnitude >= gradients[x+1][y-1].magnitude))
							isConsidered[x][y] = false;
						
					}
				}
			}
			
			
			//consider points in 8 connexity
			newConsidered = new boolean[xDim][yDim];
			
			System.out.println("estimation : 10%");
			
			// starting edge focusing
			while(sigma > decrementation) // to prevent sigma from being < 0 in the "while"
			{
			
			
			
				for(int x = 0 ;x < xDim ; x++)
					for(int y = 0; y < yDim ; y++)
					{
						newConsidered[x][y] = false;
						gradients[x][y] = null;
					}
				
				for(int x = 1 ;x < xDim-1 ; x++)
					for(int y = 1; y < yDim -1 ; y++)
						if(isConsidered[x][y])
							for(int i = x-1 ; i <= x+1 ; i++)
								for(int j  = y-1 ; j <= y+1 ; j++)
									newConsidered[i][j] = true;
				
				sigma -= decrementation;
				Image gaussrec = (Image) new GaussianSmoothing().process(bInput, maskSize, sigma);
				// Otsu !? Maybe the best way to do, maybe not
				output = (Image) new OtsuThresholding().process(gaussrec);
				 max_gradient_magnitude = 0.0;
				 
				for (int x = edging; x < xDim-edging; x++)
				{
					for (int y = edging; y < yDim-edging; y++)
					{
						if(newConsidered[x][y] || sigma <= decrementation) //consider only useful pixels ; for the last iteration : consider all pixels
						{
							gi = output.getPixelXYZTBByte(x+1,y-1,0,0,0) +2.0* output.getPixelXYZTBByte(x+1,y,0,0,0) + output.getPixelXYZTBByte(x+1,y+1,0,0,0)
								- (output.getPixelXYZTBByte(x-1,y-1,0,0,0) +2.0* output.getPixelXYZTBByte(x-1,y,0,0,0) + output.getPixelXYZTBByte(x-1,y+1,0,0,0));
							
							gj = output.getPixelXYZTBByte(x-1,y+1,0,0,0) +2.0* output.getPixelXYZTBByte(x,y+1,0,0,0) + output.getPixelXYZTBByte(x+1,y+1,0,0,0)
								- (output.getPixelXYZTBByte(x-1,y-1,0,0,0) +2.0* output.getPixelXYZTBByte(x,y-1,0,0,0) + output.getPixelXYZTBByte(x+1,y-1,0,0,0));		
							
							// commom approximation
							if(gi == 0.0)
							{
								if(gj == 0.0)
								{
									gradients[x][y] = new Gradient(x,y,Math.abs(gi) + Math.abs(gj),0);
								}
								else
								{
									gradients[x][y] = new Gradient(x,y,Math.abs(gi) + Math.abs(gj),Math.PI/2.0);
								}
							}
							else
								gradients[x][y] = new Gradient(x,y,Math.abs(gi) + Math.abs(gj),Math.atan(gj/gi));
							
							if(gradients[x][y].magnitude > max_gradient_magnitude)
								max_gradient_magnitude = gradients[x][y].magnitude;
						}
					}
				}
				
				// next step : threshold the result
				 main_thres = max_gradient_magnitude / 2.0;
				for(int x = 0 ; x < xDim ; x++)
				{
					for(int y = 0 ; y < yDim ; y++)
					{
						if(newConsidered[x][y] && gradients[x][y] !=null && gradients[x][y].magnitude >= main_thres)
							isConsidered[x][y] = true;
						else isConsidered[x][y] = false;
					}
				}
				
				// suppress non-maxima pixels
				for(int x = 0 ; x < xDim ; x++)
				{
					for(int y = 0 ; y < yDim ; y++)
					{
						if(isConsidered[x][y])
						{
							// we suppress the pixel if the 2 connected pixel in the direction of the gradient are lesser
							gxy = gradients[x][y];
							if(gxy.direction == Gradient.PI04  && gradients[x-1][y] != null && gradients[x+1][y]!=null &&  !(gxy.magnitude >= gradients[x-1][y].magnitude && gxy.magnitude >= gradients[x+1][y].magnitude))
								isConsidered[x][y] = false;
							else if(gxy.direction == Gradient.PI14  && gradients[x-1][y-1] != null && gradients[x+1][y+1]!=null && !(gxy.magnitude >= gradients[x-1][y-1].magnitude && gxy.magnitude >= gradients[x+1][y+1].magnitude))
								isConsidered[x][y] = false;
							else if(gxy.direction == Gradient.PI24  && gradients[x][y-1] != null && gradients[x][y+1]!=null && !(gxy.magnitude >= gradients[x][y-1].magnitude && gxy.magnitude >= gradients[x][y+1].magnitude))
								isConsidered[x][y] = false;
							else if(gxy.direction == Gradient.PI34  && gradients[x-1][y+1] != null && gradients[x+1][y-1]!=null && !(gxy.magnitude >= gradients[x-1][y+1].magnitude && gxy.magnitude >= gradients[x+1][y-1].magnitude))
								isConsidered[x][y] = false;
						}
					}
				}
				
				
				step_focus++;
				System.out.println("estimation : "+(int)(10.0+80.0*(double)step_focus/(double)nb_step_focus)+"%");
				
			}
			
					
			// now we apply a n-edge closing, using histeresis technique
			// as Heene said in his paper, we need to compute the image gradient
			//	the last max_gradient_magnitude was computed in the last iteration of edge-focusing
			// and all the gradients were computed too
			Image image_gradient = new DoubleImage(xDim,yDim,1,1,1);
			for(int x = 1 ;x < xDim-1 ; x++)
				for(int y = 1; y < yDim-1 ; y++)
					image_gradient.setPixelDouble(x, y, 0, 0, 0, gradients[x][y].magnitude / max_gradient_magnitude);
			
			// hysteresis values : not as global variables because it does not really depend on the image
			double thres1 = (1.0 / 3.0); 
			double thres2 = (2.0 / 3.0);
			
			Image thres1_img = (Image) new ManualThresholding().process(image_gradient, thres1);
			Image thres2_img = (Image) new ManualThresholding().process(image_gradient, thres2);
					
			//  to start, we consider the pixels that are considered ( isConsidered = true) and which are in thres2_img too
			// reinitialisation
			for(int x = 0 ;x < xDim ; x++)
			{
				for(int y = 0; y < yDim ; y++)
				{
					if(thres2_img.getPixelDouble(x, y, 0, 0, 0) > 0 && isConsidered[x][y])
						newConsidered[x][y] = true;
					else
						newConsidered[x][y] = false;
					
					if(thres1_img.getPixelDouble(x, y, 0, 0, 0) > 0 && isConsidered[x][y])
						isConsidered[x][y] = true; //no risk of value-erase !
					else
						isConsidered[x][y] = false;
				}
			}
			
			
			
			// to finish, we close the edge, joining 2 newConsidered pixels which have isConsidered points between them AND
			// following the gradient direction
			
			Vector <Point> tmp = new Vector <Point>();
			for(int x = 0 ;x < xDim ; x++)
			{
				for(int y = 0; y < yDim ; y++)
				{
					if(newConsidered[x][y])
					{
						//System.out.println("point : "+x+","+y+ " et direction ="+gradients[x][y].direction);
						//2 possible directions
						if(gradients[x][y].direction == Gradient.PI04)
						{
							if(isConsidered[x-1][y] && !newConsidered[x-1][y] )
							{
								tmp.removeAllElements();
								tmp.add(new Point(x,y));
								tmp = linking(x-1,y,tmp);
								//if(tmp.size() >= 1)
								//	System.out.println("on joint "+tmp.firstElement().x+","+tmp.firstElement().y+" a "+tmp.lastElement().x+","+tmp.lastElement().y+" , taille = "+tmp.size()+" cas 0");
								for(int i = 0 ; i < tmp.size() ; i++)
									newConsidered[tmp.elementAt(i).x][tmp.elementAt(i).y] = true;
							}
	
							if(isConsidered[x+1][y] && !newConsidered[x+1][y] )
							{
								tmp.removeAllElements();
								tmp.add(new Point(x,y));
								tmp = linking(x+1,y,tmp);
								//if(tmp.size() >= 1)
								//	System.out.println("on joint "+tmp.firstElement().x+","+tmp.firstElement().y+" a "+tmp.lastElement().x+","+tmp.lastElement().y+" , taille = "+tmp.size()+" cas 1");
								for(int i = 0 ; i < tmp.size() ; i++)
									newConsidered[tmp.elementAt(i).x][tmp.elementAt(i).y] = true;
							}
						}
						else if(gradients[x][y].direction == Gradient.PI34)
						{
							if(isConsidered[x-1][y-1] && !newConsidered[x-1][y-1] )
							{
								tmp.removeAllElements();
								tmp.add(new Point(x,y));
								tmp = linking(x-1,y-1,tmp);
								//if(tmp.size() >= 1)
								//	System.out.println("on joint "+tmp.firstElement().x+","+tmp.firstElement().y+" a "+tmp.lastElement().x+","+tmp.lastElement().y+" , taille = "+tmp.size()+" cas 2");
								for(int i = 0 ; i < tmp.size() ; i++)
									newConsidered[tmp.elementAt(i).x][tmp.elementAt(i).y] = true;
							}
	
							if(isConsidered[x+1][y+1] && !newConsidered[x+1][y+1] )
							{
								tmp.removeAllElements();
								tmp.add(new Point(x,y));
								tmp = linking(x+1,y+1,tmp);
								//if(tmp.size() >= 1)
								//	System.out.println("on joint "+tmp.firstElement().x+","+tmp.firstElement().y+" a "+tmp.lastElement().x+","+tmp.lastElement().y+" , taille = "+tmp.size()+" cas 3");
								for(int i = 0 ; i < tmp.size() ; i++)
									newConsidered[tmp.elementAt(i).x][tmp.elementAt(i).y] = true;
							}
						}
						else if(gradients[x][y].direction == Gradient.PI24)
						{
							if(isConsidered[x][y-1] && !newConsidered[x][y-1] )
							{
								tmp.removeAllElements();
								tmp.add(new Point(x,y));
								tmp = linking(x,y-1,tmp);
								//if(tmp.size() >= 1)
								//	System.out.println("on joint "+tmp.firstElement().x+","+tmp.firstElement().y+" a "+tmp.lastElement().x+","+tmp.lastElement().y+" , taille = "+tmp.size()+" cas 4");
								for(int i = 0 ; i < tmp.size() ; i++)
									newConsidered[tmp.elementAt(i).x][tmp.elementAt(i).y] = true;
							}
	
							if(isConsidered[x][y+1] && !newConsidered[x][y+1] )
							{
								tmp.removeAllElements();
								tmp.add(new Point(x,y));
								tmp = linking(x,y+1,tmp);
								//if(tmp.size() >= 1)
								//	System.out.println("on joint "+tmp.firstElement().x+","+tmp.firstElement().y+" a "+tmp.lastElement().x+","+tmp.lastElement().y+" , taille = "+tmp.size()+" cas 5");
								for(int i = 0 ; i < tmp.size() ; i++)
									newConsidered[tmp.elementAt(i).x][tmp.elementAt(i).y] = true;
							}
						}
						else if(gradients[x][y].direction == Gradient.PI14)
						{
							if(isConsidered[x-1][y+1] && !newConsidered[x-1][y+1] )
							{
								tmp.removeAllElements();
								tmp.add(new Point(x,y));
								tmp = linking(x-1,y+1,tmp);
								//if(tmp.size() >= 1)
								//	System.out.println("on joint "+tmp.firstElement().x+","+tmp.firstElement().y+" a "+tmp.lastElement().x+","+tmp.lastElement().y+" , taille = "+tmp.size()+" cas 6");
								for(int i = 0 ; i < tmp.size() ; i++)
									newConsidered[tmp.elementAt(i).x][tmp.elementAt(i).y] = true;
							}
	
							if(isConsidered[x+1][y-1] && !newConsidered[x+1][y-1] )
							{
								tmp.removeAllElements();
								tmp.add(new Point(x,y));
								tmp = linking(x+1,y-1,tmp);
								//if(tmp.size() >= 1)
								//	System.out.println("on joint "+tmp.firstElement().x+","+tmp.firstElement().y+" a "+tmp.lastElement().x+","+tmp.lastElement().y+" , taille = "+tmp.size()+" cas 7");
								for(int i = 0 ; i < tmp.size() ; i++)
									newConsidered[tmp.elementAt(i).x][tmp.elementAt(i).y] = true;
							}
						}
					}
				}
			}
			
			output.fill(0);
			for(int x = maskSize/2 ;x < xDim-maskSize/2 ; x++)
			{
				for(int y = maskSize/2; y < yDim-maskSize/2 ; y++)
				{
					output.setPixelBoolean(x, y, 0, 0, 0, newConsidered[x][y]);
				}
			}
		
		
		
		System.out.println(" Heene00 finished !");
	}
	
	/**
	 * Tests if a specified vector contains a specified point
	 * @param p
	 * @param v
	 * @return
	 */
	private boolean isIncluded(Point p,Vector v)
	{
		for(int i = 0 ; i < v.size() ; i++)
			if(((Point)v.elementAt(i)).x == p.x && ((Point)v.elementAt(i)).y == p.y)
				return true;
		return false;
	}
	
	private Vector linking(int x,int y,Vector <Point> v)
	{
		v.add(new Point(x,y));
		
		if(v.size() > 20)
		{
			v.removeAllElements();
			return v;
		}
		
		//2 possible directions
		if(gradients[x][y].direction == Gradient.PI04)
		{
			if( !isIncluded(new Point(x-1,y), v) )
			{
				if(isConsidered[x-1][y] && !newConsidered[x-1][y] )
					return linking(x-1,y,v);
				else if(newConsidered[x-1][y])
					return v;
			}

			if(!isIncluded(new Point(x+1,y), v) )
			{
				if(isConsidered[x+1][y] && !newConsidered[x+1][y])
					return linking(x+1,y,v);
				else if(newConsidered[x+1][y])
					return v;
			}
		}
		else if(gradients[x][y].direction == Gradient.PI34)
		{
			if(!isIncluded(new Point(x-1,y-1), v) )
			{
				if(isConsidered[x-1][y-1] && !newConsidered[x-1][y-1]) 
					return linking(x-1,y-1,v);
				else if(newConsidered[x-1][y-1])
					return v;
			}

			if( !isIncluded(new Point(x+1,y+1), v))
			{
				if(isConsidered[x+1][y+1] && !newConsidered[x+1][y+1])
					return linking(x+1,y+1,v);
				else if(newConsidered[x+1][y+1])
					return v;
			}
		}
		else if(gradients[x][y].direction == Gradient.PI24)
		{
			if(!isIncluded(new Point(x,y-1), v))
			{
				if(isConsidered[x][y-1] && !newConsidered[x][y-1] )
					return linking(x,y-1,v);
				else if(newConsidered[x][y-1])
					return v;
			}

			if(!isIncluded(new Point(x,y+1), v))
			{
				if(isConsidered[x][y+1] && !newConsidered[x][y+1])
					return linking(x,y+1,v);
				else if(newConsidered[x][y+1])
					return v;
			}
		}
		else if(gradients[x][y].direction == Gradient.PI14)
		{
			if(!isIncluded(new Point(x-1,y+1), v))
			{
				if(isConsidered[x-1][y+1] && !newConsidered[x-1][y+1])
					return linking(x-1,y+1,v);
				else if(newConsidered[x-1][y+1])
					return v;
			}

			if(!isIncluded(new Point(x+1,y-1), v))
			{
				if(isConsidered[x+1][y-1] && !newConsidered[x+1][y-1] )
					return linking(x+1,y-1,v);
				else if(newConsidered[x+1][y-1])
					return v;
			}
		
		}
		
		return v;
	}
	
	/**
	 * Method which applies the Heene's algorithm for coastline detection.
	 * @param input Satellite picture
	 * @param band band to be computed
	 * @return Image with the detected coastline
	 */
	public static Image exec(Image input, Integer band)
	{
		return (Image) new HeeneCoastlineDetector().process(input,band); 
	}
}


