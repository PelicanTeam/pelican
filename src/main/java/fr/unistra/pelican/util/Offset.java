package fr.unistra.pelican.util;



/**
 *	Aisance de Java, TU PARLES !
 *
 *	@author Régis Witz
 */
public class Offset {

	public int offset;
	public Offset() { this.offset = 0; }
	public Offset( int offset ) { this.offset = offset; }
	public String toString() { return new String( ""+this.offset ); }
}
