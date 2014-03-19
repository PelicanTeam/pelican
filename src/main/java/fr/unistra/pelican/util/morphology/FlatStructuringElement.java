package fr.unistra.pelican.util.morphology;

import java.awt.Point;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * Describes a flat structuring element.
 * 
 * +added diagonal lines.02/06
 * @deprecated
 */
public class FlatStructuringElement extends BooleanImage implements
		StructuringElement {
	private int rows = getYDim();

	private int cols = getXDim();

	/**
	 * 
	 */
	public static final long serialVersionUID = 234523;

	// present and future bug source:
	// the centre is kept as X,Y being : COLUMS, ROWS.
	// Careful with the transformations

	// TODO E.A : we should use a single standard...this is far too messy.
	private Point centre;

	private Point[] points = null;

	/**
	 * copy constructor with no pixel cloning
	 * 
	 * @param e
	 *            the structuring element to clone.
	 */
	public FlatStructuringElement(FlatStructuringElement e) {
		super(e.getColumns(), e.getRows(), 1, 1, 1);

		this.centre = (Point) e.getCenter().clone();
	}

	public FlatStructuringElement copyImage(boolean copy) {
		return new FlatStructuringElement(this, copy);
	}

	/**
	 * 
	 * @param se
	 * @param copy
	 */
	public FlatStructuringElement(FlatStructuringElement se, boolean copy) {
		super(se);
		this.centre = (Point) se.getCenter().clone();

		if (copy == true)
			this.setPixels((boolean[]) se.getPixels().clone());
		else
			this.setPixels(new boolean[se.getXDim() * se.getYDim() * 1 * 1 * 1]);
	}

	/**
	 * Contruct a new structuring element.
	 * 
	 * @param rows
	 *            number of rows.
	 * @param cols
	 *            number of columns.
	 */
	public FlatStructuringElement(int rows, int cols) {
		super(cols, rows, 1, 1, 1);
	}

	/**
	 * @param rows
	 *            rows number of rows.
	 * @param cols
	 *            cols number of columns.
	 * @param centre
	 *            center of the structuring element.
	 */
	public FlatStructuringElement(int rows, int cols, Point centre) {
		super(cols, rows, 1, 1, 1);
		this.centre = (Point) centre.clone();
	}

	/**
	 * @param values
	 *            image of the shape to clone.
	 */
	public void setValues(boolean[] values) {
		setPixels(values);
	}

	/**
	 * @param col
	 * @param row
	 * @return if the pixel is active
	 */
	public boolean isValue(int row, int col) {
		return getPixelBoolean(row * cols + col);
	}

	/**
	 * @param center
	 */
	public void setCenter(Point center) {
		this.centre = center;
	}

	/**
	 * @return the center in Colums,Rows format
	 */
//	public Point getCenter() {
//		return centre;
//	}

	/**
	 * @return the number of rows
	 */
	public int getRows() {
		return this.rows;
	}

	/**
	 * @return the number of colums
	 */
	public int getColumns() {
		return this.cols;
	}

	/**
	 * @param b
	 *            the value to set in each pixel of the structuring element.
	 */
	public void fill(boolean b) {
		super.fill(b);
	}

	/**
	 * Create a vertical line of length length. The center is at length/2
	 * 
	 * @param length
	 * @return a vertical line shaped SE
	 */
	public static FlatStructuringElement createVerticalLineFlatStructuringElement(
			int length) {
		FlatStructuringElement se = new FlatStructuringElement(length, 1,
				new Point(0, length / 2));
		se.fill(true);
		return se;
	}

	/**
	 * Create a left diagonal line of length length (odd). The center is at
	 * length / 2
	 * 
	 * @param length
	 * @return a left diagonal shaped SE
	 */
	public static FlatStructuringElement createLeftDiagonalLineFlatStructuringElement(
			int length) {
		FlatStructuringElement se = new FlatStructuringElement(length, length,
				new Point(length / 2, length / 2));

		for (int i = 0; i < length; i++)
			se.setValue(i, i, true);

		return se;
	}

	/**
	 * Create a right diagonal line of length length (odd). The center is at
	 * length / 2
	 * 
	 * @param length
	 * @return
	 */
	public static FlatStructuringElement createRightDiagonalLineFlatStructuringElement(
			int length) {
		FlatStructuringElement se = new FlatStructuringElement(length, length,
				new Point(length / 2, length / 2));

		for (int i = 0; i < length; i++)
			se.setValue(length - 1 - i, i, true);

		return se;
	}

	/**
	 * Create a horizontal line of length length. The center is at length/2.
	 * 
	 * @param length
	 * @param center
	 * @return
	 */
	public static FlatStructuringElement createHorizontalLineFlatStructuringElement(
			int length) {
		FlatStructuringElement se = new FlatStructuringElement(1, length,
				new Point(length / 2, 0));
		se.fill(true);
		return se;
	}

	/**
	 * Create a line of length length of a given orientation (in degrees)
	 * 
	 * @param length
	 * @param orientation
	 * @return
	 */
	public static FlatStructuringElement createLineFlatStructuringElement(
			int length, int orientation) {
		double cos = Math.cos(Math.toRadians(orientation));
		double sin = Math.sin(Math.toRadians(orientation));
		double abscos = Math.abs(cos);
		double abssin = Math.abs(sin);
		double epsilon = 0.0000001;
		double sumcos = abscos;
		double sumsin = abssin;
		int i = 0, j = 0;
		int center = (length - 1) / 2;
		FlatStructuringElement se = new FlatStructuringElement(length, length,
				new Point(center, center));
		se.setValue(center, center, true);
		for (int l = 1; l <= center; l++) {
			if (sumcos + epsilon >= sumsin) {
				i = cos > 0 ? i + 1 : i - 1;
				sumsin += abssin;
			}
			if (sumsin + epsilon >= sumcos) {
				j = sin > 0 ? j + 1 : j - 1;
				sumcos += abscos;
			}
			se.setValue(center + j, center + i, true);
			se.setValue(center - j, center - i, true);
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
	public static FlatStructuringElement createHorizontalLineFlatStructuringElement(
			int length, Point center) {
		FlatStructuringElement se = new FlatStructuringElement(1, length,
				center);
		se.fill(true);
		return se;
	}

	/**
	 * Create a cross line of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static FlatStructuringElement createCrossFlatStructuringElement(
			int radius) {
		FlatStructuringElement se = new FlatStructuringElement(2 * radius + 1,
				2 * radius + 1, new Point(radius, radius));
		for (int i = 0; i < 2 * radius + 1; i++) {
			se.setValue(i, radius, true);
			se.setValue(radius, i, true);
		}
		return se;
	}

	/**
	 * Create a circle of radius radius (plus the pixel at the cross).
	 * 
	 * @param radius
	 * @return
	 */
	public static FlatStructuringElement createCircleFlatStructuringElement(
			int radius) {
		FlatStructuringElement se = new FlatStructuringElement(2 * radius + 1,
				2 * radius + 1, new Point(radius, radius));
		for (int i = 0; i < 2 * radius + 1; i++) {
			for (int j = 0; j < 2 * radius + 1; j++) {
				if (Math
						.sqrt(Math.pow(i - radius, 2) + Math.pow(j - radius, 2)) <= radius + 0.000001)
					se.setValue(i, j, true);
			}
		}
		return se;
	}

	public void setValue(int row, int col, boolean b) {
		setPixelBoolean(row * cols + col,b);
	}

	public void setValue(int location, boolean b) {
		setPixelBoolean(location,b);
	}

	public boolean getValue(int row, int col) {
		return getPixelBoolean(row * cols + col);
	}

	/**
	 * @return The transpose of the structuring element.
	 */
	public FlatStructuringElement getTranspose() {
		FlatStructuringElement se = new FlatStructuringElement(this);

		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				se.setValue(i, j, this.getValue(cols - j - 1, rows - i - 1));

		se.setCenter(new Point(this.cols - this.centre.x - 1, this.rows - 1
				- centre.y));
		return se;
	}

	/**
	 * Create a square of edge's length size. Center is at (size/2, size/2).
	 * 
	 * @param size
	 * @return a square SE
	 */
	public static FlatStructuringElement createSquareFlatStructuringElement(
			int size) {
		FlatStructuringElement se = new FlatStructuringElement(size, size,
				new Point(size / 2, size / 2));
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
	public static FlatStructuringElement createDiamondFlatStructuringElement(
			int size) {
		FlatStructuringElement se = new FlatStructuringElement(size, size,
				new Point(size / 2, size / 2));
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

	public static FlatStructuringElement createRectangularFlatStructuringElement(
			int rows, int cols) {
		FlatStructuringElement se = new FlatStructuringElement(rows, cols,
				new Point(cols / 2, rows / 2));

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
	public static FlatStructuringElement createHollowSquareFlatStructuringElement(
			int size) {
		FlatStructuringElement se = new FlatStructuringElement(size, size,
				new Point(size / 2, size / 2));

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
	public static FlatStructuringElement createFrameFlatStructuringElement(
			int rows, int cols) {
		FlatStructuringElement se = new FlatStructuringElement(rows, cols,
				new Point(cols / 2, rows / 2));
		se.fill(false);
		for (int i = 0; i < rows; i++) {
			se.setValue(i, 0, true);
			se.setValue(i, cols - 1, true);
		}
		for (int i = 0; i < cols; i++) {
			se.setValue(0, i, true);
			se.setValue(rows - 1, i, true);
		}
		return se;
	}

	/**
	 * Create a frame square of edge's length size. Center is at (size/2,
	 * size/2).
	 * 
	 * @param size
	 * @return
	 */
	public static FlatStructuringElement createFrameFlatStructuringElement(
			int size) {
		return createFrameFlatStructuringElement(size, size);
	}

	/**
	 * 
	 * @param se1
	 * @param se2
	 * @return
	 */
	public static boolean haveSameCentre(FlatStructuringElement se1,
			FlatStructuringElement se2) {

		return se1.getCenter().equals(se2.getCenter());

	}

	public boolean[] getValues() {
		return getPixels();
	}

	/**
	 * @return true is this structuring element have 2n pixel set as true.
	 */
	public boolean isOdd() {
		return (this.howManyPoints() % 2 == 1);
	}

	/**
	 * @return The number of pixels sets as true.
	 */
	public int howManyPoints() {
		int cnt = 0;

		for (int i = 0; i < this.size(); i++)
			if (getPixelBoolean(i) == true)
				cnt++;

		return cnt;

	}

	public FlatStructuringElement rotate(double degree) {

		double angleradian = Math.toRadians(degree);

		double xinput = this.cols;
		double yinput = this.rows;

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

		int xm = this.cols / 2;
		int ym = this.rows / 2;

		int xmprime = xoutput / 2;
		int ymprime = youtput / 2;

		FlatStructuringElement se = new FlatStructuringElement(youtput,
				xoutput, new Point(xoutput / 2, youtput / 2));
		se.fill(false);

		for (int x = 0; x < this.cols; x++)
			for (int y = 0; y < this.rows; y++) {
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

				se.setValue(yprime, xprime, this.getValue(y, x));

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
		return se;
	}

	public void print() {

		for (int j = 0; j < this.rows; j++) {
			for (int i = 0; i < this.cols; i++) {
				if (this.centre.x == i && this.centre.y == j) {
					System.out.print("+ ");
				} else if (this.getValue(j, i)) {
					System.out.print("0 ");
				} else {
					System.out.print("  ");
				}
			}
			System.out.println();
		}
	}

	/**
	 * If the centre of the SE is set (=true) then it must be placed first!
	 * 
	 * @return an array of Points containing the coordinates of pixels set to
	 *         true
	 */
	public Point[] getPoints() {
		if (points != null)
			return points;

		int size = 0;
		for (int i = 0; i < cols; i++)
			for (int j = 0; j < rows; j++)
				if (isValue(j, i) == true)
					size++;

		points = new Point[size];

		int k = 0;

		if (isValue(centre.y, centre.x) == true)
			points[k++] = new Point(centre.y, centre.x);
		// System.err.println(cols + " " + rows + " " + size);

		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				if (isValue(j, i) == true && !(i == centre.x && j == centre.y))
					points[k++] = new Point(j, i);
			}
		}

		return points;
	}

	/**
	 * Dessine l'lment structurant sur une image
	 * 
	 * @param im
	 *            Image dans laquelle dessiner
	 * @param p
	 *            Translation de l'lment structurant
	 */
	public void draw(Image im, Point p) {
		draw(im, p.x, p.y);
	}

	/**
	 * Dessine l'lment structurant sur une image
	 * 
	 * @param im
	 *            Image dans laquelle dessiner
	 * @param x
	 *            Translation de l'lment structurant en x
	 * @param y
	 *            Translation de l'lment structurant en y
	 */
	public void draw(Image im, int x, int y) {
		int cX = x - centre.x;
		int cY = y - centre.x;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++) {
				int valX = i + cX;
				int valY = j + cY;
				if (getValue(i, j) && valX >= 0 && valX < im.getXDim()
						&& valY >= 0 && valY < im.getYDim())
					im.setPixelXYBoolean(valX, valY, true);
			}
	}
}