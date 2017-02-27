package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import Solutions.FractionalSol;


public class Fractional extends Knapsack {

	// Setup vars
	private int n;
	private Random rnd;
	private int seed;
	private boolean negCoef;

	// Coefficient vars
	private int m;
	private int b;
	private int[] a;
	private int[][] c;
	private int[][] d;
	private int[] numConst;
	private int[] denConst;

	// Obj values
	private long[] num;
	private long[] den;
	// Manipulation
	private double[] tau;
	private double[] ratio;


	public Fractional(String filename) {
		super();
		readFromFile(filename);
	}

	public Fractional(int n, boolean negCoef) {
		this(n,1,negCoef,1234);
	}

	public Fractional(int n, int m, boolean negCoef, int seed) {
		super();
		this.n = n;
		this.m = m;
		this.negCoef = negCoef;
		this.rnd = new Random(seed);
		this.seed = seed;
		setup();
	}

	private void setup() {
		a = new int[n];
		c = new int[m][n];
		d = new int[m][n];
		numConst = new int[m];
		denConst = new int[m];
		tau = new double[n];
		ratio = new double[n];
		for (int i = 0; i < m; i++) {
			numConst[i] = rnd.nextInt(10) + 1;
			denConst[i] = rnd.nextInt(10) + 1;
			int totalC = numConst[i];
			int totalD = denConst[i];
			boolean sat = false;
			while (!sat) {
				for (int j = 0; j < n; j++) {
						c[i][j] = rnd.nextInt(10)+1;
						d[i][j] = rnd.nextInt(10)+1;
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
		for (int i = 0; i < n; i++) {
			a[i] = rnd.nextInt(9) + 1;
			for (int j = 0; j < m; j++) {
				tau[i] += (double)(c[j][i]*1000)/d[j][i];
			}
			ratio[i] = tau[i] / a[i];
		}
		b = 100;
	}

	@Override
	public void genInit(ArrayList<Integer> x, ArrayList<Integer> r) {
		r.clear();
		x.clear();
		int totalAx = 0;
		int i = 0;
		for (int j = 0; j < n; j++) {
			r.add(j);
		}
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

		double curObj = getObj(x);

		// Check for Swaps and shifts
		boolean swapping = true;
		int swaps = 0;
		while (swapping) {
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
			double[] add = tryAdd(totalAx,r);
			double[] sub = trySub(x);
			double addObj = add[1];
			double subObj = sub[1];
			if (addObj-curObj > maxChange) {
				int addI = (int)add[0];
				x.add(addI);
				r.remove(Integer.valueOf(addI));
				curObj = getObj(x);
				totalAx = totalAx + a[addI];
			} else if (addObj-curObj > maxChange) {
				int subI = (int)sub[0];
				x.remove(Integer.valueOf(subI));
				r.add(subI);
				curObj = getObj(x);
				totalAx = totalAx - a[subI];
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
				}
			}
		}

		System.out.println("Generated Incumbent: " + curObj);
		Collections.sort(x);
		System.out.println(x.toString());
	}
	
	private double swapObj(int i, int j, ArrayList<Integer> x, double oldObj) {
		ArrayList<Integer> newX = new ArrayList<Integer>(x);
		newX.remove(Integer.valueOf(i));
		newX.add(j);
		return getObj(newX, false);
	}

	private double[] trySub(ArrayList<Integer> x) {
		double[] fail = {-1,-1};
		if (x.size() <= 1) {
			return fail;
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
			return fail;
		}
		double change = subObj(minI, num, den);
		double curObj = 0;
		for (int j = 0; j < m; j++) {
			curObj += (double)(num[j])/den[j];
		}
		if (change > curObj) {
			double[] success = {minI, change};
			return success;
		} else {
			return fail;
		}
	}

	private double[] tryAdd(int totalA, ArrayList<Integer> r) {
		double[] fail = {-1,-1};
		if (r.size() < 1) {
			return fail;
		}

		int b = this.getB();
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
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
		double change = addObj(maxI, num, den);
		double curObj = 0;
		for (int j = 0; j < m; j++) {
			curObj += (double)(num[j])/den[j];
		}
		if (change > curObj) {
			double[] add = {maxI, change};
			return add;
		} else {
			return fail;
		}
	}

	@Override
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

	private double subObj(int i, long[] num, long[] den) {
		double obj = 0;
		for (int j = 0; j < m; j++) {
			if (den[j]-d[j][i] == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]-c[j][i])/(den[j]-d[j][i]);
		}
		
		return obj;
	}

	private double addObj(int i, long[] num, long[] den) {
		double obj = 0;
		for (int j = 0; j < m; j++) {
			if (den[j]+d[j][i] == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]+c[j][i])/(den[j]+d[j][i]);
		}
		
		return obj;
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
			double newObj = 0;
			num = new long[m];
			den = new long[m];
			for (int i = 0; i < m; i++) {
				num[i] += numConst[i];
				den[i] += denConst[i];

				for (int j: x) {
					num[i] += c[i][j];
					den[i] += d[i][j];
				}
				if (den[i] == 0) {
					return -1*Double.MAX_VALUE;
				}
				newObj += (double)(num[i])/den[i];
			}
			return newObj;
		} else {
			long[] num = new long[m];
			long[] den = new long[m];
			double newObj = 0;
			for (int i = 0; i < m; i++) {
				num[i] += numConst[i];
				den[i] += denConst[i];

				for (int j: x) {
					num[i] += c[i][j];
					den[i] += d[i][j];
				}
				if (den[i] == 0) {
					return -1*Double.MAX_VALUE;
				}
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

	public long[] swapDen(int i, int j, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < m; k++) {
			newDen[k] = den[k] + d[k][j] - d[k][i];
		}
		return newDen;
	}

	public long[] subDen(int i, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < m; k++) {
			newDen[k] = den[k] - d[k][i];
		}
		return newDen;
	}

	public long[] addDen(int i, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < m; k++) {
			newDen[k] = den[k] + d[k][i];
		}
		return newDen;
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

	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			n = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			negCoef = scr.nextBoolean();
			m = scr.nextInt();
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
			pw.write(m + "\n");
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

	private void writeArr(PrintWriter pw, int[] arr) {
		for (int i = 0; i < arr.length-1; i++) {
			pw.write(arr[i] + " ");
		}
		pw.write(arr[arr.length-1] + "\n");
	}
}
