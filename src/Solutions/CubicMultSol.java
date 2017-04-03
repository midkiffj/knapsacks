package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Problems.CubicMult;

/**
 * Solution class for a Cubic Multiple Knapsack Problem
 * - Mutates solution with swaps and shifts
 * - File I/O for storing solutions
 * 
 * @author midkiffj
 */
public class CubicMultSol extends MultKnapsackSol {

	private static CubicMult cm;

	/**
	 * Construct a solution by relying on the super class
	 */
	public CubicMultSol() {
		super();
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public CubicMultSol(String filename) {
		super(filename);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param cms the solution to copy
	 */
	public CubicMultSol(CubicMultSol cms) {
		super((MultKnapsackSol)cms);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
	public CubicMultSol(boolean[] xVals) {
		super(xVals);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
	public CubicMultSol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weights of the solution
	 */
	public CubicMultSol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int[] totalA) {
		super(x,r,obj,totalA);
		cm = (CubicMult)p;
		updateValid();
	}

	/**
	 * Update the validity of the solution
	 */
	public void updateValid() {
		calcTotalA();
		int[] totalA = getTotalA();
		boolean valid = true;
		for (int i = 0; i < m && valid; i++) {
			if (totalA[i] > cm.getB(i)) {
				valid = false;
			}
		}
		setValid(valid);
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
		setObj(swapObj(i,j, getX(), getObj()));
		addA(j);
		removeA(i);
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
		oldObj = oldObj - cm.getCi(i);
		oldObj = oldObj + cm.getCi(j);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - cm.getCij(i,xk);
				oldObj = oldObj + cm.getCij(j,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - cm.getDijk(i,xk,xl);
						oldObj = oldObj + cm.getDijk(j,xk,xl);
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
	private int tryAdd(int[] totalA, ArrayList<Integer> x, ArrayList<Integer> r, boolean improveOnly) {
		if (x.size() == n) {
			return -1;
		}
		double maxRatio = -1*Double.MAX_VALUE;
		int maxI = -1;
		for (Integer i: r) {
			if (addTotalA(totalA,i)) {
				double ratio = cm.getRatio(i);
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
	 * @param i - item to add
	 */
	public void addX(int i) {
		addI(i);
		addA(i);
		setObj(addObj(i));
	}
	
	/**
	 * Remove variable i from the solution
	 * 
	 * @param i - item to remove
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
		ArrayList<Integer> curX = getX();
		double oldObj = getObj() - cm.getCi(i);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj - cm.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj - cm.getDijk(i,xk,xl);
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
		ArrayList<Integer> curX = getX();
		double oldObj = getObj() + cm.getCi(i);
		for (int k = 0; k < curX.size(); k++) {
			int xk = curX.get(k);
			if (xk != i) {
				oldObj = oldObj + cm.getCij(i,xk);
				for (int l = k+1; l < curX.size(); l++) {
					int xl = curX.get(l);
					if (xl != i) {
						oldObj = oldObj + cm.getDijk(i,xk,xl);
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
			removeX(j);
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
			int[] totalA = getTotalA();
			for (int i = 0; i < cm.getM(); i++) {
				pw.write(totalA[i] + " ");
			}
			pw.write("\n");
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
		cm = (CubicMult)p;
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			if (readObj != -1) {
				int[] readTotalA = new int[cm.getM()];
				for (int i = 0; i < cm.getM(); i++) {
					readTotalA[i] = scr.nextInt();
				}
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
