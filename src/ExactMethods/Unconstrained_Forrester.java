package ExactMethods;

import Problems.Unconstrained;
import ilog.cplex.*;
import ilog.concert.*;

/**
 * Modified Adams-Forrester Cubic knapsack linearization for the Unconstrained Cubic
 * 
 * @author midkiffj
 */
public class Unconstrained_Forrester {
	static Unconstrained u;
	static IloCplex cplex;
	static boolean exportLPs = true;

	static IloNumVar[] x;
	static IloNumVar[][] tau;
	static IloNumVar[] psi;

	static int[][] Lij;
	static int[][] Uij;
	static int[] Li;
	static int[] Ui;

	static long bestObj;

	/*
	 * Setup and run Unconstrained MIP
	 * @param args
	 */
	public static void main(String[] args) {
		// Can get file as argument
		String file = "10_0.25_false_0";
		if (args.length == 1) {
			file = args[0];
		}
		u = new Unconstrained("problems/unc/"+file);
		// Computer bounds
		computeBounds();
		try {
			// Add and run model
			cplex = new IloCplex();
			addModel();
		} catch (IloException e) {
			System.err.println("Error with Cplex");
			System.err.println(e.getMessage());
		}
	}

	public static long getBestObj() {
		return bestObj;
	}

	/*
	 * Second 'main' method to run the given problem
	 */
	public static void run(Unconstrained newU) {
		u = newU;
		computeBounds();
		try {
			cplex = new IloCplex();
			addModel();
		} catch (IloException e) {
			System.err.println("Error with Cplex");
			System.err.println(e.getMessage());
		}
	}

	/*
	 * Computer approximate bounds for forrester linearization
	 */
	private static void computeBounds() {
		int n = u.getN();
		Lij = new int[n][n];
		Uij = new int[n][n];
		Li = new int[n];
		Ui = new int[n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					for (int k = 0; k < n; k++) {
						if (k != i && k != j) {
							if (k < i && i < j) {
								int dijk = -1*u.getDijk(k,i,j);
								if (dijk < 0) {
									Lij[i][j] += dijk;
								} else {
									Uij[i][j] += dijk;
								}
							}
						}
					}
				}
			}
		}

		for (int j = 0; j < n; j++) {
			for (int i = 0; i < n; i++) {
				if (i != j) {
					if (i < j) {
						int cij = -1*u.getCij(i,j);
						if (cij + Lij[i][j] < 0) {
							Li[j] += cij + Lij[i][j];
						} 
						if (cij + Uij[i][j] > 0) {
							Ui[j] += cij + Uij[i][j];
						}
					}
				}
			}
		}
	}

	/*
	 * Add Forrester Linearization model to cplex and run.
	 * Pretty prints solution values at end.
	 * 
	 * @throws IloException
	 */
	private static void addModel() throws IloException {
		int i,j,k;
		int n = u.getN();

		// Initialize and Name Variables
		String[] xname = new String[n];
		String[][] tname = new String[n][n];
		String[] fname = new String[n];
		for (i = 0; i < n; i++) {
			xname[i] = "x_"+i;
			fname[i] = "f_"+i;
			tname[i] = new String[n];
			for (j = 0; j < n; j++) {
				tname[i][j] = "t_"+i+","+j;
			}
		}

		x = cplex.numVarArray(n, 0, 1, IloNumVarType.Bool);
		psi = cplex.numVarArray(n,-1*Double.MAX_VALUE,Double.MAX_VALUE, IloNumVarType.Float);
		tau = new IloNumVar[n][];
		for (i = 0; i < n; i++) {
			tau[i] = cplex.numVarArray(n,-1*Double.MAX_VALUE,Double.MAX_VALUE,IloNumVarType.Float,tname[i]);
		}

		// Add Objective
		IloNumExpr obj = cplex.numExpr();
		for (i = 0; i < n; i++) {
			obj = cplex.sum(obj, cplex.prod(-1*u.getCi(i)+Ui[i],x[i]));
			obj = cplex.sum(obj, cplex.prod(-1, psi[i]));
		}
		obj = cplex.sum(obj, -1*u.getC());
		cplex.addMaximize(obj);

		// Tau constraint
		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++) {
				if (i != j) {
					IloNumExpr hij = cplex.numExpr();
					for (k = 0; k < n; k++) {
						if (k != i && k != j) {
							if (k < i && i < j) {
								hij = cplex.sum(hij, cplex.prod(-1*u.getDijk(k, i, j),x[k]));
							}
						}
					}
					IloNumExpr tij = cplex.sum(tau[i][j], cplex.prod(-1*(Uij[i][j] - Lij[i][j]), x[i]), hij);
					cplex.addGe(tij, Lij[i][j]);
				}
			}
		} 

		// psi constraint
		for (j = 0; j < n; j++) {
			IloNumExpr gij = cplex.numExpr();
			IloNumExpr tij = cplex.numExpr();
			for (i = 0; i < n; i++) {
				if (i != j) {
					if (i < j) {
						gij = cplex.sum(gij, cplex.prod(-1*u.getCij(i, j),x[i]));
					}
					tij = cplex.sum(tij, cplex.prod(Uij[i][j],x[i]), cplex.prod(-1,tau[i][j]));
				}
			}
			IloNumExpr fij = cplex.sum(psi[j], cplex.prod(-1*(Ui[j] - Li[j]), x[j]), gij, tij);
			cplex.addGe(fij, Li[j]);
		}

		// Non negativity
		for (i = 0; i < n; i++) {
			for (j = i+1; j < n; j++) {
				cplex.addGe(tau[i][j], 0);
				cplex.addGe(tau[j][i], 0);
			}
			cplex.addGe(psi[i], 0);
		}

		// Export LP file.
		if (exportLPs) {
			cplex.exportModel("uncForrester.lp");
		}

		// Solve Model
		cplex.solve();
		double IPOptimal = cplex.getObjValue();
		bestObj = (long) IPOptimal;

		// Print Integral solution
		System.out.println("Model Status: " + cplex.getCplexStatus());
		System.out.println("IPOptimal: " + IPOptimal);
		prettyPrintInOrder();
	}

	/*
	 * Print the solution x-values
	 */
	private static void prettyPrintInOrder() {
		// Pretty Print solution once complete.
		int i;
		int n = u.getN();
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
