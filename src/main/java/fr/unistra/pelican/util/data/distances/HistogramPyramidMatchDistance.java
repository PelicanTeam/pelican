package fr.unistra.pelican.util.data.distances;

import fr.unistra.pelican.util.data.Data;



/**
 *	Pyramid match distance.
 *
 *	@author Erchan Aptoula (writing)
 *	@author Régis Witz (framework adaptation)
 *
 *	@see fr.unistra.pelican.util.Tools#pyramidMatchDistance(double[],double[],int,int)
 */
public class HistogramPyramidMatchDistance extends PyramidMatchDistance { 



	/**	Scales number. */
	public int scales = 3;
	/**	Size of each pyramid level. */
	public int levelSize = 7*3*3;



	@Override
	public double distance( Data data1, Data data2 ) { 

		// Get the values of both data
		Double[] values1 = ( Double[] ) data1.getValues();
		Double[] values2 = ( Double[] ) data2.getValues();
		// Check if both data have the same length
		int length = values1.length;
		if ( length != values2.length ) return -1;

		// Compute the histogram distance
		double distance = 0;
		for ( int s = 0 ; s < this.scales ; s++ )  
		for ( int i = 0 ; i < this.levelSize ; i++ ) { 

			int index = s * this.levelSize + i;
			distance += ( 1 / Math.pow( 2.,s ) ) 
						* Math.abs( values1[index]-values2[index] ) 
						/ ( 1 + values1[index] + values2[index] );
		}

		assert 0 <= distance && distance <= 1 : 
			this.getClass().getName() + " distance €[0;1] unverified : " + distance + ".";

		return distance;
	}



}
