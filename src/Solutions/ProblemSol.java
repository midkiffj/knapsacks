package Solutions;

import java.util.ArrayList;
import java.util.Collections;
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

	/**
	 * Create solution lists and update global problem
	 */
	public ProblemSol() {
		updateProblem();
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		xVals = new boolean[n];
	}

	/**
	 * Updates global problem and number of variables
	 */
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

	/**
	 * Return the solution objective
	 */
	public double getObj() {
		return obj;
	}
	
	/**
	 * Set the solution objective
	 */
	public void setObj(double newObj) {
		obj = newObj;
	}

	/**
	 * Get the entire solution array
	 */
	public ArrayList<Integer> getX() {
		return x;
	}
	
	/**
	 * Set the entire solution array
	 */
	public void setX(ArrayList<Integer> newX) {
		x = new ArrayList<Integer>(newX);
	}

	/**
	 * Get the entire unused variable array
	 */
	public ArrayList<Integer> getR() {
		return r;
	}
	
	/**
	 * Set the entire solution array
	 */
	public void setR(ArrayList<Integer> newR) {
		r = new ArrayList<Integer>(newR);
	}

	/**
	 * Return the size of the solution
	 */
	public int getXSize() {
		return x.size();
	}

	/**
	 * Return the amount of unused variables
	 */
	public int getRSize() {
		return r.size();
	}

	/**
	 * Return xVals[i] where xVals[i] = true iff i is in the solution
	 */
	public boolean getXVals(int i) {
		return xVals[i];
	}
	
	/**
	 * Set xVals[i] to the specified boolean
	 */
	public void setXVals(int i, boolean val) {
		xVals[i] = val;
	}
	
	/**
	 * Return if the solution is feasible/valid
	 */
	public boolean getValid() {
		updateValid();
		return valid;
	}
	
	/**
	 * Set the problem validity
	 */
	public void setValid(boolean newValid) {
		valid = newValid;
	}
	
	/**
	 * Update the problem validity
	 */
	public abstract void updateValid();

	
	//*****************
	// Mutation Methods
	//*****************
	
	/**
	 * Add variable i to the solution lists
	 */
	public void addI(int i) {
		xVals[i] = true;
		x.add(i);
		r.remove(Integer.valueOf(i));
	}
	
	/**
	 * Remove variable i from the solution lists
	 */
	public void removeI(int i) {
		xVals[i] = false;
		r.add(i);
		x.remove(Integer.valueOf(i));
	}
	
	/**
	 * Add the item to the solution
	 */
	public abstract void addX(int i);
	
	/**
	 * Add the item to the solution
	 */
	public abstract void removeX(int i);
	
	/**
	 * Swap item i out of the solution and item j into the solution
	 */
	public abstract void swap(int i, int j);
	
	/**
	 * Calculate the objective if i and j are swapped
	 */
	public abstract double swapObj(int i, int j);
	
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
		ProblemSol newPS = ProblemSol.copy(this);
		for (int i = 0; i < n; i++) {
			if (this.getXVals(i) != ps2.getXVals(i)) {
				newPS.removeX(i);
			} 
		}

		ArrayList<ratioNode> ratio = computeRatios(newPS.getX(), newPS.getR());

		while (ratio.size() > 0 && newPS.getValid()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newPS.addValid(rni.x)) {
				newPS.addX(rni.x);
			} else {
				ratio.remove(i);
			}
		}
		return newPS;
	}

	/**
	 * Perform a mutation for the genetic algorithm
	 * 
	 * @param removeAttempts - number of genMutate2 calls
	 * @return mutated solution
	 */
	public ProblemSol genMutate(int removeAttempts) {
		ProblemSol newPS = ProblemSol.copy(this);
		if (rnd.nextDouble() < 0.5) {
			if (newPS.getRSize() == 0) {
				newPS.shift();
			} else {
				return newPS.mutate();
			}
		} else {
			newPS = genMutate2(newPS, removeAttempts);
			if (removeAttempts < n-1) {
				removeAttempts++;
			}
		}
		return newPS;
	}
	
	/**
	 * Randomly remove a number of items from the solution nd fill with max-ratio items
	 * 
	 * @param ps - the solution to mutate
	 * @param removeAttempts - the number of items to remove (increases with each call)
	 * @return mutated solution
	 */
	private ProblemSol genMutate2(ProblemSol ps, int removeAttempts) {
		// Remove s items from the solution
		int s = removeAttempts;
		if (s >= getXSize()) {
			s = getXSize()-1;
		}
		for (int i = 0; i < s; i++) {
			int j = rnd.nextInt(ps.getXSize());
			j = ps.getX().get(j);
			ps.removeX(j);
		}

		// Compute ratios
		ArrayList<ratioNode> ratio = computeRatios(ps.getX(), ps.getR());
		Collections.sort(ratio);

		// Add max-ratio items until knapsack full
		while (ratio.size() > 0 && ps.getValid()) {
			ratioNode rni = ratio.get(ratio.size()-1);
			if (ps.addValid(rni.x)) {
				ps.addX(rni.x);
			}
		}
		return ps;
	}

	/**
	 * Create a list of ratioNodes for the given solution lists
	 * 
	 * @param x - solution list
	 * @param r - list of items not i the solution
	 * @return list of ratioNodes
	 */
	public ArrayList<ratioNode> computeRatios(ArrayList<Integer> x, ArrayList<Integer> r) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: r) {
			ratioNode rni = new ratioNode(i, p.getRatio(i));
			ratio.add(rni);
		}
		Collections.sort(ratio);
		return ratio;
	}

	/**
	 * Current unused method that 
	 * 	proportionally performs ratioMutates or bestRatioMutates.
	 * 
	 * @return mutated solution
	 */
	@SuppressWarnings("unused")
	private ProblemSol swapMutate() {
		if (rnd.nextDouble() < 0.8) {
			return ratioMutate();
		} else {
			return bestRatioMutate();
		}
	}
	
	/**
	 * Perform a ratio mutation
	 * - Find the minRatio item in the solution
	 * - Swap it with a random item outside the solution
	 * 
	 * @return mutated solution
	 */
	private ProblemSol ratioMutate() {
		ProblemSol result = null;
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
			while (!swapValid(i,j) && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = curR.get(j);
				rndCount++;
			}

			if (swapValid(i,j)) {
				result = ProblemSol.copy(this);
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
	private ProblemSol bestRatioMutate() {
		ProblemSol result = null;
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
				if (newObj > maxObj && swapValid(i,j)) {
					maxObj = newObj;
					maxJ = j;
				}
			}
			if (maxJ != -1) {
				result = ProblemSol.copy(this);
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
	private ProblemSol[] maxMinSwap(int iteration, int[][] tabuList) {
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
		while (!swapValid(i,j) && ki < getXSize()) {
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

		if (!swapValid(i,j)) {
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
			while (tabuList[i][j] >= iteration && !swapValid(i,j) && ki < getXSize()) {
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
				if (swapValid(i,j)) {
					newObj = swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
			if (tabuList[i][j] < iteration && swapValid(i,j)) {
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
		ProblemSol[] results = new ProblemSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = ProblemSol.copy(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = ProblemSol.copy(this);
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
	private ProblemSol[] ratioMutate(int iteration, int[][] tabuList) {
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
		while (tabuList[i][j] >= iteration && !swapValid(i,j) && ki < n) {
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
			if (swapValid(i,j)) {
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
		ProblemSol[] results = new ProblemSol[2];
		if (bi != -1 && bj != -1 && swapValid(bi,bj)) {
			results[0] = ProblemSol.copy(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1 && swapValid(ni,nj) && tabuList[ni][nj] < iteration) {
			results[1] = ProblemSol.copy(this);
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
	public int minRatio(int k) {
		// Find the minimum ratio in the solution
		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < getXSize()) {
			for (Integer i: getX()) {
				if (p.getRatio(i) < minRatio && !bestIs.contains(i)) {
					minRatio = p.getRatio(i);
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
	public int maxRatio(int k) {
		// Find the maximum ratio not in the solution
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < getRSize()) {
			for (Integer i: getR()) {
				if (p.getRatio(i) > maxRatio && !bestIs.contains(i)) {
					maxRatio = p.getRatio(i);
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
	private ProblemSol[] bestSwap(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;
		for(Integer i: getX()) {
			for(Integer j: getR()) {
				// Check for problem feasibility
				if (swapValid(i,j)) {
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
		ProblemSol[] results = new ProblemSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = ProblemSol.copy(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = ProblemSol.copy(this);
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
	private ProblemSol[] firstSwap(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;
		for(Integer i: getX()) {
			for(Integer j: getR()) {
				// Check for problem feasibility
				if (swapValid(i,j)) {
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
					if (ni != -1 && nj != -1)  {
						ProblemSol[] results = new ProblemSol[2];
						if (bi != -1 && bj != -1) {
							results[0] = ProblemSol.copy(this);
							results[0].swap(bi, bj);
						}
						results[1] = ProblemSol.copy(this);
						results[1].swap(ni, nj);
						return results;
					}
				}
			}
		}
		ProblemSol[] results = new ProblemSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = ProblemSol.copy(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = ProblemSol.copy(this);
			results[1].swap(ni, nj);
		}
		return results;
	}
	
	/**
	 * Return if adding item i will keep the problem feasible 
	 */
	public abstract boolean addValid(int i);
	
	/**
	 * Return if removing item i will keep the problem feasible 
	 */
	public abstract boolean subValid(int i);
	
	/**
	 * Return if swapping item i and item j will keep the problem feasible 
	 */
	public abstract boolean swapValid(int i, int j);

	/**
	 *  Shift a variable in or out of the current solution
	 *  
	 *  @return item shifted or -1 if no change
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
	public abstract int tryAdd();
	
	/**
	 * Try to remove a variable from the solution
	 * 
	 * @return the item removed or -1 if none added
	 */
	public abstract int trySub();
	
	/**
	 * Heal/Repair the solution by making it feasible
	 */
	public abstract void healSol();

	/**
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
	/**
	 * Comparison for solutions used for genetic algorithm population
	 * 
	 * 1) Invalid < Valid
	 * 2) Higher objective is better
	 */
	public int compare(ProblemSol o1, ProblemSol o2) {
		return o1.compareTo(o2);
	}
	
	//************
	// File I/O
	//************
	public abstract void writeSolution(String filename);
	
	public abstract void readSolution(String filename);
}
