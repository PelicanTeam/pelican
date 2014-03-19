package fr.unistra.pelican.util.data.distances;

import fr.unistra.pelican.util.data.Data;

/**
 * euclidean distance to be computed between double 2D arrays.
 * 
 * @author Jonathan Weber
 * 
 */
public class DoubleArray2DEuclideanDistance extends EuclideanDistance {

	@Override
	public double distance( Data data1, Data data2 ) { 

		// Get the values of both data
		Double[][] values  = ( Double[][] ) data1.getValues();
		Double[][] values2 = ( Double[][] ) data2.getValues();

		// Check if both data have the same length
		double length = values.length;
		if ( length != values2.length ) return -1;

		// Compute the weighted euclidean distance
		double sum = 0;
		for ( int i = 0 ; i < length ; i++ )
			for ( int j = 0 ; j < length ; j++ )
				sum += (values[i][j] - values2[i][j])*(values[i][j] - values2[i][j]);
		sum = Math.sqrt( sum );

		return sum;
	}

}

