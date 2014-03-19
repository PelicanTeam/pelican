/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;

/**
 * Attribute filter based on area
 * 
 * @author Benjamin Perret
 *
 */
public class AreaAttributFilter extends AttributeFilter {

	/**
	 * Size limit
	 */
	private int sizeCriterion;
	
	/**
	 * Limit is minimum size or maximum size?
	 * @author Benjamin Perret
	 *
	 */
	public static enum LimiteIs {MIN,MAX};
	
	/**
	 * By default limit is minimu size
	 */
	public LimiteIs limiteIs=LimiteIs.MIN;
	
	/**
	 * @param sizeCriterion
	 */
	public AreaAttributFilter(int sizeCriterion) {
		super();
		this.sizeCriterion = sizeCriterion;
		
	}



	/**
	 * @param sizeCriterion
	 * @param limiteIs
	 */
	public AreaAttributFilter(int sizeCriterion, LimiteIs limiteIs) {
		super();
		this.sizeCriterion = sizeCriterion;
		this.limiteIs = limiteIs;
	}



	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Attributes.AttributeFilter#filter(fr.unistra.pelican.algorithms.experimental.perret.CC.ComponentNode)
	 */
	@Override
	public boolean filter(ComponentNode node) {
		boolean res;
		switch (limiteIs)
		{		
		case MAX:
			res=node.getArea()<sizeCriterion;
			break;
		default:
			res=node.getArea()>sizeCriterion;
			break;	
		}
		return res;
	}



	public int getSizeCriterion() {
		return sizeCriterion;
	}



	public void setSizeCriterion(int sizeCriterion) {
		this.sizeCriterion = sizeCriterion;
	}
	
	

}
