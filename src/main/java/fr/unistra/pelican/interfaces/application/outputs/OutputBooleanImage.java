package fr.unistra.pelican.interfaces.application.outputs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.interfaces.application.GlobalController;

public class OutputBooleanImage implements ActionListener {

	/**
	 * 
	 */
	GlobalController controller;
	
	/**
	 * 
	 */
	JTextField tf;
	
	/**
	 * 
	 */
	String imageName;
	
	/**
	 * 
	 */
	BooleanImage imageResult;
	
	/**
	 * 
	 */
	JDialog popup;
	
	
	/**
	 * 
	 * @param controller
	 * @param outputName
	 * @param image
	 */	
	public void outputPrinting(GlobalController controller, String outputName,
			Object image) {	
		
		this.controller = controller;
		this.imageResult = (BooleanImage) image;
		
		
		//Creation of the popup menu to seize the image name
		popup = new JDialog();
	
		popup.setModal(true);
		popup.setTitle("Name of the output image");
		popup.setLocation(800, 600);
		popup.setSize(150, 80);
		Dimension dim = new Dimension(150, 150);
		popup.setMaximumSize(dim);
		popup.setMinimumSize(dim);
		popup.setResizable(true);
		

		JPanel popupPanel = new JPanel(new BorderLayout());		
		popup.add(popupPanel);
		
		tf = new JTextField("");
		popup.add(tf, BorderLayout.NORTH);
		
		JButton ok = new JButton("ok");
		popup.add(ok, BorderLayout.SOUTH);
		ok.addActionListener(this);
		
		popup.setVisible(true);
		popup.pack();
		popup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

	}



	/**
	 * Ok button action listener
	 */
	public void actionPerformed(ActionEvent arg0) {
		
		imageName = tf.getText();
		popup.dispose();
		
		BooleanImage result = imageResult;
		result.setName(imageName);		
		Viewer2D.exec(result, imageName);
		
		//to re-use the opened images
		controller.data.addImage(controller.data.imageList, result);
		controller.addLoadedImage(result);
	}
}
