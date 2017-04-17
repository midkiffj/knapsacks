package Runner;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import Heuristics.*;
import Problems.*;
import Runner.TestLogger.sf;
import Solutions.*;

/**
 * Heuristic Tester
 * - Used to test individual problems with specific metaheuristics and check for validity.
 * - Logger can be set to use console or turned off
 * 
 * @author midkiffj
 *
 */
public class HeuristicTest {

	/**
	 * Setup a problem and test
	 * 
	 * @param args - not used
	 */
	public static void main(String[] args) {
		// Set logger to use console
		for(Handler h: TestLogger.logger.getHandlers()) {
			TestLogger.logger.removeHandler(h);
		}
		ConsoleHandler ch = new ConsoleHandler();
		TestLogger.logger.setUseParentHandlers(false);
		TestLogger.logger.addHandler(ch);
		sf formatter = new sf();  
		ch.setFormatter(formatter);


		// Update problem to test here
		String file = "SN-LD/100_1_false_1";
		@SuppressWarnings("unused")
		Problem p = new Fractional("problems/fractional/"+file);
		ProblemSol ps = new FractionalSol("incumbents/fractional/"+file+"inc.txt");


		testAll(ps);
	}

	/**
	 * Initialize a HeuristicRunner object with the given solution or
	 * 	run a single heuristic
	 * 
	 * @param ps - solution to run
	 */
	private static void testAll(ProblemSol ps) {
		// Set Healing
		ps.setHealing(false);
		System.out.println(ps.getValid());

		// Run all heuristics (toggle)
		boolean runAll = true;

		if (runAll) {
			HeuristicRunner hr = new HeuristicRunner(ps);
			System.out.println(hr.getResults());
		}
		// Or pick one
		else {
			// Change desired heuristic here
			Metaheuristic h = new tabuSearch(ps,-1,60000000000L*1);
			h.run();
			ProblemSol ps2 = h.getBest();
			if (ps2.getValid()) {
				System.out.println("Valid");
			}
			System.out.println(ps.getObj());
		}
	}
}
