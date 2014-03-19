/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.VMath;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributePointList;
import fr.unistra.pelican.util.vectorial.ordering.VectorialBasedComponentOrdering;
import fr.unistra.pelican.util.vectorial.ordering.VectorialOrdering;

/**
 * Transform a vectorial component tree in fuzzy h-component tree. 
 * 
 * The vectorial order used to build the tree must be translation invariant.
 * 
 * Input image is assumed to take its values [0,1]^b.
 * Parameter k must also be in [0,1]^b.
 * In short, all nodes having contrast lower than k will be absorbed by its parent.
 * During processing the tree will be equipped with the attribute Point List.
 * 
 * This works with double [] tree, you can always process monoband images using a double [] tree with a lexicographic ordering.
 * 
 * SEE: B. Perret et al.TIP 2011.
 * 
 * @author Benjamin Perret
 *
 */
public class SimplifyTreeWithFuzzyHConnection extends Algorithm {

	/**
	 * Tree to be simplified
	 */
	public ComponentTree<double []> tree;
	
	/**
	 * k parameter (k in [0,1]) (this is equal to 1-tau in the article)
	 */
	public double [] k;
	
	public SimplifyTreeWithFuzzyHConnection(){
		this.inputs="tree,k";
		this.outputs="tree";
	}
	

	/**
	 * underlying image
	 */
	private Image im;
	
	/**
	 * Underlying vectorial ordering
	 */
	private VectorialOrdering vo;
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		if(tree.getRoot().get(AttributePointList.class)==null)
			tree.addAttribute(new AttributePointList());
		vo=((VectorialBasedComponentOrdering)tree.getComparator()).getVectorialOrdering();

		im=tree.image;
		Stack<ComponentNode<double[]>> s=new Stack<ComponentNode<double[]>>();
		s.push(tree.getRoot());
		while(!s.isEmpty())
		{
			ComponentNode<double []> n=s.pop();
			while(n.numberOfChildren()==1)
			{
				n.setLevel(n.getChild(0).getLevel());
				tree.deleteNode(n.getChild(0));
			}
			
			double [] level=n.getLevel();
			double [] peak=n.getHighest();
			boolean flag=true;
			double [] nlevel=VMath.addF(level, k);
			if(n.parent!=null)
			{
				double [] plevel=n.parent.getLevel();
				//double [] range=VMath.addF(plevel, k);
				if(vo.compare(plevel, peak)>=0)
				{
					tree.deleteNodeAndChildren(n);
					flag=false;
				}
			} else{
				
			}
			if(flag){
				nlevel=vo.min(peak,nlevel);

				n.setLevel(nlevel);

				for(ComponentNode<double []> c:n.getChildren())
					s.push(c);
			}
		}
		s.push(tree.getRoot());
		while(!s.isEmpty())
		{
			ComponentNode<double[]> n=s.pop();
			while(n.numberOfChildren()==1)
			{
				n.setLevel(n.getChild(0).getLevel());
				tree.deleteNode(n.getChild(0));
			}
			stealPoints(n,n.getLevel());
			for(ComponentNode<double []> c:n.getChildren())
				s.push(c);
		}
	}

	
	private void stealPoints(ComponentNode<double[]> receiver, double [] upTo)
	{
		AttributePointList list=receiver.get(AttributePointList.class);
		List<Point3D> plr=list.getValue();
		Stack<ComponentNode<double[]>> s=new Stack<ComponentNode<double[]>>();

		for(ComponentNode<double []> c:receiver.getChildren())
			s.push(c);
		//System.out.println("tbt " + receiver.location + " is the receiver. Max level is  " + Arrays.toString(upTo));
		while(!s.isEmpty())
		{
			ComponentNode<double[]> n=s.pop();
			
			AttributePointList pts=n.get(AttributePointList.class);
			List<Point3D> pl=pts.getValue();
			int size=pl.size();

			for(int i=0;i<size;i++){
				Point3D p=pl.get(i);
				if(vo.compare(im.getVectorPixelXYZDouble(p.x, p.y, p.z),upTo)<=0)
				{
					//System.out.println("point  " + p + " stolen  " );
					plr.add(p);
					pl.remove(p);
					i--;
					size--;
					n.setArea(n.getArea()-1);
					
					tree.givePointToNode(receiver, p); 
				}
			}
			
			if(vo.compare(n.getLevel(), upTo)<=0)
			{
				
				for(ComponentNode<double []> c:n.getChildren())
					s.push(c);
			}
		
		}
	
	}
	
	/**
	 * Simplify the given component tree to obtain a fuzzy h-component tree
	 * Original image is expected to take its values in [0,1]^b, other uses are hazardous!
	 * @param tree the tree
	 * @param k fuzzy parameter
	 * @return the new tree
	 */
	public static ComponentTree<double[]> exec(ComponentTree<double[]> tree, double [] k)
	{
		return (ComponentTree<double[]>)new SimplifyTreeWithFuzzyHConnection().process(tree,k);
	}
	


}
