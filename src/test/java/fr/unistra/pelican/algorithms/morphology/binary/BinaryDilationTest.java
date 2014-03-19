package fr.unistra.pelican.algorithms.morphology.binary;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;



public class BinaryDilationTest { 



	@Test
	public void testBinaryDilation() { 

		Image image = fr.unistra.pelican.algorithms.io.ImageLoader.exec( "samples/binary.png" );
		image = new BooleanImage( image );
		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
		Image result = BinaryDilation.exec( image,se );

		System.out.println( "BinaryDilation ne plante pas." );

		Image truth = fr.unistra.pelican.algorithms.io.ImageLoader.exec( 
				"src/test/resources/binarydilation-square5x5-truth.png" );
		assertEquals( truth.size(), result.size() );
		for ( int i = 0 ; i < truth.size() ; i++ ) 
			assertEquals( result.getPixelByte(i), truth.getPixelByte(i) );

		System.out.println( "BinaryDilation a passé le test unitaire !" );

	} // endfunc



	public static void main(String[] args) { 

		new BinaryDilationTest().testBinaryDilation();

//		Image image = fr.unistra.pelican.algorithms.io.ImageLoader.exec( "samples/binary.png" );
//		Image compare = image.copyImage( true );
//		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( image,"image de départ" );
//		image = new BooleanImage( image );
//		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
//
//		Image truth = fr.unistra.pelican.algorithms.io.ImageLoader.exec( 
//				"src/test/resources/binarydilation-square5x5-truth.png");
//		int size = truth.size();
//		int xdim = truth.getXDim();
//		int ydim = truth.getYDim();
//		int tdim = truth.getTDim();
//		int bdim = truth.getBDim();
//		int zdim = truth.getZDim();
//
//		System.out.println( "A)" );
//		long start = System.currentTimeMillis();
//		Image base = BinaryDilation.exec( image,se );
//		long end = System.currentTimeMillis();
//
//		System.out.println( "B)" );
//		long starts = System.currentTimeMillis();
//		Image opt = BinaryDilation.exec( image,se, 
//				fr.unistra.pelican.algorithms.morphology.gray.GrayErosion.RECTANGLE_OPTIMIZATION );
//		long ends = System.currentTimeMillis();
//
//		System.out.println( "taille de l'image: "+xdim+","+ydim+","+zdim+","+tdim+","+bdim );
//		System.out.println( "machin de base: "+( end-start ) + "ms." );
//		System.out.println( "truc optimisé: "+( ends-starts ) + "ms." );
//
//////		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( image,"image grise" );
//////		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( base,"machin de base" );
//////		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( opt,"truc optimisé" );
//
//		assertEquals( xdim,base.getXDim(),opt.getXDim() );
//		assertEquals( ydim,base.getYDim(),opt.getYDim() );
//		assertEquals( zdim,base.getZDim(),opt.getZDim() );
//		assertEquals( tdim,base.getTDim(),opt.getTDim() );
//		assertEquals( bdim,base.getBDim(),opt.getBDim() );
//
//		for ( int t = 0 ; t < tdim ; t++ )
//		for ( int z = 0 ; z < zdim ; z++ )
//		for ( int y = 0 ; y < ydim ; y++ ) 
//		for ( int x = 0 ; x < xdim ; x++ )
//		for ( int b = 0 ; b < bdim ; b++ ) { 
//
//			if ( !image.isPresentXYZTB(x,y,z,t,b) ) continue;
//			if ( truth.getPixelXYZTBByte(x,y,z,t,b) != opt.getPixelXYZTBByte(x,y,z,t,b) ) { 
//
//				compare.setPixelXYZTBByte( x,y,z,t,0, 255 );
//				compare.setPixelXYZTBByte( x,y,z,t,1, 0 );
//				compare.setPixelXYZTBByte( x,y,z,t,2, 0 );
//			}
//		}
//		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( compare,"conflits" );
//
//		for ( int i = 0; i < size; i++ )
//			if ( !image.isPresent( i ) ) continue;
//			else assertEquals( truth.getPixelByte(i),opt.getPixelByte(i) );

	} // endmain



}
