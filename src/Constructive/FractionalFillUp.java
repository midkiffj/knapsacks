package Constructive;

import java.util.ArrayList;
import java.util.Random;

import Problems.Fractional;
import Runner.RndGen;
import Solutions.FractionalSol;
import Solutions.ProblemSol;

/**
 * Fill Up and Exchange Heuristic for the Cubic Knapsack
 * (does what the name implies)
 * 
 * @author midkiffj
 */
public class FractionalFillUp extends ConstHeuristic {

	private Fractional f;
	private Random rnd = RndGen.getRnd();

	/**
	 * Specify the problem to solve
	 * 
	 * @param c Cubic problem
	 */
	public FractionalFillUp(Fractional f) {
		super();
		this.f = f;
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
	private FractionalSol fillUpNExchange() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> r = new ArrayList<Integer>();
		for (int i = 0; i < f.getN(); i++) {
			r.add(i);
		}

		return fillUpNExchange(x,r);
	}

	/**
	 * Complete bestImprovingSwaps or additions until no more items can be 
	 *	either swapped or added
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items outside solution
	 * @return solution constructed
	 */
	private FractionalSol fillUpNExchange(ArrayList<Integer> x, ArrayList<Integer> r) {
		FractionalSol current = new FractionalSol(x,r);

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
	private void tryAdd(FractionalSol current) {
		double maxChange = 0;
		int maxI = -1;
		// Check all possible shifts
		for(Integer i: current.getR()) {
			// Knapsack feasibility
			if (current.addValid(i)) {
				double obj = addObj(i,current.getNum(),current.getDen());
				// Track best improving addition
				double change = obj - current.getObj();
				if (change > maxChange) {
					maxChange = change;
					maxI = i;
				}
			}
		}
		if (maxI != -1) {
			current.addX(maxI);
		}
	}

	/**
	 * Calculate the objective if item i is added to the solution
	 * 
	 * @param i - item to add
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double addObj(int i, long[] num, long[] den) {
		double obj = 0;
		for (int j = 0; j < f.getM(); j++) {
			if (den[j]+f.getD(j,i) == 0) {
				return -1*Double.MAX_VALUE;
			}
			obj += (double)(num[j]+f.getC(j,i))/(den[j]+f.getD(j,i));
		}

		return obj;
	}

	/**
	 *  Perform the best improving swap that keeps the knapsack feasible
	 *  
	 *  @param current solution to improve
	 */
	private void bestImprovingSwap(FractionalSol current) {
		// Store best swaps
		int bi = -1;
		int bj = -1;
		double bObj = current.getObj();
		for(Integer i: current.getX()) {
			for(Integer j: current.getR()) {
				// Check for knapsack feasibility
				if (current.swapValid(i, j)) {
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
