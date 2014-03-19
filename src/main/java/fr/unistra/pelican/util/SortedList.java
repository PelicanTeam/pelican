/**
 * 
 */
package fr.unistra.pelican.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


/**
 * Sorted List, can handle any object implementing interface Comparable.
 * 
 * 
 * @author Benjamin Perret
 *
 */
public class  SortedList<T extends Comparable<? super T>> extends ArrayList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3863857470997634496L;

	/**
	 * Default constructor
	 */
	public SortedList() {
		super();
	}

	/**
	 * Construct sorted list with an initial capacity of n elements
	 * @param n Initial capacity
	 */
	public SortedList(int n) {
		super(n);
	}
	
	public int findElement(T element)
	{
		
		int s=size();
		int res;
		
		if(s>0){
			int i= findIndexOf(element,0, s);
			int ii=i;
			while(ii>=0 ){
				T o=get(ii);
				if(o.compareTo(element)!=0)
					ii=-1;
				else if(get(ii)==element)
					return ii;
				ii--;
			}
			ii=i+1;
			while(ii<size() ){
				T o=get(ii);
				if(o.compareTo(element)!=0)
					ii=size();
				else if(get(ii)==element)
					return ii;
				ii++;
			}
		}
			
		
	
		
		return -1;
	}
	
	public int findIndexOf(T element)
	{
		
		int s=size();

		
		if(s>0)
			return findIndexOf(element,0, s);
		
		return 0;
	}
	
	private int findIndexOf(T element,int min, int max)
	{
		
		while(min != max)
		{
			int mid=(min+max)/2;
		
			int comp=this.get(mid).compareTo(element);
			if (comp == 0)
			{
				min=max=mid;
			}
			else if(comp<0)
			{
				
				min=mid+1;
			}
			else
			{
				max=mid;
			}
			
		}
		return min;
		
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	public boolean add(T element)
	{
		super.add(findIndexOf(element),element);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Vector#add(int, java.lang.Object)
	 */
	public void add(int i,T element)
	{
		super.add(findIndexOf(element),element);
		
	}
	
	/* (non-Javadoc)
	 * @see java.util.Vector#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> element)
	{
		//super.ensureCapacity(Math.max(super.capacity(),super.elementCount + element.size()));
		for (T e:element)
			this.add(e);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Vector#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int i,Collection<? extends T> element)
	{
		
		return this.addAll(element);
	}

	
	public static void main(String[] args) {
		SortedList<Double> sl =new SortedList<Double>(258*258);
		Date d1=new Date();
		for(int i=0;i<258*258;i++)
		  sl.add(Math.random()*100);
	
		
		Date d2=new Date();
		
		//for(Double a:sl)
		//	System.out.println(a);
		System.out.println("t " + (d2.getTime()-d1.getTime()));

	}
}
