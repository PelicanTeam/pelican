/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Energy is the sum of the norm of each pixel in the node and its children
 * 
 * @author Benjamin Perret
 *
 */
public class AttributeEnergy extends ComponentAttribute<Double> {

	/**
	 * 
	 */
	public AttributeEnergy() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 */
	public AttributeEnergy(Double value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#computeAttribute(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentTree)
	 */
	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		c.resetFlag(0);
		int xdim=c.getXdim();
		int ydim=c.getYdim();
		int zdim=c.getZdim();
		Image im=c.image;
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
				{
					ComponentNode<T> n=c.findNodeAt(x, y, z);
					AttributeEnergy att=getAttr(n);
					double e=0.0;
					for(int b=0;b<im.bdim;b++)
					{
						double v=im.getPixelXYZBDouble(x, y, z,b);
						e+=v*v;
					}
					att.value+=Math.sqrt(e);
					n.flag=1;
				}
		ComponentNode<T> root=c.getRoot();
		for( ComponentNode<T> n:c.iterateFromLeafToRoot())
		{
			if(n==root)
				n.add(new AttributeEnergy(0.0));
			else if(n.flag==0)
			{
				double s=0.0;
				for(ComponentNode<T> nn: n.getChildren())
				{
					s+=nn.getAttributeValue(AttributeEnergy.class);
				}
				n.add(new AttributeEnergy(s));
			}
		}

	}

	private AttributeEnergy getAttr(ComponentNode n)
	{
		AttributeEnergy att=(AttributeEnergy)n.get(AttributeEnergy.class);
		if (att==null)
		{
			att=new AttributeEnergy(0.0);
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
		// TODO Auto-generated method stub

	}

}
