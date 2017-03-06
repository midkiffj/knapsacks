package Runner;

import java.io.FileNotFoundException;

/**
 * Testing controller used to choose problems to run and how to run them.
 * 
 * Possible Problems:
 * - Cubic
 * - Cubic Multiple Knapsack
 * - Fractional
 * - Max Probability
 * 
 * @author midkiffj
 */
public class TestBed {
	
	// Select problem(s) to run
	private static boolean c = false;
	private static boolean cm = false;
	private static boolean mp = false;
	private static boolean f = false;

	/*
	 * Runs the selected problems
	 */
	public static void main(String[] args) {
		if (c) {
			mainCubic();
		}
		if (cm) {
			mainCubicMult();
		}
		if (mp) {
			mainMaxProb();
		}
		if (f) {
			mainFractional();
		}
	}

	/*
	 * Run the cubic test bed
	 * - g: generate the test bed
	 * - rh: run the metaheuristics
	 * - mip: run the MIP formulation
	 * - log: use the testlogger
	 * - inc: run and time the incumbent generator
	 * - constr: run the constructive heuristics
	 */
	private static void mainCubic() {
		boolean g = false;
		boolean rh = false;
		boolean mip = false;
		boolean log = false;
		boolean inc = false;
		boolean constr = false;
		boolean heal = false;
		CubicTest ct = new CubicTest(g,rh,mip,log);
		try {
			ct.run();
			if (inc) {
				ct.timeIncumbent();
			}
			if (constr) {
				ct.runConstructive();
			}
			if (heal) {
				ct.runHealHeuristics();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error Running Cubic: " + e.getMessage());
		}
	}
	
	/*
	 * Run the cubic multiple knapsack test bed
	 * - g: generate the test bed
	 * - rh: run the metaheuristics
	 * - mip: run the MIP formulation
	 * - log: use the testlogger
	 */
	private static void mainCubicMult() {
		boolean g = false;
		boolean rh = false;
		boolean mip = false;
		boolean log = false;
		boolean constr = false;
		CubicMultTest cmt = new CubicMultTest(g,rh,mip,log);
		try {
			cmt.run();
			if (constr) {
				cmt.runConstructive();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error Running Cubic: " + e.getMessage());
		}
	}

	/*
	 * Run the maximum probability test bed
	 * - g: generate the test bed
	 * - rh: run the metaheuristics
	 * - mip: run the MIP formulation
	 * - log: use the testlogger
	 * - runGA: run only the genetic algorithm
	 */
	private static void mainMaxProb() {
		boolean g = false;
		boolean rh = false;
		boolean mip = false;
		boolean log = false;
		boolean runGA = false;
		MaxProbTest mpt = new MaxProbTest(g,rh,mip,log);
		try {
			mpt.run();
			if (runGA) {
				mpt.runMaxProbGA();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error Running Cubic: " + e.getMessage());
		}
	}

	/*
	 * Run the fractional knapsack test bed
	 * - g: generate the test bed
	 * - rh: run the metaheuristics
	 * - mip: run the MIP formulation
	 * - log: use the testlogger
	 */
	private static void mainFractional() {
		boolean g = false;
		boolean rh = false;
		boolean mip = false;
		boolean log = false;
		FractionalTest ft = new FractionalTest(g,rh,mip,log);
		try {
			ft.run();
			
		} catch (FileNotFoundException e) {
			System.err.println("Error Running Cubic: " + e.getMessage());
		}
	}
}
