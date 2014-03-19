/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * An attribute that can be added to a node
 * 
 * The attribute must furnish an algorithm to compute its value for each node
 * 
 * @author Benjamin Perret
 * @param <E> type of the value hold by the attribute
 */
public abstract class ComponentAttribute<E> {

	/**
	 * Value of the algorithm
	 */
	protected E value;
	
	/**
	 * get attribute value
	 * @return
	 */
	public E getValue()
	{
		return value;
	}
	
	/**
	 * set attribute value
	 */
	public void setValue(E value)
	{
		this.value=value;
	}
	
	/**
	 * Create a new Component attribute, generally used with method addAttribute of class ComponentTree when value is unknown
	 */
	public ComponentAttribute() {
		super();
	}



	/**
	 * Create a new Component attribute initialized with given value
	 * @param value
	 */
	public ComponentAttribute(E value) {
		super();
		this.value = value;
	}



	/**
	 * Compute attribute value for all nodes of the given tree 
	 * @param <T> type of value hold by component nodes
	 * @param c the tree
	 * @throws UnsupportedDataTypeException
	 */
	public  abstract  <T> void computeAttribute(ComponentTree<T> c) throws UnsupportedDataTypeException;
	
	/**
	 * Merge value of two nodes (not supported yet)
	 * @param <T>
	 * @param c
	 * @throws UnsupportedDataTypeException
	 */
	public abstract <T> void mergeWithNode(ComponentNode<T> c) throws UnsupportedDataTypeException;
}
