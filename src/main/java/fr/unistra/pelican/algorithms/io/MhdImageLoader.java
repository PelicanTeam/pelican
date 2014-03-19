package fr.unistra.pelican.algorithms.io;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.remotesensing.DataInputFactory;

/**
 * This class read mhd image file (MetaImage format).
 * It manages not all the possible types for now. 
 * 
 * @author weber
 *
 */
public class MhdImageLoader extends Algorithm {

	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image output;
	
	
	public static final int TYPEFLOAT = 1;
	
	public static final int LITTLEENDIAN = 0;
	public static final int BIGENDIAN = 1;
	
	/**
	 * Constructor
	 * 
	 */
	public MhdImageLoader() {

		super();
		super.inputs = "filename";
		super.outputs = "output";
		
	}
	
	@Override
	public void launch() throws AlgorithmException 
	{
		// Reader reading
		FileReader fd = null; // reader to open the header file
		BufferedReader br = null; // buffered to store the file
		String line; // string to store one line of the bufferedReader
		String information; // the information extracted

		// opening the header file
		try {
			fd = new FileReader(filename);
			br = new BufferedReader(fd);
		} catch (IOException ex) {
			System.err.println("readHeader() : File not found");
			return;
		} catch (IllegalArgumentException ex) {
			System.err.println("readHeader() : The file cannot be opened");
			return;
		}
		
		int nbDim=-1;
		int[] dimSizes=null;
		int dataType=-1;
		String dataFile=null;
		int byteOrder=LITTLEENDIAN;
		
		// recovery of all informations of the header file
		try {
				line = br.readLine(); // reading information
				StringTokenizer stk;
				while(line!=null)
				{
					String info = getInfo(line);
					//Determining info type and managing it
					if(line.startsWith("NDims")) //get number of dimensions
					{
						nbDim=Integer.valueOf(info);
					} else if(line.startsWith("DimSize")) //get dimensions size
					{
						String[] tDims= info.split(" ");
						dimSizes = new int[tDims.length];
						for(int i=0;i<tDims.length;i++)
							dimSizes[i]=Integer.valueOf(tDims[i]);
					}else if(line.startsWith("ElementType")) //get data type
					{
						if(info.equalsIgnoreCase("MET_FLOAT"))
						{
							dataType=TYPEFLOAT;
						}
						else
							throw new PelicanException("This type of data ("+info+") is not managed by "+this.getClass().toString());
					}else if(line.startsWith("ElementDataFile")) //get data file name
					{
						dataFile=info;
					}else if(line.startsWith("BinaryDataByteOrderMSB")) //get data file name
					{
						if(Boolean.valueOf(info))
						{
							byteOrder=BIGENDIAN;
						}else
						{
							byteOrder=LITTLEENDIAN;
						}
					}else
					{
						//TODO: manage other info 
					}
					// read the next line of the header file
					line = br.readLine();
				}
				
				// all the header file is read. Closes of the readers
				br.close();
				fd.close();
		} catch (IOException ex) {
			System.err.println("readHeader() : Error while Header file reading process -> " + ex.toString());
		} catch (IllegalArgumentException ex) {
			System.err.println("readHeader() : Error while Header file reading process -> " + ex.toString());
		}
		/*System.out.println("NbDim : " +nbDim);
		System.out.println("x : " +dimSizes[0]+" | y : " +dimSizes[1]+" | z : " +dimSizes[2]);
		System.out.println(dataType);
		System.out.println("Data file : "+dataFile);
		System.out.println("Big Endian : "+(byteOrder==BIGENDIAN));
		*/
		// opening the data file
		try 
		{
			FileInputStream fis = new FileInputStream(filename.substring(0, filename.lastIndexOf(File.separator)+1)+dataFile);
			DataInput bf = null;
			if(byteOrder==LITTLEENDIAN)
				bf = DataInputFactory.createDataInputLittleEndian(fis);
			else
				bf = DataInputFactory.createDataInputBigEndian(fis);


			if(dataType==TYPEFLOAT)
			{
				if(nbDim==3)
				{
					int size=dimSizes[0]*dimSizes[1]*dimSizes[2];
					output = new DoubleImage(dimSizes[0],dimSizes[1],dimSizes[2],1,1);
					for(int i=0;i<size;i++)
						output.setPixelDouble(i,bf.readFloat());
				}
			}

			fis.close();
		} catch (IOException ex) 
		{
			System.err.println("readHeader() : File not found");
			return;
		} catch (IllegalArgumentException ex) 
		{
			System.err.println("readHeader() : The file cannot be opened");
			return;
		}
	}
	
	/**
	 * Extracts the value from a line in the format : key = value.
	 * @param line	a String that contains the couple (key, value).
	 * @return the value of the line, or null if the character = does not occur.
	 */
	private static String getInfo(String line) {
		String res = null;

			if (line.indexOf("= ") != -1)
			{
				res = line.substring(line.indexOf("= ") + 2);
			} else if (line.indexOf("=") != -1)
			{
				res = line.substring(line.indexOf("=") + 1);
			}
		return res;
	}
	
	
	/**
	 *  Loads mhd images.
	 * 
	 * @param filename Filename of the mhd image.
	 * @return The mhd image.
	 */
	public static Image exec(String filename) {
		return (Image) new MhdImageLoader().process(filename);
	}

}
