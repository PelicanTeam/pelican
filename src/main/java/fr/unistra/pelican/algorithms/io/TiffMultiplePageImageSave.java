package fr.unistra.pelican.algorithms.io;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.RasterFactory;
import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;
import com.sun.media.imageioimpl.common.BogusColorSpace;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Save multiple-page TIFF images in 8, 16 or 32 bits
 * 
 * @author Lefevre
 * 
 */

public class TiffMultiplePageImageSave extends Algorithm {

	/**
	 * Image to be saved
	 */
	public Image input;

	/**
	 * Name of the file
	 */
	public String filename;

	/**
	 * Constructor
	 */
	public TiffMultiplePageImageSave() {
		super.inputs = "input,filename";
	}

	/**
	 * Save Tiff images.
	 * 
	 * @param input
	 *          input image
	 * @param filename
	 *          Filename of the Tiff image.
	 * @return The Tiff image.
	 */
	public static void exec(Image input, String filename) {
		new TiffMultiplePageImageSave().process(input, filename);
	}

	public void launch() {
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();

		try {
			ImageWriter writer = ImageIO.getImageWritersByFormatName("tif").next();
			ImageOutputStream ios = ImageIO
				.createImageOutputStream(new File(filename));
			writer.setOutput(ios);
			writer.prepareWriteSequence(null);
			TIFFImageWriteParam tiffWriteParam = new TIFFImageWriteParam(
				Locale.ENGLISH);
			tiffWriteParam.setCompressionMode(tiffWriteParam.MODE_DEFAULT);

			for (int t = 0; t < tdim; t++)
				for (int z = 0; z < zdim; z++) {
					Image work = input;
					if (tdim > 1)
						work = work.getImage4D(t, Image.T);
					if (zdim > 1)
						work = work.getImage4D(z, Image.Z);

					if (input instanceof DoubleImage) {
						WritableRaster r = RasterFactory.createBandedRaster(
							DataBuffer.TYPE_DOUBLE, xdim, ydim, bdim, null);
						for (int b = 0; b < bdim; b++)
							for (int y = 0; y < ydim; y++)
								for (int x = 0; x < xdim; x++)
									r.setSample(x, y, b, work.getPixelXYBDouble(x, y, b));
						BogusColorSpace cs = new BogusColorSpace(bdim);
						ComponentColorModel cm = new ComponentColorModel(cs, false, false,
							1, DataBuffer.TYPE_DOUBLE);
						BufferedImage img = new BufferedImage(cm, r, false, null);
						IIOImage image = new IIOImage(img, null, writer
							.getDefaultStreamMetadata(tiffWriteParam));
						writer.writeToSequence(image, tiffWriteParam);
					} else if (input instanceof IntegerImage) {
						WritableRaster r = RasterFactory.createBandedRaster(
							DataBuffer.TYPE_SHORT, xdim, ydim, bdim, null);
						for (int b = 0; b < bdim; b++)
							for (int y = 0; y < ydim; y++)
								for (int x = 0; x < xdim; x++)
									r.setSample(x, y, b, work.getPixelXYBInt(x, y, b));
						BogusColorSpace cs = new BogusColorSpace(bdim);
						ComponentColorModel cm = new ComponentColorModel(cs, false, false,
							1, DataBuffer.TYPE_SHORT);
						BufferedImage img = new BufferedImage(cm, r, false, null);
						IIOImage image = new IIOImage(img, null, writer
							.getDefaultStreamMetadata(tiffWriteParam));
						writer.writeToSequence(image, tiffWriteParam);
					} else {
						WritableRaster r = RasterFactory.createBandedRaster(
							DataBuffer.TYPE_BYTE, xdim, ydim, bdim, null);
						for (int b = 0; b < bdim; b++)
							for (int y = 0; y < ydim; y++)
								for (int x = 0; x < xdim; x++)
									r.setSample(x, y, b, work.getPixelXYBByte(x, y, b));
						BogusColorSpace cs = new BogusColorSpace(bdim);
						ComponentColorModel cm = new ComponentColorModel(cs, false, false,
							1, DataBuffer.TYPE_BYTE);
						BufferedImage img = new BufferedImage(cm, r, false, null);
						IIOImage image = new IIOImage(img, null, writer
							.getDefaultStreamMetadata(tiffWriteParam));
						writer.writeToSequence(image, tiffWriteParam);
					}
				}
			// for (int b = 0; b < bdim; b++)
			// for (int t = 0; t < tdim; t++)
			// for (int z = 0; z < zdim; z++) {
			// SampleModel s = RasterFactory.createBandedSampleModel(
			// DataBuffer.TYPE_BYTE, xdim, ydim, 1);
			// BufferedImage img = ImageSave.imageToBufferedImage(input
			// .getImage2D(z, t, b), s, BufferedImage.TYPE_BYTE_GRAY);
			// IIOImage image = new IIOImage(img, null, writer
			// .getDefaultStreamMetadata(tiffWriteParam));
			// writer.writeToSequence(image, tiffWriteParam);
			// }
			writer.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}