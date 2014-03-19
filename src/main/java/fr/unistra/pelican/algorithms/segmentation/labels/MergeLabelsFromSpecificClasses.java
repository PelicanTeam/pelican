package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.ArrayList;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;

/**
 *	Sets to {@link #value} pixels of the label image {@link #labels} that belong to the classes 
 *	specified by the list {@link #classes} in the samples image {@link #samples}. By default, 
 *	pixels are set to zero.
 *
 *	@see fr.unistra.pelican.Algorithm
 *	@author Régis Witz
 */
public class MergeLabelsFromSpecificClasses extends Algorithm { 

	  ////////////
	 // INPUTS //
	////////////

	/** Label image.*/
	public Image labels;
	/** Samples wich define the classes. */
	public BooleanImage samples;
	/**	Numbers of the classes wich must be set to {@link #value}. 
	 *	These are strictly positive integers.
	 */
	public ArrayList<Integer> classes;

	  /////////////
	 // OPTIONS //
	/////////////

	/**	The value to wich pixels belonging to any class of {@link #classes} must be set. 
	 *	By default, this field equals to 0.
	 */
	public int value = 0;

	  /////////////
	 // OUTPUTS //
	/////////////

	public Image output;



	  /////////////////
	 // CONSTRUCTOR //
	/////////////////

	public MergeLabelsFromSpecificClasses() {

		super();
		super.inputs = "labels,samples,classes";
		super.options = "overwrite,value";
		super.outputs = "output";
	}



	  ///////////////////
	 // LAUNCH METHOD //
	///////////////////

	/**	Launches this algorithm.
	 *	@see fr.unistra.pelican.Algorithm#launch()
	 */
	@Override
	public void launch() throws AlgorithmException { 

		if ( 	this.samples.getXDim() != this.labels.getXDim()
			 ||	this.samples.getYDim() != this.labels.getYDim()
			 ||	this.samples.getZDim() != this.labels.getZDim()
			 ||	this.samples.getTDim() != this.labels.getTDim() )

			throw new AlgorithmException( this.getClass().getName() + "can't be performed with " +
					"these inputs. Be sure that X,Y,Z and T dimensions of label image are equal " +
					"to X,Y,Z and T dimensions of samples image." );



		this.output = this.labels.copyImage( true );
		ArrayList<Integer> deadpx = new ArrayList<Integer>();

		int pxval;
		boolean dead;
 

		for ( int x = 0 ; x < this.samples.getXDim() ; x++ )
		for ( int y = 0 ; y < this.samples.getYDim() ; y++ ) 
		for ( int z = 0 ; z < this.samples.getZDim() ; z++ )
		for ( int t = 0 ; t < this.samples.getTDim() ; t++ )
		for ( int b = 0 ; b < this.samples.getBDim() ; b++ ) 

			if ( this.classes.contains( b+1 ) ) { 

				dead = this.samples.getPixelXYZTBBoolean( x,y,z,t,b );
				if ( dead ) { 

					pxval = this.labels.getPixelXYZTInt( x,y,z,t );
					if ( !deadpx.contains( pxval ) ) deadpx.add( pxval );
				}
			}

		for ( int x = 0 ; x < this.labels.getXDim() ; x++ )
		for ( int y = 0 ; y < this.labels.getYDim() ; y++ )
		for ( int z = 0 ; z < this.labels.getZDim() ; z++ )
		for ( int t = 0 ; t < this.labels.getTDim() ; t++ ) { 

			pxval = this.labels.getPixelXYZTInt( x,y,z,t );
			if ( deadpx.contains( pxval ) )
				this.output.setPixelXYZTInt( x,y,z,t, this.value );
		}

	}

	  //////////////////
	 // EXEC METHODS //
	//////////////////

	/**	Gets a copy of <tt>labels</tt> where pixels belonging to the classes specified by 
	 *	<tt>classes</tt> in <tt>samples</tt> are set to zero. 
	 * 
	 *	@param labels Label image.
	 *	@param samples Samples wich define the classes.
	 *	@param classes Classes wich must be set to <tt>value</tt>. 
	 *				   These are strictly positive integers.
	 *
	 *	@return A rewrited label image.
	 */
	public static Image exec( Image labels, 
							  BooleanImage samples,
							  ArrayList<Integer> classes ) { 

		return ( Image ) new MergeLabelsFromSpecificClasses().process( labels, samples, classes );
	}

	/**	Gets a copy of <tt>labels</tt> where pixels belonging to the classes specified by 
	 *	<tt>classes</tt> in <tt>samples</tt> are set to <tt>value</tt>. 
	 * 
	 *	@param labels Label image.
	 *	@param samples Samples wich define the classes.
	 *	@param classes Classes wich must be set to <tt>value</tt>.
	 *				   These are strictly positive integers.
	 *	@param value Value to wich pixels belonging to any class of <tt>classes</tt> must be set. 
	 *
	 *	@return A rewrited label image.
	 */
	public static Image exec( Image labels, 
							  BooleanImage samples,
							  ArrayList<Integer> classes,
							  int value ) { 

		return ( Image ) new MergeLabelsFromSpecificClasses().process( labels, 
																	   samples, 
																	   classes, 
																	   value );
	}

	/**	Gets a copy of <tt>labels</tt> where pixels belonging to the classes specified by 
	 *	<tt>classes</tt> in <tt>samples</tt> are set to <tt>value</tt> if 
	 *	<tt>changeOtherClassesThanThese</tt> equals <tt>false</tt>. 
	 *	<p>
	 *	Gets a copy of <tt>labels</tt> where pixels <i>not</i> belonging to the classes specified 
	 *	by <tt>classes</tt> in <tt>samples</tt> are set to <tt>value</tt> if 
	 *	<tt>changeOtherClassesThanThese</tt> equals <tt>true</tt>. 
	 * 
	 *	@param labels Label image.
	 *	@param samples Samples wich define the classes.
	 *	@param classes Classes wich must be set to <tt>value</tt>.
	 *				   These are strictly positive integers.
	 *	@param value Value to wich pixels belonging to any class of <tt>classes</tt> must be set. 
	 *	@param changeOtherClassesThanThese See method description.
	 *
	 *	@return A rewrited label image.
	 */
	public static Image exec( Image labels, 
							  BooleanImage samples,
							  ArrayList<Integer> classes,
							  int value, 
							  boolean changeOtherClassesThanThese ) {  

		ArrayList<Integer> relevantClasses;
		if ( changeOtherClassesThanThese ) { 

			relevantClasses = new ArrayList<Integer>();
			for ( int b = 0 ; b < samples.getBDim() ; b++ )  
				if ( !classes.contains( b+1 ) ) 
					relevantClasses.add( b+1 );
			
		} else relevantClasses = classes;
		return ( Image ) new MergeLabelsFromSpecificClasses().process( 
				labels, samples, relevantClasses, value );
	}

	/**	Gets a copy of <tt>labels</tt> where pixels belonging to the classes specified by 
	 *	<tt>classes</tt> in <tt>samples</tt> are set to 0 if <tt>changeOtherClassesThanThese</tt> 
	 *	equals <tt>false</tt>. 
	 *	<p>
	 *	Gets a copy of <tt>labels</tt> where pixels <i>not</i> belonging to the classes specified 
	 *	by <tt>classes</tt> in <tt>samples</tt> are set to 0 if <tt>changeOtherClassesThanThese</tt>
	 *	 equals <tt>true</tt>. 
	 * 
	 *	@param labels Label image.
	 *	@param samples Samples wich define the classes.
	 *	@param classes Classes wich must be set to <tt>value</tt>.
	 *				   These are strictly positive integers.
	 *	@param changeOtherClassesThanThese See method description.
	 *
	 *	@return A rewrited label image.
	 */
	public static Image exec( Image labels, 
							  BooleanImage samples,
							  ArrayList<Integer> classes,
							  boolean changeOtherClassesThanThese ) {  

		return ( Image ) new MergeLabelsFromSpecificClasses().process( 
				labels, samples, classes, 0, changeOtherClassesThanThese );
	}


}
