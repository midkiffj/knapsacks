package Constructive;

import java.util.ArrayList;

import Problems.Cubic;
import Solutions.CubicSol;
import Solutions.ProblemSol;

public class CubicIncumbent extends ConstHeuristic {

	private Cubic c;
	
	public CubicIncumbent(Cubic c) {
		this.c = c;
	}

	protected ProblemSol construct() {
		return genInit();
	}
	
	/**
	 * Generate an initial incumbent solution by adding x's until knapsack full
	 * and update the current objective value
	 */
	public ProblemSol genInit() {
		return genInit(new ArrayList<Integer>(),new ArrayList<Integer>(), new ArrayList<Integer>());
	}

	/**
	 * Generate a solution by adding x's until knapsack full
	 * and update the current objective value. 
	 * Don't use any indexes in the provided list.
	 */
	public ProblemSol genInit(ArrayList<Integer> d, ArrayList<Integer> x, ArrayList<Integer> r) {
		r.clear();
		x.clear();
		int totalAx = 0;
		int i = 0;
		ArrayList<Integer> toUse = new ArrayList<Integer>();
		for (int j = 0; j < c.getN(); j++) {
			r.add(j);
			toUse.add(j);
		}
		toUse.removeAll(d);
		boolean[] inX = new boolean[c.getN()];
		boolean done = false;
		// Fill the knapsack with the max ratio item
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
			double[] add = tryAdd(x,toUse, curObj, totalAx);
			double[] sub = trySub(x,toUse, curObj, totalAx);
			double addChange = add[0];
			double subChange = sub[0];
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				toUse.remove(Integer.valueOf(addI));
				curObj = curObj + add[0];
				totalAx = c.addA(addI, totalAx);
			} else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				toUse.add(subI);
				curObj = curObj + sub[0];
				totalAx = c.removeA(subI, totalAx);
			} else {
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					toUse.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					toUse.add(maxI);
					curObj = curObj + maxChange;
					totalAx = c.removeA(maxI, c.addA(maxJ,totalAx));
				}
			}
		}
		return new CubicSol(x,r,curObj,totalAx);
	}
	
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

	// Try to add a variable to the solution
	private double[] tryAdd(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int totalA) {
		double maxChange = 0;
		int maxI = -1;
		for(Integer i: r) {
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

	private double[] trySub(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int totalA) {
		double maxChange = 0;
		int maxI = -1;
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
