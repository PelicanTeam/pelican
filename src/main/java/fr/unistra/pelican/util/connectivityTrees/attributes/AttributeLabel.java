/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * @author Benjamin Perret
 *
 */
public class AttributeLabel extends ComponentAttribute<Integer> {

	
	
	/**
	 * 
	 */
	public AttributeLabel() {
		super();
	}

	/**
	 * @param value
	 */
	public AttributeLabel(Integer value) {
		super(value);
		
	}

	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		int l=0;
		for(ComponentNode<T> n:c.iterateFromRootToLeaf())
			n.add(new AttributeLabel(l++));
		
	}

	@Override
	public <T> void mergeWithNode(ComponentNode<T> c)
			throws UnsupportedDataTypeException {
		// TODO Auto-generated method stub
		
	}

}
