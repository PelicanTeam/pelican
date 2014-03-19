/*
 * @(#)FitsTform.java     $Revision: 1.5 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 1999 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.util.*;

/** FitsTform class decodes information in value fields of FITS
 *  table TFORM and TDISP keywords.
 *
 *  @version $Revision: 1.5 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol ESO, <pgrosbol@eso.org>
 */
public class FitsTform {

    private char dataType = '\0';
    private String format;
    private char eFormat = '\0';
    private int repeat = 1;
    private int width = 0;
    private int decimals = 0;
    private int exponent = 0;
    private String additional = "";

    /** Constructur for FitsTform class from a FITS table format string.
     *
     *  @param form String with TFORM or TDISP format
     *  @exception FitsException  */
    public FitsTform(String form) throws FitsException {
	char   ch;
	String tok = "";

	format = form.toUpperCase();
	StringTokenizer stok = new StringTokenizer(format,
						   "LXBIJAFEDCMP.OZNSG",
						   true);

	if (stok.hasMoreTokens()) {
	    tok = stok.nextToken();
	    if (Character.isDigit(tok.charAt(0))) {  // decode format repeat
		try {
		    repeat = (Integer.valueOf(tok)).intValue();
		} catch (NumberFormatException e) {
		    throw new FitsException("Wrong TFORM format",
					    FitsException.HEADER);
		}
		if (stok.hasMoreTokens()) {
		    tok = stok.nextToken();
		}
	    }
	    ch = tok.charAt(0);                      // get format data type
	    if (Character.isLetter(ch)) {
		this.dataType = ch;
	    } else {
		throw new FitsException("Wrong TFORM format",
					FitsException.HEADER);
	    }
	}

	// save additional information after data type character
	int idx = format.indexOf(this.dataType) + 1;
	if (idx<format.length()) {
	    additional = format.substring(idx);
	}

	if (dataType != 'P') {         // find and decode width etc.
	    while (stok.hasMoreTokens()) {
		tok = stok.nextToken();
		ch = tok.charAt(0);
		if (Character.isDigit(ch)) break;
		if (eFormat == '\0') {
		    eFormat = ch;
		}
	    }
	    if (Character.isDigit(tok.charAt(0))) {
		try {
		    width = (Integer.valueOf(tok)).intValue();
		} catch (NumberFormatException e) {
		    throw new FitsException("Wrong TFORM format",
					    FitsException.HEADER);
		}
	    }
	    else {
		switch (dataType) {
		case 'L' :
		case 'B' :
		case 'A' :
		    width = 1;
		    break;
		case 'X' :
		    width = (0<repeat) ? (repeat-1)%8 + 1 : 0;
		    break;
		case 'I' :
		    width = 2;
		    break;
		case 'J' :
		case 'E' :
		    width = 4;
		    break;
		case 'D' :
		case 'C' :
		case 'P' :
		    width = 8;
		    break;
		case 'M' :
		    width = 16;
		    break;
		default  :
		    throw new FitsException("Wrong TFORM format",
						   FitsException.HEADER);
		}
	    }
	    if (stok.hasMoreTokens()) {         // get no. of dicimals
		tok = stok.nextToken();
		if (tok.charAt(0) == '.') {
		    if (stok.hasMoreTokens()) tok = stok.nextToken();
		    try {
			decimals = (Integer.valueOf(tok)).intValue();
		    } catch (NumberFormatException e) {
			throw new FitsException("Wrong TFORM format",
						FitsException.HEADER);
		    }
		} else {
		    throw new FitsException("Wrong TFORM format",
					    FitsException.HEADER);
		}

		if (stok.hasMoreTokens()) {          // get size of exponent
		    tok = stok.nextToken();
		    if (tok.charAt(0) == 'E') {
			if (stok.hasMoreTokens()) tok = stok.nextToken();
			try {
			    decimals = (Integer.valueOf(tok)).intValue();
			} catch (NumberFormatException e) {
			    throw new FitsException("Wrong TFORM format",
						    FitsException.HEADER);
			}
		    } else {
			throw new FitsException("Wrong TFORM format",
						 FitsException.HEADER);
		    }
		}
	    }
	}
    }

    /** Get method to retrieve the original format string. */
    public String getFormat(){
	return format;
    }

    /** Get method to obtain the data type indicated by the format. */
    public char getDataType(){
	return this.dataType;
    }

    /** Get method to give the extended E-format display format.
     *  'E' indicates engineering format while 'S' is scientific.
     *  If none is given a null character is returned. */
    public char getEFormat(){
	return this.eFormat;
    }

    /** Get method to obtain the repeat factor of the format for
     *  Binary Tables (by default it is 1). */
    public int getRepeat(){
	return repeat;
    }

    /** Get method to retrieve the field width of a single data value
     *  in bytes. */
    public int getWidth(){
	return width;
    }

    /** Get method to give the number of decimals for display formats.
     *  For B/O/X display formats it gives maximum number. */
    public int getDecimals(){
	return decimals;
    }

    /** Get method to obtain the number of chararters to be displayd
     *  in an exponential display format. */
    public int getExponent(){
	return exponent;
    }

    /** Get method to retreive the additional information string.
     *  This  may be appended the prime data type in Binary Table
     *  TFORM keywords e.g. P type. */
    public String getAdditional(){
	return additional;
    }
}




