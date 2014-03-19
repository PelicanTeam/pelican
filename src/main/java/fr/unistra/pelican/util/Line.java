
package fr.unistra.pelican.util;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.visualisation.MViewer;



/**
 * @author Benjamin Perret
 * This Line represents a line segment in (x, y) coordinate space.
 * It provides an iterator on its discreet representation (Bresenham algorithm.)
 */
public class Line implements Iterator<Point>, Iterable<Point>{
	/* type */
	final static int VECT_HG = 0;
	final static int VECT_HD = 1;
	final static int VECT_VH = 2;
	final static int VECT_VB = 3;
	final static int OCTANT1 = 4;
	final static int OCTANT2 = 5;
	final static int OCTANT3 = 6;
	final static int OCTANT4 = 7;
	final static int OCTANT5 = 8;
	final static int OCTANT6 = 9;
	final static int OCTANT7 = 10;
	final static int OCTANT8 = 11;
	
	/* descibre line by two points */
	private Point x1,x2;
	
	/* iterator flag */
	boolean first=true;
	
	/* this is for Bresenham algorithm implementation*/
	private int type;
	private int x,y;
	private int incr1,incr2;
	private int dx,dy;
	private int d;
	
	public Line (int x1, int y1, int x2, int y2)
	{
		this(new Point(x1,y1), new Point(x2,y2));
	}
	
	/* Construct new segment [x1;x2] */
	public Line (Point x1, Point x2)
	{ 
		this.x1=new Point(x1);
		this.x2=new Point(x2);
		x=this.x1.x;
		y=this.x1.y;
		dx=this.x2.x-x;
		dy=this.x2.y-y;
		incr1=2*dx;
		incr2=2*dy;
		
		
		if (dx != 0) 
		  {
		    if (dx > 0) 
		    {
		      if (dy != 0)
		      {
		        if (dy > 0)
		        {
		        	// vecteur oblique dans le 1er quadran 
		        	if (dx >= dy)
		        	{// vecteur diagonal ou oblique proche de l horizontale, dans le 1er octant
		        		type=OCTANT1;
		        		d=dx;
		        	}
		        	else {
		        		// vecteur oblique proche de la verticale, dans le 2nd octant
		        		d=dy;
		        		type=OCTANT2;
		        	}
		        }	          
		        else { // dy < 0 (et dx > 0)
		          // vecteur oblique dans le 4e cadran
		     
		         if ( dx >= -dy)
		         {
		            // vecteur diagonal ou oblique proche de l horizontale, dans le 8e octant
		        	 d=dx;
		        	 type=OCTANT8;
		         }
		         else	{  // vecteur oblique proche de la verticale, dans le 7e octant
		            d=dy;
		            type=OCTANT7;
		         }      
		        }
		      }
		      else {  // dy = 0 (et dx > 0)	        
		        // vecteur horizontal vers la droite
		        type=VECT_HD;	        
		      }
		    }
		    else  // dx < 0
		      if (dy != 0) 
		      {
		        if (dy > 0) 
		        {
		          // vecteur oblique dans le 2nd quadran
		          if( -dx >= dy) 
		          {
		            // vecteur diagonal ou oblique proche de l horizontale, dans le 4e octant
		        	  d=dx;
		        	  type=OCTANT4;
		          }
		          else {
		            // vecteur oblique proche de la verticale, dans le 3e octant
		            d=dy;
		            type=OCTANT3;
		          }
		        }
		        else  // dy < 0 (et dx < 0)
		        {
		          // vecteur oblique dans le 3e cadran
		          
		          if ( dx <= dy ) 
		          {
		            // vecteur diagonal ou oblique proche de l horizontale, dans le 5e octant
		            d=dx;
		            type=OCTANT5;
		          }
		          else {  // vecteur oblique proche de la verticale, dans le 6e octant
		            d=dy;
		            type=OCTANT6;
		          }      
		        }
		      }
		      else
		      {// dy = 0 (et dx < 0)
		        type=VECT_HG;
		        // vecteur horizontal vers la gauche
		      }		        		   
		  }
		else { // dx = 0
		    if (dy != 0) 
		    {
		      if ( dy > 0 )
		      {
		    	  type=VECT_VH;		        
		      }
		      else  // dy < 0 (et dx = 0)
		      {
		        // vecteur vertical decroissant*
		    	  type=VECT_VB;	
		      }
		    }
		  	}	
	
	}
	
	/*
	 * Returns the starting Point of this Line.
	 */	
	public Point getX1()
	{
		return x1;
	}
	/*
	 * Returns the ending Point of this Line.
	 */	
	public Point getX2()
	{
		return x2;
	}
	
	/*
	 * Initialise Iterator
	 */
	public void initIterator()
	{
		first=true;
		x=x1.x;
		y=x1.y;
	}
	
	/*
	 * To get the next point on Breseham representation.
	 */
	public Point next()
	{
		if(!first)
		switch(type)
		{
		case VECT_HG:
			x--;
			break;
		case VECT_HD:
			x++;
			break;
		case VECT_VH:
			y++;
			break;
		case VECT_VB:
			y--;
			break;
		case OCTANT1:
			x++;
			d=d-incr2;
			if( d < 0 )
			{
				y++;
				d+=incr1;
			}
			break;
		case OCTANT2:
			y++;
			d=d-incr1;
			if( d < 0 )
			{
				x++;
				d+=incr2;
			}
			break;
		case OCTANT3:
			y++;
			d=d+incr1;
			if( d <= 0 )
			{
				x--;
				d+=incr2;
			}
			break;
		case OCTANT4:
			x--;
			d=d+incr2;
			if( d >=0 )
			{
				y++;
				d+=incr1;
			}
			break;
		case OCTANT5:
			x--;
			d=d-incr2;
			if( d >=0 )
			{
				y--;
				d+=incr1;
			}
			break;
		case OCTANT6:
			y--;
			d=d-incr1;
			if( d >=0 )
			{
				x--;
				d+=incr2;
			}
			break;
		case OCTANT7:
			y--;
			d=d+incr1;
			if( d > 0 )
			{
				x++;
				d+=incr2;
			}
			break;
		case OCTANT8:
			x++;
			d=d+incr2;
			if( d < 0 )
			{
				y--;
				d+=incr1;
			}
			break;
		}
		else first=false;
		return (new Point(x,y));
	}
	 
	
	/**
	 * To ensure iterator still has a Point to return.
	 */
	public boolean hasNext()
	{
		boolean res= true;
		if ( x==x2.x && y == x2.y ) res=false;
		return res;
	}
	
	/**
	 * Draw the line segment on an image (single band) pixels are set to 255
	 * @param im
	 */
	public void drawGrayLine(Image im)
	{
		drawGrayLine(im, 255);
	}
	
	/**
	 * Draw the line segment on an image (single band) pixels are set to level
	 * @param im
	 * @param level
	 */
	public void drawGrayLine(Image im, int level)
	{
		initIterator();
		int xDim=im.getXDim();
		int yDim=im.getYDim();
		if(x>=0 && x<xDim && y>=0 && y<yDim)
		im.setPixelXYByte(x,y,level);
		while(hasNext())
		{
			Point p=next();
			if(p.x>=0 && p.x<xDim && p.y>=0 && p.y<yDim)
			{
			im.setPixelXYByte(p.x,p.y,level);
			}
		}
	}
	
	/**
	 * Draw the line segment on an image (single band) pixels are set to level
	 * @param im
	 * @param band number of band to draw in
	 * @param level
	 */
	public void drawGrayLine(Image im,int band, int level)
	{
		initIterator();
		int xDim=im.getXDim();
		int yDim=im.getYDim();
		if(x>=0 && x<xDim && y>=0 && y<yDim)
			im.setPixelXYBByte(x,y,band,level);
		while(hasNext())
		{
			Point p=next();
			if(p.x>=0 && p.x<xDim && p.y>=0 && p.y<yDim)
			{
			im.setPixelXYBByte(p.x,p.y,band,level);
			}
		}
	}
	
	/**
	 * Draw the line segment on an image (single band) pixels are set to level
	 * @param im
	 * @param band number of band to draw in
	 * @param level
	 */
	public void drawGrayLine(Image im,int band, double level)
	{
		initIterator();
		int xDim=im.getXDim();
		int yDim=im.getYDim();
		if(x>=0 && x<xDim && y>=0 && y<yDim)
			im.setPixelXYBDouble(x,y,band,level);
		while(hasNext())
		{
			Point p=next();
			if(p.x>=0 && p.x<xDim && p.y>=0 && p.y<yDim)
			{
			im.setPixelXYBDouble(p.x,p.y,band,level);
			}
		}
	}
	public void drawColorLine(Image im,Color c)
	{
		initIterator();
		//im.setPixelXYByte(x,y,255);
		int xDim=im.getXDim();
		int yDim=im.getYDim();
		while(hasNext())
		{
			Point p=next();
			if(p.x>=0 && p.x<xDim && p.y>=0 && p.y<yDim)
			{
			im.setPixelXYBByte(p.x,p.y,0,c.getRed());
			im.setPixelXYBByte(p.x,p.y,1,c.getGreen());
			im.setPixelXYBByte(p.x,p.y,2,c.getBlue());
			}
		}
	}
	
	/**
	 * Draw the line segment on an image (single band) pixels values are interpolate between level1 and 2 
	 * @param im
	 * @param band number of band to draw in
	 * @param level1
	 * @param level2
	 */
	public void drawLine(Image im,int band, double level1, double level2)
	{
		
		int xDim=im.xdim;
		int yDim=im.ydim;
		double l=length();
		double counter=0.0;
		double s=1.0/l;
		
		//if(x>=0 && x<xDim && y>=0 && y<yDim)
		//	im.setPixelXYBDouble(x,y,band,level1);
		initIterator();
		while(hasNext())
		{
			Point p=next();
			//System.out.println("draw " +p);
			if(p.x>=0 && p.x<xDim && p.y>=0 && p.y<yDim)
				im.setPixelXYBDouble(p.x,p.y,band,(level1*(1.0-s)+level2*s));
			
			counter+=s;
		}
	}
	
	/**
	 * Draw the line segment on an image (single band) pixels values are interpolate between level1 and 2 
	 * @param im
	 * @param band number of band to draw in
	 * @param level1
	 * @param level2
	 */
	public void drawLineMin(Image im,int band, double level1, double level2)
	{
		
		int xDim=im.xdim;
		int yDim=im.ydim;
		double l=length();
		double counter=0.0;
		double s=1.0/l;
		
		//if(x>=0 && x<xDim && y>=0 && y<yDim)
		//	im.setPixelXYBDouble(x,y,band,level1);
		initIterator();
		while(hasNext())
		{
			Point p=next();
			//System.out.println("draw " +p);
			if(p.x>=0 && p.x<xDim && p.y>=0 && p.y<yDim)
				im.setPixelXYBDouble(p.x,p.y,band,Math.min(im.getPixelXYBDouble(p.x,p.y,band), (level1*(1.0-s)+level2*s)));
			
			counter+=s;
		}
	}
	
	/**
	 * Compute intersections with the line passing through p1 and p2 and a box, mais be subject to rounding errors...
	 * 
	 * @param box
	 * @return
	 */
	public Point [] intersections(Box box)
	{
		double a=((double)(x1.y-x2.y))/((double)(x1.x-x2.x));
		double b=(double)x1.y-a*(double)x1.x;
		double x1=box.x1.x;
		double x2=box.x2.x;
		double y1=box.x1.y;
		double y2=box.x2.y;
		if(x1>x2)
		{
			double tmp=x1;
			x1=x2;
			x2=tmp;
		}
		if(y1>y2)
		{
			double tmp=y1;
			y1=y2;
			y2=tmp;
		}
		ArrayList<Point> list=new ArrayList<Point>();
		// first segment y=y1, x1<=x<=x2 
		double xx=(y1-b)/a;
		if(x1<=xx && xx<=x2)
			list.add(new Point((int)(xx+0.5),(int)y1));
		
		// second segment y=y2, x1<=x<=x2 
		xx=(y2-b)/a;
		if(x1<=xx && xx<=x2)
			list.add(new Point((int)(xx+0.5),(int)y2));
		
		// third segment x=x1, y1<=y<=y2 
		double yy=a*x1+b;
		if(y1<yy && yy<y2)
			list.add(new Point((int)(x1),(int)(yy+0.5)));
		
		// fourth segment x=x2, y1<=y<=y2 
		yy=a*x2+b;
		if(y1<yy && yy<y2)
			list.add(new Point((int)(x2),(int)(yy+0.5)));
		
		return list.toArray(new Point[list.size()]);
		
	}
	
	/**
	 * Test if the segment intersects a box.
	 * @param box
	 * @return
	 */
	public boolean intersects(Box box)
	{
		// Find min and max X for the segment

		double minX=Math.min(x1.x, x2.x);
		double maxX=Math.max(x1.x, x2.x);;

	

		double bMaxX=Math.max(box.x1.x,box.x2.x );
		double bMinX=Math.min(box.x1.x,box.x2.x );
		double bMaxY=Math.max(box.x1.y,box.x2.y );
		double bMinY=Math.min(box.x1.y,box.x2.y );
		
		// Find the intersection of the segment's and rectangle's x-projections
		if(maxX > bMaxX)
		{
			maxX = bMaxX;
		}

		if(minX < bMinX)
		{
			minX = bMinX;
		}

		if(minX > maxX) // If their projections do not intersect return false
		{
			return false;
		}

		// Find corresponding min and max Y for min and max X we found before

		double minY = x1.y;
		double maxY = x2.y;

		double dx = x2.x - x1.x;

		if(Math.abs(dx) > 0.0000001)
		{
			double a = (x2.y - x1.y) / dx;
			double b = x1.y - a * x1.x;
			minY = a * minX + b;
			maxY = a * maxX + b;
		}

		if(minY > maxY)
		{
			double tmp = maxY;
			maxY = minY;
			minY = tmp;
		}

		// Find the intersection of the segment's and rectangle's y-projections

		if(maxY > bMaxY)
		{
			maxY = bMaxY;
		}

		if(minY < bMinY)
		{
			minY = bMinY;
		}

		if(minY > maxY) // If Y-projections do not intersect return false
		{
			return false;
		}

		return true;
	}

	
	
	public String toString()
	{
		return ("Line["+x1+";" +x2+"]");
	}



	/**
	 * Not implemented
	 */
	public void remove() {
		
	}

	
	public Iterator<Point> iterator() {
		initIterator();
		return this;
	}
	
	
	public int length()
	{
		int c=0;
		initIterator();
		while(hasNext())
		{
			c++;
			next();
		}
		return c;
	}
	
	
	
	
	public double[] imProfileDouble (Image im)
	{
		double[] tab=new double[length()];
		initIterator();
		int c=0;
		
		while(hasNext())
		{
			Point p=next();
			
			tab[c++]=im.getPixelXYDouble(p.x, p.y);
		}
		return tab;
	}
	
	public double[] imProfileDouble (Image im, int band)
	{
		double[] tab=new double[length()];
		initIterator();
		int c=0;
		int xDim=im.xdim;
		int yDim=im.ydim;
		while(hasNext())
		{
			Point p=next();
			if(p.x>=0 && p.x<xDim && p.y>=0 && p.y<yDim)
			{
				tab[c]=im.getPixelXYBDouble(p.x, p.y,band);
			}
			c++;
		}
		return tab;
	}
	
	public int[] imProfileInt (Image im)
	{
		int[] tab=new int[length()];
		initIterator();
		int c=0;
		
		while(hasNext())
		{
			Point p=next();
			tab[c++]=im.getPixelXYInt(p.x, p.y);
		}
		return tab;
	}
	
	
	public static void main(String [] args){
		Image im=new DoubleImage(300,300,1,1,1);
		Box b=new Box(60,60,200,200);
		Line l1=new Line(0,0,250,250);
		Line l2=new Line(100,100,150,100);
		Line l3=new Line(50,50,250,50);
		b.drawGrayRectangle(im);
		l1.drawGrayLine(im, 100);
		l2.drawGrayLine(im, 150);
		l3.drawGrayLine(im, 200);
		Point [] p1=l1.intersections(b);
		Point [] p2=l2.intersections(b);
		Point [] p3=l3.intersections(b);
		
		System.out.println("l1 : " + p1.length + " int? " + l1.intersects(b));
		System.out.println("l2 : " + p2.length + " int? " + l2.intersects(b));
		System.out.println("l3 : " + p3.length + " int? " + l3.intersects(b));
		MViewer.exec(im);
	}
	
}
