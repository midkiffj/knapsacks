package Runner;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Superclass for testing problems
 * - Stores dummy array list for testing objective calculations
 * - Initializes dummy hashmap for tested objective values while testing
 * - Toggles the test logger usage
 * 
 * @author midkiffj
 */
public abstract class ProblemTest {
	public static HashMap<String,Double> testObj;
	public static ArrayList<Integer> test;

	/**
	 * Create dummy variables and sets the logger usage
	 * 
	 * @param useLog - (T) log the testing, (F) don't log
	 */
	public ProblemTest(boolean useLog) {
		testObj = new HashMap<String,Double>();
		test = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			test.add(i);
		}
		TestLogger.setUseLog(useLog);
	}
	
	/**
	 * Run the specified methods on the test bed
	 * 
	 * @throws FileNotFoundException
	 */
	public abstract void run() throws FileNotFoundException;
	
	/**
	 * Generate the test bed
	 */
	public abstract void generate();
	
	/**
	 * Run the test bed on the Metaheuristics
	 * 
	 * @throws FileNotFoundException
	 */
	public abstract void runHeuristics() throws FileNotFoundException;
	
	/**
	 * Solve the MIP formulation for the problem
	 * 
	 * @throws FileNotFoundException
	 */
	public abstract void runMIP() throws FileNotFoundException;
	
}
