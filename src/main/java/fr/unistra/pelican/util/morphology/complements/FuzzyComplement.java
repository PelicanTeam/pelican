
package fr.unistra.pelican.util.morphology.complements;

import fr.unistra.pelican.Image;

/**
 * Represent the complement operation with respect to fuzzy T-CoNorm
 * @author perret
 *
 */
public abstract class FuzzyComplement {
	
	abstract public double complement(double a);
	
	public String getDescrition()
	{
		return "Description for this special implementation.";
	}
	
	public Image getComplement(Image im)
	{
		Image res= im.copyImage(false);
		for(int i=0;i<im.size();i++)
			res.setPixelDouble(i, complement(im.getPixelDouble(i)));
		return res;
	}
	
}
