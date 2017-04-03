package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.Fractional;

/**
 * Solution class for a Fractional Problem
 * - Mutates solution with swaps and shifts
 * - File I/O for storing solutions
 * 
 * @author midkiffj
 */
public class FractionalSol extends KnapsackSol {

	private static Fractional f;

	private long[] num;
	private long[] den;

	/**
	 * Construct a solution by relying on the super class
	 */
	public FractionalSol() {
		super();
		f = (Fractional)p;
		setNum(f.getNum());
		setDen(f.getDen());
		updateValid();
	}

	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public FractionalSol(String filename) {
		super(filename);
		f = (Fractional)p;
		updateValid();
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param fs the solution to copy
	 */
	public FractionalSol(FractionalSol fs) {
		super((KnapsackSol)fs);
		f = (Fractional)p;
		setNum(fs.getNum());
		setDen(fs.getDen());
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weight of the solution
	 * @param num - numerator values
	 * @param den - denominator values
	 */
	public FractionalSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA, long[] num, long[] den) {
		super(x,r,obj,totalA);
		f = (Fractional)p;
		setNum(num);
		setDen(num);
		updateValid();
	}

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
	public FractionalSol(boolean[] newXVals) {
		super(newXVals);
		f = (Fractional)p;
		setNum(f.getNum());
		setDen(f.getDen());
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
	public FractionalSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		f = (Fractional)p;
		setNum(f.getNum());
		setDen(f.getDen());
		updateValid();
	}

	/**
	 * Update the validity of the solution
	 */
	public void updateValid() {
		calcTotalA();
		if (getTotalA() <= f.getB()) {
			setValid(true);
		} else {
			setValid(false);
		}
	}

	public long[] getNum() {
		return num;
	}

	public long[] getDen() {
		return den;
	}

	public void setNum(long[] num) {
		this.num = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			this.num[i] = num[i];
		}
	}

	public void setDen(long[] den) {
		this.den = new long[f.getM()];
		for (int i = 0; i < f.getM(); i++) {
			this.den[i] = den[i];
		}
	}

	@Override
	/**
	 * Perform a swap of items i and j,
	 * 	- Remove i from the solution
	 * 	- Add j to the solution
	 * 	- Update objective and knapsack weight
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 */
	public void swap(int i, int j) {
		addA(j);
		removeA(i);
		this.num = swapNum(i,j,num);
		this.den = swapDen(i,j,den);
		setObj(updateObj(num,den));
		addI(j);
		removeI(i);
	}
	
	/**
	 * Calculate the objective if item i is removed from the solution 
	 * 	and item j is added to the solution.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @return calculated objective
	 */
	public double swapObj(int i, int j) {
		long[] num = swapNum(i, j, this.num);
		long[] den = swapDen(i, j, this.den);
		return updateObj(num,den);
	}
	
	/**
	 * Calculate the numerator values if item i is removed 
	 * 	and item j is added to the value.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param num - numerator value
	 * @return calculated objective
	 */
	private long[] swapNum(int i, int j, long[] num) {
		long[] newNum = new long[num.length];
		for (int k = 0; k < f.getM(); k++) {
			newNum[k] = num[k] + f.getC(k,j) - f.getC(k,i);
		}
		return newNum;
	}
	
	/**
	 * Calculate the denominator values if item i is removed 
	 * 	and item j is added to the value.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private long[] swapDen(int i, int j, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < f.getM(); k++) {
			newDen[k] = den[k] + f.getD(k,j) - f.getD(k,i);
		}
		return newDen;
	}

	/**
	 * Calculate the objective with the given numerator and denominator values
	 * 
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double updateObj(long[] num, long[] den) {
		double newObj = 0;
		for (int k = 0; k < f.getM(); k++) {
			if (den[k] == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			newObj += (double)(num[k])/den[k];
		}
		return newObj;
	}

	/**
	 * Try to add a variable to the solution
	 * 
	 * @return the item added or -1 if none added
	 */
	public int tryAdd() {
		int index = tryAdd(getTotalA(), getX(), getR(), false, num, den);
		if (index != -1) {
			addX(index);
		}
		return index;
	}

	/**
	 * Try to remove a variable from the solution
	 * 
	 * @return the item removed or -1 if none added
	 */
	public int trySub() {
		int index = trySub(getX(), false, num, den);
		if (index != -1) {
			removeX(index);
		}
		return index;
	}
	
	/**
	 * Try to add an item to the given solution
	 * - Add the maxRatio item
	 * 
	 * @param totalA - current knapsack weight
	 * @param x - solution list
	 * @param r - list of items not in the solution
	 * @param improveOnly - only remove an item if it improves the objective
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the item to add or -1 if none to add
	 */
	private int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, 
			boolean improveOnly, long[] num, long[] den) {
		if (r.size() < 1) {
			return -1;
		}

		int b = this.getB();
		double maxRatio = Double.MIN_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (totalA + f.getA(i) <= b) {
				double ratio = f.getRatio(i);
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
			double change = addObj(maxI, num, den);
			double curObj = updateObj(num, den);
			if (change > curObj) {
				return maxI;
			} else {
				return -1;
			}
		} else {
			return maxI;
		}
	}

	/**
	 * Try to remove an item from the given solution
	 * - Remove the minRatio item
	 * 
	 * @param x - solution list
	 * @param improveOnly - only remove an item if it improves the objective
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the item to remove or -1 if none to remove
	 */
	private int trySub(ArrayList<Integer> x, boolean improveOnly, long[] num, long[] den) {
		if (x.size() <= 1) {
			return -1;
		}
		int minI = minRatio(0);

		if (minI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = subObj(minI, num, den);
			double curObj = updateObj(num,den);
			if (change > curObj) {
				return minI;
			} else {
				return -1;
			}
		} else {
			return minI;
		}
	}
	
	/**
	 * Add variable i to the solution
	 * 
	 * @param i - item to add
	 */
	public void addX(int i) {
		addI(i);
		addA(i);
		num = addNum(i, num);
		den = addDen(i, den);
		setObj(updateObj(num,den));
	}
	
	/**
	 * Remove variable i from the solution
	 * 
	 * @param i - item to remove
	 */
	public void removeX(int i) {
		removeI(i);
		removeA(i);
		num = subNum(i, num);
		den = subDen(i, den);
		setObj(updateObj(num,den));
	}

	/**
	 * Calculate the objective if item i is removed from the solution
	 * 
	 * @param i - item to remove
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double subObj(int i, long[] num, long[] den) {
		double obj = 0;
		for (int j = 0; j < f.getM(); j++) {
			if (den[j]-f.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]-f.getC(j,i))/(den[j]-f.getD(j,i));
		}

		return obj;
	}

	/**
	 * Calculate the objective if item i is added to the solution
	 * 
	 * @param i - item to add
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double addObj(int i, long[] num, long[] den) {
		double obj = 0;
		for (int j = 0; j < f.getM(); j++) {
			if (den[j]+f.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]+f.getC(j,i))/(den[j]+f.getD(j,i));
		}

		return obj;
	}

	/**
	 * Calculate the numerator values if item i is removed
	 * 
	 * @param i - item to remove
	 * @param num - numerator value
	 * @return calculated objective
	 */
	private long[] subNum(int i, long[] num) {
		long[] newNum = new long[num.length];
		for (int k = 0; k < f.getM(); k++) {
			newNum[k] = num[k] - f.getC(k,i);
		}
		return newNum;
	}

	/**
	 * Calculate the numerator values if item i is added
	 * 
	 * @param i - item to add
	 * @param num - numerator value
	 * @return calculated objective
	 */
	private long[] addNum(int i, long[] num) {
		long[] newNum = new long[num.length];
		for (int k = 0; k < f.getM(); k++) {
			newNum[k] = num[k] + f.getC(k,i);
		}
		return newNum;
	}

	/**
	 * Calculate the denominator values if item i is removed 
	 * 
	 * @param i - item to remove
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private long[] subDen(int i, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < f.getM(); k++) {
			newDen[k] = den[k] - f.getD(k,i);
		}
		return newDen;
	}

	/**
	 * Calculate the denominator values if item i is added
	 * 
	 * @param i - item to add
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private long[] addDen(int i, long[] den) {
		long[] newDen = new long[den.length];
		for (int k = 0; k < f.getM(); k++) {
			newDen[k] = den[k] + f.getD(k,i);
		}
		return newDen;
	}

	@Override
	/**
	 * Heal the solution if it is invalid
	 */
	public void healSol() {
		healSolImproving();
		healSolRatio();
	}

	/**
	 * Heal the solution by removing the item that results in the best objective
	 *  until the solution is valid.
	 */
	private void healSolImproving() {
		while(!this.getValid()) {
			double maxObj = -1*Double.MAX_VALUE;
			int maxI = -1;
			for (Integer i: this.getX()) {
				double newObj = subObj(i, num, den);
				if (newObj > maxObj) {
					maxObj = newObj;
					maxI = i;
				}
			}
			if (maxI != -1) {
				removeI(maxI);
				removeA(maxI);
				setObj(maxObj);
				this.num = subNum(maxI, this.num);
				this.den = subDen(maxI, this.den);
			} else {
				System.err.println("Couldn't find an improving objective!!!");
				System.exit(-1);
			}
		}
	}

	/**
	 * Heal the solution by removing minRatio items until valid
	 */
	private void healSolRatio() {
		while(!this.getValid()) {
			int j = minRatio(0);
			removeI(j);
			removeA(j);
			setObj(subObj(j, num, den));
			num = subNum(j, num);
			den = subDen(j, den);
		}
	}

	/**
	 * Write the solution to the given file
	 * 
	 * @param filename to write
	 */
	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(getObj() + "\n");
			pw.write(getTotalA() + "\n");
			for (int i = 0; i < f.getM(); i++) {
				pw.write(num[i] + " ");
			}
			pw.write("\n");
			for (int i = 0; i < f.getM(); i++) {
				pw.write(den[i] + " ");
			}
			pw.write("\n");
			Collections.sort(getX());
			for (Integer i: getX()) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Read a solution from the given filename
	 * 
	 * @param filename to read
	 */
	public void readSolution(String filename) { 
		f = (Fractional)p;
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			int readTotalA = scr.nextInt();
			long[] readNum = new long[f.getM()];
			long[] readDen = new long[f.getM()];
			for (int i = 0; i < f.getM(); i++) {
				readNum[i] = scr.nextLong();
			}
			for (int i = 0; i < f.getM(); i++) {
				readDen[i] = scr.nextLong();
			}
			if (readObj != -1) {
				ArrayList<Integer> readX = new ArrayList<Integer>();
				while (scr.hasNextInt()) {
					readX.add(scr.nextInt());
				}
				ArrayList<Integer> readR = new ArrayList<Integer>();
				for (int i = 0; i < n; i++) {
					readR.add(i);
				}
				readR.removeAll(readX);
				setX(readX);
				setR(readR);
				setObj(readObj);
				setNum(readNum);
				setDen(readDen);
				setTotalA(readTotalA);
			} else {
				System.err.println("NO INCUMBENT SOLUTION IN FILE");
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

}
