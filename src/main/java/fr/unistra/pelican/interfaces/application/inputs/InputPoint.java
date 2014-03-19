package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.interfaces.application.GlobalController;

public class InputPoint extends JPanel implements InputType {

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
	 * This attribute represents the resulting parameter.
	 */
	Point result;

	/**
	 * This text field is the user seizing interface for the x coordonate.
	 */
	JTextField x;

	/**
	 * This text field is the user seizing interface for the x coordonate.
	 */
	JTextField y;

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
	 *            This paramter is an option or not.
	 * @throws NoSuchFieldException 
	 */
	public InputPoint(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) throws NoSuchFieldException {
		super();
		this.setLayout(new GridLayout(1, 3, 5, 5));

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
		lab.setToolTipText(controller.getJavadoc(algoName, parameterName));

		Object o;
		Integer i = null;

		try {
			o = Class
					.forName((String) controller.algoNameMapping.get(algoName))
					.newInstance();
			i = (Integer) Class.forName(
					(String) controller.algoNameMapping.get(algoName))
					.getField(parameterName).get(o);

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		x = new JTextField(Integer.toString(i));
		this.add(x);
		x.setToolTipText("x parameter of the Point");

		try {
			o = Class
					.forName((String) controller.algoNameMapping.get(algoName))
					.newInstance();
			i = (Integer) Class.forName(
					(String) controller.algoNameMapping.get(algoName))
					.getField(parameterName).get(o);

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		y = new JTextField(Integer.toString(i));
		this.add(y);
		y.setToolTipText("y parameter of the Point");

		// This instance is added to input instances array
		if (option == false) {
			controller.parameterInstanceArray[parameterNumber] = this;
		} else {
			controller.parameterInstanceArray[parameterNumber - 1] = this;
		}
	}

	public void fire() {

		result = new Point(Integer.parseInt(x.getText()), Integer.parseInt(y
				.getText()));
		if (option == false) {
			controller.parameterArray.add(parameterNumber, result);
		} else {
			controller.parameterArray.add(parameterNumber - 1, result);
		}
	}
}
