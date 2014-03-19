package fr.unistra.pelican.interfaces.online;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class MenuView extends JFrame {

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
	private static final long serialVersionUID = 1L;

	/**
	 * The menu panel displays a basic application menu.
	 */
	private JPanel menuPanel;

	/**
	 * The menuFrame 's menu bar.
	 */
	private JMenuBar menuBar = new JMenuBar();

	/**
	 * The different menu of the menu bar.
	 */
	private JMenu menu;

	/**
	 * The menu displaying the loaded images.
	 */
	public JMenu loadedImagesMenu;

	/**
	 * The different menu items.
	 */
	private JMenuItem menuItem;

	/**
	 * The constraints for the layout manager.
	 */
	public GridBagConstraints constraints;

	/**
	 * List used to browse recursively all the existing algorithms.
	 */
	private ArrayList<JMenu> menuList = new ArrayList<JMenu>();

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

	/**
	 * Constructor
	 */
	public MenuView(GlobalController controller) {

		super();

		this.controller = controller;

		// this.setTitle("PELICAN");
		Dimension dim = new Dimension(1400, 45);
		this.setPreferredSize(dim);
		this.setLocation(0, 0);
		// this.setResizable(true);
		menuPanel = new JPanel();
		this.add(menuPanel);

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		JMenuItem openItem = new JMenuItem("Open");
		fileMenu.add(openItem);
		openItem
				.addActionListener(controller.new GlobalController_Filemenu_actionAdapter());
		loadedImagesMenu = new JMenu("Loaded Images");
		fileMenu.add(loadedImagesMenu);

//		String path = File.separator + "home" + File.separator + "florent"
//				+ File.separator + "Workspace 2" + File.separator + "pelican"
//				+ File.separator + "classes" + File.separator + "fr"
//				+ File.separator + "unistra" + File.separator + "pelican"
//				+ File.separator + "algorithms" + File.separator;
		
		String path = "classes" + File.separator + "fr" + File.separator
		+ "unistra" + File.separator + "pelican" + File.separator
		+ "algorithms" + File.separator;
		
		Tree tree = new Tree("algorithm", path);
		menuBarBuilding(tree.getNodes());
		this.setJMenuBar(menuBar);
		menuBar.setOpaque(true);

		this.setVisible(true);
		// this.pack();

		// The close operation
		// this.setDefaultCloseOperation(controller.closeOperation());
	}

	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Method used to build the menuBar of the menuFrame.
	 * 
	 */
	private void menuBarBuilding(ArrayList<Tree> nodes) {

		for (int i = 0; i < nodes.size(); i++) {
			// Add a menu
			if (!nodes.get(i).isLeaf()) {
				if (menuList.isEmpty()) {
					menu = new JMenu(nodes.get(i).getValue());
					menuBar.add(menu);
					menuList.add(menu);
					menuBarBuilding(nodes.get(i).getNodes());
					menuList.remove(menuList.size() - 1);
				} else {
					menu = new JMenu(nodes.get(i).getValue());
					menuList.get(menuList.size() - 1).add(menu);
					menuList.add(menu);
					menuBarBuilding(nodes.get(i).getNodes());
					menuList.remove(menuList.size() - 1);
				}
			}
			// Add an item
			else {
				// It recreates the full name of the file from example.java to
				// fr.unistra.pelican.example
				String fullPathName = "fr.unistra.pelican.algorithms.";
				for (int j = 0; j < menuList.size(); j++) {
					fullPathName = fullPathName + menuList.get(j).getLabel()
							+ ".";
				}
				fullPathName = fullPathName + nodes.get(i).getValue();

				Object put = controller.algoNameMapping.put(nodes.get(i)
						.getValue(), fullPathName);
				menuItem = new JMenuItem(nodes.get(i).getValue());
				menuItem
						.addActionListener(controller.new GlobalController_menu_actionAdapter(
								this));
				menuList.get(menuList.size() - 1).add(menuItem);
			}
		}
	}

}
