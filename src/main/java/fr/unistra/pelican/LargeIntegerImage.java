package fr.unistra.pelican;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import fr.unistra.pelican.util.largeImages.ByteUnit;
import fr.unistra.pelican.util.largeImages.DoubleUnit;
import fr.unistra.pelican.util.largeImages.IntegerUnit;
import fr.unistra.pelican.util.largeImages.LargeImageInterface;
import fr.unistra.pelican.util.largeImages.LargeImageMemoryManager;
import fr.unistra.pelican.util.largeImages.LargeImageUtil;
import fr.unistra.pelican.util.largeImages.Unit;

/**
 * This class extends IntegerImage to add the memory management abilities from
 * LargeImageInterface.
 * 
 * @see fr.unistra.pelican.IntegerImage
 * @see fr.unistra.pelican.util.largeImages.LargeImageInterface
 */
public class LargeIntegerImage extends IntegerImage implements
		LargeImageInterface {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = -7715039485054323729L;

	/**
	 * File where data are cached when there is not enough memory
	 */
	private transient File fichier;

	/**
	 * Stores number of pixels in the Image
	 */
	private long size;

	/**
	 * Indicates how many units are contained in the image
	 */
	private int unitDim;

	/**
	 * Indicates the length of a serialized unit
	 */
	protected long unitLength;

	/**
	 * Indicates how many pixels are contained in each unit (in power of two)
	 */
	private int unitPowerSize = -1;

	/**
	 * Hashmap which contains the soft references to the units
	 */
	private transient HashMap<Integer, Unit> unitMap;

	/**
	 * Memory id given by the LargeImageMemoryManager
	 */
	private transient int memoryId;

	/**
	 * unitId is a field in order to avoid creating it at each call of getPixel
	 * or setPixel
	 */
	private transient long unitId;

	/**
	 * unitLoc is a field in order to avoid creating it at each call of getPixel
	 * or setPixel
	 */
	private transient long unitLoc;

	/**
	 * Long version of xdim
	 */
	private long longXdim;

	/**
	 * Long version of ydim
	 */
	private long longYdim;

	/**
	 * Long version of zdim
	 */
	private long longZdim;

	/**
	 * Long version of tdim
	 */
	private long longTdim;

	/**
	 * Long version of bdim
	 */
	private long longBdim;

	/**
	 * Constructs a LargeIntegerImage.
	 */
	public LargeIntegerImage() {
		super();
		this.unitMap = new HashMap<Integer, Unit>();
		this.memoryId = LargeImageMemoryManager.getInstance().addImage(this);
	}

	/**
	 * Constructs a LargeIntegerImage identical to the given argument
	 * 
	 * @param image
	 *            LargeIntegerImage to copy
	 */
	public LargeIntegerImage(LargeIntegerImage image) {
		this(image, true);
	}

	/**
	 * Constructs a LargeIntegerImage from the given argument. The pixels are
	 * copied if and only if ''copyData'' is set to true.
	 * 
	 * @param image
	 *            LargeIntegerImage to copy
	 * @param copydata
	 *            Indicates whether each pixels must be copied
	 */
	public LargeIntegerImage(LargeIntegerImage image, boolean copydata) {

		this();
		this.initializeUnitPowerSize(image.getUnitPowerSize());

		this.setDim(image.getXDim(), image.getYDim(), image.getZDim(), image
				.getTDim(), image.getBDim());
		this.copyAttributes(image);

		this.setUnitDim(image.getUnitDim());
		this.setUnitLength(image.getUnitLength());

		this.createFile();

		int uDim = this.getUnitDim();

		if (copydata) {
			IntegerUnit currentUnit;
			for (int i = 0; i < uDim; i++) {
				currentUnit = image.getAnUnit(i).clone();
				this.setUnit(currentUnit, i, true);
			}

		} else {
			fillFile();
		}
	}

	/**
	 * Constructs a LargeIntegerImage identical to the given argument
	 * 
	 * @param image
	 *            Image to copy
	 */
	public LargeIntegerImage(Image image) {
		this(image, true, 0);
	}

	/**
	 * Constructs a LargeIntegerImage from the given argument. The pixels are
	 * copied if and only if ''copyData'' is set to true.
	 * 
	 * @param image
	 *            Image to copy
	 * @param copyData
	 *            Indicates whether each pixels must be copied
	 */
	public LargeIntegerImage(Image image, boolean copyData) {
		this(image, copyData, 0);
	}

	/**
	 * Constructs a LargeIntegerImage from the given argument. The pixels are
	 * copied if and only if ''copyData'' is set to true.
	 * 
	 * @param image
	 *            Image to copy
	 * @param copyData
	 *            if and only if it is set to true are the pixels copied
	 * @param unitArea
	 *            Number of megabytes for each unit
	 */
	public LargeIntegerImage(Image image, boolean copyData, int unitArea) {
		this();

		this.setDim(image.getXDim(), image.getYDim(), image.getZDim(), image
				.getTDim(), image.getBDim());
		this.computeUnitSize(unitArea);
		this.copyAttributes(image);

		this.calculate();
		this.createFile();

		if (copyData == true) {
			int uSize = this.getUnitSize();
			IntegerUnit currentUnit;
			// for each units except the last one we copy the pixels
			for (int i = 0; i < this.getUnitDim() - 1; i++) {
				currentUnit = this.newUnit();
				for (int j = 0; j < uSize; j++) {
					currentUnit.setPixel(j, image.getPixelInt(j
							+ (i << this.getUnitPowerSize())));
				}
				this.setUnit(currentUnit, i, true);

			}
			// The last unit is not full of pixels so we take care not to ask
			// for out of bound pixels
			int i = this.getUnitDim() - 1;
			int size = image.size();//TODO if it is another large image it won't work yet
			currentUnit = this.newUnit();
			for (int j = 0; (j + (i << this.getUnitPowerSize())) < size; j++) {
				currentUnit.setPixel(j, image.getPixelInt(j
						+ (i << this.getUnitPowerSize())));
			}
			this.setUnit(currentUnit, i, true);

		} else {
			this.fillFile();
		}
	}

	/**
	 * Constructs a LargeIntegerImage with the given dimensions
	 * 
	 * @param xdim
	 *            the horizontal dimension
	 * @param ydim
	 *            the vertical dimension
	 * @param zdim
	 *            the depth
	 * @param tdim
	 *            the frame number
	 * @param bdim
	 *            the channel number
	 */
	public LargeIntegerImage(int xdim, int ydim, int zdim, int tdim, int bdim) {
		this(xdim, ydim, zdim, tdim, bdim, 0);
	}

	/**
	 * Constructs a LargeIntegerImage with the given dimensions
	 * 
	 * @param xdim
	 *            the horizontal dimension
	 * @param ydim
	 *            the vertical dimension
	 * @param zdim
	 *            the depth
	 * @param tdim
	 *            the frame number
	 * @param bdim
	 *            the channel number
	 * @param unitArea
	 *            Number of megabytes for each unit
	 */
	public LargeIntegerImage(int xdim, int ydim, int zdim, int tdim, int bdim,
			int unitArea) {
		this();

		this.setDim(xdim, ydim, zdim, tdim, bdim);
		this.computeUnitSize(unitArea);

		this.calculate();
		this.createFile();
		this.fillFile();
	}

	@Override
	public LargeIntegerImage newInstance(int xdim, int ydim, int zdim,
			int tdim, int bdim) {
		return new LargeIntegerImage(xdim, ydim, zdim, tdim, bdim);
	}

	@Override
	public void calculate() {
		LargeImageUtil.calculate(this);
	}

	@Override
	public void close() {
		LargeImageUtil.close(this);
	}

	@Override
	public void computeUnitDim() {
		LargeImageUtil.computeUnitDim(this);
	}

	@Override
	public void computeUnitLength() {
		LargeImageUtil.computeUnitLength(this);
	}

	@Override
	public void computeUnitSize(int unitArea) {
		LargeImageUtil.computeUnitSize(this, unitArea,
				LargeImageUtil.INTEGER_DATALENGTH);
	}

	@Override
	public void createFile() {
		LargeImageUtil.createFile(this);
	}

	@Override
	public void discardUnit(int currentId) {
		LargeImageUtil.discardUnit(this, currentId);

	}

	@Override
	public void fillFile() {
		LargeImageUtil.fillFile(this);
	}

	@Override
	public IntegerUnit getAnUnit(int id) {
		return (IntegerUnit) LargeImageUtil.getAnUnit(this, id);
	}

	@Override
	public File getFile() {
		return this.fichier;
	}

	@Override
	public int getMemoryId() {
		return this.memoryId;
	}

	/**
	 * Gets the pixel corresponding to the given location.
	 * 
	 * @param loc
	 *            location of the pixel given as a long
	 * @return the pixel value as an integer
	 */
	private int getPixel(long loc) {

		unitId = loc >> getUnitPowerSize();
		unitLoc = loc & ((1 << getUnitPowerSize()) - 1);

		IntegerUnit currentUnit = this.getAnUnit((int) unitId);
		return currentUnit.getPixel((int) unitLoc);
	}

	@Override
	public boolean getPixelBoolean(long loc) {
		return intToBoolean(getPixel(loc));
		//return (getPixel(loc) > Integer.MAX_VALUE / 2) ? true : false;
	}

	@Override
	public boolean getPixelBoolean(int loc) {
		return intToBoolean(getPixel(loc));
		//return (getPixel(loc) > Integer.MAX_VALUE / 2) ? true : false;
	}

	@Override
	public int getPixelByte(long loc) {
		return intToUnsignedByte(getPixel(loc));
//		return (getPixel(loc) >> 24) - Byte.MIN_VALUE;
	}

	@Override
	public int getPixelByte(int loc) {
		return intToUnsignedByte(getPixel(loc));
//		return (getPixel(loc) >> 24) - Byte.MIN_VALUE;
	}

	@Override
	public double getPixelDouble(long loc) {
		return intToDouble(getPixel(loc));
//		return DoubleImage.intToDouble * (double) getPixel(loc) + 0.5;
	}

	@Override
	public double getPixelDouble(int loc) {
		return intToDouble(getPixel(loc));
//		return DoubleImage.intToDouble * (double) getPixel(loc) + 0.5;
	}

	@Override
	public int getPixelInt(long loc) {
		return getPixel(loc);
	}

	@Override
	public int getPixelInt(int loc) {
		return getPixel(loc);
	}

	@Override
	public int getUnitDim() {
		return this.unitDim;
	}

	@Override
	public long getUnitLength() {
		return this.unitLength;
	}

	@Override
	public int getUnitPowerSize() {
		return this.unitPowerSize;
	}

	@Override
	public int getUnitSize() {
		return 1 << this.getUnitPowerSize();
	}

	@Override
	public void initializeUnitPowerSize(int newSize) {
		if (this.unitPowerSize != -1) {
			throw new PelicanException(
					"Someone tried to initialize unit power size twice");
		}
		this.unitPowerSize = newSize;
	}

	@Override
	public IntegerUnit loadAnUnit(int id) {
		return (IntegerUnit) LargeImageUtil.loadAnUnit(this, id);
	}

	@Override
	public IntegerUnit newUnit() {
		return new IntegerUnit(this.getUnitSize());
	}

	@Override
	public void putUnitIntoMap(int currentId, Unit currentUnit) {
		if (!(currentUnit instanceof IntegerUnit)) {
			throw new PelicanException(
					"Someone tried to put a wrong unit in a LargeIntegerImage");
		}
		this.unitMap.put(currentId, (IntegerUnit) currentUnit);

	}

	@Override
	public void setFile(File fichier) {
		this.fichier = fichier;
	}

	/**
	 * Sets the pixels at the given location to the given value as integer
	 * 
	 * @param loc
	 *            index of the pixel to modify
	 * @param value
	 *            desired value of the pixel as integer
	 */
	private void setPixel(long loc, int value) {

		unitId = loc >> getUnitPowerSize();
		unitLoc = loc & ((1 << getUnitPowerSize()) - 1);

		LargeImageMemoryManager.getInstance().lock.lock();
		try{	
			IntegerUnit currentUnit = this.getAnUnit((int) unitId);
			currentUnit.setPixel((int) unitLoc, value);
		}finally{
			LargeImageMemoryManager.getInstance().lock.unlock();
		}
	}

	@Override
	public void setPixelBoolean(long loc, boolean value) {
		setPixel(loc, booleanToInt(value));
//		setPixel(loc, value ? Integer.MAX_VALUE : 0);
	}

	@Override
	public void setPixelBoolean(int loc, boolean value) {
		setPixel(loc, booleanToInt(value));
//		setPixel(loc, value ? Integer.MAX_VALUE : 0);
	}

	@Override
	public void setPixelByte(long loc, int value) {
		setPixel(loc, unsignedByteToInt(value));
//		setPixel(loc, (value + Byte.MIN_VALUE) << 24);
	}

	@Override
	public void setPixelByte(int loc, int value) {
		setPixel(loc, unsignedByteToInt(value));
//		setPixel(loc, (value + Byte.MIN_VALUE) << 24);
	}

	@Override
	public void setPixelDouble(long loc, double value) {
		setPixel(loc,doubleToInt(value));
//		setPixel(loc, (int) ((value - 0.5) * doubleToInt));
	}

	@Override
	public void setPixelDouble(int loc, double value) {
		setPixel(loc,doubleToInt(value));
//		setPixel(loc, (int) ((value - 0.5) * doubleToInt));
	}

	@Override
	public void setPixelInt(long loc, int value) {
		setPixel(loc, value);
	}

	@Override
	public void setPixelInt(int loc, int value) {
		setPixel(loc, value);
	}

	@Override
	public void setSize(long newSize) {
		this.size = newSize;
	}

	@Override
	public void setUnit(Unit currentUnit, int currentId, boolean modified) {
		LargeImageUtil.setUnit(this, currentUnit, currentId, modified);
	}

	@Override
	public void setUnitDim(int newUnitDim) {
		this.unitDim = newUnitDim;
	}

	@Override
	public void setUnitLength(long newUnitLength) {
		this.unitLength = newUnitLength;
	}

	public long sizeL() {
		return this.size;
	}

	@Override
	public int size() {// TODO
		return (int) this.size;
	}

	@Override
	public boolean equals(Image im) {
		if ((im == null) || !(im instanceof IntegerImage)) {
			return false;
		}
		if (!haveSameDimensions(this, im)) {
			return false;
		}
		if (im instanceof LargeIntegerImage){
			for(int u=0;u<this.getUnitDim();u++){
				if (!this.getAnUnit(u).equals(((LargeIntegerImage)im).getAnUnit(u))){
					return false;
				}
			}
		}else{
			for (long i = 0; i < size; i++) {
				if (((IntegerImage) im).getPixelInt(i) != getPixelInt(i)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			this.close();
		} finally {
			super.finalize();
		}
	}

	@Override
	public LargeIntegerImage copyImage(boolean copyData) {
		return new LargeIntegerImage(this, copyData);
	}

	@Override
	public void fill(int b) {
		int size = this.getUnitSize();
		IntegerUnit intUnit = new IntegerUnit(size, b);
		for (int i = 0; i < this.getUnitDim(); i++) {
			this.setUnit(intUnit.clone(), i, true);
		}
	}

	@Override
	public LargeByteImage copyToByteImage() {
		LargeByteImage i = new LargeByteImage();
		i.initializeUnitPowerSize(this.getUnitPowerSize()+2);

		i.setDim(this.getXDim(), this.getYDim(), this.getZDim(), this.getTDim(), this.getBDim());
		i.copyAttributes(this);
		i.calculate();
		i.createFile();
		i.fillFile();
			
		ByteUnit byteUnit=null;		
		IntegerUnit intUnit;
		int j;
		for (int u =0; u<this.getUnitDim();u++){
			j=u%4;
			if(j==0){
				if(u!=0){
					i.setUnit(byteUnit, (u>>2)-1, true);
				}
				byteUnit = i.newUnit();
			}
			intUnit = this.getAnUnit(u);
			for(int k = 0; k<intUnit.size();k++){
				byteUnit.setPixel(j*intUnit.size()+k, unsignedByteToSignedByte(intUnit.getPixel(k)));
			}
		}
		i.setUnit(byteUnit, i.getUnitDim()-1, true);
		return i;				
	}

	@Override
	public LargeByteImage convertToByteImage() {		
		LargeIntegerImage i = scaleToVisibleRange();

		LargeByteImage b = new LargeByteImage();
		b.initializeUnitPowerSize(this.getUnitPowerSize()+2);
		b.setDim(this.getXDim(), this.getYDim(), this.getZDim(), this.getTDim(), this.getBDim());
		b.copyAttributes(this);
		b.calculate();
		b.createFile();
		b.fillFile();
		
		ByteUnit byteUnit=null;		
		IntegerUnit intUnit;
		int j;
		for (int u =0; u<this.getUnitDim();u++){
			j=u%4;
			if(j==0){
				if(u!=0){
					b.setUnit(byteUnit,((u>>2)-1), true);
				}				
				byteUnit = b.newUnit();
			}
			intUnit = i.getAnUnit(u);
			for(int k = 0; k<intUnit.size();k++){
				byteUnit.setPixel(j*intUnit.size()+k, intToSignedByte(intUnit.getPixel(k)));
			}
		}
		b.setUnit(byteUnit,b.getUnitDim()-1, true);

		return b;
	}

	@Override
	public LargeIntegerImage scaleToVisibleRange() {//TODO fusion with convertToByteImage
		LargeIntegerImage d = this.copyImage(false);

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		int tmp;
		IntegerUnit intUnit; 
		IntegerUnit newIntUnit;
		
		for (int u =0; u<this.getUnitDim();u++){
			intUnit = this.getAnUnit(u);
			for (int i=0;i<intUnit.size();i++){
				tmp = intUnit.getPixel(i);
				if (min > tmp) {
					min = tmp;
				}
				if (max < tmp) {
					max = tmp;
				}
			}
		}
		double dist = (long)max - (long)min;
		double value;
		if (max != min) {			
			for (int u =0; u<this.getUnitDim();u++){
				intUnit = this.getAnUnit(u);
				newIntUnit =d.newUnit();
				for (int i=0;i<intUnit.size();i++){
					tmp = intUnit.getPixel(i);
					value = ((double) ((long)tmp - (long)min) / dist);
					newIntUnit.setPixel(i, doubleToInt(value));
				}
				d.setUnit(newIntUnit, u, true);
			}			
		} else {
			System.err.println("Scaling error");
		}
		return d;
	}

	@Override
	public LargeDoubleImage normalise() {
		LargeDoubleImage d = new LargeDoubleImage();
		d.initializeUnitPowerSize(this.getUnitPowerSize()-1);
		d.setDim(this.getXDim(), this.getYDim(), this.getZDim(), this.getTDim(), this.getBDim());
		d.copyAttributes(this);
		d.calculate();
		d.createFile();
		d.fillFile();
		IntegerUnit intUnit=null;
		
		int minInt = Integer.MAX_VALUE;
		int maxInt = Integer.MIN_VALUE;
		int tmp;
		
		for (int u =0; u<this.getUnitDim();u++){
			intUnit = this.getAnUnit(u);
			for (int i=0;i<intUnit.size();i++){
				tmp = intUnit.getPixel(i);
				if (minInt > tmp) {
					minInt = tmp;
				}
				if (maxInt < tmp) {
					maxInt = tmp;
				}
			}
		}
		double min = intToDouble(minInt);
		double max = intToDouble(maxInt);
		
		DoubleUnit newDoubleUnit;
		int j;
		
		double dist = max - min;
		double value;
		if (max != min) {
			for (int u =0; u<d.getUnitDim();u++){
				j=u%2;
				if(j==0){
					intUnit = this.getAnUnit(u>>1);
				}
				newDoubleUnit =d.newUnit();
				for (int i=0;i<newDoubleUnit.size();i++){
					tmp = intUnit.getPixel(i+j*newDoubleUnit.size());
					value = ((double) ((long)tmp - (long)min) / dist);
					newDoubleUnit.setPixel(i, value);
				}
				d.setUnit(newDoubleUnit, u, true);
			}			
		} else {
			System.err.println("Scaling error");
		}
		return d;
	}

	@Override
	public HashMap<Integer, Unit> getMap() {
		return this.unitMap;
	}

	@Override
	public String getWorkingFileSuffix() {
		return ".largeinteger";
	}

	@Override
	public void saveData() {
		LargeImageUtil.saveData(this);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		this.unitMap = new HashMap<Integer, Unit>();
		this.memoryId = LargeImageMemoryManager.getInstance().addImage(this);
		this.createFile();
		IntegerUnit unit;
		for(int u=0;u<this.getUnitDim();u++){
			 unit = (IntegerUnit) in.readObject();
			 this.setUnit(unit, u, true);
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
		for(int u=0;u<this.getUnitDim();u++){
			out.writeObject(this.getAnUnit(u));
		}
	}

	@Override
	public int maximum(){
		int val = Integer.MIN_VALUE;
		int currVal;
		for (int u = 0; u < this.getUnitDim(); u++){
			currVal =  this.getAnUnit(u).maximum();
			if (currVal>val){
				val = currVal;
			}
		}
		return val;
	}

	@Override
	public int minimum(){
		int val = Integer.MAX_VALUE;
		int currVal;
		for (int u = 0; u < this.getUnitDim(); u++){
			currVal =  this.getAnUnit(u).minimum();
			if (currVal<val){
				val = currVal;
			}
		}
		return val;
	}

	@Override
	public int maximum(int band){
		int val = Integer.MIN_VALUE;
		int currVal;
		for (int u = 0; u < this.getUnitDim(); u++){
			currVal =  this.getAnUnit(u).maximum(band);
			if (currVal>val){
				val = currVal;
			}
		}
		return val;
	}

	@Override
	public int minimum(int band){
		int val = Integer.MAX_VALUE;
		int currVal;
		for (int u = 0; u < this.getUnitDim(); u++){
			currVal =  this.getAnUnit(u).minimum(band);
			if (currVal<val){
				val = currVal;
			}
		}
		return val;
	}
	
	/*
	 * Image methods
	 * *************************************************************
	 * *****************
	 */

	@Override
	public double volume() {
		return LargeImageUtil.volume(this);
	}

	@Override
	public int volumeByte() {
		return LargeImageUtil.volumeByte(this);
	}

	@Override
	public double getPixelDouble(int x, int y, int z, int t, int b) {
		return LargeImageUtil.getPixelXYZTBDouble(this, x, y, z, t, b);
	}

	@Override
	public double getPixelXYDouble(int x, int y) {
		return LargeImageUtil.getPixelXYDouble(this, x, y);
	}

	@Override
	public double getPixelXYZDouble(int x, int y, int z) {
		return LargeImageUtil.getPixelXYZDouble(this, x, y, z);
	}

	@Override
	public double getPixelXYBDouble(int x, int y, int b) {
		return LargeImageUtil.getPixelXYBDouble(this, x, y, b);
	}

	@Override
	public double getPixelXYTDouble(int x, int y, int t) {
		return LargeImageUtil.getPixelXYTDouble(this, x, y, t);
	}

	@Override
	public double getPixelXYZTDouble(int x, int y, int z, int t) {
		return LargeImageUtil.getPixelXYZTDouble(this, x, y, z, t);
	}

	@Override
	public double getPixelXYZBDouble(int x, int y, int z, int b) {
		return LargeImageUtil.getPixelXYZBDouble(this, x, y, z, b);
	}

	@Override
	public double getPixelXYTBDouble(int x, int y, int t, int b) {
		return LargeImageUtil.getPixelXYTBDouble(this, x, y, t, b);
	}

	@Override
	public double getPixelXYZTBDouble(int x, int y, int z, int t, int b) {
		return LargeImageUtil.getPixelXYZTBDouble(this, x, y, z, t, b);
	}

	@Override
	public void setPixelDouble(int x, int y, int z, int t, int b, double value) {
		LargeImageUtil.setPixelXYZTBDouble(this, x, y, z, t, b, value);
	}

	@Override
	public void setPixelXYDouble(int x, int y, double value) {
		LargeImageUtil.setPixelXYDouble(this, x, y, value);
	}

	@Override
	public void setPixelXYZDouble(int x, int y, int z, double value) {
		LargeImageUtil.setPixelXYZDouble(this, x, y, z, value);
	}

	@Override
	public void setPixelXYBDouble(int x, int y, int b, double value) {
		LargeImageUtil.setPixelXYBDouble(this, x, y, b, value);
	}

	@Override
	public void setPixelXYTDouble(int x, int y, int t, double value) {
		LargeImageUtil.setPixelXYTDouble(this, x, y, t, value);
	}

	@Override
	public void setPixelXYZTDouble(int x, int y, int z, int t, double value) {
		LargeImageUtil.setPixelXYZTDouble(this, x, y, z, t, value);
	}

	@Override
	public void setPixelXYZBDouble(int x, int y, int z, int b, double value) {
		LargeImageUtil.setPixelXYZBDouble(this, x, y, z, b, value);
	}

	@Override
	public void setPixelXYTBDouble(int x, int y, int t, int b, double value) {
		LargeImageUtil.setPixelXYTBDouble(this, x, y, t, b, value);
	}

	@Override
	public void setPixelXYZTBDouble(int x, int y, int z, int t, int b,
			double value) {
		LargeImageUtil.setPixelXYZTBDouble(this, x, y, z, t, b, value);
	}

	@Override
	public int getPixelInt(int x, int y, int z, int t, int b) {
		return LargeImageUtil.getPixelXYZTBInt(this, x, y, z, t, b);
	}

	@Override
	public int getPixelXYInt(int x, int y) {
		return LargeImageUtil.getPixelXYInt(this, x, y);
	}

	@Override
	public int getPixelXYZInt(int x, int y, int z) {
		return LargeImageUtil.getPixelXYZInt(this, x, y, z);
	}

	@Override
	public int getPixelXYBInt(int x, int y, int b) {
		return LargeImageUtil.getPixelXYBInt(this, x, y, b);
	}

	@Override
	public int getPixelXYTInt(int x, int y, int t) {
		return LargeImageUtil.getPixelXYTInt(this, x, y, t);
	}

	@Override
	public int getPixelXYZTInt(int x, int y, int z, int t) {
		return LargeImageUtil.getPixelXYZTInt(this, x, y, z, t);
	}

	@Override
	public int getPixelXYZBInt(int x, int y, int z, int b) {
		return LargeImageUtil.getPixelXYZBInt(this, x, y, z, b);
	}

	@Override
	public int getPixelXYTBInt(int x, int y, int t, int b) {
		return LargeImageUtil.getPixelXYTBInt(this, x, y, t, b);
	}

	@Override
	public int getPixelXYZTBInt(int x, int y, int z, int t, int b) {
		return LargeImageUtil.getPixelXYZTBInt(this, x, y, z, t, b);
	}

	@Override
	public void setPixelInt(int x, int y, int z, int t, int b, int value) {
		LargeImageUtil.setPixelXYZTBInt(this, x, y, z, t, b, value);
	}

	@Override
	public void setPixelXYInt(int x, int y, int value) {
		LargeImageUtil.setPixelXYInt(this, x, y, value);
	}

	@Override
	public void setPixelXYZInt(int x, int y, int z, int value) {
		LargeImageUtil.setPixelXYZInt(this, x, y, z, value);
	}

	@Override
	public void setPixelXYBInt(int x, int y, int b, int value) {
		LargeImageUtil.setPixelXYBInt(this, x, y, b, value);
	}

	@Override
	public void setPixelXYTInt(int x, int y, int t, int value) {
		LargeImageUtil.setPixelXYTInt(this, x, y, t, value);
	}

	@Override
	public void setPixelXYZTInt(int x, int y, int z, int t, int value) {
		LargeImageUtil.setPixelXYZTInt(this, x, y, z, t, value);
	}

	@Override
	public void setPixelXYZBInt(int x, int y, int z, int b, int value) {
		LargeImageUtil.setPixelXYZBInt(this, x, y, z, b, value);
	}

	@Override
	public void setPixelXYTBInt(int x, int y, int t, int b, int value) {
		LargeImageUtil.setPixelXYTBInt(this, x, y, t, b, value);
	}

	@Override
	public void setPixelXYZTBInt(int x, int y, int z, int t, int b, int value) {
		LargeImageUtil.setPixelXYZTBInt(this, x, y, z, t, b, value);
	}

	@Override
	public int getPixelByte(int x, int y, int z, int t, int b) {
		return LargeImageUtil.getPixelXYZTBByte(this, x, y, z, t, b);
	}

	@Override
	public int getPixelXYByte(int x, int y) {
		return LargeImageUtil.getPixelXYByte(this, x, y);
	}

	@Override
	public int getPixelXYZByte(int x, int y, int z) {
		return LargeImageUtil.getPixelXYZByte(this, x, y, z);
	}

	@Override
	public int getPixelXYBByte(int x, int y, int b) {
		return LargeImageUtil.getPixelXYBByte(this, x, y, b);
	}

	@Override
	public int getPixelXYTByte(int x, int y, int t) {
		return LargeImageUtil.getPixelXYTByte(this, x, y, t);
	}

	@Override
	public int getPixelXYZTByte(int x, int y, int z, int t) {
		return LargeImageUtil.getPixelXYZTByte(this, x, y, z, t);
	}

	@Override
	public int getPixelXYZBByte(int x, int y, int z, int b) {
		return LargeImageUtil.getPixelXYZBByte(this, x, y, z, b);
	}

	@Override
	public int getPixelXYTBByte(int x, int y, int t, int b) {
		return LargeImageUtil.getPixelXYTBByte(this, x, y, t, b);
	}

	@Override
	public int getPixelXYZTBByte(int x, int y, int z, int t, int b) {
		return LargeImageUtil.getPixelXYZTBByte(this, x, y, z, t, b);
	}

	@Override
	public void setPixelByte(int x, int y, int z, int t, int b, int value) {
		LargeImageUtil.setPixelXYZTBByte(this, x, y, z, t, b, value);
	}

	@Override
	public void setPixelXYByte(int x, int y, int value) {
		LargeImageUtil.setPixelXYByte(this, x, y, value);
	}

	@Override
	public void setPixelXYZByte(int x, int y, int z, int value) {
		LargeImageUtil.setPixelXYZByte(this, x, y, z, value);
	}

	@Override
	public void setPixelXYBByte(int x, int y, int b, int value) {
		LargeImageUtil.setPixelXYBByte(this, x, y, b, value);
	}

	@Override
	public void setPixelXYTByte(int x, int y, int t, int value) {
		LargeImageUtil.setPixelXYTByte(this, x, y, t, value);
	}

	@Override
	public void setPixelXYZTByte(int x, int y, int z, int t, int value) {
		LargeImageUtil.setPixelXYZTByte(this, x, y, z, t, value);
	}

	@Override
	public void setPixelXYZBByte(int x, int y, int z, int b, int value) {
		LargeImageUtil.setPixelXYZBByte(this, x, y, z, b, value);
	}

	@Override
	public void setPixelXYTBByte(int x, int y, int t, int b, int value) {
		LargeImageUtil.setPixelXYTBByte(this, x, y, t, b, value);
	}

	@Override
	public void setPixelXYZTBByte(int x, int y, int z, int t, int b, int value) {
		LargeImageUtil.setPixelXYZTBByte(this, x, y, z, t, b, value);
	}

	@Override
	public boolean getPixelBoolean(int x, int y, int z, int t, int b) {
		return LargeImageUtil.getPixelXYZTBBoolean(this, x, y, z, t, b);
	}

	@Override
	public boolean getPixelXYBoolean(int x, int y) {
		return LargeImageUtil.getPixelXYBoolean(this, x, y);
	}

	@Override
	public boolean getPixelXYZBoolean(int x, int y, int z) {
		return LargeImageUtil.getPixelXYZBoolean(this, x, y, z);
	}

	@Override
	public boolean getPixelXYBBoolean(int x, int y, int b) {
		return LargeImageUtil.getPixelXYBBoolean(this, x, y, b);
	}

	@Override
	public boolean getPixelXYTBoolean(int x, int y, int t) {
		return LargeImageUtil.getPixelXYTBoolean(this, x, y, t);
	}

	@Override
	public boolean getPixelXYZTBoolean(int x, int y, int z, int t) {
		return LargeImageUtil.getPixelXYZTBoolean(this, x, y, z, t);
	}

	@Override
	public boolean getPixelXYZBBoolean(int x, int y, int z, int b) {
		return LargeImageUtil.getPixelXYZBBoolean(this, x, y, z, b);
	}

	@Override
	public boolean getPixelXYTBBoolean(int x, int y, int t, int b) {
		return LargeImageUtil.getPixelXYTBBoolean(this, x, y, t, b);
	}

	@Override
	public boolean getPixelXYZTBBoolean(int x, int y, int z, int t, int b) {
		return LargeImageUtil.getPixelXYZTBBoolean(this, x, y, z, t, b);
	}

	@Override
	public void setPixelBoolean(int x, int y, int z, int t, int b, boolean value) {
		LargeImageUtil.setPixelXYZTBBoolean(this, x, y, z, t, b, value);
	}

	@Override
	public void setPixelXYBoolean(int x, int y, boolean value) {
		LargeImageUtil.setPixelXYBoolean(this, x, y, value);
	}

	@Override
	public void setPixelXYZBoolean(int x, int y, int z, boolean value) {
		LargeImageUtil.setPixelXYZBoolean(this, x, y, z, value);
	}

	@Override
	public void setPixelXYBBoolean(int x, int y, int b, boolean value) {
		LargeImageUtil.setPixelXYBBoolean(this, x, y, b, value);
	}

	@Override
	public void setPixelXYTBoolean(int x, int y, int t, boolean value) {
		LargeImageUtil.setPixelXYTBoolean(this, x, y, t, value);
	}

	@Override
	public void setPixelXYZTBoolean(int x, int y, int z, int t, boolean value) {
		LargeImageUtil.setPixelXYZTBoolean(this, x, y, z, t, value);
	}

	@Override
	public void setPixelXYZBBoolean(int x, int y, int z, int b, boolean value) {
		LargeImageUtil.setPixelXYZBBoolean(this, x, y, z, b, value);
	}

	@Override
	public void setPixelXYTBBoolean(int x, int y, int t, int b, boolean value) {
		LargeImageUtil.setPixelXYTBBoolean(this, x, y, t, b, value);
	}

	@Override
	public void setPixelXYZTBBoolean(int x, int y, int z, int t, int b,
			boolean value) {
		LargeImageUtil.setPixelXYZTBBoolean(this, x, y, z, t, b, value);
	}

	/*
	 * Mask management**************************************************
	 */

	@Override
	public boolean isInMask(int loc) {
		return LargeImageUtil.isInMask(this, loc);
	}

	@Override
	public boolean isInMask(long loc) {
		return LargeImageUtil.isInMask(this, loc);
	}

	@Override
	public boolean isPresent(int loc) {
		return LargeImageUtil.isPresent(this, loc);
	}

	@Override
	public boolean isPresent(long loc) {
		return LargeImageUtil.isPresent(this, loc);
	}

	@Override
	public int getNumberOfPresentPixel() {
		return LargeImageUtil.getNumberOfPresentPixel(this);
	}

	@Override
	public int getNumberOfPresentPixel(int band) {
		return LargeImageUtil.getNumberOfPresentPixel(this, band);
	}

	@Override
	public long getLongBDim() {
		return this.longBdim;
	}

	@Override
	public long getLongTDim() {
		return this.longTdim;
	}

	@Override
	public long getLongXDim() {
		return this.longXdim;
	}

	@Override
	public long getLongYDim() {
		return this.longYdim;
	}

	@Override
	public long getLongZDim() {
		return this.longZdim;
	}

	@Override
	public void setXDim(int xdim) {
		super.setXDim(xdim);
		this.longXdim = (long) xdim;
	}

	@Override
	public void setYDim(int ydim) {
		super.setYDim(ydim);
		this.longYdim = (long) ydim;
	}

	@Override
	public void setZDim(int zdim) {
		super.setZDim(zdim);
		this.longZdim = (long) zdim;
	}

	@Override
	public void setTDim(int tdim) {
		super.setTDim(tdim);
		this.longTdim = (long) tdim;
	}

	@Override
	public void setBDim(int bdim) {
		super.setBDim(bdim);
		this.longBdim = (long) bdim;
	}

	@Override
	public void setPixels(int[] values) {
		throw new PelicanException(
				"You can not use setPixels(int[]) in a LargeImage");
	}

	@Override
	public void setDim(int x, int y, int z, int t, int b) {
		super.setDim(x, y, z, t, b);
		this.setSize((long) x * (long) y * (long) z * (long) t * (long) b);
	}
	

	/*
	 * Constructors 
	 */

	@Override
	public LargeBooleanImage newBooleanImage() {
		return LargeImageUtil.newBooleanImage(this,false);
	}

	@Override
	public LargeBooleanImage newBooleanImage(boolean copyData) {
		return LargeImageUtil.newBooleanImage(this,copyData);
	}

	@Override
	public LargeBooleanImage newBooleanImage(int x, int y, int z, int t, int b) {
		return LargeImageUtil.newBooleanImage(x, y, z, t, b);
	}

	@Override
	public LargeByteImage newByteImage() {
		return LargeImageUtil.newByteImage(this,false);
	}

	@Override
	public LargeByteImage newByteImage(boolean copyData) {
		return LargeImageUtil.newByteImage(this,copyData);
	}

	@Override
	public LargeByteImage newByteImage(int x, int y, int z, int t, int b) {
		return LargeImageUtil.newByteImage(x,y,z,t,b);		
	}

	@Override
	public LargeDoubleImage newDoubleImage() {
		return LargeImageUtil.newDoubleImage(this,false);
	}

	@Override
	public LargeDoubleImage newDoubleImage(boolean copyData) {
		return LargeImageUtil.newDoubleImage(this,copyData);
	}

	@Override
	public LargeDoubleImage newDoubleImage(int x, int y, int z, int t, int b) {
		return LargeImageUtil.newDoubleImage(x, y, z, t, b);
	}

	@Override
	public LargeIntegerImage newIntegerImage() {
		return LargeImageUtil.newIntegerImage(this,false);
	}

	@Override
	public LargeIntegerImage newIntegerImage(boolean copyData) {
		return LargeImageUtil.newIntegerImage(this, copyData);
	}

	@Override
	public LargeIntegerImage newIntegerImage(int x, int y, int z, int t, int b) {
		return LargeImageUtil.newIntegerImage(x,y,z,t,b);
	}

}
