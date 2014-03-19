package fr.unistra.pelican.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.*;
import javax.swing.*;
import com.sun.media.jai.widget.DisplayJAI;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.IntegerImage;



/**
 *	This is a Scrollable and Zoomable picture in form of a { @link javax.swing.JPanel }. 
 *	Its main purpose is being added to a { @link javax.swing.JScrollPane }.
 *	<p> 
 *	For the GUI, that means :
 *	<p>
 *	Scroll the PZPicture as usual in the JScrollPane. When the PZPicture is in 
 *	{@link #DEFAULT_MODE}, you can roll your mouse wheel to navigate, as usual.
 *	<p>
 *	Roll your mouse wheel when the PZPicture is in {@link #ZOOM_MODE} to zoom on it.
 *	<p>
 *	Pan the PZPicture by dragging & dropping your mouse. 
 *	
 *	@author witz
 *
 */
public class CustomDisplayJAI extends DisplayJAI implements MouseWheelListener { 



	  ///////////////
	 // CONSTANTS //
	///////////////

	public static final long serialVersionUID = 1L;
	public static final int DEFAULT_MODE = 0;
	public static final int ZOOM_MODE = 1;
	public static final double ZOOM_IN_FACTOR = 1.1;
	public static final double ZOOM_OUT_FACTOR = 0.9;
	public static final boolean ZOOM_IN = true;
	public static final boolean ZOOM_OUT = false;
	public static final boolean DRAW_ON = true;
	public static final boolean DRAW_OFF = false;

	public static final int DEFAULT_BRUSH_SIZE = 3;
	public static final int MAX_BRUSH_SIZE = 100;
	public static final int MIN_BRUSH_SIZE = 1;

	  ////////////
	 // FIELDS //
	////////////


	/**	X coordinate of the cursor's last position. */
	private int xLast;
	/**	Y coordinate of the cursor's last position. */
	private int yLast;

	/**	Area taken up by graphics. */
	private Dimension area;
	private AffineTransform transform;

	int ow;
	int oh;
	private BufferedImage original;
	private RenderedImage rendered;

	private double hscalemax;
	private double hscalemin;
	private double hscale = 1.;
	private double wscalemax;
	private double wscalemin;
	private double wscale = 1.;

	public boolean horizontalScrollEnabled = false;

	private int navigateMode = DEFAULT_MODE;
	private boolean drawMode;

	/**	Determines if the user is drawing or not. */
	private boolean drawing = false;
	/**	The markers image. */
	public TiledImage raster;
	/**	Indice of the transparency of the markers (255 = completely visible). */
	public int rasterTransparency = 255;
	/**	The pen. */
	public Stroke stroke;
	/**	The pen's color. It's a 1-sized float array, wich only element is between [0;1]. 
	 *	Precisely, it's a fraction of 255. */
	public float color[];
	/** A lookup table associating all possible labels with their color ( 1 eraser+256 shades ). */
	public int[][] colorMap = new int[257][4];
	/**	The color marker image (after the createColorMarkerImage process). */
	private RenderedOp colorMarkerImage;



	/**	Default to it if any. */
	public MouseWheelListener defaultMouseWheelListener;



	  /////////////////
	 // CONSTRUCTOR //
	/////////////////

	public CustomDisplayJAI() { 

		super();

		this.addMouseListener( this );
		this.addMouseMotionListener( this );
		this.addMouseWheelListener( this );

		this.stroke = new BasicStroke( DEFAULT_BRUSH_SIZE );
		this.color = new float[1];
		this.color[0] = 1f / 256f;

		this.transform = new AffineTransform();
		this.area = new Dimension();

		this.drawEnabled( DRAW_ON );

	}



	  /////////////
	 // METHODS //
	/////////////

	@Override
	public void set( RenderedImage im ) { this.set(im, (ByteImage) null); }

	/**	Method which sets the background image and creates the markers image based 
	 *	on markers image passed in parameter, or on on dimension of the background image
	 *	if {tt}markers{/tt} equals to {tt}nil{/tt}.
	 * @param image 
	 * @param markersImage 
	 */
	public void set( RenderedImage image, ByteImage markersImage ) { 

//		super.set( image );

		this.original = (BufferedImage)image;

		Dimension odd = this.getOWH();
		this.ow = odd.width;
		this.oh = odd.height;
		this.rendered = toBuf( this.original.getScaledInstance( ow,oh, Image.SCALE_DEFAULT ) );

		// define maximum and minimum sizes (with respect to the original image resolution) 
		// to wich the rendered image can be scaled.
		int rw = Toolkit.getDefaultToolkit().getScreenSize().width;
		int rh = Toolkit.getDefaultToolkit().getScreenSize().height;
		int hmax,wmax, hmin,wmin;
		if ( this.ow > this.oh ) { 

			wmax = Math.max( this.ow, rw );
			hmax = (int)( (double)( wmax*this.oh ) / (double)this.ow );
			wmin = Math.min( this.ow, 10 );
			hmin = (int)( (double)( wmin*this.oh ) / (double)this.ow );
		} else { 

			hmax = Math.max( this.oh, rh );
			wmax = (int)( (double)( hmax*this.ow ) / (double)this.oh );
			hmin = Math.min( this.oh, 10 );
			wmin = (int)( (double)( hmin*this.ow ) / (double)this.oh );
		}
		this.hscalemax = (double)hmax / (double)oh;
		this.wscalemax = (double)wmax / (double)ow;
		this.hscalemin = (double)hmin / (double)oh;
		this.wscalemin = (double)wmin / (double)ow;

		punch();
		
		// Create a marker image without an alpha channel
		this.raster = fr.unistra.pelican.util.Tools.createGrayImage( markersImage,this.ow,this.oh );
		// repaint the component
		this.createColorMarkerImage();
		this.repaint();
	}

	
	/**	Method which sets the background image and creates the markers image based 
	 *	on markers image passed in parameter, or on on dimension of the background image
	 *	if {tt}markers{/tt} equals to {tt}nil{/tt}.
	 * @param image 
	 * @param markersImage 
	 */
	public void set( RenderedImage image, IntegerImage markersImage ) { 

		this.original = (BufferedImage)image;

		Dimension odd = this.getOWH();
		this.ow = odd.width;
		this.oh = odd.height;
		this.rendered = toBuf( this.original.getScaledInstance( ow,oh, Image.SCALE_DEFAULT ) );

		// define maximum and minimum sizes (with respect to the original image resolution) 
		// to wich the rendered image can be scaled.
		int rw = Toolkit.getDefaultToolkit().getScreenSize().width;
		int rh = Toolkit.getDefaultToolkit().getScreenSize().height;
		int hmax,wmax, hmin,wmin;
		if ( this.ow > this.oh ) { 

			wmax = Math.max( this.ow, rw );
			hmax = (int)( (double)( wmax*this.oh ) / (double)this.ow );
			wmin = Math.min( this.ow, 10 );
			hmin = (int)( (double)( wmin*this.oh ) / (double)this.ow );
		} else { 

			hmax = Math.max( this.oh, rh );
			wmax = (int)( (double)( hmax*this.ow ) / (double)this.oh );
			hmin = Math.min( this.oh, 10 );
			wmin = (int)( (double)( hmin*this.ow ) / (double)this.oh );
		}
		this.hscalemax = (double)hmax / (double)oh;
		this.wscalemax = (double)wmax / (double)ow;
		this.hscalemin = (double)hmin / (double)oh;
		this.wscalemin = (double)wmin / (double)ow;

		punch();
		
		// Create a marker image without an alpha channel
		this.raster = fr.unistra.pelican.util.Tools.createGrayImage( markersImage,this.ow,this.oh );
		// repaint the component
		this.createColorMarkerImage();
		this.repaint();
	}
	
	
	public void setNavigateMode( int navigateMode ) { 

		if (	navigateMode == DEFAULT_MODE 
			 || navigateMode == ZOOM_MODE )
			this.navigateMode = navigateMode;
		else this.navigateMode = DEFAULT_MODE;
	}

	public void drawEnabled( boolean enabled ) { 

		this.drawMode = enabled;
		if ( enabled ) this.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
		else this.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
	}

	/**	Return a {@link java.awt.Dimension} object containing the original width and height of 
	 *	the original image {@link original}. This method erases the previous values contained in 
	 *	<tt>width</tt> and <tt>height</tt>.
	 *	This method is vital, 'cause if you just do something simple like : 
	 *	<pre>
	 *	width = this.original.getWidth(null); 
	 *	height = int oh = this.original.getHeight(null); 
	 *	</pre>
	 *	<p>
	 *	 ... you're just screwed. T_T
	 */
	private Dimension getOWH() { 

		int ow = this.original.getWidth(  null );
		int oh = this.original.getHeight( null );
		while ( ow < 0 || oh < 0 ) { 

			try { Thread.sleep(25); }
			catch (InterruptedException e) {}
			ow = this.original.getWidth(  null );
			oh = this.original.getHeight( null );
		}
		return new Dimension( ow,oh );
	}

	private void punch() { 

		int rw = (int)( this.rendered.getWidth()  * this.transform.getScaleX() );
		int rh = (int)( this.rendered.getHeight() * this.transform.getScaleY() );
		if ( this.area.width != rw || this.area.width != rh ) { 

			this.area.width = rw;
			this.area.height = rh;
			// Update client's preferred size because the area taken up 
			// by the graphics has gotten larger or smaller.
			this.setPreferredSize( area );
			// Let the others know to update themselves
			this.revalidate();
		}
	}



	/**	Zoom on the rendered image if <tt>zoom</tt> equals to { @link #ZOOM_IN }.
	 *	Dezoom on the rendered image if <tt>zoom</tt> equals to { @link #ZOOM_OUT }.
	 *	The zoom ratio is of 10% in any case.
	 *	@param zoom { @link #ZOOM_IN } or { @link #ZOOM_OUT }.
	 */
	public double zoom( boolean zoom ) 
	{ 
		if ( zoom == ZOOM_IN ) 
		this.rescale( this.hscale * ZOOM_IN_FACTOR, this.wscale * ZOOM_IN_FACTOR );
		else	// zoom == ZOOM_OUT
		this.rescale( this.hscale * ZOOM_OUT_FACTOR, this.wscale * ZOOM_OUT_FACTOR );
		return this.hscale;
	}

	/**	Resets the rendered image to its initial scale. */
	public void resetScale()			{ this.rescale( 1. ); }

	/**	Sets the rendered image to the scale <tt>scale</tt>. 
	 *	@param scale New image scale. 
	 */
	public void rescale( double scale ) { this.rescale( scale,scale ); }

	/**	Sets the rendered image scale to <tt>ws</tt> in the X dimension, 
	 *	and to <tt>hs</tt> in the X dimension. 
	 * @param ws New image scale in X. 
	 * @param hs New image scale in Y. 
	 */
	public void rescale( double ws, double hs ) { 

		this.hscale = hs;
		this.wscale = ws;

		if ( this.hscale > hscalemax ) this.hscale = this.hscalemax;
		if ( this.wscale > wscalemax ) this.wscale = this.wscalemax;
		if ( this.hscale < hscalemin ) this.hscale = this.hscalemin;
		if ( this.wscale < wscalemin ) this.wscale = this.wscalemin;

//		int w = (int)( this.ow * this.wscale );
//		int h = (int)( this.oh * this.hscale );
//		this.rendered = toBuf( this.original.getScaledInstance( w,h, Image.SCALE_DEFAULT ) );
		this.transform = new AffineTransform( 
			AffineTransform.getScaleInstance( this.wscale,this.hscale ) );

		punch();
		repaint();
	}



	public static BufferedImage toBuf( Image image ) { 

		if( image instanceof BufferedImage ) return( (BufferedImage)image );
		else { 

			image = new ImageIcon(image).getImage();
			BufferedImage bufferedImage = new BufferedImage( image.getWidth(null),
															 image.getHeight(null),
															 BufferedImage.TYPE_INT_ARGB );
			Graphics g = bufferedImage.createGraphics();
			g.drawImage(image,0,0,null);
			g.dispose();
			return( bufferedImage );
		}
	}


	@Override
	public void paintComponent( Graphics g ) { 

		super.paintComponent( g );

		Graphics2D g2d2 = (Graphics2D) g;
		javax.media.jai.GraphicsJAI gj = javax.media.jai.GraphicsJAI.createGraphicsJAI(g2d2,this);
		gj.drawRenderedImage( this.rendered, this.transform );

		if ( colorMarkerImage != null ) { 

			// Draw marker image
			gj.drawRenderedImage( colorMarkerImage, this.transform );
		}

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





	public void mouseDragged( MouseEvent e ) { 

		if ( this.drawMode == DRAW_ON ) {

			if ( this.drawing == true ) { 

				if ( this.raster != null ) { 

					Graphics2D g2d = this.raster.createGraphics();
					// Set line width and marker (Aplha is 1.0 this time,
					// because lut handels transparency)
					g2d.setStroke( this.stroke );
					g2d.setColor( new Color( this.raster.getColorModel().getColorSpace(), 
											 this.color, 
											 1.0f ) );

					Point pe = new Point( e.getX(),e.getY() );
					Point pl = new Point( this.xLast,this.yLast );
					AffineTransform inverse = null;
					try{ inverse = this.transform.createInverse(); }
					catch( NoninvertibleTransformException ex ) { /* This never happens.*/ }
					inverse.transform( pe,pe );
					inverse.transform( pl,pl );
					// Draw the line
					g2d.draw( new Line2D.Double( pe, pl ) );
					// Update
					createColorMarkerImage();
					// Repaint the component
					repaint();
					this.xLast = e.getX();
					this.yLast = e.getY();
				}
			}

		} else { 

			Container c = CustomDisplayJAI.this.getParent();
			if ( c instanceof JViewport ) {

				JViewport viewport = (JViewport) c;
				Point p = viewport.getViewPosition();
				int x = p.x - (e.getX() - this.xLast);
				int y = p.y - (e.getY() - this.yLast);

				int xMax = CustomDisplayJAI.this.getWidth() - viewport.getWidth();
				int yMax = CustomDisplayJAI.this.getHeight() - viewport.getHeight();

				if ( x < 0 )	x = 0;
				if ( x > xMax )	x = xMax;
				if ( y < 0 )	y = 0;
				if ( y > yMax )	y = yMax;

				viewport.setViewPosition( new Point(x,y) );
			}
		}

	}

	public void mousePressed( MouseEvent e ) {

		if ( this.drawMode == DRAW_ON ) { 

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
					Point pe = new Point( e.getX(),e.getY() );
					Point pl = new Point( this.xLast,this.yLast );
					AffineTransform inverse = null;
					try{ inverse = this.transform.createInverse(); }
					catch( NoninvertibleTransformException ex ) { /* This never happens.*/ }
					inverse.transform( pe,pe );
					inverse.transform( pl,pl );
					// Draw the line
					g2d.draw( new Line2D.Double( pe, pl ) );

					// Update
					createColorMarkerImage();

					// Repaint the component
					repaint();

				}
			}

		} else { 

			this.setCursor( Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ) );
			xLast = e.getX();
			yLast = e.getY();
		}
	}

	public void mouseReleased( MouseEvent e ) {

		if ( this.drawMode == DRAW_ON ) { 

			if ( e.getButton() == MouseEvent.BUTTON1 ) this.drawing = false;
		}
		else this.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
	}



	public void mouseWheelMoved( MouseWheelEvent e ) { 

		if ( this.navigateMode == ZOOM_MODE ) { 

			Container c = CustomDisplayJAI.this.getParent();
			if ( c instanceof JViewport ) { 

				if ( e.getWheelRotation() < 0 ) this.zoom( CustomDisplayJAI.ZOOM_IN );
				else CustomDisplayJAI.this.zoom( CustomDisplayJAI.ZOOM_OUT );
				CustomDisplayJAI.this.revalidate();
				e.consume();
			}

		} else { 

			if ( this.defaultMouseWheelListener != null ) 
				this.defaultMouseWheelListener.mouseWheelMoved(e);
		}
	}




}
