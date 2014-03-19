package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.interfaces.application.GlobalController;

public class InputBooleanImage extends JPanel implements  InputType, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Reference to the view object.
	 */
	GlobalController controller;

	/**
	 * This frame contains the thumbnails panel
	 */
	JDialog thumbnailsFrame;

	/**
	 * This attribute defines the range of this image parameter among the other
	 * Parameters.
	 */
	int parameterNumber;

	/**
	 * This attribute represents the resulting parameter.
	 */
	BooleanImage result = null;

	/**
	 * Text filed which display the name of the image to load
	 */
	JTextField tf;

	/**
	 * Combobox which will contains all the loaded images
	 */
	JComboBox imageBox;

	/**
	 * this array will contains all the loaded images for the combobox.
	 */
	String[] imageString;

	/**
	 * Is the number of image already loaded
	 */
	int numberOfImageLoaded;
	
	/**
	 * This use to know if the parameter is called as an option or not
	 */
	boolean option;
	
	/**
	 * The constraints for the layout manager.
	 */
	public GridBagConstraints constraints;
	
	/**
	 * Input constructor.
	 * 
	 * @param view
	 * 				Reference to the main panel.
	 * @param algoName
	 * 				Name of the algorithm which needs this parameter.
	 * @param parameterName
	 * 				Name of the parameter.
	 * @param parameterNumber
	 * 				Rank of this parameter between the other parameter of the algorithm.
	 * @param option
	 * 				This paramter is an option or not.
	 */
	public InputBooleanImage(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {
		super();		
		this.setLayout(new GridLayout(1,4,5,5));

		// Initializes the attributes
		this.controller = controller;
		this.parameterNumber = parameterNumber;
		this.option = option;
		this.setMaximumSize(this.getSize());
		this.setMinimumSize(this.getSize());
		
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
	 * 				Reference to the main panel.
	 * @param algoName
	 * 				Name of the algorithm which needs this parameter.
	 * @param parameterName
	 * 				Name of the parameter.
	 * @param parameterNumber
	 * 				Rank of this parameter between the other parameter of the algorithm.
	 * @param option
	 * 				This parameter is an option or not.
	 */
	public void parameterSeizing(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {


		// Sets the label
		JLabel lab = new JLabel(parameterName);
		this.add(lab);
		lab.setToolTipText(GlobalController.getJavadoc(algoName, parameterName));

		// It uses the combobox if an image has already been opened otherwise it
		// uses
		// the jtextfield
		if (numberOfImageLoaded == 0) {

			tf = new JTextField("");;				
			this.add(tf);

		} else {

			// Sets the combobox with all opened images
			imageString = new String[controller.data.imageList.size()];
			for (int i = 0; i < controller.data.imageList.size(); i++) {
				imageString[i] = controller.data.imageList.get(i).getName();
			}
			// Creates the combo box, select item at index 0.
			imageBox = new JComboBox(imageString);
			imageBox.setSelectedIndex(0);	
			this.add(imageBox);
			imageBox.addActionListener(this);

			// Sets The pick button
			JButton pick = new JButton("Pick");	
			this.add(pick);
			pick
					.addActionListener(new InputImage_PickButton_actionAdapter(
							controller));

		}

		JButton browse = new JButton("browse");
		this.add(browse);	
		browse.addActionListener(this);

		// This instance is added to input instances array
		if (option == false) {
			controller.parameterInstanceArray[parameterNumber] = this;
		} else {
			controller.parameterInstanceArray[parameterNumber - 1] = this;
		}

	}

	public void actionPerformed(ActionEvent e) {

		// Action of the browse button
		if (e.getActionCommand().equals("browse")) {
			JFrame openDialogFrame = new JFrame("Open");
			JFileChooser fc = new JFileChooser();
			controller.parameterBox.getPanel().add(fc);
			fc.showOpenDialog(openDialogFrame);
			result = (BooleanImage) ImageLoader.exec(fc.getSelectedFile().getPath());
			// Sets the image name
			result.setName(fc.getSelectedFile().getPath()
					.substring(
							fc.getSelectedFile().getPath().lastIndexOf(
									File.separator) + 1,
							fc.getSelectedFile().getPath().lastIndexOf(".")));

			// It uses the combobox if an image was already loaded otherwise it
			// uses the jtextfield
			if (numberOfImageLoaded == 0) {
				// If an image has already been set to the jtextfield (means an
				// image has already been added to the view.imageList), then
				// this image has first to be remove from the the
				// view.imageList.
				if (numberOfImageLoaded < controller.data.imageList.size()) {
					controller.data.imageList.remove(controller.data.imageList.size() - 1);
					tf.setText(fc.getSelectedFile().getPath());
					controller.data.imageList.add(result);
				} else {
					tf.setText(fc.getSelectedFile().getPath());
					controller.data.imageList.add(result);
				}

			} else {
				// This test use to only display the current browsed image
				if (imageBox.getItemCount() > numberOfImageLoaded) {
					controller.data.imageList.remove(controller.data.imageList.size() - 1);
					controller.data.imageList.add(result);
					imageBox.removeItemAt(imageBox.getItemCount() - 1);
					if (alreadyPresent(result.getName(), imageBox)) {
						controller.data.imageList.remove(controller.data.imageList.size() - 1);
					} else {
						imageBox.addItem(result.getName());
					}
					imageBox.setSelectedItem(result.getName());

				} else {
					controller.data.imageList.add(result);
					if (alreadyPresent(result.getName(), imageBox)) {
						controller.data.imageList.remove(controller.data.imageList.size() - 1);
					} else {
						imageBox.addItem(result.getName());
					}
					imageBox.setSelectedItem(result.getName());
				}
			}
		}
		// Action of the combobox
		if (e.getActionCommand().equals("comboBoxChanged")) {
			JComboBox cb = (JComboBox) e.getSource();
			result = (BooleanImage) controller.data.imageList.get(cb.getSelectedIndex());
		}
	}

	public void fire() {

		// If the selected item is the displayed one.
		if (result == null) {
			int selectedIndex = imageBox.getSelectedIndex();
			result = (BooleanImage) controller.data.imageList.get(selectedIndex);
		}
		controller.addLoadedImage(result);
		if (option == false) {
			controller.parameterArray.add(parameterNumber, result);
		} else {
			controller.parameterArray.add(parameterNumber - 1, result);
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
	public class InputImage_PickButton_actionAdapter implements
			java.awt.event.ActionListener {
		GlobalController controller;

		/**
		 * 
		 * @param view
		 */
		InputImage_PickButton_actionAdapter(GlobalController controller) {
			this.controller = controller;
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
				//thumbnail.se
				thumbnail.setToolTipText(controller.data.imageList.get(i).getName());
				BufferedImage image = fr.unistra.pelican.util.Tools
						.pelican2Buffered(controller.data.imageList.get(i));
				icon = new ImageIcon(image.getScaledInstance(70, 70,
						java.awt.Image.SCALE_SMOOTH));
				thumbnail.setIcon(icon);
//				constraints.fill = GridBagConstraints.HORIZONTAL;
//				constraints.weightx = 2.5;
//				constraints.gridx = i % 4;
//				constraints.gridy = i / 4;
				panel.add(thumbnail, constraints);
				thumbnail
						.addActionListener(new InputImage_imageButton_actionAdapter(
								controller));

			}
			//thumbnailsFrame.
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
	public class InputImage_imageButton_actionAdapter implements
			java.awt.event.ActionListener {
		GlobalController controller;

		/**
		 * 
		 * @param view
		 */
		InputImage_imageButton_actionAdapter(GlobalController controller) {
			this.controller = controller;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {

			JButton button = (JButton) e.getSource();
			result =  (BooleanImage) controller.getLoadedImage(button.getToolTipText());

			if (imageBox.getItemCount() > numberOfImageLoaded) {
				controller.data.imageList.remove(controller.data.imageList.size() - 1);
				controller.data.imageList.add(result);
				imageBox.removeItemAt(imageBox.getItemCount() - 1);
				if (alreadyPresent(result.getName(), imageBox)) {
					controller.data.imageList.remove(controller.data.imageList.size() - 1);
				} else {
					imageBox.addItem(result.getName());
				}
				imageBox.setSelectedItem(result.getName());

			} else {
				controller.data.imageList.add(result);
				if (alreadyPresent(result.getName(), imageBox)) {
					controller.data.imageList.remove(controller.data.imageList.size() - 1);
				} else {
					imageBox.addItem(result.getName());
				}
				imageBox.setSelectedItem(result.getName());
			}

			thumbnailsFrame.dispose();

		}
	}
}
