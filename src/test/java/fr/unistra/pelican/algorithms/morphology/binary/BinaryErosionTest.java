package fr.unistra.pelican.algorithms.morphology.binary;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;



public class BinaryErosionTest { 



	@Test
	public void testBinaryErosion() { 

		Image image = fr.unistra.pelican.algorithms.io.ImageLoader.exec( "samples/binary.png" );
		image = new BooleanImage( image );
		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
		Image result = BinaryErosion.exec( image,se );

		System.out.println( "BinaryErosion ne plante pas." );

		Image truth = fr.unistra.pelican.algorithms.io.ImageLoader.exec( 
				"src/test/resources/binaryerosion-square5x5-truth.png" );
		assertEquals( truth.size(), result.size() );
		for ( int i = 0 ; i < truth.size() ; i++ ) 
			assertEquals( result.getPixelByte(i), truth.getPixelByte(i) );

		System.out.println( "BinaryErosion a passé le test unitaire !" );

	} // endfunc



	public static void main( String[] args ) { 

		new BinaryErosionTest().testBinaryErosion();

	} // endmain



}
