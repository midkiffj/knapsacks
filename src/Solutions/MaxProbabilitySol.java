package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import ExactMethods.Knapsack_Frac;
import Problems.MaxProbability;

public class MaxProbabilitySol extends KnapsackSol {

	private static MaxProbability mp;

	private double num;
	private double den;
	private int totalU;

	public MaxProbabilitySol() {
		super();
		mp = (MaxProbability)p;
		num = mp.getNum();
		den = mp.getDen();
		calcTotalU();
		updateValid();
	}

	public MaxProbabilitySol(String filename) {
		super(filename);
		mp = (MaxProbability)p;
		updateValid();
	}

	public MaxProbabilitySol(MaxProbabilitySol mps) {
		super(mps);
		mp = (MaxProbability)p;
		totalU = mps.getTotalU();
		num = mps.getNum();
		den = mps.getDen();
		updateValid();
	}

	public MaxProbabilitySol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA, double num, double den) {
		super(x,r,obj,totalA);
		mp = (MaxProbability)p;
		calcTotalU();
		this.num = num;
		this.den = den;
		updateValid();
	}

	public MaxProbabilitySol(boolean[] newXVals) {
		super(newXVals);
		mp = (MaxProbability)p;
		calcTotalU();
		num = mp.getNum();
		den = mp.getDen();
		updateValid();
	}

	public MaxProbabilitySol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		mp = (MaxProbability)p;
		calcTotalU();
		num = mp.getNum();
		den = mp.getDen();
		updateValid();
	}

	private void calcTotalU() {
		totalU = 0;
		for (int i: getX()) {
			totalU += mp.getU(i);
		}
	}

	public void updateValid() {
		calcTotalU();
		if (getTotalA() <= mp.getB() && totalU >= mp.getT()) {
			setValid(true);
		} else {
			setValid(false);
		}
	}

	public int getTotalU() {
		return totalU;
	}

	public void addU(int i) {
		this.totalU += mp.getU(i);
	}

	public void removeU(int i) {
		this.totalU -= mp.getU(i);
	}

	public double getNum() {
		return num;
	}

	public double getDen() {
		return den;
	}

	public double swapNum(int i, int j, double num) {
		return num + mp.getU(j) - mp.getU(i);
	}

	public double subNum(int i, double num) {
		return num - mp.getU(i);
	}

	public double addNum(int i, double num) {
		return num + mp.getU(i);
	}

	public double swapDen(int i, int j, double den) {
		return den + mp.getS(j) - mp.getS(i);

	}

	public double subDen(int i, double den) {
		return den - mp.getS(i);
	}

	public double addDen(int i, double den) {
		return den + mp.getS(i);
	}

	@Override
	public void swap(int i, int j) {
		addA(j);
		removeA(i);
		addU(j);
		removeU(i);
		setObj(swapObj(i,j));
		this.num = swapNum(i,j,num);
		this.den = swapDen(i,j,den);
		updateValid();
		addI(j);
		removeI(i);
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
		int index = tryAdd(getTotalA(), getX(), getR(), false, num, den);
		if (index != -1) {
			addI(index);
			addA(index);
			addU(index);
			setObj(addObj(index, getX(), num, den));
			num = addNum(index, num);
			den = addDen(index, den);
			updateValid();
		}
		return index;
	}

	private int trySub() {
		int index = trySub(totalU, getX(), false, num, den);
		if (index != -1) {
			removeI(index);
			removeA(index);
			removeU(index);
			setObj(subObj(index, getX(), num, den));
			num = subNum(index, num);
			den = subDen(index, den);
			updateValid();
		}
		return index;
	}


	private int trySub(int totalU, ArrayList<Integer> x, boolean improveOnly, 
			double num, double den) {
		if (x.size() <= 1) {
			return -1;
		}

		int minI = minRatio(0);

		if (minI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = subObj(minI, x, num, den);
			if (change > (num*num)/den) {
				return minI;
			} else {
				return -1;
			}
		} else {
			return minI;
		}
	}

	public double subObj(int i, ArrayList<Integer> x, double num,
			double den) {
		num -= mp.getU(i);
		den -= mp.getS(i);
		return (num*num)/den;
	}

	public int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, 
			boolean improveOnly, double num, double den) {
		if (r.size() < 1) {
			return -1;
		}

		int maxI = -1;
		int b = this.getB();
		double maxRatio = -1*Double.MAX_VALUE;
		for (Integer i: r) {
			if (totalA + mp.getA(i) <= b) {
				double ratio = mp.getRatio(i);
				if (ratio > maxRatio) {
					maxRatio = ratio;
					maxI = i;
				}
			}
		}

		if (maxI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = addObj(maxI, x, num, den);
			if (change > (num*num)/den) {
				return maxI;
			} else {
				return -1;
			}
		} else {
			return maxI;
		}
	}

	public double addObj(int i, ArrayList<Integer> x, double num,
			double den) {
		num += mp.getU(i);
		den += mp.getS(i);
		return (num*num)/den;
	}

	private double swapObj(int i, int j) {
		double num = swapNum(i, j, this.num);
		double den = swapDen(i, j, this.den);
		return (num*num)/den;
	}

	public ProblemSol[] tabuMutate(int iteration, int[][] tabuList) {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.6) {
			return maxMinSwap(iteration, tabuList);
		} else {
			ProblemSol[] ratioSwap = ratioMutate(iteration, tabuList);
			return ratioSwap;
		}
	}

	public ProblemSol mutate() {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.6) {
			ProblemSol[] ret = maxMinSwap(1, new int[n][n]);
			if (ret == null) {
				return null;
			} else {
				return ret[0];
			}
		} else {
			ProblemSol ratioSwap = ratioMutate();
			return ratioSwap;
		}
	}

	public ProblemSol bestMutate() {
		if (getRSize() == 0) {
			return null;
		}
		if (p.getN() >= 500) {
			ProblemSol[] fs = firstSwap(1, new int[n][n]);
			if (fs != null) {
				return fs[0];
			}
			return null;
		}
		ProblemSol[] bs = bestSwap(1, new int[n][n]);
		if (bs != null) {
			return bs[0];
		} else {
			return null;
		}
	}

	public ProblemSol[] tabuBestMutate(int iteration, int[][] tabuList) {
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
					newTotalA += mp.getA(i);
					newTotalU += mp.getU(i);
				} else {
					r.add(i);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<Integer> tempR = new ArrayList<Integer>(r);
		ArrayList<Integer> tempX = new ArrayList<Integer>(x);


		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < mp.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newTotalA + mp.getA(rni.x) <= getB()) {
				ratio.remove(i);
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += mp.getA(rni.x);
				newTotalU += mp.getU(rni.x);
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
				return newMP.mutate();
			}
		} else {
			newMP = genMutate2(newMP, removeAttempts);
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

		ArrayList<Integer> tempR = new ArrayList<Integer>(r);
		ArrayList<Integer> tempX = new ArrayList<Integer>(x);
		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < mp.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newTotalA + mp.getA(rni.x) <= getB()) {
				ratio.remove(i);
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += mp.getA(rni.x);
				newTotalU += mp.getU(rni.x);
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
		Knapsack_Frac k = new Knapsack_Frac(a,b,c,false);
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

	private MaxProbabilitySol ratioMutate() {
		MaxProbabilitySol result = null;
		boolean found = false;
		int min = 0;
		ArrayList<Integer> curR = getR();
		while (!found && min < getXSize()) {
			// Get index of min ratio
			int i = minRatio(min);

			// Swap with a random node and return
			int j = rnd.nextInt(getRSize());
			j = curR.get(j);
			int rndCount = 0;
			while ((mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT()) && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = curR.get(j);
				rndCount++;
			}

			if (mp.getA(j) - mp.getA(i) <= mp.getB() - getTotalA() && getTotalU() - mp.getU(i) + mp.getU(j) >= mp.getT()) {
				result = new MaxProbabilitySol(this);
				result.swap(i,j);
				found = true;
			}

			min++;
		}

		return result;
	}

	private MaxProbabilitySol bestRatioMutate() {
		MaxProbabilitySol result = null;
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
				result = new MaxProbabilitySol(this);
				result.swap(i,maxJ);
				found = true;
			}

			min++;
		}

		return result;
	}

	private MaxProbabilitySol[] maxMinSwap(int iteration, int[][] tabuList) {
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
		while ((mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT()) && ki < getXSize()) {
			if (changeI) {
				ki++;
				i = minRatio(ki);
				changeI = !changeI;
			}
			kj++;
			j = maxRatio(kj);
			if (kj >= getRSize()-1) {
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
			while (tabuList[i][j] >= iteration && (mp.getA(j) - mp.getA(i) > mp.getB() - getTotalA() || getTotalU() - mp.getU(i) + mp.getU(j) < mp.getT()) && ki < getXSize()) {
				if (newMin) {
					ki++;
					i = minRatio(ki);
					newMin = !newMin;
				}
				kj++;
				j = maxRatio(kj);
				if (kj >= getRSize()-1) {
					kj = -1;
					newMin = !newMin;
				}
				if (mp.getA(j) - mp.getA(i) <= getB() - getTotalA() && getTotalU() - mp.getU(i) + mp.getU(j) >= mp.getT()) {
					newObj = swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
			if (tabuList[i][j] < iteration && mp.getA(j) - mp.getA(i) <= getB() - getTotalA() && getTotalU() - mp.getU(i) + mp.getU(j) >= mp.getT()) {
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
		MaxProbabilitySol[] results = new MaxProbabilitySol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new MaxProbabilitySol(this);
			results[0].swap(bi,bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new MaxProbabilitySol(this);
			results[1].swap(bi,bj);
		}
		return results;
	}

	private MaxProbabilitySol[] ratioMutate(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MIN_VALUE;

		// Get index of min ratio
		int i = minRatio(0);

		// Swap with a random node and return
		ArrayList<Integer> curR = getR();
		int j = rnd.nextInt(getRSize());
		j = curR.get(j);
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
			j = curR.get(j);
			if (kj == n-1) {
				kj = -1;
				changeI = !changeI;
			}
			if (mp.getA(j) - mp.getA(i) <= getB() - getTotalA() && getTotalU() - mp.getU(i) + mp.getU(j) >= mp.getT()) {
				double newObj = swapObj(i, j);
				if (newObj > bObj) {
					bi = i;
					bj = j;
					bObj = newObj;
				}
			}
		}
		ni = i;
		nj = j;
		// Compile and return data
		MaxProbabilitySol[] results = new MaxProbabilitySol[2];
		if (bi != -1 && bj != -1 && mp.getA(bj) - mp.getA(bi) <= getB() - getTotalA() && getTotalU() - mp.getU(bi) + mp.getU(bj) >= mp.getT()) {
			results[0] = new MaxProbabilitySol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1 && mp.getA(nj) - mp.getA(ni) <= getB() - getTotalA() && getTotalU() - mp.getU(ni) + mp.getU(nj) >= mp.getT() && tabuList[ni][nj] < iteration) {
			results[1] = new MaxProbabilitySol(this);
			results[1].swap(ni, nj);
		}
		return results;
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
	private MaxProbabilitySol[] bestSwap(int iteration, int[][] tabuList) {
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
		// Compile and return data
		MaxProbabilitySol[] results = new MaxProbabilitySol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new MaxProbabilitySol(this);
			results[0].swap(bi,bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new MaxProbabilitySol(this);
			results[1].swap(bi,bj);
		}
		return results;
	}

	// Return the first improving swap that keeps the knapsack feasible
	private MaxProbabilitySol[] firstSwap(int iteration, int[][] tabuList) {
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
		MaxProbabilitySol[] results = new MaxProbabilitySol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new MaxProbabilitySol(this);
			results[0].swap(bi,bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new MaxProbabilitySol(this);
			results[1].swap(bi,bj);
		}
		return results;
	}

	@Override
	public void healSol() {
		//		healSolImproving();
		healSolRatio();
	}

	// most improving
	public void healSolImproving() {
		while(!this.getValid()) {
			double maxObj = -1*Double.MAX_VALUE;
			int maxI = -1;
			for (Integer i: this.getX()) {
				double newObj = subObj(i, getX(), num, den);
				if (newObj > maxObj && totalU - mp.getU(i) >= mp.getT()) {
					maxObj = newObj;
					maxI = i;
				}
			}
			if (maxI != -1) {
				removeI(maxI);
				setObj(maxObj);
				removeA(maxI);
				removeU(maxI);
				this.num = subNum(maxI, num);
				this.den = subDen(maxI, den);
			} else {
				System.err.println("Couldn't find an improving objective!!!");
				System.exit(-1);
			}
		}
	}

	//min ratio healing
	public void healSolRatio() {
		while(!this.getValid()) {
			int j = minRatio(0);
			int k = 1;
			while (k < getRSize() && totalU - mp.getU(j) < mp.getT()) {
				j = minRatio(k);
				k++;
			}
			if (totalU - mp.getU(j) >= mp.getT()) {
				removeI(j);
				removeA(j);
				removeU(j);
				setObj(subObj(j,getX(),num,den));
				num = subNum(j, num);
				den = subDen(j, den);
			} else {
				mp.genRndInit(getX(), getR());
				for (Integer i: getX()) {
					setXVals(i,true);
				}
				for (Integer i: getR()) {
					setXVals(i,false);
				}
				setObj(mp.getObj(getX()));
				num = mp.getNum();
				den = mp.getDen();
				calcTotalA();
				calcTotalU();
				updateValid();
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

	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(getObj() + "\n");
			pw.write(num + "\n");
			pw.write(den + "\n");
			pw.write(getTotalA() + "\n");
			pw.write(totalU + "\n");
			Collections.sort(getX());
			for (Integer i: getX()) {
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
				ArrayList<Integer> readX = new ArrayList<Integer>();
				while (scr.hasNextInt()) {
					readX.add(scr.nextInt());
				}
				ArrayList<Integer> readR = new ArrayList<Integer>();
				for (int i = 0; i < n; i++) {
					readR.add(i);
				}
				readR.removeAll(readX);
				setObj(readObj);
				num = readNum;
				den = readDen;
				setTotalA(readTotalA);
				totalU = readTotalU;
			} else {
				System.err.println("NO INCUMBENT SOLUTION IN FILE");
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

}
