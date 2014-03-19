package fr.unistra.pelican.interfaces.application;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.interfaces.application.inputs.InputType;

public class GlobalController {

	
	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/
	
	/**
	 * The list to contain the output objects.
	 */
	public ArrayList<Object> parameterArray;
	
	/**
	 * Hashmap used to use to link the algorithm names with the algorithm full
	 * paths.
	 */
	public HashMap algoNameMapping = new HashMap();

	/**
	 * To ensure the user didn't cancel the launch of the algorithm.
	 */
	public boolean fired;
	
	/**
	 * Array which contains the instances of the parameter classes needed for
	 * the execution of the current algorithm. This attribute is filled by the
	 * inputs classes.
	 */
	public Object[] parameterInstanceArray;

	/**
	 * Reference to the menu GUI.
	 */
	public MenuView menu;
	
	/**
	 * Reference to the tree GUI.
	 */
	public TreeView treeview;
	
	/**
	 * Reference to the datas.
	 */
	public Model data;	
	
	/**
	 * 
	 */
	public DialogView parameterBox;
	
	/**
	 * The constraints for the layout manager.
	 */
	public GridBagConstraints constraints = new GridBagConstraints();

	



	
	/***************************************************************************
	 * 
	 * 
	 * Constructors 
	 * 
	 * 
	 **************************************************************************/
	
	/**
	 * 
	 */
	public GlobalController() {
		
		menu = new MenuView(this);
		treeview = new TreeView(this);
		data = new Model(this);
		
	}
	
	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/
	
	/**
	 * Manages the adding of a new loaded image
	 * 
	 */
	public void addLoadedImage(Image image) {

		Boolean present = false;

		for (int i = 0; i < menu.loadedImagesMenu.getItemCount(); i++) {
			if (menu.loadedImagesMenu.getItem(i).getText().equals(image.getName())) {
				present = true;
			}
		}

		if (!present) {
			JMenuItem imageItem = new JMenuItem(image.getName());
			menu.loadedImagesMenu.add(imageItem);
			imageItem
					.addActionListener(new GlobalController_LoadedImagesmenu_actionAdapter(menu));
		}
	}
	
	/**
	 * Returns the image defined by the parameter name.
	 * 
	 * @param name
	 * @return
	 */
	public Image getLoadedImage(String name) {

		Image loadedImage = null;
		for (int i = 0; i < data.imageList.size(); i++) {
			if (data.imageList.get(i).getName().equals(name)) {
				loadedImage = data.imageList.get(i);
			}
		}
		return loadedImage;
	}
	
	/**
	 * 
	 * @param parameterTypesList
	 * @return
	 */
	public ArrayList<Object> inputAction(String algoName,
			Class[] inputTypesList, ArrayList<String> inputNamesList,
			Class[] optionTypesList, ArrayList<String> optionNamesList) {

		// The constraints for the layout manager.
		GridBagConstraints constraints = new GridBagConstraints();
		
		// Creates the list which will contain the input objects.
		parameterArray = new ArrayList<Object>();
		// Builds the parameter dialog which use to take the parameter from the
		// user
		parameterBox = new DialogView(this);

		parameterInstanceArray = new Object[inputTypesList.length
				+ optionTypesList.length];

		// Action for the inputs
		for (int i = 0; i < inputTypesList.length; i++) {
			String className = inputTypesList[i].getSimpleName();


			if (className.contains("[]")) {
				className = className.replace("[]", "Array");
				className = className.substring(0, 1).toUpperCase()
						+ className.substring(1);
			}
			String classCalled = "Input" + className;

			try {
				Class classe = Class.forName("fr.unistra.pelican.interfaces.application.inputs."
						+ classCalled);
				Class[] methodTypeParameters = { GlobalController.class, String.class,
						String.class, int.class, boolean.class };
				Constructor constructor = classe
						.getConstructor(methodTypeParameters);
				Object[] methodObjectParameters = { this, algoName,
						inputNamesList.get(i), i, false };
				JPanel inputPanel = (JPanel) constructor
						.newInstance(methodObjectParameters);

				
				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.weightx = 0.5;
				constraints.gridx = 0;
				constraints.gridy = i;
				parameterBox.getPanel().add(inputPanel, constraints);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}

		}

		// Separates the inputs from the options.
		JLabel spaceLabel2 = new JLabel("[options]");
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.5;
		constraints.gridy = inputTypesList.length;
		parameterBox.getPanel().add(spaceLabel2, constraints);

		// Action for the options
		for (int i = 0; i < optionTypesList.length; i++) {
			String className = optionTypesList[i].getSimpleName();
			String classCalled = "Input" + className;
			try {
				Class classe = Class.forName("fr.unistra.pelican.interfaces.application.inputs."
						+ classCalled);
				Class[] methodTypeParameters = { GlobalController.class, String.class,
						String.class, int.class, boolean.class };
				Constructor constructor = classe
						.getConstructor(methodTypeParameters);
				// 2 is added for aesthetic consideration (space separation)
				Object[] methodObjectParameters = { this, algoName,
						optionNamesList.get(i), i + inputTypesList.length + 1,
						true };
				JPanel inputPanel = (JPanel) constructor
						.newInstance(methodObjectParameters);

				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.weightx = 0.5;
				constraints.gridy = i + inputTypesList.length + 1;
				parameterBox.getPanel().add(inputPanel, constraints);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}

		}
		parameterBox.pack();
		parameterBox.setVisible(true);

		if (fired) {
			return parameterArray;
		} else {
			return null;
		}
	}

	/**
	 * Take action according to the type of output.
	 * 
	 * @param parameterTypesList
	 * @param parameterNamesList
	 */
	public void outputAction(Class[] outputTypesList,
			ArrayList<String> outputNamesList, ArrayList<String> outputList) {

		for (int i = 0; i < outputList.size(); i++) {
			String className = outputTypesList[i].getSimpleName();
			String classCalled = "Output" + className;

			try {
				Class classe = Class.forName("fr.unistra.pelican.interfaces.application.outputs."
						+ classCalled);
				Class[] methodTypeParameters = { GlobalController.class, String.class,
						Object.class };
				Method method = classe.getMethod("outputPrinting",
						methodTypeParameters);
				Object[] methodObjectParameters = { this,
						outputNamesList.get(i) + data.imageList.size(),
						outputList.get(i) };			
				method.invoke(classe.newInstance(), methodObjectParameters);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Method used to close the entire application.
	 * 
	 * @return EXIT_ON_CLOSE to close the application.
	 */
	public int closeOperation() {

		//fermer toutes les instances de vues ouvertes	
		return JFrame.EXIT_ON_CLOSE;

	}	
	

	/**
	 * Method used to get the javadoc of a class.
	 * 
	 * @param className
	 *            The selected class
	 * @return The javadoc of the className
	 */
	public static String getJavadoc(String className) {

		String javadoc = "";

		try {
			// Initialization of the file reader
			File file = new File("reports" + File.separator + "javadoc.txt");
			int size = (int) file.length();
			int chars_read = 0;
			FileReader in = new FileReader(file);
			char[] data = new char[size];

			// Reading
			while (in.ready()) {
				chars_read += in.read(data, chars_read, size - chars_read);
			}
			// Gets the text contained by javadoc.txt
			javadoc = new String(data, 0, chars_read);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = javadoc.substring(javadoc
				.indexOf("[" + className + "]"), javadoc.indexOf("[|"
				+ className + "]"));
		result = result.substring(result.indexOf("]") + 1);
		return result;
	}

	/**
	 * Method used to get the javadoc of a class.
	 * 
	 * @param className
	 *            The selected class
	 * @param attributeName
	 *            The selected attribute from the className
	 * @return The javadoc of the attributeName of the className
	 */
	public static String getJavadoc(String className, String attributeName) {

		String javadoc = "";

		try {
			// Initialization of the file reader
			File file = new File("reports" + File.separator + "javadoc.txt");
			int size = (int) file.length();
			int chars_read = 0;
			FileReader in = new FileReader(file);
			char[] data = new char[size];

			// Reading
			while (in.ready()) {
				chars_read += in.read(data, chars_read, size - chars_read);
			}
			// Gets the text contained by javadoc.txt
			javadoc = new String(data, 0, chars_read);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = javadoc.substring(javadoc.indexOf("[" + className + ":"
				+ attributeName + "]"), javadoc.indexOf("[|" + className + ":"
				+ attributeName + "]"));
		result = result.substring(result.indexOf("]") + 1);
		return result;
	}

	/***************************************************************************
	 * 
	 * 
	 * Menu class Listener
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Menu actionAdapter class launches the selected algorithm.
	 * 
	 * @author florent
	 * 
	 */
	public class GlobalController_menu_actionAdapter implements
			java.awt.event.ActionListener {
		MenuView view;

		GlobalController_menu_actionAdapter(MenuView view) {
			this.view = view;
		}

		public void actionPerformed(ActionEvent e) {
			Class classe = null;
			Algorithm object = null;
			ArrayList<Object> inputAndOptionList = new ArrayList<Object>();

			// Creates a class of the selected algorithm.
			try {
				classe = Class.forName(algoNameMapping.get(e.getActionCommand())
						.toString());
				object = (Algorithm) classe.newInstance();

			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}

			// Recovers the arguments needed to launch the algorithm.
			inputAndOptionList = inputAction(classe.getSimpleName(),
					object.getInputTypes(), object.getInputNames(), object
							.getOptionTypes(), object.getOptionNames());

			if (inputAndOptionList != null) {
				// Sets the inputs of the algorithm (with the options).
				object.setInput(inputAndOptionList);

				// Launches the algorithm.
				object.launch();

				// Gets the output type and launches the appropriate action
				if (object.getOutput() != null) {
					outputAction(object.getOutputTypes(), object
							.getOutputNames(), object.getOutput());
				}
			}
		}
	}

	/***************************************************************************
	 * 
	 * 
	 * Tree class Listener
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Menu actionAdapter class launches the selected algorithm.
	 * 
	 * @author florent
	 * 
	 */
	public class GlobalController_tree_actionAdapter extends MouseAdapter {

		TreeView treeview;

		GlobalController_tree_actionAdapter(TreeView treeview) {
			this.treeview = treeview;
		}

		public void mouseClicked(MouseEvent e) {

			// Gets the location of the click
			int selRow = treeview.tree.getRowForLocation(e.getX(), e.getY());
			// Gets the object
			TreePath selPath = treeview.tree.getPathForLocation(e.getX(), e.getY());
			if (selRow != -1) {
				// If it is a double click
				if (e.getClickCount() == 2) {
					String path = selPath.toString();
					String choosenAlgo = selPath.toString().substring(
							selPath.toString().lastIndexOf(",") + 2,
							selPath.toString().indexOf("]"));

					// If the first letter is in uppercase this is not a
					// path but an algorithm
					if (java.lang.Character.isUpperCase(choosenAlgo.charAt(0))) {
						String[] tmp2 = path.substring(0, path.length() - 1)
								.split(",");
						String algoFullPath = "";
						// It reconstitutes the full path of the algorithm
						for (int i = 0; i < tmp2.length - 1; i++) {
							algoFullPath = algoFullPath + tmp2[i].substring(1)
									+ ".";
						}
						algoFullPath = "fr.unistra.pelican." + algoFullPath
								+ choosenAlgo;

						// Now the algorithm is properly called
						Class classe = null;
						Algorithm object = null;
						ArrayList<Object> inputAndOptionList = new ArrayList<Object>();

						// Creates a class of the selected algorithm.
						try {
							classe = Class.forName(algoFullPath);
							object = (Algorithm) classe.newInstance();

						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						} catch (InstantiationException e1) {
							e1.printStackTrace();
						} catch (IllegalAccessException e1) {
							e1.printStackTrace();
						}

						// Recovers the arguments needed to launch the
						// algorithm.
						inputAndOptionList = inputAction(classe
								.getSimpleName(), object.getInputTypes(),
								object.getInputNames(),
								object.getOptionTypes(), object
										.getOptionNames());

						if (inputAndOptionList != null) {
							// Sets the arguments of the algorithm.
							object.setInput(inputAndOptionList);

							// Launches the algorithm.
							object.launch();

							// Gets the output type and launches the
							// appropriate action
							if (object.getOutput() != null) {
								outputAction(object.getOutputTypes(),
										object.getOutputNames(), object
												.getOutput());
							}
						}
					}
				}
			}
		}
	}

	/***************************************************************************
	 * 
	 * 
	 * Launch button class Listener
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 * @author florent
	 * 
	 */
	public class GlobalController_launchButton_actionAdapter implements
			java.awt.event.ActionListener {

		DialogView view;

		GlobalController_launchButton_actionAdapter(DialogView view) {
			this.view = view;
		}

		public void actionPerformed(ActionEvent e) {

			for (int i = 0; i < parameterInstanceArray.length; i++) {
				InputType o = (InputType) parameterInstanceArray[i];
//				if (!o.validate()) {
//					o.getErrorMsg();				
//					return;
//					}
				o.fire();

			}
			fired = true;
			view.dispose();
		}
	}

	/***************************************************************************
	 * 
	 * 
	 * Cancel button class Listener
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 * @author florent
	 * 
	 */
	public class GlobalController_cancelButton_actionAdapter implements
			java.awt.event.ActionListener {	

		DialogView view;

		GlobalController_cancelButton_actionAdapter(DialogView view) {
			this.view = view;
		}
		
		public void actionPerformed(ActionEvent e) {
			fired = false;
			view.dispose();
		}
	}

	/***************************************************************************
	 * 
	 * 
	 * File menu class Listener
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 * @author florent
	 * 
	 */
	public class GlobalController_Filemenu_actionAdapter implements
			java.awt.event.ActionListener {
		
	
		public void actionPerformed(ActionEvent e) {

			// Manages the dialog frame
			JFrame openDialogFrame = new JFrame("Open");
			JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(openDialogFrame);
			Image result = (Image) ImageLoader.exec(fc.getSelectedFile()
					.getPath());
			// To set the image name (useless but maybe useful in the future)
			result.setName(fc.getSelectedFile().getPath()
					.substring(
							fc.getSelectedFile().getPath().lastIndexOf(
									File.separator) + 1,
							fc.getSelectedFile().getPath().lastIndexOf(".")));
			Viewer2D.exec(result, result.getName());

			// to re-use the opened images
			data.imageList.add(result);
			addLoadedImage(result);
		}
	}

	/***************************************************************************
	 * 
	 * 
	 * Loaded images menu class Listener
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 * @author florent
	 * 
	 */
	public class GlobalController_LoadedImagesmenu_actionAdapter implements
			java.awt.event.ActionListener {
	
		MenuView view;

		GlobalController_LoadedImagesmenu_actionAdapter(MenuView view) {
			this.view = view;
		}	

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			String imageName = arg0.getActionCommand().toString();
			Image imageToBeLoaded = null;

			for (int i = 0; i < data.imageList.size(); i++) {
				if (data.imageList.get(i).getName().equals(imageName)) {
					imageToBeLoaded = data.imageList.get(i);
				}
			}
			Viewer2D.exec(imageToBeLoaded, imageToBeLoaded.getName());
		}
	}
}
