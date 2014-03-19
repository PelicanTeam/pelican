package fr.unistra.pelican.interfaces.online;

import java.io.File;
import java.util.ArrayList;

/**
 * 
 * @author florent
 * 
 */
public class Tree {

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * ï¿œtiquette
	 */
	private String val;

	/**
	 * fils
	 */
	private ArrayList<Tree> nodes;

	/***************************************************************************
	 * 
	 * 
	 * Constructors
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Constructeur des feuilles
	 */
	public Tree(String val) {
		this.val = val;
		this.nodes = new ArrayList<Tree>();
	}

	/**
	 * Constructeur des noeuds internes
	 * 
	 * @param val
	 * @param node
	 */
	public Tree(String val, ArrayList<Tree> nodes) {
		this.val = val;
		this.nodes = nodes;

	}

	public Tree(String root, String path) {
		this.val = root;

		generateNodesFromPath(path, this);
	}

	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	/**
	 * getter nodes
	 */
	public ArrayList<Tree> getNodes() {
		return this.nodes;
	}

	/**
	 * getter value
	 */
	public String getValue() {
		return this.val;
	}

	/**
	 * setter left
	 */
	public void setNodes(ArrayList<Tree> nodes) {
		this.nodes = nodes;
	}

	/**
	 * setter value
	 */
	public void setVal(String val) {
		this.val = val;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isLeaf() {
		if (this.nodes.size() == 0)
			return true;
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public int numberOfLeaves() {
		return this.nodes.size();
	}

	/**
	 * 
	 * @return
	 */
	public void insertNode(Tree node) {
		this.nodes.add(node);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public void generateNodesFromPath(String path,Tree root) {

		root.nodes = new ArrayList<Tree>();

		File directory = new File(path);
		File[] subfiles = directory.listFiles();
		// It sorts the algorithms alphabetically
		subfiles = Model.listSorting(subfiles);
		// It checks that all the algorithm are implementing the abstract class
		// Algorithm
		// subfiles = filtering(subfiles);

		for (int i = 0; i < subfiles.length; i++) {
			if (subfiles[i].isDirectory() && !subfiles[i].isHidden()) {
				String nodeName = subfiles[i].getPath().substring(
						subfiles[i].getPath().lastIndexOf(File.separator) + 1,
						subfiles[i].getPath().length());
				// We do not want to use the experimental files
				if (!nodeName.equals("experimental")) {
					Tree node = new Tree(nodeName);
					root.nodes.add(node);			
					generateNodesFromPath(subfiles[i].getPath(), node);
				}
			} else if (subfiles[i].isFile()) {
				String leafName = subfiles[i].getPath().substring(
						subfiles[i].getPath().lastIndexOf(File.separator) + 1,
						subfiles[i].getPath().indexOf("."));
				Tree node = new Tree(leafName);
				root.nodes.add(node);			
			}
		}

	}

	/**
	 * 
	 */
	public static void main(String[] args) {

		String path = "classes" + File.separator + "fr" + File.separator
				+ "unistra" + File.separator + "pelican" + File.separator
				+ "algorithms" + File.separator;
		Tree tree = new Tree("algorithm", path);
		

	}
}
