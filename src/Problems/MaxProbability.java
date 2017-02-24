package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import ExactMethods.Knapsack;
import Solutions.MaxProbabilitySol;

public class MaxProbability extends Problem {

	// Setup vars
	private int n;
	private Random rnd;
	private int seed;
	private boolean negCoef;

	// Coefficient vars
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
	private double[] tau;
	private double[] ratio;
	// Umax vars
	private boolean[] uMaxXVals;


	public MaxProbability(String filename) {
		super();
		readFromFile(filename);
	}

	public MaxProbability(int n, boolean negCoef) {
		this(n,negCoef,1234);
	}

	public MaxProbability(int n, boolean negCoef, int seed) {
		super();
		this.n = n;
		this.negCoef = negCoef;
		this.rnd = new Random(seed);
		this.seed = seed;
		int[] possibleK = {65, 75, 85, 95};
		int[] possibleP = {5, 10, 30, 50, 75};
		this.K = possibleK[rnd.nextInt(4)];
		this.P = possibleP[rnd.nextInt(5)];
		setup();
	}

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

	private void setup() {
		a = new int[n];
		u = new int[n];
		s = new int[n];
		int totalA = 0;
		tau = new double[n];
		ratio = new double[n];
		for (int i = 0; i < n; i++) {
			a[i] = rnd.nextInt(100) + 1;
			u[i] = rnd.nextInt(100) + 1;
			s[i] = rnd.nextInt(10000) + 1;
			totalA += a[i];
			tau[i] = (double)(u[i]) / s[i];
			ratio[i] = tau[i] / a[i];
		}
		b = (int) Math.floor(0.01*P*totalA);
		Knapsack k = new Knapsack(a,b,u,true);
		long umax = k.getBestObj();
		t = 0.01*K*umax;

		uMaxXVals = k.getXVals();
		MaxProbabilitySol mps = new MaxProbabilitySol(uMaxXVals);
		System.out.println("Umax Obj: " + mps.getObj());
		System.out.println("Umax Validity: " + mps.getValid());
	}

	@Override
	public void genInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		r.clear();
		x.clear();
		int totalAx = 0;
		int totalUx = 0;
		Knapsack k = new Knapsack(a,b,u,false);
		uMaxXVals = k.getXVals();
		for (int i = 0; i < n; i++) {
			if (uMaxXVals[i]) {
				x.add(i);
				totalAx = totalAx + a[i];
				totalUx = totalUx + u[i];
			} else {
				r.add(i);
			}
		}
		double curObj = getObj(x);

		// Check for Swaps and shifts
		boolean swapping = true;
		int swaps = 0;
		while (swapping) {
			//		while (swapping) {
			int maxI = -1;
			int maxJ = -1;
			double maxChange = 0;
			for(Integer xi: x) {
				for(Integer xj: r) {
					// Check for knapsack feasibility
					if (a[xj]-a[xi] <= b - totalAx && u[xj]-u[xi] >= t - totalUx) {
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
			double[] add = tryAdd(totalAx,x,r);
			double[] sub = trySub(totalUx,x,true);
			double addObj = add[1];
			double subObj = sub[1];
			if (addObj-curObj > maxChange) {
				int addI = (int)add[0];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				curObj = getObj(x);
				totalAx = totalAx + a[addI];
				totalUx = totalUx + u[addI];
			} else if (subObj-curObj > maxChange) {
				int subI = (int)sub[0];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				curObj = getObj(x);
				totalAx = totalAx - a[subI];
				totalUx = totalUx - u[subI];
			} else {
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					curObj = getObj(x);
					totalAx = totalAx + a[maxJ] - a[maxI];
					totalUx = totalUx + u[maxJ] - u[maxI];
				}
			}
		}

		System.out.println("Generated Incumbent: " + curObj);
		Collections.sort(x);
		System.out.println(x.toString());
	}

	public int minURatio(ArrayList<Integer> x) {
		int minI = -1;
		double minURatio = Double.MAX_VALUE;
		for (Integer i: x) {
			double uRatio = (double)u[i] / a[i];
			if (uRatio < minURatio) {
				minI = i;
				minURatio = uRatio;
			}
		}
		return minI;
	}

	public int maxURatio(ArrayList<Integer> r, int k) {
		// Find the maximum ratio not in the solution
		double maxURatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		ArrayList<Integer> bestIs = new ArrayList<Integer>();
		while (bestIs.size() <= k && bestIs.size() < r.size()) {
			for (Integer i: r) {
				double uRatio = (double)u[i] / a[i];
				if (uRatio > maxURatio && !bestIs.contains(i)) {
					maxURatio = uRatio;
					maxI = i;
				}
			}
			maxURatio = -1*Double.MAX_VALUE;
			bestIs.add(maxI);
		}
		return maxI;
	}

	private double[] tryAddU(int totalA, ArrayList<Integer> x, ArrayList<Integer> r) {
		double[] fail = {-1,-1};
		if (r.size() < 1) {
			return fail;
		}

		double maxURatio = -1;
		int maxI = -1;
		for (Integer i: r) {
			if (totalA + a[i] < b) {
				double uRatio = (double)(u[i] / a[i]) * 1000 / a[i];
				if (uRatio > maxURatio) {
					maxURatio = uRatio;
					maxI = i;
				}
			}
		}


		if (maxI == -1) {
			return fail;
		}
		double[] add = {maxI, u[maxI]};
		return add;
	}

	private double[] trySub(int totalU, ArrayList<Integer> x, boolean improvingOnly) {
		double[] fail = {-1,-1};
		if (x.size() <= 1) {
			return fail;
		}

		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		for (Integer i: x) {
			if (totalU - u[i] >= t) {
				double ratio = this.getRatio(i);
				if (ratio < minRatio) {
					minRatio = ratio;
					minI = i;
				}
			}
		}

		if (minI == -1) {
			return fail;
		}
		double change = subObj(minI, x, num, den);
		if (!improvingOnly || change > (num*num)/den) {
			double[] success = {minI, change};
			return success;
		} else {
			return fail;
		}
	}

	private double[] tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r) {
		double[] fail = {-1,-1};
		if (r.size() < 1) {
			return fail;
		}

		int maxI = -1;
		int b = this.getB();
		double maxRatio = -1*Double.MAX_VALUE;
		for (Integer i: r) {
			if (totalA + this.getA(i) <= b) {
				double ratio = this.getRatio(i);
				if (ratio > maxRatio) {
					maxRatio = ratio;
					maxI = i;
				}
			}
		}


		if (maxI == -1) {
			return fail;
		}
		double change = addObj(maxI, x, num, den);
		if (change > 0) {
			double[] add = {maxI, change};
			return add;
		} else {
			return fail;
		}
	}

	@Override
	/*
	 * Generate a random, feasible solution to the Max Probability problem
	 *      and fill the given lists with the solution.
	 * Ensures:
	 * 		sum(a_i*x_i) <= b
	 * 		sum(u_i*x_i) >= t
	 */
	public void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		// Clear Solution lists
		// x - variables included in solution
		// r - variables not included in solution
		x.clear();
		r.clear();

		// Initialize sum vars
		int totalAx = 0;
		int totalUx = 0;

		// Fill r with all variables
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

		// Perform swaps and shifts until totalAx <= b
		int swaps = 0;
		while (totalAx > b) {
			// Attempt to find a random swap that reduces totalAx
			int i = rnd.nextInt(x.size());
			int j = rnd.nextInt(r.size());
			int xi = x.get(i);
			int xj = r.get(j);
			int count = 0;
			// Change random numbers until a_xi > a_xj
			while(a[xj] >= a[xi] && count < n/2) {
				i = rnd.nextInt(x.size());
				j = rnd.nextInt(r.size());
				xi = x.get(i);
				xj = r.get(j);
				count++;
			}
			// Check swap for knapsack feasibility and update
			if (a[xj] < a[xi] && totalUx - u[xi] + u[xj] > t) {
				x.remove(i);
				r.add(xi);
				r.remove(j);
				x.add(xj);
				totalUx = totalUx + u[xj] - u[xi];
				totalAx = totalAx + a[xj] - a[xi];
			}
			// Increment swaps
			swaps++;
			
			// After a number of swaps, try a shift
			if (totalAx > b && swaps > 10) {
				// Attempt a substitution: 
				//	sub = {index, change in objective}
				double[] sub = trySub(totalUx,x,false);
				// If a variables can be removed
				if (sub[0] != -1) {
					// Remove variable from solution and update sums
					int subI = (int) sub[0];
					x.remove(Integer.valueOf(subI));
					r.add(subI);
					totalAx = totalAx - a[subI];
					totalUx = totalUx - u[subI];
				} 
				// Else, add a random variable to the solution
				else {
					xj = r.remove(rnd.nextInt(r.size()));
					x.add(Integer.valueOf(xj));
					totalAx = totalAx + a[xj];
					totalUx = totalUx + u[xj];
				}
				swaps = 0;
			}
		}
	}

	@Override
	public double swapObj(int i, int j, ArrayList<Integer> x, double oldObj) {
		ArrayList<Integer> newX = new ArrayList<Integer>(x);
		newX.remove(Integer.valueOf(i));
		newX.add(j);
		return getObj(newX, false);
	}

	@Override
	public int trySub(ArrayList<Integer> x, boolean improveOnly) {
		System.err.println("Used unimplemented method: mp.trySub");
		return 0;
	}

	public int trySub(int totalU, ArrayList<Integer> x, boolean improveOnly, 
			double num, double den) {
		if (x.size() <= 1) {
			return -1;
		}

		double minRatio = Double.MAX_VALUE;
		int minI = -1;
		for (Integer i: x) {
			if (totalU - u[i] >= t) {
				double ratio = this.getRatio(i);
				if (ratio < minRatio) {
					minRatio = ratio;
					minI = i;
				}
			}
		}

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
		num -= u[i];
		den -= s[i];
		return (num*num)/den;
	}

	@Override
	public int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r,
			boolean improveOnly) {
		System.err.println("Used unimplemented method: mp.tryAdd");
		return 0;
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
			if (totalA + this.getA(i) <= b) {
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
		num += u[i];
		den += s[i];
		return (num*num)/den;
	}

	@Override
	public double subObj(int i, ArrayList<Integer> x, double oldObj) {
		System.err.println("Used unimplemented method: mp.subObj");
		return 0;
	}

	@Override
	public double addObj(int i, ArrayList<Integer> x, double oldObj) {
		System.err.println("Used unimplemented method: mp.addObj");
		return 0;
	}

	@Override
	public int removeA(int i, int totalA) {
		return totalA - a[i];
	}

	@Override
	public int addA(int i, int totalA) {
		return totalA + a[i];
	}

	public int removeU(int i, int totalU) {
		return totalU - u[i];
	}

	public int addU(int i, int totalU) {
		return totalU + u[i];
	}

	@Override
	public boolean checkValid(ArrayList<Integer> x) {
		int totalA = calcTotalA(x);
		if (totalA <= b) {
			return true;
		}
		return false;
	}

	@Override
	public double getObj(ArrayList<Integer> x) {
		return getObj(x,true);
	}

	public double getObj(ArrayList<Integer> x, boolean setNumDen) {
		if (x.isEmpty()) {
			return 0;
		}
		if (setNumDen) {
			num = -1*t;
			den = 0;

			for (int i: x) {
				num += u[i];
				den += s[i];
			}

			return (num*num)/den;
		} else {
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

	public double swapNum(int i, int j, double num) {
		return num + u[j] - u[i];
	}

	public double subNum(int i, double num) {
		return num - u[i];
	}

	public double addNum(int i, double num) {
		return num + u[i];
	}

	public double getDen() {
		return den;
	}

	public double swapDen(int i, int j, double den) {
		return den + s[j] - s[i];
	}

	public double subDen(int i, double den) {
		return den - s[i];
	}

	public double addDen(int i, double den) {
		return den + s[i];
	}

	@Override
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

	@Override
	public int calcTotalA(ArrayList<Integer> x) {
		int totalA = 0;
		for (int i: x) {
			totalA += a[i];
		}
		return totalA;
	}

	public int calcTotalU(ArrayList<Integer> x) {
		int totalU = 0;
		for (int i: x) {
			totalU += u[i];
		}
		return totalU;
	}

	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			n = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			negCoef = scr.nextBoolean();
			b = scr.nextInt();
			t = scr.nextDouble();
			scr.nextLine();

			a = readArr(scr);
			u = readArr(scr);
			s = readArr(scr);

			uMaxXVals = new boolean[n];
			for (int i = 0; i < n; i++) {
				uMaxXVals[i] = scr.nextBoolean();
			}

		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
		calcTauRatio();
	}

	private void calcTauRatio() {
		tau = new double[n];
		ratio = new double[n];
		for (int i = 0; i < n; i++) {
			tau[i] = (double)(u[i]) / s[i];
			ratio[i] = tau[i]/a[i];
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
			pw.write(seed + "\n");
			pw.write(negCoef + "\n");
			pw.write(b + "\n");
			pw.write(t + "\n");
			writeArr(pw, a);
			writeArr(pw, u);
			writeArr(pw, s);
			for (int i = 0; i < n-1; i++) {
				pw.write(uMaxXVals[i] + " ");
			}
			pw.write(uMaxXVals[n-1] + "\n");

			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
			System.err.println(e.getMessage());
		}
	}

	private void writeArr(PrintWriter pw, int[] arr) {
		for (int i = 0; i < arr.length-1; i++) {
			pw.write(arr[i] + " ");
		}
		pw.write(arr[arr.length-1] + "\n");
	}

	public static void main(String[] args) {
		int[] probSizes = {10, 20, 30, 50, 100, 200};
		for (int p = 0; p < probSizes.length; p++) {
			int n = probSizes[p];
			for (int i = 0; i < 10; i++) {
				MaxProbability mp = new MaxProbability(n,false,p+n+i);
				MaxProbabilitySol mps = new MaxProbabilitySol();
				System.err.println(mps.getX().toString());
				System.err.println(mps.getObj());
				System.err.println(mps.getValid());
				if (!mps.getValid()) {
					System.err.println("@@@@@@@@@@@@@@@");
				}
			}
		}
	}
}
