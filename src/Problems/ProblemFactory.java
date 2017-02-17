package Problems;

import java.util.ArrayList;

import Solutions.*;

public class ProblemFactory {

	private static Problem problem;
	
	private ProblemFactory() {
		
	}
	
	public static void setProblem(Problem problem) {
		ProblemFactory.problem = problem;
	}
	
	public static Problem getProblem() {
		return problem;
	}
	
	public static ProblemSol genRndSol() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		problem.genRndInit(x, r);
		if (problem instanceof Cubic) {
			return new CubicSol(x,r);
		} else if (problem instanceof MaxProbability) {
			return new MaxProbabilitySol(x,r);
		} else if (problem instanceof Fractional) {
			return new FractionalSol(x,r);
		}
		return null;
	}
	
	public static ProblemSol genInitSol() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		problem.genInit(x, r);
		if (problem instanceof Cubic) {
			return new CubicSol(x,r);
		} else if (problem instanceof MaxProbability) {
			return new MaxProbabilitySol(x,r);
		}else if (problem instanceof Fractional) {
			return new FractionalSol(x,r);
		}
		return null;
	}
}
