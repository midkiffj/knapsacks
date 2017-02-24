package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import ExactMethods.Knapsack;
import Problems.MaxProbability;

public class MaxProbabilitySol extends ProblemSol {

	private static MaxProbability mp;

	private ArrayList<Integer> x;
	private ArrayList<Integer> r;
	private boolean[] xVals;
	private boolean valid;
	private double obj;
	private double num;
	private double den;
	private int totalA;
	private int totalU;

	private int b;

	public MaxProbabilitySol() {
		super();
		mp = (MaxProbability)p;
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		xVals = new boolean[p.getN()];
		p.genInit(x, r);
		for (Integer i: x) {
			xVals[i] = true;
		}
		obj = mp.getObj(x);
		num = mp.getNum();
		den = mp.getDen();
		calcTotalAU();
		updateValid();
		updateB();
	}

	public MaxProbabilitySol(String filename) {
		super();
		mp = (MaxProbability)p;
		readSolution(filename);
		xVals = new boolean[p.getN()];
		for (Integer i : x) {
			xVals[i] = true;
		}
		updateValid();
		updateB();
	}

	public MaxProbabilitySol(MaxProbabilitySol mps) {
		super();
		mp = (MaxProbability)p;
		xVals = new boolean[p.getN()];
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (Integer i : mps.getX()) {
			x.add(i);
			xVals[i] = true;
		}
		for (Integer i : mps.getR()) {
			r.add(i);
		}
		obj = mps.getObj();
		totalA = mps.getTotalA();
		totalU = mps.getTotalU();
		num = mps.getNum();
		den = mps.getDen();
		updateValid();
		updateB();
	}

	public MaxProbabilitySol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA, double num, double den) {
		super();
		mp = (MaxProbability)p;
		xVals = new boolean[p.getN()];
		this.x = new ArrayList<Integer>(x);
		this.r = new ArrayList<Integer>(r);
		for (Integer i : x) {
			xVals[i] = true;
		}
		this.obj = obj;
		calcTotalAU();
		this.num = num;
		this.den = den;
		updateValid();
		updateB();
	}

	public MaxProbabilitySol(boolean[] newXVals) {
		super();
		mp = (MaxProbability)p;
		this.xVals = newXVals;
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (int i = 0; i < xVals.length; i++) {
			if (xVals[i]) {
				x.add(i);
			} else {
				r.add(i);
			}
		}
		obj = mp.getObj(x);
		calcTotalAU();
		this.num = mp.getNum();
		this.den = mp.getDen();
		updateValid();
		updateB();
	}

	public MaxProbabilitySol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super();
		mp = (MaxProbability)p;
		xVals = new boolean[p.getN()];
		this.x = new ArrayList<Integer>(x);
		this.r = new ArrayList<Integer>(r);
		for (Integer i : x) {
			xVals[i] = true;
		}
		this.obj = mp.getObj(x);
		calcTotalAU();
		this.num = mp.getNum();
		this.den = mp.getDen();
		updateValid();
		updateB();
	}

	private void updateB() {
		if (useHealing) {
			b = Integer.MAX_VALUE;
		} else {
			b = mp.getB();
		}
	}

	private void calcTotalAU() {
		totalA = mp.calcTotalA(x);
		totalU = mp.calcTotalU(x);
	}

	private void updateValid() {
		calcTotalAU();
		if (totalA <= mp.getB() && totalU >= mp.getT()) {
			valid = true;
		} else {
			valid = false;
		}
	}

	public int getTotalA() {
		return totalA;
	}

	public int getTotalU() {
		return totalU;
	}

	@Override
	public double getObj() {
		return obj;
	}

	public double getNum() {
		return num;
	}

	public double getDen() {
		return den;
	}

	@Override
	public void swap(double newObj, int i, int j) {
		this.totalA = mp.removeA(i,mp.addA(j,totalA));
		this.totalU = mp.removeU(i,mp.addU(j,totalU));
		this.obj = newObj;
		this.num = mp.swapNum(i,j,num);
		this.den = mp.swapDen(i,j,den);
		updateValid();
		xVals[i] = false;
		xVals[j] = true;
		x.remove(Integer.valueOf(i));
		x.add(j);
		r.remove(Integer.valueOf(j));
		r.add(i);	
	}

	@Override
	public ArrayList<Integer> getX() {
		return x;
	}

	@Override
	public ArrayList<Integer> getR() {
		return r;
	}

	@Override
	public int getRItem(int i) {
		return r.get(i);
	}

	@Override
	public int getXItem(int i) {
		return x.get(i);
	}

	@Override
	public int getRSize() {
		return r.size();
	}

	@Override
	public int getXSize() {
		return x.size();
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

	// Try to add a variable to the solution
	private int tryAdd() {
		int index = mp.tryAdd(totalA, x, r, false, num, den);
		if (index != -1) {
			xVals[index] = true;
			x.add(index);
			r.remove(Integer.valueOf(index));
			totalA = mp.addA(index, totalA);
			totalU = mp.addU(index, totalU);
			obj = mp.addObj(index, x, num, den);
			num = mp.addNum(index, num);
			den = mp.addDen(index, den);
			updateValid();
		}
		return index;
	}

	private int trySub() {
		int index = mp.trySub(totalU, x, false, num, den);
		if (index != -1) {
			xVals[index] = false;
			r.add(index);
			x.remove(Integer.valueOf(index));
			totalA = mp.removeA(index, totalA);
			totalU = mp.removeU(index, totalU);
			obj = mp.subObj(index, x, num, den);
			num = mp.subNum(index, num);
			den = mp.subDen(index, den);
			updateValid();
		}
		return index;
	}

	private double swapObj(int i, int j) {
		double num = mp.swapNum(i, j, this.num);
		double den = mp.swapDen(i, j, this.den);
		return (num*num)/den;
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
		MaxProbabilitySol mps2 = (MaxProbabilitySol)ps2;
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		int newTotalA = 0;
		int newTotalU = 0;
		for (int i = 0; i < n; i++) {
			if (this.getXVals(i) == mps2.getXVals(i)) {
				if (this.getXVals(i)) {
					x.add(i);
					newTotalA = mp.addA(i,newTotalA);
					newTotalU = mp.addU(i,newTotalU);
				} else {
					r.add(i);
				}
			} else {
				r.add(i);
			}
		}

		int k = 0;
		ArrayList<Integer> tempR = new ArrayList<Integer>(r);
		ArrayList<Integer> tempX = new ArrayList<Integer>(x);
		//		while (newTotalU < mp.getT() && k < r.size()) {
		//			int maxU = mp.maxURatio(r,k);
		//			if (p.addA(maxU,newTotalA) <= mp.getB()) {
		//				newXVals[maxU] = true;
		//				x.add(maxU);
		//				newTotalA = mp.addA(maxU,newTotalA);
		//				newTotalU = mp.addU(maxU,newTotalU);
		//				r.remove(Integer.valueOf(maxU));
		//			} else {
		//				k++;
		//			}
		//		}


		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < mp.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (p.addA(rni.x,newTotalA) <= mp.getB()) {
				ratio.remove(i);
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA = mp.addA(rni.x,newTotalA);
				newTotalU = mp.addU(rni.x,newTotalU);
			} else {
				ratio.remove(i);
			}
		}

		if (newTotalU < mp.getT()) {
			x = tempX;
			r = tempR;
			newTotalA = 0;
			newTotalU = 0;
			for (Integer i: x) {
				newTotalA += mp.getA(i);
				newTotalU += mp.getU(i);
			}

			int rB = mp.getB() - newTotalA;
			int rTarget = (int) (mp.getT() - newTotalU);
			ArrayList<Integer> toAdd = bestFill(r,rB,rTarget);
			if (toAdd == null) {
				return new MaxProbabilitySol(mps2.getX(),mps2.getR());
			}
			for (Integer i: toAdd) {
				x.add(i);
				r.remove(Integer.valueOf(i));
				newTotalA += mp.getA(i);
				newTotalU += mp.getU(i);
			}
		}

		if (newTotalU < mp.getT()) {
			System.err.println("Infeasible generated");
		}

		return new MaxProbabilitySol(x,r);
	}

	public ProblemSol genMutate(int removeAttempts) {
		MaxProbabilitySol newMP = new MaxProbabilitySol(this);
		if (rnd.nextDouble() < 0.5) {
			if (newMP.getRSize() == 0) {
				newMP.shift();
			} else {
				double[] swap = newMP.mutate();
				if (swap != null) {
					newMP.swap(swap[0], (int)swap[1], (int)swap[2]);
				}
			}
		} else {
			newMP = genMutate2(newMP, removeAttempts);
			if (removeAttempts < n-1) {
				removeAttempts++;
			}
		}
		return newMP;
	}

	private MaxProbabilitySol genMutate2(MaxProbabilitySol mps, int removeAttempts) {
		// Remove s items from the solution
		ArrayList<Integer> x = new ArrayList<Integer>(mps.getX());
		ArrayList<Integer> r = new ArrayList<Integer>(mps.getR());

		int s = removeAttempts;
		if (s >= x.size()) {
			s = x.size()-1;
		}
		for (int i = 0; i < s; i++) {
			int j = rnd.nextInt(x.size());
			r.add(x.remove(j));
		}

		// Calc solution capacity
		int newTotalA = 0;
		int newTotalU = 0;
		for (Integer i: x) {
			newTotalA += mp.getA(i);
			newTotalU += mp.getU(i);
		}

		int k = 0;
		ArrayList<Integer> tempR = new ArrayList<Integer>(r);
		ArrayList<Integer> tempX = new ArrayList<Integer>(x);
		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < mp.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (p.addA(rni.x,newTotalA) <= mp.getB()) {
				ratio.remove(i);
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA = mp.addA(rni.x,newTotalA);
				newTotalU = mp.addU(rni.x,newTotalU);
			} else {
				ratio.remove(i);
			}
		}

		if (newTotalU < mp.getT()) {
			x = tempX;
			r = tempR;
			newTotalA = 0;
			newTotalU = 0;
			for (Integer i: x) {
				newTotalA += mp.getA(i);
				newTotalU += mp.getU(i);
			}

			int rB = mp.getB() - newTotalA;
			int rTarget = (int) (mp.getT() - newTotalU);
			ArrayList<Integer> toAdd = bestFill(r,rB,rTarget);
			if (toAdd == null) {
				return new MaxProbabilitySol(mps.getX(),mps.getR());
			}
			for (Integer i: toAdd) {
				x.add(i);
				r.remove(Integer.valueOf(i));
				newTotalA += mp.getA(i);
				newTotalU += mp.getU(i);
			}
		}

		if (newTotalU < mp.getT() || newTotalA > mp.getB()) {
			System.err.println("Infeasible generated");
		}

		// Calculate obj of new solution
		return new MaxProbabilitySol(x,r);
	}

	private ArrayList<Integer> bestFill(ArrayList<Integer> r, int b, int target) {
		int[] a = new int[r.size()];
		int[] c = new int[r.size()];
		for (int i = 0; i < r.size(); i++) {
			int x = r.get(i);
			a[i] = mp.getA(x);
			c[i] = mp.getU(x);
		}
		Knapsack k = new Knapsack(a,b,c,false);
		if (k.getBestObj() < target) {
			return null;
		}
		boolean[] xVals = k.getXVals();
		ArrayList<Integer> toAdd = new ArrayList<Integer>();
		for (int i = 0; i < xVals.length; i++) {
			if (xVals[i]) {
				toAdd.add(r.get(i));
			}
		}
		return toAdd;
	}

	private ArrayList<ratioNode> computeRatios(ArrayList<Integer> x, ArrayList<Integer> r) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: r) {
			ratioNode rni = new ratioNode(i, mp.getU(i));
			ratio.add(rni);
		}
		Collections.sort(ratio);
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
			while ((mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT()) && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = getRItem(j);
				rndCount++;
			}

			if (mp.getA(j) - mp.getA(i) <= mp.getB() - getTotalA() && getTotalU() - mp.getU(i) + mp.getU(j) >= mp.getT()) {
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
				if (newObj > maxObj && mp.getA(j) - mp.getA(i) <= mp.getB() - getTotalA() && getTotalU() - mp.getU(i) + mp.getU(j) >= mp.getT()) {
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
		while ((mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT()) && ki < n) {
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

		if (mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT()) {
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
			while (tabuList[i][j] >= iteration && (mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT()) && ki < n) {
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
		while (tabuList[i][j] >= iteration && (mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT()) && ki < n) {
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

		if (mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT() || tabuList[i][j] >= iteration) {
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
				if (mp.getRatio(i) < minRatio && !bestIs.contains(i)) {
					minRatio = mp.getRatio(i);
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
				if (mp.getRatio(i) > maxRatio && !bestIs.contains(i)) {
					maxRatio = mp.getRatio(i);
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
				if (mp.getA(j)-mp.getA(i) <= mp.getB() - curTotalA && getTotalU() - mp.getU(i) + mp.getU(j) >= mp.getT()) {
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
				if (mp.getA(j)-mp.getA(i) <= mp.getB() - curTotalA  && getTotalU() - mp.getU(i) + mp.getU(j) >= mp.getT()) {
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

	@Override
	public boolean getValid() {
		updateValid();
		return valid;
	}

	@Override
	public void healSol() {
		//		healSolImproving();
		healSolRatio();
	}

	// most improving
	public void healSolImproving() {
		int totalA = this.getTotalA();
		int totalU = this.getTotalU();
		double obj = this.getObj();
		double num = this.num;
		double den = this.den;
		while(!this.getValid()) {
			double maxObj = -1*Double.MAX_VALUE;
			int maxI = -1;
			for (Integer i: this.getX()) {
				double newObj = mp.subObj(i, this.getX(), num, den);
				if (newObj > maxObj && totalU - mp.getU(i) >= mp.getT()) {
					maxObj = newObj;
					maxI = i;
				}
			}
			if (maxI != -1) {
				getX().remove(Integer.valueOf(maxI));
				getR().add(Integer.valueOf(maxI));
				xVals[maxI] = false;
				obj = maxObj;
				totalA = p.removeA(maxI, totalA);
				this.totalA = totalA;
				this.obj = obj;
				this.num = mp.subNum(maxI,num);
				this.den = mp.subDen(maxI, den);
			} else {
				System.err.println("Couldn't find an improving objective!!!");
				System.exit(-1);
			}
		}
	}

	//min ratio healing
	public void healSolRatio() {
		int totalA = this.getTotalA();
		int totalU = this.getTotalU();
		double obj = this.getObj();
		double num = this.num;
		double den = this.den;
		while(!this.getValid()) {
			int j = minRatio(0);
			int k = 1;
			while (k < getRSize() && totalU - mp.getU(j) < mp.getT()) {
				j = minRatio(k);
				k++;
			}
			if (totalU - mp.getU(j) >= mp.getT()) {
				getX().remove(Integer.valueOf(j));
				getR().add(Integer.valueOf(j));
				xVals[j] = false;
				obj = mp.subObj(j, getX(), num, den);
				num = mp.subNum(j, num);
				den = mp.subDen(j, den);
				totalA = p.removeA(j, totalA);
				this.totalA = totalA;
				this.obj = obj;
				this.num = num;
				this.den = den;
			} else {
				mp.genRndInit(x, r);
				for (Integer i: x) {
					xVals[i] = true;
				}
				obj = mp.getObj(x);
				num = mp.getNum();
				den = mp.getDen();
				calcTotalAU();
				updateValid();
			}
		}
	}

	@Override
	public boolean getXVals(int i) {
		return xVals[i];
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


	//TODO Finish writing/reading solution methods
	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(obj + "\n");
			pw.write(num + "\n");
			pw.write(den + "\n");
			pw.write(totalA + "\n");
			pw.write(totalU + "\n");
			for (Integer i: x) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
		}
	}

	public void readSolution(String filename) { 
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			double readNum = scr.nextDouble();
			double readDen = scr.nextDouble();
			int readTotalA = scr.nextInt();
			int readTotalU = scr.nextInt();
			if (readObj != -1) {
				x = new ArrayList<Integer>();
				while (scr.hasNextInt()) {
					x.add(scr.nextInt());
				}
				r = new ArrayList<Integer>();
				for (int i = 0; i < n; i++) {
					r.add(i);
				}
				r.removeAll(x);
				obj = readObj;
				num = readNum;
				den = readDen;
				totalA = readTotalA;
				totalU = readTotalU;
			} else {
				System.err.println("NO INCUMBENT SOLUTION IN FILE");
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

}
