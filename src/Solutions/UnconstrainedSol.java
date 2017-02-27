package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import Problems.Unconstrained;

public class UnconstrainedSol extends ProblemSol {

	private Unconstrained u;

	private ArrayList<Integer> x;
	private ArrayList<Integer> r;
	private boolean[] xVals;
	private boolean valid;
	private double obj;

	public UnconstrainedSol() {
		super();
		u = (Unconstrained)p;
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		u.genRndInit(x, r);
		xVals = new boolean[n];
		obj = u.getObj(x);
		valid = true;

	}

	public UnconstrainedSol(UnconstrainedSol us) {
		super();
		u = (Unconstrained)p;
		xVals = new boolean[p.getN()];
		valid = us.getValid();
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (Integer i : us.getX()) {
			x.add(i);
			xVals[i] = true;
		}
		for (Integer i : us.getR()) {
			r.add(i);
		}
		obj = us.getObj();
	}

	public UnconstrainedSol(boolean[] xVals) {
		super();
		u = (Unconstrained)p;

		this.xVals = xVals;
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (int i = 0; i < xVals.length; i++) {
			if (xVals[i]) {
				x.add(i);
			} else {
				r.add(i);
			}
		}
		obj = p.getObj(x);
		valid = true;
	}

	public UnconstrainedSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super();
		u = (Unconstrained)p;
		xVals = new boolean[p.getN()];
		this.x = x;
		this.r = r;
		for (Integer i: x) {
			xVals[i] = true;
		}
		this.obj = p.getObj(x);
		valid = true;
	}

	public UnconstrainedSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj) {
		super();
		u = (Unconstrained)p;

		xVals = new boolean[p.getN()];
		this.x = x;
		this.r = r;
		for (Integer i: x) {
			xVals[i] = true;
		}
		this.obj = obj;
		valid = true;
	}

	@Override
	public double getObj() {
		return obj;
	}

	@Override
	public void swap(int i, int j) {
		this.obj = u.swapObj(i, j, x, obj);
		removeI(i);
		addI(j);
	}

	@Override
	public ProblemSol bestMutate() {
		if (r.size() > 0) {
			ProblemSol[] best = bestSwap(Integer.MAX_VALUE, new int[n][n]);
			return best[0];
		} else {
			return null;
		}
	}

	// Find the best swap possible that keeps the knapsack feasible
	private ProblemSol[] bestSwap(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MAX_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MAX_VALUE;
		for(Integer i: getX()) {
			for(Integer j: getR()) {
				double newObj = u.swapObj(i, j, x, obj);
				if (newObj < nTObj && tabuList[i][j] < iteration) {
					ni = i;
					nj = j;
					nTObj = newObj;
				}
				if (newObj < bObj) {
					bi = i;
					bj = j;
					bObj = newObj;
				}
			}
		}

		// Compile and return data
		UnconstrainedSol[] results = new UnconstrainedSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new UnconstrainedSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new UnconstrainedSol(this);
			results[1].swap(ni, nj);
		}
		return results;
	}

	public ProblemSol[] tabuMutate(int iteration, int[][] tabuList) {
		if (r.size() > 0) {
			if (rnd.nextDouble() < 0.4) {
				return maxMinSwap(iteration, tabuList);
			} else {
				ProblemSol tauSwap = tauMutate(iteration, tabuList);
				ProblemSol[] result = {tauSwap, tauSwap};
				return result;
			}
		} else {
			return null;
		}
	}

	public ProblemSol mutate() {
		if (r.size() > 0) {
			if (rnd.nextDouble() < 0.6) {
				ProblemSol[] ret = maxMinSwap(1, new int[n][n]);
				if (ret == null) {
					return null;
				} else {
					return ret[0];
				}
			} else {
				ProblemSol tauSwap = tauMutate();
				return tauSwap;
			}
		} else {
			return null;
		}
	}

	public ProblemSol[] tabuBestMutate(int iteration, int[][] tabuList) {
		if (r.size() > 0) {
			return bestSwap(iteration, tabuList);
		} else {
			return null;
		}
	}

	private UnconstrainedSol tauMutate() {
		// Get index of max tau
		int i = maxTau(0);

		// Swap with a random node and return
		ArrayList<Integer> curR = getR();
		int j = rnd.nextInt(getRSize());
		j = curR.get(j);

		UnconstrainedSol result = new UnconstrainedSol(this);
		result.swap(i, j);

		return result;
	}

	private UnconstrainedSol bestTauMutate() {
		UnconstrainedSol result = null;
		// Get index of max tau
		int i = maxTau(0);

		// Swap with all nodes and return best
		double bestObj = Integer.MAX_VALUE;
		int maxJ = -1;
		for (Integer j: getR()) {
			double newObj = u.swapObj(i, j, x, obj);
			if (newObj < bestObj) {
				bestObj = newObj;
				maxJ = j;
			}
		}
		if (maxJ != -1) {
			result = new UnconstrainedSol(this);
			result.swap(i,maxJ);
		}

		return result;
	}

	private UnconstrainedSol[] maxMinSwap(int iteration, int[][] tabuList) {
		// Store nontabu and best tabu swaps
		int ni = -1;
		int nj = -1;
		double nTObj = Integer.MAX_VALUE;
		int bi = -1;
		int bj = -1;
		double bObj = Integer.MAX_VALUE;

		int i = maxTau(0);
		int j = minTau(0);
		int ki = 0;
		int kj = 0;

		double newObj = u.swapObj(i, j, x, obj);
		bi = i;
		bj = j;
		bObj = newObj;
		if (tabuList[i][j] < iteration) {
			ni = i;
			nj = j;
			nTObj = newObj;
		} else {
			boolean newMax = false;
			while (tabuList[i][j] >= iteration && ki < x.size()) {
				if (newMax) {
					ki++;
					i = maxTau(ki);
					newMax = !newMax;
				}
				kj++;
				j = minTau(kj);
				if (kj == n) {
					kj = -1;
					newMax = !newMax;
				}
			}
			if (tabuList[i][j] < iteration) {
				newObj = u.swapObj(i, j, x, obj);
				ni = i;
				nj = j;
				nTObj = newObj;
				if (newObj < bObj) {
					bi = i;
					bj = j;
					bObj = newObj;
				}
			}
		}
		// Compile and return data
		UnconstrainedSol[] results = new UnconstrainedSol[2];
		if (bi != -1 && bj != -1) {
			results[0] = new UnconstrainedSol(this);
			results[0].swap(bi, bj);
		}
		if (ni != -1 && nj != -1) {
			results[1] = new UnconstrainedSol(this);
			results[1].swap(ni, nj);
		}
		return results;
	}

	private UnconstrainedSol tauMutate(int iteration, int[][] tabuList) {
		// Get index of min tau
		int i = maxTau(0);

		// Swap with a random node and return
		ArrayList<Integer> curR = getR();
		int j = rnd.nextInt(getRSize());
		j = curR.get(j);
		int ki = 0;
		int kj = 0;
		boolean changeI = false;
		while (tabuList[i][j] >= iteration && ki < x.size()) {
			if (changeI) {
				ki++;
				i = maxTau(ki);
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

		if (tabuList[i][j] >= iteration) {
			return null;
		}
		UnconstrainedSol result = new UnconstrainedSol(this);
		result.swap(i,j);

		return result;
	}

	private int minTau(int k) {
		// Find the minimum tau not in the solution
		double minTau = Double.MAX_VALUE;
		int minI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < getXSize()) {
			for (Integer i: getR()) {
				if (u.getTau(i) < minTau && !bestIs.contains(i)) {
					minTau = u.getTau(i);
					minI = i;
				}
			}
			minTau = Double.MAX_VALUE;
			bestIs.add(minI);
		}
		return minI;
	}

	private int maxTau(int k) {
		// Find the maximum tau in the solution
		double maxTau = -1*Double.MAX_VALUE;
		int maxI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < getRSize()) {
			for (Integer i: getX()) {
				if (u.getTau(i) > maxTau && !bestIs.contains(i)) {
					maxTau = u.getTau(i);
					maxI = i;
				}
			}
			maxTau = -1*Double.MAX_VALUE;
			bestIs.add(maxI);
		}
		return maxI;
	}

	@Override
	public int shift() {
		if (getXSize() < 2) {
			return tryAdd();
		} else {
			if (rnd.nextDouble() < 0.5) {
				return tryAdd();
			} else {
				return trySub();
			}
		}
	}

	// Try to add a variable to the solution
	private int tryAdd() {
		int index = u.tryAdd(-1, x, r, false);
		if (index != -1) {
			addI(index);
			this.obj = u.addObj(index, x, obj);
		}
		return index;
	}

	private int trySub() {
		int index = u.trySub(x, false);
		if (index != -1) {
			removeI(index);
			this.obj = u.subObj(index, x, obj);
		}
		return index;
	}

	@Override
	public ProblemSol crossover(ProblemSol ps2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProblemSol genMutate(int removeAttempts) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(ProblemSol o) 	{
		if (o.getValid() && this.getValid() || !(o.getValid() && this.getValid())) {
			double diff = this.getObj() - o.getObj();
			if (diff >= 0) {
				return 1;
			} else {
				return -1;
			}
		} else {
			if (o.getValid()) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	@Override
	public void healSol() {
		// Unneeded: There are no infeasible solutions to the Unconstrained Cubic.
		return;
	}
	
	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(getObj() + "\n");
			for (Integer i: getX()) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
		}
	}
	
	public void readSolution(String filename) { 
		u = (Unconstrained)p;
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
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
				setX(readX);
				setR(readR);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

	@Override
	public void updateValid() {
		// TODO Auto-generated method stub
		
	}

}
