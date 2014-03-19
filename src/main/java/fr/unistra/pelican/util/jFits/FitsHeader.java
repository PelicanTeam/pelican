/* @(#)FitsHeader.java     $Revision: 1.12 $ $Date: 2005/09/16 09:58:10 $
 *
 * Copyright (C) 2002 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.util.*;
import java.io.*;

/** FITS Header class which is an ordered set of FitsKeywords.
 *
 *  @version $Revision: 1.12 $ $Date: 2005/09/16 09:58:10 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsHeader {

    private Vector    keywords = null;
    private Hashtable kwHash;
    private Hashtable commentHash;
    private int headerSpace = 0;

    /** Default constructor for empty FitsHeader class  */
    public FitsHeader() {
	keywords = new Vector(Fits.NOCARDS, Fits.NOCARDS);
	kwHash = new Hashtable();
	commentHash = new Hashtable();
    }

    /** Constructor for FitsHeader class given a DataInput file
     *  positioned at the FITS header.
     *
     *  @param file    RandomAccess file positioned at the start of a
     *                 FITS header
     *  @exception FitsException */
    public FitsHeader(DataInput file) throws FitsException {
	byte record[] = new byte[Fits.RECORD];
	byte line[] = new byte[Fits.CARD];
	FitsKeyword  kw;

	try {                                       // check if FITS format
	    file.readFully(record, 0, Fits.RECORD);
	    kw = new FitsKeyword(record);
	    if (!kw.getName().equals("SIMPLE") &&
		!kw.getName().equals("XTENSION")) {
		throw new FitsException("Not Standard records",
					FitsException.HEADER);
	    }
	} catch (IOException e) {
	    throw new FitsException("No more data", FitsException.HEADER);
	} catch (FitsException e) {
	    throw new FitsException("", FitsException.HEADER);
	}

	int  no_kw = 1;
	keywords = new Vector(Fits.NOCARDS, Fits.NOCARDS);
	no_kw = 0;
	try {
	    int n = 0;
	    while (true) {
		for (int k=0; k<Fits.CARD; k++) {
		    line[k] = record[n++];
		}
		kw = new FitsKeyword(line);
		keywords.setSize(no_kw++);
		keywords.addElement(kw);
		if (Fits.RECORD <= n) {
		    file.readFully(record, 0, Fits.RECORD);
		    n = 0;
		}
	    }
	} catch (IOException e) {
	    throw new FitsException("Cannot read header",
				    FitsException.HEADER);
	} catch (FitsException e) {
	    if (e.getType() != FitsException.ENDCARD) {
		throw new FitsException("Bad FITS keyword:"+e,
					FitsException.HEADER);
	    }
	}

	int idx = keywords.size() - 1;   // remove trailing empty keywords
	while (((FitsKeyword)keywords.elementAt(idx)).isEmpty()) {
	    keywords.removeElementAt(idx--);
	}
	keywords.trimToSize();

	headerSpace = (1+no_kw/Fits.NOCARDS)*Fits.NOCARDS;
	kwHash = new Hashtable(keywords.size());
	commentHash = new Hashtable();

	Enumeration  list = keywords.elements();
	while (list.hasMoreElements()) {
	    kw = (FitsKeyword) list.nextElement();
	    hashKeyword(kw);
	}
    }

  /** Append FITS keyword to the end of the header.
   *
   *  @param kw  FitsKeyword to be appended */
    public void addKeyword(FitsKeyword kw){
	if (kw == null) return;
	keywords.addElement(kw);
	hashKeyword(kw);
    }

    /** Insert FITS keyword at a given position in the header.
     *
     *  @param kw FitsKeyword to be appended
     *  @param index place where the keyword should be inserted */
    public void insertKeywordAt(FitsKeyword kw, int index){
	if (kw == null) {
	    return;
	}
	keywords.insertElementAt(kw, index);
	hashKeyword(kw);
    }

    /** Remove FITS keyword at a given position in the header.
     *
     *  @param index location of keyword which should be removed */
    public void removeKeywordAt(int index) {
	FitsKeyword kw = (FitsKeyword) keywords.elementAt(index);
	if (kw==null) {
	    return;
	}
	kwHash.remove(kw.getName());
	keywords.removeElementAt(index);
    }

    /** Add keyword to internal hash tables.
     *
     *  @param kw  FitsKeyword to be hashed */
    private void hashKeyword(FitsKeyword kw){
	if (kw == null) {
	    return;
	}
	if (kw.getType() == FitsKeyword.COMMENT) {
	    Vector vec = (Vector) commentHash.get(kw.getName());
	    if (vec == null) {
		vec = new Vector();
		commentHash.put(kw.getName(), vec);
	    }
	    vec.addElement(kw);
	} else {
	    kwHash.put(kw.getName(), kw);
	}
    }

    /** Return the type of the FITS header e.g. Fits.IMAGE or Fits.BTABLE. */
    final public int getType(){
	if (keywords.size()<3) {
	    return Fits.FALSE;
	}

	int type = Fits.FALSE;
	FitsKeyword kw = (FitsKeyword) keywords.elementAt(0);
	if (kw.getName().equals("SIMPLE") && kw.getBool()) {
	    kw = (FitsKeyword) kwHash.get("NAXIS1");
	    if ((kw != null) && (kw.getInt() == 0)) {
		kw = (FitsKeyword) kwHash.get("GROUPS");
		type = (((kw != null) && kw.getBool()))
		    ? Fits.RGROUP : Fits.IMAGE;
	    } else {
		type = Fits.IMAGE;
	    }
	} else if (kw.getName().equals("XTENSION")) {
	    if (kw.getString().startsWith("IMAGE")) {
		type = Fits.IMAGE;
	    } else if (kw.getString().startsWith("BINTABLE")) {
		type = Fits.BTABLE;
	    } else if (kw.getString().startsWith("TABLE")) {
		type = Fits.ATABLE;
	    } else {
		type = Fits.UNKNOWN;
	    }
	}
	return type;
    }

    /** Compute size of FITS data matrix in bytes */
    final public long getDataSize(){
	if (kwHash == null) {
	    return 0;
	}
	int type = getType();
	FitsKeyword kw;

	kw = (FitsKeyword) kwHash.get("NAXIS");  // get no. of axes
	long naxis = kw.getInt();
	if (naxis < 1) {
	    return 0;
	}

	kw = (FitsKeyword) kwHash.get("BITPIX"); // get no. of bytes per value
	long n_byte = Math.abs(kw.getInt())/8;

	long  d_size = 1;
	for (int n=1; n<=naxis; n++) {
	    if (type==Fits.RGROUP && n==1) continue;
	    kw = (FitsKeyword) kwHash.get("NAXIS" + n);
	    d_size *= kw.getInt();
	}

	kw = (FitsKeyword) kwHash.get("PCOUNT"); // add parameter block size
	if (kw != null) {
	    d_size += kw.getInt();
	}
	kw = (FitsKeyword) kwHash.get("GCOUNT"); // multiple with group count
	if (kw != null) {
	    d_size *= kw.getInt();
	}
	d_size *= n_byte;

	return d_size;
    }

    /** Get name of FITS HUunit as given by the EXTNAME keyword.  If
     *  this keyword is not present in the header, 'NONE' is returned.  */
    final public String getName(){
	FitsKeyword kw = (FitsKeyword) kwHash.get("EXTNAME");
	if ((kw == null) || (kw.getType() != FitsKeyword.STRING)) {
	    return "NONE";
	}
      return kw.getString();
  }

    /** Get version of FITS HUunit as given by the EXTVER keyword.  If
     *  this keyword is not present in the header 1 is returned.  */
    final public int getVersion(){
	FitsKeyword kw = (FitsKeyword) kwHash.get("EXTVER");
	if ((kw == null) || (kw.getType() != FitsKeyword.INTEGER)) {
	    return 1;
	}
	return (int) kw.getInt();
    }

    /** Obtain the total number of keywords in the header.  */
    final public int getNoKeywords(){
	return keywords.size();
    }

    /** Return a keyword giving its relative position.  If the position
     *  is not in the valid range a NULL is returned.
     *
     *  @param  no  position of keyword in header (starting with 0) */
    final public FitsKeyword getKeyword(int no){
	if ((no < 0) || (keywords.size() <= no)) {
	    return (FitsKeyword) null;
	}
	return (FitsKeyword) keywords.elementAt(no);
    }

    /** Return a keyword giving its name.  If there a multiple keywords,
     *  the last is returned.  In cases where multiple keywords makes
     *  sense (e.g. comments), the first of this set is given.  If none
     *  is found, a NULL is returned.
     *
     *  @param  name  string with name of keyword */
    final public FitsKeyword getKeyword(String name){
	FitsKeyword kw = (FitsKeyword) kwHash.get(name);
	if (kw == null) {
	    Vector vec = (Vector) commentHash.get(name);
	    if (vec != null) {
		kw = (FitsKeyword) vec.firstElement();
	    }
	}
	return kw;
    }

    /** Return an array of keywords giving a name.  If none
     *  is found, a NULL is returned.
     *
     *  @param  name  string with name of keyword */
    final public FitsKeyword[] getKeywords(String name){
	FitsKeyword kw = (FitsKeyword) kwHash.get(name);
	if (kw == null) {
	    Vector vec = (Vector) commentHash.get(name);
	    if (vec == null) {
		return null;
	    }
	    FitsKeyword setKw[] = new FitsKeyword[vec.size()];
	    for (int n=0; n<vec.size(); n++) {
		setKw[n] = (FitsKeyword) vec.elementAt(n);
	    }
	    return setKw;     
	}
	FitsKeyword setKw[] = new FitsKeyword[1];
	setKw[0] = kw;
	return setKw;
    }

    /** Obtain an Enumeration object for the keywords in the header.  */
    final public Enumeration getKeywords(){
	return keywords.elements();
    }

    /** Generate a string with the FITS header.  The header string will
     *  include the END-card and NOT be space filled with empty cards to
     *  a full FITS 2880 char record. */
    public String toString(){
	int size = keywords.size();
	StringBuffer  hd = new StringBuffer(Fits.CARD*size);

	Enumeration itr = keywords.elements();
	while (itr.hasMoreElements()) {
	    FitsKeyword kw = (FitsKeyword) itr.nextElement();
	    hd.append(kw.toString());
	}

	return hd.toString();
    }

    /** Get space in FITS header read from file in terms of number of
     *  keyword cards to can contain. */
    public int getHeaderSpace(){
	return headerSpace;
    }

    /** Sets the first keyword in the header to XTENSION with the
     *  type indicated.  Nothing will be done if a non-standard
     *  extension is specified.
     *
     *  @param  type  Type of FITS extension
     */
    public void setExtension(int type) {
	FitsKeyword kw;

	switch (type) {
	    case Fits.IMAGE :
		kw = new FitsKeyword("XTENSION", "IMAGE", "Image extension");
		break;
	    case Fits.BTABLE :
		kw = new FitsKeyword("XTENSION", "BINTABLE",
				     "Binary table extension");
		break;
	    case Fits.ATABLE :
		kw = new FitsKeyword("XTENSION", "TABLE",
				     "ASCII table extension");
		break;
	    default: return;
	}
	removeKeywordAt(0);
	insertKeywordAt(kw, 0);
    }
}
