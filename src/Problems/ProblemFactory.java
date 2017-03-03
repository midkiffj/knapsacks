package Problems;

import java.util.ArrayList;

import Solutions.*;

/**
 * ProblemFactory used to store the current problem
 * - Accessor/Setter for problem
 * - Generate solutions for the problem
 * 
 * @author midkiffj
 */
public class ProblemFactory {

	// Global problem
	private static Problem problem;
	
	/**
	 * Update the global problem
	 * 
	 * @param problem
	 */
	public static void setProblem(Problem problem) {
		ProblemFactory.problem = problem;
	}
	
	/**
	 * Get the global problem.
	 * 
	 * @return problem to use
	 */
	public static Problem getProblem() {
		return problem;
	}
	
	/**
	 * Generate a random solution and return the correct ProblemSol sub-type
	 * 
	 * @return ProblemSol containing solution
	 */
	public static ProblemSol genRndSol() {
		// Generate Solution
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		problem.genRndInit(x, r);
		// Return correct solution based off of the problem
		if (problem instanceof Cubic) {
			return new CubicSol(x,r);
		} 
		else if (problem instanceof CubicMult) {
			return new CubicMultSol(x,r);
		} 
		else if (problem instanceof MaxProbability) {
			return new MaxProbabilitySol(x,r);
		} 
		else if (problem instanceof Fractional) {
			return new FractionalSol(x,r);
		}
		else if (problem instanceof Unconstrained) {
			return new UnconstrainedSol(x,r);
		}
		return null;
	}
	
	/**
	 * Generate an incumbent solution and return the correct ProblemSol sub-type
	 * 
	 * @return ProblemSol containing solution
	 */
	public static ProblemSol genInitSol() {
		// Generate the problem
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		problem.genInit(x, r);
		// Return correct solution based off of the problem
		if (problem instanceof Cubic) {
			return new CubicSol(x,r);
		} 
		else if (problem instanceof CubicMult) {
			return new CubicMultSol(x,r);
		} 
		else if (problem instanceof MaxProbability) {
			return new MaxProbabilitySol(x,r);
		}
		else if (problem instanceof Fractional) {
			return new FractionalSol(x,r);
		}
		else if (problem instanceof Unconstrained) {
			return new UnconstrainedSol(x,r);
		}
		return null;
	}
}
