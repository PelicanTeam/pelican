package fr.unistra.pelican.demos.applied.remotesensing;

import java.util.Vector;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.arithmetic.Difference;
import fr.unistra.pelican.algorithms.conversion.ColorImageFromMultiBandImage;
import fr.unistra.pelican.algorithms.io.HdrImageLoad;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.io.MultipleImageLoad;
import fr.unistra.pelican.algorithms.logical.AND;
import fr.unistra.pelican.algorithms.logical.CompareConstant;
import fr.unistra.pelican.algorithms.logical.CompareImage;
import fr.unistra.pelican.algorithms.morphology.binary.BinaryGradient;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;
import fr.unistra.pelican.algorithms.segmentation.RidlerThresholding;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.labels.RegionSize;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class CoastalAnalysisDemo {

	public static int ZONE = 0;

	public static int COMP = 1;
	public static int DIFF = 2;
	public static int COMP_DIFF = 3;
	public static boolean SAVE = false;
	public static boolean VIEW = true;

	int zone;
	String path = "/home/lefevre/data/global/teledetection/ecosgil/villerville/";
	String test = path + "tests/";
	String files[] = { "Zone3_MSS_60mC_25061976", // 60m 4 bandes
			"Zone3_ETM_30mC_13092002", // 30m 6 bandes
			"Zone3_ETM_30mC_21072000", // 30m 6 bandes
			"Zone3_TM_30mC_20051992", // 30m 6 bandes
			"Zone3_Spot_20m_24051987", // 20m 3 bandes
			"Zone3_Spot_20m_29061993", // 20m 3 bandes
			"Zone3_Aster_15m_050603", // 15m 3 bandes
			"Zone3_Aster_15m_250903", // 15m 3 bandes
			"Zone3_ETM_15mP_13092002", // 15m 1 bande
			"Zone3_ETM_15mP_21072000", // 15m 1 bande
			"Zone3_Spot5_10mC_230303", // 10m 3 bandes
			"Zone3_QB-MS_L1_ortho5m_261002", // 5m 4 bandes
			"Zone3_Gram_Spot5_5mC_230303", // 5m 3 bandes
			"Zone3_Spot5_5mP_230303", // 5m 1 bande
			"mnt_villerville_l1_5m", // 5m MNT 1 bande
			"Zone3_Spot5_2-5mP_23032003", // 2.5m 1 bande
			"Zone3_QB_2-4mC_26102002_ortho5m", // 2.4m 4 bandes
			"Zone3_QB_0-6mP_26102002_ortho5m", // 0.6m 1 bande
	};

	public void displayFiles() {
		System.out.println("Fichiers disponibles");
		for (int i = 0; i < files.length; i++)
			System.out.println(i + ":\t" + files[i]);
	}

	public void displaySyntax() {
		System.out.println("CoastalDetectionDemo : consulter la documentation");
	}

	public CoastalAnalysisDemo() {
		zone = ZONE;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws PelicanException {
		CoastalAnalysisDemo demo = new CoastalAnalysisDemo();

		// Affichage des fichiers
		if (args.length == 1 && args[0].equalsIgnoreCase("help"))
			demo.displayFiles();

		// Utilisation du travail de TER
		else if (args.length == 1 && args[0].equalsIgnoreCase("ter"))
			demo.etudeFiegelKamblockETM();

		// Traitement d'une image binaire
		else if (args.length == 2 && args[0].equalsIgnoreCase("process"))
			demo.processZone(args[1]);

		// Choix d'une zone
		else if (args.length == 2 && args[0].equalsIgnoreCase("zone"))
			demo.etudeZone(Integer.parseInt(args[1]));

		else
			demo.displaySyntax();

	}

	public void etudeZone(int z) {
		zone = z;
		long t1 = System.currentTimeMillis();
		System.out.println("chargement");
		Image rs = HdrImageLoad.exec(path + files[zone] + ".hdr");
		System.out.println("affichage");
		Viewer2D.exec(rs, "Villerville image #" + zone);
		System.out.println("sauvegarde");
		if (rs.getBDim() == 1 || rs.getBDim() == 3)
			ImageSave.exec(rs, path + files[zone] + ".png");
		else
			for (int i = 0; i < rs.getBDim(); i++)
				for (int j = 0; j < rs.getBDim(); j++)
					for (int k = 0; k < rs.getBDim(); k++)
						ImageSave.exec(ColorImageFromMultiBandImage.exec(rs, i,
								j, k), path + files[zone] + "-" + i + j + k
								+ ".png");
		System.out.println("seuillage");
		thresholdBands(rs);
		System.out.println("comparaison");
		compareBands(rs, COMP_DIFF, 2);
		long t2 = System.currentTimeMillis() - t1;
		System.out.println("Dmo termine : " + (t2 / 1000) + " secondes");
	}

	public void processZone(String file) {
		long t1 = System.currentTimeMillis();
		System.out.println("chargement");
		Image rs = ImageLoader.exec(file);
		rs = RidlerThresholding.exec(rs);
		System.out.println("affichage");
		Viewer2D.exec(rs, "Binarisation");
		System.out.println("local");
		ImageSave.exec(local(rs, 2), "local.png");
		System.out.println("global");
		ImageSave.exec(global(rs), "global.png");
		long t2 = System.currentTimeMillis() - t1;
		System.out.println("Dmo termine : " + (t2 / 1000) + " secondes");

	}

	public void etudeFiegelKamblockETM() {
		// Chargement
		String path = "/home/lefevre/data/global/teledetection/etm30m";
		Image rs = MultipleImageLoad.exec(path, Image.B);

		// Sous-chantillonage effectuer les tests ou gnrer les images
		// rs=Subsampling.process(rs,4,4,1,1,1,Subsampling.AVERAGE);

		// Etude prliminaire: comparaison des bandes et seuillages
		thresholdBands(rs);
		compareBands(rs, COMP_DIFF, 0);

		// Binarisation
		Image binaryKamblock = compareAndTreshold(rs, 4, 5, 14);
		ImageSave.exec(binaryKamblock, "binaryKamblock.png");
		Image binaryFiegel = singleThreshold(rs, 3, 28);
		ImageSave.exec(binaryFiegel, "binaryFiegel.png");

		// Analyse de l'image binaire
		Image resultKamblock = global(binaryKamblock);
		ImageSave.exec(resultKamblock, "resultKamblock.png");
		Image resultFiegel = local(binaryFiegel, 50);
		ImageSave.exec(resultFiegel, "resultFiegel.png");
	}

	void thresholdBands(Image rs) {
		for (int i = 0; i < rs.getBDim(); i++)
			ImageSave.exec(RidlerThresholding.exec(rs.getImage4D(i, Image.B),
					false), test + files[zone] + "/" + "thr-" + (i + 1)
					+ ".png");
	}

	void compareBands(Image img, int operation, int threshold) {
		Image rs = img;
		if (threshold != 1) {
			for (int i = 0; i < rs.getBDim(); i++)
				for (int j = 0; j < rs.getBDim(); j++)
					if (i != j) {
						if (operation == COMP || operation == COMP_DIFF)
							ImageSave.exec(CompareImage.exec(rs.getImage4D(i,
									Image.B), rs.getImage4D(j, Image.B),
									CompareImage.GEQ), test + files[zone] + "/"
									+ "comp-" + i + "-" + j + ".png");
						if (operation == DIFF || operation == COMP_DIFF)
							ImageSave.exec(Difference.exec(rs.getImage4D(i,
									Image.B), rs.getImage4D(j, Image.B), true),
									test + files[zone] + "/" + "diff-" + i
											+ "-" + j + ".png");
					}
		}
		if (threshold != 0) {
			for (int i = 0; i < rs.getBDim(); i++)
				for (int j = 0; j < rs.getBDim(); j++)
					if (i != j) {
						if (operation == COMP || operation == COMP_DIFF)
							ImageSave.exec(CompareImage.exec(rs.getImage4D(i,
									Image.B), rs.getImage4D(j, Image.B),
									CompareImage.GEQ), test + files[zone] + "/"
									+ "comps-" + i + "-" + j + ".png");
						if (operation == DIFF || operation == COMP_DIFF)
							ImageSave.exec(RidlerThresholding.exec(Difference
									.exec(rs.getImage4D(i, Image.B), rs
											.getImage4D(j, Image.B), true),
									false), test + files[zone] + "/" + "diffs-"
									+ i + "-" + j + ".png");
					}

		}
	}

	Image compareAndTreshold(Image img, int i, int j, int seuil) {
		Image rs = img;
		Image bandA = rs.getImage4D(i - 1, Image.B);
		Image bandB = rs.getImage4D(j - 1, Image.B);
		// Segmentation
		Image comp1 = CompareImage.exec(bandA, bandB, CompareImage.GEQ);
		Image comp2 = null;
		if (seuil == -1)
			comp2 = RidlerThresholding.exec(bandA, false);
		else
			comp2 = CompareConstant.exec(bandA, seuil, CompareImage.SUP);
		Image res = AND.exec(comp1, comp2);
		if (SAVE)
			ImageSave.exec(res, test + files[zone] + "/" + "comps-" + i + "-"
					+ j + ".png");
		return res;
	}

	Image singleThreshold(Image rs, int band, int seuil) {
		// Seuillage de l'image
		Image img = rs.getImage4D(band - 1, Image.B);
		if (seuil == -1)
			img = RidlerThresholding.exec(img, false);
		else
			img = ManualThresholding.exec(img, seuil);
		if (SAVE)
			ImageSave.exec(img, test + files[zone] + "/" + "bands-" + band
					+ ".png");
		return img;
	}

	Image global(Image img) {
		Image res = img.copyImage(true);
		// Filtrage
		for (int k = 1; k < 16; k++) {
			// res=BinaryErosion.process(res,FlatStructuringElement.createFrameFlatStructuringElement(2*i+1));
			// res=BinaryDilation.process(res,FlatStructuringElement.createFrameFlatStructuringElement(2*i+1));
			filter(res, k, true);
			filter(res, k, false);
		}
		if (SAVE)
			ImageSave.exec(res, "global-filtered.png");
		res = BinaryGradient.exec(res, FlatStructuringElement2D
				.createSquareFlatStructuringElement(2));
		if (SAVE)
			ImageSave.exec(res, "global-gradient.png");
		// Etiquetage en composantes connexes
		frame(res, false);
		res = BooleanConnectedComponentsLabeling.exec(res,
				BooleanConnectedComponentsLabeling.CONNEXITY8);
		// Conservation de la plus grande rgion (le trait de cte par
		// hypothse)
		int tailles[] = RegionSize.exec(res);
		int max = 1;
		for (int t = 1; t < tailles.length; t++)
			if (tailles[max] < tailles[t])
				max = t;
		for (int p = 0; p < res.size(); p++)
			if (res.getPixelInt(p) != 0)
				if (res.getPixelInt(p) == max)
					res.setPixelInt(p, 1);
				else
					res.setPixelInt(p, 2);
		// Affichage en pseudo-couleur
		res = LabelsToRandomColors.exec(res, true);
		if (SAVE)
			ImageSave.exec(res, "global-result.png");
		if (VIEW)
			Viewer2D.exec(res, "global-result");
		return res;
	}

	void frame(Image img, boolean white) {
		for (int x = 0; x < img.getXDim(); x++) {
			img.setPixelXYBoolean(x, 0, white);
			img.setPixelXYBoolean(x, img.getYDim() - 1, white);
		}
		for (int y = 0; y < img.getYDim(); y++) {
			img.setPixelXYBoolean(0, y, white);
			img.setPixelXYBoolean(img.getXDim() - 1, y, white);
		}
	}

	void filter(Image img, int size, boolean white) {
		boolean stop;
		for (int x = size; x < img.getXDim() - size; x++)
			for (int y = size; y < img.getYDim() - size; y++)
				if (img.getPixelXYBoolean(x, y) == white) {
					stop = false;
					for (int i = 0; i < size * 2 && !stop; i++)
						if (img.getPixelXYBoolean(x + i - size, y - size) == white
								|| img
										.getPixelXYBoolean(x + i - size, y
												+ size) == white
								|| img
										.getPixelXYBoolean(x - size, y + i
												- size) == white
								|| img
										.getPixelXYBoolean(x + size, y + i
												- size) == white)
							stop = true;
					if (!stop)
						img.setPixelXYBoolean(x, y, !white);
				}
	}

	Image local(Image img, int grille) {
		// Initialisation des graines
		int xDim = img.getXDim();
		int yDim = img.getYDim();
		Image res = img.copyImage(false);
		int nbre = 0;
		int[][] graines = new int[(xDim / grille) * (yDim / grille)][2];
		for (int i = 0; i < xDim - grille; i += grille)
			for (int j = 0; j < yDim - grille; j += grille) {
				graines[nbre][0] = i;
				graines[nbre][1] = j;
				nbre++;
			}
		// Filtrage des graines
		for (int i = 0; i < nbre; i++) {
			if (img.getPixelBoolean(graines[i][0], graines[i][1], 0, 0, 0))
				res
						.setPixelBoolean(graines[i][0], graines[i][1], 0, 0, 0,
								true);
			if (graines[i][0] + grille < xDim)
				if (img.getPixelBoolean(graines[i][0] + grille, graines[i][1],
						0, 0, 0))
					res.setPixelBoolean(graines[i][0], graines[i][1], 0, 0, 0,
							false);
			if (graines[i][1] + grille < yDim)
				if (img.getPixelBoolean(graines[i][0], graines[i][1] + grille,
						0, 0, 0))
					res.setPixelBoolean(graines[i][0], graines[i][1], 0, 0, 0,
							false);
		}
		if (SAVE)
			ImageSave.exec(res, "local-filtered.png");
		nbre = 0;
		// Mise  jour des graines
		for (int i = 0; i < xDim; i++)
			for (int j = 0; j < yDim; j++)
				if (res.getPixelBoolean(i, j, 0, 0, 0)) {
					graines[nbre][0] = i;
					graines[nbre][1] = j;
					nbre++;
					res.setPixelBoolean(i, j, 0, 0, 0, false);
				}
		if (SAVE)
			ImageSave.exec(res, "local-updated.png");
		// Rercherche pour chacune des graines
		for (int z = 0; z < nbre; z++)
			if (!res.getPixelBoolean(graines[z][0], graines[z][1], 0, 0, 0)) {
				int posX = graines[z][0];
				int posY = graines[z][1];
				res.setPixelBoolean(posX, posY, 0, 0, 0, true);
				Vector<Object> mouvements = new Vector<Object>();
				int debut = -1;
				int nord = 0;
				int nordEst = 1;
				int est = 2;
				int sudEst = 3;
				int sud = 4;
				int sudOuest = 5;
				int ouest = 6;
				int nordOuest = 7;
				mouvements.addElement(debut);
				while (!mouvements.isEmpty()) {
					Integer lastMouv = (Integer) mouvements.lastElement();
					boolean pos0 = false, pos1 = false, pos2 = false, pos3 = false, pos4 = false, pos5 = false, pos6 = false, pos7 = false;
					if (posY > 0)
						pos0 = img.getPixelBoolean(posX, posY - 1, 0, 0, 0);
					if (posX < (xDim - 1) && posY > 0)
						pos1 = img.getPixelBoolean(posX + 1, posY - 1, 0, 0, 0);
					if (posX < (xDim - 1))
						pos2 = img.getPixelBoolean(posX + 1, posY, 0, 0, 0);
					if (posX < (xDim - 1) && posY < (yDim - 1))
						pos3 = img.getPixelBoolean(posX + 1, posY + 1, 0, 0, 0);
					if (posY < (yDim - 1))
						pos4 = img.getPixelBoolean(posX, posY + 1, 0, 0, 0);
					if (posX > 0 && posY < (yDim - 1))
						pos5 = img.getPixelBoolean(posX - 1, posY + 1, 0, 0, 0);
					if (posX > 0)
						pos6 = img.getPixelBoolean(posX - 1, posY, 0, 0, 0);
					if (posX > 0 && posY > 0)
						pos7 = img.getPixelBoolean(posX - 1, posY - 1, 0, 0, 0);
					if (pos7
							&& !res
									.getPixelBoolean(posX - 1, posY - 1, 0, 0,
											0) && (!pos0 || !pos6)) {
						res.setPixelBoolean(posX - 1, posY - 1, 0, 0, 0, true);
						mouvements.addElement(nordOuest);
						posX--;
						posY--;
					} else if (pos6
							&& !res.getPixelBoolean(posX - 1, posY, 0, 0, 0)
							&& (!pos7 || !pos5)) {
						res.setPixelBoolean(posX - 1, posY, 0, 0, 0, true);
						mouvements.addElement(ouest);
						posX--;
					} else if (pos5
							&& !res
									.getPixelBoolean(posX - 1, posY + 1, 0, 0,
											0) && (!pos4 || !pos6)) {
						res.setPixelBoolean(posX - 1, posY + 1, 0, 0, 0, true);
						mouvements.addElement(sudOuest);
						posX--;
						posY++;
					} else if (pos4
							&& !res.getPixelBoolean(posX, posY + 1, 0, 0, 0)
							&& (!pos3 || !pos5)) {
						res.setPixelBoolean(posX, posY + 1, 0, 0, 0, true);
						mouvements.addElement(sud);
						posY++;
					} else if (pos3
							&& !res
									.getPixelBoolean(posX + 1, posY + 1, 0, 0,
											0) && (!pos2 || !pos4)) {
						res.setPixelBoolean(posX + 1, posY + 1, 0, 0, 0, true);
						mouvements.addElement(sudEst);
						posX++;
						posY++;
					} else if (pos2
							&& !res.getPixelBoolean(posX + 1, posY, 0, 0, 0)
							&& (!pos1 || !pos3)) {
						res.setPixelBoolean(posX + 1, posY, 0, 0, 0, true);
						mouvements.addElement(est);
						posX++;
					} else if (pos1
							&& !res
									.getPixelBoolean(posX + 1, posY - 1, 0, 0,
											0) && (!pos0 || !pos2)) {
						res.setPixelBoolean(posX + 1, posY - 1, 0, 0, 0, true);
						mouvements.addElement(nordEst);
						posX++;
						posY--;
					} else if (pos0
							&& !res.getPixelBoolean(posX, posY - 1, 0, 0, 0)
							&& (!pos7 || !pos1)) {
						res.setPixelBoolean(posX, posY - 1, 0, 0, 0, true);
						mouvements.addElement(nord);
						posY--;
					} else {
						mouvements.removeElementAt(mouvements.size() - 1);
						int i = lastMouv.intValue();
						switch (i) {
						case 0:
							posY++;
							break;
						case 1:
							posY++;
							posX--;
							break;
						case 2:
							posX--;
							break;
						case 3:
							posX--;
							posY--;
							break;
						case 4:
							posY--;
							break;
						case 5:
							posX++;
							posY--;
							break;
						case 6:
							posX++;
							break;
						case 7:
							posX++;
							posY++;
							break;
						}
					}
				}
			}
		if (SAVE)
			ImageSave.exec(res, "local-result.png");
		if (VIEW)
			Viewer2D.exec(res, "local-result");
		return res;
	}
}
