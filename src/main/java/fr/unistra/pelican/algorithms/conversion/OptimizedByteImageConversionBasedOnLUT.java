package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.largeImages.LargeImageInterface;
import fr.unistra.pelican.util.lut.ThreeBandByteConversionLUT;

/**
 * Made an optimized (in terms of efficiency) ByteImage conversion according to
 * a specified Look-Up Table
 * @author Jonathan Weber
 *
 */
public class OptimizedByteImageConversionBasedOnLUT extends Algorithm{

	/**
	 * Input parameter
	 */
	public ByteImage input;
	
	/**
	 * LUT for conversion
	 */
	public ThreeBandByteConversionLUT lut;

	/**
	 * Output parameter
	 */
	public ByteImage output;
	
	public OptimizedByteImageConversionBasedOnLUT() {
		super.inputs = "input,lut";
		super.outputs = "output";
		
	}
	
	@Override
	public void launch() throws AlgorithmException {
		output = input.copyImage(false);
		byte[][][] lut0=lut.lut0;
		byte[][][] lut1=lut.lut1;
		byte[][][] lut2=lut.lut2;
		
		if(input instanceof LargeImageInterface)
		{
			throw new PelicanException("LargeImage are not managed yet !");
		} else
		{
			byte[] origin = input.getPixels();
			byte[] newValues = output.getPixels();
			int origin0,origin1,origin2;
			for(int i=0;i<origin.length;i=i+3)
			{
				origin0 = origin[i]+128;
				origin1 = origin[i+1]+128;
				origin2 = origin[i+2]+128;
				newValues[i]=lut0[origin0][origin1][origin2];
				newValues[i+1]=lut1[origin0][origin1][origin2];
				newValues[i+2]=lut2[origin0][origin1][origin2];
			}
			output.setPixelsUnsafe(newValues);
		}		
	}
	
	/**
	 * Made an optimized (in terms of efficiency) ByteImage conversion according to
	 * a specified Look-Up Table
	 * @param input
	 * @param lut
	 * @return
	 */
	public static ByteImage exec(ByteImage input, ThreeBandByteConversionLUT lut)
	{
		return (ByteImage) new OptimizedByteImageConversionBasedOnLUT().process(input,lut);
	}
	
	

}
