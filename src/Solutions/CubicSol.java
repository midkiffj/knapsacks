package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.Cubic;

public class CubicSol extends KnapsackSol {

	private static Cubic c;
	
	public CubicSol() {
		super();
		c = (Cubic)p;
	}

	public CubicSol(String filename) {
		super(filename);
		c = (Cubic)p;
	}

	public CubicSol(CubicSol cs) {
		super((KnapsackSol)cs);
		c = (Cubic)p;
	}

	public CubicSol(boolean[] xVals) {
		super(xVals);
		c = (Cubic)p;
	}

	public CubicSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		c = (Cubic)p;
	}

	public CubicSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA) {
		super(x,r,obj,totalA);
		c = (Cubic)p;
	}
	
	public void updateValid() {
		calcTotalA();
		if (getTotalA() <= c.getB()) {
			setValid(true);
		} else {
			setValid(false);
		}
	}

	private CubicSol swapMutate() {
		if (rnd.nextDouble() < 0.8) {
			return ratioMutate();
		} else {
			return bestRatioMutate();
		}
	}

	/*
	 * Swap the boolean values in the current x array at indexes i and j
	 * @param curX
	 * @param i
	 * @param j
	 */
	public void swap(int i, int j) {
		setObj(swapObj(i,j));
		removeA(i);
		addA(j);
		addI(j);
		removeI(i);
		updateValid();
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
		int index = tryAdd(getTotalA(), getX(), getR(), false);
		if (index != -1) {
			addI(index);
			addA(index);
			setObj(addObj(index));
			updateValid();
		}
		return index;
	}

	private int trySub() {
		int index = trySub(getX(), false);
		if (index != -1) {
			removeI(index);
			removeA(index);
			setObj(subObj(index));
			updateValid();
		}
		return index;
	}

	public ProblemSol[] tabuMutate(int iteration, int[][] tabuList) {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.6) {
			return maxMinSwap(iteration, tabuList);
		} else {
			CubicSol ratioSwap = ratioMutate(iteration, tabuList);
			CubicSol[] result = {ratioSwap, ratioSwap};
			return result;
		}
	}

	public ProblemSol mutate() {
		if (getRSize() == 0) {
			return null;
		}
		if (rnd.nextDouble() < 0.6) {
			CubicSol[] ret = maxMinSwap(1, new int[n][n]);
			if (ret == null) {
				return null;
			} else {
				return ret[0];
			}
		} else {
			CubicSol ratioSwap = ratioMutate();
			return ratioSwap;
		}
	}

	public ProblemSol bestMutate() {
		if (getRSize() == 0) {
			return null;
		}
		if (p.getN() >= 500) {
			CubicSol[] fs = firstSwap(1, new int[n][n]);
			if (fs != null) {
				return fs[0];
			}
			return null;
		}
		CubicSol[] bs = bestSwap(1, new int[n][n]);
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
					newTotalA += c.getA(i);
				} else {
					r.add(i);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newTotalA + c.getA(rni.x) <= getB()) {
				ratio.remove(i);
				newXVals[rni.x] = true;
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += c.getA(rni.x);
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
				return newCS.mutate();
			}
		} else {
			newCS = genMutate2(newCS, removeAttempts);
			if (removeAttempts < n-1) {
				removeAttempts++;
			}
		}
		return newCS;
	}

	private int trySub(ArrayList<Integer> x, boolean improveOnly) {
		if (x.size() <= 1) {
			return -1;
		}
		int minI = minRatio(0);

		if (minI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = subObj(minI);
			if (change > getObj()) {
				return minI;
			} else {
				return -1;
			}
		} else {
			return minI;
		}
	}

	private int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly) {
		if (x.size() == n) {
			return -1;
		}
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (totalA + c.getA(i) <= getB()) {
				double ratio = c.getRatio(i);
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
			double change = addObj(maxI);
			if (change > getObj()) {
				return maxI;
			} else {
				return -1;
			}
		} else {
			return maxI;
		}
	}

	private double subObj(int i) {
		double oldObj = getObj() - c.getCi(i);
		ArrayList<Integer> curX = getX();
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - c.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - c.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	private double addObj(int i) {
		double oldObj = getObj() + c.getCi(i);
		ArrayList<Integer> curX = getX();
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj + c.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj + c.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}
	
	public double swapObj(int i, int j) {
		return swapObj(i,j,getX(),getObj());
	}

	private double swapObj(int i, int j, ArrayList<Integer> curX, double oldObj) {
		oldObj = oldObj - c.getCi(i);
		oldObj = oldObj + c.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - c.getCij(i,xk);
				oldObj = oldObj + c.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - c.getDijk(i,xk,xl);
						oldObj = oldObj + c.getDijk(j,xk,xl);
					}
				}
			}
		}
		return oldObj;
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

	private CubicSol ratioMutate() {
		CubicSol result = null;
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
			while (c.getA(j) - c.getA(i) > getB() - getTotalA() && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = curR.get(j);
				rndCount++;
			}

			if (c.getA(j) - c.getA(i) <= getB() - getTotalA()) {
				result = new CubicSol(this);
				result.swap(i, j);
				found = true;
			}

			min++;
		}

		return result;
	}

	private CubicSol bestRatioMutate() {
		CubicSol result = null;
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
				if (newObj > maxObj && c.getA(j) - c.getA(i) <= getB() - getTotalA()) {
					maxObj = newObj;
					maxJ = j;
				}
			}
			if (maxJ != -1) {
				result = new CubicSol(this);
				result.swap(i, maxJ);
				found = true;
			}

			min++;
		}

		return result;
	}

	private CubicSol[] maxMinSwap(int iteration, int[][] tabuList) {
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
		while (c.getA(j) - c.getA(i) > getB() - getTotalA() && ki < getXSize()) {
			if (changeI) {
				ki++;
				i = minRatio(ki);
				changeI = !changeI;
			}
			kj++;
			j = maxRatio(kj);
			if (kj == getRSize()-1) {
				kj = -1;
				changeI = !changeI;
			}
		}

		if (c.getA(j) - c.getA(i) > getB() - getTotalA()) {
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
			while (tabuList[i][j] >= iteration && c.getA(j) - c.getA(i) > getB() - getTotalA() && ki < getXSize()) {
				if (newMin) {
					ki++;
					i = minRatio(ki);
					newMin = !newMin;
				}
				kj++;
				j = maxRatio(kj);
				if (kj == getRSize()-1) {
					kj = -1;
					newMin = !newMin;
				}
				if (c.getA(j) - c.getA(i) <= getB() - getTotalA()) {
					newObj = swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
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
		CubicSol[] results = new CubicSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicSol(this);
			results[1].swap(ni, nj);
		}
		return results;
	}

	private CubicSol ratioMutate(int iteration, int[][] tabuList) {
		// Get index of min ratio
		int i = minRatio(0);

		// Swap with a random node and return
		ArrayList<Integer> curR = getR();
		int j = rnd.nextInt(getRSize());
		j = curR.get(j);
		int ki = 0;
		int kj = 0;
		boolean changeI = false;
		while (tabuList[i][j] >= iteration && c.getA(j) - c.getA(i) > getB() - getTotalA() && ki < n) {
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
		}

		if (c.getA(j) - c.getA(i) > getB() - getTotalA() || tabuList[i][j] >= iteration) {
			return null;
		}
		CubicSol results = new CubicSol(this);
		results.swap(i, j);
		return results;
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
	private CubicSol[] bestSwap(int iteration, int[][] tabuList) {
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
				if (c.getA(j)-c.getA(i) <= getB() - curTotalA) {
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
		CubicSol[] results = new CubicSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicSol(this);
			results[1].swap(ni, nj);
		}
		return results;
	}

	// Return the first improving swap that keeps the knapsack feasible
	private CubicSol[] firstSwap(int iteration, int[][] tabuList) {
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
				if (c.getA(j)-c.getA(i) <= getB() - curTotalA) {
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
		CubicSol[] results = new CubicSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new CubicSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new CubicSol(this);
			results[1].swap(ni, nj);
		}
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
				double newObj = subObj(i);
				if (newObj > maxObj) {
					maxObj = newObj;
					maxI = i;
				}
			}
			if (maxI != -1) {
				removeI(maxI);
				setObj(maxObj);
				removeA(maxI);
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
			removeI(j);
			setObj(subObj(j));
			removeA(j);
		}
	}

	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(getObj() + "\n");
			pw.write(getTotalA() + "\n");
			for (Integer i: getX()) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
		}
	}

	public void readSolution(String filename) { 
		c = (Cubic)p;
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			int readTotalA = scr.nextInt();
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
				setTotalA(readTotalA);
				setX(readX);
				setR(readR);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}
}
