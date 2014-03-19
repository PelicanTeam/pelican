/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;

import java.util.Comparator;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;

/**
 * Anything that can order component nodes...
 * @author Benjamin Perret
 *
 */
public abstract class ComponentNodeOrdering <T> implements  Comparator<ComponentNode<T>> {

	@Override
	public abstract int compare(ComponentNode<T> o1, ComponentNode<T> o2);

}
