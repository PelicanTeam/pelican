/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;

/**
 * Compute volume of the node.
 * Can work with double or double [] component tree
 * In the first case definition of the volume is the traditional one.
 * In the other case see my report for the definition of the vectorial volume
 * 
 * @author Benjamin Perret
 *
 */
public class AttributeVolume extends ComponentAttribute<Double> {

	public AttributeVolume() {
		super();
		
	}
	
	public AttributeVolume(Double value) {
		super(value);
		
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#computeAttribute(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode)
	 */
	@Override
	public <T> void computeAttribute(ComponentTree<T> c) throws UnsupportedDataTypeException{
		value = 0.0; // n.getArea();

		/*for (ComponentNode<T> child : c.getChildren()) {
			AttributeVolume a = new AttributeVolume();
			child.add(a);
			value += a.computeAttribute(child);// +
												// child.getArea()*(child.getLevel()-n.getLevel());
		}
		ComponentNode<T> parent = c.getParent();
		T val = c.getLevel();
		if (val instanceof Double) {
			if (parent != null) {
				value += c.getArea()
						* ((Double) val - (Double) parent.getLevel());
			} else {
				value += c.getArea() * ((Double) val);
			}
		} else {throw new UnsupportedDataTypeException("Cannot compute attribute volume over datatype " + val.getClass());}
*/	boolean arrayFlag=false;
		for( ComponentNode<T> n:c.iterateFromLeafToRoot())
		{
			value=0.0;
			ComponentNode<T> parent = n.getParent();
			T val = n.getLevel();
			if (val instanceof Double) {
				for (ComponentNode<T> child : n.getChildren()) {
					
					value += child.getAttributeValue(this.getClass());// +
														// child.getArea()*(child.getLevel()-n.getLevel());
				}
				
				if (parent != null) {
					value += n.getArea()
							* ((Double) val - (Double) parent.getLevel());
				} else {
					value += n.getArea() * ((Double) val);
				}
				n.add(new AttributeVolume(value));
			} else if (val instanceof double []) {
				arrayFlag=true;
				//System.out.println("node " +n.location );
				double area= n.getArea();
				double [] minlvl;
				if (parent != null) {
					minlvl=Tools.VectorDifference((double [])val, (double [])parent.getLevel());
				} else {
					minlvl=(double [])val;
				}
				//System.out.println("val " +ArrayToolbox.printString(val) +"l2-l1 " + ArrayToolbox.printString(minlvl) );
				double basePixelVolume=Tools.DotProduct(minlvl, minlvl);
				value+=basePixelVolume*area;
				for (ComponentNode<T> child : n.getChildren()) {
					//System.out.println("->child  " + child.location );
					value += child.getAttributeValue(this.getClass());
					//double [] clevel = (double [])child.getLevel();
					double cArea=child.getArea();// +
					double [] sum=child.getAttributeValue(AttributeSum.class);
					double a =  Tools.DotProduct(sum, minlvl);
					double b=cArea*Tools.DotProduct((double [])val, minlvl);
					//System.out.println("->area  " + cArea + "  sum " + ArrayToolbox.printString(sum) );
					value +=2.0*(a-b);
														// child.getArea()*(child.getLevel()-n.getLevel());
				}
				
				
				n.add(new AttributeVolume(value));
			} else {throw new UnsupportedDataTypeException("Cannot compute attribute volume over datatype " + val.getClass());}
		
		}
		if(arrayFlag)
			for( ComponentNode<T> n:c.iterateFromLeafToRoot())
			{
				ComponentAttribute<Double> v=(ComponentAttribute<Double>)n.getAttribute(AttributeVolume.class);
				v.value=Math.sqrt(v.value);
			}
		return ;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.ComponentAttribute#mergeNodes(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode, fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode)
	 */
	@Override
	public <T> void mergeWithNode(ComponentNode<T> c) throws UnsupportedDataTypeException{
		T val = c.getLevel();
		if (val instanceof Double) {
			value += (Double)val;
		} else {throw new UnsupportedDataTypeException("Cannot compute attribute volume over datatype " + val.getClass());}
		return ;
	}

}
