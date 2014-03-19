package fr.unistra.pelican.algorithms.spatial;

import java.awt.Point;
import java.util.LinkedList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.HierarchicalQueue;
import fr.unistra.pelican.util.Tools;

/**
 * This class realize a topographic distance transform using a FIFO queue as in
 * watershed segmentation
 * 
 * @author Lefevre
 */
public class TopographicTransform extends Algorithm {

	/*
	 * Input Image
	 */
	public Image inputImage;

	public BooleanImage mask;

	public boolean trueDistance = true;

	/**
	 * (optionnally) flag to push background borders around the image
	 */
	public boolean border = false;

	/**
	 * Flag to compute hue-based distance
	 */
	public boolean hue = false;
	
	public HierarchicalQueue queue=null;

	/*
	 * Output Image
	 */
	public IntegerImage outputImage;

	private final int NULL = 0;

	private int xdim = 0;
	private int ydim = 0;
	private BooleanImage mask2 = null;

	/**
	 * Constructor
	 * 
	 */
	public TopographicTransform() {
		super.inputs = "inputImage,mask";
		super.options = "trueDistance,border,hue,queue";
		super.outputs = "outputImage";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		xdim = inputImage.getXDim();
		ydim = inputImage.getYDim();
		IntegerImage output = new IntegerImage(inputImage.getXDim(), inputImage
			.getYDim(), 1, 1, 3);// label, distance, candidate
		outputImage = new IntegerImage(inputImage.getXDim(), inputImage.getYDim(),
			inputImage.getZDim(), inputImage.getTDim(), 1);

		int scale = Math.max(inputImage.getXDim(), inputImage.getYDim());// 500;
		if (queue==null)
			queue = new HierarchicalQueue(scale * scale * 255);
		else queue.reset();
			
		mask2 = mask.copyImage(true);
		if (border) {
			for (int x = 0; x < xdim; x++) {
				mask2.setPixelXYBoolean(x, 0, true);
				mask2.setPixelXYBoolean(x, ydim - 1, true);
			}
			for (int y = 0; y < ydim; y++) {
				mask2.setPixelXYBoolean(0, y, true);
				mask2.setPixelXYBoolean(xdim - 1, y, true);
			}
		}
		//Viewer2D.exec(mask2,"masque bord="+border);
		// Put the marker pixels in the queue
		for (int y = 0; y < ydim; y++)
			for (int x = 0; x < xdim; x++) {
				if (mask2.getPixelXYBoolean(x, y)) {
					output.setPixelXYBInt(x, y, 0, 0); // label 0 le bord
					output.setPixelXYBInt(x, y, 1, 0); // distance 0
					if (bord(x, y))
						queue.add(new Point(x, y), NULL);
					else
						output.setPixelXYBInt(x, y, 0, 1); // label 1
				}
			}
		//Viewer2D.exec(output.scaleToVisibleRange(),"marqueurs bord="+border);
		
		// Perform the flooding
		int labeled = 0;
		int current=0;
		Point p=null;
		while (!queue.isEmpty()) {
			current=queue.getCurrent();
			p = queue.get();
			// System.out.println(p.x+" "+p.y);
			// Get the label and check if it has not been labeled before
			if (output.getPixelXYBInt(p.x, p.y, 0) != NULL)
				continue;
			if (labeled % (inputImage.size() / inputImage.getBDim() / 10) == 0)
				System.out.print('.');
			// Definitely set the label from the candidate
			//int label = output.getPixelXYBInt(p.x, p.y, 2);
			int label=1;
			output.setPixelXYBInt(p.x, p.y, 0, label);
			labeled++;
			// get the non labelled 8-neighbours of (x,y)
			Point[] neighbours = getNonLabelledNeighbours(output, p.x, p.y);
			for (int i = 0; i < neighbours.length; i++) {
				// get the current distance for this neighbour
				int ndist = output.getPixelXYBInt(neighbours[i].x, neighbours[i].y, 1);
				// compute the geodesic distance between p and its
				// neighbor IN THE APPROPRIATE BAND
				double val = 0;
				if (!hue) {
					for (int b = 0; b < inputImage.getBDim(); b++) {
						double val1 = inputImage.getPixelXYBDouble(neighbours[i].x,
							neighbours[i].y, b);
						double val2 = inputImage.getPixelXYBDouble(p.x, p.y, b);
						val += (val1 - val2)*(val1-val2);
//					int val1 = inputImage.getPixelXYBByte(neighbours[i].x,
//						neighbours[i].y, b);
//					int val2 = inputImage.getPixelXYBByte(p.x, p.y, b);
//					val += Math.abs(val1 - val2);
				}
					val/=inputImage.getBDim();
					val=Math.sqrt(val);
				val*=255;

					} else {
					// compute hue-base distance
					val = Tools.HSLDistance(inputImage.getVectorPixelXYZDouble(
						neighbours[i].x, neighbours[i].y,0), inputImage
						.getVectorPixelXYZDouble(p.x, p.y, 0));
					val=Math.ceil(255*val);
					if (val == 0
						&& Tools.HSLDistance(inputImage.getVectorPixelXYZDouble(
							neighbours[i].x, neighbours[i].y, 0), inputImage
							.getVectorPixelXYZDouble(p.x, p.y, 0)) != 0)
						System.out.println(val);
				}
				if (trueDistance)
					val = scale * val + 1;// val += 1; // pour la distance
																// topographique de Philipp
				if(mask2.getPixelXYBoolean(p.x,p.y))
					val=0;
				
				int pdist = (int)val + current;//queue.getCurrent();
				// update distance and candidate if necessary
				if (ndist == 0 || pdist < ndist) {
					output.setPixelXYBInt(neighbours[i].x, neighbours[i].y, 1, pdist);
					//output.setPixelXYBInt(neighbours[i].x, neighbours[i].y, 2, label);
					// add him to the appropriate queue
					queue.add(neighbours[i], pdist);
				}

			}
		}

		outputImage.setImage4D(output.getImage4D(1, Image.B), 0, Image.B);

		return;
	}

	private boolean bord(int x, int y) {
		boolean bord = false;
		if (x > 0 && !mask2.getPixelXYBoolean(x - 1, y))
			bord = true;
		else if (x < xdim - 1 && !mask2.getPixelXYBoolean(x + 1, y))
			bord = true;
		else if (y > 0 && !mask2.getPixelXYBoolean(x, y - 1))
			bord = true;
		else if (y < ydim - 1 && !mask2.getPixelXYBoolean(x, y + 1))
			bord = true;
		if (x > 0 && y > 0 && !mask2.getPixelXYBoolean(x - 1, y - 1))
			bord = true;
		if (x < xdim - 1 && y > 0 && !mask2.getPixelXYBoolean(x + 1, y - 1))
			bord = true;
		if (x > 0 && y < ydim - 1 && !mask2.getPixelXYBoolean(x - 1, y + 1))
			bord = true;
		if (x < xdim - 1 && y < ydim - 1 && !mask2.getPixelXYBoolean(x + 1, y + 1))
			bord = true;
		return bord;
	}

	private Point[] getNonLabelledNeighbours(IntegerImage output, int x, int y) {
		Point[] neighbours = new Point[8];

		int cnt = 0;

		for (int j = y - 1; j <= y + 1; j++) {
			for (int i = x - 1; i <= x + 1; i++) {
				if (i < 0 || i >= output.getXDim() || j < 0 || j >= output.getYDim())
					continue;
				int z = output.getPixelXYBInt(i, j, 0);

				if (!(i == x && j == y) && z == NULL)
					neighbours[cnt++] = new Point(i, j);

			}
		}

		if (cnt < 8) {
			Point[] tmp = new Point[cnt];

			for (int i = 0; i < cnt; i++)
				tmp[i] = neighbours[i];

			neighbours = tmp;
		}

		return neighbours;
	}

	
	public static void main(String args[]) {
		Image rgb = ImageLoader.exec("samples/billes.png");
		BooleanImage mask = new BooleanImage(rgb.getXDim(), rgb.getYDim(), 1, 1, 1);
		mask.setPixelXYBoolean(11, 11, true);
		mask.setPixelXYBoolean(11, 12, true);
		mask.setPixelXYBoolean(12, 11, true);
		mask.setPixelXYBoolean(12, 12, true);
//		rgb=ImageLoader.exec("samples/simple.png");
//		mask=ManualThresholding.exec(rgb,0.5);
//		Viewer2D.exec(rgb, "Original image (RGB)");
//		Viewer2D.exec(mask, "Binary mask");
		Viewer2D
			.exec(GrayToPseudoColors.exec(TopographicTransform.exec(rgb, mask)),
				"distance");
		Viewer2D.exec(GrayToPseudoColors.exec(TopographicTransform.exec(rgb, mask,
			true, true)), "distance avec bords");
		Viewer2D.exec(GrayToPseudoColors.exec(TopographicTransform.exec(rgb, mask,
			false)), "pseudo-distance");
		Viewer2D.exec(GrayToPseudoColors.exec(TopographicTransform.exec(rgb, mask,
			false, true)), "pseudo-distance avec bords");
	}

	/**
	 * See header.
	 */
	public static IntegerImage exec(Image input, BooleanImage mask) {
		return (IntegerImage) new TopographicTransform().process(input, mask);
	}

	public static IntegerImage exec(Image input, BooleanImage mask,
		boolean trueDistance) {
		return (IntegerImage) new TopographicTransform().process(input, mask,
			trueDistance);
	}

	public static IntegerImage exec(Image input, BooleanImage mask,
		boolean trueDistance, boolean border) {
		return (IntegerImage) new TopographicTransform().process(input, mask,
			trueDistance, border);
	}

	public static IntegerImage exec(Image input, BooleanImage mask,
		boolean trueDistance, boolean border,HierarchicalQueue queue) {
		return (IntegerImage) new TopographicTransform().process(input, mask,
			trueDistance, border,queue);
	}

	public static IntegerImage exec(Image input, BooleanImage mask,
		boolean trueDistance, boolean border,boolean hue) {
		return (IntegerImage) new TopographicTransform().process(input, mask,
			trueDistance, border,hue);
	}

	public static IntegerImage exec(Image input, BooleanImage mask,
		boolean trueDistance, boolean border,boolean hue,HierarchicalQueue queue) {
		return (IntegerImage) new TopographicTransform().process(input, mask,
			trueDistance, border,hue,queue);
	}

}
