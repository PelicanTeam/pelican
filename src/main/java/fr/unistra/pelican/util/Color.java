package fr.unistra.pelican.util;

import java.awt.color.ColorSpace;

/**
 * Wrapper class for java.awt.Color
 * @author lefevre
 *
 */

public 	class Color extends java.awt.Color {

	public Color(ColorSpace cspace, float[] components, float alpha) {
		super(cspace, components, alpha);
	}

	public Color(float r, float g, float b, float a) {
		super(r, g, b, a);
	}

	public Color(float r, float g, float b) {
		super(r, g, b);
	}

	public Color(int rgba, boolean hasalpha) {
		super(rgba, hasalpha);
	}

	public Color(int r, int g, int b, int a) {
		super(r, g, b, a);
	}

	public Color(int r, int g, int b) {
		super(r, g, b);
	}

	public Color(int rgb) {
		super(rgb);
	}

			
	
	}

