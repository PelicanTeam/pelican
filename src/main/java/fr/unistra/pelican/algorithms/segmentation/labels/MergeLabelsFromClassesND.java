package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;


/**
 * Merge the labels of connected regions belonging to the same class in N Dimensions
 * 
 * @author Jonathan Weber
 */
public class MergeLabelsFromClassesND extends Algorithm {

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
	public MergeLabelsFromClassesND() {
		super.inputs = "labelImage,classImage";
		super.options = "connexity4";
		super.outputs = "outputImage";
	}

	public void launch() {

		outputImage = labelImage.copyImage(false);
		int xdim = labelImage.getXDim();
		int ydim = labelImage.getYDim();
		int zdim = labelImage.getZDim();
		int tdim = labelImage.getTDim();
		int bdim = labelImage.getBDim();

		// Récupération de la classe de chaque label
		if (!Image.haveSameDimensions(labelImage, classImage)) {
			System.err
				.println("MergeLabelsFromClassesND: labelImage and classImage do not have same dimensions");
			return;
		}

		// Décompte du nombre de labels
		int maxLabel = 0;
		for (int p = 0; p < labelImage.size(); p++)
			if (labelImage.getPixelInt(p) > maxLabel)
				maxLabel = labelImage.getPixelInt(p);

		// Association des labels au classes
		int tabLabel[] = new int[maxLabel + 1];
		Arrays.fill(tabLabel, -1);
		for (int p = 0; p < labelImage.size(); p++) {
			int val = labelImage.getPixelInt(p);
			if (tabLabel[val] == -1 && classImage.getPixelInt(p) != 0)
				tabLabel[val] = classImage.getPixelInt(p);
		}


		// Création de la table d'équivalence
		int newLabel[] = new int[maxLabel + 1];
		int newLabel2[] = new int[maxLabel + 1];
		for (int tt = 0; tt < newLabel.length; tt++)
			newLabel[tt] = tt;
		// Parcours de l'image de label et recherche des zones connexes
		for(int b=0;b<bdim;b++)
			for(int t=0;t<tdim;t++)
				for(int z=0;z<zdim;z++)
					for (int y = 0; y < ydim; y++)
						for (int x = 0; x < xdim; x++) {
							int val = labelImage.getPixelInt(x, y, z, t, b);
							for (int bb = -1; bb <= 1; bb++)
								for (int tt = -1; tt <= 1; tt++)
									for (int zz = -1; zz <= 1; zz++)
										for (int yy = -1; yy <= 1; yy++)
											for (int xx = -1; xx <= 1; xx++)
												if ((xx!=0||xdim==1)&&(yy!=0||ydim==1)&&(zz!=0||zdim==1)&&(tt!=0||tdim==1)&&(bb!=0||bdim==1)
														&&x+xx>=0&&y+yy>=0&&z+zz>=0&&t+tt>=0&&b+bb>=0
														&&x+xx<xdim&&y+yy<ydim&&z+zz<zdim&&t+tt<tdim&&b+bb<bdim)
													{
														int val2 = labelImage
														.getPixelInt(x + xx, y + yy, z+zz, t+tt, b+bb);
														// Si les 2 zones connexes sont de la même classe, on met
														// à
														// jour la table d'équivalence
														if (val2 != val && newLabel[val2] != newLabel[val]
														                                              && tabLabel[val2] == tabLabel[val]) {
															newLabel[val]=Math.min(newLabel[val], newLabel[val2]);
															newLabel[val2]=Math.min(newLabel[val], newLabel[val2]);
															//newLabel[Math.max(val, val2)] = Math.min(val, val2);
															System.out.println(Math.max(val, val2)+" => "+Math.min(val, val2));
														}
													}
						}
					// Simplification de la table de labels
					int nbLabels = 0;
					for (int tt = 0; tt < newLabel.length; tt++) {
						if (newLabel[tt] == tt)
							newLabel2[tt] = nbLabels++;
						System.out.println(tt+" "+tabLabel[tt]+" "+newLabel[tt]+" "+newLabel2[newLabel[tt]]);
					}
					// Nouveau parcours pour mise à jour des labels
					for(int b=0;b<bdim;b++)
						for(int t=0;t<tdim;t++)
							for(int z=0;z<zdim;z++)
								for (int y = 0; y < ydim; y++)
									for (int x = 0; x < xdim; x++)
										outputImage.setPixelInt(x, y, z, t, b,
												newLabel2[newLabel[labelImage.getPixelInt(x, y, z, t, b)]]);
	}

	public static IntegerImage exec(Image labelImage, Image classImage) {
		return (IntegerImage) new MergeLabelsFromClassesND().process(labelImage,
			classImage);
	}

	public static Image exec(Image labelImage, Image classImage,
		boolean connexity4) {
		return (IntegerImage) new MergeLabelsFromClassesND().process(labelImage,
			classImage, connexity4);
	}

}
