package fr.unistra.pelican.util.morphology.fuzzyNorms;

import fr.unistra.pelican.PelicanException;

public class FuzzyYagerNorm extends FuzzyNorm {

	private double omega;
	
	public FuzzyYagerNorm(double omega)
	{
		if (omega<=0.0 )
			throw new PelicanException("aomega parametre invalid");
		this.omega=omega;
	}
	
	public double tCoDistance(double a, double b) {	
		return Math.min(1.0,Math.pow(Math.pow(a,omega)+Math.pow(b,omega),1.0/omega));
	}

	public double tDistance(double a, double b) {

		return 1.0-Math.min(1.0,Math.pow(Math.pow(1.0-a,omega)+Math.pow(1.0-b,omega),1.0/omega));
	}

}
