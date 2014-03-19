package fr.unistra.pelican.util.morphology;

import java.awt.Point;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Describe a gray structuring element, extends IntegerImage so that morphological operation can be applied on it.
 * 
 * @author Benjamin Perret
 *	@deprecated
 */
public class GrayIntStructuringElement extends IntegerImage implements StructuringElement {
	
	/**
	 * Serialization Identifier
	 */
	private static final long serialVersionUID = -6511878719894263922L;
	
	/**
	 * Centre of StructuringElrment
	 */
	Point centre;
	
	/**
	 * Construct a new gray structuring element, all values to 0.
	 * Centre of SE will be set to (xDim/2,yDim/2)
	 * @param xDim Number of rows
	 * @param yDim Number of columns
	 */
	public GrayIntStructuringElement(int xDim, int yDim)
	{
		this(xDim,yDim,new Point(xDim/2,yDim/2));
	}
	
	/**
	 * Construct a new gray structuring element, all values to 0.
	 * @param xDim Number of rows
	 * @param yDim Number of columns
	 * @param centre Centre of SE
	 */
	public GrayIntStructuringElement(int xDim, int yDim, Point centre)
	{
		super(xDim,yDim,1,1,1);
		this.centre = centre;
	}
	
	/**
	 * Construct a new gray structuring element from an Image (copy dimensions and values)
	 * Centre of SE will be set to (xDim/2,yDim/2)
	 * @param im Image to copy
	 */
	public GrayIntStructuringElement(Image im)
	{
		this(im, new Point(im.getXDim()/2,im.getYDim()/2));
	}
	
	/**
	 * Construct a new gray structuring element from an Image (copy dimensions and values)
	 * Centre of SE will be set to (xDim/2,yDim/2)
	 * @param im Image to copy
	 * @param centre Centre of SE
	 */
	public GrayIntStructuringElement(Image im, Point centre)
	{
		super(im,true);
		this.centre = centre;
	}

	/**
	 * Return current centre
	 * @return centre
	 */
//	public Point getCenter() {
//		return centre;
//	}

	/**
	 * Set Centre
	 * @param centre
	 */
	public void setCenter(Point centre) {
		this.centre = centre;
	}

	
	public boolean isValue(int x, int y)
	{
		return getPixelXYInt(x,y)>0;
	}

	
}
