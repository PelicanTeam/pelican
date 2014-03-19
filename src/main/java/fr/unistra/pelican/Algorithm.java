package fr.unistra.pelican;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Assert;

/**
 * 
 * @author sollier,lefevre
 * 
 *         Abstract base class for all algorithms.
 *         <p>
 *         An algorithm is a process that takes a list of parameters and returns
 *         another list of parameters.
 *         </p>
 * 
 *         <p>
 *         Use of an algorithm takes four steps:<br/>
 *         - First, creation of an algorithm's intance:<br/>
 *         eg. Algorithm algorithm = new
 *         fr.unistra.pelican.algorithms.arithmetic.Inversion; <br/>
 *         - Then, set the input parameters: <br/>
 *         algorithm.setInput(inputList); <br/>
 *         - Let the algorithm process the inputs: <br/>
 *         algorithm.launch(); <br/>
 *         - Finally grab the output: <br/>
 *         ArrayList outputList = algorithm.getOutput() <br/>
 *         </p>
 *         *
 * 
 *         <p>
 *         The number of input and output parameters can be retrieved by using
 *         getInputTypes().length and getOutputTypes().length
 *         </p>
 * 
 */

public abstract class Algorithm {

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * List containing the output objects
	 */
	private ArrayList<Object> output = new ArrayList<Object>();

	/**
	 * List containing the input objects
	 */
	private ArrayList<Object> input = new ArrayList<Object>();

	/**
	 * List containing the option objects
	 */
	private ArrayList<Object> option = new ArrayList<Object>();

	/**
	 * String containing the description of the extended algorithm
	 */
	public String help;
	

	/**
	 * String containing the name of the input attributes of the extended
	 * algorithm
	 */
	public String inputs = "";

	/**
	 * String containing the name of the option attributes of the extended
	 * algorithm
	 */
	public String options = "";

	/**
	 * String containing the name of the input attributes of the extended
	 * algorithm
	 */
	public String outputs = "";

	/**
	 * String containing the suffix of inputs for test cases
	 */
	private final static String INPUT_SUFFIX = ".inputs";

	/**
	 * String containing the suffix of outputs for test cases
	 */
	private final static String OUTPUT_SUFFIX = ".outputs";

	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	/**
	 * Abstract method to implement in each inherited algorithm.
	 * 
	 * @throws AlgorithmException
	 */
	public abstract void launch() throws AlgorithmException;

	/**
	 * 
	 * Grab the input attributes of the inherited algorithms and set them into
	 * the input ArrayList
	 * 
	 * @param inputList
	 *            list of the input attributes
	 * @throws InvalidNumberOfParametersException
	 * @throws InvalidTypeOfParameterException
	 */
	public void setInput(ArrayList<Object> inputList)
			throws InvalidNumberOfParametersException,
			InvalidTypeOfParameterException {
		// Cleaning input and output parameters
		input.clear();
		option.clear();

		// parsing
		ArrayList<String> inputResult = parser(inputs);
		ArrayList<String> optionResult = parser(options);

		// Checking if the number of given parameters is at least
		// the number of mandatory parameters and less than the full set of
		// parameters
		if (inputList.size() < inputResult.size()
				|| inputList.size() > inputResult.size() + optionResult.size())
			throw new InvalidNumberOfParametersException(
					"Number of parameters is incorrect : " + inputList.size()
							+ " instead of [" + inputResult.size() + ","
							+ (inputResult.size() + optionResult.size()) + "]");

		Class c = this.getClass();
		Field f = null;
		Object o;

		// Mandatory input parameters
		for (int i = 0; i < inputResult.size(); i++) {

			o = inputList.get(i);
			try {
				f = c.getDeclaredField(inputResult.get(i));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				System.out.println("Input #" + i + " " + inputResult.get(i)
						+ " does not exist in " + c);
				e.printStackTrace();
			}
			Class t = f.getType();

			/*
			 * Class oc = o.getClass(); if (t.isPrimitive()) t =
			 * primitiveToObject(t);
			 */

			input.add(o);
			try {
				f.set(this, input.get(i));// Set the attributes of the
				// inherited algorithm
			} catch (IllegalArgumentException e) {
				// If the type is incorrect
				throw new InvalidTypeOfParameterException("Input type #" + i
						+ " is not correct : " + o.getClass().getName()
						+ " instead of " + t.getName());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		// Optional input parameters
		int nbOptions = Math.min(inputList.size() - inputResult.size(),
				optionResult.size());
		int indOptions = inputResult.size();
		for (int i = 0; i < nbOptions; i++) {

			o = inputList.get(i + indOptions);

			// If null option, skip it
			if (o == null) {
				option.add(o);
				continue;
			}

			try {
				f = c.getDeclaredField(optionResult.get(i));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				System.out.println("Option #" + i + " " + optionResult.get(i)
						+ " does not exist in " + c);
				e.printStackTrace();
			}
			Class t = f.getType();

			option.add(o);
			try {
				f.set(this, option.get(i)); // Set the attributes of the
				// inherited algorithm
			} catch (IllegalArgumentException e) {
				throw new InvalidTypeOfParameterException("Option type #" + i
						+ " is not correct : " + o.getClass() + " instead of "
						+ t);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method is used to return the list containing the outputs.
	 * 
	 * @return the list containing the outputs
	 */


	public ArrayList getOutput() {



		// parsing
		ArrayList<String> parserResult = parser(outputs);

		// clear output
		output.clear();

		Class c = this.getClass();
		Field f = null;
		Object o = null;

		for (int i = 0; i < parserResult.size(); i++) {

			try {
				f = c.getDeclaredField(parserResult.get(i));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				System.out.println("Output #" + i + " " + parserResult.get(i)
						+ " does not exist in " + c);
				e.printStackTrace();
			}
			try {
				o = f.get(this);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			output.add(o);

		}
		return output;
	}

	/**
	 * This method is used to return an array containing the types of each input
	 * attribute.
	 * 
	 * @return An array containing the types of each input attribute
	 */
	public Class[] getInputTypes() {

		ArrayList<String> parserResult = parser(inputs);

		Class[] tab = new Class[parserResult.size()];
		Class c = this.getClass();
		Field f = null;

		for (int i = 0; i < parserResult.size(); i++) {
			try {
				f = c.getDeclaredField(parserResult.get(i));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}

			if (f.getType() == int.class) {
				tab[i] = Integer.class;
			} else {
				if (f.getType() == char.class) {
					tab[i] = Character.class;
				} else {
					if (f.getType() == byte.class) {
						tab[i] = Byte.class;
					} else {
						if (f.getType() == short.class) {
							tab[i] = Short.class;
						} else {
							if (f.getType() == long.class) {
								tab[i] = Long.class;
							} else {
								if (f.getType() == float.class) {
									tab[i] = Float.class;
								} else {
									if (f.getType() == double.class) {
										tab[i] = Double.class;
									} else {
										if (f.getType() == boolean.class) {
											tab[i] = Boolean.class;
										} else {
											if (f.getType() == void.class) {
												tab[i] = Void.class;
											} else {
												tab[i] = f.getType();
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return tab;
	}

	/**
	 * This method is used to return an array containing the types of each
	 * option attribute.
	 * 
	 * @return An array containing the types of each option attribute
	 */
	public Class[] getOptionTypes() {

		ArrayList<String> parserResult = parser(options);

		Class[] tab = new Class[parserResult.size()];
		Class c = this.getClass();
		Field f = null;

		for (int i = 0; i < parserResult.size(); i++) {
			try {
				f = c.getDeclaredField(parserResult.get(i));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
			if (f.getType() == int.class) {
				tab[i] = Integer.class;
			} else {
				if (f.getType() == char.class) {
					tab[i] = Character.class;
				} else {
					if (f.getType() == byte.class) {
						tab[i] = Byte.class;
					} else {
						if (f.getType() == short.class) {
							tab[i] = Short.class;
						} else {
							if (f.getType() == long.class) {
								tab[i] = Long.class;
							} else {
								if (f.getType() == float.class) {
									tab[i] = Float.class;
								} else {
									if (f.getType() == double.class) {
										tab[i] = Double.class;
									} else {
										if (f.getType() == boolean.class) {
											tab[i] = Boolean.class;
										} else {
											if (f.getType() == void.class) {
												tab[i] = Void.class;
											} else {
												if (f.getType() == int[].class) {
													tab[i] = Integer[].class;
												} else {
													if (f.getType() == char[].class) {
														tab[i] = Character[].class;
													} else {
														if (f.getType() == byte[].class) {
															tab[i] = Byte[].class;
														} else {
															if (f.getType() == short[].class) {
																tab[i] = Short[].class;
															} else {
																if (f.getType() == long[].class) {
																	tab[i] = Long[].class;
																} else {
																	if (f
																			.getType() == float[].class) {
																		tab[i] = Float[].class;
																	} else {
																		if (f
																				.getType() == double[].class) {
																			tab[i] = Double[].class;
																		} else {
																			if (f
																					.getType() == boolean[].class) {
																				tab[i] = Boolean[].class;
																			} else {
																				tab[i] = f
																						.getType();
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return tab;
	}

	/**
	 * This method is used to return an array containing the types of each
	 * output attribute.
	 * 
	 * @return An array containing the types of each output attribute
	 */
	public Class[] getOutputTypes() {

		ArrayList<String> parserResult = parser(outputs);

		Class[] tab = new Class[parserResult.size()];
		Class c = this.getClass();
		Field f = null;

		for (int i = 0; i < parserResult.size(); i++) {
			try {
				f = c.getDeclaredField(parserResult.get(i));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
			if (f.getType() == int.class) {
				tab[i] = Integer.class;
			} else {
				if (f.getType() == char.class) {
					tab[i] = Character.class;
				} else {
					if (f.getType() == byte.class) {
						tab[i] = Byte.class;
					} else {
						if (f.getType() == short.class) {
							tab[i] = Short.class;
						} else {
							if (f.getType() == long.class) {
								tab[i] = Long.class;
							} else {
								if (f.getType() == float.class) {
									tab[i] = Float.class;
								} else {
									if (f.getType() == double.class) {
										tab[i] = Double.class;
									} else {
										if (f.getType() == boolean.class) {
											tab[i] = Boolean.class;
										} else {
											if (f.getType() == void.class) {
												tab[i] = Void.class;
											} else {
												if (f.getType() == int[].class) {
													tab[i] = Integer[].class;
												} else {
													if (f.getType() == char[].class) {
														tab[i] = Character[].class;
													} else {
														if (f.getType() == byte[].class) {
															tab[i] = Byte[].class;
														} else {
															if (f.getType() == short[].class) {
																tab[i] = Short[].class;
															} else {
																if (f.getType() == long[].class) {
																	tab[i] = Long[].class;
																} else {
																	if (f
																			.getType() == float[].class) {
																		tab[i] = Float[].class;
																	} else {
																		if (f
																				.getType() == double[].class) {
																			tab[i] = Double[].class;
																		} else {
																			if (f
																					.getType() == boolean[].class) {
																				tab[i] = Boolean[].class;
																			} else {
																				tab[i] = f
																						.getType();
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

		}
		return tab;
	}

	/**
	 * This method is used to return the description of the extended algorithm.
	 * 
	 * @return The description of the extended algorithm
	 */
	public String help() {
		return help;
	}

	/**
	 * This method parse the input string containing the names of the inherited
	 * attributes.
	 * 
	 * @param string
	 *            input string containing the names of the inherited attributes.
	 * @return A list of string where each element is a names of an inherited
	 *         attribute.
	 */
	private ArrayList<String> parser(String string) {

		int beg = 0;
		ArrayList<String> list = new ArrayList<String>();

		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == ',') {
				list.add(string.substring(beg, i));
				beg = i + 1;
			}
			if (i == (string.length() - 1)) {
				list.add(string.substring(beg, i + 1));
				beg = i + 1;
			}
		}
		return list;
	}

	/*
	 * private Class primitiveToObject(Class c) { if (c==Boolean.TYPE) return
	 * Boolean.class; else if (c==Byte.TYPE) return Byte.class; else if
	 * (c==Character.TYPE) return Character.class; else if (c==Short.TYPE)
	 * return Short.class; else if (c==Integer.TYPE) return Integer.class; else
	 * if (c==Long.TYPE) return Long.class; else if (c==Float.TYPE) return
	 * Float.class; else if (c==Double.TYPE) return Double.class; return null; }
	 */

	/**
	 * This method is used to return both the descrition of the algorithm and
	 * the attributes.
	 */
	public String toString() {

		Class c = this.getClass();

		String result = "" + c.toString() + ": " + help + "\n" + "inputs: "
				+ inputs + "\n" + "options: " + options + "\n" + "outputs: "
				+ outputs + "\n";

		return result;
	}

	/**
	 * This method is used to return the list containing the input names.
	 * 
	 * @return the list containing the input names.
	 */
	public ArrayList<String> getInputNames() {
		return parser(inputs);
	}

	/**
	 * This method is used to return the list containing the option names.
	 * 
	 * @return the list containing the option names.
	 */
	public ArrayList<String> getOptionNames() {
		return parser(options);
	}

	/**
	 * This method is used to return the list containing the output names.
	 * 
	 * @return the list containing the output names.
	 */
	public ArrayList<String> getOutputNames() {
		return parser(outputs);
	}

	/**
	 * This method is used to return the result fo the inherited algorithm.
	 * 
	 * @param parameters
	 *            the parameters to process
	 * 
	 * @return the result fo the inherited algorithm
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public Object process(Object... parameters)
			throws InvalidTypeOfParameterException, AlgorithmException,
			InvalidNumberOfParametersException {

		ArrayList<Object> inputs = new ArrayList<Object>();
		for (int i = 0; i < parameters.length; i++) {
			inputs.add(parameters[i]);
		}
		this.setInput(inputs);
		this.launch();
		if (this.getOutput() == null || this.getOutput().isEmpty()) {
			return null;
		} else {
			return (Object) this.getOutput().get(0);
		}
	}

	/**
	 * This method is used to return An arraylist containing all the outputs of
	 * the inherited algorithm.
	 * 
	 * @param parameters
	 *            the parameters to process
	 * 
	 * @return An arraylist containing all the outtputs of the inherited
	 *         algorithm
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public ArrayList<Object> processAll(Object... parameters)
			throws InvalidTypeOfParameterException, AlgorithmException,
			InvalidNumberOfParametersException {

		ArrayList<Object> inputs = new ArrayList<Object>();
		for (int i = 0; i < parameters.length; i++) {
			inputs.add(parameters[i]);
		}
		this.setInput(inputs);
		this.launch();
		if (this.getOutput() == null) {
			return null;
		} else {
			return this.getOutput();
		}
	}

	/**
	 * This method is used to return the number x element from the list of
	 * output of the inherited algorithm.
	 * 
	 * @param x
	 * @param parameters
	 *            the parameters to process
	 * 
	 * @return An arraylist containing all the outputs of the inherited
	 *         algorithm
	 * @throws InvalidTypeOfParameterException
	 * @throws AlgorithmException
	 * @throws InvalidNumberOfParametersException
	 */
	public Object processOne(Integer x, Object... parameters)
			throws InvalidTypeOfParameterException, AlgorithmException,
			InvalidNumberOfParametersException {

		ArrayList<Object> inputs = new ArrayList<Object>();
		for (int i = 0; i < parameters.length; i++) {
			inputs.add(parameters[i]);
		}
		this.setInput(inputs);
		this.launch();
		if (this.getOutput() == null || x < 0 || x >= this.getOutput().size())
			throw new InvalidNumberOfParametersException("output " + x
					+ " does not exist");
		return this.getOutput().get(x);

	}

	/**
	 * Build a default test case, i.e. perform algorithm and store
	 * inputs/outputs in files algorithmName.0.inputs and
	 * algorithmName.0.outputs
	 * 
	 * @param parameters
	 *            parameters of the algorithm
	 */
	public void buildTestCase(Object... parameters) {
		buildSpecificTestCase(0, parameters);
	}

	/**
	 * Build a given test case, i.e. perform algorithm and store inputs/outputs
	 * in files algorithmName.testCaseID.inputs and
	 * algorithmName.testCaseID.outputs
	 * 
	 * @param testID
	 *            ID of the test case files to remove
	 * @param parameters
	 *            parameters of the algorithm
	 */
	public void buildSpecificTestCase(int testID, Object... parameters) {
		if (isTestCase(testID)) {
			throw new PelicanException("Test case #"+testID+" has already been defined for "+getAlgorithmName());
		}
		// Récupération des paramètres d'entrée
		ArrayList<Object> inputs = new ArrayList<Object>();
		for (int i = 0; i < parameters.length; i++) {
			inputs.add(parameters[i]);
		}
		// Traitement
		this.setInput(inputs);
		this.launch();
		ArrayList<Object> outputs = this.getOutput();
		// Création des noms de fichiers pour les entrées et sorties
		String algoName = "tests/"+this.getAlgorithmName() + "." + testID;
		String inputFile = algoName + INPUT_SUFFIX;
		String outputFile = algoName + OUTPUT_SUFFIX;
		// Sauvegarde des entrées et sorties
		try {
			ObjectOutputStream f = null;
			// f = new ObjectOutputStream(new FileOutputStream(inputFile));
			f = new ObjectOutputStream(new GZIPOutputStream(
					new FileOutputStream(inputFile)));
			f.writeObject(inputs);
			f.close();
			f = new ObjectOutputStream(new GZIPOutputStream(
					new FileOutputStream(outputFile)));
			f.writeObject(outputs);
			f.close();
		} catch (IOException ex) {
			throw new AlgorithmException("file writing error with files: "
					+ inputFile + " & " + outputFile);
		}
	}

	/**
	 * Removes the default test case, i.e. files algorithmName.0.inputs
	 * and algorithmName.0.outputs
	 */
	public void removeTestCase() {
		removeTestCase(0);
	}

	/**
	 * Removes a given test case, i.e. files
	 * algorithmName.testCaseID.inputs and
	 * algorithmName.testCaseID.outputs
	 * 
	 * @param testID
	 *            ID of the test case files to remove
	 */
	public void removeTestCase(int testID) {
		// Création des noms de fichiers pour les entrées et sorties
		String algoName =  "tests/"+this.getAlgorithmName() + "." + testID;
		String inputFile = algoName + INPUT_SUFFIX;
		String outputFile = algoName + OUTPUT_SUFFIX;
		// Suppression des fichiers
		try {
			File in = new File(inputFile);
			in.delete();
			File out = new File(outputFile);
			out.delete();
		} catch (SecurityException ex) {
			throw new AlgorithmException("unable to remove files: " + inputFile
					+ " & " + outputFile);
		}
	}

	/**
	 * Loads inputs for a default test case, i.e. file
	 * algorithmName.0.inputs
	 * 
	 * @return input parameters stored for this test case
	 */
	public ArrayList<Object> loadTestCaseInputs() {
		return loadTestCaseInputs(0);
	}

	/**
	 * Loads outputs for a default test case, i.e. file
	 * algorithmName.0.outputs
	 * 
	 * @return output parameters stored for this test case
	 */
	public ArrayList<Object> loadTestCaseOutputs() {
		return loadTestCaseOutputs(0);
	}

	/**
	 * Loads inputs for a given test case, i.e. file
	 * algorithmName.testID.inputs
	 * 
	 * @param testID
	 *            ID of the test case file
	 * @return input parameters stored for this test case
	 */
	public ArrayList<Object> loadTestCaseInputs(int testID) {
		ArrayList<Object> result = null;
		String algoName =  "tests/"+this.getAlgorithmName() + "." + testID;
		String inputFile = algoName + INPUT_SUFFIX;
		try {
			ObjectInputStream f = null;
			// f = new ObjectOutputStream(new FileOutputStream(inputFile));
			f = new ObjectInputStream(new GZIPInputStream(new FileInputStream(
					inputFile)));
			result = (ArrayList<Object>) f.readObject();
			f.close();
		} catch (IOException ex) {
			throw new AlgorithmException("file reading error with file: "
					+ inputFile);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Loads outputs for a given test case, i.e. file
	 * algorithmName.testID.outputs
	 * 
	 * @param testID
	 *            ID of the test case file
	 * @return output parameters stored for this test case
	 */
	public ArrayList<Object> loadTestCaseOutputs(int testID) {
		ArrayList<Object> result = null;
		String algoName =  "tests/"+this.getAlgorithmName() + "." + testID;
		String inputFile = algoName + OUTPUT_SUFFIX;
		try {
			ObjectInputStream f = null;
			// f = new ObjectOutputStream(new FileOutputStream(inputFile));
			f = new ObjectInputStream(new GZIPInputStream(new FileInputStream(
					inputFile)));
			result = (ArrayList<Object>) f.readObject();
			f.close();
		} catch (IOException ex) {
			throw new AlgorithmException("file reading error with file: "
					+ inputFile);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Removes all test cases, i.e. files
	 * algorithmName.testCaseID.inputs and
	 * algorithmName.testCaseID.outputs
	 */
	public void removeAllTestCases() {
		// Build list of files to remove
		File dir = new File("tests");
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches(getAlgorithmName().replace(".", "\\.")
						+ "\\.\\d+" + "(" + INPUT_SUFFIX + "|" + OUTPUT_SUFFIX
						+ ")");
			}
		});
		// Remove all files
		for (File f : files)
			try {
				f.delete();
			} catch (SecurityException ex) {
				throw new AlgorithmException("unable to remove file: " + f);
			}
	}

	/**
	 * Build the default filename for algorithm data
	 * 
	 * @return the default filename
	 */
	private String getAlgorithmName() {
		String algoName = this.getClass().getName();
		String token = "algorithms.";
		algoName = algoName.substring(algoName.indexOf(token) + token.length());
		return algoName;
	}

	/**
	 * Performs a default test case using stored input/output test case data
	 */
	public void launchTestCase() {
		launchTestCase(0);
	}

	/**
	 * Performs a given test case using stored input/output test case data
	 * 
	 * @param testID
	 *            ID of the test case file
	 */
	public void launchTestCase(int testID) {
		setInput(loadTestCaseInputs(testID));
		launch();
		assertObjects(getOutput(), loadTestCaseOutputs(testID));
	}

	/**
	 * Recursive assertion method used to compare objects
	 * @param obj1 first object to be compared
	 * @param obj2 second object to be compared
	 */
	private void assertObjects(Object obj1, Object obj2) {
		// Check object types
		Assert.assertEquals(getAlgorithmName(), obj1.getClass(), obj2.getClass());
		// Modify type if AbstractCollection
		if (obj1 instanceof AbstractCollection) {
			obj1 = ((AbstractCollection) obj1).toArray();
			obj2 = ((AbstractCollection) obj2).toArray();
		}
		// Recursive check if arrays
		if (obj1.getClass().isArray()) {
			Assert.assertEquals(Array.getLength(obj1),Array.getLength(obj2));
			for (int i=0;i<Array.getLength(obj1);i++) {
				assertObjects(Array.get(obj1, i),Array.get(obj2, i));
			}
		}
		// Standard check otherwise
		else
			Assert.assertEquals(getAlgorithmName(), obj1,obj2);
	}

	/**
	 * Checks if input/output test case data have been stored for a default test
	 * case
	 * 
	 * @return true if input/output test case data exist, false otherwise
	 */
	public boolean isTestCase() {
		return isTestCase(0);
	}

	/**
	 * Checks if input/output test case data have been stored for a given test
	 * case
	 * 
	 * @param testID
	 *            ID of the test case file
	 * @return true if input/output test case data exist, false otherwise
	 */
	public boolean isTestCase(int testID) {
		// Création des noms de fichiers pour les entrées et sorties
		String algoName =  "tests/"+this.getAlgorithmName() + "." + testID;
		String inputFile = algoName + INPUT_SUFFIX;
		String outputFile = algoName + OUTPUT_SUFFIX;
		// Existence des fichiers
		boolean exists = true;
		try {
			File in = new File(inputFile);
			File out = new File(outputFile);
			exists = in.exists() && out.exists();
		} catch (SecurityException ex) {
			throw new AlgorithmException("unable to check files: " + inputFile
					+ " & " + outputFile);
		}
		return exists;
	}
}
