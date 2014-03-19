package fr.unistra.pelican.algorithms.io;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;
import javax.media.jai.RasterFactory;

import com.sun.media.imageioimpl.common.BogusColorSpace;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.jFits.FitsHeader;

/**
 * This class saves the given image into the desired format. All JAI supported
 * formats may be used. Including bmp,jpeg, png and tiff. <br>
 * In case of an unsupported format the input is saved as a tiff file (the
 * default format of JAI). <br>
 * The input can be either a monochannel grayscale image, a tristumulus color
 * image, or even a multispectral image (TIFF is then the only possible output
 * format). In case of depth and time dimensions, the produced file is a
 * multipage TIFF file.
 * 
 * 
 * @author Lefevre
 */
public class ImageSave extends Algorithm {

	/**
	 * First input parameter
	 */
	public String filename;

	/**
	 * Second input parameter
	 */
	public Image input;

	/**
	 * Some other data you want to save  (algorithm dependent)
	 */
	public Object [] auxData;
	
	/**
	 * Constructor
	 * 
	 */
	public ImageSave() {
		super.inputs = "input,filename";
		super.options="auxData";
		super.outputs = "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		BufferedImage img = null;
		String extension = null;
		int bdim = input.getBDim();
		int tdim = input.getTDim();
		int zdim = input.getZDim();
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		
		int indice = filename.lastIndexOf('.');
		if (indice != -1)
			extension = filename.substring(indice + 1).toLowerCase();
		else
			extension = "tiff";

		// 3D or 2D+t image, switch to multiple-page tiff
		if (zdim != 1 || tdim != 1) {
			System.err.println("Non-standard file: switching to multiple-page tiff");
			TiffMultiplePageImageSave.exec(input, filename);
		}
		// Grayscale, Colour, and Multispectral (only TIFF) images
		else {		
			
			if(extension.compareTo("fits")==0 || extension.compareTo("fit")==0)
			{
				FitsHeader h=null;
				int bitPix=-1;
				if(auxData != null && auxData.length>0 && auxData[0]  instanceof Number) {
					bitPix = ((Number) auxData[0]).intValue();	
				}
				if(auxData != null && auxData.length>1 && auxData[1]  instanceof FitsHeader) {
					h = (FitsHeader) auxData[1];	
				}
				FitsImageSave.exec(filename, input,bitPix,h);
				return;
			} else if(extension.compareTo("txt")==0 || extension.compareTo("pix")==0)
			{
				if(auxData!=null && auxData.length>0 && auxData[0] instanceof Character)
					IRAFTextImageSave.exec(filename, input,(Character)auxData[0]);
				else IRAFTextImageSave.exec(filename, input);
				return;
			}else if ( extension.compareTo("pelican")==0 || extension.compareTo("pel")==0
					|| extension.compareTo("plc")==0) {
				 PelicanImageSave.exec(input, filename);
				return;
			}else if ( extension.compareTo("osf")==0) {
				if(input instanceof IntegerImage)
				{
					OdessaSegmentationSave.exec((IntegerImage)input, filename);
					return;
				}				
			}
//			 Graylevel image
			if (bdim == 1 && !(input instanceof IntegerImage)
				&& !(input instanceof DoubleImage)) {
				SampleModel s = RasterFactory.createBandedSampleModel(
					DataBuffer.TYPE_BYTE, xdim, ydim, 1);
				img = imageToBufferedImage(input, s, BufferedImage.TYPE_BYTE_GRAY);
			}
			// Color 8-bit image in RGB
			else if (bdim == 3 && !(input instanceof IntegerImage)) {
				int[] bandOffsets = { 0, 1, 2 };
				SampleModel s = RasterFactory.createPixelInterleavedSampleModel(
					DataBuffer.TYPE_BYTE, xdim, ydim, bdim, bdim * xdim, bandOffsets);
				img = imageToBufferedImage(input, s, BufferedImage.TYPE_3BYTE_BGR);
			}
			// Multispectral image
			else {
				// 32 bits
				if (input instanceof DoubleImage) {
					WritableRaster r = RasterFactory.createBandedRaster(
						DataBuffer.TYPE_DOUBLE, xdim, ydim, bdim, null);
					for (int b = 0; b < bdim; b++)
						for (int y = 0; y < ydim; y++)
							for (int x = 0; x < xdim; x++)
								r.setSample(x, y, b, input.getPixelXYBDouble(x, y, b));
					BogusColorSpace cs = new BogusColorSpace(bdim);
					ComponentColorModel cm = new ComponentColorModel(cs, false, false, 1,
						DataBuffer.TYPE_DOUBLE);
					img = new BufferedImage(cm, r, false, null);
				} // 16 bits
				else if (input instanceof IntegerImage) {
					WritableRaster r = RasterFactory.createBandedRaster(
						DataBuffer.TYPE_SHORT, xdim, ydim, bdim, null);
					for (int b = 0; b < bdim; b++)
						for (int y = 0; y < ydim; y++)
							for (int x = 0; x < xdim; x++)
								r.setSample(x, y, b, input.getPixelXYBInt(x, y, b));
					BogusColorSpace cs = new BogusColorSpace(bdim);
					ComponentColorModel cm = new ComponentColorModel(cs, false, false, 1,
						DataBuffer.TYPE_SHORT);
					img = new BufferedImage(cm, r, false, null);
				}
				// 8 bits
				else {
					WritableRaster r = RasterFactory.createBandedRaster(
						DataBuffer.TYPE_BYTE, xdim, ydim, bdim, null);
					for (int b = 0; b < bdim; b++)
						for (int y = 0; y < ydim; y++)
							for (int x = 0; x < xdim; x++)
								r.setSample(x, y, b, input.getPixelXYBByte(x, y, b));
					BogusColorSpace cs = new BogusColorSpace(bdim);
					ComponentColorModel cm = new ComponentColorModel(cs, false, false, 1,
						DataBuffer.TYPE_BYTE);
					img = new BufferedImage(cm, r, false, null);
				}
			}
			
			
			File f = new File(filename);
			try {
				if(extension.compareTo("pdf")==0)
				{
					PDFImageSave.exec(img,f.getAbsolutePath());
				}
				else if (!ImageIO.write((RenderedImage) img, extension, f)) {
					System.err
						.println("Error encountered with the desired file format: \'" + extension + "\', switching to \'tiff\'");
					ImageIO.write((RenderedImage) img, "tiff", f);
				}
			} catch (Exception e1) {
				throw new InvalidParameterException("PELICAN cannot write :" + filename +"\nError was " +e1,e1);
			}
		}

	}

	static DataBufferDouble imageToDataBufferDouble(Image image) {
		int size = image.size();
		double[] pixels = new double[size];
		for (int i = 0; i < size; i++)
			pixels[i] = image.getPixelDouble(i);
		return new DataBufferDouble(pixels, size);
	}

	static DataBufferInt imageToDataBufferInt(Image image) {
		int size = image.size();
		int[] pixels = new int[size];
		for (int i = 0; i < size; i++)
			pixels[i] = image.getPixelInt(i);
		return new DataBufferInt(pixels, size);
	}

	static DataBufferByte imageToDataBufferByte(Image image) {
		int size = image.size();
		byte[] pixels = new byte[size];
		for (int i = 0; i < size; i++)
			pixels[i] = (byte) image.getPixelByte(i);
		return new DataBufferByte(pixels, size);
	}

	static BufferedImage imageToBufferedImage(Image image, SampleModel s, int type) {
		DataBuffer db = null;
		if (image instanceof IntegerImage)
			db = imageToDataBufferInt(image);
		// // FIXME: 32bits saving doesn't work
		// else if (image instanceof DoubleImage)
		// db = imageToDataBufferDouble(image);
		else
			db = imageToDataBufferByte(image);
		Raster r = RasterFactory.createWritableRaster(s, db, new Point(0, 0));
		BufferedImage img = new BufferedImage(image.getXDim(), image.getYDim(),
			type);
		img.setData(r);
		return img;
	}

	/**
	 * Saves the given images into the desired format.
	 * 
	 * @param image
	 *          Image to be saved.
	 * @param filename
	 *          Path where the image will be saved.
	 */
	public static void exec(Image input, String filename) {
		new ImageSave().process(input, filename);
	}
	
	/**
	 * Saves the given images into the desired format.
	 * 
	 * @param image
	 *          Image to be saved.
	 * @param filename
	 *          Path where the image will be saved.
	 * @param auxData 
	 * 			auxilary datum ... if needed
	 */
	public static void exec(Image input, String filename, Object ... auxData ) {
		new ImageSave().process(input, filename, auxData);
	}

	/**
	 * Print supported format for standard IO Java Interface, Other Pelican types are not listed
	 */
	public static void exec() {
		String readerNames[] = ImageIO.getReaderFormatNames();
		printlist(readerNames, "Reader names:");
		String readerMimes[] = ImageIO.getReaderMIMETypes();
		printlist(readerMimes, "Reader MIME types:");
		String writerNames[] = ImageIO.getWriterFormatNames();
		printlist(writerNames, "Writer names:");
		String writerMimes[] = ImageIO.getWriterMIMETypes();
		printlist(writerMimes, "Writer MIME types:");
	}

	private static void printlist(String names[], String title) {
		System.out.println(title);
		for (int i = 0, n = names.length; i < n; i++) {
			System.out.println("\t" + names[i]);
		}

	}

}
