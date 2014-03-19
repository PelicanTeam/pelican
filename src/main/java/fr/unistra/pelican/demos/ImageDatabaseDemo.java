package fr.unistra.pelican.demos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.granulometry.MorphologicalHistogram;

public class ImageDatabaseDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws PelicanException,IOException {
		String path="/home/lefevre/projets/oseo/demos/coil-20-proc-mm";
		File dir=new File(path+"/images");
		String[] fichiers=dir.list();
		for (int i=0;i<fichiers.length;i++) {
			String s=fichiers[i];
			s=s.substring(0,s.lastIndexOf('.'));
			System.out.println((100.0*i)/fichiers.length+" %");
			//if (s.indexOf(".png")==-1)
			//	continue;
			Image data=ImageLoader.exec(dir.toString()+"/"+s+".png");
			//Viewer2D.exec(data,"test");
			PrintStream out = new PrintStream(new FileOutputStream(path+"/features/"+s+".txt"));
			
			/*
			double curve[]=Histogram.process(data);
			for(int j1=0;j1<curve.length;j1++)
					out.println(curve[j1]+",");
			*/
			
			double curve[][]=(double[][]) new MorphologicalHistogram().process(data,10,MorphologicalHistogram.BOTH);
			for(int j1=0;j1<curve.length;j1++)
				for (int j2=0;j2<curve[j1].length;j2++)
					out.print(curve[j1][j2]+",");

		}
}

}
