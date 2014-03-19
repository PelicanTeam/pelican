package fr.unistra.pelican.util.data.distances;


/**
 * Abstract class representing the euclidean distance :
 * $d_w(i,j)=\sqrt{ \sum_{k=1}^m (i_k-j_k)^2 }$
 * 
 * The exact implementation has to be specified in the subclasses depending on
 * the data type.
 * 
 * @author lefevre
 * 
 */
public abstract class EuclideanDistance extends Distance {

}
