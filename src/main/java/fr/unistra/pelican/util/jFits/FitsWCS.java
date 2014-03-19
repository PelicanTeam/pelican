/* @(#)FitsWCS.java     $Revision: 1.4 $    $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2003 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

/** FitsData class represents a FITS data unit
 *
 *  @version $Revision: 1.4 $ $Date: 2004/01/12 13:13:23 $
 *  @author  P.Grosbol, DMD/ESO, <pgrosbol@eso.org>
 */
public class FitsWCS {

    public final static int LIN = 0;
    public final static int TAN = 1;
    public final static int ARC = 2;

    private final static int MPS = 20;

    protected int        type;
    protected int        nax = 0;
    protected int[]      cproj;
    protected double[]   crpix;
    protected double[]   crval;
    protected double[]   cdelt;
    protected double[]   crota;
    protected String[]   ctype;
    protected double[][] cdMatrix;
    protected double[][] pcMatrix;
    protected boolean    hasPcMatrix = false;
    protected boolean    hasCdMatrix = false;

    protected double[]   amdx = new double[MPS];
    protected double[]   amdy = new double[MPS];

    /** Default constructor for FitsWCS class.
     */
    public FitsWCS() {
	type = Fits.UNKNOWN;
    }

    /** Constructor for FitsWCS class given a FITS header with 
     *  associated data unit as a file.
     *
     *  @param header  FitsHeader object with the image header
     */
    public FitsWCS(FitsHeader header) {
	this();
	setHeader(header, ' ');
    }

    /** Constructor for FitsWCS class given a FITS header with 
     *  associated data unit as a file.
     *
     *  @param header  FitsHeader object with the image header
     *  @param ver     version of WCS i.e. ' ' or 'A'..'Z'
     */
    public FitsWCS(FitsHeader header, char ver) {
	this();
	setHeader(header, ver);
    }

    /** Constructor for FitsWCS given number of axes
     *
     *  @param nax  Number of axes in data matrix
     */
    public FitsWCS(int nax) {
	this();
	init(nax);
    }

    /** Define FITS header for FitsWCS object.
     *
     *  @param header  FitsHeader object with the image header
     *  @param ver     version of WCS i.e. ' ' or 'A'..'Z'
     */
    public void setHeader(FitsHeader header, char ver) {
	type = header.getType();

        FitsKeyword kw = header.getKeyword("NAXIS");
	nax = (kw == null) ? 0 : kw.getInt();
	init(nax);

	String sver = String.valueOf(ver).toUpperCase().trim();
	for (int j=1; j<=nax; j++) {
            kw = header.getKeyword("CRPIX"+j+sver);
            crpix[j-1] = (kw == null) ? 0.0 : kw.getReal();

            kw = header.getKeyword("CRVAL"+j+sver);
            crval[j-1] = (kw == null) ? 0.0 : kw.getReal();

            kw = header.getKeyword("CDELT"+j+sver);
            cdelt[j-1] = (kw == null) ? 1.0 : kw.getReal();
            cdMatrix[j-1][j-1] = cdelt[j-1];

            kw = header.getKeyword("CTYPE"+j+sver);
	    ctype[j-1] = (kw == null) ? "        " : kw.getString();
	    if (7<ctype[j-1].length()) {
		String wctype = ctype[j-1].substring(5, 7);
		if (wctype.equals("TAN")) {
		    cproj[j-1] = TAN;
		} else if (wctype.equals("ARC")) {
		    cproj[j-1] = ARC;
		} else {
		    cproj[j-1] = LIN;
		}
	    } else {
		cproj[j-1] = LIN;
	    }

            for (int i=1; i<=nax; i++) {
		kw = header.getKeyword("CD"+j+"_"+j+sver);
		if (kw != null) {
		    cdMatrix[j-1][i-1] = kw.getReal();
		    hasCdMatrix = true;
		}
            }

            for (int i=1; i<=nax; i++) {
		kw = header.getKeyword("PC"+j+"_"+i+sver);
		if (kw != null) {
		    pcMatrix[j-1][i-1] = kw.getReal();
		    hasPcMatrix = true;
		}
            }
        }

       for (int j=1; j<MPS; j++) {
	   kw = header.getKeyword("AMDX"+j);
	   amdx[j-1] = (kw == null) ? 0.0 : kw.getReal();
	   kw = header.getKeyword("AMDY"+j);
	   amdy[j-1] = (kw == null) ? 0.0 : kw.getReal();
       }
    }

    /** Initiate internal WCS data structures.
     *
     *  @param   nax  No. of axies of data array
     */
    private void init(int nax) {
        cproj = new int[nax];
        crpix = new double[nax];
        crval = new double[nax];
        cdelt = new double[nax];
        ctype = new String[nax];
        cdMatrix = new double[nax][nax];
        pcMatrix = new double[nax][nax];
        ctype = new String[nax];

	for (int n=0; n<nax; n++) {
	    cproj[n] = LIN;
	    crpix[n] = 0.0;
	    crval[n] = 0.0;
	    cdelt[n] = 1.0;
	    ctype[n] = "        ";
            for (int i=0; i<nax; i++) {
                cdMatrix[n][i] = (i==n) ? 1.0 : 0.0;
                pcMatrix[n][i] = (i==n) ? 1.0 : 0.0;
            }    
	}
    }

    /** Compute World Coordinates from pixel coordinates.
     *
     *  @param  p  Array with pixel coordinates
     */
    public double[] toWCS(double[] p) {
	double[] w;
	double[] q;

	for (int j=0; j<nax; j++) p[j] -= crpix[j];
	if (hasPcMatrix) {
	    q = matrixMult(pcMatrix, p);
	    for (int j=0; j<nax; j++) q[j] *= cdelt[j];
	} else if (hasCdMatrix) {
	    q = matrixMult(cdMatrix, p);
	} else {
	    q = p;
	    for (int j=0; j<nax; j++) q[j] *= cdelt[j];
	}

	switch (cproj[0]) {
	    case TAN:
	    case LIN:
		for (int j=0; j<nax; j++) q[j] += crval[j];
	}

	return q;
    }
    /** Compute pixel coordinates from a set of World Coordinates.
     *
     *  @param  wcs  Array with World Coordinates
     */
    public double[] toPixel(double[] wcs) {
	double[] pix = new double[nax];
	double[] wc  = new double[nax];

	for (int n=0; n<nax; n++) {
	    pix[n] = (wcs[n]-crval[n])/cdelt[n] + crpix[n];
	}
	return pix;
    }

    private double[] matrixMult(double[][] mtx, double[] vec) {
	int nv = vec.length;
	double[] res = new double[nv];

	for (int j=0; j<nv; j++) {
	    res[j] = 0;
	    for (int i=0; i<nv; i++) {
		res[j] += mtx[j][i] * vec[i];
	    }
	}
	return res;
    }
}





