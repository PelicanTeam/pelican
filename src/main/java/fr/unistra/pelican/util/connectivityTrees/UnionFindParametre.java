package fr.unistra.pelican.util.connectivityTrees;

import fr.unistra.pelican.util.Point3D;

/**
 * An element in the unionfind set composed of a representative element and a rank (length of the longest path to the canonical element) 
 * @author Benjamin Perret
 *
 */
public class UnionFindParametre{
	public UnionFindParametre parent;
	int rank=0;
	Point3D attachedPoint;
	
	UnionFindParametre(Point3D aPoint3D) {
		super();
		this.parent = this;
		this.attachedPoint=aPoint3D;
	}
	
	
	

}