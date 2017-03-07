package Solutions;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

import Problems.MultipleKnapsack;
import Problems.Problem;
import Problems.ProblemFactory;


public abstract class MultKnapsackSol extends ProblemSol {

	private MultipleKnapsack mk = (MultipleKnapsack)p;
	private int[] totalA;
	private int[] b;
	protected int m;

	public MultKnapsackSol() {
		super();
		m = mk.getM();
		mk.genInit(getX(), getR());
		for (Integer i: getX()) {
			setXVals(i,true);
		}
		setObj(mk.getObj(getX()));
		calcTotalA();
		updateB();
	}
	
	public MultKnapsackSol(String filename) {
		super();
		m = mk.getM();
		readSolution(filename);
		for (Integer i : getX()) {
			setXVals(i,true);
		}
		updateB();
	}
	
	public MultKnapsackSol(MultKnapsackSol mks) {
		super();
		m = mk.getM();
		setX(mks.getX());
		setR(mks.getR());
		for (Integer i : mks.getX()) {
			setXVals(i,true);
		}
		setObj(mks.getObj());
		setTotalA(mks.getTotalA());
		updateB();
	}

	public MultKnapsackSol(boolean[] xVals) {
		super();
		m = mk.getM();
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < xVals.length; i++) {
			if (xVals[i]) {
				x.add(i);
				setXVals(i,true);
			} else {
				r.add(i);
			}
		}
		setX(x);
		setR(r);
		setObj(mk.getObj(x));
		calcTotalA();
		updateB();
	}

	public MultKnapsackSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super();
		m = mk.getM();
		setX(x);
		setR(r);
		for (Integer i: x) {
			setXVals(i,true);
		}
		setObj(mk.getObj(x));
		calcTotalA();
		updateB();
	}

	public MultKnapsackSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int[] totalA) {
		super();
		m = mk.getM();
		setX(x);
		setR(r);
		setObj(obj);
		setTotalA(totalA);
		for (Integer i: x) {
			setXVals(i,true);
		}
		updateB();
	}
	
	private void updateB() {
		b = new int[m];
		if (useHealing) {
			for (int i = 0; i < m; i++) {
				b[i] = Integer.MAX_VALUE;
			}
		} else {
			for (int i = 0; i < m; i++) {
				b[i] = mk.getB(i);
			}
		}
	}

	public void calcTotalA() {
		totalA = new int[m];
		for (Integer i: getX()) {
			for(int j = 0; j < m; j++) {
				totalA[j] += mk.getA(j, i);
			}
		}
	}

	public int[] getTotalA() {
		return totalA;
	}
	
	public boolean totalAValid(int[] totalA) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] > b[i]) {
				return false;
			}
		}
		return true;
	}
	
	public boolean addTotalA(int[] totalA, int j) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] + mk.getA(i,j) > b[i]) {
				return false;
			}
		}
		return true;
	}
	
	public boolean subTotalA(int[] totalA, int j) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] - mk.getA(i,j) > b[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean swapTotalA(int[] totalA, int i, int j) {
		for (int k = 0; k < m; k++) {
			if (totalA[k] + mk.getA(k,j) - mk.getA(k,i) > b[k]) {
				return false;
			}
		}
		return true;
	}

	public void setTotalA(int[] totalA) {
		this.totalA = new int[m];
		for (int i = 0; i < m; i++) {
			this.totalA[i] = totalA[i];
		}
	}
	
	public void addA(int j) {
		for (int i = 0; i < m; i++) {
			totalA[i] += mk.getA(i,j);
		}
	}
	
	public void removeA(int j) {
		for (int i = 0; i < m; i++) {
			totalA[i] -= mk.getA(i,j);
		}
	}
	
}
