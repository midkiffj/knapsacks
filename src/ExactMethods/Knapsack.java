package ExactMethods;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

/**
 * Knapsack MIP used to solve smaller knapsack problems 
 * 	for constructive heuristics and genetic mutations
 * @author midkiffj
 *
 */
public class Knapsack {
	private IloCplex cplex;
	private boolean exportLPs = true;

	private IloNumVar[] x;
	
	private int[] a;
	private int[] c;
	private int b;
	
	private long bestObj;
	private boolean[] xVals;
	private boolean ran;
	
	// Initialize Knapsack
	//  c - objective coefficients
	//	a - weights
	// 	b - knapsack capacity
	public Knapsack(int[] a,  int b, int[] c) {
		this.a = a;
		this.b = b;
		this.c = c;
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
	
	/*
	 * Set up and run the knapsack MIP
	 */
	private void run() throws IloException {
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
	
	/*
	 * Example knapsack problem
	 */
	public static void main(String[] args) {
		int[] a = {10, 20, 15};
		int b = 35;
		int[] c = {8, 20, 10};
		Knapsack k = new Knapsack(a,b,c);
		System.out.println(k.getBestObj());
	}
}
