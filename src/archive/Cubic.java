package archive;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;


public class Cubic {
	// Cubic setup vars
	private int n;
	private Random rnd;
	private int seed;
	private boolean negCoef;
	private double density;

	// Coefficient vars
	private int[] a;
	private int b;
	private int[] ci;
	private int[][] cij;
	private int[][][] dijk;


	// Cubic manipulation vars
	private long incObj;
	private int incTotalA;
	private int[] tau;
	private double[] ratio;
	private ArrayList<Integer> x;
	private ArrayList<Integer> r;

	public Cubic(String filename) {
		readFromFile(filename);
		//		System.out.println("Read Incumbent: " + incObj);
		//		System.out.println(x.toString());
	}

	public Cubic(int n, boolean negCoef) {
		this(n,negCoef,1234,1);
	}

	public Cubic(int n, boolean negCoef, int seed) {
		this(n,negCoef,seed,1);
	}

	public Cubic(int n, boolean negCoef, int seed, double density) {
		this.n = n;
		this.negCoef = negCoef;
		this.rnd = new Random(seed);
		this.seed = seed;
		this.density = density;
		this.incObj = -1;
		this.incTotalA = -1;
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
		// a : [1,50]
		// c : [-100,100]; cij = cji; cii = 0
		for(i = 0; i < n; i++){
			if (i < n-1) {
				cij[i] = new int[n-i];
				dijk[i] = new int[n-i][];
			}
			ci[i] = rnd.nextInt(201) - 100;
			a[i] = rnd.nextInt(50)+1;
			//			cij[i][i] = 0;
			totalA += a[i];
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
			ratio[i] = (double)(tau[i])/a[i];
		}

		// b : [50,sum(a)]
		b = rnd.nextInt(totalA-50+1)+50;
	}

	/**
	 * Randomly generate a solution to the cubic
	 */
	public void genRndInit(ArrayList<Integer> d, ArrayList<Integer> x, ArrayList<Integer> r) {
		r.clear();
		x.clear();
		int totalAx = 0;
		int i = 0;
		ArrayList<Integer> toUse = new ArrayList<Integer>();
		for (int j = 0; j < n; j++) {
			r.add(j);
			toUse.add(j);
		}
		toUse.removeAll(d);
		boolean done = false;
		while (totalAx <= b && !done) {
			int num = rnd.nextInt(toUse.size());
			i = toUse.get(num);
			if (totalAx + a[i] <= b) {
				x.add(i);
				toUse.remove(Integer.valueOf(i));
				r.remove(Integer.valueOf(i));
				totalAx += a[i];
			} else {
				done = true;
			}
		}

		this.x = x;
		this.r = r;
		incObj = updateObj(x);
		incTotalA = totalAx;
	}

	/**
	 * Generate an initial incumbent solution by adding x's until knapsack full
	 * and update the current objective value
	 */
	public void genInit() {
		genInit(new ArrayList<Integer>(),new ArrayList<Integer>(), new ArrayList<Integer>());
	}

	/**
	 * Generate a solution by adding x's until knapsack full
	 * and update the current objective value. 
	 * Don't use any indexes in the provided list.
	 */
	public void genInit(ArrayList<Integer> d, ArrayList<Integer> x, ArrayList<Integer> r) {
		if (incObj != -1) {
			for (Integer i: this.x) {
				x.add(i);
			}
			for (Integer i: this.r) {
				r.add(i);
			}
			return;
		}
		r.clear();
		x.clear();
		int totalAx = 0;
		int i = 0;
		ArrayList<Integer> toUse = new ArrayList<Integer>();
		for (int j = 0; j < n; j++) {
			r.add(j);
			toUse.add(j);
		}
		toUse.removeAll(d);
		boolean[] inX = new boolean[n];
		boolean done = false;
		while (totalAx <= b && !done) {
			double maxRatio = -1*Double.MAX_VALUE;
			i = -1;
			for (int j = 0; j < toUse.size(); j++) {
				int xj = toUse.get(j);
				if (!inX[xj] && ratio[xj] >= maxRatio && totalAx + a[xj] <= b) {
					i = xj;
					maxRatio = ratio[xj];
				}
			}
			if (i == -1) {
				done = true;
			} else {
				x.add(i);
				toUse.remove(Integer.valueOf(i));
				r.remove(Integer.valueOf(i));
				totalAx += a[i];
				inX[i] = true;
			}
		}

		incObj = updateObj(x);
		long curObj = incObj;

		// Check for Swaps and shifts
		boolean swapping = true;
		int swaps = 0;
		//		while (swapping && swaps < Math.log(n)) {
		while (swapping) {
			int maxI = -1;
			int maxJ = -1;
			long maxChange = 0;
			for(Integer xi: x) {
				for(Integer xj: toUse) {
					// Check for knapsack feasibility
					if (a[xj]-a[xi] <= b - totalAx) {
						long newObj = swapObj(x, curObj, xi, xj);
						long change = newObj - curObj;
						if (change > maxChange) {
							maxI = xi;
							maxJ = xj;
							maxChange = change;
						}
					}
				}
			}
			long[] add = tryAdd(x,toUse, curObj, totalAx);
			long[] sub = trySub(x,toUse, curObj, totalAx);
			long addChange = add[0];
			long subChange = sub[0];
			if (addChange > maxChange) {
				int addI = (int)add[1];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				toUse.remove(Integer.valueOf(addI));
				curObj = curObj + add[0];
				totalAx = totalAx + a[addI];
			} else if (subChange > maxChange) {
				int subI = (int)sub[1];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				toUse.add(subI);
				curObj = curObj + sub[0];
				totalAx = totalAx - a[subI];
			} else {
				if (maxI == -1 && maxJ == -1) {
					swapping = false;
				} else {
					x.add(maxJ);
					r.remove(Integer.valueOf(maxJ));
					toUse.remove(Integer.valueOf(maxJ));
					x.remove(Integer.valueOf(maxI));
					r.add(maxI);
					toUse.add(maxI);
					curObj = curObj + maxChange;
					totalAx = totalAx + a[maxJ] - a[maxI];
				}
			}
			swaps++;
		}
		incObj = curObj;
		incTotalA = totalAx;
		this.x = x;
		this.r = r;
		System.out.println("Generated Incumbent: " + incObj);
		Collections.sort(x);
		System.out.println(x.toString());
	}

	/**
	 * Calculate the objective value with the given x values.
	 */
	public long updateObj(ArrayList<Integer> x) {
		int i,j,k;
		long curObj = 0;
		for(i = 0; i < x.size(); i++){
			int xi = x.get(i);
			curObj += ci[xi];
			for (j = i+1; j < x.size(); j++){
				int xj = x.get(j);
				curObj += 2*this.getCij(xi, xj);
				for(k = j+1; k < x.size(); k++) {
					int xk = x.get(k);
					curObj += 6*this.getDijk(xi,xj,xk);
				}
			}
		} 
		return curObj;
	}

	private long swapObj(ArrayList<Integer> curX, long oldObj, int i, int j) {
		oldObj = oldObj - this.getCi(i);
		oldObj = oldObj + this.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - 2*this.getCij(i,xk);
				oldObj = oldObj + 2*this.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - 6*this.getDijk(i,xk,xl);
						oldObj = oldObj + 6*this.getDijk(j,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	// Try to add a variable to the solution
	private long[] tryAdd(ArrayList<Integer> curX, ArrayList<Integer> r, long curObj, int totalA) {
		long maxChange = 0;
		int maxI = -1;
		for(Integer i: r) {
			if (totalA + a[i] <= b) {
				long obj = curObj + this.getCi(i);
				for (int j = 0; j < curX.size(); j++) {
					int xj = curX.get(j);
					obj += 2*this.getCij(i,xj);
					for (int k = j+1; k < curX.size(); k++) {
						int xk = curX.get(k);
						obj += 6*this.getDijk(i,xj,xk);
					}
				}
				long change = obj - curObj;
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		long[] result = {maxChange, maxI};
		return result;
	}

	private long[] trySub(ArrayList<Integer> curX, ArrayList<Integer> r, long curObj, int totalA) {
		long maxChange = 0;
		int maxI = -1;
		for(Integer i: curX) {
			long obj = curObj - this.getCi(i);
			for (int j = 0; j < curX.size(); j++) {
				int xj = curX.get(j);
				obj -= 2*this.getCij(i,xj);
				for (int k = j+1; k < curX.size(); k++) {
					int xk = curX.get(k);
					obj -= 6*this.getDijk(i,xj,xk);
				}
			}
			long change = obj - curObj;
			if (change > maxChange) {
				maxChange = change;
				maxI = i;
			}
		}
		long[] result = {maxChange, maxI};
		return result;
	}

	public int getN() {
		return n;
	}

	public long getIncObj() {
		if (incObj == -1) {
			this.genInit();
		}
		return incObj;
	}

	public int getIncTotalA() {
		if (incTotalA == -1) {
			if (incObj == -1) {
				this.genInit();
			} else {
				incTotalA = 0;
				for (Integer i: x) {
					incTotalA += this.getA(i);
				}
			}
		}
		return incTotalA;
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

	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			n = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			b = scr.nextInt();
			negCoef = scr.nextBoolean();
			density = scr.nextDouble();
			scr.nextLine();

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

			tau = readArr(scr);

			ratio = new double[n];
			for (int i = 0; i < n; i++) {
				ratio[i] = scr.nextDouble();
			}
			incObj = -1;
			incTotalA = -1;
//			incObj = scr.nextLong();
//			incTotalA = scr.nextInt();
//			if (incObj != -1) {
//				x = new ArrayList<Integer>();
//				while (scr.hasNextInt()) {
//					x.add(scr.nextInt());
//				}
//			}
//
//			r = new ArrayList<Integer>();
//			for (int i = 0; i < n; i++) {
//				r.add(i);
//			}
//			r.removeAll(x);

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
			pw.write(seed + "\n");
			pw.write(b + "\n");
			pw.write(negCoef + "\n");
			pw.write(density + "\n");
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
			writeArr(pw, tau);
			for (int i = 0; i < n-1; i++) {
				pw.write(ratio[i] + " ");
			}
			pw.write(ratio[n-1] + "\n");

			pw.write(incObj + "\n");
			pw.write(incTotalA + "\n");
			for (Integer i: x) {
				pw.write(i + " ");
			}
			pw.write("\n");
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
