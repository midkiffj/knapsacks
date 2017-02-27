package Solutions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import Problems.Problem;
import Problems.ProblemFactory;
import Runner.RndGen;

/**
 * Superclass for problem solutions
 * - Keeps track of problem object and problem size
 * - Sets usage of healing/repair algorithms
 * 
 * @author midkiffj
 */
public abstract class ProblemSol implements Comparable<ProblemSol>, Comparator<ProblemSol>{

	static Problem p = ProblemFactory.getProblem();
	static int n = p.getN();
	static boolean useHealing = false;
	static Random rnd = RndGen.getRnd();
	
	private ArrayList<Integer> x;
	private ArrayList<Integer> r;
	private boolean[] xVals;
	private boolean valid;
	private double obj;

	public ProblemSol() {
		updateProblem();
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		xVals = new boolean[n];
	}

	private void updateProblem() {
		p = ProblemFactory.getProblem();
		n = p.getN();
	}

	public void setHealing(boolean useHeal) {
		useHealing = useHeal;
	}

	public boolean getHealing() {
		return useHealing;
	}

	//**********************
	// Access/Setter Methods
	//**********************

	/*
	 * Return the solution objective
	 */
	public double getObj() {
		return obj;
	}
	
	/*
	 * Set the solution objective
	 */
	public void setObj(double newObj) {
		obj = newObj;
	}

	/*
	 * Get the entire solution array
	 */
	public ArrayList<Integer> getX() {
		return x;
	}
	
	/*
	 * Set the entire solution array
	 */
	public void setX(ArrayList<Integer> newX) {
		x = new ArrayList<Integer>(newX);
	}

	/*
	 * Get the entire unused variable array
	 */
	public ArrayList<Integer> getR() {
		return r;
	}
	
	/*
	 * Set the entire solution array
	 */
	public void setR(ArrayList<Integer> newR) {
		r = new ArrayList<Integer>(newR);
	}

	/*
	 * Return the size of the solution
	 */
	public int getXSize() {
		return x.size();
	}

	/*
	 * Return the amount of unused variables
	 */
	public int getRSize() {
		return r.size();
	}

	/*
	 * Return xVals[i] where xVals[i] is true iff i is in the solution
	 */
	public boolean getXVals(int i) {
		return xVals[i];
	}
	
	/*
	 * Set xVals[i] to the specified boolean
	 */
	public void setXVals(int i, boolean val) {
		xVals[i] = val;
	}
	
	/*
	 * Return if the solution is feasible/valid
	 */
	public boolean getValid() {
		updateValid();
		return valid;
	}
	
	/*
	 * Set the problem validity
	 */
	public void setValid(boolean newValid) {
		valid = newValid;
	}
	
	/*
	 * Update the problem validity
	 */
	public abstract void updateValid();

	
	//*****************
	// Mutation Methods
	//*****************
	
	/*
	 * Add variable i to the solution
	 */
	public void addI(int i) {
		xVals[i] = true;
		x.add(i);
		r.remove(Integer.valueOf(i));
	}
	
	/*
	 * Remove variable i from the solution
	 */
	public void removeI(int i) {
		xVals[i] = false;
		r.add(i);
		x.remove(Integer.valueOf(i));
	}
	
	/*
	 * Swap item i out of the solution and item j into the solution
	 */
	public abstract void swap(int i, int j);
	
	/*
	 * Return a mutation
	 */
	public abstract ProblemSol mutate();

	/*
	 * Return the best mutation for the solution
	 */
	public abstract ProblemSol bestMutate();
	
	/*
	 * Return the tabu and non-tabu mutations for a solution
	 * - arr[0] is the result of a tabu mutation
	 * - arr[1] is the result of a non-tabu mutation
	 */
	public abstract ProblemSol[] tabuMutate(int iteration, int[][] tabuList);
	
	/*
	 * Return the best tabu and best non-tabu mutations for a solution
	 * - arr[0] is the result of a tabu mutation
	 * - arr[1] is the result of a non-tabu mutation
	 */
	public abstract ProblemSol[] tabuBestMutate(int iteration, int[][] tabuList);

	/*
	 * Attempt to perform a shift 
	 * - Return the new solution if a shift occurs
	 * - null otherwise
	 */
	public abstract int shift();

	/*
	 * Perform a genetic algorithm crossover and return the new solution
	 */
	public abstract ProblemSol crossover(ProblemSol ps2);

	/*
	 * Perform a genetic algorithm mutation and return the new solution
	 */
	public abstract ProblemSol genMutate(int removeAttempts);
	
	/*
	 * Heal/Repair the solution by making it feasible
	 */
	public abstract void healSol();

	/*
	 * Used to duplicate a solution to 
	 * 	avoid manipulation through pass-by-reference
	 */
	public static ProblemSol copy(ProblemSol ps) {
		// Cubic
		if (ps instanceof CubicSol) {
			return new CubicSol((CubicSol)ps);
		} 
		// Cubic Multiple Knapsack
		else if (ps instanceof CubicMultSol) {
			return new CubicMultSol((CubicMultSol)ps);
		} 
		// Unconstrained Cubic
		else if (ps instanceof UnconstrainedSol) {
			return new UnconstrainedSol((UnconstrainedSol)ps);
		} 
		// Max Probability
		else if (ps instanceof MaxProbabilitySol) {
			return new MaxProbabilitySol((MaxProbabilitySol)ps);
		} 
		// Fractional
		else if (ps instanceof FractionalSol) {
			return new FractionalSol((FractionalSol)ps);
		}
		return null;
	}

	@Override
	public abstract int compareTo(ProblemSol o);

	@Override
	/*
	 * Two solutions are equal if:
	 * - Same objective AND solution
	 */
	public boolean equals(Object object) {
		ProblemSol ks2 = (ProblemSol)object;
		// If same objective,
		if (this.getObj() == ks2.getObj()) {
			// Check for same solution
			for (int i = 0; i < p.getN(); i++) {
				// If solution differs, they're not equal
				if (this.getXVals(i) != ks2.getXVals(i)) {
					return false;
				}
			}
			return true;
		} else {
			// Check for same solution, but different objective
			boolean allSame = true;
			for (int i = 0; i < p.getN(); i++) {
				if (this.getXVals(i) != ks2.getXVals(i)) {
					allSame = false;
				}
			}
			// Report incorrect solutions
			if (allSame) {
				System.err.println("Duplicate solution with different objective");
			}
		}
		return false;
	}

	@Override
	public int compare(ProblemSol o1, ProblemSol o2) {
		return o1.compareTo(o2);
	}
	
	//************
	// File I/O
	//************
	public abstract void writeSolution(String filename);
	
	public abstract void readSolution(String filename);
}
