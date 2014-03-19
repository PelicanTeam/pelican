/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import java.util.Stack;

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
 * Extract all monomodal hyper-connected components of a tree marked by the masked.
 * 
 * In other words delete all nodes of a connected components tree having an empty intersection with the given mask.
 * 
 * Or in other words performs an opening by reconstruction ;)
 * 
 * @author Benjamin Perret
 *
 */
public class OpeningByReconstruction<T> extends Algorithm {

	/**
	 * The component tree
	 */
	public ComponentTree<T> tree;
	
	/**
	 * The mask
	 */
	public Image mask;
	
	
	
	/**
	 * 
	 */
	public OpeningByReconstruction() {
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
		tree.resetFlag(0);
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
					if(mask.getPixelXYZBoolean(x, y, z))
					{		
						ComponentNode<T> n = tree.findNodeAt(x, y, z);
						while(n!=root && n!=null)
						{
							n.flag=1;
							n=n.parent;
						}
						//tree.compressPathFinding(); // path becoming too long... stack becoming too short! 
					}
		Stack<ComponentNode<T>> s= new Stack<ComponentNode<T>>();
		root.flag=1;
		s.push(root);
		while(!s.isEmpty())
		{
			ComponentNode<T> n =s.pop();
			for(ComponentNode<T> c:n.getChildren())
				s.push(c);
			if(n.flag==0)
				tree.deleteNode(n);
			
		}
	}
	
	public static <T> ComponentTree<T> exec (ComponentTree<T> tree, Image mask)
	{
		return (ComponentTree<T>)(new OpeningByReconstruction<T>()).process(tree,mask);
	}

	
	public static void main(String [] args)
	{
		ComponentTree<Double> tree=ComponentTreeUtil.getTestCase();
		BooleanImage mask=new BooleanImage(tree.getXdim(),tree.getYdim(),1,1,1);
		mask.setPixelXYBoolean(1, 0, true);
		mask.setPixelXYBoolean(2, 0, true);
		//mask.fill(true);
		MultiView mv=MViewer.exec();
		mv.add(tree.image,"Original");
		mv.add(mask,"mask");
		tree.debugDropNodeMap();
		System.out.println(tree.getRoot());
		tree=OpeningByReconstruction.exec(tree, mask);
		tree.debugDropNodeMap();
		mv.add(ReconstructImageFromTree.exec(tree),"reconstruct");
		
		
		
	}

}
