package fr.unistra.pelican.algorithms.io;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.PelicanException;

/**
 * General loading of an image. Add a hook in this algorithm when you write a
 * specific image loader.
 * 
 * @author ?, Jonathan Weber
 */

public class ImageLoader extends Algorithm {

	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * Output parameter
	 */
	public Image outputImage;
	
	/**
	 * Optional flag to perform normalization
	 */
	public boolean normalize=false;

	/**
	 * Constructor
	 * 
	 */
	public ImageLoader() {
		super.inputs = "filename";
		super.outputs = "outputImage";
		super.options="normalize";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		try {
			if ((filename.endsWith("tif") || filename.endsWith("tiff")
				|| filename.endsWith("TIF") || filename.endsWith("TIFF"))
				&& (ImageCodec.createImageDecoder("tiff",
					new FileSeekableStream(filename), null).getNumPages() > 1)) {
				System.err.println("Multiple-page TIFF file... specific processing");
				try {
					outputImage = TiffMultiplePageImageLoad.exec(filename);
				} catch (PelicanException ex) {
					ex.printStackTrace();
				}
				outputImage.type = Image.RAW;
				return;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (filename.endsWith("fits")) {
			try {
				DoubleImage d = (DoubleImage) LoadFitsWithExtensions.exec(filename);
				if (normalize)
				 d = d.scaleToZeroOne();
				outputImage = (Image) d;
			} catch (PelicanException ex) {
				throw new AlgorithmException("Image Loader, fits image load failed",ex);
			}
			outputImage.type = Image.RAW;
			return;
		}
		
		if (filename.endsWith("seg")){
			outputImage = BerkeleySegmentationImageLoad.exec(filename);
		}
		
		if (filename.endsWith("osf")){
			outputImage = OdessaSegmentationLoad.exec(filename);
		}
		
		if (filename.endsWith("pdf")){
			outputImage = PDFImageLoad.exec(filename);
			return;
		}
		
		if (filename.endsWith("pix") || filename.endsWith("txt")) {
			try {
				int xdim=100,ydim=100;
				String str=null;
				while(str==null)
				{

					str=JOptionPane.showInputDialog("Reading ascii file, please indicate x dimension : ", 100);
					if(str==null)
						throw new AlgorithmException("File reading interupted by user!");
					try{
						xdim=Integer.parseInt(str);
						if(xdim<=0)
						{
							str=null;
							JOptionPane.showMessageDialog(null, "Dimension must be strictly positive!");
						}
					} catch (NumberFormatException e)
					{
						str=null;
						JOptionPane.showMessageDialog(null, "Please enter a valid integer number!");
					}
					
				}
				str=null;
				while(str==null)
				{

					str=JOptionPane.showInputDialog("Reading ascii file, please indicate y dimension : ", xdim);
					if(str==null)
						throw new AlgorithmException("File reading interupted by user!");
					try{
						ydim=Integer.parseInt(str);
						if(ydim<=0)
						{
							str=null;
							JOptionPane.showMessageDialog(null, "Dimension must be strictly positive!");
						}
					} catch (NumberFormatException e)
					{
						str=null;
						JOptionPane.showMessageDialog(null, "Please enter a valid integer number!");
					}
					
				}
				DoubleImage d = (DoubleImage) IRAFTextImageLoad.exec(filename,xdim,ydim);
				if (normalize)
				 d = d.scaleToZeroOne();
				outputImage = (Image) d;
			} catch (PelicanException ex) {
				ex.printStackTrace();
			}
			outputImage.type = Image.RAW;
			return;
		}

		
		if (filename.endsWith("h5")) {
			try {
				outputImage = HdfImageLoad.exec(filename);
			} catch (PelicanException ex) {
				ex.printStackTrace();
			}
			outputImage.type = Image.RAW;
			return;
		}

		if (filename.endsWith("hdr")) {
			try {
				// ByteImage d = (ByteImage)HdrImageLoad.process(filename);
				// outputImage = (Image)d;
				outputImage = HdrImageLoad.exec(filename);
			} catch (PelicanException ex) {
				throw new AlgorithmException();
				// ex.printStackTrace();
			}

			outputImage.type = Image.RAW;

			return;
		}
		
		if (filename.endsWith("mhd")) {
			try {
				outputImage = MhdImageLoader.exec(filename);
			} catch (PelicanException ex) {
				throw new AlgorithmException();
				// ex.printStackTrace();
			}

			outputImage.type = Image.RAW;

			return;
		}

		if (filename.endsWith("pelican") || filename.endsWith("pel")
			|| filename.endsWith("plc")) {
			try {
				outputImage = (Image) new PelicanImageLoad().process(filename);
			} catch (PelicanException ex) {
				ex.printStackTrace();
			}
			outputImage.type = Image.RAW;
			return;
		}
		
		if (filename.endsWith("mpg") || filename.endsWith("MPG")
				|| filename.endsWith("avi")|| filename.endsWith("AVI")
				|| filename.endsWith("mov")|| filename.endsWith("MOV")
				|| filename.endsWith("mp4")|| filename.endsWith("MP4")
				|| filename.endsWith("flv")|| filename.endsWith("FLV")) {
				try {
					outputImage = (Image) new VideoLoader().process(filename);
				} catch (PelicanException ex) {
					ex.printStackTrace();
				}
				outputImage.type = Image.RAW;
				return;
			}

		// fall back to Java Imaging I/O and IOTools in order to load standard 8
		// bit images : BMP, GIF, JPEG, PNG, WBMP + TIFF, JPEG-2000, etc
		File f = new File(filename);
		if (!f.canRead())
			throw new InvalidParameterException("PELICAN cannot read : " + filename);
		BufferedImage im;
		try {
			im = ImageIO.read(f);
		} catch (IOException e) {
			throw new InvalidParameterException(
				"PELICAN cannot load (IOException) : " + filename,e);
		}
		if (im == null) {
			System.err.println("PELICAN tried to use : ");
			Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(filename.substring(
				filename.lastIndexOf('.') + 1, filename.length()));
			while (iter.hasNext())
				System.out.println(iter.next());
			throw new InvalidParameterException(
				"PELICAN cannot load (ImageIO error) : " + filename);
		}
		outputImage = convertFromJAI(im,normalize);

		// collect and recycle the garbage
		System.gc();
	}

	public static Image convertFromJAI(BufferedImage im, boolean normalize) {
		Image outputImage=null;
		// Process the JAI image
		int type = im.getType();
		WritableRaster r = im.getRaster();
		int height = r.getHeight();
		int width = r.getWidth();
		int band = r.getNumBands();
		// System.out.println(im.getColorModel());
		// System.out.println(im.getSampleModel());

		// Images binaires
		if (type == BufferedImage.TYPE_BYTE_BINARY) {
			BooleanImage imageTBB = new BooleanImage(width, height, 1, 1, band);
			int loc=0;
			for (int j = 0; j < height; j++)
				for (int i = 0; i < width; i++)
					for (int b = 0; b < band; b++)
					{
						imageTBB.setPixelBoolean(loc++, r.getSample(i, j, b) != 0);						
					}
			imageTBB.setColor(false);
			outputImage = imageTBB;
		}
		// Images 8 bits
		else if (im.getColorModel().getPixelSize() == 8 * band) {
			ByteImage image8b = new ByteImage(width, height, 1, 1, band);
			int loc=0;
			for (int j = 0; j < height; j++)
				for (int i = 0; i < width; i++)
					for (int b = 0; b < band; b++)
					{
						image8b.setPixelByte(loc++, (byte) r.getSample(i, j, b));
					}
			outputImage = image8b;
		}
		// Images 16 bits // BufferedImage.TYPE_USHORT_GRAY
		else if (im.getColorModel().getPixelSize() == 16 * band) {
			IntegerImage image16b = new IntegerImage(width, height, 1, 1, band);
			int loc=0;
			for (int j = 0; j < height; j++)
				for (int i = 0; i < width; i++)
					for (int b = 0; b < band; b++)
					{
						image16b.setPixelInt(loc++, r.getSample(i, j, b));
					}
			outputImage = image16b;
			if (normalize)
				outputImage=image16b.convertToByteImage();
		}
		// Images de plus de 16 bits : 32 bits ? BufferedImage.TYPE_CUSTOM		
		else {
			DoubleImage image1632b = new DoubleImage(width, height, 1, 1, band);
			int loc=0;
			for (int j = 0; j < height; j++)
				for (int i = 0; i < width; i++)
					for (int b = 0; b < band; b++)
					{
						image1632b.setPixelDouble(loc++, r.getSampleDouble(i, j, b));
					}
			outputImage = image1632b;
			if (normalize)
				outputImage=image1632b.scaleToZeroOne();
		}
		outputImage.type = type;
		outputImage.setColor(band == 3);
		return outputImage;
	}

	/**
	 * General loading of an image.
	 * 
	 * @param image
	 *          Image to be loaded.
	 * @return GUI containing the image.
	 */
	public static Image exec(String filename) {
		return (Image) new ImageLoader().process(filename);
	}

	public static Image exec(String filename,boolean normalize) {
		return (Image) new ImageLoader().process(filename,normalize);
	}

}
