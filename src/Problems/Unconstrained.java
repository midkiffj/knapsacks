package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import Runner.TestLogger;


public class Unconstrained extends Problem {
	// Unconstrained setup vars
	private int n;
	private Random rnd;
	private int seed;
	private boolean negCoef;
	private double density;

	// Coefficient vars
	private int c;
	private int[] ci;
	private int[][] cij;
	private int[][][] dijk;
	
	// Unconstrained mutation vars
	private int[] tau;

	public Unconstrained(String filename) {
		super();
		readFromFile(filename);
	}

	public Unconstrained(int n, boolean negCoef) {
		this(n,negCoef,1234,1);
	}

	public Unconstrained(int n, boolean negCoef, int seed) {
		this(n,negCoef,seed,1);
	}

	public Unconstrained(int n, boolean negCoef, int seed, double density) {
		super();
		this.n = n;
		this.negCoef = negCoef;
		this.rnd = new Random(seed);
		this.seed = seed;
		this.density = density;
		setup();
	}
	
	public Unconstrained(int c, int[] ci, int[][] cij, int[][][] dijk) {
		super();
		this.n = ci.length;
		this.negCoef = true;
		this.seed = 1234;
		this.rnd = new Random(seed);
		this.density = 0;
		this.c = c;
		this.ci = ci;
		this.cij = cij;
		this.dijk = dijk;
		calcTau();
	}
	
	private void calcTau() {
		this.tau = new int[n];
		int i,j,k;
		for(i = 0; i < n; i++){
			tau[i] += ci[i];
			for (j = i+1; j < n; j++){
				tau[i] += cij[i][j-i];
				tau[j] += cij[i][j-i];
				for(k = j+1; k < n; k++) {
					tau[i] += dijk[i][j-i][k-j];
					tau[j] += dijk[i][j-i][k-j];
					tau[k] += dijk[i][j-i][k-j];
				}
			}
		}
	}

	/**
	 * Initialize the objective and knapsack constraint coefficients
	 */
	/**
	 * Initialize the objective and knapsack constraint coefficients
	 */
	private void setup() {
		int i,j,k;

		// Define objective coefficient matrices
		ci = new int[n];
		cij = new int[n-1][];
		dijk = new int[n-1][][];
		tau = new int[n];

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
			
			//			cij[i][i] = 0;
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
					tau[i] += dijk[i][j-i][k-j];
					tau[j] += dijk[i][j-i][k-j];
					tau[k] += dijk[i][j-i][k-j];
				}
			}
		}
	}

	/**
	 * Randomly generate a solution to the cubic
	 */
	public void genRndInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		int xSize = rnd.nextInt(n);
		for (int i = 0; i < n; i++) {
			r.add(i);
		}
		for (int i = 0; i < xSize; i++) {
			int j = rnd.nextInt(r.size());
			x.add(r.remove(j));
		}
	}


	/**
	 * Generate a solution by adding x's until knapsack full
	 * and update the current objective value. 
	 * Don't use any indexes in the provided list.
	 */
	public void genInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		// TODO
	}

	/**
	 * Calculate the objective value with the given x values.
	 */
	public double getObj(ArrayList<Integer> x) {
		int i,j,k;
		double curObj = c;
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
		double obj = getObj(x);
		int index = -1;
		for (int i: x) {
			double newObj = subObj(i,x,obj);
			if (newObj > obj) {
				obj = newObj;
				index = i;
			}
		}
		return index;
	}
	
	public int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly) {
		double obj = getObj(x);
		int index = -1;
		for (int i: r) {
			double newObj = addObj(i,x,obj);
			if (newObj > obj) {
				obj = newObj;
				index = i;
			}
		}
		return index;
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
	
	public int removeA(int i, int totalA) {
		return -1;
	}
	
	public int addA(int i, int totalA) {
		return -1;
	}
	
	public boolean checkValid(ArrayList<Integer> x) {
		return true;
	}
	
	public int calcTotalA(ArrayList<Integer> x) {
		return -1;
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
	private double[] tryAdd(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int totalA) {
		// TODO
		return null;
	}

	private double[] trySub(ArrayList<Integer> curX, ArrayList<Integer> r, double curObj, int totalA) {
		// TODO
		return null;
	}

	public int getN() {
		return n;
	}

	public int getC() {
		return c;
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
	
	public int getTau(int i) {
		return tau[i];
	}

	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			n = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			negCoef = scr.nextBoolean();
			density = scr.nextDouble();
			c = scr.nextInt();
			scr.nextLine();

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
			pw.write(negCoef + "\n");
			pw.write(density + "\n");
			pw.write(c + "\n");
			writeArr(pw, ci);
			for (int i = 0; i < cij.length; i++) {
				writeArr(pw, cij[i]);
			}
			for (int i = 0; i < dijk.length; i++) {
				for (int j = 1; j < dijk[i].length; j++) {
					writeArr(pw, dijk[i][j]);
				}
			}

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
