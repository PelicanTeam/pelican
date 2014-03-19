package fr.unistra.pelican.gui.MultiViews;
 
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.histogram.HistogramCorrection.MultiBandPolicy;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.io.JavaImageSave;
import fr.unistra.pelican.gui.FileChooserToolBox;
import fr.unistra.pelican.gui.MultiViews.View.GammaModel;
import fr.unistra.pelican.util.ArrayToolbox;
import fr.unistra.pelican.util.IMath;
import fr.unistra.pelican.util.Tools;

/**
 * A JPanel especially designed for image display. 
 * It displays an image according to a View object and proposes popup menu for controlling options of the view.
 * 
 * @author Benjamin Perret
 */
public class ImagePanel extends JPanel implements ChangeListener, MouseListener, MouseWheelListener, MouseMotionListener, ActionListener, ViewPort{
	
	
	private DecimalFormat df=new DecimalFormat("##.#####");
	
	private String [] decimalFormats = new String[]{"#","00","#.##","0.##","0.00","#.####E0"};
	
	private JComboBox decimalFormatComboBox = new JComboBox(decimalFormats);
	
	private View view;

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Ready to fight?
	 */
	boolean init=false;
	
	int lastWidth=getWidth();
	int lastHeight=getHeight();
	
	/**
	 * The menu
	 */
	private JPopupMenu popup;
	private JMenuItem sendToClipBoard;
	private JMenuItem save;
	private JMenuItem save2;
	private JMenuItem fitInWindow;
	private JMenuItem restoreView;
	private JMenuItem refreshView;
	private JMenuItem pixelsStat;
	private JMenuItem changeDecimalFormat;
	private JCheckBoxMenuItem autoCorrect;
	
	private JTextFieldMenuItem autoCorrectLevel;
	private JCheckBoxMenuItem autoScale;
	private JCheckBoxMenuItem inverseGrayScale;
	private JMenuItem colourOptions;
	private JCheckBoxMenuItem lockFit;
	private JCheckBoxMenuItem center;
	
	private  JMenu subMenuCorrection=new JMenu("MultiBand correction");
    private JRadioButtonMenuItem correctIndep = new JRadioButtonMenuItem("Independent");
    private JRadioButtonMenuItem correctMin = new JRadioButtonMenuItem("Min");
    private JRadioButtonMenuItem correctMax = new JRadioButtonMenuItem("Max");
    private JRadioButtonMenuItem correctMedian = new JRadioButtonMenuItem("Median");
    private JRadioButtonMenuItem correctMean = new JRadioButtonMenuItem("Mean");
    
    private JMenu subMenuGammaCompression=new JMenu("Gamma Compression");
    private JRadioButtonMenuItem gammaNo = new JRadioButtonMenuItem("No Compression");
    private JRadioButtonMenuItem gammaSimple = new JRadioButtonMenuItem("Simple");
    private JRadioButtonMenuItem gammaSRGB = new JRadioButtonMenuItem("sRGB");
    private JRadioButtonMenuItem gammaREC709 = new JRadioButtonMenuItem("REC.709");

	
	/**
	 * To change colour display options
	 */
	private ColourOptionEditor coe = null;
	
	/**
	 * Builder
	 */
	public ImagePanel() {
		setLayout(null);
		setSize(400, 400);
		DecimalFormatSymbols dfs=df.getDecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(dfs);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		
		createPopUp();
	}

	
	private void createPopUp()
	{
		 popup = new JPopupMenu();
		 pixelsStat = new JMenuItem("Show Pixels Information");
		 pixelsStat.addActionListener(this);
		 popup.add( pixelsStat);
		 
		 
		 refreshView = new JMenuItem("Refresh");
		 refreshView.addActionListener(this);
		 popup.add( refreshView);
		 
		 

		 center = new JCheckBoxMenuItem("Follow Window");
		 center.setSelected(true);
		 center.addActionListener(this);
		 popup.add(center);
		 
		 fitInWindow = new JMenuItem("Fit In Window");
		 fitInWindow.addActionListener(this);
		 popup.add(fitInWindow);
		 
		 lockFit= new JCheckBoxMenuItem("Lock fit in window");
		 lockFit.setSelected(false);
		 lockFit.addActionListener(this);
		 popup.add(lockFit);
		 
		 
		 popup.add(new JPopupMenu.Separator());
		 
		 decimalFormatComboBox.setEditable(true);
		 changeDecimalFormat = new JMenuItem("Decimal Format " + df.toPattern());
		 changeDecimalFormat.addActionListener(this);
		 popup.add( changeDecimalFormat);
		 
		 popup.add(new JPopupMenu.Separator());
		 
		 autoScale = new JCheckBoxMenuItem("Contrast scaling");
		 autoScale.setSelected(true);
		 autoScale.addActionListener(this);
		 autoScale.setEnabled(false);
		 popup.add(autoScale);
		 
		
		 
		 autoCorrect = new JCheckBoxMenuItem("Contrast correction");
		 autoCorrect.setSelected(true);
		 autoCorrect.addActionListener(this);
		 popup.add(autoCorrect);
		 
		 //autoCorrectSubMenu=new JMenu("Correction options");
		// popup.add(autoCorrectSubMenu);
		 
		 autoCorrectLevel = new JTextFieldMenuItem("Threshold","99");
		 autoCorrectLevel.addActionListener(this);
		 popup.add(autoCorrectLevel);
		 
		 
		
		 
		 ButtonGroup myGroup = new ButtonGroup();
		 myGroup.add(correctIndep);
		 myGroup.add(correctMin);
		 myGroup.add(correctMax);
		 myGroup.add(correctMedian);
		 myGroup.add(correctMean);
		 correctIndep.setSelected(true);
		 subMenuCorrection.add(correctIndep);
		 correctIndep.addActionListener(this);
		 subMenuCorrection.add(correctMin);
		 correctMin.addActionListener(this);
		 subMenuCorrection.add(correctMax);
		 correctMax.addActionListener(this);
		 subMenuCorrection.add(correctMedian);
		 correctMedian.addActionListener(this);
		 subMenuCorrection.add(correctMean);
		 correctMean.addActionListener(this);
		 popup.add(subMenuCorrection);
		 
		 popup.add(new JPopupMenu.Separator());
		 
		 ButtonGroup myGroup2 = new ButtonGroup();
		 myGroup2.add(gammaNo);
		 myGroup2.add(gammaSimple);
		 myGroup2.add(gammaSRGB);
		 myGroup2.add(gammaREC709);
	
		 gammaNo.setSelected(true);
		 subMenuGammaCompression.add(gammaNo);
		 gammaNo.addActionListener(this);
		 subMenuGammaCompression.add(gammaSimple);
		 gammaSimple.addActionListener(this);
		 subMenuGammaCompression.add(gammaSRGB);
		 gammaSRGB.addActionListener(this);
		 subMenuGammaCompression.add(gammaREC709);
		 gammaREC709.addActionListener(this);
		 popup.add(subMenuGammaCompression);
		 
		 popup.add(new JPopupMenu.Separator());
		 
		 inverseGrayScale = new JCheckBoxMenuItem("Inverse Gray Scale");
		 inverseGrayScale.setSelected(false);
		 inverseGrayScale.addActionListener(this);
		 inverseGrayScale.setEnabled(true);
		 popup.add(inverseGrayScale);
		 
		 colourOptions = new JMenuItem("Colour propeties");
		 colourOptions.addActionListener(this);
		 popup.add(colourOptions);
		 
		 restoreView = new JMenuItem("Restore View");
		 restoreView.addActionListener(this);
		 popup.add(restoreView);
		 popup.add(new JPopupMenu.Separator());
		 
		 sendToClipBoard = new JMenuItem("Send View to Clipboard");
		 sendToClipBoard.addActionListener(this);
		 popup.add(sendToClipBoard);
		 
		 save = new JMenuItem("Save Original Image");
		 save.addActionListener(this);
		 popup.add(save);
		 save2 = new JMenuItem("Save View");
		 save2.addActionListener(this);
		 popup.add(save2);

	}
	/**
	 * Set the image and/or the rectangle to display
	 * @param img
	 * @param r
	 */
	public void setImage(Image img) {
		if (img == null) {
			setView(null);
		} else {
			if (view == null)
				view = new View(this);
			view.setImage(img);
			setView(view);
		}
		repaint();
	}
	
	
	/**
	 * Set the image and/or the rectangle to display
	 * @param img
	 * @param r
	 */
	public View setImage(BufferedImage img) {
		if (img == null) {
			setView(null);
		} else {
			if (view == null)
				view = new View(this);
			view.setImage(img);
			setView(view);
			repaint();
		}
		return view;
	}
	

	public void setView(View view) {
		if (this.view != null) {// this.view.setViewPort(null);
			this.view.removeChangeListener(this);
			this.view.setActive(false);
		}
		this.view = view;

		if (view != null) {
			view.addChangeListener(this);
			view.setViewPort(this);
			this.view.setActive(true);
			autoScale.setSelected(view.isScaleResult());
			autoCorrect.setSelected(view.isAutoCorrect());
			if (autoCorrect.isSelected())
				autoScale.setEnabled(false);
			else
				autoScale.setEnabled(true);
			// autoCorrectSubMenu.setEnabled(autoCorrect.isSelected());
			inverseGrayScale.setSelected(view.isInverseGrayScale());

			switch(view.getMultiBandPolicyCorrection())
			{
			case Independent:
				correctIndep.setSelected(true);
				break;
			case Min:
				correctMin.setSelected(true);
				break;
			case Max:
				correctMax.setSelected(true);
				break;
			case Mean:
				correctMean.setSelected(true);
				break;
			case Median:
				correctMedian.setSelected(true);
				break;
			}
			
			switch(view.getGammaModel())
			{
			case No:
				gammaNo.setSelected(true);
				break;
			case Simple:
				gammaSimple.setSelected(true);
				break;
			case sRGB:
				gammaSRGB.setSelected(true);
				break;
			case REC709:
				gammaREC709.setSelected(true);
				break;
			
			}
			
			lockFit.setSelected(view.isAutoFitWindow());
			center.setSelected(view.isFollowWindow());
			autoCorrectLevel.setText("" + view.getAutoCorrectLevel() * 100.0);
		}
		repaint();
	}
	
	private void drawHasch(Graphics2D g,int h, int w,Color c, double slope, double space)
	{
		double x1=0;
		double x2=0;
		double y1=0;
		double y2=0;
		for(x1=0;x1<=w;x1+=space)
		{
			
		}
	}
	
	@Override
	public void paint(Graphics g) {
		int w=getWidth();
		int h=getHeight();
		
		g.clearRect(0,0,w,h);
		//g.fillRect(0, 0, w, h);
	//	g.setColor(Color.black);
		if (view != null) {
			if (lockFit.isSelected())
				view.fitToWindow();
			BufferedImage myimg = view.getDisplay();
			if (myimg != null) {

				int xdim = myimg.getWidth(null);
				int ydim = myimg.getHeight(null);

				int finalx = (int) (view.getZoom() * xdim);
				int finaly = (int) (view.getZoom() * ydim);
				if (center.isSelected()) {
					if (init) {
						/*
						 * if (lastHeight < h && lastWidth < w) { double f1 =
						 * (double) (h) / (double) (lastHeight); double f2 =
						 * (double) (w) / (double) (lastWidth); view.zoomOn(w /
						 * 2, h / 2, Math.min(f1, f2));
						 * 
						 * } else if (lastHeight > h && lastWidth > w) { double
						 * f1 = (double) (h) / (double) (lastHeight); double f2
						 * = (double) (w) / (double) (lastWidth); view.zoomOn(w
						 * / 2, h / 2, Math.max(f1, f2));
						 * 
						 * }
						 */
					}/*
					 * else if(lastHeight!= h && lastWidth== w ) { double
					 * f1=(double)(h)/(double)(lastHeight);
					 * 
					 * view.zoomOn(w/2, h/2, f1);
					 * 
					 * }else if(lastHeight== h && lastWidth!= w ) { double
					 * f2=(double)(w)/(double)(lastWidth);
					 * 
					 * view.zoomOn(w/2, h/2, f2); }
					 */

					if (finalx < w)
						view.centerXToWindow();
					else
						view.noBlanckX();
					if (finaly < h)
						view.centerYToWindow();
					else
						view.noBlanckY();
				}
				// double fact=Math.min((double)getWidth()/(double)xdim,
				// (double)getHeight()/(double)ydim);
				// Graphics2D g2 = (Graphics2D)g;
				// AffineTransform xform =
				// AffineTransform.getScaleInstance(fact,
				// fact);
				// g2.drawImage(myimg, xform,null);
				// java.awt.Image op = myimg.getScaledInstance((int)(zoom*xdim),
				// (int)(zoom*ydim), java.awt.Image.SCALE_FAST);
				// g2.drawImage(myimg, 0, 0,(int)(fact*xdim),(int)(fact*ydim),
				// this);
				// g.drawImage(op,shiftX,shiftY,null);
				/*if (myimg.getTransparency() == BufferedImage.TRANSLUCENT) {
					g.setColor(Color.black);
					g.fillRect(view.getShiftX(), view.getShiftY(), finalx,
							finaly);

				}*/
				g.drawImage(myimg, view.getShiftX(), view.getShiftY(), finalx,
						finaly, null);
			}
			lastHeight = h;
			lastWidth = w;
			init = true;
		}
		fireChangeEvent();
	}
	
	
	public static ImagePanel createFrame(String title)
	{
		JFrame frame = new JFrame();
		frame.setTitle(title);
		ImagePanel pan= new ImagePanel();
		frame.add(pan);
		frame.setVisible(true);
		frame.setSize(400,400);
		
		return pan;
	}

	
	public void mouseClicked(MouseEvent e) {
		 maybeShowPopup(e);

		
		
	}

	
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	private boolean hold1=false;
	
	public void mousePressed(MouseEvent e) {
		 maybeShowPopup(e);

		if(e.getModifiers() == MouseEvent.BUTTON1_MASK)
		{
			px=e.getX();
			py=e.getY();
			hold1=true;
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) );
		}
		
	}

	
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
		if(e.getModifiers() == MouseEvent.BUTTON1_MASK)
		{
			setCursor(Cursor.getDefaultCursor() );
			hold1=false;
		}
		
	}

	
	public void mouseWheelMoved(MouseWheelEvent e) {
		//int onMask= MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK;
		
		double tick = e.getWheelRotation();
		if(Math.abs(tick)>0)
		{
			if (view!=null && e.isAltDown())//(e.getModifiersEx() & onMask ) == onMask)
			{


				double l=view.getAutoCorrectLevel();
				// why not?
				view.setAutoCorrectLevel(l+(1.001-l)*tick*tick*Math.signum(tick)/10.0);
				view.refresh();
			}
			else if (view!=null && !lockFit.isSelected()) {



				if (tick < 0)
					view.zoomOn(e.getX(), e.getY(), tick *= -1.2);
				else
					view.zoomOut(e.getX(), e.getY(), tick *= 1.2);

				//repaint();
			}
		}
	}

	
	public void mouseDragged(MouseEvent e) {
		
		if(view!=null && !lockFit.isSelected() && hold1)
		{
			
			int ppx,ppy;
			ppx=e.getX();
			ppy=e.getY();
			
			view.shiftX((ppx-px));
			view.shiftY((ppy-py));
			px=ppx;
			py=ppy;
			//repaint();
		}
		
	}

	private int px,py;
	
	
	public void mouseMoved(MouseEvent e) {
		if (view != null) {
			int x = view.getAbsoluteXCoord(e.getX());
			int y = view.getAbsoluteYCoord(e.getY());
			Image im=view.getPersistentImage();
			String masked=(im.isPresentXY(x, y))?"":"M";
			if (im instanceof IntegerImage) {
				int[] pix = im.getVectorPixelXYZTInt(x, y, 0, 0);
				this.setToolTipText(masked + "[" + x + ";" + y + "]->"
						+ ArrayToolbox.printString(pix));
			}
			else if (im instanceof ByteImage) {
				int[] pix = im.getVectorPixelXYZTByte(x, y, 0, 0);
				this.setToolTipText(masked +"[" + x + ";" + y + "]->"
						+ ArrayToolbox.printString(pix));
			} else {
				double[] pix = im.getVectorPixelXYZTDouble(x, y,
						0, 0);
				this.setToolTipText(masked +"[" + x + ";" + y + "]->"
						+ ArrayToolbox.printString(pix, df));
			}
		}
	}
	
	
	
	public static void main(String [] args)
	{
		//Image im=ImageLoader.exec("samples/lenna512.png");
		Image im=ImageLoader.exec("samples/AstronomicalImagesFITS/img1-10.fits");
		JFrame frame = new JFrame("Mouhaha");
		ImagePanel panel=new ImagePanel();
		panel.setImage(im);
		frame.add(panel);
		frame.setSize(400, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	private void maybeShowPopup(MouseEvent e) {
		
        if (popup.isPopupTrigger(e)) {
        	 
            popup.show(e.getComponent(),
                       e.getX(), e.getY());
            autoCorrectLevel.setText(""+df.format(view.getAutoCorrectLevel()));
            Window window = SwingUtilities.windowForComponent(popup);
            
            if (window != null)
            {
            	window.setFocusableWindowState(true);
            }
           
            popup.setFocusable(true);
           
            popup.requestFocus();
            //KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(popup);
            //textField.requestFocusInWindow();
        }
    }

	private void save(Image im, File f){
		try{
			ImageSave.exec(im,f.getAbsolutePath());
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Error while saving file : " +e, "VisuTool error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		
		Object source=e.getSource();
		if(source instanceof JMenuItem)
		{
			JMenuItem item=(JMenuItem)e.getSource();
			if(item==save)
			{
				File f = FileChooserToolBox.openSaveFileChooser((JFrame)this.getRootPane().getParent());
				if(f!=null)save(view.getPersistentImage(),f);
			}else if(item==save2)
			{
				File f = FileChooserToolBox.openSaveFileChooser((JFrame)this.getRootPane().getParent());
				// this is the funny conversion game between JavaImage->BufferedImage->PelicanImage
				if(f!=null){
					int x=view.getImage().xdim;
					int y=view.getImage().ydim;
					save(View.bufferedImageTopelicanImage(JavaImageSave.toBufferedImage(view.getDisplay().getScaledInstance((int)(view.getZoom()*x), (int)(view.getZoom()*y), BufferedImage.SCALE_REPLICATE))),f);
				}
			}else if(item==sendToClipBoard)
			{
				
				// this is the funny conversion game between JavaImage->BufferedImage->PelicanImage
				int x=view.getImage().xdim;
				int y=view.getImage().ydim;
				ClipBoardImageTranserferable.copyImageToClipboard(JavaImageSave.toBufferedImage(view.getDisplay().getScaledInstance((int)(view.getZoom()*x), (int)(view.getZoom()*y), BufferedImage.SCALE_REPLICATE)));
			
			}
			else if (item== restoreView)
			{
				view.resetView();
				//	repaint();
			}else if (item== autoCorrect)
			{
				view.setAutoCorrect(autoCorrect.isSelected());
				if(autoCorrect.isSelected())
				{
					subMenuCorrection.setEnabled(true);
					autoScale.setEnabled(false);
					autoScale.setSelected(true);
					autoCorrectLevel.setEnabled(true);
				} else {
					subMenuCorrection.setEnabled(false);
					autoScale.setEnabled(true);
					autoCorrectLevel.setEnabled(false);
				}

				repaint();
			}else if (item== correctIndep)
			{
				if(correctIndep.isSelected())
				{
					view.setMultiBandPolicyCorrection(MultiBandPolicy.Independent);
					repaint();
				}
				
			}else if (item== correctMean)
			{
				if(correctMean.isSelected())
				{
					view.setMultiBandPolicyCorrection(MultiBandPolicy.Mean);
					repaint();
				}
				
			}else if (item== correctMax)
			{
				if(correctMax.isSelected())
				{
					view.setMultiBandPolicyCorrection(MultiBandPolicy.Max);
					repaint();
				}
				
			}else if (item== correctMin)
			{
				if(correctMin.isSelected())
				{
					view.setMultiBandPolicyCorrection(MultiBandPolicy.Min);
					repaint();
				}
				
			}else if (item== correctMedian)
			{
				if(correctMedian.isSelected())
				{
					view.setMultiBandPolicyCorrection(MultiBandPolicy.Median);
					repaint();
				}
				
			}
			else if (item== gammaNo)
			{
				if(gammaNo.isSelected())
				{
					view.setGammaModel(GammaModel.No);
					repaint();
				}
				
			}else if (item== gammaSimple)
			{
				if(gammaSimple.isSelected())
				{
					view.setGammaModel(GammaModel.Simple);
					repaint();
				}
				
			}else if (item== gammaSRGB)
			{
				if(gammaSRGB.isSelected())
				{
					view.setGammaModel(GammaModel.sRGB);
					repaint();
				}
				
			}else if (item== gammaREC709)
			{
				if(gammaREC709.isSelected())
				{
					view.setGammaModel(GammaModel.REC709);
					repaint();
				}
				
			}else if (item== autoScale)
			{

				view.setScaleResult(autoScale.isSelected());
				repaint();
			}else if (item== inverseGrayScale)
			{

				view.setInverseGrayScale(inverseGrayScale.isSelected());
				repaint();
			}else if (item== fitInWindow)
			{
				view.fitToWindow();
				//repaint();
			}else if (item== lockFit)
			{
				view.setAutoFitWindow(lockFit.isSelected());
				//repaint();
			}else if (item== center)
			{
				view.setFollowWindow(center.isSelected());
				//repaint();
			}else if (item== lockFit)
			{
				view.setAutoFitWindow(lockFit.isSelected());
				//repaint();
			}else if (item== refreshView)
			{
				view.refresh();
				//repaint();
			} else if (item== pixelsStat)
			{
				JOptionPane.showMessageDialog(this, IMath.printStatistics(view.getPersistentImage()));

			} else if (item == colourOptions)
			{	if(coe==null)
				coe=new ColourOptionEditor();
			coe.setVisible(true);
			}else if (item== changeDecimalFormat)
			{
				//decimalFormatComboBox.setSelectedIndex(0);
				JOptionPane.showMessageDialog(this,decimalFormatComboBox, "Give decimal format (Java DecimalFormat pattern) : ",JOptionPane.QUESTION_MESSAGE);
				Object res=decimalFormatComboBox.getSelectedItem();
				
				if(res!=null && res instanceof String){
					try{
						df.applyPattern((String)res);
						changeDecimalFormat.setText("Decimal Format " + df.toPattern());
					} catch (IllegalArgumentException ex){
						JOptionPane.showMessageDialog(this,"Invalid decimal pattern, see javadoc DecimalFormat!");
					}
					
				}
			}
		} else if (source instanceof JTextField)
		{
			JTextField text =(JTextField)(source);
			try {
				double v=Double.parseDouble(text.getText());
				if(v<0.0 || v>1.0)
				{
					JOptionPane.showMessageDialog(this,"Value must be between 0 and 1!");
					text.setText(""+df.format(view.getAutoCorrectLevel()));
				} else {
					view.setAutoCorrectLevel(v);
					repaint();
				}
			} catch (NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(this,"Hey dude! give me a number between 0 and 1!");
				text.setText(""+view.getAutoCorrectLevel());
			}
		}
		
	}


	/**
	 * @return the view
	 */
	public View getView() {
		return view;
	}
	
	public void addPopUpOption(String optionName,JMenuItem ...item)
	{
		JMenu menu = new JMenu(optionName);
		for(JMenuItem i:item)
			menu.add(i);
		popup.add(new JPopupMenu.Separator());
		popup.add(menu);
		
	}
	
	class ColourOptionEditor extends JFrame implements ChangeListener, ActionListener{
		
		private JCheckBox coloured=new JCheckBox("Colour display");
		private JPanel main;
		private JPanel bandSelectorPanel;
		private JPanel colourSelectorPanel;
		private JPanel thresholdPanel;
		private JSlider slider=new JSlider(SwingConstants.HORIZONTAL,0,0,0);
		
		private JComboBox colourR=new JComboBox();
		private JComboBox colourG=new JComboBox();
		private JComboBox colourB=new JComboBox();
		
		private JCheckBox threshold = new JCheckBox("Threshold");
		private JComboBox thresholdBand=new JComboBox();
		private int thresholdMin=0;
		private int thresholdMax=1000;
		private int thresholdInitValue=(thresholdMax-thresholdMin)/2+thresholdMin;
		private double thresholdRange=thresholdMax-thresholdMin;
		private JSlider thresholdSlider=new JSlider(SwingConstants.HORIZONTAL,thresholdMin,thresholdMax,thresholdInitValue);
	
		private JLabel thresholdMinLabel=new JLabel("xxxxxxxxxxx");
		private JLabel thresholdMaxLabel=new JLabel("xxxxxxxxxxx");
		private double imageMin=0.0;
		private double imageMax=1.0;
		private ImagePanel histo;
		private int histoBins=255;
		private JTextField histoBinsField= new JTextField(5);
		private double [] histogram;
		private double [] histogramTicks;
		private JCheckBox histoLogX=new JCheckBox("Log Scale X axis");
		private JCheckBox histoLogY=new JCheckBox("Log Scale Y axis");
		public ColourOptionEditor(){
			main = new JPanel(new MigLayout());
			
			coloured.addChangeListener(this);
			bandSelectorPanel=getBandSelectorPanel();
			colourSelectorPanel=getColourSelectorPanel();
			thresholdPanel=getThresholdPanel();
			main.add(thresholdPanel,"wrap");
			main.add(coloured,"wrap");
			
			this.add(main);
			this.setTitle("Colour options");
			this.setResizable(false);
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
		}
		
		private JPanel getBandSelectorPanel(){
			JPanel pan=new JPanel(new MigLayout());
			pan.setBorder(BorderFactory.createTitledBorder("Band selection"));
			pan.add(new JLabel("Band number: "));
			
			slider.setMajorTickSpacing(1);
			slider.setMinorTickSpacing(1);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.addChangeListener(this);
			slider.setSnapToTicks(true);
			pan.add(slider,"wrap");
			return pan;
		}
		
		private JPanel getThresholdPanel(){
			JPanel pan=new JPanel(new MigLayout());
			
			histo=new ImagePanel();
			histo.setPreferredSize(new Dimension(600,300));
			
			pan.setBorder(BorderFactory.createTitledBorder("Threshold tool"));
			pan.add(threshold);
			pan.add(new JLabel("Band:"));
			pan.add(thresholdBand);
			thresholdBand.addActionListener(this);
			thresholdSlider.setEnabled(false);
			threshold.setSelected(false);
			Hashtable labelTable = new Hashtable();
			labelTable.put( thresholdMin, thresholdMinLabel );
			labelTable.put( thresholdMax, thresholdMaxLabel );
			thresholdSlider.setLabelTable( labelTable );
			//thresholdSlider.setMajorTickSpacing(1);
			//thresholdSlider.setMinorTickSpacing(1);
			thresholdSlider.setPaintTicks(false);
			thresholdSlider.setPaintLabels(true);
			thresholdSlider.addChangeListener(this);
			
			//thresholdSlider.setSnapToTicks(true);
			threshold.addActionListener(this);
			thresholdSlider.setPreferredSize(new Dimension(600,30));
			pan.add(thresholdSlider,"wrap");
			pan.add(histo,"span");
			pan.add(new JLabel("Bins: "));
			pan.add(histoBinsField);
			pan.add(histoLogX);
			pan.add(histoLogY);
			histoLogX.addActionListener(this);
			histoLogY.addActionListener(this);
			histoBinsField.setText(""+histoBins);
			histoBinsField.addActionListener(this);
			histoBinsField.setInputVerifier(new InputVerifier(){

				@Override
				public boolean verify(JComponent input) {
					 JTextField tf = (JTextField) input;
			 	      try{
			 	    	  Integer.parseInt(tf.getText());
			 	      }catch (NumberFormatException e)
			 	      {
			 	    	 return false; 
			 	      }

					return true;
				}
				
			});
			return pan;
		}
		
		private JPanel getColourSelectorPanel(){
			JPanel pan=new JPanel(new MigLayout());
			pan.setBorder(BorderFactory.createTitledBorder("Colour selection"));
			
			
			pan.add(new JLabel("Red Band: "));
			pan.add(colourR,"wrap");
			colourR.setEditable(false);
			colourR.addActionListener(this);
			
			
			pan.add(new JLabel("Green Band: "));
			pan.add(colourG,"wrap");
			colourG.setEditable(false);
			colourG.addActionListener(this);
			
			pan.add(new JLabel("Blue Band: "));
			pan.add(colourB,"wrap");
			colourB.setEditable(false);
			colourB.addActionListener(this);
			
			return pan;
		}
		
		private void setSliderProp(){
			Image im = view.getPersistentImage();
			Object o = thresholdBand.getSelectedItem();
			if (o != null) {
				int band = (Integer) o;
				double[] op = IMath.getMinMax(im, band);
				imageMin = op[0];
				imageMax = op[1];
				thresholdMinLabel.setText(Tools.df.format(imageMin));
				thresholdMaxLabel.setText(Tools.df.format(imageMax));
				thresholdSlider.repaint();
				// System.out.println("min " + imageMin + " max " +imageMax);
				thresholdSlider.setValue(fromDoubleToSlider(view
						.getThresholdValue(band)));
				//computeHistogram(band);
				setHisto(band);
			}
			
		}
		
		private void setDisplay()
		{
			
			if(coloured.isSelected())
			{
				//main.remove(thresholdPanel);
				main.remove(bandSelectorPanel);
				main.add(colourSelectorPanel);
			} else {
				main.remove(colourSelectorPanel);
				main.add(bandSelectorPanel,"wrap");
				//main.add(thresholdPanel);
				
			}
			this.pack();
			//this.setSize(400, 400);
		}
		
		private void  computeHistogram(int b) {
			if (histogram==null || histogram.length!=histoBins){
				histogram = new double[histoBins];
				histogramTicks=new double[histoBins];
			}
			else for(int i=0;i<histogram.length;i++)
				histogram[i]=0;
			double binSize = (double) (imageMax - imageMin) / (double) histoBins;
			Image input=view.getPersistentImage();
			for(int i=0;i<histoBins;i++)
				histogramTicks[i]=binSize*i+imageMin;
			for (int j = 0; j < input.getYDim(); j++)
				for (int i = 0; i < input.getXDim(); i++) {
				double val = input.getPixelXYBDouble(i,j,b);
				/*
				 * Min max are performed to avoid approximation errors
				 */
				int bi=Math.max(0, Math.min((int) ((val-imageMin) / binSize), histoBins - 1));
				//System.out.println("Val:" +(int) ((val-min) / binSize) + "  Bin: " +bi);
				histogram[bi]++;
			}
		}
		
		private void setHisto(int band)
		{
		    HistogramDataset dataset = new HistogramDataset();
	        Image im=view.getPersistentImage();
		    double[] value=new double[im.getNumberOfPresentPixel(band)];
		    if(histoLogX.isSelected()){
		    	for(int i=band,a=0;i<im.size();i+=im.bdim)
		    		if(im.isPresent(i))
			    	value[a++]=Math.log(im.getPixelDouble(i));}
		    else{
		    	for(int i=band,a=0;i<im.size();i+=im.bdim)
		    		if(im.isPresent(i))
			    	value[a++]=im.getPixelDouble(i);
		    }
	        dataset.addSeries("H", value, histoBins);

		    JFreeChart chart = ChartFactory.createHistogram("Histogram","Pixel value","Number of pixels",dataset,PlotOrientation.VERTICAL,false,false,false);
		    if(histoLogY.isSelected())
		    {
		    	XYPlot plot = chart.getXYPlot();
		    	//final NumberAxis domainAxis = new NumberAxis("x");
		        LogarithmicAxis rangeAxis = new LogarithmicAxis("Log(y)");
		        rangeAxis.setAllowNegativesFlag(true);
		        
		       // plot.setDomainAxis(domainAxis);
		        plot.setRangeAxis(rangeAxis);
		    }
		    
		    View v=histo.setImage(chart.createBufferedImage(histo.getWidth(), histo.getHeight()));
		    v.setAutoCorrect(false);
		    view.setScaleResult(false);
		    view.setAutoFitWindow(true);
		  
		
		}
		
		private int fromDoubleToSlider(double d)
		{
			return (int)((d-imageMin)/(imageMax-imageMin)*(thresholdMax-thresholdMin) + thresholdMin);
		}
		
		private double fromSliderToDouble(int i)
		{
			double v=i;
			return ((v-(double)thresholdMin)/(double)(thresholdRange)*(double)(imageMax-imageMin) + (double)imageMin);
		}
		
		private void setViewProperties()
		{
			coloured.setSelected(view.isColoured());
			slider.setMaximum(view.getPersistentImage().bdim-1);
			slider.setValue(view.getDisplayedBand());
			
			//bandList = new Integer[view.getPersistentImage().bdim];
			colourR.removeAllItems();
			colourG.removeAllItems();
			colourB.removeAllItems();
			thresholdBand.removeAllItems();
			colourR.removeActionListener(this);
			colourG.removeActionListener(this);
			colourB.removeActionListener(this);
			thresholdBand.removeActionListener(this);
			for(int b=0;b<view.getPersistentImage().bdim;b++)
			{
				colourR.addItem(new Integer(b));
				colourG.addItem(new Integer(b));
				colourB.addItem(new Integer(b));
				thresholdBand.addItem(new Integer(b));
			}
			
			
			colourR.setSelectedIndex(view.getColourBandR());		
			colourG.setSelectedIndex(view.getColourBandG());
			colourB.setSelectedIndex(view.getColourBandB());
			thresholdBand.setSelectedIndex(view.getDisplayedBand());
			colourR.addActionListener(this);
			colourG.addActionListener(this);
			colourB.addActionListener(this);
			thresholdBand.addActionListener(this);
			setSliderProp();
		}
		
		
		
		
		public void  setVisible(boolean flag)
		{
			super.setVisible(flag);
			if(flag==true){
				setDisplay();	
				setViewProperties();
			}
					
		}

		private boolean noInfiniteLoopEventsHistoBandSlider=true;
		private int lastSliderValue=-1;
		
		@Override
		public void stateChanged(ChangeEvent e) {
			Object source=e.getSource();
			if(source == coloured)
			{
				view.setColoured(coloured.isSelected());
				setDisplay();
			} else if (source == slider) {
				if (lastSliderValue != slider.getValue()) {
					
					lastSliderValue = slider.getValue();
					view.setDisplayedBand(slider.getValue());
					if (noInfiniteLoopEventsHistoBandSlider) {
						int a = slider.getValue();
						if (a >= 0 && a < thresholdBand.getItemCount()) {
							noInfiniteLoopEventsHistoBandSlider = false;
							thresholdBand.setSelectedIndex(a);
						}
					} else {
						noInfiniteLoopEventsHistoBandSlider = true;
					}

					
				}
				// setSliderProp();
			}else if(source == thresholdSlider)
			{
				double v=fromSliderToDouble(thresholdSlider.getValue());
				thresholdSlider.setToolTipText(""+v);
				//System.out.println("val " +v);
				view.setThresholdValue((Integer)thresholdBand.getSelectedItem(),v);
			}
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source=e.getSource();
			if(view!=null){
			if(source == colourR )
			{
				Object o=colourR.getSelectedItem();
				if(o!=null)
					view.setColourBandR((Integer)o);
				
			}
			else if(source == colourG)
			{
				Object o=colourG.getSelectedItem();
				if(o!=null)
					view.setColourBandG((Integer)o);
				
			}
			else if(source == colourB)
			{
				Object o=colourB.getSelectedItem();
				if(o!=null)
					view.setColourBandB((Integer)o);
				
			}
			else if (source==threshold)
			{
				thresholdSlider.setEnabled(threshold.isSelected());
				view.setThreshold(threshold.isSelected());
			} else if (source == thresholdBand) {
				
				if (noInfiniteLoopEventsHistoBandSlider)  {
						int a = thresholdBand.getSelectedIndex();
						if (a >= 0 && a <= slider.getMaximum()) {
							noInfiniteLoopEventsHistoBandSlider  = false;
							slider.setValue(a);
						}
				} else {noInfiniteLoopEventsHistoBandSlider=true;}
						setSliderProp();
					
				
			}else if (source==histoBinsField)
			{
				try{
					int a=Integer.parseInt(histoBinsField.getText());
					histoBins=a;
				}catch(NumberFormatException ef)
				{
					histoBinsField.setText(histoBins+"");
				}
				
				setSliderProp();
			}else if (source==histoLogX || source == histoLogY)
			{
				setSliderProp();
			}
			
		}}
		
	}
	
	
	/* ***********************************
	 * Change event thrower
	 */
	
	private ArrayList<ChangeListener> listeners=new ArrayList<ChangeListener>();
	
	public void addChangeListener(ChangeListener cl)
	{
		listeners.add(cl);
	}
	
	public void removeChangeListener(ChangeListener cl)
	{
		listeners.remove(cl);
	}
	
	public void fireChangeEvent()
	{
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener cl:listeners)
			cl.stateChanged(e);
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() instanceof View)
			repaint();
		
			
		
	}





	
}



