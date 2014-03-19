/*
 * @(#)SaveFits.java   $Revision: 1.2 $ $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2000 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.util.*;
import java.io.*;

/** SaveFits class provides a static main method fto test writing
 *  of FITS cfiles.
 *  @version $Revision: 1.2 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class SaveFits{
    /** Static method for testing the FITS class library.
     *
     *  @param argv   array of arguments i.e. options of FITS files
     */
    public static void main(String[] argv) {
	System.out.println("Start SaveFits");
	if (argv.length != 1) {
	    System.out.println("Error: must have one argument, input file");
	    System.exit(1);
	}

	FitsFile file = null;
	try {
	    file = new FitsFile(argv[0]);
	} catch (FitsException e) {
	    System.out.println("Error: is not a FITS file >" + argv[0] + "<");
	    System.exit(-1);
	} catch (IOException e) {
	    System.out.println("Error: cannot open file >" + argv[0] + "<");
	    System.exit(-1);
	}

	int noHDU = file.getNoHDUnits();
	System.out.println("FITS file has " + noHDU + " HDUnits");

	FitsHDUnit hdu = file.getHDUnit(0);
	FitsHeader hdr = hdu.getHeader();
	FitsKeyword kw = hdr.getKeyword("DATE");
	kw.setValue(new Date());
	kw.setComment("Date of writting YYYY-MM-DD");
	kw = new FitsKeyword("ANY", 124870921578.123,"Just a number");
	hdr.addKeyword(kw);
	kw = new FitsKeyword("ESO.DET.ID", "ID#123443" ,"The Detector ID");
	hdr.addKeyword(kw);
	kw = new FitsKeyword("ATOOLONGKW", 122445,"And an interger");
	hdr.addKeyword(kw);
	kw = new FitsKeyword("ISITTRUE", false,"let's try a boolesn");
	hdr.addKeyword(kw);
	kw = new FitsKeyword(" ","");
	hdr.addKeyword(kw);
	kw = new FitsKeyword("NewDate", new Date(), "Try a Date");
	hdr.addKeyword(kw);
	kw = new FitsKeyword("COMMENT","a small real");
	hdr.addKeyword(kw);
	kw = new FitsKeyword("AN-amall", 0.000000000000012234,"a small real");
	hdr.addKeyword(kw);
	kw = new FitsKeyword(" ","");
	hdr.addKeyword(kw);

	try {
	    file.saveFile();
	} catch (FitsException e) {
	    System.out.println("Error: FITS problem in writing >"
			       + argv[0] + "<");
	    System.exit(-1);
	} catch (IOException e) {
	    System.out.println("Error: cannot write file >" + argv[0] + "<");
	    System.exit(-1);
	}

	System.exit(0);
    }
}
