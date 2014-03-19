package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;


/*

 EA (13.03.05)

 - total rewrite..same algo (Vincent-Soille)
 - added multiband support
 - eliminated getBin method...use WatershedLine algorithm instead
 
 JW (21.03.10)
 
 - convert to ArrayList
 - merging of watershed lines into existing regions

 TODO

 - dont ignore ArrayIndexOutOfBoundsException...
 calculate the proper array instead
 - Biggest region merging

 FIXME (Lefevre) remis dans PELICAN car "bonnes frontières"


 */

public class Watershed2 extends Algorithm {
	
	/**
	 * Original Image (often a gradient)
	 */
	public Image input;
	/**
	 * Method used for merging watershed lines
	 * into regions (optional)
	 */
	public int wshedMergingMethod=NONE;
	/**
	 * Watershed result
	 */
	public IntegerImage output;

	private int xDim;
	private int yDim;
	private int bDim;

	public static final int WSHED = 0;
	private static final int INIT = -3;
	private static final int MASK = -2;
	private static final int INQUEUE = -1;
	
	public static final int NONE = -1;
	public static final int CLOSESTVALUE = 0;
	public static final int BIGGESTREGION = 1;

	public Watershed2() {
		super.inputs = "input";
		super.options = "wshedMergingMethod";
		super.outputs = "output";
	}

	public static IntegerImage exec(Image input) {
		return (IntegerImage) new Watershed2().process(input);
	}
	
	public static IntegerImage exec(Image input, int watershedMerging) {
		return (IntegerImage) new Watershed2().process(input,watershedMerging);
	}

	public void launch() {
		xDim = input.getXDim();
		yDim = input.getYDim();
		bDim = input.getBDim();

		// initialize labels to INIT
		output = new IntegerImage(xDim, yDim, 1, 1, bDim);
		output.fill(INIT);

		for (int b = 0; b < bDim; b++) {

			boolean flag = false;
			int x, y;
			Fifo fifo = new Fifo();
			Point p;

			int currentLabel = WSHED;

			// pixel value distribution,
			// so that we dont have to check the entire image
			ArrayList<ArrayList<Point>> distro = calculateDistro(b);

			for (int i = 0; i < 256; i++) {

				// geodesic SKIZ of level i - 1 inside level i
				ArrayList<Point> pointsList = distro.get(i);
				int size = pointsList.size();

				for (int j = 0; j < size; j++) {
					p = pointsList.get(j);

					x = (int) p.getX();
					y = (int) p.getY();

					output.setPixelXYBInt(x, y, b, MASK);

					if (areThereLabelledNeighbours(x, y, b) == true) {
						output.setPixelXYBInt(x, y, b, INQUEUE);
						fifo.add(p);
					}
				}

				while (fifo.isEmpty() == false) {
					p = fifo.retrieve();
					x = (int) p.getX();
					y = (int) p.getY();

					// for every pixel in the 8-neighbourhood of p
					for (int j = y - 1; j <= y + 1; j++) {
						for (int k = x - 1; k <= x + 1; k++) {

							if (k < 0 || k >= xDim || j < 0 || j >= yDim)
								continue;

							// if the pixel is already labelled
							if (!(j == y && k == x)
									&& output.getPixelXYBInt(k, j, b) > WSHED) {
								if (output.getPixelXYBInt(x, y, b) == INQUEUE
										|| (output.getPixelXYBInt(x, y, b) == WSHED && flag == true))
									output.setPixelXYBInt(x, y, b, output
											.getPixelXYBInt(k, j, b));

								else if (output.getPixelXYBInt(x, y, b) > WSHED
										&& output.getPixelXYBInt(x, y, b) != output
												.getPixelXYBInt(k, j, b)) {
									output.setPixelXYBInt(x, y, b, WSHED);
									flag = false;
								}
							} else if (output.getPixelXYBInt(k, j, b) == WSHED
									&& output.getPixelXYBInt(x, y, b) == INQUEUE) {
								output.setPixelXYBInt(x, y, b, WSHED);
								flag = true;
							} else if (output.getPixelXYBInt(k, j, b) == MASK) {
								output.setPixelXYBInt(k, j, b, INQUEUE);
								fifo.add(new Point(k, j));
							}
						}
					}
				}

				// check for new minima
				size = pointsList.size();

				for (int j = 0; j < size; j++) {
					p = pointsList.get(j);

					x = (int) p.getX();
					y = (int) p.getY();

					if (output.getPixelXYBInt(x, y, b) == MASK) {
						currentLabel++;
						fifo.add(p);
						output.setPixelXYBInt(x, y, b, currentLabel);

						while (fifo.isEmpty() == false) {
							p = fifo.retrieve();
							x = (int) p.getX();
							y = (int) p.getY();

							// for every pixel in the 8-neighbourhood of p
							for (int l = y - 1; l <= y + 1; l++) {
								for (int k = x - 1; k <= x + 1; k++) {
									if (k < 0 || k >= xDim || l < 0
											|| l >= yDim)
										continue;
									if (!(k == x && l == y)
											&& output.getPixelXYBInt(k, l, b) == MASK) {
										fifo.add(new Point(k, l));
										output.setPixelXYBInt(k, l, b,
												currentLabel);
									}
								}
							}
						}
					}
				}
			}
		}
		switch(wshedMergingMethod)
		{
			case CLOSESTVALUE: 	wshedMergedInRegionOfClosestValue();
								break;
			case BIGGESTREGION : System.err.println("This merging is not managed yet"); 
								break;
		}		
	}
	
	private void wshedMergedInRegionOfClosestValue()
	{
		IntegerImage newWSHEDValues = output.newIntegerImage();
		newWSHEDValues.fill(-1);
		for(int y=0;y<yDim;y++)
			for(int x=0;x<xDim;x++)
			{
				if(output.getPixelXYInt(x, y)==WSHED)
				{
					int smallestDistance=Integer.MAX_VALUE;
					int closestRegion=-1;
					int localValue = input.getPixelXYByte(x, y);
					for(int yN=-1;yN<=1;yN++)
						for(int xN=-1;xN<=1;xN++)
						{
							int xLoc=x+xN;
							int yLoc=y+yN;
							if(xLoc>=0&&xLoc<xDim&&yLoc>=0&&yLoc<yDim)
							{
								if(output.getPixelXYInt(xLoc, yLoc)!=WSHED)
								{
									int localDistance=Math.abs(input.getPixelXYByte(xLoc, yLoc)-localValue);
									if(localDistance<smallestDistance)
									{
										smallestDistance=localDistance;
										closestRegion=output.getPixelXYInt(xLoc, yLoc);
									}
								}
							}
						}
					// A watershed pixel surrounded by watershed pixel, weird but it happens...
					if(closestRegion==-1)
					{
						for(int yN=-1;yN<=1;yN++)
							for(int xN=-1;xN<=1;xN++)
							{
								int xLoc=x+xN;
								int yLoc=y+yN;
								if(xLoc>=0&&xLoc<xDim&&yLoc>=0&&yLoc<yDim)
								{
									if(newWSHEDValues.getPixelXYInt(xLoc,yLoc)!=-1)
									{
										int localDistance=Math.abs(input.getPixelXYByte(xLoc, yLoc)-localValue);
										if(localDistance<smallestDistance)
										{
											smallestDistance=localDistance;
											closestRegion=newWSHEDValues.getPixelXYInt(xLoc, yLoc);
										}
									}
								}
							}
					} //End of watershed surrounded watershed pixel treatment
					newWSHEDValues.setPixelXYInt(x, y, closestRegion);					
				}
			}
		for(int i=0;i<newWSHEDValues.size();i++)
		{
			if(newWSHEDValues.getPixelInt(i)!=-1)
			{
				output.setPixelInt(i, newWSHEDValues.getPixelInt(i));
			}
		}
	}

	private ArrayList<ArrayList<Point>> calculateDistro(int b) {
		ArrayList<ArrayList<Point>> distro = new ArrayList<ArrayList<Point>>();

		for (int i = 0; i < 256; i++)
			distro.add(new ArrayList<Point>());

		for (int x = 0; x < xDim; x++) {
			for (int y = 0; y < yDim; y++)
				distro.get(input.getPixelXYBByte(x, y, b)).add(new Point(x, y));
		}
		return distro;
	}

	private boolean areThereLabelledNeighbours(int x, int y, int b) {
		for (int j = y - 1; j <= y + 1; j++) {
			for (int i = x - 1; i <= x + 1; i++) {
				if (i < 0 || i >= xDim || j < 0 || j >= yDim)
					continue;

				if (!(i == x && j == y)
						&& output.getPixelXYBInt(i, j, b) >= WSHED)
					return true;
			}
		}
		return false;
	}

	private class Fifo {
		private ArrayList<Point> v;

		Fifo() {
			v = new ArrayList<Point>();
		}

		void add(Point o) {
			v.add(o);
		}

		Point retrieve() {
			Point o = v.get(0);
			v.remove(0);

			return o;
		}

		boolean isEmpty() {
			return v.size() == 0;
		}
	}

}
