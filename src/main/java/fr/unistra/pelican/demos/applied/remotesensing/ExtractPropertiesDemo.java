package fr.unistra.pelican.demos.applied.remotesensing;

import java.util.ArrayList;
import java.util.Arrays;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.applied.remotesensing.index.NDVI;
import fr.unistra.pelican.algorithms.conversion.AverageChannels;
import fr.unistra.pelican.algorithms.io.ImageBuilder;
import fr.unistra.pelican.algorithms.io.ImageLoader;

public class ExtractPropertiesDemo {

	public static void main(String[] args) {
		// chargement et affichage pour sï¿œlection des zones
		String path = "/home/miv/lefevre/data/teledetection/ecosgil/thrs/";
		String respath = "/home/miv/lefevre/data/teledetection/ecosgil/thrs/";
		String filepath = path + "VillervilleQB2_4.hdr";
		Image source = ImageLoader.exec(filepath);
		source=addBands(source);
		Image bg = ImageLoader.exec(respath + "input.png");
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
		public static Image addBands(Image sat) {
			Image ndvi = NDVI.exec(sat, 2, 3);
			Image pan = AverageChannels.exec(sat);
			Image sat2 = new ByteImage(sat.getXDim(), sat.getYDim(), 1, 1, sat
					.getBDim() + 2);
			sat2.setImage4D(sat.getImage4D(0, Image.B), 0, Image.B);
			sat2.setImage4D(sat.getImage4D(1, Image.B), 1, Image.B);
			sat2.setImage4D(sat.getImage4D(2, Image.B), 2, Image.B);
			sat2.setImage4D(sat.getImage4D(3, Image.B), 3, Image.B);
			sat2.setImage4D(ndvi.getImage4D(0, Image.B), 4, Image.B);
			sat2.setImage4D(pan.getImage4D(0, Image.B), 5, Image.B);
			return sat2;
		
		}

}