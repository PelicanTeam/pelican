package fr.unistra.pelican.util.connectivityTrees.attributes;

import fr.unistra.pelican.util.connectivityTrees.ComponentNode;

/**
 * Create an attribute filter based on a threshold applied to an attribute
 * 
 * Works only with attribute holding a double value
 * 
 * @author Benjamin Perret
 *
 */
public class ComponentAttributeThresholdFilter extends AttributeFilter {

	private Class<? extends ComponentAttribute> clazz;
	
	private double threshold;
	
	public ComponentAttributeThresholdFilter(Class<? extends ComponentAttribute> clazz, double threshold)
	{
		this.clazz=clazz;
		this.threshold=threshold;
	}
	
	@Override
	public boolean filter(ComponentNode node) throws AttributeNotFoundException{
		
		Number v=(Double)node.getAttributeValue(clazz);
		if(v==null)
			throw new AttributeNotFoundException("Cannot find attribute " + clazz.getSimpleName());
		return v.doubleValue() >= threshold;
		
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
