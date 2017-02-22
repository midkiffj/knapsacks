package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.Fractional;

public class FractionalSol extends ProblemSol {

	private static Fractional f;

	private ArrayList<Integer> x;
	private ArrayList<Integer> r;
	private boolean[] xVals;
	private boolean valid;
	private double obj;
	private long[] num;
	private long[] den;
	private int totalA;

	private int b;

	public FractionalSol() {
		super();
		f = (Fractional)p;
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		xVals = new boolean[p.getN()];
		p.genInit(x, r);
		for (Integer i: x) {
			xVals[i] = true;
		}
		obj = f.getObj(x);
		long[] fNum = f.getNum();
		long[] fDen = f.getDen();
		num = new long[f.getM()];
		den = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			num[i] = fNum[i];
			den[i] = fDen[i];
		}
		calcTotalA();
		updateValid();
		updateB();
	}

	public FractionalSol(String filename) {
		super();
		f = (Fractional)p;
		readSolution(filename);
		xVals = new boolean[p.getN()];
		for (Integer i : x) {
			xVals[i] = true;
		}
		updateValid();
		updateB();
	}

	public FractionalSol(FractionalSol fs) {
		super();
		f = (Fractional)p;
		xVals = new boolean[p.getN()];
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (Integer i : fs.getX()) {
			x.add(i);
			xVals[i] = true;
		}
		for (Integer i : fs.getR()) {
			r.add(i);
		}
		obj = fs.getObj();
		totalA = fs.getTotalA();
		long[] fNum = fs.getNum();
		long[] fDen = fs.getDen();
		num = new long[f.getM()];
		den = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			num[i] = fNum[i];
			den[i] = fDen[i];
		}
		updateValid();
		updateB();
	}

	public FractionalSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA, long[] num, long[] den) {
		super();
		f = (Fractional)p;
		xVals = new boolean[p.getN()];
		this.x = new ArrayList<Integer>(x);
		this.r = new ArrayList<Integer>(r);
		for (Integer i : x) {
			xVals[i] = true;
		}
		this.obj = obj;
		calcTotalA();
		this.num = new long[f.getM()];
		this.den = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			this.num[i] = num[i];
			this.den[i] = den[i];
		}
		updateValid();
		updateB();
	}

	public FractionalSol(boolean[] newXVals) {
		super();
		f = (Fractional)p;
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
		obj = f.getObj(x);
		calcTotalA();
		long[] fNum = f.getNum();
		long[] fDen = f.getDen();
		num = new long[f.getM()];
		den = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			num[i] = fNum[i];
			den[i] = fDen[i];
		}
		updateValid();
		updateB();
	}

	public FractionalSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super();
		f = (Fractional)p;
		xVals = new boolean[p.getN()];
		this.x = new ArrayList<Integer>(x);
		this.r = new ArrayList<Integer>(r);
		for (Integer i : x) {
			xVals[i] = true;
		}
		this.obj = f.getObj(x);
		calcTotalA();
		long[] fNum = f.getNum();
		long[] fDen = f.getDen();
		num = new long[f.getM()];
		den = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			num[i] = fNum[i];
			den[i] = fDen[i];
		}
		updateValid();
		updateB();
	}

	private void updateB() {
		if (useHealing) {
			b = Integer.MAX_VALUE;
		} else {
			b = f.getB();
		}
	}

	private void calcTotalA() {
		totalA = f.calcTotalA(x);
	}

	private void updateValid() {
		calcTotalA();
		if (totalA <= f.getB()) {
			valid = true;
		} else {
			valid = false;
		}
	}

	public int getTotalA() {
		return totalA;
	}

	@Override
	public double getObj() {
		return obj;
	}

	public long[] getNum() {
		return num;
	}

	public long[] getDen() {
		return den;
	}

	@Override
	public void swap(double newObj, int i, int j) {
		this.totalA = f.removeA(i,f.addA(j,totalA));
		this.obj = newObj;
		this.num = f.swapNum(i,j,num);
		this.den = f.swapDen(i,j,den);
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
		int index = f.tryAdd(totalA, x, r, false, num, den);
		if (index != -1) {
			xVals[index] = true;
			x.add(index);
			r.remove(Integer.valueOf(index));
			totalA = f.addA(index, totalA);
			obj = f.addObj(index, x, num, den);
			num = f.addNum(index, num);
			den = f.addDen(index, den);
			updateValid();
		}
		return index;
	}

	private int trySub() {
		int index = f.trySub(x, false, num, den);
		if (index != -1) {
			xVals[index] = false;
			r.add(index);
			x.remove(Integer.valueOf(index));
			totalA = f.removeA(index, totalA);
			obj = f.subObj(index, x, num, den);
			num = f.subNum(index, num);
			den = f.subDen(index, den);
			updateValid();
		}
		return index;
	}

	private double swapObj(int i, int j) {
		long[] num = f.swapNum(i, j, this.num);
		long[] den = f.swapDen(i, j, this.den);
		double newObj = 0;
		for (int k = 0; k < f.getM(); k++) {
			if (den[k] == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			newObj += (double)(num[k])/den[k];
		}
		return newObj;
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
		FractionalSol mps2 = (FractionalSol)ps2;
		boolean[] newXVals = new boolean[n];
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		int newTotalA = 0;
		for (int i = 0; i < n; i++) {
			if (this.getXVals(i) == mps2.getXVals(i)) {
				newXVals[i] = this.getXVals(i);
				if (newXVals[i]) {
					x.add(i);
					newTotalA = f.addA(i,newTotalA);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < f.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (p.addA(rni.x,newTotalA) <= f.getB()) {
				ratio.remove(i);
				newXVals[rni.x] = true;
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				newTotalA = f.addA(rni.x,newTotalA);
			} else {
				ratio.remove(i);
			}
		}

		return new FractionalSol(newXVals);
	}

	public ProblemSol genMutate(int removeAttempts) {
		FractionalSol newMP = new FractionalSol(this);
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

	private FractionalSol genMutate2(FractionalSol mps, int removeAttempts) {
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

		// Compute ratios
		ArrayList<ratioNode> ratio = computeRatios(x, r);
		Collections.sort(ratio);

		// Calc solution capacity
		int newTotalA = 0;
		for (Integer i: x) {
			newTotalA += f.getA(i);
		}
		// Add max-ratio items until knapsack full
		while (ratio.size() > 0 && newTotalA < f.getB()) {
			ratioNode rni = ratio.get(ratio.size()-1);
			if (newTotalA + f.getA(rni.x) <= f.getB()) {
				ratio.remove(ratio.size()-1);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += f.getA(rni.x);
			} else {
				ratio.remove(ratio.size()-1);
			}
		}
		// Calculate obj of new solution
		double obj = f.getObj(x);
		return new FractionalSol(x,r,obj,newTotalA, f.getNum(), f.getDen());
	}

	private ArrayList<ratioNode> computeRatios(ArrayList<Integer> x, ArrayList<Integer> r) {
		ArrayList<ratioNode> ratio = new ArrayList<ratioNode>();
		for (Integer i: r) {
			ratioNode rni = new ratioNode(i, f.getRatio(i));
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
			while ((f.getA(j) - f.getA(i) > f.getB() - getTotalA()) && rndCount < 10) {
				j = rnd.nextInt(getRSize());
				j = getRItem(j);
				rndCount++;
			}

			if (f.getA(j) - f.getA(i) <= f.getB() - getTotalA()) {
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
				if (newObj > maxObj && f.getA(j) - f.getA(i) <= f.getB() - getTotalA()) {
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
		while ((f.getA(j) - f.getA(i) > f.getB() - getTotalA()) && ki < n) {
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

		if (f.getA(j) - f.getA(i) > f.getB() - getTotalA()) {
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
			while (tabuList[i][j] >= iteration && (f.getA(j) - f.getA(i) > f.getB() - getTotalA()) && ki < n) {
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
		while (tabuList[i][j] >= iteration && (f.getA(j) - f.getA(i) > f.getB() - getTotalA()) && ki < n) {
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

		if (f.getA(j) - f.getA(i) > f.getB() - getTotalA() || tabuList[i][j] >= iteration) {
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
				if (f.getRatio(i) < minRatio && !bestIs.contains(i)) {
					minRatio = f.getRatio(i);
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
				if (f.getRatio(i) > maxRatio && !bestIs.contains(i)) {
					maxRatio = f.getRatio(i);
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
				if (f.getA(j)-f.getA(i) <= f.getB() - curTotalA) {
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
				if (f.getA(j)-f.getA(i) <= f.getB() - curTotalA) {
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
		System.err.println("Healing Unimplemented");
		//	healSolImproving();
		//	healSolRatio();
	}

	// most improving
	private void healSolImproving() {
		int totalA = this.getTotalA();
		double obj = this.getObj();
		while(!this.getValid()) {
			double maxObj = -1*Double.MAX_VALUE;
			int maxI = -1;
			for (Integer i: this.getX()) {
				double newObj = f.subObj(i, this.getX(), num, den);
				if (newObj > maxObj) {
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
				this.num = f.subNum(maxI, this.num);
				this.den = f.subDen(maxI, this.den);
			} else {
				System.err.println("Couldn't find an improving objective!!!");
				System.exit(-1);
			}
		}
	}

	//min ratio healing
	private void healSolRatio() {
		int totalA = this.getTotalA();
		double obj = this.getObj();
		while(!this.getValid()) {
			int j = minRatio(0);
			int k = 1;
			getX().remove(Integer.valueOf(j));
			getR().add(Integer.valueOf(j));
			xVals[j] = false;
			obj = f.subObj(j, getX(), num, den);
			num = f.subNum(j, num);
			den = f.subDen(j, den);
			totalA = p.removeA(j, totalA);
			this.totalA = totalA;
			this.obj = obj;
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


	/*
	 *  Write solution to the given file
	 */
	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(obj + "\n");
			pw.write(totalA + "\n");
			for (int i = 0; i < f.getM(); i++) {
				pw.write(num[i] + " ");
			}
			pw.write("\n");
			for (int i = 0; i < f.getM(); i++) {
				pw.write(den[i] + " ");
			}
			pw.write("\n");

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
			int readTotalA = scr.nextInt();
			long[] readNum = new long[f.getM()];
			long[] readDen = new long[f.getM()];
			for (int i = 0; i < f.getM(); i++) {
				readNum[i] = scr.nextLong();
			}
			for (int i = 0; i < f.getM(); i++) {
				readDen[i] = scr.nextLong();
			}
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
			} else {
				System.err.println("NO INCUMBENT SOLUTION IN FILE");
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

}
