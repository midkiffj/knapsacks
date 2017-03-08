package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.Cubic;

/**
 * Solution class for a Cubic Problem
 * - Mutates solution with swaps and shifts
 * - File I/O for storing solutions
 * 
 * @author midkiffj
 */
public class CubicSol extends KnapsackSol {

	private static Cubic c;

	/**
	 * Construct a solution by relying on the super class
	 */
	public CubicSol() {
		super();
		c = (Cubic)p;
	}

	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public CubicSol(String filename) {
		super(filename);
		c = (Cubic)p;
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param cs the solution to copy
	 */
	public CubicSol(CubicSol cs) {
		super((KnapsackSol)cs);
		c = (Cubic)p;
	}

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
	public CubicSol(boolean[] xVals) {
		super(xVals);
		c = (Cubic)p;
	}

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
	public CubicSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		c = (Cubic)p;
	}

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weight of the solution
	 */
	public CubicSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA) {
		super(x,r,obj,totalA);
		c = (Cubic)p;
	}

	/**
	 * Update the validity of the solution
	 */
	public void updateValid() {
		calcTotalA();
		if (getTotalA() <= c.getB()) {
			setValid(true);
		} else {
			setValid(false);
		}
	}

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
		setObj(swapObj(i,j));
		removeA(i);
		addA(j);
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
		return swapObj(i,j,getX(),getObj());
	}

	/**
	 * Calculate the objective if item i is remove from the solution
	 * 	and item j is added to the solution.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param curX - current solution list
	 * @param oldObj - current objective
	 * @return calculated objective
	 */
	private double swapObj(int i, int j, ArrayList<Integer> curX, double oldObj) {
		oldObj = oldObj - c.getCi(i);
		oldObj = oldObj + c.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - c.getCij(i,xk);
				oldObj = oldObj + c.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - c.getDijk(i,xk,xl);
						oldObj = oldObj + c.getDijk(j,xk,xl);
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
		int index = tryAdd(getTotalA(), getX(), getR(), false);
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
		int index = trySub(getX(), false);
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
	 * @return the item to add or -1 if none to add
	 */
	private int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly) {
		if (x.size() == n) {
			return -1;
		}
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (totalA + c.getA(i) <= getB()) {
				double ratio = c.getRatio(i);
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
			double change = addObj(maxI);
			if (change > getObj()) {
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
	 * @return the item to remove or -1 if none to remove
	 */
	private int trySub(ArrayList<Integer> x, boolean improveOnly) {
		if (x.size() <= 1) {
			return -1;
		}
		int minI = minRatio(0);

		if (minI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = subObj(minI);
			if (change > getObj()) {
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
	 * @param i - the item to add
	 */
	public void addX(int i) {
		addI(i);
		addA(i);
		setObj(addObj(i));
	}
	
	/**
	 * Remove variable i from the solution
	 * 
	 * @param i - the item to remove
	 */
	public void removeX(int i) {
		removeI(i);
		removeA(i);
		setObj(subObj(i));
	}

	/**
	 * Calculate the objective if item i is removed from the solution
	 * 
	 * @param i - item to remove
	 * @return calculated objective
	 */
	private double subObj(int i) {
		double oldObj = getObj() - c.getCi(i);
		ArrayList<Integer> curX = getX();
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - c.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - c.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
	}

	/**
	 * Calculate the objective if item i is added to the solution
	 * 
	 * @param i - item to add
	 * @return calculated objective
	 */
	private double addObj(int i) {
		double oldObj = getObj() + c.getCi(i);
		ArrayList<Integer> curX = getX();
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj + c.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj + c.getDijk(i,xk,xl);
					}
				}
			}
		}
		return oldObj;
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
				double newObj = subObj(i);
				if (newObj > maxObj) {
					maxObj = newObj;
					maxI = i;
				}
			}
			if (maxI != -1) {
				removeX(maxI);
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
			setObj(subObj(j));
			removeA(j);
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
		c = (Cubic)p;
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			int readTotalA = scr.nextInt();
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
				setTotalA(readTotalA);
				setX(readX);
				setR(readR);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}
}
