package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.interfaces.application.GlobalController;

public class InputString extends JPanel implements InputType {

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
	String result;

	/**
	 * This text field is the user seizing interface.
	 */
	JTextField tf;

	/**
	 * This use to know if the parameter is called as an option or not
	 */
	boolean option;

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
	 *            This parameter is an option or not.
	 * @throws NoSuchFieldException 
	 */
	public InputString(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) throws NoSuchFieldException {
		super();
		this.setLayout(new GridLayout(1, 2, 5, 5));

		// Initializes the attributes
		this.controller = controller;
		this.parameterNumber = parameterNumber;
		this.option = option;

		try {
			parameterSeizing(controller, algoName, parameterName,
					parameterNumber, option);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
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
	 *            This parameter is an option or not.
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	public void parameterSeizing(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option)
			throws IllegalArgumentException, SecurityException,
			NoSuchFieldException {

		JLabel lab = new JLabel(parameterName);
		this.add(lab);
		lab.setToolTipText(GlobalController.getJavadoc(algoName, parameterName));

		Object o;
		String i = null;

		try {
			o = Class
					.forName((String) controller.algoNameMapping.get(algoName))
					.newInstance();
			i = (String) Class.forName(
					(String) controller.algoNameMapping.get(algoName))
					.getField(parameterName).get(o);

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		tf = new JTextField(i);
		this.add(tf);

		// This instance is added to input instances array
		if (option == false) {
			controller.parameterInstanceArray[parameterNumber] = this;
		} else {
			controller.parameterInstanceArray[parameterNumber - 1] = this;
		}
	}

	public void fire() {

		result = tf.getText();
		if (option == false) {
			controller.parameterArray.add(parameterNumber, result);
		} else {
			controller.parameterArray.add(parameterNumber - 1, result);
		}
	}
}
