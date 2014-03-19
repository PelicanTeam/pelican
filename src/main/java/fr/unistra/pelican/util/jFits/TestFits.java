/*
 * @(#)ReadFits.java   $Revision: 1.4 $  $Date: 2005/09/16 09:58:10 $
 *
 * Copyright (C) 1999 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.util.*;
import java.io.*;

/** TestFits class provides a static main method for testing the
 *  FITS class library.  It also shows a typical usage of the
 *  classes.
 *
 *  @version $Id: TestFits.java,v 1.4 2005/09/16 09:58:10 pgrosbol Exp $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class TestFits{
    /** Static method for testing the FITS class library.
     *
     *  @param argv   array of arguments i.e.  FITS files
     */
    public static void main(String[] argv) {
	if (argv.length < 1) {
	    System.out.println("Error: must have at least one argument");
	    System.exit(1);
	}

	System.out.println("-- Test FITS files --------");
	FitsFile file = null;
	for (int na=0; na<argv.length; na++) {
	    try {
		file = new FitsFile(argv[na]);
	    } catch (FitsException e) {
		System.out.println("Error: is not a FITS file >"
				   + argv[na] + "<");
		continue;
	    } catch (IOException e) {
		System.out.println("Error: cannot open file >"
				   + argv[na] + "<");
		continue;
	    }

	    int noHDU = file.getNoHDUnits();
	    System.out.println("FITS file has " + noHDU + " HDUnits");

	    for (int i=0; i<noHDU; i++) {
		FitsHDUnit hdu = file.getHDUnit(i);
		FitsHeader hdr = hdu.getHeader();
		int noKw = hdr.getNoKeywords();
		int type = hdr.getType();
		int size = (int) hdr.getDataSize();
		System.out.println("  " + i + ": >" + hdr.getName() 
				   + "< of type >" + Fits.getType(type)
				   + "< with " + noKw + " keywords"
				   + " and " + size + " bytes of data");
		System.out.println("   Keywords:");
		Enumeration itr = hdr.getKeywords();
		while (itr.hasMoreElements()) {
		    FitsKeyword kw = (FitsKeyword) itr.nextElement();
		    System.out.print("     " + kw.getName());
		    switch (kw.getType()) {
		    case FitsKeyword.COMMENT:
			System.out.print("(C) " + kw.getComment());
			break;
		    case FitsKeyword.STRING:
			System.out.print("(S)= '" + kw.getString() + "'");
			break;
		    case FitsKeyword.BOOLEAN:
			System.out.print("(B)= " + kw.getBool());
			break;
		    case FitsKeyword.INTEGER:
			System.out.print("(I)= " + kw.getInt());
			break;
		    case FitsKeyword.REAL:
			System.out.print("(R)= " + kw.getReal());
			break;
		    case FitsKeyword.DATE:
			System.out.print("(D)= " + kw.getString());
			break;
		    default:
		    }
		    if (0<kw.getComment().length()
			&& (kw.getType()!=FitsKeyword.COMMENT)) {
			System.out.print(" / " + kw.getComment());
		    }
		    System.out.println();
		}

		if (type == Fits.IMAGE) {
		    System.out.println("\n  Check data matrix "
				       + "- compute mean and rms");
		    FitsMatrix dm = (FitsMatrix) hdu.getData();
		    int naxis[] = dm.getNaxis();
		    double crval[] = dm.getCrval();
		    double crpix[] = dm.getCrpix();
		    double cdelt[] = dm.getCdelt();

		    System.out.println("  Dimension of matrix: "
				       + naxis.length);
		    for (int n=0; n<naxis.length; n++) 
			System.out.println("   Axis " + n + ": " + naxis[n]
					   + ",  " + crpix[n] + ",  "
					   + crval[n] + ",  " + cdelt[n]);
		    System.out.println("\n");

		    int nv, off, npix;
		    int nval = dm.getNoValues();
		    if (0<nval) {
			int ncol = naxis[0];
			int nrow = nval/ncol;
			System.out.println(" Npixel,row,col: " + nval
					   + ", " + nrow + ", " + ncol);
			float data[] = new float[ncol];
			double mean, rms, val;

			off = nv = npix = 0 ;
			mean = rms = 0.0;
			long time = System.currentTimeMillis();
			for (int nr=0; nr<nrow; nr++) {
			    try {
				dm.getFloatValues(off, ncol, data);
				for (int n = 0; n<ncol; n++) {
				    val = data[n];
				    npix++;
				    mean += val;
				    rms  += val*val;
				}
			    } catch (FitsException e) {
			    }
			
			    off += ncol;
			}
			mean = mean/npix;
			rms  = rms/npix - mean*mean;
			rms = ((0.0<rms) ? Math.sqrt(rms) : 0.0);
			float dtime =
			    (float) (1000.0*(System.currentTimeMillis()-time)/
				     ((double) nval));
			System.out.println("  Mean: " + (float)mean +
					   ", rms: " + (float)rms +
					   ", Time: " + dtime
					   + " S/Mp, Pixels: " + npix);
		    }
		} else if (type==Fits.BTABLE || type==Fits.ATABLE) {
		    System.out.println("\n  Check table data - list columns");
		    FitsTable dm = (FitsTable) hdu.getData();
		    int nrow = dm.getNoRows();
		    int ncol = dm.getNoColumns();
		    FitsColumn col[] = new FitsColumn[ncol];
		    System.out.println("  Columns: " + ncol 
				       + ", Rows: " + nrow);
		    for (int n=0; n<ncol; n++) {
			col[n] = dm.getColumn(n);
			System.out.print("  " + n + " >"
					 + col[n].getLabel() + "<, ");
			System.out.print(col[n].getRepeat() + " ");
			System.out.print(col[n].getDataType() + ", >");
			System.out.print(col[n].getDisplay() + "<, >");
			System.out.println(col[n].getUnit() + "<");

			if (col[n].getDataType() == 'F'
			    || col[n].getDataType() == 'E'
			    || col[n].getDataType() == 'D') {
			    int npix = 0;
			    double mean, rms, val;
			    mean = rms = 0.0;
			    long time = System.currentTimeMillis();
			    for (int nr=0; nr<nrow; nr++) {
				val = col[n].getReal(nr);
				if (Double.isNaN(val)) continue;
				npix++;
				mean += val;
				rms  += val*val;
			    }
			    float dtime =
				(float) (1000.0*(System.currentTimeMillis()
						 -time)/((double) nrow));
			    mean = mean/npix;
			    rms  = rms/npix - mean*mean;
			    rms = ((0.0<rms) ? Math.sqrt(rms) : 0.0);
			    System.out.println("      no,mean,rms: " + npix
					       + ", " + (float)mean + ", "
					       + (float)rms + "; "
					       + dtime + " S/Mp");
			} else if (col[n].getDataType() == 'I'
				   || col[n].getDataType() == 'J'
				   || col[n].getDataType() == 'B') {
			    int npix = 0;
			    double mean, rms, val;
			    mean = rms = 0.0;
			    long time = System.currentTimeMillis();
			    for (int nr=0; nr<nrow; nr++) {
				val = col[n].getInt(nr);
				if (val == Long.MIN_VALUE) continue;
				npix++;
				mean += val;
				rms  += val*val;
			    }
			    float dtime =
				(float) (1000.0*(System.currentTimeMillis()
						 -time)/((double) nrow));
			    mean = mean/npix;
			    rms  = rms/npix - mean*mean;
			    rms = ((0.0<rms) ? Math.sqrt(rms) : 0.0);
			    System.out.println("      no,mean,rms: " + npix
					       + ", " + (float)mean + ", "
					       + (float)rms + "; "
					       + dtime + " S/Mp");
			}
		    }
		}
	    }
	    System.out.println("-- Test finished -----------------");
	}

	System.exit(0);
    }
}
