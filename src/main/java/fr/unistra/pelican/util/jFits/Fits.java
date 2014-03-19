/*
 * @(#)Fits.java     $Revision: 1.4 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 1999 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

/** Fits class defines common constants used by the FITS package
 *
 *  @version $Revision: 1.4 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class Fits {

    /**  Definition of general FITS constants  */
    final static int CARD    =   80;
    final static int RECORD  = 2880;
    final static int NOCARDS = RECORD/CARD;
    final static String END_CARD =
	"END                                     " +
	"                                        ";
    final static String BLANK_CARD =
	"                                        " +
	"                                        ";

    /**  Definition of FITS Header/Data unit types */
    final public static int FALSE   = -1;
    final public static int UNKNOWN =  0;
    final public static int IMAGE   =  1;
    final public static int BTABLE  =  2;
    final public static int ATABLE  =  3;
    final public static int RGROUP  =  4;

    /**  Definition of FITS Data types */
    final public static int BYTE   =  8;
    final public static int SHORT  =  16;
    final public static int INT    =  32;
    final public static int FLOAT  = -32;
    final public static int DOUBLE = -64;

    /** Get string with FITS extension type */
    final public static String getType(int type) {
	switch (type) {
	case FALSE  : return "False";
	case IMAGE  : return "Image";
	case BTABLE : return "BinTable";
	case ATABLE : return "AsciiTable";
	case RGROUP : return "RandomGroups";
	default:
	}
	return "Unknown";
    }
}
