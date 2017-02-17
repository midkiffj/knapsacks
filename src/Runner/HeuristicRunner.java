package Runner;

import java.util.logging.Level;

import Heuristics.*;
import Solutions.ProblemSol;

public class HeuristicRunner {
	
	private String results;

	public HeuristicRunner(ProblemSol ps) {
		
		TestLogger.setLogger("GA");
		genAlgo ga = new genAlgo(ps,-1,-1);
		System.out.println("--GA--");
		String gas = run(ga);
//		String gas = "";
		
		TestLogger.setLogger("SA");
		simAnneal sa = new simAnneal(ps,-1,-1);
		System.out.println("--SA--");
		String sas = run(sa);
//		String sas = "";
		
		TestLogger.setLogger("ST");
		simTabu st = new simTabu(ps,-1,-1);
		System.out.println("--ST--");
		String sts = run(st);
//		String sts = "";

		TestLogger.setLogger("TS");
		tabuSearch ts = new tabuSearch(ps,-1,-1);
		System.out.println("--TS--");
		String tss = run(ts);
//		String tss = "";
		
		
		results = "";
		results += gas + ",";
		results += sas + ",";
		results += sts + ",";
		results += tss + ",";
	}
	
	public String getResults() {
		return results;
	}
	
	private String run(Metaheuristic m) {
		m.run();
		String best = ""+m.getBestObj();
		ProblemSol ps = m.getBest();
		if (ps.getValid()) {
			return best;
		} else {
			System.out.println("Invalid Solution found: " + ps.getObj());
			return "INVALID";
		}
	}
}
