package Constructive;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import Problems.Cubic;
import Solutions.CubicSol;
import Solutions.ProblemSol;
import Solutions.ratioNode;

/**
 * Greedy Heuristic to the Cubic Knapsack
 * - Fills the knapsack with all items
 * - Removes the item with the minimum loss-to-weight ratio
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
		return greedyHeuristic();
	}

	/**
	 * Creates a solution by:
	 * - Adding all items to the knapsack
	 * - Removes an item with the minimum 'ratio' until Ax <= b
	 * 
	 * @return solution constructed
	 */
	private CubicSol greedyHeuristic() {
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

	public static void main(String[] args) {
		PrintWriter pw;
		try {
			pw = new PrintWriter("results/cubic/"+"cubGreedy.csv");
			pw = new PrintWriter(pw,true);
			pw.println("n,density,#,negCoef,Greedy,GreedyMax,,Times(min):,Greedy,GreedyMax");
			// Test Bed specification
			double[] densities = {0.25, 0.5, 0.75, 1};
			int[] probSizes = {10, 20, 30, 50, 100, 200, 500, 1000};
			int K = 10;
			for (int i = 0; i < densities.length; i++) {
				double density = densities[i];
				for (int j = 0; j < probSizes.length; j++) {
					int n = probSizes[j];
					for (int k = 0; k < K; k++) {
						String file1 = n+"_"+density+"_false_"+k;
						System.out.println("--"+file1+"--");
						Cubic c1 = new Cubic("problems/cubic/"+file1);
						CubicGreedy cg = new CubicGreedy(c1);
						long start = System.nanoTime();
						ProblemSol result = cg.greedyHeuristic();
						long end = System.nanoTime();
						double gTime = (double)(end-start)/60000000000L;
						double gObj = result.getObj();

						start = System.nanoTime();
						result = cg.greedyHeuristicMax();
						end = System.nanoTime();
						double gmTime = (double)(end-start)/60000000000L;
						double gmObj = result.getObj();
						
						String result1 = gObj + "," + gmObj + ",,,"+gTime + ","+gmTime;
						
						if (k == 0) {
							pw.println(n+","+density+","+k+",false,"+result1);
						} else {
							pw.println(",,"+k+",false,"+result1);
						}
					}
					for (int k = 0; k < K; k++) {
						String file1 = n+"_"+density+"_true_"+k;
						System.out.println("--"+file1+"--");
						Cubic c1 = new Cubic("problems/cubic/"+file1);
						CubicGreedy cg = new CubicGreedy(c1);
						long start = System.nanoTime();
						ProblemSol result = cg.greedyHeuristic();
						long end = System.nanoTime();
						double gTime = (double)(end-start)/60000000000L;
						double gObj = result.getObj();

						start = System.nanoTime();
						result = cg.greedyHeuristicMax();
						end = System.nanoTime();
						double gmTime = (double)(end-start)/60000000000L;
						double gmObj = result.getObj();
						
						String result1 = gObj + "," + gmObj + ",,,"+gTime + ","+gmTime;
						
						if (k == 0) {
							pw.println(n+","+density+","+k+",true,"+result1);
						} else {
							pw.println(",,"+k+",true,"+result1);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
