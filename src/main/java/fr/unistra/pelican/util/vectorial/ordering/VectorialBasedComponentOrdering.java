/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;

/**
 * Component ordering based on a vectorial ordering of component levels
 * 
 * @author Benjamin Perret
 *
 */
public class VectorialBasedComponentOrdering extends ComponentNodeOrdering<double []> {

	private VectorialOrdering vo;
	
	public VectorialBasedComponentOrdering(VectorialOrdering vo){
		this.vo=vo;
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Ordering.ComponentNodeOrdering#compare(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode, fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode)
	 */
	@Override
	public int compare(ComponentNode<double []> o1, ComponentNode<double []> o2) {
		return vo.compare(o1.getLevel(), o2.getLevel());
	}

	/**
	 * @return the vo
	 */
	public VectorialOrdering getVectorialOrdering() {
		return vo;
	}

	

}
