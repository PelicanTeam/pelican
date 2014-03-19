package fr.unistra.pelican.algorithms.morphology.gray;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;



public class GrayDilationTest { 



	@Test
	public void testGrayDilation() { 

		Image image = ImageLoader.exec("src/test/resources/watershed.png");
		if ( image.getBDim() != 1 ) image = RGBToGray.exec( image );
		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
		Image result = GrayDilation.exec( image,se );

		Image truth = ImageLoader.exec("src/test/resources/graydilation-square5x5-truth.png");

		System.out.println( "Ceci indique que la dilatation plante pas. " +
							"C'est le résultat qu'est pas bon." );
 
		assertEquals( truth.size(), result.size() );

		for ( int i = 0 ; i < truth.size() ; i++ ) 
			assertEquals( result.getPixelByte(i), truth.getPixelByte(i) );

	} // endfunc



	public static void main(String[] args) { 

		new GrayDilationTest().testGrayDilation();

		Image image = ImageLoader.exec( "src/test/resources/watershed.png" );
		Image compare = image.copyImage( true );
		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( image,"image de départ" );
		if ( image.getBDim() != 1 ) image = RGBToGray.exec( image );
		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);

		Image truth = ImageLoader.exec("src/test/resources/graydilation-square5x5-truth.png");
		int size = truth.size();
		int xdim = truth.getXDim();
		int ydim = truth.getYDim();
		int tdim = truth.getTDim();
		int bdim = truth.getBDim();
		int zdim = truth.getZDim();

		System.out.println( "A)" );
		long start = System.currentTimeMillis();
		Image base = GrayDilation.exec( image,se );
		long end = System.currentTimeMillis();

		System.out.println( "B)" );
		long starts = System.currentTimeMillis();
		Image opt = GrayDilation.exec( image,se,null,GrayErosion.RECTANGLE_OPTIMIZATION );
		long ends = System.currentTimeMillis();

		System.out.println( "taille de l'image: "+xdim+","+ydim+","+zdim+","+tdim+","+bdim );
		System.out.println( "machin de base: "+( end-start ) + "ms." );
		System.out.println( "truc optimisé: "+( ends-starts ) + "ms." );

////		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( image,"image grise" );
////		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( base,"machin de base" );
////		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( opt,"truc optimisé" );

		assertEquals( xdim,base.getXDim(),opt.getXDim() );
		assertEquals( ydim,base.getYDim(),opt.getYDim() );
		assertEquals( zdim,base.getZDim(),opt.getZDim() );
		assertEquals( tdim,base.getTDim(),opt.getTDim() );
		assertEquals( bdim,base.getBDim(),opt.getBDim() );

		for ( int t = 0 ; t < tdim ; t++ )
		for ( int z = 0 ; z < zdim ; z++ )
		for ( int y = 0 ; y < ydim ; y++ ) 
		for ( int x = 0 ; x < xdim ; x++ )
		for ( int b = 0 ; b < bdim ; b++ ) { 

			if ( !image.isPresentXYZTB(x,y,z,t,b) ) continue;
			if ( truth.getPixelXYZTBByte(x,y,z,t,b) != opt.getPixelXYZTBByte(x,y,z,t,b) ) { 

				compare.setPixelXYZTBByte( x,y,z,t,0, 255 );
				compare.setPixelXYZTBByte( x,y,z,t,1, 0 );
				compare.setPixelXYZTBByte( x,y,z,t,2, 0 );
			}
		}
		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( compare,"conflits" );

		for ( int i = 0; i < size; i++ )
			if ( !image.isPresent( i ) ) continue;
			else assertEquals( truth.getPixelByte(i),opt.getPixelByte(i) );

	} // endmain



}
