/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Simply Energy divided by area.
 * 
 * Assumes that AttributeEnergy was already added to tree
 * 
 * @author Benjamin Perret
 *
 */
public class AttributeEnergyPerPixel extends ComponentAttribute<Double> {

	/**
	 * 
	 */
	public AttributeEnergyPerPixel() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 */
	public AttributeEnergyPerPixel(Double value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#computeAttribute(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentTree)
	 */
	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
		throws UnsupportedDataTypeException {
		for(ComponentNode<T> n:c.iterateFromLeafToRoot())
		{
			double v=n.getAttributeValue(AttributeEnergy.class);
			double area=n.getArea();
			n.add(new AttributeEnergyPerPixel(v/area));
		}

	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#mergeWithNode(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode)
	 */
	@Override
	public void mergeWithNode(ComponentNode c)
			throws UnsupportedDataTypeException {
		// TODO Auto-generated method stub

	}

}
