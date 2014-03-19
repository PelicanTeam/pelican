package fr.unistra.pelican.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.File;

import javax.media.jai.RasterFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import com.sun.media.jai.widget.DisplayJAI;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageSave;

/**
 * This class creates a JFrame for the visualisation of 2dimensional byte valued
 * images. Multiple channels, frames and dimension axis are only showed one
 * image at a time by means of sliders. Only the case of images with exactly 3
 * channels and the parameter "color" set, results in color.
 * 
 *  v1.2 added a better management of the memory, 
 *  the Pelican Image is not entirely transformed in displayJAI
 *  
 *  TODO : improve zoom management
 * 
 * @author Aptoula, Lefevre, Jonathan Weber
 * @version 1.2
 */
public class Frame2D extends JFrame {
	
	private static final long serialVersionUID = -1619980872944512678L;

	private Image img;

	private boolean color;

	private int width;

	private int height;

	private JTextField statusBar;

	private JSlider channelSld;

	private JSlider frameSld;

	private JSlider depthSld;

	private JLabel channelLbl;

	private JLabel frameLbl;

	private JLabel depthLbl;

	private MouseHandler mouseHandler;

	private JScrollPane scroll;

	public JScrollPane getScroll() {
		return this.scroll;
	}

	/**
	 * 
	 * @param img
	 *          image to show
	 * @param title
	 *          window title
	 * @param color
	 *          whether it should be shown in color or not
	 */
	public Frame2D(fr.unistra.pelican.Image img, String title, boolean color) {
		this(img, color); // yes, color must be added to the constructor!
		if (title != null)
			this.setTitle(title);
	}

	/**
	 * 
	 * @param img
	 *          image to show
	 * @param color
	 *          whether it should be shown in color or not
	 */
	public Frame2D(fr.unistra.pelican.Image img, boolean color) {
		final int bdim = img.getBDim();
		final int tdim = img.getTDim();
		final int zdim = img.getZDim();

		final Frame2D ben = this; // yes its ugly
		this.color = color;
		this.width = img.getXDim();
		this.height = img.getYDim();
		this.img = img;

		if (this.color == true && bdim != 3) {
			System.err.println("Only " + bdim + " channel"
				+ ((bdim > 1) ? "s " : " ") + "found. Color visualisation cancelled");
			this.color = false;
		}

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



		JPanel root = new JPanel();

		Toolkit k = Toolkit.getDefaultToolkit();
		Dimension tailleEcran = k.getScreenSize();
		root.setPreferredSize(new Dimension(Math.min(tailleEcran.width - 3, img
			.getXDim() + 3), Math.min(tailleEcran.height - 81, img.getYDim() + 81)));

		root.setLayout(new BorderLayout());
		this.setContentPane(root);
		scroll = new JScrollPane(null,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		if (this.color == false) 
		{
			makeDisplayedImage(0,0,0);
		} 
		else 
		{
			makeColorDisplayedImage(0,0);
		}

		mouseHandler = new MouseHandler(this);

		scroll.addMouseListener(mouseHandler);
		scroll.addMouseMotionListener(mouseHandler);
		root.add(scroll, BorderLayout.CENTER);

		// lower sub panel with labels and sliders...one horizontal box for each
		JPanel bottomPanel = new JPanel(); // sliders + statusbar
		bottomPanel.setLayout(new BorderLayout());

		statusBar = new JTextField();
		statusBar.setEnabled(false);
		bottomPanel.add(statusBar, BorderLayout.SOUTH);

		JPanel subPnl = new JPanel(); // only sliders
		subPnl.setLayout(new GridLayout(3, 1, 0, 5));
		bottomPanel.add(subPnl, BorderLayout.CENTER);

		root.add(bottomPanel, BorderLayout.SOUTH);

		// channels
		JPanel channelBox = new JPanel();
		channelBox.setLayout(new BoxLayout(channelBox, BoxLayout.X_AXIS));

		channelLbl = new JLabel(" Channel : 1/" + bdim + "  ");
		channelBox.add(channelLbl);

		channelSld = new JSlider(SwingConstants.HORIZONTAL, 1, bdim, 1);
		channelSld.setSnapToTicks(true);
		channelBox.add(channelSld);

		
		channelSld.addChangeListener(new ChangeListener() { 

			public void stateChanged(ChangeEvent e) 
			{
				channelLbl.setText(" Channel : "
						+ Integer.toString(channelSld.getValue()) + "/" + bdim + "  ");

					int b = channelSld.getValue() - 1;
					int t = frameSld.getValue() - 1;
					int z = depthSld.getValue() - 1;

					if (ben.color == false) 
					{
						makeDisplayedImage(z,t,b);
					} 
					else
					{
						makeColorDisplayedImage(z,t);					
					}				
			}


			
		});

		subPnl.add(channelBox);

		if (bdim == 1 || this.color == true) {
			channelLbl.setEnabled(false);
			channelSld.setEnabled(false);
		}

		// frames
		JPanel frameBox = new JPanel();
		frameBox.setLayout(new BoxLayout(frameBox, BoxLayout.X_AXIS));

		frameLbl = new JLabel(" Frame : 1/" + tdim + "     ");
		frameBox.add(frameLbl);

		frameSld = new JSlider(SwingConstants.HORIZONTAL, 1, tdim, 1);
		frameSld.setSnapToTicks(true);
		frameBox.add(frameSld);

		frameSld.addChangeListener(new ChangeListener() { 

			public void stateChanged(ChangeEvent e) 
			{
				frameLbl.setText(" Frame : " + Integer.toString(frameSld.getValue())
						+ "/" + tdim + "     ");

					int b = channelSld.getValue() - 1;
					int t = frameSld.getValue() - 1;
					int z = depthSld.getValue() - 1;

					if (ben.color == false) 
					{
						makeDisplayedImage(z,t,b);
					} 
					else 
					{
						makeColorDisplayedImage(z,t);
					}
				}			
		});
		
		subPnl.add(frameBox);

		if (tdim == 1) {
			frameLbl.setEnabled(false);
			frameSld.setEnabled(false);
		}

		// depth
		JPanel depthBox = new JPanel();
		depthBox.setLayout(new BoxLayout(depthBox, BoxLayout.X_AXIS));

		depthLbl = new JLabel(" Depth : 1/" + zdim + "      ");
		depthBox.add(depthLbl);

		depthSld = new JSlider(SwingConstants.HORIZONTAL, 1, zdim, 1);
		depthSld.setSnapToTicks(true);
		depthBox.add(depthSld);

		depthSld.addChangeListener(new ChangeListener() { 

			public void stateChanged(ChangeEvent e) 
			{
				depthLbl.setText(" Depth : " + Integer.toString(depthSld.getValue())
						+ "/" + zdim + "      ");

					int b = channelSld.getValue() - 1;
					int t = frameSld.getValue() - 1;
					int z = depthSld.getValue() - 1;

					if (ben.color == false) 
					{
						makeDisplayedImage(z,t,b);
					} 
					else 
					{
						makeColorDisplayedImage(z,t);
					}				
			}
			
		});
		
		subPnl.add(depthBox);

		if (zdim == 1) {
			depthLbl.setEnabled(false);
			depthSld.setEnabled(false);
		}

		if (!channelLbl.isEnabled())
			subPnl.remove(channelBox);
		if (!frameLbl.isEnabled())
			subPnl.remove(frameBox);
		if (!depthLbl.isEnabled())
			subPnl.remove(depthBox);

		pack();
		setVisible(true);
	}
	
	private void makeDisplayedImage(int z, int t, int b)
	{
		DataBufferByte dbb;
		SampleModel s;
		Raster r;
		BufferedImage bimg = null;
		int xDim = img.getXDim();
		int yDim = img.getYDim();
		int zDim = img.getZDim();
		int bDim = img.getBDim();
		int imageDim = xDim*yDim;
		byte[] byteVal = new byte[imageDim];
		int shift = ((t*zDim+z)*imageDim*bDim)+b;		
		for(int i =0;i<imageDim;i++)
		{
			byteVal[i]=(byte)img.getPixelByte(shift);
			shift+=bDim;
		}
		dbb = new DataBufferByte(byteVal, byteVal.length);
		s = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, xDim, yDim, 1);
		r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
		bimg = new BufferedImage(xDim, yDim, BufferedImage.TYPE_BYTE_GRAY);
		bimg.setData(r);
		int vValue = scroll.getVerticalScrollBar().getValue();
		int hValue = scroll.getHorizontalScrollBar().getValue();
		scroll.setViewportView(new DisplayJAI(bimg));
		scroll.getVerticalScrollBar().setValue(vValue);
		scroll.getHorizontalScrollBar().setValue(hValue);
	}
	
	private void makeColorDisplayedImage(int z, int t)
	{
		DataBufferByte dbb;
		SampleModel s;
		Raster r;
		BufferedImage bimg = null;		
		int[] bandOffsets = { 0, 1, 2 };		
		int xDim = img.getXDim();
		int yDim = img.getYDim();
		int zDim = img.getZDim();
		int imageDim = xDim*yDim*3;
		byte[] byteVal = new byte[imageDim];
		int shift = (t*zDim+z)*imageDim;
		for(int i =0;i<imageDim;i++)
		{
			byteVal[i]=(byte)img.getPixelByte(shift++);
		}		
		dbb = new DataBufferByte(byteVal, byteVal.length);
		s = RasterFactory.createPixelInterleavedSampleModel(
			DataBuffer.TYPE_BYTE, xDim, yDim, 3, 3
				* xDim, bandOffsets);
		r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
		bimg = new BufferedImage(xDim, yDim, BufferedImage.TYPE_3BYTE_BGR);
		bimg.setData(r);
		int vValue = scroll.getVerticalScrollBar().getValue();
		int hValue = scroll.getHorizontalScrollBar().getValue();
		scroll.setViewportView(new DisplayJAI(bimg));
		scroll.getVerticalScrollBar().setValue(vValue);
		scroll.getHorizontalScrollBar().setValue(hValue);
	}

	void statusBarMsg(String s) {
		SwingUtilities.invokeLater(new MainFrameRunnable(this, s) {
			public void run() {
				frame2d.statusBar.setText((String) obj);
			}
		});
	}

	class MainFrameRunnable implements Runnable {
		Frame2D frame2d;

		Object obj;

		/**
		 * 
		 * @param fr2d
		 * @param obj
		 */
		public MainFrameRunnable(Frame2D fr2d, Object obj) {
			frame2d = fr2d;
			this.obj = obj;
		}

		public void run() {
		}
	}

	/**
	 * heavyweight subclass handling all mouse events fired by the main
	 * jscrollpane.
	 * 
	 * main functions : 1) coordinate and value info on statusbar 2) magnifying
	 * lens (x4)
	 * 
	 */
	private class MouseHandler extends MouseInputAdapter {
		private JWindow zoomWindow = null;

		private JPanel root = null;

		private Frame2D parent;

		private int width = 100;

		private int height = 100;

		private JScrollPane scrollX = null;

		private int widthX = 25;

		private int heightX = 25;

		private Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit()
			.createCustomCursor(new BufferedImage(1, 1, BufferedImage.TRANSLUCENT),
				new Point(0, 0), "blank");

		MouseHandler(Frame2D parent) {
			this.parent = parent;

			zoomWindow = new JWindow(parent);

			root = new JPanel();
			root.setPreferredSize(new Dimension(width, height));
			root.setLayout(new BorderLayout());

			zoomWindow.setContentPane(root);
			zoomWindow.pack();
		}

		private void updateImage(int xdim, int ydim) {
			JViewport view = parent.scroll.getViewport();
			Point local = view.getViewPosition();

			// get the pixels of the area..25x25.
			// centered on the current pixel
			byte[] pixels = null;

			int[] ipixel = null;
			int[] bandOffsets = { 0, 1, 2 };
			int[] voidPixel = { 0, 0, 0 };

			if (parent.color == false) {
				ipixel = new int[1];
				pixels = new byte[width * height];
			} else {
				ipixel = new int[3];
				pixels = new byte[width * height * 3];
			}

			int xref = xdim + local.x - widthX / 2;
			int yref = ydim + local.y - heightX / 2;

			for (int x = 0; x < widthX; x++) {
				for (int y = 0; y < heightX; y++) {
					if (xref + x < 0 || xref + x >= parent.width || yref + y < 0
						|| yref + y >= parent.height) {
						if (parent.color == false)
							setPixel(pixels, x * 4, y * 4, (byte) 0);
						else
							setColorPixel(pixels, x * 4, y * 4, voidPixel);
					} else {
						int t = frameSld.getValue() - 1;
						int z = depthSld.getValue() - 1;
						int finalX=xref + x;
						int finalY=yref + y;
						if (parent.color == false)
						{
							int b = channelSld.getValue() - 1;
							setPixel(pixels, x * 4, y * 4, (byte) img.getPixelXYZTBByte(finalX,finalY,z,t,b));
						}
						else
						{
							ipixel[0]= img.getPixelXYZTBByte(finalX,finalY,z,t,0);
							ipixel[1]= img.getPixelXYZTBByte(finalX,finalY,z,t,1);
							ipixel[2]= img.getPixelXYZTBByte(finalX,finalY,z,t,2);
							setColorPixel(pixels, x * 4, y * 4, ipixel);
						}
					}
				}
			}
			BufferedImage bimg = null;
			if (parent.color == false) {
				DataBufferByte dbb = new DataBufferByte(pixels, width * height);
				SampleModel s = RasterFactory.createBandedSampleModel(
					DataBuffer.TYPE_BYTE, width, height, 1);
				Raster r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
				bimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
				bimg.setData(r);
			} else {
				DataBufferByte dbb = new DataBufferByte(pixels, width * height * 3);
				SampleModel s = RasterFactory.createPixelInterleavedSampleModel(
					DataBuffer.TYPE_BYTE, width, height, 3, 3 * width, bandOffsets);
				Raster r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
				bimg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
				bimg.setData(r);
			}

			DisplayJAI disp = new DisplayJAI(bimg);

			if (scrollX == null) {
				scrollX = new JScrollPane(disp,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				root.add(scrollX, BorderLayout.CENTER);

				zoomWindow.pack();
			} else {
				scrollX.setViewportView(disp);
			}

			Point p = parent.scroll.getLocationOnScreen();
			zoomWindow.setLocation(p.x + xdim - width, p.y + ydim - height);
			zoomWindow.setVisible(true);
		}

		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				setCursor(BLANK_CURSOR);
				updateImage(e.getX(), e.getY());
				mouseMoved(e);
			} else {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Picture save");

				int returnVal = chooser.showSaveDialog(parent);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("You save the picture here : "
						+ chooser.getCurrentDirectory() + File.separator
						+ chooser.getSelectedFile().getName());

					ImageSave.exec(new ByteImage(img), chooser.getCurrentDirectory()
						+ File.separator + chooser.getSelectedFile().getName());
				}
			}
		}

		public void mouseDragged(MouseEvent e) {
			updateImage(e.getX(), e.getY());
			mouseMoved(e);
		}

		private void setPixel(byte[] pixels, int x, int y, byte v) {
			for (int _x = x; _x < x + 4; _x++) {
				for (int _y = y; _y < y + 4; _y++) {
					pixels[_x + width * _y] = v;
				}
			}
		}

		private void setColorPixel(byte[] pixels, int x, int y, int[] v) {
			for (int b = 0; b < 3; b++) {
				for (int _x = x; _x < x + 4; _x++) {
					for (int _y = y; _y < y + 4; _y++) {
						pixels[b + 3 * _x + 3 * width * _y] = (byte) v[b];
					}
				}
			}
		}

		public void mouseMoved(MouseEvent e) {
			int xdim = e.getX();
			int ydim = e.getY();

			JViewport view = parent.scroll.getViewport();
			Point local = view.getViewPosition();

			xdim += local.x;
			ydim += local.y;

			if (xdim >= parent.width || ydim >= parent.height || xdim < 0 || ydim < 0)
				return;

			if (color == true) {
				int t = frameSld.getValue() - 1;
				int z = depthSld.getValue() - 1;
				int pixel0 = img.getPixelXYZTBByte(xdim,ydim,z,t,0);
				int pixel1 = img.getPixelXYZTBByte(xdim,ydim,z,t,1);
				int pixel2 = img.getPixelXYZTBByte(xdim,ydim,z,t,2);
				parent.statusBarMsg("(" + xdim + "," + ydim + "):(" + pixel0 + ","
					+ pixel1 + "," + pixel2 + ")");
			} else {
				int b = channelSld.getValue() - 1;
				int t = frameSld.getValue() - 1;
				int z = depthSld.getValue() - 1;
				int pixelValue = img.getPixelXYZTBByte(xdim,ydim,z,t,b);
				parent.statusBarMsg("(" + xdim + "," + ydim + "):(" + pixelValue + ")");
			}
		}

		public void mouseReleased(MouseEvent e) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			zoomWindow.setVisible(false);
		}
	}
}
