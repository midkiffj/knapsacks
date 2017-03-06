package ExactMethods;

import java.util.ArrayList;
import java.util.Collections;

import Solutions.ratioNode;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

/**
 * Knapsack MIP used to solve smaller knapsack problems 
 * 	for constructive heuristics and genetic mutations
 * 
 * @author midkiffj
 */
public class Knapsack_Frac {
	private IloCplex cplex;
	private boolean exportLPs = true;

	private IloNumVar[] x;
	
	private int[] a;
	private int[] c;
	private int b;
	private boolean useExact;
	
	private long bestObj;
	private boolean[] xVals;
	private boolean ran;
	
	/**
	 *  Initialize Knapsack
	 * @param a - weights
	 * @param b - knapsack capacity
	 * @param c - objective coefficients
	 * @param useExact - (T) Use cplex or (F) use fractional approximation
	 */
	public Knapsack_Frac(int[] a,  int b, int[] c, boolean useExact) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.useExact = useExact;
		ran = false;
	}
	
	public long getBestObj() {
		if (!ran) {
			try {
				run();
			} catch (IloException e) {
				System.err.println("Failed to run knapsack model");
				System.exit(-1);
			}
		}
		return bestObj;
	}
	
	public boolean[] getXVals() {
		if (!ran) {
			try {
				run();
			} catch (IloException e) {
				System.err.println("Failed to run knapsack model");
				System.exit(-1);
			}
		}
		return xVals;
	}
	
	/**
	 * Set up and run the knapsack MIP
	 * 
	 * @throws IloException
	 */
	private void run() throws IloException {
			if (useExact) {
				runCplex();
			} else {
				runFractional();
			}
	}
	
	/**
	 * Add the knapsack to cplex and solve
	 * 
	 * @throws IloException
	 */
	private void runCplex() throws IloException {
		int i;
		int n = a.length;
		cplex = new IloCplex();

		// Initialize and Name Variables
		String[] xname = new String[n];
		for (i = 0; i < n; i++) {
			xname[i] = "x_"+i;
		}
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool,xname);

		// Add Objective
		IloNumExpr obj = cplex.numExpr();
		for (i = 0; i < n; i++) {
			obj = cplex.sum(obj, cplex.prod(c[i], x[i]));
		}
		cplex.addMaximize(obj);

		// Knapsack constraint
		IloNumExpr knapsack = cplex.numExpr();
		for (i = 0; i < n; i++) {
			knapsack = cplex.sum(knapsack, cplex.prod(a[i], x[i]));
		}
		cplex.addLe(knapsack, b, "knapsack");

		// Export LP file.
		if (exportLPs) {
			cplex.exportModel("knapsack.lp");
		}

		// Solve Model
		cplex.solve();
		double IPOptimal = cplex.getObjValue();
		bestObj = (long) IPOptimal;
		
		// Print the solution x values
		double[] xvals = new double[n];
		xVals = new boolean[n];
		xvals = cplex.getValues(x);
		for (i = 0; i < n; i++) {
			if (xvals[i] == 1) {
				xVals[i] = true;
			}
			System.out.println("x_"+i+": " + xvals[i]);
		}

		// Print Integral solution
		System.out.println("Model Status: " + cplex.getCplexStatus());
		System.out.println("IPOptimal: " + IPOptimal);
		
		ran = true;
	}
	
	/**
	 * Solve the Knapsack by adding max ratio items 
	 *  until a fraction of an item needs to be taken.
	 * All items fully added are considered in the solution
	 */
	private void runFractional() {
		// Calculate ratios for all items
		ArrayList<ratioNode> ratios = new ArrayList<ratioNode>();
		for (int i = 0; i < a.length; i++) {
			ratioNode rn = new ratioNode(i,(double)(c[i])/a[i]);
			ratios.add(rn);
		}
		Collections.sort(ratios);
		
		int totalA = 0;
		bestObj = 0;
		ArrayList<Integer> x = new ArrayList<Integer>();
		xVals = new boolean[a.length];
		int failedI = -1;
		// Add max-ratio items until a fraction of an item has to be taken
		for (int i = 0; i < ratios.size(); i++) {
			int xi = ratios.get(ratios.size()-(i+1)).x;
			if (totalA + a[xi] <= b) {
				xVals[xi] = true;
				x.add(xi);
				bestObj += c[xi];
				totalA += a[xi];
			} else {
				failedI = i;
				i = ratios.size();
			}
		}
		// Calculate objective using fraction of item objective
		if (failedI != -1) {
			int xi = ratios.get(ratios.size()-1-failedI).x;
			bestObj += c[xi]*((double)(b-totalA)/a[xi]);
		}
	}
}
