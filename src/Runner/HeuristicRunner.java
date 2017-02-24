package Runner;

import Heuristics.*;
import Solutions.ProblemSol;

/**
 * Heuristic Runner 
 * - Runs all heuristics on a given problem
 * - Creates a comma-delimited string of the heuristic best solutions 
 * 
 * @author midkiffj
 *
 */
public class HeuristicRunner {
	
	private String results;

	public HeuristicRunner(ProblemSol ps) {
		// Update logger for each heuristic and run
		TestLogger.setLogger("GA");
		genAlgo ga = new genAlgo(ps,-1,-1);
		System.out.println("--GA--");
		String gas = run(ga);
		
		TestLogger.setLogger("SA");
		simAnneal sa = new simAnneal(ps,-1,-1);
		System.out.println("--SA--");
		String sas = run(sa);
		
		TestLogger.setLogger("ST");
		simTabu st = new simTabu(ps,-1,-1);
		System.out.println("--ST--");
		String sts = run(st);

		TestLogger.setLogger("TS");
		tabuSearch ts = new tabuSearch(ps,-1,-1);
		System.out.println("--TS--");
		String tss = run(ts);
		
		// Compile heuristic results
		results = "";
		results += gas + ",";
		results += sas + ",";
		results += sts + ",";
		results += tss + ",";
	}
	
	public String getResults() {
		return results;
	}
	
	/*
	 * Runs a given heuristic and returns the best solution
	 */
	private String run(Metaheuristic m) {
		long start = System.nanoTime();
		m.run();
		long end = System.nanoTime();
		double duration = (double)(end-start)/60000000000L;
		System.out.println("Time taken(min): " + duration);
		String best = ""+m.getBestObj();
		
		// Detect invalid solution
		ProblemSol ps = m.getBest();
		if (ps.getValid()) {
			return best;
		} else {
			System.out.println("Invalid Solution found: " + ps.getObj());
			return "INVALID";
		}
	}
}
