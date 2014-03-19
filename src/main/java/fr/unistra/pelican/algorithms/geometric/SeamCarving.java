package fr.unistra.pelican.algorithms.geometric;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.edge.Sobel;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * This class performs image resizing using the Seam Carving algorithm
 * (doi:101145/1435417.1435437)
 * 
 * @author Lefevre
 */
public class SeamCarving extends Algorithm {

	/**
	 * The input image
	 */
	public Image input;

	/**
	 * The new X dimension
	 */
	public int xDim;

	/**
	 * The new Y dimension
	 */
	public int yDim;

	/**
	 * The output image
	 */
	public Image output;

	/**
	 * Default constructor
	 */
	public SeamCarving() {
		super.inputs = "input,xDim,yDim";
		super.outputs = "output";
	}

	/**
	 * Seam carving
	 * 
	 * @param input
	 *            The input image
	 * @param p1
	 *            The top left point
	 * @param p2
	 *            The bottom right point
	 * @return The output image
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Image> T exec(T input, int xDim, int yDim) {
		return (T) new SeamCarving().process(input, xDim, yDim);
	}

	/*
	 * distance function used in the computation of the energy map
	 */
	private int distance(int x1, int y1, int x2, int y2) {
		return Math.abs(input.getPixelXYByte(x1, y1)
				- input.getPixelXYByte(x2, y2));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		int xDelta = xDim - input.getXDim();
		int yDelta = yDim - input.getYDim();

		Image gradient = Sobel.exec(input);
		// post-processing of the gradient to avoid problems with borders
//		for(int x=0;x<gradient.getXDim();x++){
//			gradient.setPixelXYDouble(x,0, Double.MAX_VALUE);
//			gradient.setPixelXYDouble(x,gradient.getYDim()-1, Double.MAX_VALUE);
//		}
		for(int y=0;y<gradient.getYDim();y++){
			gradient.setPixelXYDouble(0,y, 200);
			gradient.setPixelXYDouble(gradient.getXDim()-1,y, 200);
		}
		Image energy = gradient.copyImage(true);
		Image path = new IntegerImage(input, false);
		double cost0, cost1, cost2, cost3;

		if (true) {
			// Removes one column (y--)

			for (int y = 1; y < input.getYDim(); y++) {
				// Case of x==0
				cost0 = energy.getPixelXYDouble(0, y);
				cost2 = energy.getPixelXYDouble(0, y - 1);
				cost3 = energy.getPixelXYDouble(1, y - 1);
				// Check for the minimum
				if (cost2 < cost3) {
					energy.setPixelXYDouble(0, y, cost2 + cost0);
					path.setPixelXYInt(0, y, 0);
				} else {
					energy.setPixelXYDouble(0, y, cost3 + cost0);
					path.setPixelXYInt(0, y, 1);
				}
				// Case of x==xDim-1
				int x2 = input.getXDim() - 1;
				cost0 = energy.getPixelXYDouble(x2, y);
				cost1 = energy.getPixelXYDouble(x2 - 1, y - 1);
				cost2 = energy.getPixelXYDouble(x2, y - 1);
				// Check for the minimum
				if (cost1 < cost2) {
					energy.setPixelXYDouble(x2, y, cost1 + cost0);
					path.setPixelXYInt(x2, y, -1);
				} else {
					energy.setPixelXYDouble(x2, y, cost2 + cost0);
					path.setPixelXYInt(x2, y, 0);
				}

				// Case of other y values
				for (int x = 1; x < input.getXDim() - 1; x++) {
					// Cost computation
					cost0 = energy.getPixelXYDouble(x, y);
					cost1 = energy.getPixelXYDouble(x - 1, y - 1);
					cost2 = energy.getPixelXYDouble(x, y-1);
					cost3 = energy.getPixelXYDouble(x + 1, y - 1);
					// Check for the minimum
					if (cost1 < cost2) {
						if (cost1 < cost3) {
							energy.setPixelXYDouble(x, y, cost1 + cost0);
							path.setPixelXYInt(x, y, -1);
						} else {
							energy.setPixelXYDouble(x, y, cost3 + cost0);
							path.setPixelXYInt(x, y, 1);
						}
					} else {
						if (cost2 < cost3) {
							energy.setPixelXYDouble(x, y, cost2 + cost0);
							path.setPixelXYInt(x, y, 0);
						} else {
							energy.setPixelXYDouble(x, y, cost3 + cost0);
							path.setPixelXYInt(x, y, 1);
						}
					}
				}
			}

			Viewer2D.exec(gradient,"sobel");
			Viewer2D.exec(GrayToPseudoColors.exec(gradient,true),"gradient");
			Viewer2D.exec(ContrastStretch.exec(energy));
			Viewer2D.exec(GrayToPseudoColors.exec(energy,true));

			// Draw the path
			double min = Double.MAX_VALUE;
			int y2 = input.getYDim() - 1;
			int xmin = -1;
			for (int x = 0; x < input.getXDim(); x++)
				if (energy.getPixelXYDouble(x, y2) < min) {
					min = energy.getPixelXYDouble(x, y2);
					xmin = x;
				}
			BooleanImage seam=new BooleanImage(input,false);
			System.out.println(xmin+" "+min);
			for (int y=y2;y>=0;y--) {
				System.out.println("Y="+y);
				seam.setPixelXYBoolean(xmin,y,true);
				xmin=xmin+path.getPixelXYInt(xmin,y);
				System.out.println(xmin);
			}
			Viewer2D.exec(seam);


		} else {
			// Removes one line (x--)
			for (int x = 1; x < input.getXDim(); x++) {
				// Case of y==0
				cost0 = energy.getPixelXYDouble(x, 0);
				cost2 = energy.getPixelXYDouble(x - 1, 0);
				cost3 = energy.getPixelXYDouble(x - 1, 1);
				// Check for the minimum
				if (cost2 < cost3) {
					energy.setPixelXYDouble(x, 0, cost2 + cost0);
					path.setPixelXYInt(x, 0, 0);
				} else {
					energy.setPixelXYDouble(x, 0, cost3 + cost0);
					path.setPixelXYInt(x, 0, 1);
				}
				// Case of y==yDim-1
				int y2 = input.getYDim() - 1;
				cost0 = energy.getPixelXYDouble(x, y2);
				cost1 = energy.getPixelXYDouble(x - 1, y2 - 1);
				cost2 = energy.getPixelXYDouble(x - 1, y2);
				// Check for the minimum
				if (cost1 < cost2) {
					energy.setPixelXYDouble(x, y2, cost1 + cost0);
					path.setPixelXYInt(x, y2, -1);
				} else {
					energy.setPixelXYDouble(x, y2, cost2 + cost0);
					path.setPixelXYInt(x, y2, 0);
				}

				// Case of other y values
				for (int y = 1; y < input.getYDim() - 1; y++) {
					// Cost computation
					cost0 = energy.getPixelXYDouble(x, y);
					cost1 = energy.getPixelXYDouble(x - 1, y - 1);
					cost2 = energy.getPixelXYDouble(x - 1, y);
					cost3 = energy.getPixelXYDouble(x - 1, y + 1);
					// Check for the minimum
					if (cost1 < cost2) {
						if (cost1 < cost3) {
							energy.setPixelXYDouble(x, y, cost1 + cost0);
							path.setPixelXYInt(x, y, -1);
						} else {
							energy.setPixelXYDouble(x, y, cost3 + cost0);
							path.setPixelXYInt(x, y, 1);
						}
					} else {
						if (cost2 < cost3) {
							energy.setPixelXYDouble(x, y, cost2 + cost0);
							path.setPixelXYInt(x, y, 0);
						} else {
							energy.setPixelXYDouble(x, y, cost3 + cost0);
							path.setPixelXYInt(x, y, 1);
						}
					}
				}
			}

			Viewer2D.exec(ContrastStretch.exec(energy));
			Viewer2D.exec(GrayToPseudoColors.exec(energy));

			// Draw the path
			double min = Double.MAX_VALUE;
			int x2 = input.getXDim() - 1;
			int ymin = -1;
			for (int y = 0; y < input.getYDim(); y++)
				if (energy.getPixelXYDouble(x2, y) < min) {
					min = energy.getPixelXYDouble(x2, y);
					ymin = y;
				}
			System.out.println(ymin);
			energy.fill(0);
			energy.setPixelXYDouble(x2, ymin, 1.0);
			Viewer2D.exec(GrayToPseudoColors.exec(energy));
		}

		// for(int y=0;y<input.getYDim();y++)
		// for (int x = 1; x < input.getXDim(); x++) {
		// // Case of y==0
		// cost2 = energy.getPixelXYByte(x - 1, 0);
		// cost3 = energy.getPixelXYByte(x - 1, 1) + distance(x - 1, 0, x, 1);
		// // Check for the minimum
		// if (cost2 < cost3) {
		// energy.setPixelXYByte(x, 0, cost2);
		// } else {
		// energy.setPixelXYByte(x, 0, cost3);
		// }
		//
		// // Case of y==yDim-1
		// int y2 = input.getYDim() - 1;
		// cost1 = energy.getPixelXYByte(x - 1, y2 - 1)
		// + distance(x - 1, y2, x, y2 - 1);
		// cost2 = energy.getPixelXYByte(x - 1, y2);
		// // Check for the minimum
		// if (cost1 < cost2) {
		// energy.setPixelXYByte(x, y2, cost1);
		// } else {
		// energy.setPixelXYByte(x, y2, cost2);
		// }
		//
		// // Case of other y values
		// for (int y = 1; y < input.getYDim() - 1; y++) {
		// // Cost computation
		// cost1 = energy.getPixelXYByte(x - 1, y - 1)
		// + distance(x, y - 1, x, y + 1)
		// + distance(x - 1, y, x, y - 1);
		// cost2 = energy.getPixelXYByte(x - 1, y)
		// + distance(x, y - 1, x, y + 1);
		// cost3 = energy.getPixelXYByte(x - 1, y + 1)
		// + distance(x, y - 1, x, y + 1)
		// + distance(x - 1, y, x, y + 1);
		// // Check for the minimum
		// if (cost1 < cost2) {
		// if (cost1 < cost3) {
		// energy.setPixelXYByte(x, y, cost1);
		// } else {
		// energy.setPixelXYByte(x, y, cost3);
		// }
		// } else {
		// if (cost2 < cost3) {
		// energy.setPixelXYByte(x, y, cost2);
		// } else {
		// energy.setPixelXYByte(x, y, cost3);
		// }
		// }
		// }
		// }
		// Viewer2D.exec(GrayToPseudoColors.exec(energy, true));

		Image output = input.newInstance(xDim, yDim, input.getZDim(), input
				.getTDim(), input.getBDim());
		output.copyAttributes(input);
	}

	public static void main(String[] args) {
		Image in = ImageLoader.exec("samples/queenstown.jpg");
		in = RGBToGray.exec(in);
		Viewer2D.exec(in, "input");
		Image out = SeamCarving.exec(in, in.getXDim(), in.getYDim() - 1);
		Viewer2D.exec(out, "output");
	}

}
