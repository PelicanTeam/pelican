package fr.unistra.pelican.algorithms.io;

import fr.unistra.pelican.*;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

/**
 * Load TIFF images.
 * 
 * @author Lefevre
 */

public class TiffMultiplePageImageLoad extends Algorithm {

	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public TiffMultiplePageImageLoad() {
		super.inputs = "filename";
		super.outputs = "output";
	}

	/**
	 * Loads Tiff images.
	 * 
	 * @param filename
	 *          Filename of the Tiff image.
	 * @return The Tiff image.
	 */
	public static Image exec(String filename) {
		return (Image) new TiffMultiplePageImageLoad().process(filename);
	}

	public void launch() {
		try {
			ImageDecoder dec = ImageCodec.createImageDecoder("tiff",
				new FileSeekableStream(filename), null);
			int pages = dec.getNumPages();
			System.err.println("Number of images in this TIFF: " + pages);
			FileImageInputStream inputStream = new FileImageInputStream(new File(
				filename));
			ImageReader reader = ImageIO.getImageReadersBySuffix("tif").next();
			reader.setInput(inputStream);
			output = null;
			for (int imageToLoad = 0; imageToLoad < dec.getNumPages(); imageToLoad++) {
				RenderedImage tiff = reader.readAsRenderedImage(imageToLoad, null);
				tiff.getData().getTransferType();
				int width = tiff.getWidth();
				int height = tiff.getHeight();
				Raster r = tiff.getData();
				int band = r.getNumBands();
				Image page = null;
				switch (tiff.getColorModel().getPixelSize()) {
				case 32:
					if (output == null)
						output = new DoubleImage(width, height, pages, 1, band);
					page = new DoubleImage(width, height, 1, 1, band);
					for (int i = 0; i < width; i++)
						for (int j = 0; j < height; j++)
							for (int b = 0; b < band; b++)
								page.setPixelXYBDouble(i, j, b, r.getSampleDouble(i, j, b));
					output.setImage4D(page, imageToLoad, Image.Z);
					break;
				case 16:
					if (output == null)
						output = new IntegerImage(width, height, pages, 1, band);
					page = new IntegerImage(width, height, 1, 1, band);
					for (int i = 0; i < width; i++)
						for (int j = 0; j < height; j++)
							for (int b = 0; b < band; b++)
								page.setPixelXYBInt(i, j, b, r.getSample(i, j, b));
					output.setImage4D(page, imageToLoad, Image.Z);
					break;
				default:
					if (output == null)
						output = new ByteImage(width, height, pages, 1, band);
					page = new ByteImage(width, height, 1, 1, band);
					for (int i = 0; i < width; i++)
						for (int j = 0; j < height; j++)
							for (int b = 0; b < band; b++)
								page.setPixelXYBByte(i, j, b, (byte) r.getSample(i, j, b));
					output.setImage4D(page, imageToLoad, Image.Z);
					break;
				}
				if (band == 3)
					output.setColor(true);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}