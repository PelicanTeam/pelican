package fr.unistra.pelican.util.largeImages;

import java.io.Serializable;
import java.util.Arrays;

import fr.unistra.pelican.PelicanException;

/**
 * Units used in LargeByteImage.
 */
public class ByteUnit extends Unit implements Serializable {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = -5362794723641341936L;

	/**
	 * Array of pixels
	 */
	private byte[] pixels;

	/**
	 * Constructs a new byte unit with the given size.
	 * 
	 * @param unitSize
	 *            number of pixel in the unit
	 */
	public ByteUnit(int unitSize) {
		super();
		this.pixels = new byte[unitSize];
	}

	/**
	 * Constructs a new byte unit with the given size and all its pixels equals
	 * to the given value
	 * 
	 * @param unitSize
	 *            number of pixel in the unit
	 * @param value
	 *            value of all pixels
	 */
	public ByteUnit(int unitSize, byte value) {
		super();
		this.pixels = new byte[unitSize];
		Arrays.fill(this.pixels, value);
	}

	/**
	 * Gets the value of the pixel at the given index.
	 * 
	 * @param loc
	 *            index of the pixel
	 * @return
	 */
	public byte getPixel(int loc) {
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
	public void setPixel(int loc, byte value) {
		this.setModified();
		this.pixels[loc] = value;
	}

	/**
	 * Sets all pixels of this unit.
	 * 
	 * @param newPixels
	 *            Array with the new values for the pixels
	 */
	public void setPixels(byte[] newPixels) {
		if (newPixels.length != this.pixels.length) {
			throw new PelicanException("ByteArray does not fit with the unit");
		} else {
			this.pixels = newPixels;
			this.setModified();
		}
	}

	@Override
	public ByteUnit clone() {
		//LargeImageMemoryManager.getInstance().checkMemory();
		ByteUnit result = new ByteUnit(this.pixels.length);
		result.setPixels(this.pixels.clone());
		return result;
	}

	/**
	 * Checks if the unit is an empty one.
	 * 
	 * @return True if the unit is empty, false otherwise
	 */
	public boolean isEmpty() {
		for (int i = 0; i < this.pixels.length; i++) {
			if (this.pixels[i] > 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Computes the maximum of the unit.
	 * 
	 * @return
	 * 			the maximum as a byte
	 */
	public byte maximum(){
		byte val = Byte.MIN_VALUE;
		if (this.end==null){
			for (int p = 0; p < this.pixels.length; p++)
				if (this.pixels[p] > val)
					val = this.pixels[p];
		}else{
			for (int p = 0; p < this.end; p++)
				if (this.pixels[p] > val)
					val = this.pixels[p];
		}
		return val;
	}
	
	/**
	 * Computes the minimum of the unit.
	 * 
	 * @return
	 * 			the minimum of the unit.
	 */
	public byte minimum(){
		byte val = Byte.MAX_VALUE;
		if (this.end==null){
			for (int p = 0; p < this.pixels.length; p++)
				if (this.pixels[p] < val)
					val = this.pixels[p];
		}else{
			for (int p = 0; p < this.end; p++)
				if (this.pixels[p] < val)
					val = this.pixels[p];
		}
		return val;
	}
	
	/**
	 * Computes the maximum of the unit in the specified band
	 * @param band
	 * 			band to proceed
	 * @return
	 * 			the maximum value for this band as a byte
	 */
	public byte maximum(int band){
		int debut = this.checkForBandWork(band);
		byte val = Byte.MIN_VALUE;
		if (this.end==null){
			for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim()){
				if (this.pixels[p] > val){
					val = this.pixels[p];
				}
			}
		}else{
			for (int p = debut; p < this.end; p+=this.parentImage.getBDim()){
				if (this.pixels[p] > val){
					val = this.pixels[p];
				}
			}
		}
		return val;
	}
	
	/**
	 * Computes the minimum of the unit in the specified band
	 * @param band
	 * 			band to proceed
	 * @return
	 * 			the minimum value for this band as a byte
	 */
	public byte minimum(int band){
		int debut = this.checkForBandWork(band);
		byte val = Byte.MAX_VALUE;
		if (this.end==null){
			for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim())
				if (this.pixels[p] < val)
					val = this.pixels[p];
		}else{
			for (int p = debut; p < this.end; p+=this.parentImage.getBDim())
				if (this.pixels[p] < val)
					val = this.pixels[p];
		}
		return val;
	}
	
	public int defaultSize(){
		return this.pixels.length;
	}
	
	public boolean equals(Unit u){
		if (u==null || !(u instanceof ByteUnit)){
			return false;
		}
		if(this.size()!=u.size()){
			return false;
		}
		for(int i =0;i<this.size();i++){
			if (this.getPixel(i)!=((ByteUnit)u).getPixel(i)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Compute the number of different pixels.
	 * 
	 * @see fr.unistra.pelican.ByteImage#nbDifferentPixels(fr.unistra.pelican.ByteImage)
	 * @param anUnit
	 * 			unit to compare
	 * @return
	 * 			the number of different pixels
	 */
	public double nbDifferentPixels(ByteUnit anUnit) {
		double res =0d;
		for(int i=0;i<this.size();i++){
			if(this.getPixel(i)!=anUnit.getPixel(i)){
				res++;
			}
		}
		return res;
	}

	/**
	 * Computes the difference used to calculate the differenceRatio
	 * @see fr.unistra.pelican.ByteImage#differenceRatio(fr.unistra.pelican.ByteImage)
	 * @param anUnit
	 * @return
	 */
	public double differenceRatio(ByteUnit anUnit) {
		double res =0d;
		for(int i=0;i<this.size();i++){
			if(this.getPixel(i)!=anUnit.getPixel(i)){
				res+=Math.abs(this.getPixel(i) - anUnit.getPixel(i));
			}
		}		
		return res;
	}
}
