package fr.unistra.pelican.algorithms.frequential;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Complex2dArray;
import fr.unistra.pelican.util.ComplexNumber;

/**
 * 2D recursive inverse fast fourier transform...
 * 
 * !!! Image dimensions must be power of 2 (dimx=2^k, dimy=2^k')
 * 
 * Input is composed of two images representing real and respectively imaginary part of transform.
 * Third input parameter allow to choose type of result (real part, imaginary part, or magnitudes)
 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#REAL
 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#IMAG
 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#MAGN
 * 
 * Result is in double precision
 * @deprecated use FFT2 faster better stronger
 * @author ?, Benjamin Perret (depracater)
 */
public class InverseFFT extends Algorithm {
	/**
	 * Real part of input image in frequency domain
	 */
	public Image inputImageReal;

	/**
	 * Imaginary part of input image in frequency domain
	 */
	public Image inputImageImag;

	/**
	 * Result of iFFT
	 */
	public Image outputImage;

	/**
	 * Wanted part of the output
	 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#REAL
	 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#IMAG
	 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#MAGN
	 */
	public int outputType;

	/**
	 * We want real part of inverse transform
	 */
	public static final int REAL = 0;

	/**
	 * We want imaginary part of inverse transform
	 */
	public static final int IMAG = 1;

	/**
	 * We want magnitude of inverse transform
	 */
	public static final int MAGN = 2;

	/**
	 * Constructor
	 * 
	 */
	public InverseFFT() {

		super();
		super.inputs = "inputImageReal,inputImageImag,outputType";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		int xDim = inputImageReal.getXDim();
		int yDim = inputImageReal.getYDim();

		double[][] pixelsR2 = new double[inputImageReal.getXDim()][inputImageReal
				.getYDim()];
		double[][] pixelsI2 = new double[inputImageReal.getXDim()][inputImageReal
				.getYDim()];

		for (int x = 0; x < inputImageReal.getXDim(); x++) {
			for (int y = 0; y < inputImageReal.getYDim(); y++) {
				pixelsR2[x][y] = inputImageReal.getPixelXYDouble(x, y);
				pixelsI2[x][y] = inputImageImag.getPixelXYDouble(x, y);
			}
		}

		pixelsR2 = shiftOrigin(pixelsR2);
		pixelsI2 = shiftOrigin(pixelsI2);

		double[] pixelsR = new double[inputImageReal.size()];
		double[] pixelsI = new double[inputImageReal.size()];

		for (int x = 0; x < inputImageReal.getXDim(); x++) {
			for (int y = 0; y < inputImageReal.getYDim(); y++) {
				pixelsR[y * inputImageReal.getXDim() + x] = pixelsR2[x][y];
				pixelsI[y * inputImageReal.getXDim() + x] = pixelsI2[x][y];
			}
		}

		Complex2dArray input = new Complex2dArray(pixelsR, pixelsI, xDim, yDim);
		Complex2dArray intermediate = new Complex2dArray(pixelsR, xDim, yDim);
		Complex2dArray output = new Complex2dArray(pixelsR, xDim, yDim);

		for (int i = 0; i < input.size; ++i)
			intermediate.putColumn(i, recIFFT(input.getColumn(i)));

		for (int i = 0; i < intermediate.size; ++i)
			output.putRow(i, recIFFT(intermediate.getRow(i)));

		// prepare output according to predefined type
		outputImage = new DoubleImage(inputImageReal, false);

		// constant factor
		double c = outputImage.xdim * outputImage.ydim; 
		
		switch (outputType) {
		case REAL:
			double[][] reals = output.getReals();
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					outputImage.setPixelXYDouble(x, y, c*reals[x][y]);
			break;
		case IMAG:
			double[][] imags = output.getImags();
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					outputImage.setPixelXYDouble(x, y, c*imags[x][y]);
			break;
		default:
			double[][] mags = output.getMagnitudes();
			for (int x = 0; x < xDim; x++)
				for (int y = 0; y < yDim; y++)
					outputImage.setPixelXYDouble(x, y, c*mags[x][y]);

		}
	}

	private double[][] shiftOrigin(double[][] input) {
		int width = input.length;
		int height = input[0].length;

		double[][] output = new double[width][height];
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
		return output;
	}

	private ComplexNumber[] recIFFT(ComplexNumber[] x) {
		ComplexNumber z1, z2, z3, z4, tmp, cTwo;
		int n = x.length;
		int m = n / 2;

		ComplexNumber[] result = new ComplexNumber[n];
		ComplexNumber[] even = new ComplexNumber[m];
		ComplexNumber[] odd = new ComplexNumber[m];
		ComplexNumber[] sum = new ComplexNumber[m];
		ComplexNumber[] diff = new ComplexNumber[m];

		cTwo = new ComplexNumber(2, 0);

		if (n == 1)
			result[0] = x[0];
		else {
			z1 = new ComplexNumber(0.0, 2 * (Math.PI) / n);
			tmp = ComplexNumber.cExp(z1);
			z1 = new ComplexNumber(1.0, 0.0);

			for (int i = 0; i < m; i++) {
				z3 = ComplexNumber.cSum(x[i], x[i + m]);
				sum[i] = ComplexNumber.cDiv(z3, cTwo);

				z3 = ComplexNumber.cDiff(x[i], x[i + m]);
				z4 = ComplexNumber.cMult(z3, z1);
				diff[i] = ComplexNumber.cDiv(z4, cTwo);

				z2 = ComplexNumber.cMult(z1, tmp);
				z1 = new ComplexNumber(z2);
			}

			even = recIFFT(sum);
			odd = recIFFT(diff);

			for (int i = 0; i < m; i++) {
				result[i * 2] = new ComplexNumber(even[i]);
				result[i * 2 + 1] = new ComplexNumber(odd[i]);
			}
		}
		return result;
	}
	
	/**
	 * 2D recursive inverse fast fourier transform...
	 * 
	 * Input is composed of two images representing real and respectively imaginary part of transform.
	 * Third input parameter allow to choose type of result (real part, imaginary part, or magnitudes)
	 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#REAL
	 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#IMAG
	 * @see fr.unistra.pelican.algorithms.frequential.InverseFFT#MAGN
	 * 
	 * Result is in double precision
	 *  
	 * @param inputImageReal Real part of image in frequency domain.
	 * @param inputImageImag Imaginary part of image in frequency domain.
	 * @param outputType Type of output.
	 * @return Inverse transform
	 */
	public static DoubleImage exec(Image inputImageReal, Image inputImageImag, int outputType)
	{
		return (DoubleImage ) new InverseFFT().process(inputImageReal,inputImageImag,outputType);
	}
}
