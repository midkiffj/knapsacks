package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import ExactMethods.Knapsack_Frac;

/**
 * Max Probability Knapsack Problem (MPP)
 * - Problem generation
 * - Problem coefficient accessors
 * - Solution objective calculation
 * - File I/O
 * 
 * @author midkiffj
 */
public class MaxProbability extends Knapsack {

	// Setup values
	private int n;
	private Random rnd;
	private int seed;
	private boolean negCoef;

	// Coefficients
	private double t;
	private int b;
	private int[] a;
	private int[] u;
	private int[] s;

	// Obj values
	private double num;
	private double den;

	// MP vars
	private int P;
	private int K;

	// Mutation values
	private double[] tau;
	private double[] ratio;

	/**
	 * Setup a MPP from the specified file
	 * 
	 * @param filename
	 */
	public MaxProbability(String filename) {
		super();
		readFromFile(filename);
	}

	/**
	 * Setup a MPP with the given values
	 * 
	 * @param n - number of items
	 * @param negCoef - allow negative coefficients
	 * @param seed - rnd seed
	 * @param K - probability for picking profit (t)
	 * @param P - probability for picking capacity (b)
	 */
	public MaxProbability(int n, boolean negCoef, int seed, int K, int P) {
		super();
		this.n = n;
		this.negCoef = negCoef;
		this.rnd = new Random(seed);
		this.seed = seed;
		this.K = K;
		this.P = P;
		setup();
	}

	/**
	 * Setup a MPP problem as specified in A. Billionnet (2004)
	 */
	private void setup() {
		// Initialize arrays
		a = new int[n];
		u = new int[n];
		s = new int[n];
		int totalA = 0;
		tau = new double[n];
		ratio = new double[n];
		// Generate coefficients
		for (int i = 0; i < n; i++) {
			a[i] = rnd.nextInt(100) + 1;
			u[i] = rnd.nextInt(100) + 1;
			s[i] = rnd.nextInt(10000) + 1;
			totalA += a[i];
			// Update potential contribution
			tau[i] = (double)(u[i]*1000) / s[i];
			ratio[i] = tau[i] / a[i];
		}
		// b = P% of totalA
		b = (int) Math.floor(0.01*P*totalA);
		// Solve max{u*x : Ax <= b, x binary}
		Knapsack_Frac k = new Knapsack_Frac(a,b,u,true);
		long umax = k.getBestObj();
		// t = K% of umax
		t = 0.01*K*umax;
	}

	@Override
	/**
	 * Generate a solution by improving the solution vector x=max{u*x : Ax <= b}
	 * Fill the provided lists with the solution.
	 * 
	 * @param x - items in the solution
	 * @param r - items outside of the solution
	 */
	public void genInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		// Clear lists
		r.clear();
		x.clear();
		int totalAx = 0;
		int totalUx = 0;
		// Solve for max{u*x : Ax <= b, x free}
		Knapsack_Frac k = new Knapsack_Frac(a,b,u,false);
		boolean[] uMaxXVals = k.getXVals();
		for (int i = 0; i < n; i++) {
			if (uMaxXVals[i]) {
				x.add(i);
				totalAx = totalAx + a[i];
				totalUx = totalUx + u[i];
			} else {
				r.add(i);
			}
		}
		// Update objective
		double curObj = getObj(x);
		num = getNum();
		den = getDen();

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
					if (a[xj]-a[xi] <= b - totalAx && u[xj]-u[xi] >= t - totalUx) {
						// Calculate new objective
						double newObj = swapObj(xi, xj, x, curObj);
						double change = newObj - curObj;
						// Update best swap
						if (change > maxChange) {
							maxI = xi;
							maxJ = xj;
							maxChange = change;
						}
					}
				}
			}
			double[] add = tryAdd(totalAx,r,curObj);
			double[] sub = trySub(totalUx,x,curObj);
			double addChange = add[0];
			double subChange = sub[0];
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				totalAx = totalAx + a[addI];
				totalUx = totalUx + u[addI];
				num = num + u[addI];
				den = den + s[addI];
				curObj = (num*num)/den;
			} else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				totalAx = totalAx - a[subI];
				totalUx = totalUx - u[subI];
				num = num - u[subI];
				den = den - s[subI];
				curObj = (num*num)/den;
			} else {
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					totalAx = totalAx + a[maxJ] - a[maxI];
					totalUx = totalUx + u[maxJ] - u[maxI];
					num = num + u[maxJ] - u[maxI];
					den = den + s[maxJ] - s[maxI];
					curObj = (num*num)/den;
				}
			}
		}
	}

	/**
	 * Find the variable that most improves the objective when removed
	 *
	 * @param x - items in the solution
	 * @param curObj - the current objective to improve upon
	 * @return {change in objective,item to add} or {0,-1} if no improving shift found
	 */
	private double[] trySub(int totalU, ArrayList<Integer> x, double curObj) {
		double maxChange = 0;
		int minI = -1;
		// Check all removals
		for (Integer i: x) {
			if (totalU - u[i] >= t) {
				// Calculate the change in objective
				double newObj = subObj(i, num, den);
				double change = newObj - curObj;
				// Update best change
				if (change < maxChange) {
					maxChange = change;
					minI = i;
				}
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
	 * Generate a random, feasible solution to the Max Probability problem
	 *      and fill the given lists with the solution.
	 * Ensures:
	 * 		sum(a_i*x_i) <= b (knapsack)
	 * 		sum(u_i*x_i) >= t (profit)
	 * 
	 * @param x solution variables
	 * @param r unused variables
	 */
	public void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		boolean valid = false;
		while (!valid) {
			// Solution lists
			// x - variables included in solution
			// r - variables not included in solution
			x.clear();
			r.clear();

			int totalAx = 0; // sum(a_i*x_i)
			int totalUx = 0; // sum(u_i*x_i)

			// Start with all variables not in the solution
			for (int j = 0; j < n; j++) {
				r.add(j);
			}

			// Randomly add variables to the solution 
			//	until sum(u_i*x_i) >= t
			while (totalUx < t) {
				// Update lists
				int k = r.remove(rnd.nextInt(r.size()));
				x.add(k);
				// Update sums
				totalUx += u[k];
				totalAx += a[k];
			}

			// Perform swaps and shifts until knapsack feasible
			//  (totalAx <= b)
			int swaps = 0;
			long start = System.nanoTime();
			long end = start;
			while (totalAx > b && (double)(end-start)/60000000000L < 0.08) {
				// Attempt to find a random swap that reduces totalAx
				int i = rnd.nextInt(x.size());
				int j = rnd.nextInt(r.size());
				// Variables to swap
				int xi = x.get(i);
				int rj = r.get(j);
				int count = 0;
				// Change random numbers until a_xi > a_rj
				//	 so that totalAx reduced
				while(a[rj] >= a[xi] && count < n/2) {
					i = rnd.nextInt(x.size());
					j = rnd.nextInt(r.size());
					xi = x.get(i);
					rj = r.get(j);
					count++;
				}
				// Check swap for reduced totalAx and profit feasibility 
				if (a[rj] < a[xi] && totalUx - u[xi] + u[rj] > t) {
					// Update solution lists
					x.remove(i);
					r.add(xi);
					r.remove(j);
					x.add(rj);
					// Update sums
					totalUx = totalUx + u[rj] - u[xi];
					totalAx = totalAx + a[rj] - a[xi];
				}
				// Increment swaps
				swaps++;

				// After a number of attempted swaps, try a shift
				if (totalAx > b && swaps > 10) {
					// Attempt a substitution: 
					//	sub = {index, change in objective}
					int subI = trySub(totalUx,x);
					// If a variable can be removed
					if (subI != -1) {
						// Remove variable from solution and update sums
						x.remove(Integer.valueOf(subI));
						r.add(subI);
						totalAx -= a[subI];
						totalUx -= u[subI];
					} 
					// Else, could not remove a variable, 
					//	so add a random variable to the solution
					else {
						rj = r.remove(rnd.nextInt(r.size()));
						x.add(Integer.valueOf(rj));
						totalAx += a[rj];
						totalUx += u[rj];
					}
					swaps = 0;
				}
				
				end = System.nanoTime();
			}
			if (totalAx <= b && totalUx >= t) {
				valid = true;
			} else {
				System.err.println("Restarting rnd gen");
			}
		}
	}

	/**
	 * Find the variable that most decreases the knapsack weight when removed
	 *
	 * @parm totalU - current profit
	 * @param x - items in the solution
	 * @return item to remove from the solution
	 */
	private int trySub(int totalU, ArrayList<Integer> x) {
		int maxA = -1;
		int subI = -1;
		// Check all removals
		for (Integer i: x) {
			if (totalU - u[i] >= t) {
				// Update best weight
				if (a[i] > maxA) {
					maxA = a[i];
					subI = i;
				}
			}
		}
		// Return the highest weight removal
		return subI;
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
	 * @param num - current numerator value
	 * @param den - current denominator value
	 * @return the new objective
	 */
	private double subObj(int i, double num,
			double den) {
		num -= u[i];
		den -= s[i];
		return (num*num)/den;
	}

	/**
	 * Calculate the objective if i is added. 
	 * 	Calculation done without changes to num/den.
	 * 
	 * @param i - item to be added
	 * @param num - current numerator value
	 * @param den - current denominator value
	 * @return the new objective
	 */
	private double addObj(int i, double num,
			double den) {
		num += u[i];
		den += s[i];
		return (num*num)/den;
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
		if (x.isEmpty()) {
			return 0;
		}
		// Update class num/den
		if (setNumDen) {
			num = -1*t;
			den = 0;

			for (int i: x) {
				num += u[i];
				den += s[i];
			}

			return (num*num)/den;
		} 
		// Use local scope num/den
		else {
			double num = -1*t;
			double den = 0;

			for (int i: x) {
				num += u[i];
				den += s[i];
			}

			return (num*num)/den;
		}
	}

	public double getNum() {
		return num;
	}

	public double getDen() {
		return den;
	}

	public int getN() {
		return n;
	}

	public int getB() {
		return b;
	}

	public double getT() {
		return t;
	}

	public int getA(int i) {
		return a[i];
	}

	public int getU(int i) {
		return u[i];
	}

	public int getS(int i) {
		return s[i];
	}

	public double getRatio(int i) {
		return ratio[i];
	}

	/**
	 * Setup a Max Probability from the given file. 
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
			// Coefficients
			b = scr.nextInt();
			t = scr.nextDouble();
			scr.nextLine();

			a = readArr(scr);
			u = readArr(scr);
			s = readArr(scr);

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
			tau[i] = (double)(u[i]*1000) / s[i];
			ratio[i] = tau[i]/a[i];
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
			// Coefficients
			pw.write(b + "\n");
			pw.write(t + "\n");
			writeArr(pw, a);
			writeArr(pw, u);
			writeArr(pw, s);

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
