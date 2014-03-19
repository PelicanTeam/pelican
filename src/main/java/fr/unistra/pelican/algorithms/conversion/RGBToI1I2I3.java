package fr.unistra.pelican.algorithms.conversion;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;

public class RGBToI1I2I3 extends Algorithm {

	public Image input;
	public Image output;

	public RGBToI1I2I3() {

		super();
		super.inputs = "input";
		super.outputs = "output";
		
	}

	public void launch() throws AlgorithmException {
		int xdim = input.getXDim();
		int ydim = input.getYDim();
		int zdim = input.getZDim();
		int tdim = input.getTDim();
		int bdim = input.getBDim();

		if (bdim != 3)
			throw new AlgorithmException(
					"The input must be a tristumulus RGB image");

		output = new DoubleImage(xdim, ydim, zdim, tdim, bdim);
		this.output.setMask( this.input.getMask() );
		output.setColor(true);

		for (int x = 0; x < xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				for (int z = 0; z < zdim; z++) {
					for (int t = 0; t < tdim; t++) {
						int R = input.getPixelXYZTBByte(x, y, z, t, 0);
						int G = input.getPixelXYZTBByte(x, y, z, t, 1);
						int B = input.getPixelXYZTBByte(x, y, z, t, 2);

						// normalise to [0,1]
						double rN = R * 0.003921;
						double gN = G * 0.003921;
						double bN = B * 0.003921;

						double I1 = (rN + gN + bN) / 3.0;
						double I2 = 0.5 * (rN - bN) + 0.5;
						double I3 = 0.25 * (2 * gN - rN - bN) + 0.5;

						output.setPixelXYZTBDouble(x, y, z, t, 0, I1);
						output.setPixelXYZTBDouble(x, y, z, t, 1, I2);
						output.setPixelXYZTBDouble(x, y, z, t, 2, I3);
					}
				}
			}
		}
	}

	public static Image exec(Image input) {
		return (Image) new RGBToI1I2I3().process(input);
	}
}