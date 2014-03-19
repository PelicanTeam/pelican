package fr.unistra.pelican.util.data.distances;

import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Pixel;
import fr.unistra.pelican.util.data.Data;
import fr.unistra.pelican.util.data.MatrixData;

/**
 *	Computes an euclidean distance between matrixes. In that particular case, 
 *	NxM matrixes are considered as vectors with lengths of NxM ...
 * 
 *	@author Régis Witz
 */
public class MatrixEuclideanDistance extends EuclideanDistance {

	@Override
	public double distance( Data data1, Data data2 ) { 

		IntegerImage matrix1 = ( IntegerImage ) ( ( MatrixData ) data1 ).getValues();
		IntegerImage matrix2 = ( IntegerImage ) ( ( MatrixData ) data2 ).getValues();
		if ( matrix1.size() != matrix2.size() ) return -1.;

//		System.out.println( "sizes okay." );
		int p1,p2;
		double distance = 0;
		try { 
//			// that's maybe a bit brutal, but if this and data are matrix of different sizes, 
//			// we might have at sometimes an ArrayIndexOutOfBoundsException thrown.
//			// in that case, the matrixes are considered very distant from each other ...

			int c = 1;
			for ( Pixel p : matrix1 ) { 

//				System.out.println( (c++) + " / " + matrix1.size() );
				p1 = matrix1.getPixelXYZTBInt( p.x,p.y,p.z,p.t,p.b );
				p2 = matrix2.getPixelXYZTBInt( p.x,p.y,p.z,p.t,p.b );

				distance += Math.pow( p1-p2, 2 );
			}

		} catch ( ArrayIndexOutOfBoundsException ex ) { return -1.; }

		distance = Math.sqrt( distance );
		System.out.println( "done: "+distance+"." );
		return distance;
	}

}
