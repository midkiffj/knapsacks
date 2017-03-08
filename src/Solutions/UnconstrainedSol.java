package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.Unconstrained;

/**
 * Solution class for a Cubic Problem
 * - Mutates solution with swaps and shifts
 * - File I/O for storing solutions
 * 
 * @author midkiffj
 */
public class UnconstrainedSol extends ProblemSol {

	private Unconstrained u;

	/**
	 * Construct a solution by calling the problem initial solution generator
	 */
	public UnconstrainedSol() {
		super();
		u = (Unconstrained)p;
		u.genRndInit(getX(), getR());
		for (Integer i: getX()) {
			setXVals(i,true);
		}
		setObj(u.getObj(getX()));
		updateValid();
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param us the solution to copy
	 */
	public UnconstrainedSol(UnconstrainedSol us) {
		super();
		u = (Unconstrained)p;
		setX(us.getX());
		setR(us.getR());
		for (Integer i : getX()) {
			setXVals(i,true);
		}
		setObj(us.getObj());
		updateValid();
	}

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
	public UnconstrainedSol(boolean[] xVals) {
		super();
		u = (Unconstrained)p;
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
		setObj(u.getObj(x));
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
	public UnconstrainedSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super();
		u = (Unconstrained)p;
		setX(x);
		setR(r);
		for (Integer i: x) {
			setXVals(i,true);
		}
		setObj(u.getObj(x));
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 */
	public UnconstrainedSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj) {
		super();
		u = (Unconstrained)p;

		setX(x);
		setR(r);
		for (Integer i: x) {
			setXVals(i,true);
		}
		setObj(obj);
		updateValid();
	}
	
	@Override
	public void updateValid() {
		setValid(true);
	}
	
	/**
	 * Return if adding item i will keep the problem feasible 
	 * 
	 * @param i - item to add
	 * @return (T) if adding i results in a valid solution
	 */
	public boolean addValid(int i) {
		return true;
	}
	
	/**
	 * Return if removing item i will keep the problem feasible 
	 * 
	 * @param i - item to remove
	 * @return (T) if removing i results in a valid solution
	 */
	public boolean subValid(int i) {
		return true;
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
		return true;
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
		setObj(swapObj(i, j, getX(), getObj()));
		removeI(i);
		addI(j);
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
		return swapObj(i, j, getX(), getObj());
	}
	
	/**
	 * Calculate the objective of curX 
	 * 	if item i is removed and item j is added
	 * 
	 * @param i - item to be removed
	 * @param j - item to be added
	 * @param curX - current solution
	 * @param oldObj - current solution objective value
	 * @return the new objective if i and j swapped
	 */
	private double swapObj(int i, int j, ArrayList<Integer> curX, double oldObj) {
		oldObj = oldObj - u.getCi(i);
		oldObj = oldObj + u.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - u.getCij(i,xk);
				oldObj = oldObj + u.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - u.getDijk(i,xk,xl);
						oldObj = oldObj + u.getDijk(j,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	/**
	 * Try to add a variable to the solution
	 * 
	 * @return the item added or -1 if none added
	 */
	public int tryAdd() {
		int index = tryAdd(getX(), getR());
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
		int index = trySub(getX());
		if (index != -1) {
			removeX(index);
		}
		return index;
	}

	/**
	 * Try to add an item to the given solution
	 * - Add the maxRatio item
	 * 
	 * @param x - solution list
	 * @param r - list of items not in the solution
	 * @return the item to add or -1 if none to add
	 */
	private int tryAdd(ArrayList<Integer> x, ArrayList<Integer> r) {
		double obj = getObj();
		int index = -1;
		for (int i: r) {
			double newObj = addObj(i);
			if (newObj > obj) {
				obj = newObj;
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * Try to remove an item from the given solution
	 * - Remove the minRatio item
	 * 
	 * @param x - solution list
	 * @return the item to remove or -1 if none to remove
	 */
	private int trySub(ArrayList<Integer> x) {
		double obj = getObj();
		int index = -1;
		for (int i: x) {
			double newObj = subObj(i);
			if (newObj > obj) {
				obj = newObj;
				index = i;
			}
		}
		return index;
	}
	
	@Override
	/**
	 * Add variable i to the solution
	 * 
	 * @param i - the item to add
	 */
	public void addX(int i) {
		addI(i);
		setObj(addObj(i));
	}

	@Override
	/**
	 * Remove variable i to the solution
	 * 
	 * @param i - the item to remove
	 */
	public void removeX(int i) {
		removeI(i);
		setObj(subObj(i));
	}

	/**
	 * Calculate the objective if item i is removed from the solution
	 * 
	 * @param i - item to remove
	 * @return calculated objective
	 */
	private double subObj(int i) {
		double oldObj = getObj() - u.getCi(i);
		for (int k = 0; k < getXSize(); k++) {
			int xk = getX().get(k);
			if (xk != i) {
				oldObj = oldObj - u.getCij(i,xk);
				for (int l = k+1; l < getXSize(); l++) {
					int xl = getX().get(l);
					if (xl != i) {
						oldObj = oldObj - u.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	/**
	 * Calculate the objective if item i is added from the solution
	 * 
	 * @param i - item to add
	 * @return calculated objective
	 */
	private double addObj(int i) {
		double oldObj = getObj() + u.getCi(i);
		for (int k = 0; k < getXSize(); k++) {
			int xk = getX().get(k);
			if (xk != i) {
				oldObj = oldObj + u.getCij(i,xk);
				for (int l = k+1; l < getXSize(); l++) {
					int xl = getX().get(l);
					if (xl != i) {
						oldObj = oldObj + u.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	@Override
	/**
	 * Comparison for solutions used in genetic algorithm
	 * 
	 * 1) Invalid < Valid
	 * 2) Lower objective is better
	 */
	public int compareTo(ProblemSol o) 	{
		if (o.getValid() && this.getValid() || !(o.getValid() && this.getValid())) {
			double diff = this.getObj() - o.getObj();
			if (diff >= 0) {
				return -1;
			} else {
				return 1;
			}
		} else {
			if (o.getValid()) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	@Override
	/**
	 * Heal the solution if it is invalid
	 */
	public void healSol() {
		// Unneeded: There are no infeasible solutions to the Unconstrained Cubic.
		return;
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
			Collections.sort(getX());
			for (Integer i: getX()) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
		}
	}

	/**
	 * Read a solution from the given filename
	 * 
	 * @param filename to read
	 */
	public void readSolution(String filename) { 
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
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
				setObj(readObj);
				setX(readX);
				setR(readR);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}
}
