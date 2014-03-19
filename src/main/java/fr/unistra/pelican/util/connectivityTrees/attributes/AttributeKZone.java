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
public class AttributeKZone<E> extends ComponentAttribute<E> {

	private E kprime;
	
	

	/**
	 * @param kprime
	 */
	public AttributeKZone(E value, E kprime) {
		super(value);
		this.kprime = kprime;
	}

	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		for(ComponentNode<T> n:c.iterateFromRootToLeaf())
			n.add(new AttributeKZone<E>(value,kprime));
		
	}

	@Override
	public <T> void mergeWithNode(ComponentNode<T> c)
			throws UnsupportedDataTypeException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the kprime
	 */
	public E getKprime() {
		return kprime;
	}

	/**
	 * @param kprime the kprime to set
	 */
	public void setKprime(E kprime) {
		this.kprime = kprime;
	}
	
	/**
	 * @param kprime the kprime to set
	 */
	public void setValue(E value) {
		this.value = value;
	}

}
