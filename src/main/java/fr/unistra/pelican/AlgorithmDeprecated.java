package fr.unistra.pelican;

import java.util.Vector;
 
  
/**
 * Base interface for all algorithms.
 * <p>An algorithm is a process that takes a vector of parameters and returns another 
 * vertor of parameters.</p>
 * 
 * <p>Use of an algorithm takes four steps:<br/>
 *  - First, creation of an algorithm's intance:<br/>
 *  eg. Algorithm algorithm = new fr.unistra.pelican.algorithms.arithmetic.Inversion; <br/>
 *  - Then, set the input parameters: <br/>
 *  algorithme.setInput(inputList); <br/>
 *  - Let the algorithme process the inputs: <br/>
 *  algorithme.lauch(); <br/>
 *  - Finally grab the output: <br/>
 *  Vector outputList = algorithm.getOutput() <br/>
 * </p> 
 *  
 * <p>For convenience the developers generally create a static method by e.g.:<br/>
 * Inversion.inversion(Image input) <br/>
 * Image result = fr.unistra.pelican.algorithms.arithmetic.Inversion.invert(inputImage);
 * </p> 
 * 
 * <p>The number of input and output parameters can be retrieved by using 
 * getInputTypes().length and getOutputTypes().length</p>
 *
 */
@Deprecated
public interface AlgorithmDeprecated {

	/** Launch the algorithm.
	 * You have to fill the input arguments with setInput before calling launch.
	 *
	 */
	public void launch()
			throws AlgorithmException;

	/**
	 * 
	 * @param v A vector containing the inputs.
	 * @throws InvalidNumberOfParametersException
	 * @throws InvalidTypeOfParameterException
	 */
	public void setInput(Vector v)
            throws InvalidNumberOfParametersException,InvalidTypeOfParameterException;

	/**
	 * @return A vector containing the outputs.
	 */
	public Vector getOutput();

	/**
	 * @return An string array containing the types of input parameters.
	 */
	public String[] getInputTypes();

	/**
	 * @return An string array containing the types of return parameters.
	 */
	public String[] getOutputTypes();

	/**
	 * 
	 * <p>The format of the help string is as follow :
	 * <pre>"Short description (less then 10 words).\n"
     *          +"first_input_type first_input_name\n"
     *          +"... more inputs ..."
     *          +"last_input_type last_input_name\n"
     *          +"\n"
     *          +"first_output_type first_output_name\n"
     *          +"... more outputs ..."
     *          +"last_output_type last_ouput_name\n"
     *          +"\n"
     *          +"Long description including references" 
     * </pre></p>
	 * 
	 * @return The description of the algorithm.
	 */
	public String help();

}
