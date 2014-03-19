package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.edge.Sobel;
import fr.unistra.pelican.algorithms.geometric.Crop2D;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.snake.Snake;

/**
 * segment an image using a snake model
 */
public class SnakeSegmentation extends Algorithm {
	public Image inputImage;

	public Image outputImage;

	public Snake snake;

	public Point init1;

	public Point init2;

	public boolean writeImage=true;

	private boolean debug = false;

	/**
	 * Constructor
	 * 
	 */
	public SnakeSegmentation() {
		super.inputs = "inputImage,init1,init2";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// Define initial snake
		snake = new Snake(20, init1, init2);

		// Initialise the parameters
		int iterations = 50;
		int zoom = 10;
		snake.setNeighbourhood(3);
		snake.setZoom(zoom);
		snake.setGradientThreshold(150);
		snake.setColorThreshold(50);
		snake.setCoefficients(1, 1, 1, 1, 1, 1, 2);

		// Initialise the image data by limiting the processing area
		try {
			Image data =  Crop2D.exec(inputImage, init1.x - zoom, init1.y
					- zoom, init2.x + zoom, init2.y + zoom);
			data = inputImage;
			snake.setData(data);
			data = RGBToGray.exec(data);
			Image gradient = Sobel.exec(data, Sobel.NORM);
			snake.setGradient(gradient);
		} catch (PelicanException ex) {
			throw new AlgorithmException(ex.getMessage());
		}

		// debug info
		Image debugImage = null;
		int debugSampling = 1;
		if (debug) {
			debugImage = new ByteImage(inputImage.getXDim(), inputImage
					.getYDim(), iterations / debugSampling, inputImage
					.getTDim(), inputImage.getBDim());
			debugImage.copyAttributes(inputImage);

		}

		boolean convergence = false;
		int k;
		for (k = 0; k < iterations && !convergence; k++) {
			convergence = snake.deform();
			snake.crop(init1, init2);
			snake.deleteDoubles(false);
			// snake.deleteCrossings(false);
			snake.repareCrossings(false);
			if (debug)
				if (k % debugSampling == 0)
					debugImage.setImage4D(snake.drawOnImage(inputImage, true,
							true), k / debugSampling, Image.Z);
		}

		if (writeImage)
			outputImage = snake.drawOnImage(inputImage, true, true);
		else
			outputImage = null;

		if (debug) {
			System.out.println(k + " iterations");
			try {
				Viewer2D.exec(debugImage, "iterations");
			} catch (PelicanException ex) {
				throw new AlgorithmException(ex.getMessage());
			}

		}
	}

	public static void checkPoints(Image inputImage, Point init1, Point init2) {
		if (init1.x < 0)
			init1.x = 0;
		if (init1.x >= inputImage.getXDim())
			init1.x = inputImage.getXDim() - 1;
		if (init1.y < 0)
			init1.y = 0;
		if (init1.y >= inputImage.getYDim())
			init1.y = inputImage.getYDim() - 1;
		if (init2.x < 0)
			init2.x = 0;
		if (init2.x >= inputImage.getXDim())
			init2.x = inputImage.getXDim() - 1;
		if (init2.y < 0)
			init2.y = 0;
		if (init2.y >= inputImage.getYDim())
			init2.y = inputImage.getYDim() - 1;
	}

	public static void main(String args[]) throws PelicanException {
		Image image = (Image) new ImageLoader().process("samples/foot.png");
		Point p1 = new Point(image.getXDim() * 7 / 10, image.getYDim() * 4 / 10);
		Point p2 = new Point(image.getXDim() * 9 / 10, image.getYDim() * 6 / 10);
		Image output = (Image) new SnakeSegmentation().process(image, p1, p2);
		Viewer2D.exec(output, "snake");
	}

}
