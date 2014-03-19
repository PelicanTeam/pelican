package fr.unistra.pelican.demos;

import java.io.File;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.AngleToPseudoColors;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.conversion.RGBToHSI;
import fr.unistra.pelican.algorithms.conversion.RGBToHSV;
import fr.unistra.pelican.algorithms.conversion.RGBToHSY;
import fr.unistra.pelican.algorithms.conversion.RGBToHSY2;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.io.PelicanImageLoad;
import fr.unistra.pelican.algorithms.io.PelicanImageSave;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;


public class PelicanIODemo
{
	public static void main(String[] args)
	{
			String path="samples/lenna.png";
			path="samples/coffee.jpg";
			//String path="samples/AstronomicalImagesFITS/img1-10.fits";

			Image input = (Image)ImageLoader.exec(path);
			input.setColor(true);
			Viewer2D.exec(input,"input");
			
			System.out.println("Sauvegarde en mode compressÃ© (par dÃ©faut)");
			new PelicanImageSave().process(input,"samples/zip.pelican");
			Image output = (Image) new PelicanImageLoad().process("samples/zip.pelican");
			output.setColor(true);
			Viewer2D.exec(output,"after IO operation");
			File f = new File("samples/zip.pelican");
			if (f.exists()) System.out.println("le fichier existe");
			System.out.println("taille du fichier : "+f.length()+" octets");
			if (f.delete()) System.out.println("le fichier n'existe plus");
			
			System.out.println("Sauvegarde en mode non compressÃ©");
			new PelicanImageSave().process(input,"samples/tmp.pelican",false);
			output = (Image) new PelicanImageLoad().process("samples/tmp.pelican",false);
			output.setColor(true);
			Viewer2D.exec(output,"after IO operation");
			f = new File("samples/tmp.pelican");
			if (f.exists()) System.out.println("le fichier existe");
			System.out.println("taille du fichier : "+f.length()+" octets");
			if (f.delete()) System.out.println("le fichier n'existe plus");

			/*
			Image hsl=RGBToHSV.exec(output);
			Viewer2D.exec(hsl.getImage4D(0, Image.B),"hue nb");
			ImageSave.exec(hsl.getImage4D(0, Image.B), "couleur-exemple-h.jpg");
			Viewer2D.exec(hsl.getImage4D(1, Image.B),"saturation nb");
			ImageSave.exec(hsl.getImage4D(1, Image.B), "couleur-exemple-s.jpg");
			Viewer2D.exec(hsl.getImage4D(2, Image.B),"brightness nb");
			ImageSave.exec(hsl.getImage4D(2, Image.B), "couleur-exemple-i.jpg");
			Viewer2D.exec(AngleToPseudoColors.exec(hsl.getImage4D(0, Image.B)),"hue");
			ImageSave.exec(AngleToPseudoColors.exec(hsl.getImage4D(0, Image.B)), "couleur-exemple-h2.jpg");
			Viewer2D.exec(GrayToPseudoColors.exec(hsl.getImage4D(1, Image.B)),"saturation");
			ImageSave.exec(AngleToPseudoColors.exec(hsl.getImage4D(1, Image.B)), "couleur-exemple-s2.jpg");
			Viewer2D.exec(GrayToPseudoColors.exec(hsl.getImage4D(2, Image.B)),"brightness");
			ImageSave.exec(AngleToPseudoColors.exec(hsl.getImage4D(2, Image.B)), "couleur-exemple-i2.jpg");
			*/
			
			/*
			output=ImageLoader.exec("/home/miv/lefevre/enseignements/fc/figures/couleur-exemple-h.jpg");
			ImageSave.exec(AngleToPseudoColors.exec(output), "couleur-exemple-h2.jpg");
			output=ImageLoader.exec("/home/miv/lefevre/enseignements/fc/figures/couleur-exemple-s.jpg");
			ImageSave.exec(GrayToPseudoColors.exec(output), "couleur-exemple-s2.jpg");
			output=ImageLoader.exec("/home/miv/lefevre/enseignements/fc/figures/couleur-exemple-i.jpg");
			ImageSave.exec(GrayToPseudoColors.exec(output), "couleur-exemple-i2.jpg");
			*/
			
			output=new ByteImage(256,512,1,1,1);
			for (int x=0;x<output.getXDim();x++)
				for (int y=0;y<output.getYDim();y++)
					output.setPixelXYByte(x,y,y/2);
			Viewer2D.exec(output);
			ImageSave.exec(AngleToPseudoColors.exec(output), "lut-angle.jpg");
			ImageSave.exec(GrayToPseudoColors.exec(output), "lut-gray.jpg");
			
	}
} 