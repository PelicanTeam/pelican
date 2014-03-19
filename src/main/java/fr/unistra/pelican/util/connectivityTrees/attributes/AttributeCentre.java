/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;



import java.util.List;

import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Center of all points in the node and its children
 * 
 * Center compuation is NOT weighted by pixel values.
 * 
 * @author Benjamin Perret
 *
 */
public class AttributeCentre extends ComponentAttribute<Point3D> {

	
	
	/**
	 * 
	 */
	public AttributeCentre() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 */
	public AttributeCentre(Point3D value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		for(ComponentNode<T> n : c.iterateFromLeafToRoot())
		{
			AttributePointList att=(AttributePointList)n.getAttribute(AttributePointList.class);
			List<Point3D> l=att.getValue();
			Point3D centre=new Point3D();
			int nb=0;
			for(Point3D pp : l)
			{
				centre.x+=pp.x;
				centre.y+=pp.y;
				centre.z+=pp.z;
				nb++;
			}
			for(int i=0;i<n.numberOfChildren();i++)
			{
				ComponentNode<T> ch=n.getChild(i);
				Point3D pc=ch.getAttributeValue(AttributeCentre.class);
				int area=ch.getArea();
				centre.x+=area*pc.x;
				centre.y+=area*pc.y;
				centre.z+=area*pc.z;
				nb+=area;
			}
			centre.x/=nb;
			centre.y/=nb;
			centre.z/=nb;
			n.add(new AttributeCentre(centre));
		}
		
	}

	@Override
	public <T> void mergeWithNode(ComponentNode<T> c)
			throws UnsupportedDataTypeException {
		// TODO Auto-generated method stub
		
	}

}
