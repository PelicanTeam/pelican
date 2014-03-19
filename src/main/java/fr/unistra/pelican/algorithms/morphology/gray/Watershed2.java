package fr.unistra.pelican.algorithms.morphology.gray;

import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.AlgorithmDeprecated;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;

/*

EA (13.03.05)

- total rewrite..same algo (Vincent-Soille)
- added multiband support
- eliminated getBin method...use WatershedLine algorithm instead

TODO

- dont ignore ArrayIndexOutOfBoundsException...
	calculate the proper array instead

*/


public class Watershed2 implements AlgorithmDeprecated
{
	private Image input;
	private IntegerImage output;

	private int xDim;
	private int yDim;
	private int bDim;

	public static final int WSHED = 0;
	private static final int INIT = -3;
	private static final int MASK = -2;
	private static final int INQUEUE = -1;

	public void launch()
	{
		xDim = input.getXDim();
		yDim = input.getYDim();
		bDim = input.getBDim();

		// initialize labels to INIT		
		output = new IntegerImage(xDim,yDim,1,1,bDim);
		output.fill(INIT);

		for(int b = 0; b < bDim; b++){
			
			int current_label = WSHED;
			boolean flag = false;
			int x,y;
			Fifo fifo = new Fifo();
			Point p;
			
			int currentLabel = WSHED;

			// pixel value distribution,
			// so that we dont have to check the entire image
			Vector[] distro = calculateDistro(b);

			for(int i = 0; i < 256; i++){

				// geodesic SKIZ of level i - 1 inside level i
				int size = distro[i].size();

				for(int j = 0; j < size; j++){
					p = (Point)distro[i].elementAt(j);

					x = (int)p.getX(); 
					y = (int)p.getY();

					output.setPixelXYBInt(x,y,b,MASK);

					if(areThereLabelledNeighbours(x,y,b) == true){
						output.setPixelXYBInt(x,y,b,INQUEUE);
						fifo.add(p);
					}
				}

				while(fifo.isEmpty() == false){
					p = (Point)fifo.retrieve();
					x = (int)p.getX();
					y = (int)p.getY();

					// for every pixel in the 8-neighbourhood of p
					for(int j = y - 1; j <= y + 1; j++){
						for(int k = x - 1; k <= x + 1; k++){

							if(k < 0 || k >= xDim || j < 0 || j >= yDim) continue;
							
							// if the pixel is already labelled
							if(!(j == y && k == x) && output.getPixelXYBInt(k,j,b) > WSHED){
								if(output.getPixelXYBInt(x,y,b) == INQUEUE || 
										(output.getPixelXYBInt(x,y,b) == WSHED && flag == true))
									output.setPixelXYBInt(x,y,b,output.getPixelXYBInt(k,j,b));

								else if(output.getPixelXYBInt(x,y,b) > WSHED && 
									output.getPixelXYBInt(x,y,b) != output.getPixelXYBInt(k,j,b)){
									output.setPixelXYBInt(x,y,b,WSHED);
									flag = false;
								}
							}else if(output.getPixelXYBInt(k,j,b) == WSHED && output.getPixelXYBInt(x,y,b) == INQUEUE){
								output.setPixelXYBInt(x,y,b,WSHED);
								flag = true;
							}else if(output.getPixelXYBInt(k,j,b) == MASK){
								output.setPixelXYBInt(k,j,b,INQUEUE);
								fifo.add(new Point(k,j));
							}
						}
					}
				}


				// check for new minima
				size = distro[i].size();

				for(int j = 0; j < size; j++){
					p = (Point)distro[i].elementAt(j);

					x = (int)p.getX(); 
					y = (int)p.getY();

					if(output.getPixelXYBInt(x,y,b) == MASK){
						currentLabel++;
						fifo.add(p);
						output.setPixelXYBInt(x,y,b,currentLabel);

						while(fifo.isEmpty() == false){
							p = (Point)fifo.retrieve();
							x = (int)p.getX();
							y = (int)p.getY();
						
							// for every pixel in the 8-neighbourhood of p
							for(int l = y - 1; l <= y + 1; l++){
								for(int k = x - 1; k <= x + 1; k++){
									if(k < 0 || k >= xDim || l < 0 || l >= yDim) continue;
									if(!(k == x && l == y) && output.getPixelXYBInt(k,l,b) == MASK){
										fifo.add(new Point(k,l));
										output.setPixelXYBInt(k,l,b,currentLabel);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private Vector[] calculateDistro(int b)
	{
		Vector[] distro = new Vector[256];

		for(int i = 0; i < 256; i++)
			distro[i] = new Vector();

		for(int x = 0; x < xDim; x++){
			for(int y = 0; y < yDim; y++)
				distro[input.getPixelXYBByte(x,y,b)].add(new Point(x,y));
		}

		return distro;
	}

	private boolean areThereLabelledNeighbours(int x,int y,int b)
	{
		for(int j = y - 1; j <= y + 1; j++){
			for(int i = x - 1; i <= x + 1; i++){
				if(i < 0 || i >= xDim || j < 0 || j >= yDim) continue;
				
				if(!(i == x && j == y) && output.getPixelXYBInt(i,j,b) >= WSHED)
					return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#setInput(java.util.Vector)
	 */
	public void setInput(Vector inputVector) 
	throws InvalidNumberOfParametersException, InvalidTypeOfParameterException {
		// Check the number of parameters.
		if (inputVector.size() != 1) 
			throw new InvalidNumberOfParametersException("Need one parameter!");
		
		Object o=inputVector.firstElement();
		
		// Check types of each parameter.
		if ((o instanceof fr.unistra.pelican.Image) == false) 
			throw new InvalidTypeOfParameterException(
			"Input param 1 need to be instance of fr.unistra.pelican.Image");
		// When type is checked, store the parameter.
		input = (Image)o;
	}

	public Vector getOutput()
	{
		Vector v=new Vector(1);
		v.add(output);
		return v;
	}

	public String[] getInputTypes()
	{
		String[] tab = new String[1];
		tab[0]="fr.unistra.pelican.Image";
		return tab;
	}

	public String[] getOutputTypes()
	{
		String[] tab = new String[1];
		tab[0]="fr.unistra.pelican.Image";

		return tab;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#help()
	 */
	public String help() {
		return "This class realize a watershed segmentation.\n"
		+"Image inputImage\n"
		+"\n"
		+"Image segmentedImage\n"
		+"\n"
		+"This class realize a watershed segmentation. "
		+"This class work on a byte resolution. "
		+"The maximum number of created segment is 2^31-1. "
		+"It return an IntegerImage, the first segment as label Integer.MIN_VALUE. " 
		+"It use the Soille algorithm with a fifo stack described in Morphological Image Analysis from Soille.";
	}
	
	/** Static fonction that use this algorithm.
	 * 
	 * Each algorithm can have one or more of theses static fonction. 
	 * It's more convenient for coding.
	 * 
	 * @param image
	 * @return result
	 * @throws InvalidTypeOfParameterException 
	 * @throws AlgorithmException 
	 * @throws InvalidNumberOfParametersException 
	 */
	public static Image process(Image image) throws InvalidTypeOfParameterException, AlgorithmException, InvalidNumberOfParametersException {
		Watershed2 algo = new Watershed2();
		Vector inputs = new Vector();
		inputs.add(image);
		algo.setInput(inputs);
		algo.launch();
		return (Image)algo.getOutput().firstElement();		
	}


	private class Fifo
	{
		private Vector v;

		Fifo()
		{
			v = new Vector();
		}

		void add(Object o)
		{
			v.add(o);
		}

		Object retrieve()
		{
			Object o = v.firstElement();
			v.remove(0);

			return o;
		}

		boolean isEmpty()
		{
			return v.size() == 0;
		}

		int size()
		{
			return v.size();
		}
	}
}