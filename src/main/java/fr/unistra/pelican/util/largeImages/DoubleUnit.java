package fr.unistra.pelican.util.largeImages;

import java.io.Serializable;
import java.util.Arrays;

import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.util.Tools;

/**
 * Units used in LargeDoubleImage
 */
public class DoubleUnit extends Unit implements Serializable {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = 796697037611825746L;

	/**
	 * Array of pixels
	 */
	private double[] pixels;

	/**
	 * Constructs a new double unit with the given size.
	 * 
	 * @param unitSize
	 *            number of pixel in the unit
	 */
	public DoubleUnit(int unitSize) {
		super();
		this.pixels = new double[unitSize];
	}

	/**
	 * Constructs a new double unit with the given size and all its pixels
	 * equals to the given value
	 * 
	 * @param unitSize
	 *            number of pixel in the unit
	 * @param value
	 *            value of all pixels
	 */
	public DoubleUnit(int unitSize, double value) {
		super();
		this.pixels = new double[unitSize];
		Arrays.fill(this.pixels, value);
	}

	/**
	 * Gets the value of the pixel at the given index.
	 * 
	 * @param loc
	 *            index of the pixel
	 * @return
	 */
	public double getPixel(int loc) {
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
	public void setPixel(int loc, double value) {
		this.setModified();
		this.pixels[loc] = value;
	}

	/**
	 * Sets all pixels of this unit.
	 * 
	 * @param newPixels
	 *            Array with the new values for the pixels
	 */
	public void setPixels(double[] newPixels) {
		if (newPixels.length != this.pixels.length) {
			throw new PelicanException("DoubleArray does not fit with the unit");
		} else {
			this.pixels = newPixels;
			this.setModified();
		}
	}

	@Override
	public DoubleUnit clone() {
		//LargeImageMemoryManager.getInstance().checkMemory();
		DoubleUnit result = new DoubleUnit(this.pixels.length);
		result.setPixels(this.pixels.clone());
		return result;
	}
	
	/**
	 * @see fr.unistra.pelican.DoubleImage#maximum()
	 * @return the maximum as a double
	 */
	public double maximum(){
		double val = Double.NEGATIVE_INFINITY;
		if(this.end==null){
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
	 * @see fr.unistra.pelican.DoubleImage#minimum()
	 * @return the minimum as a double
	 */
	public double minimum(){
		double val = Double.MAX_VALUE;
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
	 * @see fr.unistra.pelican.DoubleImage#maximum(int)
	 * @return the maximum as a double
	 */
	public double maximum(int band){
		double val = Double.NEGATIVE_INFINITY;
		int debut = this.checkForBandWork(band);
		if(this.end==null){
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
	 * @see fr.unistra.pelican.DoubleImage#minimum(int)
	 * @return the minimum as a double
	 */
	public double minimum(int band){
		double val = Double.MAX_VALUE;
		int debut = this.checkForBandWork(band);
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
	
	/**
	 * @see fr.unistra.pelican.DoubleImage#maximumIgnoreNonRealValues(int)
	 * @return the maximum as a double
	 */
	public double maximumDoubleIgnoreNonRealValues(int band){
		double val = Double.NEGATIVE_INFINITY;
		int debut = this.checkForBandWork(band);
		if(this.end==null){
			double v;
			for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim()){
				v=pixels[p];
				if (Tools.isValue(v) && v > val)
					val = v;
			}
		}else{
			double v;
			for (int p = debut; p < this.end; p+=this.parentImage.getBDim()){
				v=pixels[p];
				if (Tools.isValue(v) && v > val)
					val = v;
			}
		}
		return val;
	}
	
	/**
	 * @see fr.unistra.pelican.DoubleImage#minimumIgnoreNonRealValues(int)
	 * @return the minimum as a double
	 */
	public double minimumDoubleIgnoreNonRealValues(int band){
		double val = Double.MAX_VALUE;
		int debut = this.checkForBandWork(band);
		if(this.end==null){
			double v;
			for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim()){
				v=pixels[p];
				if (Tools.isValue(v) && v < val )
					val = v;
			}
		}else{
			double v;
			for (int p = debut; p < this.end; p+=this.parentImage.getBDim()){
				v=pixels[p];
				if (Tools.isValue(v) && v < val)
				val = v;
			}
		}
		return val;
	}
	
	public int defaultSize(){
		return this.pixels.length;
	}

	/**
	 * Fill the specified band.
	 * @param band
	 * 			Band to proceed.
	 * @param b
	 * 			New value for the pixels.
	 */
	public void fill(int band, double b) {
		int debut = this.checkForBandWork(band);
		for (int p = debut; p < this.pixels.length; p+=this.parentImage.getBDim()){
			pixels[p]=b;
		}
	}

	/**
	 * Compute the number of different pixels between this unit and the one given in argument.
	 * @param anUnit
	 * 			Unit to compare.
	 * @return
	 * 			The number of different pixels.
	 */
	public double nbDifferentPixels(DoubleUnit anUnit) {
		if (this.size()!=anUnit.size()){
			throw new PelicanException("can not compute nbDifferentPixels on two units with different size");
		}
		double res = 0d;
		for (int i=0;i<this.size();i++){
			if(this.getPixel(i)!=anUnit.getPixel(i)){
				res++;
			}
		}
		return res;
	}
	
	public boolean equals(Unit u){
		if (u==null||!(u instanceof DoubleUnit)){
			return false;
		}
		if(this.size()!=u.size()){
			return false;
		}
		for(int i =0;i<this.size();i++){
			if (this.getPixel(i)!=((DoubleUnit)u).getPixel(i)){
				return false;
			}
		}
		return true;
	}
}
