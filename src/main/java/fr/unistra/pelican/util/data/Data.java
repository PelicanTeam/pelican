package fr.unistra.pelican.util.data;

import java.io.Serializable;
import java.lang.reflect.Method;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.util.Offset;
import fr.unistra.pelican.util.data.distances.Distance;

/**
 * Abstract class representing data to be used in PELICAN. Possible use is to
 * consider data as feature and to compute distance between features.
 * 
 * The exact data structure has to be specified in the subclasses.
 * 
 *	@author lefevre, weber
 *	@author Régis Witz (clone,toString,getParsedInstance)
 * 
 */
public abstract class Data implements Cloneable, Serializable {


	private static final long serialVersionUID = -1244611776868304298L;
	/**
	 * The descriptor used to produce the data (filled automatically when
	 * computing the descriptor). Stores the class rather than the object seems is
	 * more efficient.
	 */
	Class<Descriptor> descriptor;

	/**
	 * Returns the descriptor used to produce the data
	 * 
	 * @return the descriptor used to produce the data
	 */
	public Class<Descriptor> getDescriptor() {
		return descriptor;
	}

	/**
	 * Set the descriptor used to produce the data
	 * 
	 * @param d
	 *          the descriptor used to produce the data
	 */
	public void setDescriptor(Class<Descriptor> d) {
		descriptor = d;
	}

	/**
	 * Abstract getter to be overriden by subclasses
	 * 
	 * @return the values contained in the data
	 */
	public abstract Object getValues();

	/**
	 *	Abstract setter to be overriden by subclasses
	 *	@param values new values to be set in the object
	 */
	public abstract void setValues(Object values);

	/**
	 * Abstract method defining the default distance measure to be overriden by
	 * subclasses
	 * 
	 * @param data
	 *          some data to be compared to
	 * @return the distance between the object and data
	 */
	public abstract double distance(Data data);

	/**
	 * Computes the similarity measure between the object and the given data
	 * 
	 * @param data
	 *          some data to be compared to
	 * @return the similarity measure between the object and data
	 */
	public double similarityTo(Data data)
	{
		return 1-distanceTo(data);
	}
	
	/**
	 * Computes the distance between the object and the given data
	 * 
	 * @param data
	 *          some data to be compared to
	 * @return the distance between the object and data
	 */
	public double distanceTo(Data data) {
		Class<Descriptor> descriptor = getDescriptor();
		Method method = null;
		double result = -1;
		// Première recherche : dans descriptor
		if (descriptor != null)
			try {
				method = descriptor.getMethod("distance", Data.class, Data.class);
				result = (Double) method.invoke(null, this, data);
			} catch (Exception e) {
				// Si une erreur lors de l'appel qui n'est pas un NoSuchMethodException
				if (!(e instanceof NoSuchMethodException))
					e.printStackTrace();
			}
		if (result == -1)
			// Seconde recherche : dans data
			try {
				method = getClass().getMethod("distance", Data.class);
				result = (Double) method.invoke(this, data);
			} catch (Exception e) {
				// Si une erreur lors de l'appel qui n'est pas un NoSuchMethodException
				if (!(e instanceof NoSuchMethodException))
					e.printStackTrace();
				else
					System.err.println("MÃ©thode distance absente de " + getDescriptor()
						+ " et de " + getClass());
			}
		return result;
	}

	/**
	 * Computes the distance between the two data
	 * 
	 * @param data1
	 *          First data
	 * @param data2
	 *          Second data
	 * @return the distance between the two data
	 */
	public static double distanceBetween(Data data1, Data data2) {
		return data1.distanceTo(data2);
	}

	/**
	 * Computes the distance between the object and the data using a given
	 * distance measure
	 * 
	 * @param data
	 *          some data to be compared to
	 * @param dist
	 *          A distance measure to be used
	 * @return the distance between the two data
	 */
	public double distanceTo(Data data, Distance dist) {
		return dist.distance(this, data);
	}

	/**
	 * Computes the distance between the two data using a given distance measure
	 * 
	 * @param data1
	 *          First data
	 * @param data2
	 *          Second data
	 * @param dist
	 *          A distance measure to be used
	 * @return the distance between the two data
	 */
	public static double distanceBetween(Data data1, Data data2, Distance dist) {
		return dist.distance(data1, data2);
	}

	/**
	 * Computes the distance between the two data using a given distance measure
	 * 
	 * @param data1
	 *          First data
	 * @param data2
	 *          Second data
	 * @param distanceName
	 *          The class representing the distance measure to be used
	 * @param params
	 *          The required parameters to create the distance measure object
	 * @return the distance between the two data
	 */
	@SuppressWarnings("unchecked")
	public static double distanceBetween(Data data1, Data data2, Class distanceName,
		Object... params) {
		return Distance.forName(distanceName, data1, params).distance(data1, data2);
	}

	/**
	 * Abstract method returning a String representation of the data to be
	 * overriden by subclasses
	 * @return See {@link String#toString}.
	 */
	public abstract String toString();

	/**
	 * Abstract method used to check if two data are equals
	 * 
	 * @param data
	 *          to be compared to
	 * @return true if both data are equals, false otherwise
	 */
	public abstract boolean equals(Data data);

	/**	Why shouldn't a Data be cloneable ?
	 *	@return Cloned data. 
	 *	@author Régis Witz
	 */
	public abstract Data clone();

	/**	Get a data instance described in <tt>input</tt>.
	 *	This method is not abstract with respect to lazy coders.
	 *	@param words words to parse
	 *	@param offset offset in <tt>words</tt> with wich parsing is begun
	 *	@return a new instance of Data
	 *	@author Régis Witz
	 */
	public static Data getParsedInstance( String[] words, Offset offset ) { return null; };

}
