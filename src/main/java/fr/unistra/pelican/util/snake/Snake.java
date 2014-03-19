package fr.unistra.pelican.util.snake;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;

import fr.unistra.pelican.Image;

/**
 * 
 * Snake (active contour) model based on the greedy algorithm
 * 
 * @author lefevre
 * 
 */
public class Snake {

	private int size;

	private Point[] points;

	private double averageDistance; // Energy Continuity

	private double normalx, normaly; // Energy Balloon

	private Point shifted = new Point(); // Energy Gradient

	private int color[]; // Energy Color

	private boolean deltaGradient[]; // External splitting

	private boolean deltaBackground[]; // External splitting

	private Image gradient; // Energy Gradient

	private Image reference; // Energy Background

	private Image data; // Energy Color

	private boolean colorContour = true; // Use only contour points to
											// compute

	// color
	private double epsilon = 0.01; // Normalisation epsilon

	private int zoom = 0; // Zooming factor

	private int sizeNeighbourhood = 5; // Neighbourhood size

	private boolean normaliseNeighbourhood = true;

	private boolean lengthCriteria = true;

	private boolean partialImage = false;

	// Coefficients for the different energies
	private double coeffContinuity = 1;

	private double coeffBalloon = 1;

	private double coeffCurvature = 1;

	private double coeffGradient = 1;

	private double coeffBackground = 1;

	private double coeffIntern = 1;

	private double coeffExtern = 1;

	private int gradientThr = 150;

	private int colorThr = 50;

	private int referenceThr = 50;

	private int backgroundModel = BACKGROUND_COLOR;

	private boolean drawColor = true;

	public static final int BACKGROUND_COLOR = 0;

	public static final int BACKGROUND_REFERENCE = 1;

	public static final int MERGE_CENTERS = 0;

	public static final int MERGE_EXTREMA = 1;

	public static final int MERGE_BOTH = 2;

	public static final int SPLIT_EXTERN = 1;

	public static final int SPLIT_INTERN = 2;

	public boolean DEBUG = false;

	public boolean DEBUG_LOCAL = true;

	public boolean DEBUG_GLOBAL = true;

	public boolean DEBUG_MINIMA = true;

	public int DEBUG_POINTS[];

	public Snake(Point[] points) {
		this.points = points.clone();
		size = points.length;
		deltaGradient = new boolean[size];
		deltaBackground = new boolean[size];
		Arrays.fill(deltaGradient, true);
		Arrays.fill(deltaBackground, true);
	}

	public Snake(int size) {
		this.size = size;
		points = new Point[size];
		deltaGradient = new boolean[size];
		deltaBackground = new boolean[size];
		Arrays.fill(deltaGradient, true);
		Arrays.fill(deltaBackground, true);
	}

	public Snake(int size, int xmin, int xmax, int ymin, int ymax) {
		this(size);
		initialisation(xmin, xmax, ymin, ymax);
	}

	public Snake(int size, Point p1, Point p2) {
		this(size);
		initialisation(p1, p2);
	}

	public Snake(Snake s) {
		size = s.size;
		points = s.points.clone();
		deltaGradient = s.deltaGradient.clone();
		deltaBackground = s.deltaBackground.clone();
	}

	public void clean() {
		gradient = null;
		reference = null;
		data = null;
		color = null;
		deltaGradient = null;
		deltaBackground = null;
	}

	public void updateSize() {
		deltaGradient = new boolean[size];
		deltaBackground = new boolean[size];
		Arrays.fill(deltaGradient, true);
		Arrays.fill(deltaBackground, true);
	}

	public void initialisation(int xmin, int xmax, int ymin, int ymax) {
		double xdelta, ydelta, delta;
		int k;
		if (size == 0)
			return;
		delta = size / 4.0;
		xdelta = (xmax - xmin) / delta;
		ydelta = (ymax - ymin) / delta;
		for (k = 0; k < delta; k++)
			points[k] = new Point(xmin + (int) (k * xdelta), ymin);
		for (k = 0; k < delta; k++)
			points[(int) (k + delta)] = new Point(xmax, ymin
					+ (int) (k * ydelta));
		for (k = 0; k < delta; k++)
			points[(int) (k + 2 * delta)] = new Point(
					xmax - (int) (k * xdelta), ymax);
		for (k = 0; k < delta; k++)
			points[(int) (k + 3 * delta)] = new Point(xmin, ymax
					- (int) (k * ydelta));
	}

	public void initialisation(Point p1, Point p2) {
		Point min = minimum(p1, p2);
		Point max = maximum(p1, p2);
		initialisation(min.x, max.x, min.y, max.y);
	}

	public Point[] getPoints() {
		return points;
	}

	public int getSize() {
		return size;
	}

	public void setCoefficients(double cont, double ball, double curv,
			double grad, double background, double intern, double extern) {
		coeffContinuity = cont;
		coeffBalloon = ball;
		coeffCurvature = curv;
		coeffGradient = grad;
		coeffBackground = background;
		coeffIntern = intern;
		coeffExtern = extern;
	}

	public void setGradient(Image img) {
		gradient = img;
	}

	public void setReference(Image img) {
		reference = img;
	}

	public void setData(Image img) {
		data = img;
		computeColor();
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public void setNeighbourhood(int size) {
		sizeNeighbourhood = size;
	}

	public void setEpsilon(double eps) {
		epsilon = eps;
	}

	public void setGradientThreshold(int thr) {
		gradientThr = thr;
	}

	public void setReferenceThreshold(int thr) {
		referenceThr = thr;
	}

	public void setColorThreshold(int thr) {
		colorThr = thr;
	}

	public void setBackgroundModel(int model) {
		backgroundModel = model;
	}

	public boolean equals(Snake s) {
		if (s.size != size)
			return false;
		for (int i = 0; i < size; i++)
			if (!points[i].equals(s.points[i]))
				return false;
		return true;
	}

	public void invertPoint(int pos1, int pos2) {
		Point p = points[pos1];
		points[pos1] = points[pos2];
		points[pos2] = p;
	}

	public void insertPoint(int pos, Point p) {
		Point[] points2 = new Point[size + 1];
		for (int i = 0; i < pos; i++)
			points2[i] = points[i];
		points2[pos] = new Point(p);
		for (int i = pos; i < size; i++)
			points2[i + 1] = points[i];
		size++;
		points = points2;
		updateSize();
	}

	public void deletePoint(int pos) {
		if (size == 0)
			return;
		Point[] points2 = new Point[size - 1];
		for (int i = 0; i < pos; i++)
			points2[i] = points[i];
		for (int i = pos + 1; i < size; i++)
			points2[i - 1] = points[i];
		size--;
		points = points2;
		updateSize();
	}

	public void deleteDoubles(boolean successiveOnly) {
		if (size == 0)
			return;
		int s = size;
		if (successiveOnly) {
			// check successive only
			for (int k = 0; k < size - 1; k++)
				if (points[k].equals(points[k + 1])) {
					points[k] = null;
					s--;
				}
			if (points[0] != null)
				if (points[0].equals(points[size - 1])) {
					points[size - 1] = null;
					s--;
				}
		} else
			// check all doubles
			for (int k = 0; k < size; k++)
				for (int l = k + 1; l < size && points[k] != null; l++)
					if (points[k].equals(points[l])) {
						points[k] = null;
						s--;
					}
		// trim the result
		if (s != size) {
			Point tmp[] = new Point[s];
			int l = 0;
			for (int k = 0; k < size; k++)
				if (points[k] != null)
					tmp[l++] = points[k];
			points = tmp;
			size = s;
			updateSize();
		}
	}

	public void deleteCrossings(boolean successiveOnly) {
		if (size == 0)
			return;
		int deleted[] = new int[2 * size];
		int d = 0;
		if (successiveOnly) {
			// check quasi-successive only
			for (int k = 0; k < size - 1; k++)
				if (Line2D.linesIntersect(points[k].x, points[k].y,
						points[k + 1 > size - 1 ? 0 : k + 1].x,
						points[k + 1 > size - 1 ? 0 : k + 1].y,
						points[k + 2 > size - 1 ? (k + 2) % size : k + 2].x,
						points[k + 2 > size - 1 ? (k + 2) % size : k + 2].y,
						points[k + 3 > size - 1 ? (k + 3) % size : k + 3].x,
						points[k + 3 > size - 1 ? (k + 3) % size : k + 3].y)) {
					deleted[d++] = (k + 1) % size;
					// deleted[d++]=(k+2)%size;
					points[(k + 2) % size] = mean(points[(k + 1) % size],
							points[(k + 2) % size]);
					k++;
				}
		} else
			// check all crossings
			for (int k = 0; k < size; k++)
				for (int l = k + 2; l < size; l++)
					// Special case
					if (!(k == 0 && l == size - 1))
						if (Line2D.linesIntersect(points[k].x, points[k].y,
								points[k + 1 > size - 1 ? 0 : k + 1].x,
								points[k + 1 > size - 1 ? 0 : k + 1].y,
								points[l].x, points[l].y,
								points[l + 1 > size - 1 ? 0 : l + 1].x,
								points[l + 1 > size - 1 ? 0 : l + 1].y)) {
							// Delete points between k and l
							if ((lengthCriteria == true && length(k, l) <= length(
									0, k)
									+ length(l, size - 1))
									|| (lengthCriteria == false && (l - k <= size / 2))) {
								// Add the midpoint between k+1 and l
								points[k + 1 > size - 1 ? 0 : k + 1] = mean(
										points[k + 1 > size - 1 ? 0 : k + 1],
										points[l]);
								for (int m = k + 2; m < l + 1; m++)
									deleted[d++] = m;
							}
							// Delete points between l and k
							else {
								for (int m = 0; m < k + 1; m++)
									deleted[d++] = m;
								// Add the midpoint between k and l+1
								points[l + 1 > size - 1 ? 0 : l + 1] = mean(
										points[k], points[l + 1 > size - 1 ? 0
												: l + 1]);
								for (int m = l + 2; m < size; m++)
									deleted[d++] = m;
							}
						}
		int s = d;
		/*
		 * // to avoid an empty contour if (d>=size) return;
		 */
		for (int k = 0; k < d; k++)
			if (points[deleted[k]] != null)
				points[deleted[k]] = null;
			else
				s--;
		// trim the result
		if (s != 0) {
			Point tmp[] = new Point[size - s];
			int l = 0;
			for (int k = 0; k < size; k++)
				if (points[k] != null)
					tmp[l++] = points[k];
			points = tmp;
			size -= s;
			updateSize();
		}
	}

	// TODO complete all case of this algorithm
	public void repareCrossings(boolean successiveOnly) {
		if (size == 0)
			return;
		if (successiveOnly) {
			// check quasi-successive only
			for (int k = 0; k < size - 1; k++)
				if (Line2D.linesIntersect(points[k].x, points[k].y,
						points[k + 1 > size - 1 ? 0 : k + 1].x,
						points[k + 1 > size - 1 ? 0 : k + 1].y,
						points[k + 2 > size - 1 ? (k + 2) % size : k + 2].x,
						points[k + 2 > size - 1 ? (k + 2) % size : k + 2].y,
						points[k + 3 > size - 1 ? (k + 3) % size : k + 3].x,
						points[k + 3 > size - 1 ? (k + 3) % size : k + 3].y)) {
					invertPoint(k + 1, k + 2);
					k++;
				}
		} else
			// check all crossings
			for (int k = 0; k < size; k++)
				for (int l = k + 2; l < size; l++)
					// Special case
					if (!(k == 0 && l == size - 1))
						if (Line2D.linesIntersect(points[k].x, points[k].y,
								points[k + 1 > size - 1 ? 0 : k + 1].x,
								points[k + 1 > size - 1 ? 0 : k + 1].y,
								points[l].x, points[l].y,
								points[l + 1 > size - 1 ? 0 : l + 1].x,
								points[l + 1 > size - 1 ? 0 : l + 1].y)) {
							// Invert points between k and l
							if ((lengthCriteria == true && length(k, l) <= length(
									0, k)
									+ length(l, size - 1))
									|| (lengthCriteria == false && (l - k <= size / 2)))
								for (int m = k + 1; m < l + 1; m++)
									invertPoint(m, size - m);
							// Invert points between l and k
							// TODO implement this feature
						}
	}

	public static Snake add(Snake s1, Snake s2) {
		if (s1 == null)
			return s2;
		else
			return s1.add(s2);
	}

	public Snake add(Snake s) {
		if (s == null)
			return this;
		Snake res = new Snake(size + s.size);
		for (int k = 0; k < size; k++)
			res.points[k] = new Point(points[k]);
		for (int k = 0; k < s.size; k++)
			res.points[k + size] = new Point(s.points[k]);
		return res;
	}

	public Snake extract(int pos1, int pos2) {
		if (pos2 - pos1 < 0)
			return null;
		Snake res = new Snake(1 + pos2 - pos1);
		for (int k = 0; k < 1 + pos2 - pos1; k++)
			res.points[k] = new Point(points[pos1 + k]);
		return res;
	}

	public double length(int pos1, int pos2) {
		double dist = 0;
		for (int k = pos1; k < pos2; k++)
			dist += points[k].distance(points[k + 1]);
		return dist;
	}

	public double computeAverageDistance() {
		double dist = length(0, size - 1);
		dist += points[size - 1].distance(points[0]);
		dist /= size;
		return dist;
	}

	public void computeColor() {
		color = new int[data.getBDim()];
		for (int b = 0; b < data.getBDim(); b++) {
			color[b] = 0;
			if (colorContour) {
				for (int t = 0; t < data.getTDim(); t++)
					for (int z = 0; z < data.getZDim(); z++)
						for (int k = 0; k < size; k++)
							color[b] += data.getPixelByte(points[k].x
									- shifted.x, points[k].y - shifted.y, z, t,
									b);
				color[b] /= size;
			} else {
				for (int t = 0; t < data.getTDim(); t++)
					for (int z = 0; z < data.getZDim(); z++)
						for (int x = 0; x < data.getXDim(); x++)
							for (int y = 0; y < data.getYDim(); y++)
								color[b] += data.getPixelByte(x, y, z, t, b);
				color[b] /= data.getTDim() * data.getZDim() * data.getYDim()
						* data.getXDim();
			}
		}
	}

	public void computeShift() {
		shifted = getMin();
		shifted.translate(-zoom, -zoom);
	}

	public Point getCenter() {
		int x = 0;
		int y = 0;
		for (int k = 0; k < size; k++) {
			x += points[k].x;
			y += points[k].y;
		}
		Point res = new Point(x / size, y / size);
		return res;
	}

	public Point getMin() {
		int x = Integer.MAX_VALUE;
		int y = Integer.MAX_VALUE;
		for (int k = 0; k < size; k++) {
			if (points[k].x < x)
				x = points[k].x;
			if (points[k].y < y)
				y = points[k].y;
		}
		Point res = new Point(x, y);
		return res;
	}

	public Point getMax() {
		int x = Integer.MIN_VALUE;
		int y = Integer.MIN_VALUE;
		for (int k = 0; k < size; k++) {
			if (points[k].x > x)
				x = points[k].x;
			if (points[k].y > y)
				y = points[k].y;
		}
		Point res = new Point(x, y);
		return res;
	}

	public Snake merge(Snake snake) {
		if (snake == null)
			return this;
		// Compute extrema
		Point max1 = this.getMax();
		Point max2 = snake.getMax();
		Point min1 = this.getMin();
		Point min2 = snake.getMin();
		Point min = minimum(min1, min2);
		Point max = maximum(max1, max2);
		// Create new snake
		return new Snake(size + snake.size, min, max);
	}

	public Snake[] split(int mode) {
		int m = 0;
		int invalid[] = new int[size];
		Snake[] res;
		if (mode == SPLIT_EXTERN)
			// Check which point has to be deleted
			for (int k = 0; k < size; k++)
				if (deltaGradient[k] == false && deltaBackground[k] == false)
					invalid[m++] = k;
		// Special case if no split is required
		if (m < 1) {
			res = new Snake[1];
			res[0] = this;
			return res;
		}
		// Split the snake by keeping valid successive points sequences
		int l = 0;
		res = new Snake[m + 1];
		for (int k = 0; k < m - 1; k++) {
			// compute the end of the invalid points local sequence
			int k2 = k;
			while (k2 < m - 1 && invalid[k2 + 1] - invalid[k2] == 1)
				k2++;
			if (k2 < m - 1)
				res[l++] = extract(invalid[k2] + 1, invalid[k2 + 1] - 1);
			k = k2;
		}
		// Special processing for invalid[m-1],invalid[0]
		Snake s, s1 = null, s2 = null;
		if (invalid[m - 1] + 1 < size)
			s1 = extract(invalid[m - 1], size - 1);
		if (invalid[0] > 0)
			s2 = extract(0, invalid[0]);
		s = Snake.add(s1, s2);
		if (s != null)
			res[l++] = s;
		return trim(res);
	}

	public boolean isValid(int sizeMin, int widthMin, int heightMin, int areaMin) {
		Point max = getMax();
		Point min = getMin();
		Point diff = diff(min, max);
		if (size < sizeMin)
			return false;
		if (diff.x < widthMin || diff.y < heightMin)
			return false;
		if (diff.x * diff.y < areaMin)
			return false;
		return true;
	}

	public Image drawOnImage(Image input, boolean point, boolean segment) {
		Image output = input.copyImage(true);
		if (segment) {
			Point[] tmp;
			for (int k = 0; k < size; k++) {
				tmp = bresenham(points[k], points[k + 1 > size - 1 ? 0 : k + 1]);
				for (int l = 0; l < tmp.length; l++)
					if (tmp[l].x >= 0 && tmp[l].x < input.getXDim()
							&& tmp[l].y >= 0 && tmp[l].y < input.getYDim())
						for (int z = 0; z < input.getZDim(); z++)
							for (int t = 0; t < input.getTDim(); t++)
								for (int b = 0; b < input.getBDim(); b++)
									output.setPixelBoolean(tmp[l].x, tmp[l].y,
											z, t, b, drawColor);
			}
		}
		if (point) {
			for (int k = 0; k < size; k++)
				if (points[k].x >= 0 && points[k].x < input.getXDim()
						&& points[k].y >= 0 && points[k].y < input.getYDim())
					for (int z = 0; z < input.getZDim(); z++)
						for (int t = 0; t < input.getTDim(); t++)
							for (int b = 0; b < input.getBDim(); b++) {
								for (int lx = -1; lx < 2; lx++)
									for (int ly = -1; ly < 2; ly++)
										if (points[k].x + lx >= 0
												&& points[k].y + ly >= 0
												&& points[k].x + lx < input
														.getXDim()
												&& points[k].y + ly < input
														.getYDim())
											output.setPixelBoolean(points[k].x
													+ lx, points[k].y + ly, z,
													t, b, !drawColor);
								output.setPixelBoolean(points[k].x,
										points[k].y, z, t, b, drawColor);
							}
		}
		return output;
	}

	void computeEnergyContinuity(Neighbourhood n, int pos) {
		Point tmp = new Point();
		int dx, dy;
		int shift = (n.size - 1) / 2;
		for (dx = 0; dx < n.size; dx++)
			for (dy = 0; dy < n.size; dy++) {
				tmp.setLocation(points[pos].x + dx - shift, points[pos].y + dy
						- shift);
				n.values[dx][dy] = Math.abs(averageDistance
						- tmp
								.distance(points[pos - 1 < 0 ? size - 1
										: pos - 1]));
			}
		if (normaliseNeighbourhood)
			n.normalisation();
		if (DEBUG && DEBUG_LOCAL)
			if (DEBUG_POINTS == null
					|| Arrays.binarySearch(DEBUG_POINTS, pos) >= 0)
				System.out.println(pos + ":" + points[pos].x + ","
						+ points[pos].y + ":" + "CT:" + n);
	}

	void computeEnergyBalloon(Neighbourhood n, int pos) {
		int dx, dy;
		int shift = (n.size - 1) / 2;
		double prev, next, nix, niy;
		// Norm computing using previous and next points
		prev = points[pos].distance(points[pos - 1 < 0 ? size - 1 : pos - 1]);
		next = points[pos].distance(points[pos + 1 > size - 1 ? 0 : pos + 1]);
		if (prev == 0 || next == 0) {
			n.fill(0);
			return;
		}
		// Normal vector computation and update normal vector
		nix = (points[pos].x - points[pos - 1 < 0 ? size - 1 : pos - 1].x)
				/ prev
				+ (points[pos].x - points[pos + 1 > size - 1 ? 0 : pos + 1].x)
				/ next;
		if (nix == 0)
			nix = normalx;
		else
			normalx = nix;
		niy = (points[pos].y - points[pos - 1 < 0 ? size - 1 : pos - 1].y)
				/ prev
				+ (points[pos].y - points[pos + 1 > size - 1 ? 0 : pos + 1].y)
				/ next;
		if (niy == 0)
			niy = normaly;
		else
			normaly = niy;
		// Energy computation
		for (dx = 0; dx < n.size; dx++)
			for (dy = 0; dy < n.size; dy++)
				n.values[dx][dy] = (dx - shift) * nix + (dy - shift) * niy;
		if (normaliseNeighbourhood)
			n.normalisation();
		if (DEBUG && DEBUG_LOCAL)
			if (DEBUG_POINTS == null
					|| Arrays.binarySearch(DEBUG_POINTS, pos) >= 0)
				System.out.println(pos + ":" + points[pos].x + ","
						+ points[pos].y + ":" + "BA:" + n);
	}

	void computeEnergyCurvature(Neighbourhood n, int pos) {
		Point tmp = new Point();
		int dx, dy;
		double dist;
		int shift = (n.size - 1) / 2;
		double prev, next;
		// Distance prev-next computing
		dist = points[pos - 1 < 0 ? size - 1 : pos - 1]
				.distance(points[pos + 1 > size - 1 ? 0 : pos + 1]);
		if (dist == 0) {
			n.fill(0);
			return;
		}
		// Energy computation
		for (dx = 0; dx < n.size; dx++)
			for (dy = 0; dy < n.size; dy++) {
				tmp.setLocation(points[pos].x + dx - shift, points[pos].y + dy
						- shift);
				prev = tmp.distance(points[pos - 1 < 0 ? size - 1 : pos - 1]);
				next = tmp.distance(points[pos + 1 > size - 1 ? 0 : pos + 1]);
				n.values[dx][dy] = (prev + next) / dist - 1; // why -1 ?
			}
		if (normaliseNeighbourhood)
			n.normalisation();
		if (DEBUG && DEBUG_LOCAL)
			if (DEBUG_POINTS == null
					|| Arrays.binarySearch(DEBUG_POINTS, pos) >= 0)
				System.out.println(pos + ":" + points[pos].x + ","
						+ points[pos].y + ":" + "CV:" + n);
	}

	void computeEnergyGradient(Neighbourhood n, int pos) {
		int dx, dy;
		double val;
		int shift = (n.size - 1) / 2;
		for (dx = 0; dx < n.size; dx++)
			for (dy = 0; dy < n.size; dy++) {
				val = 0;
				for (int b = 0; b < gradient.getBDim(); b++)
					val += gradient.getPixelByte((int) (points[pos].x + dx
							- shift - shifted.x), (int) (points[pos].y + dy
							- shift - shifted.y), 0, 0, 0);
				if (val < gradientThr * gradient.getBDim())
					n.values[dx][dy] = 0;
				else
					n.values[dx][dy] = -val;
			}
		if (normaliseNeighbourhood)
			n.normalisation();
		deltaGradient[pos] = !n.constant;
		if (DEBUG && DEBUG_LOCAL)
			if (DEBUG_POINTS == null
					|| Arrays.binarySearch(DEBUG_POINTS, pos) >= 0)
				System.out.println(pos + ":" + points[pos].x + ","
						+ points[pos].y + ":" + "GR:" + n);
	}

	void computeEnergyReference(Neighbourhood n, int pos) {
		int dx, dy;
		double val;
		int shift = (n.size - 1) / 2;
		for (dx = 0; dx < n.size; dx++)
			for (dy = 0; dy < n.size; dy++) {
				val = 0;
				for (int b = 0; b < reference.getBDim(); b++)
					val += reference.getPixelByte((int) (points[pos].x + dx
							- shift - shifted.x), (int) (points[pos].y + dy
							- shift - shifted.y), 0, 0, 0);
				if (val < referenceThr * reference.getBDim())
					n.values[dx][dy] = 0;
				else
					n.values[dx][dy] = -val;
			}
		if (normaliseNeighbourhood)
			n.normalisation();
		deltaBackground[pos] = !n.constant;
		if (DEBUG && DEBUG_LOCAL)
			if (DEBUG_POINTS == null
					|| Arrays.binarySearch(DEBUG_POINTS, pos) >= 0)
				System.out.println(pos + ":" + points[pos].x + ","
						+ points[pos].y + ":" + "BG:" + n);
	}

	void computeEnergyColor(Neighbourhood n, int pos) {
		int dx, dy;
		int diff;
		int shift = (n.size - 1) / 2;
		for (dx = 0; dx < n.size; dx++)
			for (dy = 0; dy < n.size; dy++) {
				diff = 0;
				for (int b = 0; b < data.getBDim(); b++)
					diff += Math.abs(data.getPixelByte((int) (points[pos].x
							+ dx - shift - shifted.x), (int) (points[pos].y
							+ dy - shift - shifted.y), 0, 0, b)
							- color[b]);
				if (diff < colorThr * data.getBDim())
					n.values[dx][dy] = 0;
				else
					n.values[dx][dy] = diff;
			}
		if (normaliseNeighbourhood)
			n.normalisation();
		deltaBackground[pos] = !n.constant;
		if (DEBUG && DEBUG_LOCAL)
			if (DEBUG_POINTS == null
					|| Arrays.binarySearch(DEBUG_POINTS, pos) >= 0)
				System.out.println(pos + ":" + points[pos].x + ","
						+ points[pos].y + ":" + "CL:" + n);
	}

	void computeEnergyGreen(Neighbourhood n, int pos) {
		if (data.getBDim() < 3)
			return;
		int dx, dy;
		int ndg;
		double green;
		int shift = (n.size - 1) / 2;
		for (dx = 0; dx < n.size; dx++)
			for (dy = 0; dy < n.size; dy++) {
				ndg = 0;
				for (int b = 0; b < data.getBDim(); b++)
					ndg += data.getPixelByte(
							(int) (points[pos].x + dx - shift - shifted.x),
							(int) (points[pos].y + dy - shift - shifted.y), 0,
							0, b);
				green = data
						.getPixelByte(
								(int) (points[pos].x + dx - shift - shifted.x),
								(int) (points[pos].y + dy - shift - shifted.y),
								0, 0, 1);
				if (green / ndg < epsilon)
					n.values[dx][dy] = 0;
				else
					n.values[dx][dy] = 1.0 - green / ndg;
			}
	}

	public boolean deform() {
		if (size == 0)
			return false;
		Neighbourhood intern = new Neighbourhood(sizeNeighbourhood);
		Neighbourhood extern = new Neighbourhood(sizeNeighbourhood);
		Neighbourhood local = new Neighbourhood(sizeNeighbourhood);
		Snake prev = new Snake(this);
		Point[] moves = new Point[size];
		// Parameters initialisation
		averageDistance = computeAverageDistance();
		if (partialImage)
			computeShift();
		normalx = normaly = 0;
		for (int k = 0; k < size; k++) {
			// Computation of intern energy
			intern.fill(0);
			if (coeffContinuity != 0 && coeffIntern != 0) {
				computeEnergyContinuity(local, k);
				intern.add(local, coeffContinuity);
			}
			if (coeffBalloon != 0 && coeffIntern != 0) {
				computeEnergyBalloon(local, k);
				intern.add(local, coeffBalloon);
			}
			if (coeffCurvature != 0 && coeffIntern != 0) {
				computeEnergyCurvature(local, k);
				intern.add(local, coeffCurvature);
			}
			intern.divide(coeffBalloon + coeffBalloon + coeffCurvature);
			// intern.normalisation();
			if (DEBUG && DEBUG_GLOBAL)
				if (DEBUG_POINTS == null
						|| Arrays.binarySearch(DEBUG_POINTS, k) >= 0)
					System.out.println(k + ":" + points[k].x + ","
							+ points[k].y + ":" + "I:" + intern);
			// Computation of extern energy
			extern.fill(0);
			if (coeffGradient != 0 && coeffExtern != 0) {
				computeEnergyGradient(local, k);
				extern.add(local, coeffGradient);
			}
			if (coeffBackground != 0 && coeffExtern != 0) {
				if (backgroundModel == BACKGROUND_COLOR)
					computeEnergyColor(local, k);
				if (backgroundModel == BACKGROUND_REFERENCE)
					computeEnergyReference(local, k);
				extern.add(local, coeffBackground);
			}
			extern.divide(coeffGradient + coeffBackground);
			// extern.normalisation();
			if (DEBUG && DEBUG_GLOBAL)
				if (DEBUG_POINTS == null
						|| Arrays.binarySearch(DEBUG_POINTS, k) >= 0)
					System.out.println(k + ":" + points[k].x + ","
							+ points[k].y + ":" + "E:" + extern);
			// Computation of global energy
			local.fill(0);
			local.add(intern, coeffIntern);
			local.add(extern, coeffExtern);
			if (DEBUG && DEBUG_GLOBAL)
				if (DEBUG_POINTS == null
						|| Arrays.binarySearch(DEBUG_POINTS, k) >= 0)
					System.out.println(k + ":" + points[k].x + ","
							+ points[k].y + ":" + "T:" + local);
			// Look for the minimum
			moves[k] = local.minimum();
			if (DEBUG && DEBUG_GLOBAL)
				if (DEBUG_POINTS == null
						|| Arrays.binarySearch(DEBUG_POINTS, k) >= 0)
					System.out
							.println(k + ":" + points[k].x + "," + points[k].y
									+ ":" + moves[k].x + "," + moves[k].y);
		}
		// Perform translation of all points
		for (int k = 0; k < size; k++)
			points[k].translate(moves[k].x, moves[k].y);
		// Optionnal : avoid oscillations
		for (int k = 0; k < size - 1; k++)
			if (points[k].equals(prev.points[k + 1])
					&& points[k + 1].equals(prev.points[k]))
				invertPoint(k, k + 1);
		if (points[0].equals(prev.points[size - 1])
				&& points[0].equals(prev.points[size - 1]))
			invertPoint(0, size - 1);
		// Check convergence
		return (!equals(prev));
	}

	public void crop(Point p1, Point p2) {
		Point min = minimum(p1, p2);
		Point max = maximum(p1, p2);
		for (int k = 0; k < size; k++) {
			if (points[k].x > max.x)
				points[k].x = max.x;
			if (points[k].y > max.y)
				points[k].y = max.y;
			if (points[k].x < min.x)
				points[k].x = min.x;
			if (points[k].y < min.y)
				points[k].y = min.y;
		}
	}

	public static Snake[] merge(Snake[] snakes, double param, int mode) {
		// Special case if less than 2 snakes to merge
		if (snakes.length < 2)
			return snakes;
		Point[] centers = null;
		Point[] min = null;
		Point[] max = null;
		Point[] diff = null;
		// Compute barycenters
		if (mode == MERGE_CENTERS || mode == MERGE_BOTH) {
			centers = new Point[snakes.length];
			for (int k = 0; k < snakes.length; k++)
				centers[k] = snakes[k].getCenter();
		}
		// Compute extrema
		if (mode == MERGE_EXTREMA || mode == MERGE_BOTH) {
			min = new Point[snakes.length];
			max = new Point[snakes.length];
			diff = new Point[snakes.length];
			for (int k = 0; k < snakes.length; k++) {
				min[k] = snakes[k].getMin();
				max[k] = snakes[k].getMax();
				diff[k] = diff(min[k], max[k]);
			}
		}
		// Compare barycenters
		int merged[];
		int m;
		// Process each snake
		for (int k = 0; k < snakes.length; k++)
			if (snakes[k] != null) {
				merged = new int[snakes.length];
				m = 0;
				// Check for close barycenters
				for (int l = k + 1; l < snakes.length; l++) {
					if (mode == MERGE_CENTERS || mode == MERGE_BOTH)
						if (centers[k].distance(centers[l]) < param)
							merged[m++] = l;
					if (mode == MERGE_EXTREMA || mode == MERGE_BOTH)
						if (diff(max[k], min[l]).x > diff[k].x * param
								&& diff(max[k], min[l]).y > diff[k].y * param)
							merged[m++] = l;
				}
				// Merge snakes iteratively
				for (int n = 0; n < m; n++) {
					snakes[k] = snakes[k].merge(snakes[merged[n]]);
					snakes[merged[n]] = null;
				}
			}
		// Create result
		return trim(snakes);
	}

	public static Snake[] split(Snake[] snakes, int mode) {
		ArrayList<Snake> v = new ArrayList<Snake>();
		if (snakes == null)
			return snakes;
		// split each snake of the array and add the results to the vector
		for (int k = 0; k < snakes.length; k++)
			v.addAll(Arrays.asList(snakes[k].split(mode)));
		return v.toArray(new Snake[0]);
	}

	public static Snake[] filter(Snake[] snakes, int sizeMin, int widthMin,
			int heightMin, int areaMin) {
		for (int k = 0; k < snakes.length; k++)
			if (!snakes[k].isValid(sizeMin, widthMin, heightMin, areaMin))
				snakes[k] = null;
		return trim(snakes);
	}

	public static Image draw(Snake[] snakes, Image inputImage, boolean point,
			boolean segment) {
		Image tmp = inputImage.copyImage(true);
		for (int k = 0; k < snakes.length; k++)
			tmp = snakes[k].drawOnImage(tmp, point, segment);
		return tmp;
	}

	public static void clean(Snake[] snakes) {
		for (int k = 0; k < snakes.length; k++)
			snakes[k].clean();
	}

	public static Snake[] trim(Snake[] snakes) {
		int size = 0;
		for (int k = 0; k < snakes.length; k++)
			if (snakes[k] != null)
				size++;
		if (size == snakes.length)
			return snakes;
		Snake tmp[] = new Snake[size];
		int l = 0;
		for (int k = 0; k < snakes.length; k++)
			if (snakes[k] != null)
				tmp[l++] = snakes[k];
		return tmp;
	}

	static Point minimum(Point p1, Point p2) {
		int x = Math.min(p1.x, p2.x);
		int y = Math.min(p1.y, p2.y);
		return new Point(x, y);
	}

	static Point maximum(Point p1, Point p2) {
		int x = Math.max(p1.x, p2.x);
		int y = Math.max(p1.y, p2.y);
		return new Point(x, y);
	}

	static Point mean(Point p1, Point p2) {
		int x = p1.x + p2.x;
		int y = p1.y + p2.y;
		return new Point(x / 2, y / 2);
	}

	static Point diff(Point p1, Point p2) {
		int x = Math.abs(p1.x - p2.x);
		int y = Math.abs(p1.y - p2.y);
		return new Point(x, y);
	}

	/**
	 * Algorithme de trace de segments de Bresenham Code inspire de celui
	 * propose par Karl Tombre (LORIA Nancy)
	 * 
	 * @param p1
	 *            premier point
	 * @param p2
	 *            second point
	 * @return liste de points a tracer
	 */
	static Point[] bresenham(Point p1, Point p2) {
		ArrayList<Point> v = new ArrayList<Point>();
		int deltaX = Math.abs(p2.x - p1.x);
		int deltaY = Math.abs(p2.y - p1.y);
		Point p;
		Point pFinal;
		if (deltaX > deltaY) {
			// direction principale Ox
			if (p1.getX() < p2.getX()) {
				// calcul des points de depart et d'arrivee
				p = new Point(p1);
				pFinal = new Point(p2);
			} else {
				p = new Point(p2);
				pFinal = new Point(p1);
			}
			int e = 2 * deltaY - deltaX;
			// Calcul des increments de e
			int horiz = 2 * deltaY;
			int diago = 2 * (deltaY - deltaX);
			// Booleen pour marquer si on est en y croissant ou non
			boolean croissant = p.getY() < pFinal.getY(); // y croissant ou
															// non
			for (int i = 0; i < deltaX; i++) {
				v.add(new Point(p));
				if (e > 0) {
					if (croissant)
						p.translate(0, 1);
					else
						p.translate(0, -1);
					e += diago;
				} else
					e += horiz;
				// Dans tous les cas
				p.translate(1, 0);
			}
		} else {
			// direction principale Oy
			if (p1.getY() < p2.getY()) {
				p = new Point(p1);
				pFinal = new Point(p2);
			} else {
				p = new Point(p2);
				pFinal = new Point(p1);
			}
			int e = 2 * deltaX - deltaY;
			// Calcul des increments de e
			int verti = 2 * deltaX;
			int diago = 2 * (deltaX - deltaY);
			boolean croissant = p.getX() < pFinal.getX(); // x croissant ou
			// non
			for (int i = 0; i < deltaY; i++) {
				v.add(new Point(p));
				if (e > 0) {
					if (croissant)
						p.translate(1, 0);
					else
						p.translate(-1, 0);
					e += diago;
				} else
					e += verti;
				// Dans tous les cas
				p.translate(0, 1);
			}
		}
		// Tracer le dernier point...
		v.add(new Point(pFinal));
		return v.toArray(new Point[0]);
	}

	class Neighbourhood {
		int size;

		double[][] values;

		boolean constant;

		Neighbourhood(int size) {
			this.size = size;
			values = new double[size][size];
		}

		Neighbourhood(int size, double val) {
			this(size);
			fill(val);
		}

		void fill(double val) {
			for (int dx = 0; dx < size; dx++)
				for (int dy = 0; dy < size; dy++)
					values[dx][dy] = val;
		}

		void divide(double val) {
			if (val != 0)
				for (int dx = 0; dx < size; dx++)
					for (int dy = 0; dy < size; dy++)
						values[dx][dy] /= val;
		}

		void normalisation() {
			double min, max, diff;
			int dx, dy;
			// Extrema computation
			min = Double.MAX_VALUE;
			max = -Double.MAX_VALUE;
			for (dx = 0; dx < size; dx++)
				for (dy = 0; dy < size; dy++) {
					if (values[dx][dy] < min)
						min = values[dx][dy];
					if (values[dx][dy] > max)
						max = values[dx][dy];
				}
			diff = max - min;
			if (diff == 0) {
				diff = 1;
				constant = true;
			} else
				constant = false;
			// Normalisation
			for (dx = 0; dx < size; dx++)
				for (dy = 0; dy < size; dy++)
					values[dx][dy] = (values[dx][dy] - min) / diff;
		}

		void add(Neighbourhood n, double coeff) {
			if (n.size != size)
				return;
			for (int dx = 0; dx < size; dx++)
				for (int dy = 0; dy < size; dy++)
					values[dx][dy] += n.values[dx][dy] * coeff;
		}

		Point minimum() {
			double min = values[size / 2][size / 2] - epsilon;
			int xmin = size / 2, ymin = size / 2;
			int nbmin = 1;
			for (int dx = 0; dx < size; dx++)
				for (int dy = 0; dy < size; dy++)
					if (values[dx][dy] < min + epsilon) {
						nbmin = 1;
						xmin = dx;
						ymin = dy;
						min = values[dx][dy];
					}
					// check if several minima
					else if (values[dx][dy] == min)
						nbmin++;
			// Several minima, not the neighbourhood center, random choice
			if (nbmin > 1) {
				if (xmin != size / 2 && ymin != size / 2) {
					int selected = (int) (Math.random() * nbmin);
					if (DEBUG && DEBUG_MINIMA)
						System.out.println(selected + "/" + nbmin);
					int k = 0;
					for (int dx = 0; dx < size; dx++)
						for (int dy = 0; dy < size; dy++)
							if (values[dx][dy] == min) {
								if (k == selected) {
									xmin = dx;
									ymin = dy;
								}
								k++;
							}
				} else if (DEBUG && DEBUG_MINIMA)
					System.out.println("_" + "/" + nbmin);
			}
			int shift = (size - 1) / 2;
			return new Point(xmin - shift, ymin - shift);
		}

		public String toString() {
			StringBuffer str = new StringBuffer();
			for (int dx = 0; dx < size; dx++) {
				for (int dy = 0; dy < size; dy++)
					str.append((int) (values[dx][dy] * 100)).append(" ");
				str.append(" | ");
			}
			return str.toString();
		}

	}

}
