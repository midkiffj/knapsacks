package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Cubic Knapsack Problem
 * - Problem generation
 * - Problem coefficient accessors
 * - Solution objective calculation
 * - File I/O
 * 
 * @author midkiffj
 */
public class Cubic extends Knapsack {
	
	// Cubic problem
	private int n;
	private Random rnd;
	private int seed;
	private boolean negCoef;
	private double density;

	// Coefficients
	private int[] a;
	private int b;
	private int[] ci;
	private int[][] cij;
	private int[][][] dijk;

	// Cubic Mutation (ratio)
	private int[] tau;
	private double[] ratio;

	/**
	 * Constructor to read from file
	 * 
	 * @param filename - to read
	 */
	public Cubic(String filename) {
		super();
		readFromFile(filename);
	}

	/**
	 * Constructor to initialize problem setup
	 * 
	 * @param n - number of items
	 * @param negCoef - allow negative coefficeints
	 * @param seed - rnd seed
	 * @param density - probability of non-zero coefficients
	 */
	public Cubic(int n, boolean negCoef, int seed, double density) {
		super();
		this.n = n;
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
		int totalA = 0;
		a = new int[n];
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
			
			// Update total weight
			a[i] = rnd.nextInt(50)+1;
			totalA += a[i];
			
			// Update tau (item contribution)
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
				// Update tau (item contribution)
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
					// Update tau (item contribution)
					tau[i] += dijk[i][j-i][k-j];
					tau[j] += dijk[i][j-i][k-j];
					tau[k] += dijk[i][j-i][k-j];
				}
			}
		}

		// Calculate ratio for each item
		// (potential/weight)
		for (i = 0; i < n; i++) {
			ratio[i] = (double)(tau[i])/a[i];
		}

		// b : [50,sum(a)]
		b = rnd.nextInt(totalA-50+1)+50;
	}

	/**
	 * Randomly generate a solution to the cubic and fill the given lists.
	 * 
	 * @param x - variables in solution
	 * @param r - variables not in solution
	 */
	public void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		// Reset lists
		r.clear();
		x.clear();
		
		// Remove all items from solution
		int totalAx = 0;
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}
		
		// Randomly add items until knapsack full, the next item cannot be added, 
		//	all items added, or 30% random stop
		boolean done = false;
		while (totalAx <= b && !done && !r.isEmpty()) {
			int num = rnd.nextInt(r.size());
			i = r.get(num);
			// Add item if it fits
			if (totalAx + a[i] <= b) {
				x.add(i);
				r.remove(num);
				totalAx += a[i];
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
	 * Fill the given lists with the solution.
	 * 
	 * @param x - items in the solution
	 * @param r - items not in the solution
	 */
	public void genInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		// Clear solution lists
		r.clear();
		x.clear();
		// Start with all items outside the solution
		int totalAx = 0;
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}
		boolean[] inX = new boolean[n];
		boolean done = false;
		// Add the maximum ratio item that fits in the knapsack until no item can be added
		while (totalAx <= b && !done) {
			double maxRatio = -1*Double.MAX_VALUE;
			i = -1;
			for (int j = 0; j < r.size(); j++) {
				int xj = r.get(j);
				if (!inX[xj] && ratio[xj] >= maxRatio && totalAx + a[xj] <= b) {
					i = xj;
					maxRatio = ratio[xj];
				}
			}
			if (i == -1) {
				done = true;
			} else {
				x.add(i);
				r.remove(Integer.valueOf(i));
				totalAx += a[i];
				inX[i] = true;
			}
		}

		double curObj = getObj(x);

		// Check for Swaps and shifts
		boolean swapping = true;
		if (n > 200) {
			swapping = false;
		}
		while (swapping) {
			// Find the best swap
			int maxI = -1;
			int maxJ = -1;
			double maxChange = 0;
			for(Integer xi: x) {
				for(Integer xj: r) {
					// Check for knapsack feasibility
					if (a[xj]-a[xi] <= b - totalAx) {
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
			// Determine the best shift
			double[] add = tryAdd(x, r, curObj, totalAx);
			double[] sub = trySub(x, curObj, totalAx);
			double addChange = add[0];
			double subChange = sub[0];
			// If an addition is better than a swap, do the addition
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				curObj = curObj + add[0];
				totalAx = totalAx + a[addI];
			} 
			// If a removal is better than a swap, do the removal
			else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				curObj = curObj + sub[0];
				totalAx = totalAx - a[subI];
			} 
			// Else,
			else {
				// If no swaps exist, stop
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} 
				// Otherwise, perform the swap
				else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					curObj = curObj + maxChange;
					totalAx = totalAx + a[maxJ] - a[maxI];
				}
			}
		}
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
	
	/**
	 * Calculate the objective of curX 
	 * 	if item i is removed and item j is added
	 * 
	 * @param i - item to be removed
	 * @param j - item to be added
	 * @param curX - current solution
	 * @param oldObj - current solution objective value
	 * @return the new objective if i and j swapped
	 */
	private double swapObj(int i, int j, ArrayList<Integer> curX, double oldObj) {
		// Calculate change in ci's
		oldObj = oldObj - this.getCi(i);
		oldObj = oldObj + this.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				// Calculate change in Cij's
				oldObj = oldObj - this.getCij(i,xk);
				oldObj = oldObj + this.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						// Calculate change in Dijk's
						oldObj = oldObj - this.getDijk(i,xk,xl);
						oldObj = oldObj + this.getDijk(j,xk,xl);
					}
				}
			}
		}
		return oldObj;
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
	private double[] tryAdd(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int totalA) {
		double maxChange = 0;
		int maxI = -1;
		// For each item
		for(Integer i: r) {
			// Check knapsack feasibility
			if (totalA + a[i] <= b) {
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
				// Update best change found
				double change = obj - curObj;
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		// Return the best change found
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
	private double[] trySub(ArrayList<Integer> curX, double curObj, int totalA) {
		double maxChange = 0;
		int maxI = -1;
		// For each item,
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
			// Update the best change in objective
			double change = obj - curObj;
			if (change > maxChange) {
				maxChange = change;
				maxI = i;
			}
		}
		// Return the best change in objective
		double[] result = {maxChange, maxI};
		return result;
	}

	public int getN() {
		return n;
	}

	public int getA(int i) {
		return a[i];
	}

	public int getB() {
		return b;
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
	 * Setup a Cubic from the given file. 
	 * It is assumed the file was generated with the toFile() method.
	 * 
	 * @param filename to be read
	 */
	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			// Read setup variables
			n = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			b = scr.nextInt();
			negCoef = scr.nextBoolean();
			density = scr.nextDouble();
			scr.nextLine();

			// Read Coefficient matrices
			a = readArr(scr);

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

			// Read mutation (ratio) values
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
			// Write setup vars
			pw.write(n + "\n");
			pw.write(seed + "\n");
			pw.write(b + "\n");
			pw.write(negCoef + "\n");
			pw.write(density + "\n");
			// Write coefficient matrices
			writeArr(pw, a);
			writeArr(pw, ci);
			for (int i = 0; i < cij.length; i++) {
				writeArr(pw, cij[i]);
			}
			for (int i = 0; i < dijk.length; i++) {
				for (int j = 1; j < dijk[i].length; j++) {
					writeArr(pw, dijk[i][j]);
				}
			}
			// Write mutation (ratio) values
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
	
	public static void main(String[] args) {
	}

}
