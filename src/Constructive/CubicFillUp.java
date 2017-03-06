package Constructive;

import java.util.ArrayList;
import java.util.Random;

import Problems.Cubic;
import Runner.RndGen;
import Solutions.CubicSol;
import Solutions.ProblemSol;

/**
 * Fill Up and Exchange Heuristic for the Cubic Knapsack
 * (does what the name implies)
 * 
 * @author midkiffj
 */
public class CubicFillUp extends ConstHeuristic {

	private Cubic c;
	private Random rnd = RndGen.getRnd();

	/**
	 * Specify the problem to solve
	 * 
	 * @parm c Cubic problem
	 */
	public CubicFillUp(Cubic c) {
		super();
		this.c = c;
	}

	/**
	 * Perform the construction of the solution
	 * 
	 * @return solution constructed
	 */
	protected ProblemSol construct() {
		return fillUpNExchange();
	}

	/**
	 * Create lists to store solution and call sub-method
	 */
	private CubicSol fillUpNExchange() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < c.getN(); i++) {
			r.add(i);
		}
		int totalA = 0;

		return fillUpNExchange(x,r,totalA);
	}
	
	/**
	 * Complete bestImprovingSwaps or additions until no more items can be 
	 *	either swapped or added
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items outside solution
	 * @param totalA - current knapsack capactiy
	 * @return solution constructed
	 */
	private CubicSol fillUpNExchange(ArrayList<Integer> x, ArrayList<Integer> r, int totalA) {
		CubicSol current = new CubicSol(x,r);

		boolean done = false;
		double curObj = current.getObj();
		while (!done) {
			boolean swap = false;
			// Perform an operation (50/50 swap or shift)
			if (rnd.nextDouble() < 0.5) {
				bestImprovingSwap(current);
				swap = true;
			} else {
				tryAdd(current);
			}
			
			// If no change, try the opposite operation
			if (curObj == current.getObj()) {
				if (swap) {
					tryAdd(current);
				} else {
					bestImprovingSwap(current);
				}
			}
			
			// If no change overall, stop
			if (curObj == current.getObj()) {
				done = true;
			} else {
				curObj = current.getObj();
			}
		}

		return current;
	}
	
	/**
	 * Try to add a variable to the solution, maintaining knapsack feasibility
	 * 
	 * @param current solution to improve
	 */
	private void tryAdd(CubicSol current) {
		double maxChange = 0;
		int maxI = -1;
		// Check all possible shifts
		for(Integer i: current.getR()) {
			// Knapsack feasibility
			if (current.getTotalA() + c.getA(i) <= c.getB()) {
				double obj = current.getObj() + c.getCi(i);
				for (int j = 0; j < current.getXSize(); j++) {
					int xj = current.getX().get(j);
					obj += c.getCij(i,xj);
					for (int k = j+1; k < current.getXSize(); k++) {
						int xk = current.getX().get(k);
						obj += c.getDijk(i,xj,xk);
					}
				}
				// Track best improving addition
				double change = obj - current.getObj();
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		if (maxI != -1) {
			current.addA(maxI);
			current.addI(maxI);
			current.setObj(current.getObj() + maxChange);
		}
	}

	/**
	 *  Perform the best improving swap that keeps the knapsack feasible
	 *  
	 *  @param current solution to improve
	 */
	private void bestImprovingSwap(CubicSol current) {
		// Get b
		int b = c.getB();
		int curTotalA = current.getTotalA();
		
		// Store best swaps
		int bi = -1;
		int bj = -1;
		double bObj = current.getObj();
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (c.getA(j)-c.getA(i) <= b - curTotalA) {
					double newObj = current.swapObj(i, j);
					if (newObj > bObj) {
						bi = i;
						bj = j;
						bObj = newObj;
					}
				}
			}
		}
		
		// Swap the best improving swap (if found)
		if (bi != -1) {
			current.swap(bi,bj);
		}
	}

}
