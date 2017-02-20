package Constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import Problems.Cubic;
import Runner.RndGen;
import Solutions.CubicSol;
import Solutions.ProblemSol;

/**
 * Greedy Heuristic + Fill Up N Exchange
 * 
 * @author midkiffj
 */
public class CubicGreedyFill extends ConstHeuristic {

	private Cubic c;
	private Random rnd = RndGen.getRnd();

	/*
	 * Specify problem to solve
	 */
	public CubicGreedyFill(Cubic c) {
		this.c = c;
	}

	protected ProblemSol construct() {
		return hybrid();
	}	

	/*
	 * Compute a greedy algorithm until the knapsack constraint is feasible
	 * Then try to use the fill algorithm to improve it
	 * 
	 */
	private CubicSol hybrid() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		// Add all items to knapsack
		int totalA = 0;
		for (int i = 0; i < c.getN(); i++) {
			x.add(i);
			totalA += c.getA(i);
		}

		int b = c.getB();
		// Remove items, picking the item that minimizes the loss-to-weight ratio
		ArrayList<ratioNode> ratio = computeRatio(x);
		while (totalA > b) {
			int i = ratio.remove(0).x;
			x.remove(Integer.valueOf(i));
			r.add(i);
			totalA -= c.getA(i);
			updateRatio(x,ratio,i);
		}


		return fillUpNExchange(x,r,totalA);
	}

	/*
	 *  Complete bestImprovingSwaps or additions until no more items can be 
	 *  either swapped or shifted
	 */	
	private CubicSol fillUpNExchange(ArrayList<Integer> x, ArrayList<Integer> r, int totalA) {
		CubicSol current = new CubicSol(x,r);

		boolean done = false;
		double curObj = current.getObj();
		while (!done) {
			boolean swap = false;
			// Perform an operation
			if (rnd.nextDouble() < 0.5) {
				bestImprovingSwap(current);
				swap = true;
			} else {
				current.tryAdd();
			}

			// If no change, try the opposite operation
			if (curObj == current.getObj()) {
				if (swap) {
					current.tryAdd();
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

	/*
	 *  Perform the best improving swap that keeps the knapsack feasible
	 */
	private void bestImprovingSwap(CubicSol current) {
		// Store b
		int b = c.getB();
		int curTotalA = current.getTotalA();
		// Store best swaps
		int bi = -1;
		int bj = -1;
		double bObj = current.getObj();
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= b - curTotalA) {
					double newObj = current.swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		// Perform the best improving swap (if found)
		if (bi != -1) {
			current.swap(bObj,bi,bj);
		}
	}

	/*
	 * Compute an items ratio:
	 * - Sum each item's current contribution to the objective
	 * - Divide the contribution by the item's weight
	 * Store the ratios in a list of ratioNodes
	 */
	private ArrayList<ratioNode> computeRatio(ArrayList<Integer> x) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		// For each item
		for (Integer i: x) {
			// Calculate the objective change if it was removed
			// (positive: obj function goes down)
			// (negative: obj function goes up)
			// We want negative ratios since that'll improve the objective
			long objChange = c.getCi(i);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				objChange += c.getCij(i,xj);
				for (int k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					objChange += c.getDijk(i,xj,xk);
				}
			}
			// Compute loss-to-weight and store as ratioNode
			double lossToWeight = (double)objChange / c.getA(i);
			ratioNode rni = new ratioNode(i, lossToWeight);
			rni.objChange = objChange;
			ratio.add(rni);
		}
		// Sort ratios
		Collections.sort(ratio);
		return ratio;
	}

	/*
	 *  Update the ratios by removing the specified item from the ratio calculation
	 *  for every other item
	 */
	private void updateRatio(ArrayList<Integer> x, ArrayList<ratioNode> ratio, int j) {
		// For each item left
		for (ratioNode rni: ratio) {
			int i = rni.x;
			// Get objective change
			long objChange = rni.objChange;
			// Subtract the contribution with the specified item
			objChange -= c.getCij(i,j);
			for (int k = 0; k < x.size(); k++) {
				int xk = x.get(k);
				objChange -= c.getDijk(i,j,xk);
			}
			// Recompute ratio and update node
			double lossToWeight = (double)objChange / c.getA(i);
			rni.ratio = lossToWeight;
			rni.objChange = objChange;
		}
		// Sort ratios
		Collections.sort(ratio);
	}

	/*
	 *  Class used to store an items current objective contribution and ratio
	 */
	private class ratioNode implements Comparable<ratioNode>{
		int x;
		long objChange;
		double ratio;

		public ratioNode(int x, double ratio) {
			this.x = x;
			this.ratio = ratio;
		}

		@Override
		public int compareTo(ratioNode o) {
			if (this.ratio - o.ratio > 0) {
				return 1;
			} else if (this.ratio - o.ratio < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
