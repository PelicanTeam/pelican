package fr.unistra.pelican.util;

import fr.unistra.pelican.Image;

/**
 * Create a view on only one band of another image. If you change a pixel in the view
 * instance, the base image is changed too, they share the same data.
 * 
 * TODO: The copyImage is clearly unoptimal.
 * @author derivaux
 */
public class ImageWrapperBandSelector extends Image {
	
	private static final long serialVersionUID = -5087997422384216729L;
	
	
	private Image 	baseImage;
	private int 	numBand;
	private int 	offset;
	
	public ImageWrapperBandSelector(Image baseImage, int numBand){
		super(baseImage.getXDim(), baseImage.getYDim(), baseImage.getZDim(), baseImage.getTDim(), 1);
		this.baseImage = baseImage;
		this.numBand = numBand;
		this.offset = numBand; // Yes it seems.
	}

	@Override
	public Image copyImage(boolean copyData) {
		Image _baseImage = baseImage.copyImage(copyData);
		return new ImageWrapperBandSelector(_baseImage, numBand);
	}

	@Override
	public Image newInstance(int xdim,int ydim,int zdim,int tdim,int bdim) {
		Image _baseImage = baseImage.newInstance(xdim,ydim,zdim,tdim,bdim);
		return new ImageWrapperBandSelector(_baseImage, numBand);		
	}

	
	@Override
	public double getPixelDouble(int loc) {
		return baseImage.getPixelDouble(loc * baseImage.getBDim() + numBand);
	}

	@Override
	public int getPixelInt(int loc) {
		return baseImage.getPixelInt(loc * baseImage.getBDim()  + numBand);
	}

	@Override
	public int getPixelByte(int loc) {
		return baseImage.getPixelByte(loc * baseImage.getBDim()  + numBand);
	}

	@Override
	public boolean getPixelBoolean(int loc) {
		return baseImage.getPixelBoolean(loc * baseImage.getBDim()  + numBand);
	}

	@Override
	public void setPixelDouble(int loc, double value) {
		baseImage.setPixelDouble(loc * baseImage.getBDim()  + offset, value);
	}

	@Override
	public void setPixelInt(int loc, int value) {
		baseImage.setPixelInt(loc * baseImage.getBDim()  + offset, value);
	}

	@Override
	public void setPixelByte(int loc, int value) {
		baseImage.setPixelByte(loc * baseImage.getBDim()  + offset, value);
	}

	@Override
	public void setPixelBoolean(int loc, boolean value) {
		baseImage.setPixelBoolean(loc * baseImage.getBDim()  + offset, value);
	}

	@Override
	public int size() {
		return baseImage.getXDim()*baseImage.getYDim()*baseImage.getZDim()*baseImage.getTDim();
	}

	@Override
	public boolean equals(Image im) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPixel(Image input, int x1, int y1, int z1, int t1, int b1,
		int x2, int y2, int z2, int t2, int b2) {
		baseImage.setPixel(input, x1, y1, z1, t1, b1, x2, y2, z2, t2, b2);		
	}

}
