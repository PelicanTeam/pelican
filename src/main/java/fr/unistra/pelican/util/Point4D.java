package fr.unistra.pelican.util;

import java.awt.Point;
import java.io.Serializable;

/**
 * Class to represent 4-D points
 * 
 * @author lefevre
 * 
 */

public class Point4D implements Comparable, Serializable {

	private static final long serialVersionUID = 4898822727089299555L;

	public int x;
	public int y;
	public int z;
	public int t;
	public int index;

	public Point4D() {
		x = 0;
		y = 0;
		z = 0;
		t = 0;
	}

	public Point4D(int x, int y, int z, int t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
	}
	
	public Point4D(int x, int y, int z, int t, int index) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
		this.index=index;
	}
	
	public Point4D(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point4D(int x, int y) {
		this.x = x;
		this.y = y;
		this.z = 0;
		this.t = 0;
	}

	public Point4D(Point4D p) {
		x = p.x;
		y = p.y;
		z = p.z;
		t = p.t;
		index = p.index;
	}

	public Point4D(Point p) {
		x = p.x;
		y = p.y;
		z = 0;
		t = 0;
	}

	public Point getPoint2D() {
		return new Point(x, y);
	}

	public boolean equals(Object obj) {
		return ((Point4D) obj).x == x && ((Point4D) obj).y == y
			&& ((Point4D) obj).z == z && ((Point4D) obj).t == t;
	}

	public Point4D getLocation() {
		return new Point4D(this);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public double getT() {
		return t;
	}

	public void move(int x, int y, int z, int t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
	}

	public void setLocation(int x, int y, int z, int t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
	}

	public void setLocation(double x, double y, double z, double t) {
		this.x = (int) x;
		this.y = (int) y;
		this.z = (int) z;
		this.t = (int) t;
	}

	public void setLocation(Point4D p) {
		x = p.x;
		y = p.y;
		z = p.z;
		t = p.t;
	}

	public String toString() {
		return "Point4D (" + x + "," + y + "," + z + "," + t + ")";
	}

	public void translate(int x, int y, int z, int t) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.t += t;
	}

	public Object clone() {
		return new Point4D(this);
	}

	public double distanceSq(Point4D p) {
		double d = 0;
		d += (p.x - x) * (p.x - x);
		d += (p.y - y) * (p.y - y);
		d += (p.z - z) * (p.z - z);
		d += (p.t - t) * (p.t - t);
		return d;
	}

	public double distance(Point4D p) 
	{
		return Math.sqrt(distanceSq(p));
	}

	public int compareTo(Object o) 
	{
		Point4D p = (Point4D) o;
		if (x != p.x)
			return x - p.x;
		if (y != p.y)
			return y - p.y;
		if (z != p.z)
			return z - p.z;
		return t - p.t;
	}
	
	public boolean equals(Point4D p)
	{
		return (this.x==p.x&&this.y==p.y&&this.z==p.z&&this.t==p.t);
	}

	public void setIndex(int xDim, int yDim, int zDim, int tDim)
		{
			index= x + xDim * ( y + yDim * (z + zDim * t));
		}
		
		public void setIndex(int xDim, int yDim, int zDim, int tDim, int bDim)
		{
			index=bDim * ( x + xDim * ( y + yDim * (z + zDim * t)));
		}

}
