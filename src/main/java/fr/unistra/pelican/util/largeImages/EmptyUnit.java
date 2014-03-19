package fr.unistra.pelican.util.largeImages;

import java.io.Serializable;

import fr.unistra.pelican.PelicanException;

/**
 * Empty Unit used to fill the file when a new Large Image is created
 */
public class EmptyUnit extends Unit implements Serializable {

	/**
	 * Serial
	 */
	private static final long serialVersionUID = 4938430061412068493L;

	/**
	 * Constructor
	 */
	public EmptyUnit() {
		super();
	}

	@Override
	public Unit clone() {
		//LargeImageMemoryManager.getInstance().checkMemory();
		return new EmptyUnit();
	}
	
	@Override
	public int defaultSize(){
		throw new PelicanException("Someone tried to call size() on an empty Unit");
	}
	
	@Override
	public boolean equals(Unit u){
		if (u==null||!(u instanceof EmptyUnit)){
			return false;
		}
		return true;
	}

}
