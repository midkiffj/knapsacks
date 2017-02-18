package Solutions;

import java.util.ArrayList;
import java.util.Collections;

import Problems.Cubic;

public class CubicSol extends KnapsackSol {

	private static Cubic c;
	private int b;

	public CubicSol() {
		super();
		updateB();
	}

	public CubicSol(String filename) {
		super(filename);
		c = (Cubic)p;
		updateB();
	}

	public CubicSol(CubicSol cs) {
		super((KnapsackSol)cs);
		c = (Cubic)p;
		updateB();
	}

	public CubicSol(boolean[] xVals) {
		super(xVals);
		c = (Cubic)p;
		updateB();
	}

	public CubicSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		c = (Cubic)p;
		updateB();
	}

	public CubicSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA) {
		super(x,r,obj,totalA);
		c = (Cubic)p;
		updateB();
	}

	private void updateB() {
		if (useHealing) {
			b = Integer.MAX_VALUE;
		} else {
			b = c.getB();
		}
	}

	private double[] swapMutate() {
		if (rnd.nextDouble() < 0.8) {
			return ratioMutate();
		} else {
			return bestRatioMutate();
		}
	}

	// Shift a variable in or out of the current solution
	public int shift() {
		if (getRSize() == 0) {
			return trySub();
		}
		if (getXSize() < 2) {
			return tryAdd();
		} else {
			if (rnd.nextDouble() < 0.8) {
				return tryAdd();
			} else {
				return trySub();
			}
		}
	}

	public double[][] tabuMutate(int iteration, int[][] tabuList) {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.4) {
			return maxMinSwap(iteration, tabuList);
		} else {
			double[] ratioSwap = ratioMutate(iteration, tabuList);
			if (ratioSwap == null) {
				return null;
			}
			double[][] result = {ratioSwap, ratioSwap};
			return result;
		}
	}

	public double[] mutate() {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.6) {
			double[][] ret = maxMinSwap(1, new int[n][n]);
			if (ret == null) {
				return null;
			} else {
				return ret[0];
			}
		} else {
			double[] ratioSwap = ratioMutate();
			if (ratioSwap == null) {
				return null;
			}
			return ratioSwap;
		}
	}

	public double[] bestMutate() {
		if (getRSize() == 0) {
			return null;
		}
		if (p.getN() >= 500) {
			double[][] fs = firstSwap(1, new int[n][n]);
			if (fs != null) {
				return fs[0];
			}
			return null;
		}
		double[][] bs = bestSwap(1, new int[n][n]);
		if (bs != null) {
			return bs[0];
		} else {
			return null;
		}
	}

	public double[][] tabuBestMutate(int iteration, int[][] tabuList) {
		if (getRSize() == 0) {
			return null;
		}
		return bestSwap(iteration, tabuList);
	}

	public ProblemSol crossover(ProblemSol ps2) {
		CubicSol cs2 = (CubicSol)ps2;
		boolean[] newXVals = new boolean[n];
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		int newTotalA = 0;
		for (int i = 0; i < n; i++) {
			if (this.getXVals(i) == cs2.getXVals(i)) {
				newXVals[i] = this.getXVals(i);
				if (newXVals[i]) {
					x.add(i);
					newTotalA = p.addA(i,newTotalA);
				} else {
					r.add(i);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < b) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (p.addA(rni.x,newTotalA) <= b) {
				ratio.remove(i);
				newXVals[rni.x] = true;
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA = p.addA(rni.x,newTotalA);
			} else {
				ratio.remove(i);
			}
		}

		return new CubicSol(newXVals);
	}

	public ProblemSol genMutate(int removeAttempts) {
		CubicSol newCS = new CubicSol(this);
		if (rnd.nextDouble() < 0.5) {
			if (newCS.getRSize() == 0) {
				newCS.shift();
			} else {
				double[] swap = newCS.mutate();
				if (swap != null) {
					newCS.swap(swap[0], (int)swap[1], (int)swap[2]);
				}
			}
		} else {
			newCS = genMutate2(newCS, removeAttempts);
			if (removeAttempts < n-1) {
				removeAttempts++;
			}
		}
		return newCS;
	}

	private CubicSol genMutate2(CubicSol cs, int removeAttempts) {
		// Remove s items from the solution
		ArrayList<Integer> x = new ArrayList<Integer>(cs.getX());
		ArrayList<Integer> r = new ArrayList<Integer>(cs.getR());
		int s = removeAttempts;
		if (s >= x.size()) {
			s = x.size()-1;
		}
		for (int i = 0; i < s; i++) {
			int j = rnd.nextInt(x.size());
			r.add(x.remove(j));
		}

		// Compute ratios
		ArrayList<ratioNode> ratio = computeRatios(x, r);
		Collections.sort(ratio);

		// Calc solution capacity
		int newTotalA = 0;
		for (Integer i: x) {
			newTotalA += c.getA(i);
		}
		// Add max-ratio items until knapsack full
		while (ratio.size() > 0 && newTotalA < c.getB()) {
			ratioNode rni = ratio.get(ratio.size()-1);
			if (newTotalA + c.getA(rni.x) <= c.getB()) {
				ratio.remove(ratio.size()-1);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += c.getA(rni.x);
			} else {
				ratio.remove(ratio.size()-1);
			}
		}
		// Calculate obj of new solution
		double obj = c.getObj(x);
		return new CubicSol(x,r,obj,newTotalA);
	}

	private ArrayList<ratioNode> computeRatios(ArrayList<Integer> x, ArrayList<Integer> r) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: r) {
			ratioNode rni = new ratioNode(i, c.getRatio(i));
			ratio.add(rni);
		}
		Collections.sort(ratio);
		return ratio;
	}

	private void updateRatios(ArrayList<Integer> x, ArrayList<ratioNode> ratio, int added) {
		for (ratioNode rni: ratio) {
			int i = rni.x;
			double iRatio = c.getCij(i,added);
			for (int j = 0; j < x.size(); j++) {
				int xj = x.get(j);
				iRatio += c.getDijk(i, xj, added);
			}
			rni.ratio += iRatio;
		}
	}

	private double[] ratioMutate() {
		double[] result = null;
		boolean found = false;
		int min = 0;
		while (!found && min < getXSize()) {
			// Get index of min ratio
			int i = minRatio(min);

			// Swap with a random node and return
			int j = rnd.nextInt(getRSize());
			j = getRItem(j);
			int rndCount = 0;
			while (c.getA(j) - c.getA(i) > b - getTotalA() && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = getRItem(j);
				rndCount++;
			}

			if (c.getA(j) - c.getA(i) <= b - getTotalA()) {
				double newObj = swapObj(i, j);
				result = new double[3];
				result[0] = newObj;
				result[1] = i;
				result[2] = j;
				found = true;
			}

			min++;
		}

		return result;
	}

	private double[] bestRatioMutate() {
		double[] result = null;
		boolean found = false;
		int min = 0;
		while (!found && min < getXSize()) {
			// Get index of min ratio
			int i = minRatio(min);

			// Swap with all nodes and return best
			double maxObj = -1;
			int maxJ = -1;
			for (Integer j: getR()) {
				double newObj = swapObj(i, j);
				if (newObj > maxObj && c.getA(j) - c.getA(i) <= b - getTotalA()) {
					maxObj = newObj;
					maxJ = j;
				}
			}
			if (maxJ != -1) {
				double newObj = swapObj(i, maxJ);
				result = new double[3];
				result[0] = newObj;
				result[1] = i;
				result[2] = maxJ;
				found = true;
			}

			min++;
		}

		return result;
	}

	private double[][] maxMinSwap(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;

		int i = minRatio(0);
		int j = maxRatio(0);
		int ki = 0;
		int kj = 0;
		boolean changeI = true;
		while (c.getA(j) - c.getA(i) > b - getTotalA() && ki < n) {
			if (changeI) {
				ki++;
				i = minRatio(ki);
				changeI = !changeI;
			}
			kj++;
			j = maxRatio(kj);
			if (kj == n-1) {
				kj = -1;
				changeI = !changeI;
			}
		}

		if (c.getA(j) - c.getA(i) > b - getTotalA()) {
			return null;
		}

		double newObj = swapObj(i, j);
		bi = i;
		bj = j;
		bObj = newObj;
		if (tabuList[i][j] < iteration) {
			ni = i;
			nj = j;
			nTObj = newObj;
		} else {
			boolean newMin = false;
			while (tabuList[i][j] >= iteration && c.getA(j) - c.getA(i) > b - getTotalA() && ki < n) {
				if (newMin) {
					ki++;
					i = minRatio(ki);
					newMin = !newMin;
				}
				kj++;
				j = maxRatio(kj);
				if (kj == n) {
					kj = -1;
					newMin = !newMin;
				}
			}
			if (tabuList[i][j] < iteration) {
				newObj = swapObj(i, j);
				ni = i;
				nj = j;
				nTObj = newObj;
				if (newObj > bObj) {
					bi = i;
					bj = j;
					bObj = newObj;
				}
			}
		}
		// Compile and return data
		double[][] results = new double[2][3];
		results[0][0] = bObj;
		results[0][1] = bi;
		results[0][2] = bj;
		results[1][0] = nTObj;
		results[1][1] = ni;
		results[1][2] = nj;
		return results;
	}

	private double[] ratioMutate(int iteration, int[][] tabuList) {
		// Get index of min ratio
		int i = minRatio(0);

		// Swap with a random node and return
		int j = rnd.nextInt(getRSize());
		j = getRItem(j);
		int ki = 0;
		int kj = 0;
		boolean changeI = false;
		while (tabuList[i][j] >= iteration && c.getA(j) - c.getA(i) > b - getTotalA() && ki < n) {
			if (changeI) {
				ki++;
				i = minRatio(ki);
				changeI = !changeI;
			}

			kj++;
			j =  rnd.nextInt(getRSize());
			j = getRItem(j);
			if (kj == n-1) {
				kj = -1;
				changeI = !changeI;
			}
		}

		if (c.getA(j) - c.getA(i) > b - getTotalA() || tabuList[i][j] >= iteration) {
			return null;
		}
		double newObj = swapObj(i, j);
		double[] result = {newObj, i, j};

		return result;
	}

	private int minRatio(int k) {
		// Find the minimum ratio in the solution
		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < getXSize()) {
			for (Integer i: getX()) {
				if (c.getRatio(i) < minRatio && !bestIs.contains(i)) {
					minRatio = c.getRatio(i);
					minI = i;
				}
			}
			minRatio = Double.MAX_VALUE;
			bestIs.add(minI);
		}
		return minI;
	}

	private int maxRatio(int k) {
		// Find the maximum ratio not in the solution
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < getRSize()) {
			for (Integer i: getR()) {
				if (c.getRatio(i) > maxRatio && !bestIs.contains(i)) {
					maxRatio = c.getRatio(i);
					maxI = i;
				}
			}
			maxRatio = -1*Double.MAX_VALUE;
			bestIs.add(maxI);
		}
		return maxI;
	}

	// Find the best swap possible that keeps the knapsack feasible
	private double[][] bestSwap(int iteration, int[][] tabuList) {
		int curTotalA = getTotalA();
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;
		for(Integer i: getX()) {
			for(Integer j: getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= b - curTotalA) {
					double newObj = swapObj(i, j);
					if (newObj > nTObj && tabuList[i][j] < iteration) {
						ni = i;
						nj = j;
						nTObj = newObj;
					}
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		if (ni == -1 && bi == -1) {
			if (rnd.nextDouble() < 0.1) {
				trySub();
			}
			return null;
		}
		// Compile and return data
		double[][] results = new double[2][3];
		results[0][0] = bObj;
		results[0][1] = bi;
		results[0][2] = bj;
		results[1][0] = nTObj;
		results[1][1] = ni;
		results[1][2] = nj;
		return results;
	}

	// Return the first improving swap that keeps the knapsack feasible
	private double[][] firstSwap(int iteration, int[][] tabuList) {
		int curTotalA = getTotalA();
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MIN_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;
		for(Integer i: getX()) {
			for(Integer j: getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= b - curTotalA) {
					double newObj = swapObj(i, j);
					if (newObj > nTObj && tabuList[i][j] < iteration) {
						ni = i;
						nj = j;
						nTObj = newObj;
					}
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		// Compile and return data
		double[][] results = new double[2][3];
		results[0][0] = bObj;
		results[0][1] = bi;
		results[0][2] = bj;
		results[1][0] = nTObj;
		results[1][1] = ni;
		results[1][2] = nj;
		return results;
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

	@Override
	public int compareTo(ProblemSol o) 	{
		if (o.getValid() && this.getValid() || !(o.getValid() || this.getValid())) {
			double diff = this.getObj() - o.getObj();
			if (diff > 0) {
				return 1;
			} else if (diff < 0) {
				return -1;
			} else {
				return 0;
			}
		} else {
			if (o.getValid()) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	@Override
	public boolean betterThan(double newObj) {
		if (newObj > getObj()) {
			return true;
		}
		return false;
	}

	
	@Override
	public void healSol() {
//		healSolImproving();
		healSolRatio();
	}

	// most improving
	public void healSolImproving() {
		int totalA = this.getTotalA();
		double obj = this.getObj();
		while(!this.getValid()) {
			double maxObj = -1*Double.MAX_VALUE;
			int maxI = -1;
			for (Integer i: this.getX()) {
				double newObj = c.subObj(i, this.getX(), obj);
				if (newObj > maxObj) {
					maxObj = newObj;
					maxI = i;
				}
			}
			if (maxI != -1) {
				getX().remove(Integer.valueOf(maxI));
				getR().add(Integer.valueOf(maxI));
				setXVals(maxI,false);
				obj = maxObj;
				totalA = p.removeA(maxI, totalA);
				this.setTotalA(totalA);
				this.setObj(obj);
			} else {
				System.err.println("Couldn't find an improving objective!!!");
				System.exit(-1);
			}
		}
	}
	
	//min ratio healing
	public void healSolRatio() {
		int totalA = this.getTotalA();
		double obj = this.getObj();
		while(!this.getValid()) {
			int j = minRatio(0);
			getX().remove(Integer.valueOf(j));
			getR().add(Integer.valueOf(j));
			setXVals(j,false);
			obj = p.subObj(j, getX(), obj);
			totalA = p.removeA(j, totalA);
			this.setTotalA(totalA);
			this.setObj(obj);
		}
	}
}
