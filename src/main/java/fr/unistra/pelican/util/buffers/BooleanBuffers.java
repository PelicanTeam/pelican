package fr.unistra.pelican.util.buffers;



/**	
 *	Implements left and right (resp. up and down) buffers for horizontal (resp.vertical) line se.
 *	@see fr.unistra.pelican.algorithms.morphology.binary.BinaryDilation 
 *	@see fr.unistra.pelican.algorithms.morphology.binary.BinaryErosion 
 *	@author Régis Witz
 */
public class BooleanBuffers extends Buffers { 

	/**	Right buffer. */
	public boolean[] g;
	/**	Left buffers. */
	public boolean[] h;

	public BooleanBuffers( int size ) { 

		this.g = new boolean[size];
		this.h = new boolean[size];
		this.size = size;
	}

	public String toString() {

		String s = "\ng: ";
		for ( int c = 0 ; c < this.size ; c++ ) s += this.g[c] + ",";
		s += "\nh: ";
		for ( int c = 0 ; c < this.size ; c++ ) s += this.h[c] + ",";
		return s;
	}
} 
