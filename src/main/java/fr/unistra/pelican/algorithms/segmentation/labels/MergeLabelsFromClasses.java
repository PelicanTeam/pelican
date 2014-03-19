package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;


/**
 * Merge the labels of connected regions belonging to the same class
 * 
 * @author Lefevre
 */
public class MergeLabelsFromClasses extends Algorithm {

	/**
	 * 
	 * Label Image
	 */
	public Image labelImage;

	/**
	 * Class Image
	 */
	public Image classImage;

	/**
	 * Flag for 4-connexity
	 */
	public boolean connexity4 = false;

	/**
	 * result
	 */
	public Image outputImage;

	/**
	 * Constructor
	 * 
	 */
	public MergeLabelsFromClasses() {
		super.inputs = "labelImage,classImage";
		super.options = "connexity4";
		super.outputs = "outputImage";
	}

	public void launch() {

		outputImage = labelImage.copyImage(false);
		int xdim = labelImage.getXDim();
		int ydim = labelImage.getYDim();

		// Rï¿œcupï¿œration de la classe de chaque label
		if (!Image.haveSameDimensions(labelImage, classImage)) {
			System.err
				.println("MergeLabelsFromClasses: labelImage and classImage do not have same dimensions");
			return;
		}

		// Dï¿œcompte du nombre de labels
		int maxLabel = 0;
		for (int p = 0; p < labelImage.size(); p++)
			if (labelImage.getPixelInt(p) > maxLabel)
				maxLabel = labelImage.getPixelInt(p);

		// Association des labels au classes
		int tabLabel[] = new int[maxLabel + 1];
		int min=0;
		Arrays.fill(tabLabel, -1);
		for (int p = 0; p < labelImage.size(); p++) {
			int val = labelImage.getPixelInt(p);
			if (tabLabel[val] == -1 && classImage.getPixelInt(p) != 0)
				tabLabel[val] = classImage.getPixelInt(p);
		}

		for (int b = 0; b < labelImage.getBDim(); b++)
			for (int t = 0; t < labelImage.getTDim(); t++)
				for (int z = 0; z < labelImage.getZDim(); z++) {
					// Crï¿œation de la table d'ï¿œquivalence
					int newLabel[] = new int[maxLabel + 1];
					int newLabel2[] = new int[maxLabel + 1];
					for (int tt = 0; tt < newLabel.length; tt++)
						newLabel[tt] = tt;
					// Parcours de l'image de label et recherche des zones connexes
					for (int y = 1; y < ydim - 1; y++)
						for (int x = 1; x < xdim - 1; x++) {
							int val = labelImage.getPixelInt(x, y, z, t, b);
							for (int yy = -1; yy <= 1; yy++)
								for (int xx = -1; xx <= 1; xx++)
									if (xx != 0 && yy != 0)
										if (!connexity4 || Math.abs(xx - yy) == 1) {
											int val2 = labelImage
												.getPixelInt(x + xx, y + yy, z, t, b);
											// Si les 2 zones connexes sont de la mï¿œme classe, on met
											// ï¿œ
											// jour la table d'ï¿œquivalence
											if (val2 != val && newLabel[val2] != newLabel[val]
												&& tabLabel[val2] == tabLabel[val]) {
												min=Math.min(newLabel[newLabel[val]], newLabel[newLabel[val2]]);
												newLabel[newLabel[val]]=min;
												newLabel[newLabel[val2]]=min;
												newLabel[val]=min;
												newLabel[val2]=min;
												//newLabel[Math.max(val, val2)] = Math.min(val, val2);
												//System.out.println(Math.max(val, val2)+" => "+Math.min(val, val2));
											}
										}
						}
					// Simplification de la table de labels
					int nbLabels = 0;
					for (int tt = 0; tt < newLabel.length; tt++) {
						if (newLabel[tt] == tt)
							newLabel2[tt] = nbLabels++;
						else {
							int tt2=tt;
							while (newLabel[tt2] != tt2)
								tt2=newLabel[tt2];
							newLabel[tt]=tt2;
						}
						//System.out.println(tt+" "+tabLabel[tt]+" "+newLabel[tt]+" "+newLabel2[newLabel[tt]]);
					 }
					// Nouveau parcours pour mise ï¿œ jour des labels
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++)
							outputImage.setPixelInt(x, y, z, t, b,
								newLabel2[newLabel[labelImage.getPixelInt(x, y, z, t, b)]]);
				}
	}

	public static IntegerImage exec(Image labelImage, Image classImage) {
		return (IntegerImage) new MergeLabelsFromClasses().process(labelImage,
			classImage);
	}

	public static Image exec(Image labelImage, Image classImage,
		boolean connexity4) {
		return (IntegerImage) new MergeLabelsFromClasses().process(labelImage,
			classImage, connexity4);
	}

}
