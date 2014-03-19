/* @(#)FitsHDUnit.java     $Revision: 1.8 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 202 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.io.*;

/** FitsData class represents a FITS data unit
 *
 *  @version $Revision: 1.8 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsHDUnit {

    private FitsHeader header;
    private FitsData data;
    private boolean changeHeader = false;
    private long headerOffset = 0;
    private RandomAccessFile headerFile = null;

    /** Constructor for FitsHDUnit class given a FITS stream and a internal
     *  storage flag.
     *
     *  @param file    DataInput file positioned at the start of the
     *                 header/data unit
     *  @param sflag   Flag for storing data matrix internally
     *  @exception FitsException */
    public FitsHDUnit(DataInput file, boolean sflag) throws FitsException {
	if (file instanceof RandomAccessFile) {
	    headerFile = (RandomAccessFile) file;
	    try {
		headerOffset = headerFile.getFilePointer();
	    } catch (IOException e) {
		throw new FitsException("Cannot read header offset",
					FitsException.FILE);
	    }
	}

	header = new FitsHeader(file);
	int type = header.getType();
	switch (type) {
	case Fits.IMAGE:
	    data = new FitsMatrix(header, file, sflag);
	    break;
	case Fits.BTABLE:
	case Fits.ATABLE:
	    data = new FitsTable(header, file, sflag);
	    break;
	case Fits.RGROUP:
	    data = new FitsRGroup(header, file, sflag);
	    break;
	}
	changeHeader = false;
    }

    /** Constructor from individual header and data objects.
     *
     *  @param header new FitsHeader object to used
     *  @param data   new FitsData object associated to header
     *  @exception FitsException  */
    public FitsHDUnit(FitsHeader header, FitsData data)
	    throws FitsException {
	this.header = header;
	this.data = data;
	changeHeader = true;
    }

    /**  Get the type of Header/Data unit as indicated by its header. */
    public int getType(){
	return header.getType();
    }

    /** Check if HD unit can be save to FITS file in place that is
     *  a FITS file exists and has space enough.
     */
    public boolean canSave() {
	if (changeHeader || (headerFile==null)) {
	    return false;
	}

	int nfill = header.getHeaderSpace() - header.getNoKeywords() - 1;
	if (nfill<0) {
	    return false;
	}
	return true;
    }

    /** Save changes of a HD unit to FITS file.  The HDunit must have
     *  been created from a read/write RandomAccess disk file.
     *  There are no check for the consistence of Header and Data.
     *
     *  @exception IOException, FitsException */
    protected void saveFile(RandomAccessFile file)
	throws IOException, FitsException {
	if (changeHeader) {
	    throw new FitsException("HD unit modified", FitsException.FILE);
	}
	if (headerFile == null) {
	    throw new FitsException("No header file", FitsException.FILE);
	}

	int nfill = header.getHeaderSpace() - header.getNoKeywords() - 1;

	if (nfill<0) {
	    throw new FitsException("No space in FITS header",
				    FitsException.NOHEADERSPACE);
	}

	file.seek(headerOffset);
	file.write((header.toString()).getBytes());  // Write the header
	while (0<nfill--) file.write(Fits.BLANK_CARD.getBytes());
	file.write(Fits.END_CARD.getBytes());
    }

    /** Write FITS header/Data unit to a DataOutput stream.
     *
     *  @param  file  DataOutput object to which to write
     *  @exception IOException, FitsException */
    public void writeFile(DataOutput file) throws IOException, FitsException {
	int nokw = header.getNoKeywords();
	if (nokw<2) {
	    throw new FitsException("Bad FITS header",FitsException.HEADER);
	}

	file.write((header.toString()).getBytes());  // Write the header

	// Add BLANK-cards and the final END-card to fill the FITS record

	int nfill = Fits.NOCARDS*(nokw/Fits.NOCARDS + 1) - nokw - 1;
	while (0<nfill--) file.write(Fits.BLANK_CARD.getBytes());
	file.write(Fits.END_CARD.getBytes());

	data.writeFile(file);
    }

    /** Remove all references to the DataInput file from data unit  */
    public void closeFile() {
	headerFile = null;
	data.closeFile();
    }
  
    /**  Return the FitsData object in the Header/Data unit */
    public FitsData getData(){
	return data;
    }

    /**  Return the header object in the Header/Data unit */
    public FitsHeader getHeader(){
	return header;
    }
}
