/**
 * 
 */
package fr.unistra.pelican.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.sun.org.apache.bcel.internal.generic.NEW;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;

/**
 * Want something more than class Name?
 * 
 * 
 * @author Benjamin Perret
 *
 */
public class PelicanImageToBufferedImage extends Algorithm {

	private static TextureHachuree texture=new TextureHachuree(10,10,4,10,Color.red,1);
	
	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Result
	 */
	public BufferedImage outputImage;
	
	
	/**
	 * Bands to use for color composition. Length must be 1 (grey scale) or 3 (color composition).
	 * Tab value must correspond to band indices.
	 */
	public int [] bands=null;
	

	private boolean colour=true;
	
	public PelicanImageToBufferedImage()
	{
		super.inputs="inputImage";
		super.options="bands";
		super.outputs="outputImage";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {

		int bdim = inputImage.getBDim();


		BufferedImage bimg = null;
		int xDim = inputImage.xdim;
		int yDim = inputImage.ydim;
		if(bands==null){
			if(bdim==3 && colour)
				bands=new int[]{0,1,2};
			else {
				bands=new int[]{0};
				colour=false;
			}
		} else {
			if(bands.length==1)
				colour=false;
			else if (bands.length != 3)
				throw new AlgorithmException("PelicanImageToBufferedImage: you must specify exactly 1 or 3 bands!");		
		}
		
		
		for(int b=0;b<bands.length;b++)
			if(bands[b]>=bdim || bands[b]<0)
				throw new AlgorithmException("PelicanImageToBufferedImage: invalid band index: " + bands[b]);
		
		if (colour==true) {
			bimg = new BufferedImage(xDim, yDim, BufferedImage.TYPE_4BYTE_ABGR);
			//Graphics2D g2 =bimg.createGraphics();
			//texture.appliquer(g2);
			//g2.fillRect(0, 0, inputImage.xdim, inputImage.ydim);
			for (int y = 0; y < yDim; y++)
				for (int x = 0; x < xDim; x++) {
					int r = inputImage.getPixelXYBByte(x, y, bands[0]);
					int g = inputImage.getPixelXYBByte(x, y, bands[1]);
					int b = inputImage.getPixelXYBByte(x, y, bands[2]);
					int alpha=(inputImage.isPresentXY(x, y))?255:10;
					int rgb = ((alpha & 0xff) << 24) |((r & 0xff) << 16) | (( g & 0xff) << 8)
							| ((b & 0xff));

					bimg.setRGB(x, y, rgb);
					
				}
		} else {
			bimg = new BufferedImage(xDim, yDim, BufferedImage.TYPE_4BYTE_ABGR);
			//for(int b=0;b<bdim;b++)
			for (int y = 0; y < yDim; y++)
				for (int x = 0; x < xDim; x++) {
					int op = (int) inputImage.getPixelXYBByte(x, y, bands[0]);
					
					int alpha=255;
					if(!inputImage.isPresentXY(x, y)){
						alpha=10;
						//op=255; 
					}
					int rgb = ((alpha & 0xff) << 24) |((op & 0xff) << 16) | (( op & 0xff) << 8)
							| ((op & 0xff));

					bimg.setRGB(x, y, rgb);
				}
		}
		
		outputImage=bimg;
	}

	public static BufferedImage exec(Image inputImage)
	{
		return (BufferedImage)(new PelicanImageToBufferedImage()).process(inputImage);
	}
	
	
	public static BufferedImage exec(Image inputImage,  int ... bands)
	{
		return (BufferedImage)(new PelicanImageToBufferedImage()).process(inputImage,bands);
	}
	
	public static void main(String [] args)
	{
		Image im = ImageLoader.exec("samples/lenna512.png");
		BooleanImage mask = new BooleanImage(im,false);
		mask.fill(true);
		for(int y=im.ydim/4;y<im.ydim/4*3;y++)
			for(int x=im.xdim/4;x<im.xdim/4*3;x++)
				mask.setPixelXYBoolean(x, y, false);
		im.pushMask(mask);
		MViewer.exec(im);
	}
	
	
}


 

class TextureHachuree extends BufferedImage
{
     private TexturePaint texture;
    
     public TextureHachuree (int largeur,int hauteur,int ecart,int biais,Color couleur,int epaisseur)
     {
          super(largeur,hauteur,BufferedImage.TYPE_INT_ARGB);
          texture=new TexturePaint(this,new Rectangle2D.Double(0.0,0.0,largeur,hauteur));
          //Dessin des hachures
          Graphics2D g=createGraphics();
          g.setColor(couleur);
          for(int i=0;i<largeur;i += ecart)
               for(int j=0;j<epaisseur;j++)
                    g.drawLine(i+j,0,i+j+biais,hauteur);
     }
     
     public void appliquer(Graphics2D g2d)
     {
          g2d.setPaint(texture);
     }
 
}
