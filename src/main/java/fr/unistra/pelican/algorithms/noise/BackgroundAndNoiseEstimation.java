/**
 * 
 */
package fr.unistra.pelican.algorithms.noise;

import java.awt.Point;
import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;

/**
 * <p>Estimate background level and noise deviation with a sigma-clipping algorithm.
 * <br>Mean value 'm' and deviation 'sigma' of pixels are estimated iteratively. 
 * <br>At each iteration pixels with value higher than 'm + k * sigma' are masked.
 * <br>The algorithm is repeated until convergence.
 * 
 * <p>Works on double precision.
 * 
 * <p>First input is the image.
 * <p>Second input is optional: it specifies value of factor k: default is 3. 
 * <p>Third and fourth inputs are optional: they allow do define an estimation window if you don't want to work over the whole image:
 * 	<br>- Center Point: give the center of the estimation window: default value is null;
 *  <br>- Window Size: size of the estimation window: default value is 256.
 * <p>If the estimation window gets out of the image, it will be automatically truncated.
 *  
 * <p>First output argument is the map of background ( a true pixel value means background ). It is of same type as input image.
 * <p>Second output is the mean value of background at double precision.
 * <p>Third output is the standard deviation of background (noise power) at double precision.
 * 
 * <p>This class has been tested on astronomical images, other uses may be hazardous!!!
 * 
 * <p>Z dim and T dim are NOT considered
 * 
 * @author Benjamin Perret
 * 
 */
public class BackgroundAndNoiseEstimation extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image inputImage;

	
	/**
	 * Sigma factor
	 */
	public double sigma=3;
	
	/**
	 * Mean value of background
	 */
	public double mean;

	/**
	 * Variance of background
	 */
	public double var;

	/**
	 * Background map
	 */
	public Image mask;

	/**
	 * Center of estimation window
	 */
	public Point centre=null;
	
	/**
	 * Size of estimation window
	 */
	public int size=256;
	
	/**
	 * Convergence criterion
	 */
	private final double epsilon = 0.00001;

	/**
	 * Limits
	 */
	private int oX, oY, fX, fY;

	/**
	 * Constructor
	 * 
	 */
	public BackgroundAndNoiseEstimation() {

		super();
		super.inputs = "inputImage";
		super.options= "sigma,centre,size";
		super.outputs = "mask,mean,var";	
	}

	/**
	 * Calcul la moyenne et l'ecart type des valeurs sous le masque
	 * 
	 * @param im
	 *            Image
	 * @param mask
	 *            Masque de donnees
	 * @return tableau de double de dimension deux, le premier element est la
	 *         moyenne, le deuxieme l'ecart type
	 */
	private double[] computeMeanVar(Image im, Image mask) {
		double mean = 0.0;
		double mean2 = 0.0;
		int count = 0;
		for (int j = oY; j < fY; j++)
			for (int i = oX; i < fX; i++)
				if (mask.getPixelXYBoolean(i - oX, j - oY)) {
					mean += im.getPixelXYDouble(i, j);
					mean2 += Math.pow(im.getPixelXYDouble(i, j), 2.0);
					count++;
				}
		mean = mean / count;
		mean2 = mean2 / count;
		double[] res = new double[2];
		res[0] = mean;
		res[1] = Math.sqrt(mean2 - Math.pow(mean, 2.0));
		return res;
	}

	/**
	 * Masque toutes les valeurs superieur e un seuil
	 * 
	 * @param im
	 *            Image
	 * @param mask
	 *            Masque e modifier
	 * @param seuil
	 *            Seui de masquage
	 */
	private void cutMask(Image im, Image mask, double seuil) {
		for (int j = oY; j < fY; j++)
			for (int i = oX; i < fX; i++)
				if (im.getPixelXYDouble(i, j) >= seuil)
					mask.setPixelXYBoolean(i - oX, j - oY, false);
	}

	/**
	 * Ajuste les limites de la fenetre de calcul
	 * 
	 * @param centre
	 *            Centre de la fenetre
	 * @param dim
	 *            Dimension de la fenetre
	 */
	private void setLimitByCrop(Point centre, int dim) {

		int x = centre.x;
		int y = centre.y;
		int fx = centre.x + dim;
		int fy = centre.y + dim;
		if (x < 0) {
			fx -= x;
			x = 0;
		} else if (fx > inputImage.getXDim()) {
			x -= (fx - inputImage.getXDim() + 1);
			fx = inputImage.getXDim();
		}
		if (y < 0) {
			fy -= y;
			y = 0;
		} else if (fy > inputImage.getYDim()) {
			y -= (fy - inputImage.getYDim() + 1);
			fy = inputImage.getYDim();
		}
		setLimit(x, y, fx, fy);
	}

	/**
	 * Definit les limites de la fenetre de calcul
	 * 
	 * @param oX
	 *            Ordonnee du coin superieur gauche
	 * @param oY
	 *            Absice du coin superieur gauche
	 * @param fX
	 *            Ordonnee du coin inferieur droit
	 * @param fY
	 *            Absice du coin inferieur droit
	 */
	private void setLimit(int oX, int oY, int fX, int fY) {
		this.oX = oX;
		this.oY = oY;
		this.fX = fX;
		this.fY = fY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {

		if (centre != null)
			setLimitByCrop(centre, size);
		else setLimit(0, 0, inputImage.getXDim(),inputImage.getYDim() );
		
		mask = inputImage.copyImage(false);
		for(int i=0;i<mask.size();i++)
		{
			if(inputImage.isPresent(i))
				mask.setPixelBoolean(i, true);
			else mask.setPixelBoolean(i, false);
		}

		
		
		double[] stat = computeMeanVar(inputImage, mask);
		do {
			mean = stat[0];
			// Viewer2D.exec(mask,"oo");
			// System.out.println("Iter: mean=" + mean);
			cutMask(inputImage, mask, mean + sigma * stat[1]);
			stat = computeMeanVar(inputImage, mask);
		} while (Math.abs(mean - stat[0]) > epsilon);

		mean = stat[0];
		var = stat[1];

	}

	/**
	 * Static function that use this algorithm.
	 * 
	 * @param image
	 * @param se
	 * @return result
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public static double[] processSubRegion(Image image, Point centre, int dim)
			throws InvalidTypeOfParameterException, AlgorithmException,
			InvalidNumberOfParametersException {
		BackgroundAndNoiseEstimation algo = new BackgroundAndNoiseEstimation();
		ArrayList<Object> inputs = new ArrayList<Object>(3);
		inputs.add(image);
		algo.setInput(inputs);
		algo.setLimitByCrop(centre, dim);
		algo.launch();
		double [] res = new double[2];
		res[0]=(Double)algo.getOutput().get(1);
		res[1]=(Double)algo.getOutput().get(2);
		return res;
	}
/*
	public static void main(String[] args) {
		Image im = (Image) new ImageLoader()
				.process("samples/AstronomicalImagesFITS/img1-12.fits");
		
		Algorithm algo= new BackgroundAndNoiseEstimation();
		ArrayList in = new ArrayList();
		in.add(im);
		in.add(2.5);
		algo.setInput(in);
		algo.launch();
		ArrayList out=algo.getOutput();
		
		Image map=(Image)out.get(0);
		double mean = (Double)out.get(1);
		double dev = (Double)out.get(2);
		
		Viewer2D.exec((Image) new HistogramCorrection().process(im,HistogramCorrection.STRETCH_NOT_USE),"Original image");
		Viewer2D.exec(map,"Background map");
		System.out.println("Res:\nMean = " + mean + "\nDeviation = " + dev);

	}
	*/
	/**
	 * Estimate background map with a sigma-clipping algorithm.
	 * 
	 * @param img1 Input image
	 * @return Background map
	 */
	public static Image exec(Image inputImage)
	{
		return (Image) new BackgroundAndNoiseEstimation().process(inputImage);
	}
	
	
}
