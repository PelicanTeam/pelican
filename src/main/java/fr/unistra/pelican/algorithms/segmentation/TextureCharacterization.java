package fr.unistra.pelican.algorithms.segmentation;

import java.awt.Point;
import java.util.Vector;

import weka.clusterers.SimpleKMeans;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.geometric.Crop2D;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.GrayOpeningByReconstruction;
import fr.unistra.pelican.algorithms.morphology.gray.granulometry.LimitedGranulometry;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * This algorithm return an segmented image based on granulometry analysis. It's
 * based on the granulometric analysis of various areas coming from a watershed
 * operation
 */

public class TextureCharacterization extends Algorithm {

	// Inputs parameters
	public Image inputImage;

	public int nbClusters;

	// Outputs parameters
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public TextureCharacterization() {

		super();
		super.inputs = "inputImage,nbClusters";
		super.outputs = "outputImage";
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		try {
			int count = 0;
			Vector granulometry_vector = new Vector();
			outputImage = inputImage.copyImage(false);
			outputImage.setColor(true);

			// Elements structurants pour les traitements
			BooleanImage se = FlatStructuringElement2D
					.createSquareFlatStructuringElement(3);
			BooleanImage se2 = FlatStructuringElement2D
					.createSquareFlatStructuringElement(2);

			// Image OKKO = GrayOCCO.process(inputImage,se);
			// Viewer2D.exec(OKKO,"okko");

			// Image median = GrayMedian.process(inputImage,se);
			// Viewer2D.exec(median,"median");

			Image to = (Image) new GrayOpeningByReconstruction().process(inputImage, se);

			Image img2 = (Image) new GrayGradient().process(to, se2);
			System.out.println("GrayGradient done");
			new Viewer2D().process(img2, "gradient");

			Image img3 = (Image) new Watershed().process(img2);
			System.out.println("Watershed done");

			// Creation du mask pour la granulometrie
			BooleanImage mask = new BooleanImage(inputImage, false);

			// Image temporaire "vide" de la taille de l'image de depart
			Image fond = inputImage.copyImage(false);

			// On calcule la distribution de chacun des labels de l image
			// segmentee par watershed
			Vector v = label_distribution(img3);

			for (int i = 1; i < v.size(); i++) {// v.size()
				mask = new BooleanImage(inputImage, false);
				mask.setBDim(1);
				mask.fill(false);
				int x, y;
				int x1 = 0, x2 = 0, y1 = 0, y2 = 0;

				Image tmp = fond.copyImage(false);

				// On recupere l'ensemble des pixels de label i
				Vector ens_pixel = (Vector) v.get(i);

				// Si l'entree i n existe pas c est que le label i n existe pas
				if (ens_pixel == null) {
					System.out.println("l entree" + i + "existe pas");
					count++;
				}

				// On recopie les pixels de label i a partir de l image de
				// depart dans une nouvelle image
				// Puis on recadre autour de la zone de label i et on effectue
				// une mesure de granulometrie
				else {

					if (ens_pixel.size() == 0)
						System.out.println("Oops");

					x1 = (int) ((Point) (ens_pixel.get(0))).getX();
					x2 = (int) ((Point) (ens_pixel.get(0))).getX();
					y1 = (int) ((Point) (ens_pixel.get(0))).getY();
					y2 = (int) ((Point) (ens_pixel.get(0))).getY();

					// Boucle pour chaque pixel de label i
					for (int j = 0; j < ens_pixel.size(); j++) {
						Point a;

						// On calcule le plus petit cadre autour de la zone de
						// pixel de label i
						a = (Point) ens_pixel.get(j);
						x = (int) a.getX();
						y = (int) a.getY();
						if (x < x1)
							x1 = x;
						if (y < y1)
							y1 = y;
						if (x > x2)
							x2 = x;
						if (y > y2)
							y2 = y;

						// On cree un masque de taille adaptee initialise a faux
						// Il sera utilise pour calculer des donnees
						// granulometriques plus precises

						// On recopie les pixels depuis l image de depart vers
						// une image a analyser
						for (int bande = 0; bande < inputImage.getBDim(); bande++) {
							tmp.setPixelXYBDouble(x, y, bande, inputImage
									.getPixelXYBDouble(x, y, bande));
							mask.setPixelXYBBoolean(x, y, 0, true);
						}
					}
				}

				// Recadrage autour de la zone de label i
				Image window = (Image) new Crop2D().process(tmp, x1, y1, x2, y2);

				BooleanImage imask = (BooleanImage) new Crop2D().process(mask, x1, y1,
						x2, y2);

				// Resultat de l analyse granulometrique de la zone de label i
				double[] granulometry = (double[]) new LimitedGranulometry().process(window,
						7, imask);

				if ((i % 1000) == 0)// pour voir avancement
					System.out.println("Granulo " + i);
				// On place ce resultat dans le vecteur resultat
				if (granulometry_vector.size() <= i) {
					granulometry_vector.setSize(i + 1);
				}
				granulometry_vector.set(i, granulometry);
			}
			System.out.println("Granulometry done ");

			// Debut de la classification Kmeans

			SimpleKMeans clusterer = new SimpleKMeans();
			try {
				clusterer.setNumClusters(nbClusters);
			} catch (Exception e) {
				e.printStackTrace();
			}

			double[] vect = (double[]) granulometry_vector.get(1);

			// Nb de mesures granulometriques
			int taille = vect.length;

			FastVector attributes = new FastVector(taille);
			for (int i = 0; i < vect.length; i++)
				attributes.addElement(new weka.core.Attribute("SE " + i));

			Instances dataset = new Instances("dataset", attributes, 0);

			for (int i = 1; i < granulometry_vector.size(); i++) {
				double tmp[] = (double[]) granulometry_vector.get(i);
				Instance instance = new Instance(dataset.numAttributes());
				for (int j = 0; j < taille; j++) {
					instance.setValue(j, tmp[j]);
				}
				instance.setDataset(dataset);
				dataset.add(instance);

			}

			// Learn the classification
			try {
				clusterer.buildClusterer(dataset);
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println(clusterer.toString());

			for (int i = 1; i < granulometry_vector.size(); i++) {
				double tmp[] = (double[]) granulometry_vector.get(i);
				Instance instance = new Instance(dataset.numAttributes());
				for (int j = 0; j < taille; j++) {
					instance.setValue(j, tmp[j]);
				}
				instance.setDataset(dataset);
				int label = -1;

				try {
					label = clusterer.clusterInstance(instance);

				} catch (Exception e) {
					e.printStackTrace();
				}

				Vector ens_pixel = (Vector) v.get(i);
				int couleur = (255 / nbClusters) * (label);

				// On attribue une couleur aux pixels d'un label i
				for (int i_ = 0; i_ < ens_pixel.size(); i_++) {
					Point a = (Point) ens_pixel.get(i_);
					int x = (int) a.getX();
					int y = (int) a.getY();

					// Pour attribuer simplement le numero du label au tous les
					// pixels, simplement mettre setPixelInt(x,y,label)
					if (label % 5 == 0) {
						outputImage.setPixelXYBByte(x, y, 0, couleur);
					} else if (label % 5 == 1) {
						outputImage.setPixelXYBByte(x, y, 1, couleur);
					} else if (label % 5 == 2) {
						outputImage.setPixelXYBByte(x, y, 2, couleur);
					} else if (label % 5 == 3) {
						outputImage.setPixelXYBByte(x, y, 0, couleur);
						outputImage.setPixelXYBByte(x, y, 1, couleur);
					} else if (label % 5 == 4) {
						outputImage.setPixelXYBByte(x, y, 1, couleur);
						outputImage.setPixelXYBByte(x, y, 2, couleur);
					} else {
						outputImage.setPixelXYBByte(x, y, 0, couleur);
						outputImage.setPixelXYBByte(x, y, 2, couleur);
					}
				}
			}

			System.out.println("Classification done");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// This function puts the coordinates of the point (of the image img) which
	// label is L at the position L in a vector
	private Vector label_distribution(Image img) {
		Vector<Vector> ens_label = new Vector<Vector>();
		for (int i = 0; i < img.getXDim(); i++) {
			for (int j = 0; j < img.getYDim(); j++) {
				int label = img.getPixelXYBInt(i, j, 0) - Integer.MIN_VALUE;

				if (ens_label.size() <= label) {
					ens_label.setSize(label + 1);
				}

				if (ens_label.get(label) == null) {
					ens_label.set(label, new Vector());
				}

				(ens_label.get(label)).add(new Point(i, j));
			}
		}
		System.out.println("Number of labels " + ens_label.size());
		return ens_label;
	}

	public static void main(String[] args) {
		String file = "c:\\aster_05082003.hdr";
		if (args.length > 0)
			file = args[0];

		try {
			// Load the image
			Image source = (Image) new ImageLoader().process(file);
			new Viewer2D().process(source, "Image " + file);
			Image source2 = (Image) new Crop2D().process(source, 0, 0, 300, 300);
			// Image source2 = Crop.process(source,0,50,700,350);
			new Viewer2D().process(source2, "Image " + file);
			Image result = (Image) new TextureCharacterization().process(source2, 5);
			new Viewer2D().process(result, "Result");
			new ImageSave().process(result, "c:\\resulat.tiff");
			new ImageSave().process(source, "c:\\source.tiff");

		} catch (InvalidTypeOfParameterException e) {
			e.printStackTrace();
		} catch (AlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidNumberOfParametersException e) {
			e.printStackTrace();
		}
	}
}
