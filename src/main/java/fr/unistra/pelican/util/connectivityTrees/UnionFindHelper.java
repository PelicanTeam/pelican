/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees;

import java.util.HashMap;
import java.util.Map;

import fr.unistra.pelican.util.Point3D;

/**
 * This class helps to maintain a collection of disjoint sets for union find algorithm
 * 
 * @author Benjamin Perret
 *
 */
public class  UnionFindHelper {


	/**
	 * Dimension of space
	 */
	private int xdim, ydim,zdim,xydim;
	
	/**
	 * List of all points
	 */
	private UnionFindParametre [] map;
	
	/**
	 * Create a union find helper for a space of given dimensions
	 * @param xdim
	 * @param ydim
	 * @param zdim
	 */
	public UnionFindHelper(int xdim, int ydim, int zdim)
	{
		this.xdim=xdim;
		this.ydim=ydim;
		this.zdim=zdim;
		xydim=xdim*ydim;
		map = new UnionFindParametre[zdim*xdim*ydim];
	}
	
	/**
	 * Compress path from all points to their canonical elements
	 */
	public void compressPathFinding(){
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
					find(x, y, z);
	}
	
	/**
	 * Build a new set containing only point p
	 * @param p
	 */
	public UnionFindParametre MakeSet(Point3D p)
	{
		return map[p.z*xydim+p.y*xdim+p.x] =  new UnionFindParametre(p);
	}
	
	/**
	 * Find canonical element representing given pixel (x,y,z)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Point3D find(int x, int y , int z)
	{
		UnionFindParametre params= find(map[z*xydim+y*xdim+x]);
		
		return params.attachedPoint;
	}
	
	/**
	 * Find canonical element representing given pixel (x,y)
	 * @param x
	 * @param y
	 * @return
	 */
	public Point3D find(int x, int y )
	{
		return find(x,y,0);
	}
	
	/**
	 * Find canonical element representing given point
	 * @param p
	 * @return
	 */
	public Point3D find(Point3D p)
	{
		return find(p.x,p.y,p.z);
	}
	
	
	/**
	 * Find canonical element representing given pixel (x,y,z)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public UnionFindParametre find(UnionFindParametre params)
	{

		if(params.parent!=params)
		{
			params.parent=find(params.parent);
		}
		return params.parent;
	}
	
	/**
	 * Link sets associated to different points
	 * Linkage is done to minimize path length to canonical element (canonical element may change!)
	 * 
	 * DO NOT USE THIS FUNCTION ONCE THE COMPONENT TREE IS BUILD AS THE CANONICAL ELEMENT OF A SET MUST BE CONSTANT AT THIS TIME
	 * 
	 * @param p1
	 * @param p2
	 * @return point chosen to be the canonical element of the other
	 */
	public Point3D link(Point3D p1, Point3D p2)
	{
		UnionFindParametre px= map[p1.z*xydim+p1.y*xdim+p1.x];
		UnionFindParametre py= map[p2.z*xydim+p2.y*xdim+p2.x];
		
		if(px.rank>py.rank)
		{
			Point3D tmp=p1;
			p1=p2;
			p2=tmp;
			UnionFindParametre tmp2=px;
			px=py;
			py=tmp2;
			
		} else if(px.rank==py.rank)
			py.rank++;
		px.parent=py;
		return p2;
		
		
	}
	
	/**
	 * Link sets associated to different points
	 * This function does not try to minimize path length to canonical element so it ensure that first point will be the canonical element of the second one
	 * @param root
	 * @param child
	 * @return
	 */
	public UnionFindParametre linkNoRankCheck(UnionFindParametre root, UnionFindParametre child)
	{
		//UnionFindParametre px= map[root.z*xydim+root.y*xdim+root.x];
		//UnionFindParametre py= map[child.z*xydim+child.y*xdim+child.x];
		/*if(px.rank<py.rank)
			px.rank=py.rank;
		else if(px.rank==py.rank)
			px.rank++;*/
		child.parent=root.parent;//root;
		//find(child);
		return root;
		
	}
	
	
	
	/**
	 * Print string representation of the set on the default output
	 */
	public void drop()
	{
		for(int y=0;y<ydim;y++)
		{System.out.print("|");
			for(int x=0;x<xdim;x++)
			{
				Point3D p =find(x,y);
				if(p.x == x && p.y==y)
					System.out.print("*(" +p.x + "," +p.y + ")r=" +  + map[p.z*xydim+p.y*xdim+p.x].rank + " |");
				else System.out.print(" (" +p.x + "," +p.y + ")r=" +  + map[p.z*xydim+p.y*xdim+p.x].rank + " |");
			}
			System.out.println();
		}
				
	}
	
	public UnionFindParametre changePointLink(Point3D p,UnionFindParametre parent)
	{
		// create a new unionfindparamtre, leave the old one as phantom to preserve links.
		//UnionFindParametre parent= map[newParent.z*xydim+newParent.y*xdim+newParent.x];
		//System.out.println(p + " " + parent);
		UnionFindParametre pr=new UnionFindParametre(p);
		pr.parent=parent;
		map[p.z*xydim+p.y*xdim+p.x]=pr;
		/*UnionFindParametre px= map[p.z*xydim+p.y*xdim+p.x];
		if(px.parent==px)
		{
			System.out.println("grrrrr this is master node!");
		}
		px.parent=newParent;
		px.rank=1;*/
		return pr;
	}
	
}


