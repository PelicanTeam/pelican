//package fr.unistra.pelican.util;
//
//import java.awt.Point;
//import java.util.LinkedList;
//
///**
// * Hierarchical Queue used in several algorithms 
// * @author lefevre
// *
// */
//public class HierarchicalQueue {
//	// private LinkedList[] queue;
//	private LinkedList[] queue;
//
//	private int current;
//	private int number;
//	private int size;
//	private boolean[] used;
//
//	public int getCurrent() {
//		return current;
//	}
//
//	public int getNumber() {
//		return number;
//	}
//	
//	public int size() {
//		return size;
//	}
//	
//
//	public HierarchicalQueue(int size) {
//		queue = new LinkedList[size];
//		for (int i = 0; i < size; i++)
//			queue[i] = new LinkedList();
//		this.size=size;
//		current = 0;
//		number = 0;
//		used=new boolean[size];
//	}
//
//	public void add(Point p, int val) {
//		if (val >= current)
//			queue[val].add(p);
//		else
//			queue[current].add(p);
//		number++;
//		if(!used[val])
//			used[val]=true;
//	}
//	
//	public int used() {
//		int realSize=0;
//		for (int i=0;i<size;i++)
//			if(used[i])
//				realSize++;
//		return realSize;
//	}
//
//	public Point get() {
//		number--;
//		if (queue[current].size() >= 2)
//			return (Point) queue[current].removeFirst();
//		else if (queue[current].size() == 1) {
//			Point p = (Point) queue[current].removeFirst();
//
//			while (current < size - 1 && number != 0
//				&& queue[current].size() == 0)
//				current++;
//			// System.out.println(current+" / "+number);
//
//			return p;
//
//		} else
//			return null;
//	}
//
//	public void clear() {
//		for (int i = 0; i < size; i++)
//			queue[i].clear();
//		current = 0;
//		number = 0;
//	}
//	
//	public void reset() {
//		if(!isEmpty())
//			clear();
//		current = 0;
//		number = 0;
//	}
//	
//	public boolean isEmpty() {
//		// int sum = 0;
//		// for (int i = current; i < queue.length; i++)
//		// sum += queue[i].size();
//		// return (sum == 0);
//		return (number == 0);
//	}
//}

package fr.unistra.pelican.util;

import java.awt.Point;
import java.util.LinkedList;

/**
 * Hierarchical Queue used in several algorithms
 * 
 * @author lefevre
 * 
 */
public class HierarchicalQueue {
	// private LinkedList[] queue;
	private LinkedList[] queue;

	private int current;
	private int number;
	private int size;
//	private boolean[] used;

	public int getCurrent() {
		return current;
	}

	public int getNumber() {
		return number;
	}

	public int size() {
		return size;
	}

	public HierarchicalQueue(int size) {
		queue = new LinkedList[size];
		// for (int i = 0; i < size; i++)
		// queue[i] = new LinkedList();
		this.size = size;
		current = 0;
		number = 0;
//		used = new boolean[size];
	}

	public void add(Point p, int val) {
		if (val >= current) {
			if (queue[val] == null)
				queue[val] = new LinkedList();
			queue[val].add(p);
		} else {
			if (queue[current] == null)
				queue[current] = new LinkedList();
			queue[current].add(p);
		}
		number++;
//		if (!used[val])
//			used[val] = true;
	}

//	public int used() {
//		int realSize = 0;
//		for (int i = 0; i < size; i++)
//			if (used[i])
//				realSize++;
//		return realSize;
//	}

	public Point get() {
		number--;
		if (queue[current] != null && queue[current].size() >= 2)
			return (Point) queue[current].removeFirst();
		else if (queue[current] != null && queue[current].size() == 1) {
			Point p = (Point) queue[current].removeFirst();

			while (current < size - 1 && number != 0
				&& (queue[current] == null || queue[current].size() == 0))
				current++;
			// System.out.println(current+" / "+number);

			return p;

		} else
			return null;
	}

	public void clear() {
		for (int i = 0; i < size; i++) {
			if (queue[i] != null) {
				queue[i].clear();
			queue[i]=null;
			}
//			used[i] = false;
		}
		current = 0;
		number = 0;
	}

	public void reset() {
		if (!isEmpty())
			clear();
		current = 0;
		number = 0;
	}

	public boolean isEmpty() {
		// int sum = 0;
		// for (int i = current; i < queue.length; i++)
		// sum += queue[i].size();
		// return (sum == 0);
		return (number == 0);
	}
}
