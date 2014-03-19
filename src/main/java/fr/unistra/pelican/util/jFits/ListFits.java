/*
 * @(#)ListFits.java   $Revision: 1.4 $ $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 1999 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.util.*;
import java.io.*;

/** ListFits class provides a static main method for listing a set
 *  of FITS keywords in FITS files.
 *  @version $Revision: 1.4 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class ListFits{
    /** Static main method for listing keywords of a set of FITS files.
     *
     *  @param argv   First argument is a list of FITS files or directories,
     *                second argument is a comma separated list of FITS 
     *                keywords to be listed.
     */
    public static void main(String[] argv) {
	if (argv.length < 1) {
	    System.out.println("Error: must be called with one argument");
	    System.exit(1);
	}

	// get list of files

	StringTokenizer files = new StringTokenizer(argv[0],",");

	// get list of keywords to list for each file

	String[] keys = new String[0];
	if (argv.length > 1) {
	    StringTokenizer stok = new StringTokenizer(argv[1],",");
	    keys = new String[stok.countTokens()];
	    int no = 0;
	    while (stok.hasMoreTokens()) {
		keys[no++] = (stok.nextToken()).toUpperCase();
	    }
	    stok = null;
	}

	// go through files one by one

	int nofiles = 0;
	int nokwords = 0;
	long time = System.currentTimeMillis();
	while (files.hasMoreTokens()) {
	    String name = files.nextToken();
	    File file = new File(name);

	    // if directory read all files in it

	    String[] flist = new String[1];
	    if (file.isDirectory()) {
		flist = file.list();
	    } else {
		flist[0] = name;
	    }

	    for (int i=0; i<flist.length; i++) {
		if (!FitsFile.isFitsFile(flist[i])) continue;

		FitsFile ffile = null;              // open FITS file
		try {
		    ffile = new FitsFile(flist[i]);
		} catch (Exception e) {
		    continue;
		}

		nofiles++;                         // get prime FITS header
		int noHDU = ffile.getNoHDUnits();
		FitsHDUnit hdu = ffile.getHDUnit(0);
		FitsHeader hdr = hdu.getHeader();
		nokwords += hdr.getNoKeywords();
		System.out.print(flist[i]);

		// list specified keywords in the file

		for (int n=0; n<keys.length; n++) {
		    FitsKeyword kw = (FitsKeyword) hdr.getKeyword(keys[n]);
		    if (kw == null) continue;
		    switch (kw.getType()) {
		    case FitsKeyword.STRING:
			System.out.print("   " + kw.getString());
			break;
		    case FitsKeyword.BOOLEAN:
			System.out.print("   " + kw.getBool());
			break;
		    case FitsKeyword.INTEGER:
			System.out.print("   " + kw.getInt());
			break;
		    case FitsKeyword.REAL:
			System.out.print("   " + kw.getReal());
			break;
		    case FitsKeyword.DATE:
			System.out.print("   " + kw.getString());
			break;
		    default:
		    }
		}
		System.out.print("\n");
	    }
	}
	if (0<nofiles) {
	    float dtime = (float) (0.001*(System.currentTimeMillis()-time));
	    float dtf = dtime/((float) nofiles);
	    float dtk = ((float) nokwords)/dtime;
	    System.out.println("  Time: " + dtf + " S/file, Rate: "
			       + dtk + " kw/S, Keywords: " + nokwords);
	}
	System.exit(0);
    }
}
