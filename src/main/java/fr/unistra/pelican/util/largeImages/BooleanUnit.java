package fr.unistra.pelican.util.largeImages;

import java.io.Serializable;
import java.util.Arrays;

import fr.unistra.pelican.PelicanException;

/**
 * Units used in LargeBooleanImage
 */
public class BooleanUnit extends Unit implements Serializable {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = 7818151845300596076L;

	/**
	 * Array of pixels
	 */
	private boolean[] pixels;

	/**
	 * Constructs a new byte unit with the given size.
	 * 
	 * @param unitSize
	 *            number of pixel in the unit
	 */
	public BooleanUnit(int unitSize) {
		super();
		this.pixels = new boolean[unitSize];
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
	public BooleanUnit(int unitSize, boolean value) {
		super();
		this.pixels = new boolean[unitSize];
		Arrays.fill(this.pixels, value);
	}

	/**
	 * Gets the value of the pixel at the given index.
	 * 
	 * @param loc
	 *            index of the pixel
	 * @return
	 */
	public boolean getPixel(int loc) {
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
	public void setPixel(int loc, boolean value) {
		this.setModified();
		this.pixels[loc] = value;
	}

	/**
	 * Sets all pixels of this unit.
	 * 
	 * @param newPixels
	 *            Array with the new values for the pixels
	 */
	public void setPixels(boolean[] newPixels) {
		if (newPixels.length != this.pixels.length) {
			throw new PelicanException(
					"BooleanArray does not fit with the unit");
		} else {
			this.pixels = newPixels;
			this.setModified();
		}
	}

	@Override
	public BooleanUnit clone() {
		//LargeImageMemoryManager.getInstance().checkMemory();
		BooleanUnit result = new BooleanUnit(this.pixels.length);
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
			if (this.pixels[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Computes the maximum value of the unit
	 * @return
	 * 		the boolean representation of the maximum
	 */
	public boolean maximum(){
		if (this.end==null){
			for (int p = 0; p < this.pixels.length; p++)
				if (pixels[p])
					return true;
			return false;
		}else{
			for (int p = 0; p < this.end; p++)
				if (pixels[p])
					return true;
			return false;
		}
	}
	
	/**
	 * Computes the minimum value of the unit
	 * @return
	 * 		the boolean representation of the minimum
	 */
	public boolean minimum(){
		if (this.end==null){
			for (int p = 0; p < this.pixels.length; p++)
				if (!pixels[p])
					return false;
			return true;
		}else{
			for (int p = 0; p < this.end; p++)
				if (!pixels[p])
					return false;
			return true;
		}
	}
	
	/**
	 * Computes the maximum value of the unit in the selected band
	 * @param band
	 * 			Band.
	 * @return
	 * 		the boolean representation of the maximum in the selected band
	 */
	public boolean maximum(int band){
		int debut = this.checkForBandWork(band);
		if (this.end==null){
			for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim())
				if (pixels[p])
					return true;
			return false;
		}else{
			for (int p = debut; p < this.end; p+=this.parentImage.getBDim())
				if (pixels[p])
					return true;
			return false;
		}
	}

	/**
	 * Computes the minimum value of the unit in the selected band
	 * @param band
	 * 			Band.
	 * @return
	 * 		the boolean representation of the minimum in the selected band
	 */
	public boolean minimum(int band){		
		int debut = this.checkForBandWork(band);
		if (this.end==null){
			for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim())
				if (!pixels[p])
					return false;
			return true;
		}else{
			for (int p = debut; p < this.end; p+=this.parentImage.getBDim())
				if (!pixels[p])
					return false;
			return true;
		}
	}
	
	@Override
	public int defaultSize(){
		return this.pixels.length;
	}
	
	@Override
	public boolean equals(Unit u){
		if (u==null || !(u instanceof BooleanUnit)){
			return false;
		}
		if(this.size()!=u.size()){
			return false;
		}
		for(int i =0;i<this.size();i++){
			if (this.getPixel(i)!=((BooleanUnit)u).getPixel(i)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Compute the complement Unit.
	 * @see fr.unistra.pelican.BooleanImage#getComplement()
	 * 
	 * @return
	 * 		The complement booleanUnit.
	 */
	public BooleanUnit getComplement() {
		BooleanUnit res = new BooleanUnit(this.defaultSize());
		for (int i = 0; i < size(); i++) {
			res.setPixel(i, !this.getPixel(i));
		}
		return res;
	}
}
