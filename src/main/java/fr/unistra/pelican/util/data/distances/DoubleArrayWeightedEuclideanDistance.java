package fr.unistra.pelican.util.data.distances;

import fr.unistra.pelican.util.data.Data;

/**
 * Weighted euclidean distance to be computed between double arrays.
 * 
 * @author lefevre
 * 
 */
public class DoubleArrayWeightedEuclideanDistance extends
	WeightedEuclideanDistance {


	public DoubleArrayWeightedEuclideanDistance(Double[] weights) {
		super(weights);
	}

	@Override
	public double distance(Data data1, Data data2) {
		// Get the values of both data
		Double[] values=(Double[])data1.getValues();
		Double[] values2=(Double[])data2.getValues();
		// Check if both data and weight array have the same length  
		if (values.length != values2.length || values.length!=weights.length)
			return -1;
		// Compute the weighted euclidean distance
		double sum = 0;
		for (int i = 0; i < values.length; i++)
			sum += weights[i]*Math.pow(values[i]-values2[i],2);
		sum = Math.sqrt(sum);
		return sum;
	}

}
