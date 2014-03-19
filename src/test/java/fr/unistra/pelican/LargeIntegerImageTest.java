package fr.unistra.pelican;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.LargeIntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;


public class LargeIntegerImageTest {
	
	Image mandrill = ImageLoader.exec("../pelican2/samples/mandrill.png");
	
	/* ###################################
	 * 	CONSTRUCTEURS 
	 * ###################################*/
	@Test
	public void largeIntegerImageTest(){
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill, true,1);

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
		
		assertEquals(mandrillLarge.color,mandrill.color);
		assertEquals(mandrillLarge.getUnitSize(),(1<<18));
				
		assertEquals("Test dépendant de l'image",3,mandrillLarge.getUnitDim());
		
		for(long i=0 ;i<mandrill.size();i++){
			assertEquals(mandrill.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}
		
		LargeIntegerImage emptyImage = new LargeIntegerImage(12, 13, 14, 15, 16);
		 
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
		
		//assertEquals("This test depends on the size of the tenured generation",(1<<21),emptyImage.getUnitSize());
	
		for(long i=0 ;i<emptyImage.size();i++){
			assertEquals(0,emptyImage.getPixelInt(i));
		}
		
		
		LargeIntegerImage mandrillLarge2 =new LargeIntegerImage(mandrillLarge);
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
			assertEquals(mandrillLarge.getPixelInt(i),mandrillLarge2.getPixelInt(i));
		}
		
	}
	
	@Test
	public void newInstanceTest(){
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill,false);
		LargeIntegerImage mandrillLarge2 = mandrillLarge.newInstance(12, 13, 14, 15, 16);
		assertEquals(true,mandrillLarge2 instanceof LargeIntegerImage);
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
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		assertEquals(mandrillClassic.size(),mandrillLarge.size());		
	}
	
	@Test	
	public void copyImageTest(){
		
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		
		LargeIntegerImage mandrillLarge2 = mandrillLarge.copyImage(false);
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(0,mandrillLarge2.getPixelInt(i));
		}
		for (int i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(0,mandrillLarge2.getPixelInt(i));
		}
				
		mandrillLarge2 = mandrillLarge.copyImage(true);
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillLarge.getPixelInt(i),mandrillLarge2.getPixelInt(i));
		}
		for (int i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillLarge.getPixelInt(i),mandrillLarge2.getPixelInt(i));
		}
		
	}
	
	@Test	
	public void setPixelTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		mandrillLarge.setPixel(mandrillClassic, 16, 17, 0, 0, 0, 21, 22, 0, 0, 0);
		assertEquals(mandrillClassic.getPixelXYZTBInt(21,22,0,0,0),mandrillLarge.getPixelInt(16,17,0,0,0));
	}
	
	@Test	
	public void setPixelBooleanTest(){
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		mandrillLarge.setPixelBoolean(2684, true);
		assertEquals(Integer.MAX_VALUE,mandrillLarge.getPixelInt(2684));
		mandrillLarge.setPixelBoolean(2684, false);
		assertEquals(Integer.MIN_VALUE,mandrillLarge.getPixelInt(2684));
	}
	
	@Test	
	public void setPixelByteTest(){
		
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		
		mandrillClassic.setPixelByte(2684, 42);
		mandrillLarge.setPixelByte(2684, 42);
		assertEquals(mandrillClassic.getPixelInt(2684),mandrillLarge.getPixelInt(2684));
		
		mandrillClassic.setPixelByte(2684, 169);
		mandrillLarge.setPixelByte(2684, 169);
		assertEquals(mandrillClassic.getPixelInt(2684),mandrillLarge.getPixelInt(2684));
	}
	
	@Test	
	public void setPixelDoubleTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		
		mandrillClassic.setPixelDouble(2684, 0.56);
		mandrillLarge.setPixelDouble(2684, 0.56);
		assertEquals(mandrillClassic.getPixelInt(2684),mandrillLarge.getPixelInt(2684));
		
		mandrillClassic.setPixelDouble(2684, 0.42);
		mandrillLarge.setPixelDouble(2684, 0.42);
		assertEquals(mandrillClassic.getPixelInt(2684),mandrillLarge.getPixelInt(2684));
	}
	
	@Test	
	public void setPixelIntTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		
		mandrillClassic.setPixelInt(2684, 32);
		mandrillLarge.setPixelInt(2684, 32);
		assertEquals(mandrillClassic.getPixelInt(2684),mandrillLarge.getPixelInt(2684));
		
		mandrillClassic.setPixelInt(2684, -189);
		mandrillLarge.setPixelInt(2684, -189);
		assertEquals(mandrillClassic.getPixelInt(2684),mandrillLarge.getPixelInt(2684));
	}
	
	@Test	
	public void setPixelsTest(){
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		try{
			mandrillLarge.setPixels(new int[10]);
			fail();
		}catch(PelicanException e){
			assertEquals("You can not use setPixels(int[]) in a LargeImage",e.getMessage());
		}
	}
	
	@Test	
	public void getPixelBooleanTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}
	}
	
	@Test	
	public void getPixelByteTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}
	}
	
	@Test	
	public void getPixelDoubleTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.000001);
		}
	}
	
	@Test	
	public void getPixelIntTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);	
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}
	}
	
	@Test	
	public void getPixelsTest(){
		/*
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		try{
			mandrillLarge.getPixels();
			fail();
		}catch(PelicanException e){
			assertEquals(e.getMessage(),"You can not getPixels on a large Image. Please correct your algorithm to avoid this method");
		}
		//*/
	}
	
	@Test
	public void setGetPixelLong(){
		LargeIntegerImage largeInt = new LargeIntegerImage(2000,2000,2,2,1000);
		
		largeInt.setPixelBoolean(3000001000L, true);
		assertEquals(true,largeInt.getPixelXYBoolean(1,1500));
		largeInt.setPixelBoolean(3000001000L, false);
		assertEquals(false,largeInt.getPixelXYBoolean(1,1500));
		
		largeInt.setPixelBoolean(3000001001L, true);
		assertEquals(true,largeInt.getPixelXYBBoolean(1,1500,1));
		largeInt.setPixelBoolean(3000001001L, false);
		assertEquals(false,largeInt.getPixelXYBBoolean(1,1500,1));
		
		largeInt.setPixelBoolean(7000001000L, true);
		assertEquals(true,largeInt.getPixelXYZBoolean(1,1500,1));
		largeInt.setPixelBoolean(7000001000L, false);
		assertEquals(false,largeInt.getPixelXYZBoolean(1,1500,1));
		
		largeInt.setPixelBoolean(11000001000L, true);
		assertEquals(true,largeInt.getPixelXYTBoolean(1,1500,1));
		largeInt.setPixelBoolean(11000001000L, false);
		assertEquals(false,largeInt.getPixelXYTBoolean(1,1500,1));
		
		largeInt.setPixelBoolean(15000001000L, true);
		assertEquals(true,largeInt.getPixelXYZTBoolean(1,1500,1,1));
		largeInt.setPixelBoolean(15000001000L, false);
		assertEquals(false,largeInt.getPixelXYZTBoolean(1,1500,1,1));
		
		largeInt.setPixelBoolean(7000001001L, true);
		assertEquals(true,largeInt.getPixelXYZBBoolean(1,1500,1,1));
		largeInt.setPixelBoolean(7000001001L, false);
		assertEquals(false,largeInt.getPixelXYZBBoolean(1,1500,1,1));
		
		largeInt.setPixelBoolean(11000001001L, true);
		assertEquals(true,largeInt.getPixelXYTBBoolean(1,1500,1,1));
		largeInt.setPixelBoolean(11000001001L, false);
		assertEquals(false,largeInt.getPixelXYTBBoolean(1,1500,1,1));

		largeInt.setPixelBoolean(15000001001L, true);
		assertEquals(true,largeInt.getPixelXYZTBBoolean(1,1500,1,1,1));
		largeInt.setPixelBoolean(15000001001L, false);
		assertEquals(false,largeInt.getPixelXYZTBBoolean(1,1500,1,1,1));
	}
	
	@Test	
	public void equalsTest(){
					
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		LargeIntegerImage mandrillLarge2 = new LargeIntegerImage(mandrill);
		
		assertEquals(true,mandrillLarge.equals(mandrillLarge));
		assertEquals(true,mandrillLarge.equals(mandrillLarge2));
		mandrillLarge2.setPixelInt(0,0);
		assertEquals(false,mandrillLarge.equals(mandrillLarge2));
		assertEquals(true,mandrillLarge.equals(mandrillClassic));
		assertEquals(false,mandrillLarge.equals(mandrill));	
	}
	
	@Test	
	public void fillDoubleTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		mandrillClassic.fill(0.53);
		mandrillLarge.fill(0.53);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}
		mandrillClassic.fill(0.24);
		mandrillLarge.fill(0.24);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}		
	}
	

	@Test
	public void minMaxIntegerTest (){
		IntegerImage intImage = new IntegerImage(512,512,1,1,3);
		
		for (int i =0;i<intImage.size();i++){
			intImage.setPixelInt(i,i);
		}
		
		LargeIntegerImage largeIm = new LargeIntegerImage(intImage,true,1);
				
		assertEquals(intImage.minimumBoolean(),largeIm.minimumBoolean());
		assertEquals(intImage.maximumBoolean(),largeIm.maximumBoolean());
		
		assertEquals(intImage.minimumByte(),largeIm.minimumByte());
		assertEquals(intImage.maximumByte(),largeIm.maximumByte());
				
		assertEquals(intImage.minimumDouble(),largeIm.minimumDouble(),0.00001);
		assertEquals(intImage.maximumDouble(),largeIm.maximumDouble(),0.00001);
		
		assertEquals(intImage.minimumDouble(0),largeIm.minimumDouble(0),0.00001);
		assertEquals(intImage.maximumDouble(0),largeIm.maximumDouble(0),0.00001);
		assertEquals(intImage.minimumDouble(1),largeIm.minimumDouble(1),0.00001);
		assertEquals(intImage.maximumDouble(1),largeIm.maximumDouble(1),0.00001);
		assertEquals(intImage.minimumDouble(2),largeIm.minimumDouble(2),0.00001);
		assertEquals(intImage.maximumDouble(2),largeIm.maximumDouble(2),0.00001);
		
		assertEquals(intImage.minimumDoubleIgnoreNonRealValues(0),largeIm.minimumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(intImage.maximumDoubleIgnoreNonRealValues(0),largeIm.maximumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(intImage.minimumDoubleIgnoreNonRealValues(1),largeIm.minimumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(intImage.maximumDoubleIgnoreNonRealValues(1),largeIm.maximumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(intImage.minimumDoubleIgnoreNonRealValues(2),largeIm.minimumDoubleIgnoreNonRealValues(2),0.00001);
		assertEquals(intImage.maximumDoubleIgnoreNonRealValues(2),largeIm.maximumDoubleIgnoreNonRealValues(2),0.00001);

		assertEquals(intImage.minimumInt(),largeIm.minimumInt());
		assertEquals(intImage.maximumInt(),largeIm.maximumInt());		
	}
	
	
	/*###########################################
	 * IntegerImageMethods
	 *########################################### */
	
	@Test	
	public void convertToByteImageTest(){		
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
				
		ByteImage mandrillClassic2 = mandrillClassic.convertToByteImage();
		ByteImage mandrillLarge2 = mandrillLarge.convertToByteImage();
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillClassic2.getPixelByte(i),mandrillLarge2.getPixelByte(i));
		}
	}
	
	@Test	
	public void copyToByteImageTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		
		ByteImage mandrillClassic2 = mandrillClassic.copyToByteImage();
		ByteImage mandrillLarge2 = mandrillLarge.copyToByteImage();
				
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillClassic2.getPixelByte(i),mandrillLarge2.getPixelByte(i));
		}
	}

	@Test	
	public void fillTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		
		mandrillClassic.fill(43);
		mandrillLarge.fill((43));
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}
		mandrillClassic.fill(73897);
		mandrillLarge.fill(73897);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}		
	}
	
	@Test
	public void normaliseTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		
		DoubleImage mandrillClassic2 = mandrillClassic.normalise();
		DoubleImage mandrillLarge2 = mandrillLarge.normalise();
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillClassic2.getPixelDouble(i),mandrillLarge2.getPixelDouble(i),0.00001);
		}
	}

	@Test
	public void scaleToVisibleRangeTest(){
		IntegerImage mandrillClassic = new IntegerImage(mandrill);		
		LargeIntegerImage mandrillLarge = new LargeIntegerImage(mandrill);
		
		IntegerImage mandrillClassic2 = mandrillClassic.scaleToVisibleRange();
		IntegerImage mandrillLarge2 = mandrillLarge.scaleToVisibleRange();
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(""+i,mandrillClassic2.getPixelInt(i),mandrillLarge2.getPixelInt(i));
		}		
		
	}
}

