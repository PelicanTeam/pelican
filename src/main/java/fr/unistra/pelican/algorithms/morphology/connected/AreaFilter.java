/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.connected.FilterComponentTree.FilterStrategy;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.ComponentTreeUtil.TreeType;
import fr.unistra.pelican.util.connectivityTrees.attributes.AreaAttributFilter;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeFilter;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;

/**
 * Connected area filter for mono band images. Delete all connected component with area less or equal to given threshold.
 * 
 * Pixel ordering is assumed to be usual ordering but can be inverted using treetype option.
 * 
 * Assumed connectivity is 8 neighborhood, option is here to modify it.
 * 
 * @author Benjamin Perret
 *
 */
public class AreaFilter extends Algorithm {

	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Size threshold
	 */
	public int size;
	
	/**
	 * 
	 */
	public boolean monoModaleHyperConnection=false;
	
	/**
	 * Result
	 */
	public Image outputImage;
	
	/**
	 * Connectivity (default = TrivialConnectivity.getHeightNeighbourhood())
	 */
	public Connectivity3D con = TrivialConnectivity.getHeightNeighbourhood();
	
	/**
	 * Default is usual ordering (treeType=TreeType.Max)
	 */
	public TreeType treeType=TreeType.Max;
	
	public AreaFilter(){
		super.inputs="inputImage,size";
		super.options="treeType,con,monoModaleHyperConnection";
		super.outputs="outputImage";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if (size<1)
			throw new AlgorithmException("Size filter must be greater or equal to one.");
		AreaAttributFilter filter = new AreaAttributFilter(size);
		ComponentTree root = BuildComponentTree.exec(inputImage, con,treeType);
				root = FilterComponentTree.exec(root, new AttributeFilter[]{filter}, FilterStrategy.Min);	
		outputImage=ReconstructImageFromTree.exec(root);
	}
	
	public static <T extends Image> T exec(T inputImage, int size)
	{
		return (T)new AreaFilter().process(inputImage,size);
	}
	
	public static <T extends Image> T exec(T inputImage, int size, TreeType treeType)
	{
		return (T)new AreaFilter().process(inputImage,size,treeType);
	}
	
	public static <T extends Image> T exec(T inputImage, int size, TreeType treeType,Connectivity3D con)
	{
		return (T)new AreaFilter().process(inputImage,size,treeType,con);
	}

	public static void main(String [] args){
		//Image im =ImageLoader.exec("samples/AstronomicalImagesFITS/img1-10.fits");
		Image im =ImageLoader.exec("samples/camera.png");
		im.setName("CameraMan");
		
		MultiView mv = MViewer.exec(im);
		mv.add(AreaFilter.exec(AreaFilter.exec(im, 20*20, TreeType.Max),20*20,TreeType.Min),"Alternate min-max filter 400 pixels");
	
	}
	
}
