package Constructive;

import java.util.ArrayList;
import java.util.Collections;

import Problems.Cubic;
import Solutions.CubicSol;
import Solutions.ProblemSol;

public class CubicGreedy extends ConstHeuristic {

	private Cubic c;
	
	public CubicGreedy(Cubic c) {
		super();
		this.c = c;
	}

	protected ProblemSol construct() {
		return greedyHeuristic2();
	}
	
	// Computes the min ratio each iteration (slow)
	private CubicSol greedyHeuristic() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		int totalA = 0;
		for (int i = 0; i < c.getN(); i++) {
			x.add(i);
			totalA += c.getA(i);
		}

		int b = c.getB();
		while (totalA > b) {
			int i = computeMinRatioI(x);
			x.remove(Integer.valueOf(i));
			r.add(i);
			totalA -= c.getA(i);
		}

		return new CubicSol(x,r);
	}
	
	// Updates the ratios at each iteration (faster)
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
	
	private int computeMinRatioI(ArrayList<Integer> x) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: x) {
			long objChange = c.getCi(i);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				objChange += c.getCij(i,xj);
				for (int k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					objChange += c.getDijk(i,xj,xk);
				}
			}
			double lossToWeight = (double)objChange / c.getA(i);
			ratioNode rni = new ratioNode(i, lossToWeight);
			ratio.add(rni);
		}
		Collections.sort(ratio);
		return ratio.get(0).x;
	}
	
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
	
	// Update the ratios by removing the item from the loss calculation
	private void updateRatio(ArrayList<Integer> x, ArrayList<ratioNode> ratio, int j) {
		for (ratioNode rni: ratio) {
			int i = rni.x;
			long objChange = rni.objChange;
			objChange -= c.getCij(i,j);
			for (int k = 0; k < x.size(); k++) {
				int xk = x.get(k);
				objChange -= c.getDijk(i,j,xk);
			}
			double lossToWeight = (double)objChange / c.getA(i);
			rni.ratio = lossToWeight;
			rni.objChange = objChange;
		}
		Collections.sort(ratio);
	}
	
	// Class used to link items to ratios
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
