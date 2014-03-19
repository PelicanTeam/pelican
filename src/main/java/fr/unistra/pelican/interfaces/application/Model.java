package fr.unistra.pelican.interfaces.application;

import java.io.File;
import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.unistra.pelican.Image;

public class Model {

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 * Reference to the controller.
	 */
	public GlobalController controller;

	/**
	 * list containing all the opened images.
	 */
	public ArrayList<Image> imageList;

	/**
	 * 
	 */
	static DefaultMutableTreeNode node;

	/***************************************************************************
	 * 
	 * 
	 * Constructors
	 * 
	 * 
	 **************************************************************************/

	public Model(GlobalController controller) {

		this.controller = controller;
		imageList = new ArrayList<Image>();
	}

	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	/**
	 * This method sorts the array parameter
	 * 
	 * @param file
	 * @return the sorted array of files
	 */
	static File[] listSorting(File[] file) {

		int length = file.length;

		while (length > 1) {
			for (int i = 0; i < length - 1; i++) {
				String u = file[i].toString().substring(
						file[i].toString().lastIndexOf(File.separator) + 1);
				String v = file[i + 1].toString().substring(
						file[i + 1].toString().lastIndexOf(File.separator) + 1);
				if (u.compareTo(v) > 0) {
					File tmp = file[i];
					file[i] = file[i + 1];
					file[i + 1] = tmp;
				}
			}
			length--;
		}
		return file;
	}

	/**
	 * This method checks all the algorithm are implementing the abstract class
	 * Algorithm
	 * 
	 * @param file
	 * @return the filtered array of files
	 */
	public static File[] filtering(File[] file) {

		ArrayList<File> tmp = new ArrayList<File>();
		File[] result;
		Boolean hasToBeFiltered;

		for (int i = 0; i < file.length; i++) {			
			hasToBeFiltered = false;
			
			if (file[i].isFile()) {
				
				try {
					String path = file[i].toString().replace("classes/", "")
							.replace(".class", "").replaceAll(File.separator,
									".");
					Class classe = Class.forName(path);
					if (classe.isAnonymousClass()) {
						hasToBeFiltered = true;
					} else {
						if (classe.isInterface()) {
							hasToBeFiltered = true;
						} else {
							if (classe.isMemberClass()) {
								hasToBeFiltered = true;
							} else {
								if (classe.isLocalClass()) {
									hasToBeFiltered = true;
								} else {
									boolean implement = classe
											.getSuperclass()
											.toString()
											.contains(
													"fr.unistra.pelican.Algorithm");
									if (implement == false) {
										hasToBeFiltered = true;
									}
								}
							}
						}
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (hasToBeFiltered == false) {
					tmp.add(file[i]);
				}
			}
			else {
				tmp.add(file[i]);
			}
		}
		
		
		result = new File[tmp.size()];
		for(int i = 0; i < tmp.size(); i++) {
			result[i] = tmp.get(i);
		}		
		return result;		
	}

	/**
	 * Manages a new image adding.
	 * 
	 */
	public void addImage(ArrayList<Image> imageList, Image image) {

		Boolean present = false;

		for (int i = 0; i < imageList.size(); i++) {
			if (imageList.get(i).getName().equals(image.getName())) {
				present = true;
			}
		}
		if (!present) {
			imageList.add(image);
		}
	}
}
