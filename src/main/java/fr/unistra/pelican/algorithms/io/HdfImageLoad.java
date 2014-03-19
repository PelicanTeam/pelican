package fr.unistra.pelican.algorithms.io;

import fr.unistra.pelican.*;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.*; // the common object package
import ncsa.hdf.object.h5.*; // the HDF5 implementation

/**
 * Loads HDF5 images. Inspired from the ImageJ plug-in from LMB group in
 * Freiburg
 * 
 * @author Lefevre
 */

public class HdfImageLoad extends Algorithm {

	/**
	 * Input parameter
	 */
	public String filename;

	/**
	 * The image index to load within the input file
	 */
	public int index = 0;

	/**
	 * Output parameter
	 */
	public Image output;

	private static int xDim = 1;
	private static int yDim = 1;
	private static int zDim = 1;
	private static int tDim = 1;
	private static int bDim = 1;

	/**
	 * Constructor
	 * 
	 */
	public HdfImageLoad() {
		super.inputs = "filename";
		super.options = "index";
		super.outputs = "output";
	}

	/**
	 * Loads HDF5 images.
	 * 
	 * @param filename
	 *            Filename of the HDF5 image.
	 * @return The HDF image.
	 */
	public static Image exec(String filename) {
		return (Image) new HdfImageLoad().process(filename);
	}

	public static Image exec(String filename, int index) {
		return (Image) new HdfImageLoad().process(filename, index);
	}

	public void launch() {
		// Ouverture du fichier
		H5File inFile = null;
		inFile = new H5File(filename, H5File.READ);
		try {
			inFile.open();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Affichage des métadonnées
		Group rootNode = (Group) ((javax.swing.tree.DefaultMutableTreeNode) inFile
				.getRootNode()).getUserObject();
		List<Dataset> varList = getDataSetList(rootNode,
				new ArrayList<Dataset>());
		for (int i = 0; i < varList.size(); ++i) {
			Dataset var = varList.get(i);
			int rank = var.getRank();
			String title = rank + "D: " + var.getFullName() + "              "
					+ var.getDatatype().getDatatypeDescription() + "( ";
			long[] extent = var.getDims();
			for (int d = 0; d < rank; ++d) {
				if (d != 0)
					title += "x";
				title += extent[d];
			}
			title += ")";
			System.out.println(title);
		}

		// Lecture des paramètres pour le chargement
		Dataset var = varList.get(index);
		int rank = var.getRank();
		Datatype datatype = var.getDatatype();
		long[] extent = var.getDims();
		System.out.println("Reading Variable: " + var.getName());
		System.out.println("   Rank = " + rank + ", Data-type = "
				+ datatype.getDatatypeDescription());
		System.out.println("   Data-type class = "
				+ datatype.getDatatypeClass() + ", Data-type size = "
				+ datatype.getDatatypeSize());
		System.out.print("   Extent in px (level,row,col):");
		for (int d = 0; d < rank; ++d)
			System.out.print(" " + extent[d]);
		System.out.println("");
		long[] selected = var.getSelectedDims();
		for (int k = 0; k < selected.length; k++)
			selected[k] = extent[k];

		// Initialisation des dimensions
		if (extent.length >= 3) {
			xDim = (int) extent[2];
			yDim = (int) extent[1];
			zDim = (int) extent[0];
		}
		if (extent.length == 4) {
			bDim = (int) extent[3];
		}

		// Construction de l'image
		Object dataObject = null;
		try {
			dataObject = var.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(dataObject);

		if (dataObject instanceof byte[]) {
			output = new ByteImage(xDim, yDim, zDim, tDim, bDim);
			byte[] data = (byte[]) dataObject;
			int k = 0;
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							for (int b = 0; b < bDim; b++)
								output.setPixelXYZTBByte(x, y, z, t, b,
										(int) data[k++]);
		}
		else if (dataObject instanceof int[]) {
			output = new IntegerImage(xDim, yDim, zDim, tDim, bDim);
			int[] data = (int[]) dataObject;
			int k = 0;
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							for (int b = 0; b < bDim; b++)
								output.setPixelXYZTBInt(x, y, z, t, b,
										(int) data[k++]);
		}
		else if (dataObject instanceof short[]) {
			output = new IntegerImage(xDim, yDim, zDim, tDim, bDim);
			short[] data = (short[]) dataObject;
			int k = 0;
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							for (int b = 0; b < bDim; b++)
								output.setPixelXYZTBInt(x, y, z, t, b,
										(int) data[k++]);
		}
		else if (dataObject instanceof double[]) {
			output = new DoubleImage(xDim, yDim, zDim, tDim, bDim);
			double[] data = (double[]) dataObject;
			int k = 0;
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							for (int b = 0; b < bDim; b++)
								output.setPixelXYZTBDouble(x, y, z, t, b,
										(double) data[k++]);
		}
		else if (dataObject instanceof float[]) {
			output = new DoubleImage(xDim, yDim, zDim, tDim, bDim);
			float[] data = (float[]) dataObject;
			int k = 0;
			for (int t = 0; t < tDim; t++)
				for (int z = 0; z < zDim; z++)
					for (int y = 0; y < yDim; y++)
						for (int x = 0; x < xDim; x++)
							for (int b = 0; b < bDim; b++)
								output.setPixelXYZTBDouble(x, y, z, t, b,
										(double) data[k++]);
		}
		else {
			System.err.print("datatype not yet supported for :"+dataObject+" (");
			if (dataObject.getClass().isArray())
				System.err.print(dataObject.getClass().getComponentType());
			else System.err.print(dataObject.getClass());
			System.err.println(")");
		}

		// Mise en couleur si nécessaire
		if (bDim == 3)
			output.setColor(true);

		// Fermeture du fichier
		try {
			inFile.close();
		} catch (HDF5Exception e) {
			e.printStackTrace();
		}

	}

	private static List<Dataset> getDataSetList(Group g, List<Dataset> datasets) {
		if (g == null)
			return datasets;

		List members = g.getMemberList();
		int n = members.size();
		HObject obj = null;
		for (int i = 0; i < n; i++) {
			obj = (HObject) members.get(i);
			if (obj instanceof Dataset) {
				((Dataset) obj).init();
				datasets.add((Dataset) obj);
				System.out.println(obj.getFullName());
			} else if (obj instanceof Group) {
				datasets = (getDataSetList((Group) obj, datasets));
			}
		}
		return datasets;
	}

	public static void main(String args[]) throws Exception {
		String filename = "/home/miv/lefevre/freiburg/PIN1_K_RAM2.h5";
		if (args.length != 0)
			filename = args[0];
		for (int i = 0; i < 4; i++)
			Viewer2D.exec(HdfImageLoad.exec(filename, i));
	}

}