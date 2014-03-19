/**
 * 
 */
package fr.unistra.pelican.util;

/**
 * Simple holder for an ordered pair
 * 
 * @author Benjamin Perret
 *
 */
public class Pair<E,F> {
	
	/**
	 * First element of the ordered pair
	 */
	public E e1;
	
	/**
	 * Second element of the ordered pair
	 */
	public F e2;
	
	
	/**
	 * Default constructor. e1 and e2 are null.
	 */
	public Pair()
	{
		
	}
	
	
	
	/**
	 * Constructor that initializes the pair
	 * @param e1 First element of the ordered pair
	 * @param e2 Second element of the ordered pair
	 */
	public Pair(E e1, F e2) {
		super();
		this.e1 = e1; 
		this.e2 = e2;
	}




	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return "(" +e1+","+e2+")";
	}
	
	
}
