package fr.unistra.pelican.algorithms.arithmetic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

public class AdditionTest {

	@Test
	public void testAdd1() {
		Image i1 = new ByteImage(1,1,1,1,1);	
		Image i2 = new ByteImage(1,1,1,1,1);	
		i1.setPixelByte(0, 1);
		i2.setPixelByte(0, 1);
		
		Image i3 = Addition.exec(i1, i2);
		
		assertEquals(i3.getPixelByte(0), 2);
	}
}
