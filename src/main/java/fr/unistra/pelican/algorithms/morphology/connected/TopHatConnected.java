/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.ComponentTreeUtil;

/**
 * @author Benjamin Perret
 *
 */
public class TopHatConnected<T> extends Algorithm {

	public ComponentTree<T> tree;
	
	public Image mask;
	
	
	
	/**
	 * 
	 */
	public TopHatConnected() {
		super();
		super.inputs="tree,mask";
		super.outputs="tree";
	}



	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		int xdim=mask.xdim;
		int ydim=mask.ydim;
		int zdim=mask.zdim;
		ComponentNode<T> root=tree.getRoot();
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
					if(mask.getPixelXYZBoolean(x, y, z))
					{
						//System.out.print("pile " + (new Point3D(x,y,z)) + " node ");
						ComponentNode<T> n = tree.findNodeAt(x, y, z);
						//System.out.println(n.location);
						while(n!=root && n!=null)
						{
							//System.out.println("-->delete " + n.location);
							tree.deleteNode(n);
							//tree.debugDropNodeMap();
							//System.out.println(root);
							n=n.parent;
						}
						tree.compressPathFinding(); // path becoming too long... stack becoming too short! 
					}


	}
	
	public static <T> ComponentTree<T> exec (ComponentTree<T> tree, Image mask)
	{
		return (ComponentTree<T>)(new TopHatConnected<T>()).process(tree,mask);
	}

	
	public static void main(String [] args)
	{
		ComponentTree<Double> tree=ComponentTreeUtil.getTestCase();
		BooleanImage mask=new BooleanImage(tree.getXdim(),tree.getYdim(),1,1,1);
		mask.setPixelXYBoolean(1, 0, true);
		mask.setPixelXYBoolean(2, 0, true);
		mask.fill(true);
		MultiView mv=MViewer.exec();
		mv.add(tree.image,"Original");
		mv.add(mask,"mask");
		
	tree.debugDropNodeMap();
		System.out.println(tree.getRoot());
		tree=TopHatConnected.exec(tree, mask);
		tree.debugDropNodeMap();
		mv.add(ReconstructImageFromTree.exec(tree),"reconstruct");
		
		
		
	}
}
