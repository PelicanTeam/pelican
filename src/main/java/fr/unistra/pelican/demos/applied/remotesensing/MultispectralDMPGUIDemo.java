package fr.unistra.pelican.demos.applied.remotesensing;

import java.awt.Container;
import java.awt.GridLayout;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialDMP;
import fr.unistra.pelican.algorithms.statistics.PCA;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.vectorial.orders.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * A toolbox to compute DMP for multispectral images
 * 
 * @author lefevre
 * 
 */
public class MultispectralDMPGUIDemo {

	public static void main(String[] args) {
		if (args.length == 0) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					launchGUI();
				}
			});
		} else
			launchConsole(args);
	}

	public static void launchGUI() {
		new MultispectralDMPGUIDemo().gui();
	}

	public static void launchConsole(String args[]) {
		new MultispectralDMPGUIDemo().console(args);
	}

	final static String appName = "MultispectralDMPDemo v1.0";
	String path;
	String filename;
	String outname;
	int order;
	int length;
	boolean debug;
	boolean display;
	Image input;
	Image output;
	VectorialOrdering[] orders;
	String[] orderNames;

	JFrame frame;
	JLabel inputLabel;
	JTextField inputText;
	JButton inputButton;
	JLabel orderLabel;
	JComboBox orderBox;
	JButton orderButton;
	JLabel lengthLabel;
	JTextField lengthText;
	JButton lengthButton;
	JButton displayButton;
	JButton proceedButton;
	JButton resultButton;
	JCheckBox pcaCheckbox;
	JCheckBox byteCheckbox;
	JCheckBox colorCheckbox;
	JCheckBox stretchCheckbox;
	JCheckBox verboseCheckbox;
	JCheckBox resultCheckbox;

	/**
	 * GUI interface
	 */
	private void gui() {
		// Create and set up the window.
		frame = new JFrame(appName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(6, 3));
		Container panel = frame.getContentPane();
		// First line : file
		inputLabel = new JLabel("Input image");
		panel.add(inputLabel);
		inputText = new JTextField("filename");
		panel.add(inputText);
		inputText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				filename = inputText.getText();
			}
		});
		inputButton = new JButton("Browse");
		panel.add(inputButton);
		inputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(path);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					try {
						filename = chooser.getSelectedFile().getCanonicalPath();
						path = chooser.getCurrentDirectory().getCanonicalPath();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					inputText.setText(filename);
				}
			}
		});
		// Second line : order
		orderLabel = new JLabel("Vectorial Ordering");
		panel.add(orderLabel);
		initialiseOrders();
		orderBox = new JComboBox(orderNames);
		orderBox.setSelectedIndex(0);
		panel.add(orderBox);
		orderBox.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				order = orderBox.getSelectedIndex();
			}
		});
		orderButton = new JButton("More info");
		panel.add(orderButton);
		orderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame,
					"For a survey of vectorial orderings:\n"
						+ "http://dx.doi.org/10.1016/j.patrec.2007.09.011");
			}
		});
		// Third line : length
		lengthLabel = new JLabel("DMP length");
		panel.add(lengthLabel);
		lengthText = new JTextField(Integer.toString(length));
		panel.add(lengthText);
		lengthText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				try {
					length = Integer.parseInt(lengthText.getText());
				} catch (NumberFormatException ex) {
					lengthText.grabFocus();
				}
			}
		});
		panel.add(new JLabel());
		// Fourth line : options
		pcaCheckbox = new JCheckBox("PCA preprocessing");
		panel.add(pcaCheckbox);
		byteCheckbox = new JCheckBox("Byte precision");
		byteCheckbox.setSelected(true);
		panel.add(byteCheckbox);
		colorCheckbox = new JCheckBox("Pseudo-color");
		panel.add(colorCheckbox);
		stretchCheckbox = new JCheckBox("Stretch before display");
		panel.add(stretchCheckbox);
		verboseCheckbox = new JCheckBox("Verbose mode");
		panel.add(verboseCheckbox);
		resultCheckbox = new JCheckBox("Display result");
		panel.add(resultCheckbox);
		// Fifth line : actions
		displayButton = new JButton("Display input");
		panel.add(displayButton);
		displayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					load();
					preprocess();
					Viewer2D.exec(stretchCheckbox.isSelected() ? ContrastStretchEachBands
						.exec(input) : input, filename);
				} catch (PelicanException ex) {
					JOptionPane.showMessageDialog(frame, "Unable to load: " + filename);
				}
			}
		});
		proceedButton = new JButton("Compute DMP");
		panel.add(proceedButton);
		proceedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (verboseCheckbox.isSelected())
					debug = true;
				else {
					System.err.close();
					verboseCheckbox.setEnabled(false);
				}
				display=resultCheckbox.isSelected();
				outname = filename.substring(0, filename.lastIndexOf('.'));
				load();
				preprocess();
				process();
			}
		});
		resultButton = new JButton("Display result");
		panel.add(resultButton);
		resultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Viewer2D.exec(stretchCheckbox.isSelected() ? ContrastStretchEachBands
						.exec(output) : output, orderNames[order]);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "Unable to display the result");
				}
			}
		});
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public MultispectralDMPGUIDemo() {
		path = System.getProperty("user.dir");
		//path = "/home/miv/lefevre/data/teledetection/jrc";
		length = 3;
	}

	/**
	 * Console interface
	 * 
	 * @param args
	 *          arguments
	 */
	public void console(String[] args) {
		initialiseOrders();
		if (!checkParams(args))
			return;
		load();
		process();
	}

	public boolean checkParams(String[] args) {
		System.out.println(appName);
		// Show help
		if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			System.out.println(help());
			return false;
		}
		// Display image
		else if (args.length == 2 && args[0].equalsIgnoreCase("display")) {
			Viewer2D.exec(ImageLoader.exec(args[1]), args[0]);
			return false;
		}
		// Process image
		else if (args.length == 4
			&& (args[0].equalsIgnoreCase("process") || args[0]
				.equalsIgnoreCase("debug"))) {
			if (args[0].equalsIgnoreCase("debug"))
				debug = true;
			filename = args[1];
			outname = filename.substring(0, filename.lastIndexOf('.'));
			setOrder(args[2]);
			length = Integer.parseInt(args[3]);
			return true;
		}
		error();
		return false;
	}

	public void error() {
		System.out.println("Bad usage...");
		System.out.println(help());
		System.exit(0);
	}

	public String help() {
		return "\navailable commands:\n"
			+ "[help] display this help\n"
			+ "[display FILE] display the image FILE \n"
			+ "[process FILE ORDER LENGTH] compute the DMP of size LENGTH with the vectorial ordering ORDER\n"
			+ "[debug ...] the same as process but in verbose mode\n"
			+ "\navailable vectorial orderings:\n" + listOrders();
	}

	public String listOrders() {
		initialiseOrders();
		StringBuffer s = new StringBuffer();
		for (String o : orderNames) {
			s.append(o);
			for (int u = 0; u < 30 - o.length(); u++)
				s.append(' ');
			s.append("\n");
		}
		return s.toString();
	}

	public void initialiseOrders() {
		/*
		 * 
		 * La plupart des ordres de pelican sont biensur developpes en considerant
		 * les images couleur. Cest-a-dire ils essayent d'exploiter en particulier
		 * les relations presentes parmi les differentes bandes (LSH). En
		 * teledetection des telles relations sont en general soit absentes soit
		 * presentes mais difficile voir impossible a estimer. Cest pourquoi des
		 * methodes asymmetrique+PCA ou simplement des methodes symmetriques sont
		 * utilises.
		 * 
		 * Comme asymmetrique, on a le lexicographique avec toutes ses variations:
		 * a-modulus d'Angulo, a-modulus generalise, marker based lexicographique et
		 * a-trimmed-lexicographical extrema..entre autres. Comme symmetrique..norm
		 * suivi de lexicographique (Rivest), distance par rapport a une reference
		 * suivi de lexicographique (Angulo). Les autres R-ordres sont souvent des
		 * pre-ordres. Pour l'instant cest tout ce dont je me souviens.
		 */
		orders = new VectorialOrdering[] {
			null,
			// new AlphaTrimmedLexicographicalOrdering2(0.5),
			// new BitMixOrdering(),
			// new CumulativeDistanceOrdering(),
			// new ExtremeOrdering(),
			// new PCALexicographicalOrdering(),
			new LexicographicalOrdering(10),
			// new MahalanobisBasedOrdering(input),
			// new MarginalOrdering(),
			new MarginalWithNormReplacementOrdering(), new NormBasedOrdering(),
			new ReducedLexicographicalOrdering(),
		// new ReferenceBasedDistanceOrdering(),
		};
		orderNames = new String[orders.length];
		orderNames[0] = "All !";
		for (int o = 1; o < orderNames.length; o++)
			orderNames[o] = orders[o].getClass().getSimpleName();
	}

	public void setOrder(String name) {
		initialiseOrders();
		// Check the appropriate order
		for (int t = 0; t < orderNames.length; t++)
			if (name.equalsIgnoreCase(orderNames[t])) {
				order = t;
				return;
			}
		error();
		order = -1;
	}

	public void load() {
		input = ImageLoader.exec(filename);
		if (input.getBDim() == 3)
			input.setColor(true);
		if (debug)
			Viewer2D.exec(input, filename);
		System.out.println("file loaded: " + filename);
	}

	public void preprocess() {
		if (pcaCheckbox.isSelected())
			input = PCA.exec(input);
		if (byteCheckbox.isSelected())
			input = new ByteImage(ContrastStretch.exec(input));
		if (colorCheckbox.isSelected()) {
			if (input.getBDim() > 3)
				input = ColorImageFromMultiBandImage.exec(input, new int[] { 3, 2, 1 });
			else if (input.getBDim() < 3)
				input = GrayToRGB.exec(input.getImage4D(0, Image.B));
		}
		if (debug)
			System.out.println(input);
	}

	public void process() {
		initialiseOrders();
		if (order == 0)
			for (int o = 1; o < orders.length; o++) {
				System.out.print(orderNames[o] + "...");
				long t1 = System.currentTimeMillis();
				apply(o);
				long t2 = System.currentTimeMillis();
				System.out.println(((t2 - t1) / 1000) + " seconds");
			}
		else
			apply(order);
	}

	public void apply(int order) {
		VectorialOrdering vec = orders[order];
		String orderName = orderNames[order];
		output = VectorialDMP.exec(input, vec, length, Image.Z);
		if (output.getBDim() == 3)
			output.setColor(true);
		if (debug)
			System.out.println(output);
		if (display)
			Viewer2D.exec(stretchCheckbox.isSelected() ? ContrastStretchEachBands
				.exec(output):output, orderName);

		//		 for (int z = 0; z < output.getZDim(); z++)
		//		 ImageSave.exec(output.getImage4D(z, Image.Z), outname + "-" + orderName
		//		 + "-" + z + ".tiff");
		//		TiffMultiplePageImageSave.exec(output, outname + "-" + orderName + ".tiff");
		//		Image tmp = ImageLoader.exec(outname + "-" + orderName + ".tiff");
		//		Viewer2D.exec(stretchCheckbox.isSelected() ? ContrastStretchEachBands
		//			.exec(tmp) : tmp, outname + "-" + orderName);
	}

}
