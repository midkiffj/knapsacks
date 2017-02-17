package Solutions;

import java.util.ArrayList;

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
	public void swap(double newObj, int i, int j) {
		this.obj = newObj;
		xVals[i] = false;
		xVals[j] = true;
		remove(x,i);
		x.add(j);
		remove(r,j);
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

	@Override
	public double[] bestMutate() {
		if (r.size() > 0) {
			double[][] best = bestSwap(Integer.MAX_VALUE, new int[n][n]);
			return best[0];
		} else {
			return null;
		}
	}

	// Find the best swap possible that keeps the knapsack feasible
	private double[][] bestSwap(int iteration, int[][] tabuList) {
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
		double[][] results = new double[2][3];
		results[0][0] = bObj;
		results[0][1] = bi;
		results[0][2] = bj;
		results[1][0] = nTObj;
		results[1][1] = ni;
		results[1][2] = nj;
		return results;
	}

	public double[][] tabuMutate(int iteration, int[][] tabuList) {
		if (r.size() > 0) {
			if (rnd.nextDouble() < 0.4) {
				return maxMinSwap(iteration, tabuList);
			} else {
				double[] tauSwap = tauMutate(iteration, tabuList);
				if (tauSwap == null) {
					return null;
				}
				double[][] result = {tauSwap, tauSwap};
				return result;
			}
		} else {
			return null;
		}
	}

	public double[] mutate() {
		if (r.size() > 0) {
			if (rnd.nextDouble() < 0.6) {
				double[][] ret = maxMinSwap(1, new int[n][n]);
				if (ret == null) {
					return null;
				} else {
					return ret[0];
				}
			} else {
				double[] tauSwap = tauMutate();
				if (tauSwap == null) {
					return null;
				}
				return tauSwap;
			}
		} else {
			return null;
		}
	}

	public double[][] tabuBestMutate(int iteration, int[][] tabuList) {
		if (r.size() > 0) {
			return bestSwap(iteration, tabuList);
		} else {
			return null;
		}
	}

	private double[] tauMutate() {
		double[] result = null;
		// Get index of max tau
		int i = maxTau(0);

		// Swap with a random node and return
		int j = rnd.nextInt(getRSize());
		j = getRItem(j);

		double newObj = u.swapObj(i, j, x, obj);
		result = new double[3];
		result[0] = newObj;
		result[1] = i;
		result[2] = j;

		return result;
	}

	private double[] bestTauMutate() {
		double[] result = null;
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
			double newObj = bestObj;
			result = new double[3];
			result[0] = newObj;
			result[1] = i;
			result[2] = maxJ;
		}

		return result;
	}

	private double[][] maxMinSwap(int iteration, int[][] tabuList) {
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
		double[][] results = new double[2][3];
		results[0][0] = bObj;
		results[0][1] = bi;
		results[0][2] = bj;
		results[1][0] = nTObj;
		results[1][1] = ni;
		results[1][2] = nj;
		return results;
	}

	private double[] tauMutate(int iteration, int[][] tabuList) {
		// Get index of min tau
		int i = maxTau(0);

		// Swap with a random node and return
		int j = rnd.nextInt(getRSize());
		j = getRItem(j);
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
			j = getRItem(j);
			if (kj == n-1) {
				kj = -1;
				changeI = !changeI;
			}
		}

		if (tabuList[i][j] >= iteration) {
			return null;
		}
		double newObj = u.swapObj(i, j, x, obj);
		double[] result = {newObj, i, j};

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
		int index = p.tryAdd(-1, x, r, false);
		if (index != -1) {
			xVals[index] = true;
			x.add(index);
			remove(r, index);
			this.obj = p.addObj(index, x, obj);
		}
		return index;
	}

	private int trySub() {
		int index = p.trySub(x, false);
		if (index != -1) {
			xVals[index] = false;
			r.add(index);
			remove(x, index);
			this.obj = p.subObj(index, x, obj);
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
	public boolean getValid() {
		return valid;
	}

	@Override
	public boolean getXVals(int i) {
		return xVals[i];
	}

	private void remove(ArrayList<Integer> arr, int i) {
		arr.remove(Integer.valueOf(i));
	}

	@Override
	public int compareTo(ProblemSol o) 	{
		if (o.getValid() && this.getValid() || !(o.getValid() && this.getValid())) {
			double diff = this.getObj() - o.getObj();
			if (diff >= 0) {
				return -1;
			} else {
				return 1;
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
	public boolean betterThan(double newObj) {
		if (newObj < obj) {
			return true;
		}
		return false;
	}

	@Override
	public void healSol() {
		// Unneeded: There are no infeasible solutions to the Unconstrained Cubic.
		return;
	}

}
