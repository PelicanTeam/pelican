package fr.unistra.pelican.demos;

import java.awt.Point;
import java.util.Vector;

import fr.unistra.pelican.AlgorithmDeprecated;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.InvalidNumberOfParametersException;
import fr.unistra.pelican.InvalidTypeOfParameterException;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.morphology.gray.GrayDilation;
import fr.unistra.pelican.algorithms.morphology.gray.GrayErosion;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.CompositeStructuringElement;

public class GrayHitOrMissDemo {

        
        public static void main(String[] args) {
                try
                {
                        Image test = ImageLoader.exec("/home/jonathan/test_HMT.bmp");
                        Viewer2D.exec(test,"Image de base");
                        ValuedCompositeStructuringElement vcse = new ValuedCompositeStructuringElement(5,5,new Point(2,2),-0.5);
                        CompositeStructuringElement cse = new CompositeStructuringElement(5,5,new Point(2,2));
                        for(int i=0;i<5;i++)
                        {
                                for(int j=0;j<5;j++)
                                {
                                        if(i==0|i==4|j==0|j==4)
                                        {
                                                vcse.setValue(i,j,0);
                                                cse.setValue(i,j,0);
                                        }
                                        else
                                        {
                                                vcse.setValue(i,j,1);
                                                cse.setValue(i,j,1);
                                        }
                                }
                        }
//                      Image resultat = GrayHitOrMissRonse.process(test,vcse);
                        GrayHitOrMissRonse algoR = new GrayHitOrMissRonse();
                        Vector inputR = new Vector(2);
                        inputR.add(test);
                        inputR.add(vcse);
                        algoR.setInput(inputR);
                        algoR.launch();
                        Image resultat=(Image) algoR.getOutput().firstElement();
                        //Image resultat2 = GrayHitOrMissSoille.process(test,cse);
                        GrayHitOrMissSoille algoS = new GrayHitOrMissSoille();
                        Vector inputS = new Vector(2);
                        inputS.add(test);
                        inputS.add(cse);
                        algoS.setInput(inputS);
                        algoS.launch();
                        Image resultat2=(Image)algoS.getOutput().firstElement();
                                        Viewer2D.exec(resultat,"Ronse");
                        Viewer2D.exec(resultat2,"Soille");
                        
                } catch (PelicanException e)
                {
                        e.printStackTrace();
                }

        }

}

class ValuedCompositeStructuringElement extends CompositeStructuringElement {

    private double value;
    
    public ValuedCompositeStructuringElement (CompositeStructuringElement e, double value)
    {
            super(e);
            this.value=value;
    }
    
    public ValuedCompositeStructuringElement (int rows,int cols, double value)
    {
            super(rows,cols);
            this.value=value;
    }

    public ValuedCompositeStructuringElement (int rows, int cols, Point centre, double value)
    {
            super(rows,cols,centre);
            this.value=value;
    }
    
    public ValuedCompositeStructuringElement (int rows, int cols, Point centre,
                    int[] values, double value)
    {
            super(rows,cols,centre,values);
            this.value=value;
    }
    
    public double getValue() {
            return this.value;
    }
    
}

class GrayHitOrMissRonse implements AlgorithmDeprecated {
    // Inputs parameters
private Image inputImage;

    private ValuedCompositeStructuringElement cse;

    // Outputs parameters
private Image outputImage;

    
    
    public void launch() throws AlgorithmException {
            try{
            outputImage = inputImage.copyImage(false);
            BooleanImage seFG = cse.getForegroundStructuringElement();
            BooleanImage seBG = cse.getBackgroundStructuringElement();
            
            Image ErosionFG = GrayErosion.exec(inputImage,seFG);
            //Image DilatationBG = GrayDilatation.process(inputImage,seBG.getTranspose());
            Image DilatationBG = GrayDilation.exec(inputImage,seBG);
            
            for(int i=0;i<outputImage.size();i++)
            {
                    double diff =DilatationBG.getPixelDouble(i)-ErosionFG.getPixelDouble(i);
                    if(cse.getValue()<0)
                    {
                            if (diff<=cse.getValue())
                            {
                                    outputImage.setPixelDouble(i,ErosionFG.getPixelDouble(i));
                            }
                            else
                            {
                                    outputImage.setPixelDouble(i,0.0);
                            }
                    }
                    else
                    {
                            if (diff>=cse.getValue())
                            {
                                    outputImage.setPixelDouble(i,ErosionFG.getPixelDouble(i));
                            }
                            else
                            {
                                    outputImage.setPixelDouble(i,0.0);
                            }
                    }
            }
            
            }
            catch(Exception ex){
                    ex.printStackTrace();
            }
            
                    }

    /* (non-Javadoc)
     * @see fr.unistra.pelican.Algorithm#setInput(java.util.Vector)
     */
    public void setInput(Vector inputVector)
                    throws InvalidNumberOfParametersException,
                    InvalidTypeOfParameterException {
            // Check the number of parameters.
	if (inputVector.size() != 2)
                    throw new InvalidNumberOfParametersException("Need two parameter!");

    
            // Check types of each parameter.
	Object o = inputVector.get(0);
            if ((o instanceof fr.unistra.pelican.Image) == false)
                    throw new InvalidTypeOfParameterException(
                                    "Input param 1 need to be instance of fr.unistra.pelican.Image");
            // When type is checked, store the parameter.
	inputImage = (Image) o;
            
            o = inputVector.get(1);
            if ((o instanceof ValuedCompositeStructuringElement) == false)
                    throw new InvalidTypeOfParameterException(
                                    "Input param 2 need to be instance of fr.unistra.pelican.util.morpholgy.FlatStructuringElement");
            // When type is checked, store the parameter.
	cse = (ValuedCompositeStructuringElement) o; 

    }

    /* (non-Javadoc)
     * @see fr.unistra.pelican.Algorithm#getOutput()
     */
    public Vector getOutput() {
            Vector outputVector = new Vector(1);
            outputVector.add(outputImage);
            return outputVector;
    }

    /* (non-Javadoc)
     * @see fr.unistra.pelican.Algorithm#getInputTypes()
     */
    public String[] getInputTypes() {
            String[] tab = new String[2];
            tab[0] = "fr.unistra.pelican.Image";
            tab[1] = "fr.unistra.pelican.util.morpholgy.ValuedCompositeStructuringElement";
            return tab;
    }

    /* (non-Javadoc)
     * @see fr.unistra.pelican.Algorithm#getOutputTypes()
     */
    public String[] getOutputTypes() {
            String[] tab = new String[1];
            tab[0] = "fr.unistra.pelican.Image";
            return tab;
    }

    /* (non-Javadoc)
     * @see fr.unistra.pelican.Algorithm#help()
     */
    public String help() {
            return "Performs a gray hit or miss with Ronse's definition with a valued composite structuring element.\n"
                            + "fr.unistra.pelican.Image inputImage\n"
                            + "fr.unistra.pelican.util.morpholgy.ValuedCompositeStructuringElement vcse\n"
                            + "\n"
                            + "fr.unistra.pelican.Image outputImage\n"
                            + "\n"
                            + "Works on double precision.";
    }

    /** Static fonction that use this algorithm.
     * 
     * @param image
     * @param cse
     * @return result
     * @throws InvalidTypeOfParameterException 
     * @throws AlgorithmException 
     * @throws InvalidNumberOfParametersException 
     */
    /*public static Image process(Image image, ValuedCompositeStructuringElement se)
                    throws InvalidTypeOfParameterException, AlgorithmException,
                    InvalidNumberOfParametersException {
            GrayHitOrMissRonse algo = new GrayHitOrMissRonse();
            Vector inputs = new Vector(2);
            inputs.add(image);
            inputs.add(se);
            algo.setInput(inputs);
            algo.launch();
            return (Image) algo.getOutput().firstElement();
    }*/
}
    
    class GrayHitOrMissSoille implements AlgorithmDeprecated {
        // Inputs parameters
	private Image inputImage;

        private CompositeStructuringElement cse;

        // Outputs parameters
	private Image outputImage;

        
        
        public void launch() throws AlgorithmException {
                try{
                outputImage = inputImage.copyImage(false);
                BooleanImage seFG = cse.getForegroundStructuringElement();
                BooleanImage seBG = cse.getBackgroundStructuringElement();
                
                Image ErosionFG = GrayErosion.exec(inputImage,seFG);
                //Image DilatationBG = GrayDilatation.process(inputImage,seBG.getTranspose());
                Image DilatationBG = GrayDilation.exec(inputImage,seBG);
                for(int i=0;i<outputImage.size();i++)
                {
                        double diff = ErosionFG.getPixelDouble(i)-DilatationBG.getPixelDouble(i);
                        if (diff>0.)
                        {
                                outputImage.setPixelDouble(i,diff);
                        }
                        else
                        {
                                outputImage.setPixelDouble(i,0.);
                        }
                }
                }
                catch(Exception ex){
                        ex.printStackTrace();
                }
                
                        }

        /* (non-Javadoc)
         * @see fr.unistra.pelican.Algorithm#setInput(java.util.Vector)
         */
        public void setInput(Vector inputVector)
                        throws InvalidNumberOfParametersException,
                        InvalidTypeOfParameterException {
                // Check the number of parameters.
		if (inputVector.size() != 2)
                        throw new InvalidNumberOfParametersException("Need two parameter!");

        
                // Check types of each parameter.
		Object o = inputVector.get(0);
                if ((o instanceof fr.unistra.pelican.Image) == false)
                        throw new InvalidTypeOfParameterException(
                                        "Input param 1 need to be instance of fr.unistra.pelican.Image");
                // When type is checked, store the parameter.
		inputImage = (Image) o;
                
                o = inputVector.get(1);
                if ((o instanceof CompositeStructuringElement) == false)
                        throw new InvalidTypeOfParameterException(
                                        "Input param 2 need to be instance of fr.unistra.pelican.util.morpholgy.CompositeStructuringElement");
                // When type is checked, store the parameter.
		cse = (CompositeStructuringElement) o; 

        }

        /* (non-Javadoc)
         * @see fr.unistra.pelican.Algorithm#getOutput()
         */
        public Vector getOutput() {
                Vector outputVector = new Vector(1);
                outputVector.add(outputImage);
                return outputVector;
        }

        /* (non-Javadoc)
         * @see fr.unistra.pelican.Algorithm#getInputTypes()
         */
        public String[] getInputTypes() {
                String[] tab = new String[2];
                tab[0] = "fr.unistra.pelican.Image";
                tab[1] = "fr.unistra.pelican.util.morpholgy.CompositeStructuringElement";
                return tab;
        }

        /* (non-Javadoc)
         * @see fr.unistra.pelican.Algorithm#getOutputTypes()
         */
        public String[] getOutputTypes() {
                String[] tab = new String[1];
                tab[0] = "fr.unistra.pelican.Image";
                return tab;
        }

        /* (non-Javadoc)
         * @see fr.unistra.pelican.Algorithm#help()
         */
        public String help() {
                return "Performs a gray hit or miss with Soille's definition with a composite structuring element.\n"
                                + "fr.unistra.pelican.Image inputImage\n"
                                + "fr.unistra.pelican.util.morpholgy.CompositeStructuringElement cse\n"
                                + "\n"
                                + "fr.unistra.pelican.Image outputImage\n"
                                + "\n"
                                + "Works on double precision.";
        }

        /** Static fonction that use this algorithm.
         * 
         * @param image
         * @param cse
         * @return result
         * @throws InvalidTypeOfParameterException 
         * @throws AlgorithmException 
         * @throws InvalidNumberOfParametersException 
         */
        /*public static Image process(Image image, CompositeStructuringElement se)
                        throws InvalidTypeOfParameterException, AlgorithmException,
                        InvalidNumberOfParametersException {
                GrayHitOrMissSoille algo = new GrayHitOrMissSoille();
                Vector inputs = new Vector(2);
                inputs.add(image);
                inputs.add(se);
                algo.setInput(inputs);
                algo.launch();
                return (Image) algo.getOutput().firstElement();
        }*/
}

