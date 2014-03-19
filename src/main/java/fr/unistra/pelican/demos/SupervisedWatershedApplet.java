package fr.unistra.pelican.demos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.jai.RasterFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.arithmetic.AdditionChecked;
import fr.unistra.pelican.algorithms.io.ImageBuilder;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedMultiProbashed;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


/**
 * 
 */
public class SupervisedWatershedApplet extends JApplet implements
		ActionListener {

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	private JButton openFile;

	private JFileChooser chooserSave = new JFileChooser();

	private JFileChooser chooserLoad = new JFileChooser();

	private String fileName;

	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	public void init() {

		// Set the load button
		setLayout(new BorderLayout());
		openFile = new JButton("Load");
		add(openFile, BorderLayout.NORTH);
		openFile.addActionListener(this);
		openFile.setPreferredSize(new Dimension(60,20));
		setSize(200, 200);

	}

	public URL getURL(String filename) {
		URL codebase = this.getCodeBase();
		URL url = null;

		try {
			url = new URL(codebase, filename);
		} catch (MalformedURLException e) {
			System.err.println("MalformedURLException");
			return null;
		}
		return url;
	}

	public void actionPerformed(ActionEvent arg0) {

		int responce = chooserLoad.showOpenDialog(this);
		if (responce == JFileChooser.APPROVE_OPTION) {
			fileName = "" + chooserLoad.getSelectedFile();
		}
		
		// Get the image
		System.out.println("le chemin seul: " + fileName);
		System.out.println("le chemin avec le codebase: " + this.getCodeBase() + fileName);
		//ImageIcon icon = new ImageIcon(getURL(fileName))
		ImageIcon icon = new ImageIcon(fileName);
		java.awt.Image img = icon.getImage();

		// create BufferedImage
		BufferedImage bimg = new BufferedImage(img.getWidth(null), img
				.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics g = bimg.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();

		Image input = bufferedImageToImage(bimg);

		Image markers = ImageBuilder.exec(input, "SupervisedWatersedApplet");
		Image samples = LabelsToBinaryMasks.exec(markers);
		long t1 = System.currentTimeMillis();
		Image result = MarkerBasedMultiProbashed.exec(input, samples);
		long t2 = System.currentTimeMillis();
		System.out.println("Supervised segmentation : " + (t2 - t1) / 1000.0
				+ " s");

		Image view1 = LabelsToColorByMeanValue.exec((IntegerImage)result, input);
		Viewer2D.exec(view1, "SupervisedWatersedDemo: regions");

		Image frontiers = BinaryDilation.exec(FrontiersFromSegmentation
				.exec(result), FlatStructuringElement2D
				.createSquareFlatStructuringElement(3));

		BooleanImage frontiers2 = FrontiersFromSegmentation
				.exec(convertToIntegerImage(markers));
		frontiers2 = (BooleanImage) OR.exec(frontiers, frontiers2);
		Image view3 = DrawFrontiersOnImage.exec(input, frontiers2);
		Viewer2D.exec(view3,
				"SupervisedWatersedDemo: frontiers with markers");

	}

	private static IntegerImage convertToIntegerImage(Image img) {
		IntegerImage res = new IntegerImage(img.getXDim(), img.getYDim(), img
				.getZDim(), img.getTDim(), img.getBDim());
		for (int p = 0; p < img.size(); p++)
			res.setPixelInt(p, img.getPixelByte(p));
		return res;
	}

	private static ByteImage convertToByteImage(Image img) {
		ByteImage res = new ByteImage(img.getXDim(), img.getYDim(), img
				.getZDim(), img.getTDim(), img.getBDim());
		for (int p = 0; p < img.size(); p++)
			res.setPixelByte(p, img.getPixelInt(p));
		return res;
	}

	public BufferedImage imageToBufferedImage(Image img) {
		byte[] pixels = new byte[img.size()];

		BufferedImage bimg = null;

		for (int i = 0; i < img.size(); i++)
			pixels[i] = (byte) img.getPixelByte(i);

		DataBufferByte dbb = new DataBufferByte(pixels, img.size());

		SampleModel s = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_BYTE, img.getXDim(), img.getYDim(), 1);
		Raster r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
		bimg = new BufferedImage(img.getXDim(), img.getYDim(),
				BufferedImage.TYPE_BYTE_GRAY);
		bimg.setData(r);

		return bimg;
	}

	public Image bufferedImageToImage(BufferedImage im) {

		Image output;
		// raster gets the value of each pixel from im
		Raster raster = im.getData();
		// Save the type of im
		int type = im.getType();
		// Save the height of im
		int height = raster.getHeight();
		// Save the width of im
		int width = raster.getWidth();
		// Set the number of band to 1, we are looking for a greyscale image
		// without alpha channel
		int band = 3;

		// Instanciates output with the correct width, height and number of band
		output = new ByteImage(width, height, 1, 1, band);

		// Transfers each byte from raster to output
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				for (int b = 0; b < band; b++)
					output.setPixelXYBByte(i, j, b, (byte) raster.getSample(i,
							j, b));

		// Set the color paramater
		output.setColor(true);
		// set the type parameter
		output.type = type;

		return output;
	}
}