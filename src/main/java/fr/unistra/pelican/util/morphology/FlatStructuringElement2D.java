package fr.unistra.pelican.util.morphology;

import java.awt.Point;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point4D;

/**
 * Utility class to create images representing structuring elements
 * 
 * @author lefevre
 * 
 */
public class FlatStructuringElement2D {

	/**
	 * 
	 */
	public static final long serialVersionUID = 234523;

	// present and future bug source:
	// the centre is kept as X,Y being : COLUMS, ROWS.
	// Careful with the transformations
	// TODO E.A : we should use a single standard...this is far too messy.

	/**
	 * Create a horizontal line of length length. The center is at length/2.
	 * 
	 * @param length
	 * @param center
	 * @return
	 */
	public static BooleanImage createHorizontalLineFlatStructuringElement(
		int length) {
		BooleanImage se = new BooleanImage(length, 1, 1, 1, 1);
		se.resetCenter();
		se.fill(true);
		return se;
	}

	/**
	 * Create a vertical line of length length. The center is at length/2
	 * 
	 * @param length
	 * @return a vertical line shaped SE
	 */
	public static BooleanImage createVerticalLineFlatStructuringElement(int length) {
		BooleanImage se = new BooleanImage(1, length, 1, 1, 1);
		se.resetCenter();
		se.fill(true);
		return se;
	}
	
	/**
	 * Create a vertical line of length length. The center is at length/2
	 * 
	 * @param length
	 * @param center
	 * @return a vertical line shaped SE
	 */
	public static BooleanImage createVerticalLineFlatStructuringElement(int length, Point center) {
		BooleanImage se = new BooleanImage(1, length, 1, 1, 1);
		se.setCenter(center);
		se.fill(true);
		return se;
	}

	/**
	 * Create a left diagonal line of length length (odd). The center is at length /
	 * 2
	 * 
	 * @param length
	 * @return a left diagonal shaped SE
	 */
	public static BooleanImage createLeftDiagonalLineFlatStructuringElement(
		int length) {
		BooleanImage se = new BooleanImage(length, length, 1, 1, 1);
		se.resetCenter();
		for (int i = 0; i < length; i++)
			se.setPixelXYBoolean(i, i, true);
		return se;
	}

	/**
	 * Create a right diagonal line of length length (odd). The center is at
	 * length / 2
	 * 
	 * @param length
	 * @return
	 */
	public static BooleanImage createRightDiagonalLineFlatStructuringElement(
		int length) {
		BooleanImage se = new BooleanImage(length, length, 1, 1, 1);
		se.resetCenter();
		for (int i = 0; i < length; i++)
			se.setPixelXYBoolean(i, length - 1 - i, true);
		return se;
	}

	/**
	 * Create a line of length length of a given orientation (in degrees)
	 * 
	 * @param length
	 * @param orientation
	 * @return
	 */
	public static BooleanImage createLineFlatStructuringElement(int length,
		double orientation) {
		double cos = Math.cos(Math.toRadians(orientation));
		double sin = Math.sin(Math.toRadians(orientation));
		double abscos = Math.abs(cos);
		double abssin = Math.abs(sin);
		double epsilon = 0.0000001;
		double sumcos = abscos;
		double sumsin = abssin;
		int i = 0, j = 0;
		int center = (length - 1) / 2;
		BooleanImage se = new BooleanImage(length, length, 1, 1, 1);
		se.setCenter(new Point(center, center));
		se.setPixelXYBoolean(center, center, true);
		for (int l = 1; l <= center; l++) {
			if (sumcos + epsilon >= sumsin) {
				i = cos > 0 ? i + 1 : i - 1;
				sumsin += abssin;
			}
			if (sumsin + epsilon >= sumcos) {
				j = sin > 0 ? j + 1 : j - 1;
				sumcos += abscos;
			}
			se.setPixelXYBoolean(center + i, center + j, true);
			se.setPixelXYBoolean(center - i, center - j, true);
		}
		return se;
	}

	/**
	 * Create a horizontal line of length length. The center is at center.
	 * 
	 * @param length
	 * @param center
	 * @return
	 */
	public static BooleanImage createHorizontalLineFlatStructuringElement(
		int length, Point center) {
		BooleanImage se = new BooleanImage(length, 1, 1, 1, 1);
		se.setCenter(center);
		se.fill(true);
		return se;
	}

	/**
	 * Create a cross line of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static BooleanImage createCrossFlatStructuringElement(int radius) {
		BooleanImage se = new BooleanImage(2 * radius + 1, 2 * radius + 1, 1, 1, 1);
		se.resetCenter();
		for (int i = 0; i < 2 * radius + 1; i++) {
			se.setPixelXYBoolean(i, radius, true);
			se.setPixelXYBoolean(radius, i, true);
		}
		return se;
	}

	/**
	 * Create a circle of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static BooleanImage createCircleFlatStructuringElement(int radius) {
		BooleanImage se = new BooleanImage(2 * radius + 1, 2 * radius + 1, 1, 1, 1);
		se.resetCenter();
		for (int i = 0; i < 2 * radius + 1; i++) {
			for (int j = 0; j < 2 * radius + 1; j++) {
				if (Math.sqrt(Math.pow(i - radius, 2) + Math.pow(j - radius, 2)) <= radius + 0.000001)
					se.setPixelXYBoolean(i, j, true);
			}
		}
		return se;
	}
	
	/**
	 * Create a empty circle of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static BooleanImage createEmptyCircleFlatStructuringElement(int radius) {
		BooleanImage se = new BooleanImage(2 * radius + 1, 2 * radius + 1, 1, 1, 1);
		se.resetCenter();
		for (int i = 0; i < 2 * radius + 1; i++) {
			for (int j = 0; j < 2 * radius + 1; j++) {
				if (Math.rint(Math.sqrt(Math.pow(i - radius, 2) + Math.pow(j - radius, 2))) == radius)
					se.setPixelXYBoolean(i, j, true);
			}
		}
		return se;
	}

	/**
	 * @return The transpose of the structuring element.
	 */
	public static BooleanImage transpose(BooleanImage se) {
		BooleanImage res = new BooleanImage(se.ydim, se.xdim, se.zdim, se.tdim,
			se.bdim);
		for (int j = 0; j < res.ydim; j++)
			for (int i = 0; i < res.xdim; i++)
				res.setPixelXYBoolean(i, j, se.getPixelXYBoolean(j, i));
		Point4D p = se.getCenter();
		res.setCenter(new Point4D(p.y, p.x, p.z, p.t));
		return res;
	}

	public static BooleanImage reflect(BooleanImage se) {
		int xdim = se.getXDim();
		int ydim = se.getYDim();
		int zdim = se.getZDim();
		int tdim = se.getTDim();
		int bdim = se.getBDim();
		BooleanImage res = new BooleanImage(se);
		for (int b = 0; b < bdim; b++)
			for (int t = 0; t < tdim; t++)
				for (int z = 0; z < zdim; z++)
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++)
								res.setPixelXYZTBBoolean(x, y, z, t, b, se
									.getPixelXYZTBBoolean(xdim - 1 - x, ydim - 1 - y, zdim - 1
										- z, tdim - 1 - t, b));
		Point4D p = se.getCenter();
		res.setCenter(new Point4D(xdim - 1 - p.x, ydim - 1 - p.y, zdim - 1 - p.z,
			tdim - 1 - p.t));
		return res;
	}

	/**
	 * Perform a rotation of the se around its middle (spatial center)
	 * 
	 * @param se
	 *          the input SE
	 * @param degree
	 *          the angle of rotation in degrees
	 * @return the rotated SE
	 */
	public static BooleanImage rotateMiddle(BooleanImage se, double degree) {
		if(degree==180)
			System.out.println("ok");
		double angleradian = Math.toRadians(degree);
		double xinput = se.xdim;
		double yinput = se.ydim;
		double tcos = Math.cos(-angleradian);
		double tsin = Math.sin(-angleradian);
		double atcos = Math.cos(angleradian);
		double atsin = Math.sin(angleradian);
		int xoutput = (int) Math.round(xinput * Math.abs(tcos) + yinput
			* Math.abs(tsin));
		int youtput = (int) Math.round(xinput * Math.abs(tsin) + yinput
			* Math.abs(tcos));
		// System.out.println(xoutput + " " + youtput + " ... "
		// + (xinput * Math.abs(tcos) + yinput * Math.abs(tsin)) + " "
		// + (xinput * Math.abs(tsin) + yinput * Math.abs(tcos)));
		int xm = (int) (xinput / 2);
		int ym = (int) (yinput / 2);
		int xmprime = xoutput / 2;
		int ymprime = youtput / 2;
		BooleanImage res = new BooleanImage(xoutput, youtput, 1, 1, 1);
		//res.setCenter(new Point(xoutput / 2, youtput / 2));
		res.fill(false);
		for (int x = 0; x < se.xdim; x++)
			for (int y = 0; y < se.ydim; y++) {
				int xprime = (int) Math.round((x - xm) * atcos - (y - ym) * atsin
					+ xmprime);
				int yprime = (int) Math.round((x - xm) * atsin + (y - ym) * atcos
					+ ymprime);
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
				res.setPixelXYBoolean(xprime, yprime, se.getPixelXYBoolean(x, y));
			}
		int centreX = (int) se.getCenter().getX();
		int centreY = (int) se.getCenter().getY();
		int centreXprime = (int) Math.round((centreX - xm) * atcos - (centreY - ym)
			* atsin + xmprime);
		int centreYprime = (int) Math.round((centreX - xm) * atsin + (centreY - ym)
			* atcos + ymprime);
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
		res.setCenter(new Point(centreXprime, centreYprime));
	//	System.out.println(res.getCenter());
		return res;
	}

	/**
	 * Perform a rotation of the SE around its center (not necessarily in the middle)
	 * 
	 * @param se
	 *          the input SE
	 * @param degree
	 *          the angle of rotation in degrees
	 * @return the rotated SE
	 */
	public static BooleanImage rotate(BooleanImage se, double degree) {
		double angleradian = Math.toRadians(degree);
		double atcos = Math.cos(angleradian);
		double atsin = Math.sin(angleradian);
		Point4D[] fg=se.foreground();
		int xm = (int) se.getCenter().getX();
		int ym = (int) se.getCenter().getY();
		int minx=xm;
		int miny=ym;
		int maxx=xm;
		int maxy=ym;
		for (int i=0;i<fg.length;i++) {
			int x=(int) fg[i].getX();
			int y=(int) fg[i].getY();
			int xprime = (int) Math.round((x - xm) * atcos - (y - ym) * atsin+xm
			);
			int yprime = (int) Math.round((x - xm) * atsin + (y - ym) * atcos+ym
			);
			fg[i].setLocation(xprime,yprime,0,0);
			if(xprime<minx)
				minx=xprime;
			if(yprime<miny)
				miny=yprime;
			if(xprime>maxx)
				maxx=xprime;
			if(yprime>maxy)
				maxy=yprime;
		}
		int xoutput = maxx-minx+1;
		int youtput = maxy-miny+1;
		BooleanImage res = new BooleanImage(xoutput, youtput, 1, 1, 1);
		res.fill(false);
		for (int i=0;i<fg.length;i++) {
			int x=(int) fg[i].getX();
			int y=(int) fg[i].getY();
			res.setPixelXYBoolean(x-minx,y-miny,true);
		}
		res.setCenter(new Point(xm-minx,ym-miny));
		return res;
	}

	/**
	 * Create a square of edge's length size. Center is at (size/2, size/2).
	 * 
	 * @param size
	 * @return a square SE
	 */
	public static BooleanImage createSquareFlatStructuringElement(int size) {
		BooleanImage se = new BooleanImage(size, size, 1, 1, 1);
		se.resetCenter();
		se.fill(true);
		return se;
	}

	/**
	 * Create a diamond of a given height (width is the same). Center is at
	 * (size/2, size/2).
	 * 
	 * @param size
	 * @return a square SE
	 */
	public static BooleanImage createDiamondFlatStructuringElement(int size) {
		BooleanImage se = new BooleanImage(size, size, 1, 1, 1);
		se.resetCenter();
		for (int x = 0; x < se.getXDim(); x++)
			for (int y = 0; y < se.getYDim(); y++)
				if (x + y + 1 > size / 2 && y <= size / 2 + x
					&& x + y + 1 <= size + size / 2 && x - y <= size / 2)
					se.setPixelXYBoolean(x, y, true);
				else
					se.setPixelXYBoolean(x, y, false);
		return se;
	}

	/**
	 * Create a rectangle. Center is at (height/2, width/2).
	 * 
	 * @param rows
	 * @param cols
	 * @return a rectangle shaped SE
	 */

	public static BooleanImage createRectangularFlatStructuringElement(int xdim,
		int ydim) {
		BooleanImage se = new BooleanImage(xdim, ydim, 1, 1, 1);
		se.resetCenter();
		se.fill(true);
		return se;
	}

	/**
	 * Create a hollow square SE element size x size. Center is at (size/2,
	 * size/2).
	 * 
	 * @param size
	 * @return a hollow square SE
	 */
	public static BooleanImage createHollowSquareFlatStructuringElement(int size) {
		BooleanImage se = new BooleanImage(size, size, 1, 1, 1);
		se.resetCenter();
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (x == 0 || x == size - 1 || y == 0 || y == size - 1)
					se.setPixelXYBoolean(x, y, true);
			}
		}
		return se;
	}

	/**
	 * Create a frame rectangle. Center is at (height/2, width/2).
	 * 
	 * @param rows
	 * @param cols
	 * @return
	 */
	public static BooleanImage createFrameFlatStructuringElement(int ydim,
		int xdim) {
		BooleanImage se = new BooleanImage(xdim, ydim, 1, 1, 1);
		se.resetCenter();
		for (int i = 0; i < se.xdim; i++) {
			se.setPixelXYBoolean(i, 0, true);
			se.setPixelXYBoolean(i, se.ydim - 1, true);
		}
		for (int i = 0; i < se.ydim; i++) {
			se.setPixelXYBoolean(0, i, true);
			se.setPixelXYBoolean(se.xdim - 1, i, true);
		}
		return se;
	}

	/**
	 * Create a frame square of edge's length size. Center is at (size/2, size/2).
	 * 
	 * @param size
	 * @return
	 */
	public static BooleanImage createFrameFlatStructuringElement(int size) {
		return createFrameFlatStructuringElement(size, size);
	}

	public static void print(BooleanImage se) {
		for (int j = 0; j < se.ydim; j++) {
			for (int i = 0; i < se.xdim; i++) {
				if (se.getCenter().x == i && se.getCenter().y == j) {
					if (se.getPixelXYBoolean(i, j))
						System.out.print("+ ");
					else
						System.out.print("O ");
				} else if (se.getPixelXYBoolean(i, j)) {
					System.out.print("0 ");
				} else {
					System.out.print("  ");
				}
			}
			System.out.println();
		}
	}

	// /**
	// * Dessine l'lment structurant sur une image
	// *
	// * @param im
	// * Image dans laquelle dessiner
	// * @param p
	// * Translation de l'lment structurant
	// */
	// public void draw(Image im, Point p) {
	// draw(im, p.x, p.y);
	// }
	//
	// /**
	// * Dessine l'lment structurant sur une image
	// *
	// * @param im
	// * Image dans laquelle dessiner
	// * @param x
	// * Translation de l'lment structurant en x
	// * @param y
	// * Translation de l'lment structurant en y
	// */
	// public void draw(Image im, int x, int y) {
	// int cX = x - centre.x;
	// int cY = y - centre.x;
	// for (int i = 0; i < rows; i++)
	// for (int j = 0; j < cols; j++) {
	// int valX = i + cX;
	// int valY = j + cY;
	// if (getValue(i, j) && valX >= 0 && valX < im.getXDim() && valY >= 0
	// && valY < im.getYDim())
	// im.setPixelXYBoolean(valX, valY, true);
	// }
	// }
}