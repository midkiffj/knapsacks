package archive;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


public class CubicConst {

	private int n;
	private Random rnd;
	private Cubic c;
	private KnapsackSol current;
	private KnapsackSol greedy;
	private KnapsackSol fill;
	private KnapsackSol hybrid;
	private KnapsackSol hybrid2;
	private long greedyTime;
	private long fillTime;
	private long hybridTime;
	private long hybrid2Time;
	
	private int mi;
	private int mj;

	public CubicConst(Cubic c) {
		this.c = c;
		KnapsackSol.c = c;
		n = c.getN();
		rnd = new Random(1234);
		
		mi = -1;
		mj = -1;

		long start = System.nanoTime();
		long end = System.nanoTime();
		long duration = (end-start)/1000000;

//		start = System.nanoTime();
//		greedyHeuristic();
//		end = System.nanoTime();
//		duration = (end-start)/1000000;
//		greedyTime = duration;
//		System.out.println("greedyHeuristic: " + duration);
//		System.out.println("Objective: " + getGreedyObj());
		
		start = System.nanoTime();
		greedyHeuristic2();
		end = System.nanoTime();
		duration = (end-start)/1000000;
		greedyTime = duration;
		System.err.println("greedyHeuristic2: " + duration);
//		System.out.println("Objective: " + getGreedyObj());

		start = System.nanoTime();
		fillUpNExchange();
		end = System.nanoTime();
		duration = (end-start)/1000000;
		fillTime = duration;
		System.err.println("fillUpNExchange: " + duration);
//		System.out.println("Objective: " + getFillObj());

		start = System.nanoTime();
		hybrid();
		end = System.nanoTime();
		duration = (end-start)/1000000;
		hybridTime = duration;
		System.err.println("hybrid: " + duration);
//		System.out.println("Objective: " + getHybridObj());
		
		start = System.nanoTime();
		hybrid2();
		end = System.nanoTime();
		duration = (end-start)/1000000;
		hybrid2Time = duration;
		System.err.println("hybrid2: " + duration);
//		System.out.println("Objective: " + getHybrid2Obj());

	}

	public static void main(String[] args) {
		if (args.length == 1) {
			CubicConst cc = new CubicConst(new Cubic(args[0]));
		} else {
			Cubic c = new Cubic("100_0.5_true_2");
			CubicConst cc = new CubicConst(c);
		}
	}

	public long getGreedyObj() {
		return greedy.getObj();
	}
	
	public long getFillObj() {
		return fill.getObj();
	}
	
	public long getHybridObj() {
		return hybrid.getObj();
	}
	
	public long getHybrid2Obj() {
		return hybrid2.getObj();
	}
	
	public long getGreedyTime() {
		return greedyTime;
	}
	
	public long getFillTime() {
		return fillTime;
	}
	
	public long getHybridTime() {
		return hybridTime;
	}
	
	public long getHybrid2Time() {
		return hybrid2Time;
	}

	private void greedyHeuristic() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		int totalA = 0;
		for (int i = 0; i < n; i++) {
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

		greedy = new KnapsackSol(x,r);
	}
	
	private void greedyHeuristic2() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		int totalA = 0;
		for (int i = 0; i < n; i++) {
			x.add(i);
			totalA += c.getA(i);
		}

		int b = c.getB();
		ArrayList<ratioNode> ratio = computeRatio(x);
		while (totalA > b) {
			int i = ratio.remove(0).x;
			x.remove(Integer.valueOf(i));
			r.add(i);
			totalA -= c.getA(i);
			updateRatio(x,ratio,i);
		}

		greedy = new KnapsackSol(x,r);
	}

	private void fillUpNExchange() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < n; i++) {
			r.add(i);
		}
		int totalA = 0;

		fillUpNExchange(x,r,totalA);
		fill = new KnapsackSol(current);
	}

	private void fillUpNExchange(ArrayList<Integer> x, ArrayList<Integer> r, int totalA) {
		current = new KnapsackSol(x,r);

		boolean done = false;
		long curObj = current.getObj();
		while (!done) {
			boolean swap = false;
			if (rnd.nextDouble() < 0.5) {
				bestImprovingSwap();
				swap = true;
			} else {
				current.tryImproveAdd();
			}
			if (curObj == current.getObj()) {
				if (swap) {
					current.tryImproveAdd();
				} else {
					bestImprovingSwap();
				}
			}
			if (curObj == current.getObj()) {
				done = true;
			} else {
				curObj = current.getObj();
			}
		}
	}

	private void hybrid() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		int totalA = 0;
		for (int i = 0; i < n; i++) {
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

		fillUpNExchange(x,r,totalA);
		hybrid = new KnapsackSol(current);
	}

	private void hybrid2() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();

		int totalA = 0;
		for (int i = 0; i < n; i++) {
			x.add(i);
			totalA += c.getA(i);
		}

		int b = c.getB();
		boolean done = false;
		while (totalA > b && !done) {
			ratioNode rni = computeMinRatio(x);
			int i = rni.x;
			if (totalA > b || (totalA <= b && rni.ratio < 0)) {
				x.remove(Integer.valueOf(i));
				r.add(i);
				totalA -= c.getA(i);
			} else {
				done = true;
			}
		}

		fillUpNExchange(x,r,totalA);
		hybrid2 = new KnapsackSol(current);
	}

	// Perform the best improving swap that keeps the knapsack feasible
	private void bestImprovingSwap() {
		// Store b
		int b = c.getB();
		int curTotalA = current.getTotalA();
		// Store best swaps
		int bi = -1;
		int bj = -1;
		long bObj = current.getObj();
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= b - curTotalA) {
					long newObj = current.swapObj(i, j);
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

	private int computeMinRatioI(ArrayList<Integer> x) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: x) {
			long objChange = c.getCi(i);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				objChange += 2*c.getCij(i,xj);
				for (int k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					objChange += 6*c.getDijk(i,xj,xk);
				}
			}
			double lossToWeight = (double)objChange / c.getA(i);
			ratioNode rni = new ratioNode(i, lossToWeight);
			ratio.add(rni);
		}
		Collections.sort(ratio);
		return ratio.get(0).x;
	}
	
	private ratioNode computeMinRatio(ArrayList<Integer> x) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: x) {
			long objChange = c.getCi(i);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				objChange += 2*c.getCij(i,xj);
				for (int k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					objChange += 6*c.getDijk(i,xj,xk);
				}
			}
			double lossToWeight = (double)objChange / c.getA(i);
			ratioNode rni = new ratioNode(i, lossToWeight);
			ratio.add(rni);
		}
		Collections.sort(ratio);
		return ratio.get(0);
	}
	
	private ArrayList<ratioNode> computeRatio(ArrayList<Integer> x) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: x) {
			long objChange = c.getCi(i);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				objChange += 2*c.getCij(i,xj);
				for (int k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					objChange += 6*c.getDijk(i,xj,xk);
				}
			}
			double lossToWeight = (double)objChange / c.getA(i);
			ratioNode rni = new ratioNode(i, lossToWeight);
			rni.objChange = objChange;
			ratio.add(rni);
		}
		Collections.sort(ratio);
		return ratio;
	}

	private void updateRatio(ArrayList<Integer> x, ArrayList<ratioNode> ratio, int j) {
		for (ratioNode rni: ratio) {
			int i = rni.x;
			long objChange = rni.objChange;
			objChange -= 2*c.getCij(i,j);
			for (int k = 0; k < x.size(); k++) {
				int xk = x.get(k);
				objChange -= 6*c.getDijk(i,j,xk);
			}
			double lossToWeight = (double)objChange / c.getA(i);
			rni.ratio = lossToWeight;
			rni.objChange = objChange;
		}
		Collections.sort(ratio);
	}
	
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
