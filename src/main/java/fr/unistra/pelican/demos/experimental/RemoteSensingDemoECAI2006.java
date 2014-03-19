package fr.unistra.pelican.demos.experimental;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.arithmetic.KFolds;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaClassification5NN;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSegmentationKmeans;
import fr.unistra.pelican.algorithms.segmentation.weka.WekaSoftClassification5NN;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement;

public class RemoteSensingDemoECAI2006 {

	public static void main(String[] args) {
			String file = "/home/derivaux/These/workspace/data/spot/espla";
			if(args.length > 0)
				file = args[0];
			FlatStructuringElement se3 = FlatStructuringElement.createSquareFlatStructuringElement(5);
			FlatStructuringElement labelFilter = FlatStructuringElement.createSquareFlatStructuringElement(3);

			
			try {
				
				
				// Load the image
				Image source = ImageLoader.exec(file + ".hdr");
				Image[] samples = KFolds.exec(SamplesLoader.exec(file), 2);
			
				Image samples1 = samples[0];
				Image samples2 = samples[1];
				
				// Classif de base
				Viewer2D.exec(WekaClassification5NN.exec(source, samples2), "Classif de base");
				
				// Classif dure puis classif de base
		/*		Image t1 = WekaClassification5NN.process(source, samples1);
				t1 = SegmentByConnexity.process(t1);
				t1 = FilteringLabels.process(t1, labelFilter);
				t1 = LabelsToColorByMeanValue.process(t1, source);
				Viewer2D.exec(WekaClassification5NN.process(t1, samples2), "Classif dure puis classif de base");
			*/	
	/*			// Clustering puis classif de base
				Image t2 = WekaSegmentationKmeans.process(source, 10);
				t2 = SegmentByConnexity.process(t2);
		//		t2 = FilteringLabels.process(t2, labelFilter);
				t2 = LabelsToColorByMeanValue.process(t2, source);
				Viewer2D.exec(WekaClassification5NN.process(t2, samples2), "Clustering puis classif de base");
				*/
				// Classif floue puis clustering puis classif de base
				Image probas = WekaSoftClassification5NN.exec(source, samples1);
				Image t3 = WekaSegmentationKmeans.exec(probas, 25);
//				t3 = SegmentByConnexity.process(t3);
//				t3 = FilteringLabels.process(t3, labelFilter);
				t3 = LabelsToColorByMeanValue.exec((IntegerImage)t3, source);
				Viewer2D.exec(WekaClassification5NN.exec(t3, samples2), "Classif floue puis clustering puis classif de base");
				

				
			} catch (InvalidTypeOfParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidNumberOfParametersException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
	

	}
}
