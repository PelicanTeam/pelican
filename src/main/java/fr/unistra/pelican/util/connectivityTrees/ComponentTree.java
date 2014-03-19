/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributePointList;
import fr.unistra.pelican.util.connectivityTrees.attributes.ComponentAttribute;
import fr.unistra.pelican.util.connectivityTrees.attributes.UnsupportedDataTypeException;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;

/**
 * A connected component tree. A tree is composed as a set of component nodes. 
 * A component node is accessed through its canonical element, i.e. a special point associated to it.
 * The union find technique is used to link each point to the canonical element representing the connected node it belongs to.
 * 
 * The class provides several utilities to manipulate the tree (access to nodes, iterate over nodes, delete nodes, add attributes)
 * 
 * Designed for 3D (xyz) images
 * 
 * @author Benjamin Perret
 *
 */
public class ComponentTree <T> {

	/**
	 * Dimensions of underlying image
	 */
	private int xdim,ydim,zdim;
	
	/**
	 * root node
	 */
	public ComponentNode<T> root;
	
	
	private Comparator<ComponentNode<T>> comparator;
	
	/**
	 * Base connectivity
	 */
	private Connectivity3D connectivity;
	
	/**
	 * Set of all node managed using union find helper class
	 */
	public UnionFindHelper nodeSet;

	/**
	 * Set of all node accessed through  xyz coordinate
	 */
	public ComponentNode<T> [][][] nodes;
	
	/**
	 * Underlying image
	 */
	public Image image;
	
	/**
	 * @param root
	 * @param nodeSet
	 */
	public ComponentTree(ComponentNode<T> root, UnionFindHelper nodeSet, ComponentNode<T> [][][] nodes) {
		super();
		this.root = root;
		this.nodeSet = nodeSet;
		this.nodes=nodes;
	}

	/**
	 * Find canonical node associated to pixel (x,y)
	 * @param x
	 * @param y
	 * @return
	 */
	public ComponentNode<T> findNodeAt(int x,int y)
	{
		Point3D p = find(x,y);
		return nodes[p.z][p.y][p.x];
	}
	
	/**
	 * Find canonical node associated to pixel (x,y,z)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public ComponentNode<T> findNodeAt(int x,int y,int z)
	{
		Point3D p = find(x,y,z);
		return nodes[p.z][p.y][p.x];
	}
	
	/**
	 * Find canonical node associated to pixel (x,y)given point
	 * @param p
	 * @return
	 */
	public ComponentNode<T> findNodeAt(Point3D p)
	{
		p=find(p);
		return nodes[p.z][p.y][p.x];
	}
	
	/**
	 * get root node
	 * @return
	 */
	public ComponentNode<T> getRoot() {
		return root;
	}

	/**
	 * Find location of canonical node associated to pixel (x,y)
	 * @param x
	 * @param y
	 * @return
	 */
	public Point3D find(int x, int y) {
		return nodeSet.find(x,y);
	}
	
	/**
	 * Find location of canonical node associated to pixel (x,y,z)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Point3D find(int x, int y, int z) {
		return nodeSet.find(x,y,z);
	}
	
	/**
	 * Find location of canonical node associated to point p
	 * @param p
	 * @return
	 */
	public Point3D find(Point3D p) {
		return nodeSet.find(p);
	}

	
	/**
	 * Add an attribute to all nodes of the tree
	 * @param a
	 * @throws UnsupportedDataTypeException
	 */
	public void addAttribute(ComponentAttribute a) throws UnsupportedDataTypeException
	{	
		//System.out.println("adding attribute " +a );
		a.computeAttribute(this);
	}
	
	/**
	 * Removean attribute to all nodes of the tree
	 * @param a
	 * @throws UnsupportedDataTypeException
	 */
	public <E> void removeAttribute(Class<? extends ComponentAttribute<E>> clazz) 
	{	
		for (ComponentNode<T> c : iterateFromRootToLeaf())
		{
			c.remove(clazz);
			
		}
	}
	
	/**
	 * Compute number of nodes in the tree (nodes + leaves)
	 * @return
	 */
	public int countNodes()
	{
		int nb=0;
		Stack<ComponentNode<T>> s = new Stack<ComponentNode<T>>();
		s.push(root);
		
		while(!s.isEmpty())
		{
			ComponentNode<T> c = s.pop();
			nb++;
			for(ComponentNode<T> child:c.getChildren())
			{
				s.push(child);
			}	
		}
		return nb;
	}
	
	/**
	 * Count  number of leaves
	 * @return
	 */
	public int countLeaf()
	{
		int nb=0;
		Stack<ComponentNode<T>> s = new Stack<ComponentNode<T>>();
		s.push(root);
		
		while(!s.isEmpty())
		{
			ComponentNode<T> c = s.pop();
			if(c.numberOfChildren()==0)
				nb++;
			else {
				for (ComponentNode<T> child : c.getChildren()) {
					s.push(child);
				}
			}
		}
		return nb;
	}
	
	/**
	 * Delete all nodes having flag value equal to given value
	 * @param value
	 */
	public void deleteOldNodeWithFlag(int value)
	{
		Stack<ComponentNode<T>> s = new Stack<ComponentNode<T>>();
		s.push(root);
		int i=0;
		while(!s.isEmpty())
		{
			ComponentNode<T> c = s.pop();
			boolean flag = true;
			while (flag) { // world is collapsing now! Sure it can be more efficient but it seems to work !
				flag = false;
				int size=c.numberOfChildren();
				for(int j=0;j<size;j++)
				{
					ComponentNode<T> child = c.getChild(j);
					if (child.flag == value) {
						deleteNode(child);
						size=c.numberOfChildren();
						j--;
						if((i++)%100==0)
							compressPathFinding();
					}
				}
				/*for (ComponentNode<T> child : c.getChildrenSafe()) {
					if (child.flag == value) {
						deleteNode(child);
						flag = true;
						i++;
						
						// System.out.println("father " + c.location + " delete
						// " + child.location);
					}
				}*/
			}
			for (ComponentNode<T> child : c.getChildren()) {
				s.push(child);
			}		
		//	System.out.print(root);
		}
	}
	
	/**
	 * Reset flag value of all nodes to given value
	 * @param value
	 */
	public void resetFlag(int value)
	{
		Stack<ComponentNode<T>> s = new Stack<ComponentNode<T>>();
		s.push(root);
		
		while(!s.isEmpty())
		{
			ComponentNode<T> c = s.pop();
			c.flag=value;
			for(ComponentNode<T> child:c.getChildren())
			{
				s.push(child);
			}		
		}
	}
	public void resetFlag2(int value)
	{
		Stack<ComponentNode<T>> s = new Stack<ComponentNode<T>>();
		s.push(root);
		
		while(!s.isEmpty())
		{
			ComponentNode<T> c = s.pop();
			c.flag2=value;
			for(ComponentNode<T> child:c.getChildren())
			{
				s.push(child);
			}		
		}
	}
	public void resetFlag3(int value)
	{
		Stack<ComponentNode<T>> s = new Stack<ComponentNode<T>>();
		s.push(root);
		
		while(!s.isEmpty())
		{
			ComponentNode<T> c = s.pop();
			c.flag3=value;
			for(ComponentNode<T> child:c.getChildren())
			{
				s.push(child);
			}		
		}
	}
	
	/**
	 * link to node together (make them connected) once the tree is already build
	 * @param root
	 * @param child
	 */
	private void lateLinkage(ComponentNode<T> root,ComponentNode<T> child)
	{
		nodeSet.linkNoRankCheck(root.locator, child.locator);
		/*if(!p.equals(root.location)) // to improve path length root and child  have been swapped, we must impact this change on the tree structure.
		{
			ComponentNode<T> tmp = nodes[root.location.z][root.location.y][root.location.x];
			
			nodes[root.location.z][root.location.y][root.location.x]=nodes[child.location.z][child.location.y][child.location.x];
			nodes[child.location.z][child.location.y][child.location.x]=tmp; 
		}*/
		
	}
	
	/**
	 * A big hack to assign arbitrary a point to given node! Use it with carefully!
	 * @param node
	 * @param p
	 */
	public void givePointToNode(ComponentNode<T> node, Point3D p)
	{
		//ComponentNode<T> nt=findNodeAt(p);
		/*if(nt.location.equals(p))
		{
			System.out.println("changinf node location");
			List<Point3D> pts=nt.getAttributeValue(AttributePointList.class);
			for(int i=pts.size()-1;i>=0;i--)
			{
				Point3D np=pts.get(i);
				if(!np.equals(p))
				{
					System.out.println("new loc: " + np);
					nodeSet.changePointLink(p, np);
					nt.location=np;
					break;
				}
			}
		}*/
		nodeSet.changePointLink(p, node.locator);
		//if(findNodeAt(p)!=node)
		//	System.out.println("grrr " );
	}
	
	/**
	 * Delete given node and all its children
	 * @param n
	 */
	public void deleteNodeAndChildren(ComponentNode<T> n){
		if(n!=root)
		{
			ComponentNode<T> parent=n.getParent();
			if(parent == null)
			{
				throw new RuntimeException("node at " + n.location  + " has no parent but is not root! tree structure is corrupted!");
			}
			parent.removeChild(n);
			lateLinkage(parent, n);
			//nodeSet.linkNoRankCheck( parent.location,n.location);
			Stack<ComponentNode<T>> s = new Stack<ComponentNode<T>>();
			s.push(n);
			
			while(!s.isEmpty())
			{
				ComponentNode<T> c = s.pop();
				for(ComponentNode<T> child:c.getChildren())
				{
					lateLinkage(parent, child);
					//nodeSet.linkNoRankCheck(parent.location, child.location);
					//parent.location=nodeSet.link(parent.location, n.location);
					child.setParent(null);
					s.push(child);
				}
				c.clearChildren();
				
			}
		}
	}
	
	/**
	 * Call this method if you have stack overflow troubles. 
	 * The union find method may lead to long path between a location and the canonical associated to it.
	 */
	public void compressPathFinding(){
		nodeSet.compressPathFinding();
	}
	
	/**
	 * Test if given point is owned by given connected component
	 * @param p
	 * @param c
	 * @return
	 */
	public boolean isMember(Point3D p, ComponentNode<T> c)
	{
		ComponentNode<T> mycc=findNodeAt(p);
		boolean res=(mycc==c);
	
		while(mycc!=null && !res)
		{
			mycc=mycc.parent;
			res=(mycc==c);
		}
		return res;
	}
	
	/**
	 * Delete given node, its children are added to the parent of given node.
	 * @param n
	 */
	public void deleteNode(ComponentNode<T> n){
		if(n!=root)
		{
			//nodeSet.drop();
			ComponentNode<T> parent=n.getParent();
			if(parent.location == null)
			{
				//throw new RuntimeException("node at " + n.location  + " has no parent but is not root! tree structure is corrupted!");
				return;
			}
			
			
			parent.removeChild(n);
			lateLinkage(parent, n);
			//nodeSet.linkNoRankCheck(parent.location, n.location);
			
			
	
			/*for(ComponentNode<T> child:n.getChildren())
			{
				child.setParent(parent);

			}*/
			parent.addAllChildren(n.getChildren());
			
			n.clearChildren();
			//n.parent=null;
			//System.out.println("after proessing");
			//nodeSet.drop();
			
		}
	}

	/**
	 * get x dimension of underlying image
	 * @return
	 */
	public int getXdim() {
		return xdim;
	}

	public void setXdim(int xdim) {
		this.xdim = xdim;
	}

	public int getYdim() {
		return ydim;
	}

	public void setYdim(int ydim) {
		this.ydim = ydim;
	}

	public int getZdim() {
		return zdim;
	}

	public void setZdim(int zdim) {
		this.zdim = zdim;
	}

	/**
	 * Provides an iterator starting from leaves to given node
	 * @return
	 */
	public  Iterable<ComponentNode<T>> iterateFromLeafToNode(ComponentNode<T> n){
		return new SuffixIterator(n);
	}
	
	/**
	 * Provides an iterator starting from leaves to root
	 * @return
	 */
	public  Iterable<ComponentNode<T>> iterateFromLeafToRoot(){
		return new SuffixIterator(root);
	}
	
	/**
	 * Provides an iterator starting from root to leaves
	 * @return
	 */
	public  Iterable<ComponentNode<T>> iterateFromRootToLeaf(){
		return new RootToLeafIterator();
	}
	
	/**
	 * A suffix iterator over the tree
	 * @author Benjamin Perret
	 *
	 */
	private class SuffixIterator implements Iterator<ComponentNode<T>>, Iterable<ComponentNode<T>>{

		Stack<ComponentNode<T>> s;
		
		public SuffixIterator(ComponentNode<T> ori){
			s = new Stack<ComponentNode<T>>();
			s.push(ori);
			
			while(!s.isEmpty())
			{
				ComponentNode<T> c = s.pop();
				c.iteratorFlag=0;
				for(ComponentNode<T> child:c.getChildren())
				{
					s.push(child);
				}		
			}
			s.push(ori);
			ori.iteratorFlag=0;
		}
		
		@Override
		public boolean hasNext() {
			return !s.isEmpty();
		}

		@Override
		public ComponentNode<T> next() {
			ComponentNode<T> n=s.peek();
			while(n.iteratorFlag!=1)
			{
				n.iteratorFlag=1;
				for(ComponentNode<T> child:n.getChildren())
				{
					s.push(child);
				}
				n=s.peek();
			}
			s.pop();
			return n;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Iterator<ComponentNode<T>> iterator() {
			
			return this;
		}
		
	}
	
	/**
	 * A root to leaves iterator
	 * @author Ben
	 *
	 */
	private class RootToLeafIterator implements Iterator<ComponentNode<T>>, Iterable<ComponentNode<T>>{

		Stack<ComponentNode<T>> s;
		
		public RootToLeafIterator(){
			s = new Stack<ComponentNode<T>>();
			s.push(root);
		}
		
		@Override
		public boolean hasNext() {
			return !s.isEmpty();
		}

		@Override
		public ComponentNode<T> next() {
			ComponentNode<T> n=s.pop();
			
			for(ComponentNode<T> child:n.getChildren())
			{
					s.push(child);
			}	
			return n;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Iterator<ComponentNode<T>> iterator() {
			
			return this;
		}
		
	}
	
	/**
	 * Print tree as a String on System.out
	 */
	public void debugDropNodeMap()
	{
		nodeSet.drop();
	}

	
	/**
	 * @return
	 */
	public Connectivity3D getConnectivity() {
		return connectivity;
	}

	public void setConnectivity(Connectivity3D connectivity) {
		this.connectivity = connectivity;
	}

	/**
	 * @return the comparator
	 */
	public Comparator<ComponentNode<T>> getComparator() {
		return comparator;
	}

	/**
	 * @param comparator the comparator to set
	 */
	public void setComparator(Comparator<ComponentNode<T>> comparator) {
		this.comparator = comparator;
	}
}
