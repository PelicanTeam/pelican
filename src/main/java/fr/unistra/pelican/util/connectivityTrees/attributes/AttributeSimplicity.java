package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Simplicity is defined area divided by perimeter.
 * 
 * It assumes that attribute Perimetre already exists.
 * 
 * Can be computed on any type of ComponentTree
 * 
 * @author Benjamin Perret
 *
 */
public class AttributeSimplicity extends ComponentAttribute<Double> {

	/**
	 * 
	 */
	public AttributeSimplicity() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 */
	public AttributeSimplicity(Double value) {
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
			n.add(new AttributeSimplicity(area/perimetre));
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
