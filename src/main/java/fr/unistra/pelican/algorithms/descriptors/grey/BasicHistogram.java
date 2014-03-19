package fr.unistra.pelican.algorithms.descriptors.grey;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBToGray;
import fr.unistra.pelican.algorithms.histogram.Histogram;
import fr.unistra.pelican.util.data.HistogramData;

/**
 * Histogram descriptor
 * Only works on grey levels
 * @author jonathan.weber
 *
 */
public class BasicHistogram extends Descriptor {

	/**	First input parameter. */
	public Image input;

	public Image original;
	
	/**	Output parameter. */
	public HistogramData output;

	/**	Constructor */
	public BasicHistogram() { 
		super();
		super.inputs = "input";
		super.outputs = "output";		
	}

	public static HistogramData exec( Image input ) { 
		return ( HistogramData ) new BasicHistogram().process( input );
	}


	@SuppressWarnings("unchecked")
	public void launch() throws AlgorithmException { 
		double[] values;
		if(input.getBDim()==3)
			values = Histogram.exec(RGBToGray.exec(input),true);
		else
			values = Histogram.exec(input,true);
		
		this.output = new HistogramData();
		this.output.setDescriptor( ( Class ) this.getClass() );
		Double[] typedValues = new Double[values.length];
		for(int i=0;i<values.length;i++)
			typedValues[i]=values[i];
		this.output.setValues( typedValues );
	}
}
