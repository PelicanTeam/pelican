package fr.unistra.pelican.util;
/**
 * Small class used to construct the gradient if a pixel
 * 
 * @author Dany DAMAJ
 */

public class Gradient {
	public static final int PI04 = 0; // 0
	public static final int PI14 = 1; // PI/4
	public static final int PI24 = 2; // PI/2
	public static final int PI34 = 3; // 3PI/4
	int x;
	int y;
	public double magnitude;
	public int direction;
	
	
	public Gradient(int x,int y,double magnitude,double gradient)
	{
		this.x=x;
		this.y=y;
		this.magnitude = magnitude;
		
		double tmp;
		// gradient must be between 0 and PI
		tmp = (gradient + Math.PI)%Math.PI;
		//approximation
		if(tmp >= Math.PI/8.0 && tmp < 3.0*Math.PI/8.0)
			direction = PI14;
		else if(tmp >= 3.0*Math.PI/8.0 && tmp < 5.0*Math.PI/8.0)
			direction = PI24;
		else if(tmp >= 5.0*Math.PI/8.0 && tmp < 7.0*Math.PI/8.0)
			direction = PI34;
		else direction = PI04;
	}
	
	public String toString()
	{
		return "point ("+x+","+y+") has magnitude "+magnitude+" and direction of gradient "+direction+"PI/4"; 
	}
	
}
