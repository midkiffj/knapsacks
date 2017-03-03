package Constructive;

import Solutions.ProblemSol;

/**
 * Super class for constructive heuristics.
 * - Sub classes implement 'construct()'
 * - Tracks and stores time + result
 * 
 * @author midkiffj
 */
public abstract class ConstHeuristic {

	// Solution made by construction
	public ProblemSol result;
	
	// Time to complete the construction
	private double timeTaken;
	
	public ConstHeuristic() {
		timeTaken = -1;
	}
	
	public ProblemSol getResult() {
		return result;
	}
	
	public double getTime() {
		return timeTaken;
	}
	
	// Time and store the result of the construction heuristic
	public void run() {
		long start = System.nanoTime();
		result = construct();
		long end = System.nanoTime();
		timeTaken = (double)(end-start)/60000000000L;
	}
	
	protected abstract ProblemSol construct();
}
