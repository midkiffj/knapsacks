package Solutions;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

import Problems.CubicMult;
import Problems.Problem;
import Problems.ProblemFactory;


public abstract class MultKnapsackSol extends ProblemSol {

	private CubicMult cm;
	
	private ArrayList<Integer> x;
	private ArrayList<Integer> r;
	private boolean[] xVals;
	private boolean valid;
	private double obj;
	private int[] totalA;

	public MultKnapsackSol() {
		super();
		cm = (CubicMult)p;
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		xVals = new boolean[p.getN()];
		p.genInit(x, r);
		for (Integer i: x) {
			xVals[i] = true;
		}
		obj = p.getObj(x);
		calcTotalA();
		updateValid();
	}
	
	public MultKnapsackSol(String filename) {
		super();
		cm = (CubicMult)p;
		readSolution(filename);
		xVals = new boolean[p.getN()];
		for (Integer i : x) {
			xVals[i] = true;
		}
		updateValid();
	}
	
	public MultKnapsackSol(MultKnapsackSol mks) {
		super();
		cm = (CubicMult)p;
		xVals = new boolean[p.getN()];
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (Integer i : mks.getX()) {
			x.add(i);
			xVals[i] = true;
		}
		for (Integer i : mks.getR()) {
			r.add(i);
		}
		obj = mks.getObj();
		int[] totalA = mks.getTotalA();
		this.totalA = new int[totalA.length];
		for (int i = 0; i < totalA.length; i++) {
			this.totalA[i] = totalA[i];
		}
		updateValid();
	}

	public MultKnapsackSol(boolean[] xVals) {
		super();
		cm = (CubicMult)p;
		this.xVals = xVals;
		x = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for (int i = 0; i < xVals.length; i++) {
			if (xVals[i]) {
				x.add(i);
			} else {
				r.add(i);
			}
		}
		obj = p.getObj(x);
		calcTotalA();
		updateValid();
	}

	public MultKnapsackSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super();
		cm = (CubicMult)p;
		xVals = new boolean[p.getN()];
		this.x = x;
		this.r = r;
		for (Integer i: x) {
			xVals[i] = true;
		}
		obj = p.getObj(x);
		calcTotalA();
		updateValid();
	}

	public MultKnapsackSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int[] totalA) {
		super();
		cm = (CubicMult)p;
		xVals = new boolean[p.getN()];
		this.x = x;
		this.r = r;
		for (Integer i: x) {
			xVals[i] = true;
		}
		this.obj = obj;
		this.totalA = new int[totalA.length];
		for (int i = 0; i < totalA.length; i++) {
			this.totalA[i] = totalA[i];
		}
		updateValid();
	}

	public void update(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int[] totalA) {
		this.x = new ArrayList<Integer>(x);
		this.r = new ArrayList<Integer>(r);
		for (Integer i: x) {
			xVals[i] = true;
		}
		for (Integer i: r) {
			xVals[i] = false;
		}
		this.obj = obj;
		this.totalA = new int[totalA.length];
		for (int i = 0; i < totalA.length; i++) {
			this.totalA[i] = totalA[i];
		}
		updateValid();
	}

	private void updateValid() {
		valid = p.checkValid(x);
	}

	public double swapObj(int i, int j) {
		return p.swapObj(i,j,x,obj);
	}

	/**
	 * Swap the boolean values in the current x array at indexes i and j
	 * @param curX
	 * @param i
	 * @param j
	 */
	public void swap(int i, int j) {
		this.obj = swapObj(i,j);
		this.totalA = cm.removeA(i,cm.addA(j,totalA));
		updateValid();
		xVals[i] = false;
		xVals[j] = true;
		remove(x,i);
		x.add(j);
		remove(r,j);
		r.add(i);
	}
	
	public int tryImproveAdd() {
		int index = cm.tryAdd(totalA, x, r, true);
		if (index != -1) {
			xVals[index] = true;
			x.add(index);
			remove(r, index);
			totalA = cm.addA(index, totalA);
			this.obj = cm.addObj(index, x, obj);
			updateValid();
		}
		return index;
	}

	public int tryImproveSub() {
		int index = p.trySub(x, true);
		if (index != -1) {
			xVals[index] = false;
			r.add(index);
			remove(x, index);
			totalA = cm.removeA(index, totalA);
			this.obj = p.subObj(index, x, obj);
			updateValid();
		}
		return index;
	}

	// Try to add a variable to the solution
	public int tryAdd() {
		int index = cm.tryAdd(totalA, x, r, false);
		if (index != -1) {
			xVals[index] = true;
			x.add(index);
			remove(r, index);
			setTotalA(cm.addA(index, totalA));
			setObj(p.addObj(index, x, obj));
			updateValid();
		}
		return index;
	}

	public int trySub() {
		int index = p.trySub(x, false);
		if (index != -1) {
			xVals[index] = false;
			r.add(index);
			remove(x, index);
			setTotalA(cm.removeA(index, totalA));
			setObj(p.subObj(index, x, obj));
			updateValid();
		}
		return index;
	}

	public int getXSize() {
		return x.size();
	}

	public int getXItem(int i) {
		if (i >= 0 && i < x.size()) {
			return x.get(i);
		} else {
			return -1;
		}
	}

	public int getRSize() {
		return r.size();
	}

	public int getRItem(int i) {
		if (i >= 0 && i < r.size()) {
			return r.get(i);
		} else {
			return -1;
		}
	}

	public ArrayList<Integer> getX() {
		return x;
	}

	public ArrayList<Integer> getR() {
		return r;
	}

	public double getObj() {
		return obj;
	}

	public void setObj(double obj) {
		this.obj = obj;
	}

	public void calcTotalA() {
		setTotalA(cm.calcAllTotalA(x));
	}

	public int[] getTotalA() {
		return totalA;
	}

	public void setTotalA(int[] totalA) {
		this.totalA = totalA;
	}

	public boolean getValid() {
		updateValid();
		return valid;
	}

	public void setValid(boolean validity) {
		valid = validity;
	}

	public boolean getXVals(int i) {
		return xVals[i];
	}
	
	public void setXVals(int i, boolean bool) {
		xVals[i] = bool;
	}

	private void remove(ArrayList<Integer> arr, int i) {
		arr.remove(Integer.valueOf(i));
	}
	
	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(obj + "\n");
			for (int i = 0; i < cm.getM(); i++) {
				pw.write(totalA[i] + " ");
			}
			pw.write("\n");
			for (Integer i: x) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
		}
	}
	
	public void readSolution(String filename) { 
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			if (readObj != -1) {
				int[] readTotalA = new int[cm.getM()];
				for (int i = 0; i < cm.getM(); i++) {
					readTotalA[i] = scr.nextInt();
				}
				x = new ArrayList<Integer>();
				while (scr.hasNextInt()) {
					x.add(scr.nextInt());
				}
				r = new ArrayList<Integer>();
				for (int i = 0; i < n; i++) {
					r.add(i);
				}
				r.removeAll(x);
				obj = readObj;
				totalA = readTotalA;
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}
	
}
