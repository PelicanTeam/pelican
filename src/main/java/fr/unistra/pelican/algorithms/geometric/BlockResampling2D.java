package fr.unistra.pelican.algorithms.geometric;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;

/**
 * This class performs a 2D image resampling on a block-based basis, it can
 * either expand or reduce an image with basic processing
 * 
 * @author Lefevre, Benjamin Perret (Fusion Rules)
 */

public class BlockResampling2D extends Algorithm {

	/**
	 * Possible fusion rules in shrinking mode
	 * @author Benjamin Perret
	 *
	 */
	public static enum FusionRule {Mean,Max,Min,Median};
	
	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The block width
	 */
	public int width;

	/**
	 * The block height
	 */
	public int height;

	/**
	 * A flag to determine the resampling operation: expand if true, reduce if
	 * false
	 */
	public boolean expand;

	/**
	 * In shrinking mode, which rule is used to compute pixel value
	 */
	public FusionRule fusionRule=FusionRule.Mean;
	
	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 */
	public BlockResampling2D() {
		super.inputs = "input,width,height,expand";
		super.options="fusionRule";
		super.outputs = "output";
		
	}

	/**
	 * Performs a 2D image resampling on a block-based basis
	 * 
	 * @param input
	 *            The input image
	 * @param width
	 *            The block width
	 * @param height
	 *            The block height
	 * @param expand
	 *            A flag to determine the resampling operation: expand if true,
	 *            reduce if false
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T input, int width, int height, boolean expand) {
		return (T) new BlockResampling2D().process(input, width, height,
				expand);
	}
	
	/**
	 * Performs a 2D image shrinking on a block-based basis
	 * 
	 * @param input
	 *            The input image
	 * @param width
	 *            The block width
	 * @param height
	 *            The block height
	 * @param fusionRule
	 *            How to combine pixels in the block
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T  exec(T input, int width, int height, FusionRule fusionRule) {
		return (T) new BlockResampling2D().process(input, width, height,
				false,fusionRule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		if (expand) {
			output = input
					.newInstance(input.getXDim() * width, input.getYDim()
							* height, input.getZDim(), input.getTDim(), input
							.getBDim());
			output.copyAttributes(input);
			for (int z = 0; z < output.getZDim(); z++)
				for (int t = 0; t < output.getTDim(); t++)
					for (int b = 0; b < output.getBDim(); b++)
						for (int x = 0; x < output.getXDim(); x++)
							for (int y = 0; y < output.getYDim(); y++)
								output.setPixelDouble(x, y, z, t, b, input
										.getPixelDouble(x / width, y / height,
												z, t, b));
		}

		else {
			switch(fusionRule)
			{
			case Mean:
				processMean();
				break;
			case Min:
				processMin();
				break;
			case Max:
				processMax();
				break;
			case Median:
				processMedian();
				break;
			}
			
		}
	}
	
	
	private  void processMean(){
		double somme = 0;
		output = input
				.newInstance(input.getXDim() / width, input.getYDim()
						/ height, input.getZDim(), input.getTDim(), input
						.getBDim());
		output.copyAttributes(input);
		for (int z = 0; z < input.getZDim(); z++)
			for (int t = 0; t < input.getTDim(); t++)
				for (int b = 0; b < input.getBDim(); b++)
					for (int x = 0; x < output.getXDim(); x++)
						for (int y = 0; y < output.getYDim(); y++) {
							somme = 0;
							for (int i = 0; i < width; i++)
								for (int j = 0; j < height; j++)
									somme += input.getPixelDouble(x * width
											+ i, y * height + j, z, t, b);
							somme /= (double)(width * height);
							output.setPixelDouble(x, y, z, t, b, somme);
						}
	}
	
	private  void processMax(){
		double max;
		output = input
				.newInstance(input.getXDim() / width, input.getYDim()
						/ height, input.getZDim(), input.getTDim(), input
						.getBDim());
		output.copyAttributes(input);
		for (int z = 0; z < input.getZDim(); z++)
			for (int t = 0; t < input.getTDim(); t++)
				for (int b = 0; b < input.getBDim(); b++)
					for (int x = 0; x < output.getXDim(); x++)
						for (int y = 0; y < output.getYDim(); y++) {
							max=Double.NEGATIVE_INFINITY;
							for (int i = 0; i < width; i++)
								for (int j = 0; j < height; j++)
								{
									double v=input.getPixelDouble(x * width+ i, y * height + j, z, t, b);
									if(max<v)
										max=v;
								}
							output.setPixelDouble(x, y, z, t, b, max);
						}
	}
	
	private  void processMin(){
		double min;
		output = input
				.newInstance(input.getXDim() / width, input.getYDim()
						/ height, input.getZDim(), input.getTDim(), input
						.getBDim());
		output.copyAttributes(input);
		for (int z = 0; z < input.getZDim(); z++)
			for (int t = 0; t < input.getTDim(); t++)
				for (int b = 0; b < input.getBDim(); b++)
					for (int x = 0; x < output.getXDim(); x++)
						for (int y = 0; y < output.getYDim(); y++) {
							min=Double.POSITIVE_INFINITY;
							for (int i = 0; i < width; i++)
								for (int j = 0; j < height; j++)
								{
									double v=input.getPixelDouble(x * width+ i, y * height + j, z, t, b);
									if(v<min)
										min=v;
								}
							output.setPixelDouble(x, y, z, t, b, min);
						}
	}
	
	private  void processMedian(){
		double val[] = new double[height*width];
		int pos=height*width/2;
		output = input
				.newInstance(input.getXDim() / width, input.getYDim()
						/ height, input.getZDim(), input.getTDim(), input
						.getBDim());
		output.copyAttributes(input);
		for (int z = 0; z < input.getZDim(); z++)
			for (int t = 0; t < input.getTDim(); t++)
				for (int b = 0; b < input.getBDim(); b++)
					for (int x = 0; x < output.getXDim(); x++)
						for (int y = 0; y < output.getYDim(); y++) {
							int c=0;
							for (int i = 0; i < width; i++)
								for (int j = 0; j < height; j++)
								{
									double v=input.getPixelDouble(x * width+ i, y * height + j, z, t, b);
									val[c++]=v;
								}
							Arrays.sort(val);
							output.setPixelDouble(x, y, z, t, b, val[pos]);
						}
	}

}
