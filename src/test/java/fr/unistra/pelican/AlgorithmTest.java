package fr.unistra.pelican;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Test;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.arithmetic.Inversion;
import fr.unistra.pelican.algorithms.io.ImageLoader;

/**
 * JUnit test for applying all default test cases previously designed by buidlTestCase method 
 * @author lefevre
 *
 */
public class AlgorithmTest {

	@Test
	public void testAllAlgorithms() {
		Class[] classes = null;
		Algorithm alg = null;
		try {
			classes = getClasses("fr.unistra.pelican.algorithms", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Class c : classes) {
			try {
				alg = (Algorithm) c.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (alg.isTestCase()) {
				alg.launchTestCase();
				System.out.println("JUnit Test done for:" + c.getName());
			}
		}
	}
	
	public void sampleTest() {
		// Build test cases
		Image input = ImageLoader.exec("samples/billes.png");
		new Inversion().buildTestCase(input);
		input = ImageLoader.exec("samples/lenna.png");
		new Inversion().buildSpecificTestCase(1, input);
		
		// launch test cases
		new Inversion().launchTestCase();
		new Inversion().launchTestCase(1);
		
		// manually load cases
		ArrayList<Object> inputs = new Inversion().loadTestCaseInputs();
		ArrayList<Object> outputs = new Inversion().loadTestCaseOutputs();
		
		// remove test cases
		new Inversion().removeTestCase();
		new Inversion().removeTestCase(1);
		new Inversion().removeAllTestCases();
		
	}

	public static void main(String[] args) {
		new AlgorithmTest().testAllAlgorithms();
	}


	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @author http://snippets.dzone.com/posts/show/4831
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static Class[] getClasses(String packageName, boolean algorithmsOnly)
			throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName, algorithmsOnly));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs.
	 * 
	 * @author http://snippets.dzone.com/posts/show/4831
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class> findClasses(File directory, String packageName,
			boolean algorithmsOnly) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		Class c = null;
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "."
						+ file.getName(), algorithmsOnly));
			} else if (file.getName().endsWith(".class")) {
				c = Class.forName(packageName
						+ '.'
						+ file.getName().substring(0,
								file.getName().length() - 6));
				if (!algorithmsOnly || Algorithm.class.isAssignableFrom(c))
					classes.add(c);
			}
		}
		return classes;
	}
}
