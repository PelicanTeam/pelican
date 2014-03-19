/**
 * 
 */
package fr.unistra.pelican;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Ben
 *
 */
public class ImageTest {

	private int xdim=2;
	private int ydim=3;
	private int zdim=4;
	private int tdim=5;
	private int bdim=6;
	
	private int bxyztToLinear(int b,int x, int y, int z, int t)
	{
		return b + bdim * (x + xdim * (y + ydim * (z + t * zdim)));
	}
	
	
	@Test
	public void imageDoubleReadTest()
	{
		DoubleImage im = new DoubleImage(xdim,ydim,zdim,tdim,bdim);
		for(int i=0;i<im.size();i++)
			im.setPixelDouble(i, i);
		
		assertEquals(im.getPixelDouble(1,1,1,1,1), bxyztToLinear(1,1,1,1,1),0.000001);
		assertEquals(im.getPixelXYZTBDouble(1,1,1,1,1), bxyztToLinear(1,1,1,1,1),0.000001);
		assertEquals(im.getPixelXYZTDouble(1,1,1,1), bxyztToLinear(0,1,1,1,1),0.000001);
		assertEquals(im.getPixelXYZDouble(1,1,1), bxyztToLinear(0,1,1,1,0),0.000001);
		assertEquals(im.getPixelXYDouble(1,1), bxyztToLinear(0,1,1,0,0),0.000001);
		assertEquals(im.getPixelXYBDouble(1,1,1), bxyztToLinear(1,1,1,0,0),0.000001);
		assertEquals(im.getPixelXYTDouble(1,1,1), bxyztToLinear(0,1,1,0,1),0.000001);
		assertEquals(im.getPixelXYZBDouble(1,1,1,1), bxyztToLinear(1,1,1,1,0),0.000001);
		assertEquals(im.getPixelXYTBDouble(1,1,1,1), bxyztToLinear(1,1,1,0,1),0.000001);
	}
	
	@Test
	public void imageDoubleWriteTest()
	{
		DoubleImage im = new DoubleImage(xdim,ydim,zdim,tdim,bdim);
		double val=1.0;
		
		im.setPixelDouble(1,1,1,1,1,val);
		im.setPixelXYZTBDouble(1,1,1,1,1,val);
		im.setPixelXYZTDouble(1,1,1,1,val);
		im.setPixelXYZDouble(1,1,1,val);
		im.setPixelXYDouble(1,1,val);
		im.setPixelXYBDouble(1,1,1,val);
		im.setPixelXYTDouble(1,1,1,val);
		im.setPixelXYZBDouble(1,1,1,1,val);
		im.setPixelXYTBDouble(1,1,1,1,val);
		
		assertEquals(im.getPixelDouble(1,1,1,1,1), val,0.000001);
		assertEquals(im.getPixelXYZTBDouble(1,1,1,1,1), val,0.000001);
		assertEquals(im.getPixelXYZTDouble(1,1,1,1), val,0.000001);
		assertEquals(im.getPixelXYZDouble(1,1,1), val,0.000001);
		assertEquals(im.getPixelXYDouble(1,1), val,0.000001);
		assertEquals(im.getPixelXYBDouble(1,1,1), val,0.000001);
		assertEquals(im.getPixelXYTDouble(1,1,1), val,0.000001);
		assertEquals(im.getPixelXYZBDouble(1,1,1,1), val,0.000001);
		assertEquals(im.getPixelXYTBDouble(1,1,1,1), val,0.000001);
	}
	
	@Test
	public void imageBooleanReadTest()
	{
		BooleanImage im = new BooleanImage(xdim,ydim,zdim,tdim,bdim);
		for(int i=0;i<im.size();i++)
			im.setPixelBoolean(i, i%2 == 0); // et pour quoi pas ?
		
		assertEquals(im.getPixelBoolean(1,1,1,1,1), bxyztToLinear(1,1,1,1,1)%2 == 0);
		assertEquals(im.getPixelXYZTBBoolean(1,1,1,1,1), bxyztToLinear(1,1,1,1,1)%2 == 0);
		assertEquals(im.getPixelXYZTBoolean(1,1,1,1), bxyztToLinear(0,1,1,1,1)%2 == 0);
		assertEquals(im.getPixelXYZBoolean(1,1,1), bxyztToLinear(0,1,1,1,0)%2 == 0);
		assertEquals(im.getPixelXYBoolean(1,1), bxyztToLinear(0,1,1,0,0)%2 == 0);
		assertEquals(im.getPixelXYBBoolean(1,1,1), bxyztToLinear(1,1,1,0,0)%2 == 0);
		assertEquals(im.getPixelXYTBoolean(1,1,1), bxyztToLinear(0,1,1,0,1)%2 == 0);
		assertEquals(im.getPixelXYZBBoolean(1,1,1,1), bxyztToLinear(1,1,1,1,0)%2 == 0);
		assertEquals(im.getPixelXYTBBoolean(1,1,1,1), bxyztToLinear(1,1,1,0,1)%2 == 0);
	}
	
	@Test
	public void imageBooleanWriteTest()
	{
		BooleanImage im = new BooleanImage(xdim,ydim,zdim,tdim,bdim);
		im.fill(false);
		boolean val=true;
		
		im.setPixelBoolean(1,1,1,1,1,val);
		im.setPixelXYZTBBoolean(1,1,1,1,1,val);
		im.setPixelXYZTBoolean(1,1,1,1,val);
		im.setPixelXYZBoolean(1,1,1,val);
		im.setPixelXYBoolean(1,1,val);
		im.setPixelXYBBoolean(1,1,1,val);
		im.setPixelXYTBoolean(1,1,1,val);
		im.setPixelXYZBBoolean(1,1,1,1,val);
		im.setPixelXYTBBoolean(1,1,1,1,val);
		
		assertEquals(im.getPixelBoolean(1,1,1,1,1), val);
		assertEquals(im.getPixelXYZTBBoolean(1,1,1,1,1), val);
		assertEquals(im.getPixelXYZTBoolean(1,1,1,1), val);
		assertEquals(im.getPixelXYZBoolean(1,1,1), val);
		assertEquals(im.getPixelXYBoolean(1,1), val);
		assertEquals(im.getPixelXYBBoolean(1,1,1), val);
		assertEquals(im.getPixelXYTBoolean(1,1,1), val);
		assertEquals(im.getPixelXYZBBoolean(1,1,1,1), val);
		assertEquals(im.getPixelXYTBBoolean(1,1,1,1), val);
	}
	

	@Test
	public void imageByteReadTest()
	{
		ByteImage im = new ByteImage(xdim,ydim,zdim,tdim,bdim);
		for(int i=0;i<im.size();i++)
			im.setPixelByte(i, i); // et pour quoi pas ?
		
		assertEquals(im.getPixelByte(1,1,1,1,1), bxyztToLinear(1,1,1,1,1));
		assertEquals(im.getPixelXYZTBByte(1,1,1,1,1), bxyztToLinear(1,1,1,1,1));
		assertEquals(im.getPixelXYZTByte(1,1,1,1), bxyztToLinear(0,1,1,1,1));
		assertEquals(im.getPixelXYZByte(1,1,1), bxyztToLinear(0,1,1,1,0));
		assertEquals(im.getPixelXYByte(1,1), bxyztToLinear(0,1,1,0,0));
		assertEquals(im.getPixelXYBByte(1,1,1), bxyztToLinear(1,1,1,0,0));
		assertEquals(im.getPixelXYTByte(1,1,1), bxyztToLinear(0,1,1,0,1));
		assertEquals(im.getPixelXYZBByte(1,1,1,1), bxyztToLinear(1,1,1,1,0));
		assertEquals(im.getPixelXYTBByte(1,1,1,1), bxyztToLinear(1,1,1,0,1));
	}
	
	@Test
	public void imageByteWriteTest()
	{
		ByteImage im = new ByteImage(xdim,ydim,zdim,tdim,bdim);
		im.fill(0);
		byte val=2;
		
		im.setPixelByte(1,1,1,1,1,val);
		im.setPixelXYZTBByte(1,1,1,1,1,val);
		im.setPixelXYZTByte(1,1,1,1,val);
		im.setPixelXYZByte(1,1,1,val);
		im.setPixelXYByte(1,1,val);
		im.setPixelXYBByte(1,1,1,val);
		im.setPixelXYTByte(1,1,1,val);
		im.setPixelXYZBByte(1,1,1,1,val);
		im.setPixelXYTBByte(1,1,1,1,val);
		
		assertEquals(im.getPixelByte(1,1,1,1,1), val);
		assertEquals(im.getPixelXYZTBByte(1,1,1,1,1), val);
		assertEquals(im.getPixelXYZTByte(1,1,1,1), val);
		assertEquals(im.getPixelXYZByte(1,1,1), val);
		assertEquals(im.getPixelXYByte(1,1), val);
		assertEquals(im.getPixelXYBByte(1,1,1), val);
		assertEquals(im.getPixelXYTByte(1,1,1), val);
		assertEquals(im.getPixelXYZBByte(1,1,1,1), val);
		assertEquals(im.getPixelXYTBByte(1,1,1,1), val);
	}

	@Test
	public void imageIntReadTest()
	{
		IntegerImage im = new IntegerImage(xdim,ydim,zdim,tdim,bdim);
		for(int i=0;i<im.size();i++)
			im.setPixelInt(i, i); // et pour quoi pas ?
		
		assertEquals(im.getPixelInt(1,1,1,1,1), bxyztToLinear(1,1,1,1,1));
		assertEquals(im.getPixelXYZTBInt(1,1,1,1,1), bxyztToLinear(1,1,1,1,1));
		assertEquals(im.getPixelXYZTInt(1,1,1,1), bxyztToLinear(0,1,1,1,1));
		assertEquals(im.getPixelXYZInt(1,1,1), bxyztToLinear(0,1,1,1,0));
		assertEquals(im.getPixelXYInt(1,1), bxyztToLinear(0,1,1,0,0));
		assertEquals(im.getPixelXYBInt(1,1,1), bxyztToLinear(1,1,1,0,0));
		assertEquals(im.getPixelXYTInt(1,1,1), bxyztToLinear(0,1,1,0,1));
		assertEquals(im.getPixelXYZBInt(1,1,1,1), bxyztToLinear(1,1,1,1,0));
		assertEquals(im.getPixelXYTBInt(1,1,1,1), bxyztToLinear(1,1,1,0,1));
	}
	
	@Test
	public void imageIntWriteTest()
	{
		IntegerImage im = new IntegerImage(xdim,ydim,zdim,tdim,bdim);
		im.fill(0);
		int val=2;
		
		im.setPixelInt(1,1,1,1,1,val);
		im.setPixelXYZTBInt(1,1,1,1,1,val);
		im.setPixelXYZTInt(1,1,1,1,val);
		im.setPixelXYZInt(1,1,1,val);
		im.setPixelXYInt(1,1,val);
		im.setPixelXYBInt(1,1,1,val);
		im.setPixelXYTInt(1,1,1,val);
		im.setPixelXYZBInt(1,1,1,1,val);
		im.setPixelXYTBInt(1,1,1,1,val);
		
		assertEquals(im.getPixelInt(1,1,1,1,1), val);
		assertEquals(im.getPixelXYZTBInt(1,1,1,1,1), val);
		assertEquals(im.getPixelXYZTInt(1,1,1,1), val);
		assertEquals(im.getPixelXYZInt(1,1,1), val);
		assertEquals(im.getPixelXYInt(1,1), val);
		assertEquals(im.getPixelXYBInt(1,1,1), val);
		assertEquals(im.getPixelXYTInt(1,1,1), val);
		assertEquals(im.getPixelXYZBInt(1,1,1,1), val);
		assertEquals(im.getPixelXYTBInt(1,1,1,1), val);
	}



	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
