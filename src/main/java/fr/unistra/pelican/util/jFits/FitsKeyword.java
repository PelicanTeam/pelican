/*
 * @(#)FitsKeyword.java     $Revision: 1.14 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2000 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.text.*;
import java.util.*;

/** FitsKeyword class describes a single FITS header keyword as
 *  defined by the FITS standard (ref. NOST-1.2).  The implementation
 *  also support the hierarchical keyword convension as defined by 
 *  the ESO Data Interface Control Board.  The name of a keyword
 *  is converted to uppercase and different hierarchical levels are
 *  separated by '.' i.e. the keyword 'HIERARCH ESO TEL NAME =' will
 *  get the name 'ESO.TEL.NAME'.
 *
 *  @version $Revision: 1.14 $  $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsKeyword {

    // Definition of FITS keyword types
    public final static int NONE = 0;
    public final static int COMMENT = 1;
    public final static int STRING = 2;
    public final static int BOOLEAN = 3;
    public final static int INTEGER = 4;
    public final static int REAL = 5;
    public final static int DATE = 6;

    private final byte NULL       = 0x00;
    private final byte SPACE      = 0x20;
    private final byte COMMA      = 0x2C;
    private final byte QUOTE      = 0x27;
    private final byte SLASH      = 0x2F;
    private final byte EQUAL      = 0x3D;
    private final byte MINUS      = 0x2D;
    private final byte UNDERSCORE = 0x5F;
    private final byte A          = 0x41;
    private final byte Z          = 0x5A;

    private String  name;
    private int     type = NONE;
    private String  kwCard;               // Original FITS keyword string
    private boolean validCard = false;    // Is kwCard value valid ?
    private Object  value;                // Value of keyword
    private String  comment;              // Comment field of keyword
    private boolean valueTruncated = false;

    private final static SimpleDateFormat ISOLONG =
	new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final static SimpleDateFormat ISOSHORT =
	new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat FITSDATE =
	new SimpleDateFormat("dd/MM/yy");
    private final static TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");

    /** Constructor for FitsKeyword class from a 80 character byte array.
     *
     *  @param card  byte array with 80 characters FITS header card
     *  @exception FitsException */
    public FitsKeyword(byte[] card) throws FitsException {
	int idx = 0;                         // index in FITS card
	int idx_last = 0;                    // last index of value field
	int idx_comm_first = 0;              // first index of comment field

	// if card is null or too short - add spaces
	if (card == null || card.length < Fits.CARD) {
	    byte[] pc = new byte[Fits.CARD];
	    int n = 0;
	    while (n < card.length) {
		pc[n] = card[n];
		n++;
	    }
	    while (n < Fits.CARD) pc[n++] = SPACE;
	    card = pc;
	}

	/*
	 * B. Perret: ok we sometimes need to be meticulous but crashing about a keyword problem is not usefull.
	 * Most of the time it will have no impact... and if it has we can always throw an exception later if really needed.
	 * 
	 */
	/*if ((card[0] != SPACE) && (card[0] != MINUS) && (card[0] != UNDERSCORE)
	       && ((card[0] < A) || (Z < card[0]))) { 
	    throw new FitsException("Illegal character",
				    FitsException.KEYWORD);    
	}*/

	kwCard = new String(card, 0, Fits.CARD);       // save keyword card
	validCard = true;
	name = kwCard.substring(0, 8);             // get prime keyword name
	String valueField = null;
	comment = null;

	if (name.startsWith("END     ")) {         // check if END card
	    throw new FitsException("END card", FitsException.ENDCARD);
	}

	if (name.startsWith("HISTORY ")        // Comment keyword
	       || name.startsWith("COMMENT ")
	       || name.startsWith("        ")) {
	    type = COMMENT;
	    idx_comm_first = 8;
	} else if (name.startsWith("HIERARCH")) {   // Hierarchical keyword
	    StringBuffer hkw = new StringBuffer(Fits.CARD);
	    boolean found = false;
	    byte last = NULL;
	    idx = 8;
	    while ((idx<Fits.CARD) && (card[idx]!=EQUAL)) {
		if (card[idx] != SPACE) {
		    if (last == SPACE && found) {
			hkw.append('.');
		    }
		    found = true;
		    hkw.append((char) card[idx]);
		}
		last = card[idx++];
	    }
	    if (Fits.CARD <= idx) {
		throw new FitsException("No equal-sign in HIERARCH keyword",
					FitsException.KEYWORD);
	    }
	    name = hkw.toString();
	} else if (card[8]==EQUAL) {             // Prim keyword with value
	    idx = 8;
	} else {                                // Comment keyword
	    type = COMMENT;
	    idx_comm_first = 8;
	}

	name = (name.trim()).toUpperCase();     // Force to uppercase

	if (card[idx] == EQUAL) {               // Keyword with value field
	    idx++;
	    while ((idx<Fits.CARD) && (card[idx]==SPACE)) idx++;
	    if (card[idx] == QUOTE) {                  // string value
		idx_last = ++idx;
		while ((idx_last < Fits.CARD-1)
		       && ((card[idx_last] != QUOTE)
			   || ((card[idx_last] == QUOTE)
			       && (card[idx_last+1] == QUOTE)))) {
		    if (card[idx_last] == QUOTE) {
			idx_last++;
		    }
		    idx_last++;
		}

		int n1 = idx;                 // convert two quotes to one
		int n2 = idx;
		boolean last_not_quote = true;
		while (n1<idx_last) {
		    card[n2] = card[n1];
		    if (card[n1] == QUOTE) {
			last_not_quote = !last_not_quote;
		    }
		    if (last_not_quote) {
			n2++;
		    }
		    n1++;
		}

		valueField = (n1 == n2) ? kwCard.substring(idx, n2)
		                        : new String(card, idx, n2-idx);
		type = STRING;
		n1 = name.lastIndexOf('.') + 1;
		if (name.regionMatches(n1, "DATE", 0, 4)) {
		    SimpleDateFormat dateFormat = FITSDATE;
		    if (0<valueField.indexOf('-')) {
			dateFormat =  (0<valueField.indexOf('T'))
			    ? ISOLONG : ISOSHORT;
		    }
		    dateFormat.setTimeZone(TIMEZONE);
		    value = dateFormat.parse(valueField, new ParsePosition(0));
		    type = DATE;
		} else {
		    value = valueField.trim();
		    type = STRING;
		}
	    } else {
		idx_last = idx;
		while ((idx_last < Fits.CARD)
		       && (card[idx_last] != SPACE)
		       && (card[idx_last] != SLASH)
		       && (card[idx_last] != COMMA)) {
		    idx_last++;
		}

		try{
			valueField = kwCard.substring(idx, idx_last);
			
		}catch(StringIndexOutOfBoundsException e){
			valueField = "";
			System.err.println("FITS warning: incorrect card <"+ kwCard.substring(idx)+"> missing ending char. Value ignored and start praying and maybe it will work!");
		}
		try {
		    if (0<=valueField.indexOf('.')) {
			value = new Double(valueField);
			type = REAL;
		    } else {
			value = new Integer(valueField);
			type = INTEGER;
		    }
		} catch (NumberFormatException e) {
		    value = new Boolean(valueField.regionMatches(true, 0,
								 "T", 0, 1));
		    type = BOOLEAN;
		}
	    }

	    while ((idx_last < Fits.CARD)           // find comment field
		   && (card[idx_last] != SLASH)) {
		idx_last++;
	    }

	    if ((idx_last < Fits.CARD) && (card[idx_last] == SLASH)) {
		idx_comm_first = idx_last+1;
	    }
	}

	if (0<idx_comm_first) {                      // get keyword comment
	    comment = kwCard.substring(idx_comm_first, Math.min(Fits.CARD,kwCard.length()));
	    comment = comment.trim();
	} else {
	    comment = new String("");
	}
    }

    /** Constructor for FitsKeyword class from String.
     *
     *  @param card  String with 80 characters FITS header card
     *  @exception   FitsException */
    public FitsKeyword(String card) throws FitsException {
	this(card.getBytes());
    }

    /** Constructor for FitsKeyword class specifying name and
     *  comment for a comment keyword'
     *
     *  @param name  String with name of keyword
     *  @param comment  String with keyword comment */
    public FitsKeyword(String name, String comment) {
	setName(name);
	this.comment = comment;
	type = COMMENT;
	validCard = false;
    }

    /** Constructor for FitsKeyword class specifying name, value and
     *  comment for a string keyword.
     *
     *  @param name  String with name of keyword
     *  @param value  String value of keyword
     *  @param comment  String with keyword comment */
    public FitsKeyword(String name, String value, String comment) {
	setName(name);
	this.value = value;
	this.comment = comment;
	type = STRING;
	validCard = false;
    }

    /** Constructor for FitsKeyword class specifying name, value and
     *  comment for a boolean keyword.
     *
     *  @param name  String with name of keyword
     *  @param value  boolean value of keyword
     *  @param comment  String with keyword comment */
    public FitsKeyword(String name, boolean value, String comment) {
	setName(name);
	this.value = new Boolean(value);
	this.comment = comment;
	type = BOOLEAN;
	validCard = false;
    }

    /** Constructor for FitsKeyword class specifying name, value and
     *  comment for an integer keyword.
     *
     *  @param name  String with name of keyword
     *  @param value  int value of keyword
     *  @param comment  String with keyword comment */
    public FitsKeyword(String name, int value, String comment) {
	setName(name);
	this.value = new Integer(value);
	this.comment = comment;
	type = INTEGER;
	validCard = false;
    }

    /** Constructor for FitsKeyword class specifying name, value and
     *  comment for a real keyword.
     *
     *  @param name  String with name of keyword
     *  @param value  double value of keyword
     *  @param comment  String with keyword comment */
    public FitsKeyword(String name, double value, String comment) {
	setName(name);
	this.value = new Double(value);
	this.comment = comment;
	type = REAL;
	validCard = false;
    }

    /** Constructor for FitsKeyword class specifying name, value and
     *  comment for a date keyword.
     *
     *  @param name     String with name of keyword
     *  @param value    Date value of keyword
     *  @param comment  String with keyword comment */
    public FitsKeyword(String name, Date value, String comment) {
	setName(name);
	this.value = value;
	this.comment = comment;
	type = DATE;
	validCard = false;
    }

    /** Method provides the value of a FITS keyword as boolean. For
     *  INTEGER type keywords, all none-zero values will return true.
     *  The method returns FALSE for all keyword types other than
     *  BOOLEAN, REAL and INTEGER. */
    public final boolean getBool() {
	if (type==BOOLEAN) {
	    return ((Boolean)value).booleanValue();
	} else if (type==INTEGER) {
	    return (((Integer)value).intValue()!=0) ? true : false;
	} else if (type==REAL) {
	    return (((Double)value).intValue()!=0) ? true : false;
	}
	return false;
    }

    /** Method provides the value of a FITS keyword as integer for
     *  keyword types INTEGER and REAL.  Zero is returned for all
     *  other types. */
    public final int getInt() {
	if (type==INTEGER) {
	    return ((Integer)value).intValue();
	} else if (type==REAL) {
	    return ((Double)value).intValue();
	}
	return 0;
    }

    /** Method provides the value of a FITS keyword as double for
     *  keyword types INTEGER and REAL.  Zero is returned for all
     *  other types. */
    public final double getReal() {
	if (type==REAL) {
	    return ((Double)value).doubleValue();
	} else if (type==INTEGER) {
	    return ((Integer)value).doubleValue();
	}
	return 0.0;
    }

    /** Method provides the value of a FITS keyword as a Date object for
     *  keywords of type DATE.  For STRING type keywords the string is
     *  converted to a Date if possible otherwise a NULL pointer
     *  is returned. */
    public final Date getDate() {
	if (value == null) {
	    return null;
	}
	if (type == DATE) {
	    return (Date) value;
	} else if (type == STRING) {
	    String str = (String) value;
	    SimpleDateFormat dateFormat = FITSDATE;
	    if (0<str.indexOf('-')) {
		dateFormat = (0<str.indexOf('T')) ? ISOLONG : ISOSHORT;
	    }
	    dateFormat.setTimeZone(TIMEZONE);
	    return dateFormat.parse(str, new ParsePosition(0));
	}
	return null;
    }

    /** Method provides the value of a FITS keyword as a String. If
     *  not value field is defined NULL is returned. */
    public final String getString() {
	if (value == null) {
	    return null;
	}
	if (type == DATE) {
	    SimpleDateFormat 	dateFormat = ISOLONG;
	    dateFormat.setTimeZone(TIMEZONE);
	    return (dateFormat.format((Date) value, new StringBuffer(),
				      new FieldPosition(0))).toString();
	}
	return value.toString();
    }

    /** Set value field for keyword of STRING type. Note: the keyword
     *  type will be changed to STRING.
     *
     *  @param value String with value of keyword value field
     */
    public final void setValue(String value) { 
	this.value = value;
	type = STRING;
	validCard = false;
    }

    /** Set value field for keyword of BOOLEAN type. Note: the keyword
     *  type will be changed to BOOLEAN.
     *
     *  @param value booelan with value of keyword value field
     */
    public final void setValue(boolean value) { 
	this.value = new Boolean(value);
	type = BOOLEAN;
	validCard = false;
    }

    /** Set value field for keyword of INTEGER type. Note: the keyword
     *  type will be changed to INTEGER.
     *
     *  @param value integer with value of keyword value field
     */
    public final void setValue(int value) { 
	this.value = new Integer(value);
	type = INTEGER;
	validCard = false;
    }

    /** Set value field for keyword of REAL type. Note: the keyword
     *  type will be changed to REAL.
     *
     *  @param value double with value of keyword value field
     */
    public final void setValue(double value) {
	this.value = new Double(value);
	type = REAL;
	validCard = false;
    }

    /** Set value field for keyword of DATE type.  Note: the keyword
     *  type will be changed to DATE.
     *
     *  @param value double with value of keyword value field
     */
    public final void setValue(Date value) {
	this.value = value;
	type = DATE;
	validCard = false;
    }

    /** Method generates an 80 character Sting of the keyword in FITS
     *  format.  Note: fields may be truncated due the the 80
     *  char. limit. */
    public String toString() {
	int  idx;

	if (validCard) {    // original FITS card valid
	    return kwCard;
	}

	StringBuffer card = new StringBuffer(80);

	if ((name.length() < 9) && (name.indexOf('.') < 0)) {  // Prime keyword
	    card.append(name);
	    idx = card.length();
	    while (idx++ < 8) card.append(" ");
	} else {                                  // Hierarchical keyword
	    card.append("HIERARCH ");
	    StringTokenizer stok = new StringTokenizer(name, ".");
	    while (stok.hasMoreTokens())
		card.append(stok.nextToken() + " ");
	}

	String val = "'        '";
	switch (type) {                  // Generate keyword value string
	case STRING :
	    StringBuffer sbuf = new StringBuffer((String) value);
	    if (0 <= ((String) value).indexOf('\'')) {
		char[] ch = ((String) value).toCharArray();
		sbuf = new StringBuffer(Fits.CARD);
		for (int n=0; n<ch.length; n++) {
		    sbuf.append(ch[n]);
		    if (ch[n]=='\'') {
			sbuf.append('\'');
		    }
		}
	    }
	    while (sbuf.length()<8) {
		sbuf.append(" ");
	    }
	    sbuf.insert(0, '\'');
	    sbuf.append('\'');
	    val = sbuf.toString();
	    break;
	case INTEGER :
	    val = ((Integer) value).toString();
	    break;
	case REAL :
	    val = ((Double) value).toString();
	    break;
	case BOOLEAN :
	    if (((Boolean) value).booleanValue()) {
		val = "T";
	     } else {
		 val = "F";
	     }
	    break;
	case DATE :
	    SimpleDateFormat dateFormat = ISOLONG;
	    dateFormat.setTimeZone(TIMEZONE);
	    StringBuffer df =
		new StringBuffer("'" +
				 dateFormat.format((Date) value,
						   new StringBuffer(),
						   new FieldPosition(0))
				 + "'");
	    val = df.toString();
	    break;
	case COMMENT :
	    card.append(comment);
	    break;
	}

	if (type!=COMMENT) {
	    card.append("= ");                 //  append value of keyword
	    idx = val.length();
	    if ((card.length() < 11) && type!=STRING) {
		while (idx++ < 20) card.append(" ");
	    }
	    card.append(val);

	    valueTruncated = false;
	    idx = card.length();                 // finally add comment field
	    if (Fits.CARD < idx) {               // check if name/value okay
		card.setCharAt(Fits.CARD-1, '\'');
		valueTruncated = true;
	    }
	    while (idx++ < 30) card.append(" ");
	    card.append(" / " + comment);
	}

	idx = card.length();                    // ensure the card has 80 chars
	if (Fits.CARD<idx) {
	    card.setLength(Fits.CARD);
	} else {
	    while (idx++ < Fits.CARD) card.append(" ");
	}
	return card.toString();
    }

    /** Check if the keyword name or value fields were truncated
     *  by the last call of the toString method. */
    public boolean isValueTruncated(){
	return valueTruncated;
    }

    /** Check if FITS keyword is empty that is has all blank (' ') name
     *  and comment. */
    public boolean isEmpty(){
	return name.length()<1 && comment.length()<1 && value==null;
    }

    /** Check if the FITS keyword was modified since it was created 
     *  from a FITS header card.  */
    public boolean isModified(){
	return !validCard;
    }

    /** Get method to provide name of FITS keyword. */
    public String getName(){
	return name;
    }

    /** Set name of FITS keyword. */
    public void setName(String name){
	this.name = (name == null) ? "" : name.toUpperCase();
    }

    /** Get method to provide type of FITS keyword. */
    public int getType(){
	return type;
    }

    /** Get method to obtain comment of FITS keyword. */
    public String getComment(){
	return comment;
    }

    /** Set comment field of a FITS keyword
     *  @param  comment  String with the keyword comment. */
    public void setComment(String comment){
	this.comment = (comment == null) ? "" : comment;
    }
}
