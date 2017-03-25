package Constructive;

import java.util.ArrayList;

import ExactMethods.Knapsack_Frac;
import Problems.MaxProbability;
import Solutions.ProblemSol;
import Solutions.MaxProbabilitySol;

/**
 * UMax heuristic for generating an incumbent solution to the Max Probability
 * 
 * @author midkiffj
 */
public class MaxProbUMax extends ConstHeuristic {

	private int n;
	private MaxProbability mp;
	private boolean useCplex;

	private double num;
	private double den;
	private int b;
	private int[] a;
	private int[] u;
	private int[] s;
	private double t;

	/**
	 * Specify the problem to solve
	 * 
	 * @param mp Max Probability problem
	 */
	public MaxProbUMax(MaxProbability mp, boolean useCplex) {
		this.mp = mp;
		n = mp.getN();
		this.useCplex = useCplex;
		b = mp.getB();
		a = new int[n];
		u = new int[n];
		s = new int[n];
		for (int i = 0; i < n; i++) {
			a[i] = mp.getA(i);
			u[i] = mp.getU(i);
			s[i] = mp.getS(i);
		}
		t = mp.getT();
	}

	/**
	 * Perform the construction of the solution
	 * 
	 * @return solution constructed
	 */
	protected ProblemSol construct() {
		return uMaxHeuristic();
	}

	private ProblemSol uMaxHeuristic() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		// Clear lists
		// Clear lists
		r.clear();
		x.clear();
		int totalAx = 0;
		int totalUx = 0;
		// Solve for max{u*x : Ax <= b, x free}
		Knapsack_Frac k = new Knapsack_Frac(a,b,u,useCplex);
		boolean[] uMaxXVals = k.getXVals();
		for (int i = 0; i < n; i++) {
			if (uMaxXVals[i]) {
				x.add(i);
				totalAx = totalAx + a[i];
				totalUx = totalUx + u[i];
			} else {
				r.add(i);
			}
		}
		// Update objective
		double curObj = mp.getObj(x);
		num = mp.getNum();
		den = mp.getDen();

		// Check for Swaps and shifts
		boolean swapping = true;
		while (swapping) {
			int maxI = -1;
			int maxJ = -1;
			double maxChange = 0;
			// Check all swaps
			for(Integer xi: x) {
				for(Integer xj: r) {
					// Check for knapsack feasibility
					if (a[xj]-a[xi] <= b - totalAx && u[xj]-u[xi] >= t - totalUx) {
						// Calculate new objective
						double newObj = swapObj(xi, xj, x, curObj);
						double change = newObj - curObj;
						// Update best swap
						if (change > maxChange) {
							maxI = xi;
							maxJ = xj;
							maxChange = change;
						}
					}
				}
			}
			double[] add = tryAdd(totalAx,r,curObj);
			double[] sub = trySub(totalUx,x,curObj);
			double addChange = add[0];
			double subChange = sub[0];
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				totalAx = totalAx + a[addI];
				totalUx = totalUx + u[addI];
				num = num + u[addI];
				den = den + s[addI];
				curObj = (num*num)/den;
			} else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				totalAx = totalAx - a[subI];
				totalUx = totalUx - u[subI];
				num = num - u[subI];
				den = den - s[subI];
				curObj = (num*num)/den;
			} else {
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					totalAx = totalAx + a[maxJ] - a[maxI];
					totalUx = totalUx + u[maxJ] - u[maxI];
					num = num + u[maxJ] - u[maxI];
					den = den + s[maxJ] - s[maxI];
					curObj = (num*num)/den;
				}
			}
		}
		return new MaxProbabilitySol(x,r);
	}

	/**
	 * Find the variable that most improves the objective when removed
	 *
	 * @param x - items in the solution
	 * @param curObj - the current objective to improve upon
	 * @return {change in objective,item to add} or {0,-1} if no improving shift found
	 */
	private double[] trySub(int totalU, ArrayList<Integer> x, double curObj) {
		double maxChange = 0;
		int minI = -1;
		// Check all removals
		for (Integer i: x) {
			if (totalU - u[i] >= t) {
				// Calculate the change in objective
				double newObj = subObj(i, num, den);
				double change = newObj - curObj;
				// Update best change
				if (change < maxChange) {
					maxChange = change;
					minI = i;
				}
			}
		}
		// Return the best improving removal
		double[] success = {maxChange, minI};
		return success;
	}

	/**
	 * Find the variable that most improves the objective when added
	 *
	 * @param totalA - current weight of knapsack
	 * @param r - items outside solution
	 * @param curObj - the current objective to improve upon
	 * @return {change in objective,item to add} or {0,-1} if no improving shift found
	 */
	private double[] tryAdd(int totalA, ArrayList<Integer> r, double curObj) {
		double maxChange = 0;
		int maxI = -1;
		// Check all additions
		for (Integer i: r) {
			if (totalA + a[i] <= b) {
				// Calculate the change in objective
				double newObj = addObj(i, num, den);
				double change = newObj - curObj;
				// Update best change
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		// Return the best improving addition
		double[] add = {maxChange, maxI};
		return add;
	}

	/**
	 * Calculate the objective if item i is removed from the solution 
	 * 	and item j is added to the solution.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @return calculated objective
	 */
	public double swapObj(int i, int j) {
		double num = swapNum(i, j, this.num);
		double den = swapDen(i, j, this.den);
		return (num*num)/den;
	}

	/**
	 * Calculate the numerator values if item i is removed 
	 * 	and item j is added to the value.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param num - numerator value
	 * @return calculated objective
	 */
	private double swapNum(int i, int j, double num) {
		return num + mp.getU(j) - mp.getU(i);
	}

	/**
	 * Calculate the denominator values if item i is removed 
	 * 	and item j is added to the value.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double swapDen(int i, int j, double den) {
		return den + mp.getS(j) - mp.getS(i);
	}

	/**
	 * Calculate the new objective if 
	 * 	item i is removed and item j is added to the solution.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param curX - current solution
	 * @param oldObj - current objective
	 * @return new objective value
	 */
	private double swapObj(int i, int j, ArrayList<Integer> x, double oldObj) {
		ArrayList<Integer> newX = new ArrayList<Integer>(x);
		newX.remove(Integer.valueOf(i));
		newX.add(j);
		return mp.getObj(newX, false);
	}

	/**
	 * Calculate the objective if i is removed. 
	 * 	Calculation done without changes to num/den.
	 * 
	 * @param i - item to be removed
	 * @param num - current numerator value
	 * @param den - current denominator value
	 * @return the new objective
	 */
	private double subObj(int i, double num,
			double den) {
		num -= u[i];
		den -= s[i];
		return (num*num)/den;
	}

	/**
	 * Calculate the objective if i is added. 
	 * 	Calculation done without changes to num/den.
	 * 
	 * @param i - item to be added
	 * @param num - current numerator value
	 * @param den - current denominator value
	 * @return the new objective
	 */
	private double addObj(int i, double num,
			double den) {
		num += u[i];
		den += s[i];
		return (num*num)/den;
	}
}