package fr.unistra.pelican;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.LargeDoubleImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;


public class LargeDoubleImageTest {
	
	Image mandrill = ImageLoader.exec("../pelican2/samples/mandrill.png");
	
	
	/* ###################################
	 * 	CONSTRUCTEURS 
	 * ###################################*/
	@Test
	public void largeDoubleImageTest(){
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill, true,1);

		assertEquals(mandrill.xdim,mandrillLarge.xdim);
		assertEquals(mandrill.ydim,mandrillLarge.ydim);
		assertEquals(mandrill.zdim,mandrillLarge.zdim);
		assertEquals(mandrill.tdim,mandrillLarge.tdim);
		assertEquals(mandrill.bdim,mandrillLarge.bdim);
		
		assertEquals(mandrill.xdim,mandrillLarge.getLongXDim());
		assertEquals(mandrill.ydim,mandrillLarge.getLongYDim());
		assertEquals(mandrill.zdim,mandrillLarge.getLongZDim());
		assertEquals(mandrill.tdim,mandrillLarge.getLongTDim());
		assertEquals(mandrill.bdim,mandrillLarge.getLongBDim());
		
		assertEquals(mandrill.color,mandrillLarge.color);
		assertEquals((1<<17),mandrillLarge.getUnitSize());
				
		assertEquals("Test dépendant de l'image",6,mandrillLarge.getUnitDim());
		
		for(long i=0 ;i<mandrill.size();i++){
			assertEquals(mandrill.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.00001);
		}
		
		LargeDoubleImage emptyImage = new LargeDoubleImage(12, 13, 14, 15, 16);
		 
		assertEquals(12,emptyImage.xdim);
		assertEquals(13,emptyImage.ydim);
		assertEquals(14,emptyImage.zdim);
		assertEquals(15,emptyImage.tdim);
		assertEquals(16,emptyImage.bdim);
		
		assertEquals(12,emptyImage.getLongXDim());
		assertEquals(13,emptyImage.getLongYDim());
		assertEquals(14,emptyImage.getLongZDim());
		assertEquals(15,emptyImage.getLongTDim());
		assertEquals(16,emptyImage.getLongBDim());
		
		//assertEquals("This test depends on the size of the Tenured generation",(1<<16),emptyImage.getUnitSize());
		
		for(long i=0 ;i<emptyImage.size();i++){
			assertEquals(0.0,emptyImage.getPixelDouble(i),0.00001);
		}
		
		
		LargeDoubleImage mandrillLarge2 =new LargeDoubleImage(mandrillLarge);
		assertEquals(mandrillLarge.xdim,mandrillLarge2.xdim);
		assertEquals(mandrillLarge.ydim,mandrillLarge2.ydim);
		assertEquals(mandrillLarge.zdim,mandrillLarge2.zdim);
		assertEquals(mandrillLarge.tdim,mandrillLarge2.tdim);
		assertEquals(mandrillLarge.bdim,mandrillLarge2.bdim);
		
		assertEquals(mandrillLarge.getLongXDim(),mandrillLarge2.getLongXDim());
		assertEquals(mandrillLarge.getLongYDim(),mandrillLarge2.getLongYDim());
		assertEquals(mandrillLarge.getLongZDim(),mandrillLarge2.getLongZDim());
		assertEquals(mandrillLarge.getLongTDim(),mandrillLarge2.getLongTDim());
		assertEquals(mandrillLarge.getLongBDim(),mandrillLarge2.getLongBDim());
				
		assertEquals(mandrillLarge.getUnitDim(),mandrillLarge2.getUnitDim());
		assertEquals(mandrillLarge.getUnitLength(),mandrillLarge2.getUnitLength());
		assertEquals(mandrillLarge.getUnitSize(),mandrillLarge2.getUnitSize());
		
		for(long i=0 ;i<mandrill.size();i++){
			assertEquals(mandrillLarge.getPixelDouble(i),mandrillLarge2.getPixelDouble(i),0.00001);
		}
		
	}
	
	@Test
	public void newInstanceTest(){
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill,false);
		LargeDoubleImage mandrillLarge2 = mandrillLarge.newInstance(12, 13, 14, 15, 16);
		assertEquals(true,mandrillLarge2 instanceof LargeDoubleImage);
		assertEquals(12,mandrillLarge2.getXDim());
		assertEquals(13,mandrillLarge2.getYDim());
		assertEquals(14,mandrillLarge2.getZDim());
		assertEquals(15,mandrillLarge2.getTDim());
		assertEquals(16,mandrillLarge2.getBDim());
		assertEquals(12*13*14*15*16,mandrillLarge2.size());
	}
		
	/*###########################################
	 * Image Methods
	 *########################################### */
	
	@Test	
	public void sizeTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		assertEquals(mandrillClassic.size(),mandrillLarge.size());		
	}
	
	@Test	
	public void copyImageTest(){
		
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);		
		LargeDoubleImage mandrillLarge2 = mandrillLarge.copyImage(false);
		
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(0.0,mandrillLarge2.getPixelDouble(i),0.00001);
		}
		for (int i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(0.0,mandrillLarge2.getPixelDouble(i),0.00001);
		}
		
				
		mandrillLarge2 = mandrillLarge.copyImage(true);
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillLarge.getPixelDouble(i),mandrillLarge2.getPixelDouble(i),0.00001);
		}
		for (int i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillLarge.getPixelDouble(i),mandrillLarge2.getPixelDouble(i),0.00001);
		}
	}
	
	@Test	
	public void setPixelTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		mandrillLarge.setPixel(mandrillClassic, 16, 17, 0, 0, 0, 21, 22, 0, 0, 0);
		assertEquals(mandrillClassic.getPixelXYZTBDouble(21,22,0,0,0),mandrillLarge.getPixelDouble(16,17,0,0,0),0.00001);
	}
	
	@Test	
	public void setPixelBooleanTest(){
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		mandrillLarge.setPixelBoolean(2684, true);
		assertEquals(1.0,mandrillLarge.getPixelDouble(2684),0.00001);
		mandrillLarge.setPixelBoolean(2684, false);
		assertEquals(0.0,mandrillLarge.getPixelDouble(2684),0.00001);
	}
	
	@Test	
	public void setPixelByteTest(){
		
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		
		mandrillClassic.setPixelByte(2684, 42);
		mandrillLarge.setPixelByte(2684, 42);
		assertEquals(mandrillClassic.getPixelDouble(2684),mandrillLarge.getPixelDouble(2684),0.00001);
		
		mandrillClassic.setPixelByte(2684, 169);
		mandrillLarge.setPixelByte(2684, 169);
		assertEquals(mandrillClassic.getPixelDouble(2684),mandrillLarge.getPixelDouble(2684),0.00001);
	}
	
	@Test	
	public void setPixelDoubleTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		
		mandrillClassic.setPixelDouble(2684, 0.56);
		mandrillLarge.setPixelDouble(2684, 0.56);
		assertEquals(mandrillClassic.getPixelDouble(2684),mandrillLarge.getPixelDouble(2684),0.00001);
		
		mandrillClassic.setPixelDouble(2684, 0.42);
		mandrillLarge.setPixelDouble(2684, 0.42);
		assertEquals(mandrillClassic.getPixelDouble(2684),mandrillLarge.getPixelDouble(2684),0.00001);
	}
	
	@Test	
	public void setPixelIntTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		
		mandrillClassic.setPixelInt(2684, 32);
		mandrillLarge.setPixelInt(2684, 32);
		assertEquals(mandrillClassic.getPixelDouble(2684),mandrillLarge.getPixelDouble(2684),0.00001);
		
		mandrillClassic.setPixelInt(2684, -189);
		mandrillLarge.setPixelInt(2684, -189);
		assertEquals(mandrillClassic.getPixelDouble(2684),mandrillLarge.getPixelDouble(2684),0.00001);
	}
	
	@Test	
	public void setPixelsTest(){
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		try{
			mandrillLarge.setPixels(new double[10]);
			fail();
		}catch(PelicanException e){
			assertEquals("You can not setPixels on a large Image. Please correct your algorithm to avoid this method",e.getMessage());
		}
	}
	
	@Test	
	public void getPixelBooleanTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}
	}
	
	@Test	
	public void getPixelByteTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}
	}
	
	@Test	
	public void getPixelDoubleTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.000001);
		}
	}
	
	@Test	
	public void getPixelIntTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);	
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}
	}
	
	@Test	
	public void getPixelsTest(){
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		try{
			mandrillLarge.getPixels();
			fail();
		}catch(PelicanException e){
			assertEquals("You can not getPixels on a large Image. Please correct your algorithm to avoid this method",e.getMessage());
		}
	}
	
	@Test 
	public void setGetPixelsLong(){

		LargeDoubleImage largeDouble = new LargeDoubleImage(2000,2000,2,2,1000);
		
		largeDouble.setPixelBoolean(3000001000L, true);
		assertEquals(true,largeDouble.getPixelXYBoolean(1,1500));
		largeDouble.setPixelBoolean(3000001000L, false);
		assertEquals(false,largeDouble.getPixelXYBoolean(1,1500));
		
		largeDouble.setPixelBoolean(3000001001L, true);
		assertEquals(true,largeDouble.getPixelXYBBoolean(1,1500,1));
		largeDouble.setPixelBoolean(3000001001L, false);
		assertEquals(false,largeDouble.getPixelXYBBoolean(1,1500,1));
		
		largeDouble.setPixelBoolean(7000001000L, true);
		assertEquals(true,largeDouble.getPixelXYZBoolean(1,1500,1));
		largeDouble.setPixelBoolean(7000001000L, false);
		assertEquals(false,largeDouble.getPixelXYZBoolean(1,1500,1));
		
		largeDouble.setPixelBoolean(11000001000L, true);
		assertEquals(true,largeDouble.getPixelXYTBoolean(1,1500,1));
		largeDouble.setPixelBoolean(11000001000L, false);
		assertEquals(false,largeDouble.getPixelXYTBoolean(1,1500,1));
		
		largeDouble.setPixelBoolean(15000001000L, true);
		assertEquals(true,largeDouble.getPixelXYZTBoolean(1,1500,1,1));
		largeDouble.setPixelBoolean(15000001000L, false);
		assertEquals(false,largeDouble.getPixelXYZTBoolean(1,1500,1,1));
		
		largeDouble.setPixelBoolean(7000001001L, true);
		assertEquals(true,largeDouble.getPixelXYZBBoolean(1,1500,1,1));
		largeDouble.setPixelBoolean(7000001001L, false);
		assertEquals(false,largeDouble.getPixelXYZBBoolean(1,1500,1,1));
		
		largeDouble.setPixelBoolean(11000001001L, true);
		assertEquals(true,largeDouble.getPixelXYTBBoolean(1,1500,1,1));
		largeDouble.setPixelBoolean(11000001001L, false);
		assertEquals(false,largeDouble.getPixelXYTBBoolean(1,1500,1,1));

		largeDouble.setPixelBoolean(15000001001L, true);
		assertEquals(true,largeDouble.getPixelXYZTBBoolean(1,1500,1,1,1));
		largeDouble.setPixelBoolean(15000001001L, false);
		assertEquals(false,largeDouble.getPixelXYZTBBoolean(1,1500,1,1,1));
	}
	
	@Test	
	public void equalsTest(){
					
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		LargeDoubleImage mandrillLarge2 = new LargeDoubleImage(mandrill);
		
		assertEquals(true,mandrillLarge.equals(mandrillLarge));
		assertEquals(true,mandrillLarge.equals(mandrillLarge2));
		
		mandrillLarge2.setPixelDouble(0,0.0);
		assertEquals(false,mandrillLarge.equals(mandrillLarge2));
		
		assertEquals(true,mandrillLarge.equals(mandrillClassic));
		assertEquals(false,mandrillLarge.equals(mandrill));			
	}
	
	@Test	
	public void fillDoubleTest(){
		
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		
		mandrillClassic.fill(0.53);
		mandrillLarge.fill(0.53);
		
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.00001);
		}
		mandrillClassic.fill(0.24);
		mandrillLarge.fill(0.24);
		
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.00001);
		}		
	}
	

	@Test
	public void minMaxDoubleTest (){
		DoubleImage doubleImage = new DoubleImage(832,512,1,1,3);
		
		for (int i =0;i<doubleImage.size();i++){
			Random rand = new Random();
			doubleImage.setPixelDouble(i,rand.nextDouble());
		}
				
		LargeDoubleImage largeIm = new LargeDoubleImage(doubleImage);
		
		assertEquals(doubleImage.minimumBoolean(),largeIm.minimumBoolean());
		assertEquals(doubleImage.maximumBoolean(),largeIm.maximumBoolean());
		
		assertEquals(doubleImage.minimumByte(),largeIm.minimumByte());
		assertEquals(doubleImage.maximumByte(),largeIm.maximumByte());
		
		assertEquals(doubleImage.minimumDouble(),largeIm.minimumDouble(),0.0000001);
		assertEquals(doubleImage.maximumDouble(),largeIm.maximumDouble(),0.0000001);
		
		assertEquals(doubleImage.minimumDouble(0),largeIm.minimumDouble(0),0.0000001);
		assertEquals(doubleImage.maximumDouble(0),largeIm.maximumDouble(0),0.0000001);
		assertEquals(doubleImage.minimumDouble(1),largeIm.minimumDouble(1),0.0000001);
		assertEquals(doubleImage.maximumDouble(1),largeIm.maximumDouble(1),0.0000001);
		assertEquals(doubleImage.minimumDouble(2),largeIm.minimumDouble(2),0.0000001);
		assertEquals(doubleImage.maximumDouble(2),largeIm.maximumDouble(2),0.0000001);
				
		assertEquals(doubleImage.minimumDoubleIgnoreNonRealValues(0),largeIm.minimumDoubleIgnoreNonRealValues(0),0.0000001);
		assertEquals(doubleImage.maximumDoubleIgnoreNonRealValues(0),largeIm.maximumDoubleIgnoreNonRealValues(0),0.0000001);
		assertEquals(doubleImage.minimumDoubleIgnoreNonRealValues(1),largeIm.minimumDoubleIgnoreNonRealValues(1),0.0000001);
		assertEquals(doubleImage.maximumDoubleIgnoreNonRealValues(1),largeIm.maximumDoubleIgnoreNonRealValues(1),0.0000001);
		assertEquals(doubleImage.minimumDoubleIgnoreNonRealValues(2),largeIm.minimumDoubleIgnoreNonRealValues(2),0.0000001);
		assertEquals(doubleImage.maximumDoubleIgnoreNonRealValues(2),largeIm.maximumDoubleIgnoreNonRealValues(2),0.0000001);
		
		assertEquals(doubleImage.minimumInt(),largeIm.minimumInt());
		assertEquals(doubleImage.maximumInt(),largeIm.maximumInt());
	}
	
	/*###########################################
	 * DoubleImageMethods
	 *########################################### */
	
	@Test	
	public void fillBand(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill,true,1);
		
		assertEquals(6,mandrillLarge.getUnitDim());
		mandrillClassic.fill(0,0.32);
		mandrillLarge.fill(0,0.32);
		mandrillClassic.fill(1,0.62);
		mandrillLarge.fill(1,0.62);
		
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(""+i,mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.000001);
		}		
	}	
	
	@Test
	public void nbDifferentPixelsTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		DoubleImage mandrillClassic2 = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		mandrillClassic2.fill(0.26);
		
		assertEquals(mandrillClassic.nbDifferentPixels(mandrillClassic),mandrillLarge.nbDifferentPixels(mandrillLarge),0.00001);
		assertEquals(mandrillClassic.nbDifferentPixels(mandrillLarge),mandrillLarge.nbDifferentPixels(mandrillClassic),0.00001);
		assertEquals(mandrillClassic.nbDifferentPixels(mandrillClassic2),mandrillLarge.nbDifferentPixels(mandrillClassic2),0.00001);
	}
	
	@Test
	public void scaleToZeroOneTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		mandrillClassic.scaleToZeroOne();
		mandrillLarge.scaleToZeroOne();
		
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.00001);
		}
		
	}
	
	@Test
	public void scaleToZeroOneIndepTest(){
		DoubleImage mandrillClassic = new DoubleImage(mandrill);
		LargeDoubleImage mandrillLarge = new LargeDoubleImage(mandrill);
		mandrillClassic.scaleToZeroOneIndep();
		mandrillLarge.scaleToZeroOneIndep();
		
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.00001);
		}
	}
	
}

