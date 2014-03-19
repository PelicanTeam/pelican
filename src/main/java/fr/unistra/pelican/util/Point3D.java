package fr.unistra.pelican.util;

/**
 * Class to represent 3-D points
 * @author lefevre
 *
 */

public 	class Point3D {
		public int x;
		public int y;
		public int z;

		public Point3D() {
			x = 0;
			y = 0;
			z = 0;
		}

		public Point3D(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Point3D(int x, int y) {
			this(x,y,0);
		}

		public Point3D(Point3D p) {
			x = p.x;
			y = p.y;
			z = p.z;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
	public boolean equals(Object obj) {
		boolean res = false;
		if (obj != null && obj instanceof Point3D) {
			Point3D p = (Point3D) obj;
			res = p.x == x && p.y == y && p.z == z;
		}
		return res;
	}

		public int hashCode(){
			return x << 16 + y << 8 + z; 
		}
		
		public Point3D getLocation() {
			return new Point3D(this);
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

		public void move(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setLocation(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setLocation(double x, double y, double z) {
			this.x = (int) x;
			this.y = (int) y;
			this.z = (int) z;
		}

		public void setLocation(Point3D p) {
			x = p.x;
			y = p.y;
			z = p.z;
		}

		public String toString() {
			return "Point3D (" + x + "," + y + "," + z + ")";
		}

		public void translate(int x, int y, int z) {
			this.x += x;
			this.y += y;
			this.z += z;
		}

		public Object clone() {
			return new Point3D(this);
		}
		
		public boolean equals(Point3D p)
		{
			return (this.x==p.x&&this.y==p.y&&this.z==p.z);
		}

	}

