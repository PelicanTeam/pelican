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
/**
 * This class loads a segmentation image from the berkeley database
 * (http://www.eecs.berkeley.edu/Research/Projects/CS/vision/bsds/)
 * according to the format description (http://www.eecs.berkeley.edu/Research/Projects/CS/vision/bsds/seg-format.txt)
 * 
 * @author Jonathan Weber
 *
 */
public class BerkeleySegmentationImageLoad extends Algorithm {

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
	public BerkeleySegmentationImageLoad() {
		super.inputs = "filename";
		super.outputs="outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException {
		File f=new File(filename);
		int xDim = 0;
		int yDim = 0;
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
				}
				line = br.readLine();
			}
			if(xDim!=0 && yDim!=0)
			{
				outputImage = new IntegerImage(xDim,yDim,1,1,1);
				while((line=br.readLine()) != null)
				{
					String[] tokens = line.split(" ");
					int label = Integer.valueOf(tokens[0]);
					int y = Integer.valueOf(tokens[1]);
					int x1 = Integer.valueOf(tokens[2]);
					int x2 = Integer.valueOf(tokens[3]);
					for(int i=x1;i<=x2;i++)
					{
						outputImage.setPixelXYInt(i, y, label);
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
		return (IntegerImage) new BerkeleySegmentationImageLoad().process(filename);
	}

}
