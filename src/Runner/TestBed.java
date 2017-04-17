package Runner;

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
	private static boolean f = false;
	private static boolean mp = false;

	/**
	 * Runs the selected problems
	 */
	public static void main(String[] args) {
		if (c) {
			mainCubic();
		}
		if (cm) {
			mainCubicMult();
		}
		if (f) {
			mainFractional();
		}
		if (mp) {
			mainMaxProb();
		}
	}

	/**
	 * Run the cubic test bed
	 * - g: generate the test bed
	 * - rh: run the metaheuristics
	 * - mip: run the MIP formulation
	 * - log: use the testlogger
	 * - inc: run and time the incumbent generator
	 * - constr: run the constructive heuristics
	 * - heal: run the metaheuristics with healing toggled on
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
		} catch (Exception e) {
			System.err.println("Error Running Cubic: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Run the cubic multiple knapsack test bed
	 * - g: generate the test bed
	 * - rh: run the metaheuristics
	 * - mip: run the MIP formulation
	 * - log: use the testlogger
	 * - constr: run the constructive heuristics
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
		} catch (Exception e) {
			System.err.println("Error Running CubicMult: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Run the maximum probability test bed
	 * - g: generate the test bed
	 * - rh: run the metaheuristics
	 * - mip: run the MIP formulation
	 * - log: use the testlogger
	 * - runGA: run only the genetic algorithm
	 * - constr: run the umax constructive heuristics
	 */
	private static void mainMaxProb() {
		boolean g = false;
		boolean rh = false;
		boolean mip = false;
		boolean log = false;
		boolean runGA = false;
		boolean constr = false;
		MaxProbTest mpt = new MaxProbTest(g,rh,mip,log);
		try {
			mpt.run();
			if (runGA) {
				mpt.runMaxProbGA();
			}
			if (constr) {
				mpt.runMaxProbConst();
			}
		} catch (Exception e) {
			System.err.println("Error Running Max Prob: " + e.getMessage());
			e.printStackTrace();

		}
	}

	/**
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
		boolean constr = false;
		FractionalTest ft = new FractionalTest(g,rh,mip,log);
		try {
			ft.run();
			if (constr) {
				ft.runConstructive();
			}
		} catch (Exception e) {
			System.err.println("Error Running Fractional: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
