package fr.unistra.pelican.algorithms.io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidFileFormatException;

/**
 * Loads fits images.EXTENSIONs are NOT and will NOT be supported
 *  - float64 works fine<br> - float32 works ok <br> - byte works ok<br> -
 * int32 works ok (? needs more testing)<br> - int16 works ok (? needs more
 * testing)<br> - calibration works ok<br> - multiband works ok<br>
 * 
 * <br>
 * 
 * TODO
 *  - still in need of a way of telling apart byte order - there is no official
 * way (the NASA engineers have disappointed me...).<br> - buffered
 * reading...do we really need it ?<br> - "BLANK pixel to min" operation not
 * tested<br> - add InvalidFileFormatException support<br> - add 64bit float
 * and double support<br> - add image extension support<br>
 * 
 * <br>
 * 
 * NOTES
 * 
 * Fits files can have up to 999 axes.<br>
 * The so-called multiband support of this class takes into account only
 * NAXIS3...<br>
 * thus supporting ONLY a total of 3 (THREE) axes...but of course NAXIS3 can
 * have<br>
 * any dimension it desires...in EA's case NAXIS3 = 6<br>
 * 
 * @author Erchan Aptoula
 * 
 */

public class FitsImageLoad extends Algorithm {
	
	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image output;

	private DataInputStream dis;

	private int bitPix; // pixel coding type

	private int bytesPerPixel;

	private int width = 0;

	private int height = 0;

	private int band = 1;

	private double bscale = 1.0;

	private double bzero = 0.0;

	private boolean scaled = false;

	// private boolean intelByteOrder = false;
	private boolean thereAreBlanks = false;

	private int blankVal = 0; // add support for customizable blank fill
								// (black or white)

	static final int BYTE = 1;

	static final int INT16_SIGNED = 2;

	static final int INT32 = 3;

	static final int INT64 = 4;

	static final int FLOAT32 = 5;

	static final int FLOAT64 = 6;

	static final int RECORD_SIZE = 80;

	static final int HEADER_SIZE = 2880;

	/**
	 * Constructor
	 * 
	 */
	public FitsImageLoad() {

		super();
		super.inputs = "filename";
		super.outputs = "output";
		
	}

	public void launch() {
		try {
			getHeaders();

			double[] pixels = readPixels();
			flip(pixels);

			if (thereAreBlanks == true)
				fillInTheBlanks(pixels); // ALWAYS before calibration
			if (scaled == true)
				calibrate(pixels);

			DoubleImage doubleOutput = new DoubleImage(width, height, 1, 1,
					band);

			for (int b = 0; b < band; b++) {
				for (int j = 0; j < height; j++) {
					for (int i = 0; i < width; i++) {
						double tmp = pixels[b * width * height + j * width + i];
						doubleOutput.setPixelXYBDouble(i, j, b, tmp);
					}
				}
			}

			//doubleOutput = doubleOutput.scaleToZeroOne();

			this.output = doubleOutput;

			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fillInTheBlanks(double[] pixels) {
		// get the min or max according to fill type...
		// black backgrounds are natural for astronomical images => min
		// BLANK used primarily with positive BITPIX => no reals...
		System.err.println("Blanks detected : filling in...");

		double min = Double.MAX_VALUE;

		for (int i = 0; i < pixels.length; i++)
			if (pixels[i] < min)
				min = pixels[i];

		for (int i = 0; i < pixels.length; i++)
			if (pixels[i] == (double) blankVal)
				pixels[i] = min;
	}

	private void calibrate(double[] pixels) {
		System.err.println("[Debug] FitsImageLoad : calibrating with bscale "
				+ bscale + " and bzero " + bzero);

		for (int i = 0; i < pixels.length; i++)
			pixels[i] = bscale * pixels[i] + bzero;

		System.err.println("[Debug] FitsImageLoad : calibration is over");
	}

	private void flip(double[] pixels) {
		int index1, index2;
		double tmp;

		for (int b = 0; b < band; b++) {
			for (int y = 0; y < height / 2; y++) {
				index1 = y * width + b * width * height;
				index2 = (height - 1 - y) * width + b * width * height;

				for (int i = 0; i < width; i++) {
					tmp = pixels[index1];
					pixels[index1++] = pixels[index2];
					pixels[index2++] = tmp;
				}
			}
		}
	}

	private double[] readPixels() throws IOException {
		int pixelCount = width * height * band;
		int byteCount = pixelCount * bytesPerPixel;

		byte[] buffer = new byte[byteCount];
		double[] pixels = new double[pixelCount];
		int tmp = 0;
		long longTmp = 0;
		dis.read(buffer);

		// default : BIG-ENDIAN
		switch (bitPix) {
		case INT32:
			for (int i = 0, j = 0; i < pixelCount; i++, j += 4) {
				tmp = (int) (((buffer[j] & 0xff) << 24)
						| ((buffer[j + 1] & 0xff) << 16)
						| ((buffer[j + 2] & 0xff) << 8) | (buffer[j + 3] & 0xff));
				pixels[i] = (new Integer((int) (tmp & 0xffffffffL)))
						.doubleValue();
			}

			break;
		case INT64:
			for (int i = 0, j = 0; i < pixelCount; i++, j += 8) {
				longTmp = 0;
				longTmp = longTmp | (int) (buffer[j] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 1] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 2] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 3] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 4] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 5] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 6] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 7] & 0xff);

				pixels[i] = (new Long((int) (longTmp & 0xffffffffffffffffL)))
						.doubleValue();
			}

			break;
		case INT16_SIGNED:
			for (int i = 0, j = 0; i < pixelCount; i++, j += 2)
				pixels[i] = (new Short(
						(short) ((((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff)))))
						.doubleValue();
			break;

		case FLOAT32:
			for (int i = 0, j = 0; i < pixelCount; i++, j += 4) {
				tmp = (int) (((buffer[j] & 0xff) << 24)
						| ((buffer[j + 1] & 0xff) << 16)
						| ((buffer[j + 2] & 0xff) << 8) | (buffer[j + 3] & 0xff));
				pixels[i] = (new Float(Float.intBitsToFloat(tmp)))
						.doubleValue();
			}

			break;
		case FLOAT64:
			for (int i = 0, j = 0; i < pixelCount; i++, j += 8) {
				longTmp = 0;
				longTmp = longTmp | (int) (buffer[j] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 1] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 2] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 3] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 4] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 5] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 6] & 0xff);
				longTmp = longTmp << 8;
				longTmp = longTmp | (int) (buffer[j + 7] & 0xff);

				pixels[i] = Double.longBitsToDouble(longTmp);
			}
			break;
		case BYTE:
			for (int i = 0; i < pixelCount; i++)
				pixels[i] = (new Byte(buffer[i])).doubleValue();
			break;
		default:
			return null;
		}

		return pixels;
	}

	private void getHeaders() throws IOException, InvalidFileFormatException {
		// Each header unit consists of any number of 80-character keyword
		// records
		int count = 1;
		dis = new DataInputStream(new FileInputStream(filename));
		String s = getString(RECORD_SIZE);

		if (s.charAt(29) != 'T') { // SIMPLE = T (conforming to standard) of F
									// (non conforming)
			dis.close();
			throw new InvalidFileFormatException("SIMPLE = F" + this.filename
					+ " does not conform to FITS standard");
		}

		do {
			count++;
			s = getString(RECORD_SIZE);

			if (s.startsWith("BITPIX")) {
				// System.err.println("[Debug] FitsImageLoad : header " + count
				// + " " + s);
				switch (getInteger(s)) {
				case 8:
					bitPix = BYTE;
					bytesPerPixel = 1;
					break;
				case 16:
					bitPix = INT16_SIGNED;
					bytesPerPixel = 2;
					break;
				case 32:
					bitPix = INT32;
					bytesPerPixel = 4;
					break;
				case -32:
					bitPix = FLOAT32;
					bytesPerPixel = 4;
					break;
				case 64:
					bitPix = INT64;
					bytesPerPixel = 8;
					break;
				case -64:
					bitPix = FLOAT64;
					bytesPerPixel = 8;
					break;
				default:
					// throw new InvalidFileFormatException("BITPIX must be 8,
					// 16, 32 or -32");
					System.err.println("BITPIX must be 8, 16, 32, -32 or -64");
					dis.close();
					return;
				}

			} else if (s.startsWith("NAXIS ")) {
				int axes = getInteger(s);
				if (axes < 2 || axes > 3)
					throw new InvalidFileFormatException(
							"Not supported FITS variant (NAXIS = " + axes + ")");

			} else if (s.startsWith("NAXIS1"))
				width = getInteger(s);
			else if (s.startsWith("NAXIS2"))
				height = getInteger(s);

			// multi-frame fits...3 axes
			// This keyword gives the number of frames in the 3rd axe
			else if (s.startsWith("NAXIS3"))
				band = getInteger(s);

			else if (s.startsWith("BSCALE")) {
				bscale = getFloat(s);

				if (bscale != 1.0)
					scaled = true;

			} else if (s.startsWith("BZERO")) {
				bzero = getFloat(s);

				if (bzero != 0.0)
					scaled = true;

			} else if (s.startsWith("BLANK")) {
				thereAreBlanks = true;
				blankVal = getInteger(s);
			}
		} while (!s.startsWith("END"));

		if (width == 0) {
			dis.close();
			throw new InvalidFileFormatException("No visual data");
		}

		// skip until the end of a multiple of 2880 bytes...
		// ...and there thou shall encounter what thou seek -> "the image"
		int skipCount = (count * RECORD_SIZE - 1) / 2880;
		skipCount = (skipCount + 1) * HEADER_SIZE - count * RECORD_SIZE;

		dis.skip(skipCount);
	}

	private int getInteger(String s) {
		int tmp = s.indexOf("/");

		if (tmp != -1)
			s = s.substring(10, tmp);
		else
			s = s.substring(10);
		s = s.trim();

		return Integer.parseInt(s);
	}

	private String getString(int length) throws IOException {
		byte[] b = new byte[length];
		dis.read(b);

		return new String(b);
	}

	private double getFloat(String s) {
		int tmp = s.indexOf("/");

		if (tmp != -1)
			s = s.substring(10, tmp);
		else
			s = s.substring(10);

		return Double.parseDouble(s);
	}
	
/**
 * Loads fits images.
 * 
 * @param filename Filename of the fits image.
 * @return the Fits image.
 */
	public static Image exec(String filename) {
		return (Image) new FitsImageLoad().process(filename);
	}
}