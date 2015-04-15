package fr.unistra.pelican.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

import javax.media.jai.RasterFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.media.jai.widget.DisplayJAI;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;


/**
 * This class creates a JFrame for the visualization of byte valued
 * videos in motion. Only the case of videos with exactly 3
 * channels and the parameter "color" set, results in color.
 * 
 * @author Jonathan Weber
 */
public class FrameVideo extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 200807081535L;

	private Image video;

	private boolean color;

	private JTextField statusBar;

	private JSlider frameSld;

	private JButton playPause;

	private JScrollPane scroll;
	
	private Timer timer;
	
	private double frameRate;
	
	private boolean onLoop=true;

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
	public FrameVideo(fr.unistra.pelican.Image img, String title, boolean color, double frameRate,boolean onLoop) {
		this(img, color, frameRate,onLoop); // yes, color must be added to the constructor!
		if (title != null)
			this.setTitle(title);
	}

	/**
	 * 
	 * @param video
	 *          image to show
	 * @param color
	 *          whether it should be shown in color or not
	 */
	public FrameVideo(fr.unistra.pelican.Image video, boolean color, final double frameRate,boolean onLoop) {
		if(video.getZDim()!=1)
			video=(ByteImage)video.getImage4D(0, Image.Z);
		
		if(video.getBDim()!=3&&video.getBDim()!=1)
			video=(ByteImage)AverageChannels.exec(video);
		
		final int bdim = video.getBDim();
		final int tdim = video.getTDim();
		this.frameRate=frameRate;
		this.video=video;
		final FrameVideo ben = this; // yes its ugly
		this.color = color;
		this.onLoop=onLoop;

		if (this.color == true && bdim != 3) {
			System.err.println("Only " + bdim + " channel"
				+ ((bdim > 1) ? "s " : " ") + "found. Color visualisation cancelled");
			this.color = false;
		}
		
		if(this.color == false && bdim !=1)
		{
			this.video = (ByteImage) this.video.getImage4D(0, Image.B);
		}

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



		JPanel root = new JPanel();

		Toolkit k = Toolkit.getDefaultToolkit();
		Dimension tailleEcran = k.getScreenSize();
		root.setPreferredSize(new Dimension(Math.min(tailleEcran.width - 3, video
			.getXDim() + 3), Math.min(tailleEcran.height - 49, video.getYDim() + 49)));

		root.setLayout(new BorderLayout());
		this.setContentPane(root);
		scroll = new JScrollPane(null,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		root.add(scroll, BorderLayout.CENTER);
		
		if (this.color == false) 
		{
			makeCurrentFrameDisplayed(0);
		} 
		else 
		{
			makeColorCurrentFrameDisplayed(0);
		}

		
		JPanel bottomPanel = new JPanel(); 
		bottomPanel.setLayout(new BorderLayout());

		statusBar = new JTextField();
		statusBar.setEnabled(false);
		bottomPanel.add(statusBar, BorderLayout.SOUTH);

		root.add(bottomPanel, BorderLayout.SOUTH);



		// frames
		JPanel frameBox = new JPanel();
		frameBox.setLayout(new BoxLayout(frameBox, BoxLayout.X_AXIS));

		playPause = new JButton("PLAY ");
		playPause.addActionListener(this);
		frameBox.add(playPause);

		frameSld = new JSlider(SwingConstants.HORIZONTAL, 1, tdim, 1);
		frameSld.setSnapToTicks(true);
		frameBox.add(frameSld);

		frameSld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				statusBar.setText(" Frame : " + Integer.toString(frameSld.getValue())
					+ "/" + tdim + "     | Time : "+ Double.toString(frameSld.getValue()/frameRate)+" sec");

				int t = frameSld.getValue() - 1;

				if (ben.color == false) 
				{
					makeCurrentFrameDisplayed(t);
				} 
				else 
				{
					makeColorCurrentFrameDisplayed(t);
				}
				if(frameSld.getValue()==frameSld.getMaximum())
				{
					if(!ben.onLoop)
					{
						timer.stop();
						playPause.setText("PLAY ");
					}
				}
			}
		});
		bottomPanel.add(frameBox, BorderLayout.CENTER);


		

		pack();
		setVisible(true);
		timer = createTimer ();
		if(onLoop)
		{
			playPause.setText("PAUSE");
			timer.start();			
		}
	}
	
	private void makeCurrentFrameDisplayed(int t)
	{
		DataBufferByte dbb;
		SampleModel s;
		Raster r;
		BufferedImage bimg = null;
		
		int xDim = video.getXDim();
		int yDim = video.getYDim();
		int frameDim = xDim*yDim;
		
		byte[] byteVal = new byte[frameDim];
		int shift = t*frameDim;
		for(int i=0;i<frameDim;i++)
		{
			byteVal[i]=(byte)video.getPixelByte(shift++);
		}		
		dbb = new DataBufferByte(byteVal, byteVal.length);
		s = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, xDim, yDim, 1);
		r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
		bimg = new BufferedImage(xDim, yDim, BufferedImage.TYPE_BYTE_GRAY);
		bimg.setData(r);		
		scroll.setViewportView(new DisplayJAI(bimg));
	}
	
	private void makeColorCurrentFrameDisplayed(int t)
	{
		DataBufferByte dbb;
		SampleModel s;
		Raster r;
		BufferedImage bimg = null;		
		int[] bandOffsets = { 0, 1, 2 };		
		int xDim = video.getXDim();
		int yDim = video.getYDim();
		int frameDim = xDim*yDim*3;
		byte[] byteVal = new byte[frameDim];
		int shift=t*frameDim;
		for(int i =0;i<frameDim;i++)
		{
			byteVal[i]=(byte)video.getPixelByte(shift++);
		}
		dbb = new DataBufferByte(byteVal, byteVal.length);
		s = RasterFactory.createPixelInterleavedSampleModel(
			DataBuffer.TYPE_BYTE, xDim, yDim, 3, 3
				* xDim, bandOffsets);
		r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
		bimg = new BufferedImage(xDim, yDim, BufferedImage.TYPE_3BYTE_BGR);
		bimg.setData(r);
		scroll.setViewportView(new DisplayJAI(bimg));
	}

	private Timer createTimer() {
		// Listener linked to the timer
	    ActionListener action = new ActionListener ()
	      {
	        public void actionPerformed (ActionEvent event)
	        {
	        	if(frameSld.getValue()!=frameSld.getMaximum())
	        	{
	        		frameSld.setValue(frameSld.getValue()+1);
	        	}
	        	else
	        	{
	        		if(onLoop)
	        		{
	        			frameSld.setValue(1);
	        		}
	        	}
	        	
	        }
	      };
	    return new Timer ((int)Math.round(1000/frameRate), action);

	}

	void statusBarMsg(String s) {
		SwingUtilities.invokeLater(new MainFrameRunnable(this, s) {
			public void run() {
				frameVideo.statusBar.setText((String) obj);
			}
		});
	}

	class MainFrameRunnable implements Runnable {
		FrameVideo frameVideo;

		Object obj;

		/**
		 * 
		 * @param frv
		 * @param obj
		 */
		public MainFrameRunnable(FrameVideo frv, Object obj) {
			frameVideo = frv;
			this.obj = obj;
		}

		public void run() {
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==playPause)
		{
			if(playPause.getText()=="PLAY ")
			{
				playPause.setText("PAUSE");
				timer.start();				
			}
			else
			{
				timer.stop();
				playPause.setText("PLAY ");
			}
		}
		
	}

}