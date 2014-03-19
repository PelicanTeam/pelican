package fr.unistra.pelican.util.morphology;

import fr.unistra.pelican.BooleanImage;

/**
 * 
 * @author Jonathan Weber
 *	
 */


public class ValuedMonoBandFlatStructuringElement implements StructuringElement {

	private BooleanImage se;
	private int bande;
	private double seuil;
	private boolean interne;
	
	public ValuedMonoBandFlatStructuringElement (BooleanImage se,int bande,double seuil,boolean interne)
	{
		this.se=se;
		this.bande = bande;
		this.seuil =seuil;
		this.interne=interne;
	}
	
	public ValuedMonoBandFlatStructuringElement (int bande,double seuil,boolean interne)
	{
		this.bande = bande;
		this.seuil =seuil;
		this.interne=interne;
	}
	
	
	public int getBande() {
		return bande;
	}
	public void setBande(int bande) {
		this.bande = bande;
	}
	public boolean isInterne() {
		return interne;
	}
	public boolean isExterne() {
		return !interne;
	}
	public void setInterne() {
		this.interne = true;
	}
	public void setExterne() {
		this.interne = false;
	}
	
	public BooleanImage getSe() {
		return se;
	}
	public void setSe(BooleanImage se) {
		this.se = se;
	}
	public double getSeuil() {
		return seuil;
	}
	public void setSeuil(double seuil) {
		this.seuil = seuil;
	}
	
	
	
}
