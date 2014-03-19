/**
 * 
 */
package fr.unistra.pelican.algorithms.spatial;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.morphology.GrayStructuringElement;

/**
 * Fast2DConvolutionWithSVD provide a fast 2D convolution algorithm 
 * based on a Singular Value Decomposition of kernel image.
 * 
 * This class is optimized to perform lots of convolution with the same kernel.
 * 
 * Warning: to perform a real convolution you must reflect your kernel manually before calling this algorithm.
 * 
 * Rank of kernel matrix determines the time taken by this Algorithm.
 * If kernel matrix is full rank Fast2DConvolutionWithSVD 
 * will provide no enhancement in performances.
 * 
 * Complexity: 
 * 	-size of input image: n²
 * 	-size of input kernel: m²
 * 	-rank of input kernel: r
 * => standard 2D convolution: n²*m²
 * => Fast2DConvolutionWithSVD: r*m*n²
 * 
 * 
 * For the moment this class only support square kernel with odd dimensions. 
 * 
 * Border of the image are replicated to allow image dimension conservation.
 * 
 * Because computation of SVD takes time it is done only once when constructing the Fast2DConvolutionWithSVD.
 * Then use method convolve(Image) to convolve an image.
 * 
 * Because Fast2DConvolutionWithSVD uses multiple buffers, 
 * you should call the method convolve with images of same dimensions, 
 * so Fast2DConvolutionWithSVD does not have to reallocate buffers at each call.
 * 
 * @author Benjamin Perret
 *
 */
public class Fast2DConvolutionWithSVD extends Algorithm {

	/**
	 * Input kernel
	 */
	public DoubleImage kernel;
	
	
	/**
	 * Hack: synonym for this, necessary for Algorithm class compliance
	 */
	public Fast2DConvolutionWithSVD me;
	
	/**
	 * Rank of input kernel
	 */
	private int rank;
	
	/**
	 * SVD
	 */
	private Matrix u,s,v;
	
	
	private int cx;
	
	private int cy;
	
	private Image tmp[];
	
	private Image inputImage;
	
	private Image outputImage;
	
	public Fast2DConvolutionWithSVD()
	{
		super.inputs="kernel";
		super.outputs="me";
	}
	
		
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if (kernel.getXDim() != kernel.getYDim())
			throw new PelicanException("Fast Concolve with SVD works only with square kernel! ");
		if (kernel.getXDim()%2 ==0  || kernel.getYDim()%2==0)
			throw new PelicanException("Please use kernel with odd dimensions! ");
		
		cx=kernel.getXDim()/2;
		cy=kernel.getYDim()/2;
		
		Matrix m=new Matrix(kernel.getPixels(),kernel.getXDim());
		SingularValueDecomposition svd = new SingularValueDecomposition(m);
		rank=svd.rank();
		s=svd.getS();
		u=svd.getU();
		v=svd.getV().transpose();
		if (rank==kernel.getYDim())
			System.err.println("Warning: using SVDConvolution with a kernel of maximum rank, try another optimisation methode!");
		//System.out.println("SVD rank :" + rank);
		tmp=new DoubleImage[rank];
		me=this;
	}

	/**
	 * Compute convolution of input image with prepared kernel
	 * @param im Image to convolve
	 * @return Convolved image
	 */
	public Image convolve(Image im)
	{
		if (outputImage==null || !Image.haveSameDimensions(outputImage, im))
		{
			outputImage= new DoubleImage(im.getXDim(),im.getYDim(),1,1,1);//im.copyImage(false);
			for(int i=0;i<rank;i++)
			{
				tmp[i]=new DoubleImage(im.getXDim(),im.getYDim(),1,1,1);//im.copyImage(false);
			}
		}
		inputImage=im;
		calculTmp();
		calcul2Etape();
		
		
		return outputImage;
	}
	
	private void calculTmp()
	{
		for(int o=0;o<rank;o++)
		{
			for (int j=0;j<outputImage.getYDim();j++)
				for (int i=0;i<outputImage.getXDim();i++)
				{
					double temp=0.0;
					for (int k=0;k<kernel.getXDim();k++)
					{
						int x=i+k-cx;
						if(x<0)
							temp+=inputImage.getPixelXYDouble(0, j)*u.get(k,o);
						else if (x>=inputImage.getXDim())
							temp+=inputImage.getPixelXYDouble(inputImage.getXDim()-1, j)*u.get(k,o);
						else
							temp+=inputImage.getPixelXYDouble(x, j)*u.get(k,o);
						
					}
					tmp[o].setPixelXYDouble(i,j,temp*s.get(o,o));
				}
						
		}
	}
	
	private void calcul2Etape()
	{
		for (int j=0;j<outputImage.getYDim();j++)
			for (int i=0;i<outputImage.getXDim();i++)
			{
				double temp=0.0;
				for(int o=0;o<rank;o++)

					for (int k=0;k<kernel.getXDim();k++)
					{
						int y=j+k-cy;
						if(y<0)
							temp+=tmp[o].getPixelXYDouble(i, 0)*v.get(o,k);
						else if(y>=inputImage.getYDim())
							temp+=tmp[o].getPixelXYDouble(i, inputImage.getYDim()-1)*v.get(o,k);
						else
							temp+=tmp[o].getPixelXYDouble(i, y)*v.get(o,k);
					}
				outputImage.setPixelXYDouble(i,j,temp);
			}	
	}
	
	
	/**
	 * Get a prepared  Fast2DConvolutionWithSVD with a given kernel.
	 * Then use method convolve() to perform convolution.
	 * @param se Kernel
	 * @return prepared  Fast2DConvolutionWithSVD
	 */
	public static Fast2DConvolutionWithSVD exec(GrayStructuringElement se)
	{
		return (Fast2DConvolutionWithSVD)new Fast2DConvolutionWithSVD().process(se);
	}
	
	/*
	public static void main(String [] args)
	{
		try {
			GaussianPSF psf=new GaussianPSF(4.1);
			GrayStructuringElement se=psf.generatePSF();
			GrayStructuringElement se2 = se.getReflection();
			Date t1=new Date();
			Fast2DConvolutionWithSVD svd = Fast2DConvolutionWithSVD.exec(se2);
			Date t2=new Date();
			long d0=t2.getTime()-t1.getTime();
			Image test=ImageLoader.exec("samples/lennaGray256.png");
			test.setColor(false);
			Image res1=Convolution2.exec(test,se);
			Image res2=svd.convolve(test);
			Viewer2D.exec(test,"Original image");
			Viewer2D.exec(res1,"Convolution with class Convolution2");
			Viewer2D.exec(res2,"Convolution with class SVDForConvolution");
			t1=new Date();
			int nbIter=50;
			for (int i=0;i<nbIter;i++)
				Convolution2.exec(test,se);
			t2=new Date();
			long d1=t2.getTime()-t1.getTime();
			
			t1=new Date();
			
			for (int i=0;i<nbIter;i++)
				svd.convolve(test);
			t2=new Date();
			long d2=t2.getTime()-t1.getTime();
			System.out.println("Convolution of a test image, dimensions: " + test.getXDim() + "*" + test.getYDim() + " pixels");
			System.out.println("Size of convolution kernel: " + se.getXDim() + "*" + se.getYDim() + " pixels");
			System.out.println("Rank of convolution kernel: 1, full central symetry");
			System.out.println("Number of iteration: " + nbIter);
			System.out.println("Time taken by Convolution2 (2d convolution no optimization):");
			System.out.println("\t" + d1 +" ms");
			System.out.println("Time taken by SVDForConvolution:");
			System.out.println("\t" + d2 +" ms");
			System.out.println("Time taken to evaluate Singular Value Decomposition (only once):");
			System.out.println("\t" + d0 +" ms");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
	
}
