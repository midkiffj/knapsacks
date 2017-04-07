package Solutions;
import java.util.ArrayList;

import Problems.Knapsack;

/**
 * Abstract knapsack solution class for knapsack interaction
 * 
 * @author midkiffj
 */
public abstract class KnapsackSol extends ProblemSol {

	private static Knapsack k;
	private int totalA;
	private int b;

	/**
	 * Construct a solution by generating an incumbent solution
	 */
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

	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public KnapsackSol(String filename) {
		super();
		k = (Knapsack)p;
		readSolution(filename);
		for (Integer i : getX()) {
			setXVals(i,true);
		}
		updateB();
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param ks the solution to copy
	 */
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

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
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

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
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

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weight of the solution
	 */
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

	/**
	 * Set the value of the knapsack capacity
	 * 	- Infinity if using healing algorithms
	 * 	- Otherwise, problem.getB()
	 */
	private void updateB() {
		if (useHealing) {
			b = Integer.MAX_VALUE;
		} else {
			b = k.getB();
		}
	}

	/**
	 * Update the knapsack weight given the current solution
	 */
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

	/**
	 * Add item i to the knapsack weight
	 * @param i - item to add
	 */
	public void addA(int i) {
		this.totalA += k.getA(i);
	}

	/**
	 * Remove item i from the knapsack weight
	 * @param i - item to remove
	 */
	public void removeA(int i) {
		this.totalA -= k.getA(i);
	}

	/**
	 * Return if adding item i will keep the problem feasible 
	 * 
	 * @param i - item to add
	 * @return (T) if adding i results in a valid solution
	 */
	public boolean addValid(int i) {
		if (i < n && i >= 0) {
			return ((getTotalA() + k.getA(i)) <= getB());
		} else {
			return false;
		}
	}

	/**
	 * Return if removing item i will keep the problem feasible 
	 * 
	 * @param i - item to remove
	 * @return (T) if removing i results in a valid solution
	 */
	public boolean subValid(int i) {
		if (i < n && i >= 0) {
			return ((getTotalA() - k.getA(i)) <= getB());
		}
		else {
			return false;
		}
	}

	/**
	 * Return if removing item i and adding item j
	 * 	will keep the problem feasible 
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @return (T) if swapping i and j results in a valid solution
	 */
	public boolean swapValid(int i, int j) {
		if (i < n && j < n && i >= 0 && j >= 0) {
			return ((getTotalA() + k.getA(j) - k.getA(i)) <= getB());
		} else {
			return false;
		}
	}

	public int getB() {
		return b;
	}
}
