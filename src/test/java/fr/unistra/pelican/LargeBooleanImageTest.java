package fr.unistra.pelican;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.LargeBooleanImage;
import fr.unistra.pelican.LargeByteImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;

public class LargeBooleanImageTest {
	
	Image mandrill = ImageLoader.exec("../pelican2/samples/mandrill.png");
	
	@Test	
	
	/* ###################################
	 * 	CONSTRUCTEURS 
	 * ###################################*/
	public void largeBooleanImageTest(){
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill, true, 1);

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
		assertEquals((1<<20),mandrillLarge.getUnitSize());
				
		assertEquals("Test dépendant de l'image",1,mandrillLarge.getUnitDim());
		
		for(long i=0 ;i<mandrill.size();i++){
			assertEquals(mandrill.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}
		
		LargeBooleanImage emptyImage = new LargeBooleanImage(12, 13, 14, 15, 16);
		 
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
		
		//assertEquals("Test dépendant de la machine ",(1<<23),emptyImage.getUnitSize());
		for(long i=0 ;i<emptyImage.size();i++){
			assertEquals(false,emptyImage.getPixelBoolean(i));
		}
		
		
		LargeBooleanImage mandrillLarge2 =new LargeBooleanImage(mandrillLarge);
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
			assertEquals(mandrillLarge.getPixelBoolean(i),mandrillLarge2.getPixelBoolean(i));
		}
		
	}
	
	@Test
	public void newInstanceTest(){
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill,false);
		LargeBooleanImage mandrillLarge2 = mandrillLarge.newInstance(12, 13, 14, 15, 16);
		assertEquals(true,mandrillLarge2 instanceof LargeBooleanImage);
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
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		assertEquals(mandrillClassic.size(),mandrillLarge.size());
	}
	
	@Test	
	public void copyImageTest(){
		
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		
		LargeBooleanImage mandrillLarge2 = mandrillLarge.copyImage(false);
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(false,mandrillLarge2.getPixelBoolean(i));
		}
		for (int i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(false,mandrillLarge2.getPixelBoolean(i));
		}
				
		mandrillLarge2 = mandrillLarge.copyImage(true);
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillLarge.getPixelBoolean(i),mandrillLarge2.getPixelBoolean(i));
		}
		for (int i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillLarge.getPixelBoolean(i),mandrillLarge2.getPixelBoolean(i));
		}
	}
	
	@Test	
	public void setPixelTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		ByteImage mandrillByte = new ByteImage(mandrill);
		IntegerImage mandrillInt = new IntegerImage(mandrill);
		DoubleImage mandrillDouble = new DoubleImage(mandrill);
		
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		mandrillLarge.setPixel(mandrillClassic, 16, 17, 0, 0, 0, 21, 22, 0, 0, 0);
		assertEquals(mandrillClassic.getPixelXYZTBBoolean(21,22,0,0,0),mandrillLarge.getPixelXYZTBBoolean(16,17,0,0,0));

		mandrillLarge.setPixel(mandrillByte, 16, 18, 0, 0, 0, 21, 22, 0, 0, 0);
		assertEquals(mandrillByte.getPixelXYZTBBoolean(21,22,0,0,0),mandrillLarge.getPixelXYZTBBoolean(16,18,0,0,0));
		
		mandrillLarge.setPixel(mandrillInt, 16, 19, 0, 0, 0, 21, 22, 0, 0, 0);
		assertEquals(mandrillInt.getPixelXYZTBBoolean(21,22,0,0,0),mandrillLarge.getPixelXYZTBBoolean(16,19,0,0,0));
		
		mandrillLarge.setPixel(mandrillDouble, 16, 20, 0, 0, 0, 21, 22, 0, 0, 0);
		assertEquals(mandrillDouble.getPixelXYZTBBoolean(21,22,0,0,0),mandrillLarge.getPixelXYZTBBoolean(16,12,0,0,0));
		
	}
	
	@Test	
	public void setPixelBooleanTest(){
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		mandrillLarge.setPixelBoolean(2684, true);
		assertEquals(true,mandrillLarge.getPixelBoolean(2684));
		assertEquals(true,mandrillLarge.getPixelBoolean((long)2684));
		mandrillLarge.setPixelBoolean((long)2684, false);
		assertEquals(false,mandrillLarge.getPixelBoolean(2684));
		assertEquals(false,mandrillLarge.getPixelBoolean((long)2684));
		
	}
	
	@Test	
	public void setPixelByteTest(){
		
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		
		mandrillClassic.setPixelByte(2684, 42);
		mandrillLarge.setPixelByte(2684, 42);
		assertEquals(false,mandrillLarge.getPixelBoolean(2684));
		assertEquals(mandrillClassic.getPixelBoolean(2684),mandrillLarge.getPixelBoolean(2684));
		assertEquals(false,mandrillLarge.getPixelBoolean((long)2684));
		assertEquals(mandrillClassic.getPixelBoolean((long)2684),mandrillLarge.getPixelBoolean((long)2684));
		
		
		mandrillClassic.setPixelByte((long)2684, 169);
		mandrillLarge.setPixelByte((long)2684, 169);
		assertEquals(true,mandrillLarge.getPixelBoolean(2684));
		assertEquals(mandrillClassic.getPixelBoolean(2684),mandrillLarge.getPixelBoolean(2684));		
		assertEquals(true,mandrillLarge.getPixelBoolean((long)2684));
		assertEquals(mandrillClassic.getPixelBoolean((long)2684),mandrillLarge.getPixelBoolean((long)2684));
		
	}
	
	@Test	
	public void setPixelDoubleTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		
		mandrillClassic.setPixelDouble(2684, 0.56);
		mandrillLarge.setPixelDouble(2684, 0.56);
		assertEquals(true,mandrillLarge.getPixelBoolean(2684));
		assertEquals(mandrillClassic.getPixelBoolean(2684),mandrillLarge.getPixelBoolean(2684));
		assertEquals(true,mandrillLarge.getPixelBoolean((long)2684));
		assertEquals(mandrillClassic.getPixelBoolean((long)2684),mandrillLarge.getPixelBoolean((long)2684));
		
		
		mandrillClassic.setPixelDouble((long)2684, 0.42);
		mandrillLarge.setPixelDouble((long)2684, 0.42);
		assertEquals(false,mandrillLarge.getPixelBoolean(2684));
		assertEquals(mandrillClassic.getPixelBoolean(2684),mandrillLarge.getPixelBoolean(2684));
		assertEquals(false,mandrillLarge.getPixelBoolean((long)2684));
		assertEquals(mandrillClassic.getPixelBoolean((long)2684),mandrillLarge.getPixelBoolean((long)2684));
		
	}
	
	@Test	
	public void setPixelIntTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		
		mandrillClassic.setPixelInt(2684, 32);
		mandrillLarge.setPixelInt(2684, 32);
		assertEquals(true,mandrillLarge.getPixelBoolean(2684));
		assertEquals(mandrillClassic.getPixelBoolean(2684),mandrillLarge.getPixelBoolean(2684));

		assertEquals(true,mandrillLarge.getPixelBoolean((long)2684));
		assertEquals(mandrillClassic.getPixelBoolean((long)2684),mandrillLarge.getPixelBoolean((long)2684));
		
		
		mandrillClassic.setPixelInt((long)2684, -189);
		mandrillLarge.setPixelInt((long)2684, -189);
		assertEquals(false,mandrillLarge.getPixelBoolean(2684));
		assertEquals(mandrillClassic.getPixelBoolean(2684),mandrillLarge.getPixelBoolean(2684));
		assertEquals(false,mandrillLarge.getPixelBoolean((long)2684));
		assertEquals(mandrillClassic.getPixelBoolean((long)2684),mandrillLarge.getPixelBoolean((long)2684));
		
		
	}
	
	@Test	
	public void setPixelsTest(){
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		try{
			mandrillLarge.setPixels(new boolean[10]);
			fail();
		}catch(PelicanException e){
			assertEquals("You can not setPixels on a large Image. Please correct your algorithm to avoid this method",e.getMessage());
		}
	}
	
	@Test	
	public void getPixelBooleanTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		for (int i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}
	}
	
	@Test	
	public void getPixelByteTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		for (int i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte((int)i),mandrillLarge.getPixelByte(i));
		}		
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte((int)i),mandrillLarge.getPixelByte(i));
		}		
	}
	
	@Test	
	public void getPixelDoubleTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);		
		for (int i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.000001);
		}
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.000001);
		}
	}
	
	@Test	
	public void getPixelIntTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);	
		for (int i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}
	}
	
	@Test
	public void setAndGetPixelsLong(){
LargeBooleanImage largeBool = new LargeBooleanImage(2000,2000,2,2,1000);
		
		largeBool.setPixelBoolean(3000001000L, true);
		assertEquals(true,largeBool.getPixelXYBoolean(1,1500));
		largeBool.setPixelBoolean(3000001000L, false);
		assertEquals(false,largeBool.getPixelXYBoolean(1,1500));
		
		largeBool.setPixelBoolean(3000001001L, true);
		assertEquals(true,largeBool.getPixelXYBBoolean(1,1500,1));
		largeBool.setPixelBoolean(3000001001L, false);
		assertEquals(false,largeBool.getPixelXYBBoolean(1,1500,1));
		
		largeBool.setPixelBoolean(7000001000L, true);
		assertEquals(true,largeBool.getPixelXYZBoolean(1,1500,1));
		largeBool.setPixelBoolean(7000001000L, false);
		assertEquals(false,largeBool.getPixelXYZBoolean(1,1500,1));
		
		largeBool.setPixelBoolean(11000001000L, true);
		assertEquals(true,largeBool.getPixelXYTBoolean(1,1500,1));
		largeBool.setPixelBoolean(11000001000L, false);
		assertEquals(false,largeBool.getPixelXYTBoolean(1,1500,1));
		
		largeBool.setPixelBoolean(15000001000L, true);
		assertEquals(true,largeBool.getPixelXYZTBoolean(1,1500,1,1));
		largeBool.setPixelBoolean(15000001000L, false);
		assertEquals(false,largeBool.getPixelXYZTBoolean(1,1500,1,1));
		
		largeBool.setPixelBoolean(7000001001L, true);
		assertEquals(true,largeBool.getPixelXYZBBoolean(1,1500,1,1));
		largeBool.setPixelBoolean(7000001001L, false);
		assertEquals(false,largeBool.getPixelXYZBBoolean(1,1500,1,1));
		
		largeBool.setPixelBoolean(11000001001L, true);
		assertEquals(true,largeBool.getPixelXYTBBoolean(1,1500,1,1));
		largeBool.setPixelBoolean(11000001001L, false);
		assertEquals(false,largeBool.getPixelXYTBBoolean(1,1500,1,1));

		largeBool.setPixelBoolean(15000001001L, true);
		assertEquals(true,largeBool.getPixelXYZTBBoolean(1,1500,1,1,1));
		largeBool.setPixelBoolean(15000001001L, false);
		assertEquals(false,largeBool.getPixelXYZTBBoolean(1,1500,1,1,1));
	}
	
	@Test	
	public void getPixelsTest(){
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		try{
			mandrillLarge.getPixels();
			fail();
		}catch(PelicanException e){
			assertEquals("You can not getPixels on a large Image. Please correct your algorithm to avoid this method",e.getMessage());
		}
	}
	
	@Test	
	public void equalsTest(){
					
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		LargeBooleanImage mandrillLarge2 = new LargeBooleanImage(mandrill);
		
		assertEquals(true,mandrillLarge.equals(mandrillLarge));
		assertEquals(true,mandrillLarge.equals(mandrillLarge2));
		mandrillLarge2.setPixelBoolean(0, false);
		assertEquals(false,mandrillLarge.equals(mandrillLarge2));		
		assertEquals(true,mandrillLarge.equals(mandrillClassic));			
		assertEquals(false,mandrillLarge.equals(mandrill));			
	}
	
	@Test	
	public void fillDoubleTest(){
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		mandrillLarge.fill(0.53);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(true,mandrillLarge.getPixelBoolean(i));
		}
		mandrillLarge.fill(0.24);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(false,mandrillLarge.getPixelBoolean(i));
		}		
	}
		
	@Test
	public void minMaxBooleanTest (){
		BooleanImage booleanImage = new BooleanImage(1024,1024,1,1,3);
		
		for (int i =0;i<booleanImage.size();i+=3){
			booleanImage.setPixelBoolean(i,true);
		}
		for (int i =1;i<booleanImage.size();i+=3){
			booleanImage.setPixelBoolean(i,false);
		}
		for (int i =2;i<booleanImage.size();i+=6){
			booleanImage.setPixelBoolean(i,false);
		}
		for (int i =5;i<booleanImage.size();i+=6){
			booleanImage.setPixelBoolean(i,true);
		}
		
		LargeBooleanImage largeIm = new LargeBooleanImage(booleanImage,true,1);

		assertEquals(booleanImage.minimumBoolean(),largeIm.minimumBoolean());
		assertEquals(booleanImage.maximumBoolean(),largeIm.maximumBoolean());
		
		assertEquals(booleanImage.minimumByte(),largeIm.minimumByte());
		assertEquals(booleanImage.maximumByte(),largeIm.maximumByte());
		
		assertEquals(booleanImage.minimumDouble(),largeIm.minimumDouble(),0.00001);
		assertEquals(booleanImage.maximumDouble(),largeIm.maximumDouble(),0.00001);
		
		assertEquals(booleanImage.minimumDouble(0),largeIm.minimumDouble(0),0.00001);
		assertEquals(booleanImage.maximumDouble(0),largeIm.maximumDouble(0),0.00001);
		assertEquals(booleanImage.minimumDouble(1),largeIm.minimumDouble(1),0.00001);
		assertEquals(booleanImage.maximumDouble(1),largeIm.maximumDouble(1),0.00001);
		assertEquals(booleanImage.minimumDouble(2),largeIm.minimumDouble(2),0.00001);
		assertEquals(booleanImage.maximumDouble(2),largeIm.maximumDouble(2),0.00001);
		
		assertEquals(booleanImage.minimumDoubleIgnoreNonRealValues(0),largeIm.minimumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(booleanImage.maximumDoubleIgnoreNonRealValues(0),largeIm.maximumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(booleanImage.minimumDoubleIgnoreNonRealValues(1),largeIm.minimumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(booleanImage.maximumDoubleIgnoreNonRealValues(1),largeIm.maximumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(booleanImage.minimumDoubleIgnoreNonRealValues(2),largeIm.minimumDoubleIgnoreNonRealValues(2),0.00001);
		assertEquals(booleanImage.maximumDoubleIgnoreNonRealValues(2),largeIm.maximumDoubleIgnoreNonRealValues(2),0.00001);
		
		assertEquals(booleanImage.minimumInt(),largeIm.minimumInt());
		assertEquals(booleanImage.maximumInt(),largeIm.maximumInt());
		
		BooleanImage booleanImage2 = new BooleanImage(1024,1024,1,1,3);
		
		booleanImage2.setPixelBoolean(1048576, true);
		
		LargeBooleanImage largeIm2 = new LargeBooleanImage(booleanImage2,true,1);		
		
		assertEquals(booleanImage2.minimumBoolean(),largeIm2.minimumBoolean());
		assertEquals(booleanImage2.maximumBoolean(),largeIm2.maximumBoolean());
		
		assertEquals(booleanImage2.minimumByte(),largeIm2.minimumByte());
		assertEquals(booleanImage2.maximumByte(),largeIm2.maximumByte());
		
		assertEquals(booleanImage2.minimumDouble(),largeIm2.minimumDouble(),0.00001);
		assertEquals(booleanImage2.maximumDouble(),largeIm2.maximumDouble(),0.00001);
		
		assertEquals(booleanImage2.minimumDouble(0),largeIm2.minimumDouble(0),0.00001);
		assertEquals(booleanImage2.maximumDouble(0),largeIm2.maximumDouble(0),0.00001);
		assertEquals(booleanImage2.minimumDouble(1),largeIm2.minimumDouble(1),0.00001);
		assertEquals(booleanImage2.maximumDouble(1),largeIm2.maximumDouble(1),0.00001);
		assertEquals(booleanImage2.minimumDouble(2),largeIm2.minimumDouble(2),0.00001);
		assertEquals(booleanImage2.maximumDouble(2),largeIm2.maximumDouble(2),0.00001);
		
		assertEquals(booleanImage2.minimumDoubleIgnoreNonRealValues(0),largeIm2.minimumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(booleanImage2.maximumDoubleIgnoreNonRealValues(0),largeIm2.maximumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(booleanImage2.minimumDoubleIgnoreNonRealValues(1),largeIm2.minimumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(booleanImage2.maximumDoubleIgnoreNonRealValues(1),largeIm2.maximumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(booleanImage2.minimumDoubleIgnoreNonRealValues(2),largeIm2.minimumDoubleIgnoreNonRealValues(2),0.00001);
		assertEquals(booleanImage2.maximumDoubleIgnoreNonRealValues(2),largeIm2.maximumDoubleIgnoreNonRealValues(2),0.00001);
		
		assertEquals(booleanImage2.minimumInt(),largeIm2.minimumInt());
		assertEquals(booleanImage2.maximumInt(),largeIm2.maximumInt());
		
	}
		
	/*###########################################
	 * BooleanImageMethods
	 *########################################### */
	
	@Test	
	public void copyToByteImageTest(){
		
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		
		ByteImage mandrillClassic2 = mandrillClassic.copyToByteImage();
		LargeByteImage mandrillLarge2 = mandrillLarge.copyToByteImage();
		
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillClassic2.getPixelByte(i),mandrillLarge2.getPixelByte(i));
		}
	}	
	
	@Test	
	public void copyToIntegerImageTest(){
		
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		
		IntegerImage mandrillClassic2 = mandrillClassic.copyToIntegerImage();
		IntegerImage mandrillLarge2 = mandrillLarge.copyToIntegerImage();
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillClassic2.getPixelInt(i),mandrillLarge2.getPixelInt(i));
		}
	}
	
	
	@Test	
	public void duplicateBandTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		
		mandrillClassic.duplicateBand(0);
		mandrillLarge.duplicateBand(0);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}
		
		mandrillClassic = new BooleanImage(mandrill);
		mandrillLarge = new LargeBooleanImage(mandrill);
		mandrillClassic.duplicateBand(1);
		mandrillLarge.duplicateBand(1);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}
		
		mandrillClassic = new BooleanImage(mandrill);
		mandrillLarge = new LargeBooleanImage(mandrill);
		mandrillClassic.duplicateBand(2);
		mandrillLarge.duplicateBand(2);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}		
	}
	
	@Test	
	public void fillTest(){
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		mandrillLarge.fill(true);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(true,mandrillLarge.getPixelBoolean(i));
		}
		mandrillLarge.fill(false);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(false,mandrillLarge.getPixelBoolean(i));
		}		
	}
	
	@Test	
	public void foregroundTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		assertArrayEquals(mandrillClassic.foreground(),mandrillLarge.foreground());
	}
		
	@Test	
	public void getComplementTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		
		assertEquals(mandrillClassic.getComplement(),mandrillLarge.getComplement());
	}
	
	@Test	
	public void getSumTest(){
		BooleanImage mandrillClassic = new BooleanImage(mandrill);
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill);
		assertEquals(mandrillClassic.getSum(),mandrillLarge.getSum());
	}
	
	@Test	
	public void isEmptyTest(){
		LargeBooleanImage mandrillLarge = new LargeBooleanImage(mandrill,false);
		assertEquals(true,mandrillLarge.isEmpty());
		
		mandrillLarge = new LargeBooleanImage(mandrill);
		assertEquals(false,mandrillLarge.isEmpty());
	}

}
