package Constructive;

import java.util.ArrayList;
import java.util.Random;

import Problems.Cubic;
import Runner.RndGen;
import Solutions.CubicSol;
import Solutions.ProblemSol;

public class CubicFillUp extends ConstHeuristic {

	private Cubic c;
	private Random rnd = RndGen.getRnd();

	public CubicFillUp(Cubic c) {
		super();
		this.c = c;
	}

	protected ProblemSol construct() {
		return fillUpNExchange();
	}

	private CubicSol fillUpNExchange() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < c.getN(); i++) {
			r.add(i);
		}
		int totalA = 0;

		return fillUpNExchange(x,r,totalA);
	}

	// Complete bestImprovingSwaps or additions until no more items can be 
	//	either swapped or shifted
	private CubicSol fillUpNExchange(ArrayList<Integer> x, ArrayList<Integer> r, int totalA) {
		CubicSol current = new CubicSol(x,r);

		boolean done = false;
		double curObj = current.getObj();
		while (!done) {
			boolean swap = false;
			// Perform an operation
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

	// Perform the best improving swap that keeps the knapsack feasible
	private void bestImprovingSwap(CubicSol current) {
		// Store b
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
		if (bi != -1) {
			current.swap(bObj,bi,bj);
		}
	}

}
