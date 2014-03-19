package fr.unistra.pelican.algorithms.arithmetic;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.util.mask.MaskStack;
import fr.unistra.pelican.util.vectorial.orders.BinaryVectorialOrdering;

/**
 * Computes the vectorial minimum of two images. 
 * Attention to the choice of ordering as it MUST BE A BINARY RELATION
 * 
 * Result is of same type as first input.
 * 
 * @author ?, Benjamin Perret
 */
public class VectorialMinimum extends Algorithm {
	/**
	 * First input image
	 */
	public Image input1;

	/**
	 * Second input image
	 */
	public Image input2;

	/**
	 * Vectorial ordering
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering
	 */
	public BinaryVectorialOrdering vo;

	/**
	 * Vectorial infimum of inputs with respect to the VectorialOrdering chosen
	 */
	public Image output;

	/**
	 * Constructor
	 * 
	 */
	public VectorialMinimum() {

		super();
		super.inputs = "input1,input2,vo";
		super.outputs = "output";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		if (!Image.haveSameDimensions(input1, input2))
			throw new AlgorithmException("Images must have same dimensions");
		
		this.output = this.input1.copyImage(false);
		MaskStack mask = new MaskStack( MaskStack.OR );
		mask.push( this.input1.getMask() );
		mask.push( this.input2.getMask() );
		this.output.setMask( mask );

		boolean isHere1, isHere2;
		for ( int x = 0 ; x < input1.getXDim() ; x++ ) 
		for ( int y = 0 ; y < input1.getYDim() ; y++ ) { 

			isHere1 = input1.isPresentXY(x,y);
			isHere2 = input2.isPresentXY(x,y);
			double[] p1 = input1.getVectorPixelXYZTDouble(x, y, 0, 0);
			double[] p2 = input2.getVectorPixelXYZTDouble(x, y, 0, 0);
			if ( isHere1 && !isHere2 ) 
				output.setVectorPixelXYZTDouble(x, y, 0, 0, vo.min(p1, p2));
			else if ( isHere1 && !isHere2 ) output.setVectorPixelXYZTDouble( x,y,0,0, p1 );
			else if ( !isHere1 && isHere2 ) output.setVectorPixelXYZTDouble( x,y,0,0, p2 );
		}
	}
	
	/**
	 * Computes the vectorial minimum of two images. 
	 * Attention to the choice of ordering as it MUST BE A BINARY RELATION
	 *  
	 * @param input1 First input Image
	 * @param input2 Second input Image
	 * @param vo Vectorial Ordering to use
	 * @return Vectorial minimum of inputs
	 */
	public static Image exec(Image input1, Image input2, BinaryVectorialOrdering vo) {
		return (Image)new VectorialMinimum().process(input1, input2,vo);
	}
}
