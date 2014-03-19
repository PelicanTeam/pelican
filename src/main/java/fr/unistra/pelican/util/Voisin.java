package fr.unistra.pelican.util;
/**
 * Such a small class doesn't have to be explained
 * 
 * @author Dany Damaj
 *
 */

public class Voisin {
	public int x,y;
	public float d; //dissimilarity
	
	public Voisin(int x,int y,float d)
	{
		this.x=x;
		this.y=y;
		this.d=d;
	}
	
	public Voisin(Voisin v)
	{
		x=v.x;
		y=v.y;
		d=v.d;
	}
	
	public String toString()
	{
		return "("+x+","+y+") and dissimilarity = "+d;
	}
}
