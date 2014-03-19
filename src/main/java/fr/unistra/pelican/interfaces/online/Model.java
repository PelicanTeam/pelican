package fr.unistra.pelican.interfaces.online;

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
	private static File[] filtering(File[] file) {

		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				try {
					String path = file[i].toString().replace("classes/", "")
							.replace(".class", "").replaceAll(File.separator,
									".");
					System.err.println(path);
					Class classe = Class.forName(path);
					if (classe.isAnonymousClass()) {
						file[i].delete();
					} else {
						if (classe.isInterface()) {
							file[i].delete();
						} else {
							if (classe.isMemberClass()) {
								file[i].delete();
							} else {
								if (classe.isLocalClass()) {
									file[i].delete();
								} else {
									boolean implement = classe
											.getSuperclass()
											.toString()
											.contains(
													"fr.unistra.pelican.Algorithm");
									if (implement == false) {
										file[i].delete();
									}
								}
							}
						}
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}
	
	
	// Ecrire une mï¿œthode qui ajoute une image ï¿œ la liste, et mï¿œthode qui supprime une image de la liste
}
