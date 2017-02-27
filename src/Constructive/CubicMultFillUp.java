package Constructive;

import java.util.ArrayList;
import java.util.Random;

import Problems.CubicMult;
import Runner.RndGen;
import Solutions.CubicMultSol;
import Solutions.ProblemSol;

/**
 * Fill Up and Exchange Heuristic for the Cubic Multiple Knapsack
 * (does what the name implies)
 * 
 * @author midkiffj
 */
public class CubicMultFillUp extends ConstHeuristic {

	private CubicMult cm;
	private Random rnd = RndGen.getRnd();

	/*
	 *  Specify problem to create solution
	 */
	public CubicMultFillUp(CubicMult cm) {
		super();
		this.cm = cm;
	}

	protected ProblemSol construct() {
		return fillUpNExchange();
	}

	/*
	 *  Create lists to store solution and call sub-method
	 */
	private CubicMultSol fillUpNExchange() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < cm.getN(); i++) {
			r.add(i);
		}

		return fillUpNExchange(x,r);
	}

	/* 
	 * Complete bestImprovingSwaps or additions until no more items can be 
	 *	either swapped or added
	 */
	private CubicMultSol fillUpNExchange(ArrayList<Integer> x, ArrayList<Integer> r) {
		CubicMultSol current = new CubicMultSol(x,r);

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
	
	/*
	 * Try to add a variable to the solution, maintaining knapsack feasibility
	 */
	private void tryAdd(CubicMultSol current) {
		double maxChange = 0;
		int maxI = -1;
		// Check all possible shifts
		for(Integer i: current.getR()) {
			// Knapsack feasibility
			if (current.addTotalA(current.getTotalA(), i)) {
				double obj = current.getObj() + cm.getCi(i);
				for (int j = 0; j < current.getXSize(); j++) {
					int xj = current.getX().get(j);
					obj += cm.getCij(i,xj);
					for (int k = j+1; k < current.getXSize(); k++) {
						int xk = current.getX().get(k);
						obj += cm.getDijk(i,xj,xk);
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

	/*
	 *  Perform the best improving swap that keeps the knapsack feasible
	 */
	private void bestImprovingSwap(CubicMultSol current) {
		// Store best swaps
		int bi = -1;
		int bj = -1;
		double bObj = current.getObj();
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (current.swapTotalA(current.getTotalA(), i, j)) {
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
