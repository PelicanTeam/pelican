package fr.unistra.pelican.demos.applied.remotesensing;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.geometric.Padding;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ROILoader;
import fr.unistra.pelican.algorithms.morphology.vectorial.VectorialDMP;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaClassification5NN;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaClassificationNaiveBayes;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationEM;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationKmeans;
import fr.unistra.pelican.algorithms.statistics.PCA;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.vectorial.orders.*;

public class HyperspectralRemoteSensingDemo {

	private static final int wekaSegmentationKMeans = 0;
	private static final int wekaSegmentationEM = 1;
	private static final int wekaClassification5NN = 2;
	private static final int wekaClassificationNaiveBayes = 3;
	public static String path = "/home/miv/lefevre/data/teledetection/hyper/";
	public static String filename = "extr4.hdr";
	public static String samples = "extr4_ROI.txt";
	
	public static void main (String [] args) {
		if (args.length==0) {
			System.out.println("HRSD Usage : path (with /) + filename + samples");
		}
		else if (args.length==3){
			path=args[0];
			filename=args[1];
			samples=args[2];
		}
		
		Image input = ImageLoader.exec(path+filename);
		Image learning=ROILoader.exec(path+samples);
		//Viewer2D.exec(learning,"samples");

		int bands=5;
		Image pca=PCA.exec(input);
		pca=Padding.exec(pca,-1,-1,-1,-1,bands,Padding.NULL);
		Viewer2D.exec(ContrastStretch.exec(pca),"samples");

		input=ContrastStretch.exec(pca);
		VectorialOrdering voMarg = null;
		VectorialOrdering voNorm = new NormBasedOrdering();
		VectorialOrdering voLex = new LexicographicalOrdering(input.getBDim());
				
		Image dmpMarg=VectorialDMP.exec(input,voMarg,3,false,true,true,true,true);
		Viewer2D.exec(ContrastStretch.exec(dmpMarg),"dmp marg");

		Image dmpNorm=VectorialDMP.exec(input,voNorm,3,false,true,true,true,true);
		Viewer2D.exec(ContrastStretch.exec(dmpNorm),"dmp norm");

		Image dmpLex=VectorialDMP.exec(input,voLex,3,false,true,true,true,true);
		Viewer2D.exec(ContrastStretch.exec(dmpLex),"dmp lex");

		//int method=wekaSegmentationKMeans;
		//int method=wekaSegmentationEM;
		//int method=wekaClassification5NN;
		int method=wekaClassificationNaiveBayes;
		
		classification (input,learning,12,method,true,"input");
		classification (dmpMarg,learning,12,method,true,"dmp marg");
		classification (dmpNorm,learning,12,method,true,"dmp norm");		
		classification (dmpLex,learning,12,method,true,"dmp lex");
	}

	
	public static Image classification(Image input,Image learning,int nbclasses,int method,boolean view, String text) {
		Image result=null;
		switch (method) {
		case wekaSegmentationKMeans:
			result=WekaSegmentationKmeans.exec(input,nbclasses);
		case wekaSegmentationEM:
			result=WekaSegmentationEM.exec(input,nbclasses);
		case wekaClassification5NN: 
			result=WekaClassification5NN.exec(input,learning);
		case wekaClassificationNaiveBayes:
			result=WekaClassificationNaiveBayes.exec(input,learning);
		}
		if (view)
			Viewer2D.exec(LabelsToRandomColors.exec(result),text);
		System.out.println(text+" performed");
		return result;
	}	
	
}