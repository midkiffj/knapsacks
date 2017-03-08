package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Fractional Knapsack Problem
 * - Problem generation
 * - Problem coefficient accessors
 * - Solution objective calculation
 * - File I/O
 * 
 * @author midkiffj
 */
public class Fractional extends Knapsack {

	// Setup
	private int n;
	private int m;
	private Random rnd;
	private int seed;
	private boolean negCoef;

	// Coefficients
	private int b;
	private int[] a;
	private int[][] c;
	private int[][] d;
	private int[] numConst;
	private int[] denConst;

	// Obj values
	private long[] num;
	private long[] den;

	// Mutation values
	private double[] tau;
	private double[] ratio;


	/**
	 * Read the problem from the specified file
	 * 
	 * @param filename - file to read
	 */
	public Fractional(String filename) {
		super();
		readFromFile(filename);
	}

	/**
	 * Setup a fractional with the specifications
	 * 
	 * @param n - number of items
	 * @param m - number of fractions
	 * @param negCoef - allow negative coefficients
	 * @param seed - rnd seed
	 */
	public Fractional(int n, int m, boolean negCoef, int seed, boolean largeNum, boolean largeDen) {
		super();
		this.n = n;
		this.m = m;
		this.negCoef = negCoef;
		this.rnd = new Random(seed);
		this.seed = seed;
		setup(largeNum,largeDen);
	}

	/**
	 * Intialize the objective and constraint coefficients
	 */
	private void setup(boolean largeNum, boolean largeDen) {
		// Initialize all arrays
		a = new int[n];
		c = new int[m][n];
		d = new int[m][n];
		numConst = new int[m];
		denConst = new int[m];
		tau = new double[n];
		ratio = new double[n];

		// For each fraction,
		for (int i = 0; i < m; i++) {
			// Generate numerator and denominator coefficients
			if (largeNum) {
				numConst[i] = rnd.nextInt(10000) + 1;
			} else {
				numConst[i] = rnd.nextInt(10) + 1;
			}
			if (largeDen) {
				denConst[i] = rnd.nextInt(10000) + 1;
			} else {
				denConst[i] = rnd.nextInt(10) + 1;
			}
			int totalC = numConst[i];
			int totalD = denConst[i];
			// Enforce that sum(cij) != 0 and sum(dij) != 0
			boolean sat = false;
			while (!sat) {
				for (int j = 0; j < n; j++) {
					if (largeNum) {
						c[i][j] = rnd.nextInt(10000)+1;
					} else {
						c[i][j] = rnd.nextInt(10)+1;
					}
					if (largeDen) {
						d[i][j] = rnd.nextInt(10000)+1;
					} else {
						d[i][j] = rnd.nextInt(10)+1;
					}
					if (negCoef) {
						if (rnd.nextBoolean()) {
							c[i][j] = c[i][j]*-1;
						}
						if (rnd.nextBoolean()) {
							d[i][j] = d[i][j]*-1;
						}
					}
					totalC += c[i][j];
					totalD += d[i][j];
				}
				if (totalC != 0 && totalD != 0) {
					sat = true;
				} else {
					totalC = numConst[i];
					totalD = denConst[i];
				}
			}

		}
		// Initialize weights and potential contribution to calculate ratios
		for (int i = 0; i < n; i++) {
			a[i] = rnd.nextInt(9) + 1;
			for (int j = 0; j < m; j++) {
				tau[i] += (double)(c[j][i]*1000)/d[j][i];
			}
			ratio[i] = tau[i] / a[i];
		}
		// Static b
		b = 100;
	}

	@Override
	/**
	 * Generate a solution by adding x's until knapsack full
	 * and update the current objective value. 
	 * Fill the provided lists with the solution.
	 * 
	 * @param x - items in the solution
	 * @param r - items outside of the solution
	 */
	public void genInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		// Clear the solution lists
		r.clear();
		x.clear();
		int totalAx = 0;
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}
		// Add Max-ratio items until none can be added
		boolean[] inX = new boolean[n];
		boolean done = false;
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

		// Update objective
		double curObj = getObj(x);

		// Check for Swaps and shifts
		boolean swapping = true;
		while (swapping) {
			int maxI = -1;
			int maxJ = -1;
			double maxChange = 0;
			// Check all swaps
			for(Integer xi: x) {
				for(Integer xj: r) {
					// Check for knapsack feasibility
					if (a[xj]-a[xi] <= b - totalAx) {
						// Calculate new objective
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
			// Get most improving shifts
			double[] add = tryAdd(totalAx,r,curObj);
			double[] sub = trySub(x,curObj);
			double addChange = add[0];
			double subChange = sub[0];

			// If addition is better than swap
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				curObj = getObj(x);
				totalAx = totalAx + a[addI];
			}
			// Else if removal is better than swap
			else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				curObj = getObj(x);
				totalAx = totalAx - a[subI];
			} 
			// Else, perform the swap
			else {
				// If no improving swap exists, stop
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					curObj = getObj(x);
					totalAx = totalAx + a[maxJ] - a[maxI];
				}
			}
		}
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
	private double swapObj(int i, int j, ArrayList<Integer> x, double oldObj) {
		ArrayList<Integer> newX = new ArrayList<Integer>(x);
		newX.remove(Integer.valueOf(i));
		newX.add(j);
		return getObj(newX, false);
	}

	/**
	 * Calculate the objective if i is removed. 
	 * 	Calculation done without changes to num/den.
	 * 
	 * @param i - item to be removed
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the new objective
	 */
	private double subObj(int i, long[] num, long[] den) {
		double obj = 0;
		// For each fraction,
		for (int j = 0; j < m; j++) {
			// Check for a zero in the denominator
			if (den[j]-d[j][i] == 0) {
				return -1*Double.MAX_VALUE;
			}
			// Otherwise, update the objective
			obj += (double)(num[j]-c[j][i])/(den[j]-d[j][i]);
		}
		return obj;
	}

	/**
	 * Calculate the objective if i is added. 
	 * 	Calculation done without changes to num/den.
	 * 
	 * @param i - item to be added
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the new objective
	 */
	private double addObj(int i, long[] num, long[] den) {
		double obj = 0;
		// For each fraction,
		for (int j = 0; j < m; j++) {
			// Check for a zero in the denominator
			if (den[j]+d[j][i] == 0) {
				return -1*Double.MAX_VALUE;
			}
			// Otherwise, update the objective
			obj += (double)(num[j]+c[j][i])/(den[j]+d[j][i]);
		}

		return obj;
	}

	/**
	 * Find the variable that most improves the objective when removed
	 *
	 * @param x - items in the solution
	 * @param curObj - the current objective to improve upon
	 * @return {change in objective,item to add} or {0,-1} if no improving shift found
	 */
	private double[] trySub(ArrayList<Integer> x, double curObj) {
		double maxChange = 0;
		int minI = -1;
		// Check all removals
		for (Integer i: x) {
			// Calculate the change in objective
			double newObj = subObj(i, num, den);
			double change = newObj - curObj;
			// Update best change
			if (change < maxChange) {
				maxChange = change;
				minI = i;
			}
		}
		// Return the best improving removal
		double[] success = {maxChange, minI};
		return success;
	}

	/**
	 * Find the variable that most improves the objective when added
	 *
	 * @param totalA - current weight of knapsack
	 * @param r - items outside solution
	 * @param curObj - the current objective to improve upon
	 * @return {change in objective,item to add} or {0,-1} if no improving shift found
	 */
	private double[] tryAdd(int totalA, ArrayList<Integer> r, double curObj) {
		double maxChange = 0;
		int maxI = -1;
		// Check all additions
		for (Integer i: r) {
			if (totalA + a[i] <= b) {
				// Calculate the change in objective
				double newObj = addObj(i, num, den);
				double change = newObj - curObj;
				// Update best change
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		// Return the best improving addition
		double[] add = {maxChange, maxI};
		return add;
	}

	@Override
	/**
	 * Fill lists x and r with a randomly generated solution to the CMKP
	 * 
	 * @param x - items in the solution
	 * @param r - items outside of the solution
	 */
	public void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		r.clear();
		x.clear();
		int totalAx = 0;
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}
		boolean done = false;
		while (totalAx <= b && !done && !r.isEmpty()) {
			int num = rnd.nextInt(r.size());
			i = r.get(num);
			if (totalAx + a[i] <= b) {
				x.add(i);
				r.remove(Integer.valueOf(i));
				totalAx += a[i];
			} else {
				done = true;
			}
			if (rnd.nextDouble() > 0.7) {
				done = true;
			}
		}
	}

	@Override
	/**
	 * Calculate the objective value with the given x values.
	 * 
	 * @param x - solution list
	 */
	public double getObj(ArrayList<Integer> x) {
		return getObj(x,true);
	}

	/**
	 * Calculate the objective value with the given x values.
	 * 
	 * @param x - solution list
	 * @param setNumDen - update the values of fields num/den
	 */
	public double getObj(ArrayList<Integer> x, boolean setNumDen) {
		// Update class num/den arrays
		if (setNumDen) {
			num = new long[m];
			den = new long[m];
			double newObj = 0;
			// For each fraction,
			for (int i = 0; i < m; i++) {
				// Calculate the value of the numerator and denominator
				num[i] += numConst[i];
				den[i] += denConst[i];
				for (int j: x) {
					num[i] += c[i][j];
					den[i] += d[i][j];
				}
				if (den[i] == 0) {
					return -1*Double.MAX_VALUE;
				}
				// Update objective
				newObj += (double)(num[i])/den[i];
			}
			return newObj;
		} else {
			long[] num = new long[m];
			long[] den = new long[m];
			double newObj = 0;
			// For each fraction,
			for (int i = 0; i < m; i++) {
				// Calculate the value of the numerator and denominator
				num[i] += numConst[i];
				den[i] += denConst[i];

				for (int j: x) {
					num[i] += c[i][j];
					den[i] += d[i][j];
				}
				if (den[i] == 0) {
					return -1*Double.MAX_VALUE;
				}
				// Update objective
				newObj += (double)(num[i])/den[i];
			}
			return newObj;
		}
	}

	public long[] getNum() {
		return num;
	}

	public long[] getDen() {
		return den;
	}

	@Override
	public int getN() {
		return n;
	}

	public int getM() {
		return m;
	}

	public int getB() {
		return b;
	}

	public int getA(int i) {
		return a[i];
	}

	public int getC(int m,int i) {
		return c[m][i];
	}

	public int getD(int m, int i) {
		return d[m][i];
	}

	public int getNumConst(int i) {
		return numConst[i];
	}

	public int getDenConst(int i) {
		return denConst[i];
	}

	public double getRatio(int i) {
		return ratio[i];
	}

	/**
	 * Setup a Fractional from the given file. 
	 * It is assumed the file was generated with the toFile() method.
	 * 
	 * @param filename to be read
	 */
	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));
			// Setup values
			n = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			negCoef = scr.nextBoolean();
			m = scr.nextInt();

			// Coefficients
			b = scr.nextInt();
			scr.nextLine();
			a = readArr(scr);
			numConst = readArr(scr);
			denConst = readArr(scr);
			c = new int[m][n];
			d = new int[m][n];
			for (int i = 0; i < m; i++) {
				c[i] = readArr(scr);
			}
			for (int i = 0; i < m; i++) {
				d[i] = readArr(scr);
			}

		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
		calcTauRatio();
	}

	/**
	 * Sub-method used to calculate the mutation values.
	 */
	private void calcTauRatio() {
		tau = new double[n];
		ratio = new double[n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				tau[i] += (double)(c[j][i]*1000)/d[j][i];
			}
			ratio[i] = tau[i] / a[i];
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
			pw.write(seed + "\n");
			pw.write(negCoef + "\n");
			pw.write(m + "\n");

			// Coefficients
			pw.write(b + "\n");
			writeArr(pw, a);
			writeArr(pw,numConst);
			writeArr(pw,denConst);
			for (int i = 0; i < m; i++) {
				writeArr(pw, c[i]);
			}
			for (int i = 0; i < m; i++) {
				writeArr(pw, d[i]);
			}

			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
			System.err.println(e.getMessage());
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
