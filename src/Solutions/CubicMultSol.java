package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.CubicMult;

/**
 * Solution class for a Cubic Multiple Knapsack Problem
 * - Mutates solution with swaps and shifts
 * - File I/O for storing solutions
 * 
 * @author midkiffj
 */
public class CubicMultSol extends MultKnapsackSol {

	private static CubicMult cm;

	/**
	 * Construct a solution by relying on the super class
	 */
	public CubicMultSol() {
		super();
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public CubicMultSol(String filename) {
		super(filename);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param cms the solution to copy
	 */
	public CubicMultSol(CubicMultSol cms) {
		super((MultKnapsackSol)cms);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
	public CubicMultSol(boolean[] xVals) {
		super(xVals);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
	public CubicMultSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weights of the solution
	 */
	public CubicMultSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int[] totalA) {
		super(x,r,obj,totalA);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Update the validity of the solution
	 */
	public void updateValid() {
		calcTotalA();
		int[] totalA = getTotalA();
		boolean valid = true;
		for (int i = 0; i < m && valid; i++) {
			if (totalA[i] > cm.getB(i)) {
				valid = false;
			}
		}
		setValid(valid);
	}

	/**
	 * Current unused method that 
	 * 	proportionally performs ratioMutates or bestRatioMutates.
	 * 
	 * @return mutated solution
	 */
	private CubicMultSol swapMutate() {
		if (rnd.nextDouble() < 0.8) {
			return ratioMutate();
		} else {
			return bestRatioMutate();
		}
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
		oldObj = oldObj - cm.getCi(i);
		oldObj = oldObj + cm.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - cm.getCij(i,xk);
				oldObj = oldObj + cm.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - cm.getDijk(i,xk,xl);
						oldObj = oldObj + cm.getDijk(j,xk,xl);
					}
				}
			}
		}
		return oldObj;
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
		setObj(swapObj(i,j, getX(), getObj()));
		addA(j);
		removeA(i);
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
	private int tryAdd(int[] totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly) {
		if (x.size() == n) {
			return -1;
		}
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (addTotalA(totalA,i)) {
				double ratio = cm.getRatio(i);
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
	public double subObj(int i) {
		ArrayList<Integer> curX = getX();
		double oldObj = getObj() - cm.getCi(i);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - cm.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - cm.getDijk(i,xk,xl);
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
	public double addObj(int i) {
		ArrayList<Integer> curX = getX();
		double oldObj = getObj() + cm.getCi(i);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj + cm.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj + cm.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	/**
	 * Perform a mutation given the current iteration number and tabu list
	 * 
	 * @param iteration - current iteration
	 * @param tabuList - current tabu list
	 * @return {best tabu solution, best nontabu solution}
	 */
	public CubicMultSol[] tabuMutate(int iteration, int[][] tabuList) {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.6) {
			return maxMinSwap(iteration, tabuList);
		} else {
			CubicMultSol[] ratioSwap = ratioMutate(iteration, tabuList);
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
			ProblemSol[] ret = maxMinSwap(1, new int[n][n]);
			if (ret == null) {
				return null;
			} else {
				return ret[0];
			}
		} else {
			ProblemSol ratioSwap = ratioMutate();
			if (ratioSwap == null) {
				return null;
			}
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
			ProblemSol[] fs = firstSwap(1, new int[n][n]);
			if (fs != null) {
				return fs[0];
			}
			return null;
		}
		ProblemSol[] bs = bestSwap(1, new int[n][n]);
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
		CubicMultSol cs2 = (CubicMultSol)ps2;
		boolean[] newXVals = new boolean[n];
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		int[] newTotalA = new int[m];
		for (int i = 0; i < n; i++) {
			if (this.getXVals(i) == cs2.getXVals(i)) {
				newXVals[i] = this.getXVals(i);
				if (newXVals[i]) {
					x.add(i);
					addA(i,newTotalA);
				} else {
					r.add(i);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && totalAValid(newTotalA)) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (addTotalA(newTotalA, rni.x)) {
				ratio.remove(i);
				newXVals[rni.x] = true;
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				addA(rni.x,newTotalA);
			} else {
				ratio.remove(i);
			}
		}

		return new CubicMultSol(newXVals);
	}

	/**
	 * Update the knapsack weights of the given array 
	 * 	if item j is added
	 * @param j - item to add
	 * @param totalA - weights
	 */
	private void addA(int j, int[] totalA) {
		for (int i = 0; i < m; i++) {
			totalA[i] += cm.getA(i,j);
		}
	}

	/**
	 * Perform a mutation for the genetic algorithm
	 * 
	 * @param removeAttempts - number of genMutate2 calls
	 * @return mutated solution
	 */
	public ProblemSol genMutate(int removeAttempts) {
		CubicMultSol newCS = new CubicMultSol(this);
		if (rnd.nextDouble() < 0.5) {
			if (newCS.getRSize() == 0) {
				newCS.shift();
			} else {
				newCS = (CubicMultSol) newCS.mutate();
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
	 * Randomly remove a number of items from the solution
	 * 
	 * @param cs - the solution to mutate
	 * @param removeAttempts - the number of items to remove (increases with each call)
	 * @return mutated solution
	 */
	private CubicMultSol genMutate2(CubicMultSol cs, int removeAttempts) {
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
		int[] newTotalA = calcTotalA(x);

		// Add max-ratio items until knapsack full
		while (ratio.size() > 0 && totalAValid(newTotalA)) {
			ratioNode rni = ratio.get(ratio.size()-1);
			if (addTotalA(newTotalA,rni.x)) {
				ratio.remove(ratio.size()-1);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				addA(rni.x, newTotalA);
			} else {
				ratio.remove(ratio.size()-1);
			}
		}
		// Calculate obj of new solution
		double obj = cm.getObj(x);
		return new CubicMultSol(x,r,obj,newTotalA);
	}

	/**
	 * Calculate the knapsack weights of the given solution
	 * 
	 * @param x - solution list
	 * @return knapsack weights
	 */
	private int[] calcTotalA(ArrayList<Integer> x) {
		int[] totalA = new int[m];
		for (Integer i: x) {
			for(int j = 0; j < m; j++) {
				totalA[j] += cm.getA(j, i);
			}
		}
		return totalA;
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
			ratioNode rni = new ratioNode(i, cm.getRatio(i));
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
			double iRatio = cm.getCij(i,added);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				iRatio += cm.getDijk(i, xj, added);
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
	private CubicMultSol ratioMutate() {
		CubicMultSol result = null;
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
			while (!swapTotalA(getTotalA(), i, j) && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = curR.get(j);
				rndCount++;
			}

			if (swapTotalA(getTotalA(), i, j)) {
				result = new CubicMultSol(this);
				result.swap(i,j);
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
	private CubicMultSol bestRatioMutate() {
		CubicMultSol result = null;
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
				if (newObj > maxObj && swapTotalA(getTotalA(), i, j)) {
					maxObj = newObj;
					maxJ = j;
				}
			}
			if (maxJ != -1) {
				result = new CubicMultSol(this);
				result.swap(i,maxJ);
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
	private CubicMultSol[] maxMinSwap(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;

		int i = minRatio(0);
		int j = maxRatio(0);
		int ki = 0;
		int kj = 0;
		boolean changeI = true;
		while (!swapTotalA(getTotalA(), i, j) && ki < getXSize()) {
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

		if (!swapTotalA(getTotalA(), i, j)) {
			return null;
		}

		double newObj = swapObj(i, j);
		bi = i;
		bj = j;
		bObj = newObj;
		if (tabuList[i][j] < iteration) {
			ni = i;
			nj = j;
			nTObj = newObj;
		} else {
			boolean newMin = false;
			while (tabuList[i][j] >= iteration && !swapTotalA(getTotalA(), i, j) && ki < getXSize()) {
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
				if (swapTotalA(getTotalA(), i, j)) {
					newObj = swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
			if (tabuList[i][j] < iteration) {
				newObj = swapObj(i, j);
				ni = i;
				nj = j;
				nTObj = newObj;
				if (newObj > bObj) {
					bi = i;
					bj = j;
					bObj = newObj;
				}
			}
		}
		// Compile and return data
		CubicMultSol[] results = new CubicMultSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicMultSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicMultSol(this);
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
	private CubicMultSol[] ratioMutate(int iteration, int[][] tabuList) {
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
		while (tabuList[i][j] >= iteration && !swapTotalA(getTotalA(), i, j) && ki < n) {
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
			if (swapTotalA(getTotalA(), i, j)) {
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
		CubicMultSol[] results = new CubicMultSol[2];
		if (bi != -1 && bj != -1 && swapTotalA(getTotalA(),bi,bj)) {
			results[0] = new CubicMultSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1 && swapTotalA(getTotalA(),ni,nj) && tabuList[ni][nj] < iteration) {
			results[1] = new CubicMultSol(this);
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
				if (cm.getRatio(i) < minRatio && !bestIs.contains(i)) {
					minRatio = cm.getRatio(i);
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
				if (cm.getRatio(i) > maxRatio && !bestIs.contains(i)) {
					maxRatio = cm.getRatio(i);
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
	private CubicMultSol[] bestSwap(int iteration, int[][] tabuList) {
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
				if (swapTotalA(getTotalA(), i, j)) {
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
		CubicMultSol[] results = new CubicMultSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicMultSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicMultSol(this);
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
	private CubicMultSol[] firstSwap(int iteration, int[][] tabuList) {
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
				if (swapTotalA(getTotalA(), i, j)) {
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
					if (ni != -1 && nj != -1) {
						// Compile and return data
						CubicMultSol[] results = new CubicMultSol[2];
						if (bi != -1 && bj != -1) {
							results[0] = new CubicMultSol(this);
							results[0].swap(bi, bj);
						}
						results[1] = new CubicMultSol(this);
						results[1].swap(ni, nj);
						return results;
					}
				}
			}
		}
		// Compile and return data
		CubicMultSol[] results = new CubicMultSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicMultSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicMultSol(this);
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
		//		healSolImproving();
		healSolRatio();
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
				removeA(maxI);
				setObj(maxObj);
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
			removeA(j);
			setObj(subObj(j));
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
			int[] totalA = getTotalA();
			for (int i = 0; i < cm.getM(); i++) {
				pw.write(totalA[i] + " ");
			}
			pw.write("\n");
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
	 * 
	 * @param filename to read
	 */
	public void readSolution(String filename) { 
		cm = (CubicMult)p;
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			if (readObj != -1) {
				int[] readTotalA = new int[cm.getM()];
				for (int i = 0; i < cm.getM(); i++) {
					readTotalA[i] = scr.nextInt();
				}
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
