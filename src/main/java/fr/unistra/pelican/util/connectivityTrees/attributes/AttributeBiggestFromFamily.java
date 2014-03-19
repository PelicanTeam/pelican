/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Attribute is true if the node is biggest (area) among its brothers
 * 
 * @author Benjamin Perret
 *
 */
public class AttributeBiggestFromFamily extends ComponentAttribute<Boolean> {

	
	
	/**
	 * 
	 */
	public AttributeBiggestFromFamily() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 */
	public AttributeBiggestFromFamily(Boolean value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		c.resetFlag(0);
		for(ComponentNode<T> n:c.iterateFromLeafToRoot())
		{
			if(n.numberOfChildren()!=0)
			{
				ComponentNode<T> imax=null;
				int max=-1;
				for(ComponentNode<T> cc:n.getChildren())
				{
					if(cc.getArea()>max)
					{
						max=cc.getArea();
						imax=cc;
					}
				}
				for(ComponentNode<T> cc:n.getChildren())
				{
					cc.add(new AttributeBiggestFromFamily(cc==imax));
					
				}
			}
		}
		c.getRoot().add(new AttributeBiggestFromFamily(false));

		
	}

	@Override
	public <T> void mergeWithNode(ComponentNode<T> c)
			throws UnsupportedDataTypeException {
		// TODO Auto-generated method stub
		
	}

}
