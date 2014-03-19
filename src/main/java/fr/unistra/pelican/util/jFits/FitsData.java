/* @(#)FitsData.java     $Revision: 1.8 $ $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2002 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.io.*;

/** FitsData class represents a FITS data unit
 *
 *  @version $Revision: 1.8 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsData {

    protected int type;
    protected int[] naxis;
    protected long size;
    protected int bitpix = 0;
    protected int noParm = 0;
    protected int noGroup = 1;
    protected boolean changeData = false;

    protected RandomAccessFile dataFile = null;
    protected long dataOffset = 0;
    protected byte[] dataArray = null;
    protected boolean isRAFile = false;

    /** Constructor for FitsData class given a FITS header with 
     *  associated data unit as a file.
     *
     *  @param header  FitsHeader object with the image header
     *  @param file    RandomAccess file positioned at the start of the
     *                 associated data unit
     *  @param sflag   Flag for storing data matrix internally
     *  @exception FitsException
     */
    public FitsData(FitsHeader header, DataInput file, boolean sflag)
	throws FitsException {

	if (file instanceof RandomAccessFile) {
	    dataFile = (RandomAccessFile) file;
	    try {
		dataOffset = dataFile.getFilePointer();
	    } catch (IOException e) {
		throw new FitsException("Cannot read data offset",
					FitsException.FILE);
	    }
	    isRAFile = true;
	}

	size = header.getDataSize();
	type = header.getType();
	long skip = size;
	if (size%Fits.RECORD != 0) {
	    skip = (size/Fits.RECORD+1)*Fits.RECORD;
	}

	try {
	    if (sflag && !isRAFile) {
		dataArray = new byte[(int) skip];
		file.readFully(dataArray);
	    } else {
		file.skipBytes((int) skip);
	    }
	} catch (IOException e) {
	    throw new FitsException("Cannot read/skip over data matrix",
				    FitsException.FILE);
	}

	decodeBasicHeader(header);
    }

    /** Constructor for FitsData class given the size of the data matrix.
     *  An array equal to the size of the data matrix will be allocated.
     *
     *  @param bitpix  value of FITS BITPIX keyword bits/pixel
     *  @param nax     Integer array defining the dimensions of the
     *                 data matrix or for BINTABLE the heap size
     *  @exception FitsException
     */
    public FitsData(int bitpix, int nax[]) 
	throws FitsException {
	switch (bitpix) {
	    case Fits.BYTE :
	    case Fits.SHORT :
	    case Fits.INT :
	    case Fits.FLOAT :
	    case Fits.DOUBLE : this.bitpix = bitpix; break;
	    default: throw new FitsException("Invalid BITPIX value",
					     FitsException.DATA);
	}
	type = Fits.UNKNOWN;
	size = 1;
	naxis = new int[nax.length];
	for (int n=0; n<nax.length; n++) {
	    naxis[n] = nax[n];
	    size *= naxis[n];
	}
	size *= Math.abs(this.bitpix)/8;
	if (size < 0) throw new FitsException("Data size less than zero",
					      FitsException.DATA);

	long  skip = size;
	if (size%Fits.RECORD != 0) {
	    skip = (size/Fits.RECORD+1)*Fits.RECORD;
	}
	
	dataArray = new byte[(int) skip];
    }

    /** Decodes basic header information for data matrix.
     *
     *  @param  header  FITS header
     */
    private void decodeBasicHeader(FitsHeader header) throws FitsException {

	FitsKeyword kw = header.getKeyword("NAXIS");
	if (kw == null) {
	    throw new FitsException("Missing NAXIS keyword",
				    FitsException.HEADER);
	}
	int nax = kw.getInt();
	naxis = new int[nax];
	kw = header.getKeyword("BITPIX");
	if (kw == null) {
	    throw new FitsException("Missing BITPIX keyword",
				    FitsException.HEADER);
	}
	bitpix = kw.getInt();

	for (int n=1; n<=nax; n++) {
	    kw = header.getKeyword("NAXIS"+n);
	    if (kw == null) {
		throw new FitsException("Missing NAXISn keyword",
					FitsException.HEADER);
	    }
	    naxis[n-1] = kw.getInt();
	}

	kw = header.getKeyword("GCOUNT");
	noGroup = (kw == null) ? 1 : kw.getInt();
	kw = header.getKeyword("PCOUNT");
	noParm = (kw == null) ? 0 : kw.getInt();
	changeData = false;
    }

    /** Create and return a minimum FITS header for data Matrix.
     */
    public FitsHeader getHeader() {
	FitsHeader hdr = new FitsHeader();

	hdr.addKeyword(new FitsKeyword("SIMPLE", true,
				       "Standard FITS format; NOST 100-2.0"));
	hdr.addKeyword(new FitsKeyword("BITPIX", bitpix,
				       "No. of bits per pixel"));
	hdr.addKeyword(new FitsKeyword("NAXIS", naxis.length,
				       "No. of axes in image"));
	for (int n=1; n<=naxis.length; n++) {
	    hdr.addKeyword(new FitsKeyword("NAXIS"+n, naxis[n-1],
					   "No. of pixels"));
	}
	hdr.addKeyword(new FitsKeyword("PCOUNT", 0, "Parameter count"));
	hdr.addKeyword(new FitsKeyword("GCOUNT", 1, "Groupe count"));

	return hdr;
    }

    /** Write data martix to DataOutput stream.
     *
     *  @param  file  DataOutput stream to which data are written
     *  @exception  IOException, FitsException  */
    public void writeFile(DataOutput file) throws IOException, FitsException {
	byte[] buf;
	int nbytes = 0;
	int block = 10*Fits.RECORD;

	if (isRAFile) {
	    buf = new byte[block];
	    dataFile.seek(dataOffset);
	    int  nrec = (int) size/block;    // write large block first
	    while (0<nrec--) {
		dataFile.read(buf);
		file.write(buf);
	    }

	    nbytes = (int) size%block;      // and then the remaining blocks
	    if (nbytes == 0) return;

	    buf = new byte[nbytes];
	    dataFile.read(buf);
	    file.write(buf);
	} else if (dataArray != null) {
	    file.write(dataArray);
	}

	if (nbytes%Fits.RECORD != 0) {      // finally fill with zeros
	    nbytes = Fits.RECORD*(nbytes/Fits.RECORD + 1) - nbytes;
	    buf = new byte[nbytes];
	    if (type == Fits.ATABLE) {     // or if ASCII table the space fill
		for (int n=0; n<nbytes; n++) buf[n] = 0x20;
	    }
	    file.write(buf);
	}
    }

    /** Closes the associated data file */
    public void closeFile(){
	dataFile = null;
	dataOffset = 0;
	size = 0;
	type = 0;
    }

    /** Retrives number of axes defined for the data unit (ref. NAXIS) */
    public int getNoAxes(){
	return naxis.length;
    }

    /** Gets FITS type of data unit. This is specified in the
     *  assocated header such as Fits.IMAGE or Fits.BTABLE */
    public int getType(){
	return type;
    }

    /** Gets the dimentions of the axes.  This is defined for the data
     *  unit by the NAXISn keywords. */
    public int[] getNaxis(){
	return naxis;
    }
}


