package Constructive;

import java.util.ArrayList;

import Problems.Cubic;
import Solutions.CubicSol;
import Solutions.ProblemSol;

/**
 * Implement the incumbent heuristic of Dantzig on the Cubic knapsack problem
 * 
 * @author midkiffj
 */
public class CubicIncumbent extends ConstHeuristic {

	private Cubic c;
	
	/**
	 * Specify the problem to solve
	 * 
	 * @param c
	 */
	public CubicIncumbent(Cubic c) {
		this.c = c;
	}

	/**
	 * Construct a solution to the problem
	 * 
	 * @param solution constructed
	 */
	protected ProblemSol construct() {
		return genInit();
	}
	
	/**
	 * Generate an initial incumbent solution by adding x's until knapsack full
	 * and update the current objective value
	 * 
	 * @return solution generated
	 */
	public ProblemSol genInit() {
		return genInit(new ArrayList<Integer>(),new ArrayList<Integer>(), new ArrayList<Integer>());
	}

	/**
	 * Generate a solution by adding x's until knapsack full
	 * and update the current objective value. 
	 * Don't use any indexes in the provided list (d).
	 * 
	 * @param d - items to ignore
	 * @param x - solution list
	 * @param r - list of items not in solution\
	 * @return solution generated
	 */
	public ProblemSol genInit(ArrayList<Integer> d, ArrayList<Integer> x, ArrayList<Integer> r) {
		r.clear();
		x.clear();
		int totalAx = 0;
		int i = 0;
		// Create list of items that can be used
		ArrayList<Integer> toUse = new ArrayList<Integer>();
		for (int j = 0; j < c.getN(); j++) {
			r.add(j);
			toUse.add(j);
		}
		toUse.removeAll(d);
		boolean[] inX = new boolean[c.getN()];
		boolean done = false;
		// Fill the knapsack with the max ratio item until the max-ratio item won't fit
		while (totalAx <= c.getB() && !done) {
			double maxRatio = -1*Double.MAX_VALUE;
			i = -1;
			for (int j = 0; j < toUse.size(); j++) {
				int xj = toUse.get(j);
				if (!inX[xj] && c.getRatio(xj) >= maxRatio && totalAx + c.getA(xj) <= c.getB()) {
					i = xj;
					maxRatio = c.getRatio(xj);
				}
			}
			if (i == -1) {
				done = true;
			} else {
				x.add(i);
				toUse.remove(Integer.valueOf(i));
				r.remove(Integer.valueOf(i));
				totalAx += c.getA(i);
				inX[i] = true;
			}
		}

		double curObj = c.getObj(x);

		// Check for Swaps and shifts
		boolean swapping = true;
		while (swapping) {
			int maxI = -1;
			int maxJ = -1;
			double maxChange = 0;
			// Determine best swap
			for(Integer xi: x) {
				for(Integer xj: toUse) {
					// Check for knapsack feasibility
					if (c.getA(xj)-c.getA(xi) <= c.getB() - totalAx) {
						double newObj = swapObj(x, curObj, xi, xj);
						double change = newObj - curObj;
						if (change > maxChange) {
							maxI = xi;
							maxJ = xj;
							maxChange = change;
						}
					}
				}
			}
			// Determine shifts
			double[] add = tryAdd(x,toUse, curObj, totalAx);
			double[] sub = trySub(x, curObj, totalAx);
			double addChange = add[0];
			double subChange = sub[0];
			
			// If add is best, add an item
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				toUse.remove(Integer.valueOf(addI));
				curObj = curObj + add[0];
				totalAx += c.getA(addI);
			} 
			// If sub is best, remove an item
			else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				toUse.add(subI);
				curObj = curObj + sub[0];
				totalAx -= c.getA(subI);
			} 
			// Else, perform the best improving swap
			else {
				// If no improving swap, stop searching
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					// Otherwise, apply swap
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					toUse.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					toUse.add(maxI);
					curObj = curObj + maxChange;
					totalAx = totalAx + c.getA(maxJ) - c.getA(maxI);
				}
			}
		}
		return new CubicSol(x,r,curObj,totalAx);
	}
	
	/**
	 * Calculate the change in objective 
	 *  if item i is removed and item j is added to the solution curX
	 *  
	 *  @param curX - current solution list
	 *  @param oldObj - objective before swap
	 *  @param i - item to remove
	 *  @param j - item to add
	 *  @return new objective after swap
	 */
	private double swapObj(ArrayList<Integer> curX, double oldObj, int i, int j) {
		oldObj = oldObj - c.getCi(i);
		oldObj = oldObj + c.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - c.getCij(i,xk);
				oldObj = oldObj + c.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - c.getDijk(i,xk,xl);
						oldObj = oldObj + c.getDijk(j,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	/**
	 * Try to add a variable to the solution, maintaining knapsack feasibility
	 * 
	 * @param curX - solution list
	 * @param r - items outside solution
	 * @param curObj - current objective
	 * @param totalA - current solution knapsack weight
	 * 
	 * @return {change in objective, item to add} or {0,-1} if no improving addition found
	 */
	private double[] tryAdd(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int totalA) {
		double maxChange = 0;
		int maxI = -1;
		// Check all possible shifts
		for(Integer i: r) {
			// Knapsack feasibility
			if (totalA + c.getA(i) <= c.getB()) {
				double obj = curObj + c.getCi(i);
				for (int j = 0; j < curX.size(); j++) {
					int xj = curX.get(j);
					obj += c.getCij(i,xj);
					for (int k = j+1; k < curX.size(); k++) {
						int xk = curX.get(k);
						obj += c.getDijk(i,xj,xk);
					}
				}
				// Track best improving addition
				double change = obj - curObj;
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		double[] result = {maxChange, maxI};
		return result;
	}

	/**
	 * Try to remove a variable to the solution, maintaining knapsack feasibility
	 * 
	 * @param curX - solution list
	 * @param r - items outside solution
	 * @param curObj - current objective
	 * @param totalA - current solution knapsack weight
	 * 
	 * @return {change in objective, item to remove} or {0,-1} if no improving removal found
	 */
	private double[] trySub(ArrayList<Integer> curX, double curObj, int totalA) {
		double maxChange = 0;
		int maxI = -1;
		// Check all possible removals
		for(Integer i: curX) {
			double obj = curObj - c.getCi(i);
			for (int j = 0; j < curX.size(); j++) {
				int xj = curX.get(j);
				obj -= c.getCij(i,xj);
				for (int k = j+1; k < curX.size(); k++) {
					int xk = curX.get(k);
					obj -= c.getDijk(i,xj,xk);
				}
			}
			// Track best improving removal
			double change = obj - curObj;
			if (change > maxChange) {
				maxChange = change;
				maxI = i;
			}
		}
		double[] result = {maxChange, maxI};
		return result;
	}
}
