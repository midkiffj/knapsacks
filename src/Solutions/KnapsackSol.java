package Solutions;
import java.util.ArrayList;

import Problems.Knapsack;


public abstract class KnapsackSol extends ProblemSol {

	private static Knapsack k;
	private int totalA;
	private int b;

	public KnapsackSol() {
		super();
		k = (Knapsack)p;
		k.genInit(getX(), getR());
		for (Integer i: getX()) {
			setXVals(i,true);
		}
		setObj(k.getObj(getX()));
		calcTotalA();
		updateB();
	}
	
	public KnapsackSol(String filename) {
		super();
		k = (Knapsack)p;
		readSolution(filename);
		for (Integer i : getX()) {
			setXVals(i,true);
		}
		updateB();
	}
	
	public KnapsackSol(KnapsackSol ks) {
		super();
		k = (Knapsack)p;
		setX(ks.getX());
		setR(ks.getR());
		for (Integer i : getX()) {
			setXVals(i,true);
		}
		setObj(ks.getObj());
		totalA = ks.getTotalA();
		updateB();
	}

	public KnapsackSol(boolean[] xVals) {
		super();
		k = (Knapsack)p;
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < xVals.length; i++) {
			if (xVals[i]) {
				x.add(i);
			} else {
				r.add(i);
			}
			setXVals(i,xVals[i]);
		}
		setX(x);
		setR(r);
		setObj(k.getObj(x));
		calcTotalA();
		updateB();
	}

	public KnapsackSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super();
		k = (Knapsack)p;
		setX(x);
		setR(r);
		for (Integer i: x) {
			setXVals(i,true);
		}
		setObj(k.getObj(x));
		calcTotalA();
		updateB();
	}

	public KnapsackSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA) {
		super();
		k = (Knapsack)p;
		setX(x);
		setR(r);
		for (Integer i: x) {
			setXVals(i,true);
		}
		setObj(obj);
		this.totalA = totalA;
		updateB();
	}
	
	private void updateB() {
		if (useHealing) {
			b = Integer.MAX_VALUE;
		} else {
			b = k.getB();
		}
	}

	public void calcTotalA() {
		int totalA = 0;
		for (Integer i: getX()) {
			totalA += k.getA(i);
		}
		setTotalA(totalA);
	}

	public int getTotalA() {
		return totalA;
	}

	public void setTotalA(int totalA) {
		this.totalA = totalA;
	}
	
	public void addA(int i) {
		this.totalA += k.getA(i);
	}
	
	public void removeA(int i) {
		this.totalA -= k.getA(i);
	}
	
	public int getB() {
		return b;
	}
}
