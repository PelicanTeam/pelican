/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;



import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Add a list of all points contained in the given node.
 * For memory considerations, the points of its children are NOT included in this list
 * 
 * @author Benjamin Perret
 *
 */
public class AttributePointList extends ComponentAttribute<List<Point3D>> {

	public AttributePointList() {
		super();
	}
	
	public AttributePointList(List<Point3D> value) {
		super(value);
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#computeAttribute(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentTree)
	 */
	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		
		int xdim=c.getXdim();
		int ydim=c.getYdim();
		int zdim=c.getZdim();
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
				{
					ComponentNode<T> n =c.findNodeAt(x, y, z);
					List<Point3D> list=n.getAttributeValue(AttributePointList.class);							
					if(list==null)
					{
						list=new ArrayList<Point3D>();
						n.add(new AttributePointList(list));
					}
					list.add(new Point3D(x,y,z));
				}
		
		for(ComponentNode<T> n:c.iterateFromRootToLeaf())
		{
			if(n.getAttributeValue(AttributePointList.class)==null)
				n.add(new AttributePointList(new ArrayList<Point3D>()));
			
		}
		return ;
		
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#mergeWithNode(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode)
	 */
	@Override
	public <T> void mergeWithNode(ComponentNode<T> c)
			throws UnsupportedDataTypeException {
		List<Point3D> list1=c.getAttributeValue(AttributePointList.class);
		value.addAll(list1);
		return ;
	}

	private static  Stack<ComponentNode> s=new Stack<ComponentNode>();
	
	public static <T>  boolean contains(ComponentNode<T> n, Point3D p)
	{
		boolean res=false;
		s.clear();
		s.push(n);
		while(!s.isEmpty())
		{
			ComponentNode<T> c=s.pop();
			List<Point3D> l=c.getAttributeValue(AttributePointList.class);
			if(l.contains(p))
				return true;
			for(ComponentNode<T> nn :c.getChildren())
				s.push(nn);
		}
		return res;
		
	}
	
}
