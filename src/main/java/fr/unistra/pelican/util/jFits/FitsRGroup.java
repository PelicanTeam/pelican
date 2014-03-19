/* @(#)FitsRGroup.java     $Revision: 1.2 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2000 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.io.*;

/** FitsRGroup class represents a FITS data matrix in the Random Group
 *  format.
 *
 *  @version $Revision: 1.2 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsRGroup extends FitsData {

    private int noValues;
    private int dataFormat = Fits.FLOAT;
    private int bytesPerData = 4;
    private boolean scaling = false;
    private double zero = 0.0;
    private double scale = 1.0;
    private FitsWCS wcs;

    /** Constructor for FitsRGroup class given a FITS prime matrix or
     *  an image extension header with associated data unit as a file.
     *
     *  @param header  FitsHeader object with the image header
     *  @param file    RandomAccess file positioned at the start of the
     *                 associated data unit
     *  @param sflag   Flag for storing data matrix internally
     *  @exception FitsException */
    public FitsRGroup(FitsHeader header, DataInput file, boolean sflag)
	    throws FitsException {
	super(header, file, sflag);

	if (type != Fits.RGROUP) {
	    throw  new FitsException("Wrong header type",
				     FitsException.HEADER);
	}

	int nax = naxis.length;

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

    /** Create and return a minimum FITS header for data Matrix.
     */
    public FitsHeader getHeader() {
        FitsHeader hdr = new FitsHeader();

        hdr.addKeyword(new FitsKeyword("SIMPLE", true,
                                       "Standard FITS format; NOST 100-2.0"));
        hdr.addKeyword(new FitsKeyword("BITPIX", bitpix,
                                       "No. of bits per pixel"));
        hdr.addKeyword(new FitsKeyword("NAXIS", naxis.length+1,
                                       "No. of axes in image"));
        hdr.addKeyword(new FitsKeyword("NAXIS1",0,"Random Groups convension"));
        for (int n=2; n<=naxis.length+1; n++) {
            hdr.addKeyword(new FitsKeyword("NAXIS"+n, naxis[n-2],
                                           "No. of pixels"));
        }
        hdr.addKeyword(new FitsKeyword("PCOUNT", noParm, "Parameter count"));
        hdr.addKeyword(new FitsKeyword("GCOUNT", noGroup, "Group count"));
        hdr.addKeyword(new FitsKeyword("GROUPS", true,
				       "Random Groups format"));


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
	DataInputStream dis = getStream(offset, size);
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
    public int[] getFloatValues(int offset, int size, int data[])
	    throws FitsException {

	if ((offset<0) || (size<1)) return data;
	if (noValues < offset+size) size = (int) (noValues - offset);
	if ((data == null) || (data.length<size)) data = new int[size];

	int n = 0;
	DataInputStream dis = getStream(offset, size);
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
	    throw new FitsException("Cannot convert data", FitsException.DATA);
	}

	if (scaling) {
	  for (int i=0; i<n; i++) data[i] = (int) (scale*data[i]+zero);
	}

	return data;
    }

    /** Read set of data values from the matrix as a float array.  The
     *  values are returned as a float array.
     *
     *  @param  offset   pixel offset within hte data matrix
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
	DataInputStream dis = getStream(offset, size);
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
	    throw new FitsException("Cannot convert data", FitsException.DATA);
	}

	if (scaling) {
	  for (int i=0; i<n; i++) data[i] = (float) (scale*data[i]+zero);
	}

	return data;
    }

    private DataInputStream getStream(int offset, int size)
	    throws FitsException {
	DataInputStream di;
	try {
	    dataFile.seek(dataOffset+offset*bytesPerData);
	    if (noValues < offset + size) {
		size = (int) (noValues - offset);
	    }
	    byte[] dbuf = new byte[size*bytesPerData];
	    dataFile.read(dbuf);
	    di = new DataInputStream(new ByteArrayInputStream(dbuf));
	} catch (IOException e) {
	    throw new FitsException("Cannot read data", FitsException.DATA);
	}
	return di;
    }


    /** Gets the total number of data values in the data matrix.  */
    public int getNoValues(){
	return noValues;
    }
    
    /** Gets reference pixel for the axes (see CRPIXn). */
    public double[] getCrpix(){
	return wcs.crpix;
    }

    /** Gets coordinate value for the reference pixel of the axes
     *  (see CRVALn). */
    public double[] getCrval(){
	return wcs.crval;
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


