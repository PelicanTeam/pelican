/* @(#)FitsFile.java     $Revision: 1.13 $    $Date: 2005/09/16 09:58:10 $
 *
 * Copyright (C) 2002 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.util.*;
import java.io.*;

/** FitsFile class represents a FITS file consisting of a set of
 *  Header/Data Units.  The header information is stored in
 *  FitsHeader objects while data are not saved in objects but
 *  accessed through file.  Thus, files may be corrupted if the
 *  disk file is modified independently by other modules.
 *
 *  @version $Revision: 1.13 $ $Date: 2005/09/16 09:58:10 $
 *  @author  P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class FitsFile {

    private File file;
    private RandomAccessFile raFile;
    private Vector hdUnits;
    private boolean changeHDU = false;

    /** Default constructor for FitsFile class
     */
    public FitsFile() {
	hdUnits = new Vector(1);
    }

    /** Constructor for FitsFile class given a FITS stream.
     *  
     *
     *  @param file    DataInput stream positioned at its start
     *  @exception FitsException */
    public FitsFile(DataInput file) throws FitsException {
	this();
	scanFitsFile(file, false);
    }

    /** Constructor for FitsFile class given a FITS stream and a flag
     *  indicating if the data matrices should be stored internally.
     *  The store flag must be true to allow access to the data matrices
     *  whereas header data will always be available.
     *
     *  @param file    DataInput stream positioned at its start
     *  @param sflag   Flag indicating if data matrices should be
     *                 stored internally in the class.
     *  @exception FitsException */
    public FitsFile(DataInput file, boolean sflag) throws FitsException {
	this();
	scanFitsFile(file, sflag);
    }

    /** Constructor specifying a name of a disk file. Note: the file
     *  will be opened in read-only mode for security reasons.  This
     *  means that the data matrix cannot be modified although headers
     *  can as they are stored in memory.  If data should be modified,
     *  one must create a DataInput object for the file explicitly
     *  (with read/write permissions) and use the appropriate
     *  constructor.
     *
     *  @param file name of disk file in FITS format
     *  @exception IOException,FitsException */
    public FitsFile(File file) throws IOException, FitsException {
	this();
	this.raFile = new RandomAccessFile(file, "r");
	this.file = file;
	scanFitsFile(raFile, false);
    }

    /** Constructor from name of disk file. Note: the file will be
     *  opened in read-only mode for security reasons.  This means
     *  that the data matrix cannot be modified although headers can
     *  as they are stored in memory.  If data should be modified, one
     *  must create a DataInput object for the file explicitly (with
     *  read/write permissions) and use the appropriate constructor.
     *
     *  @param filename name of disk file in FITS format
     *  @exception IOException,FitsException */
    public FitsFile(String filename) throws IOException, FitsException {
	this(new File(filename));
    }

    /** Private method which scans an input stream.  It is used by the
     *  constructors.
     *  @param file    DataInput file positioned at its start
     *  @param sflag   Flag for internal storage of data matrices
     *  @exception FitsException */
    private void scanFitsFile(DataInput file, boolean sflag)
	throws FitsException {
	FitsHDUnit hdu;
	int no_hdu = 0;

	try {
	    while (true) {
		hdu = new FitsHDUnit(file, sflag);
		hdUnits.setSize(no_hdu++);
		hdUnits.addElement(hdu);
	    }
	} catch (FitsException e) {
		//System.out.println("Fits read stop because " + e);
	    if (no_hdu<1) {
		throw new FitsException("Not a FITS file (" +e +")", FitsException.FILE);
	    }
	}
	hdUnits.trimToSize();
    }

    /** Finalize method which close disk file
     *
     *  @exception IOException */
    protected void finalize() throws IOException {
	if (raFile != null) raFile.close();
	file = null;
    }

    /** Static method to test if a disk file possibly is in FITS format.
     *  The test is trivial in the sense that the file may not be a correct
     *  FITS file even if 'true' is returned.  On the other hand, it is
     *  certainly not a FITS file if 'false' is returned.
     *
     *  @param file  disk file */
    public static boolean isFitsFile(File file) {
	int nb = 0;
	byte[] card = new byte[Fits.CARD];
	try {
	    RandomAccessFile raf = new RandomAccessFile(file, "r");
	    nb = raf.read(card);
	    raf.close();
	} catch (IOException e) {
	    return false;
	}
	if (nb<Fits.CARD) {
	    return false;
	}
	String str = new String(card);
	return str.startsWith("SIMPLE  = ");
    }

    /** Static method to test if a disk file possibly is in FITS format.
     *  The test is trivial in the sense that the file may not be a correct
     *  FITS file even if 'true' is returned.  On the other hand, it is
     *  certainly not a FITS file if 'false' is returned.
     *
     *  @param filename name of disk file */
    public static boolean isFitsFile(String filename) {
	return isFitsFile(new File(filename));
    }

    /** Add new HDUnit to FITS file.
     *
     *  @param  hdu  FitsHDUnit to be added */
    public void addHDUnit(FitsHDUnit hdu) {
	hdUnits.addElement(hdu);
	changeHDU = true;
    }

    /** Insert new HDUnit to FITS file at specified location.
     *
     *  @param  hdu  FitsHDUnit to be inserted
     *  @param  index  location at which hte HDU should be inserted */
    public void insertHDUnitAt(FitsHDUnit hdu, int index) {
	hdUnits.insertElementAt(hdu, index);
	changeHDU = true;
    }

    /** Remove HDUnit with given location from FITS file.
     *
     *  @param  index  location of the HDU to be removed  */
    public void removeHDUnitAt(int index) {
	hdUnits.removeElementAt(index);
	changeHDU = true;
    }

    /** Get HDUnit in FitsFile by its position.  If the position is
     *  less that 0, the first HDU is returned while the kast is given
     *  for positions beyond the actual number.
     *
     *  @param  no  number of HDUnit to retrieve (starting with 0) */
    final public FitsHDUnit getHDUnit(int no){
	int  n = 0;

	if (no<0) {
	    return (FitsHDUnit) hdUnits.firstElement();
	} else if (no>=hdUnits.size()) {
	    return (FitsHDUnit) hdUnits.lastElement();
	}

	FitsHDUnit   hdu;
	Enumeration  itr = hdUnits.elements();
	while (itr.hasMoreElements()) {
	    hdu = (FitsHDUnit) itr.nextElement();
	    if (n==no) return hdu;
	    n++;
	}
	return (FitsHDUnit) hdUnits.lastElement();
    }

    /** Save changes made to a FITS file on disk. The FitsFile must have
     *  been created from a read/write RandomAccess disk file.
     *  Further, headers and data must fit into the original file.
     *  Not check is done to verify the correctness of the FITS headers.
     *
     *  @exception IOException, FitsException  */
    public void saveFile() throws IOException,FitsException {
	if (changeHDU) {
	    throw new FitsException("HD Units of file have been changes",
				    FitsException.FILE);
	}
	Enumeration  itr = hdUnits.elements();
	while (itr.hasMoreElements()) {
	    if (!((FitsHDUnit) itr.nextElement()).canSave()) {
		throw new FitsException("No space in FITS header",
					FitsException.NOHEADERSPACE);
	    }
	}

	RandomAccessFile raf = new RandomAccessFile(file, "rw");

	itr = hdUnits.elements();
	while (itr.hasMoreElements()) {
	    ((FitsHDUnit)itr.nextElement()).saveFile(raf);
	}
	raf.close();
    }

    /** Write FITS file to a DataOutput stream. Not check is done to verify
     *  the correctness of the FITS headers.
     *
     *  @param  filename  name of new file to be written
     *  @exception IOException, FitsException  */
    public void writeFile(DataOutput filename) throws IOException,FitsException {
	Enumeration  itr = hdUnits.elements();
	while (itr.hasMoreElements()) {
	    ((FitsHDUnit)itr.nextElement()).writeFile(filename);
	}
    }

    /** Write FITS file on a new diskfile. Not check is done to verify
     *  the correctness of the FITS headers.
     *
     *  @param  file  new file to be written
     *  @exception IOException, FitsException  */
    public void writeFile(File file) throws IOException,FitsException {
	if (file == null) {
	    throw new FitsException("Cannot write to null-pointer file",
				    FitsException.FILE);
	}
	if (file.exists()) {
	    if (!file.isFile()) {
		throw new FitsException("Cannot overwrite special file",
					FitsException.FILE);
	    }
	    if ((this.file != null)
		&& this.file.getCanonicalPath().equals(
					    file.getCanonicalPath())) {
		throw new FitsException("Cannot overwrite itself",
					FitsException.FILE);
	    }
	}
	RandomAccessFile raf = new RandomAccessFile(file, "rw");
	writeFile(raf);
	raf.close();
    }

    /** Write FITS file on a new diskfile. Not check is done to verify
     *  the correctness of the FITS headers.
     *
     *  @param  filename  name of new file to be written
     *  @exception IOException, FitsException  */
    public void writeFile(String filename) throws IOException,FitsException {
	writeFile(new File(filename));
    }

    /** Remove all references to associated DataInput files. */
    public void closeFile() {
	Enumeration  itr = hdUnits.elements();
	while (itr.hasMoreElements()) {
	    ((FitsHDUnit)itr.nextElement()).closeFile();
	}
	if (raFile != null) {
	    try {
		raFile.close();
	    } catch (IOException e) {
	    }
	}
	this.file = null;
    }

    /** Gets numnber of HDUnits in FITS file */
    final public int getNoHDUnits(){
	return hdUnits.size();
    }

    /** Gets Canonical path of FITS file */
    public String getName(){
	String name = "";
	if (file != null) {
	    try {
		name = file.getCanonicalPath();
	    } catch (IOException e) {
		name = file.getAbsolutePath();
	    }
	}
	return name;
  }

    /** Gets file identifier for FITS file */
    public File getFile(){
	return file;
  }
}




