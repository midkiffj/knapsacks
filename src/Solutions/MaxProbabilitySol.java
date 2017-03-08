package Solutions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import ExactMethods.Knapsack_Frac;
import Problems.MaxProbability;

/**
 * Solution class for a Max Probability Problem
 * - Mutates solution with swaps and shifts
 * - File I/O for storing solutions
 * 
 * @author midkiffj
 */
public class MaxProbabilitySol extends KnapsackSol {

	private static MaxProbability mp;

	private double num;
	private double den;
	private int totalU;

	/**
	 * Construct a solution by relying on the super class
	 */
	public MaxProbabilitySol() {
		super();
		mp = (MaxProbability)p;
		num = mp.getNum();
		den = mp.getDen();
		calcTotalU();
	}

	/**
	 * Construct a solution from the given file
	 * 
	 * @param filename to read
	 */
	public MaxProbabilitySol(String filename) {
		super(filename);
		mp = (MaxProbability)p;
	}

	/**
	 * Construct a solution that is equivalent to the solution passed in
	 * 
	 * @param mps the solution to copy
	 */
	public MaxProbabilitySol(MaxProbabilitySol mps) {
		super(mps);
		mp = (MaxProbability)p;
		totalU = mps.getTotalU();
		num = mps.getNum();
		den = mps.getDen();
	}

	/**
	 * Construct a solution with the given solution lists, objective, and knapsack weight
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 * @param obj - objective of the solution
	 * @param totalA - weight of the solution
	 * @param totalU - profit of the solution
	 * @param num - solution numerator value
	 * @param den - solution denominator value
	 */
	public MaxProbabilitySol(ArrayList<Integer> x, ArrayList<Integer> r, double obj, int totalA, double num, double den) {
		super(x,r,obj,totalA);
		mp = (MaxProbability)p;
		calcTotalU();
		this.num = num;
		this.den = den;
	}

	/**
	 * Construct a solution with the given xVals
	 * 
	 * @param xVals (T) if item i is in the solutions
	 */
	public MaxProbabilitySol(boolean[] newXVals) {
		super(newXVals);
		mp = (MaxProbability)p;
		calcTotalU();
		num = mp.getNum();
		den = mp.getDen();
	}

	/**
	 * Construct a solution with the given solution lists
	 * 
	 * @param x - list of items in solution
	 * @param r - list of items not in solution
	 */
	public MaxProbabilitySol(ArrayList<Integer> x, ArrayList<Integer> r) {
		super(x,r);
		mp = (MaxProbability)p;
		calcTotalU();
		num = mp.getNum();
		den = mp.getDen();
		updateValid();
	}

	/**
	 * Calculate the total profit of the solution
	 */
	private void calcTotalU() {
		totalU = 0;
		for (int i: getX()) {
			totalU += mp.getU(i);
		}
	}
	
	/**
	 * Update the validity of the solution
	 */
	public void updateValid() {
		calcTotalU();
		if (getTotalA() <= mp.getB() && totalU >= mp.getT()) {
			setValid(true);
		} else {
			setValid(false);
		}
	}
	
	/**
	 * Return if adding item i will keep the problem feasible 
	 * 
	 * @param i - item to add
	 * @return (T) if adding i results in a valid solution
	 */
	public boolean addValid(int i) {
		return ((getTotalA() + mp.getA(i)) <= getB() && getTotalU() + mp.getU(i) >= mp.getT());
	}
	
	/**
	 * Return if removing item i will keep the problem feasible 
	 * 
	 * @param i - item to remove
	 * @return (T) if removing i results in a valid solution
	 */
	public boolean subValid(int i) {
		return ((getTotalA() - mp.getA(i)) <= getB() && getTotalU() - mp.getU(i) >= mp.getT());
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
		return ((getTotalA() + mp.getA(j) - mp.getA(i)) <= getB() && getTotalU() + mp.getU(j) - mp.getU(i) >= mp.getT());
	}

	public int getTotalU() {
		return totalU;
	}

	public void addU(int i) {
		this.totalU += mp.getU(i);
	}

	public void removeU(int i) {
		this.totalU -= mp.getU(i);
	}

	public double getNum() {
		return num;
	}

	public double getDen() {
		return den;
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
		addA(j);
		removeA(i);
		addU(j);
		removeU(i);
		setObj(swapObj(i,j));
		this.num = swapNum(i,j,num);
		this.den = swapDen(i,j,den);
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
		double num = swapNum(i, j, this.num);
		double den = swapDen(i, j, this.den);
		return (num*num)/den;
	}
	
	/**
	 * Calculate the numerator values if item i is removed 
	 * 	and item j is added to the value.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param num - numerator value
	 * @return calculated objective
	 */
	private double swapNum(int i, int j, double num) {
		return num + mp.getU(j) - mp.getU(i);
	}

	/**
	 * Calculate the denominator values if item i is removed 
	 * 	and item j is added to the value.
	 * 
	 * @param i - item to remove
	 * @param j - item to add
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double swapDen(int i, int j, double den) {
		return den + mp.getS(j) - mp.getS(i);

	}

	/**
	 * Try to add a variable to the solution
	 * 
	 * @return the item added or -1 if none added
	 */
	public int tryAdd() {
		int index = tryAdd(getTotalA(), getX(), getR(), false, num, den);
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
		int index = trySub(totalU, getX(), false, num, den);
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
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the item to add or -1 if none to add
	 */
	private int tryAdd(int totalA, ArrayList<Integer> x, ArrayList<Integer> r, 
			boolean improveOnly, double num, double den) {
		if (r.size() < 1) {
			return -1;
		}

		int maxI = -1;
		int b = this.getB();
		double maxRatio = -1*Double.MAX_VALUE;
		for (Integer i: r) {
			if (totalA + mp.getA(i) <= b) {
				double ratio = mp.getRatio(i);
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
			double change = addObj(maxI, x, num, den);
			if (change > (num*num)/den) {
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
	 * @param num - current numerator values
	 * @param den - current denominator values
	 * @return the item to remove or -1 if none to remove
	 */
	private int trySub(int totalU, ArrayList<Integer> x, boolean improveOnly, 
			double num, double den) {
		if (x.size() <= 1) {
			return -1;
		}

		int minI = minRatio(0);

		if (minI == -1) {
			return -1;
		}
		if (improveOnly) {
			double change = subObj(minI, x, num, den);
			if (change > (num*num)/den) {
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
		addU(i);
		setObj(addObj(i, getX(), num, den));
		num = addNum(i, num);
		den = addDen(i, den);
	}
	
	/**
	 * Remove variable i from the solution
	 * 
	 * @param i - item to remove
	 */
	public void removeX(int i) {
		super.removeI(i);
		removeA(i);
		removeU(i);
		setObj(subObj(i, getX(), num, den));
		num = subNum(i, num);
		den = subDen(i, den);
	}

	/**
	 * Calculate the objective if item i is removed from the solution
	 * 
	 * @param i - item to remove
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double subObj(int i, ArrayList<Integer> x, double num,
			double den) {
		num -= mp.getU(i);
		den -= mp.getS(i);
		return (num*num)/den;
	}

	/**
	 * Calculate the objective if item i is added to the solution
	 * 
	 * @param i - item to add
	 * @param num - numerator values
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double addObj(int i, ArrayList<Integer> x, double num,
			double den) {
		num += mp.getU(i);
		den += mp.getS(i);
		return (num*num)/den;
	}
	
	/**
	 * Calculate the numerator values if item i is removed
	 * 
	 * @param i - item to remove
	 * @param num - numerator value
	 * @return calculated objective
	 */
	private double subNum(int i, double num) {
		return num - mp.getU(i);
	}

	/**
	 * Calculate the numerator values if item i is added
	 * 
	 * @param i - item to add
	 * @param num - numerator value
	 * @return calculated objective
	 */
	private double addNum(int i, double num) {
		return num + mp.getU(i);
	}
	
	/**
	 * Calculate the denominator values if item i is removed 
	 * 
	 * @param i - item to remove
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double subDen(int i, double den) {
		return den - mp.getS(i);
	}

	/**
	 * Calculate the denominator values if item i is added 
	 * 
	 * @param i - item to add
	 * @param den - denominator values
	 * @return calculated objective
	 */
	private double addDen(int i, double den) {
		return den + mp.getS(i);
	}

	/**
	 * Perform a crossover mutation with the specified solution
	 * - Keeps items in the solution that appear in both solutions
	 * - Fills the solution with max-ratio items until full
	 * - If the final solution is infeasible, 
	 *    instead fill the solution by solving a knapsack to maximize the profit
	 *    (Note: Infeasible solutions can still result)
	 * 
	 * @param ps2 - solution to combine
	 * @return solution after crossover
	 */
	public ProblemSol crossover(ProblemSol ps2) {
		MaxProbabilitySol mps2 = (MaxProbabilitySol)ps2;
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		int newTotalA = 0;
		int newTotalU = 0;
		for (int i = 0; i < n; i++) {
			if (this.getXVals(i) == mps2.getXVals(i)) {
				if (this.getXVals(i)) {
					x.add(i);
					newTotalA += mp.getA(i);
					newTotalU += mp.getU(i);
				} else {
					r.add(i);
				}
			} else {
				r.add(i);
			}
		}

		ArrayList<Integer> tempR = new ArrayList<Integer>(r);
		ArrayList<Integer> tempX = new ArrayList<Integer>(x);


		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < mp.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newTotalA + mp.getA(rni.x) <= getB()) {
				ratio.remove(i);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += mp.getA(rni.x);
				newTotalU += mp.getU(rni.x);
			} else {
				ratio.remove(i);
			}
		}

		if (newTotalU < mp.getT()) {
			x = tempX;
			r = tempR;
			newTotalA = 0;
			newTotalU = 0;
			for (Integer i: x) {
				newTotalA += mp.getA(i);
				newTotalU += mp.getU(i);
			}

			int rB = mp.getB() - newTotalA;
			int rTarget = (int) (mp.getT() - newTotalU);
			ArrayList<Integer> toAdd = bestFill(r,rB,rTarget);
			if (toAdd == null) {
				return new MaxProbabilitySol(mps2.getX(),mps2.getR());
			}
			for (Integer i: toAdd) {
				x.add(i);
				r.remove(Integer.valueOf(i));
				newTotalA += mp.getA(i);
				newTotalU += mp.getU(i);
			}
		}

		if (newTotalU < mp.getT()) {
			System.err.println("Infeasible generated");
		}

		return new MaxProbabilitySol(x,r);
	}

	/**
	 * Perform a mutation for the genetic algorithm
	 * 
	 * @param removeAttempts - number of genMutate2 calls
	 * @return mutated solution
	 */
	public ProblemSol genMutate(int removeAttempts) {
		MaxProbabilitySol newMP = new MaxProbabilitySol(this);
		if (rnd.nextDouble() < 0.5) {
			if (newMP.getRSize() == 0) {
				newMP.shift();
			} else {
				return newMP.mutate();
			}
		} else {
			newMP = genMutate2(newMP, removeAttempts);
		}
		return newMP;
	}

	/**
	 * Randomly remove a number of items from the solution and fill with max-ratio items
	 * - If the final solution is infeasible, 
	 *    instead fill the solution by solving a knapsack to maximize the profit
	 *    (Note: Infeasible solutions can still result)
	 * 
	 * @param mps - the solution to mutate
	 * @param removeAttempts - the number of items to remove (increases with each call)
	 * @return mutated solution
	 */
	private MaxProbabilitySol genMutate2(MaxProbabilitySol mps, int removeAttempts) {
		// Remove s items from the solution
		ArrayList<Integer> x = new ArrayList<Integer>(mps.getX());
		ArrayList<Integer> r = new ArrayList<Integer>(mps.getR());

		int s = removeAttempts;
		if (s >= x.size()) {
			s = x.size()-1;
		}
		for (int i = 0; i < s; i++) {
			int j = rnd.nextInt(x.size());
			r.add(x.remove(j));
		}

		// Calc solution capacity
		int newTotalA = 0;
		int newTotalU = 0;
		for (Integer i: x) {
			newTotalA += mp.getA(i);
			newTotalU += mp.getU(i);
		}

		ArrayList<Integer> tempR = new ArrayList<Integer>(r);
		ArrayList<Integer> tempX = new ArrayList<Integer>(x);
		ArrayList<ratioNode> ratio = computeRatios(x, r);

		while (ratio.size() > 0 && newTotalA < mp.getB()) {
			int i = rnd.nextInt(ratio.size());
			int j = rnd.nextInt(ratio.size());
			i = Math.max(i,j);
			ratioNode rni = ratio.get(i);
			if (newTotalA + mp.getA(rni.x) <= getB()) {
				ratio.remove(i);
				//				updateRatios(x, ratio, rni.x);
				//				Collections.sort(ratio);
				x.add(rni.x);
				r.remove(Integer.valueOf(rni.x));
				newTotalA += mp.getA(rni.x);
				newTotalU += mp.getU(rni.x);
			} else {
				ratio.remove(i);
			}
		}

		if (newTotalU < mp.getT()) {
			x = tempX;
			r = tempR;
			newTotalA = 0;
			newTotalU = 0;
			for (Integer i: x) {
				newTotalA += mp.getA(i);
				newTotalU += mp.getU(i);
			}

			int rB = mp.getB() - newTotalA;
			int rTarget = (int) (mp.getT() - newTotalU);
			ArrayList<Integer> toAdd = bestFill(r,rB,rTarget);
			if (toAdd == null) {
				return new MaxProbabilitySol(mps.getX(),mps.getR());
			}
			for (Integer i: toAdd) {
				x.add(i);
				r.remove(Integer.valueOf(i));
				newTotalA += mp.getA(i);
				newTotalU += mp.getU(i);
			}
		}

		if (newTotalU < mp.getT() || newTotalA > mp.getB()) {
			System.err.println("Infeasible generated");
		}

		// Calculate obj of new solution
		return new MaxProbabilitySol(x,r);
	}

	/**
	 * Creates a list of the items in r that maximize the profit given the capacity b (Knapsack)
	 * The profit gained must reach the target.
	 * 
	 * @param r - list of items to consider
	 * @param b - capacity left in knapsack
	 * @param target - target profit to obtain
	 * @return list of items that fill the weight b and maximize the profit or null if the target isn't reached
	 */
	private ArrayList<Integer> bestFill(ArrayList<Integer> r, int b, int target) {
		int[] a = new int[r.size()];
		int[] c = new int[r.size()];
		for (int i = 0; i < r.size(); i++) {
			int x = r.get(i);
			a[i] = mp.getA(x);
			c[i] = mp.getU(x);
		}
		Knapsack_Frac k = new Knapsack_Frac(a,b,c,false);
		if (k.getBestObj() < target) {
			return null;
		}
		boolean[] xVals = k.getXVals();
		ArrayList<Integer> toAdd = new ArrayList<Integer>();
		for (int i = 0; i < xVals.length; i++) {
			if (xVals[i]) {
				toAdd.add(r.get(i));
			}
		}
		return toAdd;
	}

	@Override
	/**
	 * Heal the solution if it is invalid
	 */
	public void healSol() {
		System.err.println("Max Prob Healing not fully implemented");
		System.exit(-1);
		healSolImproving();
		healSolRatio();
	}

	/**
	 * Heal the solution by removing the item that results in the best objective
	 *  until the solution is valid.
	 */
	public void healSolImproving() {
		while(!this.getValid()) {
			double maxObj = -1*Double.MAX_VALUE;
			int maxI = -1;
			for (Integer i: this.getX()) {
				double newObj = subObj(i, getX(), num, den);
				if (newObj > maxObj && totalU - mp.getU(i) >= mp.getT()) {
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
	public void healSolRatio() {
		while(!this.getValid()) {
			int j = minRatio(0);
			int k = 1;
			while (k < getRSize() && totalU - mp.getU(j) < mp.getT()) {
				j = minRatio(k);
				k++;
			}
			if (totalU - mp.getU(j) >= mp.getT()) {
				removeX(j);
			} else {
				mp.genRndInit(getX(), getR());
				for (Integer i: getX()) {
					setXVals(i,true);
				}
				for (Integer i: getR()) {
					setXVals(i,false);
				}
				setObj(mp.getObj(getX()));
				num = mp.getNum();
				den = mp.getDen();
				calcTotalA();
				calcTotalU();
				updateValid();
			}
		}
	}

	@Override
	public int compareTo(ProblemSol o) 	{
		if (o.getValid() && this.getValid() || !(o.getValid() || this.getValid())) {
			double diff = this.getObj() - o.getObj();
			if (diff > 0) {
				return 1;
			} else if (diff < 0) {
				return -1;
			} else {
				return 0;
			}
		} else {
			if (o.getValid()) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	public void writeSolution(String filename) {
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.write(getObj() + "\n");
			pw.write(num + "\n");
			pw.write(den + "\n");
			pw.write(getTotalA() + "\n");
			pw.write(totalU + "\n");
			Collections.sort(getX());
			for (Integer i: getX()) {
				pw.write(i + " ");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error with Print Writer");
		}
	}

	public void readSolution(String filename) { 
		mp = (MaxProbability)p;
		Scanner scr;
		try {
			scr = new Scanner(new FileInputStream(filename));

			double readObj = scr.nextDouble();
			double readNum = scr.nextDouble();
			double readDen = scr.nextDouble();
			int readTotalA = scr.nextInt();
			int readTotalU = scr.nextInt();
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
				num = readNum;
				den = readDen;
				setTotalA(readTotalA);
				totalU = readTotalU;
			} else {
				System.err.println("NO INCUMBENT SOLUTION IN FILE");
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error finding file: " + filename);
		}
	}

}
