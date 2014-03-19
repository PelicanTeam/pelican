/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.util.connectivityTrees.ComponentNode;
import fr.unistra.pelican.util.connectivityTrees.ComponentTree;
import fr.unistra.pelican.util.connectivityTrees.attributes.ComponentAttribute;
import fr.unistra.pelican.util.connectivityTrees.attributes.UnsupportedDataTypeException;
import fr.unistra.pelican.util.vectorial.ordering.VectorialBasedComponentOrdering;
import fr.unistra.pelican.util.vectorial.ordering.VectorialOrdering;

/**
 * Reconstruct an image from a connected component tree
 * 
 * @author Benjamin Perret
 *
 */
public class ReconstructImageFromTree extends Algorithm {

	/**
	 * Data usable for reconstruction
	 * @author Benjamin Perret
	 *
	 */
	public static enum Data{Level,Area,Attribute};
	
	/**
	 * Default data is connected component level
	 */
	public Data data=Data.Level;
	
	/**
	 * Take data from specified attribute
	 */
	public ComponentAttribute attribute;
	
	/**
	 * The component tree
	 */
	public ComponentTree tree;
	
	/**
	 * Constraint result to be smaller than underlying image (ensure anti-extensivity)
	 */
	public boolean constraint=false;
	
	/**
	 * Class of he attribute used for reconstruction
	 */
	public Class<? extends ComponentAttribute> clazz;
	
	/**
	 * Result
	 */
	public Image output;
	
	/**
	 * Dimensions of result
	 */
	private int xdim,ydim,zdim;
	
	/**
	 * Root of the tree
	 */
	private ComponentNode root;
	
	/**
	 * Reconstrucut only leaves
	 */
	public boolean onlyLeaves=false;
	
	public ReconstructImageFromTree(){
		super.inputs="tree";
		super.options="onlyLeaves,data,clazz";
		super.outputs="output";
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		xdim=tree.getXdim();
		ydim=tree.getYdim();
		zdim=tree.getZdim();
		root=tree.getRoot();
		switch (data)
		{
		case Level:
			try {
					drawLevel();
				} catch (UnsupportedDataTypeException e) {
					throw new AlgorithmException("Error message was: " +e.getMessage());
				}
			break;
		case Area:	
			drawArea();
			break;
		case Attribute:
			if (clazz==null)
				throw new AlgorithmException("You must specify a class Attribute to use when using Attribute mode!");
			try {
					drawAttr();
				} catch (UnsupportedDataTypeException e) {
					throw new AlgorithmException("Unsupported datatype exception caught! " +e);
				}
			break;
		}
		

	}

	private void drawAttr() throws UnsupportedDataTypeException
	{
		Object vv= tree.findNodeAt(0, 0, 0).getAttributeValue(clazz);
		if(vv instanceof Double)
		{
		output=new DoubleImage(xdim,ydim,zdim,1,1);
		output.fill(Double.NEGATIVE_INFINITY);
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
				{
					ComponentNode n= tree.findNodeAt(x, y, z);
					if(!onlyLeaves || n.numberOfChildren()==0)
					{
					Object v= n.getAttributeValue(clazz);
					if(v!=null)
					output.setPixelXYZDouble(x, y, z,(Double)v);	
					else {
						System.out.println("Attribute does not exist for node: " +tree.findNodeAt(x, y, z) + " for pixel " + "["+x+";"+y+";"+z+"]" + "  NOW I WILL CRASH!");
						v.toString();
					}
					}
				}
		}else if(vv instanceof double[])
		{
			output=new DoubleImage(xdim,ydim,zdim,1,((double[])vv).length);
			output.fill(Double.NEGATIVE_INFINITY);
			for(int z=0;z<zdim;z++)
				for(int y=0;y<ydim;y++)
					for(int x=0;x<xdim;x++)
					{
						ComponentNode n= tree.findNodeAt(x, y, z);
						if(!onlyLeaves || n.numberOfChildren()==0)
						{
							Object v= n.getAttributeValue(clazz);
							if(v!=null)
								output.setVectorPixelXYZTDouble(x, y, z,0,(double [])v);	
							else {
								System.out.println("Attribute does not exist for node: " +tree.findNodeAt(x, y, z) + " for pixel " + "["+x+";"+y+";"+z+"]" + "  NOW I WILL CRASH!");
								v.toString();
							}
						}
					}
		} else if(vv instanceof Integer)
		{
			output=new IntegerImage(xdim,ydim,zdim,1,1);
			//output.fill(Double.NEGATIVE_INFINITY);
			for(int z=0;z<zdim;z++)
				for(int y=0;y<ydim;y++)
					for(int x=0;x<xdim;x++)
					{
						ComponentNode n= tree.findNodeAt(x, y, z);
						if(!onlyLeaves || n.numberOfChildren()==0)
						{
						Object v= n.getAttributeValue(clazz);
						if(v!=null)
						output.setPixelXYZInt(x, y, z,(Integer)v);	
						else {
							System.out.println("Attribute does not exist for node: " +tree.findNodeAt(x, y, z) + " for pixel " + "["+x+";"+y+";"+z+"]" + "  NOW I WILL CRASH!");
							v.toString();
						}
						}
					}
			}else if(vv instanceof Boolean)
			{
				output=new BooleanImage(xdim,ydim,zdim,1,1);
				//output.fill(Double.NEGATIVE_INFINITY);
				for(int z=0;z<zdim;z++)
					for(int y=0;y<ydim;y++)
						for(int x=0;x<xdim;x++)
						{
							ComponentNode n= tree.findNodeAt(x, y, z);
							if(!onlyLeaves || n.numberOfChildren()==0)
							{
							Object v= n.getAttributeValue(clazz);
							if(v!=null)
							output.setPixelXYZBoolean(x, y, z,(Boolean)v);	
							else {
								System.out.println("Attribute does not exist for node: " +tree.findNodeAt(x, y, z) + " for pixel " + "["+x+";"+y+";"+z+"]" + "  NOW I WILL CRASH!");
								v.toString();
							}
							}
						}
				}else throw new UnsupportedDataTypeException("I don't know how to manage this kind of attribute " + vv + " of type " + vv.getClass());
		output.setName(clazz.getSimpleName());
	
	}
	
	private void drawLevel() throws UnsupportedDataTypeException
	{
		Object v=tree.findNodeAt(0, 0, 0).getLevel();
		
		if(v instanceof Double)
		{
			output=new DoubleImage(xdim,ydim,zdim,1,1);
			
			output.fill(Double.NEGATIVE_INFINITY);
			for(int z=0;z<zdim;z++)
				for(int y=0;y<ydim;y++)
					for(int x=0;x<xdim;x++)
					{
						ComponentNode n= tree.findNodeAt(x, y, z);
						if(!onlyLeaves || n.numberOfChildren()==0)
						{
							double level=(Double)n.getLevel();
							if(constraint)
								level=Math.min(level, tree.image.getPixelXYZDouble(x, y, z));
							output.setPixelXYZDouble(x, y, z,level);	
						}
							
					}
		} else if (v instanceof double[])
		{
			output=new DoubleImage(xdim,ydim,zdim,1,((double [])tree.findNodeAt(0, 0, 0).getLevel()).length);
			output.fill(Double.NEGATIVE_INFINITY);
			
			VectorialBasedComponentOrdering comp=(VectorialBasedComponentOrdering )tree.getComparator();
			VectorialOrdering comparator=comp.getVectorialOrdering();
			for(int z=0;z<zdim;z++)
				for(int y=0;y<ydim;y++)
					for(int x=0;x<xdim;x++)
					{
					
							
						ComponentNode n= tree.findNodeAt(x, y, z);
						
							
						double [] level = (double [])n.getLevel();
						
						if(constraint)
							level=comparator.min(level, tree.image.getVectorPixelXYZDouble(x, y, z));
						if(!onlyLeaves || n.numberOfChildren()==0)
							output.setVectorPixelXYZTDouble(x, y, z, 0, level);	
					}
		}
		else throw new UnsupportedDataTypeException("Data type not supported: " +v.getClass());
		output.setName("Reconstruction");
	}
	
	private void drawArea()
	{
		output=new IntegerImage(xdim,ydim,zdim,1,1);
		output.fill(Integer.MIN_VALUE);
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
				{
					ComponentNode n= tree.findNodeAt(x, y, z);
					if(!onlyLeaves || n.numberOfChildren()==0)
						output.setPixelXYZInt(x, y, z,n.getArea());	
				}
		output.setName("Area map");
	}
	
	public static Image exec(ComponentTree tree)
	{
		return (Image)(new ReconstructImageFromTree()).process(tree);
	}
	
	public static Image exec(ComponentTree tree, boolean onlyLeaves)
	{
		return (Image)(new ReconstructImageFromTree()).process(tree,onlyLeaves);
	}
	
	public static Image exec(ComponentTree tree, Data data)
	{
		return (Image)(new ReconstructImageFromTree()).process(tree,null,data);
	}
	
	public static Image exec(ComponentTree tree, Data data,Class<? extends ComponentAttribute> clazz)
	{
		return (Image)(new ReconstructImageFromTree()).process(tree,null,data,clazz);
	}
	
	public static Image exec(ComponentTree tree, Data data,Class<? extends ComponentAttribute> clazz, boolean onlyLeaves)
	{
		return (Image)(new ReconstructImageFromTree()).process(tree,onlyLeaves,data,clazz);
	}
}
