package fr.unistra.pelican.util;

import java.awt.Color;
import java.awt.Point;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;


/**
 * Classe représentant un rectangle, permet de dessiner dans une image Pelican
 * 
 * @author BenSemoule
 * 
 */
public class Box {
	public Point x1, x2;

	public Box()
	{
		this(0,0,1,1);
	}
	
	/**
	 * Construit un rectangle
	 * 
	 * @param x1
	 *            Coin supérieur gauche
	 * @param x2
	 *            Coin inférieur droit
	 */
	public Box(Point x1, Point x2) {
		this(x1.x, x1.y, x2.x, x2.y);
	}

	/**
	 * Construit un rectangle
	 * 
	 * @param x1
	 *            Ordonnée du Coin supérieur gauche
	 * @param y1
	 *            Abscice du Coin supérieur gauche
	 * @param x2
	 *            Ordonnée du Coin inférieur droit
	 * @param y2
	 *            Abscice du Coin inférieur droit
	 */
	public Box(int x1, int y1, int x2, int y2) {
		this.x1 = new Point(x1, y1);
		this.x2 = new Point(x2, y2);
	}

	public void drawCenter(Image im)
	{
		int x=(x1.x+x2.x)/2;
		int y=(x1.y+x2.y)/2;
		im.setPixelXYBoolean(x,y,true);
		im.setPixelXYBoolean(x+1,y,true);
		im.setPixelXYBoolean(x+1,y+1,true);
		im.setPixelXYBoolean(x,y+1,true);
		im.setPixelXYBoolean(x-1,y+1,true);
		im.setPixelXYBoolean(x-1,y,true);
		im.setPixelXYBoolean(x-1,y-1,true);
		im.setPixelXYBoolean(x,y-0,true);
		im.setPixelXYBoolean(x+1,y-1,true);
	}
	
	/**
	 * Dessine un rectangle en blanc dans une image monobande
	 * 
	 * @param im
	 *            Image de destination
	 */
	public void drawGrayRectangle(Image im) {
		Point x3 = new Point(x1.x, x2.y);
		Point x4 = new Point(x2.x, x1.y);
		Line c1 = new Line(x1, x3);
		Line c2 = new Line(x3, x2);
		Line c3 = new Line(x2, x4);
		Line c4 = new Line(x4, x1);
		c1.drawGrayLine(im);
		c2.drawGrayLine(im);
		c3.drawGrayLine(im);
		c4.drawGrayLine(im);
	}

	/**
	 * Dessine un rectangle en couleur dans une image à 3 bandes
	 * 
	 * @param im
	 *            Image de destination
	 * @param c
	 *            Couleur de dessin
	 */
	public void drawColorRectangle(Image im, Color c) {
		Point x3 = new Point(x1.x, x2.y);
		Point x4 = new Point(x2.x, x1.y);
		Line c1 = new Line(x1, x3);
		Line c2 = new Line(x3, x2);
		Line c3 = new Line(x2, x4);
		Line c4 = new Line(x4, x1);
		c1.drawColorLine(im, c);
		c2.drawColorLine(im, c);
		c3.drawColorLine(im, c);
		c4.drawColorLine(im, c);
	}

	public String toString()
	{
		return "Box: " + x1 + "*" + x2;
	}
}
