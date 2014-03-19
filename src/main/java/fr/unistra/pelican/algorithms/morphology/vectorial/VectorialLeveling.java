package fr.unistra.pelican.algorithms.morphology.vectorial;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;
import fr.unistra.pelican.util.vectorial.orders.BinaryVectorialOrdering;

/**
 * Standard morphological levelling. A means of image simplification that preserves
 * the image borders.
 * 
 * TODO: lambda leveling support
 * 
 * @author Abdullah
 * 
 */
public class VectorialLeveling extends Algorithm
{
	/**
	 * the input image
	 */
	public Image input;

	/**
	 * the marker image
	 */
	public Image marker;

	/**
	 * lambda value
	 */
	public int lambda;

	/**
	 * a binary vectorial ordering
	 */
	public BinaryVectorialOrdering vo;

	/**
	 * the output image
	 */
	public Image output;

	private boolean DEBUG = false;
	
	/**
	 * This class performs a Standard morphological levelling.
	 * @param input the input image
	 * @param marker the marker image
	 * @param lambda the lambda value
	 * @param vo the vectorial ordering
	 * @return the levelled image
	 */
	public static Image exec(Image input, Image marker, Integer lambda,BinaryVectorialOrdering vo) {
		return (Image) new VectorialLeveling().process(input,marker,lambda,vo);
	}

	/**
	 * Constructor
	 * 
	 */
	public VectorialLeveling() {

		super();
		super.inputs = "input,marker,lambda,vo";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		// checking only the immediate 8-neighborhood
		BooleanImage se = FlatStructuringElement2D
				.createSquareFlatStructuringElement(3);
		Point4D[] points = se.foreground();

		Image tmp = null;

		output = marker.copyImage(true);

		int i = 0;

		do {
			tmp = output;

			output = level(output, points, se);

			if ( DEBUG ) System.err.println("Iteration " + (i++));

		} while (output.equals(tmp) == false);
	}

	private Image level(Image img, Point4D[] points, BooleanImage se) {
		Image tmp = img.copyImage(true);

		for (int x = 0; x < img.getXDim(); x++) {
			for (int y = 0; y < img.getYDim(); y++) {

				if ( !input.isPresentXYZT( x,y,0,0 ) ) continue;

				double[] g = tmp.getVectorPixelXYZTDouble(x, y, 0, 0);
				double[] f = input.getVectorPixelXYZTDouble(x, y, 0, 0);

				if (vo.compare(g, f) > 0)
					tmp.setVectorPixelXYZTDouble(x, y, 0, 0,
							getLowerLevelledGray(tmp, x, y, points, se));
				else if (vo.compare(g, f) < 0)
					tmp.setVectorPixelXYZTDouble(x, y, 0, 0,
							getUpperLevelledGray(tmp, x, y, points, se));
				else
					; // f == g do nothing
			}
		}

		return tmp;
	}

	private double[] getLowerLevelledGray(Image img, int x, int y,
			Point4D[] points, BooleanImage se) {
		double[][] vectorArray = new double[points.length][];
		int tmp = 0;

		for (int i = 0; i < points.length; i++) {
			int _x = x - se.getCenter().y + points[i].y;
			int _y = y - se.getCenter().x + points[i].x;

			if (	_x >= 0 && _x < input.getXDim() 
				 && _y >= 0 && _y < input.getYDim() 
				 && input.isPresentXYZT( _x,_y,0,0 ) )
				vectorArray[tmp++] = img.getVectorPixelXYZTDouble(_x, _y, 0, 0);
		}

		if (tmp == 0)
			return input.getVectorPixelXYZTDouble(x, y, 0, 0);

		else if (tmp < vectorArray.length) {
			double[][] tmpArray = new double[tmp][];

			for (int i = 0; i < tmp; i++)
				tmpArray[i] = vectorArray[i];

			vectorArray = tmpArray;

		}

		return vo.max(vo.min(vectorArray), input.getVectorPixelXYZTDouble(x,y,0,0));
	}

	private double[] getUpperLevelledGray(Image img, int x, int y,
			Point4D[] points, BooleanImage	 se) {
		double[][] vectorArray = new double[points.length][];
		int tmp = 0;

		for (int i = 0; i < points.length; i++) {
			int _x = x - se.getCenter().y + points[i].y;
			int _y = y - se.getCenter().x + points[i].x;

			if (	_x >= 0 && _x < input.getXDim() 
				 && _y >= 0 && _y < input.getYDim() 
				 && input.isPresentXYZT( _x,_y,0,0 ) )
				vectorArray[tmp++] = img.getVectorPixelXYZTDouble(_x, _y, 0, 0);
		}

		if (tmp == 0)
			return input.getVectorPixelXYZTDouble(x, y, 0, 0);

		else if (tmp < vectorArray.length) {
			double[][] tmpArray = new double[tmp][];

			for (int i = 0; i < tmp; i++)
				tmpArray[i] = vectorArray[i];

			vectorArray = tmpArray;

		}

		return vo.min(vo.max(vectorArray), input.getVectorPixelXYZTDouble(x, y,
				0, 0));
	}

}
