package fr.unistra.pelican.algorithms.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidParameterException;

/**
 * Load ROI Text Files from Envi and return a BooleanImage
 * 
 * @author Lefevre
 */

public class ROILoader extends Algorithm {
	
	/**
	 * ROI Filename
	 */
	public String filename;

	/**
	 * Output Boolean Image
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public ROILoader() {
		super.inputs = "filename";
		super.outputs = "output";
	}

	public void launch() {
		LineNumberReader br=null;
		try {
			br= new LineNumberReader (new BufferedReader (new FileReader(filename)));
		} catch (FileNotFoundException e) {
			throw new InvalidParameterException("Wrong ROI Filename :"+filename);
		}
		try {
			// Decompte du nombre de classes
			int bands=0,bands2=0;
			int xdim=0,ydim=0;
			String s=br.readLine();
			while (s!=null && s.charAt(0)==';') {
				if (s.startsWith("; Number of ROIs:"))
					bands=Integer.parseInt(s.substring(s.indexOf(':')+1).trim());
				if (s.startsWith("; File Dimension:")) {
					xdim=Integer.parseInt(s.substring(s.indexOf(':')+1,s.indexOf('x')).trim());
					ydim=Integer.parseInt(s.substring(s.indexOf('x')+1).trim());
				}
				if (s.startsWith("; ROI name:"))
					bands2++;
				s=br.readLine();
				}
			if (bands!=bands2)
			System.out.println("The number of classes seems wrong...");
			// Creation de l'image
			output=new BooleanImage(xdim,ydim,1,1,bands);
			int b=0;
			while (s!=null && b<bands) {
				if (s.trim().isEmpty())
					b++;
				else {
					int x=Integer.parseInt(s.substring(6,11).trim());
					int y=Integer.parseInt(s.substring(11).trim());
					output.setPixelXYBBoolean(x-1, y-1, b, true);
				}
				s=br.readLine();
			}
		} catch (IOException e) {
			throw new InvalidParameterException("Error reading ROI File :"+filename+" at line "+br.getLineNumber());
		}

	}
	/**
	 *  Load ROI Text Files from Envi and return a BooleanImage
	 * 
	 * @param filename ROI Filename
	 * @return Output Boolean Image
	 */
	public static Image exec(String filename) {
		return (Image) new ROILoader().process(filename);
	}
}