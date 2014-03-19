package fr.unistra.pelican.util;

import java.util.ArrayList;

/**
 * The Stack class represents a last-in-first-out (LIFO) stack of objects. 
 * It is similar to the Stack class provided by Java but based on ArrayList
 * instead of naughty Vector.
 * 
 * @author Jonathan Weber
 * @param <T>
 */
public class Stack<T> extends ArrayList<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 80414061658871163L;
	/**
	 * Looks at the object at the top of this stack without removing it from the stack
	 * @return The object at the top of this stack (the last item of the ArrayList object).
	 */
	public final T peek()
	{
		return this.get(this.size()-1);
	}
	
	/**
	 * Removes the object at the top of this stack and returns that object as the value of this function.
	 * @return The object at the top of this stack (the last item of the ArrayList object).
	 */
	public final T pop()
	{
		return this.remove(this.size()-1);
	}
	
	/**
	 * Pushes an item onto the top of this stack.
	 * @param element
	 */
	public final void push(T element)
	{
		this.add(element);
	}
	/**
	 * Returns the 1-based position where an object is on this stack
	 * @param element
	 * @return Distance from the top of the stack
	 */
	public final int search(T element)
	{
		return (this.size()-this.indexOf(element));
	}
}
