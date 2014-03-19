package fr.unistra.pelican;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.LargeByteImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;


public class LargeByteImageTest {
	
	Image mandrill = ImageLoader.exec("../pelican2/samples/mandrill.png");
	ByteImage billes = (ByteImage) ImageLoader.exec("../pelican2/samples/billes.png");
	
	/* ###################################
	 * 	CONSTRUCTEURS 
	 * ###################################*/
	@Test
	public void largeByteImageTest(){
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill, true,10);

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
		assertEquals((1<<23),mandrillLarge.getUnitSize());
				
		assertEquals("Test dépendant de l'image",1,mandrillLarge.getUnitDim());
		
		for(long i=0 ;i<mandrill.size();i++){
			assertEquals(mandrill.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}
		
		LargeByteImage emptyImage = new LargeByteImage(12, 13, 14, 15, 16);
		 
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
		
		assertEquals("This test depends on the size of the Tenured generation",(1<<23),emptyImage.getUnitSize());
		for(long i=0 ;i<emptyImage.size();i++){
			assertEquals(128,emptyImage.getPixelByte(i));
		}
		
		
		LargeByteImage mandrillLarge2 =new LargeByteImage(mandrillLarge);
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
			assertEquals(mandrillLarge.getPixelByte(i),mandrillLarge2.getPixelByte(i));
		}
		
	}
	
	@Test
	public void newInstanceTest(){
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill,false);
		LargeByteImage mandrillLarge2 = mandrillLarge.newInstance(12, 13, 14, 15, 16);
		assertEquals(true,mandrillLarge2 instanceof LargeByteImage);
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
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		assertEquals(mandrillClassic.size(),mandrillLarge.size());		
	}
	
	@Test	
	public void copyImageTest(){
		
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		LargeByteImage mandrillLarge2 = mandrillLarge.copyImage(false);
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(128,mandrillLarge2.getPixelByte(i));
		}
		for (int i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(128,mandrillLarge2.getPixelByte(i));
		}
		
				
		mandrillLarge2 = mandrillLarge.copyImage(true);
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillLarge.getPixelByte(i),mandrillLarge2.getPixelByte(i));
		}
		for (int i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillLarge.getPixelByte(i),mandrillLarge2.getPixelByte(i));
		}
	}
	
	@Test	
	public void setPixelTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		mandrillLarge.setPixel(mandrillClassic, 16, 17, 0, 0, 0, 21, 22, 0, 0, 0);
		assertEquals(mandrillClassic.getPixelXYZTBByte(21,22,0,0,0),mandrillLarge.getPixelXYZTBByte(16,17,0,0,0));
		
	}
	
	@Test	
	public void setPixelBooleanTest(){
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		mandrillLarge.setPixelBoolean(2684, true);
		assertEquals(255,mandrillLarge.getPixelByte(2684));
		mandrillLarge.setPixelBoolean(2684, false);
		assertEquals(0,mandrillLarge.getPixelByte(2684));		
	}
	
	@Test	
	public void setPixelByteTest(){
		
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		mandrillClassic.setPixelByte(2684, 42);
		mandrillLarge.setPixelByte(2684, 42);
		assertEquals(mandrillClassic.getPixelByte(2684),mandrillLarge.getPixelByte(2684));
		
		mandrillClassic.setPixelByte(2684, 169);
		mandrillLarge.setPixelByte(2684, 169);
		assertEquals(mandrillClassic.getPixelByte(2684),mandrillLarge.getPixelByte(2684));
	}
	
	@Test	
	public void setPixelDoubleTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		mandrillClassic.setPixelDouble(2684, 0.56);
		mandrillLarge.setPixelDouble(2684, 0.56);
		assertEquals(mandrillClassic.getPixelByte(2684),mandrillLarge.getPixelByte(2684));
		
		mandrillClassic.setPixelDouble(2684, 0.42);
		mandrillLarge.setPixelDouble(2684, 0.42);
		assertEquals(mandrillClassic.getPixelByte(2684),mandrillLarge.getPixelByte(2684));
	}
	
	@Test	
	public void setPixelIntTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		mandrillClassic.setPixelInt(2684, 32);
		mandrillLarge.setPixelInt(2684, 32);
		assertEquals(mandrillClassic.getPixelByte(2684),mandrillLarge.getPixelByte(2684));
		
		mandrillClassic.setPixelInt(2684, -189);
		mandrillLarge.setPixelInt(2684, -189);
		assertEquals(mandrillClassic.getPixelByte(2684),mandrillLarge.getPixelByte(2684));
	}
	
	@Test	
	public void setPixelsTest(){
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		try{
			mandrillLarge.setPixels(new byte[10]);
			fail();
		}catch(PelicanException e){
			assertEquals("You can not setPixels on a large Image. Please correct your algorithm to avoid this method",e.getMessage());
		}
	}
	
	@Test	
	public void getPixelBooleanTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelBoolean(i),mandrillLarge.getPixelBoolean(i));
		}
	}
	
	@Test	
	public void getPixelByteTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}
	}
	
	@Test	
	public void getPixelDoubleTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelDouble(i),mandrillLarge.getPixelDouble(i),0.000001);
		}
	}
	
	@Test	
	public void getPixelIntTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);	
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelInt(i),mandrillLarge.getPixelInt(i));
		}
	}
	
	@Test	
	public void getPixelsTest(){
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		try{
			mandrillLarge.getPixels();
			fail();
		}catch(PelicanException e){
			assertEquals("You can not getPixels on a large Image. Please correct your algorithm to avoid this method",e.getMessage());
		}
	}
	
	@Test
	public void setGetPixelLong(){
		
		LargeByteImage largeByte = new LargeByteImage(2000,2000,2,2,1000);
		
		largeByte.setPixelBoolean(3000001000L, true);
		assertEquals(true,largeByte.getPixelXYBoolean(1,1500));
		largeByte.setPixelBoolean(3000001000L, false);
		assertEquals(false,largeByte.getPixelXYBoolean(1,1500));
		
		largeByte.setPixelBoolean(3000001001L, true);
		assertEquals(true,largeByte.getPixelXYBBoolean(1,1500,1));
		largeByte.setPixelBoolean(3000001001L, false);
		assertEquals(false,largeByte.getPixelXYBBoolean(1,1500,1));
		
		largeByte.setPixelBoolean(7000001000L, true);
		assertEquals(true,largeByte.getPixelXYZBoolean(1,1500,1));
		largeByte.setPixelBoolean(7000001000L, false);
		assertEquals(false,largeByte.getPixelXYZBoolean(1,1500,1));
		
		largeByte.setPixelBoolean(11000001000L, true);
		assertEquals(true,largeByte.getPixelXYTBoolean(1,1500,1));
		largeByte.setPixelBoolean(11000001000L, false);
		assertEquals(false,largeByte.getPixelXYTBoolean(1,1500,1));
		
		largeByte.setPixelBoolean(15000001000L, true);
		assertEquals(true,largeByte.getPixelXYZTBoolean(1,1500,1,1));
		largeByte.setPixelBoolean(15000001000L, false);
		assertEquals(false,largeByte.getPixelXYZTBoolean(1,1500,1,1));
		
		largeByte.setPixelBoolean(7000001001L, true);
		assertEquals(true,largeByte.getPixelXYZBBoolean(1,1500,1,1));
		largeByte.setPixelBoolean(7000001001L, false);
		assertEquals(false,largeByte.getPixelXYZBBoolean(1,1500,1,1));
		
		largeByte.setPixelBoolean(11000001001L, true);
		assertEquals(true,largeByte.getPixelXYTBBoolean(1,1500,1,1));
		largeByte.setPixelBoolean(11000001001L, false);
		assertEquals(false,largeByte.getPixelXYTBBoolean(1,1500,1,1));

		largeByte.setPixelBoolean(15000001001L, true);
		assertEquals(true,largeByte.getPixelXYZTBBoolean(1,1500,1,1,1));
		largeByte.setPixelBoolean(15000001001L, false);
		assertEquals(false,largeByte.getPixelXYZTBBoolean(1,1500,1,1,1));		
	}
	
	@Test	
	public void equalsTest(){
					
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		LargeByteImage mandrillLarge2 = new LargeByteImage(mandrill);
		
		
		assertEquals(true,mandrillLarge.equals(mandrillLarge));
		assertEquals(true,mandrillLarge.equals(mandrillLarge2));
		mandrillLarge2.setPixelByte(0, 0);
		assertEquals(false,mandrillLarge.equals(mandrillLarge2));		
		assertEquals(true,mandrillLarge.equals(mandrillClassic));
		assertEquals(true,mandrillLarge.equals(mandrill));		
		assertEquals(false,mandrillLarge.equals(billes));		
	}
	
	@Test	
	public void fillDoubleTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		mandrillClassic.fill(0.53);
		mandrillLarge.fill(0.53);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}
		mandrillClassic.fill(0.24);
		mandrillLarge.fill(0.24);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}		
	}
	
	@Test
	public void minMaxByteTest (){
		ByteImage byteImage = new ByteImage(1024,1024,1,1,3);
		
		for (int i =0;i<byteImage.size();i+=3){
			byteImage.setPixelByte(i,255);
		}
		for (int i =1;i<byteImage.size();i+=3){
			byteImage.setPixelByte(i,0);
		}
		for (int i =2;i<byteImage.size();i+=3){
			byteImage.setPixelByte(i,128);
		}
		
		LargeByteImage largeIm = new LargeByteImage(byteImage,true,1);
		
		assertEquals(byteImage.minimumBoolean(),largeIm.minimumBoolean());
		assertEquals(byteImage.maximumBoolean(),largeIm.maximumBoolean());
		
		assertEquals(byteImage.minimumByte(),largeIm.minimumByte());
		assertEquals(byteImage.maximumByte(),largeIm.maximumByte());
		
		assertEquals(byteImage.minimumDouble(),largeIm.minimumDouble(),0.00001);
		assertEquals(byteImage.maximumDouble(),largeIm.maximumDouble(),0.00001);
		
		assertEquals(byteImage.minimumDouble(0),largeIm.minimumDouble(0),0.00001);
		assertEquals(byteImage.maximumDouble(0),largeIm.maximumDouble(0),0.00001);
		assertEquals(byteImage.minimumDouble(1),largeIm.minimumDouble(1),0.00001);
		assertEquals(byteImage.maximumDouble(1),largeIm.maximumDouble(1),0.00001);
		assertEquals(byteImage.minimumDouble(2),largeIm.minimumDouble(2),0.00001);
		assertEquals(byteImage.maximumDouble(2),largeIm.maximumDouble(2),0.00001);
		
		assertEquals(byteImage.minimumDoubleIgnoreNonRealValues(0),largeIm.minimumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(byteImage.maximumDoubleIgnoreNonRealValues(0),largeIm.maximumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(byteImage.minimumDoubleIgnoreNonRealValues(1),largeIm.minimumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(byteImage.maximumDoubleIgnoreNonRealValues(1),largeIm.maximumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(byteImage.minimumDoubleIgnoreNonRealValues(2),largeIm.minimumDoubleIgnoreNonRealValues(2),0.00001);
		assertEquals(byteImage.maximumDoubleIgnoreNonRealValues(2),largeIm.maximumDoubleIgnoreNonRealValues(2),0.00001);

		assertEquals(byteImage.minimumInt(),largeIm.minimumInt());
		assertEquals(byteImage.maximumInt(),largeIm.maximumInt());
		
		ByteImage byteImage2 = new ByteImage(1024,1024,1,1,3);
		byteImage2.setPixelByte(1048576, 0);
		LargeByteImage largeIm2 = new LargeByteImage(byteImage2,true,1);
				
		assertEquals(byteImage2.minimumBoolean(),largeIm2.minimumBoolean());
		assertEquals(byteImage2.maximumBoolean(),largeIm2.maximumBoolean());
		
		assertEquals(byteImage2.minimumByte(),largeIm2.minimumByte());
		assertEquals(byteImage2.maximumByte(),largeIm2.maximumByte());
		
		assertEquals(byteImage2.minimumDouble(),largeIm2.minimumDouble(),0.00001);
		assertEquals(byteImage2.maximumDouble(),largeIm2.maximumDouble(),0.00001);
		
		assertEquals(byteImage2.minimumDouble(0),largeIm2.minimumDouble(0),0.00001);
		assertEquals(byteImage2.maximumDouble(0),largeIm2.maximumDouble(0),0.00001);
		assertEquals(byteImage2.minimumDouble(1),largeIm2.minimumDouble(1),0.00001);
		assertEquals(byteImage2.maximumDouble(1),largeIm2.maximumDouble(1),0.00001);
		assertEquals(byteImage2.minimumDouble(2),largeIm2.minimumDouble(2),0.00001);
		assertEquals(byteImage2.maximumDouble(2),largeIm2.maximumDouble(2),0.00001);
		
		assertEquals(byteImage2.minimumDoubleIgnoreNonRealValues(0),largeIm2.minimumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(byteImage2.maximumDoubleIgnoreNonRealValues(0),largeIm2.maximumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(byteImage2.minimumDoubleIgnoreNonRealValues(1),largeIm2.minimumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(byteImage2.maximumDoubleIgnoreNonRealValues(1),largeIm2.maximumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(byteImage2.minimumDoubleIgnoreNonRealValues(2),largeIm2.minimumDoubleIgnoreNonRealValues(2),0.00001);
		assertEquals(byteImage2.maximumDoubleIgnoreNonRealValues(2),largeIm2.maximumDoubleIgnoreNonRealValues(2),0.00001);

		assertEquals(byteImage2.minimumInt(),largeIm2.minimumInt());
		assertEquals(byteImage2.maximumInt(),largeIm2.maximumInt());
	}
	
	
	
	/*###########################################
	 * ByteImageMethods
	 *########################################### */
	
	@Test	
	public void copyToIntegerImageTest(){		
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		IntegerImage mandrillClassic2 = mandrillClassic.copyToIntegerImage();
		IntegerImage mandrillLarge2 = mandrillLarge.copyToIntegerImage();
		for (long i=0;i<mandrillLarge2.size() ;i++){
			assertEquals(mandrillClassic2.getPixelInt(i),mandrillLarge2.getPixelInt(i));
		}
	}
	
	@Test
	public void differenceRatio(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		assertEquals(mandrillClassic.differenceRatio(mandrillClassic),mandrillLarge.differenceRatio(mandrillLarge),0.00001);		
		assertEquals(mandrillClassic.differenceRatio(mandrillLarge),mandrillLarge.differenceRatio(mandrillClassic),0.00001);
		assertEquals(mandrillClassic.differenceRatio(billes),mandrillLarge.differenceRatio(billes),0.00001);
	}
	
	@Test	
	public void duplicateBandTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		mandrillClassic.duplicateBand(0);
		mandrillLarge.duplicateBand(0);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}
		
		mandrillClassic = new ByteImage(mandrill);
		mandrillLarge = new LargeByteImage(mandrill);
		mandrillClassic.duplicateBand(1);
		mandrillLarge.duplicateBand(1);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}
		
		mandrillClassic = new ByteImage(mandrill);
		mandrillLarge = new LargeByteImage(mandrill);
		mandrillClassic.duplicateBand(2);
		mandrillLarge.duplicateBand(2);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}		
	}
	
	@Test	
	public void fillTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);		
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		mandrillClassic.fill((byte)43);
		mandrillLarge.fill((byte)43);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}
		mandrillClassic.fill((byte)73897);
		mandrillLarge.fill((byte)73897);
		for (long i=0;i<mandrillLarge.size() ;i++){
			assertEquals(mandrillClassic.getPixelByte(i),mandrillLarge.getPixelByte(i));
		}		
	}
	
	@Test
	public void getPixelAverageTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		assertEquals(mandrillClassic.getPixelAverage(0),mandrillLarge.getPixelAverage(0),0.00001);
		assertEquals(mandrillClassic.getPixelAverage(1),mandrillLarge.getPixelAverage(1),0.00001);
		assertEquals(mandrillClassic.getPixelAverage(2),mandrillLarge.getPixelAverage(2),0.00001);
	}
		
	@Test	
	public void getPixelSumTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		assertEquals(mandrillClassic.getPixelSum(0),mandrillLarge.getPixelSum(0));
		assertEquals(mandrillClassic.getPixelSum(1),mandrillLarge.getPixelSum(1));
		assertEquals(mandrillClassic.getPixelSum(2),mandrillLarge.getPixelSum(2));
	}
	
	@Test	
	public void isEmptyTest(){
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill,false);
		assertEquals(true,mandrillLarge.isEmpty());
		
		mandrillLarge = new LargeByteImage(mandrill);
		assertEquals(false,mandrillLarge.isEmpty());
	}
	
	@Test
	public void nbDifferentPixelsTest(){
		ByteImage mandrillClassic = new ByteImage(mandrill);
		ByteImage mandrillClassic2 = new ByteImage(mandrill);
		LargeByteImage mandrillLarge = new LargeByteImage(mandrill);
		
		assertEquals(mandrillClassic.nbDifferentPixels(mandrillClassic2),mandrillLarge.nbDifferentPixels(mandrillClassic2),0.00001);
		
		mandrillClassic2.fill((byte)81);
		
		assertEquals(mandrillClassic.nbDifferentPixels(mandrillClassic2),mandrillLarge.nbDifferentPixels(mandrillClassic2),0.00001);
	}
}

