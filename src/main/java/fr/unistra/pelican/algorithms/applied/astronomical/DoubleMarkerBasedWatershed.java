package fr.unistra.pelican.algorithms.applied.astronomical;


import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.arithmetic.Minimum;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryInternGradient;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershed;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


/**
 * This class combines two marker images in order to apply a marker based watershed transform
 * It is designed for use in conjuntion with the galaxy detection module.
 * 
 * It accepts an external marker as well as an internal marker
 * 
 * @author Abdullah
 *
 */

public class DoubleMarkerBasedWatershed extends Algorithm
{
	/**
	 * Image to be segmented
	 */
	public Image input = null;
	
	/**
	 * The external marker image
	 */
	public Image eMarker = null;
	
	/**
	 * The internal marker image
	 */
	public Image iMarker = null;
	
	/**
	 * The output image
	 */
	public Image output = null;
	
	/**
	 * This class combines two marker images in order to apply a marker based watershed transform
	 * @param input Input image
	 * @param eMarker External marker
	 * @param iMarker Internal marker
	 * @return The segmented image
	 */
	public static Image exec(Image input,Image eMarker,Image iMarker)
	{
		return (Image) new DoubleMarkerBasedWatershed().process(input,eMarker,iMarker);
	}


  	/**
  	 * Constructor
  	 *
  	 */
	public DoubleMarkerBasedWatershed() {		
		
		super();		
		super.inputs = "input,eMarker,iMarker";		
		super.outputs = "output";		
		
	}
  	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException
	{
		try{
			// cut off any connections between the two markers

			int xDim = input.getXDim();
			int yDim = input.getYDim();
		
			Image tmpImage = new ByteImage(iMarker,true);

			for(int x = 0; x < xDim; x++){
				for(int y = 0; y < yDim; y++){
					int p = tmpImage.getPixelXYByte(x,y);

					// check for external marker pixels in its 8-neighbourhood.
					if(p == 0){
						boolean flag = false;

						for(int j = y - 1; j <= y + 1; j++){
							for(int k = x - 1; k <= x + 1; k++){
								if(j < 0 || j >= yDim || k < 0 || k >= xDim) continue;

								int v = eMarker.getPixelXYByte(k,j);

								if(v == 0) flag = true;
							}
						}
					
						// blow up the neighbourhood
						if(flag == true){
							for(int j = y - 1; j <= y + 1; j++){
								for(int k = x - 1; k <= x + 1; k++){
									if(j < 0 || j >= yDim || k < 0 || k >= xDim) continue;

									iMarker.setPixelXYByte(k,j,255);
								}
							}
						}
					}
				}
			}
		
			// combine internal and external markers
			Image marker = (Image) new Minimum().process(eMarker,iMarker);;

			// add 1 to the original smoothed image so that the markers are the only zeroed pixels
			for(int i = 0; i < input.size(); i++){
				int p = input.getPixelByte(i);
				if(p < 255) input.setPixelByte(i,p + 1);
			}

			// combine image and marker
			input = (Image)new Minimum().process(marker,input);
			
			Image result = (Image)new MarkerBasedWatershed().process(input);
			output = new ByteImage(result);

			// get rid of the labels...
			for(int i = 0; i < input.size(); i++){
				int p = output.getPixelByte(i);
				if(p == 1) output.setPixelByte(i,255);
				else output.setPixelByte(i,0);
			}

			// and inverse the detection result...so as the object is white and the rest black
			output = (Image)new Inversion().process(output);
			
			// take the 8 connected borders
			BooleanImage SQUARE = FlatStructuringElement2D.createSquareFlatStructuringElement(3);
			output = (Image) new BinaryInternGradient().process(output,SQUARE);
		}catch(PelicanException ex){
			ex.printStackTrace();
		}
		
	}


}