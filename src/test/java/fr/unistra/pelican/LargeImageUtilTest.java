package fr.unistra.pelican;

import static org.junit.Assert.assertEquals;


import org.junit.Test;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.largeImages.LargeImageUtil;

import fr.unistra.pelican.LargeBooleanImage;
import fr.unistra.pelican.LargeByteImage;
import fr.unistra.pelican.LargeDoubleImage;


public class LargeImageUtilTest {
		
	@Test
	public void computePowerOfTwoTest(){
		//Base Time 13
		assertEquals(LargeImageUtil.computePowerOfTwo(1),0);
		try{
		LargeImageUtil.computePowerOfTwo(-396);		
		}catch(PelicanException e){
			assertEquals(e.getMessage(),"Can not compute power of two for negative numbers ");
		}
		assertEquals(LargeImageUtil.computePowerOfTwo(1),0);
		assertEquals(LargeImageUtil.computePowerOfTwo(54),5);
		assertEquals(LargeImageUtil.computePowerOfTwo(156),7);
		assertEquals(LargeImageUtil.computePowerOfTwo(256),8);
		assertEquals(LargeImageUtil.computePowerOfTwo(1024),10);
		assertEquals(LargeImageUtil.computePowerOfTwo(1023),9);
	}
	
	@Test
	public void computeUnitSizeTest(){
		//Base Time 586
		LargeBooleanImage largeIm = new LargeBooleanImage(1024,1024,1024,1,3,1);
		assertEquals(20,largeIm.getUnitPowerSize());
		assertEquals(1048576,largeIm.getUnitSize());
	}
	
	@Test
	public void calculateTest(){
		//Base Time 93
		LargeBooleanImage largeIm = new LargeBooleanImage(1024,1024,1024,1,3);
		assertEquals("This test depends on the size of the Tenured generation",384,largeIm.getUnitDim());
		assertEquals(3221225472L,largeIm.sizeL());
	}
	
	/*
	@Test
	public void setUnitTest(){
		//Base Time 844 000
		LargeBooleanImage largeIm = new LargeBooleanImage(1024,1024,1024,1,3);
		BooleanUnit currUnit = largeIm.newUnit();
		boolean[] tab = new boolean[8388608];
		Arrays.fill(tab,true);
		currUnit.setPixels(tab);
		largeIm.setUnit(currUnit,312,true);
		
		for (long i  = 0; i<largeIm.sizeL();i++){
			assertEquals((i/8388608)==312,largeIm.getPixelBoolean(i));
		}
		assertEquals(true,largeIm.getPixelBoolean(312L*8388608L));
		
		largeIm.saveData();
		
		for (long i  = 0; i<largeIm.sizeL();i++){
			assertEquals((i/8388608)==312,largeIm.getPixelBoolean(i));
		}
		assertEquals(true,largeIm.getPixelBoolean(312L*8388608L));
		
		Arrays.fill(tab,false);
		currUnit.setPixels(tab);		
		largeIm.setUnit(currUnit,312,false);
		for (long i = 2617245696L; i<largeIm.sizeL();i++){
			assertEquals("If this check fails, the large image threw away an unit it had just loaded something may have invaded the memory",false,largeIm.getPixelBoolean(i));
		}
		
		largeIm.saveData();
		
		for (long i  = 0; i<largeIm.sizeL();i++){
			assertEquals((i/8388608)==312,largeIm.getPixelBoolean(i));
		}
	}
	
	//*/

	
	@Test
	public void volumeTest(){
		//Base time 1000
		DoubleImage doubleImage = new DoubleImage(512,512,1,1,3);
		
		for (int i =0;i<doubleImage.size();i++){
			doubleImage.setPixelDouble(i,Math.random());
		}
		
		LargeDoubleImage largeIm = new LargeDoubleImage(doubleImage);
		
		assertEquals(doubleImage.volume(),largeIm.volume(),0.00001);
		assertEquals(doubleImage.volumeByte(),largeIm.volumeByte());
		
		LargeDoubleImage doubleImage2 = new LargeDoubleImage(512,512,1,1,3);
		doubleImage2.fill(1.0);
		assertEquals(doubleImage2.sizeL(),doubleImage2.volume(),0.00001);
		
	}
	
	/*
	@Test
	public void fillTest(){
		//Base Time 370 000
		LargeByteImage largeIm = new LargeByteImage(1024,1024,1024,1,3);
		largeIm.fill(0.54);
		for( long i =0;i<largeIm.sizeL();i++){
			assertEquals(138,largeIm.getPixelByte(i));
		}
	}
	//*/
	
	/*
	@Test
	public void linearIndexTest(){
		//Base Time 463 000
		LargeBooleanImage largeIm = new LargeBooleanImage(98,99,100,101,102);
		
		//public static long getLinearIndexXY___(LargeImageInterface largeIm, int x, int y)
		long i = 0;
		for(int y=0;y<largeIm.getYDim();y++){
			for(int x=0;x<largeIm.getXDim();x++){
				assertEquals(i,LargeImageUtil.getLinearIndexXY___(largeIm, x, y));
				i+=largeIm.getLongBDim();
			}
		}
		
				
		//public static long getLinearIndexXYZ__(LargeImageInterface largeIm, int x, int y, int z)
		i = 0;
		for(int z=0;z<largeIm.getZDim();z++){
			for(int y=0;y<largeIm.getYDim();y++){
				for(int x=0;x<largeIm.getXDim();x++){
						assertEquals(i,LargeImageUtil.getLinearIndexXYZ__(largeIm, x, y, z));
						i+=largeIm.getLongBDim();
				}				
			}
		}
		
		//public static long getLinearIndexXY__B(LargeImageInterface largeIm, int x, int y, int b)
		i = 0;
		for(int y=0;y<largeIm.getYDim();y++){
			for(int x=0;x<largeIm.getXDim();x++){
				for(int b=0;b<largeIm.getBDim();b++){
					assertEquals(i,LargeImageUtil.getLinearIndexXY__B(largeIm, x, y, b));
					i++;
				}
			}
		}
		
		//public static long getLinearIndexXY_T_(LargeImageInterface largeIm, int x, int y, int t)
		i = 0;
		for(int t=0;t<largeIm.getTDim();t++){			
			for(int y=0;y<largeIm.getYDim();y++){
				for(int x=0;x<largeIm.getXDim();x++){						
					assertEquals(i,LargeImageUtil.getLinearIndexXY_T_(largeIm, x, y, t));
					i+=largeIm.getLongBDim();
				}
			}
			i+=((largeIm.getLongZDim()-1)*largeIm.getLongYDim()*largeIm.getLongXDim()*largeIm.getLongBDim());
		}
		
		//public static long getLinearIndexXYZT_(LargeImageInterface largeIm, int x, int y, int z, int t)
		i = 0;
		for(int t=0;t<largeIm.getTDim();t++){
			for(int z=0;z<largeIm.getZDim();z++){
				for(int y=0;y<largeIm.getYDim();y++){
					for(int x=0;x<largeIm.getXDim();x++){
						assertEquals(i,LargeImageUtil.getLinearIndexXYZT_(largeIm, x, y, z, t));
						i+=largeIm.getLongBDim();
					}
				}
			}
		}
		
		//public static long getLinearIndexXYZ_B(LargeImageInterface largeIm, int x, int y, int z, int b)
		i = 0;
		for(int z=0;z<largeIm.getZDim();z++){
			for(int y=0;y<largeIm.getYDim();y++){
				for(int x=0;x<largeIm.getXDim();x++){
					for(int b=0;b<largeIm.getBDim();b++){
						assertEquals(i,LargeImageUtil.getLinearIndexXYZ_B(largeIm, x, y, z, b));
						i++;
					}
				}
			}
		}
		
		//public static long getLinearIndexXY_TB(LargeImageInterface largeIm, int x, int y, int t, int b)
		i = 0;
		for(int t=0;t<largeIm.getTDim();t++){			
			for(int y=0;y<largeIm.getYDim();y++){
				for(int x=0;x<largeIm.getXDim();x++){
					for(int b=0;b<largeIm.getBDim();b++){
						assertEquals(i,LargeImageUtil.getLinearIndexXY_TB(largeIm, x, y, t, b));
						i++;
					}
				}
			}
			i+=((largeIm.getLongZDim()-1)*largeIm.getLongYDim()*largeIm.getLongXDim()*largeIm.getLongBDim());
		}
		
		//public static long getLinearIndexXYZTB(LargeImageInterface largeIm, int x, int y, int z, int t, int b)
		i = 0;
		for(int t=0;t<largeIm.getTDim();t++){
			for(int z=0;z<largeIm.getZDim();z++){
				for(int y=0;y<largeIm.getYDim();y++){
					for(int x=0;x<largeIm.getXDim();x++){
						for(int b=0;b<largeIm.getBDim();b++){
							assertEquals(i,LargeImageUtil.getLinearIndexXYZTB(largeIm, x, y, z, t, b));
							i++;
						}
					}
				}
			}
		}
	}
	
	//*/
	
	
	@Test
	public void linearIndexTestV2(){
		//Base Time 463 000
		LargeBooleanImage largeIm = new LargeBooleanImage(98,99,100,101,102);
		
		//public static long getLinearIndexXY___(LargeImageInterface largeIm, int x, int y)
		long i = 706860;
		int p =0;
		for(int y=70;y<largeIm.getYDim();y++){
			for(int x=70;x<largeIm.getXDim();x++){
				assertEquals(""+p,i,LargeImageUtil.getLinearIndexXY___(largeIm, x, y));
				i+=largeIm.getLongBDim();
			}
			i+=largeIm.getLongBDim()*70;
		}
		
				
		//public static long getLinearIndexXYZ__(LargeImageInterface largeIm, int x, int y, int z)
		i = 69979140;
		for(int z=70;z<largeIm.getZDim();z++){
			for(int y=70;y<largeIm.getYDim();y++){
				for(int x=70;x<largeIm.getXDim();x++){
						assertEquals(i,LargeImageUtil.getLinearIndexXYZ__(largeIm, x, y, z));
						i+=largeIm.getLongBDim();
				}
				i+=largeIm.getLongBDim()*70;
			}
			i+=(largeIm.getLongBDim()*70)*largeIm.getLongXDim();
		}
		
		//public static long getLinearIndexXY__B(LargeImageInterface largeIm, int x, int y, int b)
		i = 706930;
		for(int y=70;y<largeIm.getYDim();y++){
			for(int x=70;x<largeIm.getXDim();x++){
				for(int b=70;b<largeIm.getBDim();b++){
					assertEquals(i,LargeImageUtil.getLinearIndexXY__B(largeIm, x, y, b));
					i++;
				}
				i+=70;
			}
			i+=70*largeIm.getLongBDim();
		}
		
		//public static long getLinearIndexXY_T_(LargeImageInterface largeIm, int x, int y, int t)
		i = 6927934860L;
		for(int t=70;t<largeIm.getTDim();t++){			
			for(int y=70;y<largeIm.getYDim();y++){
				for(int x=70;x<largeIm.getXDim();x++){						
					assertEquals(i,LargeImageUtil.getLinearIndexXY_T_(largeIm, x, y, t));
					i+=largeIm.getLongBDim();
				}
				i+=largeIm.getLongBDim()*70;
			}
			i+=70*(largeIm.getLongXDim()*largeIm.getLongBDim());
			i+=((largeIm.getLongZDim()-1)*largeIm.getLongYDim()*largeIm.getLongXDim()*largeIm.getLongBDim());
		}
		
		//public static long getLinearIndexXYZT_(LargeImageInterface largeIm, int x, int y, int z, int t)
		i = 6997207140L;
		for(int t=70;t<largeIm.getTDim();t++){
			for(int z=70;z<largeIm.getZDim();z++){
				for(int y=70;y<largeIm.getYDim();y++){
					for(int x=70;x<largeIm.getXDim();x++){
						assertEquals(i,LargeImageUtil.getLinearIndexXYZT_(largeIm, x, y, z, t));
						i+=largeIm.getLongBDim();
					}
					i+=largeIm.getLongBDim()*70;;
				}
				i+=70*(largeIm.getLongXDim()*largeIm.getLongBDim());
			}
			i+=70*(largeIm.getLongYDim()*largeIm.getLongXDim()*largeIm.getLongBDim());
		}
		
		//public static long getLinearIndexXYZ_B(LargeImageInterface largeIm, int x, int y, int z, int b)
		i = 69979210;
		for(int z=70;z<largeIm.getZDim();z++){
			for(int y=70;y<largeIm.getYDim();y++){
				for(int x=70;x<largeIm.getXDim();x++){
					for(int b=70;b<largeIm.getBDim();b++){
						assertEquals(i,LargeImageUtil.getLinearIndexXYZ_B(largeIm, x, y, z, b));
						i++;
					}
					i+=70;
				}
				i+=70*largeIm.getBDim();
			}
			i+=70*largeIm.getLongBDim()*largeIm.getLongXDim();
		}
		
		//public static long getLinearIndexXY_TB(LargeImageInterface largeIm, int x, int y, int t, int b)
		i = 6927934930L;
		for(int t=70;t<largeIm.getTDim();t++){			
			for(int y=70;y<largeIm.getYDim();y++){
				for(int x=70;x<largeIm.getXDim();x++){
					for(int b=70;b<largeIm.getBDim();b++){
						assertEquals(i,LargeImageUtil.getLinearIndexXY_TB(largeIm, x, y, t, b));
						i++;
					}
					i+=70;
				}
				i+=70*largeIm.getBDim();
			}
			i+=70*(largeIm.getLongXDim()*largeIm.getLongBDim());
			i+=((largeIm.getLongZDim()-1)*largeIm.getLongYDim()*largeIm.getLongXDim()*largeIm.getLongBDim());
		}
		
		//public static long getLinearIndexXYZTB(LargeImageInterface largeIm, int x, int y, int z, int t, int b)
		i = 6997207210L;
		for(int t=70;t<largeIm.getTDim();t++){
			for(int z=70;z<largeIm.getZDim();z++){
				for(int y=70;y<largeIm.getYDim();y++){
					for(int x=70;x<largeIm.getXDim();x++){
						for(int b=70;b<largeIm.getBDim();b++){
							assertEquals(i,LargeImageUtil.getLinearIndexXYZTB(largeIm, x, y, z, t, b));
							i++;
						}
						i+=70;
					}
					i+=70*largeIm.getLongBDim();
				}
				i+=70*largeIm.getLongBDim()*largeIm.getLongXDim();
			}
			i+=70*largeIm.getLongBDim()*largeIm.getLongXDim()*largeIm.getLongYDim();
		}
	}
	
	@Test
	public void setAndGetPixelTest(){
		//Base Time 625
		LargeBooleanImage largeBool = new LargeBooleanImage(512,512,1,1,3);
		LargeByteImage largeByte = new LargeByteImage(512,512,1,1,3);
		LargeIntegerImage largeInt = new LargeIntegerImage(512,512,1,1,3);
		LargeDoubleImage largeDouble = new LargeDoubleImage(512,512,1,1,3);
		
		BooleanImage normalBool = new BooleanImage(512,512,1,1,3);
		ByteImage normalByte = new ByteImage(512,512,1,1,3);
		IntegerImage normalInt = new IntegerImage(512,512,1,1,3);
		DoubleImage normalDouble = new DoubleImage(512,512,1,1,3);
		
		double value = Math.random();
		set(largeBool,value);
		set(largeByte,value);
		set(largeInt,value);
		set(largeDouble,value);
		set(normalBool,value);
		set(normalByte,value);
		set(normalInt,value);
		set(normalDouble,value);
		
		get(normalBool,largeBool);
		get(normalByte,largeByte);
		get(normalInt,largeInt);
		get(normalDouble,largeDouble);
						
	}
	
	public void set(Image image, double value){		
		
		double valueDouble1 = value;
		double valueDouble2 = 1.0-value;
		
		int valueInt1 = (int) (IntegerImage.doubleToInt * (valueDouble1 - 0.5));
		int valueInt2 = (int) (IntegerImage.doubleToInt * (valueDouble2 - 0.5));
		
		int valueByte1 = (int) Math.round(ByteImage.doubleToByte * valueDouble1);
		int valueByte2 = (int) Math.round(ByteImage.doubleToByte * valueDouble2);
		
		boolean valueBool1 =(valueDouble1 >= 0.5) ? true : false;
		boolean valueBool2 =(valueDouble2 >= 0.5) ? true : false;
		
		
		image.setPixelXYDouble(0, 0, valueDouble1);
		image.setPixelXYDouble(0, 1, valueDouble2);

		image.setPixelXYZDouble(1, 0, 0, valueDouble1);
		image.setPixelXYZDouble(1, 1, 0, valueDouble2);
		
		image.setPixelXYBDouble(2, 0, 0, valueDouble1);
		image.setPixelXYBDouble(2, 1, 0, valueDouble2);

		image.setPixelXYTDouble(3, 0, 0, valueDouble1);
		image.setPixelXYTDouble(3, 1, 0, valueDouble2);

		image.setPixelXYZTDouble(4, 0, 0, 0,valueDouble1);
		image.setPixelXYZTDouble(4, 1, 0, 0,valueDouble2);

		image.setPixelXYZBDouble(5, 0, 0, 0, valueDouble1);
		image.setPixelXYZBDouble(5, 1, 0, 0, valueDouble2);
		
		image.setPixelXYTBDouble(6, 0, 0, 0, valueDouble1);
		image.setPixelXYTBDouble(6, 1, 0, 0, valueDouble2);

		image.setPixelXYZTBDouble(7, 0, 0, 0, 0,valueDouble1);
		image.setPixelXYZTBDouble(7, 1, 0, 0, 0,valueDouble2);
		
		image.setPixelXYInt(8, 0, valueInt1);
		image.setPixelXYInt(8, 1, valueInt2);

		image.setPixelXYZInt(9, 0, 0, valueInt1);
		image.setPixelXYZInt(9, 1, 0, valueInt2);
		
		image.setPixelXYBInt(10, 0, 0, valueInt1);
		image.setPixelXYBInt(10, 1, 0, valueInt2);

		image.setPixelXYTInt(11, 0, 0, valueInt1);
		image.setPixelXYTInt(11, 1, 0, valueInt2);

		image.setPixelXYZTInt(12, 0, 0, 0, valueInt1);
		image.setPixelXYZTInt(12, 1, 0, 0, valueInt2);

		image.setPixelXYZBInt(13, 0, 0, 0, valueInt1);
		image.setPixelXYZBInt(13, 1, 0, 0, valueInt2);

		image.setPixelXYTBInt(14, 0, 0, 0, valueInt1);
		image.setPixelXYTBInt(14, 1, 0, 0, valueInt2);

		image.setPixelXYZTBInt(15, 0, 0, 0, 0, valueInt1);
		image.setPixelXYZTBInt(15, 1, 0, 0, 0, valueInt2);
		
		image.setPixelXYByte(16, 0, valueByte1);
		image.setPixelXYByte(16, 1, valueByte2);

		image.setPixelXYZByte(17, 0, 0, valueByte1);
		image.setPixelXYZByte(17, 1, 0, valueByte2);

		image.setPixelXYBByte(18, 0, 0, valueByte1);
		image.setPixelXYBByte(18, 1, 0, valueByte2);

		image.setPixelXYTByte(19, 0, 0, valueByte1);
		image.setPixelXYTByte(19, 1, 0, valueByte2);

		image.setPixelXYZTByte(20, 0, 0, 0, valueByte1);
		image.setPixelXYZTByte(20, 1, 0, 0, valueByte2);

		image.setPixelXYZBByte(21, 0, 0, 0, valueByte1);
		image.setPixelXYZBByte(21, 1, 0, 0, valueByte2);

		image.setPixelXYTBByte(22, 0, 0, 0, valueByte1);
		image.setPixelXYTBByte(22, 1, 0, 0, valueByte2);

		image.setPixelXYZTBByte(23, 0, 0, 0, 0, valueByte1);
		image.setPixelXYZTBByte(23, 1, 0, 0, 0, valueByte2);
		
		image.setPixelXYBoolean(24, 0, valueBool1);
		image.setPixelXYBoolean(24, 1, valueBool2);

		image.setPixelXYZBoolean(25, 0, 0, valueBool1);
		image.setPixelXYZBoolean(25, 1, 0, valueBool2);

		image.setPixelXYBBoolean(26, 0, 0, valueBool1);
		image.setPixelXYBBoolean(26, 1, 0, valueBool2);

		image.setPixelXYTBoolean(27, 0, 0, valueBool1);
		image.setPixelXYTBoolean(27, 1, 0, valueBool2);

		image.setPixelXYZTBoolean(28, 0, 0, 0, valueBool1);
		image.setPixelXYZTBoolean(28, 1, 0, 0, valueBool2);

		image.setPixelXYZBBoolean(29, 0, 0, 0, valueBool1);
		image.setPixelXYZBBoolean(29, 1, 0, 0, valueBool2);

		image.setPixelXYTBBoolean(30, 0, 0, 0, valueBool1);
		image.setPixelXYTBBoolean(30, 1, 0, 0, valueBool2);
		
		image.setPixelXYZTBBoolean(31, 0, 0, 0, 0, valueBool1);
		image.setPixelXYZTBBoolean(31, 1, 0, 0, 0, valueBool2);
	}
	
	public void get(Image img1,Image img2){
		
		assertEquals(img1.getPixelXYDouble(0, 0),img2.getPixelXYDouble(0, 0),0.00001);
		assertEquals(img1.getPixelXYDouble(0, 1),img2.getPixelXYDouble(0, 1),0.00001);

		assertEquals(img1.getPixelXYZDouble(1, 0, 0),img2.getPixelXYZDouble(1, 0, 0),0.00001);
		assertEquals(img1.getPixelXYZDouble(1, 1, 0),img2.getPixelXYZDouble(1, 1, 0),0.00001);
		
		assertEquals(img1.getPixelXYBDouble(2, 0, 0),img2.getPixelXYBDouble(2, 0, 0),0.00001);
		assertEquals(img1.getPixelXYBDouble(2, 1, 0),img2.getPixelXYBDouble(2, 1, 0),0.00001);

		assertEquals(img1.getPixelXYTDouble(3, 0, 0),img2.getPixelXYTDouble(3, 0, 0),0.00001);
		assertEquals(img1.getPixelXYTDouble(3, 1, 0),img2.getPixelXYTDouble(3, 1, 0),0.00001);

		assertEquals(img1.getPixelXYZTDouble(4, 0, 0, 0),img2.getPixelXYZTDouble(4, 0, 0, 0),0.00001);
		assertEquals(img1.getPixelXYZTDouble(4, 1, 0, 0),img2.getPixelXYZTDouble(4, 1, 0, 0),0.00001);

		assertEquals(img1.getPixelXYZBDouble(5, 0, 0, 0),img2.getPixelXYZBDouble(5, 0, 0, 0),0.00001);
		assertEquals(img1.getPixelXYZBDouble(5, 1, 0, 0),img2.getPixelXYZBDouble(5, 1, 0, 0),0.00001);
		
		assertEquals(img1.getPixelXYTBDouble(6, 0, 0, 0),img2.getPixelXYTBDouble(6, 0, 0, 0),0.00001);
		assertEquals(img1.getPixelXYTBDouble(6, 1, 0, 0),img2.getPixelXYTBDouble(6, 1, 0, 0),0.00001);

		assertEquals(img1.getPixelXYZTBDouble(7, 0, 0, 0, 0),img2.getPixelXYZTBDouble(7, 0, 0, 0, 0),0.00001);
		assertEquals(img1.getPixelXYZTBDouble(7, 1, 0, 0, 0),img2.getPixelXYZTBDouble(7, 1, 0, 0, 0),0.00001);
		
		assertEquals(img1.getPixelXYInt(8, 0),img2.getPixelXYInt(8, 0));
		assertEquals(img1.getPixelXYInt(8, 1),img2.getPixelXYInt(8, 1));

		assertEquals(img1.getPixelXYZInt(9, 0, 0),img2.getPixelXYZInt(9, 0, 0));
		assertEquals(img1.getPixelXYZInt(9, 1, 0),img2.getPixelXYZInt(9, 1, 0));
		
		assertEquals(img1.getPixelXYBInt(10, 0, 0),img2.getPixelXYBInt(10, 0, 0));
		assertEquals(img1.getPixelXYBInt(10, 1, 0),img2.getPixelXYBInt(10, 1, 0));

		assertEquals(img1.getPixelXYTInt(11, 0, 0),img2.getPixelXYTInt(11, 0, 0));
		assertEquals(img1.getPixelXYTInt(11, 1, 0),img2.getPixelXYTInt(11, 1, 0));

		assertEquals(img1.getPixelXYZTInt(12, 0, 0, 0),img2.getPixelXYZTInt(12, 0, 0, 0));
		assertEquals(img1.getPixelXYZTInt(12, 1, 0, 0),img2.getPixelXYZTInt(12, 1, 0, 0));

		assertEquals(img1.getPixelXYZBInt(13, 0, 0, 0),img2.getPixelXYZBInt(13, 0, 0, 0));
		assertEquals(img1.getPixelXYZBInt(13, 1, 0, 0),img2.getPixelXYZBInt(13, 1, 0, 0));

		assertEquals(img1.getPixelXYTBInt(14, 0, 0, 0),img2.getPixelXYTBInt(14, 0, 0, 0));
		assertEquals(img1.getPixelXYTBInt(14, 1, 0, 0),img2.getPixelXYTBInt(14, 1, 0, 0));

		assertEquals(img1.getPixelXYZTBInt(15, 0, 0, 0, 0),img2.getPixelXYZTBInt(15, 0, 0, 0, 0));
		assertEquals(img1.getPixelXYZTBInt(15, 1, 0, 0, 0),img2.getPixelXYZTBInt(15, 1, 0, 0, 0));
		
		assertEquals(img1.getPixelXYByte(16, 0),img2.getPixelXYByte(16, 0));
		assertEquals(img1.getPixelXYByte(16, 1),img2.getPixelXYByte(16, 1));

		assertEquals(img1.getPixelXYZByte(17, 0, 0),img2.getPixelXYZByte(17, 0, 0));
		assertEquals(img1.getPixelXYZByte(17, 1, 0),img2.getPixelXYZByte(17, 1, 0));

		assertEquals(img1.getPixelXYBByte(18, 0, 0),img2.getPixelXYBByte(18, 0, 0));
		assertEquals(img1.getPixelXYBByte(18, 1, 0),img2.getPixelXYBByte(18, 1, 0));

		assertEquals(img1.getPixelXYTByte(19, 0, 0),img2.getPixelXYTByte(19, 0, 0));
		assertEquals(img1.getPixelXYTByte(19, 1, 0),img2.getPixelXYTByte(19, 1, 0));

		assertEquals(img1.getPixelXYZTByte(20, 0, 0, 0),img2.getPixelXYZTByte(20, 0, 0, 0));
		assertEquals(img1.getPixelXYZTByte(20, 1, 0, 0),img2.getPixelXYZTByte(20, 1, 0, 0));

		assertEquals(img1.getPixelXYZBByte(21, 0, 0, 0),img2.getPixelXYZBByte(21, 0, 0, 0));
		assertEquals(img1.getPixelXYZBByte(21, 1, 0, 0),img2.getPixelXYZBByte(21, 1, 0, 0));

		assertEquals(img1.getPixelXYTBByte(22, 0, 0, 0),img2.getPixelXYTBByte(22, 0, 0, 0));
		assertEquals(img1.getPixelXYTBByte(22, 1, 0, 0),img2.getPixelXYTBByte(22, 1, 0, 0));

		assertEquals(img1.getPixelXYZTBByte(23, 0, 0, 0, 0),img2.getPixelXYZTBByte(23, 0, 0, 0, 0));
		assertEquals(img1.getPixelXYZTBByte(23, 1, 0, 0, 0),img2.getPixelXYZTBByte(23, 1, 0, 0, 0));
		
		assertEquals(img1.getPixelXYBoolean(24, 0),img2.getPixelXYBoolean(24, 0));
		assertEquals(img1.getPixelXYBoolean(24, 1),img2.getPixelXYBoolean(24, 1));

		assertEquals(img1.getPixelXYZBoolean(25, 0, 0),img2.getPixelXYZBoolean(25, 0, 0));
		assertEquals(img1.getPixelXYZBoolean(25, 1, 0),img2.getPixelXYZBoolean(25, 1, 0));

		assertEquals(img1.getPixelXYBBoolean(26, 0, 0),img2.getPixelXYBBoolean(26, 0, 0));
		assertEquals(img1.getPixelXYBBoolean(26, 1, 0),img2.getPixelXYBBoolean(26, 1, 0));

		assertEquals(img1.getPixelXYTBoolean(27, 0, 0),img2.getPixelXYTBoolean(27, 0, 0));
		assertEquals(img1.getPixelXYTBoolean(27, 1, 0),img2.getPixelXYTBoolean(27, 1, 0));

		assertEquals(img1.getPixelXYZTBoolean(28, 0, 0, 0),img2.getPixelXYZTBoolean(28, 0, 0, 0));
		assertEquals(img1.getPixelXYZTBoolean(28, 1, 0, 0),img2.getPixelXYZTBoolean(28, 1, 0, 0));

		assertEquals(img1.getPixelXYZBBoolean(29, 0, 0, 0),img2.getPixelXYZBBoolean(29, 0, 0, 0));
		assertEquals(img1.getPixelXYZBBoolean(29, 1, 0, 0),img2.getPixelXYZBBoolean(29, 1, 0, 0));

		assertEquals(img1.getPixelXYTBBoolean(30, 0, 0, 0),img2.getPixelXYTBBoolean(30, 0, 0, 0));
		assertEquals(img1.getPixelXYTBBoolean(30, 1, 0, 0),img2.getPixelXYTBBoolean(30, 1, 0, 0));
		
		assertEquals(img1.getPixelXYZTBBoolean(31, 0, 0, 0, 0),img2.getPixelXYZTBBoolean(31, 0, 0, 0, 0));
		assertEquals(img1.getPixelXYZTBBoolean(31, 1, 0, 0, 0),img2.getPixelXYZTBBoolean(31, 1, 0, 0, 0));
	}
	
	
	
	/*##########################################################################
	 * tests valables lorsque size renverra un long
	 *##########################################################################*/
	
	
	/*
	@Test
	public void fillLongTest(){
		//Base Time 1 260 000
		LargeDoubleImage largeIm = new LargeDoubleImage(1024,1024,1024,1,3);
		largeIm.fill(0.54);
		for( long i =0;i<largeIm.sizeL();i++){
			assertEquals(0.54,largeIm.getPixelDouble(i),0.00001);
		}
	}
	//*/

	//*
	@Test
	public void maxMinLongBooleanTest(){
		//Base Time 125 000
		LargeBooleanImage largeIm = new LargeBooleanImage(1024,1024,1024,1,3);
		
		assertEquals(false,largeIm.maximumBoolean());	
		
		largeIm.setPixelBoolean(largeIm.sizeL()-1,true);
		assertEquals(true,largeIm.maximumBoolean());
		
		assertEquals(false,largeIm.minimumBoolean());
		
		largeIm.fill(true);
		assertEquals(true,largeIm.minimumBoolean());
	}
	//*/

	/*
	@Test
	public void maxMinLongDoubleTest(){
		//Base Time 10 608 000 
		LargeDoubleImage largeIm = new LargeDoubleImage(1024,1024,1024,1,3);
		for (long i =0;i<largeIm.sizeL();i++){
			largeIm.setPixelDouble(i, (double)i);
		}
		
		assertEquals(0.0,largeIm.minimumDouble(),0.00001);
		assertEquals((double)(largeIm.sizeL()-1),largeIm.maximumDouble(),0.00001);
		
		assertEquals(0,largeIm.minimumDouble(0),0.00001);
		assertEquals(largeIm.sizeL()-3,largeIm.maximumDouble(0),0.00001);
		assertEquals(1.0,largeIm.minimumDouble(1),0.00001);
		assertEquals(largeIm.sizeL()-2,largeIm.maximumDouble(1),0.00001);
		assertEquals(2.0,largeIm.minimumDouble(2),0.00001);
		assertEquals(largeIm.sizeL()-1,largeIm.maximumDouble(2),0.00001);
		
		assertEquals(0.0,largeIm.minimumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(largeIm.sizeL()-3,largeIm.maximumDoubleIgnoreNonRealValues(0),0.00001);
		assertEquals(1.0,largeIm.minimumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(largeIm.sizeL()-2,largeIm.maximumDoubleIgnoreNonRealValues(1),0.00001);
		assertEquals(2.0,largeIm.minimumDoubleIgnoreNonRealValues(2),0.00001);
		assertEquals(largeIm.sizeL()-1,largeIm.maximumDoubleIgnoreNonRealValues(2),0.00001);
		
				
		largeIm.setPixelDouble(0, Double.NEGATIVE_INFINITY);
		assertEquals(Double.NEGATIVE_INFINITY,largeIm.minimumDouble(0),0.00001);
		assertEquals(3.0,largeIm.minimumDoubleIgnoreNonRealValues(0),0.00001);
		
		largeIm.setPixelDouble(0, Double.POSITIVE_INFINITY);

		assertEquals(Double.POSITIVE_INFINITY,largeIm.maximumDouble(0),0.00001);
		assertEquals(largeIm.sizeL()-3,largeIm.maximumDoubleIgnoreNonRealValues(0),0.00001);
						
	}
	//*/
	
	/*
	@Test
	public void volumeLongTest(){
		//Base Time 1 530 000
		LargeDoubleImage largeIm = new LargeDoubleImage(1024,1024,1024,1,3);
		largeIm.fill(1.0);	
		assertEquals(largeIm.sizeL(),largeIm.volume(),0.00001);
	}
	//*/
}