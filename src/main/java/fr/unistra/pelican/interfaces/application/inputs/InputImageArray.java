package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.interfaces.application.GlobalController;

public class InputImageArray extends JPanel implements InputType,
		ActionListener {

	/**
	 * Reference to the view object.
	 */
	GlobalController controller;

	/**
	 * This attribute defines the range of this parameter among the other
	 * parameters.
	 */
	int parameterNumber;

	/**
	 * This attribute defines the number of element contained in the result
	 * array
	 */
	int arraySize;

	/**
	 * This attribute represents the resulting parameter.
	 */
	Image[] result;

	/**
	 * This attribute represents the temporary result (need to be transformed
	 * into an array)
	 */
	ArrayList<Image> tmpResult = new ArrayList<Image>();

	/**
	 * This frame contains the thumbnails panel
	 */
	JDialog thumbnailsFrame;

	/**
	 * This use to know if the parameter is called as an option or not
	 */
	boolean option;

	/**
	 * Is the number of image already loaded
	 */
	int numberOfImageLoaded;

	/**
	 * This attribute represents the resulting parameter.
	 */
	ArrayList<JPanel> panelArray = new ArrayList<JPanel>();
	
	/**
	 * The constraints for the layout manager.
	 */
	public GridBagConstraints constraints;

	/**
	 * Input constructor.
	 * 
	 * @param view
	 *            Reference to the main panel.
	 * @param algoName
	 *            Name of the algorithm which needs this parameter.
	 * @param parameterName
	 *            Name of the parameter.
	 * @param parameterNumber
	 *            Rank of this parameter between the other parameter of the
	 *            algorithm.
	 * @param option
	 *            This paramter is an option or not.
	 */
	public InputImageArray(GlobalController controller, String algoName, String parameterName,
			int parameterNumber, boolean option) {
		super();
		this.setLayout(new GridBagLayout());

		// Initializes the attributes
		this.controller = controller;
		this.parameterNumber = parameterNumber;
		this.option = option;

		// Initializes the number of image already loaded
		if (controller.data.imageList.isEmpty()) {
			numberOfImageLoaded = 0;
		} else {
			numberOfImageLoaded = controller.data.imageList.size();
		}

		parameterSeizing(controller, algoName, parameterName, parameterNumber, option);
	}

	/**
	 * This method allows the seize of the parameter by the user.
	 * 
	 * @param view
	 *            Reference to the main panel.
	 * @param algoName
	 *            Name of the algorithm which needs this parameter.
	 * @param parameterName
	 *            Name of the parameter.
	 * @param parameterNumber
	 *            Rank of this parameter between the other parameter of the
	 *            algorithm.
	 * @param option
	 *            This paramter is an option or not.
	 */
	public void parameterSeizing(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {

		arraySize = 1;

		JPanel line = new JPanel(new GridLayout(1, 6, 10, 10));
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.5;
		constraints.gridx = 0;
		constraints.gridy = arraySize;
		this.add(line, constraints);

		JLabel lab = new JLabel(parameterName);
		line.add(lab);
		lab.setToolTipText(controller.getJavadoc(algoName, parameterName));

		if (numberOfImageLoaded == 0) {

			JTextField tf = new JTextField("");
			;
			line.add(tf);

		} else {

			// Sets the combobox with all opened images
			String[] imageString = new String[controller.data.imageList.size()];
			for (int i = 0; i < controller.data.imageList.size(); i++) {
				imageString[i] = controller.data.imageList.get(i).getName();
			}
			// Creates the combo box, select item at index 0.
			JComboBox imageBox = new JComboBox(imageString);
			imageBox.setSelectedIndex(0);
			line.add(imageBox);
			imageBox
					.addActionListener(new InputImageArray_BrowseAndCombobox_actionAdapter(
							controller, 0));

			// Sets The pick button
			JButton pick = new JButton("Pick");
			line.add(pick);
			pick
					.addActionListener(new InputImageArray_PickButton_actionAdapter(
							controller, arraySize - 1));
		}

		JButton browse = new JButton("browse");
		line.add(browse);
		browse
				.addActionListener(new InputImageArray_BrowseAndCombobox_actionAdapter(
						controller, 0));

		JButton plusButton = new JButton("+");
		line.add(plusButton);
		plusButton.addActionListener(this);

		JButton minusButton = new JButton("-");
		line.add(minusButton);
		minusButton.addActionListener(this);

		panelArray.add(line);

		// This instance is added to input instances array
		if (option == false) {
			controller.parameterInstanceArray[parameterNumber] = this;
		} else {
			controller.parameterInstanceArray[parameterNumber - 1] = this;
		}
	}

	/**
	 * Uses to check if an image is not already present in the combobox list
	 * 
	 * @param imageName
	 *            Name of the image to be entered into the combobox list.
	 * @param combo
	 *            The checked combobox
	 * @return
	 */
	private Boolean alreadyPresent(String imageName, JComboBox combo) {

		Boolean present = false;

		for (int i = 0; i < combo.getItemCount(); i++) {
			String name = (String) combo.getItemAt(i);
			if (name.equals(imageName)) {
				present = true;
			}
		}
		return present;
	}

	/**
	 * Launch effect!
	 */
	public void fire() {

		result = new Image[arraySize];
		for (int i = 0; i < arraySize; i++) {

			if (tmpResult.size() == 1) {
				System.err.println("premiere condition");
				JComboBox combo = (JComboBox) panelArray.get(i).getComponent(1);
				int selectedIndex = combo.getSelectedIndex();
				result[i] = controller.data.imageList.get(selectedIndex);
				controller.addLoadedImage(result[i]);
			} else {
				result[i] = tmpResult.get(i);
				controller.addLoadedImage(result[i]);
			}			
		}

		if (option == false) {
			controller.parameterArray.add(parameterNumber, result);
		} else {
			controller.parameterArray.add(parameterNumber - 1, result);
		}
	}

	public void actionPerformed(ActionEvent arg0) {

		// The plus button add an element in the array
		if (arg0.getActionCommand().equals("+")) {

			arraySize++;
			tmpResult.add(null);

			JPanel line2 = new JPanel(new GridLayout(1, 6, 10, 10));
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 0.5;
			constraints.gridx = 0;
			constraints.gridy = arraySize;
			this.add(line2, constraints);

			JLabel lab2 = new JLabel("Element nï¿œ" + (arraySize));
			line2.add(lab2);

			if (numberOfImageLoaded == 0) {

				JTextField tf2 = new JTextField("");
				;
				line2.add(tf2);

			} else {

				// Sets the combobox with all opened images
				String[] imageString = new String[controller.data.imageList.size()];
				for (int i = 0; i < controller.data.imageList.size(); i++) {
					imageString[i] = controller.data.imageList.get(i).getName();
				}
				// Creates the combo box, select item at index 0.
				JComboBox imageBox2 = new JComboBox(imageString);
				imageBox2.setSelectedIndex(0);
				line2.add(imageBox2);
				imageBox2
						.addActionListener(new InputImageArray_BrowseAndCombobox_actionAdapter(
								controller, arraySize - 1));

				// Sets The pick button
				JButton pick2 = new JButton("Pick");
				line2.add(pick2);
				pick2
						.addActionListener(new InputImageArray_PickButton_actionAdapter(
								controller, arraySize - 1));

			}

			JButton browse2 = new JButton("browse");
			line2.add(browse2);
			browse2
					.addActionListener(new InputImageArray_BrowseAndCombobox_actionAdapter(
							controller, arraySize - 1));

			JLabel lab3 = new JLabel("        ");
			line2.add(lab3);

			JLabel lab4 = new JLabel("        ");
			line2.add(lab4);

			panelArray.add(line2);

			controller.parameterBox.pack();

			// The minus button remove an element from the array
		} else {

			// At leat one element remains
			if (panelArray.size() > 1) {

				tmpResult.remove(tmpResult.size() - 1);
				arraySize--;

				JPanel line2 = panelArray.get(panelArray.size() - 1);
				panelArray.remove(panelArray.size() - 1);
				line2.removeAll();
				controller.parameterBox.pack();

			}
		}
	}

	/***************************************************************************
	 * 
	 * 
	 * Pick button class Listener
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 * @author florent
	 * 
	 */
	public class InputImageArray_PickButton_actionAdapter implements
			java.awt.event.ActionListener {
		GlobalController controller;

		int rank;

		/**
		 * 
		 * @param view
		 */
		InputImageArray_PickButton_actionAdapter(GlobalController controller, int rank) {
			this.controller = controller;
			this.rank = rank;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {

			// This panel displays the available image

			JButton thumbnail;

			ImageIcon icon;

			thumbnailsFrame = new JDialog();
			thumbnailsFrame.setModal(true);
			thumbnailsFrame.setTitle("Choose an image");
			thumbnailsFrame.setLocation(370, 70);
			Dimension dim = new Dimension(600, 200);
			thumbnailsFrame.setMinimumSize(dim);
			thumbnailsFrame.setMaximumSize(dim);
			thumbnailsFrame.setResizable(true);
			JPanel panel = new JPanel();

			for (int i = 0; i < controller.data.imageList.size(); i++) {

				thumbnail = new JButton();
				// thumbnail.se
				thumbnail.setToolTipText(controller.data.imageList.get(i).getName());
				BufferedImage image = fr.unistra.pelican.util.Tools
						.pelican2Buffered(controller.data.imageList.get(i));
				icon = new ImageIcon(image.getScaledInstance(70, 70,
						java.awt.Image.SCALE_SMOOTH));
				thumbnail.setIcon(icon);
				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.weightx = 2.5;
				constraints.gridx = i % 4;
				constraints.gridy = i / 4;
				panel.add(thumbnail, constraints);
				thumbnail
						.addActionListener(new InputImageArray_imageButton_actionAdapter(
								controller, rank));

			}
			// thumbnailsFrame.
			thumbnailsFrame.setContentPane(panel);
			thumbnailsFrame.setVisible(true);
			thumbnailsFrame.pack();

		}
	}

	/**
	 * 
	 * @author florent
	 * 
	 */
	public class InputImageArray_imageButton_actionAdapter implements
			java.awt.event.ActionListener {
		
		GlobalController controller;
		
		int rank;

		/**
		 * 
		 * @param view
		 */
		InputImageArray_imageButton_actionAdapter(GlobalController controller, int rank) {
			this.controller = controller;
			this.rank = rank;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {

			JButton button = (JButton) e.getSource();
			tmpResult.add(rank, controller.getLoadedImage(button.getToolTipText()));
			
			JComboBox combo = (JComboBox) panelArray.get(rank)
			.getComponent(1);

			if (combo.getItemCount() > numberOfImageLoaded) {
				controller.data.imageList.remove(controller.data.imageList.size() - 1);
				controller.data.imageList.add(tmpResult.get(rank));
				combo.removeItemAt(combo.getItemCount() - 1);
				if (alreadyPresent(tmpResult.get(rank).getName(), combo)) {
					controller.data.imageList.remove(controller.data.imageList.size() - 1);
				} else {
					combo.addItem(tmpResult.get(rank).getName());
				}
				combo.setSelectedItem(tmpResult.get(rank).getName());

			} else {
				controller.data.imageList.add(tmpResult.get(rank));
				if (alreadyPresent(tmpResult.get(rank).getName(), combo)) {
					controller.data.imageList.remove(controller.data.imageList.size() - 1);
				} else {
					combo.addItem(tmpResult.get(rank).getName());
				}
				combo.setSelectedItem(tmpResult.get(rank).getName());
			}

			thumbnailsFrame.dispose();

		}
	}

	/***************************************************************************
	 * 
	 * 
	 * Browse button and combobox class Listener
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 * @author florent
	 * 
	 */
	public class InputImageArray_BrowseAndCombobox_actionAdapter implements
			java.awt.event.ActionListener {

		GlobalController controller;

		int rank;

		/**
		 * 
		 * @param view
		 */
		InputImageArray_BrowseAndCombobox_actionAdapter(GlobalController controller, int rank) {
			this.controller = controller;
			this.rank = rank;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			
			// Action of the browse button
			if (e.getActionCommand().equals("browse")) {
				JFrame openDialogFrame = new JFrame("Open");
				JFileChooser fc = new JFileChooser();
				controller.parameterBox.getPanel().add(fc);
				fc.showOpenDialog(openDialogFrame);

				tmpResult.add(rank, (Image) ImageLoader.exec(fc
						.getSelectedFile().getPath()));
				// Sets the image name
				tmpResult
						.get(rank)
						.setName(
								fc
										.getSelectedFile()
										.getPath()
										.substring(
												fc.getSelectedFile().getPath()
														.lastIndexOf(
																File.separator) + 1,
												fc.getSelectedFile().getPath()
														.lastIndexOf(".")));

				// It uses the combobox if an image was already loaded otherwise
				// it
				// uses the jtextfield
				if (numberOfImageLoaded == 0) {
					// If an image has already been set to the jtextfield (means
					// an
					// image has already been added to the view.imageList), then
					// this image has first to be remove from the the
					// view.imageList.
					if (numberOfImageLoaded < controller.data.imageList.size()) {
						controller.data.imageList.remove(controller.data.imageList.size() - 1);
						JTextField textfield = (JTextField) panelArray
								.get(rank).getComponent(1);
						textfield.setText(fc.getSelectedFile().getPath());
						controller.data.imageList.add(tmpResult.get(rank));
					} else {
						JTextField textfield = (JTextField) panelArray
								.get(rank).getComponent(1);
						textfield.setText(fc.getSelectedFile().getPath());
						controller.data.imageList.add(tmpResult.get(rank));
					}

				} else {
					JComboBox combo = (JComboBox) panelArray.get(rank)
							.getComponent(1);

					// This test use to only display the current browsed image
					if (combo.getItemCount() > numberOfImageLoaded) {
						controller.data.imageList.remove(controller.data.imageList.size() - 1);
						controller.data.imageList.add(tmpResult.get(rank));
						combo.removeItemAt(combo.getItemCount() - 1);
						if (alreadyPresent(tmpResult.get(rank).getName(), combo)) {
							controller.data.imageList.remove(controller.data.imageList.size() - 1);
						} else {
							combo.addItem(tmpResult.get(rank).getName());
						}
						combo.setSelectedItem(tmpResult.get(rank).getName());

					} else {
						controller.data.imageList.add(tmpResult.get(rank));
						if (alreadyPresent(tmpResult.get(rank).getName(), combo)) {
							controller.data.imageList.remove(controller.data.imageList.size() - 1);
						} else {
							combo.addItem(tmpResult.get(rank).getName());
						}
						combo.setSelectedItem(tmpResult.get(rank).getName());
					}
				}
			}
			// Action of the combobox
			if (e.getActionCommand().equals("comboBoxChanged")) {
				JComboBox combo = (JComboBox) panelArray.get(rank)
						.getComponent(1);
				tmpResult.add(rank, controller.data.imageList
						.get(combo.getSelectedIndex()));
			}
		}
	}
}
