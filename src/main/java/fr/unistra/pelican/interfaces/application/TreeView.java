package fr.unistra.pelican.interfaces.application;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeView extends JFrame {

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 */
	private static final long serialVersionUID = -8255386425207433726L;

	/**
	 * This panel displays the tree of all the pelican algorithms.
	 */
	private JScrollPane algoTreePanel;

	/**
	 * List used to browse recursively all the existing algorithms.
	 */
	private ArrayList<DefaultMutableTreeNode> treeList = new ArrayList<DefaultMutableTreeNode>();

	/**
	 * The root of the algorithm tree.
	 */
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(
			"algorithms");

	/**
	 * The algorithm tree.
	 */
	public JTree tree = new JTree(root);

	/**
	 * A node of the algorithm tree.
	 */
	private DefaultMutableTreeNode node;

	/**
	 * List used to browse recursively all the existing algorithms.
	 */
	private ArrayList<DefaultMutableTreeNode> menuList = new ArrayList<DefaultMutableTreeNode>();

	/**
	 * 
	 * Reference to the controller.
	 */
	public GlobalController controller;

	/***************************************************************************
	 * 
	 * 
	 * Constructors
	 * 
	 * 
	 **************************************************************************/

	public TreeView(GlobalController controller) throws HeadlessException {

		super();

		this.controller = controller;

		/* The algoTreeFrame creation */
		this.setTitle("Algorithms");
		this.setLocation(0, 70);
		Dimension dim = new Dimension(370, 500);
		this.setPreferredSize(dim);
		this.setResizable(true);
		algoTreePanel = new JScrollPane(tree);
		tree
				.addMouseListener(controller.new GlobalController_tree_actionAdapter(
						this));
		this.add(algoTreePanel);
		this.setVisible(true);
		this.pack();

		// The close operation
		this.setDefaultCloseOperation(controller.closeOperation());

		// builds the algorithm tree of the algoTreeFrame.
		String path = "classes" + File.separator + "fr" + File.separator
				+ "unistra" + File.separator + "pelican" + File.separator
				+ "algorithms" + File.separator;
		Tree dataTree = new Tree("algorithm", path);
		algoTreeBuilding(dataTree.getNodes(), root);

	}

	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Method used to build the algorithm tree of the algoTreeFrame.
	 * 
	 */
	private void algoTreeBuilding(ArrayList<Tree> nodes,
			DefaultMutableTreeNode parent) {

		for (int i = 0; i < nodes.size(); i++) {

			// Add a menu
			if (!nodes.get(i).isLeaf()) {
				if (menuList.isEmpty()) {

					node = new DefaultMutableTreeNode(nodes.get(i).getValue());
					parent.add(node);
					menuList.add(node);
					algoTreeBuilding(nodes.get(i).getNodes(), node);
					menuList.remove(menuList.size() - 1);

				} else {
					node = new DefaultMutableTreeNode(nodes.get(i).getValue());
					menuList.get(menuList.size() - 1).add(node);
					menuList.add(node);
					algoTreeBuilding(nodes.get(i).getNodes(), node);
					menuList.remove(menuList.size() - 1);
				}

			} else {

				// It recreates the full name of the file from example.java to
				// fr.unistra.pelican.example
				String fullPathName = "fr.unistra.pelican.algorithms.";
				for (int j = 0; j < menuList.size(); j++) {
					fullPathName = fullPathName + menuList.get(j).toString()
							+ ".";
				}
				fullPathName = fullPathName + nodes.get(i).getValue();

				node = new DefaultMutableTreeNode(nodes.get(i).getValue());
				parent.add(node);
			}
		}
	}
}
