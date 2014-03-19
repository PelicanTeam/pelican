package fr.unistra.pelican.util.mask;

import java.awt.Point;
/**
 * Represent a mask composed of a single rectangle.
 * The rectangle can correspond to present or absent pixels
 * depending of the chosen option.
 * 
 * The rectangle is in xy coordinates and can apply either to all bands or only to a particular band.
 * 
 * z dim ant t dim are not considered
 * 
 * Rectangle mask cannot work with linear locations!
 * 
 * @author Benjamin Perret
 *
 */
public class RectangleMask implements Mask {

	/**
	 * Denote that mask applies in all bands
	 */
	public static final int ALL_CHANNELS=-1;
	
	/**
	 * Denote that pixels in the square are present
	 */
	public static final boolean IS_PRESENT=false;
	
	/**
	 * Denote that pixels in the square are not present
	 */
	public static final boolean IS_NOT_PRESENT=true;
	
	
	/**
	 * Up left corner
	 */
	private Point p1;
	
	/**
	 * Down right corner
	 */
	private Point p2;
	
	private boolean option=false;
	
	private int channel=ALL_CHANNELS;
	
	public RectangleMask(int x1, int y1, int x2, int y2,int channel, boolean option)
	{
		int minx=Math.min(x1,x2);
		int maxx=Math.max(x1,x2);
		int miny=Math.min(y1,y2);
		int maxy=Math.max(y1,y2);
		p1=new Point(minx,miny);
		p2=new Point(maxx,maxy);
		this.channel=channel;
		this.option=option;
	}
	
	public RectangleMask(Point p1, Point p2,int channel, boolean option)
	{
		this(p1.x,p1.y,p2.x,p2.y,channel,option);
	}
	

	public Mask cloneMask() {
		
		return new RectangleMask(p1,p2,channel,option);
	}

	
	public boolean isInMask(int loc) {
		System.err.println("Rectangle mask cannot work with linear locations!");
		return false;
	}

	public boolean isInMask(long loc) {
		System.err.println("Rectangle mask cannot work with linear locations!");
		return false;
	}
	
	public boolean isInMask(int x, int y, int z, int t, int b) {
		return ( ((channel==b || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
	}

	
	public boolean isInMaskXY(int x, int y) {
		return ( ((channel==0 || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
	}

	
	public boolean isInMaskXYB(int x, int y, int b) {
		return ( ((channel==b || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
	}

	
	public boolean isInMaskXYT(int x, int y, int t) {
		return ( ((channel==0 || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
	}

	
	public boolean isInMaskXYTB(int x, int y, int t, int b) {
		return ( ((channel==b || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
	}

	
	public boolean isInMaskXYZ(int x, int y, int z) {
		return ( ((channel==0 || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
	}

	
	public boolean isInMaskXYZB(int x, int y, int z, int b) {
		return ( ((channel==b || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
	}

	
	public boolean isInMaskXYZT(int x, int y, int z, int t) {
		return ( ((channel==0 || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
		}

	
	public boolean isInMaskXYZTB(int x, int y, int z, int t, int b) {
		return ( ((channel==b || channel==ALL_CHANNELS) && (x>=p1.x && x<=p2.x && y>=p1.y && y<=p2.y)) ^ option );
		}

}
