package fr.unistra.pelican.algorithms.morphology.binary.hitormiss;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Equal;
import fr.unistra.pelican.algorithms.logical.OR;
import fr.unistra.pelican.util.morphology.CompositeStructuringElement;

/**
 * This class computes the binary convex hull of the input. Only the 4 standard
 * directions are used and so the result is not relatively refined. The
 * algorithm was taken from Gonzales & Woods Digital Image Processing, 2nd
 * edition, p539
 * 
 * Further improvement may be achieved by employing more directions in the
 * structuring element.
 * 
 * @author aptoula, weber, lefevre
 * 
 */
public class BinaryConvexHull extends Algorithm {
	/**
	 * Image to be processed
	 */
	public Image input;

	/**
	 * resulting image
	 */
	public BooleanImage output;

	
	/**
	 * Constructor
	 * 
	 */
	public BinaryConvexHull() {
		super.inputs = "input";
		super.outputs = "output";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() {
			
			CompositeStructuringElement[] se = new CompositeStructuringElement[8];
			BooleanImage[] seFG=new BooleanImage[8];
			BooleanImage[] seBG=new BooleanImage[8];
			
			int[] values = { 1, -1, -1, 1, 0, -1, 1, -1, -1 };
			int k=0;
			seFG[k]=new BooleanImage(3,3,1,1,1);
			seFG[k].resetCenter();
			seBG[k]=new BooleanImage(3,3,1,1,1);
			seBG[k].resetCenter();
			for (int v=0;v<values.length;v++)
				if (values[v]==1)
					seFG[k].setPixelBoolean(v,true);
				else if (values[v]==-1)
					seBG[k].setPixelBoolean(v,true);


			values = new int[]{ 1, 1, 1, -1, 0, -1, -1, -1, -1 };
			k++;
			seFG[k]=new BooleanImage(3,3,1,1,1);
			seFG[k].resetCenter();
			seBG[k]=new BooleanImage(3,3,1,1,1);
			seBG[k].resetCenter();
			for (int v=0;v<values.length;v++)
				if (values[v]==1)
					seFG[k].setPixelBoolean(v,true);
				else if (values[v]==-1)
					seBG[k].setPixelBoolean(v,true);

			values = new int[]{ -1, -1, 1, -1, 0, 1, -1, -1, 1 };
			k++;
			seFG[k]=new BooleanImage(3,3,1,1,1);
			seFG[k].resetCenter();
			seBG[k]=new BooleanImage(3,3,1,1,1);
			seBG[k].resetCenter();
			for (int v=0;v<values.length;v++)
				if (values[v]==1)
					seFG[k].setPixelBoolean(v,true);
				else if (values[v]==-1)
					seBG[k].setPixelBoolean(v,true);

			values = new int[]{ -1, -1, -1, -1, 0, -1, 1, 1, 1 };
			k++;
			seFG[k]=new BooleanImage(3,3,1,1,1);
			seFG[k].resetCenter();
			seBG[k]=new BooleanImage(3,3,1,1,1);
			seBG[k].resetCenter();
			for (int v=0;v<values.length;v++)
				if (values[v]==1)
					seFG[k].setPixelBoolean(v,true);
				else if (values[v]==-1)
					seBG[k].setPixelBoolean(v,true);			
			
			BooleanImage[] results = new BooleanImage[4];

			for (int i = 0; i < 4; i++) {
				Image tmp = input;
				BooleanImage tmp2 = null;

				do {
					tmp2 = BinaryHitOrMiss.exec(tmp, seFG[i],seBG[i]);
					tmp2 = (BooleanImage) OR.exec(tmp2, tmp);

					if (Equal.exec(tmp2, tmp) == true)
						break;

					tmp = tmp2;
				} while (true);

				results[i] = tmp2;
			}

			output = results[0];

			for (int i = 1; i < 4; i++)
				output = (BooleanImage) OR.exec(output, results[i]);

			for (int b = 0; b < input.getBDim(); b++) {
				for (int t = 0; t < input.getTDim(); t++) {
					for (int z = 0; z < input.getZDim(); z++) {

						int sag = 0, sol = input.getXDim() - 1, asagi = 0, ust = input
								.getYDim() - 1;

						for (int y = 0; y < input.getYDim(); y++) {
							for (int x = 0; x < input.getXDim(); x++) {
								boolean p = input.getPixelXYZTBBoolean(x, y, z,
										t, b);
								if (p == true && x > sag)
									sag = x;
								if (p == true && y > asagi)
									asagi = y;
							}
						}

						for (int y = input.getYDim() - 1; y >= 0; y--) {
							for (int x = input.getXDim() - 1; x >= 0; x--) {
								boolean p = input.getPixelXYZTBBoolean(x, y, z,
										t, b);
								if (p == true && x < sol)
									sol = x;
								if (p == true && y < ust)
									ust = y;
							}
						}

						for (int y = 0; y < input.getYDim(); y++) {
							for (int x = 0; x < input.getXDim(); x++) {
								boolean p = output.getPixelXYZTBBoolean(x, y,
										z, t, b);

								if (p == true
										&& (x > sag || x < sol || y > asagi || y < ust))
									output.setPixelXYZTBBoolean(x, y, z, t, b,
											false);
							}
						}

					}
				}
			}

		
	}
	
	/**
	 *  This method computes the binary convex hull of the input
	 * @param input image to be processed
	 * @return convex hull
	 */
	public static BooleanImage exec(Image input)
	{
		return (BooleanImage) new BinaryConvexHull().process(input);
	}
}