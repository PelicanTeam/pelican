/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees;

import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.algorithms.morphology.connected.BuildComponentTree;
import fr.unistra.pelican.algorithms.morphology.connected.BuildComponentTreeVectorial;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;
import fr.unistra.pelican.util.vectorial.ordering.ComponentNodeOrdering;
import fr.unistra.pelican.util.vectorial.ordering.LexicographicalSortedOrdering;
import fr.unistra.pelican.util.vectorial.ordering.VectorialBasedComponentOrdering;

/**
 * @author Benjamin Perret
 *
 */
public abstract class ComponentTreeUtil {
	
	/**
	 * The two types of connected components tree : min or max
	 * @author Benjamin Perret
	 *
	 */
	public static enum TreeType{Min,Max};
	
	private static DoubleImage testCase;
	private static DoubleImage testCaseVectorial;
	
	/**
	 * Get a small component tree of monoband image for test purpose
	 * @return
	 */
	public static ComponentTree<Double> getTestCase()
	{
		if(testCase==null)
		{
			testCase= new DoubleImage(3,5,1,1,1);
			testCase.setPixels(new double[]{110,90,100,50,50,50,40,20,50,50,50,50,120,70,80});
		}
		Connectivity3D con = TrivialConnectivity.getFourNeighbourhood();
		return  BuildComponentTree.exec(testCase.copyImage(true), con);
	}
	
	/**
	 * Get a small component tree of multiband image for test purpose
	 * @return
	 */
	public static ComponentTree<double []> getTestCaseVectorial()
	{
		if(testCaseVectorial==null)
		{
			testCaseVectorial= new DoubleImage(3,7,1,1,2);
			testCaseVectorial.setPixels(new double[]{40,20,40,20,40,20,50,20,80,20,50,20,40,20,40,20,40,20,10,10,15,10,0,10,20,40,20,40,20,40,10,50,10,80,10,50,20,40,20,40,20,40});
		}
		Connectivity3D con = TrivialConnectivity.getFourNeighbourhood();
		ComponentNodeOrdering<double []> cno=new VectorialBasedComponentOrdering(new LexicographicalSortedOrdering());
		
		return BuildComponentTreeVectorial.exec(testCaseVectorial, con, cno);
		
	}
	
	
	public static  final String PointListAvailable="POINT_LIST_AVAILABLE";
	public static  final String PointList="POINT_LIST";
	public static  final String MeanValue="MEAN_VALUE";
	public static  final String MinValue="MIN_VALUE";
	public static  final String MaxValue="MAX_VALUE";
	public static  final String VolumeValue="VOLUME";
}
