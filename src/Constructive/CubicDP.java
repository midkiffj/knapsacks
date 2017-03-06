package Constructive;

import java.util.ArrayList;
import java.util.Random;

import Problems.Cubic;
import Runner.RndGen;
import Solutions.CubicSol;
import Solutions.ProblemSol;

/**
 * Dynamic Programming heuristic for generating an incumbent solution
 * 
 * @author midkiffj
 */
public class CubicDP extends ConstHeuristic {

	private int n;
	private Random rnd = RndGen.getRnd();
	private Cubic c;

	/**
	 * Specify the problem to solve
	 * 
	 * @parm c Cubic problem
	 */
	public CubicDP(Cubic c) {
		this.c = c;
		n = c.getN();
	}

	/**
	 * Perform the construction of the solution
	 * 
	 * @return solution constructed
	 */
	protected ProblemSol construct() {
		return dpHeuristic();
	}

	/**
	 * Generate an incumbent solution using DP
	 * - Changes the algorithm to use a list instead of an array
	 */
	private CubicSol dpHeuristic() {
		// Initialize Arrays/Lists
		int b = c.getB();
		long[] f = new long[b+1];
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] B = new ArrayList[b+1];
		for (int i = 0; i < B.length; i++) {
			B[i] = new ArrayList<Integer>();
		}

		// Iterate over all items
		for (int k = 0; k < n; k++) {
			int ak = c.getA(k);
			// Check all weights where the item could fit
			for (int r = b; r >= ak; r--) {
				int weight = r-ak;
				// Check profit and update f and B
				long Beta = f[weight] + profit(k,B[weight]);
				if (Beta > f[r]) {
					f[r] = Beta;
					B[r] = new ArrayList<Integer>(B[weight]);
					B[r].add(k);
				}
			}
		}

		// Find the maximum objective value
		int rmax = 0;
		for (int r = 1; r < f.length; r++) {
			if (f[r] >= f[rmax]) {
				rmax = r;
			}
		}

		// Return the solution that made the max obj value
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < n; i++) {
			r.add(i);
		}
		x.addAll(B[rmax]);
		r.removeAll(B[rmax]);

		return localSearch(new CubicSol(x,r));
	}

	/**
	 * Calculate the change in objective value 
	 *   by adding item k with the items in B.
	 */
	private long profit(int k, ArrayList<Integer> B) {
		long oldObj = c.getCi(k);
		for (int i = 0; i < B.size(); i++) {
			int xi = B.get(i);
			oldObj = oldObj + c.getCij(xi,k);
			for (int l = i+1; l < B.size(); l++) {
				int xl = B.get(l);
				oldObj = oldObj + c.getDijk(xi,xl,k);
			}
		}
		return oldObj;
	}

	/**
	 * Complete best improving swaps (and a shift) 
	 *   until the objective value is no longer improved
	 *   
	 * @param current the solution to improve
	 */
	private CubicSol localSearch(CubicSol current) {
		double curObj = current.getObj();
		bestImprovingSwap(current);
		boolean done = false;
		while (!done) {
			// Complete swaps until no improving swap found
			while (current.getObj() > curObj) {
				curObj = current.getObj();
				bestImprovingSwap(current);
			}
			curObj = current.getObj();
			// Attempt an improving shift
			CubicSol shift = new CubicSol(current);
			int change = shift.shift();
			if (change != -1 && shift.compareTo(current) > 0) {
				current = shift;
			}
			// Stop if no change from swapping and shifting
			if (current.getObj() <= curObj) {
				done = true;	
			}
		}
		return current;
	}

	/**
	 * Perform the best improving swap that keeps the knapsack feasible
	 * 
	 * @param current the solution to improve
	 */
	private void bestImprovingSwap(CubicSol current) {
		// Occasionally perform a shift
		if (rnd.nextDouble() < 0.6) {
			CubicSol shift= new CubicSol(current);
			int change = shift.shift();
			if (change != -1 && shift.compareTo(current) > 0) {
				current = shift;
				return;
			}
		}
		// Store b
		int b = c.getB();
		int curTotalA = current.getTotalA();
		// Store best swaps
		int bi = -1;
		int bj = -1;
		double bObj = current.getObj();
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= b - curTotalA) {
					double newObj = current.swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		// Complete the best improving swap (if found)
		if (bi != -1) {
			current.swap(bi,bj);
		}
	}
}
