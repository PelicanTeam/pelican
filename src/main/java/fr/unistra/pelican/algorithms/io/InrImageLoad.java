package fr.unistra.pelican.algorithms.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Loads images in INRIA (INR) format.
 */
public class InrImageLoad extends Algorithm {

	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image output;

	private FileInputStream fis;

	private InputStream is;

	private int xDim, yDim, zDim, vDim;

	private int bytesPerPixel;

	private int bitsPerPixel;

	private boolean isUnsigned;

	private boolean isInteger;

	private boolean isBigEndian;

	private double voxelXSize, voxelYSize, voxelZSize;

	private final int bufferSize = 4096;

	/**
	 * Constructor
	 * 
	 */
	public InrImageLoad() {

		super();
		super.inputs = "filename";
		super.outputs = "output";
		
	}

	public void launch() throws AlgorithmException {
		// Test if the input file is compressed or not
		try {
			if (filename.substring(filename.length() - 3).compareToIgnoreCase(
					".gz") == 0)
				is = new GZIPInputStream(new FileInputStream(filename));
			else
				is = new DataInputStream(new FileInputStream(filename));
			is = new BufferedInputStream(is);
			// Parse the header
			parseHeader();
			// Generate output
			output = new IntegerImage(xDim, yDim, zDim, 1, 1);

			// Parse the data
			parseRawData();
			// Close the file
			is.close();
		} catch (IOException ex) {
			throw new AlgorithmException("file reading error with file: "
					+ filename);
		}
	}

	private void parseLine(String line) {
		String[] s = line.split("=", 2);

		if (s.length == 2) {
			String rightToken = s[0];
			String leftToken = s[1];
			if (rightToken.compareTo("XDIM") == 0)
				xDim = Integer.parseInt(leftToken);
			if (rightToken.compareTo("YDIM") == 0)
				yDim = Integer.parseInt(leftToken);
			if (rightToken.compareTo("ZDIM") == 0)
				zDim = Integer.parseInt(leftToken);
			if (rightToken.compareTo("VDIM") == 0)
				vDim = Integer.parseInt(leftToken);
			if (rightToken.compareTo("TYPE") == 0)
				isUnsigned = leftToken.startsWith("unsigned");
			if (rightToken.compareTo("PIXSIZE") == 0)
				bytesPerPixel = Integer.parseInt((leftToken.split(" ", 2))[0]) / 8;
			if (rightToken.compareTo("CPU") == 0)
				isBigEndian = leftToken.startsWith("sun")
						|| leftToken.startsWith("sgi");
			if (rightToken.compareTo("VX") == 0)
				voxelXSize = Double.parseDouble(leftToken);
			if (rightToken.compareTo("VY") == 0)
				voxelYSize = Double.parseDouble(leftToken);
			if (rightToken.compareTo("VZ") == 0)
				voxelZSize = Double.parseDouble(leftToken);
		}
	}

	/*
	 * Private methods
	 */
	private void parseHeader() throws IOException {
		byte[] buffer = new byte[256];
		is.read(buffer, 0, 256);
		String[] line = (new String(buffer)).split("\n", 13);
		for (int i = 0; i < line.length; i++)
			parseLine(line[i]);
		// printHeader();
	}

	private void printHeader() {
		System.out.println("xDim          : " + xDim);
		System.out.println("yDim          : " + yDim);
		System.out.println("zDim          : " + zDim);
		System.out.println("isUnsigned    : " + isUnsigned);
		System.out.println("bytesPerPixel : " + bytesPerPixel);
		System.out.println("voxelXSize    : " + voxelXSize);
		System.out.println("voxelYSize    : " + voxelYSize);
		System.out.println("voxelZSize    : " + voxelZSize);
		System.out.println("isBigEndian   : " + isBigEndian);
	}

	private void parseRawData() throws IOException {
		int value;
		int numPixelRead = 0;
		byte[] buffer = new byte[bufferSize];
		;
		int numPixelPerBuffer = bufferSize / bytesPerPixel;
		is.read(buffer, 0, bufferSize);
		for (int z = 0; z < zDim; z++)
			for (int y = 0; y < yDim; y++)
				for (int x = 0; x < xDim; x++) {
					value = 0;
					for (int i = 0; i < bytesPerPixel; i++)
						value += (buffer[numPixelRead * bytesPerPixel
								+ (isBigEndian ? (bytesPerPixel - 1 - i) : i)] & 0x000000FF) << (i * 8);
					numPixelRead++;
					value = isUnsigned ? value
							: ((value >> (bytesPerPixel * 8 - 1)) * 0xFFFF0000)
									| value;
					// System.out.print(" "+value);
					output.setPixelXYZInt(x, y, z, value);
					if (numPixelRead == numPixelPerBuffer) {
						is.read(buffer, 0, bufferSize);
						numPixelRead = 0;
					}
				}
	}
	
	/**
	 * Loads images in INRIA (INR) format.
	 * 
	 * @param filename Filename of the INR image.
	 * @return The INR image.
	 */
	public static Image exec(String filename) {
		return (Image) new InrImageLoad().process(filename);
	}

}
