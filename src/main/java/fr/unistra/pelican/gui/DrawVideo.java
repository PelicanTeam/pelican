package fr.unistra.pelican.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Random;
import javax.media.jai.*;
import javax.swing.*;
import javax.swing.event.*;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.History;

/**
 * The DrawVideo class allows the user to draw markers with a background image.
 */
public class DrawVideo extends JPanel 
{ 
	///////////////
	// CONSTANTS //
	///////////////

	public static final boolean TRANSPARENCY_PRESENT_BY_DEFAULT = false;
	public static final boolean ADD_LABELS_PRESENT_BY_DEFAULT = true;

	////////////
	// FIELDS //
	////////////

	// GUI fields

	private JDialog frame;
	/**	Contains the labels list. */
	private JComboBox labelsBox;
	/** Allows to change the stroke size of the brush. */
	private JSpinner brushSpinner;
	/**	Allows to change the transparency of the marker image. */
	private JSlider transparencySlider;
	/** Contains the slider to switch between frames */
	private JScrollPane scroll;
	/**	Displays video's frames */
	private CustomDisplayJAI display;
	/**	Slider that allows to switch between video's frames*/
	private JSlider frameSld;


	private JPanel colorPanel, npanel, spanel, bpanel;
	private JLabel brushThicknessLabel, transparencyLabel, frameLbl;
	private JButton addLabelsButton, renameButton, undoButton, redoButton, resetCurButton, resetAllButton, okButton;

	// listeners

	private aListener alistener;
	private cListener clistener;
	private kListener klistener;
	private mListener mlistener;


	// images

	/**	The "background" image that must be processed in this. */
	public Image inputImage;
	/**	The user marked image. */
	public IntegerImage output;
	/**	The background image converted in BufferedImage.
	 *	Necessary for displaying the pic in the GUI.*/
	private BufferedImage bimg;
	/**	The markers image.
	 *	Always equal to the marker image that was passed in argument at creation.
	 *	And : yes, that means "always equal to <tt>null</tt>" if nothing was passed.
	 */
	private IntegerImage markersImage = null;

	// booleans

	/**	Whether or not {#transparencyLabel} and {#transparencySlider} will be added to this. */
	private boolean transparencyEnabled;
	/**	Whether or not {#addLabelsButton} will be added to this. */
	private boolean addLabelsEnabled;
	/**	true if resetting	*/
	private boolean reset = false;
	
	//time measurement
	private long humanTime;


	// arrays 

	/**	Use to save the marker image after each drawing in case of an undo. */
	private History<WritableRenderedImage>[] markersHistory;
	/**	Use to contain comboBox labels*/
	public String[] tabLabels = new String[1]; // (eraser)


	//////////////////
	// CONSTRUCTORS //
	//////////////////

	/**	Secondary constructor.
	 *	@param inputImage An {@link fr.unistra.pelican.Image} to set as background.
	 */
	public DrawVideo( Image inputImage ) 
	{ 
		this( inputImage,null); 
	}

	/**	Secondary constructor.
	 *	@param inputImage An {@link fr.unistra.pelican.Image} to set as background.
	 *	@param title The frame's title.
	 */
	public DrawVideo( Image inputImage, String title) 
	{ 
		this( inputImage,title, null); 
	}


	/**	Secondary constructor.
	 *	@param inputImage An {@link fr.unistra.pelican.Image} to set as background.
	 *	@param title The frame's title.
	 *	@param markersImage 
	 */
	public DrawVideo( Image inputImage, String title, IntegerImage markersImage ) 
	{ 
		this(inputImage,title, markersImage, TRANSPARENCY_PRESENT_BY_DEFAULT,ADD_LABELS_PRESENT_BY_DEFAULT);
	}

	/**	Primary constructor.
	 *	@param OrigVideo An {@link fr.unistra.pelican.Image} to set as background.
	 *	@param title The frame's title.
	 *	@param transparencyEnabled 
	 *	@param addLabelsEnabled 
	 */
	public DrawVideo( Image _inputImage, String title, IntegerImage _markersImage, boolean transparencyEnabled, boolean addLabelsEnabled ) 
	{ 
		this.inputImage = _inputImage;
		if ( this.inputImage.getBDim() != 3) 
		{
			this.inputImage = fr.unistra.pelican.algorithms.conversion.GrayToRGB.exec( 
					fr.unistra.pelican.algorithms.conversion.AverageChannels.exec( this.inputImage ) );
		}	
		this.markersImage = _markersImage;
		this.transparencyEnabled = transparencyEnabled;
		this.addLabelsEnabled = addLabelsEnabled;
		this.bimg = fr.unistra.pelican.util.Tools.pelican2BufferedT( this.inputImage, 0);
		this.output = new IntegerImage(inputImage.getXDim(),inputImage.getYDim(),1,inputImage.getTDim(),1);
		this.output.fill(0);
		this.frame = new JDialog();
		if(title != null)
			this.frame.setTitle( title );
		this.frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		this.frame.setLocationRelativeTo(null);
		this.frame.setModal(true);
		System.out.println("GUI init...");
		this.guiInitialization();
		humanTime = System.currentTimeMillis();
		this.frame.setVisible(true);

		
	}

	/////////////
	// METHODS //
	/////////////

	/**	Initialization of the Video2D GUI. */
	private void guiInitialization() 
	{
		this.setLayout( new BorderLayout() );

		npanel = new JPanel(new GridBagLayout());	// the "north" panel
		spanel = new JPanel(new BorderLayout());	// the "south" panel
		bpanel = new JPanel(new GridBagLayout());	// the "button" panel into "south" panel

		alistener = new aListener();
		clistener = new cListener();
		klistener = new kListener();
		mlistener = new mListener();

		// one: create the components which will be put on the "north" panel
		if(markersImage != null)
		{
			this.output = this.markersImage;
			this.tabLabels = new String[((String[])this.markersImage.getProperty("Labels")).length];

			for(int i = 0; i< this.tabLabels.length; i++)
			{
				this.tabLabels = ((String[])markersImage.getProperty("Labels"));
			}
		}
		else
		{
			this.tabLabels[0] = "Eraser";
		}

		this.labelsBox = new JComboBox(tabLabels);
		this.labelsBox.setBackground(SystemColor.control);
		this.labelsBox.setOpaque(false);
		this.labelsBox.setSelectedIndex(0);
		this.labelsBox.addActionListener( alistener );

		int[][] colorMap = lutInitialization();

		this.colorPanel = new JPanel();
		this.colorPanel.setBorder( BorderFactory.createLineBorder( Color.black ) );
		this.colorPanel.setMinimumSize( new Dimension( 20,20 ) );
		this.colorPanel.setPreferredSize( new Dimension( 20,20 ) );

		this.colorPanel.setBackground( new Color( 
				colorMap[ this.labelsBox.getSelectedIndex() ][0],
				colorMap[ this.labelsBox.getSelectedIndex() ][1],
				colorMap[ this.labelsBox.getSelectedIndex() ][2]));

		this.addLabelsButton = new JButton( "+" );
		this.addLabelsButton.addActionListener( alistener );
		this.renameButton = new JButton("Rename label");
		this.renameButton.addActionListener( alistener );
		this.brushThicknessLabel = new JLabel( "Thickness: " );
		this.brushThicknessLabel.setBackground( SystemColor.control );
		this.brushSpinner = new JSpinner();

		this.brushSpinner.setModel( new SpinnerNumberModel( 
				CustomDisplayJAI.DEFAULT_BRUSH_SIZE, 
				CustomDisplayJAI.MIN_BRUSH_SIZE, 
				CustomDisplayJAI.MAX_BRUSH_SIZE, 
				1) );

		this.brushSpinner.setMaximumSize( new Dimension( 32767,32767 ) );
		this.brushSpinner.setMinimumSize(   new Dimension( 40,18 ) );
		this.brushSpinner.setPreferredSize( new Dimension( 40,18 ) );
		this.brushSpinner.addChangeListener( clistener );

		// this will be a waste if this.transparencyEnabled = false
		this.transparencyLabel = new JLabel( "Transparency:" );
		this.transparencySlider = new JSlider();
		this.transparencySlider.setExtent( 0 );
		this.transparencySlider.setMaximum( 255 );
		this.transparencySlider.setPaintLabels( false );
		this.transparencySlider.setPaintTicks( false );
		this.transparencySlider.setPaintTrack( true );
		this.transparencySlider.setBackground(SystemColor.control);
		this.transparencySlider.setMaximumSize( new Dimension( 32767,24 ) );
		this.transparencySlider.setValue( 255 );
		this.transparencySlider.addChangeListener( clistener );


		// two: create the components which will be put on the "south" panel

		this.undoButton = new JButton( "Undo" );
		this.undoButton.addActionListener( alistener );
		this.undoButton.setEnabled( false );

		this.redoButton = new JButton( "Redo" );
		this.redoButton.addActionListener( alistener );
		this.redoButton.setEnabled( false );

		this.resetCurButton = new JButton( "Reset current" );
		this.resetCurButton.addActionListener( alistener );
		this.resetCurButton.setEnabled( false );

		//resetAllButton is always enabled
		this.resetAllButton = new JButton ( "Reset all");
		this.resetAllButton.addActionListener( alistener );
		this.resetAllButton.setEnabled( true );

		//okButton is always enabled
		this.okButton = new JButton( "Done" );
		this.okButton.addActionListener( alistener );
		this.okButton.setEnabled( true );				


		// three: create the component which contains this.display 
		// and will be placed on the central panel

		this.display = new CustomDisplayJAI();
		this.display.colorMap = colorMap;
		this.display.set( this.bimg,this.markersImage );
		MouseListener[] ml = this.display.getMouseListeners();
		this.display.removeMouseListener( ml[0] );
		this.display.addMouseListener( mlistener );

		markerHistoryInit();

		// Component which contains the MarkerDisplayJAI instance (display)
		this.scroll = new JScrollPane( this.display );
		scroll.setViewportBorder( BorderFactory.createLineBorder( Color.black ) );
		MouseWheelListener[] listeners = scroll.getMouseWheelListeners();
		MouseWheelListener defaultMouseWheelListener = listeners[0];
		scroll.removeMouseWheelListener( defaultMouseWheelListener );
		this.display.defaultMouseWheelListener = defaultMouseWheelListener;

		// four: put all that GUI stuff in the panels

		GridBagConstraints nconstraints = new GridBagConstraints();
		nconstraints.fill = GridBagConstraints.HORIZONTAL;
		nconstraints.weightx = 1.0;
		nconstraints.gridx = 0;
		nconstraints.gridy = 0;
		nconstraints.gridwidth = 1;
		npanel.add( this.labelsBox, nconstraints );
		nconstraints.weightx = .25;
		nconstraints.gridx ++;
		npanel.add( this.colorPanel, nconstraints );

		if ( this.addLabelsEnabled ) 
		{ 
			nconstraints.gridx ++;
			npanel.add( this.addLabelsButton, nconstraints );
		}

		nconstraints.weightx = 1.0;
		nconstraints.gridx ++;
		npanel.add( this.renameButton, nconstraints );
		nconstraints.gridx ++;
		npanel.add( Box.createRigidArea( new Dimension( 10,0 ) ) );
		nconstraints.gridx ++;
		npanel.add( brushThicknessLabel, nconstraints );
		nconstraints.weightx = .25;
		nconstraints.gridx ++;
		npanel.add( this.brushSpinner, nconstraints );
		nconstraints.weightx = 1.0;
		nconstraints.gridx ++;

		if ( this.transparencyEnabled ) 
		{ 
			npanel.add( Box.createRigidArea( new Dimension( 10,0 ) ) );
			nconstraints.gridx++;
			npanel.add( this.transparencyLabel, nconstraints );
			nconstraints.weightx = 2.0;
			nconstraints.gridx++;
			npanel.add( this.transparencySlider, nconstraints );
		}

		GridBagConstraints sconstraints = new GridBagConstraints();
		sconstraints.fill = GridBagConstraints.HORIZONTAL;
		sconstraints.weightx = 1.0;
		sconstraints.gridx = 0;
		sconstraints.gridy = 0;
		sconstraints.gridwidth = 1;
		bpanel.add( this.undoButton, sconstraints );
		sconstraints.gridx ++;
		bpanel.add( this.redoButton, sconstraints );
		sconstraints.gridx ++;
		bpanel.add( this.resetCurButton, sconstraints );
		sconstraints.gridx ++;
		bpanel.add( this.resetAllButton, sconstraints );
		sconstraints.gridx ++;
		bpanel.add( Box.createRigidArea( new Dimension( 10,0 ) ) );
		sconstraints.gridx ++;
		bpanel.add( this.okButton, sconstraints );
		sconstraints.gridx ++;

		GridBagConstraints iconstraints = new GridBagConstraints();
		iconstraints.fill = GridBagConstraints.BOTH;
		iconstraints.gridx = 0;
		iconstraints.gridy = 0;
		iconstraints.gridwidth = 1;
		iconstraints.gridheight = 1;
		iconstraints.gridx ++;

		//frames slider

		JPanel frameBox = new JPanel();
		frameBox.setLayout(new BoxLayout(frameBox, BoxLayout.X_AXIS));

		frameLbl = new JLabel(" Frame : 1/" + inputImage.getTDim() + "     ");
		frameBox.add(frameLbl);
		frameSld = new JSlider(SwingConstants.HORIZONTAL, 1, inputImage.getTDim(), 1);
		frameSld.setSnapToTicks(true);
		frameBox.add(frameSld);

		frameSld.addChangeListener(new ChangeListener() 
		{ 
			/**
			 *	Listener qui change de frame selon le slider
			 **/
			public void stateChanged(ChangeEvent e) 
			{
				int t = frameSld.getValue() - 1;
				frameLbl.setText(" Frame : " + Integer.toString(frameSld.getValue()) + "/" + inputImage.getTDim() + "     ");
				bimg = fr.unistra.pelican.util.Tools.pelican2BufferedT( inputImage, t );
				int vValue = scroll.getVerticalScrollBar().getValue();
				int hValue = scroll.getHorizontalScrollBar().getValue();
				display.set(bimg, (IntegerImage)output.getImage4D(t, Image.T));
				checkButtons();
				scroll.getVerticalScrollBar().setValue(vValue);
				scroll.getHorizontalScrollBar().setValue(hValue);
			}		
		});

		if (inputImage.getTDim() == 1) 
		{
			frameLbl.setEnabled(false);
			frameSld.setEnabled(false);
		}

		JPanel subPnl = new JPanel(); 
		subPnl.setLayout( new BorderLayout() );

		if (frameLbl.isEnabled())
		{
			subPnl.add(frameBox, BorderLayout.CENTER);
		}

		spanel.add(bpanel, BorderLayout.SOUTH);
		spanel.add(subPnl, BorderLayout.NORTH);
		this.add( npanel, BorderLayout.NORTH);
		this.add( this.scroll, BorderLayout.CENTER );
		this.add( spanel, BorderLayout.SOUTH);


		// five : finalize. you know that.

		this.display.getMouseListeners();
		this.display.getMouseMotionListeners();
		MouseWheelListener[] mwlis = this.display.getMouseWheelListeners();
		this.display.removeMouseWheelListener( mwlis[0] );

		Component[] allthegui = {	this, frame, npanel, spanel, display, scroll, 
				undoButton, redoButton, resetCurButton, resetAllButton, okButton, labelsBox, 
				colorPanel, addLabelsButton, renameButton, brushThicknessLabel, brushSpinner, 
				transparencyLabel, transparencySlider };

		KeyListener[] klis;

		for ( int c = 0 ; c < allthegui.length ; c++ ) 
		{ 
			klis = allthegui[c].getKeyListeners();
			System.out.println(klis.length+" "+allthegui[c].getClass().getName());
			allthegui[c].addKeyListener( klistener );
			allthegui[c].addMouseWheelListener( mwlis[0] );
		}

		//zoom +50%
		for(int i =0; i< 6; i++)
		{
			display.zoom(true);
			display.set( bimg, output);
		}

		this.setOpaque( true );
		this.frame.setContentPane( this );
		this.frame.pack();
	}

	/**	Fill colorMap with 256 different colors. */
	private int[][] lutInitialization() 
	{
		int[][] colorMap = new int[257][4];
		Random random = new Random();
		byte[] color = new byte[3];

		// Set the first table element to be transparent
		colorMap[0][0] = 127;
		colorMap[0][1] = 127;
		colorMap[0][2] = 127;
		colorMap[0][3] = 0;

		int x = 1;

		// Set all the other colors (opaque)
		for ( int j = 0 ; j < 4; j++ ) 
		{ 
			for ( int k = 0 ; k < 4; k++ ) 
			{ 
				for ( int i = 0;  i < 16; i++ ) 
				{ 
					random.setSeed(x * 131);
					random.nextBytes(color);

					colorMap[x][0] = color[0]+128;
					colorMap[x][1] = color[1]+128;
					colorMap[x][2] = color[2]+128;
					colorMap[x][3] = 255;		

					x++;
				}
			}
		}
		return colorMap;
	}

	/**	Get an estimation (upper bound) of the number of labels. 
	 * @return The number of item currently in {@link #labelBox}.
	 */

	public int labels()
	{
		return labelsBox.getItemCount();
	}

	/**	Initialise l'historique des marqueurs en sauvegardant 
	 * 	la premiere image de toutes les frames de la videos. */
	@SuppressWarnings("unchecked")
	public void markerHistoryInit()
	{
		markersHistory = new History[inputImage.getTDim()];

		for(int i = 0; i< inputImage.getTDim(); i++)
		{
			markersHistory[i] = new History<WritableRenderedImage>();
			// save first image for undo
			int widthTI  = display.raster.getWidth();
			int heightTI = display.raster.getHeight();

			WritableRenderedImage copy = fr.unistra.pelican.util.Tools.createGrayImage( (IntegerImage)null, widthTI,heightTI );

			copy.setData( display.raster.copyData() );
			markersHistory[i].add(copy );
		}					
	}

	/**	Enable/disable buttons.	*/
	public void checkButtons()
	{
		int t = frameSld.getValue() - 1;

		if ( markersHistory[t].canForward() )
		{
			redoButton.setEnabled( true );
		}
		else 
		{
			redoButton.setEnabled( false );
		}

		if ( markersHistory[t].canRewind() ) 
		{ 
			undoButton.setEnabled( true );
			resetCurButton.setEnabled( true );
		}
		else
		{
			resetCurButton.setEnabled( false );
			undoButton.setEnabled( false );
		}
	}

	/**	Sauvegarde les marqueurs de la frame visionnée.	*/
	public void saveMarker()
	{
		//redimensionner l'image avant la sauvegarde

		BufferedImage bi = ((PlanarImage) display.raster).getAsBufferedImage();
		Raster raster = bi.getData();
		int type = bi.getType();
		int height = raster.getHeight();
		int width = raster.getWidth();

		// Transfers each byte from raster to output
		for ( int x = 0 ; x < width ; x++ )
		{
			for ( int y = 0 ; y < height ; y++ )
			{
				output.setPixelXYTBInt( x,y,frameSld.getValue()-1,0, (byte) raster.getSample( x,y,0 ) );
			}
		}
		output.setColor( false );
		output.setProperty("humanTime", humanTime);
		output.type = type;
		output.properties.put("Labels", tabLabels);
	}


	//////////////////////
	// EVENT MANAGEMENT //
	// INTERNAL CLASSES //
	//////////////////////

	private class aListener implements ActionListener 
	{
		public void actionPerformed( ActionEvent e ) 
		{ 
			Object source = e.getSource();
			if ( source == labelsBox ) 
			{ 
				if ( !reset ) 
				{
					//the +1 is a bug fixed but probably not really solve the bug just a temporary patch
					display.color[0] = (float) ( (labelsBox.getSelectedIndex()) / 255f );
					// set the current color to labelColor
					Color color = new Color( 
							(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 0 ],
							(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 1 ],
							(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 2 ] );
					colorPanel.setBackground(color);
				}
			} 
			else if ( source == addLabelsButton ) 
			{ 
				display.color[0] = (float) ( labelsBox.getSelectedIndex() / 256f );
				// set the current color to labelColor
				Color color = new Color( 
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 0 ],
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 1 ],
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 2 ] );
				colorPanel.setBackground( color );

				String newLabelName = JOptionPane.showInputDialog(null, 
						"What will be the name for this new label ?", "Add new label",
						JOptionPane.QUESTION_MESSAGE);

				if(newLabelName != null)
				{
					while(newLabelName.startsWith(" ") && newLabelName.length() > 1)
					{
						newLabelName = newLabelName.substring(1);
					}

					if(newLabelName.equals(" ") || newLabelName.length() <= 1 )
					{
						newLabelName = "Label " + tabLabels.length;
					}

					String[] temp = tabLabels;
					tabLabels = new String[tabLabels.length + 1];
					tabLabels[labelsBox.getItemCount()] = newLabelName;

					for(int i =0; i< temp.length; i++)
					{
						tabLabels[i] = temp[i];
					}

					labelsBox.removeActionListener(alistener);
					labelsBox.removeAllItems();

					for(int i = 0; i< tabLabels.length; i++)
					{
						labelsBox.addItem(tabLabels[i]);
					}

					labelsBox.addActionListener( alistener );
					labelsBox.setSelectedItem( newLabelName );
				}
			} 
			else if ( source == renameButton )
			{
				int saveItemIndex = labelsBox.getSelectedIndex();

				String label = JOptionPane.showInputDialog(null, 
						"New label's name ? \n ", "Rename label", 
						JOptionPane.QUESTION_MESSAGE);

				if(label != null)
				{
					while(label.startsWith(" ") && label.length() > 1)
					{
						label = label.substring(1);
					}

					if(label.length() > 1 )
					{
						tabLabels[labelsBox.getSelectedIndex()] = label;
						labelsBox.removeActionListener(alistener);
						labelsBox.removeAllItems();

						for(int i = 0; i< tabLabels.length; i++)
						{
							labelsBox.addItem(tabLabels[i]);
						}

						labelsBox.addActionListener( alistener );
						labelsBox.setSelectedIndex(saveItemIndex);
					}
				}
			}
			else if ( source == undoButton ) 
			{ 
				// re-display the last marker image saved in history
				WritableRenderedImage copy = markersHistory[frameSld.getValue()-1].rewind();
				display.raster.setData( copy.getData() );
				display.createColorMarkerImage();
				display.repaint();
				saveMarker();
				checkButtons();
			} 
			else if ( source == redoButton ) 
			{ 
				WritableRenderedImage copy = markersHistory[frameSld.getValue()-1].forward();
				display.raster.setData( copy.getData() );
				if ( !markersHistory[frameSld.getValue()-1].canForward() ) redoButton.setEnabled( false );
				display.createColorMarkerImage();
				display.repaint();
				saveMarker();
				checkButtons();
			} 
			else if ( source == resetCurButton ) 
			{ 
				reset = true;
				WritableRenderedImage copy = markersHistory[frameSld.getValue()-1].genesis();
				display.raster.setData( copy.getData() );
				display.createColorMarkerImage();
				display.repaint();
				display.color[0] = (float) 1 / 256;
				new Color( 
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 0 ],
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 1 ],
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 2 ] );
				reset = false;
				saveMarker();
				checkButtons();
			} 
			else if ( source == resetAllButton ) 
			{ 
				int option = JOptionPane.showConfirmDialog(null, 
						"Are you sure you want to reset the entire video ?"
						+"\nThis will clear all the markers you have drawn.", 
						"Reset every markers ?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if(option == JOptionPane.OK_OPTION)
				{
					bimg = fr.unistra.pelican.util.Tools.pelican2BufferedT( inputImage, 0);
					frameSld.setValue(1);
					display.set( bimg);
					output = new IntegerImage(inputImage.getXDim(),inputImage.getYDim(),1,inputImage.getTDim(),1);
					output.fill(0);
					markerHistoryInit();
					checkButtons();
				}
			}
			else if ( source == okButton ) 
			{ 
				humanTime= System.currentTimeMillis()-humanTime;
				saveMarker();
				frame.dispose();
			} 
		}
	}

	private class cListener implements ChangeListener 
	{ 
		public void stateChanged( ChangeEvent e ) 
		{ 
			Object source = e.getSource();
			if ( source == brushSpinner ) 
			{ 
				if ( brushSpinner.getValue() instanceof Number ) 
				{ 
					int brushSize = (Integer) brushSpinner.getValue();
					display.stroke = new BasicStroke( brushSize );
				}
			} 
			else if ( source == transparencySlider ) 
			{ 
				display.rasterTransparency = transparencySlider.getValue();
				// Set the colorMap transparency for all but the first element
				for ( int i = 1 ; i < 256 ; i++ ) 
					display.colorMap[i][3] = (byte) display.rasterTransparency;

				// refresh
				display.createColorMarkerImage();
				repaint();
			}	
		}
	}

	private class kListener extends KeyAdapter 
	{ 
		public void keyPressed( KeyEvent e ) 
		{ 
			if ( e.getKeyCode() == KeyEvent.VK_SHIFT ) 
			{ 
				display.horizontalScrollEnabled = !display.horizontalScrollEnabled;
				display.revalidate();
			} 
			else if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) 
			{
				display.setNavigateMode( CustomDisplayJAI.ZOOM_MODE );
			}
			else if ( e.getKeyCode() == KeyEvent.VK_ALT ) 
			{
				display.drawEnabled( CustomDisplayJAI.DRAW_OFF );
			}
			else if ( e.getKeyCode() == KeyEvent.VK_LEFT ) 
			{
				if(frameSld.getValue() > 1)
				{
					frameSld.setValue(frameSld.getValue()-1);
				}
			}
			else if ( e.getKeyCode() == KeyEvent.VK_RIGHT )
			{
				if(frameSld.getValue() < inputImage.getTDim())
				{
					frameSld.setValue(frameSld.getValue()+1);
				}
			}
			else if ( e.getKeyCode() == KeyEvent.VK_UP )
			{
				if(e.isAltDown())
				{
					if (labelsBox.getSelectedIndex() < (labelsBox.getItemCount()-1) )
					{
						labelsBox.setSelectedIndex(labelsBox.getSelectedIndex()+1);
					}
				}
			}
			else if ( e.getKeyCode() == KeyEvent.VK_DOWN )
			{
				if(e.isAltDown())
				{
					if (labelsBox.getSelectedIndex() > 0 )
					{
						labelsBox.setSelectedIndex(labelsBox.getSelectedIndex()-1);
					}
				}
			}

			else if( e.getKeyCode() == 107 ) // touche "+" du pavé numérique
			{
				addLabelsButton.doClick();

			}
			else System.out.println("glop ");
		}

		public void keyReleased( KeyEvent e ) 
		{ 
			if ( e.getKeyCode() == KeyEvent.VK_SHIFT ) 
			{ 
				display.horizontalScrollEnabled = !display.horizontalScrollEnabled;
				display.revalidate();
			} 
			else if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) 
			{
				display.setNavigateMode( CustomDisplayJAI.DEFAULT_MODE );
			}
			else if ( e.getKeyCode() == KeyEvent.VK_ALT ) 
			{
				display.drawEnabled( CustomDisplayJAI.DRAW_ON );
			}
		}
	}

	private class mListener implements MouseListener 
	{ 
		public void mouseClicked( MouseEvent e ) { display.mouseClicked(e); }
		public void mouseEntered( MouseEvent e ) { display.mouseEntered(e); }
		public void mouseExited( MouseEvent e )  { display.mouseExited(e);  }
		public void mousePressed( MouseEvent e ) { display.mousePressed(e); }

		/** Invoked when the mouse button has been released on a component. 
		 *	@param e The event to process.
		 */
		public void mouseReleased( MouseEvent e ) 
		{ 
			// save current image for undo
			int widthTI  = display.raster.getWidth();
			int heightTI = display.raster.getHeight();
			WritableRenderedImage copy = fr.unistra.pelican.util.Tools.createGrayImage((IntegerImage) null, widthTI,heightTI );
			copy.setData( display.raster.copyData() );
			markersHistory[frameSld.getValue()-1].add( copy );
			checkButtons();
			saveMarker();
		}
	}


	////////////////////
	// ANNOYING STUFF //
	////////////////////

	public static final long serialVersionUID = 1L;
	/*	This gets rid of the annoying exception for not using native acceleration :
	 *	"Could not find mediaLib accelerator wrapper classes. Continuing in pure Java mode."
	 */	static { System.setProperty( "com.sun.media.jai.disableMediaLib", "true" ); }
}