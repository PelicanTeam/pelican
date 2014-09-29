package fr.unistra.pelican.util;

import java.util.Comparator;
import java.util.List;


import fr.unistra.pelican.util.data.Corner;

public class CornerComparator implements Comparator<Corner>{
	private List<Corner> corners;
	
	public CornerComparator(List<Corner> corners){
		this.corners = corners;
	}
	@Override
	public int compare(Corner c0, Corner c1) {
		double d0 = c0.getH();
		double d1 = c1.getH();
		return Double.compare(d1, d0);
	}
	
}
