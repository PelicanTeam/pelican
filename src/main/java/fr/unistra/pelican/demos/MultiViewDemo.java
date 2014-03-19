package fr.unistra.pelican.demos;

import javax.swing.JFrame;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.MViewer;
import fr.unistra.pelican.gui.MultiViews.MultiView;

public class MultiViewDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		Image im=ImageLoader.exec("samples/AstronomicalImagesFITS/img1-12.fits");
		im.setName("img1-12");
		MultiView mv=MViewer.exec(im);
		
		im=ImageLoader.exec("samples/AstronomicalImagesFITS/img1-10.fits");
		im.setName("img1-10");
		mv.addImage(im);
		
		
		im=ImageLoader.exec("samples/lenna512.png");
		im.setName("Lenna");
		mv.addImage(im);
		
		im=ImageLoader.exec("samples/horse2.png");
		im.setName("Chwal");
		mv.addImage(im);
		
		im=ImageLoader.exec("samples/curious.png");
		im.setName("HumHum");
		mv.addImage(im);
		
		im=ImageLoader.exec("samples/blobs.png");
		im.setName("blop blop");
		mv.addImage(im);
		
		im=ImageLoader.exec("samples/camera.png");
		im.setName("Camocam");
		mv.addImage(im);
		
		im=ImageLoader.exec("samples/monsters.png");
		im.setName("Monsre & co");
		mv.addImage(im);
	}

}
