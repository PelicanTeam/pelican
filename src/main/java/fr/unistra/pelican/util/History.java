package fr.unistra.pelican.util;
import java.util.ArrayList;



/**	This class manages an history of objects.
 *	Its functions are inspired by a magneto-recorder. You can navigate through time, "rewinding" 
 *	and "forwarding", each time getting the "past" of "future" element wich was added at this date, 
 *	and each time this element becomes the "present" of this history.
 *	It's a kind of array wich you can navigate through by steps of 1, if you want ...
 *	<p>
 *	I didn't override one of the Lists class, 'coz of lack of time. As we say, "I'll do it later"
 *	... so do it if you wanna.
 *
 *	TODO : Currently this class potentially provoke java heap size error. Somebody should verify 
 *	if it is the case, and fix it (probably by imposing a maximum size to the History).
 *
 *	@author witz
 *	@param <E> Class of the elements you want to put in this.
 */
public class History<E> { 



	  //////////////
	 // CONSTANT //
	//////////////

	public static final long serialVersionUID = 1L;





	  ////////////
	 // FIELDS //
	////////////

	/**	Current element's index. */
	private int index;
	/**	The very "history" of this class. */
	private ArrayList<E> history;





	  /////////////////
	 // CONSTRUCTOR //
	/////////////////

	public History() { 

		super();
		this.index = -1;
		this.history = new ArrayList<E>();
	}





	  /////////////
	 // METHODS //
	/////////////

	/**	Add an element to the history.
	 *	If you already rewinded in the history, "future" elements will be forgotten.
	 *	@param element The element that'll figure the "present". 
	 */
	public void add( E element ) { 

		for ( int i = this.history.size()-1 ; i > this.index ; i-- ) this.history.remove( i );
		this.index++;
		this.history.add( this.index, element );
	}



	/**	Get the next element in the "Past", without changing the whole history. This element 
	 *	becomes "Present time".
	 *	@return The next element in the "Past". <tt>null</tt> if you want to know what's before 
	 *	the Big Bang.
	 */
	public E rewind() { 

		if ( !this.canRewind() ) return null;
		this.index--;
		return this.history.get( this.index );
	}



	/**	Get the next element in the "Future", without changing the whole history. This element 
	 *	becomes "Present time".
	 *	@return The next element in the "Future". <tt>null</tt> if you want to know what's after 
	 *	the Judgment Day.
	 */
	public E forward() { 

		if ( !this.canForward() ) return null;
		this.index++;
		return this.history.get( this.index );
	}



	/**	Get the element at "Present time", without changing the whole history. 
	 *	No changes on anyone of this instance's fields.
	 *	@return The "present" element.
	 */
	public E current() { 

		if ( this.isEmpty() ) return null;
		return this.history.get( this.index );
	}

	public E genesis() { 

		if ( this.isEmpty() ) return null;
		this.index = 0;
		return this.history.get( this.index );
	}



	public boolean canRewind()  { return this.index > 0; }
	public boolean canForward() { return this.index < this.history.size()-1; }
	public boolean isEmpty()	{ return this.index < 0; }



}
