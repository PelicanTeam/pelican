/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.connected.ReconstructImageFromTree.Data;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.util.IMath;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.attributes.AreaAttributFilter;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeFilter;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeKZone;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;

/**
 * Filter a component tree assuming a k-zone hyper-connection 
 * see G.K. Ouzounis and M.H.F. Wilkinson
 * @author Benjamin Perret
 *
 */
public class FilterComponentTreeKZone extends Algorithm {

	/**
	 * Filtering strategy available
	 * @author Benjamin Perret
	 *
	 */
	public static enum Rule {Substractive, Absorption};
	
	/**
	 * Max range parameter
	 */
	public double k;
	
	/**
	 * Filters to apply
	 */
	public AttributeFilter [] filters;
	
	/**
	 * Tree to filter
	 */
	public ComponentTree<Double> tree;
	
	/**
	 * Filtering strategy to use
	 */
	public Rule rule=Rule.Substractive;
	
	public FilterComponentTreeKZone(){
		super.inputs="tree,k,filters";
		super.options="rule";
		super.outputs="tree";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		tree.addAttribute(new AttributeKZone<Double>(0.0,0.0));
		switch(rule){
		case Substractive:
			substractive();
			break;
		case Absorption:
			absorption();
			break;	
		}
	
	}
	
	private void substractive(){
		ComponentNode<Double> root=tree.getRoot();
		double level=root.getLevel();
		
		AttributeKZone<Double> att=root.getAttribute(AttributeKZone.class);
		if (checkFilters(root))
		{
			att.setKprime(k);
			att.setValue(level);
		}else{
			att.setKprime(0.0);
			att.setValue(0.0);
		}
		for(ComponentNode<Double> node:tree.iterateFromRootToLeaf())
		{
			if(node.parent!=null){
				ComponentNode<Double> parent=node.parent;
				att=node.getAttribute(AttributeKZone.class);
				AttributeKZone<Double> attparent=parent.getAttribute(AttributeKZone.class);
				double difflevel=node.getLevel()-parent.getLevel();
				if(node.getHighest()-parent.getLevel()>k && checkFilters(node)){
					att.setValue(attparent.getValue()+difflevel);
					att.setKprime(k);
				}else{
					if (difflevel>attparent.getKprime())
					{
						att.setValue(attparent.getValue() + attparent.getKprime());
						att.setKprime(0.0);
					}else{
						att.setValue(attparent.getValue() + difflevel);
						att.setKprime(attparent.getKprime()-difflevel);
					}
				}
			}
		}
	}

	private void absorption(){
		ComponentNode<Double> root=tree.getRoot();
		double level=root.getLevel();
		
		AttributeKZone<Double> att=root.getAttribute(AttributeKZone.class);
		if (checkFilters(root))
		{
			att.setKprime(k);
			att.setValue(level);
		}else{
			att.setKprime(0.0);
			att.setValue(0.0);
		}
		for(ComponentNode<Double> node:tree.iterateFromRootToLeaf())
		{
			if(node.parent!=null){
				ComponentNode<Double> parent=node.parent;
				att=node.getAttribute(AttributeKZone.class);
				AttributeKZone<Double> attparent=parent.getAttribute(AttributeKZone.class);
				double difflevel=node.getLevel()-parent.getLevel();
				if(node.getHighest()-parent.getLevel()>k){
					if(checkFilters(node))
					{
						if(attparent.getKprime()>=0.0){
							att.setValue(attparent.getValue()+difflevel);
							att.setKprime(k);
						}else{
							att.setValue(attparent.getValue());
							att.setKprime(attparent.getKprime()+difflevel);
							if(att.getKprime()>0)
							{
								att.setValue(att.getValue()+att.getKprime()+k);
								att.setKprime(k);
							}
						}
					}
					else{
						att.setValue(attparent.getValue());
						att.setKprime(-k);
					}
					
				}else{
					if (difflevel>attparent.getKprime())
					{
						att.setValue(attparent.getValue() + Math.max(attparent.getKprime(),0));
						att.setKprime(0.0);
					}else{
						att.setValue(attparent.getValue() + difflevel);
						att.setKprime(attparent.getKprime()-difflevel);
					}
				}
			}
		}
	}
	
	private boolean checkFilters(ComponentNode c)
	{
		boolean res=true;
		for(AttributeFilter f: filters)
		{
			res=f.filter(c);
			if(!res) break;
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static  ComponentTree<Double> exec(ComponentTree<Double> tree, double k, AttributeFilter filter ){
		return (ComponentTree<Double>)new FilterComponentTreeKZone().process(tree,k,new AttributeFilter[]{filter});
	}
	
	@SuppressWarnings("unchecked")
	public static  ComponentTree<Double> exec(ComponentTree<Double> tree, double k, AttributeFilter [] filters ){
		return (ComponentTree<Double>)new FilterComponentTreeKZone().process(tree,k,filters);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Image im=new DoubleImage(ImageLoader.exec("samples/lennaGray256.png"),true);
		IMath.scaleToZeroOne(im);
		ComponentTree<Double> tree=BuildComponentTree.exec(im, TrivialConnectivity.getHeightNeighbourhood());
		AttributeFilter filter=new AreaAttributFilter(100);
		tree=FilterComponentTreeKZone.exec(tree, 20.0/255.0, filter);
		Image res= ReconstructImageFromTree.exec(tree, Data.Attribute, AttributeKZone.class);
		MViewer.exec(im,res, Difference.exec(im, res,false));
		

	}

}
