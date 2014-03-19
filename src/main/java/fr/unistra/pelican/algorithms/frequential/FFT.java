package fr.unistra.pelican.algorithms.frequential;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Complex2dArray;
import fr.unistra.pelican.util.ComplexNumber;

/**
 * 2D recursive fast fourier transform... one channel images only..
 * 
 * !!! Image dimensions must be power of 2 (dimx=2^k, dimy=2^k')
 * 
 * Result is a array of 3 DoubleImage
 * 	- real part of transform
 *  - imaginary part of transform
 *  - magnitudes ( sqrt ( real^2 + imaginary^2 ) )
 * 
 * @deprecated use FFT2 faster better stronger
 * @author ?, Benjamin Perret (depracater)
 */
public class FFT extends Algorithm {
	
	/**
	 * Input image
	 */
	public Image inputImage;

	/**
	 * Coefficient images result
	 * First element is the real part
	 * Second element is imaginary part
	 * Third element is magnitudes ( sqrt ( real^2 + imaginary^2 ) )
	 */
	public DoubleImage [] outputs = new DoubleImage[3];
	
	/* no use ???
	public static final int REAL = 0;

	public static final int IMAG = 1;

	public static final int MAGN = 2;
	 */
	
	/**
	 * Constructor
	 * 
	 */
	public FFT() {

		super();
		super.inputs = "inputImage";
		super.outputs = "outputs";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xDim = inputImage.getXDim();
		int yDim = inputImage.getYDim();

		double[] pixels = new double[inputImage.size()];

		for (int x = 0; x < inputImage.size(); x++)
			pixels[x] = (double) inputImage.getPixelDouble(x);

		Complex2dArray input = new Complex2dArray(pixels, xDim, yDim);
		Complex2dArray intermediate = new Complex2dArray(pixels, xDim, yDim);
		Complex2dArray output = new Complex2dArray(pixels, xDim, yDim);

		for (int i = 0; i < input.size; ++i)
			intermediate.putColumn(i, recFFT(input.getColumn(i)));

		for (int i = 0; i < intermediate.size; ++i)
			output.putRow(i, recFFT(intermediate.getRow(i)));

		outputs[0] = new DoubleImage(inputImage, false);
		outputs[1] = new DoubleImage(inputImage, false);
		outputs[2] = new DoubleImage(inputImage, false);

		double[][] reals = output.getReals();// shiftOrigin(output.getReals());
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				outputs[0].setPixelXYDouble(x, y, reals[x][y]);

		double[][] imags = output.getImags();// shiftOrigin(output.getImags());
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				outputs[1].setPixelXYDouble(x, y, imags[x][y]);

		double[][] mags = output.getMagnitudes();
		for (int x = 0; x < xDim; x++)
			for (int y = 0; y < yDim; y++)
				outputs[2].setPixelXYDouble(x, y, mags[x][y]);
	}

	private static DoubleImage shiftOrigin(DoubleImage img) {
		int width = img.getXDim();
		int height = img.getYDim();

		double[][] input = new double[width][height];

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				input[i][j] = img.getPixelXYDouble(i, j);

		double[][] output = new double[width][height];
		DoubleImage out = new DoubleImage(img);

		int x = width / 2;
		int y = height / 2;
		int i2, j2;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				i2 = i + x;
				j2 = j + y;

				if (i2 >= width)
					i2 = i2 % width;
				if (j2 >= height)
					j2 = j2 % height;
				output[i][j] = input[i2][j2];
			}
		}

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				out.setPixelXYDouble(i, j, output[i][j]);

		return out;
	}

	private ComplexNumber[] recFFT(ComplexNumber[] x) {
		ComplexNumber z1, z2, z3, z4, tmp, cTwo;

		int n = x.length;

		int m = n / 2;

		ComplexNumber[] result = new ComplexNumber[n];
		ComplexNumber[] even = new ComplexNumber[m];
		ComplexNumber[] odd = new ComplexNumber[m];
		ComplexNumber[] sum = new ComplexNumber[m];
		ComplexNumber[] diff = new ComplexNumber[m];

		if (n == 1)
			result[0] = x[0];
		else {
			z1 = new ComplexNumber(0.0, -2 * (Math.PI) / n);
			tmp = ComplexNumber.cExp(z1);
			z1 = new ComplexNumber(1.0, 0.0);
			cTwo = new ComplexNumber(2.0, 0.0);

			for (int i = 0; i < m; ++i) {
				z3 = ComplexNumber.cSum(x[i], x[i + m]);
				sum[i] = ComplexNumber.cDiv(z3, cTwo);

				z3 = ComplexNumber.cDiff(x[i], x[i + m]);
				z4 = ComplexNumber.cMult(z3, z1);

				diff[i] = ComplexNumber.cDiv(z4, cTwo);

				z2 = ComplexNumber.cMult(z1, tmp);
				z1 = new ComplexNumber(z2);
			}

			even = recFFT(sum);
			odd = recFFT(diff);

			for (int i = 0; i < m; i++) {
				result[i * 2] = new ComplexNumber(even[i]);
				result[i * 2 + 1] = new ComplexNumber(odd[i]);
			}
		}

		return result;
	}
	
	/**
	 * 2D recursive fast fourier transform... one channel images only..
	 * 
	 * Result is a array of 3 DoubleImage
	 * 	- real part of transform
	 *  - imaginary part of transform
	 *  - magnitudes ( sqrt ( real^2 + imaginary^2 ) )
	 * 
	 * @param inputImage Input image
	 * @return Result of transform
	 */
	public static DoubleImage[] exec(Image inputImage)
	{
		return (DoubleImage []) new FFT().process(inputImage);
	}
}
