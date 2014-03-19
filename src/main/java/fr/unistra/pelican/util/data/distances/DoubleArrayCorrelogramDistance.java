package fr.unistra.pelican.util.data.distances;

import fr.unistra.pelican.util.data.Data;

/**
 * 
 *	@author Erchan Aptoula (writing)
 *	@author Régis Witz (framework adaptation)
 *
 *	@see fr.unistra.pelican.util.Tools#correlogramDistance(double[], double[])
 */
public class DoubleArrayCorrelogramDistance extends CorrelogramDistance {

	@Override
	public double distance( Data data1, Data data2 ) {

		Double[] values1 = ( Double[] ) data1.getValues();
		Double[] values2 = ( Double[] ) data2.getValues();

		int len = values1.length;
		if ( len != values2.length ) return 1;

		double distance = 0;
		for( int i = 0 ; i < len ; i++ ) { 
			distance += Math.abs( values1[i] - values2[i] ) / ( 1 + values1[i] + values2[i] );
		}

		assert 0 <= distance && distance <= 1 : 
			this.getClass().getName() + " €[0;1] unverified : " + distance + ".";

		return distance;
	}

}
