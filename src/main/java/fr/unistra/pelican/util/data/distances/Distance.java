package fr.unistra.pelican.util.data.distances;

import fr.unistra.pelican.util.data.Data;

/**
 * Abstract class representing a distance to be computed between two given data
 * in PELICAN. Possible use is to consider data as feature and to compute
 * distance between features.
 * 
 * The exact distance measure has to be specified in the subclasses.
 * 
 * @author lefevre
 * 
 */
public abstract class Distance {

	/**
	 * Abstract method representing the distance measure to be overriden by the
	 * subclass
	 * 
	 * @param d1 First data
	 * @param d2 Second data
	 * @return Distance between <tt>d1</tt> and <tt>d2</tt>.
	 */
	public abstract double distance(Data d1, Data d2);

	/**
	 * Static method able to return the required distance measure adapted to the data type
	 * @param dist Class representing the distance measure 
	 * @param data Data on which will be computed the distance measure
	 * @param params Optional parameters to the constructor of the distance measure : take care of (Object) vs (Object[])
	 * @return the instance of the distance measure adapted to the data type
	 */
	public static Distance forName( Class<?> dist, Data data, Object... params) {
		Distance d = null;
		String sData = data.getClass().getSimpleName();
		String sDist = dist.getSimpleName();
		String sPack = dist.getPackage().getName();
		String name = sPack + "." + sData.substring(0, sData.length() - 4) + sDist;
		try {
			// Constructeur sans paramÃ¨tre
			if (params==null)
			d = (Distance) Class.forName(name).newInstance();
			else {
				// Constructeur avec paramÃ¨tres, on rÃ©cupÃ¨re les types des paramÃ¨tres
				Class<?>[] cParams=new Class[params.length];
				for (int i=0;i<params.length;i++)
					cParams[i]=params[i].getClass();
				d=(Distance)Class.forName(name).getConstructor(cParams).newInstance(params);
			}
		} catch (Exception e) {
			if (e instanceof ClassNotFoundException)
				System.err.println("Distance " + sDist + " is not defined for " + sData
					+ " in package " + sPack);
			if (e instanceof NoSuchMethodException)
				System.err.println("Bad call to the constructor of class " + name + " (check the parameters)");
			else
				e.printStackTrace();
		}
		return d;
	}

}
