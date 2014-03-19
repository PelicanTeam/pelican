package fr.unistra.pelican.util.remotesensing;

import java.io.File;

import fr.unistra.pelican.Image;


/**
 * You have to extend this abstract class in order to read binary files.
 * For example, BSQReader extends BinReader and overrides getPelicanImage().
 * If you have nothing to do, you should write BIPReader.
 * 
 * @author Clément Hengy
 */
public abstract class BinReader {
	
	public HdrReader hr;
	public File path;
	public Image img;
	
	/**
	 * This function reads the binary file according to its construction.
	 * After that, it call setProperties to set up all the properties.
	 * To finish, it returns the fr.unistra.pelican.Image.
	 * 
	 * @return the fr.unistra.pelican.Image, built according to the binary file or null if an error occured during the building.
	 */
	public abstract Image getPelicanImage()  throws Throwable;
	
	/**
	 * This function reads a portion of the binary file according to its construction.
	 * After that, it call setProperties to set up all the properties.
	 * To finish, it returns the fr.unistra.pelican.Image.
	 * 
	 * @return the fr.unistra.pelican.Image, built according to the binary file or null if an error occured during the building.
	 * @throws Throwable 
	 */
	public abstract Image getPelicanImage(int sx, int sy, int ex, int ey) throws Throwable;
	
	public void setProperties(){
		int i = 0;
		
		img.setProperty(HdrReader.DESCRIPION_ENVI, hr.getDescription());
		img.setProperty(HdrReader.SAMPLE_ENVI, hr.getCols());
		img.setProperty(HdrReader.LINES_ENVI, hr.getLines());
		img.setProperty(HdrReader.X_START_ENVI, hr.getXStart());
		img.setProperty(HdrReader.Y_START_ENVI, hr.getYStart());
		img.setProperty(HdrReader.BYTE_NUMBER, hr.getBytesNumber());
		img.setProperty(HdrReader.HEADER_PATH, hr.getHeaderPath());
		img.setProperty(HdrReader.RESOLUTIONX, hr.getResolutionX());
		img.setProperty(HdrReader.RESOLUTIONY, hr.getResolutionY());
		if(0 != hr.getWavelength().length)
			for(double wave : hr.getWavelength())
				img.setProperty(HdrReader.WAVELENGTH_ENVI+(i++), wave);
		
	}
}
