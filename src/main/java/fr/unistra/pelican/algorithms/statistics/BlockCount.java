package fr.unistra.pelican.algorithms.statistics;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * This class counts the number of white pixels on a block-based basis
 * It accepts the width and height of the blocks in question
 * 
 * @author lefevre
 */

public class BlockCount extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * block width
	 */
	public int w;

	/**
	 * block height
	 */
	public int h;

	/**
	 * Output image
	 */
	public Image output;
	
	/**
	 * This class counts the number of white pixels on a block-based basis
	 * @param input the input image
	 * @param w block width
	 * @param h block height
	 * @return the image containing the number of white pixels on a block-based basis
	 */
	public static Image exec(Image input,Integer w,Integer h)
	{
		return (Image) new BlockCount().process(input,w,h);
	}

	/**
	 * Constructor
	 * 
	 */
	public BlockCount() {
		super.inputs = "input,w,h";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		boolean intMode = w * h >= 256;
		int count = 0;
		if (!intMode)
			output = new ByteImage(input.getXDim() / w, input.getYDim() / h,
					input.getZDim(), input.getTDim(), input.getBDim());
		else
			output = new IntegerImage(input.getXDim() / w, input.getYDim() / h,
					input.getZDim(), input.getTDim(), input.getBDim());
		output.copyAttributes(input);
		for (int z = 0; z < input.getZDim(); z++)
			for (int t = 0; t < input.getTDim(); t++)
				for (int b = 0; b < input.getBDim(); b++)
					for (int x = 0; x < output.getXDim(); x++)
						for (int y = 0; y < output.getYDim(); y++) {
							count = 0;
							for (int i = 0; i < w; i++)
								for (int j = 0; j < h; j++)
									if (input.getPixelBoolean(x * w + i, y * h
											+ j, z, t, b))
										count++;
							if (!intMode)
								output.setPixelByte(x, y, z, t, b, count);
							else
								output.setPixelInt(x, y, z, t, b, count);
						}
	}

}
