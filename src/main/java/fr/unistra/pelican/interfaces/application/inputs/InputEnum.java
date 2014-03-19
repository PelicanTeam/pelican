package fr.unistra.pelican.interfaces.application.inputs;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.unistra.pelican.interfaces.application.GlobalController;


public class InputEnum extends JPanel  implements InputType {

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
	 * 				This paramter is an option or not.
	 */
	public InputEnum(GlobalController controller, String algoName,
			String parameterName, int parameterNumber, boolean option) {
		super();		
		this.setLayout(new BorderLayout(60, 60));

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
		this.add(lab, BorderLayout.LINE_START);
		lab.setToolTipText(controller.getJavadoc(algoName, parameterName));
		
		String[] choice = null;
		/** Probleme ici, on ne peut pas appeler getEnumConstant parce qu'on a aucune rï¿œfï¿œrence sur l'objet Enum		
		try {
			//for (Object p : .getEnumConstants()) {
				// Remplir choice avec les valeurs de getEnumConstant
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}
		*/
		 
		comboChoice = new JComboBox(choice);
		comboChoice.setSelectedIndex(0);	
		this.add(comboChoice, BorderLayout.CENTER);
		
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
		
	}	
}
