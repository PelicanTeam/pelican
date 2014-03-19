package fr.unistra.pelican.util.largeImages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import fr.unistra.pelican.PelicanException;

/**
 * Concrete Units must have getPixel(int loc),setPixel(int loc, ? value) and
 * setPixels(?[] newPixels) methods
 */
public abstract class Unit {

	/**
	 * Image which contains this unit.
	 */
	public transient LargeImageInterface parentImage;

	/**
	 * id of the unit in the image.
	 */
	public transient int id;

	/**
	 * Indicates whether the unit was modified or not.
	 */
	public transient boolean modified;
	
	/**
	 * Indicates the offset concerning the bands so you can easily work on individual band.
	 */
	public transient Integer bOffset=null;
	
	/**
	 * Indicates the end of the used pixels, the whole array is used when end is null.
	 */
	public transient Integer end = null;

	/**
	 * Constructor
	 */
	public Unit() {
		LargeImageMemoryManager.getInstance().checkMemory();
		this.modified = false;
	}

	/**
	 * Sets the modification flag to true.
	 */
	public void setModified() {
		this.modified = true;
	}

	/**
	 * Sets the modification flag to the given value.
	 * 
	 * @param flag
	 *            New value for the modification flag
	 */
	public void setModified(boolean flag) {
		this.modified = flag;
	}

	/**
	 * Gets the modification flag.
	 * 
	 * @return The modification flag
	 */
	public boolean isModified() {
		return this.modified;
	}

	/**
	 * Set the index of the unit.
	 * 
	 * @param newId
	 *            index of the unit in its image
	 */
	public void setId(int newId) {
		this.id = newId;
	}

	/**
	 * Sets the parentImage to which these unit belongs.
	 * 
	 * @param img
	 *            The image to which these unit belongs.
	 */
	public void setParentImage(LargeImageInterface img) {
		this.parentImage = img;
	}

	/**
	 * Saves the unit in the file of its image if it has been modified
	 */
	public void discard() {

		if (this.isModified() && (this.parentImage != null)) {

			File currentFile = this.parentImage.getFile();
			long unitLength = this.parentImage.getUnitLength();

			try {
				RandomAccessFile randomAccess = new RandomAccessFile(
						currentFile, "rw");
				try {
					randomAccess.seek((long) this.id * unitLength);
					FileOutputStream fileOutput = new FileOutputStream(
							randomAccess.getFD());
					try {
						ObjectOutputStream objectOutput = new ObjectOutputStream(
								fileOutput);
						objectOutput.writeUnshared(this);
					} finally {
						fileOutput.close();
					}
				} finally {
					randomAccess.close();
				}
			} catch (IOException e) {
				throw new PelicanException("Unable to work in the file "
						+ currentFile.getAbsolutePath());
			}
			this.setModified(false);
		}
	}

	@Override
	public abstract Unit clone();
	
	/**
	 * Computes the bOffset to be able to work easily by band.</br>
	 * It also sets the end field when the unit is the last one in the image.
	 */
	public void computeOffsets(){
		this.bOffset = (int)(((long)this.parentImage.getUnitSize()*(long)this.id)%((long)this.parentImage.getBDim()));
		if (id == (this.parentImage.getUnitDim()-1)){
			this.end = (int)(this.parentImage.size()-((long)(id)*(long)this.parentImage.getUnitSize()));
		}
	}
	
	/**
	 * This method is used to work on one band in a LargeImageInterface.</br>
	 * Just use this kind of loops : for (int p = this.checkForBandWork(bandNumber); p < this.pixels.length; p+=this.parentImage.getBDim())
	 * @param band
	 * 			Band in which you want to work.
	 * @return
	 * 			The first index of this band in the unit. Integer.MAX_VALUE if there is no pixel corresponding at this band in the unit.
	 */
	public int checkForBandWork(int band){
		if (this.bOffset==null||this.parentImage==null){
			throw new PelicanException("Cannot use maximum(int band) on an unit which was not set on a parent Image");
		}
		int debut;
		if (this.bOffset>band){
			debut = this.parentImage.getBDim()-this.bOffset+band;
			if(debut <0){
				return Integer.MAX_VALUE;
			}
		}else{
			debut = band-bOffset;
		}
		return debut;
	}
	
	/**
	 * Gets the size of the unit (the number of pixels that are stored in this unit)
	 * @return
	 * 		the end value if one has been defined, defaultSize() otherwise.
	 */
	public int size(){
		if (end!=null){
			return end;
		}
		return defaultSize();		
	}
	
	/**
	 * Gets the default size of the unit (the size of its array).
	 * @return
	 * 		the size of the unit array
	 */
	public abstract int defaultSize();
	
	public boolean equals(Object o){
		if(o==null||!(o instanceof Unit)){
			return false;
		}
		return equals((Unit)o);
	}
	
	/**
	 * Compares with the given Unit
	 * 
	 * @param im
	 *          image to compare
	 * @return <code>true</code> if and only if the given unit has the same
	 *         pixel values as this unit
	 */
	public abstract boolean equals(Unit u);
	
}
