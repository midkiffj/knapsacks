package Constructive;

import java.util.ArrayList;
import java.util.Collections;

import Problems.Cubic;
import Solutions.CubicSol;
import Solutions.ProblemSol;
import Solutions.ratioNode;

/**
 * Greedy Heuristic to the Cubic Knapsack
 * - Fills the knapsack with all items
 * - Removes the item that best improves the objective
 * 
 * @author midkiffj
 */
public class CubicGreedy extends ConstHeuristic {

	private Cubic c;
	
	/**
	 * Specify the problem to solve
	 * 
	 * @param c Cubic problem
	 */
	public CubicGreedy(Cubic c) {
		super();
		this.c = c;
	}

	protected ProblemSol construct() {
		return greedyHeuristic2();
	}
	
	/**
	 * Creates a solution by:
	 * - Adding all items to the knapsack
	 * - Removes an item with the minimum 'ratio' until Ax <= b
	 * 
	 * @return solution constructed
	 */
	private CubicSol greedyHeuristic2() {
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

		return new CubicSol(x,r);
	}
	
	/**
	 * Compute an items ratio:
	 * - Sum each item's current contribution to the objective
	 * - Divide the contribution by the item's weight
	 * Store the ratios in a list of ratioNodes
	 * 
	 * @param x - the list of items in the solution
	 */
	private ArrayList<ratioNode> computeRatio(ArrayList<Integer> x) {
		// List of ratios to return
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
	
	/**
	 * Update the ratios by removing the specified item from the ratio calculation
	 *  for every other item
	 *  
	 * @param x - list of items in the solution
	 * @param ratio - list of ratioNodes
	 * @param j - item removed
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
}
