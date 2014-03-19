package fr.unistra.pelican.algorithms.applied.remotesensing;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.algorithms.arithmetic.AdditionConstantChecked;
import fr.unistra.pelican.algorithms.arithmetic.DeleteSmallValues;
import fr.unistra.pelican.algorithms.arithmetic.EuclideanNorm;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayGradient;
import fr.unistra.pelican.algorithms.morphology.gray.geodesic.FastGrayReconstruction;
import fr.unistra.pelican.algorithms.segmentation.Watershed;
import fr.unistra.pelican.algorithms.segmentation.flatzones.BooleanConnectedComponentsLabeling;
import fr.unistra.pelican.algorithms.segmentation.labels.DeleteFrontiers;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;


/** 
 * Create regions using the watershed algorithm on  original image.
 * Settings are :
 * - hmin : threshold value of the gradient image to limit oversegmentation
 * - dynamics  min dynamics to be taken in account.
 * - min spatial size to create a bassin.
 * 
 * @author Sebastien Derivaux
 */
public class RegionBuilderWatershedClassical extends Algorithm {
	
	// Inputs parameters
	public Image inputImage;
	public double hmin;
	public double dynamics;
	public int spatial;
	
	// Outputs parameters
	public Image outputImage;

	
	/**
  	 * Constructor
  	 *
  	 */
	public RegionBuilderWatershedClassical() {		
		
		super();		
		super.inputs = "inputImage,hmin,dynamics,spatial";		
		super.outputs = "outputImage";		
		
	}
	
	/* (non-Javadoc)
	 * @see fr.unistra.pelican.Algorithm#launch()
	 */
	public void launch() throws AlgorithmException {
		BooleanImage se3 = FlatStructuringElement2D.createSquareFlatStructuringElement(3);

		Image work;
		try {
			
			// Compute the gradient on each band 
			work = (Image) new GrayGradient().process(inputImage, se3);
			//work = ContrastStretchEachBands.process(work);
			
			Image process = (Image) new EuclideanNorm().process(work);
			// Compute the euclidian distance of the gradient
			work = process;
			//Viewer2D.exec(work, "RegionBuilderWatershedClassical");

			Object process2 = new AdditionConstantChecked().process(work, 0.0);
			work = (Image) process2;
			//work = ContrastStretch.process(work);
			//Viewer2D.exec(work, "RegionBuilderWatershedClassical");
			
			// Reduce the gradient image with hmin to remove useless local
			// minima
			if(hmin > 0.0)
				work = (Image) new DeleteSmallValues().process(work, hmin);

			
			// reduce catchement bassins with low dynamic
			if(dynamics > 0.0 ) {
				Image upper = (Image) new AdditionConstantChecked().process(work, dynamics);
	//			work = (Image) new GrayReconstructionByErosion().process(upper, work, se3);			
				work = (Image) new FastGrayReconstruction().process(upper, work , BooleanConnectedComponentsLabeling.CONNEXITY8, true);
			}
			
			// reduce catchement bassins with low surface
			if(spatial > 0) {
				BooleanImage sufFilter = FlatStructuringElement2D.createSquareFlatStructuringElement(spatial);
				Image dil = (Image) new GrayDilation().process(work,  sufFilter);
				work = (Image) new FastGrayReconstruction().process(dil, work , BooleanConnectedComponentsLabeling.CONNEXITY8, true);
			}

			// Process a watershed transformation
			work = (Image) new Watershed().process(work);
			

			work = (Image) new DeleteFrontiers().process(work);
			
			outputImage = work;
			
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
	
	/** 
	 * Create regions using the watershed algorithm on  original image.
	 */
	public static Image exec(Image image) throws InvalidTypeOfParameterException, AlgorithmException, InvalidNumberOfParametersException {
		return (Image)new RegionBuilderWatershedClassical().process(image, 0.0, 0.0, 0);		
	}
	
	/** 
	 * Create regions using the watershed algorithm on  original image.
	 * Settings are :
	 * @param hmin threshold value of the gradient image to limit oversegmentation
	 */
	public static Image exec(Image image, double hmin) throws InvalidTypeOfParameterException, AlgorithmException, InvalidNumberOfParametersException {
		return (Image)new RegionBuilderWatershedClassical().process(image, hmin, 0.0, 0);		
	}
	
	/** 
	 * Create regions using the watershed algorithm on  original image.
	 * Settings are :
	 * @param hmin threshold value of the gradient image to limit oversegmentation
	 * @param dynamics  min dynamics to be taken in account.
	 */
	public static Image exec(Image image, double hmin, double dynamic) throws InvalidTypeOfParameterException, AlgorithmException, InvalidNumberOfParametersException {
		return (Image)new RegionBuilderWatershedClassical().process(image, hmin, dynamic, 0);		
	}
	
	/** 
	 * Create regions using the watershed algorithm on  original image.
	 * Settings are :
	 * @param dynamics  min dynamics to be taken in account.
	 * @param min spatial size to create a bassin.
	 * 
	 */
	public static Image exec(Image image, double dynamic, int spatial) throws InvalidTypeOfParameterException, AlgorithmException, InvalidNumberOfParametersException {
		return (Image)new RegionBuilderWatershedClassical().process(image, 0, dynamic, spatial);		
	}
	
	/** 
	 * Create regions using the watershed algorithm on  original image.
	 * Settings are :
	 * @param hmin threshold value of the gradient image to limit oversegmentation
	 * @param dynamics  min dynamics to be taken in account.
	 * @param min spatial size to create a bassin.
	 * 
	 */
	public static Image exec(Image image, double hmin, double dynamic, int spatial) throws InvalidTypeOfParameterException, AlgorithmException, InvalidNumberOfParametersException {
		return (Image)new RegionBuilderWatershedClassical().process(image, hmin, dynamic, spatial);		
	}
	
//	public static void main(String[] args) {
//		String file = "./samples/remotesensing1";
//		
//		if(args.length > 0)
//			file = args[0];
//		
//		
//		try {
//			// Load the image
//			Image source = (Image) new ImageLoader().process(file + ".png");
//			Image regions = (Image) new RegionsLoader().process(file);
//			
//			System.out.println("RegionBuilderWatershedClassical of " + file);
//
//			// Create regions
//			Image result = (Image) new RegionBuilderWatershedClassical().process(source, 0.0, 0.0, 0);
//			
//			// Easy filter
//		/*	FlatStructuringElement labelFilter = FlatStructuringElement.createSquareFlatStructuringElement(5);
//			result = FilteringLabels.process(result, labelFilter);
//*/
//			// View it
//			new Viewer2D().process(new DrawFrontiersOnImage().process(source, new FrontiersFromSegmentation().process(result)), "RegionBuilderWatershedClassical of " + file);
//			//Viewer2D.exec(LabelsToColorByMeanValue.process(result, source), "RegionBuilderWatershedClassical of " + file);
//		//	System.out.println(EvalSegmentation.process(result, regions));
//
//			
//			result = (Image)new RegionBuilderWatershedClassical().process(source, 0.0, 0.15, 0);
//			
//			// Easy filter
//		/*	FlatStructuringElement labelFilter = FlatStructuringElement.createSquareFlatStructuringElement(5);
//			result = FilteringLabels.process(result, labelFilter);
//*/
//			// View it
//			new Viewer2D().process(new DrawFrontiersOnImage().process(source,new  FrontiersFromSegmentation().process(result)), "RegionBuilderWatershedClassical of " + file);
//			//Viewer2D.exec(LabelsToColorByMeanValue.process(result, source), "RegionBuilderWatershedClassical of " + file);
//		//	System.out.println(EvalSegmentation.process(result, regions));
//
//			
//		} catch (InvalidTypeOfParameterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (AlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidNumberOfParametersException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
