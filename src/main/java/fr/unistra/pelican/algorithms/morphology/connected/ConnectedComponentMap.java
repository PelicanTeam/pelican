/**
 * 
 */
package fr.unistra.pelican.algorithms.morphology.connected;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;
import fr.unistra.pelican.util.Point3D;
import fr.unistra.pelican.util.connectivityTrees.UnionFindHelper;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.FlatConnectivity;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;

/**
 * Performs labeling of connected component with respect to a given a connectivity.
 * 
 * Note that input image is only there to provide information on dimensions 
 * as the connectivity contains all informations needed for construction 
 * of connected components...
 * 
 * 
 * @author Benjamin Perret
 *
 */
public class ConnectedComponentMap extends Algorithm {

	
	public Image inputImage;
	
	public Connectivity3D connectivity;
	
	public IntegerImage labelMap;
	
	private UnionFindHelper pSet ;
	
	private int xdim,ydim,zdim;
	
	private ArrayList<Point3D> pList;
	
	/**
	 * 
	 */
	public ConnectedComponentMap() {
		super.inputs="inputImage,connectivity";
		super.outputs="labelMap";
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException {
		xdim=inputImage.xdim;
		ydim=inputImage.ydim;
		zdim=inputImage.zdim;
		pSet = new UnionFindHelper(xdim,ydim,zdim);
		labelMap=new IntegerImage(xdim,ydim,zdim,1,1);
		pList = new ArrayList<Point3D>(xdim*ydim*zdim);
		for(int z=0;z<zdim;z++)
			for(int y=0;y<ydim;y++)
				for(int x=0;x<xdim;x++)
				{
					Point3D p= new Point3D(x,y,z);
					pList.add(p);
					pSet.MakeSet(p);
				}
		
		for(Point3D p:pList)
		{
			
			Point3D compp = pSet.find(p);
			connectivity.setCurrentPoint(p.x, p.y, p.z);
			//System.out.println(p + "   " + compp);
			for(Point3D q: connectivity)
			{
				/*System.out.println(p + "   " +q);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				Point3D compq = pSet.find(q);
				if(compp != compq)
					compp = pSet.link(compq, compp);
			}
		}
		
		
		for (Point3D p:pList)
		{
			Point3D r=pSet.find(p);
			
			labelMap.setPixelXYZInt(p.x, p.y, p.z, r.z*xdim*ydim + r.y*xdim +r.x);
		}
		
	}

	public static IntegerImage exec(Image inputImage, Connectivity3D connectivity)
	{
		return (IntegerImage)(new ConnectedComponentMap()).process(inputImage,connectivity);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		
		Image im = ImageLoader.exec("samples/binary.png");
		MultiView mView= MViewer.exec(im); 
		
		Connectivity3D con = new FlatConnectivity(im, TrivialConnectivity.getHeightNeighbourhood());
		IntegerImage label = ConnectedComponentMap.exec(im, con);
		mView.add(LabelsToRandomColors.exec(label));

	}

}
