package fr.unistra.pelican.algorithms.applied.remotesensing.coastline;

import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.geometric.Crop2D;
import fr.unistra.pelican.algorithms.geometric.Padding;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryErosion;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershed;
import fr.unistra.pelican.algorithms.segmentation.OtsuThresholding;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionSize;



/** 
 * Class that applies the Bagli's (2004) algorithm for detection of coastlines.
 * 
 * @author Dany DAMAJ, Jonathan WEBER
 */
public class BagliCoastlineDetector extends Algorithm
{
		/**
		 * Image to be processed
		 */
		public Image input;
		/**
		 * Bagli mode (SRG or watershed)
		 */
		public Integer mode;
        
		/**
		 * Binarisation mode
		 */
		public Integer binarisationmode;
		/**
		 * Threshold for binarisation
		 */
		public Integer thresh; //if binarisationmode == other
    	
        /**
         * Band computed
         */
        public Integer band;
		
		/**
		 * Constant for the mean binarisation method
		 */
    	public static final int MEAN= 0;
    	/**
		 * Constant for the otsu binarisation method
		 */
    	public static final int OTSU = 1;
    	/**
		 * Constant for the manual binarisation method
		 */
    	public static final int MANUAL = 2;
        
    	/**
    	 * Resulting picture
    	 */
        public BooleanImage outputs;
        
        private Image inputs;

        
        /**
         * Constant for Seed Region Growing
         */
        public static final int SRG = 0;
        /**
         * Constant for Watershed
         */
        public static final int WS = 1;
        
    	private Vector<Integer> mat[][];
    	private boolean isAffected[][];
    	private float mean[];
    	private Vector<Point> ppl[];
    	private int nbAffected;
        
    	private int xdim,ydim,bdim;
    	
    	
    	/**
      	 * Constructor
      	 *
      	 */
    	public BagliCoastlineDetector() {		
    		
    		super();		
    		super.inputs = "input,mode,binarisationmode,thresh,band";		
    		super.outputs = "outputs";		
    		
    	}
    	
    	private void init(int xdim,int ydim,int nbRegions)
    	{
        	// we must consider a matrix of region-belonging points
        	mat = new Vector[xdim][ydim];
    		//voisins = new Vector[nbRegions];
        	isAffected = new boolean[xdim][ydim];
        	for(int x = 0 ; x<xdim ; x++)
        	{
        		for(int y = 0 ; y < ydim ; y++)
        		{
        			mat[x][y] = new Vector<Integer>();
        			isAffected[x][y] = false;
        		}
        	}
        	
        	mean = new float[nbRegions];
        	ppl = new Vector[nbRegions];
        	for(int i = 0 ; i < nbRegions ; i++)
        	{
        		ppl[i] = new Vector<Point>();
        		mean[i] = 0.0f;
        	}
        	
        	nbAffected = 0;
    	}
    	
        /* (non-Javadoc)
         * @see fr.unistra.pelican.Algorithm#launch()
         */
        public void launch() throws AlgorithmException
        { 
        	// first, we consider as many seeders as there are regions
        	// for example with Otsu method
        	xdim = input.getXDim();
        	ydim = input.getYDim();
        	bdim = input.getBDim();
    		int tdim = input.getTDim(); //temps
    		int zdim = input.getZDim();
        	
    		// gray-level transformation :
    		try
    		{
    			input = (Image)new RGBToGray().process(input);
    		}catch(AlgorithmException ae) {System.out.println("Image already non-rgb ; ignoring...");}      	
        	
    		inputs = new ByteImage(xdim,ydim,zdim,tdim,1);
    		outputs = new BooleanImage(xdim,ydim,zdim,tdim,1);
    		
    		 
    			for(int t = 0; t < tdim; t++)
    				for(int z = 0; z < zdim; z++)
    					for(int y = 1; y < ydim-1 ; y++)
    						for(int x = 1; x < xdim-1 ; x++)
    							inputs.setPixelByte(x, y, z, t, 0, input.getPixelXYZTBByte(x, y, z, t, band));
    		
	    		if(mode == SRG)
	    		{
		        	Image bi = (Image) new OtsuThresholding().process(inputs);
		        	
		        	// segmentation (labellisation) by 8-connexity
		        	Image ii = BooleanConnectedComponentsLabeling.exec(bi,BooleanConnectedComponentsLabeling.CONNEXITY8,true);
		        	// ii is now an image with bdim = tdim = zdim = 1
		        	
		        	// to know the size of the regions
		        	int tailles[];
		        	tailles = (int[]) new RegionSize().process(ii);
		        	
		        	// to begin SRG algorithm, we choose one seeder (though one pixel) for each region
		        	init(xdim,ydim,tailles.length);
		        	
		        	for(int x = 0 ; x<xdim ; x++)
		        	{
		        		for(int y = 0 ; y < ydim ; y++)
		        		{
		        			if( ppl[ii.getPixelInt(x, y, 0, 0, 0) ].size() == 0 )
		        					affectPointToLabel(x,y,ii.getPixelInt(x, y, 0, 0, 0));
		        		}
		        	}
		
		        	// some variables needed for the progress evaluation
		        	int pas = xdim*ydim / 50;
		        	int pasc = pas;
		        	int pc = 0;
		        	
		        	while( nbAffected < xdim*ydim)
		        	{
		        		if( nbAffected > pasc)
		        		{
		        			System.out.println("avancement : "+(2*pc) +" %");
		        			pc++;
		        			pasc += pas;
		        		}
		        		searchMinimumDissimilarities();
		        	}
		        	
	    			// for the parametrized threshold
	    			Image grays = new ByteImage(xdim,ydim,1,1,1);
		            for(int i = 0 ; i < ppl.length ; i++)
		            {
		            	for(int j = 0 ; j < ppl[i].size() ; j++)
		            	{
		            		Point pij = (Point)ppl[i].elementAt(j);
	    					grays.setPixelByte(pij.x, pij.y, 0, 0, 0, (int)mean[i]);
	    				}
	    			}
	    			
	    			if(binarisationmode == MEAN)
	    			{
	    				int compteur = 0;
	    				double valeur_moyenne = 0;
						for(int y = 0; y < ydim ; y++){
							for(int x = 0; x < xdim ; x++)
							{
								valeur_moyenne +=  inputs.getPixelXYZTBDouble(x,y,0,0,0);
								compteur++;
							}
						}
						valeur_moyenne /= (double) compteur;
						outputs = (BooleanImage) new ManualThresholding().process(grays,valeur_moyenne);

	    			}
	    			else if(binarisationmode == OTSU)
	    				outputs = (BooleanImage) new OtsuThresholding().process(grays);
	    			else if(binarisationmode == MANUAL)
	    				outputs = (BooleanImage) new ManualThresholding().process(grays,thresh);
		        	
	    		}
	    		else //watershed
	    		{
	    			BooleanImage fse = new BooleanImage(3,3,1,1,1);
	    			fse.resetCenter();
	    			fse.fill(true);
	    			
	    			// we need the image of the morphological gradient ...
	    			ByteImage inputGradient = (ByteImage)new GrayGradient().process(inputs, fse);
	    			
	    			// WARNING : this inputGradient image is only definied in range [1,xdim-2] , [1,ydim -2] though :
	    			inputGradient = (ByteImage)new Crop2D().process(inputGradient, new Point(1, 1), new Point(xdim-2,ydim-2));
	    			/*for(int x=0;x<inputGradient.getXDim();x++) {
	    				inputGradient.setPixelXYByte(x,0,0);
	    				inputGradient.setPixelXYByte(x,inputGradient.getYDim()-1,0);
	    			}
  					for(int y=0;y<inputGradient.getYDim();y++) {
	    				inputGradient.setPixelXYByte(0,y,0);
	    				inputGradient.setPixelXYByte(inputGradient.getXDim()-1,y,0);
  					}
	    			*/
	    			// ... threshold ... NOTE : the Otsu's thresholding has some limitations, maybe mean method could be better sometimes 
	    			BooleanImage otsu = (BooleanImage) new OtsuThresholding().process(inputGradient);
	    			
	    			// ... and erode in order to have a good start point for the watershed algorithm
	    			otsu = (BooleanImage) new BinaryErosion().process(otsu, fse);
	    			
	    			// we copy the start-image ...
	    			ByteImage copyInput = new ByteImage(inputGradient,true);
	    			
	    			// ... and place the markers for WS as pixels of values 0, as the pixels who survive the erosion
	    			for(int i = 0 ; i < otsu.getXDim() ; i++)
	    				for(int j = 0 ; j < otsu.getYDim() ; j++)
	    					if(otsu.getPixelBoolean(i, j, 0, 0, 0))
	    					{
	    						copyInput.setPixelXYZTBByte(i, j, 0, 0, 0, 0);
	    					}
	    			
	    			// ... and we apply WS algorithm on these image.
	    			IntegerImage intInput = (IntegerImage)new MarkerBasedWatershed().process(copyInput);
	    			
	    			// (for the visualisation)
	    			//IntegerImage intImage = intInput.scaleToVisibleRange();
	    			//Viewer2D.exec(intImage, "marked");
	    			
	    			// Now we have to compute extrema of the WS result ...
	    			int min = Integer.MAX_VALUE,max = Integer.MIN_VALUE;
	    			for(int i = 0 ; i < intInput.getXDim() ; i++)
	    			{
	    				for(int j = 0 ; j <intInput.getYDim() ; j++)
	    				{
	    					int tmp =intInput.getPixelInt(i, j, 0, 0, 0);
	    					if(tmp < min)
	    						min = tmp;
	    					if(tmp > max)
	    						max = tmp;
	    				}
	    			}
	    			
	    			// ... which are required to compute the mean intensity value of each area ...
	    			int nb_regions = max-min+1;
	    			
	    			int nb_elements[] = new int[nb_regions];
	    			int sum_intensities[] = new int[nb_regions];
	    			
	    			for(int k = 0 ; k < nb_regions ; k++)
	    			{
	    				nb_elements[k] = 0;
	    				sum_intensities[k] = 0;
	    			}
	    			
	    			for(int i = 0 ; i <  intInput.getXDim()  ; i++)
	    			{
	    				for(int j = 0 ; j <  intInput.getYDim()  ; j++)
	    				{
	    					int tmp = intInput.getPixelInt(i, j, 0, 0, 0);
	    					sum_intensities[tmp - min] += inputs.getPixelByte(i, j, 0, 0, 0);
	    					nb_elements[tmp - min] ++;
	    				}
	    			}
	    			
	    			// parametrized threshold
	    			Image grays = new ByteImage(intInput.getXDim(),intInput.getYDim(),1,1,1);
	    			for(int i = 0 ; i <intInput.getXDim() ; i++)
	    			{
	    				for(int j = 0 ; j < intInput.getYDim() ; j++)
	    				{
	    					int tmp = intInput.getPixelInt(i, j, 0, 0, 0);
	    					if(nb_elements[tmp - min] != 0)
	    					{
	    						grays.setPixelByte(i, j, 0, 0, 0, sum_intensities[tmp - min] / nb_elements[tmp - min]);
	    					}
	    				}
	    			}
	    			
	    			if(binarisationmode == MEAN)
	    			{
	    				int compteur = 0;
	    				double valeur_moyenne = 0;
						for(int y = 0; y < ydim ; y++){
							for(int x = 0; x < xdim ; x++)
							{
								valeur_moyenne +=  inputs.getPixelXYZTBDouble(x,y,0,0,0);
								compteur++;
							}
						}
						valeur_moyenne /= (double) compteur;
						outputs = (BooleanImage) new ManualThresholding().process(grays,valeur_moyenne);

	    			}
	    			else if(binarisationmode == OTSU)
	    				outputs = (BooleanImage) new OtsuThresholding().process(grays);
	    			else if(binarisationmode == MANUAL)
	    				outputs = (BooleanImage) new ManualThresholding().process(grays,thresh);
	    			
	    			outputs = (BooleanImage) new Padding().process(outputs,outputs.getXDim()+2,outputs.getYDim()+2,1,1,1,Padding.NULL,1,1,0,0,0);
	    			
	    		}
	    		
	    		System.out.println(input.getXDim()+" "+input.getYDim()+":"+outputs.getXDim()+" "+outputs.getYDim());
    		}
        
        
        /*
         * For the tests
         */
        private void afficherTout()
        {
        	System.out.println("===== Affichage de l'etat courant des variables =====");
        	System.out.println("Nb points affectes : "+nbAffected);
        	System.out.println("Par region :");
        	for(int i = 0 ; i < mean.length ; i++)
        	{
        		System.out.println("Region : "+i+" , contient "+ppl[i].size()+" points et est de moyenne : "+mean[i]);
        	}
        	for(int x = 0 ; x<xdim ; x++)
        	{
        		for(int y = 0 ; y < ydim ; y++)
        		{
        			for(int k = 0 ; k < mat[x][y].size() ; k++)
        			{
        				System.out.println("point ("+x+","+y+") touche label " + ((Integer)mat[x][y].elementAt(k)).intValue() );
        			}
        		}
        	}
        	System.out.println("===== Fin =====");
        }
    	
        /**
         * Affect Point (x,y) to the region of label "label"
         */
        private void affectPointToLabel(int x,int y,int label)
        {
        	nbAffected++;
        	isAffected[x][y] = true;
        	mean[label] = (mean[label]*(float)ppl[label].size() + (float)inputs.getPixelByte(x, y, 0, 0, 0)) / ((float)ppl[label].size() +1.0f);
        	
        	//update_and_quicksort(label); <- experimental work for bagli's optimization (don't work for the moment)
        	
        	ppl[label].add(new Point(x,y));
        	
        	int imin = Math.max(0, x-1),imax = Math.min(xdim-1, x+1);
        	int jmin = Math.max(0, y-1),jmax = Math.min(ydim-1, y+1);
        	boolean found;
        	
        	for(int i = imin ; i <= imax ; i++)
        	{
        		for(int j = jmin ; j <= jmax ; j++)
        		{
        			found = false;
        			for(int k = 0 ; k < mat[i][j].size() ; k++)
        			{
        				if(mat[i][j].elementAt(k).intValue() == label)
        					found = true;
        			}
        			if( !isAffected[i][j] && !found )
        			{
        				mat[i][j].add(new Integer(label));
        			}        			
        		}
        	}
        	mat[x][y] .removeAllElements(); //in order not to have too much memory used
        }
        
        /*
         * Search the current minimum dissimlarity and affects corresponding pixels
         */
        private void searchMinimumDissimilarities()
        {
        	float min = Float.MAX_VALUE;
        	Vector<Point> points = new Vector<Point>();
        	Vector<Integer> labels = new Vector<Integer>();
        	int intensity;
        	float dissimilarity;
        	int taille;
        	
        	for(int x = 0 ; x<xdim ; x++)
        	{
        		for(int y = 0 ; y < ydim ; y++)
        		{
        			if( /*!isAffected[x][y] &&*/ (taille=mat[x][y].size()) > 0)
        			{
        				intensity = inputs.getPixelByte(x, y, 0,0,0);
	        			for(int z = 0 ; z < taille ; z++)
	        			{
	        				dissimilarity = Math.abs(intensity - mean[ ((Integer)mat[x][y].elementAt(z)).intValue() ]);
	        				if(dissimilarity < min )
	        				{
	        					min = dissimilarity;
	        					points.removeAllElements();
	        					labels.removeAllElements();
	        					points.add(new Point(x,y));
	        					labels.add(new Integer((Integer)mat[x][y].elementAt(z)));
	        				}
	        				else if (dissimilarity == min)
	        				{
	        					points.add(new Point(x,y));
	        					labels.add(new Integer((Integer)mat[x][y].elementAt(z)));
	        				}
	        			}
        			}
        		}
        	}
        	
        	for(int i = 0 ; i < points.size() ; i++)
        	{
        		affectPointToLabel(((Point)points.elementAt(i)).x,((Point)points.elementAt(i)).y,((Integer) labels.elementAt(i)).intValue());
        	}
        }
        
        /**
         * Method never called : selects APPROXIMATIVE minimum dissimarities (error accepted : 0.1)
         * @param bands
         */
        private void searchMinimumDissimilarities10(int bands)
        {
        	int min = Integer.MAX_VALUE;
        	Vector<Point> points = new Vector<Point>();
        	Vector<Integer> labels = new Vector<Integer>();
        	int intensity;
        	int dissimilarity;
        	
        	for(int x = 0 ; x<xdim ; x++)
        	{
        		for(int y = 0 ; y < ydim ; y++)
        		{
        			if(mat[x][y].size() != 0)
        			{
    				intensity = input.getPixelByte(x, y, 0,0,0);
        			for(int z = 0 ; z < mat[x][y].size() ; z++)
        			{
        				dissimilarity = (int)(10.0 * Math.abs(intensity - mean[ ((Integer)mat[x][y].elementAt(z)).intValue() ]));
        				if(dissimilarity < min )
        				{
        					min = dissimilarity;
        					points.removeAllElements();
        					labels.removeAllElements();
        					points.add(new Point(x,y));
        					labels.add(new Integer((Integer)mat[x][y].elementAt(z)));
        				}
        				else if (dissimilarity == min)
        				{
        					points.add(new Point(x,y));
        					labels.add(new Integer((Integer)mat[x][y].elementAt(z)));
        				}
        			}
        			}
        		}
        	}
        	
        	for(int i = 0 ; i < points.size() ; i++)
        	{
        		affectPointToLabel(((Point)points.elementAt(i)).x,((Point)points.elementAt(i)).y,((Integer) labels.elementAt(i)).intValue());
        	}
        	
        }

        /**
         * Method never called : selects APPROXIMATIVE minimum dissimarities (error accepted : cast as int)
        */
        private void searchMinimumDissimilaritiesInt(int bands)
        {
        	int min = Integer.MAX_VALUE;
        	Vector<Point> points = new Vector<Point>();
        	Vector<Integer> labels = new Vector<Integer>();
        	int intensity;
        	int dissimilarity;
        	
        	for(int x = 0 ; x<xdim ; x++)
        	{
        		for(int y = 0 ; y < ydim ; y++)
        		{
        			if(mat[x][y].size() != 0)
        			{
    				intensity = input.getPixelByte(x, y, 0,0,0);
        			for(int z = 0 ; z < mat[x][y].size() ; z++)
        			{
        				dissimilarity = (int)(Math.abs(intensity - mean[ ((Integer)mat[x][y].elementAt(z)).intValue() ]));
        				if(dissimilarity < min )
        				{
        					min = dissimilarity;
        					points.removeAllElements();
        					labels.removeAllElements();
        					points.add(new Point(x,y));
        					labels.add(new Integer((Integer)mat[x][y].elementAt(z)));
        				}
        				else if (dissimilarity == min)
        				{
        					points.add(new Point(x,y));
        					labels.add(new Integer((Integer)mat[x][y].elementAt(z)));
        				}
        			}
        			}
        		}
        	}
        	
        	for(int i = 0 ; i < points.size() ; i++)
        	{
        		affectPointToLabel(((Point)points.elementAt(i)).x,((Point)points.elementAt(i)).y,((Integer) labels.elementAt(i)).intValue());
        	}
        	
        }
        /**
         * Method that applies the Bagli's (2004) algorithm for detection of coastlines.
         * @param input Satellite picture
         * @param mode Bagli's mode
         * @param binarisationmode Binarisation mode
         * @param thresh threshold for binarisation
         * @param band Band to compute
         * @return image with coastline
         */
        public static BooleanImage exec(Image input,int mode,int binarisationmode,double thresh,int band)
        {
        	return (BooleanImage) new BagliCoastlineDetector().process(input, mode, binarisationmode, thresh, band);
        }
}


