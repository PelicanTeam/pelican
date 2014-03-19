package fr.unistra.pelican.demos.experimental;

import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.applied.remotesensing.RegionBuilderClassificationConnexity;
import fr.unistra.pelican.algorithms.applied.remotesensing.RegionBuilderSoftClassificationWatershed;
import fr.unistra.pelican.algorithms.applied.remotesensing.RegionBuilderWatershedClassification;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.SamplesLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement;

public class RemoteSensingDemo {

	public static void main(String[] args) {
		String file = "./samples/remotesensing1";
		if(args.length > 0)
			file = args[0];
		
			// Load the image
			Image source;
			try {
				source = ImageLoader.exec("/home/derivaux/These/workspace/data/spot/CRVB.hdr");
			Image samples = SamplesLoader.exec("/home/derivaux/These/workspace/data/spot/espla");
			source.setColor(true);
			Viewer2D.exec(source, "source");
	
			
			FlatStructuringElement se3 = FlatStructuringElement.createSquareFlatStructuringElement(3);

			//source = GrayMedian.process(source, se3);

			
			// Create regions
			Image result = RegionBuilderWatershedClassification.exec(source, 0.05, samples);

			// View it
			Viewer2D.exec(DrawFrontiersOnImage.exec(source, FrontiersFromSegmentation.exec(result)), "RegionBuilderWatershedClassification of " + file);
			Viewer2D.exec(LabelsToColorByMeanValue.exec((IntegerImage)result, source), "RegionBuilderWatershedClassification of " + file);
			
			
			//FIXME: replace the default values 0.0, 0 added to ensure compatibility
			result = RegionBuilderClassificationConnexity.exec(LabelsToColorByMeanValue.exec((IntegerImage)RegionBuilderSoftClassificationWatershed.exec(source, samples, 0.20,0.0,0), source), samples);

			// View it
			Viewer2D.exec(DrawFrontiersOnImage.exec(source, FrontiersFromSegmentation.exec(result)), "RegionBuilderSoftClassificationWatershed of " + file);
			Viewer2D.exec(LabelsToColorByMeanValue.exec((IntegerImage)result, source), "RegionBuilderSoftClassificationWatershed of " + file);
		
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
