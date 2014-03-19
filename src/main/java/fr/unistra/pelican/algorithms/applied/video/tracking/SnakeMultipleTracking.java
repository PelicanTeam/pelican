package fr.unistra.pelican.algorithms.applied.video.tracking;

import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.segmentation.SnakeSegmentation;
import fr.unistra.pelican.util.snake.Snake;

/**
 * This class performs multiple object tracking using snake models initialized on
 * the first frame
 * 
 * S. Lefèvre, N. Vincent, Real time multiple object tracking based on active
 * contours, International Conference on Image Analysis and Recognition, Lecture
 * Notes in Computer Science, Vol. 3212, pages 606?613, Porto, Portugal, 2004.
 * 
 * @author Sébatien Lefèvre
 */
public class SnakeMultipleTracking extends Algorithm {

	/**
	 * The input image sequence
	 */
	public Image inputSequence;

	/**
	 * The first input points to define initial snakes
	 */
	public Point[] p1;

	/**
	 * The second input points to define initial snakes
	 */
	public Point[] p2;

	/**
	 * The strategy used in splitting step
	 */
	public int splitStrategy = Snake.SPLIT_EXTERN;
	/**
	 * The strategy used in merging step
	 */
	public int mergeStrategy = Snake.MERGE_CENTERS;

	/**
	 * The merging parameter value
	 */
	public double mergeParameter = 10;

	/**
	 * The minimum size to validate a snake
	 */
	public int checkSizeMin = 3;

	/**
	 * The minimum width to validate a snake
	 */
	public int checkWidthMin = 3;

	/**
	 * The minimum height to validate a snake
	 */
	public int checkHeightMin = 3;

	/**
	 * The minimum area to validate a snake
	 */
	public int checkAreaMin = 5;

	/**
	 * The additional size of the initial bounding box
	 */
	public int additionalSize = 10;
	
	/**
	 * The output image sequence
	 */
	public Image outputSequence;

	/**
	 * The snakes obtained for each frame (frame, snake index)
	 */
	public Snake[][] snakes;

	/**
	 * Default constructor
	 */
	public SnakeMultipleTracking() {
		super.inputs = "inputSequence,p1,p2";
		super.options = "splitStrategy,mergeStrategy,mergeParameter,checkSizeMin,checkWidthMin,checkHeightMin,checkAreaMin,additionalSize";
		super.outputs = "outputSequence,snakes";
	}

	/**
	 * performs multiple object tracking using snake models initialized on the
	 * first frame
	 * 
	 * @param input
	 *            The input image sequence
	 * @param p1
	 *            The first input points to define initial snakes
	 * @param p2
	 *            The second input points to define initial snakes
	 * @return The output image sequence
	 */
	public static Image exec(Image inputSequence, Point[] p1, Point[] p2) {
		return (Image) new SnakeMultipleTracking().process(inputSequence,p1,p2);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
		snakes = new Snake[inputSequence.getTDim()][];
		outputSequence = inputSequence.copyImage(false);
		outputSequence.copyAttributes(inputSequence);
		SnakeSegmentation segmentation = new SnakeSegmentation();
		Image inputImage, outputImage;
		Point pp1, pp2;
		Point[] array1 = p1;
		Point[] array2 = p2;
		ArrayList<Snake> tmp = new ArrayList<Snake>();
		for (int t = 0; t < inputSequence.getTDim(); t++) {
			// get the initial bounding boxes
			if (array1.length != array2.length)
				throw new AlgorithmException(
						"MultipleSnakeTracking : Arrays of initial points have not similar size");
			tmp.clear();
			// get current frame
			inputImage = inputSequence.getImage4D(t, Image.T);
			// process each initial snake
			for (int s = 0; s < array1.length; s++) {
				pp1 = array1[s];
				pp2 = array2[s];
				// launch segmentation on the current frame
				// Add the snake to the temporary results lists
				tmp.add((Snake) segmentation.processOne(1,inputImage,p1,p2,false));
			}
			// Do the split and merge process
			Snake[] results = tmp.toArray(new Snake[0]);
			results = Snake.split(results, splitStrategy);
			results = Snake.filter(results, checkSizeMin, checkWidthMin,
					checkHeightMin, checkAreaMin);
			if (results == null)
				results = tmp.toArray(new Snake[0]);
			results = Snake.merge(results, mergeParameter, mergeStrategy);
			// write the image result
			outputImage = Snake.draw(results, inputImage, true, true);
			outputSequence.setImage4D(outputImage, t, Image.T);
			// write the snakes results
			Snake.clean(results);
			snakes[t] = results;
			// Define initial snakes for next frame
			array1 = new Point[results.length];
			array2 = new Point[results.length];
			for (int r = 0; r < results.length; r++) {
				pp1 = results[r].getMin();
				pp2 = results[r].getMax();
				pp1.translate(-additionalSize, -additionalSize);
				pp2.translate(additionalSize, additionalSize);
				SnakeSegmentation.checkPoints(inputImage, pp1, pp2);
				array1[r] = pp1;
				array2[r] = pp2;
			}

		}

	}

}
