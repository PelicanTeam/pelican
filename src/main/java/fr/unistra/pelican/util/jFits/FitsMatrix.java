/* @(#)FitsMatrix.java     $Revision: 1.10 $ $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2000 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.io.*;

/** FitsMatrix class represents a FITS data matrix either as a prime
 *  HD unit or as an image extension.
 *
 *  @version $Revision: 1.10 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsMatrix extends FitsData {

    private int noValues;
    private int dataFormat = Fits.FLOAT;
    private int bytesPerData = 4;
    private boolean scaling = false;
    private double zero = 0.0;
    private double scale = 1.0;
    private FitsWCS wcs;

    /** Constructor for FitsMatrix class given a FITS prime matrix or
     *  an image extension header with associated data unit as a file.
     *
     *  @param header  FitsHeader object with the image header
     *  @param file    RandomAccess file positioned at the start of the
     *                 associated data unit
     *  @param sflag   Flag for storing data matrix internally
     *  @exception FitsException */
    public FitsMatrix(FitsHeader header, DataInput file, boolean sflag)
	    throws FitsException {
	super(header, file, sflag);

	if (type != Fits.IMAGE) {
	    throw  new FitsException("Wrong header type",
				     FitsException.HEADER);
	}

	FitsKeyword kw = header.getKeyword("BITPIX");
	if ((kw == null) || (kw.getType() != FitsKeyword.INTEGER)) {
	    throw  new FitsException("Invalid or missing BITPIX",
				     FitsException.HEADER);
	}
 
	dataFormat = kw.getInt();
	bytesPerData = Math.abs(dataFormat)/8;
	noValues = (int) (size / bytesPerData);
    
	wcs = new FitsWCS(header);

	kw = header.getKeyword("BSCALE");     // check if scale is given
	if (kw!=null) {
	    scale = kw.getReal();
	    if (scale != 1.0) {
		scaling = true;
	    }
	}

	kw = header.getKeyword("BZERO");      // check if zero point is given
	if (kw!=null) {
	    zero = kw.getReal();
	    if (zero != 0.0) {
		scaling = true;
	    }
	}
    }

    /** Constructor for FitsMatrix class given definition of the matrix
     *  size and dimensions.
     *
     *  @param bitpix  Bits per pixel for data values in matrix
     *  @param nax     Array with dimensions of data matrix
     *  @exception FitsException */
    public FitsMatrix(int bitpix, int nax[]) throws FitsException {
	super(bitpix, nax);
	type = Fits.IMAGE;
	dataFormat = bitpix;
	bytesPerData = Math.abs(dataFormat)/8;
	noValues = (int) (size / bytesPerData);
	wcs = new FitsWCS(nax.length);
    }

    /** Create and return a minimum FITS header for data Matrix.
     */
    public FitsHeader getHeader() {
	FitsHeader hdr = super.getHeader();

//	hdr.addKeyword(new FitsKeyword("", ""));
//	for (int n=1; n<=naxis.length; n++) {
//	    hdr.addKeyword(new FitsKeyword("CRPIX"+n, wcs.crpix[n-1],
//					   "Reference pixel"));
//	    hdr.addKeyword(new FitsKeyword("CRVAL"+n, wcs.crval[n-1],
//					   "Coordinate at reference pixel"));
//	    hdr.addKeyword(new FitsKeyword("CDELT"+n, wcs.cdelt[n-1],
//					   "Coordinate increament per pixel"));
//	}

	return hdr;
    }

    /** Gets set of data points from the matrix as a short values.
     *  Only FITS file with BITPIX 8, 16 and 32 are read.
     *
     *  @param  offset   pixel offset within hte data matrix
     *  @param  size     no. of pixel values to be read
     *  @param  data     array which will hold the return values.
     *                   If null an array of size is created.
     *  @return          data[] array updated with pixel values
     *  @exception FitsException */
    public short[] getShortValues(int offset, int size, short data[])
	    throws FitsException {

	if ((offset<0) || (size<1)) return data;
	if (noValues < offset+size) size = (int) (noValues - offset);
	if ((data == null) || (data.length<size)) data = new short[size];

	int n = 0;
	DataInputStream dis = getInStream(offset, size);
	try {
	    switch (dataFormat) {
	    case Fits.BYTE:
		while (n<size) data[n++] = (short) dis.readUnsignedByte();
		break;
	    case Fits.SHORT:
		while (n<size) data[n++] = dis.readShort();
		break;
	    case Fits.INT:
		while (n<size) data[n++] = (short) dis.readInt();
		break;
	    case Fits.FLOAT:
	    case Fits.DOUBLE:
	    default: return data;
	    } 
	} catch (IOException e) {
	    throw new FitsException("Cannot convert data", FitsException.DATA);
	}

	if (scaling) {
	  for (int i=0; i<n; i++) data[i] = (short) (scale*data[i]+zero);
	}

	return data;
    }

    /** Gets set of data points from the matrix as a int values.
     *  Only FITS file with BITPIX 8, 16 and 32 are read.
     *
     *  @param  offset   pixel offset within hte data matrix
     *  @param  size     no. of pixel values to be read
     *  @param  data     array which will hold the return values.
     *                   If null an array of size is created.
     *  @return          data[] array updated with pixel values
     *  @exception FitsException */
    public int[] getIntValues(int offset, int size, int data[])
	    throws FitsException {

	if ((offset<0) || (size<1)) return data;
	if (noValues < offset+size) size = (int) (noValues - offset);
	if ((data == null) || (data.length<size)) data = new int[size];

	int n = 0;
	DataInputStream dis = getInStream(offset, size);
	try {
	    switch (dataFormat) {
	    case Fits.BYTE:
		while (n<size) data[n++] = (int) dis.readUnsignedByte();
		break;
	    case Fits.SHORT:
		while (n<size) data[n++] = (int) dis.readShort();
		break;
	    case Fits.INT:
		while (n<size) data[n++] = dis.readInt();
		break;
	    case Fits.FLOAT:
	    case Fits.DOUBLE:
	    default: return data;
	    } 
	} catch (IOException e) {
	    throw new FitsException("Cannot read data", FitsException.DATA);
	}

	if (scaling) {
	  for (int i=0; i<n; i++) data[i] = (int) (scale*data[i]+zero);
	}

	return data;
    }

    /** Read set of data values from the matrix as a float array.  The
     *  values are returned as a float array.
     *
     *  @param  offset   pixel offset within the data matrix
     *  @param  size     no. of pixel values to be read
     *  @param  data     array which will hold the return values.
     *                   If null an array of size is created.
     *  @return          data[] array updated with pixel values
     *  @exception FitsException */
    public float[] getFloatValues(int offset, int size, float data[])
	    throws FitsException {

	if ((offset<0) || (size<1)) return data;
	if (noValues < offset+size) size = (int) (noValues - offset);
	if ((data == null) || (data.length<size)) data = new float[size];

	int n = 0;
	DataInputStream dis = getInStream(offset, size);
	try {
	    switch (dataFormat) {
	    case Fits.BYTE:
		while (n<size) data[n++] = (float) dis.readUnsignedByte();
		break;
	    case Fits.SHORT:
		while (n<size) data[n++] = (float) dis.readShort();
		break;
	    case Fits.INT:
		while (n<size) data[n++] = (float) dis.readInt();
		break;
	    case Fits.FLOAT:
		while (n<size) data[n++] = dis.readFloat();
		break;
	    case Fits.DOUBLE:
		while (n<size) data[n++] = (float) dis.readDouble();
		break;
	    default: return data;
	    } 
	} catch (IOException e) {
	    throw new FitsException("Cannot read data", FitsException.DATA);
	}

	if (scaling) {
	  for (int i=0; i<n; i++) data[i] = (float) (scale*data[i]+zero);
	}

	return data;
    }
    
    
    /** Read set of data values from the matrix as a double array.  The
     *  values are returned as a double array.
     *
     *  @param  offset   pixel offset within the data matrix
     *  @param  size     no. of pixel values to be read
     *  @param  data     array which will hold the return values.
     *                   If null an array of size is created.
     *  @return          data[] array updated with pixel values
     *  @exception FitsException */
    public double[] getDoubleValues(int offset, int size, double data[])
	    throws FitsException {

	if ((offset<0) || (size<1)) return data;
	if (noValues < offset+size) size = (int) (noValues - offset);
	if ((data == null) || (data.length<size)) data = new double[size];

	int n = 0;
	DataInputStream dis = getInStream(offset, size);
	try {
	    switch (dataFormat) {
	    case Fits.BYTE:
		while (n<size) data[n++] = (double) dis.readUnsignedByte();
		break;
	    case Fits.SHORT:
		while (n<size) data[n++] = (double) dis.readShort();
		break;
	    case Fits.INT:
		while (n<size) data[n++] = (double) dis.readInt();
		break;
	    case Fits.FLOAT:
		while (n<size) data[n++] = (double)dis.readFloat();
		break;
	    case Fits.DOUBLE:
		while (n<size) data[n++] =  dis.readDouble();
		break;
	    default: return data;
	    } 
	} catch (IOException e) {
	    throw new FitsException("Cannot read data", FitsException.DATA);
	}

	if (scaling) {
	  for (int i=0; i<n; i++) data[i] = (float) (scale*data[i]+zero);
	}

	return data;
    }

    private DataInputStream getInStream(int offset, int size)
	    throws FitsException {
	DataInputStream dis;
	try {
	    dataFile.seek(dataOffset+offset*bytesPerData);
	    if (noValues < offset + size) {
		size = (int) (noValues - offset);
	    }
	    byte[] dbuf = new byte[size*bytesPerData];
	    dataFile.read(dbuf);
	    dis = new DataInputStream(new ByteArrayInputStream(dbuf));
	} catch (IOException e) {
	    throw new FitsException("Cannot read InStream data",
				    FitsException.DATA);
	}
	return dis;
    }

    /** Store set of data values from a short array into the data matrix.
     *
     *  @param  offset   pixel offset within the data matrix
     *  @param  data     array with values.
     *  @exception FitsException */
    public void setShortValues(int offset, short sdata[])
	    throws FitsException {

	if ((offset<0) || (noValues<=offset))
	    throw new FitsException("Invalid pixel offset",
				    FitsException.DATA);
	if (sdata == null) throw new FitsException("Invalid data array",
						  FitsException.DATA);

	ByteArrayOutputStream baos = new ByteArrayOutputStream(sdata.length * 
							       bytesPerData);
	DataOutputStream dos = new DataOutputStream(baos);

	if (scaling) {
	    throw new FitsException("Scaling of short not supported",
				    FitsException.DATA);
	}

	int n = 0;
	try {
	    switch (dataFormat) {
	    case Fits.BYTE:
		throw new FitsException("Cannot convert data to BYTE",
					FitsException.DATA);
	    case Fits.SHORT:
		while (n<sdata.length) dos.writeShort((short) sdata[n++]);
		break;
	    case Fits.INT:
		while (n<sdata.length) dos.writeInt((int) sdata[n++]);
		break;
	    case Fits.FLOAT:
		while (n<sdata.length) dos.writeFloat((float) sdata[n++]);
		break;
	    case Fits.DOUBLE:
		while (n<sdata.length) dos.writeDouble((double) sdata[n++]);
		break;
	    default: throw new FitsException("Invalid data format",
					     FitsException.DATA);
	    }

	    if (isRAFile) {
		dataFile.seek(dataOffset+offset*bytesPerData);
		dataFile.write(baos.toByteArray());
	    } else {
		byte[] vals = baos.toByteArray();
		n = (int) (dataOffset * bytesPerData);
		for (int i=0; i<vals.length; i++) {
		    dataArray[n++] = vals[i];
		}
	    }
	} catch (IOException e) {
	    throw new FitsException("Cannot convert data", FitsException.DATA);
	}
    }

    /** Store set of data values from an int array into the data matrix.
     *
     *  @param  offset   pixel offset within the data matrix
     *  @param  data     array with data values.
     *  @exception FitsException */
    public void setIntValues(int offset, int idata[])
	    throws FitsException {
	if ((offset<0) || (noValues<=offset))
	    throw new FitsException("Invalid pixel offset",
				    FitsException.DATA);
	if (idata == null) throw new FitsException("Invalid data array",
						  FitsException.DATA);

	ByteArrayOutputStream baos = new ByteArrayOutputStream(idata.length * 
							       bytesPerData);
	DataOutputStream dos = new DataOutputStream(baos);

	if (scaling) {
	    throw new FitsException("Scaling of int not supported",
				    FitsException.DATA);
	}

	int n = 0;
	try {
	    switch (dataFormat) {
	    case Fits.BYTE:
		throw new FitsException("Cannot convert data",
					FitsException.DATA);
	    case Fits.SHORT:
		while (n<idata.length) dos.writeShort((short) idata[n++]);
		break;
	    case Fits.INT:
		while (n<idata.length) dos.writeInt((int) idata[n++]);
		break;
	    case Fits.FLOAT:
		while (n<idata.length) dos.writeFloat((float) idata[n++]);
		break;
	    case Fits.DOUBLE:
		while (n<idata.length) dos.writeDouble((double) idata[n++]);
		break;
	    default: throw new FitsException("Invalid data format",
					     FitsException.DATA);
	    }

	    if (isRAFile) {
		dataFile.seek(dataOffset+offset*bytesPerData);
		dataFile.write(baos.toByteArray());
	    } else {
		byte[] vals = baos.toByteArray();
		n = (int) (dataOffset * bytesPerData);
		for (int i=0; i<vals.length; i++) {
		    dataArray[n++] = vals[i];
		}
	    }
	} catch (IOException e) {
	    throw new FitsException("Cannot write data", FitsException.DATA);
	}
    }

    /** Store set of data values from a float array into the data matrix.
     *
     *  @param  offset   pixel offset within the data matrix
     *  @param  data     array with data values.
     *  @exception FitsException */
    public void setFloatValues(int offset, float data[])
	    throws FitsException {

	if ((offset<0) || (noValues<=offset))
	    throw new FitsException("Invalid pixel offset",
				    FitsException.DATA);
	if (data == null) throw new FitsException("Invalid data array",
						  FitsException.DATA);

	ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length * 
							       bytesPerData);
	DataOutputStream dos = new DataOutputStream(baos);

	if (scaling) {
	  for (int i=0; i<data.length; i++)
	      data[i] = (float) ((data[i]-zero)/scale);
	}

	int n = 0;
	try {
	    switch (dataFormat) {
	    case Fits.BYTE:
		throw new FitsException("Cannot convert data",
					FitsException.DATA);
	    case Fits.SHORT:
		while (n<data.length) dos.writeShort((short) data[n++]);
		break;
	    case Fits.INT:
		while (n<data.length) dos.writeInt((int) data[n++]);
		break;
	    case Fits.FLOAT:
		while (n<data.length) dos.writeFloat((float) data[n++]);
		break;
	    case Fits.DOUBLE:
		while (n<data.length) dos.writeDouble((double) data[n++]);
		break;
	    default: throw new FitsException("Invalid data format",
					     FitsException.DATA);
	    }

	    if (isRAFile) {
		dataFile.seek(dataOffset+offset*bytesPerData);
		dataFile.write(baos.toByteArray());
	    } else {
		byte[] vals = baos.toByteArray();
		n = (int) (dataOffset * bytesPerData);
		for (int i=0; i<vals.length; i++) {
		    dataArray[n++] = vals[i];
		}
	    }
	} catch (IOException e) {
	    throw new FitsException("Cannot write data", FitsException.DATA);
	}
    }
    
    /** Store set of data values from a double array into the data matrix.
    *
    *  @param  offset   pixel offset within the data matrix
    *  @param  data     array with data values.
    *  @exception FitsException */
   public void setDoubleValues(int offset, double data[])
	    throws FitsException {

	if ((offset<0) || (noValues<=offset))
	    throw new FitsException("Invalid pixel offset",
				    FitsException.DATA);
	if (data == null) throw new FitsException("Invalid data array",
						  FitsException.DATA);

	ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length * 
							       bytesPerData);
	DataOutputStream dos = new DataOutputStream(baos);

	if (scaling) {
	  for (int i=0; i<data.length; i++)
	      data[i] = (double) ((data[i]-zero)/scale);
	}

	int n = 0;
	try {
	    switch (dataFormat) {
	    case Fits.BYTE:
		throw new FitsException("Cannot convert data",
					FitsException.DATA);
	    case Fits.SHORT:
		while (n<data.length) dos.writeShort((short) data[n++]);
		break;
	    case Fits.INT:
		while (n<data.length) dos.writeInt((int) data[n++]);
		break;
	    case Fits.FLOAT:
		while (n<data.length) dos.writeFloat((float) data[n++]);
		break;
	    case Fits.DOUBLE:
		while (n<data.length) dos.writeDouble((double) data[n++]);
		break;
	    default: throw new FitsException("Invalid data format",
					     FitsException.DATA);
	    }

	    if (isRAFile) {
		dataFile.seek(dataOffset+offset*bytesPerData);
		dataFile.write(baos.toByteArray());
	    } else {
		byte[] vals = baos.toByteArray();
		n = (int) (dataOffset * bytesPerData);
		for (int i=0; i<vals.length; i++) {
		    dataArray[n++] = vals[i];
		}
	    }
	} catch (IOException e) {
	    throw new FitsException("Cannot write data", FitsException.DATA);
	}
   }

    /** Gets the total number of data values in the data matrix.  */
    public int getNoValues(){
	return noValues;
    }
    
    /** Sets reference pixel for the axes (see CRPIXn). */
    public void setCrpix(double crp[]){
        wcs.crpix = new double[naxis.length];
        for (int n=0; n<naxis.length; n++) {
            wcs.crpix[n] = ((crp!=null) && (n<crp.length)) ? crp[n] : 1.0;
        }
    }
    /** Gets reference pixel for the axes (see CRPIXn). */
    public double[] getCrpix(){
	return wcs.crpix;
    }

    /** Sets coordinate value for the reference pixel of the axes
     *  (see CRVALn). */
    public void setCrval(double crv[]){
        wcs.crval = new double[naxis.length];
        for (int n=0; n<naxis.length; n++) {
            wcs.crval[n] = ((crv!=null) && (n<crv.length)) ? crv[n] : 1.0;
        }
    }

    /** Gets coordinate value for the reference pixel of the axes
     *  (see CRVALn). */
    public double[] getCrval(){
	return wcs.crval;
    }

    /** Sets step size for the axes (see CDELTn). */
    public void setCdelt(double cd[]){
        wcs.cdelt = new double[naxis.length];
        for (int n=0; n<naxis.length; n++) {
            wcs.cdelt[n] = ((cd!=null) && (n<cd.length)) ? cd[n] : 1.0;
        }
    }

    /** Gets step size for the axes (see CDELTn). */
    public double[] getCdelt(){
	return wcs.cdelt;
    }

    /** Gets the WCS object for the image.  */
    public FitsWCS getWCS(){
	return wcs;
    }

    /** Compute World Coordinates from pixel coordinates.
     *
     *  @param  pix  Array with pixel coordinates
     */
    public double[] toWCS(double[] pix) {
	return wcs.toWCS(pix);
    }

    /** Compute pixel coordinates from a set of World Coordinates.
     *
     *  @param  wc  Array with World Coordinates
     */
    public double[] toPixel(double[] wc) {
	return wcs.toPixel(wc);
    }
}


