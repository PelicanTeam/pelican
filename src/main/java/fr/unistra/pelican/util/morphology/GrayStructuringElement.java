package fr.unistra.pelican.util.morphology;


import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Point4D;

/**
 * A grayscale structuring element;
 * to be used in conjunction with grayscale morphology
 * 
 * @author Abdullah
 * @deprecated
 */
public class GrayStructuringElement extends DoubleImage implements StructuringElement 
{	
	/**
	 * 
	 */
	public static final long serialVersionUID = 234524;
	
	// x.y as in image coordinates.
	// no more rows & columns confusion, enough is enough!
	protected Point center;
	
	protected ArrayList<Point[]> points = null;
	
	/*
	 * the value considered to be background..
	 */
	private static double INF = Double.NEGATIVE_INFINITY;

	
	
	
	/**
	 * copy constructor with no pixel cloning
	 * @param e the structuring element to clone.
	 */
	public GrayStructuringElement(GrayStructuringElement e)
	{
		super(e.getXDim(),e.getYDim(),1,1,1);
		this.center = (Point) e.getCenter().clone();
	}
	
	/**
	 * 
	 */
	public GrayStructuringElement copyImage(boolean copy)
	{
		return new GrayStructuringElement(this,copy);
	}
	
	/**
	 * 
	 * @param se
	 * @param copy
	 */
	public GrayStructuringElement(GrayStructuringElement se, boolean copy)
	{
		super(se);
		this.center = se.center;
		
		if(copy == true)
			this.setPixels((double[])se.getPixels().clone());
		else
			this.setPixels(new double[se.getXDim() * se.getYDim() * 1 * 1 * 1]);
	}

	/**
	 * @param xdim number of columns
	 * @param ydim number of rows
	 */
	public GrayStructuringElement(int xdim, int ydim)
	{
		super(xdim,ydim,1,1,1);
	}
	
	/**
	 * @param xdim number of columns
	 * @param ydim number of rows
	 * @param bdim number of channels
	 */
	public GrayStructuringElement(int xdim, int ydim,int bdim)
	{
		super(xdim,ydim,1,1,bdim);
	}

	/**
	 * @param xdim horizontal dimension
	 * @param ydim vertical dimension
	 * @param centre center of the structuring element.
	 */
	public GrayStructuringElement(int xdim, int ydim, Point centre)
	{
		super(xdim,ydim,1,1,1);
		this.center = (Point) centre.clone();
		this.setCenter(new Point4D(centre.x,centre.y,0,0));
	}
	
	/**
	 * @param xdim horizontal dimension
	 * @param ydim vertical dimension
	 * @param bdim number of channels
	 * @param centre center of the structuring element.
	 */
	public GrayStructuringElement(int xdim, int ydim, int bdim, Point centre)
	{
		super(xdim,ydim,1,1,bdim);
		this.center = (Point) centre.clone();
	}
	
	/**
	 * 
	 * @param img input image
	 * @param center center
	 */
	public GrayStructuringElement(Image img, Point center)
	{
		super(img);
		this.center = (Point) center.clone();
	}

	/**
	 * @param x
	 * @param y
	 * @return whether the pixel at the given location is non zero
	 */
	public boolean isValue(int x, int y)
	{
		return getPixelXYBDouble(x,y,0) > INF;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param b
	 * @return whether the pixel at the given location is non zero
	 */
	public boolean isValue(int x, int y,int b)
	{
		return getPixelXYBDouble(x,y,b) > INF;
	}
	
	
	/**
	 * Get pixel value with respect to center translation
	 * @param x
	 * @param y
	 * @return
	 */
	public double getCenteredValue(int x, int y)
	{
		return getPixelXYDouble(x+center.x, y+center.y);
	}

	/**
	 * Get pixel value
	 * @param x
	 * @param y
	 * @return
	 */
	public double getValue(int x, int y)
	{
		return getPixelXYDouble(x, y);
	}

	/**
	 * Get pixel value with respect to center translation
	 * @param x
	 * @param y
	 * @return
	 */
	public double getCenteredValue(int x, int y, int b)
	{
		return getPixelXYBDouble(x+center.x, y+center.y,b);
	}
	
	/**
	 * 
	 * @return the maximum value of this se
	 */
	public double getMaxValue()
	{
		double max = 0.0;
		
		for(int i = 0; i < size(); i++){
			double d = getPixelDouble(i);
			if (d > max) max = d;
		}
		
		return max;
	}

	/**
	 * @param center
	 */
	public void setCenter(Point center)
	{
		this.center = center;
	}

	/**
	 * @return the center
	 */
	//public Point getCenter()
	//{
	//	return center;
	//}

	/**
	 * Create a circle of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static GrayStructuringElement createCircleStructuringElement(int radius) {
		GrayStructuringElement se = new GrayStructuringElement(2 * radius + 1, 2 * radius + 1);
		se.resetCenter();
		se.center = new Point(radius,radius);
		se.fill(INF);
		for (int i = 0; i < 2 * radius + 1; i++) {
			for (int j = 0; j < 2 * radius + 1; j++) {
				if (Math.sqrt(Math.pow(i - radius, 2) + Math.pow(j - radius, 2)) <= radius + 0.000001)
					se.setPixelXYDouble(i, j, 1.0);
			}
		}
		return se;
	}
	
	/**
	 * Create a cone of radius radius  (plus the pixel at the cross) and height height.
	 * 
	 * @param radius
	 * @return
	 */
	public static GrayStructuringElement createConeStructuringElement(int radius, int height) {
		GrayStructuringElement se = new GrayStructuringElement(2 * radius + 1, 2 * radius + 1);
		se.resetCenter();
		se.fill(INF);
		for (int i = 0; i < 2 * radius + 1; i++) {
			for (int j = 0; j < 2 * radius + 1; j++) {
				double distFromCenter = Math.sqrt(Math.pow(i - radius, 2) + Math.pow(j - radius, 2));
				if (distFromCenter <= radius + 0.000001)
				{
					se.setPixelXYByte(i, j, (int) ((1-(distFromCenter/radius))*height));
				}
			}
		}
		return se;
	}
	
	/**
	 * Create a cone of radius radius  (plus the pixel at the cross) and height height.
	 * 
	 * @param radius
	 * @return
	 */
	public static GrayStructuringElement createConeToZeroStructuringElement(int radius, double slope) {
		GrayStructuringElement se = new GrayStructuringElement(2 * radius + 1, 2 * radius + 1);
		se.resetCenter();
		se.fill(INF);
		for (int i = 0; i < 2 * radius + 1; i++) {
			for (int j = 0; j < 2 * radius + 1; j++) {
				double distFromCenter = Math.sqrt(Math.pow(i - radius, 2) + Math.pow(j - radius, 2));
				if (distFromCenter <= radius + 0.000001)
				{
					se.setPixelXYDouble(i, j, -distFromCenter*slope);
				}
			}
		}
		//MViewer.exec(se);
		return se;
	}
	
	/**
	 * Create a vertical line of length length. The center is at length/2
	 * @param length length of se
	 * @return the resulting se
	 */
	public static GrayStructuringElement createVerticalLineFlatStructuringElement(int length)
	{
		GrayStructuringElement se = new GrayStructuringElement(1,length,new Point(0,length/2));
		se.fill(1.0);
		
		return se;
	}

	/** 
	 * Create a horizontal line of length length. The center is at length/2.
	 * @param length
	 * @return the resulting SE
	 */
	public static GrayStructuringElement createHorizontalLineGrayStructuringElement(int length)
	{
		GrayStructuringElement se = new GrayStructuringElement(length,1,new Point(length/2,0));
		se.resetCenter();
		se.fill(1.0);
		return se;
	}

	/**
	 *  Create a horizontal line of length length. The center is at center.
	 * @param length
	 * @param center
	 * @return the resulting SE
	 */
	public static GrayStructuringElement createHorizontalLineFlatStructuringElement(int length, Point center) {
		GrayStructuringElement se = new GrayStructuringElement(length,1,center);
		se.fill(1.0);
		return se;
	}

	/** 
	 * Create a cross line of radius radius (plus the pixel at the cross).
	 * @param radius
	 * @return the resulting SE
	 */
	public static GrayStructuringElement createCrossFlatStructuringElement(int radius)
	{
		GrayStructuringElement se = new GrayStructuringElement(2 * radius + 1,2 * radius + 1, new Point(radius, radius));
		
		for (int i = 0; i < 2 * radius + 1; i++){
			se.setPixelXYDouble(i, radius,1.0);
			se.setPixelXYDouble(radius, i,1.0);
		}
		
		return se;
	}

	/** Create a square of edge's length size. Center is at (size/2, size/2).
	 * 
	 * @param size
	 * @return se
	 */
	public static GrayStructuringElement createSquareFlatStructuringElement(int size)
	{
		GrayStructuringElement se = new GrayStructuringElement(size,size,new Point(size/2,size/2));
		se.fill(1.0);
		return se;
	}

	/** 
	 * Create a rectangle. Center is at (height/2, width/2).
	 * 
	 * @param xdim
	 * @param ydim
	 * 
	 * @return se the resulting se
	 */
	public static GrayStructuringElement createRectangularFlatStructuringElement(int xdim, int ydim)
	{
		GrayStructuringElement se = new GrayStructuringElement(xdim,ydim,new Point(xdim/2,ydim/2));
		se.fill(1.0);
		return se;
	}

	/**
	 * 
	 * @param se1
	 * @param se2
	 * @return whether the two SEs have the same centre.
	 */
	public static boolean haveSameCentre(GrayStructuringElement se1,GrayStructuringElement se2){
		return se1.getCenter().equals(se2.getCenter());

	}
	
	
	/**
	 * 
	 * @return the number of pixels strictement superior to INF
	 */
	public int[] numberOfActivePixels()
	{
		int[] tmp = new int[getBDim()];
		
		for(int b = 0; b < getBDim(); b++){
			for(int x = 0; x < getXDim(); x++){
				for(int y = 0 ; y < getYDim(); y++){
					if(isValue(x,y,b) == true) tmp[b]++;
				}
			}
		}
		
		return tmp;
	}
	
	
	
	/**
	 * Apply a central symmetry on SE
	 * SE(p)=SE(-p)
	 * @return reflected SE
	 */
	public GrayStructuringElement getReflection()
	{
		GrayStructuringElement se = new GrayStructuringElement(this,false);
		int xd=this.getXDim()-1;
		int yd=this.getYDim()-1;
		se.setCenter(new Point(xd-this.getCenter().x,yd-this.getCenter().y) );
		for(int j=0;j<=yd;j++)
			for(int i=0;i<=xd;i++)
			{
				se.setPixelXYDouble(xd-i,yd-j,this.getPixelXYDouble(i, j));
			}
		return se;
	}
	
	
	/**
	 * Get dual SE* from SE defined as 
	 * SE*(p) = -SE(p)
	 * 
	 * Support is also inverted
	 * 
	 * Caution: this definition differs from usual one : SE*(p)=-SE*(-p)
	 * @return dual from SE
	 */
	public GrayStructuringElement dual()
	{
		GrayStructuringElement se = new GrayStructuringElement(this,true);
		
		for(int i = 0; i < size(); i++)
			se.setPixelDouble(i,-1.0 * se.getPixelDouble(i));
		
		GrayStructuringElement.INF = -1.0;
		
		return se;
	}
	
	/**
	 * If the centre of the SE is set (=true) then it must be placed first!
	 * 
	 * @return an array of Points containing the coordinates of pixels set to true
	 */
	public ArrayList<Point[]> getPoints()
	{
		if(points != null) return points;
		
		int[] tmp = numberOfActivePixels();
		
		points = new ArrayList<Point[]>(getBDim());
		
		for(int b = 0; b < getBDim(); b++){
			Point[] p = new Point[tmp[b]];
			int k = 0;
			
			if(isValue(center.x,center.y,b) == true) p[k++] = new Point(center.x,center.y);
			
			for(int x = 0; x < getXDim(); x++){
				for(int y = 0 ; y < getYDim(); y++){
					
					if(isValue(x,y,b) == true && !(x == center.x && y == center.y))
						p[k++] = new Point(x,y);
				}
			}
			
			points.add(p);
		}
		
		return points;
	}

	/**
	 * Get the actual value representing the background (default value is 0)
	 * @return the iNF
	 */
	public static double getINF() {
		return GrayStructuringElement.INF;
	}

	/**
	 * Set the value representing the background
	 * @param inf the iNF to set
	 */
	public static void setINF(double inf) {
		GrayStructuringElement.INF = inf;
	}
	
	public GrayStructuringElement rotate(double degree) {

		double angleradian = Math.toRadians(degree);

		double xinput = this.getXDim();
		double yinput = this.getYDim();

		double tcos = Math.cos(-angleradian);
		double tsin = Math.sin(-angleradian);

		double atcos = Math.cos(angleradian);
		double atsin = Math.sin(angleradian);

		int xoutput = (int) Math.round(xinput * Math.abs(tcos) + yinput
				* Math.abs(tsin));
		int youtput = (int) Math.round(xinput * Math.abs(tsin) + yinput
				* Math.abs(tcos));
//		System.out.println(xoutput + " " + youtput + " ... "
//				+ (xinput * Math.abs(tcos) + yinput * Math.abs(tsin)) + " "
//				+ (xinput * Math.abs(tsin) + yinput * Math.abs(tcos)));

		int xm = this.getXDim() / 2;
		int ym = this.getYDim() / 2;

		int xmprime = xoutput / 2;
		int ymprime = youtput / 2;

		GrayStructuringElement se = new GrayStructuringElement(xoutput,
				youtput, new Point(xoutput / 2, youtput / 2));
		se.fill(INF);

		for (int x = 0; x < this.getXDim(); x++)
			for (int y = 0; y < this.getYDim(); y++) {
				int xprime = (int) Math.round((x - xm) * atcos - (y - ym)
						* atsin + xmprime);
				int yprime = (int) Math.round((x - xm) * atsin + (y - ym)
						* atcos + ymprime);

				if (xprime < 0) {
					xprime = 0;
				}
				if (xprime >= xoutput) {
					xprime = xoutput - 1;
				}

				if (yprime < 0) {
					yprime = 0;
				}
				if (yprime >= youtput) {
					yprime = youtput - 1;
				}

				se.setPixelXYDouble(xprime, yprime, this.getValue(x, y));

			}
		int centreX = (int) this.getCenter().getX();
		int centreY = (int) this.getCenter().getY();
		int centreXprime = (int) Math.round((centreX - xm) * atcos
				- (centreY - ym) * atsin + xmprime);
		int centreYprime = (int) Math.round((centreX - xm) * atsin
				+ (centreY - ym) * atcos + ymprime);
		if (centreXprime < 0) {
			centreXprime = 0;
		}
		if (centreXprime >= xoutput) {
			centreXprime = xoutput - 1;
		}

		if (centreYprime < 0) {
			centreYprime = 0;
		}
		if (centreYprime >= youtput) {
			centreYprime = youtput - 1;
		}

		se.setCenter(new Point(centreXprime, centreYprime));
		se.setCenter(new Point4D(centreXprime,centreYprime,0,0));
		return se;
	}
	
	/**
	 * Reset the center of the gse to the central position 
	 */
	public void resetCenter() 
	{
		super.resetCenter();
		this.center=new Point(xdim/2,ydim/2);
		
	}
}