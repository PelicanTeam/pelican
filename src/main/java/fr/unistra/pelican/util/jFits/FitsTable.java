/* @(#)FitsTable.java     $Revision: 1.8 $    $Date: 2005/09/16 09:58:10 $
 *
 * Copyright (C) 2002 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.io.*;
import java.util.*;

/** FitsTable class represents a FITS table extension in either ASCII
 *  or BINARY table format.  It is a collection of FitsColumn object
 *  giving acess to the table data.
 *
 *  @version $Revision: 1.8 $ $Date: 2005/09/16 09:58:10 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsTable extends FitsData {

    private Vector columns;
    private int noRows;

    /** Constructor for FitsTable class given a FITS table extension
     *  header with associated data unit as a file.
     *
     *  @param header  FitsHeader object with the table extension header
     *  @param file    RandomAccess file positioned at the start of the
     *                 associated data unit
     *  @param sflag   Flag for storing data matrix internally
     *  @exception FitsException */
    public FitsTable(FitsHeader header, DataInput file, boolean sflag)
	    throws FitsException {
	super(header, file, sflag);

	if ((bitpix != 8) || (noParm < 0) || (noGroup != 1)) {
	    throw new FitsException("Incompatible TABLE header",
				    FitsException.HEADER);
	}

	FitsKeyword kw = header.getKeyword("TFIELDS");
	if (kw == null) {
	    throw new FitsException("Missing TFIELDS keyword",
				    FitsException.HEADER);
	}
	int ncol = (int) kw.getInt();
	columns = new Vector(ncol);
	int recordSize = naxis[0];
	noRows = naxis[1];

	kw = header.getKeyword("THEAP");
	long heapOffset = (kw == null) ? 0 : kw.getInt();

	int npos = 0;
	long   tnull;
	double tzero, tscale;
	String tform, ttype, tunit, tdisp, tdim;
	FitsColumn column;

	for (int n=1; n<=ncol; n++) {
	    kw = header.getKeyword("TFORM"+n);
	    if (kw == null) { 
		throw new FitsException("Missing TFORMn keyword",
					FitsException.HEADER);
	    }
	    tform = kw.getString();
	    if (type == Fits.ATABLE) {
		kw = header.getKeyword("TBCOL"+n);
		if (kw == null) { 
		    throw new FitsException("Missing TBCOLn keyword",
					    FitsException.HEADER);
		}
		npos = (int) kw.getInt();
	    }
	    kw = header.getKeyword("TTYPE"+n);
	    ttype = (kw == null) ? "Label"+n : kw.getString();

	    // create the Column

	    column = new FitsColumn(type, tform, ttype, noRows);
	    if (isRAFile) {
		column.setData(dataFile, dataOffset, npos, recordSize);
	    } else if (dataArray != null) {
		column.setData(dataArray, npos, recordSize);
	    }

	    if (type == Fits.BTABLE) {
		npos += column.getWidth();
	    }

	    kw = header.getKeyword("TZERO"+n);
	    if (kw != null) {
		column.setZero(kw.getReal());
	    }

	    kw = header.getKeyword("TSCALE"+n);
	    if (kw != null) {
		column.setScale(kw.getReal());
	    }

	    kw = header.getKeyword("TNULL"+n);
	    if (kw != null) {
		if (type == Fits.BTABLE) {
		    column.setNull(kw.getInt());
		} else {
		    column.setNull(kw.getString());
		}
	    }

	    kw = header.getKeyword("TUNIT"+n);
	    if (kw != null) {
		column.setUnit(kw.getString());
	    }

	    kw = header.getKeyword("TDISP"+n);
	    if (kw != null) {
		column.setDisplay(kw.getString());
	    }

	    kw = header.getKeyword("TDIM"+n);
	    if (kw != null) {
		column.setDim(kw.getString());
	    }

	    columns.addElement(column);
	}
    }

    /** Create and return a minimum FITS header for data Matrix.
     */
    public FitsHeader getHeader() {
        FitsHeader hdr = new FitsHeader();

        hdr.addKeyword(new FitsKeyword("XTENSION", "BINTABLE",
                                       "Bibary table extension"));
        hdr.addKeyword(new FitsKeyword("BITPIX", 8,
                                       "No. of bits per pixel"));
        hdr.addKeyword(new FitsKeyword("NAXIS", 2,
                                       "No. of axes in image"));
	int nb = 0;
	for (int n=0; n<columns.size(); n++);


        hdr.addKeyword(new FitsKeyword("NAXIS1", nb,
                                       "No. of bytes in row"));
        hdr.addKeyword(new FitsKeyword("NAXIS2", getNoRows(),
                                       "No. of rows in table"));
        hdr.addKeyword(new FitsKeyword("PCOUNT", noParm, "Size of heap area"));
        hdr.addKeyword(new FitsKeyword("GCOUNT", noGroup, "Group count"));
        hdr.addKeyword(new FitsKeyword("TFIELDS", getNoColumns(),
                                       "No. of columns in table"));

	return hdr;
    }

    /** Add column to table.
     *
     *  @param  column  FitsColumn to be appended to the table */
    public void addColumn(FitsColumn column) {
	columns.addElement(column);
    }

    /** Insert column in table at specified position.
     *
     *  @param  column  FitsColumn to be inserted into the table
     *  @param  index   position where to insert column */
    public void insertColumnAt(FitsColumn column, int index) {
	columns.insertElementAt(column, index);
    }

    /** Remove column from table.
     *
     *  @param  index  position of column to be removed */
    public void removeColumnAt(int index) {
	columns.removeElementAt(index);
    }

    /** Get column with a given index in the table.
     *
     *  @param index  position of column in table */
    public FitsColumn getColumn(int index){
	if ((index < 0) || (columns.size() <= index)) {
	    return null;
	}
	Enumeration itr = columns.elements();
	while (0<index--) itr.nextElement();
	return (FitsColumn) itr.nextElement();
    }

    /** Get column with a given label.  The first column found with 
     *  the label is returned.  If none is found a NULL is returned.
     *
     *  @param label  string with column label */
    public FitsColumn getColumn(String label){
	FitsColumn col;
	Enumeration itr = columns.elements();
	while (itr.hasMoreElements()) {
	    col = (FitsColumn) itr.nextElement();
	    if (label.equalsIgnoreCase(col.getLabel())) {
		return col;
	    }
	}
	return null;
    }

    /** Retrieve number of columns in current table. */
    public int getNoColumns(){
	return columns.size();
    }

    /** Get total number of rows defined for the table (see NAXIS2). */
    public int getNoRows() {
	return noRows;
    }
}


