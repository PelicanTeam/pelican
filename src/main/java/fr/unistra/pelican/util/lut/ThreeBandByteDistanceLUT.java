package fr.unistra.pelican.util.lut;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import fr.unistra.pelican.AlgorithmException;

public class ThreeBandByteDistanceLUT implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 200911161811L;
	/**
	 * The distance LUT according to a specified distance measure
	 */
	public double[] lut= new double[256*256*256];
	
	/**
	 * Constructor
	 */
	public ThreeBandByteDistanceLUT()	{}
	
	/**
	 * Constructs the distanceLUT for classical L1
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getClassicalL1LUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					lut.lut[i++]=distanceBand0+distanceBand1+distanceBand2; 
				}
		return lut;
	}
	
	/**
	 * Constructs the distanceLUT for classical L2
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getClassicalL2LUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					lut.lut[i++]=Math.sqrt(distanceBand0*distanceBand0+distanceBand1*distanceBand1+distanceBand2*distanceBand2); 
				}
		return lut;
	}
	
	/**
	 * Constructs the distanceLUT for classical LInf
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getClassicalLInfLUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					lut.lut[i++]=Math.max(distanceBand0,Math.max(distanceBand1,distanceBand2)); 
				}
		return lut;
	}
	
	
	
	/**
	 * Constructs the distanceLUT for LSH L1
	 * 
	 * Specificity of the circular Hue component
	 * 
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getLSHL1LUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					if(distanceBand2<128)
					{
						lut.lut[i++]=distanceBand0+distanceBand1+distanceBand2;
					} else
					{
						lut.lut[i++]=distanceBand0+distanceBand1+(255-distanceBand2);
					}
				}
		return lut;
	}
	
	/**
	 * Constructs the distanceLUT for LSH L2
	 * 
	 * Specificity of the circular Hue component
	 * 
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getLSHL2LUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					if(distanceBand2<128)
					{
						lut.lut[i++]=Math.sqrt(distanceBand0*distanceBand0+distanceBand1*distanceBand1+distanceBand2*distanceBand2);
					} else
					{
						lut.lut[i++]=Math.sqrt(distanceBand0*distanceBand0+distanceBand1*distanceBand1+(255-distanceBand2)*(255-distanceBand2));
					}					 
				}
		return lut;
	}
	
	/**
	 * Constructs the distanceLUT for LSH LInf
	 * 
	 * Specificity of the circular Hue component
	 * 
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getLSHLInfLUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					if(distanceBand2<128)
					{
						lut.lut[i++]=Math.max(distanceBand0,Math.max(distanceBand1,distanceBand2)); 
					} else
					{
						lut.lut[i++]=Math.max(distanceBand0,Math.max(distanceBand1,(255-distanceBand2))); 
					}					
				}
		return lut;
	}
	
	/**
	 * Constructs the distanceLUT for LSH Hue 
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getLSHHueLUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					if(distanceBand2<128)
						lut.lut[i++]=distanceBand2;
					else
						lut.lut[i++]=(255-distanceBand2);
				}
		return lut;
	}
	
	/**
	 * Constructs the distanceLUT for HSV L1
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getHSVL1LUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					if(distanceBand0<128)
						lut.lut[i++]=distanceBand0+distanceBand1+distanceBand2;
					else
						lut.lut[i++]=(255-distanceBand0)+distanceBand1+distanceBand2;
				}
		return lut;
	}
	
	/**
	 * Constructs the distanceLUT for HSV L2
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getHSVL2LUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					if(distanceBand0<128)
						lut.lut[i++]=Math.sqrt(distanceBand0*distanceBand0+distanceBand1*distanceBand1+distanceBand2*distanceBand2);
					else
						lut.lut[i++]=Math.sqrt((255-distanceBand0)*(255-distanceBand0)+distanceBand1*distanceBand1+distanceBand2*distanceBand2);
				}
		return lut;
	}
	
	
	
	/**
	 * Constructs the distanceLUT for HSV LInf
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getHSVLInfLUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					if(distanceBand0<128)
						lut.lut[i++]=Math.max(distanceBand0,Math.max(distanceBand1,distanceBand2));
					else
						lut.lut[i++]=Math.max((255-distanceBand0),Math.max(distanceBand1,distanceBand2));
				}
		return lut;
	}
	
	
	
	/**
	 * Constructs the distanceLUT for HSV Hue 
	 * @return
	 */
	public static ThreeBandByteDistanceLUT getHSVHueLUT()
	{
		ThreeBandByteDistanceLUT lut = new ThreeBandByteDistanceLUT();
		int i=0;
		for(int distanceBand0=0;distanceBand0<256;distanceBand0++)
			for(int distanceBand1=0;distanceBand1<256;distanceBand1++)
				for(int distanceBand2=0;distanceBand2<256;distanceBand2++)
				{
					if(distanceBand0<128)
						lut.lut[i++]=distanceBand0;
					else
						lut.lut[i++]=(255-distanceBand0);
				}
		return lut;
	}
	
	public final double get(int b0, int b1, int b2)
	{
		return lut[(b0<<8|b1)<<8|b2];
	}
	
	/**
	 * Save the LUT on the specified filename
	 * @param filename
	 */
	public void save(String filename)
	{
		try {
			ObjectOutputStream f = null;
			f = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filename)));
			
			f.writeObject(this);
			f.close();			
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			throw new AlgorithmException("file writing error with file: " + filename);
		}
	}
	
	/**
	 * Load a LUT from a specified filename
	 * @param filename
	 * @return LUT
	 */
	public static ThreeBandByteDistanceLUT load(String filename)
	{
		ThreeBandByteDistanceLUT lut=null;
		try {
			ObjectInputStream f = null;
				f = new ObjectInputStream(new GZIPInputStream(new FileInputStream(
					filename)));
			
				lut = (ThreeBandByteDistanceLUT) f.readObject();
			f.close();
		} catch (IOException ex) {
			throw new AlgorithmException("file reading error with file: " + filename,ex);
		} catch (ClassNotFoundException ex) {
			throw new AlgorithmException("file reading error with file: " + filename,ex);
		}
		return lut;
	}
}



