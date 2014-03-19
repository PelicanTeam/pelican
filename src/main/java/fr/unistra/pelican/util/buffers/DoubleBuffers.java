package fr.unistra.pelican.util.buffers;

/**	
 *	Implements left and right (resp. up and down) buffers for horizontal (resp.vertical) line se.
 *	@see fr.unistra.pelican.algorithms.morphology.gray.GrayDilation 
 *	@see fr.unistra.pelican.algorithms.morphology.gray.GrayErosion 
 *	@author Régis Witz
 */
public class DoubleBuffers extends Buffers { 

	/**	Right buffer. */
	public double[] g;
	/**	Left buffers. */
	public double[] h;
	public DoubleBuffers( int size ) { 

		this.g = new double[size];
		this.h = new double[size];
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
