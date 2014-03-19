package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Compactness is area divided by square perimetre.
 * Compactness is scale independent
 * @author Benjamin Perret
 *
 */
public class AttributeCompactness extends ComponentAttribute<Double> {

	
	
	/**
	 * 
	 */
	public AttributeCompactness() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 */
	public AttributeCompactness(Double value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#computeAttribute(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentTree)
	 */
	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		for (ComponentNode<T> n : c.iterateFromLeafToRoot()) {
			double perimetre=n.getAttributeValue(AttributePerimetre.class);
			double area=n.getArea();
			n.add(new AttributeCompactness(area/(perimetre*perimetre)));
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
