/*
 * @(#)FitsException.java     $Revision: 1.5 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 1999 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

/** FitsException defines special exception for the FITS package
 *  HD unit or as an image extension.
 *
 *  @version $Revision: 1.5 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsException extends Exception {
    
    /** Define FITS exception types */
    final public static int NONE = 0;
    final public static int FILE = 1;
    final public static int KEYWORD = 2;
    final public static int ENDCARD = 3;
    final public static int HEADER = 4;
    final public static int DATA = 5;
    final public static int NOHEADERSPACE = 6;

    private int type = NONE;

    /** Default constructor for FitsExtension class */
    public FitsException() {
	super();
	this.type = NONE;
    }

    /** Constructor for FitsExtension class specifying a message.
     *
     *  @param mess   error message for exception */
    public FitsException(String mess) {
	super(mess);
	this.type = NONE;
    }

    /** Constructor for FitsExtension class specifying message and type.
     *
     *  @param mess  error message for exception
     *  @param type  FITS exception tyep */
    public FitsException(String mess, int type) {
	super(mess);
	this.type = type;
    }

    /** Get FITS exception type */
    public int getType(){
	return type;
    }
}






