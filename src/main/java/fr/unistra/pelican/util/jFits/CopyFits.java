/* @(#)CopyFits.java   $Revision: 1.2 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2002 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.io.*;

/** CopyFits class provides a static main method to test writing
 *  of FITS files by copying an existing FITS file.
 *
 *  @version $Revision: 1.2 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class CopyFits{
    /** Static method for testing the FITS class library.
     *
     *  @param argv   array of arguments i.e. options of FITS files
     */
    public static void main(String[] argv) {
	System.out.println("Start CopyFits");
	if (argv.length != 2) {
	    System.err.println("Error: must have two argument> input output");
	    System.exit(1);
	}

	FitsFile file = null;
	try {
	    file = new FitsFile(argv[0]);
	} catch (FitsException e) {
	    System.err.println("Error: is not a FITS file >" + argv[0] + "<");
	    System.exit(-1);
	}  catch (IOException e) {
	    System.err.println("Error: cannot open file >" + argv[0] + "<");
	    System.exit(-1);
	}

	int noHDU = file.getNoHDUnits();
	System.out.println("FITS file has " + noHDU + " HDUnits");

	try {
	    file.writeFile(argv[1]);
	} catch (FitsException e) {
	    System.err.println("Error: FITS problem in writing >"
			       + argv[1] + "<");
	    System.exit(-1);
	} catch (IOException e) {
	    System.err.println("Error: cannot write file >" + argv[1] + "<");
	    System.exit(-1);
	}

	System.out.println("Finish CopyFits");
	System.exit(0);
    }
}
