/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import java.util.Collection;
import java.util.Stack;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeFilter;

/**
 * Apply attribute filters on a component tree
 * 
 * 3 rules are available 
 * - direct : all nodes not that does not fulfill filter rules are deleted
 * - min : all nodes nodes not that does not fulfill filter rules are deleted with their childrens
 * - max : a node is deleted if it and its children do not fulfill filter rules
 * 
 * Combination rule of filters is AND, 
 * @TODO add more combination rules
 * 
 * @author Benjamin Perret
 *
 */
public class FilterComponentTree extends Algorithm {

	
	/**
	 * Filter strategy available
	 * @author Benjamin Perret
	 *
	 */
	public static enum FilterStrategy {Min, Max, Direct};
	
	/**
	 * The component tree
	 */
	public ComponentTree root;
	
	/**
	 * The filter rules
	 */
	public AttributeFilter [] filters;
	
	/**
	 * The strategy
	 */
	public FilterStrategy strategy;

	
	/**
	 * 
	 */
	public FilterComponentTree() {
		super.inputs="root,filters,strategy";
		super.outputs="root";
	}

	private boolean checkFilters(ComponentNode c)
	{
		boolean res=true;
		for(AttributeFilter f: filters)
		{
			res=f.filter(c);
			if(!res) break;
		}
		return res;
	}
	
	private void directFilter()
	{
		Stack<ComponentNode> s= new Stack<ComponentNode>();
		s.push(root.getRoot());
		while(!s.isEmpty())
		{
			ComponentNode c= s.pop();
			for (ComponentNode child:(Collection<ComponentNode>)c.getChildren()) // genericity sucks
				s.push(child);
			if(!checkFilters(c))
			{
				//System.out.println("delete "  + c.location);
				root.deleteNode(c);
			}
			
				
			 
			
		
		}
	}
	
	private void minFilter()
	{
		
		
		Stack<ComponentNode> s= new Stack<ComponentNode>();
		s.push(root.getRoot());
		ComponentNode r=root.getRoot();
		while(!s.isEmpty())
		{
			ComponentNode c= s.pop();
			if(c!=r && !checkFilters(c))
			{
				//System.out.println("delete "  + c.location);
				root.deleteNodeAndChildren(c);
			}
			else{
				for (ComponentNode child:(Collection<ComponentNode>)c.getChildren())
					s.push(child);
			}
			
		
		}
	}
	
	private void maxFilter()
	{
		root.resetFlag(0);
		
		Stack<ComponentNode> s= new Stack<ComponentNode>();
		s.push(root.getRoot());
		while(!s.isEmpty())
		{
			ComponentNode c= s.peek();
			if(c.numberOfChildren()==0)
			{
				
				if(!checkFilters(c))
				{
					
					root.deleteNodeAndChildren(c);
				}
				s.pop();
			}else {
				if(c.flag==0)
				{
					for (ComponentNode child:(Collection<ComponentNode>)c.getChildren())
						s.push(child);
					c.flag=1;
				}
				else s.pop();
			}
			
		
		}
	}
	
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		switch(strategy)
		{
		case Min:
			minFilter();
			break;
		case Max:
			maxFilter();
			break;
		case Direct:
			directFilter();
			break;
		default:
			System.out.println("Not supported yet: " + strategy);
		}

	}

	@SuppressWarnings("unchecked")
	public static<T> ComponentTree<T> exec(ComponentTree<T> tree, AttributeFilter [] filters, FilterStrategy strategy)
	{
		return (ComponentTree<T>)(new FilterComponentTree()).process(tree,filters,strategy);
	}
	
	@SuppressWarnings("unchecked")
	public static<T> ComponentTree<T> exec(ComponentTree<T> tree, AttributeFilter filter, FilterStrategy strategy)
	{
		return (ComponentTree<T>)(new FilterComponentTree()).process(tree,new AttributeFilter[]{filter},strategy);
	}
	
	

}
