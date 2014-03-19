package fr.unistra.pelican.algorithms.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;

/** This class allows to load a video segmentation from a file using the ODESSA Segmentation Format
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
public class OdessaSegmentationLoad extends Algorithm {

	/**
	 * Input parameter
	 */
	public String filename;
	
	/**
	 * Output parameter
	 */
	public IntegerImage outputImage;
	
	/**
	 * Constructor
	 * 
	 */
	public OdessaSegmentationLoad() {
		super.inputs = "filename";
		super.outputs="outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException {
		File f=new File(filename);
		int xDim = 0;
		int yDim = 0;
		int tDim = 0;
		try
		{
			BufferedReader br= new BufferedReader(new FileReader(f));
			String line = br.readLine();
			while(!line.equalsIgnoreCase("data"))
			{
				String[] tokens = line.split(" ");
				if(tokens[0].equalsIgnoreCase("height"))
				{
					yDim = Integer.valueOf(tokens[1]);
				} else if(tokens[0].equalsIgnoreCase("width"))
				{
					xDim = Integer.valueOf(tokens[1]);
				} else if(tokens[0].equalsIgnoreCase("length"))
				{
					tDim = Integer.valueOf(tokens[1]);
				}
				line = br.readLine();
			}
			if(xDim!=0 && yDim!=0 && tDim!=0)
			{
				outputImage = new IntegerImage(xDim,yDim,1,tDim,1);
				int i=0;
				while((line=br.readLine()) != null)
				{
					String[] tokens = line.split(" ");
					int nb_pixel = Integer.valueOf(tokens[0]);
					int label = Integer.valueOf(tokens[1]);
					for(int j=0;j<nb_pixel;j++,i++)
					{
						outputImage.setPixelInt(i, label);
					}
				}
			} else
			{
				throw(new PelicanException(filename+" has a bad format"));
			}
		}
		catch (FileNotFoundException e) {
			throw new AlgorithmException("Error opening file " +f,e);
		} catch (IOException e) {
			throw new AlgorithmException("Error reading file " +f,e);
		}catch (NumberFormatException e) {
			throw new AlgorithmException("Error reading file " +f + " : value can not be parsed as a double.",e);
		}

	}
	
	public static IntegerImage exec(String filename) {
		return (IntegerImage) new OdessaSegmentationLoad().process(filename);
	}

}
