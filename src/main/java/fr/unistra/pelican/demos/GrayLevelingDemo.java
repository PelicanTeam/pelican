package fr.unistra.pelican.demos;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayASF;
import fr.unistra.pelican.algorithms.morphology.gray.GrayLeveling;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class GrayLevelingDemo {

	public static void main(String[] args) {
		Image img = (Image) new ImageLoader().process("samples/lenna512.png");
		new Viewer2D().process(img, "input");
		BooleanImage se = FlatStructuringElement2D
				.createSquareFlatStructuringElement(5);
		Image marker = (Image) new GrayASF().process(img, se,
				GrayASF.OPENING_FIRST, new Integer(4));
		new Viewer2D().process(marker, "marker");
		img = (Image) new GrayLeveling().process(img, marker, new Integer(0));
		new Viewer2D().process(img, "leveling");
	}

}
