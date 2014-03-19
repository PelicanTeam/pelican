package fr.unistra.pelican.util.multiscale;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Pyramidal multiscale representation of an image
 * 
 * @author lefevre
 *
 * TODO: generalize the class to consider not only 2x2 average factor
 *
 */
public class Pyramid {

	Image base;
	Image images[];
	int depth;
	
	/**
	 * Constructor
	 * @param input base image
	 * @param size pyramid depth
	 * @param computeData pyramid build
	 */
	public Pyramid(Image input, int size, boolean computeData)
	{
		if (size<1) return;
		while (input.getXDim()<Math.pow(2,size))
			size--;
		while (input.getYDim()<Math.pow(2,size))
			size--;
		this.depth=size;
		images=new Image[depth];
		base=input.copyImage(true);
		build(computeData);
	}
	
	/**
	 * Constructor by copy
	 * @param p the pyramid to be copied
	 */
	public Pyramid(Pyramid p) {
		this(p,true);
	}
	
	/**
	 * Constructor by copy
	 * @param p the pyramid to be copied
	 * @param copyData to indicate if data are also copied
	 */
	public Pyramid(Pyramid p, boolean copyData) {
		images=p.images.clone();
		depth=p.depth;
		base=images[0];
		if (!copyData)
			for (int d=0;d<depth;d++)
				images[d].fill(0);
	}
	
	/**
	 * return the bottom of the pyramid (i.e. the base image)
	 * @return the image at the bottom of the pyramid
	 */
	public Image getBottom() {
		return images[0];
	}
	
	/**
	 * return the top of the pyramid
	 * @return the image at the top of the pyramid
	 */
	public Image getTop() {
		return images[depth-1];
	}
	
	/**
	 * get an image at a given scale
	 * 
	 * @param d the given scale (should be positive and less or equal to depth)
	 * @return the image at the selected scale
	 */
	public Image getScale(int d) {
		if (d>=0 && d<depth)
			return images[d];
		else
			return null;
	}
	
	/**
	 * set an image at a given scale
	 * 
	 * @param image the input image (should have same dimensions as the pyramid internal image
	 * @param d the selected scale (should be positive and less or equal to depth)
	 */
	public void setScale(Image image, int d) {
		if (d<0 || d>=depth)
			return;
		if (Image.haveSameDimensions(image,images[d])==false)
			return;
		images[d]=image.copyImage(true);
	}	
	
	
	/**
	 * return the depth of the pyramid
	 * 
	 * @return the depth of the pyramid
	 */
	public int getDepth() {
		return depth;
	}
	
	/**
	 * build the pyramid by setting the appropriate values
	 * double values are used during the process
	 * implementation is dedicated to 2x2 multiscale factor and average method
	 * 
	 * @param computeData to indicate if data are to be computed or not (initialised to 0)
	 */
	public void build(boolean computeData) {
		int xdim,ydim;
		int k=1;
		double val;
		if (base==null) return;
		images[0]=base;
		for (int d=1;d<depth;d++) {
			// compute the 2-D size of the image
			k*=2;
			xdim=base.getXDim()/k;
			ydim=base.getYDim()/k;
			// check the nature of the image
			if (base instanceof BooleanImage)
				images[d]=new BooleanImage(xdim,ydim,base.getZDim(),base.getTDim(),base.getBDim());
			else if (base instanceof ByteImage)
				images[d]=new ByteImage(xdim,ydim,base.getZDim(),base.getTDim(),base.getBDim());
			else if (base instanceof IntegerImage)
				images[d]=new IntegerImage(xdim,ydim,base.getZDim(),base.getTDim(),base.getBDim());
			else if (base instanceof DoubleImage)
				images[d]=new DoubleImage(xdim,ydim,base.getZDim(),base.getTDim(),base.getBDim());
			images[d].fill(0);
			images[d].copyAttributes(base);
			if(computeData)
				// Compute the values at current level
				for (int b=0;b<base.getBDim();b++)
					for (int t=0;t<base.getTDim();t++)
						for (int z=0;z<base.getZDim();z++)
							for (int x=0;x<images[d].getXDim();x++)
								for (int y=0;y<images[d].getYDim();y++) {
									val=images[d-1].getPixelXYZTBDouble(x*2,y*2,z,t,b);
									val+=images[d-1].getPixelXYZTBDouble(x*2+1,y*2,z,t,b);
									val+=images[d-1].getPixelXYZTBDouble(x*2,y*2+1,z,t,b);
									val+=images[d-1].getPixelXYZTBDouble(x*2+1,y*2+1,z,t,b);
									images[d].setPixelXYZTBDouble(x,y,z,t,b,val/4);
									}
			}
		}
	
	/**
	 * 
	 * convert a pyramid into an Image
	 * 
	 * note : the Z dimension is currently used to display the different scales
	 * 
	 * @return the returned image
	 */
	public Image convertToImage() {
		// image creation
		Image output=base.newInstance(base.getXDim(),base.getYDim(),depth,base.getTDim(),base.getBDim());
		// base image computation
		output.setImage4D(base,0,Image.Z);
		// other image computation
		int k=1;
		for (int d=1;d<depth;d++) {
			k*=2;
			output.setImage4D(this.extractImage(d),d,Image.Z);
		}
		output.copyAttributes(base);
		return output;
	}

	/**
	 * 
	 * extract an image representation of a given scale of the pyramid
	 * 
	 * note : size of the image is similar to the original (base) scale
	 * 
	 * @param d the selected scale
	 * @return the image represented at the selected scale
	 */
	public Image extractImage(int d) {
		Image output=base.copyImage(false);
		// image creation
		int k=(int)Math.pow(2,d);
		for(int x=0;x<k*images[d].getXDim();x++)
			for (int y=0;y<k*images[d].getYDim();y++)
				for(int z=0;z<base.getZDim();z++)
					for (int t=0;t<base.getTDim();t++)
						for(int b=0;b<base.getBDim();b++)
							output.setPixelDouble(x,y,z,t,b,images[d].getPixelDouble(x/k,y/k,z,t,b));
		return output;
	}

	/**
	 * @param args
	 * @throws PelicanException
	 */
	public static void main (String args[]) throws PelicanException {
		Image image=(Image) new ImageLoader().process("samples/lenna.png");
		Pyramid pyramid=new Pyramid(image,12,true);
		Image output=pyramid.convertToImage();
		new Viewer2D().process(output,"multiscale");
	}
	
	
}
