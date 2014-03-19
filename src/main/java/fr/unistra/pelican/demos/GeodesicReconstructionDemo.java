package fr.unistra.pelican.demos;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.AbsoluteDifference;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.binary.*;
import fr.unistra.pelican.algorithms.morphology.binary.geodesic.*;
import fr.unistra.pelican.algorithms.morphology.gray.*;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.*;
import fr.unistra.pelican.algorithms.morphology.vectorial.*;
import fr.unistra.pelican.algorithms.morphology.vectorial.geodesic.*;
import fr.unistra.pelican.util.morphology.*;
import fr.unistra.pelican.util.vectorial.orders.*;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * Demonstration of geodesic operators (reconstruction) on binary and grayscale
 * images
 * 
 * @author lefevre
 * 
 */

public class GeodesicReconstructionDemo {
	public static void main(String args[]) {
		String path1 = "samples/blobs.png";
		String path2 = "samples/spot5.png";
		String path3 = "unversioned/nairobi.hdr";
		Image image, marker, open, close, tmp, cond, fast, fast2, dmp, invert;
		long t1, t2;
		BooleanImage se = FlatStructuringElement2D
			.createCircleFlatStructuringElement(7);
		BooleanImage se2 = FlatStructuringElement2D
			.createSquareFlatStructuringElement(3);
		VectorialOrdering vo = new NormBasedOrdering();

		/*
		 * // BINARY CASE t1=System.currentTimeMillis(); image =
		 * ImageLoader.exec(path1); t2=System.currentTimeMillis();
		 * Viewer2D.exec(image, "input "+(t2-t1));
		 * 
		 * t1=System.currentTimeMillis(); marker=BinaryOpening.exec(image,se);
		 * t2=System.currentTimeMillis(); Viewer2D.exec(marker, "marker "+(t2-t1));
		 * 
		 * t1=System.currentTimeMillis();
		 * open=BinaryOpeningByReconstruction.exec(image,se);
		 * t2=System.currentTimeMillis(); Viewer2D.exec(open, "open "+(t2-t1));
		 * 
		 * invert=Inversion.exec(image); t1=System.currentTimeMillis();
		 * close=BinaryClosingByReconstruction.exec(invert,se);
		 * t2=System.currentTimeMillis(); Viewer2D.exec(Inversion.exec(close),
		 * "close "+(t2-t1));
		 * 
		 * t1=System.currentTimeMillis();
		 * cond=BinaryReconstructionByDilation.exec(marker,image,se2);
		 * t2=System.currentTimeMillis(); Viewer2D.exec(cond, "cond "+(t2-t1));
		 * 
		 * t1=System.currentTimeMillis();
		 * fast=FastBinaryReconstructionUsingCC.exec(marker,image,FastGrayReconstruction.CONNEXITY8);
		 * t2=System.currentTimeMillis(); Viewer2D.exec(fast, "fast "+(t2-t1));
		 * 
		 * t1=System.currentTimeMillis();
		 * fast2=FastBinaryReconstruction.exec(marker,image,FastGrayReconstruction.CONNEXITY8);
		 * t2=System.currentTimeMillis(); Viewer2D.exec(fast2, "fast2 "+(t2-t1));
		 */
		// GRAYSCALE CASE
		t1 = System.currentTimeMillis();
		image = ImageLoader.exec(path2);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(image, "input " + (t2 - t1));

		t1 = System.currentTimeMillis();
		marker = GrayOpening.exec(image, se);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(marker, "marker " + (t2 - t1));

		t1 = System.currentTimeMillis();
		open = GrayOpeningByReconstruction.exec(image, se);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(open, "open " + (t2 - t1));

		invert = Inversion.exec(image);
		t1 = System.currentTimeMillis();
		close = GrayClosingByReconstruction.exec(invert, se);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(Inversion.exec(close), "close " + (t2 - t1));

		t1 = System.currentTimeMillis();
		cond = GrayReconstructionByDilation.exec(marker, image, se2);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(cond, "cond " + (t2 - t1));

		t1 = System.currentTimeMillis();
		fast = FastGrayReconstruction.exec(marker, image,
			FastGrayReconstruction.CONNEXITY8);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(fast, "fast " + (t2 - t1));

		t1 = System.currentTimeMillis();
		dmp = GrayDMP.exec(image, 10);// ,false,true,false,true);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(ContrastStretchEachBands.exec(dmp), "dmp " + (t2 - t1));
		Viewer2D.exec(dmp, "dmp " + (t2 - t1));

		t1 = System.currentTimeMillis();
		dmp = GrayDMP.exec(image, 10, null, true, true, true, false);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(ContrastStretchEachBands.exec(dmp), "dmp-nongeod "
			+ (t2 - t1));
		Viewer2D.exec(dmp, "dmp-nongeod " + (t2 - t1));

		// MULTISPECTRAL CASE
		t1 = System.currentTimeMillis();
		image = ImageLoader.exec(path3);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(image, "input " + (t2 - t1));

		t1 = System.currentTimeMillis();
		marker = VectorialErosion.exec(image, se, vo);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(marker, "marker " + (t2 - t1));

		t1 = System.currentTimeMillis();
		cond = VectorialReconstructionByDilation.exec(marker, image, se2, vo);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(cond, "cond " + (t2 - t1));

		t1 = System.currentTimeMillis();
		fast = FastVectorialReconstruction.exec(marker, image, vo,
			FastGrayReconstruction.CONNEXITY8);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(fast, "fast " + (t2 - t1));

		t1 = System.currentTimeMillis();
		open = VectorialOpeningByReconstruction.exec(image, se, vo,
			FastGrayReconstruction.CONNEXITY8);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(open, "open " + (t2 - t1));

		invert = Inversion.exec(image);
		t1 = System.currentTimeMillis();
		close = VectorialClosingByReconstruction.exec(invert, se, vo,
			FastGrayReconstruction.CONNEXITY8);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(Inversion.exec(close), "close " + (t2 - t1));

		t1 = System.currentTimeMillis();
		dmp = VectorialDMP.exec(image, vo, 10);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(ContrastStretchEachBands.exec(dmp), "dmp " + (t2 - t1));

		t1 = System.currentTimeMillis();
		dmp = VectorialDMP.exec(image, vo, 10, true, false, true, true, false);
		t2 = System.currentTimeMillis();
		Viewer2D.exec(ContrastStretchEachBands.exec(dmp), "dmp-nongeod "
			+ (t2 - t1));

	}

}
