package fr.unistra.pelican.algorithms.applied.astronomical;

import java.awt.Point;
import java.util.LinkedList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.arithmetic.Maximum;
//import fr.unistra.pelican.algorithms.experimental.abdullah.ConvexHull;
import fr.unistra.pelican.algorithms.histogram.Histogram;
import fr.unistra.pelican.algorithms.io.MultipleImageLoad;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryInternGradient;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.BinaryFillHole;
import fr.unistra.pelican.algorithms.morphology.binary.hitormiss.BinaryConvexHull;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;
import fr.unistra.pelican.algorithms.morphology.gray.GrayExternGradient;
import fr.unistra.pelican.algorithms.morphology.gray.GrayMedian;
import fr.unistra.pelican.algorithms.morphology.gray.GrayOpening;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


/**
 * This class represents a buggy effort aimed at multispectral galaxy detection.
 * 
 * It accepts the path of a folder containing each channel of a multispectral galaxy
 * and computes the borders of a galaxy, if present, which are superimposed on the
 * channel-wise union.
 * 
 * FIXME : Normalization issues due to the fits->byte transition.
 * FIXME : convex hull measure
 * FIXME : dilation measure
 * 
 * @author Abdullah
 *
 */

public class GalaxyDetection extends Algorithm
{
	/**
	 * The path of the folder containing each channel
	 */
	public String folder;
	
	/**
	 * The output image
	 */
	public Image output;
	
	/**
	 * This class computes galaxy borders.
	 * 
	 * @param folder the path of a folder containing each channel of a multispectral galaxy
	 * @return the borders of a galaxy, if present, which are superimposed on the channel-wise union.
	 */
	public static Image exec(String folder)
	{
		return (Image) new GalaxyDetection().process(folder);
	}
	
	/**
  	 * Constructor
  	 *
  	 */
	public GalaxyDetection() {		
		
		super();		
		super.inputs = "folder";		
		super.outputs = "output";		
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException{
		boolean DEBUG = false;
		double C1 = 0.9;
		
		// 7x7 disk
		BooleanImage se = new BooleanImage(7,7,1,1,1);
		se.resetCenter();

		boolean[] val = {false,false,true,true,true,false,false,
					false,true,true,true,true,true,false,
					true,true,true,true,true,true,true,
					true,true,true,true,true,true,true,
					true,true,true,true,true,true,true,
					false,true,true,true,true,true,false,
					false,false,true,true,true,false,false,
					};
		se.setPixels(val);
		
		// 5x5 disk
		BooleanImage se3 = new BooleanImage(5,5,1,1,1);
		se.resetCenter();

		boolean[] val3 = {false,false,true,false,false,
					false,true,true,true,false,
					true,true,true,true,true,
					false,true,true,true,false,
					false,false,true,false,false};
		se3.setPixels(val3);

		// annulare 3x3 square
		BooleanImage se4 = new BooleanImage(3,3,1,1,1);
		se.resetCenter();

		boolean[] val4 = {true,true,true,
					true,false,true,
					true,true,true};
		se4.setPixels(val4);

		BooleanImage seSQUARE = FlatStructuringElement2D.createSquareFlatStructuringElement(3);

		try{
			// Combine all channels to a single multichannel image
			ByteImage input = (ByteImage)new MultipleImageLoad().process(folder,new Integer(Image.B));
			int channels = input.getBDim();
			
			int xdim = input.getXDim();
			int ydim = input.getYDim();

			// initial preprocessing...
			// should be realized according to channel noise characteristics..
			input = (ByteImage)new GrayMedian().process(input,se);

			// combine the first two channels for visualisation purposes only..to be used only in the end.
			Image asil = (Image)new Maximum().process(input.getImage4D(0,Image.B),input.getImage4D(1,Image.B));

			// internal marker of each channel..
			Image[] inMarker = new Image[channels];
			
			// external gradient of each channel
			Image[] exgradient = new Image[channels];
			
			// external marker of each channel
			Image[] exMarker = new Image[channels];

			// for every channel
			for(int b = 0; b < channels; b++){
				
				// ************************************************************
				// Extract an initial component representing roughly the galaxy
				// ************************************************************
				inMarker[b] = input.getImage4D(b,Image.B);

				// pile up the external gradients...later we'll take their max..
				exgradient[b] = (Image)new GrayExternGradient().process(inMarker[b],se3);

				// get the histogram
				double[] histo = Histogram.exec(inMarker[b],new Boolean(false));

				// histo mean => initial threshold..
				double sum = 0.0;

				for(int i = 0; i < 256; i++)
					sum += histo[i] * (i+1);
				
				if(DEBUG) System.err.println("sum " + sum);

				int threshold = (int)Math.ceil(sum / (xdim * ydim));
				
				if(DEBUG) System.err.println("threshold " + threshold + " for channel " + b);

				// apply threshold
				for(int i = 0; i < inMarker[b].size(); i++){
					int p = inMarker[b].getPixelByte(i);

					if(p < threshold) inMarker[b].setPixelByte(i,0);
				}
				
				if(DEBUG)new  Viewer2D().process(inMarker[b],"initial approximation 0");
				
				
				// **********************************
				// Marker Extraction
				// **********************************
				Point cntr = getBrightest(inMarker[b]);

				if(DEBUG) System.err.println("Galaxy center (brightest point) detected at : " + cntr.x + " " + cntr.y + " for channel " + b);
				
				// binarize..
				for(int i = 0; i < inMarker[b].size(); i++){
					int p = inMarker[b].getPixelByte(i);

					if(p > 0) inMarker[b].setPixelByte(i,255);
				}

				// a strong opening aiming to eliminate secondary objects..
				inMarker[b] = GrayOpening.exec(inMarker[b],se);
				
				if(DEBUG) Viewer2D.exec(inMarker[b],"opened approximation 1");

				// cut off any connections with the image edges..
				for(int x = 0; x < xdim; x++){
					for(int y = 0; y < ydim; y++){
						if(x == 0 || x == xdim - 1 || y == 0 || y == ydim - 1)
							inMarker[b].setPixelXYByte(x,y,0);
					}
				}
				
				if(DEBUG) Viewer2D.exec(inMarker[b],"no connections to the edges 2");

				/*
				// keep the connected component containing the brightest pixel=center
				// by means of a reconstruction by dilation...its faster than the pixel
				// based approach
				BooleanImage geoMarker = new BooleanImage(xdim,ydim,1,1,1);
				geoMarker.fill(false);
				geoMarker.setPixelXYBoolean(cntr.x,cntr.y,true);

				bool = (BooleanImage)BinaryReconstructionByDilation.process(geoMarker,bool,se2);
				*/
				Image bool = keepCC(inMarker[b],cntr.x,cntr.y);
				if(DEBUG)new  Viewer2D().process(bool,"a single CC is kept 3");
				
				// fill all holes...it takes valuable time but it is crucial in order
				// to obtain viable markers..if it takes too much time try a BIG closing instead.
				bool = (Image)new BinaryFillHole().process(bool,se3);
				//Image bool = (Image)BinaryClosing.process(inMarker[b],se);
				if(DEBUG)new  Viewer2D().process(bool,"holes are now filled 4");
				
				double syOlagan = getPixelNumber(bool);
				
				Image ch = BinaryConvexHull.exec(bool);
				// fill it up
				fill(ch,cntr.x,cntr.y);
				
				if(DEBUG)new  Viewer2D().process(ch,"CH");
				
				double syCh = getPixelNumber(ch);
				double convexity = syOlagan / syCh;
				
				if(DEBUG) System.err.println("Convexity " + convexity + " channel " + b);
				
				C1 = convexity;

				exMarker[b] = new ByteImage(bool,true);
				long pNbr = getPixelNumber(bool);
				
				// we will dilate until this surface ratio is reached..
				double ratio = 1.0;

				while(ratio < 2 - C1){
					exMarker[b] = (Image)new GrayDilation().process(exMarker[b],se3);

					long tmp = getPixelNumber(exMarker[b]);

					ratio = (double)tmp / (double)pNbr;

					if(DEBUG) System.err.println("dilation ratio : " + ratio);
				}
				
				if(DEBUG)new  Viewer2D().process(exMarker[b],"exMarker");
				
				// keep the connected component containing the brightest pixel=center
				// once more...because the successive erosions could have eventually
				// broken the object to more than one parts..the internal marker MUST
				// be a single CC.
				/*
				geoMarker = new BooleanImage(xdim,ydim,1,1,1);
				geoMarker.fill(false);
				geoMarker.setPixelXYBoolean(cntr.x,cntr.y,true);

				bool = (BooleanImage)BinaryReconstructionByDilation.process(geoMarker,inMarker[b],se3);
				*/
				
				inMarker[b] = new ByteImage(bool,true);

				// we will erode until this surface ratio is reached..
				ratio = 1.0;

				while(ratio > C1){
					inMarker[b] = (Image)new  GrayErosion().process(inMarker[b],se3);

					long tmp = getPixelNumber(inMarker[b]);

					ratio = (double)tmp / (double)pNbr;

					if(DEBUG) System.err.println("erosion ratio : " + ratio);
				}
				
				// keep the connected component containing the brightest pixel=center
				// once more...because the successive erosions could have eventually
				// broken the object to more than one parts..the internal marker MUST
				// be a single CC.
				/*geoMarker = new BooleanImage(xdim,ydim,1,1,1);
				geoMarker.fill(false);
				geoMarker.setPixelXYBoolean(cntr.x,cntr.y,true);

				bool = (BooleanImage)BinaryReconstructionByDilation.process(geoMarker,inMarker[b],se3);
				*/
				if(DEBUG)new Viewer2D().process(inMarker[b],"ONCEEEEEEEEEEE");
				inMarker[b] = keepCC(inMarker[b],cntr.x,cntr.y);
				
				if(DEBUG)new  Viewer2D().process(inMarker[b],"a single CC is kept after erosions 5");

				// inverse so that the marker is in black (necessary for the watershed)
				inMarker[b] = (Image)new Inversion().process(inMarker[b]);
				
				if(DEBUG)new  Viewer2D().process(inMarker[b],"internal marker for channel " + b);
			}

			// internal marker...maximum taken as they are already black.
			Image internalMarker = inMarker[0];
			
			for(int i = 1; i < channels; i++)
				internalMarker = (Image)new Maximum().process(internalMarker,inMarker[i]);
			
			if(DEBUG) new Viewer2D().process(internalMarker,"internal marker");

			// external marker
			Image externalMarker = exMarker[0];
			
			for(int i = 1; i < channels; i++)
				externalMarker = (Image)new Maximum().process(externalMarker,exMarker[i]);
			
			if(DEBUG)new  Viewer2D().process(externalMarker,"external marker");
			
			// cut off any connections with the image edges..
			for(int x = 0; x < xdim; x++){
				for(int y = 0; y < ydim; y++){
					if(x == 0 || x == xdim - 1 || y == 0 || y == ydim - 1)
						externalMarker.setPixelXYByte(x,y,0);
				}
			}
			
			// we need the 8-connected borders of externalMarker..so we
			// take the internal gradient with a square SE of size 3.
			Image bool = (Image)new BinaryInternGradient().process(externalMarker,seSQUARE);
			externalMarker = new ByteImage(bool);

			// and inverse...once black you never go back.
			externalMarker = (Image)new Inversion().process(externalMarker);
			
			if(DEBUG)new  Viewer2D().process(externalMarker,"external marker");

			// take the max of external gradients and use it as watershed support
			Image externalGradient = exgradient[0];
			for(int i = 1; i < channels; i++)
				externalGradient = (Image)new Maximum().process(externalGradient,exgradient[i]);
			
			if(DEBUG)new  Viewer2D().process(externalGradient,"wshed support");

			// MWSHED
			Image result = (Image) new DoubleMarkerBasedWatershed().process(externalGradient,externalMarker,internalMarker);
			if(DEBUG)new  Viewer2D().process(result,"wshed");

			// add up with the original for visualisation purposes
			output = (Image)new Maximum().process(asil,result);
		}catch(PelicanException ex){
			ex.printStackTrace();
		}
	}
	
	// keep the CC containing p(x,y)
	private Image keepCC(Image img,int x,int y)
	{
		Image out = img.copyImage(false);
		out.fill(0.0);
		
		LinkedList<Point> fifo = new LinkedList<Point>();

		fifo.add(new Point(x,y));

		while(fifo.size() > 0){
			Point p = (Point)fifo.removeFirst();

			out.setPixelXYByte(p.x,p.y,255);

			for(int j = p.y - 1; j <= p.y + 1; j++){
				for(int k = p.x - 1; k <= p.x + 1; k++){
					try{
						if(!(k == p.x && j == p.y) && out.getPixelXYByte(k,j) == 0 && img.getPixelXYByte(k,j) == 255){
							int size = fifo.size();
							boolean f = false;

							for(int i = 0; i < size; i++){
								Point v = (Point)fifo.get(i);
								if(v.x == k && v.y == j) f = true;
							}
							if(f == false) fifo.add(new Point(k,j));
						}
					}catch(java.lang.ArrayIndexOutOfBoundsException ex){}
				}
			}	
		}
		
		return out;
	}
	
	private void fill(Image img,int x,int y)
	{
		LinkedList<Point> fifo = new LinkedList<Point>();

		fifo.add(new Point(x,y));

		while(fifo.size() > 0){
			Point p = (Point)fifo.removeFirst();

			img.setPixelXYByte(p.x,p.y,255);
			
			check(p.x,p.y + 1,p,img,fifo);
			check(p.x,p.y - 1,p,img,fifo);
			check(p.x + 1,p.y,p,img,fifo);
			check(p.x - 1,p.y,p,img,fifo);	
		}
	}
	
	private void check(int k,int j,Point p,Image img,LinkedList<Point> fifo)
	{
		try{
			if(img.getPixelXYByte(k,j) == 0){
				int size = fifo.size();
				boolean f = false;

				for(int i = 0; i < size; i++){
					Point v = (Point)fifo.get(i);
					if(v.x == k && v.y == j) f = true;
				}
				if(f == false) fifo.add(new Point(k,j));
			}
		}catch(java.lang.ArrayIndexOutOfBoundsException ex){}
	}

	private long getPixelNumber(Image b)
	{
		long sy = 0;

		for(int i = 0; i < b.size(); i++){
			int p = b.getPixelByte(i);
			if(p == 255) sy++;
		}

		return sy;
	}

	private Point getBrightest(Image b)
	{
		int centerX = 0;
		int centerY = 0;
		int maxV = b.getPixelXYByte(0,0);

		int xDim = b.getXDim();
		int yDim = b.getYDim();

		for(int x = 0; x < xDim; x++){
			for(int y = 0; y < yDim; y++){
				int p = b.getPixelXYByte(x,y);

				if(p > maxV){
					centerX = x;
					centerY = y;
					maxV = p;
				}
			}
		}

		return new Point(centerX,centerY);
	}
} 