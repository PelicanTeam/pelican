package fr.unistra.pelican.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

import javax.media.jai.RasterFactory;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.media.jai.widget.DisplayJAI;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

/** - This class creates a JFrame for the visualisation of byte valued images (fr.unistra.pelican format).
 *  - Multiple channels and frames  are only showed one image at a time by means of sliders.
 *  - Only the case of images with exactly 3 channels and the parameter "color" set, results in color.
 *  - ThreeDimensional(x,y,z) images are shown in 3 displays : face view (XY),left view (ZY) and bottom view (XZ)
 *    ___x      ___z 
 *   |         |
 *  y|        y|
 * 
 *    ___x
 *   |
 *  z|
 *  
 *  - User can navigate(slide) through X/Y/Z/B/T axis using sliders.
 *  - While grabbing (mouseClick+mouseMove) over each display User can navigate(slide) through the two other views.
 *  - A status bar gives  information about the pixel pointed an its gray(color) byte value(s).

 * TODO
 * 1/. mise en page: forcer une largeur minimale =400px afin que les infoBarText soient affichées entièrement.
 * 2/. implémenter fonction snapShot sur bouton(ou click gauche+ menu= snap 1 ou 3 displays) ! ---> nécessite accès fileSystem... //BouloBoulo
 * 3/. tester autres formats d'image: tiff, video*, Dicom, brainWeb ...
 * 4/. tester la dimension T !!! + video*
 * 5/. factoriser  + le code ? reflechir a l'emplacement des methodes ? (normé / structure projet?):
 * 		- 3 nouvelles Méthodes dans frame3x2D : loadDisplayXY(), loadDisplayZY() et loadDisplayXZ(). + methode loadAllDisplays();
 *  	- 1 Méthode pour tester isTrheeD au chargement(constructeur) et n'afficher qu'un seul display si 3D==false.
 *  
 *  
 * @author M.Sablier
 * @version 0.9
 */

public class Frame3x2D extends JFrame
{
	private Image img;
	private boolean color;
	
	final int xdim;
	final int ydim;
	final int bdim;
	final int tdim;
	final int zdim;

	private JPanel slidersSubPanel;
	private JScrollPane scrollSubPanel;
	private JPanel infoBarSubPanel;

	private JPanel slidersBTCont;
	private JPanel slidersXYZCont;
	private JPanel displaysCont;

	private JLabel infoBar;

	private JPanel sliderBBox;
	private JPanel sliderTBox;
	private JPanel sliderXBox;
	private JPanel sliderYBox;
	private JPanel sliderZBox;
	private JPanel displayXYCont;
	private JPanel displayZYCont;
	private JPanel displayXZCont;

	private String infoBarText;
	
	private JLabel sliderBLabel;
	private JSlider sliderBSlide;
	private JLabel sliderTLabel;
	private JSlider sliderTSlide;
	private JLabel sliderXLabel;
	private JSlider sliderXSlide;
	private JLabel sliderYLabel;
	private JSlider sliderYSlide;
	private JLabel sliderZLabel;
	private JSlider sliderZSlide;
	private DisplayJAI[] displayXY;
	private DisplayJAI[] displayZY;
	private DisplayJAI[] displayXZ;
	
	private int x;
	private int y;
	private int z;
	private int t;
	private int b;
	
	private boolean SurvolDisplayXYCont;
	private boolean SurvolDisplayZYCont;
	private boolean SurvolDisplayXZCont;
	
	Point displayXYLocation;
	Point displayXYContLocation;
	Point displayZYLocation;
	Point displayZYContLocation;
	Point displayXZLocation;
	Point displayXZContLocation;
	
	public Frame3x2D(fr.unistra.pelican.Image img,String title,boolean color)
	{
		this(img,color);
		if(title != null) this.setTitle(title);
	}

	
	public Frame3x2D(fr.unistra.pelican.Image img,boolean color)
	{ 
		xdim = img.getXDim();
		ydim = img.getYDim();
		bdim = img.getBDim();
		tdim = img.getTDim();
		zdim = img.getZDim();
		this.img = img;
		final Frame3x2D ben = this;//:{
		this.color = color;

		if(this.color == true && bdim != 3){
			System.err.println("Only " + bdim + " channel" + ((bdim>1)?"s ":" ") + "found. Color visualisation cancelled");
			this.color = false;
		}

//______//DISPLAYS		
		
		//DisplayXY
		displayXY = null;
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
		DataBufferByte dbbXY;
		SampleModel sXY;
		Raster rXY;
		BufferedImage bimgXY = null;
		byte[] tmp2gray;
		byte[] tmp2color;
		if(this.color == false){
			displayXY = new DisplayJAI[bdim * tdim * zdim];
			for(int b = 0; b < bdim; b++){
				for(int t = 0; t < tdim; t++){
					for(int z = 0; z < zdim; z++){
						ByteImage tmp = img.getByteChannelZTB(z,t,b);
						tmp2gray = new byte[tmp.size()];
						for(int i = 0; i < tmp.size(); i++)
							tmp2gray[i] = (byte)tmp.getPixelByte(i);

						dbbXY = new DataBufferByte(tmp2gray,tmp.size());
						sXY = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE,tmp.getXDim(),tmp.getYDim(),1);
						rXY = RasterFactory.createWritableRaster(sXY,dbbXY,new Point(0,0));
						bimgXY = new BufferedImage(tmp.getXDim(),tmp.getYDim(),BufferedImage.TYPE_BYTE_GRAY);
						bimgXY.setData(rXY);
						displayXY[b * tdim * zdim + t * zdim + z] = new DisplayJAI(bimgXY);
					}
				}
			}
		}
		
		if(this.color == true){
			displayXY = new DisplayJAI[tdim * zdim];
			int[] bandOffsets = {0,1,2};
			for(int t = 0; t < tdim; t++){
				for(int z = 0; z < zdim; z++){
					ByteImage tmp = img.getColorByteChannelZT(z,t);
					tmp2color = new byte[tmp.size()];
					for(int i = 0; i < tmp.size(); i++)
						tmp2color[i] = (byte)tmp.getPixelByte(i);

					dbbXY = new DataBufferByte(tmp2color,tmp.size());
					sXY = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,tmp.getXDim(),tmp.getYDim(),bdim,bdim * tmp.getXDim(),bandOffsets);
					rXY = RasterFactory.createWritableRaster(sXY,dbbXY,new Point(0,0));
					bimgXY = new BufferedImage(tmp.getXDim(),tmp.getYDim(),BufferedImage.TYPE_3BYTE_BGR);
					bimgXY.setData(rXY);
					displayXY[t * zdim + z] = new DisplayJAI(bimgXY);
				}
			}
		}

		//DisplayZY
		displayZY = null;
		DataBufferByte dbbZY;
		SampleModel sZY;
		Raster rZY;
		BufferedImage bimgZY = null;
		
		if(this.color == false){
			displayZY = new DisplayJAI[bdim * tdim * xdim];
			for(int b = 0; b < bdim; b++){
				for(int t = 0; t < tdim; t++){
					for(int x = (xdim-1); x >=0 ; x--){
						ByteImage tmp = img.getByteChannelXTB(x, t, b);
						byte[] tmp2 = new byte[tmp.size()];
						for(int i = 0; i<tmp.size(); i++)
							tmp2[i] = (byte)tmp.getPixelByte(i);

						dbbZY = new DataBufferByte(tmp2,tmp.size());
						sZY = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE,tmp.getXDim(),tmp.getYDim(),1);
						rZY = RasterFactory.createWritableRaster(sZY,dbbZY,new Point(0,0));
						bimgZY = new BufferedImage(tmp.getXDim(),tmp.getYDim(),BufferedImage.TYPE_BYTE_GRAY);
						bimgZY.setData(rZY);
						displayZY[b * tdim * xdim + t * xdim + x] = new DisplayJAI(bimgZY);
					}	
				}
			}
		}
		
		if(this.color == true){
			displayZY = new DisplayJAI[tdim * xdim];
			int[] bandOffsets = {0,1,2};
			for(int t = 0; t < tdim; t++){
				for(int x = (xdim-1); x >=0 ; x--){
					ByteImage tmp = img.getColorByteChannelXT(x,t);
					byte[] tmp2 = new byte[tmp.size()];
					for(int i = 0; i < tmp.size(); i++)
						tmp2[i] = (byte)tmp.getPixelByte(i);

					dbbZY = new DataBufferByte(tmp2,tmp.size());
					sZY = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,tmp.getXDim(),tmp.getYDim(),bdim,bdim * tmp.getXDim(),bandOffsets);
					rZY = RasterFactory.createWritableRaster(sZY,dbbZY,new Point(0,0));
					bimgZY = new BufferedImage(tmp.getXDim(),tmp.getYDim(),BufferedImage.TYPE_3BYTE_BGR);
					bimgZY.setData(rZY);
					displayZY[t * xdim + x] = new DisplayJAI(bimgZY);
				}
			}
		}

		//displayXZ
		displayXZ = null;
		DataBufferByte dbbXZ;
		SampleModel sXZ;
		Raster rXZ;
		BufferedImage bimgXZ = null;
		
		if(this.color == false){
			displayXZ = new DisplayJAI[bdim * tdim * ydim];
			for(int b = 0; b < bdim; b++){
				for(int t = 0; t < tdim; t++){
					for(int y = (ydim-1); y >=0 ; y--){
						ByteImage tmp = img.getByteChannelYTB(y, t, b);
						byte[] tmp2 = new byte[tmp.size()];
						for(int i = 0; i<tmp.size(); i++)
							tmp2[i] = (byte)tmp.getPixelByte(i);

						dbbXZ = new DataBufferByte(tmp2,tmp.size());
						sXZ = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE,tmp.getXDim(),tmp.getYDim(),1);
						rXZ = RasterFactory.createWritableRaster(sXZ,dbbXZ,new Point(0,0));
						bimgXZ = new BufferedImage(tmp.getXDim(),tmp.getYDim(),BufferedImage.TYPE_BYTE_GRAY);
						bimgXZ.setData(rXZ);
						displayXZ[b * tdim * ydim + t * ydim + y] = new DisplayJAI(bimgXZ);
					}	
				}
			}
		}
		
		if(this.color == true){
			displayXZ = new DisplayJAI[tdim * ydim];
			int[] bandOffsets = {0,1,2};
			for(int t = 0; t < tdim; t++){
				for(int y = (ydim-1); y >=0 ; y--){
					ByteImage tmp = img.getColorByteChannelYT(y,t);
					byte[] tmp2 = new byte[tmp.size()];
					for(int i = 0; i < tmp.size(); i++)
						tmp2[i] = (byte)tmp.getPixelByte(i);

					dbbXZ = new DataBufferByte(tmp2,tmp.size());
					sXZ = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,tmp.getXDim(),tmp.getYDim(),bdim,bdim * tmp.getXDim(),bandOffsets);
					rXZ = RasterFactory.createWritableRaster(sXZ,dbbXZ,new Point(0,0));
					bimgXZ = new BufferedImage(tmp.getXDim(),tmp.getYDim(),BufferedImage.TYPE_3BYTE_BGR);
					bimgXZ.setData(rXZ);
					displayXZ[t * ydim + y] = new DisplayJAI(bimgXZ);
				}
			}
		}

//______//FRAME ROOT
		
		//rootPanel//
		JPanel rootPanel = new JPanel();
		rootPanel.setPreferredSize(new Dimension(   (19)+   10+(10+xdim+10)+10+(10+zdim+10)+10 ,   (19)+   (80)+   10+(10+ydim+10)+10+(10+zdim+10)+10   +(30)   ));
		//TODO:forcer une taille minimale largeur=400px afin que les infoBarSuPanelText soient affichées entièrement
		//if(rootPanel.getSize().width<400)
		//	rootPanel.setSize(400 , (19)+ (80)+ 10+(10+ydim+10)+10+(10+zdim+10)+10 + (30)   );
		rootPanel.setLayout(new BorderLayout(0,0));
		rootPanel.setBackground(java.awt.Color.red);
		this.setContentPane(rootPanel);
				
				
//______//FRAME CENTER		
	
		//rootPanel(CENTER)/ 	<-- //scrollSubPanel//
		scrollSubPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollSubPanel.setPreferredSize(new Dimension(   (19)+   10+(10+xdim+10)+10+(10+zdim+10)+10 , (19)+   10+(10+ydim+10)+10+(10+zdim+10)+10));
		scrollSubPanel.setLayout(new ScrollPaneLayout());
		scrollSubPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		scrollSubPanel.setBackground(java.awt.Color.white);
		rootPanel.add(scrollSubPanel, BorderLayout.CENTER);

		//rootPanel(CENTER)/scrollSubPanel/ 	<-- //displaysCont//
		displaysCont = new JPanel();
		displaysCont.setSize(new Dimension(10+(10+xdim+10)+10+(10+zdim+10)+10 , 10+(10+ydim+10)+10+(10+zdim+10)+10  ));
		displaysCont.setLayout(new GridBagLayout());
		displaysCont.setBackground(java.awt.Color.GRAY);
		scrollSubPanel.setViewportView(displaysCont);
		
		//rootPanel(CENTER)/scrollSubPanel/displaysCont/ 	<-- //displayXYCont//
		displayXYCont = new JPanel();
		displayXYCont.setSize(10+xdim+10, 10+ydim+10);
		displayXYCont.setLayout(new GridBagLayout());
		//survolXYCont
		MouseListener survolXYCont = new MouseAdapter() {			
			public void mouseEntered(MouseEvent e) {
				SurvolDisplayXYCont = true;
				SurvolDisplayZYCont = false;
				SurvolDisplayXZCont = false;
				displayXYLocation = displayXY[z].getLocation();
				displayXYContLocation = displayXYCont.getLocation();
			}		
			public void mouseExited (MouseEvent e) {
				SurvolDisplayXYCont = false;
			}
			
			public void mouseClicked(MouseEvent e) {
				/*takeSnapShot();
				System.out.println("clicked*snap");*/
			}	
		};
		//add
		displayXYCont.addMouseListener(survolXYCont);
		//viseurXYCont
		MouseMotionListener viseurXYCont = new MouseMotionListener() {
			
			public void mouseMoved(MouseEvent e) {
				Cursor croix = new Cursor(CROSSHAIR_CURSOR);
				displayXYCont.getComponent(0).setCursor(croix);
			}
			public void mouseDragged(MouseEvent e) {
				if(SurvolDisplayXYCont==true){
					x=  (int)e.getX() - (int)displayXYLocation.getX();
					y=  (int)e.getY() - (int)displayXYLocation.getY();
					if(x>=xdim)x=xdim-1;
					if(x<0)x=0;
					if(y>=ydim)y=ydim-1;
					if(y<0)y=0;
					updateDisplayZY();
					updateDisplayXZ();
					updateSlidersSlide();
				}
			}
		};
		//add
		displayXYCont.addMouseMotionListener(viseurXYCont);
		displaysCont.add(displayXYCont, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		
		
		//rootPanel(CENTER)/scrollSubPanel/displaysCont/ 	<-- //displayZYCont//		
		displayZYCont= new JPanel();
		displayZYCont.setSize(10+zdim+10, 10+ydim+10);
		displayZYCont.setLayout(new GridBagLayout());
		//survolZYCont
		MouseListener survolZYCont = new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				SurvolDisplayXYCont = false;
				SurvolDisplayZYCont = true;
				SurvolDisplayXZCont = false;
				displayZYLocation = displayZY[0].getLocation();
				displayZYContLocation = displayZYCont.getLocation();
			}
			public void mouseExited (MouseEvent e) {
				SurvolDisplayZYCont = false;
			}
			public void mouseClicked(MouseEvent e){
			}
		};
		//add
		displayZYCont.addMouseListener(survolZYCont);
		//viseurZYCont
		MouseMotionListener viseurZYCont = new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {
				Cursor croix = new Cursor(CROSSHAIR_CURSOR);
				displayZYCont.getComponent(0).setCursor(croix);
			}
			public void mouseDragged(MouseEvent e) {
				if(SurvolDisplayZYCont==true){
					z=  (int)e.getX() - (int)displayZYLocation.getX();
					y=  (int)e.getY() - (int)displayZYLocation.getY();
					if(z>=zdim)z=zdim-1;
					if(z<0)z=0;
					if(y>=ydim)y=ydim-1;
					if(y<0)y=0;
					updateDisplayXY();
					updateDisplayXZ();
					updateSlidersSlide();
				}
			}		
		};
		//add
		displayZYCont.addMouseMotionListener(viseurZYCont);
		displaysCont.add(displayZYCont,new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		
		
		//rootPanel(CENTER)/scrollSubPanel/displaysCont/ 	<-- //displayXZCont//	
		displayXZCont = new JPanel();
		displayXZCont.setSize(10+xdim+10, 10+zdim+10);
		displayXZCont.setLayout(new GridBagLayout());	
		//survolXZCont
		MouseListener survolXZCont = new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				SurvolDisplayXYCont = false;
				SurvolDisplayZYCont = false;
				SurvolDisplayXZCont = true;
				displayXZLocation = displayXZ[0].getLocation();
				displayXZContLocation = displayXZCont.getLocation();
			}
			public void mouseExited (MouseEvent e) {
				SurvolDisplayXZCont = false;
			}
			public void mouseClicked(MouseEvent e){
			}
		};
		//add
		displayXZCont.addMouseListener(survolXZCont);
		//viseurXZCont
		MouseMotionListener viseurXZCont = new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {
				Cursor croix = new Cursor(CROSSHAIR_CURSOR);
				displayXZCont.getComponent(0).setCursor(croix);
			}
			public void mouseDragged(MouseEvent e) {
				if(SurvolDisplayXZCont==true){
					x=  (int)e.getX() - (int)displayXZLocation.getX();
					z=  (int)e.getY() - (int)displayXZLocation.getY();
					if(x>=xdim)x=xdim-1;
					if(x<0)x=0;
					if(z>=zdim)z=zdim-1;
					if(z<0)z=0;
					updateDisplayXY();
					updateDisplayZY();
					updateSlidersSlide();
				}
			}		
		};
		//add
		displayXZCont.addMouseMotionListener(viseurXZCont);	
		displaysCont.add(displayXZCont,new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));

		
		//rootPanel(CENTER)/scrollSubPanel/displaysCont/display**Cont/	 <-- //display**//	
		displayXYCont.add(displayXY[0], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		displayZYCont.add(displayZY[0], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		displayXZCont.add(displayXZ[0], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
			

//______//FRAME	NORTH
		
		
		//rootPanel(NORTH)/ 	<-- //slidersSubPanel//
		slidersSubPanel = new JPanel();
		slidersSubPanel.setPreferredSize(new Dimension(10+(10+xdim+10)+10+(10+zdim+10)+10 , 80));
		slidersSubPanel.setLayout(new GridLayout(1, 2, 15, 0));
		slidersSubPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		rootPanel.add(slidersSubPanel,BorderLayout.NORTH);

		
		//rootPanel(NORTH)/slidersSubPanel/	 <-- //sliders**Cont//
		slidersBTCont = new JPanel();
		slidersBTCont.setLayout(new GridLayout(3,1,0,10));
		slidersSubPanel.add(slidersBTCont,0);
		slidersXYZCont = new JPanel();
		slidersXYZCont.setLayout(new GridLayout(3,1,0,10));		
		slidersSubPanel.add(slidersXYZCont,1);
		
//B		//rootPanel(NORTH)/slidersSubPanel/slidersBTCont/		<-- //sliderBBox//
		sliderBBox = new JPanel();
		sliderBBox.setLayout((new BoxLayout(sliderBBox,BoxLayout.X_AXIS)));
		//label
		sliderBLabel = new JLabel(" Channel : 1/" + bdim + "     ");
		sliderBBox.add(sliderBLabel);
		//slide
		sliderBSlide = new JSlider(SwingConstants.HORIZONTAL,1,bdim,1);
		sliderBSlide.setSnapToTicks(true);
		sliderBBox.add(sliderBSlide);
		//listner
		sliderBSlide.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				sliderBLabel.setText(" Channel : " + Integer.toString(sliderBSlide.getValue()) + "/" + bdim + "     ");
				b = sliderBSlide.getValue() - 1;
				updateDisplayXY();
				updateDisplayZY();
				updateDisplayXZ();
				updateInfoBarText();
			}		 
		});
		slidersBTCont.add(sliderBBox);
		if(bdim == 1 || color == true){
			sliderBLabel.setEnabled(false);
			sliderBSlide.setEnabled(false);
		}

		
//T		//rootPanel(NORTH)/slidersSubPanel/slidersBTCont/		<-- //sliderTBox//
		sliderTBox = new JPanel();
		sliderTBox.setLayout(new BoxLayout(sliderTBox,BoxLayout.X_AXIS));
		//label
		sliderTLabel = new JLabel(" Frame : 1/" + tdim + "     ");
		sliderTBox.add(sliderTLabel);
		//slide
		sliderTSlide = new JSlider(SwingConstants.HORIZONTAL,1,tdim,1);
		sliderTSlide.setSnapToTicks(true);
		sliderTBox.add(sliderTSlide);
		//listner
		sliderTSlide.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				sliderTLabel.setText(" Frame : " + Integer.toString(sliderTSlide.getValue()) + "/" + tdim + "     ");
				t = sliderTSlide.getValue() - 1;
				updateDisplayXY();
				updateDisplayZY();
				updateDisplayXZ();
				updateInfoBarText();
			}
		});
		slidersBTCont.add(sliderTBox);
		if(tdim == 1){
			sliderTLabel.setEnabled(false);
			sliderTSlide.setEnabled(false);
		}

//X		//rootPanel(NORTH)/slidersSubPanel/slidersXYZCont/		<-- //sliderXBox//
		sliderXBox = new JPanel();
		sliderXBox.setLayout((new BoxLayout(sliderXBox,BoxLayout.X_AXIS)));
		//label
		sliderXLabel = new JLabel(" Xpos : 1/" + xdim + "     ");
		sliderXBox.add(sliderXLabel);
		//slide
		sliderXSlide = new JSlider(SwingConstants.HORIZONTAL,1,xdim,1);
		sliderXSlide.setSnapToTicks(true);
		sliderXBox.add(sliderXSlide);
		//listner
		sliderXSlide.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				sliderXLabel.setText(" Xpos : " + Integer.toString(sliderXSlide.getValue()) + "/" + xdim + "     ");
				x = sliderXSlide.getValue() - 1;
				updateDisplayZY();
				updateInfoBarText();
			}		 
		});
		slidersXYZCont.add(sliderXBox);
		if(xdim == 1){
			sliderXLabel.setEnabled(false);
			sliderXSlide.setEnabled(false);
		}

		
//Y		//rootPanel(NORTH)/slidersSubPanel/slidersXYZCont/		<-- //sliderYBox//
		sliderYBox = new JPanel();
		sliderYBox.setLayout((new BoxLayout(sliderYBox,BoxLayout.X_AXIS)));
		//label
		sliderYLabel = new JLabel(" Ypos : 1/" + ydim + "     ");
		sliderYBox.add(sliderYLabel);
		//slide
		sliderYSlide = new JSlider(SwingConstants.HORIZONTAL,1,ydim,1);
		sliderYSlide.setSnapToTicks(true);
		sliderYBox.add(sliderYSlide);
		//listner
		sliderYSlide.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				sliderYLabel.setText(" Ypos : " + Integer.toString(sliderYSlide.getValue()) + "/" + ydim + "     ");
				y = sliderYSlide.getValue() - 1;
				updateDisplayXZ();
				updateInfoBarText();
			}		 
		});
		slidersXYZCont.add(sliderYBox);
		if(ydim == 1){
			sliderYLabel.setEnabled(false);
			sliderYSlide.setEnabled(false);
		}

		
//Z		//rootPanel(NORTH)/slidersSubPanel/slidersXYZCont/		<-- //sliderZBox//
		sliderZBox = new JPanel();
		sliderZBox.setLayout((new BoxLayout(sliderZBox,BoxLayout.X_AXIS)));
		//label
		sliderZLabel = new JLabel(" Zpos : 1/" + zdim + "     ");
		sliderZBox.add(sliderZLabel);
		//slide
		sliderZSlide = new JSlider(SwingConstants.HORIZONTAL,1,zdim,1);
		sliderZSlide.setSnapToTicks(true);
		sliderZBox.add(sliderZSlide);	
		//listner
		sliderZSlide.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				sliderZLabel.setText(" Zpos : " + Integer.toString(sliderZSlide.getValue()) + "/" + zdim + "     ");
				z = sliderZSlide.getValue() - 1;
				updateDisplayXY();
				updateInfoBarText();
			}		 
		});
		slidersXYZCont.add(sliderZBox);
		if(zdim == 1){
			sliderZLabel.setEnabled(false);
			sliderZSlide.setEnabled(false);
		}
		
//______//FRAME SOUTH
		
		
		//rootPanel(SOUTH)/	 <-- //statusSubPanel//	
		infoBarSubPanel = new JPanel();
		infoBarSubPanel.setPreferredSize(new Dimension(xdim>zdim ?	xdim+xdim:zdim+zdim , 30));
		//infoBarSubPanel.setMinimumSize(new Dimension(400 , 30));
		infoBarSubPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		infoBarSubPanel.setLayout(new GridLayout(1,2,0,0));
		infoBar = new JLabel(infoBarText);
		infoBarSubPanel.add(infoBar);
		rootPanel.add(infoBarSubPanel,BorderLayout.SOUTH);
		
		
//______//PACKING
		
		pack();
		setVisible(true);
	}
	
	
	public void updateInfoBarText(){
		if(color==false){
			infoBarText = "   position : " + "x:" + (x+1) + " / y:" + (y+1) + " / z:" + (z+1) + " / t:" + t  + " / b:" + b +"    " +
			 			  "value : Gray:"  + img.getPixelByte( x, y, z, t, b);
		}
		if(color==true){
			infoBarText = "   position : "+"x:"+ (x+1) + " / y:" + (y+1) + " / z:" + (z+1) + " / t:" + t + "    " + 
						  " value : R:"+img.getPixelByte( x, y, z, t, 0)+
						  		" / G:" + img.getPixelByte( x, y, z, t, 1) + 
						  		" / B:" + img.getPixelByte( x, y, z, t, 2);
		}
		infoBar.setText(infoBarText);
	}

	
	public void updateDisplayXY(){
		displayXYCont.removeAll();
		if(color==false)
			displayXYCont.add(displayXY[b * tdim * zdim + t * zdim + z], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		if(color==true)
			displayXYCont.add(displayXY[t * zdim + z], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		displayXYCont.repaint();
	}
	
	
	public void updateDisplayZY(){
		displayZYCont.removeAll();
		if(color==false)
			displayZYCont.add(displayZY[b * tdim * xdim + t * xdim + x], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		if(color==true)
			displayZYCont.add(displayZY[t * xdim + x], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		displayZYCont.repaint();
	}
	
	
	public void updateDisplayXZ(){
		displayXZCont.removeAll();
		if(color==false)
			displayXZCont.add(displayXZ[b * tdim * ydim + t * ydim + y], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		if(color==true)
			displayXZCont.add(displayXZ[t * ydim + y], new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,10,10), 0, 0));
		displayXZCont.repaint();
	}
	
	
	public void updateSlidersSlide(){
		sliderBSlide.setValue(b+1);
		sliderTSlide.setValue(t+1);
		sliderXSlide.setValue(x+1);
		sliderYSlide.setValue(y+1);
		sliderZSlide.setValue(z+1);
		updateInfoBarText();
	}
	
/*	// SUPPRIMER DECLARATION : METHODE DESORMAIS INUTILE
	public void ResetAllDisplays(){
		scrollSubPanel.repaint();
	}
*/	
	
/*	TODO: debogguer
 * public void takeSnapShot(){
		if(color==false){
		ByteImage tmp = new ByteImage(x,y,1,1,1);
		tmp = img.getByteChannelZTB(z,t,b);
		PelicanImageSave.process(tmp, "output/slideXY.pelican");	
		}
*/	
	
}
