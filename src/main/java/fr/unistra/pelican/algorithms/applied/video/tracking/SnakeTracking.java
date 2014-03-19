package fr.unistra.pelican.algorithms.applied.video.tracking;

import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.segmentation.SnakeSegmentation;
import fr.unistra.pelican.util.snake.Snake;

/**
 * This class performs single object tracking using a snake model initialized on
 * the first frame
 * 
 * S. Lefèvre, N. Vincent, Real time multiple object tracking based on active
 * contours, International Conference on Image Analysis and Recognition, Lecture
 * Notes in Computer Science, Vol. 3212, pages 606?613, Porto, Portugal, 2004.
 * 
 * @author Sébatien Lefèvre
 */
public class SnakeTracking extends Algorithm {

	/**
	 * The input image sequence
	 */
	public Image inputSequence;

	/**
	 * The first input point to define initial snake
	 */
	public Point p1;

	/**
	 * The second input point to define initial snake
	 */
	public Point p2;

	/**
	 * The output image sequence
	 */
	public Image outputSequence;

	/**
	 * The snake obtained for each frame
	 */
	public Snake[] snakes;

	/**
	 * Default constructor
	 */
	public SnakeTracking() {
		super.inputs = "inputSequence,p1,p2";
		super.outputs = "outputSequence,snakes";
	}

	/**
	 * performs single object tracking using a snake model initialized on the
	 * first frame
	 * 
	 * @param inputSequence
	 *            The input image sequence
	 * @param p1
	 *            The first input point to define initial snake
	 * @param p2
	 *            The second input point to define initial snake
	 * @return The output image sequence
	 */
	public static Image exec(Image inputSequence, Point p1, Point p2) {
		return (Image) new SnakeTracking().process(inputSequence, p1, p2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		snakes = new Snake[inputSequence.getTDim()];
		outputSequence = inputSequence.copyImage(false);
		SnakeSegmentation segmentation = new SnakeSegmentation();
		Snake snake;
		Image inputImage, outputImage;
		Point pp1 = this.p1;
		Point pp2 = this.p2;
		int zoom = 10;
		for (int t = 0; t < inputSequence.getTDim(); t++) {
			// get current frame
			inputImage = inputSequence.getImage4D(t, Image.T);
			// launch segmentation on the current frame
			ArrayList<Object> v = segmentation.processAll(inputImage, pp1, pp2, true);
			outputImage = (Image) v.get(0);
			snake = (Snake) v.get(1);
			// write the result
			outputSequence.setImage4D(outputImage, t, Image.T);
			snake.clean();
			snakes[t] = snake;
			// Define initial snake for next frame
			pp1 = snake.getMin();
			pp2 = snake.getMax();
			pp1.translate(-zoom, -zoom);
			pp2.translate(zoom, zoom);
		}
	}

}
