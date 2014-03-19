package fr.unistra.pelican.algorithms.morphology.vectorial.hitormiss;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialDilation;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialErosion;
import fr.unistra.pelican.util.vectorial.orders.VectorialOrdering;
/**
 * This class is an implementation of the paper
 * E. Aptoula, S. Lefèvre, C. Ronse, A Hit-or-Miss Transform for Multivariate Images,
 * Pattern Recognition Letters, Vol. 30, No. 8, june 2009, pages 760-764
 * 
 * but with the restriction of using flat structuring elements
 * 
 * TODO : Develop the generalized algorithm with non-flat SEs
 * 
 * @author Jonathan Weber
 *
 */
public class VectorialHitOrMiss extends Algorithm
{
	/**
	 * Input image
	 */
	public Image input;
	
	/**
	 * Foreground structuring element
	 */
	public BooleanImage seFG;
	
	/**
	 * Background structuring element
	 */
	public BooleanImage seBG;
	
	/**
	 * the vector ordering
	 */
	public VectorialOrdering vo;

	/**
	 * The choosed operator for VHMT
	 */
	public int operator=INTEGRALINTERVALOPERATOR;
	
	/**
	 * Output
	 */
	public Image output;

	
	public static final int INTEGRALINTERVALOPERATOR = 0;
	public static final int SUPREMALINTERVALOPERATOR = 1;
	
	/**
	 * Constructor
	 * 
	 */
	public VectorialHitOrMiss()
	{
		super();
		super.inputs = "input,seFG,seBG,vo";
		super.options = "operator";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch()
	{	
		int tDim = input.getTDim();
		int zDim = input.getZDim();
		int yDim = input.getYDim();
		int xDim = input.getXDim();
		int bDim = input.getBDim();

		Image erosionFG = VectorialErosion.exec(input, seFG, vo);
		//Image erosionFG = PelicanImageLoad.exec("C:\\Documents and Settings\\Jonathan.weber\\Mes documents\\Mes images\\MHMT_artificial\\erosionFG.pelican");
	
		Image dilationBG = VectorialDilation.exec(input, seBG, vo);
		//Image dilationBG = PelicanImageLoad.exec("C:\\Documents and Settings\\Jonathan.weber\\Mes documents\\Mes images\\MHMT_artificial\\dilationBG.pelican");

		output = input.copyImage(false);
		output.fill(0.);
		
		if(operator==INTEGRALINTERVALOPERATOR)
		{
			for(int t=0;t<tDim;t++)
				for(int z=0;z<zDim;z++)
					for(int y=0;y<yDim;y++)
						for(int x=0;x<xDim;x++)
						{
							double[] erosion = new double[bDim]; 
							double[] dilation = new double[bDim];
							for(int b=0;b<bDim;b++)
							{
								erosion[b] = erosionFG.getPixelXYZTBDouble(x, y, z, t, b);
								dilation[b] = dilationBG.getPixelXYZTBDouble(x, y, z, t, b);
							}
							double[][] test = new double[2][bDim];
							test[0] = erosion;
							test[1] = dilation;
							if(!Arrays.equals(erosion,dilation)&&Arrays.equals(vo.min(test),dilation))
							{							
								for(int b=0;b<bDim;b++)
								{
									double value = Math.abs(erosionFG.getPixelXYZTBDouble(x, y, z, t, b)-dilationBG.getPixelXYZTBDouble(x, y, z, t, b));
									output.setPixelXYZTBDouble(x,y,z,t,b,value);									
								}
							}else
							{
								for(int b=0;b<bDim;b++)
								{
									output.setPixelXYZTBDouble(x,y,z,t,b,0.);
								}
							}
						}
		} else if (operator ==SUPREMALINTERVALOPERATOR)
		{
			for(int t=0;t<tDim;t++)
				for(int z=0;z<zDim;z++)
					for(int y=0;y<yDim;y++)
						for(int x=0;x<xDim;x++)
						{
							double[] erosion = new double[bDim]; 
							double[] dilation = new double[bDim];
							for(int b=0;b<bDim;b++)
							{
								erosion[b] = erosionFG.getPixelXYZTBDouble(x, y, z, t, b);
								dilation[b] = dilationBG.getPixelXYZTBDouble(x, y, z, t, b);
							}
							double[][] test = new double[2][bDim];
							test[0] = erosion;
							test[1] = dilation;
							if(Arrays.equals(vo.min(test),dilation))
							{
								for(int b=0;b<bDim;b++)
								{
									output.setPixelXYZTBDouble(x,y,z,t,b,erosionFG.getPixelXYZTBDouble(x, y, z, t, b));
								}
							}else
							{
								for(int b=0;b<bDim;b++)
								{
									output.setPixelXYZTBDouble(x,y,z,t,b,0.);
								}
							}
						}
		} else
		{
			throw new PelicanException("Invalid Operator : "+operator);
		}
		
	}
	
	/**
	 * This methods perform a Vectorial hit or miss with integral interval operator
	 * 
	 * @param input  Image to compute
	 * @param seFG   Structuring Element representing the foreground
	 * @param seBG	 Structuring Element representing the background	
	 * @param vo     Vectorial Ordering used
	 * @return		 Result of the VHMT
	 */
	public static Image exec(Image input, BooleanImage seFG, BooleanImage seBG, VectorialOrdering vo)
	{
		return (Image) new VectorialHitOrMiss().process(input,seFG,seBG,vo);
	}
	
	/**
	 * This methods perform a Vectorial hit or miss with desired operator
	 * 
	 * @param input  	Image to compute
	 * @param seFG   	Structuring Element representing the foreground
	 * @param seBG	 	Structuring Element representing the background	
	 * @param vo     	Vectorial Ordering used
	 * @param operator	Desired interval operator (integral or supremal)
	 * @return		 	Result of the VHMT
	 */
	public static Image exec(Image input, BooleanImage seFG, BooleanImage seBG, VectorialOrdering vo, int operator)
	{
		return (Image) new VectorialHitOrMiss().process(input,seFG,seBG,vo, operator);
	}

	
}
