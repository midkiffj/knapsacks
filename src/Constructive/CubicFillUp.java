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

	/*
	 *  Specify problem to create solution
	 */
	public CubicFillUp(Cubic c) {
		super();
		this.c = c;
	}

	protected ProblemSol construct() {
		return fillUpNExchange();
	}

	/*
	 *  Create lists to store solution and call sub-method
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

	/* 
	 * Complete bestImprovingSwaps or additions until no more items can be 
	 *	either swapped or added
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
				current.tryAdd();
			}
			
			// If no change, try the opposite operation
			if (curObj == current.getObj()) {
				if (swap) {
					current.tryAdd();
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
	 *  Perform the best improving swap that keeps the knapsack feasible
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
			current.swap(bObj,bi,bj);
		}
	}

}
