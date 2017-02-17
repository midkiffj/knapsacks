package Problems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import Solutions.FractionalSol;


public class Fractional extends Problem {

	// Setup vars
	private int n;
	private Random rnd;
	private int seed;
	private boolean negCoef;

	// Coefficient vars
	private int b;
	private int[] a;
	private int[] c;
	private int[] d;
	private int numConst;
	private int denConst;

	// Obj values
	private double num;
	private double den;
	// Manipulation
	private double[] tau;
	private double[] ratio;


	public Fractional(String filename) {
		super();
		readFromFile(filename);
	}

	public Fractional(int n, boolean negCoef) {
		this(n,negCoef,1234);
	}

	public Fractional(int n, boolean negCoef, int seed) {
		super();
		this.n = n;
		this.negCoef = negCoef;
		this.rnd = new Random(seed);
		this.seed = seed;
		setup();
	}

	private void setup() {
		a = new int[n];
		c = new int[n];
		d = new int[n];
		tau = new double[n];
		ratio = new double[n];
		int totalC = 0;
		int totalD = 0;
		boolean sat = false;
		while (!sat) {
			for (int i = 0; i < n; i++) {
				a[i] = rnd.nextInt(9) + 1;
				if (negCoef) {
					c[i] = rnd.nextInt(21) - 10;
					d[i] = rnd.nextInt(21) - 10;
				} else {
					c[i] = rnd.nextInt(11);
					d[i] = rnd.nextInt(11);
				}
				totalC += c[i];
				totalD += d[i];
				tau[i] = (double)(c[i]) / d[i];
				ratio[i] = tau[i] / a[i];
			}
			if (totalC != 0 && totalD != 0) {
				sat = true;
			} else {
				totalC = 0;
				totalD = 0;
			}
		}
		numConst = rnd.nextInt(10) + 1;
		denConst = rnd.nextInt(10) + 1;
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
			double[] add = tryAdd(totalAx,x,r);
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
		double change = subObj(minI, x, num, den);
		if (change > num/den) {
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
		double change = addObj(maxI, x, num, den);
		if (change > 0) {
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

	@Override
	public double swapObj(int i, int j, ArrayList<Integer> x, double oldObj) {
		ArrayList<Integer> newX = new ArrayList<Integer>(x);
		newX.remove(Integer.valueOf(i));
		newX.add(j);
		return getObj(newX, false);
	}


	@Override
	public int trySub(ArrayList<Integer> x, boolean improveOnly) {
		System.err.println("Used unimplemented f.trySub(x,boolean)");
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

	public int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly) {
		System.err.println("Used unimplemented f.tryAdd(totalA,x,r,boolean)");
		if (x.size() == this.getN()) {
			return -1;
		}
		int b = this.getB();
		double maxRatio = Double.MIN_VALUE;
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

	public double subObj(int i, ArrayList<Integer> x, double num,
			double den) {
		num -= c[i];
		den -= d[i];
		if (den == 0) {
			return -1*Double.MAX_VALUE;
		}
		return num/den;
	}

	public int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, 
			boolean improveOnly, double num, double den) {
		if (r.size() < 1) {
			return -1;
		}

		int b = this.getB();
		double maxRatio = Double.MIN_VALUE;
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
			return -1;
		}
		if (improveOnly) {
			double change = addObj(maxI, x, num, den);
			if (change > 0) {
				return maxI;
			} else {
				return -1;
			}
		} else {
			return maxI;
		}
	}

	public int trySub(ArrayList<Integer> x, boolean improveOnly, double num, double den) {
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
			double change = subObj(minI, x, num, den);
			if (change > 0) {
				return minI;
			} else {
				return -1;
			}
		} else {
			return minI;
		}
	}

	public double addObj(int i, ArrayList<Integer> x, double num,
			double den) {
		num += c[i];
		den += d[i];
		if (den == 0) {
			return -1*Double.MAX_VALUE;
		}
		return num/den;
	}

	@Override
	public double subObj(int i, ArrayList<Integer> x, double oldObj) {
		System.err.println("Used unimplemented method: f.subObj(i,x,oldObj)");
		return 0;
	}

	@Override
	public double addObj(int i, ArrayList<Integer> x, double oldObj) {
		System.err.println("Used unimplemented method: f.addObj(i,x,oldObj");
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
			num = numConst;
			den = denConst;

			for (int i: x) {
				num += c[i];
				den += d[i];
			}
			if (den == 0) {
				return -1*Double.MAX_VALUE;
			}
			return num/den;
		} else {
			double num = numConst;
			double den = denConst;

			for (int i: x) {
				num += c[i];
				den += d[i];
			}
			if (den == 0) {
				return -1*Double.MAX_VALUE;
			}
			return num/den;
		}
	}

	public double getNum() {
		return num;
	}

	public double swapNum(int i, int j, double num) {
		return num + c[j] - c[i];
	}

	public double subNum(int i, double num) {
		return num - c[i];
	}

	public double addNum(int i, double num) {
		return num + c[i];
	}

	public double getDen() {
		return den;
	}

	public double swapDen(int i, int j, double den) {
		return den + d[j] - d[i];
	}

	public double subDen(int i, double den) {
		return den - d[i];
	}

	public double addDen(int i, double den) {
		return den + d[i];
	}

	@Override
	public int getN() {
		return n;
	}

	public int getB() {
		return b;
	}

	public int getA(int i) {
		return a[i];
	}

	public int getC(int i) {
		return c[i];
	}

	public int getD(int i) {
		return d[i];
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

	public void readFromFile(String filename) {
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			n = scr.nextInt();
			seed = scr.nextInt();
			rnd = new Random(seed);
			negCoef = scr.nextBoolean();
			b = scr.nextInt();
			numConst = scr.nextInt();
			denConst = scr.nextInt();
			scr.nextLine();

			a = readArr(scr);
			c = readArr(scr);
			d = readArr(scr);

		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
		calcTauRatio();
	}

	private void calcTauRatio() {
		tau = new double[n];
		ratio = new double[n];
		for (int i = 0; i < n; i++) {
			tau[i] = (double)(c[i]) / d[i];
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
			pw.write(b + "\n");
			pw.write(numConst + "\n");
			pw.write(denConst + "\n");
			writeArr(pw, a);
			writeArr(pw, c);
			writeArr(pw, d);

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
				Fractional f = new Fractional(n,false,p+n+i);
				FractionalSol fs = new FractionalSol();
				System.err.println(fs.getX().toString());
				System.err.println(fs.getObj());
				System.err.println(fs.getValid());
				if (!fs.getValid()) {
					System.err.println("@@@@@@@@@@@@@@@");
				}
			}
		}
	}
}
