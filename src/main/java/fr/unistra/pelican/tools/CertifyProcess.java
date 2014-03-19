package fr.unistra.pelican.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.unistra.pelican.Algorithm;

/**
 * This class aims to certify the analyzed class. Indeed each Algorithm
 * inherited classes must follow some rules: - Javadoc must be filled for the
 * class, the input/option/outputs attributes and the exec method. - Check if no
 * main method remains in classes. - Check if the super attribute help in the
 * constructor is filled. - Check if the only public methods are the
 * constructor, the launch method and the exec method. - Check if the exec
 * method is static. - Check if super.input (in the constructor) and the
 * parameters of the exec method have got the same name and are in the same
 * order. - Check if the inputs/options/outputs attributes are public. - Check
 * that there is no import ended by *.
 * 
 * 
 * @author Florent Sollier
 * 
 */
public class CertifyProcess {

	/***************************************************************************
	 * 
	 * 
	 * Attributes
	 * 
	 * 
	 **************************************************************************/

	/**
	 * List containing the names of all the inputs attributes.
	 */
	private ArrayList<String> inputs = new ArrayList<String>();

	/**
	 * List containing the names of all the options attributes.
	 */
	private ArrayList<String> options = new ArrayList<String>();

	/**
	 * List containing the names of all the outputs attributes.
	 */
	private ArrayList<String> outputs = new ArrayList<String>();

	/**
	 * Class name of the analyzed class.
	 */
	private String className;

	/**
	 * Full class name of the analyzed class.
	 */
	private String fullClassName;

	/**
	 * Report of the analyzed class.
	 */
	private String report = "";

	/**
	 * Report of the analyzed class.
	 */
	private static String javadoc = "";

	/**
	 * All the reports.
	 */
	private static String allReports = "";

	/**
	 * Boolean attribute which indicates if the class if conform or not.
	 */
	private static boolean notConformClass = false;

	/**
	 * List of all the algorithm to be checked.
	 */
	public static ArrayList<String> algorithms = new ArrayList<String>();

	/**
	 * Pattern for the regexps.
	 */
	private static Pattern pattern;

	/**
	 * Matcher for the regexps.
	 */
	private static Matcher matcher;

	/**
	 * Conformity errors messages.
	 */
	public final static String IMPORTS = "- Imports are not conform, need to be organized. \n";

	public final static String AUTHOR = "- No author is declared \n";

	public final static String EXEC_PROCESS = "- Unexpected object presence inside the exec method. \n";

	public final static String JAVADOC_OPTION_ATTRIBUTES = "- The javadoc of the option attribute is not conform, or the super options attribute \n"
			+ "is not correctly filled, or the options attribute is not declared as public. \n";

	public final static String JAVADOC_OUTPUT_ATTRIBUTES = "- The javadoc of output attribute is not conform, or the super outputs attribute \n"
			+ "is not correctly filled, or an outputs attribute is not \n declared as public. \n";

	public final static String JAVADOC_INPUT_ATTRIBUTES = "- The input javadoc of attribute is not conform, or the super inputs attribute \n"
			+ "is not correctly filled, or an inputs attribute is not declared as public. \n";

	public final static String JAVADOC_EXEC = "- There is no exec method Javadoc. \n";

	public final static String JAVADOC_PARAM_EXEC = "- All the parameters of the exec method are not described in the javadoc. \n";

	public final static String JAVADOC_RETURN_EXEC = "- The return javadoc declaration is not correctly described. \n";

	public final static String JAVADOC_CONSTRUCTOR = "- The constructor Javadoc is not conform. \n";

	public final static String EXEC_NUMBER_OF_PARAMETER = "- The number of parameter of the exec method must be the number of inputs. \n";

	public final static String MAIN = "- A main method remains. \n";

	public final static String NO_HELP = "- The super attribute help (from the constructor) is not declared. \n";

	public final static String SHORT_HELP = "- The super attribute help seems not to be well filled (under 30 caracters). \n";

	public final static String MISFILLED_HELP = "- The super attribute help seems not to be well filled (1 line \n"
			+ " is needed for class description, 1 line is needed for each input, 1 line is needed for each option, \n"
			+ " 1 line is needed for each output and 1 line is needed for the return if the algorithm return something). \n";

	public final static String PUBLIC_METHOD = "- A method other than the constructor, the launch or the exec is declared as public. \n";

	public final static String PUBLIC_ATTRIBUTES = "- An attribute other than an inputs/ouputs/options attribute is declared as public, or\n "
			+ "the super inputs/outputs/options attributes are not correctly filled. \n";

	public final static String STATIC_EXEC = "- The exec method is not declared as static. \n";

	public final static String MISSING_ATTRIBUTE = "- An inputs/ouputs/options attribute is missing or is not declared as public. \n";

	public final static String EXEC_PARAMATER = "- The exec method parameters does not have the attribute's names or are misordered. \n";

	/***************************************************************************
	 * 
	 * 
	 * Setters and getters
	 * 
	 * 
	 **************************************************************************/

	/**
	 * To get the name of the class.
	 * 
	 * @return the name of the class.
	 */
	private String getClassName() {
		return className;
	}

	/**
	 * Set the class name.
	 * 
	 * @param className
	 *            name of the class.
	 */
	private void setClassName(String className) {
		this.className = className;
	}

	/**
	 * To get the full name of the class.
	 * 
	 * @return the full name of the class.
	 */
	private String getFullClassName() {
		return fullClassName;
	}

	/**
	 * Set the full class name.
	 * 
	 * @param fullClassName
	 *            full name of the class.
	 */
	private void setFullClassName(String fullClassName) {
		this.fullClassName = fullClassName;
	}

	/***************************************************************************
	 * 
	 * 
	 * Methods
	 * 
	 * 
	 **************************************************************************/

	/**
	 * This method filters comments of the source code of a class.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 * @return The source code without comments.
	 */
	private String commentsFilter(String sourceText) {

		// It deletes all comments beginning by "//"
		pattern = Pattern.compile("//[^\n]*\n");
		sourceText = pattern.matcher(sourceText).replaceAll("");
		return sourceText;
	}

	/**
	 * This method filters comments of the source code which are too big to be
	 * directly treated by a regular expression.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 * @return The source code without comments.
	 */
	private String preFiltering(String sourceText) {

		int size = sourceText.length();
		String newText = "";
		int index = 0;
		// Beginning and ending indexes of the source code comments
		int beginning, ending;
		boolean first, second, third, fourth, fifth;
		// String containing all comments which will be deleted from the text.
		ArrayList<Integer> comments = new ArrayList<Integer>();

		while (index < size - 2) {

			first = (sourceText.charAt(index) == '/');
			second = (sourceText.charAt(index + 1) == '*');
			third = (sourceText.charAt(index + 2) != '*');

			if (first && second && third) {

				beginning = index;
				fourth = false;
				fifth = false;

				while (!(fourth && fifth)) {

					fourth = (sourceText.charAt(index) == '*');
					fifth = (sourceText.charAt(index + 1) == '/');

					if (fourth && fifth) {
						ending = index + 2;
						comments.add(beginning);
						comments.add(ending);
					}
					index++;
				}
				index--;
			}
			index++;
		}

		comments.add(0, 0);
		comments.add(sourceText.length());
		// All the comments found in the source code are deleted.
		for (int i = 0; i < comments.size(); i++) {
			newText = newText
					+ sourceText
							.substring(comments.get(i), comments.get(i + 1));
			i++;
		}
		return newText;
	}

	/**
	 * This method filters the inner classes of the source code of a class.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 * @return The source code without inner classes.
	 */
	private String innerClassesFilter(String sourceText) {

		String copy = sourceText;
		pattern = Pattern.compile("(\\w*(?<!public)\\s+class(\\s|\\w)*\\{)");
		matcher = pattern.matcher(sourceText);
		while (matcher.find()) {
			int groupStart = matcher.start();
			int bracketStart = matcher.end();
			int bracketNumber = 1;
			while (bracketNumber > 0) {
				bracketStart++;
				if (sourceText.charAt(bracketStart) == '{') {
					bracketNumber++;
				}
				if (sourceText.charAt(bracketStart) == '}') {
					bracketNumber--;
				}
			}
			copy = copy.replace(sourceText.substring(groupStart,
					bracketStart + 1), "");
		}
		return copy;
	}

	/**
	 * This method filters the inner interfaces of the source code of a class.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 * @return The source code without inner interfaces.
	 */
	private String innerInterfaceFilter(String sourceText) {

		String copy = sourceText;
		pattern = Pattern.compile("(\\w*\\s*interface(\\s|\\w)*\\{)");
		matcher = pattern.matcher(sourceText);
		while (matcher.find()) {
			int groupStart = matcher.start();
			int bracketStart = matcher.end();
			int bracketNumber = 1;
			while (bracketNumber > 0) {
				bracketStart++;
				if (sourceText.charAt(bracketStart) == '{') {
					bracketNumber++;
				}
				if (sourceText.charAt(bracketStart) == '}') {
					bracketNumber--;
				}
			}
			copy = copy.replace(sourceText.substring(groupStart,
					bracketStart + 1), "");
		}
		return copy;
	}

	/**
	 * This method gets the input/option/output attributes of the analysed class
	 * to initialise the inputs/outputs/options arraylists.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void getAttributes(String sourceText) {

		String match;
		String tmp;
		String[] tmp2;

		// Gets the inputs
		if (sourceText.contains("super.inputs")) {
			match = sourceText
					.substring(sourceText.indexOf("super.inputs"), sourceText
							.indexOf(";", sourceText.indexOf("super.inputs")));
			tmp = match.substring(match.indexOf("\"") + 1, match
					.lastIndexOf("\""));
			tmp2 = tmp.split(",");
			for (int i = 0; i < tmp2.length; i++) {
				inputs.add(tmp2[i]);
			}
		}

		// Gets the options
		if (sourceText.contains("super.options")) {
			match = sourceText.substring(sourceText.indexOf("super.options"),
					sourceText
							.indexOf(";", sourceText.indexOf("super.options")));
			tmp = match.substring(match.indexOf("\"") + 1, match
					.lastIndexOf("\""));
			tmp2 = tmp.split(",");
			for (int i = 0; i < tmp2.length; i++) {
				options.add(tmp2[i]);
			}
		}

		// Gets the outputs
		if (sourceText.contains("super.outputs")) {
			match = sourceText.substring(sourceText.indexOf("super.outputs"),
					sourceText
							.indexOf(";", sourceText.indexOf("super.outputs")));
			tmp = match.substring(match.indexOf("\"") + 1, match
					.lastIndexOf("\""));
			tmp2 = tmp.split(",");

			for (int i = 0; i < tmp2.length; i++) {
				outputs.add(tmp2[i]);
			}
		}
	}

	/**
	 * This method checks if the Javadoc is filled for the input/option/outputs
	 * attributes.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void javadocAttributesCheck(String sourceText) {

		for (int i = 0; i < inputs.size(); i++) {
			pattern = Pattern
					.compile("/\\*\\*([^\\*]|\\*(?!/))*\\*/(\\s|\n)*public\\s+\\w+(\\.|\\w)*(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+"
							+ inputs.get(i) + "\\s*=*\\s*\\w*\\s*;");
			matcher = pattern.matcher(sourceText);
			if (!matcher.find()) {
				report = report + JAVADOC_INPUT_ATTRIBUTES;
				notConformClass = true;
			} else {
				String doc = matcher.group().substring(0,
						matcher.group().indexOf("public") - 6);
				javadoc = javadoc + "[" + this.getClassName() + ":"
						+ inputs.get(i) + "]" + doc + "[|"
						+ this.getClassName() + ":" + inputs.get(i) + "]"
						+ "\n";
			}
		}
		for (int i = 0; i < options.size(); i++) {
			pattern = Pattern
					.compile("/\\*\\*([^\\*]|\\*(?!/))*\\*/(\\s|\n)*public\\s+\\w+(\\.|\\w)*(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+"
							+ options.get(i) + "\\s*=*\\s*\\w*\\s*;");
			matcher = pattern.matcher(sourceText);
			if (!matcher.find()) {
				report = report + JAVADOC_OPTION_ATTRIBUTES;
				notConformClass = true;
			} else {
				String doc = matcher.group().substring(0,
						matcher.group().indexOf("public") - 6);
				javadoc = javadoc + "[" + this.getClassName() + ":"
						+ options.get(i) + "]" + doc + "[|"
						+ this.getClassName() + ":" + options.get(i) + "]"
						+ "\n";
			}
		}
		for (int i = 0; i < outputs.size(); i++) {
			pattern = Pattern
					.compile("/\\*\\*([^\\*]|\\*(?!/))*\\*/(\\s|\n)*public\\s+\\w+(\\.|\\w)*(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+"
							+ outputs.get(i) + "\\s*=*\\s*\\w*\\s*;");
			matcher = pattern.matcher(sourceText);
			if (!matcher.find()) {
				report = report + JAVADOC_OUTPUT_ATTRIBUTES;
				notConformClass = true;
			} /*
			 * else { String doc = matcher.group().substring(0,
			 * matcher.group().indexOf("public") - 6); javadoc = javadoc + "[" +
			 * this.getClassName() + ":" + outputs.get(i) + "]" + doc + "[|" +
			 * this.getClassName() + ":" + outputs.get(i) + "]" + "\n"; }
			 */
		}
	}

	/**
	 * This method checks if the Javadoc is filled for the constructor.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void javadocConstructorCheck(String sourceText) {

		pattern = Pattern
				.compile("/\\*\\*([^\\*]|\\*(?!/))*\\*/(\\s|\n)*public\\s+"
						+ getClassName() + "\\s*\\(\\)(\\s|\n)*\\{");

		matcher = pattern.matcher(sourceText);
		if (!matcher.find()) {
			report = report + JAVADOC_CONSTRUCTOR;
			notConformClass = true;
		}
	}

	/**
	 * This method checks if the process of the exec method is called on the
	 * right object..
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void processCheck(String sourceText) {

		pattern = Pattern
				.compile("public\\s+static\\s+\\w+(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+exec\\s*\\([^\\)]*\\)\\s*\\{[^\\}]*\\}");
		matcher = pattern.matcher(sourceText);
		ArrayList<String> execNoDocList = new ArrayList<String>();

		while (matcher.find()) {
			execNoDocList.add(matcher.group());
		}

		for (int i = 0; i < execNoDocList.size(); i++) {
			pattern = Pattern.compile("new\\s+" + getClassName()
					+ "\\(\\)\\.process");
			matcher = pattern.matcher(execNoDocList.get(i));
			if (!matcher.find()) {
				report = report + EXEC_PROCESS;
				notConformClass = true;
			}
		}
	}

	/**
	 * This method checks if the Javadoc is filled for the exec method.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void javadocExecCheck(String sourceText) {

		ArrayList<String> execDocList = new ArrayList<String>();
		ArrayList<String> execNoDocList = new ArrayList<String>();

		// Check if the javadoc is present.
		pattern = Pattern
				.compile("/\\*\\*([^\\*]|\\*(?!/))*\\*/(\\s|\n)*public\\s+static\\s+\\w+(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+exec\\s*\\(");
		matcher = pattern.matcher(sourceText);
		if (!matcher.find()) {
			report = report + JAVADOC_EXEC;
			notConformClass = true;
		} else {
			// The few following lines are only useful for the javadoc file
			String doc = "";
			if (matcher.group().contains("@")) {
				doc = matcher.group().substring(0,
						matcher.group().indexOf("@") - 1);
			} else {
				doc = matcher.group().substring(0,
						matcher.group().indexOf("public") - 6);
			}
			javadoc = javadoc + "[" + this.getClassName() + "]" + doc + "[|"
					+ this.getClassName() + "]" + "\n";

			// here it continues with the checking operation
			execDocList.add(matcher.group());
			while (matcher.find()) {
				execDocList.add(matcher.group());
			}

			// Check if the Javadoc has the correct parameters declaration.
			pattern = Pattern
					.compile("public\\s+static\\s+\\w+(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+exec\\s*\\([^\\)]*\\)");
			matcher = pattern.matcher(sourceText);

			if (matcher.find()) {
				execNoDocList.add(matcher.group());
				while (matcher.find()) {
					execNoDocList.add(matcher.group());
				}

				for (int i = 0; i < execDocList.size(); i++) {
					// First gets the number of parameters.
					String tmp = execNoDocList.get(i).substring(
							execNoDocList.get(i).indexOf("(") + 1,
							execNoDocList.get(i).lastIndexOf(")"));
					String[] tmp2 = tmp.split(",");
					int paramNb = tmp2.length;

					// Then gets the number of parameters declared.
					int paramDocNb = regexOccur(execDocList.get(i), "@param");
					if (paramDocNb != paramNb) {
						report = report + JAVADOC_PARAM_EXEC;
						notConformClass = true;
					}
				}
			}

			for (int i = 0; i < execDocList.size(); i++) {
				// Check if the Javadoc has the correct return declaration.
				pattern = Pattern.compile("@return");
				matcher = pattern.matcher(execDocList.get(i));

				// Is there any return declaration?
				boolean isReturn = matcher.find();

				// Does the exec method return something?
				if (matcher.find()) {
					pattern = Pattern.compile("void");
					matcher = pattern.matcher(execNoDocList.get(i));
					boolean isVoid = matcher.find();
					if (isVoid == isReturn) {
						report = report + JAVADOC_RETURN_EXEC;
						notConformClass = true;
					}
				}
			}
		}
	}

	/**
	 * Return the number of occurencies of the specified pattern in the text
	 * paramater.
	 * 
	 * @param text
	 *            Initial text.
	 * @param regex
	 *            Regular expression which the number of occurencies has to be
	 *            counted.
	 * @return Return the number of occurencies of the specified pattern in the
	 *         text paramater.
	 */
	private static final int regexOccur(String text, String regex) {

		Matcher matcher = Pattern.compile(regex).matcher(text);
		int occur = 0;
		while (matcher.find()) {
			occur++;
		}
		return occur;
	}

	/**
	 * This method checks if no main method remains in classes.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void noMainCheck(String sourceText) {

		pattern = Pattern.compile("public\\s+static\\s+void\\s+main\\s*\\(");
		matcher = pattern.matcher(sourceText);
		if (matcher.find()) {
			report = report + MAIN;
			notConformClass = true;
		}
	}

	/**
	 * This method checks if the only public methods are the constructor, the
	 * launch method and the exec method.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void publicMethodCheck(String sourceText) {

		pattern = Pattern
				.compile("public\\s*\\w*\\s*\\w*(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+\\w+\\s*\\([^\\)]*\\)");
		matcher = pattern.matcher(sourceText);

		while (matcher.find()) {
			String match = matcher.group();
			String tmp = match.substring(0, match.lastIndexOf("("));
			int sum = regexOccur(tmp, "\\b" + getClassName() + "\\b")
					+ regexOccur(tmp, "\\blaunch\\b")
					+ regexOccur(tmp, "\\bexec\\b");
			if (sum == 0) {
				report = report + PUBLIC_METHOD;
				notConformClass = true;
			}
		}
	}

	/**
	 * This method Checks if super.input (in the constructor) and the parameters
	 * of the exec method have got the same name and are in the same order. It
	 * also checks if the exec method is static.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void execCheck(String sourceText) {

		ArrayList<String> execList = new ArrayList<String>();

		pattern = Pattern
				.compile("public\\s+static\\s+\\w+(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+exec\\s*\\([^\\)]*\\)");
		matcher = pattern.matcher(sourceText);

		if (matcher.find()) {

			boolean found = false;
			String[] save = new String[inputs.size()];

			execList.add(matcher.group());
			while (matcher.find()) {
				execList.add(matcher.group());
			}

			// super.input (in the constructor) and the parameters
			// of the exec method are in the same order.
			for (int i = 0; i < execList.size(); i++) {

				String tmp = execList.get(i).substring(
						execList.get(i).indexOf("(") + 1,
						execList.get(i).lastIndexOf(")"));
				String[] tmp2 = new String[tmp.split(",").length];

				for (int j = 0; j < tmp.split(",").length; j++) {
					matcher = Pattern
							.compile(
									"\\s*(\\w+)(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s*")
							.matcher(tmp.split(",")[j]);
					matcher.find();
					matcher.find();
					tmp2[j] = matcher.group();
				}
				int paramNb = tmp2.length;
				if ((paramNb + 1) == inputs.size()) {
					found = true;
					save = tmp2;
				}
			}
			if (!found) {
				report = report + EXEC_NUMBER_OF_PARAMETER;
				notConformClass = true;
			} else {

				// Checks the super.input (in the constructor) and the
				// parameters
				// of the exec method have got the same name
				for (int i = 0; i < save.length; i++) {
					if (!save[i].equals(inputs.get(i))) {
						report = report + EXEC_PARAMATER;
						notConformClass = true;
					}
				}
			}
		}
	}

	/**
	 * This method Check if the inputs/options/outputs attributes are public and
	 * if the other attributes are not.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void publicAttributesCheck(String sourceText) {

		int sum = 0;
		// Checks if the input atributes are declared as public
		for (int i = 0; i < inputs.size(); i++) {
			sum = regexOccur(
					sourceText,
					"public\\s+\\w+(\\.|\\w)*(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+"
							+ inputs.get(i) + "\\s*=*\\s*\\w*\\s*;");
			if (sum == 0) {
				report = report + MISSING_ATTRIBUTE;
				notConformClass = true;
			}
			sum = 0;
		}
		// Checks if the options atributes are declared as public
		for (int i = 0; i < options.size(); i++) {
			sum = regexOccur(
					sourceText,
					"public\\s+\\w+(\\.|\\w)*(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+"
							+ options.get(i) + "\\s*=*\\s*\\w*\\s*;");
			if (sum == 0) {
				report = report + MISSING_ATTRIBUTE;
				notConformClass = true;
			}
			sum = 0;
		}
		// Checks if the output atributes are declared as public
		for (int i = 0; i < outputs.size(); i++) {
			sum = regexOccur(
					sourceText,
					"public\\s+\\w+(\\.|\\w)*(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+"
							+ outputs.get(i) + "\\s*=*\\s*\\w*\\s*;");
			if (sum == 0) {
				report = report + MISSING_ATTRIBUTE;
				notConformClass = true;
			}
			sum = 0;
		}

		// Checks if all the public attributes are the
		// inputs/options/outputs
		// attributes (excepted for the final attributes which may be
		// public).
		pattern = Pattern
				.compile("public\\s+\\w+(\\s*\\[\\]\\s*)*(\\<\\s*\\w*(\\s*\\[\\]\\s*)*\\s*\\>)*\\s+\\w+\\s*=*\\s*\\w*\\s*;");

		matcher = pattern.matcher(sourceText);

		int sumInput = 0;
		int sumOption = 0;
		int sumOutput = 0;
		while (matcher.find()) {
			String match = matcher.group();
			for (int i = 0; i < inputs.size(); i++) {
				sumInput = sumInput + regexOccur(match, inputs.get(i));
			}
			for (int i = 0; i < options.size(); i++) {
				sumOption = sumOption + regexOccur(match, options.get(i));
			}
			for (int i = 0; i < outputs.size(); i++) {
				sumOutput = sumOutput + regexOccur(match, outputs.get(i));
			}
			sum = sumInput + sumOption + sumOutput;
			if (sum == 0) {
				report = report + PUBLIC_ATTRIBUTES;
				notConformClass = true;
			}
			sumOutput = 0;
			sumOption = 0;
			sumInput = 0;
			sum = 0;
		}
	}

	/**
	 * This method Check that there is no import ended by *.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 */
	private void importsCheck(String sourceText) {

		pattern = Pattern.compile("import\\s+\\w+(\\*|\\.|\\w)*\\s*;");
		matcher = pattern.matcher(sourceText);
		int result = 0;
		while (matcher.find()) {
			String match = matcher.group();
			result = regexOccur(match, "\\*");
			if (result != 0) {
				report = report + IMPORTS;
				notConformClass = true;
			}
		}
	}

	/**
	 * This method check the default test case (if exists)
	 */
	public void testCheck() {
		try {
			Algorithm alg;
			alg = (Algorithm) Class.forName(fullClassName).newInstance();
			if (alg.isTestCase()) {
				alg.launchTestCase();
				System.out.println("JUnit Test done for:" + className);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method gets the author of the class.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 * @return the author of the class.
	 */
	private String getAuthor(String sourceText) {

		String author = "";
		pattern = Pattern.compile("@author\\s+(\\w|\\s|,)*");
		matcher = pattern.matcher(sourceText);

		if (matcher.find()) {
			author = matcher.group();
		} else {
			author = AUTHOR;
		}
		return author;
	}

	/**
	 * This method sets the list of all the algorithms of the workspace.
	 * 
	 * @param directoryPath
	 *            Path of the directory to be checked.
	 * @return The list of all the algorithms of the workspace.
	 */
	private static ArrayList<String> algorithmList(String directoryPath) {
		File directory = new File(directoryPath);
		File[] subfiles = directory.listFiles();
		boolean experimental;

		for (int i = 0; i < subfiles.length; i++) {

			if (subfiles[i].isDirectory() && !subfiles[i].isHidden()) {
				algorithmList(subfiles[i].getPath());
			}
			if (subfiles[i].isFile()) {
				// We do not want to check the experimental files
				pattern = Pattern.compile(File.separator + "experimental"
						+ File.separator);
				matcher = pattern.matcher(subfiles[i].getPath());
				experimental = matcher.find();
				if (!experimental) {
					algorithms.add(subfiles[i].getPath());
				}
			}
		}
		return algorithms;
	}

	/**
	 * This method Checks the entire conformity of the source code and produce a
	 * report about it.
	 * 
	 * @param sourceText
	 *            Source code of the class.
	 * @return The report of the conformity.
	 */
	private String fullCheck(String sourceText) {

		// Filters the comments of the text, the inner interfaces and the inner
		// classes.

		try {
			String filtered = this.preFiltering(sourceText);
			filtered = this.commentsFilter(sourceText);
			filtered = this.innerClassesFilter(filtered);
			filtered = this.innerInterfaceFilter(filtered);

			report = report + this.getAuthor(sourceText) + "\n\n";
			this.getAttributes(filtered);
			this.javadocAttributesCheck(filtered);
			this.javadocConstructorCheck(filtered);
			this.javadocExecCheck(filtered);
			this.noMainCheck(filtered);
			this.publicMethodCheck(filtered);
			this.execCheck(filtered);
			this.processCheck(filtered);
			this.publicAttributesCheck(filtered);
			this.importsCheck(filtered);

			this.testCheck();

		} catch (StackOverflowError e) {
			System.err
					.println("\n"
							+ "******************************************************************** \n"
							+ "The stack size need to be increased, please put -Xss????k        \n"
							+ "(where ???? is the new stack size) as JVM argument.         	 \n"
							+ "******************************************************************** \n");
		}

		return report;

	}

	/***************************************************************************
	 * 
	 * 
	 * Main
	 * 
	 * 
	 **************************************************************************/

	public static void main(String[] args) {

		// Change due to new organization (main/java and test) with Maven
		ArrayList<String> algoList = algorithmList("src" + File.separator
				+ "main" + File.separator + "java" + File.separator + "fr"
				+ File.separator + "unistra" + File.separator + "pelican"
				+ File.separator + "algorithms" + File.separator);
		// ArrayList<String> algoList = algorithmList("src" + File.separator +
		// "fr" + File.separator + "unistra" + File.separator + "pelican" +
		// File.separator + "algorithms" + File.separator);
		// ArrayList<String> algoList =
		// algorithmList("src/fr/unistra/pelican/algorithms/");
		FileWriter fw;
		try {
			for (int i = 0; i < algoList.size(); i++) {

				CertifyProcess cp = new CertifyProcess();
				// Variable which will contain the source code.
				String sourceText = "";

				// Set the class name with the name of the checked file.
				String className = algoList.get(i).substring(
						algoList.get(i).lastIndexOf(File.separator) + 1,
						algoList.get(i).indexOf("."));
				cp.setClassName(className);

				// Set the package name of the checked file.
				String packageName = algoList.get(i).substring(
						algoList.get(i)
								.substring(
										0,
										algoList.get(i).lastIndexOf(
												File.separator) - 1)
								.lastIndexOf(File.separator) + 1,
						algoList.get(i).lastIndexOf(File.separator));
				
				String algName=algoList.get(i);
				algName=algName.substring(algName.indexOf("fr/unistra/pelican/algorithms"));
				algName=algName.substring(0,algName.lastIndexOf(".java"));
				algName=algName.replace(File.separator, ".");
				cp.setFullClassName(algName);

				// Set the java source code into a string.
				try {
					InputStream ips = new FileInputStream(algoList.get(i));
					InputStreamReader ipsr = new InputStreamReader(ips);
					BufferedReader br = new BufferedReader(ipsr);
					String ligne;
					while ((ligne = br.readLine()) != null) {
						sourceText += ligne + "\n";
					}
					br.close();
				} catch (Exception e) {
					System.out.println(e.toString());
				}

				// Use the fullCheck method to generate the report.
				String report = "\n\n" + "Class: " + algoList.get(i) + "\n";

				try {
					report += cp.fullCheck(sourceText);
				} catch (IllegalStateException ex) {
					ex.printStackTrace();
					report += ex.getMessage();
				}

				if (notConformClass) {

					// System.out.println(System.getProperty("user.dir")+"/"+
					// "reports" + File.separator + packageName + File.separator
					// + className + ".txt");

					allReports += report;
					File f = new File("reports" + File.separator + packageName
							+ File.separator + className + ".txt");
					f.getParentFile().mkdirs();
					fw = new FileWriter(f);
					//System.out.println(f);
					fw.write(report);
					fw.close();

					notConformClass = false;
				}
			}
			// All the reports are saved in one file to be able to checked
			// quickly
			// the conformity
			fw = new FileWriter("reports" + File.separator + "allReports.txt");
			fw.write(allReports);
			fw.close();

			// Filtering of the javadoc attribute
			javadoc = javadoc.replaceAll("\\*", "");
			javadoc = javadoc.replaceAll("\\/", "");
			// The javadoc of all the classes is saved.
			fw = new FileWriter("reports" + File.separator + "javadoc.txt");
			fw.write(javadoc);
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
