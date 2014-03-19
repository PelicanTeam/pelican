package fr.unistra.pelican.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

import javax.media.jai.*;
import javax.swing.*;
import javax.swing.event.*;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.*;
import fr.unistra.pelican.util.History;

/**
 * The Draw2DThread class allows the user to draw markers with or without a background
 * image.
 * 
 * @author Florent Sollier, Jonathan Weber, Regis Witz
 * 
 */
public class Draw2DThread extends JPanel implements Runnable { 



	  ///////////////
	 // CONSTANTS //
	///////////////


	private static final long serialVersionUID = 1L;
	public static final boolean TRANSPARENCY_PRESENT_BY_DEFAULT = false;
	public static final boolean ADD_LABELS_PRESENT_BY_DEFAULT = true;

	public static final double ZOOM_IN_FACTOR = 1.1;
	public static final double ZOOM_OUT_FACTOR = 0.9;



	  ////////////
	 // FIELDS //
	////////////

	// GUI fields

	private JDialog frame;

	/**	Contains the labels list. */
	private JComboBox labelsBox;
	/**	Contains the current label color. */
	private JPanel colorPanel;
	/** Add Labels to {#labelsBox}. */
	private JButton addLabelsButton;
	/** Allows to change the stroke size of the brush. */
	private JSpinner brushSpinner;
	/**	Just the label that goes with {#transparencySlider}. */
	private JLabel transparencyLabel;
	/**	Allows to change the transparency of the marker image. */
	private JSlider transparencySlider;
	/**	Wheter or not {#transparencyLabel} and {#transparencySlider} will be added to this. */
	private boolean transparencyEnabled;
	/**	Wheter or not {#addLabelsButton} will be added to this. */
	private boolean addLabelsEnabled;

	private JScrollPane scroll;
	private CustomDisplayJAI display;

	/** Undoes the last label painting. */
	private JButton undoButton;
	/** Undoes the last label painting. */
	private JButton redoButton;
	/** Begins a new marker image. */
	private JButton resetButton;
	/**	Confirms marker drawing's end. */
	private JButton okButton;



	// other fields

	/**	The "background" image that must be processed in this. */
	public Image inputImage;
	/**	The user marked image. */
	public ByteImage output;

	/**	The background image converted in BufferedImage.
	 *	Necessary for displaying the pic in the GUI.
	 */
	private BufferedImage bimg;
	
	/**	The markers image.
	 *	Always equal to the marker image that was passed in argument at creation.
	 *	And : yes, that means "always equal to <tt>null</tt>" if nothing was passed.
	 */
	public ByteImage markersImage;

	/** In general, this is just equal to {@link #labelsBox} size. 
	 *	But we need this field to numerotate the next label when it is created. */
	private int nbLabels;

	/**
	 * true if resetting
	 */
	private boolean reset = false;

	/**	Use to save the marker image after each drawing in case of an undo. */
	private History<WritableRenderedImage> markersHistory;

	public Image results;
	public Image rescold;
	public BooleanImage frontiers;

	/**	The reference to the calling Thread. */
	private Object invoker;
	public boolean isActive;



	  //////////////////
	 // CONSTRUCTORS //
	//////////////////

	/**	Secondary constructor.
	 *	@param inputImage An {@link fr.unistra.pelican.Image} to set as background.
	 *	@param title The frame's title.
	 *	@param invoker 
	 */
	public Draw2DThread( Image inputImage, 
						String title, 
						Object invoker ) { 

		this( inputImage,title,invoker, null ); 
	}
	
	/**	Secondary constructor.
	 *	@param inputImage An {@link fr.unistra.pelican.Image} to set as background.
	 *	@param title The frame's title.
	 *	@param invoker 
	 *	@param transparencyEnabled 
	 *	@param addLabelsEnabled 
	 */
	public Draw2DThread(	Image inputImage, 
						String title, 
						Object invoker, 
						boolean transparencyEnabled, 
						boolean addLabelsEnabled ) { 

		this( inputImage,title,invoker, null, transparencyEnabled, addLabelsEnabled );
	}
	
	/**	Secondary constructor.
	 *	@param inputImage An {@link fr.unistra.pelican.Image} to set as background.
	 *	@param title The frame's title.
	 *	@param invoker 
	 *	@param markersImage 
	 */
	public Draw2DThread( Image inputImage, 
						String title, 
						Object invoker, 
						ByteImage markersImage ) { 

		this(	inputImage,title,invoker, markersImage, 
				TRANSPARENCY_PRESENT_BY_DEFAULT,
				ADD_LABELS_PRESENT_BY_DEFAULT
			);
	}
	
	/**	Primary constructor.
	 *	@param inputImage An {@link fr.unistra.pelican.Image} to set as background.
	 *	@param title The frame's title.
	 *	@param invoker 
	 *	@param markersImage 
	 *	@param transparencyEnabled 
	 *	@param addLabelsEnabled 
	 */
	public Draw2DThread(	Image inputImage, 
						String title, 
						Object invoker, 
						ByteImage markersImage, 
						boolean transparencyEnabled, 
						boolean addLabelsEnabled ) { 

		this.inputImage = inputImage;
		this.invoker = invoker;
		this.markersImage = markersImage;
		this.transparencyEnabled = transparencyEnabled;
		this.addLabelsEnabled = addLabelsEnabled;
		this.bimg = fr.unistra.pelican.util.Tools.pelican2Buffered( this.inputImage );
		this.nbLabels = 2;

		this.frame = new JDialog();
		this.frame.setTitle( title );
		this.frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		this.frame.addWindowListener( new wListener() );

		this.isActive = true;
	}

	public void run()
	{
		this.guiInitialization();
		this.frame.setVisible(true);
	}

	


	  /////////////
	 // METHODS //
	/////////////
	
	/**	Initialisation of the Draw2D GUI. */
	private void guiInitialization() {

		this.setLayout( new BorderLayout() );
		// the "north" panel
		JPanel npanel = new JPanel();
		npanel.setLayout( new GridBagLayout() );
		// the "images" panel
		JPanel ipanel = new JPanel();
		ipanel.setLayout( new GridBagLayout() );
		// the "south" panel
		JPanel spanel = new JPanel();
		spanel.setLayout( new GridBagLayout() );

		// these three will listen to the events thrown by the components 
		// that will be created later in this method
		// ( definitions for the internal classes [a|c|m]Listener 
		//    can be found at the end of this file )
		aListener alistener = new aListener();
		cListener clistener = new cListener();
		kListener klistener = new kListener();
		mListener mlistener = new mListener();


		// one: create the components which will be put on the "north" panel

		this.labelsBox = new JComboBox();
		this.labelsBox.setBackground(SystemColor.control);
		this.labelsBox.setOpaque(false);
		this.labelsBox.addItem("Eraser  ");
		this.labelsBox.addItem("Label 1 ");
		this.labelsBox.addItem("Label 2 ");
		this.labelsBox.setSelectedIndex(1);
		this.labelsBox.addActionListener( alistener );

		int[][] colorMap = lutInitialization();
		this.colorPanel = new JPanel();
		this.colorPanel.setBorder( BorderFactory.createLineBorder( Color.black ) );
		this.colorPanel.setMinimumSize( new Dimension( 20,20 ) );
		this.colorPanel.setPreferredSize( new Dimension( 20,20 ) );
		this.colorPanel.setBackground( new Color( 
				colorMap[ this.labelsBox.getSelectedIndex() ][0],
				colorMap[ this.labelsBox.getSelectedIndex() ][1],
				colorMap[ this.labelsBox.getSelectedIndex() ][2]	) );

		this.addLabelsButton = new JButton( "+" );
		this.addLabelsButton.addActionListener( alistener );

		JLabel brushThicknessLabel = new JLabel( "Thickness: " );
		brushThicknessLabel.setBackground( SystemColor.control );

		this.brushSpinner = new JSpinner();
		this.brushSpinner.setModel( new SpinnerNumberModel( 
				CustomDisplayJAI.DEFAULT_BRUSH_SIZE, 
				CustomDisplayJAI.MIN_BRUSH_SIZE, 
				CustomDisplayJAI.MAX_BRUSH_SIZE, 
				1					) );
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

		this.resetButton = new JButton( "Reset" );
		this.resetButton.addActionListener( alistener );
		this.resetButton.setEnabled( false );

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

		this.markersHistory = new History<WritableRenderedImage>();
		// save first image for undo
		int widthTI  = this.display.raster.getWidth();
		int heightTI = this.display.raster.getHeight();
		WritableRenderedImage copy = 
			fr.unistra.pelican.util.Tools.createGrayImage( (ByteImage)null, widthTI,heightTI );
		copy.setData( this.display.raster.copyData() );
		this.markersHistory.add( copy );

		// Component which contains the MarkerDisplayJAI instance (display)
		this.scroll = new JScrollPane( this.display );

//		scroll.setPreferredSize( new Dimension( 300,250 ) );
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
		if ( this.addLabelsEnabled ) { 
			nconstraints.gridx ++;
			npanel.add( this.addLabelsButton, nconstraints );
		}
		nconstraints.weightx = 1.0;
		nconstraints.gridx ++;
		npanel.add( Box.createRigidArea( new Dimension( 10,0 ) ) );
		nconstraints.gridx ++;
		npanel.add( brushThicknessLabel, nconstraints );
		nconstraints.weightx = .25;
		nconstraints.gridx ++;
		npanel.add( this.brushSpinner, nconstraints );
		nconstraints.weightx = 1.0;
		nconstraints.gridx ++;

		if ( this.transparencyEnabled ) { 

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
		spanel.add( this.undoButton, sconstraints );
		sconstraints.gridx ++;
		spanel.add( this.redoButton, sconstraints );
		sconstraints.gridx ++;
		spanel.add( this.resetButton, sconstraints );
		sconstraints.gridx ++;
		spanel.add( Box.createRigidArea( new Dimension( 10,0 ) ) );
		sconstraints.gridx ++;
		spanel.add( this.okButton, sconstraints );
		sconstraints.gridx ++;

		GridBagConstraints iconstraints = new GridBagConstraints();
		iconstraints.fill = GridBagConstraints.BOTH;
		iconstraints.gridx = 0;
		iconstraints.gridy = 0;
		iconstraints.gridwidth = 1;
		iconstraints.gridheight = 1;
//		ipanel.add( this.scroll, iconstraints );
		iconstraints.gridx ++;

		this.add( npanel, BorderLayout.NORTH);
		this.add( this.scroll, BorderLayout.CENTER );
		this.add( spanel, BorderLayout.SOUTH);



		// five : don't forget to manage the markers image possibly passed at creation
		
		if( this.markersImage != null ) { 

			for( int i = 0 ; i < this.markersImage.maximumByte()-1 ; i++ ) { 

				this.nbLabels ++;

				// add a label to the combobox
				this.labelsBox.addItem( "Label " + this.nbLabels );
				this.labelsBox.setSelectedItem( "Label " + this.nbLabels );

				// set the current color
				Color color = new Color( 
						(int) this.display.colorMap[ labelsBox.getSelectedIndex() ][ 0 ],
						(int) this.display.colorMap[ labelsBox.getSelectedIndex() ][ 1 ],
						(int) this.display.colorMap[ labelsBox.getSelectedIndex() ][ 2 ] );
				this.colorPanel.setBackground( color );
			}
			okButton.setEnabled(true);
			resetButton.setEnabled(true);
		}



		// six: finalize. you know that.


		MouseListener[] mlis = this.display.getMouseListeners();
		MouseMotionListener[] mmlis = this.display.getMouseMotionListeners();
		MouseWheelListener[] mwlis = this.display.getMouseWheelListeners();
//		this.display.removeMouseListener( mlis[0] );
//		this.display.removeMouseMotionListener( mmlis[0] );
		this.display.removeMouseWheelListener( mwlis[0] );
		Component[] allthegui = {	this, frame, npanel, ipanel, spanel, display, scroll, 
									undoButton, redoButton, resetButton, okButton, labelsBox, 
									colorPanel, addLabelsButton, brushThicknessLabel, brushSpinner, 
									transparencyLabel, transparencySlider 
								};
		KeyListener[] klis;
		for ( int c = 0 ; c < allthegui.length ; c++ ) { 

			klis = allthegui[c].getKeyListeners();
			System.out.println(klis.length+" "+allthegui[c].getClass().getName());
			allthegui[c].addKeyListener( klistener );
//			allthegui[c].addMouseListener( mlis[0] );
//			allthegui[c].addMouseMotionListener( mmlis[0] );
			allthegui[c].addMouseWheelListener( mwlis[0] );
		}

		this.setOpaque( true );
		this.frame.setContentPane( this );
		this.frame.pack();

	}

	/**	Fill colorMap with 256 different colors. */
	private int[][] lutInitialization() {

		int[][] colorMap = new int[257][4];
		// a list of numbers corresponding to different hues
		final ArrayList<Integer> colorList = new ArrayList<Integer>();
		int indice = 0;
		colorList.add(128);
		int newElement;
		while ( colorList.size() < 16 ) {

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

		final double[] saturationList = { 1, 0.75, 0.5, 0.25 };
		final double[] luminanceList = { 1, 0.75, 0.5, 0.25 };

		// Set the first table element to be transparent
		colorMap[0][0] = 127;
		colorMap[0][1] = 127;
		colorMap[0][2] = 127;
		colorMap[0][3] = 0;

		int x = 1;

		// Set all the other colors (opaque)
		for ( int j = 0 ; j < 4; j++ ) { 
			for ( int k = 0 ; k < 4; k++ ) { 
				for ( int i = 0;  i < 16; i++ ) { 

					int[] tmp = HSYToRGB.convert(
						(double) ( colorList.get(i)/256.0 ),saturationList[j], luminanceList[k] );

					colorMap[x][0] = tmp[0];
					colorMap[x][1] = tmp[1];
					colorMap[x][2] = tmp[2];
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
	public int labels(){
		return labelsBox.getItemCount();
	}



	   //////////////////////
	  // EVENT MANAGEMENT //
	 // INTERNAL CLASSES //
	//////////////////////



	private class aListener implements ActionListener { 

		public void actionPerformed( ActionEvent e ) { 

			Object source = e.getSource();
			if ( source == labelsBox ) { 

				if ( !reset ) {

					display.color[0] = (float) ( labelsBox.getSelectedIndex() / 256f );
					// set the current color to labelColor
					Color color = new Color( 
							(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 0 ],
							(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 1 ],
							(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 2 ] );
					colorPanel.setBackground(color);
				}

			} else
			if ( source == colorPanel ) { 

				// nothing to do here, chum.
			} else
			if ( source == addLabelsButton ) { 

				// Update plusButtonIndex
				nbLabels ++;
				display.color[0] = (float) ( labelsBox.getSelectedIndex() / 256f );

				// set the current color to labelColor
				Color color = new Color( 
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 0 ],
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 1 ],
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 2 ] );
				colorPanel.setBackground( color );

				// add a label to the combobox
				labelsBox.addItem( "Label " + nbLabels ) ;
				labelsBox.setSelectedItem( "Label " + nbLabels );

			} else
			if ( source == undoButton ) { 

				// re-display the last marker image saved in history
				WritableRenderedImage copy = markersHistory.rewind();
				display.raster.setData( copy.getData() );

				redoButton.setEnabled( true );
				if ( !markersHistory.canRewind() ) { 

					undoButton.setEnabled( false );
					// and display.tiledImage is empty, so ...
					okButton.setEnabled( false );
				}

				display.createColorMarkerImage();
				display.repaint();

			} else
			if ( source == redoButton ) { 

				WritableRenderedImage copy = markersHistory.forward();
				display.raster.setData( copy.getData() );

				undoButton.setEnabled( true );
				if ( !markersHistory.canForward() ) redoButton.setEnabled( false );

				display.createColorMarkerImage();
				display.repaint();

			} else
			if ( source == resetButton ) { 

				reset = true;
				WritableRenderedImage copy = markersHistory.genesis();
				undoButton.setEnabled( false );
				if ( markersHistory.canForward() ) redoButton.setEnabled( true );
				display.raster.setData( copy.getData() );
				display.createColorMarkerImage();
				display.repaint();
				// Reset variables
				nbLabels = 1;
				display.color[0] = (float) 1 / 256;
				// Remove all items and re-add the first ones
//				labelsBox.removeAllItems();
//				labelsBox.addItem("Eraser  ");
//				labelsBox.addItem("Label 1 ");
//				labelsBox.addItem("Label 2 ");
//				labelsBox.setSelectedIndex(1);
				Color color = new Color( 
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 0 ],
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 1 ],
						(int) display.colorMap[ labelsBox.getSelectedIndex() ][ 2 ] );
//				colorPanel.setBackground(color);
				reset = false;

			} else
			if ( source == okButton ) { 

				// Transform the marker image as a bufferedImage for the transformation
				BufferedImage bi = ((PlanarImage) display.raster).getAsBufferedImage();
				// raster gets the value of each pixel from im
				Raster raster = bi.getData();
				// Save the features of bi
				int type = bi.getType();
				int height = raster.getHeight();
				int width = raster.getWidth();
				// Set the band number to 1, we are looking for a greyscale image without alpha
				int band = 1;
				// Instanciates output with the correct width, height and number of band
				output = new ByteImage( width, height, 1, 1, band );
				// Transfers each byte from raster to output
				for ( int i = 0 ; i < width ; i++ )
					for ( int j = 0 ; j < height ; j++ )
						output.setPixelXYBByte( i,j,0, (byte) raster.getSample( i,j,0 ) );
				// Set the color parameter
				output.setColor( false );
				// set the type parameter
				output.type = type;

				frame.dispose();
			} 

		}
	}

	private class cListener implements ChangeListener { 

		public void stateChanged( ChangeEvent e ) { 

			Object source = e.getSource();
			if ( source == brushSpinner ) { 

				if ( brushSpinner.getValue() instanceof Number ) { 

					int brushSize = (Integer) brushSpinner.getValue();
					display.stroke = new BasicStroke( brushSize );
				}
			} else 
			if ( source == transparencySlider ) { 

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

	private class kListener extends KeyAdapter { 

		public void keyPressed( KeyEvent e ) { 

			System.out.println("glop" );
			if ( e.getKeyCode() == KeyEvent.VK_SHIFT ) { 

				display.horizontalScrollEnabled = !display.horizontalScrollEnabled;
				display.revalidate();
			} else 
			if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) 
				display.setNavigateMode( CustomDisplayJAI.ZOOM_MODE );
			else 
			if ( e.getKeyCode() == KeyEvent.VK_ALT ) 
				display.drawEnabled( CustomDisplayJAI.DRAW_OFF );
		}

		public void keyReleased( KeyEvent e ) { 

			if ( e.getKeyCode() == KeyEvent.VK_SHIFT ) { 

				display.horizontalScrollEnabled = !display.horizontalScrollEnabled;
				display.revalidate();
			} else 
			if ( e.getKeyCode() == KeyEvent.VK_CONTROL ) 
				display.setNavigateMode( CustomDisplayJAI.DEFAULT_MODE );
			else 
			if ( e.getKeyCode() == KeyEvent.VK_ALT ) 
				display.drawEnabled( CustomDisplayJAI.DRAW_ON );
		}
	}


	private class mListener implements MouseListener { 

		public void mouseClicked( MouseEvent e ) { display.mouseClicked(e); }
		public void mouseEntered( MouseEvent e ) { display.mouseEntered(e); }
		public void mouseExited( MouseEvent e )  { display.mouseExited(e); }
		public void mousePressed( MouseEvent e ) { display.mousePressed(e); }

		/** Invoked when the mouse button has been released on a component. 
		 *	@param e The event to process.
		 */
		public void mouseReleased( MouseEvent e ) { 

			// save current image for undo
			int widthTI  = display.raster.getWidth();
			int heightTI = display.raster.getHeight();
			WritableRenderedImage copy = 
				fr.unistra.pelican.util.Tools.createGrayImage( (ByteImage)null, widthTI,heightTI );
			copy.setData( display.raster.copyData() );
			markersHistory.add( copy );

			// Enable buttons
			undoButton.setEnabled( true );
			redoButton.setEnabled( false );
			resetButton.setEnabled( true );
			okButton.setEnabled( true );

//			display.mouseReleased(e);
        }

	}

	private class wListener implements WindowListener { 

		public void windowActivated( WindowEvent e ) {}

		public void windowClosed( WindowEvent e ) { 

			synchronized ( invoker ) { 

				isActive = false;
				invoker.notify();
			}
		}

		public void windowClosing( WindowEvent e ) {}
		public void windowDeactivated( WindowEvent e ) {}
		public void windowDeiconified( WindowEvent e ) {}
		public void windowIconified( WindowEvent e ) {}
		public void windowOpened( WindowEvent e ) {}
	}





	  ////////////////////
	 // STATIC METHODS //
	////////////////////

	/*	This gets rid of the annoying exception for not using native acceleration :
	 *	"Could not find mediaLib accelerator wrapper classes. Continuing in pure Java mode."
	 */	static { System.setProperty( "com.sun.media.jai.disableMediaLib", "true" ); }



}
