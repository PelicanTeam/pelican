package fr.unistra.pelican.util;

/**
 * Class to represent video points (x,y,t)
 * @author Jonathan Weber
 *
 */

public 	class PointVideo {
		public int x;
		public int y;
		public int t;
		public int index;

		public PointVideo() {
			x = 0;
			y = 0;
			t = 0;
		}

		public PointVideo(int x, int y, int t) {
			this.x = x;
			this.y = y;
			this.t = t;
		}
		
		public PointVideo(int x, int y, int t, int index) {
			this.x = x;
			this.y = y;
			this.t = t;
			this.index=index;
		}
		
		public PointVideo(PointVideo p) {
			x = p.x;
			y = p.y;
			t = p.t;
			index=p.index;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			boolean res = false;
			if (obj != null && obj instanceof PointVideo) {
				res = this.equals((PointVideo) obj);
			}
			return res;
		}
	
		public boolean equals (PointVideo p)
		{
			return p.x == x && p.y == y && p.t == t;
		}

		public int hashCode(){
			return x << 16 + y << 8 + t; 
		}
		
		public PointVideo getLocation() {
			return new PointVideo(this);
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getT() {
			return t;
		}

		public void move(int x, int y, int t) {
			this.x = x;
			this.y = y;
			this.t = t;
		}

		public void setLocation(int x, int y, int t) {
			this.x = x;
			this.y = y;
			this.t = t;
		}

		public void setLocation(double x, double y, double t) {
			this.x = (int) x;
			this.y = (int) y;
			this.t = (int) t;
		}

		public void setLocation(PointVideo p) {
			x = p.x;
			y = p.y;
			t = p.t;
		}

		public String toString() {
			return "PointVideo (" + x + "," + y + "," + t + ")";
		}

		public void translate(int x, int y, int t) {
			this.x += x;
			this.y += y;
			this.t += t;
		}
		
		public void setIndex(int xDim, int yDim, int tDim)
		{
			index= x + xDim * ( y + yDim * t );
		}
		
		public void setIndex(int xDim, int yDim, int tDim, int bDim)
		{
			index=bDim * ( x + xDim * ( y + yDim * t ));
		}

		public Object clone() {
			return new PointVideo(this);
		}
	}


