package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.interfaces.application.GlobalController;

public class InputColor extends JPanel  implements  InputType, ActionListener  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Reference to the view object.
	 */
	GlobalController controller;

	/**
	 * This attribute defines the range of this parameter among the other
	 * Parameters.
	 */
	int parameterNumber;

	/**
	 * This attribute represents the resulting parameter.
	 */
	Color result;

	/**
	 * Combobox which contains all the color proposed by Java.
	 */
	JComboBox colorBox;

	/**
	 * This use to know if the parameter is called as an option or not
	 */
	boolean option;

	/**
	 * The red field
	 */
	JTextField red;

	/**
	 * The green field
	 */
	JTextField green;

	/**
	 * The blue field
	 */
	JTextField blue;

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
	public InputColor(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {
		super();		
		this.setLayout(new GridLayout(1,5,3,3));

		// Initializes the attributes
		this.controller = controller;
		this.parameterNumber = parameterNumber;
		this.option = option;
		
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
	 * 				This paramter is an option or not.
	 */
	public void parameterSeizing(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {


		JLabel lab = new JLabel(parameterName);
		this.add(lab);
		lab.setToolTipText(GlobalController.getJavadoc(algoName, parameterName));

		String[] proposedColors = { "", "black", "blue", "cyan", "darkGrey",
				"gray", "green", "lightGray", "magenta", "orange", "pink",
				"red", "white", "yellow" };

		// Creates the combo box, select item at index 0.
		colorBox = new JComboBox(proposedColors);
		colorBox.setSelectedIndex(0);
		this.add(colorBox);
		colorBox.addActionListener(this);

		red = new JTextField("red");
		this.add(red);

		green = new JTextField("green");
		this.add(red);

		blue = new JTextField("blue");
		this.add(red);

		// This instance is added to input instances array
		if (option == false) {
			controller.parameterInstanceArray[parameterNumber] = this;
		} else {
			controller.parameterInstanceArray[parameterNumber - 1] = this;
		}

	}

	public void actionPerformed(ActionEvent e) {

	}

	public void fire() {

		int index = colorBox.getSelectedIndex();
		switch (index) {

		case 0:
			if (red.getText().equals("red") || green.getText().equals("green")
					|| blue.getText().equals("blue")) {
				if (option == false) {
					System.err
							.println("ERROR: The boolean parameter is mandatory");
				} else {
					controller.parameterArray.add(parameterNumber - 1, null);
				}
			} else {
				result = new Color(Integer.parseInt(red.getText()), Integer.parseInt(green.getText()), Integer.parseInt(blue.getText()));
				if (option == false) {
					controller.parameterArray.add(parameterNumber, result);
				} else {
					controller.parameterArray.add(parameterNumber - 1, result);
				}
			}
			break;

		case 1:
			result = Color.black;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}			
			break;
			
		case 2:
			result = Color.blue;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 3:
			result = Color.cyan;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 4:
			result = Color.darkGray;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 5:
			result = Color.gray;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 6:
			result = Color.green;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 7:
			result = Color.lightGray;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 8:
			result = Color.magenta;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 9:
			result = Color.orange;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 10:
			result = Color.pink;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 11:
			result = Color.red;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 12:
			result = Color.white;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
			
		case 13:
			result = Color.yellow;
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
			break;
		}
	}
}
