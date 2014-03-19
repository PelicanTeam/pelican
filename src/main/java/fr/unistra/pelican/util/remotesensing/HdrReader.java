package fr.unistra.pelican.util.remotesensing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;


/**
 * Provides some fields and methods to read an image from its HDR file.
 * The HDR file is read, and then, a class extending the BinReader abstract class is called depending on the format.
 * For the moment, only ENVI format is supported (not ArcView).
 * Some pieces of code are taken from Mustic's RawImage and BinImage.
 * More information here : http://www.brockmann-consult.de/beam/doc/help/BeamDimapFormat.html
 * 
 * @author Clément Hengy
 */
public final class HdrReader {
	
	/** String of the Image category. */
	public static final String IMAGE_CATEG = "Image";
	
	/** String of the Image Size category. */
	public static final String IMAGE_SIZE_CATEG = "Image size";
	
	/** String of the Data category. */
	public static final String DATA_CATEG = "Data";
	
	/** String of the Geographical information category. */
	public static final String GEO_INFO_CATEG = "Geographical information";
	
	/** Code of the BSQ format. */
	public static final int FORMAT_BSQ = 1;

	/** Code of the BIL format. */
	public static final int FORMAT_BIL = 2;

	/** Code of the BIP format. */
	public static final int FORMAT_BIP = 3;
	
	/** Representes the error code. */
	public static final int I_ERR = -1;
	
	/** Represents the width tag in header file. */
	public static final String DESCRIPION_ENVI = "description";
	
	/** Represents the code of description tag. */
	public static final int I_DESC = 0;
	
	 /** Represents the width tag in header file. */
	public static final String SAMPLE_ENVI = "samples";
	
	/** Represents the code of width tag. */
	public static final int I_SAMP = 1;
	
	/** Represents the height tag in header file. */
	public static final String LINES_ENVI = "lines";
	
	/** Represents the code of height tag. */
	public static final int I_LIN = 2;
	
	/** Represents the number of band tag in header file. */
	public static final String BANDS_ENVI = "bands";
	
	/** Represents the code of number of band tag. */
	public static final int I_BAND = 3;
	
	/** Represents the format tag in header file = "bsq", "bil" or "bip". */
	public static final String INTERLEAVE_ENVI = "interleave";
	
	/** Represents the code of format tag. */
	public static final int I_INTERL = 4;
	
	/** Represents the byte order tag in header file. */
	public static final String BYTE_ORDER_ENVI = "byte order";
	
	/** Represents the code of byte order tag. */
	public static final int I_BYTE = 5;
	
	/** Represents the data type tag in header file. */
	public static final String DATA_TYPE_ENVI = "data type";

	/** Represents the code of data type tag. */
	public static final int I_DATA = 6;
	
	/** Represents the (0,0) x position tag in header file. */
	public static final String X_START_ENVI = "x-start";
	public static final String X_START_ENVI2 = "x start";
	
	/** Represents the code of (0,0) x position tag. */
	public static final int I_X = 7;
	
	/** Represents the (0,0) y position tag in header file. */
	public static final String Y_START_ENVI = "y-start";
	public static final String Y_START_ENVI2 = "y start";
	
	/** Represents the code of (0,0) y position tag. */
	public static final int I_Y = 8;
	
	/** Represents the wavelength tag in header file. */
	public static final String WAVELENGTH_ENVI = "wavelength";
	
	/** Represents the code of wavelength tag. */
	public static final int I_WAVE = 9;
	
	/** Represents the map info tag in the header file */
	public static final String MAP_INFO_ENVI = "map info";

	/** Represents the code of the map info tag */
	public static final int I_MAP_INFO = 10;
	
	/** Represents the fwhm tag in the header file */
	public static final String FWHM_ENVI = "fwhm";
	
	/** Represents the code of the fwhm tag */
	public static final int I_FWHM = 11;
	
	/** Represents the band names tag in header file.*/
	public static final String BAND_NAMES_ENVI = "band names";
	
	/**Represents the code of band names tag. */
	public static final int I_BAND_NAMES = 12;
	
	/** String describing the image or processing performed. */
	private String description = "No description";
	
	/** Number of pixels per image line for each band. */
	private int cols;
	
	/** Number of lines per image for each band. */
	private int lines;
	
	/** Number of bands per image file. */
	private int bands;
	
	/**
	 * Depending on this number, the value of each pixel can be stored on 1, 2, 4 or 8 bytes (signed or not).
	 * Here is the list of the supported values and there meaning :
	 * 1 : 1 byte
	 * 2 : 2 bytes (signed integer)
	 * 3 : 4 bytes (signed long integer)
	 * 4 : 4 bytes (floating point)
	 * 5 : 8 bytes (double precision floating point)
	 * 12 : 2 bytes (unsigned integer) 
	 */
	private int dataType;
	
	/** This field can take the values HdrReader.FORMAT_BSQ, HdrReader.FORMAT_BIL or HdrReader.FORMAT_BSQ. */
	private int fileType;
	
	/** True if the values are stored in big-endian (Most Significant Byte First), False otherwise. */
	private boolean byteOrder = true;
	
	/** True if the values are signed, False otherwise. */
	private boolean isSigned;
	
	/** Number of byte per pixel. */
	public static final String BYTE_NUMBER = "byte number";
	
	/** Number of bytes used to store each value. */
	private int bytesNumber;
	
	/** Lists geographic coordinates information in the order of projection name (UTM), reference pixel x location in file coordinates, pixel y, pixel easting, pixel northing, x pixel size, y pixel size, Projection Zone, "North" or "South" for UTM only. */
	private String mapinfo = "No map information";
	
	/** Lists the center wavelength values of each band in an image. */
	private ArrayList<Double> wavelength = new ArrayList<Double>();
	
	/** Lists of the band names */
	private ArrayList<String> bandNames = new ArrayList<String>();
	
	/** Lists the full width at half maximum of each band in an image. */
	private String fwhm = "No FWHM";
	
	private double xstart = 0.0;
	private double ystart = 0.0;
	
	/** Absolute location of the header file*/
	public static final String HEADER_PATH = "header path";
	
	/** The absolute path of the header file. */
	private String headerPath;
	
	/** Represents the x pixel size (in meters) */
	public static final String RESOLUTIONX = "resolution x";
	private double resolutionX = 1.0;
	
	/** Represents the y pixel size (in meters) */
	public static final String RESOLUTIONY = "resolution y";
	private double resolutionY = 1.0;
	
	/** Here is all the crap they don't know where to place it somewhere else. */
	private String history = "No history";
	
	/** A class implementing BinReader will convert the binary file into a fr.unistra.pelican.DoubleImage. */
	private BinReader br;
	
	/**
	 * 
	 * @param path	path of the header file
	 * @return the fr.unistra.pelican Image generated  
	 */
	public Image getPelicanImage(String path){
		this.readHeader(path);
		switch(this.fileType){
			case HdrReader.FORMAT_BSQ:
				br = new BSQReader(this, path);
				break;
			case HdrReader.FORMAT_BIL:
				br = new BILReader(this, path);
				break;
			default:
				System.err.println("getPelicanImage(String) : the asked format is currently not supported");
				return null;
		}
		
		headerPath = path;
		try{
			return br.getPelicanImage();
		}catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * 
	 * @param path	path of the header file
	 * @return the fr.unistra.pelican Image generated  
	 */
	public Image getPelicanImage(String path, int sx, int sy, int ex, int ey){
		this.readHeader(path);
		switch(this.fileType){
			case HdrReader.FORMAT_BSQ:
				br = new BSQReader(this, path);
				break;
			case HdrReader.FORMAT_BIL:
				br = new BILReader(this, path);
				break;
			default:
				System.err.println("getPelicanImage(String) : the asked format is currently not supported");
				return null;
		}
		
		headerPath = path;
		try{
			return br.getPelicanImage(sx, sy, ex, ey);
		}catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * Determines the tag corresponding with the line of a header file.
	 * @param line	a line contained in the header file.
	 * 
	 * @return the tag corresponding with the description or HdrReader.I_ERR if the description is not found.
	 */
	private static int getTypeInfo(String line) {
		int resVal = HdrReader.I_ERR;

		// find the correct code for each keyword
		if (line.startsWith(HdrReader.DESCRIPION_ENVI)) { // the description keyword
			resVal = HdrReader.I_DESC;
		} else if (line.startsWith(HdrReader.SAMPLE_ENVI)) { // the numberof columns keyword
			resVal = HdrReader.I_SAMP;
		} else if (line.startsWith(HdrReader.LINES_ENVI)) { // the number of lines keyword
			resVal = HdrReader.I_LIN;
		} else if (line.startsWith(HdrReader.BANDS_ENVI)) { // the number of bands keyword
			resVal = HdrReader.I_BAND;
		} else if (line.startsWith(HdrReader.INTERLEAVE_ENVI)) { // the type of bin file keyword
			resVal = HdrReader.I_INTERL;
		} else if (line.startsWith(HdrReader.BYTE_ORDER_ENVI)) { // the type of coding keyword
			resVal = HdrReader.I_BYTE;
		} else if (line.startsWith(HdrReader.DATA_TYPE_ENVI)) { // the position of the signed bit keyword
			resVal = HdrReader.I_DATA;
		} else if (line.startsWith(HdrReader.X_START_ENVI) || line.startsWith(HdrReader.X_START_ENVI2)) { // the x-start position keyword
			resVal = HdrReader.I_X;
		} else if (line.startsWith(HdrReader.Y_START_ENVI) || line.startsWith(HdrReader.Y_START_ENVI2)) { // the y-start position keyword
			resVal = HdrReader.I_Y;
		} else if (line.startsWith(HdrReader.WAVELENGTH_ENVI)
				&& line.startsWith(HdrReader.WAVELENGTH_ENVI + " = {")) { // the list of the wavelenght keyword
			resVal = HdrReader.I_WAVE;
		} else if (line.startsWith(HdrReader.MAP_INFO_ENVI)) {
			resVal = HdrReader.I_MAP_INFO;
		} else if(line.startsWith(HdrReader.FWHM_ENVI)) {
			resVal = HdrReader.I_FWHM;
		} else if(line.startsWith(HdrReader.BAND_NAMES_ENVI+" = {")){
			resVal = HdrReader.I_BAND_NAMES;
		}

		return resVal;
	}
	
	/**
	 * Extracts the value from a line in the format : key = value.
	 * @param line	a String that contains the couple (key, value).
	 * @return the value of the line, or null if the character = does not occur.
	 */
	private static String getInfo(String line) {
		String res = null;

			if (line.indexOf("=") != -1)
				res = line.substring(line.indexOf("= ") + 2);

		return res;
	}
	
	/**
	 * Reads and extracts all informations in the header file of the binary image.
	 * @param headerPath	path of the header file
	 */
	public void readHeader(String headerPath){

		FileReader fd = null; // reader to open the header file
		BufferedReader br = null; // buffered to store the file
		String line; // string to store one line of the bufferedReader
		String information; // the information extracted

		String fileName = headerPath;

		// opening the header file
		try {
			fd = new FileReader(fileName);
			br = new BufferedReader(fd);
		} catch (IOException ex) {
			System.err.println("readHeader() : File not found");
			return;
		} catch (IllegalArgumentException ex) {
			System.err.println("readHeader() : The file cannot be opened");
			return;
		}

		// recovery of all informations of the header
		try {
			line = br.readLine(); // reading information
			StringTokenizer stk;
			while (line != null) {
				information = getInfo(line); // extracts the field name

				// result of the reading information
				switch (getTypeInfo(line)) {
				case I_DESC:
					if (information.startsWith("{")) {
						while (!(information.endsWith("}"))) {
							information += "\n";
							line = br.readLine();
							if (line == null) {
								System.err.println("readHeader() : Error while reading Header file");
								return;
							}
							information += line;
						}
					}
					this.description = information; // description line
					break;
				case I_BAND:
					this.bands = Integer.parseInt(information); // number of bands
					break;
				case I_SAMP:
					this.cols = Integer.parseInt(information); // number of columns
					break;
				case I_LIN:
					this.lines = Integer.parseInt(information); // number of lines
					break;
				case I_INTERL:
					if(information.equals("bsq") || information.equals("BSQ"))
						this.fileType = HdrReader.FORMAT_BSQ;
					else if(information.equals("bil") || information.equals("BIL"))
						this.fileType = HdrReader.FORMAT_BIL;
					else if(information.equals("bip") || information.equals("BIP"))
						this.fileType = HdrReader.FORMAT_BIP;
					break;
				case I_WAVE:
					line = line.substring(line.indexOf("{")+1);
					
					while(true){
						stk = new StringTokenizer(line, ",");

						while(stk.hasMoreTokens()){
							String str = stk.nextToken();
							
							if(str.contains("}"))
								str = str.substring(0, str.length()-1);
								
							try{
								wavelength.add(Double.parseDouble(str));
							}
							catch(NumberFormatException ex){
								System.err.println("Little error while reading wavelength, please, check your HDR.");
							}
						}

						if(line.contains("}"))
							break;
						line = br.readLine();
					}
					
					
					
					break;
				case I_FWHM:
					line = br.readLine(); // list of fwhm
					this.fwhm= line; // the list is in the next line after the descriptor
					break;
				case I_DATA:
					this.dataType = Integer.parseInt(information); // coding type
					break;
				case I_BYTE:
					this.byteOrder = (Integer.parseInt(information) == 1); // order of the bytes in the bin file
					break;
				case I_MAP_INFO:
					this.mapinfo = information;
					// next section will extract x and y resolution
					stk = new StringTokenizer(information, ",");
					for(int i = 0; i < 5; ++i){stk.nextToken();}
					resolutionX = Double.parseDouble(stk.nextToken());
					resolutionY = Double.parseDouble(stk.nextToken());
					break;
				case I_X:
					this.xstart = Double.parseDouble(information);
					break;
				case I_Y:
					this.ystart = Double.parseDouble(information);
					break;
				case I_BAND_NAMES:
					line = line.substring(line.indexOf("{")+1);
					
					while(true){
						stk = new StringTokenizer(line, ",");

						while(stk.hasMoreTokens()){
							String str = stk.nextToken();
							
							if(str.contains("}"))
								str = str.substring(0, str.length()-1);
								
							try{
								if(str.length()!=1)
									bandNames.add(str);
							}
							catch(NumberFormatException ex){
								System.err.println("Little error while reading wavelength, please, check your HDR.");
							}
						}

						if(line.contains("}"))
							break;
						line = br.readLine();
					}
					break;
				default:
					break;
				}

				// read the next line of the header file
				line = br.readLine();
			}

			// all the header file is read. Closes of the readers
			br.close();
			fd.close();

			switch (this.dataType) {
			case 1:
				this.bytesNumber = 1;
				this.isSigned = false;
				break;
			case 2:
				this.bytesNumber = 2;
				this.isSigned = true;
				break;
			case 3:
			case 4:
				this.bytesNumber = 4;
				this.isSigned = true;
				break;

			case 5:
				this.bytesNumber = 8;
				this.isSigned = true;
				break;
			case 12:
				this.bytesNumber = 2;
				this.isSigned = false;
				break;
			default:
				this.bytesNumber = 8;
				this.isSigned = true;
				System.out.println("readHeader() : no dataType or unsupported dataType. Assumed values are 8 bytes signed.");
				break;
			}

		} catch (IOException ex) {
			System.err.println("readHeader() : Error while Header file reading process -> " + ex.toString());
		} catch (IllegalArgumentException ex) {
			System.err.println("readHeader() : Error while Header file reading process -> " + ex.toString());
		}
	}


	public int getBands() {
		return bands;
	}


	public boolean getByteOrder() {
		return byteOrder;
	}
	



	public int getBytesNumber() {
		return bytesNumber;
	}


	public int getCols() {
		return cols;
	}


	public int getDataType() {
		return dataType;
	}


	public String getDescription() {
		return description;
	}

	public double getXStart() {
		return xstart;
	}
	
	
	public double getYStart() {
		return ystart;
	}
	
	
	public int getFileType() {
		return fileType;
	}


	public String getFwhm() {
		return fwhm;
	}


	public String getHistory() {
		return history;
	}


	public boolean isSigned() {
		return isSigned;
	}


	public int getLines() {
		return lines;
	}


	public String getMapinfo() {
		return mapinfo;
	}


	public Double[] getWavelength() {
		return this.wavelength.toArray(new Double[0]);
	}
	
	public String[] getBandNames(){
		return this.bandNames.toArray(new String[0]);
	}

	
	public String getHeaderPath(){
		return headerPath;
	}
	
	public double getResolutionX(){
		return resolutionX;
	}
	
	public double getResolutionY(){
		return resolutionY;
	}

	
	public static void main(String[] args)
	{
		//String path = "/home/hengy/Desktop/images/LVlandsat.hdr";
		String path = "/home/hengy/double.hdr";
		
		Image img = (Image) new ImageLoader().process(path);
		
		img = (Image) new ContrastStretchEachBands().process(img);
		//img = fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage.process(img, 0, 3, 2);
		new Viewer2D().process(img, "toto");
		
		/*
		HdrReader hr = new HdrReader();
		
		hr.readHeader(path);
		
		System.out.println("******** Informations : ********");
		System.out.println("Description : " + hr.description);
		System.out.println("Nombre de lignes : " + hr.lines);
		System.out.println("Nombre de colonnes : " + hr.cols);
		System.out.println("Nombre de bandes : " + hr.bands);
		System.out.println("Data type : " + hr.dataType);
		System.out.println("File type : " + hr.fileType);
		System.out.println("Ordre des octets : " + hr.byteOrder);
		System.out.println("Signé : " + hr.isSigned);
		System.out.println("Nombre d'octets : " + hr.bytesNumber);
		//for(int i = 0; i < hr.wavelength.length; ++i)
			//System.out.println("WaveLength : " + hr.wavelength[i]);
		System.out.println("********************************");
		
		System.out.println("Image en préparation");
		Image img = hr.getPelicanImage(path);
		//img = ((fr.unistra.pelican.DoubleImage) img).scaleToZeroOne();
		img = ((fr.unistra.pelican.IntegerImage) img).scaleToVisibleRange();
		img = fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage.process(img,0,1,2);
		System.out.println("Affichage de l'image");
		//img.setColor(true);
		fr.unistra.pelican.algorithms.visualisation.Viewer2D.exec(img, "toto");
		*/
	}
}
