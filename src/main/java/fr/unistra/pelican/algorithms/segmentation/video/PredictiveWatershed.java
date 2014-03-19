package fr.unistra.pelican.algorithms.segmentation.video;

import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.segmentation.Watershed;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;



/***
 * This class is an implementation of the predictive watershed 
 * presented in Chien, S.Y., Huang, Y.W. and Chen, L.G.,
 * Predictive watershed: a fast watershed algorithm for video segmentation.
 *  IEEE Trans. Circuits Systems Video Technol. vol. 13 n. 5. 453-461.
 * @author Jonathan Weber
 *
 */
public class PredictiveWatershed extends Algorithm {

	public final static int CONNEXITY4 = 4;
	public final static int CONNEXITY8 = 8;
	
	/**
	 * Density image used by the watershed (i.e. gradient image)
	 */
	public Image originalVideo;
	
	/**
	 * Watershed result
	 */
	public IntegerImage outputVideo;
	
	private Image currentFrame;
	
	private Image correspondingFrame;
	
	private Image currentGradient;
	
	private IntegerImage currentLabelImage;
	
	private BooleanImage updatingAreaMask;
	
	private	BooleanImage se;
	
	/**
	 * Connexity used for the gradient computing
	 */
	public int connexity = PredictiveWatershed.CONNEXITY8;
	
	/**
	 * Size of the square blocks
	 */
	public int blockSize = 8;

	/**
	 * Threshold used for the block-based change detection step
	 */
	public int blockChangeDetectionThreshold = blockSize*blockSize*30;
	
	public PredictiveWatershed() 
	{
		super();
		super.inputs = "originalVideo";
		super.options = "blockSize,blockChangeDetectionThreshold,connexity";
		super.outputs = "outputVideo";
	}
	
	
	public void launch() throws AlgorithmException 
	{		
		if(connexity==CONNEXITY8)
		{
			se = FlatStructuringElement2D.createSquareFlatStructuringElement(3);
		}
		else
		{
			se = FlatStructuringElement2D.createCrossFlatStructuringElement(1);
		}
		
		//Video conversion in gray if color
		if(originalVideo.getBDim()==3)
			originalVideo = RGBToGray.exec(originalVideo);
		
		outputVideo = new IntegerImage(originalVideo.getXDim(),originalVideo.getYDim(),1,originalVideo.getTDim(),1);
		
		//First frame
		correspondingFrame = originalVideo.getImage4D(0,Image.T);
		currentGradient = GrayGradient.exec(correspondingFrame,se);
		currentLabelImage = (IntegerImage) Watershed.exec(currentGradient);
		outputVideo.setImage4D(currentLabelImage, 0, Image.T);
		
		//Other frame
		for(int frame = 1; frame<originalVideo.getTDim(); frame++)
		{
			currentFrame = originalVideo.getImage4D(frame, Image.T);
			updatingAreaMask = createUAM();
			updateGradient();
			currentLabelImage = new WatershedInUpdatedAreas(currentGradient,currentLabelImage,updatingAreaMask).exec();
			outputVideo.setImage4D(currentLabelImage, frame, Image.T);
		}

	}

	//TODO : Gestion des bords
	private BooleanImage createUAM()
	{
		BooleanImage uAM = new BooleanImage (originalVideo.getXDim(),originalVideo.getYDim(),1,1,1);
		uAM.fill(false);
		int xDim = originalVideo.getXDim();
		int yDim = originalVideo.getYDim();
		for(int xBlock=0;(xBlock+1)*blockSize-1<xDim;xBlock++)
			for(int yBlock=0;(yBlock+1)*blockSize-1<yDim;yBlock++)
			{
				int absoluteDifference = 0;
				for(int xImage=xBlock*blockSize;xImage<(xBlock+1)*blockSize;xImage++)
					for(int yImage=yBlock*blockSize;yImage<(yBlock+1)*blockSize;yImage++)
					{
						absoluteDifference+= Math.abs(correspondingFrame.getPixelXYByte(xImage,yImage)-currentFrame.getPixelXYByte(xImage,yImage));
					}
				if(absoluteDifference>blockChangeDetectionThreshold)
				{
					for(int xImage=xBlock*blockSize;xImage<(xBlock+1)*blockSize;xImage++)
						for(int yImage=yBlock*blockSize;yImage<(yBlock+1)*blockSize;yImage++)
							uAM.setPixelXYBoolean(xImage, yImage, true);
				}				
			}
		return uAM;
	}
	
	private void updateGradient()
	{
		Image updatingGradient = GrayGradient.exec(currentFrame, se, updatingAreaMask);
		for(int pixel=0; pixel<currentGradient.size(); pixel++)
			if(updatingAreaMask.getPixelBoolean(pixel))
				currentGradient.setPixelByte(pixel,updatingGradient.getPixelByte(pixel));		
	}
	
	public static IntegerImage exec(Image video) {
		return (IntegerImage)new PredictiveWatershed().process(video);
	}
	
	public static IntegerImage exec(Image video, int blockSize, int blockChangeDetectionThreshold)
	{
		return (IntegerImage)new PredictiveWatershed().process(video, blockSize, blockChangeDetectionThreshold);
	}

	/*	
	public static void main(String[] args) {
		//Image original = PelicanImageLoad.exec("C:\\Documents and Settings\\Jonathan.weber\\Mes documents\\videos\\segmentation_tests\\lambor_global.pelican");
		Image original = VideoLoader.exec("../pelican2/samples/sample.avi");
		ViewerVideo.exec(LabelsToRandomColors.exec(PredictiveWatershed.exec(original)));
	}
	*/
	
	
	
	private class WatershedInUpdatedAreas{


		private Image gradientImage;
		
		private IntegerImage previousWatershed;

		private BooleanImage updatingAreaMask;
		
		private Image watershed;

		/**
		 * A constant to represent watershed lines
		 */
		private static final int WSHED = 0;

		/*
		 * Private attributes
		 */
		private static final int INIT = -1;

		private static final int MASK = -2;



		private final Point fictitious = new Point(-1, -1);

		/**
		 * Constructor
		 */
		WatershedInUpdatedAreas(Image gradientImage, IntegerImage previousWatershed, BooleanImage updatingAreaMask) 
		{
			this.gradientImage = gradientImage;
			this.previousWatershed = previousWatershed;
			this.updatingAreaMask = updatingAreaMask;
			this.exec();
	
		}



		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.unistra.pelican.Algorithm#launch()
		 */
		public IntegerImage exec() throws AlgorithmException {
			IntegerImage work = new IntegerImage(previousWatershed.getXDim(), previousWatershed.getYDim(), 1, 1, 1);
			IntegerImage dist = new IntegerImage(previousWatershed.getXDim(), previousWatershed.getYDim(), 1, 1, 1);
			IntegerImage workOut = new IntegerImage(previousWatershed.getXDim(), gradientImage.getYDim(), 1, 1, 1);
			watershed = new IntegerImage(previousWatershed, false);
						for (int x = 0; x < gradientImage.getXDim(); x++)
							for (int y = 0; y < gradientImage.getYDim(); y++)
								// That's a nice hack, isn't it? No Byte to Integer
								// conversion.
								// Work still have values from 0 to 255.
								if(updatingAreaMask.getPixelXYBoolean(x,y))
								{
									work.setPixelInt(x, y, 0, 0, 0, gradientImage.getPixelByte(x, y, 0, 0, 0));
								}
						int currentLabel = WSHED;
						dist.fill(0);
						Fifo fifo = new Fifo();
						Point p;
						// Initialise the workout image and the current label
						workOut.fill(INIT);
						for(int i=0;i<workOut.size();i++)
							if(!updatingAreaMask.getPixelBoolean(i))
							{
								workOut.setPixelInt(i, previousWatershed.getPixelInt(i));
								if(workOut.getPixelInt(i)>currentLabel)
								{
									currentLabel=workOut.getPixelInt(i);
								}
							}
						// pixel value distribution,
						Vector[] distro = calculateDistro(work);

						// start flooding
						for (int i = 0; i < 256; i++) {

							// geodesic SKIZ of level i - 1 inside level i
							int size = distro[i].size();

							for (int j = 0; j < size; j++) 
							{
								p = (Point) distro[i].elementAt(j);

								workOut.setPixelXYInt(p.x, p.y, MASK);

								if (areThereLabelledNeighbours(workOut, p.x, p.y) == true) 
								{
									dist.setPixelXYInt(p.x, p.y, 1);
									fifo.add(p);
								}
							}

							int curDist = 1;
							fifo.add(fictitious);

							do {
								p = (Point) fifo.retrieve();

								if (p.x == -1 && p.y == -1) 
								{
									if (fifo.isEmpty() == true)
										break;
									else 
									{
										fifo.add(fictitious);
										curDist++;
										p = fifo.retrieve();
									}
								}

								// labelling p by inspecting its neighbours
								for (int j = p.y - 1; j <= p.y + 1; j++) 
								{
									for (int k = p.x - 1; k <= p.x + 1; k++) 
									{
										if (k < 0 || k >= gradientImage.getXDim()
												|| j < 0
												|| j >= gradientImage.getYDim())
											continue;

										// if the pixel is
										// already labelled
										if (!(j == p.y && k == p.x)
												&& dist.getPixelXYInt(k, j) < curDist
												&& workOut.getPixelXYInt(k, j) > WSHED) 
										{
											if (workOut.getPixelXYInt(k, j) > 0) 
											{
												if (workOut.getPixelXYInt(p.x, p.y) == MASK
														|| workOut.getPixelXYInt(p.x, p.y) == WSHED)
												{
													workOut.setPixelXYInt(p.x, p.y,
															workOut.getPixelXYInt(
																	k, j));
												}
												else if (workOut.getPixelXYInt(p.x,
														p.y) != workOut
														.getPixelXYInt(k, j))
												{
													workOut.setPixelXYInt(p.x, p.y,	WSHED);
												}
											} else if (workOut.getPixelXYInt(p.x,
													p.y) == MASK)
											{
												workOut.setPixelXYInt(p.x, p.y,	WSHED);
											}
											// if the neighbour is a plateau pixel
										} else if (workOut.getPixelXYInt(k, j) == MASK
												&& dist.getPixelXYInt(k, j) == 0) 
										{
											dist.setPixelXYInt(k, j, curDist + 1);
											fifo.add(new Point(k, j));
										}
									}
								}
							} while (true);

							// check for new minima
							size = distro[i].size();

							// detect and process new minima at level i
							for (int j = 0; j < size; j++) {
								p = (Point) distro[i].elementAt(j);

								// reset distance to 0
								dist.setPixelXYInt(p.x, p.y, 0);

								// if p is inside a new minimum
								if (workOut.getPixelXYInt(p.x, p.y) == MASK) {

									// create a new label
									currentLabel++;
									fifo.add(p);
									workOut.setPixelXYInt(p.x, p.y, currentLabel);

									while (fifo.isEmpty() == false) {
										Point q = fifo.retrieve();

										// for every pixel in the 8-neighbourhood of
										// q
										for (int l = q.y - 1; l <= q.y + 1; l++) {
											for (int k = q.x - 1; k <= q.x + 1; k++) {
												if (k < 0
														|| k >= previousWatershed.getXDim()
														|| l < 0
														|| l >= previousWatershed.getYDim())
													continue;

												if (!(k == q.x && l == q.y)
														&& workOut.getPixelXYInt(k,
																l) == MASK) {
													fifo.add(new Point(k, l));
													workOut.setPixelXYInt(k, l,
															currentLabel);
												}
											}
										}
									}
								}
							}

							// Copy the result to the outputImage
							for (int _x = 0; _x < gradientImage.getXDim(); _x++)
								for (int _y = 0; _y < gradientImage.getYDim(); _y++) {
									// That's a nice hack, isn't it? No Integer to
									// Byte
									// conversion.
									// Values are inside [0,255] if the algo is
									// correct.
									watershed.setPixelInt(_x, _y, 0, 0, 0, workOut.getPixelInt(_x, _y, 0, 0, 0));
								}

						}
			return (IntegerImage) watershed;
		}

		private Vector[] calculateDistro(IntegerImage img) {
			Vector[] distro = new Vector[256];

			for (int i = 0; i < 256; i++)
				distro[i] = new Vector();

			for (int x = 0; x < img.getXDim(); x++) {
				for (int y = 0; y < img.getYDim(); y++)
					if(updatingAreaMask.getPixelXYBoolean(x,y))
						distro[img.getPixelXYInt(x, y)].add(new Point(x, y));
			}

			return distro;
		}

		private boolean areThereLabelledNeighbours(IntegerImage img, int x, int y)
				throws AlgorithmException {
			for (int j = y - 1; j <= y + 1; j++) {
				if (j >= img.getYDim() || j < 0)
					continue;

				for (int i = x - 1; i <= x + 1; i++) {
					if (i >= img.getXDim() || i < 0)
						continue;
					// try{
					if(updatingAreaMask.getPixelXYBoolean(i,j))
						if (!(i == x && j == y) && img.getPixelXYInt(i, j) >= WSHED)
							return true;
				}
			}

			return false;
		}

		private class Fifo {
			private Vector<Object> v;

			Fifo() {
				v = new Vector<Object>();
			}

			void add(Object o) {
				v.add(o);
			}

			Point retrieve() {
				Object o = v.firstElement();
				v.remove(0);

				return (Point) o;
			}

			boolean isEmpty() {
				return (v.size() == 0);
			}

		}

	}
	
	
}
