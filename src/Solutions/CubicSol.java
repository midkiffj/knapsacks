package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.Cubic;

/**
 * Solution class for a Cubic Problem
 * - Mutates solution with swaps and shifts
 * - File I/O for storing solutions
 * 
 * @author midkiffj
 */
public class CubicSol extends KnapsackSol {

	private static Cubic c;

	/**
	 * Construct a solution by relying on the super class
	 */
	public CubicSol() {
		super();
		c = (Cubic)p;
		updateValid();
	}

	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public CubicSol(String filename) {
		super(filename);
		c = (Cubic)p;
		updateValid();
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param cs the solution to copy
	 */
	public CubicSol(CubicSol cs) {
		super((KnapsackSol)cs);
		c = (Cubic)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
	public CubicSol(boolean[] xVals) {
		super(xVals);
		c = (Cubic)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
	public CubicSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		c = (Cubic)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weight of the solution
	 */
	public CubicSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA) {
		super(x,r,obj,totalA);
		c = (Cubic)p;
		updateValid();
	}

	/**
	 * Update the validity of the solution
	 */
	public void updateValid() {
		calcTotalA();
		if (getTotalA() <= c.getB()) {
			setValid(true);
		} else {
			setValid(false);
		}
	}

	/**
	 * Current unused method that 
	 * 	proportionally performs ratioMutates or bestRatioMutates.
	 * 
	 * @return mutated solution
	 */
	private CubicSol swapMutate() {
		if (rnd.nextDouble() < 0.8) {
			return ratioMutate();
		} else {
			return bestRatioMutate();
		}
	}

	/**
	 * Perform a swap of items i and j,
	 * 	- Remove i from the solution
	 * 	- Add j to the solution
	 * 	- Update objective and knapsack weight
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 */
	public void swap(int i, int j) {
		setObj(swapObj(i,j));
		removeA(i);
		addA(j);
		addI(j);
		removeI(i);
		updateValid();
	}

	/**
	 *  Shift a variable in or out of the current solution
	 */
	public int shift() {
		if (getRSize() == 0) {
			return trySub();
		}
		if (getXSize() < 2) {
			return tryAdd();
		} else {
			if (rnd.nextDouble() < 0.8) {
				return tryAdd();
			} else {
				return trySub();
			}
		}
	}

	/**
	 * Try to add a variable to the solution
	 * 
	 * @return the item added or -1 if none added
	 */
	private int tryAdd() {
		int index = tryAdd(getTotalA(), getX(), getR(), false);
		if (index != -1) {
			addI(index);
			addA(index);
			setObj(addObj(index));
			updateValid();
		}
		return index;
	}

	/**
	 * Try to remove a variable from the solution
	 * 
	 * @return the item removed or -1 if none added
	 */
	private int trySub() {
		int index = trySub(getX(), false);
		if (index != -1) {
			removeI(index);
			removeA(index);
			setObj(subObj(index));
			updateValid();
		}
		return index;
	}

	/**
	 * Perform a mutation given the current iteration number and tabu list
	 * 
	 * @param iteration - current iteration
	 * @param tabuList - current tabu list
	 * @return {best tabu solution, best nontabu solution}
	 */
	public ProblemSol[] tabuMutate(int iteration, int[][] tabuList) {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.6) {
			return maxMinSwap(iteration, tabuList);
		} else {
			CubicSol[] ratioSwap = ratioMutate(iteration, tabuList);
			return ratioSwap;
		}
	}

	/**
	 * Mutate the solution
	 * 
	 * @return mutated solution
	 */
	public ProblemSol mutate() {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.6) {
			CubicSol[] ret = maxMinSwap(1, new int[n][n]);
			if (ret == null) {
				return null;
			} else {
				return ret[0];
			}
		} else {
			CubicSol ratioSwap = ratioMutate();
			return ratioSwap;
		}
	}

	/**
	 * Perform the best mutation (swap) possible
	 * 
	 * @return mutated solution
	 */
	public ProblemSol bestMutate() {
		if (getRSize() == 0) {
			return null;
		}
		if (p.getN() >= 500) {
			CubicSol[] fs = firstSwap(1, new int[n][n]);
			if (fs != null) {
				return fs[0];
			}
			return null;
		}
		CubicSol[] bs = bestSwap(1, new int[n][n]);
		if (bs != null) {
			return bs[0];
		} else {
			return null;
		}
	}

	/**
	 * Perform the best mutation given the current iteration and tabu list
	 * 
	 * @param iteration - current iteration count
	 * @param tabuList - current tabu list
	 * @return {best tabu solution, best nontabu solution}
	 */
	public ProblemSol[] tabuBestMutate(int iteration, int[][] tabuList) {
		if (getRSize() == 0) {
			return null;
		}
		return bestSwap(iteration, tabuList);
	}

	/**
	 * Perform a crossover mutation with the specified solution
	 * - Keeps items in the solution that appear in both solutions
	 * - Fills the solution with max-ratio items until full
	 * 
	 * @param ps2 - solution to combine
	 * @return solution after crossover
	 */
	public ProblemSol crossover(ProblemSol ps2) {
		CubicSol cs2 = (CubicSol)ps2;
		boolean[] newXVals = new boolean[n];
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		int newTotalA = 0;
		for (int i = 0; i < n; i++) {
			if (this.getXVals(i) == cs2.getXVals(i)) {
				newXVals[i] = this.getXVals(i);
				if (newXVals[i]) {
					x.add(i);
					newTotalA += c.getA(i);
				} else {
					r.add(i);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newTotalA + c.getA(rni.x) <= getB()) {
				ratio.remove(i);
				newXVals[rni.x] = true;
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += c.getA(rni.x);
			} else {
				ratio.remove(i);
			}
		}

		return new CubicSol(newXVals);
	}

	/**
	 * Perform a mutation for the genetic algorithm
	 * 
	 * @param removeAttempts - number of genMutate2 calls
	 * @return mutated solution
	 */
	public ProblemSol genMutate(int removeAttempts) {
		CubicSol newCS = new CubicSol(this);
		if (rnd.nextDouble() < 0.5) {
			if (newCS.getRSize() == 0) {
				newCS.shift();
			} else {
				return newCS.mutate();
			}
		} else {
			newCS = genMutate2(newCS, removeAttempts);
			if (removeAttempts < n-1) {
				removeAttempts++;
			}
		}
		return newCS;
	}

	/**
	 * Try to remove an item from the given solution
	 * - Remove the minRatio item
	 * 
	 * @param x - solution list
	 * @param improveOnly - only remove an item if it improves the objective
	 * @return the item to remove or -1 if none to remove
	 */
	private int trySub(ArrayList<Integer> x, boolean improveOnly) {
		if (x.size() <= 1) {
			return -1;
		}
		int minI = minRatio(0);

		if (minI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = subObj(minI);
			if (change > getObj()) {
				return minI;
			} else {
				return -1;
			}
		} else {
			return minI;
		}
	}

	/**
	 * Try to add an item to the given solution
	 * - Add the maxRatio item
	 * 
	 * @param totalA - current knapsack weight
	 * @param x - solution list
	 * @param r - list of items not in the solution
	 * @param improveOnly - only remove an item if it improves the objective
	 * @return the item to add or -1 if none to add
	 */
	private int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly) {
		if (x.size() == n) {
			return -1;
		}
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (totalA + c.getA(i) <= getB()) {
				double ratio = c.getRatio(i);
				if (ratio > maxRatio) {
					maxRatio = ratio;
					maxI = i;
				}
			}
		}

		if (maxI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = addObj(maxI);
			if (change > getObj()) {
				return maxI;
			} else {
				return -1;
			}
		} else {
			return maxI;
		}
	}

	/**
	 * Calculate the objective if item i is removed from the solution
	 * 
	 * @param i - item to remove
	 * @return calculated objective
	 */
	private double subObj(int i) {
		double oldObj = getObj() - c.getCi(i);
		ArrayList<Integer> curX = getX();
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - c.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - c.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	/**
	 * Calculate the objective if item i is added to the solution
	 * 
	 * @param i - item to add
	 * @return calculated objective
	 */
	private double addObj(int i) {
		double oldObj = getObj() + c.getCi(i);
		ArrayList<Integer> curX = getX();
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj + c.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj + c.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
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
		return swapObj(i,j,getX(),getObj());
	}

	/**
	 * Calculate the objective if item i is remove from the solution
	 * 	and item j is added to the solution.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param curX - current solution list
	 * @param oldObj - current objective
	 * @return calculated objective
	 */
	private double swapObj(int i, int j, ArrayList<Integer> curX, double oldObj) {
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
	 * Randomly remove a number of items from the solution
	 * 
	 * @param cs - the solution to mutate
	 * @param removeAttempts - the number of items to remove (increases with each call)
	 * @return mutated solution
	 */
	private CubicSol genMutate2(CubicSol cs, int removeAttempts) {
		// Remove s items from the solution
		ArrayList<Integer> x = new ArrayList<Integer>(cs.getX());
		ArrayList<Integer> r = new ArrayList<Integer>(cs.getR());
		int s = removeAttempts;
		if (s >= x.size()) {
			s = x.size()-1;
		}
		for (int i = 0; i < s; i++) {
			int j = rnd.nextInt(x.size());
			r.add(x.remove(j));
		}

		// Compute ratios
		ArrayList<ratioNode> ratio = computeRatios(x, r);
		Collections.sort(ratio);

		// Calc solution capacity
		int newTotalA = 0;
		for (Integer i: x) {
			newTotalA += c.getA(i);
		}
		// Add max-ratio items until knapsack full
		while (ratio.size() > 0 && newTotalA < c.getB()) {
			ratioNode rni = ratio.get(ratio.size()-1);
			if (newTotalA + c.getA(rni.x) <= c.getB()) {
				ratio.remove(ratio.size()-1);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += c.getA(rni.x);
			} else {
				ratio.remove(ratio.size()-1);
			}
		}
		// Calculate obj of new solution
		double obj = c.getObj(x);
		return new CubicSol(x,r,obj,newTotalA);
	}

	/**
	 * Create a list of ratioNodes for the given solution lists
	 * 
	 * @param x - solution list
	 * @param r - list of items not i the solution
	 * @return list of ratioNodes
	 */
	private ArrayList<ratioNode> computeRatios(ArrayList<Integer> x, ArrayList<Integer> r) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: r) {
			ratioNode rni = new ratioNode(i, c.getRatio(i));
			ratio.add(rni);
		}
		Collections.sort(ratio);
		return ratio;
	}

	/**
	 * Update the list of ratioNodes for the given solution lists
	 * 
	 * @param x - solution list
	 * @param ratio - list of ratioNodes
	 * @param added - item added to the solution
	 */
	private void updateRatios(ArrayList<Integer> x, ArrayList<ratioNode> ratio, int added) {
		for (ratioNode rni: ratio) {
			int i = rni.x;
			double iRatio = c.getCij(i,added);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				iRatio += c.getDijk(i, xj, added);
			}
			rni.ratio += iRatio;
		}
	}

	/**
	 * Perform a ratio mutation
	 * - Find the minRatio item in the solution
	 * - Swap it with a random item outside the solution
	 * 
	 * @return mutated solution
	 */
	private CubicSol ratioMutate() {
		CubicSol result = null;
		boolean found = false;
		int min = 0;
		ArrayList<Integer> curR = getR();
		while (!found && min < getXSize()) {
			// Get index of min ratio
			int i = minRatio(min);

			// Swap with a random node and return
			int j = rnd.nextInt(getRSize());
			j = curR.get(j);
			int rndCount = 0;
			while (c.getA(j) - c.getA(i) > getB() - getTotalA() && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = curR.get(j);
				rndCount++;
			}

			if (c.getA(j) - c.getA(i) <= getB() - getTotalA()) {
				result = new CubicSol(this);
				result.swap(i, j);
				found = true;
			}

			min++;
		}

		return result;
	}

	/**
	 * Perform the best ratio mutation by swapping the minimum ratio item 
	 * 	with the item j in R that best improves the objective.
	 * 
	 * @return mutated solution
	 */
	private CubicSol bestRatioMutate() {
		CubicSol result = null;
		boolean found = false;
		int min = 0;
		while (!found && min < getXSize()) {
			// Get index of min ratio
			int i = minRatio(min);

			// Swap with all nodes and return best
			double maxObj = -1;
			int maxJ = -1;
			for (Integer j: getR()) {
				double newObj = swapObj(i, j);
				if (newObj > maxObj && c.getA(j) - c.getA(i) <= getB() - getTotalA()) {
					maxObj = newObj;
					maxJ = j;
				}
			}
			if (maxJ != -1) {
				result = new CubicSol(this);
				result.swap(i, maxJ);
				found = true;
			}

			min++;
		}

		return result;
	}

	/**
	 * Perform a maxMin swap by swap the min ratio item from the solution
	 * 	with the max ratio item outside the solution
	 * 
	 * @param iteration - current tabu search iteration
	 * @param tabuList - tabu list
	 * @return {tabu mutated solution, nontabu mutated solution}
	 */
	private CubicSol[] maxMinSwap(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;

		int i = minRatio(0);
		int j = maxRatio(0);
		int ki = 0;
		int kj = 0;
		boolean changeI = true;
		while (c.getA(j) - c.getA(i) > getB() - getTotalA() && ki < getXSize()) {
			if (changeI) {
				ki++;
				i = minRatio(ki);
				changeI = !changeI;
			}
			kj++;
			j = maxRatio(kj);
			if (kj >= getRSize()-1) {
				kj = -1;
				changeI = !changeI;
			}
		}

		if (c.getA(j) - c.getA(i) > getB() - getTotalA()) {
			return null;
		}

		double newObj = swapObj(i, j);
		bi = i;
		bj = j;
		bObj = newObj;
		if (tabuList[i][j] < iteration) {
			ni = i;
			nj = j;
		} else {
			boolean newMin = false;
			while (tabuList[i][j] >= iteration && c.getA(j) - c.getA(i) > getB() - getTotalA() && ki < getXSize()) {
				if (newMin) {
					ki++;
					i = minRatio(ki);
					newMin = !newMin;
				}
				kj++;
				j = maxRatio(kj);
				if (kj >= getRSize()-1) {
					kj = -1;
					newMin = !newMin;
				}
				if (c.getA(j) - c.getA(i) <= getB() - getTotalA()) {
					newObj = swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
			if (tabuList[i][j] < iteration && c.getA(j) - c.getA(i) <= getB() - getTotalA()) {
				newObj = swapObj(i, j);
				ni = i;
				nj = j;
				if (newObj > bObj) {
					bi = i;
					bj = j;
					bObj = newObj;
				}
			}
		}
		// Compile and return data
		CubicSol[] results = new CubicSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicSol(this);
			results[1].swap(ni, nj);
		}
		return results;
	}

	/**
	 * Perform a ratio mutate by swaping the min ratio item from the solution
	 * 	with a random item outside the solution
	 * 
	 * @param iteration - current tabu search iteration
	 * @param tabuList - tabu list
	 * @return {tabu mutated solution, nontabu mutated solution}
	 */
	private CubicSol[] ratioMutate(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;

		// Get index of min ratio
		int i = minRatio(0);

		// Swap with a random node and return
		ArrayList<Integer> curR = getR();
		int j = rnd.nextInt(getRSize());
		j = curR.get(j);
		int ki = 0;
		int kj = 0;
		boolean changeI = false;
		while (tabuList[i][j] >= iteration && c.getA(j) - c.getA(i) > getB() - getTotalA() && ki < n) {
			if (changeI) {
				ki++;
				i = minRatio(ki);
				changeI = !changeI;
			}

			kj++;
			j =  rnd.nextInt(getRSize());
			j = curR.get(j);
			if (kj == n-1) {
				kj = -1;
				changeI = !changeI;
			}
			if (c.getA(j) - c.getA(i) <= getB() - getTotalA()) {
				double newObj = swapObj(i, j);
				if (newObj > bObj) {
					bi = i;
					bj = j;
					bObj = newObj;
				}
			}
		}
		ni = i;
		nj = j;
		// Compile and return data
		CubicSol[] results = new CubicSol[2];
		if (bi != -1 && bj != -1 && c.getA(bj) - c.getA(bi) <= getB() - getTotalA()) {
			results[0] = new CubicSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1 && c.getA(nj) - c.getA(ni) <= getB() - getTotalA() && tabuList[ni][nj] < iteration) {
			results[1] = new CubicSol(this);
			results[1].swap(ni, nj);
		}
		return results;
	}

	/**
	 * Determine the kth minimum ratio item currently in the solution
	 * 
	 * @param k - which minimum
	 * @return item number
	 */
	private int minRatio(int k) {
		// Find the minimum ratio in the solution
		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < getXSize()) {
			for (Integer i: getX()) {
				if (c.getRatio(i) < minRatio && !bestIs.contains(i)) {
					minRatio = c.getRatio(i);
					minI = i;
				}
			}
			minRatio = Double.MAX_VALUE;
			bestIs.add(minI);
		}
		return minI;
	}

	/**
	 * Determine the kth maximum ratio item currently not in the solution
	 * 
	 * @param k - which maximum
	 * @return item number
	 */
	private int maxRatio(int k) {
		// Find the maximum ratio not in the solution
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < getRSize()) {
			for (Integer i: getR()) {
				if (c.getRatio(i) > maxRatio && !bestIs.contains(i)) {
					maxRatio = c.getRatio(i);
					maxI = i;
				}
			}
			maxRatio = -1*Double.MAX_VALUE;
			bestIs.add(maxI);
		}
		return maxI;
	}

	/**
	 * Find the best swap possible that keeps the knapsack feasible. 
	 * 
	 * @param iteration - current tabu search iteration
	 * @param tabuList - current tabu list
	 * @return {tabu mutated solution, nontabu mutated solution}
	 */
	private CubicSol[] bestSwap(int iteration, int[][] tabuList) {
		int curTotalA = getTotalA();
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;
		for(Integer i: getX()) {
			for(Integer j: getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= getB() - curTotalA) {
					double newObj = swapObj(i, j);
					if (newObj > nTObj && tabuList[i][j] < iteration) {
						ni = i;
						nj = j;
						nTObj = newObj;
					}
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		// Compile and return data
		CubicSol[] results = new CubicSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicSol(this);
			results[1].swap(ni, nj);
		}
		return results;
	}

	/**
	 * Return the first improving swap that keeps the knapsack feasible
	 * 
	 * @param iteration - current tabu search iteration
	 * @param tabuList - current tabu list
	 * @return {tabu mutated solution, nontabu mutated solution}
	 */
	private CubicSol[] firstSwap(int iteration, int[][] tabuList) {
		int curTotalA = getTotalA();
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;
		for(Integer i: getX()) {
			for(Integer j: getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= getB() - curTotalA) {
					double newObj = swapObj(i, j);
					if (newObj > nTObj && tabuList[i][j] < iteration) {
						ni = i;
						nj = j;
						nTObj = newObj;
					}
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		CubicSol[] results = new CubicSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicSol(this);
			results[1].swap(ni, nj);
		}
		return results;
	}

	@Override
	/**
	 * Comparison for solutions used in genetic algorithm
	 * 
	 * 1) Invalid < Valid
	 * 2) Higher objective is better
	 */
	public int compareTo(ProblemSol o) 	{
		if (o.getValid() && this.getValid() || !(o.getValid() || this.getValid())) {
			double diff = this.getObj() - o.getObj();
			if (diff > 0) {
				return 1;
			} else if (diff < 0) {
				return -1;
			} else {
				return 0;
			}
		} else {
			if (o.getValid()) {
				return -1;
			} else {
				return 1;
			}
		}
	}


	@Override
	/**
	 * Heal the solution if it is invalid
	 */
	public void healSol() {
		healSolImproving();
		//		healSolRatio();
	}

	/**
	 * Heal the solution by removing the item that results in the best objective
	 *  until the solution is valid.
	 */
	public void healSolImproving() {
		while(!this.getValid()) {
			double maxObj = -1*Double.MAX_VALUE;
			int maxI = -1;
			for (Integer i: this.getX()) {
				double newObj = subObj(i);
				if (newObj > maxObj) {
					maxObj = newObj;
					maxI = i;
				}
			}
			if (maxI != -1) {
				removeI(maxI);
				setObj(maxObj);
				removeA(maxI);
			} else {
				System.err.println("Couldn't find an improving objective!!!");
				System.exit(-1);
			}
		}
	}

	/**
	 * Heal the solution by removing minRatio items until valid
	 */
	public void healSolRatio() {
		while(!this.getValid()) {
			int j = minRatio(0);
			removeI(j);
			setObj(subObj(j));
			removeA(j);
		}
	}

	/**
	 * Write the solution to the given file
	 * 
	 * @param filename to write
	 */
	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(getObj() + "\n");
			pw.write(getTotalA() + "\n");
			Collections.sort(getX());
			for (Integer i: getX()) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
		}
	}

	/**
	 * Read a solution from the given filename
	 */
	public void readSolution(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			int readTotalA = scr.nextInt();
			if (readObj != -1) {
				ArrayList<Integer> readX = new ArrayList<Integer>();
				while (scr.hasNextInt()) {
					readX.add(scr.nextInt());
				}
				ArrayList<Integer> readR = new ArrayList<Integer>();
				for (int i = 0; i < n; i++) {
					readR.add(i);
				}
				readR.removeAll(readX);
				setObj(readObj);
				setTotalA(readTotalA);
				setX(readX);
				setR(readR);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}
}
