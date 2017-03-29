package ExactMethods;
import java.util.ArrayList;

import Problems.Cubic;
import Solutions.CubicSol;
import ilog.cplex.*;
import ilog.concert.*;

/**
 * Runs the Adams-Forrester MIP on a Cubic problem
 * 
 * @author midkiffj
 *
 */
public class Cubic_Forrester {
	static Cubic c;
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
	static double gap;
	static boolean timeout;
	static String file;

	/**
	 * Setup and run a MIP on a specific cubic problem
	 * 
	 * @param args - can accept file name
	 */
	public static void main(String[] args) {
		// Can get cubic problem as argument
		file = "50_0.25_true_2";
		if (args.length == 1) {
			file = args[0];
		}
		c = new Cubic("problems/cubic/"+file);

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

	public static boolean getTimeout() {
		return timeout;
	}
	
	public static double getGap() {
		return gap;
	}

	/**
	 * Compute approximate bounds for Adams-Forrester linearization
	 * - Bounds are updated to reflect cubic upper-triangular matrix
	 */
	private static void computeBounds() {
		int n = c.getN();
		Lij = new int[n][n];
		Uij = new int[n][n];
		Li = new int[n];
		Ui = new int[n];

		for (int i = 0; i < n; i++) {
			for (int j = i+1; j < n; j++) {
				if (i != j) {
					for (int k = j+1; k < n; k++) {
						if (k != i && k != j) {
							int dijk = c.getDijk(i,j,k);
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
					int cij = c.getCij(i,j);
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

	/**
	 * Add MIP model to cplex and run.
	 * Pretty prints solution values at end.
	 * 
	 * @throws IloException
	 */
	private static void addModel() throws IloException {
		int i,j,k;
		int n = c.getN();

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
			obj = cplex.sum(obj, cplex.prod(c.getCi(i)+Ui[i],x[i]));
			obj = cplex.sum(obj, cplex.prod(-1, psi[i]));
		}
		cplex.addMaximize(obj);

		// Knapsack constraint
		IloNumExpr knapsack = cplex.numExpr();
		for (i = 0; i < n; i++) {
			knapsack = cplex.sum(knapsack, cplex.prod(c.getA(i), x[i]));
		}
		cplex.addLe(knapsack, c.getB(), "knapsack");

		// Tau constraint
		for (i = 0; i < n; i++) {
			for (j = i+1; j < n; j++) {
				if (i != j) {
					IloNumExpr hij = cplex.numExpr();
					for (k = j+1; k < n; k++) {
						if (k != i && k != j) {
							hij = cplex.sum(hij, cplex.prod(c.getDijk(k, i, j),x[k]));
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
					gij = cplex.sum(gij, cplex.prod(c.getCij(i, j),x[i]));
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
		CubicSol inc = new CubicSol("incumbents/cubic/"+file+"inc.txt");
		ArrayList<Integer> incX = inc.getX();
		seedMIP(incX);

		// Solve Model with time limit for bigger problems
		cplex.setParam(IloCplex.DoubleParam.TiLim, 60);
		cplex.solve();
		if (cplex.getCplexStatus() == IloCplex.CplexStatus.AbortTimeLim) {
			System.err.println(file + " Timeout");
			timeout = true;
		}
		double IPOptimal = cplex.getObjValue();
		bestObj = (long) IPOptimal;
		gap = cplex.getMIPRelativeGap();
		System.out.println("Gap: " + gap*100);

		// Print Integral solution
		System.out.println("Model Status: " + cplex.getCplexStatus());
		System.out.println("IPOptimal: " + IPOptimal);
		prettyPrintInOrder();

		// Create solution lists from MIP solution
		double[] xvals = new double[n];
		ArrayList<Integer> solX = new ArrayList<Integer>();
		ArrayList<Integer> solR = new ArrayList<Integer>();
		xvals = cplex.getValues(x);
		for (i = 0; i < n; i++) {
			if (xvals[i] > 1e-05) {
				solX.add(i);
			} else {
				solR.add(i);
			}
		}
		// Check MIP against problem solution and problem objectives
		CubicSol cs = new CubicSol(solX,solR);
		double cObj = c.getObj(solX);
		if (cs.getObj() != bestObj || cs.getObj() != cObj) {
			System.err.println("Different cs obj: " + cs.getObj());
			System.err.println("CObj: " + cObj);
		}
	}

	/**
	 * Seed cplex with the given MIP solution
	 * 
	 * @param initX - solution list to seed to cplex
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

	/**
	 * Print the solution x values from cplex
	 */
	private static void prettyPrintInOrder() {
		// Pretty Print solution once complete.
		int i;
		int n = c.getN();
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
