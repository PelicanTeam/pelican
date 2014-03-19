package fr.unistra.pelican.algorithms.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.IntegerImage;

/** This class allows to save a video segmentation into the ODESSA Segmentation Format
 * This format is inspired by the Berkeley Segmentation Format for images.
 * 
 * Format Description
 * 
 * Segmentation files end in ".osf".
 * 
 * The overall structure of the file is as follows:
 * <header>
 * data
 * <data>
 * 
 * The first part of the file is the header.  The header is ascii text. The header
 * is separated from the data with a line containing the literal text "data".
 * 
 * The header can contain the following information, in any order:
 * 
 * date <date string>
 * width <int>	// width of image
 * height <int>	// height of image
 * length <int> // temporal length of image (in frames)
 * 
 * The {width,height,length} lines are required.  All others lines are optional.
 * 
 * The format is designed to be very easy to parse; it is not optimized for space.
 * Compress osf files if you want smaller files!  Each line in the data section contains 2 integers:
 * 
 * <nb_p> <l>
 * 
 * <l> is the region label; <nb_p> is the number of consecutive pixels to this label
 * 
 * The video is considered as 1D. Dimensions are encapsuled like lines in rows in frames.
 * 
 * 
 * @author Jonathan Weber
 *
 */

public class OdessaSegmentationSave extends Algorithm {

	/**
	 * Input parameter
	 */
	public IntegerImage inputImage;
	
	/**
	 * Output file
	 */
	public String filename;
	
	/**
	 * Constructor
	 * 
	 */
	public OdessaSegmentationSave() {
		super.inputs = "inputImage,filename";
	}
	
	@Override
	public void launch() throws AlgorithmException {
		String seg=new String();
		try
		{
			FileWriter file = new FileWriter(new File(filename));
			Calendar now = Calendar.getInstance();
			file.write("date "+new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(now.getTime())+"\n");
			file.write("height "+inputImage.getYDim()+"\n");
			file.write("width "+inputImage.getXDim()+"\n");
			file.write("length "+inputImage.getTDim()+"\n");
			file.write("data\n");
			long i=1;
			int currentLabel=inputImage.getPixelInt(0);
			int nb_pixel=1;
			while(i<inputImage.size())
			{
				if(inputImage.getPixelInt(i)==currentLabel)
				{
					nb_pixel++;
				}else
				{
					file.write(nb_pixel+" "+currentLabel+"\n");
					currentLabel=inputImage.getPixelInt(i);
					nb_pixel=1;
				}
				i++;
			}
			file.write(nb_pixel+" "+currentLabel+"\n");
			file.close();
		} catch (IOException ex){
			throw new AlgorithmException("Error writing file " +filename, ex);}
		
		

	}
	
	public static void exec(IntegerImage inputImage, String filename) {
		new OdessaSegmentationSave().process(inputImage, filename);
	}
}
