package fr.unistra.pelican.util.data.distances;

/**
 * Abstract class representing the weighted euclidean distance :
 * $d_w(i,j)=\sqrt{ \sum_{k=1}^m w_k (i_k-j_k)^2 }$
 * 
 * The exact implementation has to be specified in the subclasses depending on
 * the data type.
 * 
 * @author lefevre
 * 
 */
public abstract class WeightedEuclideanDistance extends Distance {

	/**
	 * The weights to be used in the weighthed euclidean distance
	 */
	Double[] weights;
	
	/**
	 * Default constructor
	 * @param weights weights weights to be used in the distance measure
	 */
	public WeightedEuclideanDistance(Double[] weights) {
		this.weights=weights;
	}
	
}
