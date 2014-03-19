/**
 * 
 */
package fr.unistra.pelican.util.vectorial.ordering;





/**
 * What Erchan has called called vectorial ordering are not really ones : as he said :"TODO change name as VectorialExtremum"
 * but it can be easily adapt to my true VectorialOrdering :)
 * 
 * @author Benjamin Perret
 *
 */
public class ErchanToMyVectorialOrderingAdapter extends VectorialOrdering {

	private fr.unistra.pelican.util.vectorial.orders.VectorialOrdering EVO;
	
	private double [][] tmp= new double[2][];
			
	/**
	 * @param evo
	 */
	public ErchanToMyVectorialOrderingAdapter(fr.unistra.pelican.util.vectorial.orders.VectorialOrdering evo) {
		super();
		EVO = evo;
	}

	/* (non-Javadoc)
	 * @see fr.unistra.pelican.algorithms.experimental.perret.CC.Ordering.VectorialOrdering#compare(double[], double[])
	 */
	@Override
	public int compare(double[] o1, double[] o2) {
		tmp[0]=o1;
		tmp[1]=o2;
		double[] max = EVO.max(tmp);
		double[] min = EVO.min(tmp);
		int res;
		if(equal(min,max))
			res=0;
		else if(equal(o1,min))
			res=-1;
		else res=1;
		return res;
	}
	
	private boolean equal(double [] o1, double [] o2)
	{
		if(o1.length!=o2.length)
			return false;
		for(int i=0;i<o1.length;i++)
			if(o1[i]!=o2[i])
				return false;
		return true;
	}

}
