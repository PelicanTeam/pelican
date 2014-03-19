package fr.unistra.pelican.util;

import java.util.LinkedList;

/**
 * This class represents a FIFO (first-in-first-out) queue of objects. 
 * 
 * @author Jonathan Weber
 * @param <T>
 */
public class FIFOQueue<T> extends LinkedList<T>
{
	
	
	/**
	 * Looks at the object at the top of this FIFO queue without removing it from the stack
	 * @return The object at the top of this FIFO queue (the first item of the ArrayList object).
	 */
	public final T peek()
	{
		return this.getFirst();
	}
	
	/**
	 * Removes the object at the top of this FIFO queue and returns that object as the value of this function.
	 * @return The object at the top of this FIFO queue (the first item of the ArrayList object).
	 */
	public final T pop()
	{
		return this.removeFirst();
	}
	
	/**
	 * Pushes an item onto the bottom of this FIFO queue.
	 * @param element
	 */
	public final void push(T element)
	{
		this.addLast(element);
	}
	/**
	 * Returns the 1-based position where an object is on this FIFO queue
	 * @param element
	 * @return Distance from the top of the FIFO queue
	 */
	public final int search(T element)
	{
		return (this.indexOf(element));
	}
}

