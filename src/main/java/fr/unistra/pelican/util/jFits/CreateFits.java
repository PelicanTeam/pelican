/* @(#)CreateFits.java   $Revision: 1.2 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2002 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.util.*;
import java.io.*;

/** CreateFits class provides a static main method to test creation
 *  and writing a FITS from scratch.
 *
 *  @version $Revision: 1.2 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class CreateFits{

    public static final int ROWS = 473;

    /** Static method for testing the FITS class library.
     *
     *  @param argv   name of FITS file to create
     */
    public static void main(String[] argv) {
	if (argv.length < 1) {
	    System.err.println("Usage: java org.eso.fits.CreateFits file");
	    System.exit(1);
	}

	System.out.println("Start CreateFits");

	// First create a data matrix

	int[] naxis = new int[2];
	naxis[0] = 251; naxis[1] = 247;
	int nax = naxis[0] * naxis[1];
	float[] data = new float[nax];
	for (int n=0; n<nax; n++) {
	    data[n] = n;
	}

	// Create a FITS data matrix

	FitsHDUnit hdu0 = null;
	FitsHDUnit hdu1 = null;
	FitsHDUnit hdu2 = null;

	try {
	    FitsMatrix mtx0 = new FitsMatrix(Fits.FLOAT, naxis);
	    mtx0.setFloatValues(0, data);
	    double[] cr = new double[2];
	    cr[0] = 234.02; cr[1] = -12.1;
	    mtx0.setCrpix(cr);
	    cr[0] = -331.3; cr[1] = 721.3;
	    mtx0.setCrval(cr);
	    cr[0] = 0.214; cr[1] = 0.331;
	    mtx0.setCdelt(cr);

	    // Use that to make a HDU

	    FitsHeader hdr0 = mtx0.getHeader();
	    hdr0.addKeyword(new FitsKeyword("", ""));
	    hdr0.addKeyword(new FitsKeyword("DATE", new Date(),
					    "Date of writing"));
	    hdu0 = new FitsHDUnit(hdr0, mtx0);

	    // Then we try to make a small table extension in the same way

	    FitsColumn col0 = new FitsColumn(Fits.INT,    "1I", "No",  ROWS);
	    FitsColumn col1 = new FitsColumn(Fits.DOUBLE, "1D", "RA",  ROWS);
	    FitsColumn col2 = new FitsColumn(Fits.DOUBLE, "1D", "Dec", ROWS);
	    FitsColumn col3 = new FitsColumn(Fits.FLOAT,  "1E", "Mag", ROWS);
/*
	for (int n=0; n<ROWS; n++) {
	    col0.setInt(n, n);
	    col1.setReal(n, 0.02*n);
	    col2.setReal(n, 0.03*n);
	    col3.setReal(n, 0.5*n);
	}

	    FitsTable tab1 = new FitsTable(Fits.BYTE, null);
	    tab1.addColumn(col0);
	    tab1.addColumn(col1);
	    tab1.addColumn(col2);
	    tab1.addColumn(col3);

	    FitsHeader hdr1 = tab1.getHeader();
	    hdu1 = new FitsHDUnit(hdr1, tab1);
*/
	    // And finally a rather small image extension

	    naxis = new int[3];
	    naxis[0] = 64; naxis[1] = 64; naxis[2] = 3;
	    nax = naxis[0] * naxis[1] * naxis[2];
	    short[] ndata = new short[nax];
	    for (int n=0; n<nax; n++) {
		ndata[n] = (short) n;
	    }

	    FitsMatrix mtx2 = new FitsMatrix(Fits.SHORT, naxis);
	    mtx2.setShortValues(0, ndata);

	    FitsHeader hdr2 = mtx2.getHeader();
	    hdr2.setExtension(Fits.IMAGE);
	    hdr2.addKeyword(new FitsKeyword("", ""));
	    hdr2.addKeyword(new FitsKeyword("EXTNAME", "TEST",
					    "Extension name"));
	    hdr2.addKeyword(new FitsKeyword("DATE", new Date(),
					    "Date of writing"));
	    hdu2 = new FitsHDUnit(hdr2, mtx2);
	} catch (FitsException e) {
	    System.err.println("Error: cannot create HDU0;" + e);
	    System.exit(1);
	}

	// All it all together in a file

	FitsFile file = new FitsFile();
	file.addHDUnit(hdu0);
//	file.addHDUnit(hdu1);
	file.addHDUnit(hdu2);

        // And write it out to a disk file
	
	int noHDU = file.getNoHDUnits();
	System.out.println("FITS file has " + noHDU + " HDUnits");

	try {
	    file.writeFile(argv[0]);
	} catch (FitsException e) {
	    System.err.println("Error: FITS problem in writing >"
			       + argv[0] + "<");
	    System.exit(-1);
	} catch (IOException e) {
	    System.err.println("Error: cannot write file >" + argv[0] + "<");
	    System.exit(-1);
	}
	System.out.println("Finish CreateFits");

	System.exit(0);
    }
}
