package fr.unistra.pelican.algorithms.statistics;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
/**
 * This class calculate Shannon entropy for grey-level byte image and color byte image.
 * 
 * @author weber
 *
 */
public class ShannonEntropy extends Algorithm
{
	
	public Image input;
	
	public Double entropy;
	
	public ShannonEntropy()
	{
		super.inputs="input";
		super.outputs="entropy";
	}
	
	
	public void launch() throws AlgorithmException 
	{
		double[] pk;
		entropy=0.;
		if(input.getBDim()==1)
		{
			pk = new double[256];
			Arrays.fill(pk,0);
			for(int i=0;i<input.size();i++)
				pk[input.getPixelByte(i)]++;
			for(int i=0;i<256;i++)
				pk[i]=pk[i]/input.size();			
		}
		else if(input.getBDim()==3)
		{
			pk = new double[256*256*256];
			Arrays.fill(pk,0);
			for(int i=0;i<input.size();i+=3)
			{
				pk[input.getPixelByte(i)+(input.getPixelByte(i+1)*256)+(input.getPixelByte(i+2)*256*256)]++;
			}
			int nbPix=input.size()/3;
			for(int i=0;i<pk.length;i++)
				pk[i]=pk[i]/nbPix;		
		}
		else
		{
			throw new PelicanException("This type of image is not managed yet");
		}
		for(int i=0;i<pk.length;i++)
			if(pk[i]>0)
				entropy+=-pk[i]*(Math.log(pk[i])/Math.log(2));
	

	}
	
	public static Double exec(Image input)
	{
		return (Double) new ShannonEntropy().process(input);
	}
}
