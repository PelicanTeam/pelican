package fr.unistra.pelican.algorithms.detection;

import java.util.ArrayList;
import java.util.Collections;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayInternGradient;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Keypoint;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.Tools;
import fr.unistra.pelican.util.data.DoubleArrayData;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 *	The classical Harris interest point detector
 *
 *	@author Erchan Aptoula
 *	@author Régis Witz ( recovery & customization )
 */
public class Harris extends Algorithm {

	/**	Input (grayscale) image. */
	public Image input;
	/**	Output image. */
	public ArrayList<Keypoint> output;

	/**	Tunable sensitivity parameter. */
	public double alpha = 0.06; // \in [0.04,0.06]
	/**	Number of points to keep. */
	public int nbpoints = 50;



	public Harris() {
		super.inputs = "input";
		super.options = "nbpoints,alpha";
		super.outputs = "output";

	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> exec( Image input ) { 
		return ( ArrayList<Keypoint> ) new Harris().process( input );
	}
	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> exec( Image input, int nbpoints ) { 
		return ( ArrayList<Keypoint> ) new Harris().process( input, nbpoints );
	}
	@SuppressWarnings("unchecked")
	public static ArrayList<Keypoint> exec( Image input, int nbpoints, double alpha ) { 
		return ( ArrayList<Keypoint> ) new Harris().process( input, nbpoints, alpha );
	}

	/**	@see fr.unistra.pelican.Algorithm#launch() */
	public void launch() throws AlgorithmException { 

		// horizontal differential
		BooleanImage seH = FlatStructuringElement2D.createHorizontalLineFlatStructuringElement(3);
		Image inputX = (Image) new GrayInternGradient().process( this.input, seH );

		// vertical differential
		BooleanImage seV = FlatStructuringElement2D.createVerticalLineFlatStructuringElement(3);
		Image inputY = (Image) new GrayInternGradient().process( this.input, seV );

		// 8-neighborhood
		BooleanImage se = FlatStructuringElement2D.createSquareFlatStructuringElement(3);
		Point4D[] points = se.foreground();

		ArrayList<Keypoint> tmp = new ArrayList<Keypoint>();
		double p;
		for ( int x = 0 ; x < input.getXDim() ; x++ ) { 
			for ( int y = 0; y < input.getYDim(); y++ ) { 

				// matrix elements
				double diffX2 = 0;
				double diffY2 = 0;
				double diffX = 0;
				double diffY = 0;

				for ( int i = 0 ; i < points.length ; i++ ) { 

					int valX = x - se.getCenter().x + points[i].y;
					int valY = y - se.getCenter().y + points[i].x;

					if ( valX >= 0 && valX < this.input.getXDim() 
					  && valY >= 0 && valY < this.input.getYDim() ) { 

						p = inputX.getPixelXYDouble(valX, valY);
						diffX2 += p * p;
						diffX += p;

						p = inputY.getPixelXYDouble(valX, valY);
						diffY2 += p * p;
						diffY += p;
					}
				}

				double determinant = diffX2*diffY2 - diffX*diffX * diffY*diffY;
				double trace = diffX2 + diffY2;

				double h = determinant - this.alpha * trace*trace;

				Double[] descriptor = new Double[1];
				descriptor[0] = h;

				DoubleArrayData data = new  DoubleArrayData();
//				data.setDescriptor( null );
				data.setValues( descriptor );

				tmp.add( new Keypoint( x,y,data ) );
			}
		}

		Collections.sort( tmp );

		this.output = new ArrayList<Keypoint>();
		for ( int i = 0 ; i < this.nbpoints ; i++ ) this.output.add( tmp.get(i) );
	}


	 public static void main(String[] args) { 

		Image img = ImageLoader.exec("samples/lenna256.png");
		Viewer2D.exec(img, "init");
		img = (Image) AverageChannels.exec(img);
		Viewer2D.exec(img, "AverageChannels");
		ArrayList<Keypoint> res = Harris.exec( img );

		for ( int p = 0 ; p < img.size(); p++ ) img.setPixelBoolean( p,false );
		Double[] values;
		for ( Keypoint key : res ) { 

			values = ( Double[] ) key.data.getValues();
			img.setPixelXYDouble( (int)key.x,(int)key.y,values[0] );
		}
		Viewer2D.exec( img, "sonuc " );

		System.out.println( Tools.imageVolume( img, 0 ) );
	}

}
