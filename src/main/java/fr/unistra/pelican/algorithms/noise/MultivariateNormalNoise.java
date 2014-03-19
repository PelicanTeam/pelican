package fr.unistra.pelican.algorithms.noise;

import java.util.Random;

import Jama.CholeskyDecomposition;
import Jama.Matrix;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;


/**
 * Add multivariate normal distributed noise to a multi bands image.
 * 
 * First input is the original image with 'b' bands.
 * Second input is the mean vector of size b.
 * Third input is the covariance matrix (size b*b) (must be symmetric and positive definite)
 * 
 * Fourth input is optional: if set to true values are kept in image natural limits (e.g. [0;1] for DoubleImage).
 * 
 * Output is of same type as input image.
 * 
 * Works on double precision.
 * 
 * @author Benjamin Perret
 *
 */
public class MultivariateNormalNoise extends Algorithm {

	
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Noisy result
	 */
	public Image outputImage;

	
	/**
	 * mean value of noise per channel
	 */
	public double [] mean;

	/**
	 * Correlation matrix for noise
	 */
	public double [][] corr;
	
	/**
	 * do we force value to be between [0;1] ?
	 */
	public boolean safe=false;

	/**
	 * Constructor
	 * 
	 */
	public MultivariateNormalNoise() {
		super();
		super.inputs = "inputImage,mean,corr";
		super.options = "safe";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		
		if (corr.length ==0 || corr.length != corr[0].length)
			throw new AlgorithmException(
				"Correlation matrix must be square");
		
		if (inputImage.getBDim() != corr.length)
			throw new AlgorithmException(
					"Dimensions of correlation matrix and number of bands in the image do not match.");
		
		if (inputImage.getBDim() != mean.length)
			throw new AlgorithmException(
					"Dimensions of mean vector and number of bands in the image do not match.");
		/**
		 * For a symmetric, positive definite matrix A, the Cholesky decomposition is an lower triangular matrix L so that A = L*L'.
		 */
		Matrix m = new Matrix(corr);
		CholeskyDecomposition cd = new CholeskyDecomposition(m);
		if (!cd.isSPD())
			throw new AlgorithmException(
			"Correlation matrix must be symmetric and positive definite");
		Matrix a=cd.getL();
		
		
		
		outputImage = inputImage.copyImage(false);

		Random rand = new Random();

		
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();
		int tDim = inputImage.getTDim();
		int zDim = inputImage.getZDim();
		int bDim = inputImage.getBDim();
		double [] noise = new double[bDim];
		        
		for (int t = 0; t < tDim; t++)
			for (int z = 0; z < zDim; z++)
				for (int x = 0; x < xDim; x++)
					for (int y = 0; y < yDim; y++) {
						double[] d = inputImage.getVectorPixelXYZTDouble(x, y,z, t);
						for(int i=0 ; i<bDim ; i++)
							noise[i]=rand.nextGaussian();
						Matrix n=new Matrix(noise,bDim);
						Matrix r=a.times(n);
						double [] nc = r.getColumnPackedCopy();
						for(int i=0 ; i<bDim ; i++)
						{
							nc[i]+=mean[i] + d[i];
							if(safe) nc[i]=Math.min(1.0, Math.max(0.0,nc[i]));
						}
						
						outputImage.setVectorPixelXYZTDouble(x, y, z, t, nc);
						
					}
		
	}

	/**
	 * Add multivariate normal distributed noise to a multi bands image.
	 * Works on double precision.
	 * 
	 * @param inputImage original image with 'b' bands.
	 * @param mean mean vector of size b.
	 * @param corr covariance matrix (size b*b) (must be symmetric and positive definite)
	 * @return Noisy image (same type as img1)
	 */
	public static Image exec(Image inputImage, double [] mean, double [][] corr)
	{
		return (Image) new MultivariateNormalNoise().process(inputImage,mean,corr);
	}
	
	/**
	 * Add multivariate normal distributed noise to a multi bands image.
	 * Works on double precision.
	 * 
	 * @param inputImage original image with 'b' bands.
	 * @param mean mean vector of size b.
	 * @param corr covariance matrix (size b*b) (must be symmetric and positive definite)
	 * @param safe If true pixels values are forced to stay in image natural bounds
	 * @return Noisy image (same type as img1)
	 */
	public static Image exec(Image inputImage, double [] mean, double [][] corr,boolean safe)
	{
		return (Image) new MultivariateNormalNoise().process(inputImage,mean,corr,safe);
	}
	
	/**
	 * Add multivariate normal distributed noise to a multi bands image.
	 * 
	 * Noise will have same mean and deviation in each band.
	 * Correlation between each bands will be the same.
	 * Works on double precision.
	 * 
	 * @param inputImage original image.
	 * @param mean mean value of noise.
	 * @param dev standard deviation of noise.
	 * @param corr band correlation.
	 * @param safe If true pixels values are forced to stay in image natural bounds.
	 * @return Noisy image (same type as img1)
	 */
	public static Image exec(Image inputImage, double  mean, double dev,double corr, boolean safe)
	{
		if (corr>1 || corr<-1 )
			throw new AlgorithmException(
			"Correlation must be in [-1;1]");
		
		if (dev<=0 )
			throw new AlgorithmException(
			"Deviation must be strictly positive");
		
		int b=inputImage.getBDim();
		double [] means= new double[b];
		double [][] cov = new double[b][b];
		
		double v=dev*dev;
		double c=v*corr;
		
		for(int i =0 ; i < b ; i++)
		{
			means[i]=mean;
			cov[i][i]=v;
		}
		
		for(int i =0 ; i < b ; i++)
			for(int j =i+1 ; j < b ; j++)
			{
				cov[i][j]=c;
				cov[j][i]=c;
			}
				
		return (Image) new MultivariateNormalNoise().process(inputImage,means,cov,safe);
	}
	
	/*
	public static void main(String [] args)
	{
		Image im = new DoubleImage(ImageLoader.exec("samples/curious.png"),true);
		im.setColor(true);
		
		double []  means = {0.5,0.5,0.5};
		
		double var=0.5;
		double corel=1.0*0.2*0.2;
		double corr1[][] = {{var,0.0,0.0} ,{0.0,var,0.0} ,{0.0,0.0,var}};
		double corr2[][] = {{var,corel,corel} ,{corel,var,corel} ,{corel,corel,var}};
		double corr3[][] = {{var,-corel,-corel} ,{-corel,var,-corel} ,{-corel,-corel,var}};
		
		Viewer2D.exec(((DoubleImage)(MultivariateNormalNoise.exec(im,means,corr1))).scaleToZeroOne(),"No correlation");
		Viewer2D.exec(((DoubleImage)(MultivariateNormalNoise.exec(im,means,corr2))).scaleToZeroOne(),"Fully correlated noise ");
		Viewer2D.exec(((DoubleImage)(MultivariateNormalNoise.exec(im,means,corr3))).scaleToZeroOne(),"Fully anti-correlated noise");
	}*/

}
