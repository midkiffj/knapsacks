package ExactMethods;
// Cplex imports
import ilog.concert.*;
import ilog.cplex.*;

import java.util.ArrayList;
import java.util.Random;

import Problems.Cubic;
import Problems.Unconstrained;


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
	 * Setup and run a tabu search algorithm on a cubic knapsack problem
	 * @param args
	 */
	public static void main(String[] args) {
		u = new Unconstrained(5,true);
		try {
			cplex = new IloCplex();
			addModel();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void run(Unconstrained newU) {
		u = newU;
		try {
			cplex = new IloCplex();
			addModel();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static private void seedMIP(ArrayList<Integer> initX) throws IloException {
		// New solution to be passed in to MIP.
		IloNumVar[] iniX = cplex.numVarArray(initX.size(),0,1,IloNumVarType.Bool);
		double[] values = new double[initX.size()];
		for (int i = 0; i < initX.size(); i++) {
			int xi = initX.get(i);
			System.out.println("x_"+xi);
			iniX[i] = x[xi];
			values[i] = 1;
		}
		cplex.addMIPStart(iniX,values,"initSol");
	}

	private static void addModel() throws IloException {
		int i,j,k;
		int n = u.getN();

		// Initialize and Name Variables
		String[] xname = new String[n];
		String[][] wname = new String[n][n];
		String[][][] yname = new String[n][n][n];
		for (i = 0; i < n; i++) {
			xname[i] = "x_"+i;
			//			wname[i] = new String[n];
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
//			cplex.addEq(w[i][i], 0);
			for (j = i+1; j < n; j++) {
				cplex.addLe(w[i][j], x[i]);
				cplex.addLe(w[i][j], x[j]);
				cplex.addGe(cplex.sum(w[i][j], cplex.prod(-1,x[i]),cplex.prod(-1,x[j])),-1);
				cplex.addGe(w[i][j], 0);
//				cplex.addEq(w[i][j],w[j][i]);
			}
		}

		// Y_ijk constraints
		for (i = 0; i < n; i++) {
			//			cplex.addEq(w[i][i], 0);
			for (j = i+1; j < n; j++) {
				for (k = j+1; k < n; k++) {
					cplex.addLe(y[i][j][k], x[i]);
					cplex.addLe(y[i][j][k], x[j]);
					cplex.addLe(y[i][j][k], x[k]);
					cplex.addGe(cplex.sum(y[i][j][k], cplex.prod(-1,x[i]),cplex.prod(-1,x[j]),cplex.prod(-1, x[k])),-2);
					cplex.addGe(y[i][j][k], 0);
//										cplex.addEq(y[i][k][j], y[i][j][k]);
//										cplex.addEq(y[j][k][i], y[i][j][k]);
//										cplex.addEq(y[j][i][k], y[i][j][k]);
//										cplex.addEq(y[k][i][j], y[i][j][k]);
//										cplex.addEq(y[k][j][i], y[i][j][k]);
				}	
			}
		}

		// Export LP file.
		if (exportLPs) {
			cplex.exportModel("uncStd.lp");
		}

//		ArrayList<Integer> x = new ArrayList<Integer>();
//		u.genInit(x, new ArrayList<Integer>());
//		seedMIP(x);
		
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
	 * Print the given xvals, wvals, and yvals
	 */
	private static void prettyPrintInOrder() {
		// Pretty Print solution once complete.
		int i, j, k;
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
//		// Get w_ij values
//		double[][] wvals = new double[n][n];
//		for (i = 0; i < n; i++) {
//			for (j = i+1; j < n; j++) {
//				try {
//					wvals[i][j] = cplex.getValue(w[i][j]);
//				} catch (IloException e) {
//					System.err.println("Error retrieving w values " + i + "," + j);
//				}
//			}
//		}
//		for (i = 0; i < n; i++) {
//			for (j = i+1; j < n; j++) {
//				System.out.println("w_"+i+","+j+": " + wvals[i][j]);
//			}
//		}
//		// Get y_ijk values
//		double[][][] yvals = new double[n][n][n];
//		for (i = 0; i < n; i++) {
//			for (j = i+1; j < n; j++) {
//				for (k = j+1; k < n; k++) {
//					try {
//						yvals[i][j][k] = cplex.getValue(y[i][j][k]);
//					} catch (IloException e) {
//						System.err.println("Error retrieving w values " + i + "," + j);
//					}
//				}
//			}
//		}
//		for (i = 0; i < n; i++) {
//			for (j = i+1; j < n; j++) {
//				for (k = j+1; k < n; k++) {
//					System.out.println("y_"+i+","+j+","+k+": " + yvals[i][j][k]);
//				}
//			}
//		}
	}
}
