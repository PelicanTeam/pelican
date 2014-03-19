package fr.unistra.pelican.util.morphology;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/**
 * Utility class to create images representing gray structuring elements
 * 
 * @author Jonathan Weber
 * 
 */

public class GrayStructuringElement2D {

	
	public Image createConeGrayStructuringElement2D(int radius, int height)
	{
		ByteImage se = new ByteImage(2 * radius + 1, 2 * radius + 1, 1, 1, 1);
		se.resetCenter();
		se.fill(0);
		for (int i = 0; i < 2 * radius + 1; i++) {
			for (int j = 0; j < 2 * radius + 1; j++) {
				double distFromCenter = Math.sqrt(Math.pow(i - radius, 2) + Math.pow(j - radius, 2));
				if (distFromCenter <= radius + 0.000001)
				{
					se.setPixelXYByte(i, j, (int) (1-(distFromCenter/radius)*height));
				}
			}
		}
		return se;
	}
	
}
