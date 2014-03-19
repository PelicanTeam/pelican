package fr.unistra.pelican.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.*;

import com.sun.media.jai.widget.DisplayJAI;

import fr.unistra.pelican.ByteImage;

/**	The CustomDisplayJAI handles all the image traitments, including for the
*	background image and for the marker image.
*
*	Thanks to the works of Allan Hanbury, Ilkka Luoma, Christophe Millet (SAIST)
*/
public class OldCustomDisplayJAI extends DisplayJAI {


	public static final int DEFAULT_BRUSH_SIZE = 5;
	public static final int MAX_BRUSH_SIZE = 100;
	public static final int MIN_BRUSH_SIZE = 1;
	private static final long serialVersionUID = 1L;

	/**	The markers image. */
	public TiledImage raster;
	/**	Indice of the transparency of the markers (255 = completely visible). */
	public int rasterTransparency = 255;
	/**	The pen. */
	public Stroke stroke;
	/**	The pen's color. It's a 1-sized float array, wich only element is between [0;1]. 
	 *	Precisely, it's a fraction of 255. */
	public float color[];

	/**	X coordinate of the cursor's last position. */
	private int xLast;
	/**	Y coordinate of the cursor's last position. */
	private int yLast;

	/**	The color marker image (after the createColorMarkerImage process). */
	private RenderedOp colorMarkerImage;

	/**	Determines if the user is drawing or not. */
	private boolean drawing = false;


	/** A lookup table associating all possible labels 
	 *	with their color ( 1 eraser + 256 shades ) 
	 */
	public int[][] colorMap = new int[257][4];

	public double scaleFactor = 1.0;
	AffineTransform transform = new AffineTransform();


	/**
	 * Constructor
	 * 
	 */
	public OldCustomDisplayJAI() { 

		super();
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		// Initializes variables
		this.stroke = new BasicStroke( DEFAULT_BRUSH_SIZE );

		// default pen color
		this.color = new float[1];
		this.color[0] = (float) 1 / 256;

	}

	@Override
	public void set( RenderedImage im ) { this.set(im,null); }

	/**	Method which sets the background image and creates the markers image based 
	 *	on markers image passed in parameter, or on on dimension of the background image
	 *	if {tt}markers{/tt} equals to {tt}nil{/tt}.
	 * @param im 
	 * @param markersImage 
	 */
	public void set( RenderedImage im, ByteImage markersImage ) { 

		super.set(im);
		// Create a marker image without an alpha channel
		this.raster = fr.unistra.pelican.util.Tools.createGrayImage( 
				markersImage, 
				im.getWidth(), 
				im.getHeight() );
		// repaint the component
		this.createColorMarkerImage();
		this.repaint();
	}

	/**
	 * paintComponent method
	 * @param g 
	 */
	public void paintComponent(Graphics g) { 

		// Paint the image in super class
//		super.paintComponent(g);

//////////////
		// Paint the image in super class
		// this is nearly the same instructions as in superclass, ...
		Graphics2D g2d = (Graphics2D)g;
		if ( source == null ) { 
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, getWidth(), getHeight());
			return;
		}
		Insets insets = getInsets();
		int tx = insets.left + originX;
		int ty = insets.top  + originY;
		Rectangle clipBounds = g2d.getClipBounds();
		g2d.setColor( getBackground() );
		g2d.fillRect(	clipBounds.x,
						clipBounds.y,
						clipBounds.width,
						clipBounds.height );

		// ... but not that : I added it for zoom management
		AffineTransform tr = new AffineTransform( this.transform );
		// Translation moves the entire image within the container	
		tr.concatenate( AffineTransform.getTranslateInstance(tx,ty) );
		g2d.drawRenderedImage( source,tr );

//////////////

		if ( colorMarkerImage != null ) { 
			// Get graphics and create GraphicsJAI
			Graphics2D g2d2 = (Graphics2D) g;
			GraphicsJAI gj = GraphicsJAI.createGraphicsJAI(g2d2, this);

			// Draw marker image
			gj.drawRenderedImage(colorMarkerImage, tr );
		}
	}

	/**
	 * Mouse pressed method
	 * @param e 
	 */
	public void mousePressed(MouseEvent e) { 

		if ( e.getButton() == MouseEvent.BUTTON1 ) { 

			if ( raster != null ) { 

				this.drawing = true;
				this.xLast = e.getX();
				this.yLast = e.getY();

				Graphics2D g2d = this.raster.createGraphics();

				// Set line width and marker (Alpha is 1.0,
				// because colorMap handles transparency)
				g2d.setStroke( this.stroke );
				g2d.setColor( new Color( 
						this.raster.getColorModel().getColorSpace(), 
						this.color, 
						1.0f ) );
				// Draw the line
				g2d.draw( new Line2D.Double( e.getX(),e.getY(), e.getX(),e.getY() ) );

				// Update
				createColorMarkerImage();

				// Repaint the component
				repaint();

			}
		}
	}

	/**
	 * Mouse dragged method
	 * @param e 
	 */
	public void mouseDragged(MouseEvent e) {

		if ( this.drawing == true ) { 

			if ( this.raster != null ) { 

				Graphics2D g2d = this.raster.createGraphics();
				// Set line width and marker (Aplha is 1.0 this time,
				// because lut handels transparency)
				g2d.setStroke( this.stroke );
				g2d.setColor( new Color( 
						this.raster.getColorModel().getColorSpace(), 
						this.color, 
						1.0f ) );

				// Draw the line
				g2d.draw( new Line2D.Double( e.getX(),e.getY(), this.xLast,this.yLast ) );

				// Update
				createColorMarkerImage();

				// Repaint the component
				repaint();

				this.xLast = e.getX();
				this.yLast = e.getY();
			}
		}
	}

	/**
	 * Mouse released method
	 * @param e 
	 */
	public void mouseReleased(MouseEvent e) {

		if ( e.getButton() == MouseEvent.BUTTON1 ) this.drawing = false;
	}

	/**	Initializes {@link #colorMarkerImage}. */
	public void createColorMarkerImage() {

		byte[][] lut = new byte[4][256];
		for ( int i = 0 ; i < 256 ; i++ ) { 

			lut[0][i] = (byte) colorMap[i][0];	// reds
			lut[1][i] = (byte) colorMap[i][1];	// greens
			lut[2][i] = (byte) colorMap[i][2];	// blues
			lut[3][i] = (byte) colorMap[i][3];	// alphas
		}
		LookupTableJAI table = new LookupTableJAI(lut);

		ParameterBlock pb = new ParameterBlock();
		pb.addSource( this.raster );
		pb.add( table );

		this.colorMarkerImage = JAI.create( "Lookup",pb,null );
	}

	@Override
	public void setPreferredSize( Dimension preferredSize ) { 

		super.setPreferredSize( preferredSize );
		this.transform.scale( scaleFactor,scaleFactor );
		repaint();
	}

}
