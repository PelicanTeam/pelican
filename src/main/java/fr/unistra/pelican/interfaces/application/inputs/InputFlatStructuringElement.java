package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.interfaces.application.GlobalController;

public class InputFlatStructuringElement extends JPanel  implements InputType{


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
	BooleanImage result;
	
	/**
	 * This text field is the user seizing interface.
	 */
	JTextField rows;
	
	/**
	 * This text field is the user seizing interface.
	 */
	JTextField cols;
	
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
	public InputFlatStructuringElement(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {
		super();		
		this.setLayout(new GridLayout(1,5,5,5));

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
	public void parameterSeizing(GlobalController controller, String algoName, String parameterName,
			int parameterNumber, boolean option) {


		JLabel lab = new JLabel(parameterName);
		this.add(lab);
		lab.setToolTipText(controller.getJavadoc(algoName, parameterName));

		rows = new JTextField("Default");
		this.add(rows);	
		rows.setToolTipText("number of rows of the FlatStructuringElement");
		
		cols = new JTextField("Default");
		this.add(cols);	
		cols.setToolTipText("number of columns of the FlatStructuringElement");
		
		x = new JTextField("Default");
		this.add(x);
		x.setToolTipText("x parameter of the Point of the FlatStructuringElement");		
		
		y = new JTextField("Default");
		this.add(y);
		y.setToolTipText("y parameter of the Point of the FlatStructuringElement");
		
		//This instance is added to input instances array
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
		
		
		if (rows.getText().equals("Default") || cols.getText().equals("Default") || x.getText().equals("Default") || y.getText().equals("Default")) {
			if (option == false) {
				System.err.println("ERROR: The integer parameter is mandatory");
			} else {
				controller.parameterArray.add(parameterNumber - 1, null);
			}
		} else {			
			result =  new BooleanImage(Integer.parseInt(rows.getText()),Integer.parseInt(cols.getText()),1,1,1);
			result.setCenter(new Point(Integer.parseInt(x.getText()), Integer.parseInt(y.getText())));
			if (option == false) {
				controller.parameterArray.add(parameterNumber, result);
			} else {
				controller.parameterArray.add(parameterNumber - 1, result);
			}
		}	
	}
}

