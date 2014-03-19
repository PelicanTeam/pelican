package fr.unistra.pelican.algorithms.spatial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Computes the distance transform of a binary image
 * 
 * @author Lefevre
 */
public class DistanceTransform extends Algorithm {

	/**
	 * Input image
	 */
	public Image input;

	/**
	 * Output image
	 */
	public IntegerImage output;

	/**
	 * (optionnally) flag to push background borders around the image
	 */
	public boolean border = false;

	/**
	 * Constructor
	 * 
	 */
	public DistanceTransform() {
		super.inputs = "input";
		super.outputs = "output";
		super.options = "border";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		output = new IntegerImage(input, false);
		xDim = input.getXDim();
		yDim = input.getYDim();
		zDim = input.getZDim();
		tDim = input.getTDim();
		bDim = input.getBDim();
		// Initialisation du masque de distance
		// a < b < c < d et d < a+b et c < 2a et e < min(b+c,a+d)
		int valMax = setDistanceMask(5, 7, 9, 11, 13);
		// int valMax=setDistanceMask(1,1,1,1,1);

		for (int i = 0; i < input.size(); i++)
			if (!input.getPixelBoolean(i))
				output.setPixelInt(i, valMax);
			else
				output.setPixelInt(i, 0);
		if (border)
			pushBorder(output, 5);
		// Premier parcours
		for (int b = 0; b < bDim; b++)
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							if (!input.getPixelBoolean(x, y, z, t, b)) {
								output.setPixelInt(x, y, z, t, b, getForwardDistance(x, y, z,
									t, b));
							}
		// Second parcours (inverse)
		for (int b = bDim - 1; b >= 0; b--)
			for (int t = tDim - 1; t >= 0; t--)
				for (int z = zDim - 1; z >= 0; z--)
					for (int y = yDim - 1; y >= 0; y--)
						for (int x = xDim - 1; x >= 0; x--)
							if (!input.getPixelBoolean(x, y, z, t, b)) {
								output.setPixelInt(x, y, z, t, b, getBackwardDistance(x, y, z,
									t, b));
							}
	}

	private int dist[][][][][] = new int[2][2][2][2][2];
	private int xDim, yDim, zDim, tDim, bDim;

	private void pushBorder(Image im, int v) {
		xDim = input.getXDim();
		yDim = input.getYDim();
		zDim = input.getZDim();
		tDim = input.getTDim();
		bDim = input.getBDim();
		// B
		if (bDim > 1)
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++) {
							im.setPixelInt(x, y, z, t, 0, v);
							im.setPixelInt(x, y, z, t, bDim - 1, v);
						}
		// T
		if (tDim > 1)
			for (int b = 0; b < bDim; b++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++) {
							im.setPixelInt(x, y, z, 0, b, v);
							im.setPixelInt(x, y, z, tDim - 1, b, v);
						}
		// Z
		if (zDim > 1)
			for (int b = 0; b < bDim; b++)
				for (int t = 0; t < tDim; t++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++) {
							im.setPixelInt(x, y, 0, t, b, v);
							im.setPixelInt(x, y, bDim - 1, t, b, v);
						}
		// Y
		if (yDim > 1)
			for (int b = 0; b < bDim; b++)
				for (int t = 0; t < tDim; t++)
					for (int z = 0; z < zDim; z++)
						for (int x = 0; x < xDim; x++) {
							im.setPixelInt(x, 0, z, t, b, v);
							im.setPixelInt(x, yDim - 1, z, t, b, v);
						}
		// X
		if (xDim > 1)
			for (int b = 0; b < bDim; b++)
				for (int t = 0; t < tDim; t++)
					for (int z = 0; z < zDim; z++)
						for (int y = 0; y < yDim; y++) {
							im.setPixelInt(0, y, z, t, b, v);
							im.setPixelInt(xDim - 1, y, z, t, b, v);
						}
	}

	private int setDistanceMask(int a, int b, int c, int d, int e) {
		dist[0][0][0][0][0] = 0;
		dist[1][0][0][0][0] = a;
		dist[0][1][0][0][0] = a;
		dist[0][0][1][0][0] = a;
		dist[0][0][0][1][0] = a;
		dist[0][0][0][0][1] = a;
		dist[1][1][0][0][0] = b;
		dist[1][0][1][0][0] = b;
		dist[1][0][0][1][0] = b;
		dist[1][0][0][0][1] = b;
		dist[0][1][1][0][0] = b;
		dist[0][1][0][1][0] = b;
		dist[0][1][0][0][1] = b;
		dist[0][0][1][1][0] = b;
		dist[0][0][1][0][1] = b;
		dist[0][0][0][1][1] = b;
		dist[0][0][1][1][1] = c;
		dist[0][1][0][1][1] = c;
		dist[0][1][1][0][1] = c;
		dist[0][1][1][1][0] = c;
		dist[1][0][0][1][1] = c;
		dist[1][0][1][0][1] = c;
		dist[1][0][1][1][0] = c;
		dist[1][1][0][0][1] = c;
		dist[1][1][0][1][0] = c;
		dist[1][1][1][0][0] = c;
		dist[1][1][1][1][0] = d;
		dist[1][1][1][0][1] = d;
		dist[1][1][0][1][1] = d;
		dist[1][0][1][1][1] = d;
		dist[0][1][1][1][1] = d;
		dist[1][1][1][1][1] = e;
		return max(xDim, yDim, zDim, tDim, bDim) * e;
	}

	private int max(int a, int b, int c, int d, int e) {
		int m = a;
		if (b > m)
			m = b;
		if (c > m)
			m = c;
		if (d > m)
			m = d;
		if (e > m)
			m = e;
		return m;
	}

	private int getForwardDistance(int x, int y, int z, int t, int b) {
		int min = output.getPixelInt(x, y, z, t, b);
		boolean stop = false;
		for (int bb = -1; bb <= 1; bb++)
			for (int tt = -1; tt <= 1; tt++)
				for (int zz = -1; zz <= 1; zz++)
					for (int yy = -1; yy <= 1; yy++)
						for (int xx = -1; xx <= 1; xx++)
							if (!stop) {
								if (xx == 0 && yy == 0 && zz == 0 && tt == 0 && bb == 0)
									stop = true;
								else if (x + xx >= 0 && y + yy >= 0 && z + zz >= 0
									&& t + tt >= 0 && b + bb >= 0 && x + xx < xDim
									&& y + yy < yDim && z + zz < zDim && t + tt < tDim
									&& b + bb < bDim) {
									min = Math.min(min, output.getPixelInt(x + xx, y + yy,
										z + zz, t + tt, b + bb)
										+ dist[Math.abs(xx)][Math.abs(yy)][Math.abs(zz)][Math
											.abs(tt)][Math.abs(bb)]);
									if (min == Integer.MIN_VALUE)
										System.out.println("xxx");
								}
							}
		return min;
	}

	private int getBackwardDistance(int x, int y, int z, int t, int b) {
		int min = output.getPixelInt(x, y, z, t, b);
		boolean stop = false;
		for (int bb = 1; bb >= -1; bb--)
			for (int tt = 1; tt >= -1; tt--)
				for (int zz = 1; zz >= -1; zz--)
					for (int yy = 1; yy >= -1; yy--)
						for (int xx = 1; xx >= -1; xx--)
							if (!stop) {
								if (xx == 0 && yy == 0 && zz == 0 && tt == 0 && bb == 0)
									stop = true;
								else if (x + xx >= 0 && y + yy >= 0 && z + zz >= 0
									&& t + tt >= 0 && b + bb >= 0 && x + xx < xDim
									&& y + yy < yDim && z + zz < zDim && t + tt < tDim
									&& b + bb < bDim) {
									min = Math.min(min, output.getPixelInt(x + xx, y + yy,
										z + zz, t + tt, b + bb)
										+ dist[Math.abs(xx)][Math.abs(yy)][Math.abs(zz)][Math
											.abs(tt)][Math.abs(bb)]);
								}
							}
		return min;
	}

	public static IntegerImage exec(Image input) {
		return (IntegerImage) new DistanceTransform().process(input);
	}

	public static IntegerImage exec(Image input, boolean border) {
		return (IntegerImage) new DistanceTransform().process(input, border);
	}

}
