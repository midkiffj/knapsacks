package Constructive;

import java.util.ArrayList;

import Problems.Fractional;
import Solutions.FractionalSol;
import Solutions.ProblemSol;

/**
 * FractionalIncumbent performs the Fill Up and Exchange Extension of Forrester 
 * 	on the 0-1 fractional
 * 
 * @author midkiffj
 */
public class FractionalIncumbent extends ConstHeuristic {

	private Fractional f;
	
	/**
	 * Set the problem to solve
	 * @param f - Fractional
	 */
	public FractionalIncumbent(Fractional f) {
		this.f = f;
	}
	
	@Override
	/**
	 * Construct a solution to the problem
	 */
	protected ProblemSol construct() {
		return fillUpAndExchangeExtension();
	}
	
	/**
	 * Perform the construction, returning the solution result
	 * @return FractionalSol of solution
	 */
	private ProblemSol fillUpAndExchangeExtension() {
		// Initialize solution list as empty
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		
		int totalAx = 0;
		int i = 0;
		for (int j = 0; j < f.getN(); j++) {
			r.add(j);
		}
		// Add Max-ratio items until none can be added
		boolean[] inX = new boolean[f.getN()];
		boolean done = false;
		while (totalAx <= f.getB() && !done) {
			double maxRatio = -1*Double.MAX_VALUE;
			i = -1;
			for (int j = 0; j < r.size(); j++) {
				int xj = r.get(j);
				if (!inX[xj] && f.getRatio(xj) >= maxRatio && totalAx + f.getA(xj) <= f.getB()) {
					i = xj;
					maxRatio = f.getRatio(xj);
				}
			}
			if (i == -1) {
				done = true;
			} else {
				x.add(i);
				r.remove(Integer.valueOf(i));
				totalAx += f.getA(i);
				inX[i] = true;
			}
		}

		// Update objective
		double curObj = f.getObj(x);

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
					if (f.getA(xj)-f.getA(xi) <= f.getB() - totalAx) {
						// Calculate new objective
						double newObj = swapObj(xi, xj, x, curObj);
						double change = newObj - curObj;
						if (change > maxChange) {
							maxI = xi;
							maxJ = xj;
							maxChange = change;
						}
					}
				}
			}
			// Get most improving shifts
			double[] add = tryAdd(totalAx,r,curObj);
			double[] sub = trySub(x,curObj);
			double addChange = add[0];
			double subChange = sub[0];

			// If addition is better than swap
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				curObj = f.getObj(x);
				totalAx = totalAx + f.getA(addI);
			}
			// Else if removal is better than swap
			else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				curObj = f.getObj(x);
				totalAx = totalAx - f.getA(subI);
			} 
			// Else, perform the swap
			else {
				// If no improving swap exists, stop
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					curObj = f.getObj(x);
					totalAx = totalAx + f.getA(maxJ) - f.getA(maxI);
				}
			}
		}
		
		// Return the solution
		return new FractionalSol(x,r);
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
		return f.getObj(newX, false);
	}
	
	/**
	 * Find the variable that most improves the objective when removed
	 *
	 * @param x - items in the solution
	 * @param curObj - the current objective to improve upon
	 * @return {change in objective,item to add} or {0,-1} if no improving shift found
	 */
	private double[] trySub(ArrayList<Integer> x, double curObj) {
		double maxChange = 0;
		int minI = -1;
		// Check all removals
		for (Integer i: x) {
			// Calculate the change in objective
			double newObj = subObj(i, f.getNum(), f.getDen());
			double change = newObj - curObj;
			// Update best change
			if (change < maxChange) {
				maxChange = change;
				minI = i;
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
			if (totalA + f.getA(i) <= f.getB()) {
				// Calculate the change in objective
				double newObj = addObj(i, f.getNum(), f.getDen());
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
	 * Calculate the objective if i is removed. 
	 * 	Calculation done without changes to num/den.
	 * 
	 * @param i - item to be removed
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the new objective
	 */
	private double subObj(int i, long[] num, long[] den) {
		double obj = 0;
		// For each fraction,
		for (int j = 0; j < f.getM(); j++) {
			// Check for a zero in the denominator
			if (den[j]-f.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			// Otherwise, update the objective
			obj += (double)(num[j]-f.getC(j,i))/(den[j]-f.getD(j,i));
		}
		return obj;
	}

	/**
	 * Calculate the objective if i is added. 
	 * 	Calculation done without changes to num/den.
	 * 
	 * @param i - item to be added
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the new objective
	 */
	private double addObj(int i, long[] num, long[] den) {
		double obj = 0;
		// For each fraction,
		for (int j = 0; j < f.getM(); j++) {
			// Check for a zero in the denominator
			if (den[j]+f.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			// Otherwise, update the objective
			obj += (double)(num[j]+f.getC(j,i))/(den[j]+f.getD(j,i));
		}

		return obj;
	}

}
