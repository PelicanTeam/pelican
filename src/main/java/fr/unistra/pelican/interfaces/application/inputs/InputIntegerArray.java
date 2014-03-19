package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.interfaces.application.GlobalController;

public class InputIntegerArray extends JPanel implements InputType,
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
	Integer[] result;

	/**
	 * This use to know if the parameter is called as an option or not
	 */
	boolean option;

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
	public InputIntegerArray(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {
		super();
		this.setLayout(new GridBagLayout());

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

		JPanel line = new JPanel(new GridLayout(1, 4, 10, 10));
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.5;
		constraints.gridx = 0;
		constraints.gridy = arraySize;
		this.add(line, constraints);

		JLabel lab = new JLabel(parameterName);
		line.add(lab);
		lab.setToolTipText(controller.getJavadoc(algoName, parameterName));

		JTextField tf = new JTextField("Default");
		line.add(tf);

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
	 * 
	 */
	public void fire() {

		boolean misfilled = false;
		result = new Integer[arraySize];
		for (int i = 0; i < arraySize; i++) {
			JTextField textfield = (JTextField) panelArray.get(i).getComponent(
					1);			
			if (textfield.getText().equals("Default")) {
				misfilled = true;
			} else {
				result[i] = Integer.valueOf(textfield.getText());
			}
		}

		if (misfilled == true) {
			if (option == false) {
				System.err
						.println("ERROR: The integer parameter is mandatory, one parameter is not well filled");
			} else {
				controller.parameterArray.add(parameterNumber - 1, null);
			}
		} else {

			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
		}
	}

	public void actionPerformed(ActionEvent arg0) {

		// The plus button add an element in the array
		if (arg0.getActionCommand().equals("+")) {

			arraySize++;

			JPanel line2 = new JPanel(new GridLayout(1, 4, 10, 10));
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 0.5;
			constraints.gridx = 0;
			constraints.gridy = arraySize;
			this.add(line2, constraints);

			JLabel lab2 = new JLabel("Element nï¿œ" + (arraySize));
			line2.add(lab2);

			JTextField tf2 = new JTextField("Default");
			line2.add(tf2);

			JLabel lab3 = new JLabel("        ");
			line2.add(lab3);

			JLabel lab4 = new JLabel("        ");
			line2.add(lab4);

			panelArray.add(line2);

			controller.parameterBox.pack();

			// The plus button add an element in the array
		} else {

			// At leat one element remains
			if (panelArray.size() > 1) {

				arraySize--;

				JPanel line2 = panelArray.get(panelArray.size() - 1);
				panelArray.remove(panelArray.size() - 1);
				line2.removeAll();
				controller.parameterBox.pack();

			}
		}
	}
}
