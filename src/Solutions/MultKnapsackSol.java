package Solutions;
import java.util.ArrayList;
import Problems.MultipleKnapsack;

/**
 * Abstract multiple knapsack solution class for knapsack interaction
 * 
 * @author midkiffj
 */
public abstract class MultKnapsackSol extends ProblemSol {

	private MultipleKnapsack mk = (MultipleKnapsack)p;
	private int[] totalA;
	private int[] b;
	protected int m;

	/**
	 * Construct a solution by generating an incumbent solution
	 */
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
	
	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public MultKnapsackSol(String filename) {
		super();
		m = mk.getM();
		readSolution(filename);
		for (Integer i : getX()) {
			setXVals(i,true);
		}
		updateB();
	}
	
	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param mks the solution to copy
	 */
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

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
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

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
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

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weights of the solution
	 */
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
	
	/**
	 * Set the value of the knapsack capacity
	 * 	- Infinity if using healing algorithms
	 * 	- Otherwise, problem.getB()
	 */
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
	
	/**
	 * Update the knapsack weight given the current solution
	 */
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
	
	/**
	 * Return if adding item i will keep the problem feasible 
	 * 
	 * @param i - item to add
	 * @return (T) if adding i results in a valid solution
	 */
	public boolean addValid(int i) {
		return addTotalA(getTotalA(),i);
	}
	
	/**
	 * Return if removing item i will keep the problem feasible 
	 * 
	 * @param i - item to remove
	 * @return (T) if removing i results in a valid solution
	 */
	public boolean subValid(int i) {
		return subTotalA(getTotalA(),i);
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
		return swapTotalA(getTotalA(),i,j);
	}
	
	/**
	 * Return if adding the item to the given weight 
	 * 	will keep the problem feasible
	 * 
	 * @param totalA solution weights
	 * @param j - item to add
	 * @return (T) if j can be added and maintain problem feasibility
	 */
	public boolean addTotalA(int[] totalA, int j) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] + mk.getA(i,j) > b[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Return if removing the item to the given weight 
	 * 	will keep the problem feasible
	 * 
	 * @param totalA solution weights
	 * @param j - item to remove
	 * @return (T) if j can be remove and maintain problem feasibility
	 */
	public boolean subTotalA(int[] totalA, int j) {
		for (int i = 0; i < m; i++) {
			if (totalA[i] - mk.getA(i,j) > b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return if swapping the items to the given weight 
	 * 	will keep the problem feasible
	 * 
	 * @param totalA solution weights
	 * @param i - item to remove
	 * @param j - item to add
	 * @return (T) if i,j can be swapped and maintain problem feasibility
	 */
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
