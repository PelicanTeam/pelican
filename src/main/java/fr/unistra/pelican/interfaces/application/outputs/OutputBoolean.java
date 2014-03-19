package fr.unistra.pelican.interfaces.application.outputs;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.interfaces.application.GlobalController;

public class OutputBoolean {

	/**
	 * 
	 */
	GlobalController controller;
	
	public void outputPrinting(GlobalController controller, String outputName, Object object) {

		this.controller = controller;
		Dimension dim;
		Boolean result = (Boolean) object;

		JFrame resultFrame = new JFrame(outputName);
		dim = new Dimension(200, 200);
		resultFrame.setPreferredSize(dim);
		resultFrame.setLocation(0, 0);
		resultFrame.setResizable(true);
		resultFrame.setVisible(true);
		JPanel resultPanel = new JPanel();
		JTextField resultTextArea = new JTextField();
		dim = new Dimension(100, 20);
		resultTextArea.setPreferredSize(dim);
		resultTextArea.setText(String.valueOf(result));
		resultTextArea.setEditable(false);
		resultFrame.setContentPane(resultPanel);
		resultPanel.add(resultTextArea);

		resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		resultFrame.pack();

	}
}
