/**
 * 
 */
package fr.unistra.pelican.gui.MultiViews;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.histogram.HistogramCorrection;
import fr.unistra.pelican.algorithms.histogram.HistogramCorrection.MultiBandPolicy;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.util.PelicanImageToBufferedImage;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.colour.GammaCompressionModel;
import fr.unistra.pelican.util.colour.REC709GammaCompressionModel;
import fr.unistra.pelican.util.colour.SRGBGammaCompressionModel;
import fr.unistra.pelican.util.colour.SimpleGammaCompressionModel;
import fr.unistra.pelican.util.colour.GammaCompressionModel.Band;
import fr.unistra.pelican.util.mask.MaskStack;

/**
 * How to display an image (zoom, translation, histogram correction, pixel scaling, colour composition, ...)
 * 
 * @author Benjamin Perret
 *
 */
public class View {

	
	/**
	 * Image is currently displayed
	 */
	private boolean active=false;
	
	/**
	 * Zoom factor on image
	 */
	private double zoom=1.0;
	
	/**
	 * X Shift of origin (zoom factor does NOT apply on it)
	 */
	private int shiftX=0;
	
	/**
	 * Y Shift of origin (zoom factor does NOT apply on it)
	 */
	private int shiftY=0;
	
	/**
	 * Use auto histogram correction
	 */
	private boolean autoCorrect=true;
	
	/**
	 * ThresholdLevel for auto correction
	 */
	private double autoCorrectLevel=0.99;
	
	/**
	 * Multiband policy for autoCorrect
	 */
	private MultiBandPolicy multiBandPolicyCorrection=MultiBandPolicy.Independent;
	
	/**
	 * Scale histogram to [0;1] (band independent)
	 */
	private boolean scaleResult=true;
	
	/**
	 * Show in inverse gray scale
	 */
	private boolean inverseGrayScale=false;
	
	/**
	 * One buffered image per band
	 */
	private BufferedImage [] myimg = null;
	
	/**
	 * In color band only one buffered image
	 */
	private BufferedImage colourImg = null;
	
	
	/** 
	 * the image to display 
	 */
	private BufferedImage display = null;

	/**
	 * Reference to the original image
	 */
	private Image oriImage =null;
	
	/**
	 * Internal copy (insure coherence of display if original image is modified, so changes are not reflected until refresh is called)
	 */
	private Image copyImage =null;

	/**
	 * Try to follow Viewport object (if exists)
	 */
	private boolean followWindow=true;
	
	/**
	 * Autofit in Viewport object (if exists)
	 */
	private boolean autoFitWindow=false;
	
	/**
	 * Is the original image a Pelican Image
	 */
	private boolean originIsPelicanImage=true;
	
	
	/**
	 * Use color?
	 */
	private boolean coloured=false;
	
	/**
	 * Is not color, which band do we need to view
	 */
	private int displayedBand=0;
	
	/**
	 * In color band which band is used for red
	 */
	private int colourBandR=0;
	
	/**
	 * In color band which band is used for green
	 */
	private int colourBandG=1;
	
	/**
	 * In color band which band is used for blue
	 */
	private int colourBandB=2;
	
	/**
	 * Do we apply threshold
	 */
	private boolean threshold=false;
	
	/**
	 * Threshold value for each band
	 */
	private double thresholdValue [];
	
	
	public static enum GammaModel {No,Simple,sRGB,REC709};
	
	private GammaModel gammaModel=GammaModel.No;
	
	private GammaCompressionModel gammaCompressionModel=null;
	
	/**
	 * Free to use!
	 */
	public Map<String,Object> properties = new TreeMap<String, Object>();
	
	/**
	 * Observator view port
	 */
	private ViewPort viewPort;
	
	public View(ViewPort viewPort)
	{
		this.viewPort=viewPort;
	}
	
	/**
	 * 
	 * @return the zoom
	 */
	public double getZoom() {
		return zoom;
	}

	/**
	 * @param zoom the zoom to set
	 */
	public void setZoom(double zoom) {
		if(this.zoom !=zoom )fireChangeEvent();
		this.zoom = zoom;
		
	}

	/**
	 * @return the shiftX
	 */
	public int getShiftX() {
		return shiftX;
	}

	/**
	 * @param shiftX the shiftX to set
	 */
	public void setShiftX(int shiftX) {
		if(shiftX!=this.shiftX)fireChangeEvent();
		this.shiftX = shiftX;
		
	}

	/**
	 * @return the shiftY
	 */
	public int getShiftY() {
		return shiftY;
	}

	/**
	 * @param shiftY the shiftY to set
	 */
	public void setShiftY(int shiftY) {
		if(shiftY!=this.shiftY)fireChangeEvent();
		this.shiftY = shiftY;
		
	}

	/**
	 * @return the autoCorrect
	 */
	public boolean isAutoCorrect() {
		return autoCorrect;
	}

	/**
	 * @param autoCorrect the autoCorrect to set
	 */
	public void setAutoCorrect(boolean autoCorrect) {
		this.autoCorrect = autoCorrect;
		clearTempImg();
		setPreProcessing();
	}

	

	/**
	 * @return the autoCorrectLevel
	 */
	public double getAutoCorrectLevel() {
		return autoCorrectLevel;
	}

	/**
	 * @param autoCorrectLevel the autoCorrectLevel to set
	 */
	public void setAutoCorrectLevel(double autoCorrectLevel) {
		
		this.autoCorrectLevel = Math.min(1.0,Math.max(0.0,autoCorrectLevel));
		clearTempImg();
		setPreProcessing();
	}

	/**
	 * @param myimg the myimg to set
	 */
	public void setImage(BufferedImage myimg) {
		this.colourImg = myimg;
		originIsPelicanImage=false;
		copyImage=oriImage=bufferedImageTopelicanImage(colourImg);
		checkBandsForColour();
		if (copyImage.bdim==3)
		{
			coloured=true;
		}
		this.myimg=new BufferedImage[oriImage.bdim];
		thresholdValue = new double[oriImage.bdim];
		setPreProcessing();
	}

	/**
	 * @return the oriImage
	 */
	public Image getImage() {
		return oriImage;
	}
	
	/**
	 * @return the oriImage
	 */
	public Image getPersistentImage() {
		return copyImage;
	}

	/**
	 * @param oriImage the oriImage to set
	 */
	public void setImage(Image image) {
		
		this.oriImage = image;
		if (copyImage != null && image != null
				&& Image.haveSameDimensions(copyImage, image)) {
			copyImage.setMask(image.getMask());
			for (int i = 0; i < image.size(); i++)
				copyImage.setPixelDouble(i, image.getPixelDouble(i));
		}

		else {
			copyImage = image.copyImage(true);
			}
		createMaskForImage(copyImage);
		originIsPelicanImage=true;
		thresholdValue = new double[oriImage.bdim];
		myimg=new BufferedImage[oriImage.bdim];
		checkBandsForColour();
		if (image.bdim==3)
		{
			coloured=true;
		}
		setPreProcessing();
		
	}

	private void createMaskForImage(Image im)
	{
		if (im instanceof DoubleImage) {
			boolean flag = true;
			for (int i = 0; i < im.size() && flag; i++)
				if (!Tools.isValue(im.getPixelDouble(i)))
					flag = false;
			if (!flag) {
				BooleanImage mask = new BooleanImage(im, false);
				for (int i = 0; i < mask.size(); i++)
					if (Tools.isValue(im.getPixelDouble(i)))
						mask.setPixelBoolean(i, true);
				im.pushMask(mask);
			}
		}
		
	}
	
	public void refresh()
	{
		if(originIsPelicanImage)
			setImage(oriImage);
		else setImage(colourImg);
		fireChangeEvent();
	}
	
	private Image performThreshold(Image im)
	{
		Image res=new BooleanImage(im.xdim,im.ydim,1,1,im.bdim);
		for(int b=0;b<im.bdim;b++)
			for(int y=0;y<im.ydim;y++)
				for(int x=0;x<im.xdim;x++)
					res.setPixelXYBBoolean(x, y, b, im.getPixelXYBDouble(x, y, b)>= thresholdValue[b]);
		return res;
	}
	
	
	private Image temp;
	
	private void setPreProcessing() {
		Image op;
		if(threshold)
		{
			op=performThreshold(copyImage);
			
		}else {
			op=copyImage;
		}
		if (!threshold && autoCorrect) {
			/*
			 * if(myimgCorrected==null) myimgCorrected =
			 * VariousTools.pelicanImageToBufferedImage(HistogramCorrection.exec(oriImage));
			 * 
			 * 
			 * display=myimgCorrected;
			 */
			temp = HistogramCorrection.exec(op,autoCorrectLevel,HistogramCorrection.STRETCH_NOT_USE,multiBandPolicyCorrection);
		} else {
			/*
			 * if(myimg==null) myimg =
			 * VariousTools.pelicanImageToBufferedImage((scaleResult)?scaleToZeroOne(oriImage):oriImage);
			 * display=myimg;
			 */
			temp = (scaleResult) ? scaleToZeroOne(op) : op;

		}
		if (gammaCompressionModel!=null)
			for (int i = 0; i < temp.size(); i++)
				temp.setPixelDouble(i, gammaCompressionModel.compress(temp.getPixelDouble(i), Band.UNKNOWN));
		if (inverseGrayScale) {
			for (int i = 0; i < temp.size(); i++)
				temp.setPixelDouble(i, 1.0 - temp.getPixelDouble(i));
		}
	
		setDisplay();
	}
	
	private void setDisplay() {
		if(temp==null)
			setPreProcessing();
		if (coloured)
			display = colourImg = PelicanImageToBufferedImage.exec(temp, 
					colourBandR, colourBandG, colourBandB);
		else{
			if(myimg[displayedBand] == null)
				myimg[displayedBand] = PelicanImageToBufferedImage.exec(
						temp, displayedBand);
			display = myimg[displayedBand];
		}
			
	}
	
	
	private Image scaleToZeroOne(Image im)
	{
		Image res = im.copyImage(false);
		for(int b=0;b<im.bdim;b++)
		{
		
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for (int i = b; i < im.size(); i+=im.bdim)
				if(im.isPresent(i)) {
					double val = im.getPixelDouble(i);
					
					if (val < min)
						min = val;
					if (val > max)
						max = val;

				}
			for(int i=b;i<im.size();i+=im.bdim)
				if(im.isPresent(i))
					res.setPixelDouble(i, (im.getPixelDouble(i)-min)/(max-min));
		}
		return res;
	}
	
	public Rectangle areaCovered()
	{
		int xdim=viewPort.getWidth();
		int ydim=viewPort.getHeight();
		int xmin = getAbsoluteXCoord(0);
		int ymin = getAbsoluteYCoord(0);;
		int xmax = getAbsoluteXCoord(xdim)+1;
		int ymax = getAbsoluteYCoord(ydim)+1;
		/*if(shiftX>=0)
		{
			xmax=(int)Math.min((((double)(xdim+shiftX))/zoom),display.getWidth());
		}else {
			xmin=(int)(-shiftX/zoom);
			xmax=(int)Math.min(((double)(xdim-shiftX))/zoom, display.getWidth());
		}
		if(shiftY>=0)
		{
			ymax=(int)Math.min((((double)(ydim-shiftY))/zoom),display.getHeight());
		}else {
			ymin=(int)(-shiftY/zoom);
			ymax=(int)Math.min(((double)(ydim-shiftY))/zoom, display.getHeight());
		}*/

		return new Rectangle(xmin,ymin,xmax-xmin,ymax-ymin);
	}
	
	public void resetView()
	{
		this.zoom=1.0;
		this.shiftX=0;
		this.shiftY=0;
		fireChangeEvent();
	}
	
	public boolean isActive(){
		return active;
	}
	
	public void zoomOn(int x, int y, double coef)
	{
		if(coef!=1.0)
		{
		double orix=((double)x-(double)shiftX)/zoom;
		double oriy=((double)y-(double)shiftY)/zoom;
		
		zoom*=coef;
		
		shiftX=(int)(x-zoom*orix);
		shiftY=(int)(y-zoom*oriy);
		fireChangeEvent();}
	}
	
	public void zoomOut(int x, int y, double coef)
	{
		zoomOn(x, y, 1.0/coef);
	}
	
	public void shiftX(int s)
	{
		shiftX+=s;
		if(s!=0) fireChangeEvent();
	}
	
	public void shiftY(int s)
	{
		shiftY+=s;
		if(s!=0) fireChangeEvent();
	}
	
	public void fitToWindow()
	{
		
		zoom=Math.min(((double)viewPort.getWidth())/((double)oriImage.getXDim()), ((double)viewPort.getHeight())/((double)oriImage.getYDim()));
		centerToWindow();
	}
	
	public void centerToWindow()
	{
		centerXToWindow();
		centerYToWindow();
	}
	
	public double viewSizeX(){
		return zoom*(double)oriImage.getXDim();
	}
	
	public double viewSizeY(){
		return zoom*(double)oriImage.getYDim();
		
	}
	
	public void centerXToWindow()
	{	
		int shiftXc=(int)(((double)viewPort.getWidth()-viewSizeX())/2.0);	
		if(shiftXc!=shiftX)
		{
			shiftX=shiftXc;
			fireChangeEvent();
		}
	}
	
	public void centerYToWindow()
	{
		int shiftYc=(int)(((double)viewPort.getHeight()-viewSizeY())/2.0);
		if(shiftYc!=shiftY)
		{
			shiftY=shiftYc;
			fireChangeEvent();
		}
	}
	
	public void noBlanckY()
	{
		int shiftYc=(int)Math.max(Math.min(0,shiftY),viewPort.getHeight()-viewSizeY());
		if(shiftYc!=shiftY)
		{
			shiftY=shiftYc;
			fireChangeEvent();
		}
	}
	
	public void noBlanckX()
	{
		int shiftXc=(int)Math.max(Math.min(0,shiftX),viewPort.getWidth()-viewSizeX());
		if(shiftXc!=shiftX)
		{
			shiftX=shiftXc;
			fireChangeEvent();
		}
	}
	
	
	public int getAbsoluteXCoord(int xMousePosition)
	{
		return (int)Math.max(Math.min((((double)xMousePosition-(double)shiftX)/zoom),oriImage.xdim-1),0);
	}
	
	public int getRelativeXCoord(int xImagePosition)
	{
		return (int)(((double)xImagePosition)*zoom+(double)shiftX);
	}
	
	public int getRelativeYCoord(int yImagePosition)
	{
		return (int)(((double)yImagePosition)*zoom+(double)shiftY);
	}
	
	public int getAbsoluteYCoord(int yMousePosition)
	{
		return (int)Math.max(Math.min((((double)yMousePosition-(double)shiftY)/zoom),oriImage.ydim-1),0);
	}
	
	public double [] getPixelValue(int x, int y)
	{
		 double [] res=null;
		x=getAbsoluteXCoord(x);
		y=getAbsoluteYCoord(y);
		res=oriImage.getVectorPixelXYZTDouble(x, y, 0, 0);
	
		return res;
	}
	
	/**
	 * @return the display
	 */
	public BufferedImage getDisplay() {
	
		
		
		
		return display;
	}

	/**
	 * @return the scaleResult
	 */
	public boolean isScaleResult() {
		return scaleResult;
	}

	private void clearTempImg()
	{
		if(myimg != null)
			for(int i=0;i<myimg.length;i++)
				myimg[i]=null;
	}
	
	/**
	 * @param scaleResult the scaleResult to set
	 */
	public void setScaleResult(boolean scaleResult) {
		
		if(this.scaleResult != scaleResult)
		{
			
			this.scaleResult = scaleResult;		
			clearTempImg();
			setPreProcessing();
			fireChangeEvent();
		}
		
	}

	/**
	 * @return the followWindow
	 */
	public boolean isFollowWindow() {
		return followWindow;
	}

	/**
	 * @param followWindow the followWindow to set
	 */
	public void setFollowWindow(boolean followWindow) {
		this.followWindow = followWindow;
		if(followWindow!=this.followWindow)
			fireChangeEvent();
		
	}

	/**
	 * @return the autoFitWindow
	 */
	public boolean isAutoFitWindow() {
		
		return autoFitWindow;
	}

	/**
	 * @param autoFitWindow the autoFitWindow to set
	 */
	public void setAutoFitWindow(boolean autoFitWindow) {
		this.autoFitWindow = autoFitWindow;
		if(autoFitWindow!=this.autoFitWindow)
			fireChangeEvent();
		
	}
	
	/**
	 * Does not throw change event !!!!
	 * @param v
	 */
	public void copyAttribute(View v)
	{
		zoom=v.zoom;
		shiftX=v.shiftX;
		shiftY=v.shiftY;
		//autoCorrect=v.autoCorrect;
		autoFitWindow=v.autoFitWindow;
		//scaleResult=v.scaleResult;
		followWindow=v.followWindow;
		setDisplayedBand(v.displayedBand);
		//setAutoCorrectLevel(v.getAutoCorrectLevel());
		fireChangeEvent();
		
	}
	
	public ViewPort getViewPort() {
		return viewPort;
	}

	public void setViewPort(ViewPort viewPort) {
		this.viewPort = viewPort;
	}

	public void setActive(boolean active) {
		this.active = active;
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
	
	public boolean isRegistredChangeListener(ChangeListener cl)
	{
		return listeners.contains(cl);
	}
	
	public void fireChangeEvent()
	{
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener cl:listeners)
			cl.stateChanged(e);
	}

	/**
	 * @return the inverseGrayScale
	 */
	public boolean isInverseGrayScale() {
		
		return inverseGrayScale;
	}

	/**
	 * @param inverseGrayScale the inverseGrayScale to set
	 */
	public void setInverseGrayScale(boolean inverseGrayScale) {
		this.inverseGrayScale = inverseGrayScale;
		clearTempImg();
		setPreProcessing();
	}

	public boolean isColoured() {
		return coloured;
	}

	private void checkBandsForColour()
	{
		if(colourBandR <0 || colourBandR >= copyImage.bdim)
			colourBandR=0;
		if(colourBandG <0 || colourBandG >= copyImage.bdim)
			colourBandG=(copyImage.bdim>1)?1:0;
		if(colourBandB <0 || colourBandB >= copyImage.bdim)
			colourBandB=(copyImage.bdim>2)?2:0;
			
	}
	
	public void setColoured(boolean coloured) {
		if(coloured != this.coloured)
		{
			this.coloured = coloured;
			setDisplay();
			fireChangeEvent();
		}
		
	}

	public int getDisplayedBand() {
		return displayedBand;
	}

	public void setDisplayedBand(int displayedBand) {
		displayedBand=Math.min(Math.max(0,displayedBand),copyImage.bdim-1);
		if(displayedBand != this.displayedBand)
		{
			this.displayedBand = displayedBand;
			setDisplay();
			fireChangeEvent();
		}
		
	}

	public int getColourBandR() {
		return colourBandR;
	}

	public void setColourBandR(int coulourBandR) {
		if(this.colourBandR != coulourBandR)
		{
			this.colourBandR = coulourBandR;
			checkBandsForColour();
			setDisplay();
			fireChangeEvent();
		}
		
	}

	public int getColourBandG() {
		return colourBandG;
	}

	public void setColourBandG(int coulourBandG) {
		if(this.colourBandG != coulourBandG)
		{
			this.colourBandG = coulourBandG;
			checkBandsForColour();
			setDisplay();
			fireChangeEvent();
		}
	}

	public int getColourBandB() {
		return colourBandB;
	}

	public void setColourBandB(int coulourBandB) {
		if(this.colourBandB != coulourBandB)
		{
			this.colourBandB = coulourBandB;
			checkBandsForColour();
			setDisplay();
			fireChangeEvent();
		}
	}

	public boolean isThreshold() {
		return threshold;
	}

	public void setThreshold(boolean threshold) {
		if(threshold != this.threshold)
		{
			this.threshold = threshold;
			
			clearTempImg();
			setPreProcessing();
			fireChangeEvent();
		}
		
	}

	public double getThresholdValue(int band) {
		return thresholdValue[band];
	}

	public void setThresholdValue(int band, double thresholdValue) {
		
		if(thresholdValue != this.thresholdValue[band])
		{
			this.thresholdValue[band] = thresholdValue;;
			clearTempImg();
			setPreProcessing();
			fireChangeEvent();
		}
	}
	
	/**
	 * Convert BufferedImage image to pelican ByteImage
	 * @param img
	 * @return
	 */
	public static Image bufferedImageTopelicanImage(BufferedImage img) {

		/*PixelGrabber pixelGrabber = new PixelGrabber(img, 0, 0, 320, 240, true);
		pixelGrabber.setDimensions(320, 240);

		try {
			while (!pixelGrabber.grabPixels())
				;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		//int[] data = (int[]) pixelGrabber.getPixels();

		
		int type=img.getType();
		int nbbande=3;
		if(type == BufferedImage.TYPE_BYTE_BINARY || BufferedImage.TYPE_BYTE_GRAY==type || type == BufferedImage.TYPE_BYTE_INDEXED || type ==BufferedImage.TYPE_USHORT_GRAY )
			nbbande=1;
			
		ByteImage imgPiaf = new ByteImage(img.getWidth(), img.getHeight(), 1, 1, nbbande);
		
		Raster r =img.getData();
		//int [] data = r.getSamples(0, 0, img.getWidth(), img.getHeight(), 3, (int[])null);
		
		
		if(nbbande==1)
		{
			for (int i = 0; i < imgPiaf.getXDim(); i++) {
				for (int j = 0; j < imgPiaf.getYDim(); j++) {
					imgPiaf.setPixelXYBByte(i, j, 0,r.getSample(i, j, 0));
				
				}
			}
		}else{
			
		int [] data = img.getRGB(0, 0, img.getWidth(), img.getHeight(), (int[])null	, 0, img.getWidth());
		for (int i = 0; i < imgPiaf.getXDim(); i++) {
			for (int j = 0; j < imgPiaf.getYDim(); j++) {
				
				imgPiaf.setPixelXYBByte(i, j, 0,(data[j* imgPiaf.xdim + i] >> 16) & 0xff);
				imgPiaf.setPixelXYBByte(i, j, 1,(data[j* imgPiaf.xdim + i] >>  8) & 0xff);
				imgPiaf.setPixelXYBByte(i, j, 2,(data[j* imgPiaf.xdim + i]      ) & 0xff);
	
		        /* old way
				imgPiaf.setPixelXYBByte(i, j, 0, new Color(data[j
						* imgPiaf.getXDim() + i]).getRed());
				imgPiaf.setPixelXYBByte(i, j, 1, new Color(data[j
						* imgPiaf.getXDim() + i]).getGreen());
				imgPiaf.setPixelXYBByte(i, j, 2, new Color(data[j
						* imgPiaf.getXDim() + i]).getBlue());
						* */
			}
		}
		}
		return imgPiaf;
	}

	/**
	 * @return the multiBandPolicyCorrection
	 */
	public MultiBandPolicy getMultiBandPolicyCorrection() {
		return multiBandPolicyCorrection;
	}

	/**
	 * @param multiBandPolicyCorrection the multiBandPolicyCorrection to set
	 */
	public void setMultiBandPolicyCorrection(
			MultiBandPolicy multiBandPolicyCorrection) {
		if(multiBandPolicyCorrection != this.multiBandPolicyCorrection)
		{
			this.multiBandPolicyCorrection = multiBandPolicyCorrection;
			clearTempImg();
			setPreProcessing();
			fireChangeEvent();
		}
		
	}

	/**
	 * @return the gammaModel
	 */
	public GammaModel getGammaModel() {
		return gammaModel;
	}

	/**
	 * @param gammaModel the gammaModel to set
	 */
	public void setGammaModel(GammaModel gammaModel) {
		if(gammaModel!=this.gammaModel)
		{
			this.gammaModel = gammaModel;
			switch (gammaModel)
			{
				case No:
					gammaCompressionModel=null;
					break;
				case Simple:
					gammaCompressionModel=new SimpleGammaCompressionModel();
					break;
				case sRGB:
					gammaCompressionModel=new SRGBGammaCompressionModel();
					break;
				case REC709:
					gammaCompressionModel=new REC709GammaCompressionModel();
					break;
			}
			
			clearTempImg();
			setPreProcessing();
			fireChangeEvent();
		}
		
	}

	
	
	
}
