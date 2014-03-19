package fr.unistra.pelican.algorithms.segmentation.labels;

import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Order the labels of the images based on the region size
 * 
 * @author Lefevre
 */
public class ReorderLabelsBySize extends Algorithm {

	/**
	 * Input Image
	 */
	public Image inputImage;

	/**
	 * result
	 */
	public Image outputImage;

	/**
	 * (optional) flag to consider the largest region as background
	 */
	public boolean background = false;

	/**
	 * (optional) flag to work on 2D
	 */
	public boolean mode2D = true;

	/**
	 * Constructor
	 * 
	 */
	public ReorderLabelsBySize() {
		super.inputs = "inputImage";
		super.outputs = "outputImage";
		super.options = "background,mode2D";
	}

	public void launch() {

		outputImage = new IntegerImage(inputImage, false);

		if (mode2D) {

			for (int b = 0; b < inputImage.getBDim(); b++)
				for (int t = 0; t < inputImage.getTDim(); t++)
					for (int z = 0; z < inputImage.getZDim(); z++) {
						Image input = inputImage.getImage2D(z, t, b);

						int[] regionSize = RegionSize.exec(input);
						Couple[] tab = new Couple[regionSize.length];
						for (int tt = 0; tt < tab.length; tt++) {
							tab[tt] = new Couple(regionSize[tt], tt);
						}
						Arrays.sort(tab);
						int[] newLabels = new int[regionSize.length];
						for (int tt = 0; tt < tab.length; tt++)
							newLabels[tab[tt].y] = tab.length - 1 - tt;

						if (background)
							for (int tt = 1; tt < newLabels.length; tt++)
								newLabels[tt]--;

						Image output = input.copyImage(false);
						for (int i = 0; i < output.size(); i++)
							output.setPixelInt(i, newLabels[input.getPixelInt(i)]);

						outputImage.setImage2D(output, z, t, b);

					}
		} else {

			for (int b = 0; b < inputImage.getBDim(); b++) {
				Image input = inputImage.getImage4D(b, Image.B);

				int[] regionSize = RegionSize.exec(input);
				Couple[] tab = new Couple[regionSize.length];
				for (int tt = 0; tt < tab.length; tt++) {
					tab[tt] = new Couple(regionSize[tt], tt);
				}
				Arrays.sort(tab);
				int[] newLabels = new int[regionSize.length];
				for (int tt = 0; tt < tab.length; tt++)
					newLabels[tab[tt].y] = tab.length - 1 - tt;

				if (background)
					for (int tt = 1; tt < newLabels.length; tt++)
						newLabels[tt]--;

				Image output = input.copyImage(false);
				for (int i = 0; i < output.size(); i++)
					output.setPixelInt(i, newLabels[input.getPixelInt(i)]);

				outputImage.setImage4D(output, b, Image.B);

			}
		}
	}

	private class Couple implements Comparable<Object> {
		int x;
		int y;

		public Couple(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int compareTo(Object o) {
			Couple c = (Couple) o;
			return x - c.x;
		}
	}

	public static Image exec(Image inputImage) {
		return (Image) new ReorderLabelsBySize().process(inputImage);
	}

	public static Image exec(Image inputImage, boolean background) {
		return (Image) new ReorderLabelsBySize().process(inputImage, background);
	}

	public static Image exec(Image inputImage, boolean background, boolean mode2D) {
		return (Image) new ReorderLabelsBySize().process(inputImage, background,
			mode2D);
	}

}
