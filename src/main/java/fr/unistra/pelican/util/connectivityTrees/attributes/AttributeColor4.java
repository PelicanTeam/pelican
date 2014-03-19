package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
/**
 * Color is simply the difference between two bands
 * 
 * @author Benjamin Perret
 *
 */
public class AttributeColor4 extends ComponentAttribute<Double> {

	private int b1;
	
	private int b2;
	
	public AttributeColor4(int b1, int b2)
	{
		this.b1=b1;
		this.b2=b2;
	}
	
	public AttributeColor4(int b1, int b2,double d) {
		this.b1=b1;
		this.b2=b2;
		this.value=d;
	}

	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		for (ComponentNode<T> n : c.iterateFromLeafToRoot()) {
			double [] sum=n.getAttributeValue(AttributeSum.class);
			
			n.add(new AttributeColor4(b1,b2,sum[b1]-sum[b2]));
		}
		
	}

	@Override
	public <T> void mergeWithNode(ComponentNode<T> c)
			throws UnsupportedDataTypeException {
		// TODO Auto-generated method stub
		
	}
}
