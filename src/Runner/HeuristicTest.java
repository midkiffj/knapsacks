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
		String file = "incumbents/cubic/30_0.5_false_1inc.txt";
		Problem p = new Cubic(file);
		ProblemSol ps = new CubicSol(file);

		testAll(ps);
	}

	private static void testAll(ProblemSol ps) {
		// Set Healing
		ps.setHealing(false);
		System.out.println(ps.getValid());

		// Run all heuristics
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
