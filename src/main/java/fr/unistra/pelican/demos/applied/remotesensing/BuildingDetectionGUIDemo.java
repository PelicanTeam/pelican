package fr.unistra.pelican.demos.applied.remotesensing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.applied.remotesensing.building.HMTBuildingDetection;
import fr.unistra.pelican.algorithms.applied.remotesensing.building.OriginalBinaryBuildingDetection;
import fr.unistra.pelican.algorithms.arithmetic.Blending;
import fr.unistra.pelican.algorithms.arithmetic.LabelCombination;
import fr.unistra.pelican.algorithms.arithmetic.Maximum;
import fr.unistra.pelican.algorithms.conversion.BinaryMasksToLabels;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.logical.XOR;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryGradient;
import fr.unistra.pelican.algorithms.segmentation.HistogramBasedClustering;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.RidlerThresholding;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToBinaryMasks;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationKmeans;
import fr.unistra.pelican.algorithms.statistics.ConfusionMatrix;
import fr.unistra.pelican.algorithms.statistics.DetectionQualityEvaluation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * A toolbox to perform building detection
 * 
 * @author lefevre
 * 
 */
public class BuildingDetectionGUIDemo {

	public BuildingDetectionGUIDemo() {
		path = System.getProperty("user.dir");
	}

	public static void debug() {
		int XMin = 33;
		int YMin = 13;
		int XMax = 33;
		int YMax = 13;
		boolean globalTTR = false;
		double minRatio = 0;
		int nbangles = 12;
		int af = 0;
		int filterMode = HMTBuildingDetection.SMALL_FILTER;
		boolean debug = true;
		String path = "test-hmt";
		ImageSave.exec(HMTBuildingDetection.exec(ManualThresholding.exec(
				ImageLoader.exec(path + ".png"), 0.5), XMin, YMin, XMax, YMax,
				false, debug, debug, filterMode, af, nbangles, globalTTR,
				minRatio, "debug/" + path + "-exp-"), "debug/" + path
				+ "-res.png");
		System.exit(0);
	}

	public static void main(String[] args) {
		// debug();
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
		new BuildingDetectionGUIDemo().gui();
	}

	public static void launchConsole(String args[]) {
		new BuildingDetectionGUIDemo().console(args);
	}

	/**
	 * Console interface
	 * 
	 * @param args
	 *            arguments
	 */
	public void console(String[] args) {
		BuildingDetectionDemo.main(args);
	}

	final static String appName = "BuildingDetectionGUIDemo v1.0";
	String path = "";
	String filename = "input.tif";
	String labelname = "label.tif";
	String resname = "result.tif";
	String refname = "reference.tif";
	String statname = "stats.txt";
	Image input;
	Image labels;
	Image valid;
	Image valid2;
	Image combi;
	Image result;
	Image reference;
	Image output;
	String[] binNames = { "MANUAL", "AUTO", "CLUSTER", "KMEANS", "FAST",
			"ULTRA FAST" };
	// enum binFlags {MANUAL, AUTO,CLUSTER,KMEANS,FAST};
	int[] binValues = { 0, 1, 2, 3, 4, 5 };
	String[] filterNames = { "NO", "MANUAL", "AUTO", "SAFE", "SMALL" };
	int[] filterValues = { -1, 0, 1, 2, 3 };
	String binName = binNames[3];
	int binMode = binValues[3];
	String filterName = filterNames[4];
	int filterMode = filterValues[4];
	int nbInvalidate;
	int nbInvalidate2;

	JFrame frame;
	JLabel inputLabel;
	JTextField inputText;
	JButton inputButton;
	JLabel binLabel;
	JComboBox binBox;
	JTextField binParam;
	JLabel labelLabel;
	JTextField labelText;
	JButton labelButton;
	JButton displayButton;
	JButton binButton;
	JButton labelsButton;
	JCheckBox invalidateBox;
	JTextField invalidateText;
	JButton invalidateButton;
	JLabel mergeLabel;
	JTextField mergeText;
	JButton mergeButton;
	JCheckBox invalidateBox2;
	JTextField invalidateText2;
	JButton invalidateButton2;
	JLabel xLabel;
	JTextField xminText;
	JTextField xmaxText;
	JLabel yLabel;
	JTextField yminText;
	JTextField ymaxText;
	JLabel angleLabel;
	JTextField anglesText;
	JTextField angleFirstText;
	JLabel alphaLabel;
	JTextField alphaText;
	JLabel filterLabel;
	JComboBox filterBox;
	JTextField ratioText;
	JButton launchButton;
	JButton infoButton;
	JLabel expLabel;
	JTextField expText;
	JButton exitButton;
	JLabel resultLabel;
	JTextField resultText;
	JButton resultButton;
	JLabel referenceLabel;
	JTextField referenceText;
	JButton referenceButton;
	JLabel statLabel;
	JTextField statText;
	JButton statButton;
	JButton refresButton;
	JButton compResButton;
	JButton compRefButton;
	JButton compareButton;
	Container mainPanel;
	JPanel bar;
	JPanel barFlags;
	JLabel message;
	JPanel panel;
	JLabel isInput;
	JLabel isLabels;
	JLabel isValid;
	JLabel isValid2;
	JLabel isResult;
	JLabel isReference;
	JButton viewLabels;
	JButton viewResult;
	JButton viewReference;

	private void update() {
		isInput.setText(input != null ? "INPUT" : "");
		isLabels.setText(labels != null ? "LABELS" : "");
		isValid.setText(valid != null ? "VALID" : "");
		isValid2.setText(valid2 != null ? "VALID2" : "");
		isResult.setText(result != null ? "RESULT" : "");
		isReference.setText(reference != null ? "REFERENCE" : "");
		// frame.repaint();
	}

	private String params() {
		StringBuffer s = new StringBuffer();
		s.append("\nParameters\n==========");
		s.append("\ninput image: " + filename);
		s.append("\nbinarisation method: " + binName);
		s.append("\nlabel image: " + labelname);
		s.append("\n").append(invalidateBox.isSelected() ? "in" : "").append(
				"validated clusters before merging: ").append(
				invalidateText.getText());
		s.append("\nmerging size: " + mergeText.getText());
		s.append("\n").append(invalidateBox2.isSelected() ? "in" : "").append(
				"validated clusters after merging: ").append(
				invalidateText2.getText());
		s.append("\nwidth min/max: ").append(xminText.getText()).append(" / ")
				.append(xmaxText.getText());
		s.append("\nheight min/max: ").append(yminText.getText()).append(" / ")
				.append(ymaxText.getText());
		s.append("\nangle number/start: ").append(anglesText.getText()).append(
				" / ").append(angleFirstText.getText());
		s.append("\nuncertainty/min ratio: ").append(alphaText.getText())
				.append(" / ").append(ratioText.getText());
		s.append("\nfiltering method: " + filterName);
		s.append("\nresult image: " + resname);
		s.append("\nreference image: " + refname);
		s.append("\n\n");
		return s.toString();
	}

	private void invalidate() {
		message.setText("Invalidate in progress");
		boolean invalidate = invalidateBox.isSelected();
		update();
		Image labels2 = LabelsToBinaryMasks.exec(labels, true);
		// Parse the String
		String[] result = invalidateText.getText().split(",");
		int bands[] = new int[result.length];
		int errors = 0;
		for (int i = 0; i < result.length; i++) {
			try {
				bands[i] = Integer.parseInt(result[i]);
			} catch (NumberFormatException ex) {
				message.setText("Invalid cluster list");
				update();
			}
			if (bands[i] <= 0 || bands[i] > labels2.getBDim()) {
				bands[i] = -1;
				errors++;
			}
		}
		// Check if invalidation / validation
		if (errors == bands.length) {
			if (invalidate) {
				valid = labels2;
				message.setText("No invalidation");
			} else {
				valid = null;
				message.setText("Error, no validation");
			}
			return;
		}
		// Remove errors
		int bands2[] = new int[bands.length - errors];
		int j = 0;
		for (int i = 0; i < bands.length; i++)
			if (bands[i] != -1)
				bands2[j++] = bands[i] - 1;
		bands = bands2;
		Arrays.sort(bands);
		// Build the new image
		int nbBands = 0;
		if (invalidate) {
			nbBands = labels2.getBDim() - bands.length;
			nbInvalidate = bands.length;
		} else {
			nbBands = bands.length;
			nbInvalidate = labels2.getBDim() - bands.length;
		}
		if (invalidate && nbBands <= 0) {
			message.setText("Error with invalidation, no more clusters in  :"
					+ labelname);
			valid = null;
			return;
		} else if (invalidate && nbBands >= labels2.getBDim()) {
			message.setText("All bands are validated");
			valid = labels2;
			return;
		}
		valid = labels2.newInstance(labels2.getXDim(), labels2.getYDim(),
				labels2.getZDim(), labels2.getTDim(), nbBands);
		j = 0;
		for (int i = 0; i < labels2.getBDim(); i++)
			if ((invalidate && Arrays.binarySearch(bands, i) < 0)
					|| (!invalidate && Arrays.binarySearch(bands, i) >= 0))
				valid.setImage4D(labels2.getImage4D(i, Image.B), j++, Image.B);
		update();
	}

	private void invalidate2() {
		message.setText("Invalidate 2 in progress");
		boolean invalidate = invalidateBox2.isSelected();
		update();
		// If merging image has not been computed
		if (combi == null) {
			int clusters = Integer.parseInt(mergeText.getText());
			// Load label image and validate
			if (valid == null)
				try {
					if (labels == null)
						labels = ImageLoader.exec(labelname);
					if (labels instanceof ByteImage)
						labels = ((ByteImage) labels).copyToIntegerImage();
					invalidate();
					// Quit if nothing remains after invalidate
					if (valid == null) {
						update();
						return;
					}
				} catch (Exception ex) {
					message.setText("Unable to load: " + labelname);
					return;
				}
			combi = LabelCombination.exec(BinaryMasksToLabels
					.exec((BooleanImage) valid), clusters, false);
		}
		Image labels2 = combi;
		// Parse the String
		String[] result = invalidateText2.getText().split(",");
		int bands[] = new int[result.length];
		int errors = 0;
		for (int i = 0; i < result.length; i++) {
			try {
				bands[i] = Integer.parseInt(result[i]);
			} catch (NumberFormatException ex) {
				message.setText("Invalid cluster list");
				update();
			}
			if (bands[i] <= 0 || bands[i] > labels2.getBDim()) {
				bands[i] = -1;
				errors++;
			}
		}
		// Check if invalidation / validation
		if (errors == bands.length) {
			if (invalidate) {
				valid2 = labels2;
				message.setText("No invalidation");
			} else {
				valid2 = null;
				message.setText("Error, no validation");
			}
			return;
		}
		// Remove errors
		int bands2[] = new int[bands.length - errors];
		int j = 0;
		for (int i = 0; i < bands.length; i++)
			if (bands[i] != -1)
				bands2[j++] = bands[i] - 1;
		bands = bands2;
		Arrays.sort(bands);
		// Build the new image
		int nbBands = 0;
		if (invalidate) {
			nbBands = labels2.getBDim() - bands.length;
			nbInvalidate2 = bands.length;
		} else {
			nbBands = bands.length;
			nbInvalidate2 = labels2.getBDim() - bands.length;
		}
		if (invalidate && nbBands <= 0) {
			message.setText("Error with invalidation, no more clusters in  :"
					+ labelname);
			valid2 = null;
			return;
		} else if (invalidate && nbBands >= labels2.getBDim()) {
			message.setText("All bands are validated");
			valid2 = labels2;
			return;
		}
		valid2 = labels2.newInstance(labels2.getXDim(), labels2.getYDim(),
				labels2.getZDim(), labels2.getTDim(), nbBands);
		j = 0;
		for (int i = 0; i < labels2.getBDim(); i++)
			if ((invalidate && Arrays.binarySearch(bands, i) < 0)
					|| (!invalidate && Arrays.binarySearch(bands, i) >= 0))
				valid2.setImage4D(labels2.getImage4D(i, Image.B), j++, Image.B);
		update();
	}

	/**
	 * GUI interface
	 */
	private void gui() {
		// Create and set up the window.
		frame = new JFrame(appName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		mainPanel = frame.getContentPane();

		bar = new JPanel();
		mainPanel.add(bar, BorderLayout.SOUTH);
		bar.setLayout(new GridLayout(3, 1));
		JSeparator sep = new JSeparator();
		bar.add(sep);
		barFlags = new JPanel();
		barFlags.setLayout(new GridLayout(1, 6));
		isInput = new JLabel();
		isLabels = new JLabel();
		isValid = new JLabel();
		isValid2 = new JLabel();
		isResult = new JLabel();
		isReference = new JLabel();
		barFlags.add(isInput);
		barFlags.add(isLabels);
		barFlags.add(isValid);
		barFlags.add(isResult);
		barFlags.add(isReference);
		bar.add(barFlags);
		message = new JLabel("Welcome to Building Detection GUI Demo !");
		bar.add(message);

		panel = new JPanel();
		panel.setLayout(new GridLayout(22, 3));
		mainPanel.add(panel, BorderLayout.CENTER);

		// line 1 : file
		inputLabel = new JLabel("Input image");
		panel.add(inputLabel);
		inputText = new JTextField(filename);
		panel.add(inputText);
		inputText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				filename = inputText.getText();
				input = null;
				update();
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
					input = null;
					update();
				}
			}
		});
		// line 2 : binarisation method
		binLabel = new JLabel("Binarisation method");
		panel.add(binLabel);
		binBox = new JComboBox(binNames);
		binBox.setSelectedIndex(3);
		panel.add(binBox);
		binBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// binBox.addFocusListener(new FocusAdapter() {
				// public void focusLost(FocusEvent e) {
				binMode = binValues[binBox.getSelectedIndex()];
				binName = binNames[binBox.getSelectedIndex()];
				switch (binMode) {
				case OriginalBinaryBuildingDetection.BINARISATION_MANUAL:
					binParam.setText("0.5");
					break;
				case OriginalBinaryBuildingDetection.BINARISATION_AUTO:
					binParam.setText("no value required");
					break;
				case OriginalBinaryBuildingDetection.BINARISATION_CLUSTER:
					binParam.setText("0.8");
					break;
				case OriginalBinaryBuildingDetection.BINARISATION_KMEANS:
					binParam.setText("5");
					break;
				}
			}
		});
		binParam = new JTextField("5");
		panel.add(binParam);
		// line 3 : labels
		labelLabel = new JLabel("Label image");
		panel.add(labelLabel);
		labelText = new JTextField(labelname);
		panel.add(labelText);
		labelText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				labelname = labelText.getText();
				labels = null;
				valid = null;
				combi = null;
				update();
			}
		});
		labelButton = new JButton("Browse");
		panel.add(labelButton);
		labelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(path);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					try {
						labelname = chooser.getSelectedFile()
								.getCanonicalPath();
						path = chooser.getCurrentDirectory().getCanonicalPath();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					labelText.setText(labelname);
					labels = null;
					valid = null;
					combi = null;
					update();
				}
			}
		});
		// line 4 : actions load/binarize/display labels
		displayButton = new JButton("Display input");
		panel.add(displayButton);
		displayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (input == null)
						input = ContrastStretch
								.exec(ImageLoader.exec(filename));
					System.out.println(input);
					Viewer2D.exec(ContrastStretch.exec(input), filename);
					message.setText("Input displayed");
					update();
				} catch (Exception ex) {
					message.setText("Unable to load: " + filename);
				}
			}
		});
		binButton = new JButton("Binarize and Save");
		panel.add(binButton);
		binButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (input == null)
						input = ContrastStretch
								.exec(ImageLoader.exec(filename));
				} catch (Exception ex) {
					message.setText("Unable to load: " + filename);
					return;
				}
				update();
				message.setText("Binarization in progress");
				switch (binMode) {
				case OriginalBinaryBuildingDetection.BINARISATION_MANUAL:
					double thr = Double.parseDouble(binParam.getText());
					labels = ManualThresholding.exec(input, thr);
					labels = ((BooleanImage) labels).copyToByteImage();
					message.setText("Binarization completed");
					try {
						ImageSave.exec(labels, labelname);
					} catch (Exception ex) {
						message.setText("Unable to save: " + labelname);
					}// ImageSave.exec(new ByteImage(labels), labelname);
					break;
				case OriginalBinaryBuildingDetection.BINARISATION_AUTO:
					labels = RidlerThresholding.exec(input, false);
					labels = ((BooleanImage) labels).copyToByteImage();
					message.setText("Binarization completed");
					try {
						ImageSave.exec(labels, labelname);
					} catch (Exception ex) {
						message.setText("Unable to save: " + labelname);
					}// ImageSave.exec(new ByteImage(labels), labelname);
					break;
				case OriginalBinaryBuildingDetection.BINARISATION_CLUSTER:
					double ratio = Double.parseDouble(binParam.getText());
					labels = HistogramBasedClustering.exec(input, ratio);
					message.setText("Binarization completed");
					try {
						ImageSave.exec(((IntegerImage) labels)
								.copyToByteImage(), labelname);
					} catch (Exception ex) {
						message.setText("Unable to save: " + labelname);
					}
					break;
				case OriginalBinaryBuildingDetection.BINARISATION_KMEANS:
				case 4:// FAST K-MEANS
				case 5:// ULTRA FAST K-MEANS
					int k = Integer.parseInt(binParam.getText());
					if (binMode == 5)
						labels = WekaSegmentationKmeans.exec(input, k, 1);
					else if (binMode == 4)
						labels = WekaSegmentationKmeans.exec(input, k, 10);
					else
						labels = WekaSegmentationKmeans.exec(input, k);
					message.setText("Binarization completed");
					try {
						ImageSave.exec(((IntegerImage) labels)
								.copyToByteImage(), labelname);
					} catch (Exception ex) {
						message.setText("Unable to save: " + labelname);
					}
					break;
				}
				update();
			}
		});
		labelsButton = new JButton("Display labels");
		panel.add(labelsButton);
		labelsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (labels == null)
						labels = ImageLoader.exec(labelname);
					if (labels instanceof ByteImage)
						labels = ((ByteImage) labels).copyToIntegerImage();
					update();
					Viewer2D.exec(LabelsToRandomColors.exec(labels), labelname);
					message.setText("Labels displayed");
				} catch (Exception ex) {
					message.setText("Unable to load: " + labelname);
				}
			}
		});
		// line 5 : blank
		panel.add(new JLabel());
		panel.add(new JLabel());
		panel.add(new JLabel());
		// line : invalidate
		invalidateBox = new JCheckBox("Invalidate label");
		invalidateBox.setSelected(true);
		panel.add(invalidateBox);
		invalidateText = new JTextField("comma-separated list");
		invalidateText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				valid = null;
				update();
			}
		});
		panel.add(invalidateText);
		invalidateButton = new JButton("Display remaining");
		invalidateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (labels == null)
						labels = ImageLoader.exec(labelname);
					if (labels instanceof ByteImage)
						labels = ((ByteImage) labels).copyToIntegerImage();
				} catch (Exception ex) {
					message.setText("Unable to load: " + labelname);
					return;
				}
				invalidate();
				if (valid != null) {
					Viewer2D.exec(valid, "Invalidation of " + nbInvalidate
							+ " clusters : " + labelname);
					message.setText("Valid labels displayed");
				}
				update();
			}
		});
		panel.add(invalidateButton);
		// line 6 : merge
		mergeLabel = new JLabel("Merging size");
		panel.add(mergeLabel);
		mergeText = new JTextField("2");
		panel.add(mergeText);
		mergeButton = new JButton("Display merging");
		panel.add(mergeButton);
		mergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int clusters = Integer.parseInt(mergeText.getText());
				// Load label image and validate
				if (valid == null)
					try {
						if (labels == null)
							labels = ImageLoader.exec(labelname);
						if (labels instanceof ByteImage)
							labels = ((ByteImage) labels).copyToIntegerImage();
						invalidate();
						// Quit if nothing remains after invalidate
						if (valid == null) {
							update();
							return;
						}
					} catch (Exception ex) {
						message.setText("Unable to load: " + labelname);
						return;
					}
				message.setText("Merge in progress");
				update();
				combi = LabelCombination.exec(BinaryMasksToLabels
						.exec((BooleanImage) valid), clusters, false);
				Viewer2D.exec(combi, "Combination of " + clusters
						+ " clusters: " + labelname);
				message.setText("Merge displayed");
			}
		});
		// line : invalidate
		invalidateBox2 = new JCheckBox("Invalidate merged label");
		invalidateBox2.setSelected(true);
		panel.add(invalidateBox2);
		invalidateText2 = new JTextField("comma-separated list");
		invalidateText2.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				valid2 = null;
				update();
			}
		});
		panel.add(invalidateText2);
		invalidateButton2 = new JButton("Display remaining");
		invalidateButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invalidate2();
				if (valid2 != null) {
					Viewer2D.exec(valid2, "Invalidation of " + nbInvalidate2
							+ " clusters : " + labelname);
					message.setText("Valid merged labels displayed");
				}
				update();
			}
		});
		panel.add(invalidateButton2);
		// lines 7/8 : x/y min/max
		xLabel = new JLabel("Width min/max");
		panel.add(xLabel);
		xminText = new JTextField("11");
		panel.add(xminText);
		xmaxText = new JTextField("49");
		panel.add(xmaxText);
		yLabel = new JLabel("Height min/max");
		panel.add(yLabel);
		yminText = new JTextField("11");
		panel.add(yminText);
		ymaxText = new JTextField("49");
		panel.add(ymaxText);
		// line 9 : angle
		angleLabel = new JLabel("Angle number / start");
		panel.add(angleLabel);
		anglesText = new JTextField("4");
		panel.add(anglesText);
		angleFirstText = new JTextField("0");
		panel.add(angleFirstText);
		// line : alpha
		alphaLabel = new JLabel("Uncertainty / min ratio");
		panel.add(alphaLabel);
		alphaText = new JTextField("0.6");
		panel.add(alphaText);
		ratioText = new JTextField("0");
		panel.add(ratioText);
		// line 10 : filtering method
		filterLabel = new JLabel("Filtering");
		panel.add(filterLabel);
		filterBox = new JComboBox(filterNames);
		filterBox.setSelectedIndex(4);
		panel.add(filterBox);
		filterBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterMode = filterValues[filterBox.getSelectedIndex()];
				filterName = filterNames[filterBox.getSelectedIndex()];
			}
		});
		panel.add(new JLabel());
		// line 14 : result
		resultLabel = new JLabel("Result image");
		panel.add(resultLabel);
		resultText = new JTextField(resname);
		panel.add(resultText);
		resultText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				resname = resultText.getText();
				result = null;
			}
		});
		resultButton = new JButton("Browse");
		panel.add(resultButton);
		resultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(path);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					try {
						resname = chooser.getSelectedFile().getCanonicalPath();
						path = chooser.getCurrentDirectory().getCanonicalPath();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					resultText.setText(resname);
					result = null;
				}
			}
		});
		// new line
		expLabel = new JLabel("Experiment name");
		panel.add(expLabel);
		expText = new JTextField("");
		panel.add(expText);
		infoButton = new JButton("Info");
		panel.add(infoButton);
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								frame,
								"Toward a publication in IEEE Transactions...\n\nHotline : +33 3 90 24 45 70 or lefevre@lsiit.u-strasbg.fr");
			}
		});
		// new line
		launchButton = new JButton("Detect and Save");
		panel.add(launchButton);
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				message.setText("Detection in progress");
				if (valid2 == null)
					invalidate2();
				// if (combi == null) {
				// int clusters = Integer.parseInt(mergeText.getText());
				// // Chargement de l'image label puis validation
				// if (valid == null)
				// try {
				// if (labels == null)
				// labels = ImageLoader.exec(labelname);
				// if (labels instanceof ByteImage)
				// labels = ((ByteImage) labels).copyToIntegerImage();
				// invalidate();
				// // On quitte si plus rien aprÃšs l'invalidation
				// if (valid == null) {
				// update();
				// return;
				// }
				// } catch (Exception ex) {
				// message.setText("Unable to load: " + labelname);
				// return;
				// }
				// message.setText("Merge in progress");
				// update();
				// combi = LabelCombination.exec(BinaryMasksToLabels
				// .exec((BooleanImage) valid), clusters, false);
				// }

				int XMin = Integer.parseInt(xminText.getText());
				int YMin = Integer.parseInt(yminText.getText());
				int XMax = Integer.parseInt(xmaxText.getText());
				int YMax = Integer.parseInt(ymaxText.getText());
				int af = Integer.parseInt(angleFirstText.getText());
				int nbangles = Integer.parseInt(anglesText.getText());
				int minRatio = Integer.parseInt(ratioText.getText());
				double alpha = Double.parseDouble(alphaText.getText());
				boolean globalTTR = false;
				int step = 2;
				String exp = expText.getText().trim();
				boolean debug = !exp.isEmpty();
				// result = HMTBuildingDetection.exec(combi, XMin, YMin, XMax,
				// YMax,
				// false, debug, debug, filterMode, af, nbangles, globalTTR,
				// minRatio,
				// exp,step,alpha);
				result = HMTBuildingDetection.exec(valid2, XMin, YMin, XMax,
						YMax, false, debug, debug, filterMode, af, nbangles,
						globalTTR, minRatio, exp, step, alpha);
				try {
					ImageSave.exec(result, resname);
					message.setText("Result completed and saved");
				} catch (Exception ex) {
					message.setText("Unable to save: " + resname);
				}
				update();
			}
		});
		compResButton = new JButton("Display result");
		panel.add(compResButton);
		compResButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (result == null)
						result = ManualThresholding.exec(ImageLoader
								.exec(resname), 0.5);
					Viewer2D.exec(result, resname);
					message.setText("Result displayed");
				} catch (Exception ex) {
					message.setText("Unable to load: " + resname);
				}
				update();
			}
		});
		exitButton = new JButton("Exit");
		panel.add(exitButton);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(frame,
						"Confirmer la sortie ?", "BuildingDetectionGUIDemo",
						JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
					frame.dispose();
			}
		});
		// line 13 : blank
		panel.add(new JLabel());
		panel.add(new JLabel());
		panel.add(new JLabel());
		// line 15 : reference
		referenceLabel = new JLabel("Reference image");
		panel.add(referenceLabel);
		referenceText = new JTextField(refname);
		panel.add(referenceText);
		referenceText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				refname = referenceText.getText();
				reference = null;
				update();
			}
		});
		referenceButton = new JButton("Browse");
		panel.add(referenceButton);
		referenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(path);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					try {
						refname = chooser.getSelectedFile().getCanonicalPath();
						path = chooser.getCurrentDirectory().getCanonicalPath();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					referenceText.setText(refname);
					reference = null;
					update();
				}
			}
		});
		// line 16 : stats
		statLabel = new JLabel("Statistics file");
		panel.add(statLabel);
		statText = new JTextField(statname);
		panel.add(statText);
		statText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				statname = statText.getText();
			}
		});
		statButton = new JButton("Browse");
		panel.add(statButton);
		statButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(path);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					try {
						statname = chooser.getSelectedFile().getCanonicalPath();
						path = chooser.getCurrentDirectory().getCanonicalPath();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					statText.setText(statname);
				}
			}
		});
		compareButton = new JButton("Evaluate");
		panel.add(compareButton);
		compareButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (result == null)
					try {
						result = ManualThresholding.exec(ImageLoader
								.exec(resname), 0.5);
					} catch (Exception ex) {
						message.setText("Unable to load: " + resname);
					}
				if (reference == null)
					try {
						reference = ManualThresholding.exec(ImageLoader
								.exec(refname), 0.5);
					} catch (Exception ex) {
						message.setText("Unable to load: " + refname);
					}
				update();
				PrintStream out;
				boolean toFile = true;
				try {
					out = new PrintStream(new FileOutputStream(statname, true));
				} catch (Exception ex) {
					message.setText("Unable to write in : " + statname);
					out = System.out;
					toFile = false;
				}
				if (result != null && reference != null) {
					// Print params
					out.println("\n\nCurrent Date Time : "
							+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
									.format(new java.util.Date()));
					out.println(params());
					// Print evaluation stats
					message.setText("Evaluation in progess");
					out.println("\nOld evaluation\n==============");
					out.println(DetectionQualityEvaluation.exec(result,
							reference, true, true, true));
					out.println("\nNew evaluation\n==============");
					Properties prop = ConfusionMatrix.exec(reference, result,
							false, true);
					prop.list(out);
					message.setText("Evaluation completed");
				}
				if (toFile)
					out.close();
			}
		});
		// line 17 : compare
		compRefButton = new JButton("Display reference");
		panel.add(compRefButton);
		compRefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (reference == null)
						reference = ManualThresholding.exec(ImageLoader
								.exec(refname), 0.5);
					update();
					Viewer2D.exec(reference, refname);
					message.setText("Reference displayed");
				} catch (Exception ex) {
					message.setText("Unable to load: " + refname);
				}
			}
		});
		refresButton = new JButton("Result & Reference");
		panel.add(refresButton);
		refresButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (result == null)
						result = ManualThresholding.exec(ImageLoader
								.exec(resname), 0.5);
				} catch (Exception ex) {
					message.setText("Unable to load: " + resname);
					return;
				}
				try {
					if (reference == null)
						reference = ManualThresholding.exec(ImageLoader
								.exec(refname), 0.5);
				} catch (Exception ex) {
					message.setText("Unable to load: " + refname);
					return;
				}
				update();
				String title = (resname.indexOf('/') == -1 ? resname : resname
						.substring(resname.lastIndexOf('/') + 1))
						+ " on "
						+ (refname.indexOf('/') == -1 ? refname : refname
								.substring(refname.lastIndexOf('/') + 1));
				Image tmp = new ByteImage(result.getXDim(), result.getYDim(),
						result.getZDim(), result.getTDim(), 3);
				tmp.fill(0);
				tmp.setImage4D(XOR.exec(reference, result), 0, Image.B);
				tmp.setImage4D(result, 1, Image.B);
				tmp.setColor(true);
				Viewer2D.exec(tmp, title);
				message.setText("Result & Refence displayed");
			}
		});
		// new line
		panel.add(new JLabel());
		panel.add(new JLabel());
		panel.add(new JLabel());
		// new line
		viewLabels = new JButton("Overlay labels");
		panel.add(viewLabels);
		viewLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (labels == null)
						labels = ImageLoader.exec(labelname);
				} catch (Exception ex) {
					message.setText("Unable to load: " + labelname);
					return;
				}
				try {
					if (input == null)
						input = ContrastStretch
								.exec(ImageLoader.exec(filename));
				} catch (Exception ex) {
					message.setText("Unable to load: " + filename);
					return;
				}
				update();
				Image tmp = input;
				LabelsToRandomColors.exec(labels);
				if (input.getBDim() < 3)
					tmp = GrayToRGB.exec(input);
				else if (input.getBDim() > 3)
					tmp = ColorImageFromMultiBandImage.exec(input, 2, 1, 0);
				String title = (labelname.indexOf('/') == -1 ? labelname
						: labelname.substring(labelname.lastIndexOf('/') + 1))
						+ " on "
						+ (filename.indexOf('/') == -1 ? filename : filename
								.substring(filename.lastIndexOf('/') + 1));
				Viewer2D.exec(Blending.exec(tmp, LabelsToRandomColors
						.exec(labels), 0.5), title);
				message.setText("Labels overlayed");
			}
			// ??? String s = 1 == 0 ? " " : " (";
		});
		viewResult = new JButton("Overlay result");
		panel.add(viewResult);
		viewResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (result == null)
						result = ManualThresholding.exec(ImageLoader
								.exec(resname), 0.5);
				} catch (Exception ex) {
					message.setText("Unable to load: " + resname);
					return;
				}
				try {
					if (input == null)
						input = ContrastStretch
								.exec(ImageLoader.exec(filename));
				} catch (Exception ex) {
					message.setText("Unable to load: " + filename);
					return;
				}
				update();
				String title = (resname.indexOf('/') == -1 ? resname : resname
						.substring(resname.lastIndexOf('/') + 1))
						+ " on "
						+ (filename.indexOf('/') == -1 ? filename : filename
								.substring(filename.lastIndexOf('/') + 1));
				Viewer2D.exec(Maximum.exec(input, BinaryGradient.exec(
						result,
						FlatStructuringElement2D
								.createSquareFlatStructuringElement(3))
						.duplicateDimension(0, input.getBDim(), Image.B)),
						title);
				message.setText("Result overlayed");
			}
		});
		viewReference = new JButton("Overlay reference");
		panel.add(viewReference);
		viewReference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (reference == null)
						reference = ManualThresholding.exec(ImageLoader
								.exec(refname), 0.5);
				} catch (Exception ex) {
					message.setText("Unable to load: " + refname);
					return;
				}
				try {
					if (input == null)
						input = ContrastStretch
								.exec(ImageLoader.exec(filename));
				} catch (Exception ex) {
					message.setText("Unable to load: " + filename);
					return;
				}
				update();
				String title = (refname.indexOf('/') == -1 ? refname : refname
						.substring(refname.lastIndexOf('/') + 1))
						+ " on "
						+ (filename.indexOf('/') == -1 ? filename : filename
								.substring(filename.lastIndexOf('/') + 1));
				Viewer2D.exec(Maximum.exec(input, BinaryGradient.exec(
						reference,
						FlatStructuringElement2D
								.createSquareFlatStructuringElement(3))
						.duplicateDimension(0, input.getBDim(), Image.B)),
						title);
				message.setText("Reference overlayed");
			}
		});
		// Display the window.
		frame.pack();
		frame.setVisible(true);
		exitButton.requestFocusInWindow();
	}
}
