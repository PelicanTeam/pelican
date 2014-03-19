package fr.unistra.pelican.util.morphology;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 * Utility class to create 4D flat structuring element
 * 
 * @author Jonathan Weber
 *
 */
public class FlatStructuringElement4D {
	
	public static BooleanImage create28ConnectivitySEWithoutCenterPixel()
	{
		BooleanImage se = FlatStructuringElement4D.create28ConnectivitySE();		
		se.setPixelXYZTBoolean(1,1,1,1,false);
		return se;
	}
	
	public static BooleanImage create28ConnectivitySE()
	{
		BooleanImage se = new BooleanImage(3,3,3,3,1);
		se.resetCenter();
		se.fill(false);
		se.setImage4D(FlatStructuringElement3D.createSquareFlatStructuringElement(3), 1, Image.T);
		se.setPixelXYZTBoolean(1,1,1,0,true);
		se.setPixelXYZTBoolean(1,1,1,2,true);
		return se;
	}
	
	public static BooleanImage create6TemporalConnectivitySE()
	{
		BooleanImage se = new BooleanImage(3,3,1,3,1);
		se.resetCenter();
		se.fill(false);
		se.setPixelXYZTBoolean(1,1,0,1,true);
		se.setPixelXYZTBoolean(1,0,0,1,true);
		se.setPixelXYZTBoolean(0,1,0,1,true);
		se.setPixelXYZTBoolean(2,1,0,1,true);
		se.setPixelXYZTBoolean(1,2,0,1,true);
		se.setPixelXYZTBoolean(0,0,0,0,true);
		se.setPixelXYZTBoolean(0,0,0,2,true);
		return se;
	}
	public static BooleanImage create10TemporalConnectivitySE()
	{
		BooleanImage se = new BooleanImage(3,3,1,3,1);
		se.resetCenter();
		se.fill(false);
		se.setPixelXYZTBoolean(1,1,0,1,true);
		se.setPixelXYZTBoolean(1,0,0,1,true);
		se.setPixelXYZTBoolean(0,1,0,1,true);
		se.setPixelXYZTBoolean(2,1,0,1,true);
		se.setPixelXYZTBoolean(1,2,0,1,true);
		se.setPixelXYZTBoolean(0,0,0,1,true);
		se.setPixelXYZTBoolean(0,2,0,1,true);
		se.setPixelXYZTBoolean(2,0,0,1,true);
		se.setPixelXYZTBoolean(2,2,0,1,true);
		se.setPixelXYZTBoolean(0,0,0,0,true);
		se.setPixelXYZTBoolean(0,0,0,2,true);
		return se;
	}

}
