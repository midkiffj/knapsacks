package Constructive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import Problems.Cubic;
import Solutions.CubicSol;
import Solutions.KnapsackSol;
import Solutions.ProblemSol;

public class CubicDP extends ConstHeuristic {

	private int n;
	private Random rnd;
	private Cubic c;

	public CubicDP(Cubic c) {
		this.c = c;
		n = c.getN();
		rnd = new Random(1234);

		long start;
		long end;
		long duration;

		//		start = System.nanoTime();
		//		dpHeuristic4();
		//		end = System.nanoTime();
		//		duration = (end-start)/1000000;
		//		System.out.println("dpHeuristic4: " + duration);
		//
		//		start = System.nanoTime();
		//		dpHeuristic3();
		//		end = System.nanoTime();
		//		duration = (end-start)/1000000;
		//		System.out.println("dpHeuristic3: " + duration);

		start = System.nanoTime();
		dpHeuristic2();
		end = System.nanoTime();
		duration = (end-start)/1000000;
		//		System.out.println("dpHeuristic2: " + duration);

		//		start = System.nanoTime();
		//		dpHeuristic();
		//		end = System.nanoTime();
		//		duration = (end-start)/1000000;
		//		System.out.println("dpHeuristic: " + duration);
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			CubicDP cdp = new CubicDP(new Cubic(args[0]));
		} else {
			Cubic c = new Cubic(90,false);
			CubicDP cdp = new CubicDP(c);
		}
	}
	

	protected ProblemSol construct() {
		return dpHeuristic2();
	}

	// Array Version
	private void dpHeuristic() {
		// Initialize Arrays
		int b = c.getB();
		long[] f = new long[b+1];
		boolean[][] B = new boolean[b+1][n];

		// Iterate over all items
		for (int k = 0; k < n; k++) {
			int ak = c.getA(k);
			// Check all weights where the item could fit
			for (int r = b; r >= ak; r--) {
				int weight = r-ak;
				// Check profit and update f and B
				long Beta = f[weight] + profit(k,weight,B);
				if (Beta > f[r]) {
					f[r] = Beta;
					for (int i = 0; i < k; i++) {
						B[r][i] = B[weight][i];
					}
					B[r][k] = true;
				}
			}
		}

		int rmax = 0;
		for (int r = 1; r < f.length; r++) {
			if (f[r] >= f[rmax]) {
				rmax = r;
			}
		}

		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		int totalA = 0;
		for (int i = 0; i < n; i++) {
			if (B[rmax][i]) {
				x.add(i);
				totalA = totalA + c.getA(i);
			} else {
				r.add(i);
			}
		}

		System.out.println("Generated Tour: " + f[rmax]);
		System.out.println(x.toString());
	}

	private long profit(int k, int weight, boolean[][] B) {
		long oldObj = c.getCi(k);
		for (int i = 0; i < k; i++) {
			if (B[weight][i]) {
				oldObj = oldObj + c.getCij(i,k);
				for (int l = i+1; l < k; l++) {
					if (B[weight][l]) {
						oldObj = oldObj + c.getDijk(i,l,k);
					}
				}
			}
		}
		return oldObj;
	}

	// List Version
	private CubicSol dpHeuristic2() {
		// Initialize Arrays/Lists
		int b = c.getB();
		long[] f = new long[b+1];
		ArrayList<Integer>[] B = new ArrayList[b+1];
		for (int i = 0; i < B.length; i++) {
			B[i] = new ArrayList<Integer>();
		}

		// Iterate over all items
		for (int k = 0; k < n; k++) {
			int ak = c.getA(k);
			// Check all weights where the item could fit
			for (int r = b; r >= ak; r--) {
				int weight = r-ak;
				// Check profit and update f and B
				long Beta = f[weight] + profit2(k,B[weight]);
				if (Beta > f[r]) {
					f[r] = Beta;
					B[r] = new ArrayList<Integer>(B[weight]);
					B[r].add(k);
				}
			}
		}

		// Find the maximum objective value
		int rmax = 0;
		for (int r = 1; r < f.length; r++) {
			if (f[r] >= f[rmax]) {
				rmax = r;
			}
		}

		// Return the solution that made the max obj value
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < n; i++) {
			r.add(i);
		}
		x.addAll(B[rmax]);
		r.removeAll(B[rmax]);

		return localSearch(new CubicSol(x,r));
		//		System.out.println("Generated Tour: " + f[rmax]);
		//		System.out.println(x.toString());
	}

	// Calculate the change in objective value by 
	// 	adding item k with the items in B.
	private long profit2(int k, ArrayList<Integer> B) {
		long oldObj = c.getCi(k);
		for (int i = 0; i < B.size(); i++) {
			int xi = B.get(i);
			oldObj = oldObj + c.getCij(xi,k);
			for (int l = i+1; l < B.size(); l++) {
				int xl = B.get(l);
				oldObj = oldObj + c.getDijk(xi,xl,k);
			}
		}
		return oldObj;
	}
	
	// Complete best improving swaps (and a shift) 
	// 	until the objective value is no longer improved
	private CubicSol localSearch(CubicSol current) {
		double curObj = current.getObj();
		bestImprovingSwap(current);
		boolean done = false;
		while (!done) {
			while (current.getObj() > curObj) {
				curObj = current.getObj();
				bestImprovingSwap(current);
			}
			curObj = current.getObj();
			improvingShift(current);
			if (current.getObj() <= curObj) {
				done = true;	
			}
		}
		return current;
	}

	// Perform the best improving swap that keeps the knapsack feasible
	private void bestImprovingSwap(CubicSol current) {
		// Occasionally perform a shift\
		if (rnd.nextDouble() < 0.6) {
			int change = improvingShift(current);
			if (change != -1) {
				return;
			}
		}
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
		if (bi != -1) {
			current.swap(bObj,bi,bj);
		}
	}

	// Shift a variable in or out of the current solution
	private int improvingShift(CubicSol current) {
		if (current.getXSize() < 2) {
			return current.tryImproveAdd();
		} else {
			if (rnd.nextDouble() < 0.8) {
				return current.tryImproveAdd();
			} else {
				return current.tryImproveSub();
			}
		}
	}

	// Tried to be smart...didn't work
	private void dpHeuristic3() {
		// Initialize Arrays
		int b = c.getB();
		long[] f = new long[b+1];
		ArrayList<Integer>[] B = new ArrayList[b+1];
		for (int i = 0; i < B.length; i++) {
			B[i] = new ArrayList<Integer>();
		}

		// Iterate over all items
		for (int k = 0; k < n; k++) {
			int ak = c.getA(k);
			long lastBeta = -1;
			// Check all weights where the item could fit
			for (int r = b; r >= ak; r--) {
				int weight = r-ak;
				if (lastBeta != -1) {
					if (B[weight+1].size() == B[weight].size() && B[weight].containsAll(B[weight+1])) {
						f[r] = lastBeta;
						B[r] = new ArrayList<Integer>(B[r+1]);
					} else {
						lastBeta = -1;
					}
				}
				if (lastBeta == -1) {
					// Check profit and update f and B
					long Beta = f[weight] + profit2(k,B[weight]);
					if (Beta > f[r]) {
						f[r] = Beta;
						B[r] = new ArrayList<Integer>(B[weight]);
						B[r].add(k);
						lastBeta = Beta;
					}
				}
			}
		}

		int rmax = 0;
		for (int r = 1; r < f.length; r++) {
			if (f[r] >= f[rmax]) {
				rmax = r;
			}
		}

		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		int totalA = 0;
		for (int i = 0; i < n; i++) {
			r.add(i);
		}
		x.addAll(B[rmax]);
		r.removeAll(B[rmax]);

		//		System.out.println("Generated Tour: " + f[rmax]);
		//		System.out.println(x.toString());
	}

	// Attempted ordering by ratio
	private void dpHeuristic4() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r1 = new ArrayList<Integer>();
		int totalA = 0;
		for (int i = 0; i < n; i++) {
			r1.add(i);
		}
		// Initialize Arrays
		int b = c.getB();
		long[] f = new long[b+1];
		ArrayList<Integer>[] B = new ArrayList[b+1];
		for (int i = 0; i < B.length; i++) {
			B[i] = new ArrayList<Integer>();
		}
		ArrayList<ratioNode> ratio = computeRatios(r1);
		Collections.sort(ratio);


		// Iterate over all items
		for (int rnk = n-1; rnk >-1; rnk--) {
			int k = ratio.get(rnk).x;
			int ak = c.getA(k);
			// Check all weights where the item could fit
			for (int r = b; r >= ak; r--) {
				int weight = r-ak;
				// Check profit and update f and B
				long Beta = f[weight] + profit2(k,B[weight]);
				if (Beta > f[r]) {
					f[r] = Beta;
					B[r] = new ArrayList<Integer>(B[weight]);
					B[r].add(k);
				}
			}
		}

		int rmax = 0;
		for (int r = 1; r < f.length; r++) {
			if (f[r] >= f[rmax]) {
				rmax = r;
			}
		}


		x.addAll(B[rmax]);
		r1.removeAll(B[rmax]);

		System.out.println("Generated Tour: " + f[rmax]);
		System.out.println(x.toString());
	}

	private ArrayList<ratioNode> computeRatios(ArrayList<Integer> r) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: r) {
			ratioNode rni = new ratioNode(i, c.getRatio(i));
			ratio.add(rni);
		}
		return ratio;
	}

	private class ratioNode implements Comparable<ratioNode>{
		int x;
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
