package fr.unistra.pelican.util.data.distances;

import java.util.ArrayList;

import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.data.Data;
import fr.unistra.pelican.util.data.KeypointArrayData;



/**
 *	@author Régis Witz
 */
public class KeypointArrayEuclideanDistance extends EuclideanDistance { 

	@Override
	@SuppressWarnings( "unchecked" )
	public double distance( Data d1, Data d2 ) { 

		ArrayList<Keypoint> values = 
			( ArrayList<Keypoint> ) ( ( KeypointArrayData ) d1 ).getValues();
		ArrayList<Keypoint> values2 = 
			( ArrayList<Keypoint> ) ( ( KeypointArrayData ) d2 ).getValues();

		int nbpoints = values.get(0).getDescLength();
		int nbpoints2 = values2.get(0).getDescLength();
		if( nbpoints != nbpoints2 ) { 

			System.err.println( "Incompatible keypoint descriptors lengths !" );
			return 1;
		}

		double distance = 0, d, min;
		for ( Keypoint k1 : values ) { 

			min = 1;
			for ( Keypoint k2 : values2 ) { 

				d = k1.data.distance( k2.data );
				if ( d < min ) min = d;
			}
			distance += min;
		}
		if ( nbpoints > 0 ) distance /= nbpoints;

		assert 0 <= distance && distance <= 1 : 
			this.getClass().getName() + " €[0;1] unverified : " + distance + ".";

		return distance;
	}



}
