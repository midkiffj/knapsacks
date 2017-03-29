package Constructive;

import java.util.ArrayList;
import java.util.Collections;

import Problems.Fractional;
import Solutions.FractionalSol;
import Solutions.ProblemSol;
import Solutions.ratioNode;

/**
 * Greedy Heuristic to the Cubic Knapsack
 * - Fills the knapsack with all items
 * - Removes the item with the minimum loss-to-weight ratio
 * 
 * @author midkiffj
 */
public class FractionalGreedyMax extends ConstHeuristic {

	private Fractional f;
	private long[] num;
	private long[] den;

	/**
	 * Specify the problem to solve
	 * 
	 * @param c Cubic problem
	 */
	public FractionalGreedyMax(Fractional f) {
		super();
		this.f = f;
		num = new long[f.getM()];
		den = new long[f.getM()];
	}

	protected ProblemSol construct() {
		return greedyMaxHeuristic();
	}

	/**
	 * Creates a solution by:
	 * - Adding all items to the knapsack
	 * - Removes an item with the minimum 'ratio' until Ax <= b
	 * 
	 * @return solution constructed
	 */
	private FractionalSol greedyMaxHeuristic() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		// Add all items to knapsack
		for (int i = 0; i < f.getN(); i++) {
			r.add(i);
		}
		FractionalSol fs = new FractionalSol(x,r);
		num = fs.getNum();
		den = fs.getDen();


		// Remove items, picking the item that minimizes the loss-to-weight ratio
		ArrayList<ratioNode> ratio = computeMaxRatio(fs.getR(), fs.getObj());
		while (fs.getValid() && ratio.size() > 0 && ratio.get(ratio.size()-1).ratio > 0) {
			ratioNode rni = ratio.remove(ratio.size()-1);
			int i = rni.x;
			if (fs.getTotalA() + f.getA(i) <= fs.getB()) {
				fs.addX(i);
				num = fs.getNum();
				den = fs.getDen();
				updateMaxRatio(x,fs.getObj(),ratio,i);
			}
		}

		return new FractionalSol(fs.getX(),fs.getR());
	}

	/**
	 * Compute an items ratio:
	 * - Sum each item's current contribution to the objective
	 * - Divide the contribution by the item's weight
	 * Store the ratios in a list of ratioNodes
	 * 
	 * @param x - the list of items in the solution
	 */
	private ArrayList<ratioNode> computeMaxRatio(ArrayList<Integer> r, double curObj) {
		// List of ratios to return
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		// For each item
		for (Integer i: r) {
			// Calculate the objective change if it was added
			// (positive: obj function goes up)
			// (negative: obj function goes down)
			// We want positive ratios since that'll improve the objective
			double newObj = addObj(i,num,den);
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
	 * Update the ratios by removing the specified item from the ratio calculation
	 *  for every other item
	 *  
	 * @param x - list of items in the solution
	 * @param ratio - list of ratioNodes
	 * @param j - item removed
	 */
	private void updateMaxRatio(ArrayList<Integer> r, double curObj, ArrayList<ratioNode> ratio, int j) {
		// For each item left
		for (ratioNode rni: ratio) {
			int i = rni.x;
			// Get objective change
			double newObj = addObj(i,num,den);
			double objChange = newObj - curObj;
			// Recompute ratio and update node
			double lossToWeight = objChange / f.getA(i);
			rni.ratio = lossToWeight;
		}
		// Sort ratios
		Collections.sort(ratio);
	}
}
