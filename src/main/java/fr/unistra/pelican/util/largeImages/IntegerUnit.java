package fr.unistra.pelican.util.largeImages;

import java.io.Serializable;
import java.util.Arrays;

import fr.unistra.pelican.PelicanException;

/**
 * Units used in LargeIntegerImage.
 */
public class IntegerUnit extends Unit implements Serializable {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = 8774080508572023405L;
	/**
	 * Array of pixels
	 */
	public int[] pixels;

	/**
	 * Constructs a new integer unit with the given size.
	 * 
	 * @param unitSize
	 *            number of pixel in the unit
	 */
	public IntegerUnit(int unitSize) {
		super();
		this.pixels = new int[unitSize];
	}

	/**
	 * Constructs a new Integer unit with the given size and all its pixels
	 * equals to the given value
	 * 
	 * @param unitSize
	 *            number of pixel in the unit
	 * @param value
	 *            value of all pixels
	 */
	public IntegerUnit(int unitSize, int value) {
		super();
		this.pixels = new int[unitSize];
		Arrays.fill(this.pixels, value);
	}

	/**
	 * Gets the value of the pixel at the given index.
	 * 
	 * @param loc
	 *            index of the pixel
	 * @return
	 */
	public int getPixel(int loc) {
		return this.pixels[loc];
	}

	/**
	 * Sets a pixel to the given value.
	 * 
	 * @param loc
	 *            index of the pixel
	 * @param value
	 *            new value for the pixel
	 */
	public void setPixel(int loc, int value) {
		this.setModified();
		this.pixels[loc] = value;
	}

	/**
	 * Sets all pixels of this unit.
	 * 
	 * @param newPixels
	 *            Array with the new values for the pixels
	 */
	public void setPixels(int[] newPixels) {
		if (newPixels.length != this.pixels.length) {
			throw new PelicanException(
					"IntegerArray does not fit with the unit");
		} else {
			this.pixels = newPixels;
			this.setModified();
		}
	}

	@Override
	public IntegerUnit clone() {
		//LargeImageMemoryManager.getInstance().checkMemory();
		IntegerUnit result = new IntegerUnit(this.pixels.length);
		result.setPixels(this.pixels.clone());
		return result;
	}
	
	/**
	 * Computes the maximum value of this unit.
	 * 
	 * @return
	 * 		the maximum value as an integer.
	 */
	public int maximum(){
		int val = Integer.MIN_VALUE;
		if(this.end == null){
			for (int p = 0; p < this.pixels.length; p++)
				if (pixels[p] > val)
					val = pixels[p];
		}else{
			for (int p = 0; p < this.end; p++)
				if (pixels[p] > val)
					val = pixels[p];
		}
		return val;
	}
	
	/**
	 * Computes the minimum value of this unit.
	 * 
	 * @return
	 * 		the minimum value as an integer.
	 */
	public int minimum(){
		int val = Integer.MAX_VALUE;
		if(this.end==null){
			for (int p = 0; p < this.pixels.length; p++)
				if (pixels[p] < val)
					val = pixels[p];
		}else{
			for (int p = 0; p < this.end; p++)
				if (pixels[p] < val)
					val = pixels[p];
		}
		return val;
	}
	
	/**
	 * Computes the maximum value of this unit in the selected band
	 * @param band
	 * 			Band to proceed.
	 * @return
	 * 			The maximum as an integer.
	 */
	public int maximum(int band){
		int val = Integer.MIN_VALUE;
		int debut = this.checkForBandWork(band);
		if(this.end == null){
			for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim())
				if (pixels[p] > val)
					val = pixels[p];
		}else{
			for (int p = debut; p < this.end; p+=this.parentImage.getBDim())
				if (pixels[p] > val)
					val = pixels[p];
		}
		return val;
	}
	
	/**
	 * Computes the minimum value of this unit in the selected band
	 * @param band
	 * 			Band to proceed.
	 * @return
	 * 			The minimum as an integer.
	 */
	public int minimum(int band){
		int val = Integer.MAX_VALUE;
		int debut= this.checkForBandWork(band);
		if(this.end==null){
			for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim())
				if (pixels[p] < val)
					val = pixels[p];
		}else{
			for (int p = debut; p < this.end; p+=this.parentImage.getBDim())
				if (pixels[p] < val)
					val = pixels[p];
		}
		return val;
	}
	
	@Override
	public int defaultSize(){
		return this.pixels.length;
	}
	
	@Override
	public boolean equals(Unit u){
		if (u==null||!(u instanceof IntegerUnit)){
			return false;
		}
		if(this.size()!=u.size()){
			return false;
		}
		for(int i =0;i<this.size();i++){
			if (this.getPixel(i)!=((IntegerUnit)u).getPixel(i)){
				return false;
			}
		}
		return true;
	}
}
