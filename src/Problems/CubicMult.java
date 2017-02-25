package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import Runner.TestLogger;


public class CubicMult extends MultipleKnapsack {
	// Cubic setup vars
	private int n;
	private int m;
	private Random rnd;
	private int seed;
	private boolean negCoef;
	private double density;

	// Coefficient vars
	private int[][] a;
	private int[] b;
	private int[] ci;
	private int[][] cij;
	private int[][][] dijk;


	// Cubic manipulation vars
	private int[] tau;
	private double[] ratio;

	public CubicMult(String filename) {
		super();
		readFromFile(filename);
	}

	public CubicMult(int n, boolean negCoef) {
		this(n,negCoef,1234,1);
	}

	public CubicMult(int n, boolean negCoef, int seed) {
		this(n,negCoef,seed,1);
	}

	public CubicMult(int n, boolean negCoef, int seed, double density) {
		this(n,1,negCoef,seed,density);
	}

	public CubicMult(int n, int m, boolean negCoef, int seed, double density) {
		super();
		this.n = n;
		this.m = m;
		this.negCoef = negCoef;
		this.rnd = new Random(seed);
		this.seed = seed;
		this.density = density;
		setup();
	}

	/**
	 * Initialize the objective and knapsack constraint coefficients
	 */
	private void setup() {
		int i,j,k;

		// Define objective coefficient matrices
		int[] totalA = new int[m];;
		a = new int[m][n];
		ci = new int[n];
		cij = new int[n-1][];
		dijk = new int[n-1][][];
		tau = new int[n];
		ratio = new double[n];

		// Fill matrices with randomized coefficients
		// a : [1,50]
		// c : [-100,100]; cij = cji; cii = 0
		for(i = 0; i < n; i++){
			if (i < n-1) {
				cij[i] = new int[n-i];
				dijk[i] = new int[n-i][];
			}
			if (negCoef) {
				ci[i] = rnd.nextInt(201) - 100;
			} else {
				ci[i] = rnd.nextInt(101);
			}

			for (j = 0; j < m; j++) {
				a[j][i] = rnd.nextInt(50)+1;
				totalA[j] += a[j][i];
			}

			tau[i] += ci[i];
			for (j = i+1; j < n; j++){
				dijk[i][j-i] = new int[n-j];
				if (rnd.nextDouble() <= density) {
					if (negCoef) {
						cij[i][j-i] = rnd.nextInt(201) - 100;
					} else {
						cij[i][j-i] = rnd.nextInt(101);
					}
				} else {
					cij[i][j-i] = 0;
				}
				//				cij[j][i] = cij[i][j];
				tau[i] += cij[i][j-i];
				tau[j] += cij[i][j-i];
				for(k = j+1; k < n; k++) {
					if (rnd.nextDouble() <= density) {
						if (negCoef) {
							dijk[i][j-i][k-j] = rnd.nextInt(201) - 100;
						} else {
							dijk[i][j-i][k-j] = rnd.nextInt(101);
						}
					} else {
						dijk[i][j-i][k-j] = 0;
					}
					//					dijk[i][k][j] = dijk[i][j][k];
					//					dijk[j][k][i] = dijk[i][j][k];
					//					dijk[j][i][k] = dijk[i][j][k];
					//					dijk[k][i][j] = dijk[i][j][k];
					//					dijk[k][j][i] = dijk[i][j][k];
					tau[i] += dijk[i][j-i][k-j];
					tau[j] += dijk[i][j-i][k-j];
					tau[k] += dijk[i][j-i][k-j];
				}
			}
		}

		for (i = 0; i < n; i++) {
			double sumRatios = 0;
			for (j = 0; j < m; j++) {
				sumRatios += (double)(tau[i])/a[j][i];
			}
			ratio[i] = sumRatios/m;
		}

		// b : [50,sum(a)]
		b = new int[m];
		for (j = 0; j < m; j++) {
			b[j] = rnd.nextInt(totalA[j]-50+1)+50;
		}
	}

	/**
	 * Randomly generate a solution to the cubic
	 */
	public void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		r.clear();
		x.clear();
		int[] totalAx = new int[m];
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}
		boolean done = false;
		while (totalAValid(totalAx) && !done && !r.isEmpty()) {
			int num = rnd.nextInt(r.size());
			i = r.get(num);
			if (addTotalA(totalAx,i)) {
				x.add(i);
				r.remove(Integer.valueOf(i));
				addA(i,totalAx);
			} else {
				done = true;
			}
			if (rnd.nextDouble() > 0.7) {
				done = true;
			}
		}
	}


	/**
	 * Generate a solution by adding x's until knapsack full
	 * and update the current objective value. 
	 * Don't use any indexes in the provided list.
	 */
	public void genInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		r.clear();
		x.clear();
		int[] totalAx = new int[m];
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}
		boolean[] inX = new boolean[n];
		boolean done = false;
		while (totalAValid(totalAx) && !done) {
			double maxRatio = -1*Double.MAX_VALUE;
			i = -1;
			for (int j = 0; j < r.size(); j++) {
				int xj = r.get(j);
				if (!inX[xj] && ratio[xj] >= maxRatio && addTotalA(totalAx,xj)) {
					i = xj;
					maxRatio = ratio[xj];
				}
			}
			if (i == -1) {
				done = true;
			} else {
				x.add(i);
				r.remove(Integer.valueOf(i));
				totalAx = addA(i,totalAx);
				inX[i] = true;
			}
		}

		double curObj = getObj(x);

		// Check for Swaps and shifts
		boolean swapping = true;
		int swaps = 0;
		//		while (swapping && swaps < 5) {
		while (swapping) {
			int maxI = -1;
			int maxJ = -1;
			double maxChange = 0;
			for(Integer xi: x) {
				for(Integer xj: r) {
					// Check for knapsack feasibility
					if (swapTotalA(totalAx,xi,xj)) {
						double newObj = swapObj(xi, xj, x, curObj);
						double change = newObj - curObj;
						if (change > maxChange) {
							maxI = xi;
							maxJ = xj;
							maxChange = change;
						}
					}
				}
			}
			double[] add = tryAdd(x,r, curObj, totalAx);
			double[] sub = trySub(x,r, curObj, totalAx);
			double addChange = add[0];
			double subChange = sub[0];
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				curObj = curObj + add[0];
				totalAx = addA(addI,totalAx);
			} else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				curObj = curObj + sub[0];
				totalAx = removeA(subI,totalAx);
			} else {
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					curObj = curObj + maxChange;
					totalAx = addA(maxJ,removeA(maxI,totalAx));
				}
			}
			swaps++;
		}

		System.out.println("Generated Incumbent: " + curObj);
		Collections.sort(x);
		System.out.println(x.toString());
	}

	/**
	 * Calculate the objective value with the given x values.
	 */
	public double getObj(ArrayList<Integer> x) {
		int i,j,k;
		double curObj = 0;
		for(i = 0; i < x.size(); i++){
			int xi = x.get(i);
			curObj += ci[xi];
			for (j = i+1; j < x.size(); j++){
				int xj = x.get(j);
				curObj += this.getCij(xi, xj);
				for(k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					curObj += this.getDijk(xi,xj,xk);
				}
			}
		} 
		return curObj;
	}


	public int trySub(ArrayList<Integer> x, boolean improveOnly) {
		if (x.size() <= 1) {
			return -1;
		}
		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		for (Integer i: x) {
			double ratio = this.getRatio(i);
			if (ratio < minRatio) {
				minRatio = ratio;
				minI = i;
			}
		}

		if (minI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = subObj(minI, x, 0);
			if (change > 0) {
				return minI;
			} else {
				return -1;
			}
		} else {
			return minI;
		}
	}

	public int tryAdd(int[] totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly) {
		if (x.size() == this.getN()) {
			return -1;
		}
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (addTotalA(totalA,i)) {
				double ratio = this.getRatio(i);
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
			double change = addObj(maxI, x, 0);
			if (change > 0) {
				return maxI;
			} else {
				return -1;
			}
		} else {
			return maxI;
		}
	}

	public double subObj(int i, ArrayList<Integer> curX, double oldObj) {
		oldObj = oldObj - this.getCi(i);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - this.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - this.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	public double addObj(int i, ArrayList<Integer> curX, double oldObj) {
		oldObj = oldObj + this.getCi(i);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj + this.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj + this.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	public int[] removeA(int i, int[] totalA) {
		int[] newTotalA = new int[m];
		for (int j = 0; j < m; j++) {
			newTotalA[j] = totalA[j] - a[j][i];
		}
		return newTotalA;
	}

	public int[] addA(int i, int[] totalA) {
		int[] newTotalA = new int[m];
		for (int j = 0; j < m; j++) {
			newTotalA[j] = totalA[j] + a[j][i];
		}
		return newTotalA;
	}

	public boolean checkValid(ArrayList<Integer> x) {
		int[] totalA = calcAllTotalA(x);
		return totalAValid(totalA);
	}
	
	public int[] calcAllTotalA(ArrayList<Integer> x) {
		int[] totalA = new int[m];
		for (Integer i: x) {
			for(int j = 0; j < m; j++) {
				totalA[j] += a[j][i];
			}
		}
		return totalA;
	}
	
	public boolean totalAValid(int[] totalA) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] > b[i]) {
				return false;
			}
		}
		return true;
	}
	
	public boolean addTotalA(int[] totalA, int j) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] + a[i][j] > b[i]) {
				return false;
			}
		}
		return true;
	}
	
	public boolean subTotalA(int[] totalA, int j) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] - a[i][j] > b[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean swapTotalA(int[] totalA, int i, int j) {
		for (int k = 0; k < m; k++) {
			if (totalA[k] + a[k][j] - a[k][i] > b[k]) {
				return false;
			}
		}
		return true;
	}
	
	public double swapObj(int i, int j, ArrayList<Integer> curX, double oldObj) {
		oldObj = oldObj - this.getCi(i);
		oldObj = oldObj + this.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - this.getCij(i,xk);
				oldObj = oldObj + this.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - this.getDijk(i,xk,xl);
						oldObj = oldObj + this.getDijk(j,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	// Try to add a variable to the solution
	private double[] tryAdd(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int[] totalA) {
		double maxChange = 0;
		int maxI = -1;
		for(Integer i: r) {
			if (addTotalA(totalA,i)) {
				double obj = curObj + this.getCi(i);
				for (int j = 0; j < curX.size(); j++) {
					int xj = curX.get(j);
					obj += this.getCij(i,xj);
					for (int k = j+1; k < curX.size(); k++) {
						int xk = curX.get(k);
						obj += this.getDijk(i,xj,xk);
					}
				}
				double change = obj - curObj;
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		double[] result = {maxChange, maxI};
		return result;
	}

	private double[] trySub(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int[] totalA) {
		double maxChange = 0;
		int maxI = -1;
		for(Integer i: curX) {
			double obj = curObj - this.getCi(i);
			for (int j = 0; j < curX.size(); j++) {
				int xj = curX.get(j);
				obj -= this.getCij(i,xj);
				for (int k = j+1; k < curX.size(); k++) {
					int xk = curX.get(k);
					obj -= this.getDijk(i,xj,xk);
				}
			}
			double change = obj - curObj;
			if (change > maxChange) {
				maxChange = change;
				maxI = i;
			}
		}
		double[] result = {maxChange, maxI};
		return result;
	}

	public int getN() {
		return n;
	}
	
	public int getM() {
		return m;
	}

	public int getA(int i,int j) {
		return a[i][j];
	}

	public int getB(int i) {
		return b[i];
	}

	public int getCi(int i) {
		return ci[i];
	}

	public int getCij(int i, int j) {
		if (i == j) {
			return 0;
		} else if (i < j) {
			return cij[i][j-i];
		} else {
			return cij[j][i-j];
		}
	}

	public int getDijk(int i, int j, int k) {
		if (i == j || i == k || j == k)  {
			return 0;
		}
		if (i < j) {
			if (j < k) {
				return dijk[i][j-i][k-j];
			} else if (k < i) {
				return dijk[k][i-k][j-i];
			} else {
				return dijk[i][k-i][j-k];
			}
		} else if (j < i) {
			if (i < k) {
				return dijk[j][i-j][k-i];
			} else if (k < j) {
				return dijk[k][j-k][i-j];
			} else {
				return dijk[j][k-j][i-k];
			}
		} else {
			return 0;
		}
	}

	public double getRatio(int i) {
		return ratio[i];
	}

	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			n = scr.nextInt();
			m = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			scr.nextLine();
			b = readArr(scr);
			negCoef = scr.nextBoolean();
			density = scr.nextDouble();
			scr.nextLine();

			a = new int[m][];
			for (int i = 0; i < m; i++) {
				a[i] = readArr(scr);
			}

			ci = readArr(scr);

			cij = new int[n-1][];
			for(int i = 0; i < n-1; i++){
				cij[i] = readArr(scr);
			}

			dijk = new int[n-1][][];
			for(int i = 0; i < n-1; i++){
				dijk[i] = new int[n-i][];
				for (int j = i+1; j < n; j++) {
					dijk[i][j-i] = readArr(scr);
				}
			}

			tau = readArr(scr);

			ratio = new double[n];
			for (int i = 0; i < n; i++) {
				ratio[i] = scr.nextDouble();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

	private int[] readArr(Scanner scr) {
		String line = scr.nextLine().trim();
		String[] data = line.split(" ");
		int[] ret = new int[data.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = Integer.parseInt(data[i]);
		}
		return ret;
	}

	public void toFile(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(n + "\n");
			pw.write(m + "\n");
			pw.write(seed + "\n");
			writeArr(pw, b);
			pw.write(negCoef + "\n");
			pw.write(density + "\n");
			for (int i = 0; i < m; i++) {
				writeArr(pw, a[i]);
			}
			writeArr(pw, ci);
			for (int i = 0; i < cij.length; i++) {
				writeArr(pw, cij[i]);
			}
			for (int i = 0; i < dijk.length; i++) {
				for (int j = 1; j < dijk[i].length; j++) {
					writeArr(pw, dijk[i][j]);
				}
			}
			writeArr(pw, tau);
			for (int i = 0; i < n-1; i++) {
				pw.write(ratio[i] + " ");
			}
			pw.write(ratio[n-1] + "\n");

			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
		}
	}

	private void writeArr(PrintWriter pw, int[] arr) {
		for (int i = 0; i < arr.length-1; i++) {
			pw.write(arr[i] + " ");
		}
		pw.write(arr[arr.length-1] + "\n");
	}

	public static void main(String[] args) {
	}

}
