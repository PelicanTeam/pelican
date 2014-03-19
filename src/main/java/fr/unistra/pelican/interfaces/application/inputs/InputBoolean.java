package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.unistra.pelican.interfaces.application.GlobalController;

public class InputBoolean extends JPanel  implements InputType {

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
	 * parameters. 
	 */
	int parameterNumber;
	
	/**
	 * This attribute represents the resulting parameter.
	 */
	Boolean result;
	
	/**
	 * This combobox is the user seizing interface.
	 */
	JComboBox comboChoice;
	
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
	 * 				This parameter is an option or not.
	 */
	public InputBoolean(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {
		super();		
		this.setLayout(new GridLayout(1,2,5,5));

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
	 * 				This parameter is an option or not.
	 */
	public void parameterSeizing(GlobalController controller, String algoName, String parameterName,
			int parameterNumber, boolean option) {


		JLabel lab = new JLabel(parameterName);
		this.add(lab);
		lab.setToolTipText(GlobalController.getJavadoc(algoName, parameterName));
		
		String[] choice = { "", "true", "false" };
		comboChoice = new JComboBox(choice);
		comboChoice.setSelectedIndex(0);	
		this.add(comboChoice);
		
		// This instance is added to input instances array
		if (option == false) {
			controller.parameterInstanceArray[parameterNumber] = this;
		} else {
			controller.parameterInstanceArray[parameterNumber - 1] = this;
		}
	}
	
	public void fire() {
		
		
		int index = comboChoice.getSelectedIndex();		
		switch(index) {
		
		case 0:
			if (option == false) {
				System.err.println("ERROR: The boolean parameter is mandatory");
			} else {
				controller.parameterArray.add(parameterNumber - 1, null);
			}
			break;
			
		// true is selected
		case 1:
			if (option == false) {
				controller.parameterArray.add(parameterNumber, new Boolean(true));
			} else {
				controller.parameterArray.add(parameterNumber - 1, new Boolean(true));
			}
			break;
			
		// false is selected
		case 2:
			if (option == false) {
				controller.parameterArray.add(parameterNumber, new Boolean(false));
			} else {
				controller.parameterArray.add(parameterNumber - 1, new Boolean(false));
			}
			break;
		}		
	}	
}
