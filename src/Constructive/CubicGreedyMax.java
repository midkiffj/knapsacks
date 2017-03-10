package Constructive;

import java.util.ArrayList;
import java.util.Collections;

import Problems.Cubic;
import Solutions.CubicSol;
import Solutions.ProblemSol;
import Solutions.ratioNode;

/**
 * Greedy Heuristic to the Cubic Knapsack
 * - Fills the knapsack with items that maximize the gain-to-weight ratio
 * 
 * @author midkiffj
 */
public class CubicGreedyMax extends ConstHeuristic {

	private Cubic c;

	/**
	 * Specify the problem to solve
	 * 
	 * @param c Cubic problem
	 */
	public CubicGreedyMax(Cubic c) {
		super();
		this.c = c;
	}

	protected ProblemSol construct() {
		return greedyHeuristicMax();
	}

	/**
	 * Creates a solution by:
	 * - Adding an item with the maximum 'ratio' until Ax <= b
	 * 
	 * @return solution constructed
	 */
	private CubicSol greedyHeuristicMax() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		// Add all items to knapsack
		int totalA = 0;
		for (int i = 0; i < c.getN(); i++) {
			r.add(i);
		}

		int b = c.getB();
		// Remove items, picking the item that maximize the gain-to-weight ratio
		ArrayList<ratioNode> ratio = computeMaxRatio(x,r);
		while (totalA <= b && ratio.size() > 0) {
			int i = ratio.remove(ratio.size()-1).x;
			if (totalA + c.getA(i) <= b) {
				r.remove(Integer.valueOf(i));
				x.add(i);
				totalA += c.getA(i);
				updateMaxRatio(x,ratio,i);
			}
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
	private ArrayList<ratioNode> computeMaxRatio(ArrayList<Integer> x,  ArrayList<Integer> r) {
		// List of ratios to return
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		// For each item
		for (Integer i: r) {
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
			// Compute gain-to-weight and store as ratioNode
			double gainToWeight = (double)objChange / c.getA(i);
			ratioNode rni = new ratioNode(i, gainToWeight);
			rni.objChange = objChange;
			ratio.add(rni);
		}
		// Sort ratios
		Collections.sort(ratio);
		return ratio;
	}

	/**
	 * Update the ratios by adding the specified item to the ratio calculation
	 *  for every item
	 *  
	 * @param x - list of items in the solution
	 * @param ratio - list of ratioNodes
	 * @param j - item added
	 */
	private void updateMaxRatio(ArrayList<Integer> x, ArrayList<ratioNode> ratio, int j) {
		// For each item left
		for (ratioNode rni: ratio) {
			int i = rni.x;
			// Get objective change
			long objChange = rni.objChange;
			// Subtract the contribution with the specified item
			objChange += c.getCij(i,j);
			for (int k = 0; k < x.size(); k++) {
				int xk = x.get(k);
				objChange += c.getDijk(i,j,xk);
			}
			// Recompute ratio and update node
			double gainToWeight = (double)objChange / c.getA(i);
			rni.ratio = gainToWeight;
			rni.objChange = objChange;
		}
		// Sort ratios
		Collections.sort(ratio);
	}
}
