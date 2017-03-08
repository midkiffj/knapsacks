package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.Fractional;

/**
 * Solution class for a Fractional Problem
 * - Mutates solution with swaps and shifts
 * - File I/O for storing solutions
 * 
 * @author midkiffj
 */
public class FractionalSol extends KnapsackSol {

	private static Fractional f;

	private long[] num;
	private long[] den;

	/**
	 * Construct a solution by relying on the super class
	 */
	public FractionalSol() {
		super();
		f = (Fractional)p;
		setNum(f.getNum());
		setDen(f.getDen());
		updateValid();
	}

	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public FractionalSol(String filename) {
		super(filename);
		f = (Fractional)p;
		updateValid();
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param fs the solution to copy
	 */
	public FractionalSol(FractionalSol fs) {
		super((KnapsackSol)fs);
		f = (Fractional)p;
		setNum(fs.getNum());
		setDen(fs.getDen());
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weight of the solution
	 * @param num - numerator values
	 * @param den - denominator values
	 */
	public FractionalSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA, long[] num, long[] den) {
		super(x,r,obj,totalA);
		f = (Fractional)p;
		setNum(num);
		setDen(num);
		updateValid();
	}

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
	public FractionalSol(boolean[] newXVals) {
		super(newXVals);
		f = (Fractional)p;
		setNum(f.getNum());
		setDen(f.getDen());
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
	public FractionalSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		f = (Fractional)p;
		setNum(f.getNum());
		setDen(f.getDen());
		updateValid();
	}

	/**
	 * Update the validity of the solution
	 */
	public void updateValid() {
		calcTotalA();
		if (getTotalA() <= f.getB()) {
			setValid(true);
		} else {
			setValid(false);
		}
	}

	public long[] getNum() {
		return num;
	}

	public long[] getDen() {
		return den;
	}

	public void setNum(long[] num) {
		this.num = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			this.num[i] = num[i];
		}
	}

	public void setDen(long[] den) {
		this.den = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			this.den[i] = den[i];
		}
	}

	@Override
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
		addA(j);
		removeA(i);
		this.num = swapNum(i,j,num);
		this.den = swapDen(i,j,den);
		setObj(updateObj(num,den));
		addI(j);
		removeI(i);
		updateValid();
	}

	/**
	 * Calculate the objective with the given numerator and denominator values
	 * 
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double updateObj(long[] num, long[] den) {
		double newObj = 0;
		for (int k = 0; k < f.getM(); k++) {
			if (den[k] == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			newObj += (double)(num[k])/den[k];
		}
		return newObj;
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
		int index = tryAdd(getTotalA(), getX(), getR(), false, num, den);
		if (index != -1) {
			addI(index);
			addA(index);
			num = addNum(index, num);
			den = addDen(index, den);
			setObj(updateObj(num,den));
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
		int index = trySub(getX(), false, num, den);
		if (index != -1) {
			removeI(index);
			removeA(index);
			num = subNum(index, num);
			den = subDen(index, den);
			setObj(updateObj(num,den));
			updateValid();
		}
		return index;
	}

	/**
	 * Try to add an item to the given solution
	 * - Add the maxRatio item
	 * 
	 * @param totalA - current knapsack weight
	 * @param x - solution list
	 * @param r - list of items not in the solution
	 * @param improveOnly - only remove an item if it improves the objective
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the item to add or -1 if none to add
	 */
	private int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, 
			boolean improveOnly, long[] num, long[] den) {
		if (r.size() < 1) {
			return -1;
		}

		int b = this.getB();
		double maxRatio = Double.MIN_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (totalA + f.getA(i) <= b) {
				double ratio = f.getRatio(i);
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
			double change = addObj(maxI, num, den);
			double curObj = updateObj(num, den);
			if (change > curObj) {
				return maxI;
			} else {
				return -1;
			}
		} else {
			return maxI;
		}
	}

	/**
	 * Try to remove an item from the given solution
	 * - Remove the minRatio item
	 * 
	 * @param x - solution list
	 * @param improveOnly - only remove an item if it improves the objective
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the item to remove or -1 if none to remove
	 */
	private int trySub(ArrayList<Integer> x, boolean improveOnly, long[] num, long[] den) {
		if (x.size() <= 1) {
			return -1;
		}
		int minI = minRatio(0);

		if (minI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = subObj(minI, num, den);
			double curObj = updateObj(num,den);
			if (change > curObj) {
				return minI;
			} else {
				return -1;
			}
		} else {
			return minI;
		}
	}

	/**
	 * Calculate the objective if item i is removed from the solution
	 * 
	 * @param i - item to remove
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double subObj(int i, long[] num, long[] den) {
		double obj = 0;
		for (int j = 0; j < f.getM(); j++) {
			if (den[j]-f.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]-f.getC(j,i))/(den[j]-f.getD(j,i));
		}

		return obj;
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
		for (int j = 0; j < f.getM(); j++) {
			if (den[j]+f.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]+f.getC(j,i))/(den[j]+f.getD(j,i));
		}

		return obj;
	}

	/**
	 * Calculate the objective if item i is removed from the solution 
	 * 	and item j is added to the solution.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @return calculated objective
	 */
	private double swapObj(int i, int j) {
		long[] num = swapNum(i, j, this.num);
		long[] den = swapDen(i, j, this.den);
		return updateObj(num,den);
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
	public long[] swapNum(int i, int j, long[] num) {
		long[] newNum = new long[num.length];
		for (int k = 0; k < f.getM(); k++) {
			newNum[k] = num[k] + f.getC(k,j) - f.getC(k,i);
		}
		return newNum;
	}

	/**
	 * Calculate the numerator values if item i is removed
	 * 
	 * @param i - item to remove
	 * @param num - numerator value
	 * @return calculated objective
	 */
	public long[] subNum(int i, long[] num) {
		long[] newNum = new long[num.length];
		for (int k = 0; k < f.getM(); k++) {
			newNum[k] = num[k] - f.getC(k,i);
		}
		return newNum;
	}

	/**
	 * Calculate the numerator values if item i is added
	 * 
	 * @param i - item to add
	 * @param num - numerator value
	 * @return calculated objective
	 */
	public long[] addNum(int i, long[] num) {
		long[] newNum = new long[num.length];
		for (int k = 0; k < f.getM(); k++) {
			newNum[k] = num[k] + f.getC(k,i);
		}
		return newNum;
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
	public long[] swapDen(int i, int j, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < f.getM(); k++) {
			newDen[k] = den[k] + f.getC(k,j) - f.getC(k,i);
		}
		return newDen;
	}

	/**
	 * Calculate the denominator values if item i is removed 
	 * 
	 * @param i - item to remove
	 * @param den - denominator values
	 * @return calculated objective
	 */
	public long[] subDen(int i, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < f.getM(); k++) {
			newDen[k] = den[k] - f.getC(k,i);
		}
		return newDen;
	}

	/**
	 * Calculate the denominator values if item i is added
	 * 
	 * @param i - item to add
	 * @param den - denominator values
	 * @return calculated objective
	 */
	public long[] addDen(int i, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < f.getM(); k++) {
			newDen[k] = den[k] + f.getC(k,i);
		}
		return newDen;
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
			ProblemSol[] ratioSwap = ratioMutate(iteration, tabuList);
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
		FractionalSol mps2 = (FractionalSol)ps2;
		boolean[] newXVals = new boolean[n];
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		int newTotalA = 0;
		for (int i = 0; i < n; i++) {
			if (this.getXVals(i) == mps2.getXVals(i)) {
				newXVals[i] = this.getXVals(i);
				if (newXVals[i]) {
					x.add(i);
					newTotalA += f.getA(i);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < f.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newTotalA + f.getA(rni.x) <= f.getB()) {
				ratio.remove(i);
				newXVals[rni.x] = true;
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				newTotalA += f.getA(rni.x);
			} else {
				ratio.remove(i);
			}
		}

		return new FractionalSol(newXVals);
	}

	/**
	 * Perform a mutation for the genetic algorithm
	 * 
	 * @param removeAttempts - number of genMutate2 calls
	 * @return mutated solution
	 */
	public ProblemSol genMutate(int removeAttempts) {
		FractionalSol newMP = new FractionalSol(this);
		if (rnd.nextDouble() < 0.5) {
			if (newMP.getRSize() == 0) {
				newMP.shift();
			} else {
				newMP = (FractionalSol) newMP.mutate();
			}
		} else {
			newMP = genMutate2(newMP, removeAttempts);
			if (removeAttempts < n-1) {
				removeAttempts++;
			}
		}
		return newMP;
	}

	/**
	 * Randomly remove a number of items from the solution
	 * 
	 * @param cs - the solution to mutate
	 * @param removeAttempts - the number of items to remove (increases with each call)
	 * @return mutated solution
	 */
	private FractionalSol genMutate2(FractionalSol mps, int removeAttempts) {
		// Remove s items from the solution
		ArrayList<Integer> x = new ArrayList<Integer>(mps.getX());
		ArrayList<Integer> r = new ArrayList<Integer>(mps.getR());
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
			newTotalA += f.getA(i);
		}
		// Add max-ratio items until knapsack full
		while (ratio.size() > 0 && newTotalA < f.getB()) {
			ratioNode rni = ratio.get(ratio.size()-1);
			if (newTotalA + f.getA(rni.x) <= f.getB()) {
				ratio.remove(ratio.size()-1);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += f.getA(rni.x);
			} else {
				ratio.remove(ratio.size()-1);
			}
		}
		// Calculate obj of new solution
		double obj = f.getObj(x);
		return new FractionalSol(x,r,obj,newTotalA, f.getNum(), f.getDen());
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
			ratioNode rni = new ratioNode(i, f.getRatio(i));
			ratio.add(rni);
		}
		Collections.sort(ratio);
		return ratio;
	}

	/**
	 * Perform a ratio mutation
	 * - Find the minRatio item in the solution
	 * - Swap it with a random item outside the solution
	 * 
	 * @return mutated solution
	 */
	private FractionalSol ratioMutate() {
		FractionalSol result = null;
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
			while ((f.getA(j) - f.getA(i) > f.getB() - getTotalA()) && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = curR.get(j);
				rndCount++;
			}

			if (f.getA(j) - f.getA(i) <= f.getB() - getTotalA()) {

				result = new FractionalSol(this);
				result.swap(i,j);
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
	private FractionalSol bestRatioMutate() {
		FractionalSol result = null;
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
				if (newObj > maxObj && f.getA(j) - f.getA(i) <= f.getB() - getTotalA()) {
					maxObj = newObj;
					maxJ = j;
				}
			}
			if (maxJ != -1) {
				result = new FractionalSol(this);
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
	private FractionalSol[] maxMinSwap(int iteration, int[][] tabuList) {
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
		while ((f.getA(j) - f.getA(i) > f.getB() - getTotalA()) && ki < getXSize()) {
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

		if (f.getA(j) - f.getA(i) > f.getB() - getTotalA()) {
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
			while (tabuList[i][j] >= iteration && (f.getA(j) - f.getA(i) > f.getB() - getTotalA()) && ki < getXSize()) {
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
				if (f.getA(j) - f.getA(i) <= getB() - getTotalA()) {
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
		FractionalSol[] results = new FractionalSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new FractionalSol(this);
			results[0].swap(bi,bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new FractionalSol(this);
			results[1].swap(ni,nj);
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
	private FractionalSol[] ratioMutate(int iteration, int[][] tabuList) {
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
		while (tabuList[i][j] >= iteration && (f.getA(j) - f.getA(i) > f.getB() - getTotalA()) && ki < n) {
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
			if (f.getA(j) - f.getA(i) <= getB() - getTotalA()) {
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
		FractionalSol[] results = new FractionalSol[2];
		if (bi != -1 && bj != -1 && f.getA(bj) - f.getA(bi) <= getB() - getTotalA()) {
			results[0] = new FractionalSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1 && f.getA(nj) - f.getA(ni) <= getB() - getTotalA() && tabuList[ni][nj] < iteration) {
			results[1] = new FractionalSol(this);
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
				if (f.getRatio(i) < minRatio && !bestIs.contains(i)) {
					minRatio = f.getRatio(i);
					minI = i;
				}
			}
			minRatio = Double.MAX_VALUE;
			bestIs.add(minI);
		}
		return minI;
	}

	/**
	 * Determine the kth maximum ratio item currently outside the solution
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
				if (f.getRatio(i) > maxRatio && !bestIs.contains(i)) {
					maxRatio = f.getRatio(i);
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
	private FractionalSol[] bestSwap(int iteration, int[][] tabuList) {
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
				if (f.getA(j)-f.getA(i) <= f.getB() - curTotalA) {
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
		FractionalSol[] results = new FractionalSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new FractionalSol(this);
			results[0].swap(bi,bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new FractionalSol(this);
			results[1].swap(ni,nj);
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
	private FractionalSol[] firstSwap(int iteration, int[][] tabuList) {
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
				if (f.getA(j)-f.getA(i) <= f.getB() - curTotalA) {
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
						FractionalSol[] results = new FractionalSol[2];
						if (bi != -1 && bj != -1) {
							results[0] = new FractionalSol(this);
							results[0].swap(bi,bj);
						}
						results[1] = new FractionalSol(this);
						results[1].swap(ni,nj);
						return results;
					}
				}
			}
		}
		// Compile and return data
		FractionalSol[] results = new FractionalSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new FractionalSol(this);
			results[0].swap(bi,bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new FractionalSol(this);
			results[1].swap(ni,nj);
		}
		return results;
	}

	@Override
	/**
	 * Heal the solution if it is invalid
	 */
	public void healSol() {
		healSolImproving();
		//	healSolRatio();
	}

	/**
	 * Heal the solution by removing the item that results in the best objective
	 *  until the solution is valid.
	 */
	private void healSolImproving() {
		while(!this.getValid()) {
			double maxObj = -1*Double.MAX_VALUE;
			int maxI = -1;
			for (Integer i: this.getX()) {
				double newObj = subObj(i, num, den);
				if (newObj > maxObj) {
					maxObj = newObj;
					maxI = i;
				}
			}
			if (maxI != -1) {
				removeI(maxI);
				removeA(maxI);
				setObj(maxObj);
				this.num = subNum(maxI, this.num);
				this.den = subDen(maxI, this.den);
			} else {
				System.err.println("Couldn't find an improving objective!!!");
				System.exit(-1);
			}
		}
	}

	/**
	 * Heal the solution by removing minRatio items until valid
	 */
	private void healSolRatio() {
		while(!this.getValid()) {
			int j = minRatio(0);
			removeI(j);
			removeA(j);
			setObj(subObj(j, num, den));
			num = subNum(j, num);
			den = subDen(j, den);
		}
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
			for (int i = 0; i < f.getM(); i++) {
				pw.write(num[i] + " ");
			}
			pw.write("\n");
			for (int i = 0; i < f.getM(); i++) {
				pw.write(den[i] + " ");
			}
			pw.write("\n");
			Collections.sort(getX());
			for (Integer i: getX()) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Read a solution from the given filename
	 * 
	 * @param filename to read
	 */
	public void readSolution(String filename) { 
		f = (Fractional)p;
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			int readTotalA = scr.nextInt();
			long[] readNum = new long[f.getM()];
			long[] readDen = new long[f.getM()];
			for (int i = 0; i < f.getM(); i++) {
				readNum[i] = scr.nextLong();
			}
			for (int i = 0; i < f.getM(); i++) {
				readDen[i] = scr.nextLong();
			}
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
				setNum(readNum);
				setDen(readDen);
				setTotalA(readTotalA);
			} else {
				System.err.println("NO INCUMBENT SOLUTION IN FILE");
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

}
