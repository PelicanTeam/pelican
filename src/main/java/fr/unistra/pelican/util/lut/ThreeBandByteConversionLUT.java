package fr.unistra.pelican.util.lut;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;

public class ThreeBandByteConversionLUT implements Serializable
{

	private static final long serialVersionUID = 200911131007L;
	/**
	 * LUT for band 0
	 */
	public byte[][][] lut0= new byte[256][256][256];
	/**
	 * LUT for band 1
	 */
	public byte[][][] lut1= new byte[256][256][256];
	/**
	 * LUT for band 2
	 */
	public byte[][][] lut2= new byte[256][256][256];

	/**
	 * Constructor
	 */
	public ThreeBandByteConversionLUT()	{}
	
	/**
	 * Create the LUT for converting RGB image to Lab Image
	 * @return LUT RGBToLab
	 */
	public static ThreeBandByteConversionLUT getRGBToLabLUT()
	{
		ThreeBandByteConversionLUT tbclut = new ThreeBandByteConversionLUT();
		double[][][] xyz0 = new double[256][256][256];
		double[][][] xyz1 = new double[256][256][256];
		double[][][] xyz2 = new double[256][256][256];
		
		//RGBToXYZ
		for(int r=0;r<256;r++)
			for(int g=0;g<256;g++)
				for(int b=0;b<256;b++)
				{
					double rN = r * 0.003921;
					double gN = g * 0.003921;
					double bN = b * 0.003921;
					xyz0[r][g][b]=0.412453 * rN + 0.357580 * gN + 0.180423 * bN;
					xyz1[r][g][b]=0.212671 * rN + 0.715160 * gN + 0.072169 * bN;
					xyz2[r][g][b]=0.019334 * rN + 0.119193 * gN + 0.950227 * bN;
				}
		//XYZToLab
		double Xn = 0.950456;
		double Yn = 1.0;
		double Zn = 1.088754;
		for(int r=0;r<256;r++)
			for(int g=0;g<256;g++)
				for(int b=0;b<256;b++)
				{
					double Xfrac = xyz0[r][g][b] / Xn;
					double Yfrac = xyz1[r][g][b] / Yn;
					double Zfrac = xyz2[r][g][b] / Zn;

					if (Xfrac > 0.008856)
						Xfrac = Math.pow(Xfrac, 0.333333);
					else
						Xfrac = 7.787 * Xfrac + 16.0 / 116.0;

					if (Yfrac > 0.008856)
						Yfrac = Math.pow(Yfrac, 0.333333);
					else
						Yfrac = 7.787 * Yfrac + 16.0 / 116.0;

					if (Zfrac > 0.008856)
						Zfrac = Math.pow(Zfrac, 0.333333);
					else
						Zfrac = 7.787 * Zfrac + 16.0 / 116.0;

					

					tbclut.lut0[r][g][b] = Image.unsignedByteToSignedByte((int)Math.round((116 * Yfrac - 16.0)*2.55));
					tbclut.lut1[r][g][b] = Image.unsignedByteToSignedByte((int)Math.round(500 * (Xfrac - Yfrac))+128);
					tbclut.lut2[r][g][b] = Image.unsignedByteToSignedByte((int)Math.round(200 * (Yfrac - Zfrac))+128);
				}
		return tbclut;
	}
	
	/**
	 * Create the LUT for converting RGB image to LSH Image
	 * @return LUT RGBToLSH
	 */
	public static ThreeBandByteConversionLUT getRGBToLSHLUT()
	{
		ThreeBandByteConversionLUT tbclut = new ThreeBandByteConversionLUT();
		for(int R=0;R<256;R++)
			for(int G=0;G<256;G++)
				for(int B=0;B<256;B++)
				{	
					double r = Image.unsignedByteToDouble(R);
					double g = Image.unsignedByteToDouble(G);
					double b = Image.unsignedByteToDouble(B);					
					double[] lsh = new double[3];
					lsh[0] = lsh[1] = lsh[2] = 0.0;
					double max = 0.0, med = 0.0, min = 0.0;
		
					if( r >= g && r >= b ) 
					{ 
						max = r;
						if ( g >= b ) 
						{ 
							med = g;
							min = b;
						} else { 
							med = b;
							min = g;
						}
					} else if( g >= r && g >= b ) {
						max = g;
						if( r >= b ) 
						{ 
							med = r;
							min = b;
						} else 
						{ 
							med = b;
							min = r;
						}
					} else if( b >= r && b >= g ) 
					{ 
						max = b;
						if( r >= g ) 
						{ 
							med = r;
							min = g;
						} else 
						{ 
							med = g;
							min = r;
						}
					} // fi

					// luminance
					lsh[0] = ( max + med + min ) / 3.0;

					// saturation
					if( lsh[0] >= med ) 
						lsh[1] = 1.5 * ( max - lsh[0] );
					else lsh[1] = 1.5 * ( lsh[0] - min );

					// hue
					double k = 1.0/6.0;
					int lambda = 0;
					if( r > g  && g >= b ) lambda = 0;
					else if( g >= r && r > b  ) lambda = 1;
					else if( g > b  && b >= r ) lambda = 2;
					else if( b >= g && g > r  ) lambda = 3;
					else if( b > r  && r >= g ) lambda = 4;
					else if( r >= b && b > g  ) lambda = 5;

					if ( lsh[1] > 0.0 ) 
						lsh[2] = k * ( lambda + 0.5 - Math.pow(-1,lambda) * ( max+min-2*med ) / ( 2*lsh[1] ) );
					else lsh[2] = 0;

					if( lsh[2] < 0 ) lsh[2] = 0;
					tbclut.lut0[R][G][B] = Image.doubleToSignedByte(lsh[0]);
					tbclut.lut1[R][G][B] = Image.doubleToSignedByte(lsh[1]);
					tbclut.lut2[R][G][B] = Image.doubleToSignedByte(lsh[2]);
				}
		return tbclut;
	}
	
	/**
	 * Create the LUT for converting RGB image to HSV Image
	 * @return LUT RGBToLab
	 */
	public static ThreeBandByteConversionLUT getRGBToHSVLUT()
	{
		ThreeBandByteConversionLUT tbclut = new ThreeBandByteConversionLUT();
		for(int R=0;R<256;R++)
			for(int G=0;G<256;G++)
				for(int B=0;B<256;B++)
				{	
					double rN = R * 0.003921;
					double gN = G * 0.003921;
					double bN = B * 0.003921;

					double H, S, V;

					double min = rN;
					if (gN < min)
						min = gN;
					if (bN < min)
						min = bN;

					double max = rN;
					if (gN > max)
						max = gN;
					if (bN > max)
						max = bN;

					S = H = 0.0;
					V = max;

					double delta = max - min;

					if (max != 0 && delta != 0.0) {
						S = delta / max;

						if (rN == max)
							H = 60 * (gN - bN) / delta;

						else if (gN == max)
							H = 60 * (bN - rN) / delta + 120;

						else
							H = 60 * (rN - gN) / delta + 240; // bN == max

							if (H < 0.0)
								H += 360;
							if (H > 360)
								H -= 360;

							H = H / 360.0;
					}
					tbclut.lut0[R][G][B] = Image.doubleToSignedByte(H);
					tbclut.lut1[R][G][B] = Image.doubleToSignedByte(S);
					tbclut.lut2[R][G][B] = Image.doubleToSignedByte(V);
				}
		return tbclut;
	}
	
	/**
	 * Create the LUT for converting RGB image to HSL Image
	 * @return LUT RGBToLab
	 */
	public static ThreeBandByteConversionLUT getRGBToHSLLUT()
	{
		ThreeBandByteConversionLUT tbclut = new ThreeBandByteConversionLUT();
		for(int R=0;R<256;R++)
			for(int G=0;G<256;G++)
				for(int B=0;B<256;B++)
				{	
					// normalise to [0,1]
					double rN = R * 0.003921;
					double gN = G * 0.003921;
					double bN = B * 0.003921;

					double H, S, L;

					H = S = L = 0.0;

					double min = rN;
					if (gN < min)
						min = gN;
					if (bN < min)
						min = bN;

					double max = rN;
					if (gN > max)
						max = gN;
					if (bN > max)
						max = bN;

					double delta = max - min;

					L = (max + min) * 0.5;

					if (delta >= 0.0 && delta <= 0.0) {
						H = S = 0.0;
					} else {
						if (L < 0.5)
							S = delta / (max + min);
						else
							S = delta / (2 - max - min);
					}

					double _R = (((max - rN) / 6.0) + delta * 0.5) / delta;
					double _G = (((max - gN) / 6.0) + delta * 0.5) / delta;
					double _B = (((max - bN) / 6.0) + delta * 0.5) / delta;

					if (rN == max)
						H = _B - _G;
					else if (gN == max)
						H = 1.0 / 3.0 + _R - _B;
					else if (bN == max)
						H = 2.0 / 3.0 + _G - _R;

					if (H < 0.0)
						H += 1.0;
					if (H > 1.0)
						H -= 1.0;				
					
					tbclut.lut0[R][G][B] = Image.doubleToSignedByte(H);
					tbclut.lut1[R][G][B] = Image.doubleToSignedByte(S);
					tbclut.lut2[R][G][B] = Image.doubleToSignedByte(L);
				}
		return tbclut;
	}
	
	/**
	 * Save the LUT on the specified filename
	 * @param filename
	 */
	public void save(String filename)
	{
		try {
			ObjectOutputStream f = null;
			f = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filename)));
			
			f.writeObject(this);
			f.close();			
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			throw new AlgorithmException("file writing error with file: " + filename);
		}
	}
	
	/**
	 * Load a LUT from a specified filename
	 * @param filename
	 * @return LUT
	 */
	public static ThreeBandByteConversionLUT load(String filename)
	{
		ThreeBandByteConversionLUT lut=null;
		try {
			ObjectInputStream f = null;
				f = new ObjectInputStream(new GZIPInputStream(new FileInputStream(
					filename)));
			
				lut = (ThreeBandByteConversionLUT) f.readObject();
			f.close();
		} catch (IOException ex) {
			throw new AlgorithmException("file reading error with file: " + filename,ex);
		} catch (ClassNotFoundException ex) {
			throw new AlgorithmException("file reading error with file: " + filename,ex);
		}
		return lut;
	}
	
}
