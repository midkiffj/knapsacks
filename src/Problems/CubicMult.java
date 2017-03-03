package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Cubic Multiple Knapsack Problem (CMKP)
 * - Problem generation
 * - Problem coefficient accessors
 * - Solution objective calculation
 * - File I/O
 * 
 * @author midkiffj
 */
public class CubicMult extends MultipleKnapsack {

	// CMKP Problem
	private int n;
	private int m;
	private Random rnd;
	private int seed;
	private boolean negCoef;
	private double density;

	// Coefficients
	private int[][] a;
	private int[] b;
	private int[] ci;
	private int[][] cij;
	private int[][][] dijk;

	// Mutation values
	private int[] tau;
	private double[] ratio;

	/**
	 * Initialize a problem from the file
	 * @param filename
	 */
	public CubicMult(String filename) {
		super();
		readFromFile(filename);
	}

	/**
	 * 
	 * Setup a problem with the specificiations
	 * 
	 * @param n - number of items
	 * @param m - number of knapsacks
	 * @param negCoef - allow negative coefficients
	 * @param seed - rnd seed
	 * @param density - probability of non-zero coefficients
	 */
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
		// a 	: [1,50]
		// ci   : negcoef ? [-100,100] : [0,100]
		// cij  : negcoef ? [-100,100] : [0,100]
		// dijk : negcoef ? [-100,100] : [0,100]
		// 
		// Note: Left-Upper triangular matrix stored
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

			// Update weights
			for (j = 0; j < m; j++) {
				a[j][i] = rnd.nextInt(50)+1;
				totalA[j] += a[j][i];
			}
			// Update item potential contribution
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
				// Update item potential contribution
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
					// Update item potential contribution
					tau[i] += dijk[i][j-i][k-j];
					tau[j] += dijk[i][j-i][k-j];
					tau[k] += dijk[i][j-i][k-j];
				}
			}
		}
		// Calculate ratio for each item as the average of the knapsack ratios
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
	 * Fill lists x and r with a randomly generated solution to the CMKP
	 * 
	 * @param x - items in the solution
	 * @param r - items outside of the solution
	 */
	public void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		// Reset lists
		r.clear();
		x.clear();

		// Remove all items from solution
		int[] totalAx = new int[m];
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}

		// Randomly add items until knapsacks full, the next item cannot be added, 
		//	all items added, or 30% random stop
		boolean done = false;
		while (totalAValid(totalAx) && !done && !r.isEmpty()) {
			int num = rnd.nextInt(r.size());
			i = r.get(num);
			// Add item if it fits
			if (addTotalA(totalAx,i)) {
				x.add(i);
				r.remove(num);
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
	 * Fill the provided lists with the solution.
	 * 
	 * @param x - items in the solution
	 * @param r - items outside of the solution
	 */
	public void genInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		// Clear Solution
		r.clear();
		x.clear();
		int[] totalAx = new int[m];
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}
		// Add maximum ratio items until none can be added
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
				addA(i,totalAx);
				inX[i] = true;
			}
		}

		// Update objective
		double curObj = getObj(x);

		// Check for Swaps and shifts
		boolean swapping = true;
		while (swapping) {
			// Check all swaps
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
			// Check for an improving add or removal
			double[] add = tryAdd(x, r, curObj, totalAx);
			double[] sub = trySub(x, curObj, totalAx);
			double addChange = add[0];
			double subChange = sub[0];
			// If addition is better than swap,
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				curObj = curObj + add[0];
				addA(addI,totalAx);
			} 
			// Else if, removal is better than swap
			else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				curObj = curObj + sub[0];
				removeA(subI,totalAx);
			} 
			// Else, attempt the swap
			else {
				// If no improving swap exists, stop
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					curObj = curObj + maxChange;
					removeA(maxI,totalAx);
					addA(maxJ,totalAx);
				}
			}
		}
	}
 
	
	/**
	 * Find the variable that most improves the objective when added
	 *
	 * @param curX - items in solution
	 * @param r - items outside solution
	 * @param curObj - current objective
	 * @param totalA - current weight of knapsack
	 * @return {change in objective,item to add} or {0,-1} if no improving shift found
	 */
	private double[] tryAdd(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int[] totalA) {
		double maxChange = 0;
		int maxI = -1;
		// Check all items
		for(Integer i: r) {
			// If knapsack feasible,
			if (addTotalA(totalA,i)) {
				// Calculate change in objective
				double obj = curObj + this.getCi(i);
				for (int j = 0; j < curX.size(); j++) {
					int xj = curX.get(j);
					obj += this.getCij(i,xj);
					for (int k = j+1; k < curX.size(); k++) {
						int xk = curX.get(k);
						obj += this.getDijk(i,xj,xk);
					}
				}
				// Update the best change in objective
				double change = obj - curObj;
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		// Return the best change
		double[] result = {maxChange, maxI};
		return result;
	}


	/**
	 * Find the variable that most improves the objective when removed
	 *
	 * @param curX - items in solution
	 * @param curObj - current objective
	 * @param totalA - current weight of knapsack
	 * @return {change in objective,item to add} or {0,-1} if no improving shift found
	 */
	private double[] trySub(ArrayList<Integer> curX, double curObj, int[] totalA) {
		double maxChange = 0;
		int maxI = -1;
		// Check all removals
		for(Integer i: curX) {
			// Calculate the new objective
			double obj = curObj - this.getCi(i);
			for (int j = 0; j < curX.size(); j++) {
				int xj = curX.get(j);
				obj -= this.getCij(i,xj);
				for (int k = j+1; k < curX.size(); k++) {
					int xk = curX.get(k);
					obj -= this.getDijk(i,xj,xk);
				}
			}
			// Update the best change in objective found
			double change = obj - curObj;
			if (change > maxChange) {
				maxChange = change;
				maxI = i;
			}
		}
		// Return the best change (or {0,-1})
		double[] result = {maxChange, maxI};
		return result;
	}

	
	/*
	 * Private implementations of multiple knapsack interaction 
	 * 	for use in generating solutions.
	 */
	
	private void addA(int i, int[] totalA) {
		for (int j = 0; j < m; j++) {
			totalA[j] += a[j][i];
		}
	}

	private void removeA(int i, int[] totalA) {
		for (int j = 0; j < m; j++) {
			totalA[j] -= a[j][i];
		}
	}

	private boolean totalAValid(int[] totalA) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] > b[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean addTotalA(int[] totalA, int j) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] + a[i][j] > b[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean swapTotalA(int[] totalA, int i, int j) {
		for (int k = 0; k < m; k++) {
			if (totalA[k] + a[k][j] - a[k][i] > b[k]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Calculate the new objective if 
	 * 	item i is removed and item j is added to the solution.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param curX - current solution
	 * @param oldObj - current objective
	 * @return new objective value
	 */
	private double swapObj(int i, int j, ArrayList<Integer> curX, double oldObj) {
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

	/**
	 * Calculate the objective value with the given x values.
	 * 
	 * @param x - solution list
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

	/**
	 * Setup a CMKP from the given file. 
	 * It is assumed the file was generated with the toFile() method.
	 * 
	 * @param filename to be read
	 */
	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			// Setup specifications
			n = scr.nextInt();
			m = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			scr.nextLine();
			b = readArr(scr);
			negCoef = scr.nextBoolean();
			density = scr.nextDouble();
			scr.nextLine();

			// Coefficients
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

			// Mutation values
			tau = readArr(scr);
			ratio = new double[n];
			for (int i = 0; i < n; i++) {
				ratio[i] = scr.nextDouble();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

	/**
	 * Read in an int array of coefficients
	 * 
	 * @param scr - Scanner to read form
	 * @return 
	 */
	private int[] readArr(Scanner scr) {
		String line = scr.nextLine().trim();
		String[] data = line.split(" ");
		int[] ret = new int[data.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = Integer.parseInt(data[i]);
		}
		return ret;
	}

	/**
	 * Write the problem to the specified file.
	 * 
	 * @param filename - to write to
	 */
	public void toFile(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			// Setup values
			pw.write(n + "\n");
			pw.write(m + "\n");
			pw.write(seed + "\n");
			writeArr(pw, b);
			pw.write(negCoef + "\n");
			pw.write(density + "\n");
			
			// Coefficients
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
			
			// Mutation values
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

	/**
	 * Write the given coefficient array with the writer
	 * 
	 * @param pw - writer to use
	 * @param arr - array to write
	 */
	private void writeArr(PrintWriter pw, int[] arr) {
		for (int i = 0; i < arr.length-1; i++) {
			pw.write(arr[i] + " ");
		}
		pw.write(arr[arr.length-1] + "\n");
	}
}
