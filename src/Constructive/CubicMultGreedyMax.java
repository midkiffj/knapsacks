package Constructive;

import java.util.ArrayList;
import java.util.Collections;

import Problems.CubicMult;
import Solutions.CubicMultSol;
import Solutions.ProblemSol;
import Solutions.ratioNode;

/**
 * Greedy Heuristic to the Cubic Knapsack
 * - Fills the knapsack with items that maximize the gain-to-weight ratio
 * 
 * @author midkiffj
 */
public class CubicMultGreedyMax extends ConstHeuristic {

	private CubicMult cm;

	/**
	 * Specify the problem to solve
	 * 
	 * @param cm Cubic problem
	 */
	public CubicMultGreedyMax(CubicMult cm) {
		super();
		this.cm = cm;
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
	private CubicMultSol greedyHeuristicMax() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		// Add all items to knapsack
		int[] totalA = new int[cm.getM()];
		for (int i = 0; i < cm.getN(); i++) {
			r.add(i);
		}

		// Remove items, picking the item that maximize the gain-to-weight ratio
		ArrayList<ratioNode> ratio = computeMaxRatio(x,r);
		while (validTotalA(totalA) && ratio.size() > 0) {
			int i = ratio.remove(ratio.size()-1).x;
			if (addTotalA(totalA,i)) {
				r.remove(Integer.valueOf(i));
				x.add(i);
				addA(i,totalA);
				updateMaxRatio(x,ratio,i);
			}
		}

		return new CubicMultSol(x,r);
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
			// We want max positive ratios since that'll improve the objective
			long objChange = cm.getCi(i);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				objChange += cm.getCij(i,xj);
				for (int k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					objChange += cm.getDijk(i,xj,xk);
				}
			}
			// Compute gain-to-weight and store as ratioNode
			double sumRatios = 0;
			for (int k = 0; k < cm.getM(); k++) {
				sumRatios += (double)(objChange)/cm.getA(k,i);
			}
			double gainToWeight = (double)sumRatios / cm.getM();
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
			// Add the contribution with the specified item
			objChange += cm.getCij(i,j);
			for (int k = 0; k < x.size(); k++) {
				int xk = x.get(k);
				objChange += cm.getDijk(i,j,xk);
			}
			// Recompute ratio and update node
			double sumRatios = 0;
			for (int k = 0; k < cm.getM(); k++) {
				sumRatios += (double)(objChange)/cm.getA(k,i);
			}
			double gainToWeight = (double)sumRatios / cm.getM();
			rni.ratio = gainToWeight;
			rni.objChange = objChange;
		}
		// Sort ratios
		Collections.sort(ratio);
	}
	
	/**
	 * Determine if the given weights are valid (feasible)
	 */
	private boolean validTotalA(int[] totalA) {
		for (int i = 0; i < cm.getM(); i++) {
			if (totalA[i] > cm.getB(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Add the given item to the specified weights
	 */
	private void addA(int j, int[] totalA) {
		for (int i = 0; i < cm.getM(); i++) {
			totalA[i] += cm.getA(i,j);
		}
	}
	
	/**
	 * Return if adding the item to the given weight 
	 * 	will keep the problem feasible
	 * 
	 * @param totalA solution weights
	 * @param j - item to add
	 * @return (T) if j can be added and maintain problem feasibility
	 */
	private boolean addTotalA(int[] totalA, int j) {
		for (int i = 0; i < cm.getM(); i++) {
			if (totalA[i] + cm.getA(i,j) > cm.getB(i)) {
				return false;
			}
		}
		return true;
	}
}
