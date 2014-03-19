package fr.unistra.pelican.demos;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.conversion.HSIToRGB;
import fr.unistra.pelican.algorithms.conversion.HSVToRGB;
import fr.unistra.pelican.algorithms.conversion.LUVToXYZ;
import fr.unistra.pelican.algorithms.conversion.RGBToHSI;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.algorithms.conversion.RGBToXYZ;
import fr.unistra.pelican.algorithms.conversion.RGBToYIQ;
import fr.unistra.pelican.algorithms.conversion.XYZToLAB;
import fr.unistra.pelican.algorithms.conversion.XYZToLUV;
import fr.unistra.pelican.algorithms.conversion.XYZToRGB;
import fr.unistra.pelican.algorithms.conversion.YIQToRGB;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class ConversionDemo
{
	public static void main(String[] args)
	{
		try{
			Image input = (Image)ImageLoader.exec("samples/remotesensing1.png");
			input.setColor(true);
			Viewer2D.exec(input,"RGB");

			Image HSI = (Image)new RGBToHSI().process(input);
			HSI.setColor(true);
			Viewer2D.exec(HSI,"RGB->HSI");
			
			Image RGB = (Image)new HSIToRGB().process(HSI);
			RGB.setColor(true);
			Viewer2D.exec(RGB,"HSI->RGB");
			
			Image HSV = (Image)new RGBToHSV().process(RGB);
			HSV.setColor(true);
			Viewer2D.exec(HSV,"RGB->HSV");
			
			RGB = (Image)new HSVToRGB().process(HSV);
			RGB.setColor(true);
			Viewer2D.exec(RGB,"HSV->RGB");
			
			Image YIQ = (Image)new RGBToYIQ().process(RGB);
			DoubleImage d = (DoubleImage)YIQ;
			d = d.scaleToZeroOne();
			d.setColor(true);
			Viewer2D.exec(d,"RGB->YIQ (scaled to [0,1])");
			
			RGB = (Image)new YIQToRGB().process(YIQ);
			RGB.setColor(true);
			Viewer2D.exec(RGB,"YIQ->RGB");
			
			Image XYZ = (Image)new RGBToXYZ().process(RGB);
			d = (DoubleImage)XYZ;
			d = d.scaleToZeroOne();
			d.setColor(true);
			Viewer2D.exec(d,"RGB->XYZ (scaled to [0,1])");
			
			Image LAB = (Image)new XYZToLAB().process(XYZ);
			d = (DoubleImage)LAB;
			d = d.scaleToZeroOne();
			d.setColor(true);
			Viewer2D.exec(d,"XYZ->LAB (scaled to [0,1])");
			
			Image LUV = (Image)new XYZToLUV().process(XYZ);
			d = (DoubleImage)LUV;
			d = d.scaleToZeroOne();
			d.setColor(true);
			Viewer2D.exec(d,"XYZ->LUV (scaled to [0,1])");
			
			XYZ = (Image)new LUVToXYZ().process(LUV);
			
			RGB = (Image)new XYZToRGB().process(XYZ);
			RGB.setColor(true);
			Viewer2D.exec(RGB,"XYZ->RGB");
			
		}catch(InvalidTypeOfParameterException ex){
			ex.printStackTrace();
		}catch(InvalidNumberOfParametersException ex){
			ex.printStackTrace();
		}catch(AlgorithmException ex){
			ex.printStackTrace();
		}
	}
} 