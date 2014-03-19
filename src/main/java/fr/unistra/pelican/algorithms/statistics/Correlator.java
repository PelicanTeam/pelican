package fr.unistra.pelican.algorithms.statistics;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;

/**
 * This class correlates a given array of doubles according to the correlation matrix for
 * more details cf : Comer and Delp, Morphological operations for color image
 * processing, page 17
 * 
 * Besides the vector to correlate it also accepts the correlation deviation
 * 
 * @author Abdullah
 * 
 */
public class Correlator extends Algorithm
{
	/**
	 * The output vector
	 */
	public double[] output = null;

	/**
	 * The input vector
	 */
	public double[] vector;

	/**
	 * Correlation deviation
	 */
	public double[][] sigma;
	
	/**
	 * 
	 * @param vector the vector to correlate
	 * @param sigma correlation deviations
	 * @return the correlated vector
	 */
	public static double[] exec(double[] vector,double[][] sigma)
	{
		return (double[]) new Correlator().process(vector,sigma);
	}

	/**
	 * Constructor
	 * 
	 */
	public Correlator() {

		super();
		super.inputs = "vector,sigma";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		final int s = vector.length;

		// construct a 2-dim matrix from vecto
		double[][] tmp = new double[s][1];

		for (int i = 0; i < s; i++)
			tmp[i][0] = vector[i];

		Matrix Z = new Matrix(tmp, s, 1);

		Matrix M = new Matrix(sigma);

		EigenvalueDecomposition ev = new EigenvalueDecomposition(M);

		// get the eigenvalues
		double[] eigenvalues = ev.getRealEigenvalues();

		// construct the square root eigenvalues matrix
		double[][] lamda = new double[s][s];

		for (int i = 0; i < s; i++) {
			for (int j = 0; j < s; j++) {
				if (i == j) {
					lamda[i][j] = Math.sqrt(eigenvalues[i]);
					if (eigenvalues[i] < 0)
						System.err.println("zart");
				} else
					lamda[i][j] = 0.0;
			}
		}

		Matrix L = new Matrix(lamda);

		// get the eigenvector matrix
		Matrix V = ev.getV();

		Matrix R = (V.times(L)).times(Z);

		output = new double[s];

		for (int i = 0; i < s; i++)
			output[i] = R.get(i, 0);
	}
}