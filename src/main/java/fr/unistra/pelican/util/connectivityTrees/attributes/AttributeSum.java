/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Attribute sum for ComponentTree<double []>.
 * Attribute value is the sum of all pixels values in the node and its children
 * 
 * @author Benjamin Perret
 *
 */
public class AttributeSum extends ComponentAttribute<double []> {

	/**
	 * 
	 */
	public AttributeSum() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 */
	public AttributeSum(double [] value) {
		super(value);
		// TODO Auto-generated constructor stub
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
		Image im=c.image;
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
				{
					ComponentNode<T> n=c.findNodeAt(x, y, z);
					AttributeSum att=getAttr(n,im.bdim);
					for(int b=0;b<im.bdim;b++)
					{
						att.value[b]+=im.getPixelXYZBDouble(x, y, z,b);	
					}

				}
		ComponentNode<T> root=c.getRoot();
		for( ComponentNode<T> n:c.iterateFromLeafToRoot())
		{
				AttributeSum att=getAttr(n,im.bdim);
				for(ComponentNode<T> nn: n.getChildren())
				{
					double [] cs=nn.getAttributeValue(AttributeSum.class);
					for(int b=0;b<cs.length;b++)
						att.value[b]+=cs[b];
				}
				
			
		}

	}

	private AttributeSum getAttr(ComponentNode n, int bdim)
	{
		AttributeSum att=(AttributeSum)n.get(AttributeSum.class);
		if (att==null)
		{
			att=new AttributeSum(new double[bdim]);
			n.add(att);
		}
		return att;
		
	}
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#mergeWithNode(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode)
	 */
	@Override
	public void mergeWithNode(ComponentNode c)
			throws UnsupportedDataTypeException {
		AttributeSum att=getAttr(c,value.length);
		for(int b=0;b<value.length;b++)
			value[b]+=att.value[b];

	}

}
