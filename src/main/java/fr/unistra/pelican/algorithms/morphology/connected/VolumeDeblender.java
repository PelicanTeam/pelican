/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.connected.ReconstructImageFromTree.Data;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.ComponentTreeUtil;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeEnergy;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeEnergyPerPixel;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeSum;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeVolume;
import fr.unistra.pelican.util.connectivityTrees.attributes.UnsupportedDataTypeException;

/**
 * Source deblending algorithm. Developed for astronomical images, perhaps other uses exist.
 * 
 * The algorithm filters a component tree to perform sources separation.
 * 
 * A node is an independent source if :
 * - its volume is bigger than a given fraction of the volume of its parent
 * - at least one other child of its parent fulfill previous condition 
 * 
 * @author Benjamin Perret
 *
 */
public class  VolumeDeblender<T> extends Algorithm {

	/**
	 * The tree to filter
	 */
	public ComponentTree<T> tree;
	
	/**
	 * print debug info?
	 */
	private boolean DEBUG=false;
	
	/**
	 * threshold 
	 */
	public double fractionThreshold=0.005;
	
	
	public double energyThreshold=Double.POSITIVE_INFINITY;
	/**
	 * 
	 */
	public VolumeDeblender() {
		super.inputs="tree";
		super.options="fractionThreshold,energyThreshold";
		super.outputs="tree";
	}

	
	private double getVolume(ComponentNode<Double> n)
	{
		return n.getAttributeValue(AttributeVolume.class);
	}
	

	
	private void filter3()
	{
		ComponentNode<T> root = tree.getRoot();
		double volume = root.getAttributeValue(AttributeVolume.class);
		
		double t = fractionThreshold * volume;
		//System.out.println("threshold " + t);
		tree.resetFlag(0);
	//	MViewer.exec(ReconstructImageFromTree.exec(tree, Data.Attribute, AttributeEnergyPerPixel.class));
		for (ComponentNode<T> n : tree.iterateFromLeafToRoot()) {
			if (n != root) {

				// if (n.numberOfChildren() < 2)
				// tree.deleteNode(n);
				// else {
				int count = 0;
			//	System.out.println("Going for " + n.location);
				// double volume=n.getAttributeValue(AttributeVolume.class);
				// double t=fractionThreshold*volume;
				/*volume = n.getAttributeValue(AttributeVolume.class);
				
				t = fractionThreshold * volume;*/
				for (ComponentNode<T> child : n.getChildren()) {
					
					double vv = child.getAttributeValue(AttributeVolume.class);
					if (child.flag!=1 && vv >= t) {
				//		System.out.println("-child " + child.location
				//				+ " is ok (v=" + vv);
						count++;
					}
				}
				//System.out.println("->count " + count + " on "
				//		+ n.numberOfChildren());
				
				for (ComponentNode<T> child : n.getChildren()) {
					double vv = child.getAttributeValue(AttributeVolume.class);
					double ee = child.getAttributeValue(AttributeEnergyPerPixel.class);
					if ( (count < 2 || vv < t) ) {
						//System.out.println("-->delete " + child.location
						//		+ " volume " + vv);
						// tree.deleteNode(child);
						if(ee < energyThreshold)
							child.flag = 1;
						else {
							//System.out.println("-->saved1  " + child.location	+ " volume " + vv);
						}
					} else {
						//System.out.println("-->saved2  " + child.location	+ " volume " + vv +" count " +count );
					}
				}
			}
		}
		
		tree.deleteOldNodeWithFlag(1);
		
		
	}
	
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		filter3();

	}

	public static <T> ComponentTree<T> exec(ComponentTree<T> tree)
	{
		return (ComponentTree<T>)(new VolumeDeblender<T>()).process(tree);
	}
	
	public static <T> ComponentTree<T> exec(ComponentTree<T> tree, double fractionThreshold, double energyThreshold)
	{
		return (ComponentTree<T>)(new VolumeDeblender<T>()).process(tree,fractionThreshold,energyThreshold);
	}
	
	/**
	 * @param args
	 * @throws UnsupportedDataTypeException 
	 */
	public static void main(String[] args)  {
		ComponentTree<double []> root = ComponentTreeUtil.getTestCaseVectorial();
		
		try {
			System.out.println(root.getRoot());
			root.addAttribute(new AttributeSum());
			root.addAttribute(new AttributeVolume());
			root=VolumeDeblender.exec(root);
			Image im0=ReconstructImageFromTree.exec(root,Data.Attribute,AttributeVolume.class);
			Image im1=ReconstructImageFromTree.exec(root);
			MViewer.exec(root.image,im0,im1);
			
		} catch (UnsupportedDataTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
