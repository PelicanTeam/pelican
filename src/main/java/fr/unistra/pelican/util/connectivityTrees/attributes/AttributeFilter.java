/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;

/**
 * Abstract definition of an attribute filter
 * 
 * @author Benjamin Perret
 *
 */
public abstract class AttributeFilter {

	/**
	 * says whether or not the node fulfill attribute filter criteria
	 * @param node the component node to test
	 * @return result of the test
	 * @throws AttributeNotFoundException The needed attribute is not found in given ComponentNode
	 */
	public abstract boolean filter(ComponentNode node) throws AttributeNotFoundException;
}
