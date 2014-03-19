/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import java.util.Stack;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.ComponentTreeUtil;

/**
 * Remove of all nodes having a unique child in the component tree.
 * 
 * @author Benjamin Perret
 *
 */
public class SimplifyTree<T> extends Algorithm {

	public ComponentTree<T> tree;
	
	public SimplifyTree(){
		super.inputs="tree";
		super.outputs="tree";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		
		Stack<ComponentNode<T>> s=new Stack<ComponentNode<T>>();
		s.push(tree.getRoot());
		while(!s.isEmpty()){
			ComponentNode<T> n=s.pop();
			while(n.numberOfChildren()==1)
			{
				ComponentNode<T> child=n.getChild(0);
				n.setLevel(child.getLevel());
				tree.deleteNode(child);
				
			}
			for(ComponentNode<T> c:n.getChildren())
				s.push(c);
			
			
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ComponentTree<T> exec(ComponentTree<T> tree){
		return (ComponentTree<T>)new SimplifyTree<T>().process(tree);
	}

	

}
