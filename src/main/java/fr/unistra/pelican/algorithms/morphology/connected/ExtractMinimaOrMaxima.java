/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.ProcessChannels;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.ComponentTreeUtil.TreeType;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;
import fr.unistra.pelican.util.vectorial.ordering.LexicographicalOrdering;
import fr.unistra.pelican.util.vectorial.ordering.VectorialBasedComponentOrdering;
import fr.unistra.pelican.util.vectorial.ordering.VectorialOrdering;

/**
 * Extract local minima or maxima of a multiband image with respect to a given connectivity and vectorial ordering.
 * 
 * Maxima are just leaves of a max tree and minima leaves o a min tree
 * 
 * Note that in a natural unfiltered image there will probably be a lot of maxima or minima
 * 
 * @author Benjamin Perret
 *
 */
public class ExtractMinimaOrMaxima extends Algorithm {

	/**
	 * Minima or maxima?
	 * @author Benjamin Perret
	 *
	 */
	public static enum Operation{Minima, Maxima};
	
	
	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Operation to perform (default is extract maxima)
	 */
	public Operation op=Operation.Minima;
	
	/**
	 * Connectivity to use (default is TrivialConnectivity.getFourNeighbourhood()) 
	 */
	public Connectivity3D co= TrivialConnectivity.getFourNeighbourhood();
	
	/**
	 * Vectorial ordering o use
	 */
	public VectorialOrdering vo = new LexicographicalOrdering();
	
	/**
	 * Result 
	 */
	public BooleanImage outputImage;
	
	
	public ExtractMinimaOrMaxima(){
		super.inputs="inputImage";
		super.options="op,co,vo";
		super.outputs="outputImage";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		outputImage= new BooleanImage(inputImage.xdim,inputImage.ydim,inputImage.zdim,1,1);
		outputImage.fill(false);
		ComponentTree tree;
		switch (op)
		{
		case Minima:
			tree=BuildComponentTreeVectorial.exec(inputImage, co, new VectorialBasedComponentOrdering(vo),TreeType.Min);
			break;
		case Maxima:
			tree=BuildComponentTreeVectorial.exec(inputImage, co, new VectorialBasedComponentOrdering(vo),TreeType.Max);
			break;
		default:
			throw new AlgorithmException("Wow! you managed to create and undefnied element in an enum structure, i can't let you do that!");
		}
		
		Image res=ReconstructImageFromTree.exec(tree,true);
		
		res=ProcessChannels.exec(res,ProcessChannels.MAXIMUM);

		for(int i=0;i<outputImage.size();i++)
		{
			double v=res.getPixelDouble(i);
			if(!Double.isNaN(v) && v > -Double.MAX_VALUE)
				outputImage.setPixelBoolean(i, true);
		}
	}

	public static BooleanImage exec(Image inputImage)
	{
		return (BooleanImage) new ExtractMinimaOrMaxima().process(inputImage);
	}
	
	public static BooleanImage exec(Image inputImage, Operation op)
	{
		return (BooleanImage) new ExtractMinimaOrMaxima().process(inputImage, op);
	}
	
	public static BooleanImage exec(Image inputImage, Operation op, Connectivity3D co)
	{
		return (BooleanImage) new ExtractMinimaOrMaxima().process(inputImage, op, co);
	}
	
	public static BooleanImage exec(Image inputImage, Operation op, Connectivity3D co, VectorialOrdering vo)
	{
		return (BooleanImage) new ExtractMinimaOrMaxima().process(inputImage, op, co, vo);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Image im = ImageLoader.exec("samples/lennaGray256.png");
		Image res=ExtractMinimaOrMaxima.exec(im, Operation.Maxima, TrivialConnectivity.getHeightNeighbourhood());
		MViewer.exec(im,res);

	}

}
