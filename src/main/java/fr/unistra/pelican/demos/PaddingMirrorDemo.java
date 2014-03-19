/**
 * 
 */
package fr.unistra.pelican.demos;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.geometric.Padding;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

/**
 * So many Lenna!
 * @author Benjamin Perret
 *
 */
public class PaddingMirrorDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Image im = ImageLoader.exec("samples/lenna.png");
		Image res = Padding.exec(im,im.xdim*5,im.ydim*5,-1,-1,-1,Padding.MIRROR,im.xdim*2,im.ydim*2,0,0,0);
		Viewer2D.exec(res,"Lenna upside-down 25* !!!!");

	}

}
