/**
 * 
 */
package fr.unistra.pelican.algorithms.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.Tools;

/**
 * <p>
 * Save a 2D image (XY) in an ASCII file in the task wtextimage from IRAF manner, values are printed in lines composed of three columns in their ASCII representation.
 * So it is impossible to read back this file without knowing its dimensions... (this is surely the most idiot format i've ever seen!)
 * <p>
 * IRAF task can print a header in a fits format style, this is not supported! (so perhaps it is not the MOST idiot one, but why not setting it compulsory!,)
 * <p>
 * This useless class was written to furnish data usable with BUDDA (only god knows why they have chosen this silly format)
 * 
 * 
 * @author Benjamin Perret
 *
 */
public class IRAFTextImageSave extends Algorithm {

	/**
	 * The image to be saved
	 */
	public Image inputImage;
	
	/**
	 * The path to the destination file
	 */
	public String filename;
	
	
	/**
	 * Number of columns in  file, if you wn't do know why it is 3 then ask developers of the IRAF package called dataio
	 */
	private int columns=3;
	
	
	/**
	 * Number of chars per column (filled by blanks)
	 */
	private int columnWidth=23;
	
	public IRAFTextImageSave(){
		super.inputs="filename,inputImage";
		super.options="separator";
	}
	StringBuffer sbf=new StringBuffer(columnWidth);
	
	private String format(String s)
	{
		sbf.delete(0, columnWidth);
		int aa=columnWidth-s.length();
		
		for(int i=0;i<aa-1;i++)
			sbf.append(" ");
		sbf.append(s);
		//System.out.println("aa " + aa +"<" + sbf + ">");
		return sbf.toString();
		
	}
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if (inputImage.bdim > 1 || inputImage.tdim > 1 || inputImage.zdim > 1)
			throw new AlgorithmException("ASCIIImage save can only deal with 2 dims images, monoband!");
		PrintStream pw = null;
		try {
			OutputStream os=new FileOutputStream(filename);
			pw = new PrintStream(os,true,"ISO-8859-1");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int columnCounter=1;
		if(inputImage instanceof IntegerImage)
		{
			for(int y=0;y<inputImage.ydim;y++)
			{
				for(int x=0;x<inputImage.xdim;x++)
				{
					if(columnCounter==columns)
					{
						pw.print(format(""+inputImage.getPixelXYInt(x, y))+"\n");
						columnCounter=1;
					}
					else{
						pw.print(format(""+inputImage.getPixelXYInt(x, y)));
						columnCounter++;
					}
					
					
				}
				
			}
		}else if(inputImage instanceof ByteImage || inputImage instanceof BooleanImage)
		{
			for(int y=0;y<inputImage.ydim;y++)
			{
				for(int x=0;x<inputImage.xdim;x++)
				{
					if(columnCounter==columns)
					{
						pw.print(format(""+inputImage.getPixelXYByte(x, y))+"\n");
						columnCounter=1;
					}
					else{
						pw.print(format(""+inputImage.getPixelXYByte(x, y)));
						columnCounter++;
					}
				}
				
			}
		}else 
		{
			for(int y=0;y<inputImage.ydim;y++)
			{
				for(int x=0;x<inputImage.xdim;x++)
				{
					if(columnCounter==columns)
					{
						pw.print(format(""+inputImage.getPixelXYDouble(x, y))+"\n");
						columnCounter=1;
					}
					else{
						pw.print(format(""+inputImage.getPixelXYDouble(x, y)));
						columnCounter++;
					}
				}
				
			}
		}
		
		pw.close();
	}
	
	public static void exec(String filename, Image inputImage)
	{
		new IRAFTextImageSave().process(filename,inputImage);
	}
	
	public static void exec(String filename, Image inputImage, char separator)
	{
		new IRAFTextImageSave().process(filename,inputImage, separator);
	}

}
