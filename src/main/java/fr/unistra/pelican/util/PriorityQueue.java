/**
 * 
 */
package fr.unistra.pelican.util;

import java.util.Collections;
import java.util.TreeMap;


/**
 * Prioritized queue.
 * 
 * Can handle any type of objects and any type of priority.
 * 
 * Implementation relies on SortedList
 * 
 * @author Benjamin Perret
 *
 * @param <E> element type
 * @param <T> priority type (anything implementing the interface Comparable)
 */
public class  PriorityQueue<E,T extends Comparable<? super T>> extends TreeMap<PriorityQueue.PrioritizedElement<E,T>,PriorityQueue.PrioritizedElement<E,T>> {

	public static enum PriorityPolicy{LowestHasPriority,HighestHasPriority  };
	
	private PriorityPolicy policy=PriorityPolicy.LowestHasPriority;
	
	/**
	 * Default constructor
	 */
	public PriorityQueue() {
		super();
	}
	
	/**
	 * Default constructor
	 */
	public PriorityQueue(PriorityPolicy policy) {
		super((policy.equals(PriorityPolicy.HighestHasPriority)?Collections.reverseOrder():null));
		this.policy=policy;
		
			
	}

	/**
	 * Construct queue with initial size n.
	 * @param n initial size
	 */
	public PriorityQueue(int n) {
		super();
	}


	/**
	 * Add an element with given priority
	 * @param element 
	 * @param priority
	 */
	public void add(E element, T priority)
	{
		PrioritizedElement<E,T> pe=new PrioritizedElement<E,T>(element,priority);
		this.put(pe,pe);
	}
	
	/**
	 * Add an element with given priority
	 */
	public void add(PrioritizedElement<E,T> pe)
	{
		this.put(pe,pe);
	}
	
	/**
	 * Add an element with given priority
	 * @param element 
	 * @param priority
	 */
/*	public void insertAndReplace(E element, T priority)
	{
		tmp.element=element;
		PrioritizedElement<E,T> pe=get(tmp);
		if(pe != null && pe.priority.compareTo(priority)<0)
		{
			remove(tmp);
			pe.priority=priority;
			put(pe,pe);
		} else{
			pe=new PrioritizedElement<E,T>(element,priority);
			put(pe,pe);
		}
	}*/
	
	/*private int whereIsNext()
	{
		if (policy == PriorityPolicy.LowestHasPriority)
			return 0;
		else return size()-1;
	}*/
	
	private PrioritizedElement<E,T> tmp= new PrioritizedElement<E,T>();
	
	public boolean contains(E element)
	{
		tmp.element=element;
		return containsValue(tmp);
	}
	
	/*public PrioritizedElement<E,T> get(E element){
		PrioritizedElement<E,T> res=null;
		tmp.element=element;
		/int i= indexOf(tmp);
		if(i>=0)
			res= get(i);
		
		return res;
	}*/
	
	/* (non-Javadoc)
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	/*@Override
	public boolean remove(Object o) {
		tmp.element=o;
		return super.remove(tmp);
	}*/
	
	/*public void remove(Object element)
	{
		tmp.element=element;
		return;
	}*/
	
	/**
	 * Get element of highest priority and remove it from list
	 * @return element of highest
	 */
	public E popElement()
	{
		/*int s=whereIsNext();//size()-1;
		PrioritizedElement<E, T> e = this.get(s);
		this.remove(s);
		return e.getElement();*/
		
		return firstEntry().getValue().element;
	}

	/**
	 * Get element of highest priority and leave it in the list
	 * @return element of highest
	 */
	public E peekElement()
	{
		//return this.get(size()-1).getElement();
		//return this.get(whereIsNext()).getElement();
		return peek().element;
	}
	
	/**
	 * 
	 * @return
	 */
	public PrioritizedElement<E,T> pop()
	{
		/*int s=whereIsNext();//size()-1;
		PrioritizedElement<E, T> e = this.get(s);
		this.remove(s);
		return e;*/
		PrioritizedElement<E, T> e=firstKey();
		remove(e);
		return e;
	}
	
	public PrioritizedElement<E,T> peek()
	{
		//return this.get(size()-1);
		//return this.get(whereIsNext());
		return firstKey();
	}
	
	public void dropContentToOutputStream()
	{
		
		System.out.println(toString());
	}
	
	/**
	 * Small class to group element and priority
	 * 
	 * @author Benjamin Perret
	 *
	 * @param <E>
	 * @param <T>
	 */
	public static class PrioritizedElement<E,T extends Comparable<? super T>> implements Comparable<PrioritizedElement<?,T>>
	{

		private static long count=0;
		
		/**
		 * element
		 */
		private E element;
		
		/**
		 * priority
		 */
		private T priority;
		
		private long id=0;

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		
		public int compareTo(PrioritizedElement<?, T> o) {
			int c=priority.compareTo(o.priority);
			if (c==0) 
			{
				if(id < o.id)
					c=-1;
				else if(id>o.id)
					c=1;
			}
			return c;
		}

		public PrioritizedElement(){
			id=count++;
		}
		
		/**
		 * @param element
		 * @param priority
		 */
		public PrioritizedElement(E element, T priority) {
			this();
			this.element = element;
			this.priority = priority;
		}

		/**
		 * @return
		 */
		public E getElement() {
			return element;
		}

		/**
		 * @param element
		 */
		public void setElement(E element) {
			this.element = element;
		}

		/**
		 * @return
		 */
		public T getPriority() {
			return priority;
		}

		public void setPriority(T priority) {
			this.priority = priority;
		}


		public String toString()
		{
			return "("+element + "," +priority + ")";
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			boolean res=false;
			if(obj!=null && obj instanceof PrioritizedElement)
			{
				res=element.equals(((PrioritizedElement)obj).element);
			}
			return res;
		}

		
		
	}

	

	
}


