package ExactMethods;
// Cplex imports
import ilog.concert.*;
import ilog.cplex.*;

import java.util.Random;

import Problems.Unconstrained;

/**
 * Standard cubic MIP linearization modified for Unconstrained
 * 
 * @author midkiffj
 */
public class UnconstrainedStd {
	static int n;
	static Unconstrained u;
	static Random rnd;
	static IloCplex cplex;

	static IloNumVar[] x;
	static IloNumVar[][] w;
	static IloNumVar[][][] y;
	
	static boolean exportLPs = true;

	/**
	 * Setup and run MIP
	 * @param args
	 */
	public static void main(String[] args) {
		u = new Unconstrained(5,true,1234,.75);
		try {
			cplex = new IloCplex();
			addModel();
		} catch (IloException e) {
			System.err.println("Error with Cplex");
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Second 'main' method used to run the given problem
	 * 
	 * @param newU - Unconstrained problem to solve
	 */
	public static void run(Unconstrained newU) {
		u = newU;
		try {
			cplex = new IloCplex();
			addModel();
		} catch (IloException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Add standard linearization model to cplex
	 * 
	 * @throws IloException
	 */
	private static void addModel() throws IloException {
		int i,j,k;
		int n = u.getN();

		// Initialize and Name Variables
		String[] xname = new String[n];
		String[][] wname = new String[n][n];
		String[][][] yname = new String[n][n][n];
		for (i = 0; i < n; i++) {
			xname[i] = "x_"+i;
			for (j = 0; j < n; j++) {
				wname[i][j] = "w_"+i+","+j;
				for (k = 0; k < n; k++) {
					yname[i][j][k] = "y_"+i+","+j+","+k;
				}
			}
		}
		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool, xname);
		w = new IloNumVar[n][];
		y = new IloNumVar[n][n][];
		for (i = 0; i < n; i++) {
			w[i] = cplex.numVarArray(n,-1*Double.MAX_VALUE,Double.MAX_VALUE,IloNumVarType.Float,wname[i]);
			for (j = 0; j < n; j++) {
				y[i][j] = cplex.numVarArray(n,-1*Double.MAX_VALUE,Double.MAX_VALUE,IloNumVarType.Float,yname[i][j]);
			}
		}

		// Add Objective
		IloNumExpr obj = cplex.numExpr();
		for (i = 0; i < n; i++) {
			obj = cplex.sum(obj, cplex.prod(u.getCi(i),x[i]));
			for (j = i+1; j < n; j++) {
				obj = cplex.sum(obj, cplex.prod(u.getCij(i,j),w[i][j]));
				for (k = j+1; k < n; k++) {
					obj = cplex.sum(obj, cplex.prod(u.getDijk(i,j,k),y[i][j][k]));
				}
			}
		}
		obj = cplex.sum(obj, u.getC());
		cplex.addMinimize(obj);
		
		cplex.addGe(obj, 0);

		// W_ij constraints
		for (i = 0; i < n; i++) {
			for (j = i+1; j < n; j++) {
				cplex.addLe(w[i][j], x[i]);
				cplex.addLe(w[i][j], x[j]);
				cplex.addGe(cplex.sum(w[i][j], cplex.prod(-1,x[i]),cplex.prod(-1,x[j])),-1);
				cplex.addGe(w[i][j], 0);
			}
		}

		// Y_ijk constraints
		for (i = 0; i < n; i++) {
			for (j = i+1; j < n; j++) {
				for (k = j+1; k < n; k++) {
					cplex.addLe(y[i][j][k], x[i]);
					cplex.addLe(y[i][j][k], x[j]);
					cplex.addLe(y[i][j][k], x[k]);
					cplex.addGe(cplex.sum(y[i][j][k], cplex.prod(-1,x[i]),cplex.prod(-1,x[j]),cplex.prod(-1, x[k])),-2);
					cplex.addGe(y[i][j][k], 0);
				}	
			}
		}

		// Export LP file.
		if (exportLPs) {
			cplex.exportModel("uncStd.lp");
		}
		
		// Solve Model
		cplex.solve();
		double IPOptimal = cplex.getObjValue();

		// Print Integral solution
		System.out.println("Model Status: " + cplex.getCplexStatus());
		System.out.println("IPOptimal: " + IPOptimal);
		prettyPrintInOrder();

		System.exit(0);
	}

	/**
	 * Print the solution x values
	 */
	private static void prettyPrintInOrder() {
		// Pretty Print solution once complete.
		int i;
		// Get x_ij values
		double[] xvals = new double[n];
		try {
			xvals = cplex.getValues(x);
		} catch (IloException e) {
			System.err.println("Error retrieving x values");
		}
		for (i = 0; i < n; i++) {
			System.out.println("x_"+i+": " + xvals[i]);
		}
	}
}
