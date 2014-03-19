/**
 * 
 */
package fr.unistra.pelican.util.connectivityTrees;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import fr.unistra.pelican.util.ArrayToolbox;
import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.connectivityTrees.attributes.ComponentAttribute;

/**
 * This class represents a connected component of an image.
 * It represents root, nodes and leaves of the tree.
 *
 * It is furnished with default attributes : level, area, highest level.
 * You can add more attributes using the addAttribute method of class ComponentTree
 *
 * @author Benjamin Perret
 *
 */
public class ComponentNode <T>{

	/**
	 * A field free to use, useful to mark nodes before deletion for example.
	 */
	public int flag;
	public int flag2;
	public int flag3;
	
	/**
	 * 3 fields free to use...
	 */
	public T helper1;
	public T helper2;
	public T helper3;

	public Object helper4;
	
	/**
	 * THIS FIELD IS USED BY ITERATOR PROVIDED BY CLASS COMPONENT TREE DO NOT USE IT WHILE USING SUCH ITERATORS !!!
	 */
	public int iteratorFlag;
	
	/**
	 * Location of canonical element
	 */
	public Point3D location;
	 
	public UnionFindParametre locator;
	
	/**
	 * Level of given node
	 */
	private T level;
	
	/**
	 * Area of node (number of pixels in it and in its children)
	 */
	private int area;
	
	public int perimeter;
	
	/**
	 * highest level of pixels in it and in its children
	 */
	private T highest;
	
	/**
	 * Parent of component node (null if root)
	 */
	public ComponentNode<T> parent;
	
	/**
	 * Map of all attributes added to this node
	 */
	private Map<Class<? extends ComponentAttribute>,ComponentAttribute> attributes = new HashMap<Class<? extends ComponentAttribute>,ComponentAttribute>();
	
	/**
	 * List of children nodes
	 */
	private ArrayList<ComponentNode<T>> children=new ArrayList<ComponentNode<T>>();
	
	/**
	 * Build node with canonical element at given location at given level
	 * @param location
	 * @param level
	 */
	public ComponentNode(Point3D location,T level)
	{
		this.level=level;
		this.highest=level;

		this.area=1;
		this.location=location;
	}

	/**
	 * Add a child node
	 * @param e
	 * @return
	 */
	public boolean addChild(ComponentNode<T> e) {
		e.setParent(this);
		return children.add(e);
	}

	/**
	 * Add a collection of children to node
	 * @param c
	 * @return
	 */
	public boolean addAllChildren(Collection<? extends ComponentNode<T>> c) {
		for(ComponentNode<T> child:c)
		{
			child.setParent(this);
		}
		return children.addAll(c);
	}

	/**
	 * delete all children
	 */
	public void clearChildren() {
		children.clear();
	}

	/**
	 * get child number index
	 * @param index
	 * @return
	 */
	public ComponentNode<T> getChild(int index) {
		return children.get(index);
	}
	
	/**
	 * get collection of all children (warning unsafe, collection is a reference to the one managed by the node)
	 * @return
	 */
	public Collection<? extends ComponentNode<T>> getChildren() {
		return children;
	}

	/**
	 * get collection of all children (safe you get a copy)
	 * @return
	 */
	public Collection<? extends ComponentNode<T>> getChildrenSafe() {
		return (Collection<? extends ComponentNode<T>>)children.clone();
	}
	
	/**
	 * Test if node is a leaf (numberOfChildren()==0)
	 * @return
	 */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/**
	 * Delete given child
	 * @param e
	 * @return
	 */
	public boolean removeChild(ComponentNode<T> e) {
		return children.remove(e);
	}

	/**
	 * get the number of children of the node
	 * @return
	 */
	public int numberOfChildren() {
		return children.size();
	}

	/**
	 * get level of the node
	 * @return
	 */
	public T getLevel() {
		return level;
	}

	
	public void setLevel(T level) {
		this.level = level;
	}

	public int getArea() {
		return area;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public T getHighest() {
		return highest;
	}

	public void setHighest(T highest) {
		this.highest = highest;
	}

	
	

	
	
	public String toString()
	{
		
		return toString(0);
	}
	
	
	private String toString(int depth)
	{
		String op="";
		for(int i=0;i<depth;i++)
			op+="|";
		
		op+="-> " + location +" Level: " + ArrayToolbox.printString(level) + " area " + area + "\n"; 
		
		for(ComponentNode<T> c: children)
			op+=c.toString(depth+1);
		return op;
	}

	public ComponentNode<T> getParent() {
		return parent;
	}

	public void setParent(ComponentNode<T> parent) {
		this.parent = parent;
	}
	/**
	 * NOT IMPLEMENTED merge attributes of two nodes
	 * @TODO do it
	 * @param c
	 * @param comp
	 */
	public void mergeAttributsWith(ComponentNode<T> c, Comparator<ComponentNode<T>> comp)
	{
		this.level = (comp.compare(this, c)<=0)?this.getLevel():c.getLevel();
	}

	/**
	 * Add an attributes to this node
	 * @param e
	 */
	public <E> void add(ComponentAttribute<E> e) {
		attributes.put(e.getClass(),e);
	}
	
	/**
	 * Add an attributes to this node
	 * @param e
	 */
	public <E> void remove(Class<? extends ComponentAttribute<E>> clazz) {
		attributes.remove(clazz);
	}

	/**
	 * Get attribute of this node
	 * 
	 * Requesting an attribute of an unknown class of attributes for this node will produce a class cast exception
	 * 
	 * @param <T>
	 * @param clazz class of the attribute to return
	 * @return
	 */
	public <T> T get(Class<T> clazz) {
		return (T)attributes.get(clazz);
	}

	/**
	 * Get attribute of given class
	 * @param <Q>
	 * @param clazz
	 * @return
	 */
	public <Q extends ComponentAttribute<?>> Q getAttribute(Class<Q> clazz)
	{
		return (Q)attributes.get(clazz);
	}
	
	/**
	 * Get value of attribute designed by given class
	 * @param <Q>
	 * @param clazz
	 * @return
	 */
	public <Q> Q getAttributeValue(Class<? extends ComponentAttribute<Q>> clazz)
	{
		Q res=null;
		ComponentAttribute<Q> o=attributes.get(clazz);
		if(o!=null) {
			res=o.getValue();
		}
		return res;
	}
	
}
