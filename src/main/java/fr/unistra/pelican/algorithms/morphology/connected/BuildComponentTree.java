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
import fr.unistra.pelican.algorithms.morphology.connected.FilterComponentTree.FilterStrategy;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.UnionFindHelper;
import fr.unistra.pelican.util.connectivityTrees.ComponentTreeUtil.TreeType;
import fr.unistra.pelican.util.connectivityTrees.attributes.AreaAttributFilter;
import fr.unistra.pelican.util.connectivityTrees.attributes.AttributeFilter;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;

/**
 * 
 * Build component tree from a monoband image
 * 
 * @author Benjamin Perret
 * @deprectaed use BuildComponentTreeVectorial even on monoband image!
 */
public class BuildComponentTree extends Algorithm {


	/**
	 * Image dimensions
	 */
	private int xdim,ydim,zdim;
	
	/**
	 * TreeType to compute
	 */
	public TreeType treeType=TreeType.Max;
	
	/**
	 * Input image
	 */
	public Image inputImage;
	
	/**
	 * Connectivity to use
	 */
	public Connectivity3D connectivity;
	
	/**
	 * Result
	 */
	public ComponentTree<Double> tree;
	
	/**
	 * My sweet comparator
	 */
	private Comparator<ComponentNode<Double>> comparator = new Comparator<ComponentNode<Double>>(){
		@Override
		public int compare(ComponentNode<Double> o1, ComponentNode<Double> o2) {
			return o1.getLevel().compareTo(o2.getLevel());
		}

		
	};
	
	/**
	 * The root node
	 */
	private ComponentNode<Double> root;
	
	/**
	 * The universe of nodes
	 */
	private ArrayList<ComponentNode<Double>> nodeList;
	
	/**
	 * Set of nodes
	 */
	private UnionFindHelper treeSet;
	
	/**
	 * Set of connected components
	 */
	private UnionFindHelper nodeSet;
	
	/**
	 * All connected components
	 */
	private ComponentNode<Double> [][][] nodes;
	
	/**
	 * Corresponding nodes in the tree
	 */
	private ComponentNode<Double> [][][] nodeslow;
	
	/**
	 * mask of processed pixels
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
	
	public BuildComponentTree(){
		this.inputs="inputImage,connectivity";
		this.options="treeType";
		this.outputs="tree";
	}
	
	
	private void intialize(){
		int s=inputImage.size();
		processed = new BooleanImage(inputImage.xdim,inputImage.ydim, inputImage.zdim,1,1);
		nodeList= new ArrayList<ComponentNode<Double>>(s);
		//lowestNodes = new TreeMap<ComponentTree, ComponentTree>();
		treeSet = new UnionFindHelper(xdim,ydim,zdim);
		nodeSet = new UnionFindHelper(xdim,ydim,zdim);
		nodes = new ComponentNode[inputImage.zdim][inputImage.ydim][inputImage.xdim];
		nodeslow = new ComponentNode[inputImage.zdim][inputImage.ydim][inputImage.xdim];
		
		//labelMap = new IntegerImage(xdim,ydim,zdim,1,1);
		for(int z=0;z<inputImage.zdim;z++)
			for(int y=0;y<inputImage.ydim;y++)
				for(int x=0;x<inputImage.xdim;x++)
		{
			Point3D p = new Point3D(x,y,z);
			ComponentNode<Double> c =new ComponentNode<Double>(p,inputImage.getPixelXYZDouble(x,y,z));
			
			nodeList.add(c);
			treeSet.MakeSet(p);
			c.locator=nodeSet.MakeSet(p);
			//lowestNodes.put(c,c);
			nodes[z][y][x]=c;
			nodeslow[z][y][x]=c;
		}
		
		if(treeType == TreeType.Min)
			comparator=Collections.reverseOrder(comparator);
		Collections.sort(nodeList,Collections.reverseOrder(comparator));
		
	}
	
	private ComponentNode<Double> mergeNodes(ComponentNode<Double> x, ComponentNode<Double> y)
	{
		ComponentNode<Double> tmpNode = findNodeAt(nodeSet.link(x.location, y.location));
		ComponentNode<Double> tmpNode2;
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
		tmpNode.setHighest(Math.max(tmpNode.getHighest(), tmpNode2.getHighest()));
		return tmpNode;
	}
	
	private ComponentNode<Double> findNodeAt(Point3D p)
	{
		return nodes[p.z][p.y][p.x];
	}
	
	private ComponentNode<Double> findLowestNodeAt(Point3D p)
	{
		return nodeslow[p.z][p.y][p.x];
	}
	
	private void setLowestNodeAt(Point3D p,ComponentNode<Double>  c)
	{
		nodeslow[p.z][p.y][p.x]=c;
	}
	
	private void mainLoop()
	{

		for (ComponentNode<Double> c : nodeList) {
	
			Point3D curTree = treeSet.find(c.location);
			ComponentNode<Double> curNode = findNodeAt(nodeSet
					.find(findLowestNodeAt(curTree).location));
			connectivity.setCurrentPoint(c.location);
			for (Point3D p : connectivity) {
				if (p.x >= 0 && p.y >= 0 && p.z >= 0 && p.x < inputImage.xdim
						&& p.y < inputImage.ydim && p.z < inputImage.zdim) {
					Point3D adjTree = treeSet.find(p);
					ComponentNode<Double> adjNode = findNodeAt(nodeSet
							.find(findLowestNodeAt(adjTree).location));
					if (processed.getPixelXYZBoolean(p.x, p.y, p.z)
							&& comparator.compare(c,adjNode)<=0 ) {
						
						if (curNode != adjNode) {
							if (comparator.compare(curNode,adjNode)==0) {
								curNode = mergeNodes(adjNode, curNode);
								
							} else {
								curNode.addChild(adjNode);
								curNode.setArea(curNode.getArea()
										+ adjNode.getArea());
								curNode.setHighest(Math.max(curNode
										.getHighest(), adjNode.getHighest()));
							
								
							}
							curTree = treeSet.link(adjTree, curTree);
							setLowestNodeAt(curTree,curNode);
	
						}
					}
					
				}
				
				
			}
			Point3D ccc = c.location;
			processed.setPixelXYZBoolean(ccc.x, ccc.y, ccc.z, true);
		}
	}
	
	private void setParents(ComponentNode<Double> c, ComponentNode<Double> parent) {
		Stack<ComponentNode<Double>> s = new Stack<ComponentNode<Double>>();
		s.push(c);

		c.setParent(parent);
		
		while (!s.isEmpty()) {
			ComponentNode<Double> cc=s.pop();

			for (ComponentNode<Double> t : cc.getChildren()) {

				t.setParent(cc);
				s.push(t);
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
		root = findLowestNodeAt(treeSet.find(nodeSet.find(nodeList.get(0).location)));
		tree= new ComponentTree<Double>(root,nodeSet,nodes);
		setParents(root,null);
		tree.setXdim(inputImage.xdim);
		tree.setYdim(inputImage.ydim);
		tree.setZdim(inputImage.zdim);
		tree.setConnectivity(connectivity);
		tree.image=inputImage;

	}

	
	
	public static ComponentTree<Double> exec(Image inputImage, Connectivity3D connectivity){
		return (ComponentTree<Double>)(new BuildComponentTree()).process(inputImage,connectivity);
	}
	
	public static ComponentTree<Double> exec(Image inputImage, Connectivity3D connectivity, TreeType treeType){
		return (ComponentTree<Double>)(new BuildComponentTree()).process(inputImage,connectivity,treeType);
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Image im =ImageLoader.exec("samples/lennaGray256.png");
		//Image im =ImageLoader.exec("samples/AstronomicalImagesFITS/PGC0002600_u.fits");
		//im = IMath.mult(new DoubleImage(im,true), -1.0);
		DoubleImage im = new DoubleImage(3,5,1,1,1);
		im.setPixels(new double[]{110,90,100,50,50,50,40,20,50,50,50,50,120,70,80});
		Connectivity3D con = TrivialConnectivity.getFourNeighbourhood();
		ComponentTree<Double> root = BuildComponentTree.exec(im, con);
		
		AreaAttributFilter filter = new AreaAttributFilter(10);
		root = FilterComponentTree.exec(root, new AttributeFilter[]{filter}, FilterStrategy.Min);
		Image im2=ReconstructImageFromTree.exec(root);
		//ComponentTree<Double> root2 = BuildComponentTree.exec(im, con,TreeType.Min);
		//System.out.println(root.getRoot());
	/*	try {
			root.addAttribute(new AttributePerimetre());
			//root.addAttribute(new AttributeVolume());
			//root2.addAttribute(new AttributeVolume());
		} catch (UnsupportedDataTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//DoubleImage im2 = (DoubleImage)ReconstructImageFromTree.exec(root,Data.Attribute,AttributePerimetre.class);//new DoubleImage(im.xdim,im.ydim,im.zdim,1,1);
		//drawAreaOnImage(im2,root);
		//DoubleImage im3 = (DoubleImage)ReconstructImageFromTree.exec(root2,Data.Attribute,AttributeVolume.class);// new DoubleImage(im.xdim,im.ydim,im.zdim,1,1);
		//drawAreaOnImage(im3,root2);
		//Viewer2D.exec(LabelsToRandomColors.exec(im2));
		//Image im2 = im.copyImage(false);
		MultiView m =MViewer.exec(im);
		m.add(im2);
		//m.add(im3);
	
	}

}
