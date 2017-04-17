package Constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import Problems.Fractional;
import Runner.RndGen;
import Solutions.FractionalSol;
import Solutions.ProblemSol;
import Solutions.ratioNode;

/**
 * Greedy Heuristic + FillUpNExchange for Fractional
 * 
 * @author midkiffj
 */
public class FractionalGreedyFill extends ConstHeuristic {

	private Fractional f;
	private Random rnd = RndGen.getRnd();
	private long[] num;
	private long[] den;

	/**
	 * Specify the problem to solve
	 * 
	 * @param f Fractional problem
	 */
	public FractionalGreedyFill(Fractional f) {
		super();
		this.f = f;
		num = new long[f.getM()];
		den = new long[f.getM()];
	}

	protected ProblemSol construct() {
		return greedyHeuristic();
	}

	/**
	 * Creates a solution by:
	 * - Adding all items to the knapsack
	 * - Removes an item with the minimum 'ratio' until Ax <= b
	 * 
	 * @return solution constructed
	 */
	private FractionalSol greedyHeuristic() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		// Add all items to knapsack
		for (int i = 0; i < f.getN(); i++) {
			x.add(i);
		}
		FractionalSol fs = new FractionalSol(x,r);
		num = fs.getNum();
		den = fs.getDen();


		// Remove items, picking the item that minimizes the loss-to-weight ratio
		ArrayList<ratioNode> ratio = computeRatio(x, fs.getObj());
		while (ratio.size() > 0 && (ratio.get(0).ratio < 0 || !fs.getValid())) {
			int i = ratio.remove(0).x;
			fs.removeX(i);
			num = fs.getNum();
			den = fs.getDen();
			updateRatio(x,fs.getObj(),ratio,i);
		}

		return fillUpNExchange(x,r);
	}

	/**
	 * Compute an items ratio:
	 * - Sum each item's current contribution to the objective
	 * - Divide the contribution by the item's weight
	 * Store the ratios in a list of ratioNodes
	 * 
	 * @param x - the list of items in the solution
	 */
	private ArrayList<ratioNode> computeRatio(ArrayList<Integer> x, double curObj) {
		// List of ratios to return
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		// For each item
		for (Integer i: x) {
			// Calculate the objective change if it was removed
			// (positive: obj function goes down)
			// (negative: obj function goes up)
			// We want negative ratios since that'll improve the objective
			double newObj = subObj(i,num,den);
			double objChange = newObj - curObj;
			// Compute loss-to-weight and store as ratioNode
			double lossToWeight = objChange / f.getA(i);
			ratioNode rni = new ratioNode(i, lossToWeight);
			ratio.add(rni);
		}
		// Sort ratios
		Collections.sort(ratio);
		return ratio;
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
	 * Update the ratios by removing the specified item from the ratio calculation
	 *  for every other item
	 *  
	 * @param x - list of items in the solution
	 * @param ratio - list of ratioNodes
	 * @param j - item removed
	 */
	private void updateRatio(ArrayList<Integer> x, double curObj, ArrayList<ratioNode> ratio, int j) {
		// For each item left
		for (ratioNode rni: ratio) {
			int i = rni.x;
			// Get objective change
			double newObj = subObj(i,num,den);
			double objChange = newObj - curObj;
			// Recompute ratio and update node
			double lossToWeight = objChange / f.getA(i);
			rni.ratio = lossToWeight;
		}
		// Sort ratios
		Collections.sort(ratio);
	}
	
	/**
	 * Complete bestImprovingSwaps or additions until no more items can be 
	 *	either swapped or added
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items outside solution
	 * @return solution constructed
	 */
	private FractionalSol fillUpNExchange(ArrayList<Integer> x, ArrayList<Integer> r) {
		FractionalSol current = new FractionalSol(x,r);

		boolean done = false;
		double curObj = current.getObj();
		while (!done) {
			boolean swap = false;
			// Perform an operation (50/50 swap or shift)
			if (rnd.nextDouble() < 0.5) {
				bestImprovingSwap(current);
				swap = true;
			} else {
				tryAdd(current);
			}

			// If no change, try the opposite operation
			if (curObj == current.getObj()) {
				if (swap) {
					tryAdd(current);
				} else {
					bestImprovingSwap(current);
				}
			}

			// If no change overall, stop
			if (curObj == current.getObj()) {
				done = true;
			} else {
				curObj = current.getObj();
			}
		}
		return current;
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
		for (int j = 0; j < f.getM(); j++) {
			if (den[j]+f.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]+f.getC(j,i))/(den[j]+f.getD(j,i));
		}

		return obj;
	}

	/**
	 *  Perform the best improving swap that keeps the knapsack feasible
	 *  
	 *  @param current solution to improve
	 */
	private void bestImprovingSwap(FractionalSol current) {
		// Store best swaps
		int bi = -1;
		int bj = -1;
		double bObj = current.getObj();
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (current.swapValid(i, j)) {
					double newObj = current.swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}

		// Swap the best improving swap (if found)
		if (bi != -1) {
			current.swap(bi,bj);
		}
	}
}
