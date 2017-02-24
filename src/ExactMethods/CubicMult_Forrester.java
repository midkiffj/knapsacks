package ExactMethods;
import java.util.ArrayList;

import Problems.CubicMult;
import Solutions.CubicMultSol;
import ilog.cplex.*;
import ilog.concert.*;

/**
 * Runs the Adams-Forrester MIP on a Cubic Multiple Knapsack problem
 * 
 * @author midkiffj
 *
 */
public class CubicMult_Forrester {
	static CubicMult cm;
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
	static String file;

	/*
	 * Setup and run a MIP on a specific cubic multiple knapsack problem
	 * @param args
	 */
	public static void main(String[] args) {
		// Can get file name as argument
		file = "10_5_0.25_false_0";
		if (args.length == 1) {
			file = args[0];
		}
		cm = new CubicMult("problems/cm/"+file);

		// Approximate Upper and Lower bounds
		computeBounds();
		try {
			cplex = new IloCplex();
			addModel();
		} catch (IloException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	public static long getBestObj() {
		return bestObj;
	}

	/*
	 * Compute approximate bounds for Adams-Forrester linearization
	 * - Bounds are updated to reflect cubic upper-triangular matrix
	 */
	private static void computeBounds() {
		int n = cm.getN();
		Lij = new int[n][n];
		Uij = new int[n][n];
		Li = new int[n];
		Ui = new int[n];

		for (int i = 0; i < n; i++) {
			for (int j = i+1; j < n; j++) {
				if (i != j) {
					for (int k = j+1; k < n; k++) {
						if (k != i && k != j) {
							int dijk = cm.getDijk(i,j,k);
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

		for (int j = 0; j < n; j++) {
			for (int i = 0; i < j; i++) {
				if (i != j) {
					int cij = cm.getCij(i,j);
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

	/*
	 * Add MIP model to cplex and run.
	 * Pretty prints solution values at end.
	 * 
	 * @throws IloException
	 */
	private static void addModel() throws IloException {
		int i,j,k;
		int n = cm.getN();

		// Initialize and Name Variables
		String[] xname = new String[n];
		String[][] tname = new String[n][n];
		String[] fname = new String[n];
		for (i = 0; i < n; i++) {
			xname[i] = "x_"+i;
			fname[i] = "f_"+i;
			tname[i] = new String[n];
			for (j = i+1; j < n; j++) {
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
			obj = cplex.sum(obj, cplex.prod(cm.getCi(i)+Ui[i],x[i]));
			obj = cplex.sum(obj, cplex.prod(-1, psi[i]));
		}
		cplex.addMaximize(obj);

		// Knapsack constraints
		for (j = 0; j < cm.getM(); j++) {
			IloNumExpr knapsack = cplex.numExpr();
			for (i = 0; i < n; i++) {
				knapsack = cplex.sum(knapsack, cplex.prod(cm.getA(j,i), x[i]));
			}
			cplex.addLe(knapsack, cm.getB(j), "knapsack"+j);
		}

		// Tau constraint
		for (i = 0; i < n; i++) {
			for (j = i+1; j < n; j++) {
				if (i != j) {
					IloNumExpr hij = cplex.numExpr();
					for (k = j+1; k < n; k++) {
						if (k != i && k != j) {
							hij = cplex.sum(hij, cplex.prod(cm.getDijk(k, i, j),x[k]));
						}
					}
					IloNumExpr tij = cplex.sum(tau[i][j], cplex.prod(-1*(Uij[i][j] - Lij[i][j]), x[i]), hij);
					cplex.addGe(tij, Lij[i][j]);
				}
			}
		}

		// Psi constraint
		for (j = 0; j < n; j++) {
			IloNumExpr gij = cplex.numExpr();
			IloNumExpr tij = cplex.numExpr();
			for (i = 0; i < j; i++) {
				if (i != j) {
					gij = cplex.sum(gij, cplex.prod(cm.getCij(i, j),x[i]));
					tij = cplex.sum(tij, cplex.prod(Uij[i][j],x[i]), cplex.prod(-1,tau[i][j]));
				}
			}
			IloNumExpr fij = cplex.sum(psi[j], cplex.prod(-1*(Ui[j] - Li[j]), x[j]), gij, tij);
			cplex.addGe(fij, Li[j]);
		}

		// Non-negativity
		for (i = 0; i < n; i++) {
			for (j = i+1; j < n; j++) {
				cplex.addGe(tau[i][j], 0);
				cplex.addGe(tau[j][i], 0);
			}
			cplex.addGe(psi[i], 0);
		}

		// Export LP file.
		if (exportLPs) {
			cplex.exportModel("cubicForrester.lp");
		}

		// Seed MIP with incumbent solution
		CubicMultSol cs = new CubicMultSol("incumbents/cm/"+file+"inc.txt");
		ArrayList<Integer> x = cs.getX();
		seedMIP(x);

		// Solve Model with time limit for bigger problems
		cplex.setParam(IloCplex.DoubleParam.TiLim, 1200);
		cplex.solve();
		if (cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim) {
			System.err.println(file + " Timeout");
		}
		double IPOptimal = cplex.getObjValue();
		bestObj = (long) IPOptimal;

		// Print Integral solution
		System.out.println("Model Status: " + cplex.getCplexStatus());
		System.out.println("IPOptimal: " + IPOptimal);
		prettyPrintInOrder();
	}

	/*
	 * Seed cplex with the given MIP solution
	 * @param initX
	 * @throws IloException
	 */
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

	/*
	 * Print the given xvals
	 */
	private static void prettyPrintInOrder() {
		// Pretty Print solution once complete.
		int i;
		int n = cm.getN();
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
