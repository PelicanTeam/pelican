package fr.unistra.pelican.algorithms.geometric;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;

/**
 * This class performs 2D image cropping (i.e. reducing the size of the image)
 * 
 * Set a coordinate (top, bottom, left, right) to -1 to disable it
 * 
 * @author Lefevre
 */
public class Crop2D extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The top left point
	 */
	public Point p1;

	/**
	 * The bottom right point
	 */
	public Point p2;

	/** 
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 */
	public Crop2D() {
		super.inputs = "input,p1,p2";
		super.outputs = "output";
		
	}

	/**
	 * Performs 2D image cropping (i.e. reducing the size of the image)
	 * @param input The input image
	 * @param p1 The top left point
	 * @param p2 The bottom right point
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec (T input, Point p1, Point p2) {
		return (T) new Crop2D().process(input,p1,p2);
	}
	
	/**
	 * Performs 2D image cropping (i.e. reducing the size of the image)
	 * @param input The input image
	 * @param x1 X coordinate of the top left point
	 * @param y1 Y coordinate of the top left point
	 * @param x2 X coordinate of the bottom right point
	 * @param y2 Y coordinate of the bottom right point
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec (T input, int x1, int y1, int x2, int y2) {
		return (T) new Crop2D().process(input,new Point(x1,y1),new Point(x2,y2));
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		if (p1.x <= -1)
			p1.x = 0;
		if (p1.y <= -1)
			p1.y = 0;
		if (p2.x >= input.getXDim())
			p2.x = input.getXDim() - 1;
		if (p2.y >= input.getYDim())
			p2.y = input.getYDim() - 1;
		int w = p2.x - p1.x + 1;
		int h = p2.y - p1.y + 1;
		if (w == input.getXDim() && h == input.getYDim())
			output = input.copyImage(true);
		else if (w <= 0 || h <= 0)
			output = input.copyImage(true);
		else {
			output = input.newInstance(w, h, input.getZDim(), input.getTDim(),
					input.getBDim());
			output.copyAttributes(input);
			for (int z = 0; z < output.getZDim(); z++)
				for (int t = 0; t < output.getTDim(); t++)
					for (int b = 0; b < output.getBDim(); b++)
						for (int x = 0; x < output.getXDim(); x++)
							for (int y = 0; y < output.getYDim(); y++)
									output.setPixelDouble(x, y, z, t, b, input
											.getPixelDouble(x + p1.x, y
													+ p1.y, z, t, b));
		}
	}

}
