/* @(#)FitsColumn.java     $Revision: 1.6 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 1999 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.io.*;

/** FitsColumn class represents a FITS table column in either ASCII or
 *  BINARY table format. Note: only binary data formats A,L,I,J,E,D
 *  are fully supported.
 *
 *  @version $Revision: 1.6 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsColumn {

    private String stringNull = null;
    private long    intNull;
    private boolean intNullDefined = false;
    private int repeat = 1;
    private char dataType = '\0';
    private long columnOffset = 0;
    private int recordSize = 0;
    private FitsTform format;
    private int bytesPerData = 0;
    private long noRows = 0;
    private boolean binColumn = true;
    private String display;
    private String label;
    private String unit;
    private double zero;
    private double scale;
    private boolean scaling = false;

    private RandomAccessFile dataFile = null;
    private long dataOffset = 0;
    private boolean isRAFile = true;
    private byte[] dataArray = null;

    /** Constructor for FitsColumn class from a file
     *
     *  @param type storage type of column i.e. Fits.ATABLE or
     *              Fits.BTABLE, by default Fits.BTABLE is assumed
     *  @param tform storage format of data in column 
     *  @param label name of column
     *  @param rows  no. of rows in the column
     *  @exception FitsException
     */
    public FitsColumn(int type, String tform, String label, int rows)
	throws FitsException {

	this.format = new FitsTform(tform);
	this.dataType = format.getDataType();
	this.repeat = format.getRepeat();
	this.bytesPerData = format.getWidth();
	this.label = label;
	this.noRows = rows;
	this.binColumn = (type == Fits.BTABLE);
    }

    /** Sets data matrix with the table data as a RandomAcessFile.
     *
     *  @param file  associated RandomAccessFile with FITS data
     *  @param dataOffset byte offset of the table data unit in the FITS file
     *  @param position relative byte start position of of column data
     *                  within record (first byte is 0)
     *  @param recordSize byte size of table record
     */
    public void setData(RandomAccessFile file, long dataOffset,
			int position, int recordSize) {
	dataFile = file;
	this.dataOffset = dataOffset;
	isRAFile = true;
	this.columnOffset = position;
	this.recordSize = recordSize;
    }

    /** Sets data matrix with the table data as a byte array
     *
     *  @param array  byte array with table data matrix
     *  @param position relative byte start position of of column data
     *                  within record (first byte is 0)
     *  @param recordSize byte size of table record
     */
    public void setData(byte[] array, int position, int recordSize) {
	dataArray = array;
	isRAFile = false;
	this.columnOffset = position;
	this.recordSize = recordSize;
    }

    /** Read single column element as integer value.  An ndefined
     *  value is returned as Integer.MIN_VALUE. The following column
     *  types are supported i.e. binary I/J formats.  For array type
     *  elements only the first element is read. Note: ASCII table
     *  columns are read with free format and does not conform to the
     *  FITS standard in the current implementation.
     *
     *  @param row no. of element in column (starting with 0)
     */
    public int getInt(int row) {
	int value = Integer.MIN_VALUE;
	byte[] dbuf = getBytes(row);

	try {
	    if (binColumn) {
		DataInputStream di = 
		    new DataInputStream(new ByteArrayInputStream(dbuf));
		switch (dataType) {
		case 'I' :
		    value = (int) di.readShort();
		    break;
		case 'J' :
		    value = di.readInt();
		    break;
		}
		if (intNullDefined && value==intNull) {
		    value = Integer.MIN_VALUE;
		} else if (scaling) {
		    value = (int) (zero + scale*value);
		}
	    } else {
		String str = new String(dbuf);
		if (stringNull == null || !str.startsWith(stringNull)) {
		    value = (Integer.valueOf(str)).intValue();
		    if (scaling) {
			value = (int) (zero + scale*value);
		    }
		}
	    }
	} catch (Exception e) {
	}

	return value;
    }

    /** Read single column element as integer array.  Undefined values
     *  are returned as Integer.MIN_VALUE. The following column types
     *  are supported i.e. binary I/J formats. Note: ASCII table
     *  columns are read with free format and does not conform to the
     *  FITS standard in the current implementation.
     *
     *  @param row no. of element in column (starting with 0)
     */
    public int[] getInts(int row) {
	int[] arr = new int[repeat];
	byte[] dbuf = getBytes(row);

	try {
	    if (binColumn) {
		DataInputStream di = 
		    new DataInputStream(new ByteArrayInputStream(dbuf));
		int value;
		for (int n=0; n<repeat; n++) {
		    value = Integer.MIN_VALUE;
		    switch (dataType) {
		    case 'I' :
			value = (int) di.readShort();
			break;
		    case 'J' :
			value =  di.readInt();
			break;
		    }
		    if (intNullDefined && value==intNull) {
			value = Integer.MIN_VALUE;
		    } else if (scaling) {
			    value = (int) (zero + scale*value);
			}
		    arr[n] = value;
		}
	    } else {
		String str = new String(dbuf);
		arr[0] = Integer.MIN_VALUE;
		if (stringNull == null || !str.startsWith(stringNull)) {
		    arr[0] = (Integer.valueOf(str)).intValue();
		    if (scaling) {
			arr[0] = (int) (zero + scale*arr[0]);
		    }
		}
	    }
	} catch (Exception e) {
	}

	return arr;
    }

    /** Read single column element as double value.  An undefined
     *  value is returned as Double.NaN. The following column types
     *  are supported i.e. binary I/J/E/D formats.  For array type
     *  elements only the first element is read.  Note: ASCII table
     *  columns are read with free format and does not conform to the
     *  FITS standard in the current implementation.
     *
     *  @param row no. of element in column (starting with 0)
     */
    public double getReal(int row) {
	double value = Double.NaN;
	byte[] dbuf = getBytes(row);

	try {
	    if (binColumn) {
		DataInputStream di = 
		    new DataInputStream(new ByteArrayInputStream(dbuf));
		switch (dataType) {
		case 'I' :
		    value = (double) di.readShort();
		    break;
		case 'J' :
		    value = (double) di.readInt();
		    break;
		case 'E' :
		    value = (double) di.readFloat();
		    break;
		case 'D' :
		    value = di.readDouble();
		    break;
		}
		if (scaling) {
		    value = zero + scale*value;
		}
	    }
	    else {
		String str = new String(dbuf);
		if ((stringNull == null) || !str.startsWith(stringNull)) {
		    value = (Double.valueOf(str)).doubleValue();
		    if (scaling) {
			value = zero + scale*value;
		    }
		}
	    }
	} catch (Exception e) {
	}

	return value;
    }

    /** Read single column element as double array. Undefined value
     *  are returned as Double.NaN. The following column types are
     *  supported: binary I/J/E/D formats. Note: ASCII table columns
     *  are read with free format and does not conform to the FITS
     *  standard in the current implementation.
     *
     *  @param row no. of element in column (starting with 0)
     */
    public double[] getReals(int row) {
	double[] arr = new double[repeat];
	byte[] dbuf = getBytes(row);
	
	try {
	    if (binColumn) {
		DataInputStream di = 
		    new DataInputStream(new ByteArrayInputStream(dbuf));
		for (int n=0; n<repeat; n++) {
		    switch (dataType) {
		    case 'I' :
			arr[n] = (double) di.readShort();
			break;
		    case 'J' :
			arr[n] = (double) di.readInt();
			break;
		    case 'E' :
			arr[n] = (double) di.readFloat();
			break;
		    case 'D' :
			arr[n] = di.readDouble();
			break;
		    }
		    if (scaling) {
			arr[n] = zero + scale*arr[n];
		    }
		}
	    } else {
		double value = Double.NaN;
		String str = new String(dbuf);
		if ((stringNull == null) || !str.startsWith(stringNull)) {
		    arr[0] = (Double.valueOf(str)).doubleValue();
		    if (scaling) {
			arr[0] = zero + scale*arr[0];
		    }
		}
	    }
	} catch (Exception e) {
	}

	return arr;
    }

    /** Read single column element as string. An undefined value
     *  is returned as a NULL string. The following column types are
     *  supported: A/L formats.
     *
     *  @param row number of element in column (starting with 0)
     */
    public String getString(int row) {
	String str = null;
	if ((dataType == 'A') || (dataType == 'L')) {
	    byte[] dbuf = getBytes(row);
	    if (dbuf != null) {
		if ((stringNull == null) || !str.startsWith(stringNull)) {
		    str = new String(dbuf);
		}
	    }
	}
	return str;
    }

    /** Extract a column element from the 'file' as byte array.
     *
     *  @param row number of element in column (starting with 0)
     */
    private byte[] getBytes(int row) {
	if (row<0 || noRows<=row) return null;
	byte[] dbuf = new byte[getWidth()];

	if (isRAFile) {
	    try {
	    if (dataFile == null) return null;
		dataFile.seek(dataOffset + columnOffset + row*recordSize);
		dataFile.read(dbuf);
	    } catch (IOException e) {
		return null;
	    }
	} else {
	    if (dataArray == null) return null;
	    int k = 0;
	    int n = (int) (columnOffset + row*recordSize);
	    int nsize = getWidth();
	    while (0 < nsize--) dbuf[k++] = dataArray[n++];
	}

	return dbuf;
    }

    /** Define NULL string for ASCII table column.
     *
     *  @param nullValue string with null value
     */
    public void setNull(String nullValue){
	this.stringNull = nullValue;
    }

    /** Define NULL value for interger format Binary table columns
     *  @param nullValue value of NULL integer */
    public void setNull(int nullValue){
	this.intNull = nullValue;
	intNullDefined = true;
    }

    /** Define dimension of binary table column. Note: This is not
     *  used in the current implementation
     *
     *  @param dim string with dimension soecification for column
     */
    public void setDim(String dim){
    }

    /** Retrieve repeat factor that is number of values per column element */
    public int getRepeat() {
	return this.repeat;
    }

    /** Get the data type character for column as given in the TFORM
     *  FITS keyword. */
    public char getDataType() {
	return this.dataType;
    }

    /** Calculate the number of bytes associated to a column entry. */
    protected int getWidth() {
	return repeat*bytesPerData;
    }

    /** Retrieve the display format for the column (ref. TDISP keyword). */
    public String getDisplay(){
	return display;
    }

    /** Set the display format of the column.
     *
     *  @param  display  string with the display format for the column */
    public void setDisplay(String display){
	this.display = display;
    }

    /** Retrieve unit string for the column. */
    public String getUnit(){
	return unit;
    }

    /** Set unit string for column.
     *
     *  @param  unit  string with the unit of the column */
    public void setUnit(String unit){
	this.unit = unit;
    }

    /** Retrieve column label. */
    public String getLabel(){
	return label;
    }

    /** Set label of the column.
     *
     *  @param label  string with the column label */
    public void setLabel(String label){
	this.label = label;
    }

    /** Get scaling zero point for table column. */
    public double getZero(){
	return zero;
    }

    /** Define scaling zero point for table column.
     *
     *  @param zero  scaling zero point applied on raw data values */
    public void setZero(double zero){
	this.zero = zero;
	this.scaling = true;
    }

    /** Get scaling factor for table column. */
    public double getScale(){
	return scale;
    }

    /** Define scaling factor for table column.
     *
     *  @param scale  scaling factor applied on raw data values */
    public void setScale(double scale){
	this.scale = scale;
	this.scaling = true;
    }
}
