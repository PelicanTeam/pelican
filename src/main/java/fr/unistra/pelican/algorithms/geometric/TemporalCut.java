package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;

/**
 * This class performs a temporal cut on a video sequence
 * 
 * @author Jonathan Weber
 */
public class TemporalCut extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The first frame
	 */
	public int firstFrame;

	/**
	 * The last frame
	 */
	public int lastFrame;

	/** 
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 */
	public TemporalCut() {
		super.inputs = "input,firstFrame,lastFrame";
		super.outputs = "output";
		
	}

	/**
	 * Performs temporal cut
	 * @param input The input image
	 * @param firstFrame The first frame of the cut
	 * @param lastFrame The last frame of the cut
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec (T input, int firstFrame, int lastFrame) {
		return (T) new TemporalCut().process(input,firstFrame,lastFrame);
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() 
	{
		int xDim = input.getXDim();
		int yDim = input.getYDim();
		int zDim = input.getZDim();
		int tDim = input.getTDim();
		int bDim = input.getBDim();
		if(firstFrame<0||lastFrame<0||firstFrame>=tDim||lastFrame>=tDim||firstFrame>lastFrame)
		{
			throw new PelicanException("frame index problem : original images has "+tDim+" frames, parameters are firstFrame="+firstFrame+" | lastFrame="+lastFrame);
		}
		output = input.newInstance(xDim, yDim, zDim, lastFrame-firstFrame+1, bDim);
		int index = input.getLinearIndexXYZTB(0, 0, 0, firstFrame, 0);
		System.out.println("index : "+index);
		System.out.println("input size : "+input.size());
		System.out.println("output size : "+output.size());
		for(int i=0;i<output.size();i++,index++)
		{
			output.setPixelByte(i,input.getPixelByte(index));
		}
	}

}
