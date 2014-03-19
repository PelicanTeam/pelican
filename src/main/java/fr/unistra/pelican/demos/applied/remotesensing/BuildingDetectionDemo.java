package fr.unistra.pelican.demos.applied.remotesensing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.applied.remotesensing.building.OriginalBinaryBuildingDetection;
import fr.unistra.pelican.algorithms.arithmetic.Maximum;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.logical.AND;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryGradient;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.statistics.DetectionQualityEvaluation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Demo class for MM-based building detection
 * 
 * @author lefevre,weber
 * 
 */
public class BuildingDetectionDemo {

	// Attributs
	boolean view = false;
	boolean debug = true;
	boolean save = true;
	boolean computeDuration = true;
	String dataset = "";
	String bMode = "";
	String fMode = "";
	String experiment = "";
	String duration = "";
	PrintStream out = System.out;
	int repeat = -1;
	int min = -1;
	int max = 0;
	int xmin = -1;
	int xmax = -1;
	int ymin = -1;
	int ymax = -1;
	int binMode = OriginalBinaryBuildingDetection.BINARISATION_CLUSTER;
	double binThr = 0.8;
	int binParam = 1;
	int filterMode = OriginalBinaryBuildingDetection.FILTER_GRANULOMETRY;
	boolean globalTTR = true;
	double minRatio = 0;
	int granStep = 2;
	int occo = 5;
	String[] command = null;
	double angle = 15.;

	// Default path
	String path = "/home/lefevre/data/teledetection/bati/";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws PelicanException {

		// Possible use of hard-coded filename
		/*
		 * String file="pescara_input.bmp";String reference="pescara_ref.bmp";
		 * String file="bishkek_input.bmp";String reference="bishkek_ref.bmp";
		 * String file="strasbourg_input.bmp";String reference="strasbourg_ref.bmp";
		 * String file="pesc_input.bmp";String reference="pesc_ref.bmp"; String
		 * file="pescs_input.bmp";String reference="pescs_ref.bmp"; String
		 * file="qbs1_input.png";String reference="qbs1_ref.png"; String
		 * file="qbs2_input.png";String reference="qbs2_ref.png"; String
		 * file="ortho_input.png";String reference="ortho_ref.png";
		 */

		BuildingDetectionDemo demo = new BuildingDetectionDemo();
		demo.command = args.clone();

		// Example of hard-coded use of evaluation mode
		// demo.evaluateResult("/home/lefevre/data/global/teledetection/bati/qbs1_ref.png","/home/lefevre/publis/work/rig/tests/resultats/results-25nov2006/qbs1-manual/0-smooth.png","qbs1-smooth-stats.txt");

		// Affichage des paramï¿œtres
		if (args.length == 0) {
			afficheSimple();
			return;
		}

		if (args[0].equalsIgnoreCase("help")) {
			afficheComplet();
			return;
		}

		// Mode evaluation
		else if (args[0].equalsIgnoreCase("evaluate")) {
			System.out.println("Evaluation Mode");
			switch (args.length) {
			case 3:
				demo.evaluateResult(args[1], args[2]);
				break;
			case 4:
				demo.evaluateResult(args[1], args[2], args[3], false);
				break;
			case 5:
				demo.evaluateResult(args[1], args[2], args[3], args[4]
					.equalsIgnoreCase("append"));
				break;
			default:
				System.out.println("evaluate needs between 2 and 4 parameters");
			}
			return;
		}

		// Mode detection
		else if (args[0].equalsIgnoreCase("detect")) {
			System.out.println("Detection Mode");
			try {
				demo.configure(args);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			// Benchmark
			demo.bench();
		}

	}

	private void bench() {
		// Dï¿œmarrage
		long t1 = System.currentTimeMillis();
		System.out.println(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "h "
			+ Calendar.getInstance().get(Calendar.MINUTE) + "m "
			+ Calendar.getInstance().get(Calendar.SECOND) + "s");
		// Chargement
		String file = dataset + "_input.png";
		String reference = dataset + "_ref.png";
		Image inputImage = ImageLoader.exec(path + file);
		if (view)
			Viewer2D.exec(inputImage, "input");
		// Calcul du nombre d'itï¿œrations
		int iterations = Math.abs(repeat);
		for (int d = 1; d < iterations + 1; d++) {
			// Paramï¿œtres
			experiment = dataset + "-" + bMode + "-" + fMode;
			if (iterations != 1)
				experiment = experiment + "-" + d + "0";
			experiment = experiment + "/";
			File f = new File(experiment);
			f.mkdir();
			if (repeat != -1 || binThr == -1)
				binThr = (double) (d) / (iterations + 1);
			if (save)
				ImageSave.exec(inputImage, experiment + "input.png");
			// Dï¿œtection
			// Image outputImage =
			// BinaryBuildingDetection.exec(inputImage,xmin,ymin,xmax,ymax);
			Image outputImage = (Image) new OriginalBinaryBuildingDetection().process(
				inputImage, xmin, ymin, xmax, ymax, view, save, debug, null, binThr,
				binMode, filterMode, 0, angle, binParam, granStep, globalTTR, minRatio,
				occo, experiment);
			// Image outputImage =
			// BinaryBuildingDetection.exec(inputImage,binMode,binParam,binThr,filterMode,angle,xmin,ymin,xmax,ymax);
			// Sauvegarde des rï¿œsultats
			if (save)
				ImageSave.exec(outputImage, experiment + "mask.png");
			Image contour = BinaryGradient.exec(outputImage, FlatStructuringElement2D
				.createSquareFlatStructuringElement(3));
			Image visu = Maximum.exec(inputImage, contour);
			if (view)
				Viewer2D.exec(visu, "visu2");
			if (save)
				ImageSave.exec(visu, experiment + "result.png");
			// Evaluation
			Image ref = ImageLoader.exec(path + reference);
			if (view && debug)
				Viewer2D.exec(ref, "reference");
			Image croise = AND.exec(outputImage, ref);
			if (view && debug)
				Viewer2D.exec(croise, "croisement");
			Image contour2 = BinaryGradient.exec(croise, FlatStructuringElement2D
				.createSquareFlatStructuringElement(3));
			Image visu2 = Maximum.exec(inputImage, contour2);
			if (view)
				Viewer2D.exec(visu2, "visu2");
			if (save)
				ImageSave.exec(visu2, experiment + "correct.png");
			// Statistiques
			try {
				out = new PrintStream(new FileOutputStream(experiment + "stats.txt"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			writeHeader();
			// Arrï¿œt
			long t2 = System.currentTimeMillis() - t1;
			String duration = "Temps CPU : " + (t2 / 1000) + " secondes";
			if (computeDuration)
				out.println(duration + "\n");
			System.out.println("Dï¿œmo terminï¿œe. " + duration);
			System.out.println(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
				+ "h " + Calendar.getInstance().get(Calendar.MINUTE) + "m "
				+ Calendar.getInstance().get(Calendar.SECOND) + "s");
			out.println(DetectionQualityEvaluation.exec(outputImage, ref, true, true,
				true));
		}

	}

	private void writeHeader() {
		out.print("Date : ");
		out.print(Calendar.getInstance().get(Calendar.DATE) + "/");
		out.print(Calendar.getInstance().get(Calendar.MONTH) + "/");
		out.print(Calendar.getInstance().get(Calendar.YEAR) + " -- ");
		out.print(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":");
		out.print(Calendar.getInstance().get(Calendar.MINUTE) + ":");
		out.print(Calendar.getInstance().get(Calendar.SECOND) + "\n");
		out.print("Options : ");
		for (int i = 0; i < command.length; i++)
			out.print(command[i] + " ");
		out.println();
		out.println("Dataset : " + dataset);
		out.println("Mode : " + bMode + " - " + fMode);
		out.println("binThr : " + binThr + " / " + " Itï¿œrations : " + repeat
			+ " binParam: " + binParam);
		out.println("Tailles : [" + xmin + "x" + ymin + " , " + xmax + "x" + ymax
			+ "]");
		out.println();
	}

	private void evaluateResult(String pathResult, String pathRef) {
		// Options
		out.print("Options : ");
		for (int i = 0; i < command.length; i++)
			out.print(command[i] + " ");
		out.println();
		out.println();
		// Evaluation
		Image imgResult = ManualThresholding
			.exec(ImageLoader.exec(pathResult), 0.5);
		Image imgRef = ManualThresholding.exec(ImageLoader.exec(pathRef), 0.5);
		out.println(DetectionQualityEvaluation.exec(imgResult, imgRef, true, true,
			true));
	}

	private void evaluateResult(String pathResult, String pathRef,
		String pathOutput, boolean append) {
		try {
			out = new PrintStream(new FileOutputStream(pathOutput, append));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		evaluateResult(pathResult, pathRef);
	}

	private static void afficheComplet() {
		System.out.println("BuildingDetectionDemo usage :");
		System.out.println("\n evaluation mode :");
		System.out
			.println("   evaluate pathResult pathReference (pathOutput) (append)");
		System.out.println("\t pathResult: path to the generated binary result");
		System.out.println("\t pathReference: path to the reference binary image");
		System.out
			.println("\t pathOutput: path to the textual file used as output");
		System.out
			.println("\t append: if true, append the text at the end of the existing pathOutput file");
		System.out.println("\n detection mode :");
		System.out.println("   detect [-options]");
		System.out
			.println("\t -data name of the dataset (xxx_input.png and xxx_ref.png)");
		System.out.println("\t -bmode binarisation method : auto, manual, cluster");
		System.out.println("\t -fmode filtering method : auto, granulometry");
		System.out
			.println("\t -repeat number of iterations for varying parameter (threshold or percentage)");
		System.out
			.println("\t -threshold value of the threshold used in binarisation process (real between 0 and 1)");
		System.out
			.println("\t -view enable or disable image viewing (true or false)");
		System.out.println("\t -debug enable or disable debugging (true or false)");
		System.out
			.println("\t -save enable or disable image saving (true or false)");
		System.out.println("\t -min minimal size (height or width) of a building");
		System.out.println("\t -max maximum size (height or width) of a building");
		System.out.println("\t -xmin minimal height of a building");
		System.out.println("\t -xmax maximum height of a building");
		System.out.println("\t -ymin minimal width of a building");
		System.out.println("\t -ymax maximum width of a building");
		System.out
			.println("\t -fusion maximum number of clusters in fusion process");
		System.out
			.println("\t -level color level of objects : 0 for black objects, 1 for white objects, 2 for both");
		System.out.println("\t -binParam equivalent to -fusion or -level");
		System.out
			.println("\t -path specify the directory which contains data (optional)");
		System.out.println("\t -angle set the step of the orientation(real > 0.");
	}

	private static void afficheSimple() {
		System.out.println("BuildingDetectionDemo usage :");
		System.out.println("\n evaluation mode :");
		System.out
			.println("   evaluate pathResult pathReference (pathOutput) (append)");
		System.out.println("\t pathResult: path to the generated binary result");
		System.out.println("\t pathReference: path to the reference binary image");
		System.out
			.println("\t pathOutput: path to the textual file used as output");
		System.out
			.println("\t append: if true, append the text at the end of the existing pathOutput file");
		System.out.println("\n detection mode :");
		System.out.println("   detect [-options]");
		System.out
			.println("\t -data name of the dataset (xxx_input.png and xxx_ref.png)");
		System.out.println("\t -bmode binarisation method : auto, manual, cluster");
		System.out.println("\t -fmode filtering method : auto, granulometry");
		System.out
			.println("\t -repeat number of iterations for varying parameter (threshold or percentage)");
		System.out
			.println("\t -threshold value of the threshold used in binarisation process (real between 0 and 1)");
		System.out
			.println("\t -view enable or disable image viewing (true or false)");
		System.out.println("\t -debug enable or disable debugging (true or false)");
		System.out
			.println("\t -save enable or disable image saving (true or false)");
		System.out.println("\t -min minimal size (height or width) of a building");
		System.out.println("\t -max maximum size (height or width) of a building");
		System.out.println("\t -xmin minimal height of a building");
		System.out.println("\t -xmax maximum height of a building");
		System.out.println("\t -ymin minimal width of a building");
		System.out.println("\t -ymax maximum width of a building");
		System.out
			.println("\t -fusion maximum number of clusters in fusion process");
		System.out
			.println("\t -level color level of objects : 0 for black objects, 1 for white objects, 2 for both");
		System.out.println("\t -binParam equivalent to -fusion or -level");
		System.out
			.println("\t -path specify the directory which contains data (optional)");
		System.out.println("\t -angle set the step of the orientation(real > 0.");
	}

	private void configure(String[] args) throws Exception {
		int i = 0;
		try {
			for (i = 0; i < args.length - 1; i++) {
				if (args[i].equalsIgnoreCase("-data"))
					dataset = args[i + 1];
				else if (args[i].equalsIgnoreCase("-bMode"))
					bMode = args[i + 1];
				else if (args[i].equalsIgnoreCase("-repeat"))
					repeat = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-threshold")
					|| args[i].equalsIgnoreCase("-seuil"))
					binThr = Double.parseDouble(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-view"))
					view = Boolean.parseBoolean(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-debug"))
					debug = Boolean.parseBoolean(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-save"))
					save = Boolean.parseBoolean(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-min"))
					min = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-max"))
					max = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-xmin"))
					xmin = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-xmax"))
					xmax = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-ymin"))
					ymin = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-ymax"))
					ymax = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-fusion")
					|| args[i].equalsIgnoreCase("-level")
					|| args[i].equalsIgnoreCase("-binParam"))
					binParam = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-globalTTR"))
					globalTTR = Boolean.parseBoolean(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-minratio"))
					minRatio = Double.parseDouble(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-occo"))
					occo = Integer.parseInt(args[i + 1]);
				else if (args[i].equalsIgnoreCase("-fMode"))
					fMode = args[i + 1];
				else if (args[i].equalsIgnoreCase("-path"))
					path = args[i + 1];
				else if (args[i].equalsIgnoreCase("-angle"))
					angle = Double.parseDouble(args[i + 1]);
			}
		} catch (Exception ex) {
			throw new Exception("Problï¿œme avec paramï¿œtre #" + i + " : " + args[i]
				+ " " + args[i + 1]);
		}
		// configure sizes if set to default
		if (max==0)
			System.out.println("Error : max unknown !");
		if (xmax == -1)
			xmax = max;
		if (ymax == -1)
			ymax = max;
		if (xmin == -1)
			if (min == -1)
				xmin = xmax / 2;
			else
				xmin = min;
		if (ymin == -1)
			if (min == -1)
				ymin = ymax / 2;
			else
				ymin = min;
		// transform mode size into values
		if (bMode.equalsIgnoreCase("MANUAL")) {
			binMode = OriginalBinaryBuildingDetection.BINARISATION_MANUAL;
		} else if (bMode.equalsIgnoreCase("AUTO")) {
			binMode = OriginalBinaryBuildingDetection.BINARISATION_AUTO;
		} else if (bMode.equalsIgnoreCase("CLUSTER")) {
			binMode = OriginalBinaryBuildingDetection.BINARISATION_CLUSTER;
		}
		if (fMode.equalsIgnoreCase("MANUAL")) {
			filterMode = OriginalBinaryBuildingDetection.FILTER_MANUAL;
		} else if (fMode.equalsIgnoreCase("GRANULOMETRY")) {
			filterMode = OriginalBinaryBuildingDetection.FILTER_GRANULOMETRY;
		}
	}

}
