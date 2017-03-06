package Constructive;

import java.util.ArrayList;
import java.util.Collections;

import Problems.CubicMult;
import Solutions.CubicMultSol;
import Solutions.ProblemSol;
import Solutions.ratioNode;

/**
 * Greedy Heuristic to the Cubic Multiple Knapsack
 * - Fills the solution with all items
 * - Removes the item that best improves the objective
 * 
 * @author midkiffj
 */
public class CubicMultGreedy extends ConstHeuristic {

	private CubicMult cm;
	
	/**
	 * Specify problem to solve
	 * 
	 * @param CubicMult problem
	 */
	public CubicMultGreedy(CubicMult cm) {
		super();
		this.cm = cm;
	}

	/**
	 * Construct a solution to the problem
	 * 
	 * @return solution constructed
	 */
	protected ProblemSol construct() {
		return greedyHeuristic();
	}
	
	/**
	 * Creates a solution by:
	 * - Adding all items to the solution
	 * - Removes an item with the minimum 'ratio' until Ax <= b
	 * 
	 * @return solution constructed
	 */
	private CubicMultSol greedyHeuristic() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		// Add all items to knapsack
		for (int i = 0; i < cm.getN(); i++) {
			x.add(i);
		}
		
		CubicMultSol cms = new CubicMultSol(x,r);

		// Remove items, picking the item that minimizes the loss-to-weight ratio
		ArrayList<ratioNode> ratio = computeRatio(x);
		while (!cms.getValid()) {
			int i = ratio.remove(0).x;
			cms.removeI(i);
			cms.removeA(i);
			updateRatio(cms.getX(),ratio,i);
		}

		return new CubicMultSol(cms.getX(),cms.getR());
	}
	
	/**
	 * Compute an items ratio:
	 * - Sum each item's current contribution to the objective
	 * - Divide the contribution by the item's weight
	 * Store the ratios in a list of ratioNodes
	 * 
	 * @param x - solution list
	 * @return list linking items to ratios/objective changes
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
			long objChange = cm.getCi(i);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				objChange += cm.getCij(i,xj);
				for (int k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					objChange += cm.getDijk(i,xj,xk);
				}
			}
			// Compute loss-to-weight and store as ratioNode
			double sumRatios = 0;
			for (int j = 0; j < cm.getM(); j++) {
				sumRatios += (double)(objChange)/cm.getA(j,i);
			}
			double lossToWeight = (double)sumRatios / cm.getM();
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
			objChange -= cm.getCij(i,j);
			for (int k = 0; k < x.size(); k++) {
				int xk = x.get(k);
				objChange -= cm.getDijk(i,j,xk);
			}
			// Recompute ratio and update node
			double sumRatios = 0;
			for (int k = 0; k < cm.getM(); k++) {
				sumRatios += (double)(objChange)/cm.getA(k,i);
			}
			double lossToWeight = (double)sumRatios / cm.getM();
			rni.ratio = lossToWeight;
			rni.objChange = objChange;
		}
		// Sort ratios
		Collections.sort(ratio);
	}
}
