package fr.unistra.pelican.algorithms.morphology.gray;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;



public class GrayErosionTest { 



	@Test
	public void testGrayErosion() { 

		Image image = ImageLoader.exec("src/test/resources/watershed.png");
		if ( image.getBDim() != 1 ) image = RGBToGray.exec( image );
		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
		Image result = GrayErosion.exec( image,se );
		Image truth = ImageLoader.exec("src/test/resources/grayerosion-square5x5-truth.png");

		System.out.println( "Ceci indique que l'erosion plante pas. " +
							"C'est le résultat qu'est pas bon." );
 
		assertEquals( truth.size(), result.size() );

		for ( int i = 0 ; i < truth.size() ; i++ ) 
			assertEquals( result.getPixelByte(i), truth.getPixelByte(i) );

	} // endfunc



	public static void main(String[] args) { 

		new GrayErosionTest().testGrayErosion();

		Image image = ImageLoader.exec( "src/test/resources/watershed.png" );
		Image compare = image.copyImage( true );
		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( image,"image de départ" );
		if ( image.getBDim() != 1 ) image = RGBToGray.exec( image );
		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(5);
		int size = image.size();
		int xdim = image.getXDim();
		int ydim = image.getYDim();
		int tdim = image.getTDim();
		int bdim = image.getBDim();
		int zdim = image.getZDim();

		long start = System.currentTimeMillis();
		Image truth = GrayErosion.exec( image,se );
		long end = System.currentTimeMillis();

		long starts = System.currentTimeMillis();
		Image opt = GrayErosion.exec( image,se,null,GrayErosion.RECTANGLE_OPTIMIZATION );
		long ends = System.currentTimeMillis();

		System.out.println( "taille de l'image: "+xdim+","+ydim+","+zdim+","+tdim+","+bdim );
		System.out.println( "machin de base: "+( end-start ) + "ms." );
		System.out.println( "truc optimisé: "+( ends-starts ) + "ms." );

//		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( image,"image grise" );
//		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( truth,"machin de base" );
//		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec( opt,"truc optimisé" );

		assertEquals( xdim,truth.getXDim(),opt.getXDim() );
		assertEquals( ydim,truth.getYDim(),opt.getYDim() );
		assertEquals( zdim,truth.getZDim(),opt.getZDim() );
		assertEquals( tdim,truth.getTDim(),opt.getTDim() );
		assertEquals( bdim,truth.getBDim(),opt.getBDim() );

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
