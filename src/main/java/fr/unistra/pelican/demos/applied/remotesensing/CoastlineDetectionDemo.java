package fr.unistra.pelican.demos.applied.remotesensing;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.applied.remotesensing.coastline.DoubleThresholdCoastlineDetector;
import fr.unistra.pelican.algorithms.applied.remotesensing.index.IB;
import fr.unistra.pelican.algorithms.applied.remotesensing.index.NDVI;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.conversion.GrayToPseudoColors;
import fr.unistra.pelican.algorithms.detection.MHMTBoundaryDetection;
import fr.unistra.pelican.algorithms.histogram.ContrastStretchEachBands;
import fr.unistra.pelican.algorithms.io.ImageBuilder;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.statistics.PerformanceIndex;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.detection.MHMTDetectionParameters;

public class CoastlineDetectionDemo {

	public void setup() {
		// icisp();
		 maraisVillerville();
		// dragey();
		 //dragey2();
		//octeville();
		// falaiseVillerville();
	}

	String pathJonathan = "C:\\Documents and Settings\\Jonathan.weber\\Mes documents\\Mes images\\Trait de cote\\";
	String respathJonathan = "C:\\Documents and Settings\\Jonathan.weber\\Mes documents\\Mes images\\Trait de cote\\results\\";
	String pathSebastien = "/home/miv/lefevre/data/teledetection/ecosgil/isprs/";
	String respathSebastien = "/home/miv/lefevre/data/teledetection/ecosgil/isprs/results/";

	String path = pathJonathan;
	String respath = respathJonathan;

	String filepath = path;
	String refpath = path;
	ArrayList<MHMTDetectionParameters> mhmtdp = new ArrayList<MHMTDetectionParameters>();
	ArrayList<MHMTDetectionParameters> mhmtdp2 = new ArrayList<MHMTDetectionParameters>();
	boolean displayInput = false;
	boolean displayIntermediaryResults = true;

	public static void main(String[] args) {
		new CoastlineDetectionDemo();
	}

	public CoastlineDetectionDemo() {
		boolean save = true;
		setup();

		//properties(filepath);

		// Ajout des bandes
		Image sat = ImageLoader.exec(filepath);
		System.err.println("Image loaded");
		sat = addBands(sat);
		if (displayInput)
			Viewer2D.exec(sat);
		// Conversion en 8-bits
		sat = new ByteImage(sat);
		// Traitement
		Image mhmt1 = MHMTBoundaryDetection.exec(sat, mhmtdp);
		System.err.println("MHMT 1 done");
		if (displayIntermediaryResults)
			Viewer2D.exec(GrayToPseudoColors.exec(mhmt1));
		Image mhmt2 = MHMTBoundaryDetection.exec(sat, mhmtdp2);
		System.err.println("MHMT 2 done");
		if (displayIntermediaryResults)
			Viewer2D.exec(GrayToPseudoColors.exec(mhmt2));
		int filteringSize = computeBorderWidth(mhmtdp2);
		filteringSize=10;
		Image result = DoubleThresholdCoastlineDetector.exec(mhmt1, mhmt2,
				filteringSize);
		System.err.println("DoubleThresholdCoastlineDetector done");
		if (displayIntermediaryResults)
			Viewer2D.exec(result);
		// Sauvegarde
		ImageSave.exec(result, respath + "result.png");
		// Enregistrement des parametres
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(respath + "params.txt"));
		} catch (FileNotFoundException e) {
			System.err.println("probleme avec le chemin " + respath);
		}
		out.println("Parametres marqueur");
		for (int p = 0; p < mhmtdp.size(); p++)
			out.println(mhmtdp.get(p));
		out.println("\nParametres masque");
		for (int p = 0; p < mhmtdp2.size(); p++)
			out.println(mhmtdp2.get(p));
		// Evaluation
		Image reference = ImageLoader.exec(refpath);
		System.err.println("Launch Evaluation");
		out.println("\nEvaluation");
		out.println("MIN="
				+ PerformanceIndex
						.exec(reference, result, PerformanceIndex.MIN)
				+ " / "
				+ PerformanceIndex.exec(reference, result,
						PerformanceIndex.MIN, true));
		System.err.println("MIN DONE");
		out.println("MAX="
				+ PerformanceIndex
						.exec(reference, result, PerformanceIndex.MAX)
				+ " / "
				+ PerformanceIndex.exec(reference, result,
						PerformanceIndex.MAX, true));
		System.err.println("MAX DONE");
		out.println("FRECHET="
				+ PerformanceIndex.exec(reference, result,
						PerformanceIndex.FRECHET));
		System.err.println("FRECHET DONE");
		out.println("SKL="
				+ PerformanceIndex.exec(reference, result,
						PerformanceIndex.SKEL)
				+ " / "
				+ PerformanceIndex.exec(reference, result,
						PerformanceIndex.SKEL, true));
		System.err.println("SKEL DONE");
		out.println("DIST="
				+ PerformanceIndex.exec(reference, result,
						PerformanceIndex.DIST)
				+ " / "
				+ PerformanceIndex.exec(reference, result,
						PerformanceIndex.DIST, true));
		System.err.println("DIST DONE");
		out.println("FPC="
				+ PerformanceIndex
						.exec(reference, result, PerformanceIndex.FPC));
		System.err.println("FPC DONE");
		out.println("FPP="
				+ PerformanceIndex
						.exec(reference, result, PerformanceIndex.FPP));
		System.err.println("FPP DONE");
		out.close();
		if (save) {
			for (int b = 0; b < sat.getBDim(); b++)
				ImageSave.exec(sat.getImage4D(b, Image.B), respath + "band-"
						+ b + ".png");
			ImageSave.exec(ContrastStretchEachBands
					.exec(ColorImageFromMultiBandImage.exec(sat, 2, 1, 0)),
					respath + "color1.png");
			ImageSave.exec(ContrastStretchEachBands
					.exec(ColorImageFromMultiBandImage.exec(sat, 3, 2, 1)),
					respath + "color2.png");
			ImageSave.exec(mhmt1, respath + "mhmt1.png");
			ImageSave.exec(mhmt2, respath + "mhmt2.png");
			ImageSave.exec(mhmt1, respath + "mhmt1.png");
			ImageSave.exec(mhmt2, respath + "mhmt2.png");
			ImageSave.exec(ManualThresholding.exec(mhmt1, 1), respath
					+ "bin1.png");
			ImageSave.exec(ManualThresholding.exec(mhmt2, 1), respath
					+ "bin2.png");
			ImageSave.exec(reference, respath + "reference.png");
		}

	}

	public void maraisVillerville() {
		refpath = path + "ref_QB_villerville_marais.TIF";
		filepath = path + "extrait1_VillervilleQB2_marais.hdr";
		// filepath = path + "coast.pelican";
		// respath = respath + "coast_seg";
		respath = respath + "marais/marais-";
		// Vegetation > 150 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 150.0 / 255, true, -1, -5));
		mhmtdp2.add(new MHMTDetectionParameters(4, 145.0 / 255, true, -1, -5));

		// Plage et Mer et <150 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 150.0 / 255, false, 1, 50));
		mhmtdp2.add(new MHMTDetectionParameters(4, 155.0 / 255, false, 1, 50));

		// Contraintes fortes

		// Plage >120 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 120.0 / 255, true, 1, 5));
		// Mer < 20 dans le PIR
		mhmtdp.add(new MHMTDetectionParameters(3, 20.0 / 255, false, 300, 100));
		// Ter+Mer > 15 dans le Rouge
		mhmtdp.add(new MHMTDetectionParameters(2, 20.0 / 255, true, 1, 10));

	}

	public void falaiseVillerville() {
		refpath = path + "ref_QB_villerville_falaise.TIF";
		filepath = path + "extrait2_VillervilleQB_falaise.hdr";
		respath = respath + "falaise/falaise-";
		// Vegetation > 130 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 130.0 / 255, true, -1, -1));
		mhmtdp2.add(new MHMTDetectionParameters(4, 125.0 / 255, true, -1, -1));
		// Plage et Mer et <130 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 130.0 / 255, false, 1, 50));
		mhmtdp2.add(new MHMTDetectionParameters(4, 130.0 / 255, false, 1, 50));

	}

	public void dragey() {
		refpath = path + "ref_qb_dragey_extrait1.TIF";
		filepath = path + "QB_dragey_extrait1.hdr";
		respath = respath + "dragey/dragey-";
		// Vegetation > 150 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 150.0 / 255, true, -1, -5));
		mhmtdp2.add(new MHMTDetectionParameters(4, 145.0 / 255, true, -1, -5));
		// Elimination des dunes immergeables, par le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 150.0 / 255, true, -1, -100));
		mhmtdp2
				.add(new MHMTDetectionParameters(4, 145.0 / 255, true, -1, -100));
		// Plage <155 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 150.0 / 255, false, 1, 5));
		mhmtdp2.add(new MHMTDetectionParameters(4, 155.0 / 255, false, 1, 5));
		// Contraintes fortes
		// Mer et plage > 40 dans le PIR
		mhmtdp.add(new MHMTDetectionParameters(3, 40.0 / 255, true, 1, 50));
	}

	public void dragey2() {
		refpath = path + "ref2_qb_dragey_extrait1.TIF";
		filepath = path + "QB_dragey_extrait1.hdr";
		respath = respath + "dragey2/dragey2-";
		// Vegetation > 150 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 150.0 / 255, true, -1, -5));
		mhmtdp2.add(new MHMTDetectionParameters(4, 145.0 / 255, true, -1, -5));
		// Plage <155 dans le NDVI
		mhmtdp.add(new MHMTDetectionParameters(4, 150.0 / 255, false, 1, 50));
		mhmtdp2.add(new MHMTDetectionParameters(4, 150.0 / 255, false, 1, 50));
		// Contraintes fortes
		// Mer et plage > 40 dans le PIR
		mhmtdp.add(new MHMTDetectionParameters(3, 40.0 / 255, true, 1, 50));
	}

	public void octeville() {
		refpath = path + "refex2_octeville.TIF";
		filepath = path + "QB-MS_Octeville_extrait2.hdr";
		respath = respath + "octeville/octeville-";

		// Vegetation > 15 dans le IB
		mhmtdp.add(new MHMTDetectionParameters(5, 15.0 / 255, true, -1, -80));
		mhmtdp2.add(new MHMTDetectionParameters(5, 15.0 / 255, true, -1, -10));

		// Falaise <15 dans le IB
		mhmtdp.add(new MHMTDetectionParameters(5, 15.0 / 255, false, 1, 80));
		mhmtdp2.add(new MHMTDetectionParameters(5, 15.0 / 255, false, 1, 10));

	}

	/*
	 * Image mhmt1 = ImageLoader.exec(respath + "MHMT1ndg.png"); BooleanImage
	 * thr1 = ManualThresholding.exec(mhmt1, 1); ImageSave.exec(thr1, respath +
	 * "MHMT1nb.png"); Image mhmt2 = ImageLoader.exec(respath + "MHMT2ndg.png");
	 * BooleanImage thr2 = ManualThresholding.exec(mhmt2, 1);
	 * thr2=BinaryClosing.exec(thr2, FlatStructuringElement
	 * .createCircleFlatStructuringElement(3)); ImageSave.exec(thr2, respath +
	 * "MHMT2nb.png"); BooleanImage mask = (BooleanImage)
	 * FastBinaryReconstruction.exec(thr1, thr2); mask = (BooleanImage)
	 * BinaryFillHole.exec(mask); ImageSave.exec(mask, respath + "masknb.png");
	 * l1 = System.currentTimeMillis();
	 * mhmt2=AdditionConstantChecked.exec(mhmt2,1/255.); Image relief =
	 * Minimum.exec(mhmt2, mask); mask = BinaryDilation.exec(mask,
	 * FlatStructuringElement .createCircleFlatStructuringElement(5)); Image
	 * labels = MarkerBasedWatershed.exec(relief, mask); Image frontiers =
	 * DrawFrontiersFromElevation.exec(labels, relief); l2 =
	 * System.currentTimeMillis(); System.out.println("WT1 processed in " + ((l2
	 * - l1) / 1000) + "s"); ImageSave.exec(relief, respath + "maskndg.png");
	 * ImageSave.exec(frontiers, respath + "coastline.png"); l1 =
	 * System.currentTimeMillis(); Image skel = BinaryHST.exec(frontiers, 5); l2
	 * = System.currentTimeMillis(); System.out.println("WT2 processed in " +
	 * ((l2 - l1) / 1000) + "s"); ImageSave.exec(skel, respath +
	 * "coastline2.png");
	 */

	/*
	 * ImageSave.exec(DrawFrontiers.exec(Watershed.exec(mask),true), respath +
	 * "test1.png");
	 * ImageSave.exec(LabelsToRandomColors.exec(Watershed.exec(mask)), respath +
	 * "test1b.png");
	 * ImageSave.exec(DrawFrontiers.exec(Watershed.exec(Minimum.exec
	 * (mhmt2,mask)),true), respath + "test2.png");
	 * ImageSave.exec(LabelsToRandomColors
	 * .exec(Watershed.exec(Minimum.exec(mhmt2,mask))), respath + "test2b.png");
	 * if(true)return; // Obtention du trait par squelettisation Image skel =
	 * BinaryHST.exec(mask, 100); ImageSave.exec(skel, respath +
	 * "connected.png"); System.out.println("skel1 ok"); skel =
	 * BinaryConditionalHST.exec(mask, thr1,0); ImageSave.exec(skel, respath +
	 * "connected2.png"); skel = BinaryHST.exec(skel,100); ImageSave.exec(skel,
	 * respath + "connected3.png");
	 */

	/*
	 * FlatStructuringElement
	 * se=FlatStructuringElement.createCrossFlatStructuringElement(1); ByteImage
	 * test1=new ByteImage(skel,false); for (int p=0;p<test1.size();p++)
	 * test1.setPixelByte(p,(mhmt1.getPixelByte(p)+mhmt2.getPixelByte(p))/2);
	 * skel = GraySkeleton.exec(test1, se); ImageSave.exec(skel, respath +
	 * "test1.png"); ByteImage test2=new ByteImage(skel,false); for (int
	 * p=0;p<test2.size();p++)
	 * test2.setPixelByte(p,(thr1.getPixelByte(p)+thr2.getPixelByte(p))/2); skel
	 * = GraySkeleton.exec(test2, se); ImageSave.exec(skel, respath +
	 * "test2.png"); ByteImage test3=new ByteImage(skel,false); for (int
	 * p=0;p<test3.size();p++)
	 * test3.setPixelByte(p,(thr1.getPixelByte(p)+mask.getPixelByte(p))/2); skel
	 * = GraySkeleton.exec(test3, se); ImageSave.exec(skel, respath +
	 * "test3.png");
	 */

	/*
	 * public static void icisp() {
	 * 
	 * String respath = "/home/miv/lefevre/data/teledetection/ecosgil/thrs/";
	 * String filepath = respath + "VillervilleQB2_4.hdr"; // si les resultats
	 * sont precalcules boolean process = true;
	 * 
	 * Image sat = ImageLoader.exec(filepath); System.err.println("Image
	 * loaded"); //
	 * ImageSave.exec(ContrastStretchEachBands.exec(ColorImageFromMultiBandImage
	 * .exec(sat,2,1,0)),respath+"input.png");
	 * 
	 * sat = addBands(sat); // for(int b=0;b<sat.getBDim();b++) //
	 * ImageSave.exec(sat.getImage4D(b,Image.B),respath+"band-"+b+".png");
	 * 
	 * if (process) {
	 * 
	 * double seuil = 128;
	 * 
	 * Vector<MHMTDetectionParameters> mhmtdp = new
	 * Vector<MHMTDetectionParameters>(); mhmtdp.add(new
	 * MHMTDetectionParameters(4, seuil / 255., false, 1, 75)); mhmtdp.add(new
	 * MHMTDetectionParameters(4, seuil / 255., true, -1, -75)); mhmtdp.add(new
	 * MHMTDetectionParameters(2, seuil * 0.1 / 255., true, 5, 150));
	 * 
	 * ImageSave.exec(MHMTBoundaryDetection.exec(sat, mhmtdp), respath +
	 * "MHMT1.png"); System.err.println("MHMT1 processed"); // Tentative avec
	 * double seuillage, MHMT plus souple double alpha = 10 / 100.0;
	 * 
	 * Vector<MHMTDetectionParameters> mhmtdp2 = new
	 * Vector<MHMTDetectionParameters>(); mhmtdp2.add(new
	 * MHMTDetectionParameters(4, seuil * (1 + alpha) / 255., false, 1, 75));
	 * mhmtdp2.add(new MHMTDetectionParameters(4, seuil * (1 - alpha) / 255.,
	 * true, -1, -75));
	 * 
	 * mhmtdp2.add(new MHMTDetectionParameters(2, seuil * 0.1 (1 - alpha) /
	 * 255., true, 5, 150));
	 * 
	 * ImageSave.exec(MHMTBoundaryDetection.exec(sat, mhmtdp2), respath +
	 * "MHMT2.png"); System.err.println("MHMT2 processed"); }
	 * 
	 * Image mhmt1 = ImageLoader.exec(respath + "MHMT1.png"); BooleanImage thr1
	 * = ManualThresholding.exec(mhmt1, 1); ImageSave.exec(thr1, respath +
	 * "initial.png");
	 * 
	 * Image mhmt2 = ImageLoader.exec(respath + "MHMT2.png"); BooleanImage thr2
	 * = ManualThresholding.exec(mhmt2, 1); thr2 = BinaryClosing.exec(thr2,
	 * FlatStructuringElement .createCircleFlatStructuringElement(5));
	 * 
	 * BooleanImage mask = (BooleanImage) FastBinaryReconstruction.exec(thr1,
	 * thr2); Image skel = BinaryHST.exec(mask, 10); ImageSave.exec(skel,
	 * respath + "connected.png"); Viewer2D.exec(skel, "resultat ameliore");
	 * 
	 * 
	 * }
	 */

	public static int computeBorderWidth(
			ArrayList<MHMTDetectionParameters> params) {
		int size = 0;
		for (MHMTDetectionParameters p : params)
			if ((Math.abs(p.getSegmentShift() + p.getSegmentLength()) > size))
				size = Math.abs(p.getSegmentShift() + p.getSegmentLength());
		return size;
	}

	public static Image addBands(Image sat) {
		Image ndvi = NDVI.exec(sat, 2, 3);
		Image ib = IB.exec(sat, 2, 3);
		Image sat2 = sat.newInstance(sat.getXDim(), sat.getYDim(), 1, 1, sat
				.getBDim() + 2);
		sat2.setImage4D(sat.getImage4D(0, Image.B), 0, Image.B);
		sat2.setImage4D(sat.getImage4D(1, Image.B), 1, Image.B);
		sat2.setImage4D(sat.getImage4D(2, Image.B), 2, Image.B);
		sat2.setImage4D(sat.getImage4D(3, Image.B), 3, Image.B);
		sat2.setImage4D(ndvi.getImage4D(0, Image.B), 4, Image.B);
		sat2.setImage4D(ib.getImage4D(0, Image.B), 5, Image.B);
		return sat2;

	}

	public static void properties(String filepath) {
		Image source = ImageLoader.exec(filepath);
		source = addBands(source);
		Image bg = ContrastStretchEachBands.exec(ColorImageFromMultiBandImage
				.exec(source, 2, 1, 0));

		if(!(bg instanceof ByteImage))
				bg=new ByteImage(bg);
		ArrayList<Object> params = new ImageBuilder().processAll(bg,
				"select a zone to get its properties");
		Image markers = (Image) params.get(0);
		// Viewer2D.exec(markers);
		int labels = 1 + (Integer) params.get(1);
		int bands = source.getBDim();

		// calcul des statistiques : min,max,average,std dans chaque bande
		int min[][] = new int[labels][bands];
		int max[][] = new int[labels][bands];
		for (int l = 0; l < labels; l++)
			Arrays.fill(min[l], 255);
		double avg[][] = new double[labels][bands];
		double std[][] = new double[labels][bands];
		int sum[][] = new int[labels][bands];
		int sum2[][] = new int[labels][bands];
		int count[] = new int[labels];
		for (int x = 0; x < markers.getXDim(); x++)
			for (int y = 0; y < markers.getYDim(); y++) {
				int label = markers.getPixelXYByte(x, y);
				count[label]++;
				for (int b = 0; b < bands; b++) {
					int val = source.getPixelXYBByte(x, y, b);
					sum[label][b] += val;
					sum2[label][b] += val * val;
					if (val < min[label][b])
						min[label][b] = val;
					if (val > max[label][b])
						max[label][b] = val;
				}
			}
		for (int b = 0; b < bands; b++)
			for (int l = 0; l < labels; l++) {
				avg[l][b] = ((double) sum[l][b]) / count[l];
				std[l][b] = sum2[l][b] + count[l] * avg[l][b] * avg[l][b] - 2
						* avg[l][b] * sum[l][b];
			}
		for (int l = 0; l < labels; l++) {
			System.out.println("label: " + l + "\t #=" + count[l]);
			for (int b = 0; b < bands; b++) {
				System.out.print("  band: " + b);
				System.out.print("\t min=" + min[l][b]);
				System.out.print("\t max=" + max[l][b]);
				System.out.print("\t avg=" + ((int) (100 * avg[l][b])) / 100.0);
				System.out.print("\t std=" + ((int) (std[l][b])) / 100.0);
				System.out.println();
			}
			System.out.println();
		}

	}
}
