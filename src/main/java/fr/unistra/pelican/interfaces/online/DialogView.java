package fr.unistra.pelican.interfaces.online;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class DialogView extends JDialog{

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * This frame contains the parameter panel.
	 */
	private JPanel parameterPanel;

	/**
	 * Button used to launch the algorithm when the parameters are set.
	 */
	public JButton launchButton;
	
	/**
	 * Launch button panel.
	 */
	private JPanel launchButtonPanel;
	

	/**
	 * Button used to cancel the parameters setting.
	 */
	public JButton cancelButton;
	
	/**
	 * The constraints for the layout manager.
	 */
	public GridBagConstraints constraints;
	
	/**
	 * 
	 * Reference to the controller.
	 */
	public GlobalController controller;
	
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
	public DialogView(GlobalController controller) {
		
		super();
		this.controller = controller;
		
		/* The parameterFrame creation */
		this.setModal(true);
		this.setTitle("Algorithm parameters");
		this.setLocation(0, 580);
		this.setSize(370, 430);
		Dimension dim = new Dimension(370, 430);
		this.setMaximumSize(dim);
		this.setMinimumSize(dim);
		this.setResizable(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		parameterPanel = new JPanel(new GridBagLayout());
		constraints = new GridBagConstraints();
		this.setModal(true);
		this.setLayout(new BorderLayout());
		this.add(parameterPanel, BorderLayout.NORTH);

		launchButtonPanel = new JPanel(new BorderLayout());
		this.add(launchButtonPanel, BorderLayout.SOUTH);
		launchButton = new JButton("Launch");
		launchButton.addActionListener(controller.new GlobalController_launchButton_actionAdapter(
				this));
		launchButtonPanel.add(launchButton, BorderLayout.LINE_START);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(controller.new GlobalController_cancelButton_actionAdapter(
				this));
		launchButtonPanel.add(cancelButton, BorderLayout.LINE_END);
	}
	
	
	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/
	
	/**
	 * 
	 */
	public JPanel getPanel() {
		return parameterPanel;
	}
}
