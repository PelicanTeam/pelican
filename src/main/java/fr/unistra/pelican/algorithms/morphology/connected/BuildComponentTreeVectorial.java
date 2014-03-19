/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.morphology.connected.ReconstructImageFromTree.Data;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.UnionFindHelper;
import fr.unistra.pelican.util.connectivityTrees.ComponentTreeUtil.TreeType;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeSum;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeVolume;
import fr.unistra.pelican.util.connectivityTrees.attributes.UnsupportedDataTypeException;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;
import fr.unistra.pelican.util.vectorial.ordering.ComponentNodeOrdering;
import fr.unistra.pelican.util.vectorial.ordering.LexicographicalSortedOrdering;
import fr.unistra.pelican.util.vectorial.ordering.VectorialBasedComponentOrdering;
import fr.unistra.pelican.util.vectorial.ordering.VectorialOrdering;

/**
 * Build the component tree of a vectorial image using Tarjan union find algorithm.
 * 
 * @author Benjamin Perret
 *
 */
public class BuildComponentTreeVectorial extends Algorithm {

	
	/**
	 * Internal fields for dimensions
	 */
	private int xdim,ydim,zdim;
	
	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Connectivity to use
	 */
	public Connectivity3D connectivity;
	
	/**
	 * Do we need to build max or min tree?
	 */
	public TreeType treeType=TreeType.Max;
	
	/**
	 * The tree
	 */
	public ComponentTree<double []> tree;
	
	/**
	 * Ordering for node of the tree
	 */
	public Comparator<ComponentNode<double []>> comparator;
	
	/**
	 * Vectorial ordering
	 */
	public VectorialOrdering vOrdering;
	
	/**
	 * Root node
	 */
	private ComponentNode<double []> root;
	
	/**
	 * List of all nodes
	 */
	private ArrayList<ComponentNode<double []>> nodeList;
	
	/**
	 * Set of the nodes of the tree management using union find
	 */
	private UnionFindHelper treeSet;
	
	/**
	 * Set of nodes of pixel management using union find
	 */
	private UnionFindHelper nodeSet;
	
	/**
	 * buffer for all nodes
	 */
	private ComponentNode<double []> [][][] nodes;
	
	/**
	 * for all component nodes it gives the positions of the representative tree node
	 */
	private ComponentNode<double []> [][][] nodeslow;
	
	/**
	 * Boolean mask to mark processed pixels
	 */
	private BooleanImage processed;
	
	/*
	public void dropLow()
	{
		for(int y=0;y<ydim;y++)
		{System.out.print("|");
			for(int x=0;x<xdim;x++)
			{
				Point3D p =nodeslow[0][y][x].location;
				//if(p.x == x && p.y==y)
				//	System.out.print("*(" +p.x + "," +p.y + ") |");
				System.out.print(" (" +p.x + "," +p.y + ") |");
			}
			System.out.println();
		}
				
	}*/
	
	public BuildComponentTreeVectorial(){
		this.inputs="inputImage,connectivity,comparator";
		this.options="treeType,vOrdering";
		this.outputs="tree";
	}
	
	/**
	 * Initialize all data structures and so on
	 */
	private void intialize(){
		this.xdim = inputImage.xdim;
		this.ydim = inputImage.ydim;
		this.zdim = inputImage.zdim;
		int s=inputImage.size();
		processed = new BooleanImage(inputImage.xdim,inputImage.ydim, inputImage.zdim,1,1);
		nodeList= new ArrayList<ComponentNode<double []>>(s);

		treeSet = new UnionFindHelper(xdim,ydim,zdim);
		nodeSet = new UnionFindHelper(xdim,ydim,zdim);
		nodes = new ComponentNode[inputImage.zdim][inputImage.ydim][inputImage.xdim];
		nodeslow = new ComponentNode[inputImage.zdim][inputImage.ydim][inputImage.xdim];
		int nbV = connectivity.getConnectedNeighbours().length;
		for(int z=0;z<inputImage.zdim;z++)
			for(int y=0;y<inputImage.ydim;y++)
				for(int x=0;x<inputImage.xdim;x++)
		{
			Point3D p = new Point3D(x,y,z);
			ComponentNode<double []> c =new ComponentNode<double []>(p,inputImage.getVectorPixelXYZDouble(x,y,z));
			c.perimeter=nbV;
			nodeList.add(c);
			treeSet.MakeSet(p);
			c.locator=nodeSet.MakeSet(p);

			nodes[z][y][x]=c;
			nodeslow[z][y][x]=c;
		}
		if(comparator instanceof VectorialBasedComponentOrdering)
			vOrdering=((VectorialBasedComponentOrdering)comparator).getVectorialOrdering();
		else {
			System.err.println(this + " cannot extract vectorial ordering from component ordering, highest value of nodes are NOT computed !");
		}
		if(treeType == TreeType.Min)
		{
			comparator=Collections.reverseOrder(comparator);
			/**
			 *  @TODO inverse vOrdering
			 */
			//vOrdering=Collections.reverseOrder(vOrdering);
		}
			
		Collections.sort(nodeList,Collections.reverseOrder(comparator));

	}
	
	/**
	 * Merge two nodes, take care of minimizing path length to root node of the set
	 * @param x
	 * @param y
	 * @return
	 */
	private ComponentNode<double []> mergeNodes(ComponentNode<double []> x, ComponentNode<double []> y)
	{
		ComponentNode<double []> tmpNode = findNodeAt(nodeSet.link(x.location, y.location));
		ComponentNode<double []> tmpNode2;
		if(tmpNode == y)
		{
			y.addAllChildren(x.getChildren());
			x.clearChildren();
			tmpNode2=x;
		} else {
			x.addAllChildren(y.getChildren());
			y.clearChildren();
			tmpNode2=y;
		}
		tmpNode.setArea(tmpNode.getArea() + tmpNode2.getArea());
		tmpNode.perimeter+=tmpNode2.perimeter-2;
		if(vOrdering!=null)tmpNode.setHighest(vOrdering.max(tmpNode.getHighest(), tmpNode2.getHighest()));
		tmpNode.mergeAttributsWith(tmpNode2, comparator);
		return tmpNode;
	}
	
	private ComponentNode<double []> findNodeAt(Point3D p)
	{
		return nodes[p.z][p.y][p.x];
	}
	
	private ComponentNode<double []> findLowestNodeAt(Point3D p)
	{
		return nodeslow[p.z][p.y][p.x];
	}
	
	private void setLowestNodeAt(Point3D p,ComponentNode<double []>  c)
	{
		nodeslow[p.z][p.y][p.x]=c;
	}
	
	/**
	 * Do it baby!
	 */
	private void mainLoop()
	{
		for (ComponentNode<double []> c : nodeList) { // for each node
			
			Point3D curTree = treeSet.find(c.location); // locate representative node of the component
			ComponentNode<double []> curNode = findNodeAt(nodeSet // locate representative node of the tree (necessary because shortening of path length may put them at different locations)
					.find(findLowestNodeAt(curTree).location));
			
			
			// lets process neighbors
			connectivity.setCurrentPoint(c.location);
			for (Point3D p : connectivity) {
				if (p.x >= 0 && p.y >= 0 && p.z >= 0 && p.x < inputImage.xdim
						&& p.y < inputImage.ydim && p.z < inputImage.zdim) {
					
					// find representative component node and tree node of neighbor 
					Point3D adjTree = treeSet.find(p);
					ComponentNode<double []> adjNode = findNodeAt(nodeSet
							.find(findLowestNodeAt(adjTree).location));
					
					// if neighbor is already processed and we are less or equal to it
					if (processed.getPixelXYZBoolean(p.x, p.y, p.z)
							&& comparator.compare(c,adjNode)<=0 ) {
						// perhaps we are part of the same node ?
						if (curNode != adjNode) {
							if (comparator.compare(curNode,adjNode)==0) {
								// we are equal lets merge our tree nodes
								curNode = mergeNodes(adjNode, curNode);
								
							} else {
								// i am smaller i will take you as my child for the rest of my life.
								curNode.addChild(adjNode);
								curNode.setArea(curNode.getArea()
										+ adjNode.getArea());
								curNode.perimeter+=adjNode.perimeter-2;
								if(vOrdering!=null)curNode.setHighest(vOrdering.max(curNode
										.getHighest(), adjNode.getHighest()));
							
								
							}
							// finalize linking of components
							curTree = treeSet.link(adjTree, curTree);
							// remember where is the representative tree node for me 
							setLowestNodeAt(curTree,curNode);

							
						}else{
							curNode.perimeter-=2;
						}
					}
					
				}
				
				
			}
			Point3D ccc = c.location;
			processed.setPixelXYZBoolean(ccc.x, ccc.y, ccc.z, true);
		}
	}
	
	/**
	 * During the process only parents are linked to their children, we now build the link from children to parent
	 * @param c
	 * @param parent
	 */
	private void setParents(ComponentNode<double []> c, ComponentNode<double []> parent) {
		Stack<ComponentNode<double []>> s = new Stack<ComponentNode<double []>>();
		s.push(c);

		c.setParent(parent);
		
		while (!s.isEmpty()) {
			ComponentNode<double []> cc=s.pop();

			for (ComponentNode<double []> t : cc.getChildren()) {

				t.setParent(cc);
				s.push(t);
			}
		}
	}
	
	
	
	/**
	 * Determines whether or not the graph contained in the buffer is a tree or a forest
	 * @return
	 */
	private boolean isForest()
	{
		
		Point3D p = new Point3D();
		// find root of a point (no matter which one)
		ComponentNode<double[]> root=findLowestNodeAt(treeSet.find(nodeSet.find(nodeList.get(0).location)));
		// find roots of all other points if it is always the same then we have a tree
		for(int z=0;z<nodes.length;z++)
			for(int y=0;y<nodes[z].length;y++)
				for(int x=0;x<nodes[z][y].length;x++)
				{
					p.setLocation(x, y, z);
					ComponentNode<double []> n = findLowestNodeAt(treeSet.find(nodeSet.find(nodes[p.z][p.y][p.x].location)));
					if(n!=root )
					{
						return true;
					}
				}
		return false;
	}
	
	/**
	 * Create a tree from a forest, attach all convex part to a root
	 * @param root
	 */
	private void ensureTreeStructure(ComponentNode<double[]> root)
	{
		Point3D p = new Point3D();
		for(int z=0;z<nodes.length;z++)
			for(int y=0;y<nodes[z].length;y++)
				for(int x=0;x<nodes[z][y].length;x++)
				{
					p.setLocation(x, y, z);
					ComponentNode<double []> n =  findLowestNodeAt(treeSet.find(nodeSet.find(nodes[p.z][p.y][p.x].location)));;
					if(n.parent == null )
					{
						setParents(n, root);
						root.addChild(n);
					}
					
				}
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		this.xdim = inputImage.xdim;
		this.ydim = inputImage.ydim;
		this.zdim = inputImage.zdim;
		intialize();
		mainLoop();
		
		
		if(isForest()){
			//System.out.println("Your connectivity sucks or at least is partial, i will create an imaginary root to ensure tree structure!");
			root=new ComponentNode<double[]>(null,null);
			ensureTreeStructure(root);
		}
		else{
			root = findLowestNodeAt(treeSet.find(nodeSet.find(nodeList.get(0).location)));
			setParents(root,null);
		}
		tree= new ComponentTree<double []>(root,nodeSet,nodes);
		tree.setXdim(inputImage.xdim);
		tree.setYdim(inputImage.ydim);
		tree.setZdim(inputImage.zdim);
		tree.setConnectivity(connectivity);
		tree.setComparator(comparator);
		tree.image=inputImage;

	}

	
	
	public static ComponentTree<double []> exec(Image inputImage, Connectivity3D connectivity, Comparator<ComponentNode<double []>> comparator){
		return (ComponentTree<double []>)(new BuildComponentTreeVectorial()).process(inputImage,connectivity,comparator);
	}
	
	public static ComponentTree<double []> exec(Image inputImage, Connectivity3D connectivity, Comparator<ComponentNode<double []>> comparator, TreeType treeType){
		return (ComponentTree<double []>)(new BuildComponentTreeVectorial()).process(inputImage,connectivity,comparator,treeType);
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DoubleImage im = new DoubleImage(3,1,1,1,1);
		im.setPixels(new double[]{1,2,1});
		Connectivity3D con = TrivialConnectivity.getFourNeighbourhood();
		ComponentNodeOrdering<double []> cno=new VectorialBasedComponentOrdering(new LexicographicalSortedOrdering());
		
		ComponentTree<double []> root = BuildComponentTreeVectorial.exec(im, con, cno);
		System.out.println(root.getRoot());
		//Image im =ImageLoader.exec("samples/lennaGray256.png");
		//im = IMath.mult(new DoubleImage(im,true), -1.0);
		//DoubleImage im = new DoubleImage(3,5,1,1,1);
		//im.setPixels(new double[]{110,90,100,50,50,50,40,20,50,50,50,50,120,70,80});
	/*	DoubleImage im = new DoubleImage(3,7,1,1,2);
		im.setPixels(new double[]{40,20,40,20,40,20,50,20,80,20,50,20,40,20,40,20,40,20,10,10,15,10,0,10,20,40,20,40,20,40,10,50,10,80,10,50,20,40,20,40,20,40});
		Connectivity3D con = TrivialConnectivity.getFourNeighbourhood();
		ComponentNodeOrdering<double []> cno=new VectorialBasedComponentOrdering(new LexicographicalSortedOrdering());
		
		ComponentTree<double []> root = BuildComponentTreeVectorial.exec(im, con, cno);
	//	System.out.println(root.getRoot());
		try {
			root.addAttribute(new AttributeSum());
			root.addAttribute(new AttributeVolume());
		} catch (UnsupportedDataTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image im1=ReconstructImageFromTree.exec(root, Data.Area);
		Image im2=ReconstructImageFromTree.exec(root, Data.Attribute, AttributeSum.class);
		Image im3=ReconstructImageFromTree.exec(root, Data.Attribute, AttributeVolume.class);
		//DoubleImage im2 = new DoubleImage(im.xdim,im.ydim,im.zdim,1,1);
		//drawAreaOnImage(im2,root);
		//Viewer2D.exec(LabelsToRandomColors.exec(im2));
		//Image im2 = im.copyImage(false);
		MultiView m =MViewer.exec(im,im1,im2,im3);
		//m.add(im2);*/
	}

}
