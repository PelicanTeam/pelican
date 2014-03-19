/**
 * 
 */
package fr.unistra.pelican.util.optimization;

import java.util.Arrays;

import Jama.Matrix;
import fr.unistra.pelican.InvalidParameterException;
import fr.unistra.pelican.util.Tools;

/**
 * 
 * Multidimentional non linear least square fitting algorithm.
 * <p>
 * Levenberg-Marquardt Algorithm optimization, implementation is the one given in M. 
 * <p><i>Lourakis and A. Argyros. The Design and Implementation of
 * a Generic Sparse Bundle Adjustment Software Package Based on the
 * Levenberg-Marquardt Algorithm. Technical Report 340, Institute of Computer
 * Science - FORTH, Heraklion, Greece, Aug. 2004.</i>
 * <p>
 * Wikipedia description (mouarfff!!!) : <i> The LMA interpolates between the Gauss–Newton algorithm (GNA) 
 * and the method of gradient descent. The LMA is more robust than the GNA, which means that in many cases 
 * it finds a solution even if it starts very far off the final minimum. On the other hand, for well-behaved 
 * functions and reasonable starting parameters, the LMA tends to be a bit slower than the GNA.<\i>
 * <p>
 * The damping term is quite sensitive, the algorithm provides an adaptive scheme to evaluate it but it needs an initial factor.
 * The initial factor is called tau and can be adjusted by user. The smaller it is, the faster is the algorithm but it increases
 * the probability of failure.
 * <p>
 * Epsilon parameters control the stop condition. espilon1 and 2 are quite technical, 3rd one is just a limit in squared error.
 * Maximum iteration can also controlled the end of the process...
 * <p>
 * At the end you can call success() to see if stop condition is a normal one.
 * The event which stops the algorithm can be known using getStopEvent()
 * 
 * <p>
 * Constraint management is hazardous !
 * 
 * 
 * @author Benjamin Perret
 * @version 1.0, 01.09.2009
 * 
 */
 
public class LevenbergMarquardt {
	
	/**
	 * Possible cause of stop
	 * @author Benjamin Perret
	 *
	 */
	public enum StopEvent {Max_Iteration, Gradient_Too_Low, Relative_Increment_Too_Low, Error_Limit_Reached, Blocked_By_Constraints, OverFlow_In_Adaptive_Damping, NaN_In_Error_Computation}
	
	/**
	 * Can we consider that the fit is a success (in an algorithmic sense)
	 */
	public boolean success=true;
	
	/**
	 * Origin of stop
	 */
	public StopEvent stopEvent;
	
	/** Wanna chat */
	public boolean verbose = false;
	
	/** 
	 * Function to be fitted
	 */
	private LevenbergMarquardtFunction function;
	
	/** 
	 * Parameters to optimize
	 */
	private double[] parameters;
	
	/** 
	 * Data points : i.e. what you are trying to fit to
	 */
	private double data[];

	/** 
	 * Weights applied to data
	 */
	private double[] weights;
	
	/**
	 * JtJ  with J the jacobian
	 */
	private Matrix a;
	
	/**
	 * weight*Jt*(y-f)
	 */
	private double[] g;
	
	/**
	 * Initial lambda factor
	 */
	private double tau = 1e-1;
	
	/**
	 * Gain ratio of current estimate compared to previous one
	 */
	private double gainRatio;
	
	/**
	 * Increment from previous parameter
	 */
	private double[] da;
	
	/**
	 * Lambda damping term (initialized latter)
	 */
	private double lambda = 0.00;
	
	/**
	 * Initial lambdaFactor (factor applied to lambda if Chi2 increases
	 */
	private double lambdaFactor = 2;
	
	/**
	 * Current chi2
	 */
	private double chi2;
	
	/**
	 * Chi2 with new parameters
	 */
	private double incrementedChi2;
	
	/**
	 * Proposed parameters
	 */
	private double[] newParameters;
	
	/**
	 * Iterations done
	 */
	private int iterationCount;
	
	/**
	 * Have we reached a stop condition ?
	 */
	private boolean stop=false;
	
	/**
	 * Stop condition 1 gradient too low :  ||g||inf <= epsilon1
	 */
	private double epsilon1=1e-16;
	
	/**
	 * Stop condition 2 magnitude difference between da and parameter too low : ||da|| <= epsilon2 ||parameter||
	 */
	private double epsilon2=1e-16;
	
	/**
	 * Stop condition 3 chi2 is very low : chi2 <= epsilon3 (reach it baby!)
	 */
	private double epsilon3=1e-16;
	
	
	/**
	 * Stop condition on chi2 variation : chi2 - newChi2 <= minChi2Change
	 */
	private double minChi2Change=1e-3;
	
	/**
	 * Maximum number of iterations
	 */
	private int maxIterations = 100;
	
	/**
	 * Number of significant data points (i.e. not 0 weighted)
	 */
	private int nbDataPoints=0;
	
	/**
	 * Jacobian Matrix
	 */
	private double [][] partialDerivative;
	
	/**
	 * Current values of fitted function (function.getY(parameters))
	 */
	private double [] functionValues;
	
	/**
	 * New values of fitted function (function.getY(newParameters))
	 */
	private double [] tempFunctionValues;
	
	/**
	 * Covariance matrix of errors
	 */
	private double [][] covarianceMatrix;
	
	/**
	 * Standard deviation for each parameter (i.e. square root of the diagonal term of the covariance matrix)
	 */
	private double [] standardError;
	
	/**
	 * Normalized chi2 at the end of the process
	 */
	private double finalNormalizedChi2=Double.POSITIVE_INFINITY;
	
	/**
	 * Prepare algorithm to fit given function to given dataPoints with given initial parameters.
	 * 
	 * @param function The function to be fitted, takes M parameters and produce data over a space of size N
	 * @param parameters The initial guess for the fit parameters, length M.
	 * @param data data values...
	 */
	public LevenbergMarquardt(LevenbergMarquardtFunction function, double[] parameters, double[] data) {
		initialize (function, parameters, data,	null);
	}
	

	/**
	 * Prepare algorithm to fit given function to given dataPoints with given initial parameters.
	 * 
	 * @param function The function to be fitted, takes M parameters and produce data over a space of size N
	 * @param parameters The initial guess for the fit parameters, length M.
	 * @param dataPoints data values...
	 * @param weights The weights, normally given as: <code>weights[i] = 1 / sigma_i^2</code>.
	 */
	public LevenbergMarquardt(LevenbergMarquardtFunction function, double[] parameters, double[] data, double[] weights) {
		initialize(function, parameters, data, weights);
	}

	/**
	 * Performs all needed initializations and allocations
	 * @param function Function to fit
	 * @param parameters Initial parameters
	 * @param yDataPoints Data points
	 * @param weights Weights (positive real, one weight at least must be greater than 0)
	 */
	private void initialize(LevenbergMarquardtFunction function, double[] parameters, double[] yDataPoints, double[] weights) {
		this.function = function;
		this.parameters = parameters;
		this.data = yDataPoints;
		if(weights==null)
		{
			weights=createDefaultWeights(data.length);
		}
		
		checkWeights(yDataPoints.length, weights);
		this.weights=weights;
		this.newParameters = new double[parameters.length]; 
		this.a = new Matrix(parameters.length, parameters.length);
		this.g = new double[parameters.length];
		this.da = new double[parameters.length];
	}
	
	/**
	 * Create an array of size n and initialize all values to 1.0
	 * @param n size of the array
	 * @return a array filled by 1.0
	 */
	private static double [] createDefaultWeights(int n)
	{
		double [] w = new double[n];
		Arrays.fill(w, 1.0);
		return w;
	}
	
	
	/**
	 * The little hack to save first function evaluation :)
	 */
	private boolean first=true;
	
	
	
	/** 
	 * Do the job baby
	 */
	public void fit()  {
		
		iterationCount = 0;
		
		incrementedChi2 = calculateChi2();
		
		if (Double.isNaN(chi2)) 
		{
			stop=true;
			success=false;
			stopEvent=StopEvent.NaN_In_Error_Computation;
		}
			

		while (!stop ) {

			chi2 = incrementedChi2;
			if (verbose)
				System.out.println(iterationCount + ": chi2 = " + chi2 + ", " + Arrays.toString(parameters));
			
			updateA();
			updateG();
			
			
			if (first) {
				lambda = computeInitialLambda();
				first = false;
			}
			
			/**
			 * flag is true until a good increment is found
			 */
			boolean flag = true;
			
			do {
				solveIncrements();
				if (Tools.euclideanNorm(da) <= epsilon2* Tools.euclideanNorm(parameters)) {
					stop=true;
					success=true;
					stopEvent=StopEvent.Relative_Increment_Too_Low;
				} else {
					boolean constraintsCheck=true;
					if(function.checkConstraints(newParameters))
					{
						incrementedChi2 = calculateIncrementedChi2();
						gainRatio = computGainRatio();
					}else {
						if(verbose)System.out.println("Constraints in the wall!");
						constraintsCheck=false;
						gainRatio=-1.0;
					}
					if (gainRatio <= 0.0) {
						lambda *= lambdaFactor;
						lambdaFactor = lambdaFactor * 2.0;
						if(Double.isInfinite(lambdaFactor))
						{
							if(!constraintsCheck)
							{
								stopEvent=StopEvent.Blocked_By_Constraints;
							}else{
								stopEvent=StopEvent.OverFlow_In_Adaptive_Damping;
							}
							success=false;
							stop=true;
						}
					} else {
						flag = false;
					}
				}
			} while (flag && !stop);
			if(!stop)
			{
				//System.out.println("GainRatio=" + gainRatio);

				if (Tools.infiniteNorm(g) <= epsilon1)
				{
					stop=true;
					success=true;
					stopEvent=StopEvent.Gradient_Too_Low;
				}
				if(Tools.DotProduct(da, da) <= epsilon3)
				{
					stop=true;
					success=true;
					stopEvent=StopEvent.Relative_Increment_Too_Low;
				}
				lambdaFactor = 2.0;
				lambda = lambda	* Math.max(0.33333333333, 1.0 - Math.pow(2.0 * gainRatio - 1.0, 3.0));

				double[] temp = functionValues;
				functionValues = tempFunctionValues;
				tempFunctionValues = temp;

				updateParameters();

				//System.out.println("Lambda=" + lambda);

				// lambda *= lambdaFactor;
				/*if(chi2 - incrementedChi2 <=minChi2Change)
				{
					System.out.println("minimum chi2 change limit");
					stop=true;
				}*/
				iterationCount++;
				if(iterationCount >= maxIterations)
				{
					stop=true;
					success=true;
					stopEvent=StopEvent.Max_Iteration;
				}
			}
		}
		errorEstimation();
	}
	
	private void errorEstimation() {
		covarianceMatrix=getCovarianceMatrixOfStandardErrorsInParameters();
		standardError = getStandardErrorsOfParameters();
		finalNormalizedChi2=chi2Goodness();
		if (verbose) {
			System.out.println(" ***** FIT ENDED ***** ");
			if(success)
				System.out.println(" ***** SUCCESS ***** ");
			else
				System.out.println(" ***** FAILED ***** ");
			System.out.println(" Stoped by: " + stopEvent);	
			System.out.println(" Normalized square error: " + finalNormalizedChi2);				
			System.out.println(" Parameter std errors: " + Arrays.toString(standardError));
			System.out.println(" ********************* ");		
		}
	}
	
	/**
	 * Compute gain ratio: (chi2-newChi2)/(da t*(delta*da+g))
	 * @return
	 */
	private double computGainRatio()
	{
		double tmp=0.0;
		for(int i=0;i<da.length;i++)
		{
			double op=0.0;
			for(int j=0;j<da.length;j++)
			{
				op=lambda*da[j]+g[j];
			}
			tmp+=da[i]*op;
		}
		return (chi2 - incrementedChi2)/tmp;
	}
	
	/**
	 * Initial lambda damping term 
	 * @return tau*max(A(i,i))
	 */
	private double computeInitialLambda(){
		double max=Double.NEGATIVE_INFINITY;
		for(int i=0;i<a.getColumnDimension();i++)
		{
			double v=a.get(i, i);
			if(v>max)
				max=v;
		}
		return tau*max;
	}
	

	

	
	/** Updates parameters from incrementedParameters. */
	protected void updateParameters() {
		System.arraycopy(newParameters, 0, parameters, 0, parameters.length);
	}
	
	/**
	 * Inverse system to find new parameters
	 */
	protected void solveIncrements()  {
		
		
		Matrix alpha2=a.copy();
		for(int i=0;i<alpha2.getColumnDimension();i++)
		{
			double v=alpha2.get(i,i);
			alpha2.set(i, i, v+lambda);//v*(1.0+lambda));
		}
		
		Matrix m = alpha2.inverse();
		//alpha.setMatrix(0, alpha.getRowDimension() - 1, 0, alpha.getColumnDimension() - 1, m);
		
		matrixMultiply(m,g, da);
		for (int i = 0; i < parameters.length; i++) {
			newParameters[i] = parameters[i] + da[i];
		}
	}
	
	/**
	 * @return square error for current parameters
	 */
	private double calculateChi2() {
		double result = 0;
		functionValues=function.getY(parameters,functionValues);
		for (int i = 0; i < data.length; i++) {
			double dy = data[i] - functionValues[i];
			result += weights[i] * dy * dy; 
		}
		return result;
	}
	
	/**
	 *  @return square error for current newParameters
	 */
	private double calculateIncrementedChi2() {
		double result = 0;
		tempFunctionValues=function.getY(newParameters,tempFunctionValues);
		for (int i = 0; i < data.length; i++) {
			double dy = data[i] - functionValues[i];	
			result += weights[i] * dy * dy; 
		}
		return result;
	}
	
	/** compute new a=JtJ */
	private void updateA() {

		partialDerivative=function.getJacobian(parameters);
		for (int i = 0; i < parameters.length; i++) {
			for (int j = 0; j < parameters.length; j++) {
				double v = 0;
				for (int k = 0; k < data.length; k++) {
					v += 
						weights[k] * 
						partialDerivative[i][k]*
						partialDerivative[j][k];
				}
				
				a.set(i, j, v);
			}
		}
	}
	

	
	/** Calculates all elements for g. */
	private void updateG() {
		for (int i = 0; i < parameters.length; i++) {
			g[i] = 0.0;
			for (int j = 0; j < data.length; j++) {
				g[i] += weights[j] * (data[j] - functionValues[j])
						* partialDerivative[i][j];
			}
		}
	}
	

	

	
	/** @return Compute weighted mean square error */
	public double chi2Goodness() {
		return  (chi2 / (double) (nbDataPoints - parameters.length));
	}
	
	
	
	/**
	 * Checks that the given array in not null, filled with zeros or contain negative weights.
	 * @return A valid weights array.
	 */
	protected void checkWeights(int length, double[] weights) {
		
		
		// check if all elements are zeros or if there are negative, NaN or Infinite elements
		
		boolean allZero = true;
		boolean illegalElement = false;
		for (int i = 0; i < weights.length && !illegalElement; i++) {
			if (weights[i] < 0 || Double.isNaN(weights[i]) || Double.isInfinite(weights[i])) 
			{
				throw new InvalidParameterException("Incorrect weight value at position " +i +" = " +weights[i]);
			}
			if(weights[i]>0.0)
			{
				allZero=false;
				nbDataPoints++;
			}
		}
		if(allZero)
			throw new InvalidParameterException("Incorrect weights : one weight must be strctly positiv at least");
		return ;
	}
	
	/**
	 * @return The covariance matrix of the fit parameters.
	 */
	public double[][] getCovarianceMatrixOfStandardErrorsInParameters()  {
		double[][] result = new double[parameters.length][parameters.length];
		double oldLambda = lambda;
		lambda = 0;
		updateA();
	
		Matrix m = a.inverse();
		a.setMatrix(0, m.getRowDimension() - 1, 0, m.getColumnDimension() - 1, m);

		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result.length; j++) {
				result[i][j] = a.get(i, j);
			}
		}


		lambda = oldLambda;

		return result;
	}
	
	/**
	 * Compute error for each parameter (deviation, no correlation info)
	 * @return The estimated standard errors of the fit parameters.
	 */
	public double[] getStandardErrorsOfParameters()  {
		//double[][] cov = getCovarianceMatrixOfStandardErrorsInParameters();

		double[] result = new double[parameters.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Math.sqrt(covarianceMatrix[i][i]);
		}
		return result;
	}
	

	/**
	 * small helper to compute result = m * vector
	 * @param m matrix
	 * @param vector input vector
	 * @param result m * vector
	 */
	private  static void matrixMultiply(Matrix m,double[] vector, double[] result) {
		for (int i = 0; i < m.getRowDimension(); i++) {
			result[i] = 0;
			for (int j = 0; j < m.getColumnDimension(); j++) {
				 result[i] += m.get(i, j) * vector[j];
			}
		}
	}


	/**
	 * @return the verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}


	/**
	 * @param verbose the verbose to set
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}


	/**
	 * Get the initial lambda factor damping tau
	 * @return the tau
	 */
	public double getTau() {
		return tau;
	}


	/**
	 * Set the initial lambda factor tau (typical value between 1e-1 to 1e-4), default 1e-1
	 * Positive value only
	 * @param tau the tau to set
	 */
	public void setTau(double tau) {
		this.tau = (tau>0)?tau:this.tau;
	}


	/**
	 * Stop condition 1 gradient too low default is 10e-16
	 * @return the epsilon1
	 */
	public double getEpsilon1() {
		return epsilon1;
	}


	/**
	 * Stop condition 1 gradient too low, default is 10e-16
	 * Positive value only
	 * @param epsilon1 the epsilon1 to set
	 */
	public void setEpsilon1(double epsilon1) {
		this.epsilon1 = (epsilon1>0)?epsilon1:this.epsilon1;
	}


	/**
	 * Stop condition 2 magnitude difference between new parameters and old parameters too low, default is 10e-16
	 * @return the epsilon2
	 */
	public double getEpsilon2() {
		return epsilon2;
	}


	/**
	 * Stop condition 2 magnitude difference between new parameters and old parameters too low, default is 10e-16
	 * Positive value only
	 * @param epsilon2 the epsilon2 to set
	 */
	public void setEpsilon2(double epsilon2) {
		this.epsilon2 = (epsilon2>0)?epsilon2:this.epsilon2;
	}


	/**
	 * Stop condition 3 square error is very low : chi2 <= epsilon3, default is 10e-16
	 * @return the epsilon3
	 */
	public double getEpsilon3() {
		return epsilon3;
	}


	/**
	 * Stop condition 3 square error is very low : chi2 <= epsilon3, default is 10e-16
	 * @param epsilon3 the epsilon3 to set
	 */
	public void setEpsilon3(double epsilon3) {
		this.epsilon3 = (epsilon3>0)?epsilon3:this.epsilon3;
	}


	/**
	 * Is it a success or not ?
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}


	/**
	 * Tells you the event which has caused the end of the algorithm
	 * @return the stopEvent
	 */
	public StopEvent getStopEvent() {
		return stopEvent;
	}


	/**
	 * Maximum iterations limit
	 * @return the maxIterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}


	/**
	 * Stop conditions on the number of iterations
	 * @param maxIterations the maxIterations to set
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = (maxIterations>0)?maxIterations:this.maxIterations;
	}


	/**
	 * Covariance matrix of errors i.e. (JtJ)^-1
	 * @return the covarianceMatrix
	 */
	public double[][] getCovarianceMatrix() {
		return covarianceMatrix;
	}


	/**
	 * Standard deviation for each parameter (square root of the diagonal terms of the covariance matrix) 
	 * @return the standardError
	 */
	public double[] getStandardError() {
		return standardError;
	}


	/**
	 * Final normalized quadratic error 
	 * @return the finalNormalizedChi2
	 */
	public double getFinalNormalizedChi2() {
		return finalNormalizedChi2;
	}
	
}
