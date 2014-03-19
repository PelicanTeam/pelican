package fr.unistra.pelican.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.Random;

import javax.media.jai.GraphicsJAI;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import com.sun.media.jai.widget.DisplayJAI;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.conversion.GrayToRGB;
import fr.unistra.pelican.algorithms.conversion.HSYToRGB;

/**
 * The Draw2D class allows the user to draw markers with or without a background
 * image.
 * 
 * @author Florent Sollier, Jonathan Weber
 * 
 */
public class OldDraw2D extends JPanel {

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;

	/**
	 * Scroll pane for the image
	 */
	public JScrollPane scroll;

	/**
	 * Name of the background image file
	 */
	public String title;

	/**
	 * Maximum stroke size for the bruch
	 */
	public final int MAX_BRUSH_SIZE = 100;

	/**
	 * Defaut stroke size of the brush
	 */
	public final int DEFAULT_BRUSH = 5;

	/**
	 * Button wich allows the user ti add a new label
	 */
	public JButton plusButton = new JButton();

	/**
	 * Combobox which contains all the labels
	 */
	public JComboBox labelComboBox = new JComboBox();

	/**
	 * Spinner which allows the user to change the stroke size of the brush
	 */
	public JSpinner brushSpinner = new JSpinner();

	/**
	 * Spinner number model of the brush spinner
	 */
	public SpinnerNumberModel brushSpinnerModel;

	/**
	 * Panel which contains the color of the current label
	 */
	public JPanel labelColor = new JPanel();

	/**
	 * Label of the brush stroke size
	 */
	public JLabel bruschLabel = new JLabel();

	/**
	 * label for the transparency slider
	 */
	public JLabel transparencyLabel = new JLabel();

	/**
	 * To change the transparency of the marker image
	 */
	public JSlider transparencySlider = new JSlider();

	/**
	 * Button to undo the last label painting
	 */
	public JButton undoButton = new JButton();

	/**
	 * Button to confirm marker drawing's end
	 */
	public JButton okButton = new JButton();

	/**
	 * Button to begin a new marker image
	 */
	public JButton resetButton = new JButton();

	/**
	 * Image builder frame
	 */
	public JDialog frame = new JDialog();

	/**
	 * Instance of MarkerDisplayJAI
	 */
	public OldMarkerDisplayJAI display = new OldMarkerDisplayJAI();

	/**
	 * The background image converted in BufferedImage
	 */
	private BufferedImage bimg = null;

	/**
	 * The marker image
	 */
	public ByteImage output;
	
	/**
	 * markers Image
	 */
	public ByteImage markersImage = null;
	
	/**
	 * Background image
	 */
	public Image inputImage;

	/**
	 * true if resetting
	 */
	private boolean reset = false;

	/***************************************************************************
	 * 
	 * 
	 * Constructors
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Constructor with a background image
	 * 
	 * @param inputImage
	 *            the fr.unistra.pelican image to set as background
	 * @param fileName Name of the background image file
	 */
	public OldDraw2D(Image inputImage, String title) {

		this.inputImage = inputImage;
		this.title = title;
		bimg = pelicanImageToBufferedImage(this.inputImage);
		// The BufferedImage is setted to display
		display.set(bimg);
		guiInitialisation();
	}
	
	/**
	 * Constructor with a background image
	 * 
	 * @param inputImage
	 *            the fr.unistra.pelican image to set as background
	 * @param fileName Name of the background image file
	 */
	public OldDraw2D(Image inputImage, String title, ByteImage markersImage) {

		this.inputImage = inputImage;
		this.title = title;
		this.markersImage= markersImage;
		bimg = pelicanImageToBufferedImage(this.inputImage);
		// The BufferedImage is setted to display
		display.set(bimg, this.markersImage);
		guiInitialisation();
	}


	
	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Initialisation of the Draw2D GUI
	 * 
	 */
	private void guiInitialisation() {

		frame = new JDialog();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setModal(true);

		this.setLayout(new BorderLayout());

		JPanel jPanel2 = new JPanel();
		jPanel2.setLayout(new GridBagLayout());

		JPanel jPanel3 = new JPanel();
		jPanel3.setLayout(new BorderLayout());

		// Component which contains the MarkerDisplayJAI instance (display)
		scroll = new JScrollPane(display,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scroll, BorderLayout.CENTER);

		brushSpinnerModel = new SpinnerNumberModel(DEFAULT_BRUSH, 1,
				MAX_BRUSH_SIZE, 1);

		labelComboBox.setBackground(SystemColor.control);
		labelComboBox.setOpaque(false);
		labelComboBox.addItem("Eraser  ");
		labelComboBox.addItem("Label 1 ");
		labelComboBox.setSelectedIndex(1);
		labelComboBox.addActionListener(new Draw2D_labelComboBox_actionAdapter(this));
		

		plusButton.setText("+");
		plusButton.addActionListener(new Draw2D_plusButton_actionAdapter(this));

		labelColor.setBorder(BorderFactory.createLineBorder(Color.black));
		labelColor.setMinimumSize(new Dimension(20, 20));
		labelColor.setPreferredSize(new Dimension(20, 20));
		bruschLabel.setBackground(SystemColor.control);
		bruschLabel.setText("Thickness: ");
		brushSpinner.setModel(brushSpinnerModel);
		brushSpinner.setMaximumSize(new Dimension(32767, 32767));
		brushSpinner.setMinimumSize(new Dimension(40, 18));
		brushSpinner.setPreferredSize(new Dimension(40, 18));
		brushSpinner.addChangeListener(new Draw2D_brushSpinner_actionAdapter(
				this));
		transparencyLabel.setText("         Label Transparency:");
		transparencySlider.setExtent(0);
		transparencySlider.setMaximum(255);
		transparencySlider.setPaintLabels(false);
		transparencySlider.setPaintTicks(false);
		transparencySlider.setPaintTrack(true);
		transparencySlider.setBackground(SystemColor.control);
		transparencySlider.setMaximumSize(new Dimension(32767, 24));
		transparencySlider.setValue(255);
		transparencySlider
				.addChangeListener(new Draw2D_transparencySlider_changeAdapter(
						this));

		undoButton.setText("Undo");
		undoButton.addActionListener(new Draw2D_undoButton_actionAdapter(this));
		jPanel3.add(undoButton, BorderLayout.WEST);
		undoButton.setEnabled(false);

		okButton.setText("Ok");
		okButton.addActionListener(new Draw2D_okButton_actionAdapter(this));
		jPanel3.add(okButton, BorderLayout.EAST);
		okButton.setEnabled(false);

		resetButton.setText("Reset");
		resetButton
				.addActionListener(new Draw2D_resetButton_actionAdapter(this));
		jPanel3.add(resetButton, BorderLayout.CENTER);
		resetButton.setEnabled(false);

		jPanel2.add(labelColor, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						5, 0, 5, 0), 0, 0));
		jPanel2.add(brushSpinner, new GridBagConstraints(5, 0, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						0, 5, 0), 0, 0));
		jPanel2.add(plusButton, new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						5, 5, 5, 5), 0, 0));
		jPanel2.add(labelComboBox, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						15, 5, 0), 0, 0));
		jPanel2.add(bruschLabel, new GridBagConstraints(4, 0, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						15, 5, 0), 0, 0));
		jPanel2.add(transparencyLabel, new GridBagConstraints(7, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 0, 0, 5), 0, 0));
		jPanel2.add(transparencySlider, new GridBagConstraints(8, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 0, 5, 5), 0, 0));

		this.add(jPanel2, BorderLayout.NORTH);
		this.add(jPanel3, BorderLayout.SOUTH);
		
		if(markersImage!=null)
		{
			for(int i=0; i<markersImage.maximumByte()-1;i++)
			{
				// Update plusButtonIndex
				display.plusButtonIndex++;

				// Update currentColorIndex
				display.setMarker(display.plusButtonIndex);

				// set the current color to labelColor
				Color color = display.getColorMarker();
				labelColor.setBackground(color);

				// add a label to the combobox
				labelComboBox.addItem("Label " + display.plusButtonIndex);
				labelComboBox.setSelectedItem("Label " + display.plusButtonIndex);
			}
			okButton.setEnabled(true);
			resetButton.setEnabled(true);
		}
		frame.setTitle(title);
		frame.setContentPane(this);
		frame.pack();
		frame.setPreferredSize(new Dimension(800, 400));
		frame.setVisible(true);

	}

	/**
	 * Method to get an estimation (upper bound) of the number of labels
	 */
	public int labels(){
		return display.plusButtonIndex;
	}
	
	/**
	 * Method which converts the background image (Image type) to a
	 * BufferedImage
	 */
	private BufferedImage pelicanImageToBufferedImage(Image inputImage) {

		if (inputImage.getBDim()!=3)
			inputImage=GrayToRGB.exec(AverageChannels.exec(inputImage));
		
		BufferedImage bimg = null;

		final int bdim = inputImage.getBDim();
		final int tdim = inputImage.getTDim();
		final int zdim = inputImage.getZDim();

		// transform the image into an array of BufferedImages
		DataBufferByte dbb;
		SampleModel s;
		Raster r;
		
		int[] bandOffsets = { 0, 1, 2 };

		for (int t = 0; t < tdim; t++) {
			for (int z = 0; z < zdim; z++) {

				ByteImage tmp = inputImage.getColorByteChannelZT(z, t);				

				byte[] tmp2 = new byte[tmp.size()];
				for (int i = 0; i < tmp.size(); i++)
					tmp2[i] = (byte) tmp.getPixelByte(i);

				dbb = new DataBufferByte(tmp2, tmp.size());
				s = RasterFactory.createPixelInterleavedSampleModel(
						DataBuffer.TYPE_BYTE, tmp.getXDim(), tmp.getYDim(),
						bdim, bdim * tmp.getXDim(), bandOffsets);
				r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
				bimg = new BufferedImage(tmp.getXDim(), tmp.getYDim(),
						BufferedImage.TYPE_3BYTE_BGR);
				bimg.setData(r);

			}
		}
		return bimg;
	}

	/***************************************************************************
	 * 
	 * 
	 * GUI functionnalities
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Method which allows the user to remove or re-insert the last marker drawn
	 */
	public void undoButton_actionPerformed(ActionEvent e) {

		display.undo();
	}

	/**
	 * this method will remove all the markers drawn by the user
	 * 
	 * @param e
	 */
	public void resetButton_actionPerformed(ActionEvent e) {

		// resetting
		reset = true;

		// Reset variables
		display.set(bimg);
		display.plusButtonIndex = 1;
		display.currentColorIndex[0] = (float) 1 / 255;

		// Remove all items and re-add the first ones
		labelComboBox.removeAllItems();
		labelComboBox.addItem("Eraser  ");
		labelComboBox.addItem("Label 1 ");
		labelComboBox.setSelectedIndex(1);

		// set the current color to labelColor
		display.setMarker(labelComboBox.getSelectedIndex());
		Color color = display.getColorMarker();
		labelColor.setBackground(color);

		// reset process ended
		reset = false;
	}

	/**
	 * Sets the thicknees of the pen
	 * 
	 * @param e
	 */
	public void brushSpinner_changed(ChangeEvent e) {

		if (brushSpinner.getValue() instanceof Number) {
			Number n = (Number) brushSpinner.getValue();
			float brushSize = n.floatValue();
			display.setStroke(new BasicStroke(brushSize));
		}

	}

	/**
	 * Changes the transparency of the markers
	 * 
	 * @param e
	 */
	public void transparencySlider_stateChanged(ChangeEvent e) {

		int transparency = transparencySlider.getValue();
		display.setMarkerTransparency(transparency);
	}

	/**
	 * Adds a new label in the combobox and sets the new color
	 * 
	 * @param e
	 */
	public void labelComboBox_actionPerformed(ActionEvent e) {

		if (!reset) {

			display.setMarker(labelComboBox.getSelectedIndex());

			// set the current color to labelColor
			Color color = display.getColorMarker();
			labelColor.setBackground(color);
		}

	}

	/**
	 * Adds a new label
	 * 
	 * @param e
	 */
	public void plusButton_actionPerformed(ActionEvent e) {

		// Update plusButtonIndex
		display.plusButtonIndex++;

		// Update currentColorIndex
		display.setMarker(display.plusButtonIndex);

		// set the current color to labelColor
		Color color = display.getColorMarker();
		labelColor.setBackground(color);

		// add a label to the combobox
		labelComboBox.addItem("Label " + display.plusButtonIndex);
		labelComboBox.setSelectedItem("Label " + display.plusButtonIndex);

	}

	/**
	 * Generates an fr.unistra.pelican.image from the marker image
	 * 
	 * @param e
	 */
	public void okButton_actionPerformed(ActionEvent e) {

		// Gets the marker image
		RenderedImage source = display.getMarkerImage();
		// Transform the marker image as a bufferedImage for the transformation
		BufferedImage im = ((PlanarImage) source).getAsBufferedImage();
		// raster gets the value of each pixel from im
		Raster raster = im.getData();
		// Save the type of im
		int type = im.getType();
		// Save the height of im
		int height = raster.getHeight();
		// Save the width of im
		int width = raster.getWidth();
		// Set the number of band to 1, we are looking for a greyscale image
		// without alpha channel
		int band = 1;

		// Instanciates output with the correct width, height and number of band
		output = new ByteImage(width, height, 1, 1, band);

		// Transfers each byte from raster to output
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				output.setPixelXYBByte(i, j, 0, (byte) raster
						.getSample(i, j, 0));

		// Set the color paramater
		output.setColor(false);
		// set the type parameter
		output.type = type;

		frame.dispose();

	}

	/***************************************************************************
	 * 
	 * 
	 * Listener inner-classes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * brushSpinner actionAdapter uses to call brushSpinner_changed(e)
	 */
	public class Draw2D_brushSpinner_actionAdapter implements
			javax.swing.event.ChangeListener {
		OldDraw2D d2d;

		Draw2D_brushSpinner_actionAdapter(OldDraw2D d2d) {
			this.d2d = d2d;
		}

		public void stateChanged(ChangeEvent e) {
			d2d.brushSpinner_changed(e);
		}
	}

	/**
	 * undoButton actionAdapter uses to call undoButton_actionPerformed(e)
	 */
	public class Draw2D_undoButton_actionAdapter implements
			java.awt.event.ActionListener {
		OldDraw2D d2d;

		Draw2D_undoButton_actionAdapter(OldDraw2D d2d) {
			this.d2d = d2d;
		}

		public void actionPerformed(ActionEvent e) {
			d2d.undoButton_actionPerformed(e);
		}
	}

	/**
	 * labelComboBox actionAdapter uses to call labelComboBox_actionPerformed(e)
	 */
	public class Draw2D_labelComboBox_actionAdapter implements
			java.awt.event.ActionListener {
		OldDraw2D d2d;

		Draw2D_labelComboBox_actionAdapter(OldDraw2D d2d) {
			this.d2d = d2d;
		}

		public void actionPerformed(ActionEvent e) {
			d2d.labelComboBox_actionPerformed(e);
		}
	}

	/**
	 * okButton actionAdapter uses to call okButton_actionPerformed(e)
	 */
	public class Draw2D_okButton_actionAdapter implements
			java.awt.event.ActionListener {
		OldDraw2D d2d;

		Draw2D_okButton_actionAdapter(OldDraw2D d2d) {
			this.d2d = d2d;
		}

		public void actionPerformed(ActionEvent e) {
			d2d.okButton_actionPerformed(e);
		}
	}

	/**
	 * resetButton actionAdapter uses to call resetButton_actionPerformed(e)
	 */
	public class Draw2D_resetButton_actionAdapter implements
			java.awt.event.ActionListener {
		OldDraw2D d2d;

		Draw2D_resetButton_actionAdapter(OldDraw2D d2d) {
			this.d2d = d2d;
		}

		public void actionPerformed(ActionEvent e) {
			d2d.resetButton_actionPerformed(e);
		}
	}

	/**
	 * plusButton actionAdapter uses to call plusButton_actionPerformed(e)
	 */
	public class Draw2D_plusButton_actionAdapter implements
			java.awt.event.ActionListener {
		OldDraw2D d2d;

		Draw2D_plusButton_actionAdapter(OldDraw2D d2d) {
			this.d2d = d2d;
		}

		public void actionPerformed(ActionEvent e) {
			d2d.plusButton_actionPerformed(e);
		}
	}

	/**
	 * transparencySlider changeAdapter uses to call
	 * transparencySlider_stateChanged(e)
	 */
	public class Draw2D_transparencySlider_changeAdapter implements
			javax.swing.event.ChangeListener {
		OldDraw2D d2d;

		Draw2D_transparencySlider_changeAdapter(OldDraw2D d2d) {
			this.d2d = d2d;
		}

		public void stateChanged(ChangeEvent e) {
			d2d.transparencySlider_stateChanged(e);
		}
	}

	/***************************************************************************
	 * 
	 * 
	 * MarkerDisplayJAI
	 * 
	 * 
	 **************************************************************************/

	/**
	 * The MarkerDisplayJAI handles all theimage traitments, including for the
	 * background image and for the marker image.
	 */
	class OldMarkerDisplayJAI extends DisplayJAI {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The marker image
		 */
		public TiledImage markerImage1;

		/**
		 * oldMarkerRi is use to save the marker image after after each draw in
		 * case of an undo.
		 */
		public WritableRenderedImage oldMarkerRi;

		/**
		 * Indice of the transparency of the markers (255 = completely visible)
		 */
		public int markerTransparency = 255;

		/**
		 * last x position of the cursor
		 */
		public int last_x;

		/**
		 * last y position of the cursor
		 */
		public int last_y;

		/**
		 * The color marker image (after the createColorMarkerImage process)
		 */
		public RenderedOp colorMarkerImage;

		/**
		 * Determines if the user is drawing or not
		 */
		public boolean drawing = false;

		/**
		 * Thickness of the pen
		 */
		public Stroke stroke = new BasicStroke(5.0f);

		/**
		 * Contain the current color index
		 */
		float currentColorIndex[];

		/**
		 * Button to add a new label
		 */
		int plusButtonIndex;

		/**
		 * Instance of random to generate random numbers
		 */
		Random random;

		/**
		 * Map which contains all the label with their associated color
		 */
		int[][] colorMap = new int[257][4];

		/**
		 * Constructor
		 * 
		 */
		OldMarkerDisplayJAI() {
			super();
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

			// Initializes variables
			currentColorIndex = new float[1];
			currentColorIndex[0] = (float) 1 / 255;
			plusButtonIndex = 1;

			addMouseListener(this);
			addMouseMotionListener(this);

			// Fill colorMap with 256 random colors
			initColors();

		}

		/**
		 * Method which sets the background image and creates the invisible
		 * image based on the dimension of the background image.
		 */
		public void set(RenderedImage im) {
			super.set(im);

			// Create a marker image without an alpha channel
			markerImage1 = ImageCreator.createGrayImage(im.getWidth(), im
					.getHeight());

			// repaint the component
			createColorMarkerImage();
			repaint();

		}
		
		/**
		 * Method which sets the background image and creates the markers
		 * image based on markers image on parameters.
		 */
		public void set(RenderedImage im, ByteImage markersImage) {
			super.set(im);

			// Create a marker image without an alpha channel
			markerImage1 = ImageCreator.createGrayImage(markersImage);
			
			
			// repaint the component
			createColorMarkerImage();
			repaint();

		}
		

		/**
		 * 
		 * @return the marker image
		 */
		public RenderedImage getMarkerImage() {
			return markerImage1;
		}

		/**
		 * 
		 * @return a copy of the marker image
		 */
		public WritableRenderedImage copyMarkerImage() {
			WritableRenderedImage copy = ImageCreator.createGrayImage(
					markerImage1.getWidth(), markerImage1.getHeight());
			copy.setData(markerImage1.copyData());
			return copy;
		}

		/**
		 * Set the marker transparency
		 * 
		 * @param markerTransparency
		 *            indice of transparency
		 */
		public void setMarkerTransparency(int markerTransparency) {
			if (colorMap != null) {
				this.markerTransparency = markerTransparency;

				// Set the colorMap transparency for all but the first
				// element
				for (int i = 1; i < 256; i++) {
					colorMap[i][3] = (byte) markerTransparency;
				}
				// refresh
				createColorMarkerImage();
				repaint();
			}
		}

		/**
		 * paintComponent method
		 */
		public void paintComponent(Graphics g) {
			// Paint the image in super class
			super.paintComponent(g);

			if (colorMarkerImage != null) {
				// Get graphics and create GraphicsJAI
				Graphics2D g2d = (Graphics2D) g;
				GraphicsJAI gj = GraphicsJAI.createGraphicsJAI(g2d, this);

				// Draw marker image
				gj.drawRenderedImage(colorMarkerImage, new AffineTransform());
			}
		}

		/**
		 * Mouse pressed method
		 */
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == e.BUTTON1) {
				if (markerImage1 != null) {
					// save old image for undo
					oldMarkerRi = copyMarkerImage();

					drawing = true;

					last_x = e.getX();
					last_y = e.getY();

					Graphics2D g2d = markerImage1.createGraphics();

					// Set line width and marker (Aplha is 1.0,
					// because colorMap handels transparency)
					g2d.setStroke(stroke);
					g2d.setColor(new Color(markerImage1.getColorModel()
							.getColorSpace(), currentColorIndex, 1.0f));
					// Draw the line
					g2d.draw(new Line2D.Double(e.getX(), e.getY(), e.getX(), e
							.getY()));

					// Update
					createColorMarkerImage();

					// Repaint the component
					repaint();

				}
			}
		}

		/**
		 * Mouse dragged method
		 */
		public void mouseDragged(MouseEvent e) {

			if (drawing == true) {
				if (markerImage1 != null) {

					Graphics2D g2d = markerImage1.createGraphics();

					// Set line width and marker (Aplha is 1.0 this time,
					// because lut handels transparency)
					g2d.setStroke(stroke);
					g2d.setColor(new Color(markerImage1.getColorModel()
							.getColorSpace(), currentColorIndex, 1.0f));
					// Draw the line
					g2d.draw(new Line2D.Double(e.getX(), e.getY(), last_x,
							last_y));

					// Update
					createColorMarkerImage();

					// Repaint the component
					repaint();

					last_x = e.getX();
					last_y = e.getY();
				}
			}
		}

		/**
		 * Mouse released method
		 */
		public void mouseReleased(MouseEvent e) {

			if (e.getButton() == e.BUTTON1) {
				drawing = false;
			}
			if (markerImage1 != null) {

				// Enable buttons
				undoButton.setEnabled(true);
				okButton.setEnabled(true);
				resetButton.setEnabled(true);

			}
		}

		/**
		 * Set the thickness of the pen
		 * 
		 * @param stroke
		 *            value of the stroke
		 */
		public void setStroke(Stroke stroke) {
			this.stroke = stroke;
		}

		/**
		 * 
		 * Creates a color marker image from the marker image (greyscale)
		 */
		public void createColorMarkerImage() {

			byte[] reds = new byte[256];
			byte[] greens = new byte[256];
			byte[] blues = new byte[256];
			byte[] alpha = new byte[256];

			for (int i = 0; i < 255; i++) {
				reds[i] = (byte) colorMap[i][0];
				greens[i] = (byte) colorMap[i][1];
				blues[i] = (byte) colorMap[i][2];
				alpha[i] = (byte) colorMap[i][3];
			}

			byte[][] lut = new byte[4][256];
			lut[0] = reds;
			lut[1] = greens;
			lut[2] = blues;
			lut[3] = alpha;

			LookupTableJAI table = new LookupTableJAI(lut);

			ParameterBlock pb = new ParameterBlock();
			pb.addSource(markerImage1);
			pb.add(table);

			colorMarkerImage = JAI.create("lookup", pb, null);
		}

		/**
		 * Method which allows the user to remove or re-insert the last marker
		 * drawn
		 */
		public void undo() {

			// re-display the last marker image saved in oldMarkerRi
			WritableRenderedImage tempoRi = copyMarkerImage();
			markerImage1.setData(oldMarkerRi.getData());
			oldMarkerRi = tempoRi;

			createColorMarkerImage();
			repaint();
		}

		/**
		 * 
		 * @return the indice of the current marker color
		 */
		public int getMarker() {
			return (int) (currentColorIndex[0] * 255);
		}

		/**
		 * Set the indice of the current marker color
		 * 
		 * @param marker
		 */
		public void setMarker(int marker) {
			this.currentColorIndex[0] = (float) (marker / 255f);

		}

		/**
		 * Create a list of numbers corresponding to different hues
		 * 
		 * @return list
		 */
		private ArrayList<Integer> createHueList() {

			ArrayList<Integer> colorList = new ArrayList<Integer>();
			int indice = 0;
			colorList.add(128);
			int newElement;

			while (colorList.size() < 16) {

				newElement = colorList.get(indice) / 2;
				if (!((newElement < 1) || (newElement > 256))) {
					colorList.add(newElement);
					newElement = newElement + 128;
					if (!((newElement < 1) || (newElement > 256))) {
						colorList.add(newElement);
					}
				}
				indice++;
			}
			return colorList;
		}

		/**
		 * Fill colorMap with 256 different colors
		 */
		private void initColors() {

			final ArrayList<Integer> colorList = createHueList();
			final double[] saturationList = { 1, 0.75, 0.5, 0.25 };
			final double[] luminanceList = { 1, 0.75, 0.5, 0.25 };

			// Set the first table element to be transparent
			colorMap[0][0] = 127;
			colorMap[0][1] = 127;
			colorMap[0][2] = 127;
			colorMap[0][3] = 0;

			int x = 1;

			// Set all the other colors
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 4; k++) {
					for (int i = 0; i < 16; i++) {

						int[] tmp = HSYToRGB.convert(
								(double) (colorList.get(i) / 256.0),
								saturationList[j], luminanceList[k]);

						colorMap[x][0] = tmp[0];

						colorMap[x][1] = tmp[1];
						colorMap[x][2] = tmp[2];
						colorMap[x][3] = 255;
						x++;
					}
				}

			}

			// Set the color of labelColor
			labelColor
					.setBackground(new Color(colorMap[plusButtonIndex][0],
							colorMap[plusButtonIndex][1],
							colorMap[plusButtonIndex][2]));

		}

		/**
		 * 
		 * @return the current marker color
		 */
		public Color getColorMarker() {

			Color color = new Color((int) colorMap[(int) (getMarker())][0],
					(int) colorMap[(int) (getMarker())][1],
					(int) colorMap[(int) (getMarker())][2]);
			return color;
		}

	}
}

/*******************************************************************************
 * 
 * 
 * ImageCreator class
 * 
 * 
 ******************************************************************************/

/**
 * Creates an empty TiledImage
 */
class ImageCreator {
	private ImageCreator() {

	}

	/**
	 * Creates an empty TiledImage without an alpha channel
	 * 
	 * @param width
	 * @param height
	 * @return the TiledImage object
	 */
	public static TiledImage createGrayImage(int width, int height) {
		byte[][] imageData = new byte[width][height];

		// Fill with zeros
		// For testing, fill with something else
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				imageData[w][h] = 0;
			}
		}

		byte[] imageDataSingleArray = new byte[width * height];
		int count = 0;
		// Convert to single array
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				imageDataSingleArray[count++] = imageData[w][h];
			}
		}

		DataBufferByte dbuffer = new DataBufferByte(imageDataSingleArray, width
				* height);

		SampleModel sampleModel = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_BYTE, width, height, 1);

		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);

		Raster raster = RasterFactory.createWritableRaster(sampleModel,
				dbuffer, new Point(0, 0));

		TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0,
				sampleModel, colorModel);

		tiledImage.setData(raster);

		return tiledImage;
	}
	
	/**
	 * Creates an empty TiledImage without an alpha channel
	 * based on defined marker image
	 * 
	 * @param width
	 * @param height
	 * @return the TiledImage object
	 */
	public static TiledImage createGrayImage(ByteImage markers) {
		byte[][] imageData = new byte[markers.getXDim()][markers.getYDim()];
		
		markers = (ByteImage) AverageChannels.exec(markers);

		// Fill with zeros
		// For testing, fill with something else
		for (int x = 0; x < markers.getXDim(); x++) {
			for (int y = 0; y < markers.getYDim(); y++) {
				imageData[x][y] = (byte)markers.getPixelXYByte(x, y);
			}
		}

		byte[] imageDataSingleArray = new byte[markers.getXDim() * markers.getYDim()];
		int count = 0;
		// Convert to single array
		for (int y = 0; y < markers.getYDim(); y++) {
			for (int x = 0; x < markers.getXDim(); x++) {
				imageDataSingleArray[count++] = imageData[x][y];
			}
		}

		DataBufferByte dbuffer = new DataBufferByte(imageDataSingleArray, markers.getXDim()
				* markers.getYDim());

		SampleModel sampleModel = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_BYTE, markers.getXDim(), markers.getYDim(), 1);

		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);

		Raster raster = RasterFactory.createWritableRaster(sampleModel,
				dbuffer, new Point(0, 0));

		TiledImage tiledImage = new TiledImage(0, 0, markers.getXDim(), markers.getYDim(), 0, 0,
				sampleModel, colorModel);

		tiledImage.setData(raster);

		return tiledImage;
	}

	/**
	 * Creates an empty TiledImage with an alpha channel
	 * 
	 * @param width
	 * @param height
	 * @return the TiledImage object
	 */
	public static TiledImage createGrayImageWithAlpha(int width, int height) {
		byte[][][] imageData = new byte[width][height][2];

		// Fill with zeros
		// For testing, fill with something else
		// Set alpha to transparent
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				for (int channel = 0; channel < 2; channel++) {
					imageData[w][h][channel] = 0;
				}
			}
		}

		byte[] imageDataSingleArray = new byte[width * height * 2];
		int count = 0;
		// Convert to single array
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				for (int channel = 0; channel < 2; channel++) {
					imageDataSingleArray[count++] = imageData[w][h][channel];
				}
			}
		}

		DataBufferByte dbuffer = new DataBufferByte(imageDataSingleArray, width
				* height * 2);

		SampleModel sampleModel = RasterFactory
				.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width,
						height, 2);

		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);

		Raster raster = RasterFactory.createWritableRaster(sampleModel,
				dbuffer, new Point(0, 0));

		TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0,
				sampleModel, colorModel);

		tiledImage.setData(raster);

		return tiledImage;
	}

}
