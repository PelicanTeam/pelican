package fr.unistra.pelican.util.data.distances;

import java.util.ArrayList;
import java.util.HashMap;

import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.data.Data;
import fr.unistra.pelican.util.data.KeypointArrayData;



/**
 *	Should probably be integrated to SURF ... 'will think about that later :/
 *	@author Régis Witz
 */
public class KeypointArraySURFDistance extends SURFDistance {

	@Override
	@SuppressWarnings("unchecked")
	public double distance( Data d1, Data d2 ) { 

		ArrayList<Keypoint> values = 
			( ArrayList<Keypoint> ) ( ( KeypointArrayData ) d1 ).getValues();
		ArrayList<Keypoint> values2 = 
			( ArrayList<Keypoint> ) ( ( KeypointArrayData ) d2 ).getValues();

		int nbpoints = values.size();
		int len1 = values.get(0).getDescLength();
		int len2 = values2.get(0).getDescLength();
		if( len1 != len2 ) { 

			System.err.println( "Incompatible keypoint descriptors lengths !" );
			return 1;
		}

		int matchescount = 0;	// total number of keypoints wich matched
		int match;
		for ( Keypoint key : values ) { 

			match = KeypointArraySURFDistance.match( key, values2 );
			if ( match > -1 ) matchescount++; // found one !
		}
		double distance = 1;
		if ( nbpoints > 0 ) distance = 1 - ( matchescount / nbpoints );

		assert 0 <= distance && distance <= 1 : 
			this.getClass().getName() + " ¤[0;1] unverified : " + distance + ".";

		return distance;
	}



	/**	Matches <tt>this</tt> with a list of interest points <tt>keys</tt>. If one, 
	 *	returns the position in <tt>keys</tt> of the closest interest point. 
	 *	@param point Point to match with <tt>points</tt>. 
	 *	@param points Points to be matched with <tt>point</tt>. 
	 *	@return The position of the closest keypoint, or -1 if there is no 
	 *	point wich is close enough to <tt>this</tt>. 
	 */
	public static int match( Keypoint point, ArrayList<Keypoint> points ) { 

		double mind = Double.MAX_VALUE, second = Double.MAX_VALUE;
		double v1,v2;
		int len, count = -1, match = -1;

		Double[] desc = ( Double[] ) point.data.getValues();
		Double[] desc2;

		for ( Keypoint key : points ) { 

			count++;

			desc2 = ( Double[] ) key.data.getValues();

			// take advantage of Laplacian to speed up matching
			if ( !desc2[1].equals( desc[1] ) ) continue;

			len = key.getDescLength();
			if ( len < 0 || len != point.getDescLength()  ) continue;

			// calculate the square distance between this.descriptor and key.descriptor
			double d = 0.;
			for ( int i = 3 ; i < len ; i++ ) { 

				v1 = desc2[i];
				v2 = desc[i];
				d += ( v1-v2 )*( v1-v2 );
			}

			if ( d < mind ) { 

				second = mind;
				mind = d;
				match = count;
			} else if ( d < second ) second = d;

		}

		if ( mind < 0.5 * second ) return match;

		return -1;
	}



	/**	Match a <tt>KeypointArrayData</tt> with <tt>this</tt> and get the correspondances 
	 *	between the keypoints who matched.
	 *	@param d1 A <tt>KeypointArrayData</tt> with wich <tt>d2</tt> must be compared.
	 *	@param d2 A <tt>KeypointArrayData</tt> with wich <tt>d1</tt> must be compared.
	 *	@return An {@link HashMap} : the keys are the matching points from <tt>this</tt> and 
	 *	the values are their echoes from <tt>data</tt>.
	 */
	@SuppressWarnings("unchecked")
	public HashMap<Keypoint,Keypoint> getMatches( Data d1, Data d2 ) { 

		ArrayList<Keypoint> values = 
			( ArrayList<Keypoint> ) ( ( KeypointArrayData ) d1 ).getValues();
		ArrayList<Keypoint> values2 = 
			( ArrayList<Keypoint> ) ( ( KeypointArrayData ) d2 ).getValues();
		int nbkp1 = values.size();
		int nbkp2 = values2.size();

		if( nbkp1 == 0 || nbkp2 == 0 ) return null;

		int kpsize = values.get(0).getDescLength();
		int kpsize2 = values2.get(0).getDescLength();
		if( kpsize != kpsize2 ) return null;

		HashMap<Keypoint,Keypoint> map = new HashMap<Keypoint,Keypoint>();
		int match;
		for ( Keypoint key : values ) { 

			match = KeypointArraySURFDistance.match( key, values2 );
			if ( match > -1 ) map.put( key, values2.get( match ) );
		}

		return map;
	}


}
