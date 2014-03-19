/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import java.util.Stack;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;

/**
 * @author Benjamin Perret
 *
 */
public class AttributePerimetre extends ComponentAttribute<Double> {

	/**
	 * 
	 */
	public AttributePerimetre() {
		super();
		// TODO Auto-generated constructor stub
	}




	/**
	 * @param value
	 */
	public AttributePerimetre(Double value) {
		super(value);
		// TODO Auto-generated constructor stub
	}




	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#computeAttribute(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentTree)
	 */
	@Override
	public <T> void computeAttribute(ComponentTree<T> c)
			throws UnsupportedDataTypeException {
		
		
		
		
		//List<Point3D> l=c.getRoot().getAttributeValue(AttributePointList.class);
		Stack<Point3D> s = new Stack<Point3D>();
		Connectivity3D con=c.getConnectivity();
		Connectivity3D c4=TrivialConnectivity.getFourNeighbourhood();
		BooleanImage mask = new BooleanImage(c.getXdim(),c.getYdim(),c.getZdim(),1,1);
		//System.out.println(con);
		ComponentNode<T> root=c.getRoot();
		//if(l==null)
		//	c.addAttribute(new AttributePointList());
		for (ComponentNode<T> n : c.iterateFromLeafToRoot()) {
			if (n == root) {
				//System.out.println("addroot");
				n.add(new AttributePerimetre(
						(double) (2.0*(mask.xdim + mask.ydim ))));
			} else {
				s.push(n.location);

				mask.fill(true);
				mask.setPixelXYZBoolean(n.location.x, n.location.y,
						n.location.z, false);
				// System.out.println("Location:" + n.location);
				double per = 0;
				while (!s.isEmpty()) {
					Point3D p = s.pop();
					// System.out.println(p);
					c4.setCurrentPoint(p);

					for (Point3D pp : c4) {
						if (pp.x < 0 || pp.y < 0 || pp.z < 0
								|| pp.x >= mask.xdim || pp.y >= mask.ydim
								|| pp.z >= mask.zdim || !c.isMember(pp, n)) // !AttributePointList.contains(n,
																			// pp))
						{

							per++;
							// System.out.println("permitre " + pp + " " + per);
						}
					}
					con.setCurrentPoint(p);
					for (Point3D pp : con) {
						// System.out.print("->Watching for " +pp);
						if (pp.x >= 0 && pp.y >= 0 && pp.z >= 0
								&& pp.x < mask.xdim && pp.y < mask.ydim
								&& pp.z < mask.zdim
								&& mask.getPixelXYZBoolean(pp.x, pp.y, pp.z)
								&& c.isMember(pp, n)) // AttributePointList.contains(n,
														// pp) )
						{ // System.out.println("Added");
							s.push(new Point3D(pp));
							mask.setPixelXYZBoolean(pp.x, pp.y, pp.z, false);
						}
						// else{System.out.println("no");}
					}

				}
				n.add(new AttributePerimetre(per));
			}
		}
		return;
	}

	
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#mergeWithNode(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode)
	 */
	@Override
	public <T> void mergeWithNode(ComponentNode<T> c)
			throws UnsupportedDataTypeException {
		// TODO Auto-generated method stub
		return ;
	}

}
