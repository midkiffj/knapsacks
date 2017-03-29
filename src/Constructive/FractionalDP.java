package Constructive;

import java.util.ArrayList;
import java.util.Random;

import Problems.Fractional;
import Runner.RndGen;
import Solutions.FractionalSol;
import Solutions.ProblemSol;

/**
 * Dynamic Programming heuristic for generating an incumbent solution
 * 
 * @author midkiffj
 */
public class FractionalDP extends ConstHeuristic {

	private int n;
	private Random rnd = RndGen.getRnd();
	private Fractional fract;

	/**
	 * Specify the problem to solve
	 * 
	 * @param c Cubic problem
	 */
	public FractionalDP(Fractional f) {
		this.fract = f;
		n = fract.getN();
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
	private FractionalSol dpHeuristic() {
		// Initialize Arrays/Lists
		int b = fract.getB();
		double[] f = new double[b+1];
		long[][] num = new long[b+1][fract.getM()];
		long[][] den = new long[b+1][fract.getM()];
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] B = new ArrayList[b+1];
		for (int i = 0; i < B.length; i++) {
			B[i] = new ArrayList<Integer>();
		}

		// Iterate over all items
		for (int k = 0; k < n; k++) {
			int ak = fract.getA(k);
			// Check all weights where the item could fit
			for (int r = b; r >= ak; r--) {
				int weight = r-ak;
				// Check profit and update f and B
				double Beta = profit(k,num[weight],den[weight]);
				if (Beta > f[r]) {
					f[r] = Beta;
					B[r] = new ArrayList<Integer>(B[weight]);
					B[r].add(k);
					num[r] = addNum(k,num[weight]);
					den[r] = addDen(k,den[weight]);
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

//		return new FractionalSol(x,r);
		return localSearch(new FractionalSol(x,r));
	}

	/**
	 * Calculate the change in objective value 
	 *   by adding item k with the items in B.
	 */
	private double profit(int i, long[] num, long[] den) {
		double obj = 0;
		for (int j = 0; j < fract.getM(); j++) {
			if (den[j]+fract.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]+fract.getC(j,i))/(den[j]+fract.getD(j,i));
		}

		return obj;
	}
	
	/**
	 * Calculate the numerator values if item i is added
	 * 
	 * @param i - item to add
	 * @param num - numerator value
	 * @return calculated objective
	 */
	private long[] addNum(int i, long[] num) {
		long[] newNum = new long[num.length];
		for (int k = 0; k < fract.getM(); k++) {
			newNum[k] = num[k] + fract.getC(k,i);
		}
		return newNum;
	}
	
	/**
	 * Calculate the denominator values if item i is added
	 * 
	 * @param i - item to add
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private long[] addDen(int i, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < fract.getM(); k++) {
			newDen[k] = den[k] + fract.getD(k,i);
		}
		return newDen;
	}

	/**
	 * Complete best improving swaps (and a shift) 
	 *   until the objective value is no longer improved
	 *   
	 * @param current the solution to improve
	 */
	private FractionalSol localSearch(FractionalSol current) {
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
			tryAdd(current);
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
	private void bestImprovingSwap(FractionalSol current) {
		// Occasionally perform a shift
		if (rnd.nextDouble() < 0.6) {
			FractionalSol shift= new FractionalSol(current);
			int change = shift.shift();
			if (change != -1 && shift.compareTo(current) > 0) {
				current = shift;
				return;
			}
		}
		// Store b
		int b = fract.getB();
		int curTotalA = current.getTotalA();
		// Store best swaps
		int bi = -1;
		int bj = -1;
		double bObj = current.getObj();
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (fract.getA(j)-fract.getA(i) <= b - curTotalA) {
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
	
	/**
	 * Try to add a variable to the solution, maintaining knapsack feasibility
	 * 
	 * @param current solution to improve
	 */
	private void tryAdd(FractionalSol current) {
		double maxChange = 0;
		int maxI = -1;
		// Check all possible shifts
		for(Integer i: current.getR()) {
			// Knapsack feasibility
			if (current.addValid(i)) {
				double obj = addObj(i,current.getNum(),current.getDen());
				// Track best improving addition
				double change = obj - current.getObj();
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		if (maxI != -1) {
			current.addX(maxI);
		}
	}
	
	/**
	 * Calculate the objective if item i is added to the solution
	 * 
	 * @param i - item to add
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double addObj(int i, long[] num, long[] den) {
		double obj = 0;
		for (int j = 0; j < fract.getM(); j++) {
			if (den[j]+fract.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]+fract.getC(j,i))/(den[j]+fract.getD(j,i));
		}

		return obj;
	}

}
