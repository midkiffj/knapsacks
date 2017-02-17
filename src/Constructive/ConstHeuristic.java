package Constructive;

import Solutions.ProblemSol;

public abstract class ConstHeuristic {

	// Solution made by construction
	public ProblemSol result;
	
	// Time to complete the construction
	private long timeTaken;
	
	public ConstHeuristic() {}
	
	public ProblemSol getResult() {
		return result;
	}
	
	public long getTime() {
		return timeTaken;
	}
	
	// Time and store the result of the construction
	public void run() {
		long start = System.nanoTime();
		result = construct();
		long end = System.nanoTime();
		timeTaken = end-start;
	}
	
	protected abstract ProblemSol construct();
}
